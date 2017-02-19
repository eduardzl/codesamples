package com.verint.textanalytics.common.diagnostics;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.*;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.utils.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

/**
 * Created by EZlotnik on 1/18/2016.
 */
public class PerformanceMetrics implements DisposableBean {
	private MetricRegistry registry;
	private PickledGraphite pickledGraphite;

	private GraphiteReporter graphiteReporter;
	private Slf4jReporter  slf4jReporter;

	private boolean isInitialized = false;

	/**
	 * Constructor.
	 * @param configurationManager condfiguration manager
	 */
	public PerformanceMetrics(ConfigurationManager configurationManager) {
		ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();
		if (!StringUtils.isNullOrBlank(appConfig.getGraphiteServer())) {

			this.registry = new MetricRegistry();

			this.pickledGraphite = new PickledGraphite(new InetSocketAddress(appConfig.getGraphiteServer(), appConfig.getGraphitePort()), appConfig.getGraphiteReporterBatchSize());
			this.graphiteReporter = GraphiteReporter.forRegistry(registry)
		                                        .prefixedWith(appConfig.getGraphiteReporterPrefix())
		                                        .convertRatesTo(TimeUnit.SECONDS)
		                                        .convertDurationsTo(TimeUnit.MILLISECONDS)
		                                        .filter(MetricFilter.ALL)
		                                        .build(pickledGraphite);

			graphiteReporter.start(appConfig.getGraphiteReporterIntervalSeconds(), TimeUnit.SECONDS);

			this.isInitialized = true;
		}
	}

	/**
	 * Start timed operation.
	 * @param operationType operation type
	 * @return operation context
	 */
	public Timer.Context startTimedOperation(OperationType operationType) {
		if (this.isInitialized) {
			Timer timer = this.registry.timer(MetricRegistry.name("", operationType.toString()));
			Timer.Context context = timer.time();

			return context;
		} else {
			return null;
		}
	}

	/**
	 * Stops timed operation.
	 * @param context operation context
	 * @return milliseconds of the timer
	 */
	public long stopTimedOperation(Timer.Context context) {
		long nanoseconds = 0, milliseconds = 0;

		if (this.isInitialized) {
			nanoseconds = context.stop();
			milliseconds = TimeUnit.MILLISECONDS.convert(nanoseconds, TimeUnit.NANOSECONDS);
		}

		return milliseconds;
	}

	@Override
	public void destroy() throws Exception {
		try {
			if (this.graphiteReporter != null) {
				this.graphiteReporter.stop();
			}

			if (this.pickledGraphite != null) {
				this.pickledGraphite.close();
			}
		} catch (Exception ex) {

		}
	}
}
