package com.verint.textanalytics.common.utils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.logging.log4j.*;

/**
 * ThreadUtils class.
 * @author EZlotnik
 *
 */
public class ThreadUtils {
	private static Logger s_logger;

	static {
		s_logger = LogManager.getLogger(ThreadUtils.class);
	}

	private ThreadUtils() {

	}

	/**
	 * Disposed threads in thread pool.
	 * @param threadPool
	 *            thread pool
	 */
	public static void shutdownExecutionThreadPool(ExecutorService threadPool) {
		if (threadPool != null) {
			List<Runnable> awaitingExecutionThreads = threadPool.shutdownNow();
			if (awaitingExecutionThreads != null && awaitingExecutionThreads.size() > 0) {
				s_logger.debug("Executor was abruptly shut down. {} tasks will not be executed.", awaitingExecutionThreads.size());

			}
		}
	}
}
