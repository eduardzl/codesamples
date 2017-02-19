package com.verint.textanalytics.dal.modelEditor;

import javax.ws.rs.core.Response;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import lombok.val;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.exceptions.ModelEditorErrorCode;
import com.verint.textanalytics.common.exceptions.ModelEditorException;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.dal.darwin.RestRequestPathsAndQueryParams;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.modelEditor.ModelsTree;

/***
 * 
 * @author imor
 *
 */
public class ModelEditorProvider {

	private static final String RESPONSE = "response";
	private static final String HTTP_CODE = "httpCode";

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private RestDataAccess restDataAccess;

	@Autowired
	private ConfigServiceRequestGenerator requestGenerator;

	@Autowired
	private ConfigServiceResponseConvertor responseConverter;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	private String storedSearchesRepositoryServiceBaseUrl;

	/***
	 * 
	 */
	public void initialize() {
		logger.debug("Initializing ModelEditorProvider");

		val applicationConfig = configurationManager.getApplicationConfiguration();
		this.storedSearchesRepositoryServiceBaseUrl = applicationConfig.getConfigServiceURL();

		logger.debug("Stored Search provider was initialized with following base url , Config Server - {}",
		             !StringUtils.isNullOrBlank(this.storedSearchesRepositoryServiceBaseUrl) ? this.storedSearchesRepositoryServiceBaseUrl : "");

		logger.debug("ModelEditorProvider was initialized");
	}

	/***
	 * this will return the repository of categories.
	 * @param tenant
	 *            tenant
	 * @return categories retrieve Models Tree
	 */
	public ModelsTree retrieveModelsTree(String tenant) {

		ModelsTree modelsTree = null;

		Timer.Context context = performanceMetrics.startTimedOperation(OperationType.RetriveOntologyModelsTree);

		try {
			logger.info("Executing Retrive Models Tree for tenant - {}", tenant);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getRetrieveModelsTreeQuery(tenant);

			Response response = this.restDataAccess.executeGetRequestFullResponse(this.storedSearchesRepositoryServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                                      restRequestPathsAndQueryParams.getQueryParams());

			switch (response.getStatus()) {

				case TAConstants.httpCode200:
					//200 OK – Success
					String file = response.readEntity(String.class);
					modelsTree = responseConverter.convertModelsTreeResponse(file);
					break;
				case TAConstants.httpCode404:
					//404 File not found - Channel does not exists
					throw new ModelEditorException(ModelEditorErrorCode.RetrieveModelsTreeTenantNotFoundError).put(HTTP_CODE, response.getStatus()).put(RESPONSE, response);
				case TAConstants.httpCode400:
					//400 Bad Request – invalid URL detected
				case TAConstants.httpCode500:
					//500 Server error - general error has occurred, more information will be included in the response body
				default:
					throw new ModelEditorException(ModelEditorErrorCode.RetrieveModelsTreeError).put(HTTP_CODE, response.getStatus()).put(RESPONSE, response);
			}

			this.performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, ModelEditorException.class);
			Throwables.propagate(new ModelEditorException(ex, ModelEditorErrorCode.RetrieveModelsTreeError));
		}
		return modelsTree;
	}
}