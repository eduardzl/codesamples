package com.verint.textanalytics.bl.security;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.exceptions.FoundationServicesExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.FoundationServicesExecutionException;
import com.verint.textanalytics.common.utils.IDGeneratorUtils;
import com.verint.textanalytics.common.utils.XmlUtils;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.Tenant;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.verint.textanalytics.common.constants.TAConstants.Environment.IMPACT360_SOFTWARE_DIR;

/**
 * @author yzanis
 *
 */
public class EMConfigurationManager {


	// This will hold the last good tenant list to return in case of an error
	private static List<Tenant> s_lastGoodTenantList = null;
	private DateTime backupTime;

	@Autowired
	private ConfigurationManager configurationManager;

	private final String channelType = "TextProject";
	private final String tenantType = "TextStore";
	private final String nodeName = "dat:DataSource";
	private final String chaneelsNodeNameInTenantXML = "dat:AssociatedDataSource";
	private final String idAttributName = "Identifier";
	private final String nameAttributName = "Name";
	private final String mainTypeAttributName = "Type";
	private final String mainTypeAttributValue = "TextAnalytics";
	private final String subTypeAttributName = "SubType";
	private final int channelAndTenantMultiplier = 1000;

	private Logger logger = LogManager.getLogger(this.getClass());

	/***
	 * this function will create the list of tenants and for each tenant the
	 * list of channels that available in the EM.
	 * 
	 * @return list of tenants in the system.
	 */
	@Cacheable(value = "TextAnalyticsLongTermCache")
	public List<Tenant> getEMConfiguratedDataSources() {

		// this map holds the channels objects in order to add to the tenants
		// objects
		Map<Integer, Channel> channelsMap = new HashMap<Integer, Channel>();
		List<Tenant> result = new ArrayList<Tenant>();
		Path path = getDataSourcesDir();

		logger.debug("Generating EMConfiguratedDataSources by reading from path {} ", path);

		try {

			String type;

			// go over each file of DS
			for (File dataSource : getListOfDataSourcesFiles(path)) {

				Document doc = XmlUtils.getXMLDocumentFromFile(dataSource);
				NodeList nList = XmlUtils.getXMLElementsByTagName(doc, nodeName);

				if (nList != null && nList.getLength() == 1) {
					Element el = (Element) nList.item(0);

					type = XmlUtils.getAttribute(el, mainTypeAttributName, "");
					if (type.equals(mainTypeAttributValue)) {

						type = XmlUtils.getAttribute(el, subTypeAttributName, "");

						// check what is the type of the DS
						switch (type) {
							case channelType:
								createChannelDataFromFile(channelsMap, el);
								break;
							case tenantType:
								createTenantDataFromFile(channelsMap, result, el);
								break;
							default:
								break;
						}
					}
				}
			}

			// after reading all files check that the data is full.

			// for(Tenant)
		} catch (Exception ex) {

			// check if we saved some good tenants list and if so return this
			// list.
			if (s_lastGoodTenantList != null) {
				// log the error
				logger.error("Error while reading Data sources files. Error = {}", ex);
				logger.error("Error while reading data sources files, returning last good data source list from : {} ", backupTime.toString());

				return s_lastGoodTenantList;
			}

			logger.error("Error while reading Data sources files. ErrorMessage = {}", ex.getMessage());
			logger.error("Error while reading Data sources files.backup not available raising error to client. Error - {}", ex.getMessage());

			Throwables.propagateIfInstanceOf(ex, FoundationServicesExecutionException.class);
			Throwables.propagate(new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.DataSourcesFileCorapted));
		}

		s_lastGoodTenantList = result;
		backupTime = DateTime.now();

		return result;
	}

	/***
	 * 
	 * @param authorizedChannels
	 *            list of channels the the user is authorizes to.
	 * @return List of tenants by user permisions
	 */
	public List<Tenant> getDataSoursesListFiltered(List<Channel> authorizedChannels) {
		List<Tenant> res = new ArrayList<Tenant>();

		val allTenants = getEMConfiguratedDataSources();
		if (allTenants == null || authorizedChannels == null) {
			return res;
		}

		for (Tenant tenant : allTenants) {
			Tenant newTenant = tenant.cloneWithoutChannels();

			for (Channel channel : tenant.getChannels()) {

				if (authorizedChannels.contains(channel)) {
					newTenant.getChannels().add(channel);
				}
			}

			// if there are channel matches, add it to the res object
			if (newTenant.getChannels().size() > 0) {
				res.add(newTenant);
			}
		}

		return res;
	}

	/***
	 * this function gets the channel inner XMl and creates the channel object.
	 * 
	 * @param channelsMap
	 *            map of all created channels
	 * @param el
	 *            inner xml
	 */
	private void createChannelDataFromFile(Map<Integer, Channel> channelsMap, Element el) {
		Integer id;
		Channel curChannel;
		id = Integer.parseInt(XmlUtils.getAttribute(el, idAttributName, "-1"));

		// check if the channel object was updated in the map if not create the
		// object.
		if (channelsMap.containsKey(id)) {
			curChannel = channelsMap.get(id);
		} else {
			curChannel = new Channel();
			curChannel.setEmId(id);
		}

		// update name attribute in channel object
		curChannel.setDisplayName(XmlUtils.getAttribute(el, nameAttributName, ""));

		channelsMap.putIfAbsent(id, curChannel);
	}

	/**
	 * this function gets the tenant inner XMl and creates the tenant object.
	 * 
	 * @param channelsMap
	 *            map of all created channels
	 * @param result
	 *            the list of all tenants created so far.
	 * @param el
	 *            inner xml
	 */
	private void createTenantDataFromFile(Map<Integer, Channel> channelsMap, List<Tenant> result, Element el) {
		Integer id;
		Channel curChannel;
		Tenant curTenant;
		List<Channel> channels;
		curTenant = new Tenant();

		curTenant.setEmId(Integer.parseInt(XmlUtils.getAttribute(el, idAttributName, "-1")));
		curTenant.setDisplayName(XmlUtils.getAttribute(el, nameAttributName, ""));

		curTenant.setId(Integer.toString(IDGeneratorUtils.generateTenantID(curTenant.getEmId(), configurationManager.getApplicationConfiguration().getSystemID())));

		channels = new ArrayList<Channel>();

		// read channels nodes
		NodeList channelsNl = XmlUtils.getXMLElementsByTagName(el, chaneelsNodeNameInTenantXML);
		if (channelsNl != null && channelsNl.getLength() > 0) {
			for (int i = 0; i < channelsNl.getLength(); i++) {

				Element channelel = (Element) channelsNl.item(i);
				id = Integer.parseInt(XmlUtils.getAttribute(channelel, idAttributName, "-1"));

				// check if channel exist in the map and take the object from
				// there or create a new one(with only id)
				if (channelsMap.containsKey(id)) {
					curChannel = channelsMap.get(id);
				} else {
					curChannel = new Channel();
					curChannel.setEmId(id);
					channelsMap.put(id, curChannel);
				}

				curChannel.setId(Integer.toString(IDGeneratorUtils.generateChannelID(id, configurationManager.getApplicationConfiguration().getSystemID())));
				channels.add(curChannel);

			}
		}

		curTenant.setChannels(channels);
		result.add(curTenant);
	}

	/***
	 * this function builds the path to the dir in data-dir to get data sources
	 * files from.
	 * 
	 * @return the path.
	 */
	public Path getDataSourcesDir() {
		Map<String, String> env = System.getenv();

		Path path;
		path = Paths.get(env.get(IMPACT360_SOFTWARE_DIR), "Conf", "Cache");

		return path;
	}

	/***
	 * This function returns the files from the given path that macues the
	 * pattern of DataSource*.xml .
	 * 
	 * @param path
	 *            the path that the files located in
	 * @return list of files that match the pattern of DataSource files.
	 */
	private List<File> getListOfDataSourcesFiles(Path path) {

		List<File> result = null;
		try {
			// go over all files in this folder.
			// filter only the files that name matches the pattern and then
			// return list of files.
			result = Files.walk(path).filter(Files::isRegularFile).filter(p -> p.getFileName().toString().startsWith("DataSource") && p.getFileName().toString().endsWith(".xml")).map(Path::toFile).collect(Collectors.toList());
		} catch (IOException ex) {
			Throwables.propagateIfInstanceOf(ex, FoundationServicesExecutionException.class);
			Throwables.propagate(new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.DataSourcesDirectoryNotExist));

		}

		return result;
	}
}