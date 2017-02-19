package com.verint.textanalytics.common.logger;

import static com.verint.textanalytics.common.constants.TAConstants.Environment.IMPACT360_SOFTWARE_DIR;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;

import org.glassfish.jersey.client.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.utils.StringUtils;

/**
 * HttpResource manager resource manager for log4net Verint logger appender.
 * 
 * @author EZlotnik
 *
 */
public class HttpResourceManager extends AbstractManager {

	private HttpPost poster;

	private EmbeddedDb db;

	private String dirName;

	private String httpEndpointUrl;

	private String proxyHost;

	private int proxyPort;

	private int batchSize;

	private final int messageSizeLimit = 5200;
	private final int posterShutdownTimeout = 100;

	private Object waitLock = new Object();

	private String formUrlEncoded = "application/x-www-form-urlencoded";
	private final String utf8Encoding = "UTF-8";

	/**
	 * Constructor.
	 * @param name
	 *            appender name
	 * @param endpointUrl
	 *            request url
	 * @param dirName
	 *            directory for temporary storage
	 * @param batchSize
	 *            batch size for uploading events
	 */
	public HttpResourceManager(String name, String endpointUrl, String dirName, int batchSize) {
		super(name);

		this.httpEndpointUrl = endpointUrl;
		this.dirName = dirName;
		this.batchSize = batchSize;

		this.activateOptions();
	}

	/**
	 * Creates embedded database instance and working thread.
	 */
	public void activateOptions() {

		if (dirName == null) {
			LOGGER.warn("directory for log queue was not set.  Please set the \"dirName\" property");
		}

		if (this.httpEndpointUrl == null) {
			LOGGER.warn("Log http endpoint url for log was not set.  Please set the \"httpEndpointUrl\" property");
		}

		Map<String, String> env = System.getenv();

		String dbFilePath = "";
		String impact360DataDirVarValue = env.get(TAConstants.Environment.IMPACT360_DATA_DIR);
		if (!StringUtils.isNullOrBlank(impact360DataDirVarValue)) {
			impact360DataDirVarValue = StringUtils.trimEnd(impact360DataDirVarValue, File.separator);
			dbFilePath = Paths.get(impact360DataDirVarValue, dirName).toAbsolutePath().toString();
		}

		LOGGER.debug("Creating database in " + dbFilePath + " with name " + getName());
		db = new EmbeddedDb(dirName, getName(), LOGGER);

		poster = new HttpPost();

		Thread posterThread = new Thread(poster);
		posterThread.start();
	}

	/**
	 * static method to get instance.
	 * 
	 * @param name
	 *            name
	 * @param httpEndpointUrl
	 *            - http endpoint url
	 * @param dirName
	 *            directory for storage
	 * @param batchSize
	 *            batch size
	 * @return resource manager
	 */
	public static HttpResourceManager getHttpResourceManager(String name, String httpEndpointUrl, String dirName, int batchSize) {
		LOGGER.debug("Allocating HttpResourceManager for HttpAppender");

		return new HttpResourceManager(name, httpEndpointUrl, dirName, batchSize);
	}

	/**
	 * Sends event.
	 * @param layout
	 *            layout
	 * @param event
	 *            event.
	 */
	public void sendEvent(final Layout<?> layout, final LogEvent event) {
		assert layout != null : "Cannot log, there is no layout configured.";

		byte[] eventBytes = layout.toByteArray(event);

		/**
		 * We always only produce to the current file. So there's no need for
		 * locking
		 */

		String output = new String(eventBytes, StandardCharsets.UTF_8);

		synchronized (waitLock) {
			db.writeEntry(output, System.nanoTime());
			waitLock.notify();
		}

		if (poster.getState() == ThreadState.STOPPED) {
			LOGGER.debug("Noticed thread stopped!");
		}
	}

	@Override
	public void releaseSub() {
		try {
			LOGGER.debug("Stopping HttpResourceManager");
			if (poster != null) {
				LOGGER.debug("Stopping Http poster");
				// Stop is a blocking call, it waits for HttpPost to finish.
				poster.stop();
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to shutdown Http poster", ex);
		}

		try {
			if (db != null) {
				LOGGER.debug("Shutting down Embedded Db");
				// now shutdown the database
				db.shutdown();
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to shutdown Embedded Db", ex);
		}
	}

	/**
	 * Thread status enum.
	 * @author EZlotnik
	 *
	 */
	private enum ThreadState {
		START, RUNNING, STOP_REQUESTED, STOPPED
	};

	/**
	 * Server to post log messages to http endpoint.
	 * @author EZlotnik
	 *
	 */
	private class HttpPost implements Runnable {
		// State variables needs to be volatile, otherwise it can be cached local to the thread and stop() will never work
		private volatile ThreadState curState = ThreadState.START;
		private volatile ThreadState requestedState = ThreadState.RUNNING;

		private final int sleepTimeout = 1000;

		private final Object stopLock = new Object();

		private Client client;

		/**
		 * Constructor.
		 */
		public HttpPost() {

		}

		public void run() {

			curState = ThreadState.RUNNING;
			LOGGER.debug("Loggly: background thread waiting for db");

			boolean initialized = waitUntilDbInitialized();
			if (initialized) {
				LOGGER.debug("Loggly: background thread starting");

				List<Entry> messages = db.getNext(batchSize);

				// ThreadState.STOP_REQUESTED lets us keep running until our queue is empty, but stop when it is
				while (curState == ThreadState.RUNNING || (curState == ThreadState.STOP_REQUESTED && messages != null && messages.size() > 0)) {

					if (curState == ThreadState.STOP_REQUESTED) {
						LOGGER.warn("Loggly: Stop requested, emptying queue of: " + messages.size());
					}

					if (messages == null || messages.size() == 0) {

						// We aren't synchronized around the database, because that doesn't matter
						// this synchronization block just lets us be notified sooner if a new message comes it
						synchronized (waitLock) {
							try {
								// nothing to consume, sleep for 1 second
								waitLock.wait(sleepTimeout);
							} catch (InterruptedException e) {
								if (curState == ThreadState.STOP_REQUESTED) {
									// no-op, we are shutting down
									assert true;
								} else {
									// an error
									LOGGER.error("Unable to sleep for 1 second in queue consumer", e, 1);
								}
							}
						}

					} else {

						try {
							int response = sendData(messages);
							switch (response) {
								case TAConstants.httpCode200:
								case TAConstants.httpCode201:
									db.deleteEntries(messages);
									break;
								case TAConstants.httpCode400:
									LOGGER.warn("loggly: bad request dumping message");
									db.deleteEntries(messages);
									break;
								default:
									LOGGER.error("Received error code " + response + " from Loggly servers.");
									break;
							}
						} catch (IOException e) {
							LOGGER.error(String.format("Unable to send data to log http endpoint at URL %s", httpEndpointUrl), e, 2);
						}
					}

					// The order of these two if statements (and the else) is very important
					// If the order was reversed, we would drop straight from RUNNING to STOPPED without one last 'cleanup' pass.
					// If the else was missing, we would permently be stuck in the STOP_REQUESTED state.
					if (curState == ThreadState.STOP_REQUESTED) {
						curState = ThreadState.STOPPED;
					} else if (requestedState == ThreadState.STOPPED) {
						curState = ThreadState.STOP_REQUESTED;
					}

					messages = db.getNext(batchSize);

				}

				LOGGER.debug("Loggly background thread is stopped.");
			} else {
				LOGGER.warn("Loggly bailing out because we were interrupted while waiting to initialize");
				curState = ThreadState.STOPPED;
			}

			synchronized (stopLock) {
				stopLock.notify();
			}
		}

		/**
		 * @return
		 */
		public ThreadState getState() {
			return curState;
		}

		/**
		 * Waits until the db is initialized, or stop has been requested.
		 * 
		 * @return
		 */
		public boolean waitUntilDbInitialized() {

			synchronized (db.initializeLock()) {
				while (!db.isInitialized() && requestedState != ThreadState.STOPPED) {
					try {
						db.initializeLock().wait();
					} catch (InterruptedException e) {
						LOGGER.error("HttpAppender interrupted waiting for db initalization", e);
					}
				}
			}

			// if this returns false, we should abort, because it means we were interrupted
			// after shutdown was requested.
			return db.isInitialized();
		}

		/**
		 * Send the data via http post.
		 *
		 * @param messages messages to send to remove endpoint
		 * @throws IOException
		 */
		private int sendData(List<Entry> messages) throws IOException {
			URL url = new URL(httpEndpointUrl);
			Proxy proxy = Proxy.NO_PROXY;
			if (proxyHost != null) {
				SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
				proxy = new Proxy(Proxy.Type.HTTP, addr);
			}

			URLConnection conn = url.openConnection(proxy);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);

			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			//conn.setRequestProperty("Content-Type", "text/plain");

			OutputStream os = conn.getOutputStream();
			String messageString;

			for (Entry message : messages) {
				messageString = message.getMessage();
				final byte[] msgBytes = messageString.getBytes(utf8Encoding);
				if (msgBytes.length < messageSizeLimit) {
					conn.getOutputStream().write(msgBytes);
				} else {
					LOGGER.warn("message to large for loggly - dropping msg:\n" + msgBytes);
				}
			}

			os.flush();
			os.close();
			HttpURLConnection huc = ((HttpURLConnection) conn);
			int respCode = huc.getResponseCode();
			// grabbed from http://download.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html
			BufferedReader in = null;
			StringBuffer response = null;
			try {
				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), utf8Encoding));
				response = new StringBuffer();
				int value = -1;
				while ((value = in.read()) != -1) {
					response.append((char) value);
				}
				in.close();
			} catch (IOException e) {
				try {
					response = new StringBuffer();
					response.append("Status: ").append(respCode).append(" body: ");
					in = new BufferedReader(new InputStreamReader(huc.getErrorStream(), utf8Encoding));
					int value = -1;
					while ((value = in.read()) != -1) {
						response.append((char) value);
					}
					in.close();

					LOGGER.error(String.format("Unable to send data to loggly at URL %s Response %s", httpEndpointUrl, response));
				} catch (IOException ee) {
					LOGGER.error(String.format("Unable to send data to loggly at URL %s", httpEndpointUrl), e, 2);
				}
			}
			return respCode;
		}

		/**
		 * Stop this thread sending data and write the last read position.
		 */

		public void stop() {
			LOGGER.debug("Stopping background thread");
			requestedState = ThreadState.STOPPED;

			// Poke the thread to shut it down.
			synchronized (waitLock) {
				LOGGER.debug("Loggly: Waking background thread up");
				waitLock.notify();
			}

			synchronized (poster.stopLock) {
				LOGGER.debug("Loggly: Waiting for background thread to stop");
				while (poster.curState != ThreadState.STOPPED) {
					try {
						poster.stopLock.wait(posterShutdownTimeout);
					} catch (InterruptedException e) {
						LOGGER.error("Interrupted while waiting for Http thread to stop, bailing out.");
					}
				}
			}
		}
	}
}
