package com.verint.textanalytics.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by yzanis on 08-May-16.
 */
public final class IDGeneratorUtils {

	private static Logger s_logger;

	private final static int channelAndTenantMultiplier = 1000;
	private final static int categoryMultiplier = 100000;

	//we will add 1Mil in oreder tenant and channel id be diffrent from speech instance id
	private final static int channelAndTenantAdding = 1000000;
	private final static int categoryAdding = 500000;


	static {
		s_logger = LogManager.getLogger(JSONUtils.class.getName());
	}

	private IDGeneratorUtils() {

	}



	/***
	 * @param dataSourceID The ID of the datasurce representing the channel
	 * @param sis3 The ID of the EM , sis3 in server xml
	 * @return calculated channel id
	 */
	public static int generateChannelID(int dataSourceID, String sis3) {
		int sysID = Integer.parseInt(sis3);
		return ((dataSourceID * channelAndTenantMultiplier + sysID) + channelAndTenantAdding);
	}

	/***
	 * @param dataSourceID The ID of the datasurce representing the channel
	 * @param sis3 The ID of the EM , sis3 in server xml
	 * @return calculated tenant id
	 */
	public static int generateTenantID(int dataSourceID, String sis3) {
		int sysID = Integer.parseInt(sis3);
		return ((dataSourceID * channelAndTenantMultiplier + sysID) + channelAndTenantAdding);

	}

	/***
	 * @param channelName    The name of the channel to get channel data source id from
	 * @param nextCategoryID the current max id for category
	 * @return calculated category id
	 */
	public static int generateCategoryID(String channelName, int nextCategoryID) {
		int dataSourceId = getDataSourceIDFromName(channelName);
		return (nextCategoryID * categoryMultiplier + dataSourceId) + categoryAdding;
	}

	/***
	 * @param dataSourceName The name of the channel or Tenant to get data source id from
	 * @return the ID of the data source
	 */
	public static int getDataSourceIDFromName(String dataSourceName) {

		if (dataSourceName.matches("\\d+")) {
			int dsName = Integer.parseInt(dataSourceName);
			dsName = dsName - channelAndTenantAdding;
			return (int) ((double) dsName / (double) channelAndTenantMultiplier);
		}
		return 0;
		//int dataSourceID = Integer.parseInt() return 0;
	}

}
