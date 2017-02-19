package com.verint.textanalytics.bl.applicationservices;

import org.springframework.beans.factory.annotation.Autowired;

import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;

/***
 * 
 * @author yzanis
 *
 */
public class DataSourcesManagmentService {

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	/***
	 * 
	 * @param tenant
	 *            the name of the tenant the check
	 * @return if the tenent exsists and if not if the tenant was created. or
	 *         false in case of an error.
	 */
	public boolean isTenantExists(String tenant) {
		return this.textAnalyticsProvider.createTenantIfNotExists(tenant);
	}

	/***
	 * 
	 * @param tenant
	 *            the name of the tenant the check
	 * @return this deletes the tenant and the tenant data, return true if was
	 *         deleted false if the tenant does not exists or error in any other
	 *         case.
	 */
	public boolean deleteTenantAndTenantData(String tenant) {
		return this.textAnalyticsProvider.deleteTenantAndTenantData(tenant);
	}

}