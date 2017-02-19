package com.verint.textanalytics.common.diagnostics;

/**
 * Interface to report performance metrics.
 * @author EZlotnik
 *
 */
public interface PerformanceReporter {

	/** Report operation executing time.
	 * @param operationType operation type : Search Interactions ...
	 * @param executionMillisecs execution time in milliseconds.
	 */
	void reportOperationLastExecutionTime(OperationType operationType, long executionMillisecs);
}
