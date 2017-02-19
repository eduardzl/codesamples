package com.verint.textanalytics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    private String filePath = "C:\\temp\\test.txt";

    private Logger logger = LogManager.getLogger(this.getClass());
    private int timeout = 10;

    private List<String> shared1 = new ArrayList<>();
    private Object sharedLock = new Object();

    private Lock rwLock = new ReentrantLock();

    private Semaphore semaphore = new Semaphore(1);
    private AtomicInteger counter = new AtomicInteger();

    public static void main( String[] args ) {
        try {
            App app = new App();
            app.parseJson();

        } catch (Exception ex) {

        }
    }

    public void parseJson() {
        String json = "";
        try {
            List<Category> categories = new ArrayList<>();

            InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("categories.json");
            json = IOUtils.toString(fileStream);

            if (json != null && !"".equals(json)) {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(json);

                JsonNode categoriesNode = rootNode.path("categories");
                if (!categoriesNode.isMissingNode() && categoriesNode.isArray()) {

                    for (JsonNode category : categoriesNode) {
                        logger.debug("Category name {}", category.path("name").asText(""));

                        categories.add(new Category(category.path("name").asText(""), category.path("id").asInt()));
                    }
                }
            }

            Set<Integer> ids = new HashSet<>();
            if (categories != null) {
                categories.stream()
                          .forEach(c -> {
                              if (!ids.contains(c.getId())) {
                                  ids.add(c.getId());
                              }
                          });
            }

            logger.debug("Categories ids count {}", ids.size());

            long numOfNonEmpty = categories.stream()
                                            .filter(c -> !c.getName().equals(""))
                                            .count();

            List<String> namesWithIds =  categories.stream()
                                                    .map(c -> c.getName() + c.getId())
                                                    .collect(Collectors.toList());



        } catch (Exception ex) {
            logger.error("Failed to load json file", ex);
        }
    }

    public void readFile() {
        BufferedReader buffRd = null;

        try {
            if (filePath != null && !"".equals(filePath)) {
                buffRd = new BufferedReader(new FileReader(filePath));

                String line = null;
                while ((line = buffRd.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Exception while reading file");
        } finally {
            try {
                if (buffRd != null) {
                    buffRd.close();
                }
            } catch (Exception e) {}
        }
    }

    public void readFileWithApacheCommons() {
        try {
            if (this.filePath != null && !"".equals(this.filePath)) {
                List<String> lines = IOUtils.readLines(new FileReader(this.filePath));

                if (lines != null) {
                    lines.stream()
                         .forEach(a ->
                            System.out.println(a)
                    );
                }
            }
        } catch (Exception ex) {
            System.err.println("Exception while reading file");
        }
    }


    public void startCalculations() {

        Thread thread1 = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    logger.debug("Thread {} running {}", Thread.currentThread().getName(), i);
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    logger.debug("Thread {} running {}", Thread.currentThread().getName(), i);
                }
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (Exception ex) {
            logger.debug("Exception waiting for threads to complere");
        } finally {
            logger.debug("Ruuning thread 1 and thread 2 completed");
        }
    }

    public void startCalculationsWithExecutorService() {
        ExecutorService threadPool =  Executors.newFixedThreadPool(2);

        Callable<Object> task1 = () -> {

            synchronized (this.sharedLock) {
                for (int i = 0; i < 10; i++) {
                    this.shared1.add(String.valueOf(i));
                }
            }

            return new Object();
        };

        Callable<Object> task2 = () -> {
            synchronized (this.sharedLock) {
                for (int i = 10; i < 20; i++) {
                    this.shared1.add(String.valueOf(i));
                }
            }

            return new Object();
        };

        Callable<Object> task3 = () -> {
            boolean isLocked = rwLock.tryLock(timeout, TimeUnit.SECONDS);
            if (isLocked) {
                for (int i = 0; i < 10; i++) {
                    this.shared1.add(String.valueOf(i));
                }

                rwLock.unlock();
            }

            return new Object();
        };

        Callable<Object> task4 = () -> {
            boolean isLocked = rwLock.tryLock(timeout, TimeUnit.SECONDS);
            if (isLocked) {
                for (int i = 20; i < 30; i++) {
                    this.shared1.add(String.valueOf(i));
                }
                rwLock.unlock();
            }


            return new Object();
        };

        Callable<Object> task5 = () -> {
            boolean isAcquired = false;

            try {
                isAcquired = semaphore.tryAcquire(1);
                if (isAcquired) {
                    for (int i = 0; i < 10; i++) {
                        this.shared1.add(String.valueOf(i));
                    }
                }
            } catch (Exception ex) {

            } finally {
                if (isAcquired) {
                    semaphore.release(1);
                }
            }

            return new Object();
        };

        Callable<Object> task6 = () -> {
            for (int j = 0; j < 10; j++) {
                counter.getAndIncrement();
            }

            return new Object();
        };

        List<Callable<Object>> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);
        tasks.add(task4);
        tasks.add(task5);
        tasks.add(task6);

        try {
            List<Future<Object>> futures = threadPool.invokeAll(tasks, timeout, TimeUnit.SECONDS);

            if (futures != null) {
                for (Future<Object> future : futures) {
                    if (future.isDone()) {
                        Object result = future.get();
                        if (result != null) {
                            logger.debug("Result {}", result.toString());
                        }
                    }
                }

                logger.debug("Shared {}", this.shared1.toString());
            }

        } catch (Exception ex) {
            logger.debug("Exception thrown", ex);
        } finally {
             threadPool.shutdown();
        }

    }
}
