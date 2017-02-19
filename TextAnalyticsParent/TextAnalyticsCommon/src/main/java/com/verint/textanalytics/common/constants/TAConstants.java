package com.verint.textanalytics.common.constants;

/**
 * @author EZlotnik Application constants class
 */
public final class TAConstants {

	/**
	 * @author EZlotnik Environment variables
	 */
	public final static class Environment {
		public static final String IMPACT360_DATA_DIR = "IMPACT360DATADIR";
		public static final String IMPACT360_SOFTWARE_DIR = "IMPACT360SOFTWAREDIR";
	}

	public static final int httpRequestTimeout = 60;

	public static final int httpCode200 = 200;
	public static final int httpCode201 = 201;
	public static final int httpCode400 = 400;
	public static final int httpCode404 = 404;
	public static final int httpCode409 = 409;
	public static final int httpCode500 = 500;

	public static final String utf8Encoding = "UTF-8";
	public static final String trueLowerCase = "true";
	public static final String falseLowerCase = "false";

	public static final String restServicesPackageName = "com.verint.textanalytics.web.portal.";

	public static final String i360FoundationTokenHeader = "Impact360AuthToken";

	public static final int sentimentVeryNegative = -2;
	public static final int sentimentNegative = -1;
	public static final int sentimentNeutral = 0;
	public static final int sentimentPositive = 1;
	public static final int sentimentVeryPositive = 2;

	public static final String curlyOpenBracket = "{";
	public static final String curlyCloseBracket = "}";

	public static final String requestId = "reqId";

	/**
	 * Priority of REST filters.
	 */
	public static final class RequestFiltersPriority {
		public static final int requestIdFilter = 1;
		public static final int authorizationFilter = 500;
		public static final int userSessionKeepAlive = 600;
	}

	/**
	 * Values for Date Range filters.
	 *
	 * @author EZlotnik
	 */
	public static final class DateRangeValues {
		public static final String dateRangeNow = "NOW";
		public static final String dateRangeLastDay = "NOW-1DAY";
		public static final String dateRangeLast7Days = "NOW-7DAY";
		public static final String dateRangeLast1Month = "NOW-1MONTH";
		public static final String dateRangeLast3Months = "NOW-3MONTH";
		public static final String dateRangeLast6Months = "NOW-6MONTH";
		public static final String dateRangeLast1Year = "NOW-1YEAR";
		public static final String dateRangeLast3Year = "NOW-3YEAR";
		public static final String dateRangeLast5Year = "NOW-5YEAR";

		public static final String dailyVolumeNow = "NOW/DAY";
		public static final String dailyVolumeLast3Years = "NOW-3YEAR/DAY";
		public static final String dailyVolumeLast7Years = "NOW-7YEARS/DAY";
		public static final String dateRangeGap1Day = "+1DAY";
	}

	/**
	 * Speaker separation field values.
	 *
	 * @author EZlotnik
	 */
	public static final class SpeakerTypeValues {
		public static final String Agent = "agent";
		public static final String Customer = "customer";
		public static final String Automated = "automated";
	}

	/**
	 *  Interactions Sentiment API.
	 */
	public static final class SentimentAPI {
		public static final String ContextName = "InteractionSentimentChart";
		public static final String Topics = "topics";
		public static final String Relations = "relations";
		public static final String SortParameter = "solrSort";
		public static final String Leaves = "leaves";
	}

	/**
	 * Schema fields constants class.
	 *
	 * @author EZlotnik
	 */
	public final static class SchemaFieldNames {
		public static final String documentId = "id";
		public static final String tenant = "tenant";
		public static final String channel = "channel";

		// source type
		public static final String sourceType = "Meta_s_messageSourceType";

		public static final String text_total_format = "text_%s_total";
		public static final String text_agent_format = "text_%s_agent";
		public static final String text_customer_format = "text_%s_customer";

		// Topics, Entities
		public static final String topics = "topics";
		public static final String topics_f = "topics_f";
		public static final String topics_f_total = "topics_f_total";
		public static final String topics_f_agent = "topics_f_agent";
		public static final String topics_f_customer = "topics_f_customer";
		public static final String topics_total = "topics_total";
		public static final String topics_agent = "topics_agent";
		public static final String topics_customer = "topics_customer";

		// Relations
		public static final String relations = "relations";
		public static final String relations_f = "relations_f";
		public static final String relations_f_total = "relations_f_total";
		public static final String relations_f_agent = "relations_f_agent";
		public static final String relations_f_customer = "relations_f_customer";
		public static final String relations_total = "relations_total";

		// KeyTerms
		public static final String keyterms = "terms";
		public static final String keyterms_f = "terms_f";
		public static final String keyterms_f_total = "terms_f_total";
		public static final String keyterms_f_agent = "terms_f_agent";
		public static final String keyterms_f_customer = "terms_f_customer";

		public static final String relevancyScore = "score";

		// email fields
		public static final String subject = "Meta_s_subject";
		public static final String ccNames = "Meta_ss_ccNames";
		public static final String ccEmails = "Meta_ss_ccEmails";
		public static final String toNames = "Meta_ss_toNames";
		public static final String toEmails = "Meta_ss_toEmails";
		public static final String fromName = "Meta_s_fromName";
		public static final String fromEmail = "Meta_s_fromEmail";
		public static final String customerEmails = "Meta_ss_customerEmails";
		public static final String employeesEmails = "Meta_ss_employeesEmails";

		// agent fields
		public static final String agentNames = "Meta_ss_employeeNames";
		public static final String agentLocalStartTime = "Meta_dt_employeeStartTime";
		public static final String agentTimeZone = "Meta_s_employeeTimeZone";
		public static final String agentMessagesCount = "Meta_i_employeesMessages";
		public static final String agentAvgResponseTime = "Meta_l_avgEmployeeResponseTime";

		// customer fields
		public static final String customerNames = "Meta_ss_customerNames";
		public static final String customerLocalStartTime = "Meta_dt_customerStartTime";
		public static final String customerTimeZone = "Meta_s_customerTimeZone";
		public static final String customerMessagesCount = "Meta_i_customerMessages";
		public static final String customerAvgResponseTime = "Meta_l_avgCustomerResponseTime";

		// category fields
		public static final String categoriesIds = "categories";

		// org fields
		public static final String orgIds = "Meta_ss_orgIds";
		public static final String groupIds = "Meta_ss_groupIds";
		public static final String peopleIds = "Meta_ss_agentIds";

		// dates
		public static final String parentDate = "date";
		public static final String childDate = "date";

		public static final String messagesCount = "Meta_i_messagesCount";
		public static final String handleTime = "Meta_l_handleTime";

		public static final String parentId = "parentID";

		public static final String speakerType = "speaker_type";

		public static final String language = "language";
		public static final String interactionSentiment = "interaction_sentiment";
		public static final String interactionSentimentIsMixed = "interaction_sentiment_ismixed";

		public static final String utteranceSentiment = "doc_sentiment";
		public static final String utteranceSentimentIsMixed = "doc_sentiment_ismixed";

		public static final String categories = "categories";

		public static final String maxSimultaneousChats = "Meta_i_maxSimultaneousChats";
		public static final String numberOfRobotMessages = "Meta_i_numberOfRobotMessages";
		public static final String idleTime = "Meta_l_idleTime";

		public static final String content_type = "content_type";
		public static final String content_type_parent = "content_type:PARENT";
		public static final String content_type_child = "content_type:CHILD";

		public static final String robotMessagesCount = "Meta_i_numberOfRobotMessages";

		public static final String dayOfDate = "Meta_l_date_day";
		public static final String uuid = "uuid";
	}

	/**
	 * Column Mapping in interactions Grid.
	 *
	 * @author EZlotnik
	 */
	public final static class InteractionsGridColumns {
		public static final String startTime = "StartTime";
		public static final String sourceType = "SourceType";

		public static final String agentNames = "AgentNames";
		public static final String agentLocalStartTime = "AgentLocalStartTimeTicks";
		public static final String agentMessagesCount = "AgentMessagesCount";
		public static final String agentAvgResponseTime = "AgentAvgResponseTime";

		public static final String customerNames = "CustomerNames";
		public static final String customerLocalStartTime = "CustomerLocalStartTimeTicks";
		public static final String customerMessagesCount = "CustomerMessagesCount";
		public static final String customerAvgResponseTime = "CustomerAvgResponseTime";

		public static final String robotMessagesCount = "NumberOfRobotMessages";

		public static final String relevancyScore = "RelevancyScore";
		public static final String messagesCount = "MessagesCount";
		public static final String handleTime = "HandleTime";
		public static final String sentiment = "Sentiment";
	}

	/**
	 * Column names in Text Elements grid.
	 *
	 * @author EZlotnik
	 */
	public final static class TextElementsGridColumns {
		public static final String name = "Name";
		public static final String volume = "Volume";
		public static final String relativeChange = "RelativeChange";
		public static final String absoluteChange = "AbsoluteChange";
	}

	/**
	 * Constants to ber using in facet queries.
	 *
	 * @author EZlotnik
	 */
	public final static class FacetQuery {
		public final static String facet = "facet";
		public final static String facets = "facets";
		public final static String sentiments = "sentiments";
		public static final String termsAutoCompleteFacetAlias = "termsAutoComplete";
		public static final String interactionsDailyVolumeAlias = "interactionsDailyVolume";
		public static final String textElementFacetAlias = "textElementFacet";
		public static final String interactionsCountStatAlias = "interactions_count";
		public static final String interactionsCountAlias = "count";
		public static final String bucketTitleAlias = "val";
		public static final String totalInteractionsCountStatAlias = "total_interactions_count";
		public static final String bucketsAlias = "buckets";
		public static final String innerFacetStats = "InnerFacetStats";
		public static final int bucketsLimitNotLimited = -1;
		public static final String trendsFacetbyParent = "PARENT";
		public static final String globalStatsAlias = "global";

		public static final String facetParam = "facet";
		public static final String rangeFacetParam = "facet.range";
		public static final String rangeFacetStart = "facet.range.start";
		public static final String rangeFacetEnd = "facet.range.end";
		public static final String rangeFacetGap = "facet.range.gap";
		public static final String facetMincount = "facet.mincount";
		public static final String facetLimit = "facet.limit";
		public static final String facetThreads = "facet.threads";

		public static final String facetCounts = "facet_counts";
		public static final String facetFields = "facet_fields";
		public static final String facetRanges = "facet_ranges";
		public static final String facetDate = "date";
		public static final String counts = "counts";

		public static final String simpleFacet = "facet";
		public static final String simpleFacetField = "facet.field";
		public static final String simpleFacetMethod = "facet.method";
		public static final String simpleFacetPrefix = "facet.prefix";
		public static final String simpleFacetLimit = "facet.limit";
		public static final String simpleFacetThreads = "facet.threads";
		public static final String simpleFacetMinCount = "facet.mincount";
	}

	/**
	 * Interactions parsing json field names.
	 */
	public final static class InteractionsQuery {
		public final static String response = "response";
		public final static String docs = "docs";
		public final static String highlightLayers = "layers_marking";
		public final static String relationsHighlightsLayer = "relations_layer";
		public final static String topicsHighlightsLayer = "topics_layer";
		public final static String keyTermsHighlightsLayer = "terms_layer";
		public final static String sentimentHighlightsLayer = "sentiments_layer";
		public final static String queryTermsHighlightsLayer = "query_terms_layer";

		public final static String term = "term";
		public final static String relation = "relation";
		public final static String keyterm = "term";
		public final static String positions = "positions";
		public final static String positionStart = "start";
		public final static String solrPositionStart = "starts";
		public final static String positionEnds = "ends";
		public final static String sentiments = "sentiments";

		public final static String speakerType = "speaker_type";
		public final static String noCacheOnFq = "{!cache=false}";
	}

	/**
	 * Constants to ber using in facet queries.
	 *
	 * @author imor
	 */
	public final static class MetricsQuery {
		public final static String metricsPrefix = "METRIC_";
		public final static String volume = "Volume";
		public final static String volumePercentage = "VolumePercentage";
		public final static String correlationPercentage = "CorrelationPercentage";
		public final static String averageSentiment = "AvgSentiment";
		public final static String averageHandleTime = "AvgHandleTime";
		public final static String averageEmployeeResponseTime = "AvgEmployeeResponseTime";
		public final static String averageCustomerResponseTime = "AvgCustomerResponseTime";
		public final static String averageMessagesCount = "AvgMessagesCount";
		public final static String averageEmployeeMessages = "AvgEmployeesMessages";
		public final static String averageCustomerMessages = "AvgCustomerMessages";
	}

	/**
	 * Error codes.
	 *
	 * @author EZlotnik
	 */
	public final static class ErrorDataKeys {
		public static final String antlrParsingErrors = "antlrparsingerrors";
		public static final String searchTermsQuery = "searchtermsquery";
		public static final String solrJsonResponse = "solrjsonresponse";
		public static final String solrExecutionStatus = "solrqueryexecutionstatus";
	}

	/**
	 * Error message keys.
	 * @author EZlotnik
	 */
	public final static class ErrorMessageKeys {
		public static final String vtaSyntaxErrorPrefix = "InteractionsSearchInvalidQuery";
		public static final String vtaSyntaxRecognitionError = "VTASyntaxRecognitionError";
		public static final String vtaSyntaxProcessingGenericError = "VTASyntaxProcessingGenericError";

		public static final String textQueryExecutionGeneralError = "textqueryexecutiongeneralerror";
		public static final String applicationConfigurationNotValidError = "ApplicationConfigurationNotValidError";

		// Categories Load errors
		public static final String categoriesLoadParsingError = "CategoriesLoad_CategoriesParsingError";
		public static final String categoriesLoadLoadingError = "CategoriesLoad_CategoriesLoadError";

		// Saved Searches Load errors
		public static final String savedSearchesLoadParsingError = "SavedSearches_SavedSearchesParsingError";
		public static final String savedSearchesLoadLoadingError = "SavedSearches_SavedSearchesLoadingError";

		// Category Add error messages
		public static final String categoryAddNameExists = "CategoryAdd_NameExists";
		public static final String categoryAddInvalidName = "CategoryAdd_InvalidName";
		public static final String categoryAddGenericError = "CategoryAdd_GenericError";

		// Saved Search Add error messages
		public static final String savedSearchAddNameExists = "SavedSearchAdd_NameExists";
		public static final String savedSearchInvalidName = "SavedSearch_InvalidName";
		public static final String savedSearchAddGenericError = "SavedSearch_AddError";

		// Category Update errors
		public static final String categoryUpdateCategoryWasNotFound = "CategoryUpdate_CategoryWasNotFound";
		public static final String categoryUpdateCategoryNotLatestVersion = "CategoryUpdate_CategoryNotLatestVersion";
		public static final String categoryUpdateGenericError = "CategoryUpdate_GenericError";

		// Saved Search update errors
		public static final String savedSearchSavedSearchWasNotFound = "SavedSearchUpdate_SavedSearchWasNotFound";
		public static final String savedSearchSavedSearchNotLatestVersion = "SavedSearchUpdate_SavedSearchNotLatestVersion";
		public static final String savedSearchGenericError = "SavedSearchUpdate_GenericError";

		// Category Delete errors
		public static final String categoryRemoveCategoryNotLatestVersion = "CategoryRemove_CategoryNotLatestVersion";
		public static final String categoryRemoveCategoryRemoveError = "CategoryRemove_CategoryRemoveError";

		// Saved Search delete errors
		public static final String savedSearchRemoveSavedSearchNotLatestVersion = "SavedSearchRemove_SavedSearchNotLatestVersion";
		public static final String savedSearchRemoveSavedSearchRemoveError = "SavedSearchRemove_SavedSearchRemoveError";

		public static final String storedSearchUpdateGenericError = "StoredSearch_GenericError";
		public static final String categoriesFacetFacetLoadingError = "CategoriesFacet_FacetLoadingError";
		public static final String categoriesGenericError = "Categories_GenericError";
	}

	/**
	 * Trends BaseDate.
	 *
	 * @author YHemi
	 */
	public static final class TrendsBaseDate {
		public static final String TrendNow = "NOW";
		public static final String TrendLast = "LAST";
	}

	/**
	 * Graphite reporter defaulr values.
	 */
	public static final class GraphiteReporter {
		public static final int pickleReporterPort = 2004;
	}

	/**
	 * Error Messages.
	 *
	 * @author EZlotnik
	 */
	public final static class ErrorMessages {
		public static final String jacksonJsonProcessingError = "Jackson Json Processing Error";
		public static final String textElementValueEmpty = "Text Element Value is empty";
		public static final String textElementTypeInvalid = "Text Element Type is not valid";
	}

	/**
	 * Logger names.
	 */
	public final static class LoggerNames {
		public static final String textElementsFacet = "com.verint.textanalytics.bl.applicationservices.facet.textelements.TextElementsFacetService";
	}

	public static final String appGenericErrorCode = "ApplicationGenericErrorCode";
	public static final String jacksonJsonSerializationErrorCode = "JacksonJsonSerializationError";

	public static final String treeNodeNoIconCls = "no-icon";
	public static final String treeNodeCls = "TA-Tree-Node";

	public static final double percentage_100 = 100.0;
	public static final String sourceTypeUnknown = "unknown";

	private TAConstants() {

	}
}
