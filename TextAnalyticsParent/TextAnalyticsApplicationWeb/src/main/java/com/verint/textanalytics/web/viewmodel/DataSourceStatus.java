package com.verint.textanalytics.web.viewmodel;

/***
 * 
 * @author yzanis
 *
 */
public enum DataSourceStatus {

	// @formatter:off
	ExistOnlyInEM(0),
	ExistsOnlyInSolr(1),
	NoDataInSolr(2),
	configured(3);
	// @formatter:on

	private final int dataSourceStatusCode;

	private DataSourceStatus(int statusCode) {
		this.dataSourceStatusCode = statusCode;
	}

}