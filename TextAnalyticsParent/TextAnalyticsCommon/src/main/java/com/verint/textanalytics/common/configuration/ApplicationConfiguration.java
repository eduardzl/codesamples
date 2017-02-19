package com.verint.textanalytics.common.configuration;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent the Application Configuration.
 *
 * @author imor
 */
@Accessors(chain = true)
public class ApplicationConfiguration {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Getter
	@Setter
	private boolean sendExceptionDetailsToClient = false;

	@Mandatory
	@Getter
	@Setter
	private String darwinTextEngineServiceBaseUrl;

	@Getter
	@Setter
	private String darwinTextRepositoryServiceBaseUrl;

	@Mandatory
	@Getter
	@Setter
	private String systemID;

	@Getter
	@Setter
	private String applicationID;

	@Getter
	@Setter
	private int darwinRestRequestTimeout = TAConstants.httpRequestTimeout;

	@Getter
	@Setter
	private String debugTenant;

	@Getter
	@Setter
	private List<String> debugTenantChannels;

	@Getter
	@Setter
	private int interactionSnippetsMaxPrecedingWords;

	@Getter
	@Setter
	private int interactionSnippetsMaxFollowingWords;

	@Getter
	@Setter
	private int interactionSnippetsFullTextMaxLength;

	@Getter
	@Setter
	private int interactionSnippetsMaxUtterancesToOperate;

	@Getter
	@Setter
	private int entitiesTrendsGridRowsLimit;

	@Getter
	@Setter
	private int relationsTrendsGridRowsLimit;

	@Getter
	@Setter
	private int keytermsTrendsGridRowsLimit;

	@Getter
	@Setter
	private int categoriesTrendsGridRowsLimit;

	// ANALAYZE CONTENT and BarsChart/FoamTree limitations properties

	@Getter
	@Setter
	private int textElementsFacetWithStatsFirstLevelLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int entitiesFacetRootTopLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int entitiesFacetDescendantsTopLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double entitiesFacetRootPercentageLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double entitiesFacetDescendantsPercentageLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int relationsFacetRootTopLimit;


	@Getter
	@Setter
	@Accessors(chain = true)
	private int relationsFacetDescendantsTopLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double relationsFacetRootPercentageLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double relationsFacetDescendantsPercentageLimit;

	// leaves limits
	@Getter
	@Setter
	@Accessors(chain = true)
	private int entitiesFacetLeavesTopLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double entitiesFacetLeavesPercentageLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int relationsFacetLeavesTopLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double relationsFacetLeavesPercentageLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Integer textElementsFacetQueryGroupSize;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Integer textElementsFacetTopLeafsJoinLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Integer textElementsFacetTopLeafsGroupSizeForSentimentQuery;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int autoCompletePrefixMinLengthConfiguration;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean invokeAutoCompleteRequest = true;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int autoCompleteLimitForQuery;

	@Mandatory
	@Getter
	@Setter
	@Accessors(chain = true)
	private String foundationServiceURL;

	@Mandatory
	@Getter
	@Setter
	@Accessors(chain = true)
	private String configServiceURL;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String bypassTrendsThreshold;

	@Mandatory
	@Getter
	@Setter
	@Accessors(chain = true)
	private String modelEditorServiceURL;

	@Mandatory
	@Getter
	@Setter
	@Accessors(chain = true)
	private String foundationServiceUserContextURL;


	@Getter
	@Setter
	@Accessors(chain = true)
	private String analyticsTaggerServiceURL;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String categoryReprocessingContextPrefixTaggerURL;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String categoryReprocessingContextURL;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int searchTermsSuggestionType;

	@Getter
	@Accessors(chain = true)
	private boolean isValid = false;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int numberOfRetriesInStoredSearches;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String configServiceFolder;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String categoryFile;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String channelFile;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String languageProperty;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String savedSearchesFile;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String categoriesReprocessingStateFile;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String timeStempFiled;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int storedSearchNameMaxLength;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int searchTermsLimit;


	@Getter
	@Setter
	@Accessors(chain = true)
	private int storedSearchDescriptionMaxLength;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean quickMenuOrgAvailable = true;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean quickMenuMetadataAvailable = true;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int categoryReprocessingAllowedIntervalMinutes = 10;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int searchWildCardPrefixMinLength;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String graphiteServer;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int graphitePort  = TAConstants.GraphiteReporter.pickleReporterPort;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String graphiteReporterPrefix;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int graphiteReporterBatchSize = 10;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int graphiteReporterIntervalSeconds = 5;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean logHttpClientRequests;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int httpClientMaxConnectionsTotal;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int httpClientMaxConnectionsPerRoute;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int facetThreadLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int termsSuggestionsFacetThreadsLimit;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean dateRangeRound;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String dateRangeRoundUpTo;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String awtApiKeyId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String awtApiKey;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int sampleSizeThreshold;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean ignoreSSLCertificateErrors = false;


	/**
	 * @return true if ApplicationConfiguration object is valid, otherwise false
	 */
	public boolean validate() {
		List<String> invalidFields = getInvalidFields();

		if (invalidFields.size() == 0) {
			this.isValid = true;
		} else {
			for (String invalidField : invalidFields) {
				logger.error("Mandatory Field [{}] is missing !", invalidField);
			}
			this.isValid = false;
		}
		return this.isValid;
	}

	/**
	 * @return List of fields name that are Mandatory and are not valid (null)
	 */
	public List<String> getInvalidFields() {
		List<String> invalidFields = new ArrayList<String>();

		List<Field> mandatoryFields = getMandatoryFields();
		for (Field mandatoryField : mandatoryFields) {
			try {
				if (mandatoryField.get(this) == null) {
					invalidFields.add(mandatoryField.getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("Error in Application Configuration get Invalid Fields. Error - {}", ExceptionUtils.getStackTrace(e));
			}
		}
		return invalidFields;
	}

	protected List<Field> getMandatoryFields() {
		List<Field> mandatoryFields = new ArrayList<Field>();
		Field[] allFields = this.getClass().getDeclaredFields();
		for (Field field : allFields) {
			Annotation[] annotations = field.getDeclaredAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Mandatory.class) {
					mandatoryFields.add(field);
				}
			}
		}
		return mandatoryFields;
	}

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject(this);
		return jsonObject.toString();
	}
}
