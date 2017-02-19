package com.verint.textanalytics.bl.security;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.exceptions.FoundationServicesExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.FoundationServicesExecutionException;
import com.verint.textanalytics.common.exceptions.UserNotLoggedInErrorCode;
import com.verint.textanalytics.common.exceptions.UserNotLoggedInException;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.User;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * converts the post response of foundation service.
 *
 * @author yzanis
 */
public class FoundationResponseConverter {

	private final String userSecurityContextJSONElm = "UserSecurityContext";
	private final String userDetailsJSONElm = "userDetails";
	private final String dataSourcesJSONElm = "dataSources";
	private final String securityContextJSONElm = "securityContext";
	private final String uiPreferencesJSONElm = "uiPreferences";

	private final String dataSourceIdJSONElm = "dataSourceId";
	private final String dataSourceNameJSONElm = "dataSourceName";

	private final String principalJSONElm = "principal";
	private final String viewTimeZoneJSONElm = "viewTimeZone";
	private final String idJSONElm = "id";
	private final String rolesJSONElm = "roles";
	private final String privilegesJSONElm = "privileges";
	private final String privilegeNameJSONElm = "privilegeName";
	private final String superUserJSONElm = "superUser";

	private List<String> authorizedPrivilegesList = Arrays.asList("IF.USEAPPLICATION", "IF.SEARCHBYKEYWORDS", "IF.ADDFORM", "IF.INTELLIFIND", "IF.VIEWTRANSCRIPT", "IF.TEXTMODELMANAGEMENT");

	private Logger logger = LogManager.getLogger(this.getClass());

	/***
	 * @param foundationResponse response from foundation service
	 * @return list of projects that the user is autorazied to
	 */
	public List<Channel> getListOfProjectDataSources(String foundationResponse) {

		val lstChannels = new ArrayList<Channel>();

		try {

			Channel currentChannel;

			if (!StringUtils.isNullOrBlank(foundationResponse)) {

				// load JSON response and parse it
				JSONTokener tokener = new JSONTokener(foundationResponse);

				JSONObject root = new JSONObject(tokener);
				if (root != null) {

					JSONObject userSecurityContext = JSONUtils.getJSONObject(root, userSecurityContextJSONElm);
					if (userSecurityContext != null) {

						JSONObject userDetails = JSONUtils.getJSONObject(userSecurityContext, userDetailsJSONElm);
						if (userDetails != null) {

							JSONArray dataSources = JSONUtils.getJSONArray(userDetails, dataSourcesJSONElm);

							if (dataSources != null) {
								JSONObject dataSource;
								for (int i = 0; i < dataSources.length(); i++) {

									currentChannel = new Channel();

									dataSource = dataSources.getJSONObject(i);

									currentChannel.setEmId(JSONUtils.getInt(dataSourceIdJSONElm, dataSource, 0));
									currentChannel.setDisplayName(JSONUtils.getString(dataSourceNameJSONElm, dataSource, ""));

									lstChannels.add(currentChannel);
								}
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			logger.error("Error while Parrsing response from foundation.Foundation Response = {} , Error = {}", foundationResponse, ex);
			throw new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.JsonResponseParsingError);
		}

		return lstChannels;
	}

	/**
	 * @param userJson userJson
	 * @return User
	 */
	public User getUser(String userJson) {

		User user = new User();

		try {

			if (!StringUtils.isNullOrBlank(userJson)) {

				//At first check that the user is logged in 

				// load JSON response and parse it
				if (userJson.contains("Please login here")) {
					throw new UserNotLoggedInException(UserNotLoggedInErrorCode.UserNotLogedInError);
				}

				JSONTokener tokener = new JSONTokener(userJson);

				JSONObject root = new JSONObject(tokener);
				if (root != null) {

					JSONObject userSecurityContext = JSONUtils.getJSONObject(root, userSecurityContextJSONElm);
					if (userSecurityContext != null) {

						user.setUserID(this.getUserID(userSecurityContext));

						JSONObject securityContext = JSONUtils.getJSONObject(userSecurityContext, securityContextJSONElm);
						if (securityContext != null) {

							user.setName(this.getUserName(securityContext));
							user.setPrivilegesList(this.getPrivilegesList(securityContext));
							user.setSuperUser(this.getIsSuperUser(securityContext));

						}

						JSONObject uiPreferences = JSONUtils.getJSONObject(userSecurityContext, uiPreferencesJSONElm);
						if (uiPreferences != null) {
							user.setViewTimeZone(this.getViewTimeZone(uiPreferences));
						}
					}
				}
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, UserNotLoggedInException.class);
			logger.error("Error while Converting User data response ,User Response : {} , Error = {}", userJson, ex);
			throw new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.JsonResponseParsingError);
		}

		return user;
	}

	private String getViewTimeZone(JSONObject uiPreferences) {
		String res = "GMT";

		try {
			res = JSONUtils.getString(viewTimeZoneJSONElm, uiPreferences, res);
		} catch (Exception ex) {
			logger.error("Error while retriving user timezone , Error = {}", ex);
			throw new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.JsonResponseParsingError);
		}

		return res;
	}

	/**
	 * @param securityContext securityContext
	 * @return String
	 */
	private String getUserName(JSONObject securityContext) {
		String res = "";

		try {
			res = JSONUtils.getString(principalJSONElm, securityContext, "");
		} catch (Exception ex) {
			logger.error("Error while retriving user name , Error = {}", ex);
			throw new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.JsonResponseParsingError);
		}

		return res;
	}

	private int getUserID(JSONObject userSecurityContext) {
		int res = 0;

		try {
			res = JSONUtils.getInt(idJSONElm, userSecurityContext, 0);
		} catch (Exception ex) {
			logger.error("Error while retriving user ID, Error = {}", ex);
			throw new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.JsonResponseParsingError);
		}

		return res;
	}

	/**
	 * @param securityContext securityContext
	 * @return List<String>
	 */
	private List<String> getPrivilegesList(JSONObject securityContext) {
		List<String> res = new ArrayList<String>();

		try {

			val roles = JSONUtils.getJSONArray(securityContext, rolesJSONElm);

			if (roles != null) {

				JSONObject role;
				for (int i = 0; i < roles.length(); i++) {

					role = roles.getJSONObject(i);

					val privileges = JSONUtils.getJSONArray(role, privilegesJSONElm);
					if (privileges != null) {

						JSONObject privilege;
						for (int j = 0; j < privileges.length(); j++) {
							privilege = privileges.getJSONObject(j);

							val privilegeName = JSONUtils.getString(privilegeNameJSONElm, privilege, "");

							if (authorizedPrivilegesList.contains(privilegeName)) {
								res.add(JSONUtils.getString(privilegeNameJSONElm, privilege, ""));
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			logger.error("Error while retriving user Privilage list, Error = {}", ex);
			throw new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.JsonResponseParsingError);
		}

		return res;
	}

	/**
	 * @param securityContext securityContext
	 * @return boolean
	 */
	private boolean getIsSuperUser(JSONObject securityContext) {
		boolean res = false;

		try {
			res = JSONUtils.getBoolean(superUserJSONElm, securityContext, false);
		} catch (Exception ex) {
			logger.error("Error while checking if a user is a superuser, Error = {}", ex);
			throw new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.JsonResponseParsingError);
		}
		return res;
	}

}