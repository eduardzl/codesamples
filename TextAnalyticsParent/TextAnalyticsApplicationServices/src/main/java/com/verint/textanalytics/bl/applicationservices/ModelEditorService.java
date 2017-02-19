package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.bl.security.MembershipProvider;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.security.JWTService;
import com.verint.textanalytics.dal.modelEditor.ModelEditorProvider;
import com.verint.textanalytics.model.modelEditor.ModelsTree;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;
import lombok.Setter;
import lombok.val;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map.Entry;

/**
 * Model Editor Service.
 *
 * @author imor
 */
public class ModelEditorService {

	@Autowired
	private ModelEditorProvider modelEditorProvider;

	@Autowired
	@Setter
	private JWTService jwtService;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private MembershipProvider membershipProvider;

	/***
	 * @param tenant tenant
	 * @return ModelsTree
	 */
	public ModelsTree retrieveModelsTree(String tenant) {
		return modelEditorProvider.retrieveModelsTree(tenant);
	}

	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @return Entry<String, String>
	 */
	public Entry<String, String> getModelEditorHeaderToken(String i360FoundationToken) {

		User user = membershipProvider.getUser(i360FoundationToken);
		val userTenants = user.getTenantsList();

		val applicationId = configurationManager.getApplicationConfiguration().getApplicationID();

		JSONArray tenantsJSONArray = new JSONArray();
		JSONObject tenantJSONObject;
		for (Tenant tenant : userTenants) {
			tenantJSONObject = new JSONObject();
			tenantJSONObject.put("id", tenant.getId());
			tenantJSONObject.put("name", tenant.getDisplayName());
			tenantsJSONArray.add(tenantJSONObject);
		}
		JSONObject dataJSONObject = new JSONObject();
		dataJSONObject.put("userName", user.getName());
		dataJSONObject.put("userId", user.getUserID());
		dataJSONObject.put("tenants", tenantsJSONArray);

		String data = dataJSONObject.toString();

		// the tenant id is the first one in all the user tenants
		return jwtService.createHeaderToken(userTenants.get(0).getId(), applicationId, Integer.toString(user.getUserID()), data);

	}
}