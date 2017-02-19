package com.verint.textanalytics.dal.darwin;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.collection.MultivaluedStringMap;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.exceptions.TextQueryGenerationErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryGenerationException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.UriUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.QueryTerm;
import com.verint.textanalytics.dal.darwin.vtasyntax.TASQueryConfiguration;
import com.verint.textanalytics.dal.darwin.vtasyntax.VTASyntaxAnalyzer;
import com.verint.textanalytics.dal.darwin.vtasyntax.VTASyntaxParsingResult;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.documentSchema.FieldDataType;
import com.verint.textanalytics.model.documentSchema.TextSchemaField;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.storedSearch.StoredSearchQuery;
import com.verint.textanalytics.model.trends.TrendType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Generates URL for Solr requests according to Solr API definition.
 *
 * @author EZlotnik
 */
public class SolrRequestGenerator implements RequestGenerator, DisposableBean {
	public static final String TENANT_PARAMETER_IS_NULL_EMPTY = "tenant parameter is null empty";
	public static final String CHANNEL_PARAMETER_IS_NULL_EMPTY = "channel parameter is null empty";
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	@Getter
	@Setter
	@Accessors(chain = true)
	protected SolrQueryParameters queryParams;

	protected final String squareOpenBracket = "]";
	protected final String squareCloseBracket = "]";

	protected final String curlyOpenBracket = "{";
	protected final String curlyCloseBracket = "}";
	protected final String openPareth = "(";
	protected final String closeParetn = ")";

	protected final String backslash = "\\";
	protected final String slash = "/";
	protected final String minus = "-";
	protected final String space = " ";

	protected final String fq = "fq";
	protected final String fqInBody = "fq=";
	protected final String q = "q";
	protected final String quote = "\"";
	protected final String and = " AND ";
	protected final String or = " OR ";
	protected final String tab = "\t";
	protected final String tab2 = "\t\t";
	protected final String tab3 = "\t\t\t";

	protected final String contentType = "content_type";
	protected final String parent = "PARENT";
	protected final String child = "CHILD";

	// join to get child documents data in query
	protected final String generateChildResultSetBlockJoin = "{!child of='content_type:PARENT'}";

	// join to get all children of parent of children
	protected final String generateAllChildrenOfParentsResultSetBlockJoin = "{!child of='content_type:PARENT'}{!parent which='content_type:PARENT'}";

	// join to get parent of children
	protected final String generateParentResultSetBlockJoin = "{!parent which='content_type:PARENT'}";

	// join to get parent of children
	protected final String generateParentResultSetBlockJoinWithScore = "{!parent which='content_type:PARENT' score=total}";

	// to be specified in fl to get children data with parent in response
	protected final String childDocumentsField = "[child parentFilter=content_type:PARENT limit=%s]";

	protected String interactionPredefinedFields;

	protected final String nestedQueryParserParam = "_query_:";

	protected final String luceneQueryParser = "";
	protected final String complexPhraseQueryParser = "{!complexphrase inOrder=true}";

	protected final String costParser = "{!cost=%s}";

	protected final String sort = "sort";
	protected final String desc = "desc";
	protected final String asc = "asc";

	protected final String jsonFacet = "json.facet";
	protected final String facetThreads = "facet.threads";

	protected final String speakerAndQueryTemplate = " AND %s:%s";
	protected final String filterFieldQueryTemplate = "%s:%s";
	protected final String pattenInParet = "\"%s\"";

	protected final String queryAll = "*:*";
	private final String amp = "&";

	@Autowired
	@Getter
	@Setter
	@Accessors(chain = true)
	private TextEngineSchemaService textEngineConfigurationService;

	@Autowired
	@Getter
	@Setter
	@Accessors(chain = true)
	private ConfigurationManager configurationManager;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<FieldMetric> textElementsFacetMetricFields;

	@Autowired
	@Getter
	@Setter
	@Accessors(chain = true)
	private VTASyntaxAnalyzer vtaSyntaxAnalyzer;

	private Map<String, TASQueryConfiguration> vtaSyntaxQueryConfigurations;

	private final Integer fqCost2 = null;
	private final Integer fqCost3 = null;

	private ApplicationConfiguration configuration;

	/**
	 * Empty constructor.
	 */
	public SolrRequestGenerator() {
		this.vtaSyntaxQueryConfigurations = new HashMap<>();
	}

	/**
	 * Constructor.
	 *
	 * @param metrics list of metrics to calculate for text elemenets facet
	 */
	public SolrRequestGenerator(List<FieldMetric> metrics) {
		this.vtaSyntaxQueryConfigurations = new HashMap<>();
		this.textElementsFacetMetricFields = metrics;
	}

	/**
	 * Initialization method.
	 */
	public void initialize() {
		try {
			this.getInteractionsPredefinedFieldsList();

			this.configuration = configurationManager.getApplicationConfiguration();
		} catch (Exception ex) {
			logger.error("Failure during generation of interactions fields list.", ex);
		}
	}

	@Override
	public RestRequestPathsAndQueryParams getSearchInteractionsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, int pageStart, int pageSize, String sortProperty, String sortDirection) {

		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(true);

		try {
			boolean queryExists = false;

			this.validateParameters(tenant, channel, searchContext, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);

			// Either free text search or Tagged Elements search exists
			queryExists = addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, true);

			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);
			addInteractionFieldsList(restRequestPathsAndQueryParams, tenant, channel, queryExists, 1);
			addSort(restRequestPathsAndQueryParams, sortProperty, sortDirection);
			addStartOfRows(pageStart, restRequestPathsAndQueryParams);
			addNumberOfRows(pageSize, restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public StoredSearchQuery getSearchInteractionsQueryForCategory(String tenant, String channel, SearchInteractionsContext searchContext, String language, boolean isEncoded) {
		StoredSearchQuery storedSearchQuery = new StoredSearchQuery();
		StringBuilder encodedQuery = new StringBuilder();
		StringBuilder debugQuery = new StringBuilder();

		try {
			val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(true);

			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, null);
			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, null);
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			MultivaluedStringMap searchQueryParams = restRequestPathsAndQueryParams.getQueryParams();

			// add query parameters
			if (queryParams != null) {
				int searchQueryParamsSize, queryParamsValuesSize;

				searchQueryParamsSize = searchQueryParams.keySet().size();
				for (String queryParamKey : searchQueryParams.keyList()) {

					Collection<String> queryParamsValues = searchQueryParams.get(queryParamKey);
					queryParamsValuesSize = queryParamsValues.size();

					for (String queryParamsValue : queryParamsValues) {
						debugQuery.append(String.format("%s=%s", queryParamKey, queryParamsValue));
						encodedQuery.append(String.format("%s=%s", queryParamKey, UriUtils.encodeQueryParam(queryParamsValue)));

						queryParamsValuesSize--;

						if (queryParamsValuesSize != 0) {
							debugQuery.append(amp);
							encodedQuery.append(amp);
						}
					}

					searchQueryParamsSize--;
					if (searchQueryParamsSize != 0) {
						debugQuery.append(amp);
						encodedQuery.append(amp);
					}
				}
			}

			storedSearchQuery.setDebugQuery(debugQuery.toString());
			storedSearchQuery.setQuery(encodedQuery.toString());
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return storedSearchQuery;
	}

	@Override
	public RestRequestPathsAndQueryParams getFacetQuery(String tenant, String channel, SearchInteractionsContext searchContext, String facetQueryField, List<FieldMetric> facetMetrics, Integer limit, String language) {
		return getFacetQuery(tenant, channel, searchContext, facetQueryField, facetMetrics, limit, language, false);
	}

	// Currently Facets are expected to run on metadata fields on interactions level only.
	@Override
	public RestRequestPathsAndQueryParams getFacetQuery(String tenant, String channel, SearchInteractionsContext searchContext, String facetQueryField, List<FieldMetric> facetMetrics, Integer limit, String language, boolean preventExclude) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, searchContext, language);

			ResultSetGenerationType resultSetGenerationType = ResultSetGenerationType.ParentResultSet;

			// get the hierarchy level of the field
			if (this.textEngineConfigurationService.isParentDocumentField(tenant, channel, facetQueryField)) {
				resultSetGenerationType = ResultSetGenerationType.ParentResultSet;
			} else {
				throw new TextQueryGenerationException(TextQueryGenerationErrorCode.FacetOnUtteranceLevelField);
			}

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);

			// filters and terms
			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, resultSetGenerationType, false);

			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, resultSetGenerationType, fqCost2);
			addDateRange(searchContext, restRequestPathsAndQueryParams, resultSetGenerationType, fqCost3);
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), facetQueryField, resultSetGenerationType);

			//@formatter:off
			FacetQuery facetQuery = new FacetQuery();

			boolean excludeFilterField = false;
			// if filter fields contain the field of faceting, then exclusion indication should be added to facet 
			if (searchContext.getFilterFields() != null && !preventExclude) {
				val facetFieldPresentInFilters = searchContext.getFilterFields().stream()
																				.filter(f -> !f.getLocked() && facetQueryField.equalsIgnoreCase(f.getName()))
																				.findFirst();

				excludeFilterField = facetFieldPresentInFilters.isPresent();
			}

			NestedTermsFacet termsFacet = new NestedTermsFacet();
			termsFacet.setQueryType(FacetQueryType.Terms)
			          .setMethodType(FacetMethodType.dv)
					  .setFieldName(facetQueryField)
					  .setAddExcludeTag(excludeFilterField)
					  .setAlias(String.format("facetFor%s", facetQueryField))
					  .setMinCount(this.queryParams.getFacetMinCount())
					  .setLimit(limit != null ? limit.intValue() : this.queryParams.getFacetLimit());
			//@formatter:on

			// if metrics should be calculated for facet
			if (!CollectionUtils.isEmpty(facetMetrics)) {
				FacetQueryStat facetQueryStat;
				for (FieldMetric metric : facetMetrics) {
					facetQueryStat = new FacetQueryStat(metric.getName(), metric.getFieldName(), metric.getStatFunction().toString());
					termsFacet.addFacetStat(facetQueryStat);
				}
			}

			facetQuery.addNestedFacet(termsFacet);
			addJsonFacet(facetQuery.toJsonString(true, false), restRequestPathsAndQueryParams);

			// formatting and number of rows
			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}


	//@formatter:off
	@Override
	public RestRequestPathsAndQueryParams getTextElementsFacetWithStatsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
																			 String textElementPrefix, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric, SpeakerQueryType speaker, boolean leavesOnly, int elementsLimit) {
		//@formatter:on

		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(false);

		try {
			this.validateParameters(tenant, channel, searchContext, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);

			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);

			// filters params
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			// add Speaker filter (to support getting facet only for specific speaker)
			// addSpeakerFilter(restRequestPathsAndQueryParams.getQueryParams(), speaker);

			String facetFieldName = "";
			switch (textElementType) {
				case Entities:
					// if "Leaves" only is required, then facet should be performed on "topics" and not "topics_f"
					// as "topics" has the actual values, not abstract groups
					facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_total : TAConstants.SchemaFieldNames.topics_f_total;

					switch (speaker) {
						case Agent:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_agent : TAConstants.SchemaFieldNames.topics_f_agent;
							break;
						case Customer:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_customer : TAConstants.SchemaFieldNames.topics_f_customer;
							break;
						case Automated:
						default:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_total : TAConstants.SchemaFieldNames.topics_f_total;
					}
					break;

				case Relations:

					// if "Leaves" only is required, then facet should be performed on "relations" and not "relations_f"
					// as "relations" has the actual values, not abstract groups
					switch (speaker) {
						case Agent:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.relations_f_agent : TAConstants.SchemaFieldNames.relations_f_agent;
							break;
						case Customer:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.relations_f_customer : TAConstants.SchemaFieldNames.relations_f_customer;
							break;
						case  Automated:
						default:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.relations_total : TAConstants.SchemaFieldNames.relations_f_total;
					}
					break;
				default:
					throw new IllegalArgumentException(TAConstants.ErrorMessages.textElementTypeInvalid);
			}

			String sortBy = "", sortDirection = "";

			List<FacetQueryStat> statsToCalculate = null;
			// Sentiment is calculated in separate API, exclude it from facet stats
			if (!CollectionUtils.isEmpty(metricsToCalc)) {
				metricsToCalc = metricsToCalc.stream().filter(m -> !m.name().equals(TextElementMetricType.AvgSentiment.name())).collect(toList());
				statsToCalculate = this.getFacetStatsToCalculate(metricsToCalc);
			}

			Pair<String, String> statToOrderBy = this.getFacetOrderStat(orderMetric);
			if (statToOrderBy != null) {
				sortBy = statToOrderBy.getLeft();
				sortDirection = statToOrderBy.getRight();
			}

			FacetQuery facet = new FacetQuery();

			NestedTermsFacet textElementsFacet = new NestedTermsFacet();

			// when "Leaves" only enabled, no need to add prefix
			if (!StringUtils.isNullOrBlank(textElementPrefix)) {
				textElementsFacet.setPrefix(String.format(pattenInParet, textElementPrefix));
			}

			//@formatter:off
			textElementsFacet.setAlias(TAConstants.FacetQuery.textElementFacetAlias)
			                 .setQueryType(FacetQueryType.Terms)
			                 .setMethodType(FacetMethodType.dv)
			                 .setFieldName(facetFieldName)
			                 .setLimit(elementsLimit);

			if (!StringUtils.isNullOrBlank(sortBy)) {
				textElementsFacet.setSortBy(sortBy)
				                 .setSortDirection(sortDirection);
			}
			//@formatter:on

			if (!CollectionUtils.isEmpty(statsToCalculate)) {
				textElementsFacet.getFacetStats().addAll(statsToCalculate);
			}

			facet.addNestedFacet(textElementsFacet);


			addJsonFacet(facet.toJsonString(true, false), restRequestPathsAndQueryParams);

			// formatting and number of rows
			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	//@formatter:off
	@Override
	public RestRequestPathsAndQueryParams getTextElementsChildrenFacetWithStatsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
																			         List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
																					 SpeakerQueryType speaker, boolean leavesOnly, int elementsLimit) {
		//@formatter:on

		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(false);

		try {
			this.validateParameters(tenant, channel, searchContext, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);

			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);

			// filters params
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			// when 2 or lower levels are required, filter for Text Element can be added
			if (!CollectionUtils.isEmpty(textElements)) {

				String fieldName = "";
				switch (textElementType) {
					case Entities:
						// if "Leaves" only is required, then facet should be performed on "topics" and not "topics_f"
						// as "topics" has the actual values, not abstract groups
						fieldName = TAConstants.SchemaFieldNames.topics_f_total;
						switch (speaker) {
							case Agent:
								fieldName = TAConstants.SchemaFieldNames.topics_f_agent;
								break;
							case Customer:
								fieldName = TAConstants.SchemaFieldNames.topics_f_customer;
								break;
							case Automated:
							default:
								fieldName = TAConstants.SchemaFieldNames.topics_f_total;
						}
						break;
					case Relations:
						// if "Leaves" only is required, then facet should be performed on "relations" and not "relations_f"
						// as "relations" has the actual values, not abstract groups
						switch (speaker) {
							case Agent:
								fieldName = TAConstants.SchemaFieldNames.relations_f_agent;
								break;
							case Customer:
								fieldName = TAConstants.SchemaFieldNames.relations_f_customer;
								break;
							case  Automated:
							default:
								fieldName = TAConstants.SchemaFieldNames.relations_f_total;
						}
						break;
					default:
						throw new IllegalArgumentException(TAConstants.ErrorMessages.textElementTypeInvalid);
				}

				FilterField f = new FilterField();
				f.setName(fieldName);
				f.setDataType(FieldDataType.Text);

				List<FilterFieldValue> values = textElements.stream().map(te -> new FilterFieldValue(te.getValue())).collect(toList());
				FilterFieldValue[] valuesArray = new FilterFieldValue[values.size()];
				f.setValues(values.toArray(valuesArray));

				this.addFilters(tenant, channel, Arrays.asList(f), restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);
			}

			// formatting and number of rows
			addFormatedResults(restRequestPathsAndQueryParams);
			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);

			String facetFieldName = "";
			switch (textElementType) {
				case Entities:
					// if "Leaves" only is required, then facet should be performed on "topics" and not "topics_f"
					// as "topics" has the actual values, not abstract groups
					facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_total : TAConstants.SchemaFieldNames.topics_f_total;
					switch (speaker) {
						case Agent:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_agent : TAConstants.SchemaFieldNames.topics_f_agent;
							break;
						case Customer:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_customer : TAConstants.SchemaFieldNames.topics_f_customer;
							break;
						case Automated:
						default:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_total : TAConstants.SchemaFieldNames.topics_f_total;
					}
					break;
				case Relations:
					// if "Leaves" only is required, then facet should be performed on "relations" and not "relations_f"
					// as "relations" has the actual values, not abstract groups
					switch (speaker) {
						case Agent:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.relations_f_agent : TAConstants.SchemaFieldNames.relations_f_agent;
							break;
						case Customer:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.relations_f_customer : TAConstants.SchemaFieldNames.relations_f_customer;
							break;
						case  Automated:
						default:
							facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.relations_total : TAConstants.SchemaFieldNames.relations_f_total;
					}
					break;
				default:
					throw new IllegalArgumentException(TAConstants.ErrorMessages.textElementTypeInvalid);
			}

			String sortBy = "", sortDirection = "";

			List<FacetQueryStat> statsToCalculate = null;
			// Sentiment is calculated in separate API, exclude it from facet stats
			if (!CollectionUtils.isEmpty(metricsToCalc)) {
				metricsToCalc = metricsToCalc.stream().filter(m -> !m.name().equals(TextElementMetricType.AvgSentiment.name())).collect(toList());
				statsToCalculate = this.getFacetStatsToCalculate(metricsToCalc);
			}

			Pair<String, String> statToOrderBy = this.getFacetOrderStat(orderMetric);
			if (statToOrderBy != null) {
				sortBy = statToOrderBy.getLeft();
				sortDirection = statToOrderBy.getRight();
			}

			FacetQuery facet = new FacetQuery();

			// Second level query
			for (int i = 0; i < textElements.size(); i++) {
				TextElementsFacetNode taggedTextElement = textElements.get(i);

				if (!StringUtils.isNullOrBlank(taggedTextElement.getChildrenPrefix())) {

					NestedTermsFacet queryFacet = new NestedTermsFacet();
					queryFacet.setPrefix(String.format(pattenInParet, taggedTextElement.getChildrenPrefix()));

					//@formatter:off
					queryFacet.setAlias(TAConstants.FacetQuery.textElementFacetAlias + i)
					          .setQueryType(FacetQueryType.Terms)
					          .setMethodType(FacetMethodType.dv)
					          .setFieldName(facetFieldName)
					          .setLimit(elementsLimit);

					if (!StringUtils.isNullOrBlank(sortBy)) {
						queryFacet.setSortBy(sortBy)
							       .setSortDirection(sortDirection);
					}
					//@formatter:on

					if (!CollectionUtils.isEmpty(statsToCalculate)) {
						queryFacet.getFacetStats().addAll(statsToCalculate);
					}

					facet.addNestedFacet(queryFacet);
				}
			}

			addJsonFacet(facet.toJsonString(true, false), restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public Pair<RestRequestPathsAndQueryParams, Map<String, String>> getTextElementsMetricsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, Boolean leavesOnly) {
		Map<String, String> textElementsFacetAliases = new HashMap<>();

		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(false);

		try {
			this.validateParameters(tenant, channel, searchContext, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);

			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);

			// filters params
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			String facetFieldName = "";
			switch (textElementType) {
				case Entities:
					facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.topics_total : TAConstants.SchemaFieldNames.topics_f_total;
					break;
				case Relations:
					facetFieldName = leavesOnly ? TAConstants.SchemaFieldNames.relations_total : TAConstants.SchemaFieldNames.relations_f_total;
					break;
				default:
					throw new IllegalArgumentException(TAConstants.ErrorMessages.textElementTypeInvalid);
			}

			// add Text Element Value to the query
			if (!CollectionUtils.isEmpty(textElements)) {

				FilterField f = new FilterField();
				f.setName(facetFieldName);
				f.setDataType(FieldDataType.Text);

				FilterFieldValue[] values = new FilterFieldValue[textElements.size()];
				int i = 0;
				for (TextElementsFacetNode textElement : textElements) {
					FilterFieldValue value = new FilterFieldValue(leavesOnly ? textElement.getLeaveValue() : textElement.getValue());
					values[i] = value;
					i++;
				}

				f.setValues(values);

				this.addFilters(tenant, channel, Arrays.asList(f), restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);
			}

			// formatting and number of rows
			addFormatedResults(restRequestPathsAndQueryParams);
			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);

			List<FacetQueryStat> statsToCalculate = null;
			// Sentiment is calculated in separate API, exclude it from facet stats
			if (!CollectionUtils.isEmpty(metricsToCalc)) {
				metricsToCalc = metricsToCalc.stream().filter(m -> !m.name().equals(TextElementMetricType.AvgSentiment.name())).collect(toList());
				statsToCalculate = this.getFacetStatsToCalculate(metricsToCalc);
			}

			// add stats to Facet Global stats
			FacetQuery facet = new FacetQuery();
			if (!CollectionUtils.isEmpty(textElements)) {

				int i = 0;
				for (TextElementsFacetNode textElement : textElements) {
					NestedQueryFacet queryFacet = new NestedQueryFacet();
					queryFacet.setQuery(String.format("%s:(\\\"%s\\\")", facetFieldName, leavesOnly ? textElement.getLeaveValue() : textElement.getValue()));

					queryFacet.setAlias(TAConstants.FacetQuery.textElementFacetAlias + i);
					queryFacet.setMethodType(FacetMethodType.dv);
					queryFacet.setLimit(-1);

					// create mapping between Facet aliases and values of text elements
					textElementsFacetAliases.put(queryFacet.getAlias(), textElement.getValue());

					if (!CollectionUtils.isEmpty(statsToCalculate)) {
						queryFacet.getFacetStats().addAll(statsToCalculate);
					}

					facet.addNestedFacet(queryFacet);
					i++;
				}
			}

			addJsonFacet(facet.toJsonString(true, false), restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return new ImmutablePair<>(restRequestPathsAndQueryParams, textElementsFacetAliases);
	}

	/**
	 * Generates Entities facet request using Solr 5 Facet JSON API.
	 *
	 * @param tenant               tenant
	 * @param channel              channel
	 * @param searchContext        searchContext searchContext
	 * @param language             language
	 * @param textElementType      textElement
	 * @param hierarchyLevelNumber hierarchy level to retrieve
	 * @param textElements         text elements to retrieve children
	 * @param metricsToCalc        metrics to calculate
	 * @param orderMetric          order metric
	 * @param speakerType          speaker
	 * @param elementsLimit        limit on number of elements
	 * @return request parameters
	 */
	@Override
	public RestRequestPathsAndQueryParams getTextElementsFacetWithStatsOnSameUtteranceQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, int hierarchyLevelNumber, List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric, SpeakerQueryType speakerType, int elementsLimit) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(false);

		try {
			this.validateParameters(tenant, channel, searchContext, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSearchTermsSameUtteranceMode(searchContext, speakerType, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, true);

			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);

			// filters params
			addFiltersOnSameUtterance(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			//@formatter:off
			FacetQuery facet = new FacetQuery();
			NestedTermsFacet textElementsFacet = new NestedTermsFacet();

			switch (textElementType) {
				case Entities:
					textElementsFacet.setAlias(TAConstants.FacetQuery.textElementFacetAlias)
					     .setQueryType(FacetQueryType.Terms)
					     .setMethodType(FacetMethodType.dv)
					     .setFieldName(TAConstants.SchemaFieldNames.topics_f_total)
					     .setLimit(this.configuration.getTextElementsFacetWithStatsFirstLevelLimit());
					break;
				case Relations:
					textElementsFacet.setAlias(TAConstants.FacetQuery.textElementFacetAlias)
					     .setQueryType(FacetQueryType.Terms)
					     .setMethodType(FacetMethodType.dv)
					     .setFieldName(TAConstants.SchemaFieldNames.relations_f_total)
					     .setLimit(this.configuration.getTextElementsFacetWithStatsFirstLevelLimit());
					break;
				default:
					throw new IllegalArgumentException("textElement is not defined");
			}
			//@formatter:on

			if (!CollectionUtils.isEmpty(metricsToCalc)) {
				for (TextElementMetricType metric : metricsToCalc) {
					// find "sizeBy" metric in the list of available metrics
					Optional<FieldMetric> sizeByStatsOpt = this.textElementsFacetMetricFields.stream().filter(m -> m.getName().equals(metric.name())).findFirst();
					if (sizeByStatsOpt.isPresent()) {
						FieldMetric metricField = sizeByStatsOpt.get();

						// add stat to Facet stats
						val facetQueryStat = new FacetQueryStat(metricField.getName(), metricField.getFieldName(), metricField.getStatFunction().toString());
						textElementsFacet.addFacetStat(facetQueryStat);
					}
				}
			}

			if (orderMetric != null) {
				// find "sizeBy" metric in the list of available metrics
				Optional<FieldMetric> colorByStatsOpt = this.textElementsFacetMetricFields.stream().filter(m -> m.getName().equals(orderMetric.name())).findFirst();
				if (colorByStatsOpt.isPresent()) {
					FieldMetric metricField = colorByStatsOpt.get();
					// add stat to Facet stats
					val facetQueryStat = new FacetQueryStat(metricField.getName(), metricField.getFieldName(), metricField.getStatFunction().toString());
					textElementsFacet.addFacetStat(facetQueryStat);
				}
			}

			facet.addNestedFacet(textElementsFacet);
			addJsonFacet(facet.toJsonString(true, false), restRequestPathsAndQueryParams);

			// formatting and number of rows
			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	private String addJsonFacetByInteractions(List<TextElementType> textElements, boolean prettyFormat) {

		if (textElements.size() == 0) {
			return "";
		}
		String textElementSchemaFieldName = "";

		switch (textElements.get(0)) {
			case Entities:
				textElementSchemaFieldName = TAConstants.SchemaFieldNames.topics_f;
				break;
			case Relations:
				textElementSchemaFieldName = TAConstants.SchemaFieldNames.relations_f;
				break;
			default:
				throw new IllegalArgumentException("textElement is not defined");
		}

		String nl = prettyFormat ? System.getProperty("line.separator") : "";

		StringBuilder queryJson = new StringBuilder();

		queryJson.append(nl + curlyOpenBracket + nl);

		queryJson.append(tab + "interactions: \"unique(parentID)\"," + nl);

		queryJson.append(tab + textElementSchemaFieldName + ": " + curlyOpenBracket + nl);

		// facet type
		queryJson.append(tab2 + String.format("type : %s, %s", "terms", nl));

		// field name
		queryJson.append(tab2 + String.format("field : %s, %s", textElementSchemaFieldName, nl));

		// add limit
		queryJson.append(tab2 + String.format("limit : %s, %s", "10", nl));

		// add prefix for level 2
		queryJson.append(tab2 + String.format("prefix : %s,", "\"2/\""));
		if (textElements.size() > 1) {
			queryJson.append("facet:" + curlyOpenBracket + nl);
			queryJson.append("getInteractionParent:" + curlyOpenBracket + nl);
			queryJson.append("type:query," + nl);
			queryJson.append("domain:{blockParent: \"content_type:PARENT\"}," + nl);
			queryJson.append("facet:" + curlyOpenBracket + nl);
			queryJson.append("getUtterances:" + curlyOpenBracket + nl);
			queryJson.append("type:query," + nl);
			queryJson.append("domain:{blockChildren: \"content_type:PARENT\"}" + nl);

			queryJson.append(String.format(", %s", nl));
			// add sub facet
			String subFacet = this.addJsonFacetByInteractions(textElements.subList(1, textElements.size()), prettyFormat);
			queryJson.append(tab2 + String.format("facet : %s %s", subFacet, nl));

			queryJson.append(curlyCloseBracket + nl);
			queryJson.append(curlyCloseBracket + nl);
			queryJson.append(curlyCloseBracket + nl);
			queryJson.append(curlyCloseBracket + nl);
		} else {
			queryJson.append("facet:" + curlyOpenBracket + nl);
			queryJson.append("interactions: \"unique(parentID)\"" + nl);
			queryJson.append(tab + curlyCloseBracket + nl);
		}

		queryJson.append(tab + curlyCloseBracket + nl);
		queryJson.append(nl + curlyCloseBracket + nl);

		return queryJson.toString();

	}

	/***
	 * This facet api json will return sub facet quantities for utterances level
	 * query.
	 *
	 * @param textElements
	 * @param prettyFormat
	 * @return
	 */
	private String addJsonFacetSameUtterance(List<TextElementType> textElements, boolean prettyFormat) {

		if (textElements.size() == 0) {
			return "";
		}
		String textElementSchemaFieldName = "";

		switch (textElements.get(0)) {
			case Entities:
				textElementSchemaFieldName = TAConstants.SchemaFieldNames.topics_f;
				break;
			case Relations:
				textElementSchemaFieldName = TAConstants.SchemaFieldNames.relations_f;
				break;
			default:
				throw new IllegalArgumentException("textElement is not defined");
		}

		String nl = prettyFormat ? System.getProperty("line.separator") : "";

		StringBuilder queryJson = new StringBuilder();

		queryJson.append(nl + curlyOpenBracket + nl);

		queryJson.append(tab + textElementSchemaFieldName + ": " + curlyOpenBracket + nl);

		// facet type
		queryJson.append(tab2 + String.format("type : %s, %s", "terms", nl));

		// field name
		queryJson.append(tab2 + String.format("field : %s, %s", textElementSchemaFieldName, nl));

		// add limit
		queryJson.append(tab2 + String.format("limit : %s, %s", "10", nl));

		// add prefix for level 2
		queryJson.append(tab2 + String.format("prefix : %s", "\"2/\""));

		if (textElements.size() > 1) {
			queryJson.append(String.format(", %s", nl));
			// add sub facet
			String subFacet = this.addJsonFacetSameUtterance(textElements.subList(1, textElements.size()), prettyFormat);
			queryJson.append(tab2 + String.format("facet : %s %s", subFacet, nl));

		}

		queryJson.append(tab + curlyCloseBracket + nl);
		queryJson.append(nl + curlyCloseBracket + nl);

		return queryJson.toString();

	}

	private void addSpeakerFilter(MultivaluedStringMap params, SpeakerQueryType speaker) {

		if (speaker != null) {
			switch (speaker) {
				case Automated:
					params.add(fq, String.format(filterFieldQueryTemplate, TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Automated));
					break;
				case Agent:
					params.add(fq, String.format(filterFieldQueryTemplate, TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Agent));
					break;
				case Customer:
					params.add(fq, String.format(filterFieldQueryTemplate, TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Customer));
					break;
				default:
					//add nothing
					break;
			}
		}
	}

	@Override
	public RestRequestPathsAndQueryParams getTotalInteractionsQuantityQuery(String tenant, String channel, SearchInteractionsContext searchContext) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, searchContext);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSelectingEverything(restRequestPathsAndQueryParams);
			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);

			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getCurrentSearchInteractionsQuantityQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language) {

		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, searchContext, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);
			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getTermsSuggestionsForAutocompleteQuery(String tenant, String channel, SearchInteractionsContext searchContext, String suggestionValue, String language) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();

			this.validateParameters(tenant, channel, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);

			// add suggestion prefix to Search Context
			if (!StringUtils.isNullOrBlank(suggestionValue)) {
				List<String> terms = searchContext.getTerms();
				terms.add(String.format("%s*", suggestionValue));
			}

			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);
			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);

			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			int limit = this.configuration.getAutoCompleteLimitForQuery();

			// add SimpleFacet query
			this.addSuggestionsFacet(restRequestPathsAndQueryParams, suggestionValue, language, limit, appConfig.getTermsSuggestionsFacetThreadsLimit());

			addNumberOfRows(0, restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}
		return restRequestPathsAndQueryParams;
	}

	/**
	 * Parses Search Query and checks it's validity.
	 *
	 * @param searchQuery search terms expression
	 * @param language    language
	 */
	@Override
	public void validateSearchQuery(String searchQuery, String language) {
		this.vtaSyntaxAnalyzer.parseQuery(searchQuery, this.getVTASyntaxTASQueryConfiguration(language));
	}

	@Override
	public RestRequestPathsAndQueryParams getInteractionPreviewQuery(String tenant, String channel, String interactionId) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(true);

		try {
			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSelectingEverything(restRequestPathsAndQueryParams);
			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);

			val filterField = new FilterField();
			filterField.setDataType(FieldDataType.Text);
			filterField.setName(TAConstants.SchemaFieldNames.documentId);
			filterField.setValues(new FilterFieldValue[] { new FilterFieldValue(interactionId, "") });

			addFilters(tenant, channel, Arrays.asList(filterField), restRequestPathsAndQueryParams.getQueryParams(), null,
			           ResultSetGenerationType.FilterParentGenerateParentResultSet);

			addInteractionFieldsList(restRequestPathsAndQueryParams, tenant, channel, false, this.queryParams.getChildDocumentsForParentLimit());
			// interactions review should retrieve only one document
			addNumberOfRows(1, restRequestPathsAndQueryParams);

			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getInteractionsHighlightsQuery(String tenant, String channel, List<FilterFieldValue> parentDocumentIds, SpeakerType speaker, List<QueryTerm> terms, String language) {

		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams(true);

		try {
			if (parentDocumentIds != null && parentDocumentIds.size() > 0) {

				addTenant(tenant, restRequestPathsAndQueryParams);
				addSelect(restRequestPathsAndQueryParams);
				addSelectingEverything(restRequestPathsAndQueryParams);
				addChannel(channel, restRequestPathsAndQueryParams);
				addHighlighting(language, restRequestPathsAndQueryParams);
				addTermsForHighlighting(terms, restRequestPathsAndQueryParams);

				addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ChildResultSet, fqCost2);

				// add document IDs as filter
				val filterField = new FilterField();
				filterField.setName(TAConstants.SchemaFieldNames.documentId);
				filterField.setDataType(FieldDataType.Text);

				FilterFieldValue[] documentIds = new FilterFieldValue[parentDocumentIds.size()];
				filterField.setValues(parentDocumentIds.toArray(documentIds));
				addFilters(tenant, channel, Arrays.asList(filterField), restRequestPathsAndQueryParams.getQueryParams(), null,
				           ResultSetGenerationType.FilterParentGenerateChildResultSet);

				// The Speaker type filter should be added only if Agent or
				// Customer speaker type
				if (speaker == SpeakerType.Agent || speaker == SpeakerType.Customer) {
					val speakerFilterField = new FilterField();
					speakerFilterField.setName(TAConstants.SchemaFieldNames.speakerType);
					speakerFilterField.setDataType(FieldDataType.Text);
					speakerFilterField.setValues(new FilterFieldValue[] { new FilterFieldValue(speaker.toString(), "") });
					addFilters(tenant, channel, Arrays.asList(speakerFilterField), restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ChildResultSet);
				}

				addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSize(), restRequestPathsAndQueryParams);
				addFormatedResults(restRequestPathsAndQueryParams);
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getTextElementsTrendsQuery(String tenant, String channel, TrendType trendType, String periodName, String baseDate, String textElementValue, String sortProperty, String sortDirection, int limitTo, SpeakerQueryType speaker) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateTrendsParameters(tenant, channel, periodName);
			val bypassTrendsThreshold = configuration.getBypassTrendsThreshold();

			addTenant(tenant, restRequestPathsAndQueryParams);
			addChannelPath(channel, restRequestPathsAndQueryParams);
			addTrends(restRequestPathsAndQueryParams);
			addTrendsField(restRequestPathsAndQueryParams, trendType);
			addTrendsPeriod(periodName, restRequestPathsAndQueryParams);
			addTrendsLimit(restRequestPathsAndQueryParams, limitTo);
			addTrendsSpeaker(restRequestPathsAndQueryParams, speaker);

			// Changes the BaseDate according to trend period
			// Set the resolution of date to 1 minute
			if (baseDate == null) {
				addTrendsBaseDate(restRequestPathsAndQueryParams, String.format("%s/MINUTE", TAConstants.TrendsBaseDate.TrendNow));
			} else {
				addTrendsBaseDate(restRequestPathsAndQueryParams, String.format("%s/MINUTE", baseDate));
			}

			if (bypassTrendsThreshold != null && bypassTrendsThreshold.equals("true")) {
				addBypassTrendsThresholdsMinTotalDocs(restRequestPathsAndQueryParams);
				addBypassTrendsThresholdsMinTotalDocsPercentage(restRequestPathsAndQueryParams);
			}
			addTrendsSortBy(restRequestPathsAndQueryParams, sortProperty, sortDirection);

			addSelectingEverything(restRequestPathsAndQueryParams);

			switch (trendType) {
				case Categories:
					// don't add prefix for categories
					break;
				default:
					addTextElementPrefix(restRequestPathsAndQueryParams, this.getTextElementNextPath(textElementValue));
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getInteractionsDailyVolumeSeriesQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSearchTerms(searchContext, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);

			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addChannel(channel, restRequestPathsAndQueryParams);

			addDateRange(searchContext, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);
			addFilters(tenant, channel, searchContext, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			addFormatedResults(restRequestPathsAndQueryParams);
			addNumberOfRows(0, restRequestPathsAndQueryParams);

			FacetQuery dateRangeFacet = this.getInteractionsDailyVolumeFacet(searchContext);
			addJsonFacet(dateRangeFacet.toJsonString(true, false), restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getTrendInteractionsDailyVolumeSeriesQueryByType(String tenant, String channel, TrendType trendType, String entityValue, SpeakerQueryType speaker) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, "en");

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSelectingEverything(restRequestPathsAndQueryParams);

			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addChannel(channel, restRequestPathsAndQueryParams);

			val filterField = new FilterField();
			filterField.setDataType(FieldDataType.Text);
			filterField.setName(this.getFieldNameNotStoredByTrendType(trendType, speaker));
			filterField.setValues(new FilterFieldValue[] { new FilterFieldValue(entityValue, "") });

			addFilters(tenant, channel, Arrays.asList(filterField), restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			FacetQuery dateRangeFacet = this.getInteractionsDailyVolumeFacet(null);
			addJsonFacet(dateRangeFacet.toJsonString(true, false), restRequestPathsAndQueryParams);

			addNumberOfRows(0, restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getResultSetMetricsQuery(String tenant, String channel, SearchInteractionsContext context, String language, List<FieldMetric> fieldsMetrics) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, context, language);

			ResultSetGenerationType resultSetGenerationType = ResultSetGenerationType.ParentResultSet;

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSearchTerms(context, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);

			addResultType(restRequestPathsAndQueryParams, resultSetGenerationType, fqCost2);
			addChannel(channel, restRequestPathsAndQueryParams);

			addDateRange(context, restRequestPathsAndQueryParams, resultSetGenerationType, fqCost3);
			addFilters(tenant, channel, context, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			List<FacetQueryStat> facetQueryStatList = new ArrayList<FacetQueryStat>();

			for (FieldMetric fieldMetric : fieldsMetrics) {
				facetQueryStatList.add(new FacetQueryStat(fieldMetric.getName(), fieldMetric.getFieldName(), fieldMetric.getStatFunction().toString()));
			}

			addJsonFacet(toJsonString(facetQueryStatList, false), restRequestPathsAndQueryParams);

			// formatting and number of rows
			addNumberOfRows(this.queryParams.getSearchInteractionsResultSetSizeInFacetQueries(), restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getTextElementsChildrenSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, TextElementMetricType orderField, int elementsLimit) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, context, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);

			addSearchTerms(context, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);
			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(context, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);
			addFilters(tenant, channel, context, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			String facetFieldName = "";
			switch (textElementType) {
				case Entities:
					facetFieldName = TAConstants.SchemaFieldNames.topics_f_total;
					break;
				case Relations:
					facetFieldName = TAConstants.SchemaFieldNames.relations_f_total;
					break;
				default:
					throw new IllegalArgumentException(TAConstants.ErrorMessages.textElementTypeInvalid);
			}

			if (!CollectionUtils.isEmpty(textElements)) {
				// when 2 or lower levels are required, filter for Text Element can be added
				FilterField f = new FilterField();
				f.setName(textElementType == TextElementType.Relations ? TAConstants.SchemaFieldNames.relations_f_total : TAConstants.SchemaFieldNames.topics_f_total);
				f.setDataType(FieldDataType.Text);

				List<FilterFieldValue> values = textElements.stream().map(te -> new FilterFieldValue(te.getValue())).collect(toList());
				FilterFieldValue[] valuesArray = new FilterFieldValue[values.size()];
				f.setValues(values.toArray(valuesArray));

				this.addFilters(tenant, channel, Arrays.asList(f), restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);
			}

			String sortExpression = "";
			if (orderField != null) {
				// find "sizeBy" metric in the list of available metrics
				Optional<FieldMetric> sizeByStatsOpt = this.textElementsFacetMetricFields.stream().filter(m -> m.getName().equals(orderField.name())).findFirst();
				if (sizeByStatsOpt.isPresent()) {
					FieldMetric metricField = sizeByStatsOpt.get();

					// add stat to Facet stats
					sortExpression = String.format("%s(%s) %s", metricField.getStatFunction().toString(), metricField.getFieldName(), desc);
					// add sort parameter
					restRequestPathsAndQueryParams.getQueryParams().add(TAConstants.SentimentAPI.SortParameter, sortExpression);
				}
			}

			List<String> textElementsPrefixes = null;
			if (!CollectionUtils.isEmpty(textElements)) {
				textElementsPrefixes = textElements.stream().map(te -> te.getChildrenPrefix()).collect(Collectors.toList());
			}

			// add prefixes to be used in Facet
			this.addSentimentPrefixes(restRequestPathsAndQueryParams, textElementsPrefixes);

			this.addFormatedResults(restRequestPathsAndQueryParams);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getTextElementsChildrenSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType, String prefix, TextElementMetricType orderField, int elementsLimit) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			this.validateParameters(tenant, channel, context, language);

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);

			addSearchTerms(context, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);
			addChannel(channel, restRequestPathsAndQueryParams);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);
			addDateRange(context, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);
			addFilters(tenant, channel, context, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

			String facetFieldName = "";
			switch (textElementType) {
				case Entities:
					facetFieldName = TAConstants.SchemaFieldNames.topics_f_total;
					break;
				case Relations:
					facetFieldName = TAConstants.SchemaFieldNames.relations_f_total;
					break;
				default:
					throw new IllegalArgumentException(TAConstants.ErrorMessages.textElementTypeInvalid);
			}

			String sortExpression = "";
			if (orderField != null) {
				// find "sizeBy" metric in the list of available metrics
				Optional<FieldMetric> sizeByStatsOpt = this.textElementsFacetMetricFields.stream().filter(m -> m.getName().equals(orderField.name())).findFirst();
				if (sizeByStatsOpt.isPresent()) {
					FieldMetric metricField = sizeByStatsOpt.get();

					// add stat to Facet stats
					sortExpression = String.format("%s(%s) %s", metricField.getStatFunction().toString(), metricField.getFieldName(), desc);
					// add sort parameter
					restRequestPathsAndQueryParams.getQueryParams().add(TAConstants.SentimentAPI.SortParameter, sortExpression);
				}
			}

			// add prefixes to be used in Facet
			this.addSentimentPrefixes(restRequestPathsAndQueryParams, Arrays.asList(prefix));

			this.addFormatedResults(restRequestPathsAndQueryParams);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getTextElementsSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, int elementsLimit) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			if (!CollectionUtils.isEmpty(textElements)) {
				this.validateParameters(tenant, channel, context, language);

				addTenant(tenant, restRequestPathsAndQueryParams);
				addChannel(channel, restRequestPathsAndQueryParams);
				addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);

				addSelect(restRequestPathsAndQueryParams);
				addSearchTerms(context, tenant, channel, language, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, false);
				addDateRange(context, restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost3);
				addFilters(tenant, channel, context, restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

				// when leaves only, filter for Text Element can be added
				FilterField f = new FilterField();
				f.setName(textElementType == TextElementType.Relations ? TAConstants.SchemaFieldNames.relations_f_total : TAConstants.SchemaFieldNames.topics_f_total);
				f.setDataType(FieldDataType.Text);

				List<FilterFieldValue> values = textElements.stream().map(te -> new FilterFieldValue(te.getValue())).collect(toList());
				FilterFieldValue[] valuesArray = new FilterFieldValue[values.size()];
				f.setValues(values.toArray(valuesArray));

				this.addFilters(tenant, channel, Arrays.asList(f), restRequestPathsAndQueryParams.getQueryParams(), null, ResultSetGenerationType.ParentResultSet);

				List<String> textElementsValues = textElements.stream().map(te -> te.getValue()).collect(Collectors.toList());
				// add prefixes to be used in Facet
				this.addSentimentPrefixes(restRequestPathsAndQueryParams, textElementsValues);

				addFormatedResults(restRequestPathsAndQueryParams);
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getCheckSourceTypeInChannelQuery(String tenant, String channel, String sourceType) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		try {
			if (StringUtils.isNullOrBlank(tenant)) {
				throw new IllegalArgumentException(TENANT_PARAMETER_IS_NULL_EMPTY);
			}

			if (StringUtils.isNullOrBlank(channel)) {
				throw new IllegalArgumentException(CHANNEL_PARAMETER_IS_NULL_EMPTY);
			}

			addTenant(tenant, restRequestPathsAndQueryParams);
			addSelect(restRequestPathsAndQueryParams);
			addSelectingEverything(restRequestPathsAndQueryParams);

			addChannel(channel, restRequestPathsAndQueryParams);
			addSourceType(restRequestPathsAndQueryParams, sourceType);
			addResultType(restRequestPathsAndQueryParams, ResultSetGenerationType.ParentResultSet, fqCost2);

			addNumberOfRows(1, restRequestPathsAndQueryParams);
			addFormatedResults(restRequestPathsAndQueryParams);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}

		return restRequestPathsAndQueryParams;
	}

	/**
	 * Generates request to Collection status.
	 *
	 * @param tenant tenant
	 * @return query parameters.
	 */
	public RestRequestPathsAndQueryParams getCollectionStatusRequest(String tenant) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		List<String> reqPathParams = restRequestPathsAndQueryParams.getQueryPaths();
		reqPathParams.add("admin");
		reqPathParams.add("collections");

		MultivaluedStringMap reqQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		reqQueryParams.add("action", "clusterstatus");
		reqQueryParams.add("collection", tenant);
		addFormatedResults(restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	/**
	 * Generates query for suggestions.
	 *
	 * @param tenant                tenant
	 * @param searchQuery           search query
	 * @param suggester             suggester name as defined in solrconfig.xml
	 * @param suggestionsCount      numbner of suggestions to fetch
	 * @param joinedReplicasToQuery list of active replicas in collection
	 * @return http query parameters
	 */
	@Override
	public RestRequestPathsAndQueryParams getFreeTextLookupSuggestionsQuery(String tenant, String searchQuery, String suggester, Integer suggestionsCount, String joinedReplicasToQuery) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		List<String> reqPathParams = restRequestPathsAndQueryParams.getQueryPaths();
		reqPathParams.add(tenant);
		reqPathParams.add("suggest");

		MultivaluedStringMap reqQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		reqQueryParams.add("suggest", "true");
		reqQueryParams.add("shards.qt", "/suggest");
		reqQueryParams.add("shards", joinedReplicasToQuery);
		reqQueryParams.add("suggest.dictionary", suggester);
		reqQueryParams.add("suggest.q", DataUtils.escapeCharsForSolrQuery(searchQuery));
		reqQueryParams.add("suggest.count", suggestionsCount.toString());
		addFormatedResults(restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}


	private void addSuggestionsFacet(RestRequestPathsAndQueryParams requestParams, String suggestionValue, String language, int limit, int threadsCount) {
		if (!StringUtils.isNullOrBlank(suggestionValue)) {
			MultivaluedStringMap reqQueryParams = requestParams.getQueryParams();
			reqQueryParams.add(TAConstants.FacetQuery.simpleFacet, TAConstants.trueLowerCase);
			reqQueryParams.add(TAConstants.FacetQuery.simpleFacetField, String.format(TAConstants.SchemaFieldNames.text_total_format, language));
			reqQueryParams.add(TAConstants.FacetQuery.simpleFacetMethod, SimpleFacetMethod.Enum.toString());
			reqQueryParams.add(TAConstants.FacetQuery.simpleFacetPrefix, suggestionValue);
			reqQueryParams.add(TAConstants.FacetQuery.simpleFacetLimit, String.valueOf(limit));
			reqQueryParams.add(TAConstants.FacetQuery.simpleFacetMinCount, String.valueOf(1));
			reqQueryParams.add(TAConstants.FacetQuery.simpleFacetThreads, String.valueOf(threadsCount));
		}
	}

	private List<FacetQueryStat> getFacetStatsToCalculate(List<TextElementMetricType> metricsToCalc) {
		List<FacetQueryStat> facetStats = new ArrayList<>();

		if (!CollectionUtils.isEmpty(metricsToCalc)) {
			for (TextElementMetricType metric : metricsToCalc) {
				// find "sizeBy" metric in the list of available metrics
				Optional<FieldMetric> sizeByStatsOpt = this.textElementsFacetMetricFields.stream().filter(m -> m.getName().equalsIgnoreCase(metric.name())).findFirst();
				if (sizeByStatsOpt.isPresent()) {
					FieldMetric metricField = sizeByStatsOpt.get();

					// add facet Stat to generate
					facetStats.add(new FacetQueryStat(metricField.getName(), metricField.getFieldName(), metricField.getStatFunction().toString()));
				}
			}
		}

		return facetStats;
	}

	private MutablePair<String, String> getFacetOrderStat(TextElementMetricType orderMetric) {
		MutablePair<String, String> order = new MutablePair<>();

		if (orderMetric != null) {
			// find "sizeBy" metric in the list of available metrics
			Optional<FieldMetric> colorByStatsOpt = this.textElementsFacetMetricFields.stream().filter(m -> m.getName().equals(orderMetric.name())).findFirst();
			if (colorByStatsOpt.isPresent()) {
				FieldMetric metricField = colorByStatsOpt.get();

				// set sorting of facet
				order.setLeft(metricField.getName());
				order.setRight(desc);
			}
		}

		return order;
	}

	private String toJsonString(List<FacetQueryStat> facetQueryStatList, boolean prettyFormat) {
		String nl = prettyFormat ? System.getProperty("line.separator") : "";
		String t = prettyFormat ? this.tab : "";

		StringBuilder queryJson = new StringBuilder();

		queryJson.append(nl + "{" + nl);

		if (facetQueryStatList != null) {
			// add comma after each line
			FacetQueryStat queryStat;
			int i;
			for (i = 0; i < facetQueryStatList.size() - 1; i++) {
				queryStat = facetQueryStatList.get(i);
				queryJson.append(t + String.format("%s : \"%s(%s)\", %s", queryStat.getAlias(), queryStat.getFunction(), queryStat.getField(), nl));
			}
			// add the last one without comma
			queryStat = facetQueryStatList.get(i);
			queryJson.append(t + String.format("%s : \"%s(%s)\"", queryStat.getAlias(), queryStat.getFunction(), queryStat.getField()));
		}

		queryJson.append(nl + "}");

		return queryJson.toString();
	}

	protected void addTenant(String tenant, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		// tenant is going to be added to paths
		// http://{host}:{port}/solr/#/tenant1
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(DataUtils.escapeCharsForSolrQuery(tenant));
	}

	protected void addChannelPath(String channel, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(DataUtils.escapeCharsForSolrQuery(channel));
	}

	protected void addSelect(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("select");
	}

	protected void addQuery(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("query");
	}

	protected void addDataFiller(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("DataFiller");
	}

	protected void addTrends(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("trends");
	}

	protected void addTrendsField(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, TrendType trendType) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(getFieldNameByTrendType(trendType));
	}

	protected void addTrendsSpeaker(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, SpeakerQueryType speaker) {

		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();

		if (speaker == SpeakerQueryType.Customer) {

			requestQueryParams.add("speaker", "customer");

		} else if (speaker == SpeakerQueryType.Agent) {

			requestQueryParams.add("speaker", "agent");

		}

	}

	protected String getFieldNameByTrendType(TrendType trendType) {

		String fieldName;

		switch (trendType) {
			case Entities:
				fieldName = TAConstants.SchemaFieldNames.topics;
				break;
			case Relations:
				fieldName = TAConstants.SchemaFieldNames.relations;
				break;
			case Keyterms:
				fieldName = TAConstants.SchemaFieldNames.keyterms;
				break;
			case Categories:
				fieldName = TAConstants.SchemaFieldNames.categories;
				break;
			default:
				throw new IllegalArgumentException("Trend Type is not defined properly");
		}

		return fieldName;
	}

	protected String getFieldNameNotStoredByTrendType(TrendType trendType, SpeakerQueryType speaker) {
		String fieldName;

		switch (trendType) {
			case Entities:
				if (speaker == SpeakerQueryType.Agent) {
					fieldName = TAConstants.SchemaFieldNames.topics_f_agent;
				} else if (speaker == SpeakerQueryType.Customer) {
					fieldName = TAConstants.SchemaFieldNames.topics_f_customer;
				} else {
					fieldName = TAConstants.SchemaFieldNames.topics_f_total;
				}

				break;
			case Relations:

				if (speaker == SpeakerQueryType.Agent) {
					fieldName = TAConstants.SchemaFieldNames.relations_f_agent;
				} else if (speaker == SpeakerQueryType.Customer) {
					fieldName = TAConstants.SchemaFieldNames.relations_f_customer;
				} else {
					fieldName = TAConstants.SchemaFieldNames.relations_f_total;
				}

				break;
			case Keyterms:

				if (speaker == SpeakerQueryType.Agent) {
					fieldName = TAConstants.SchemaFieldNames.keyterms_f_agent;
				} else if (speaker == SpeakerQueryType.Customer) {
					fieldName = TAConstants.SchemaFieldNames.keyterms_f_customer;
				} else {
					fieldName = TAConstants.SchemaFieldNames.keyterms_f_total;
				}

				break;
			case Categories:
				fieldName = TAConstants.SchemaFieldNames.categories;
				break;
			default:
				throw new IllegalArgumentException("Trend Type is not defined properly");
		}

		return fieldName;
	}

	protected void addTrendsSortBy(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, String sortProperty, String sortDirection) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("sort", sortProperty + " " + sortDirection);
	}

	protected void addTextElementPrefix(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, String prefix) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("prefix", prefix);
	}

	protected  void addSentimentPrefixes(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, List<String> prefixes) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		for (String prefix : prefixes) {
			requestQueryParams.add("prefix", prefix);
		}
	}

	/**
	 * Generates Select everything query.
	 *
	 * @return generated query
	 */
	private String getSelectingEverythingQueryParam() {
		return queryAll;
	}

	protected void addSelectingEverything(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("q", this.getSelectingEverythingQueryParam());
	}

	private String getChannelQueryParam(String channel) {
		return String.format("channel:%s", DataUtils.escapeCharsForSolrQuery(channel));
	}

	protected void addChannel(String channel, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add(fq, this.getChannelQueryParam(channel));
	}

	protected void addChannel(String channel, List<Pair<String, String>> requestParams) {
		requestParams.add(new ImmutablePair<>(fq, this.getChannelQueryParam(channel)));
	}

	protected void addTrendsPeriod(String period, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();

		period = String.format("%s", DataUtils.escapeCharsForSolrQuery(period));
		requestQueryParams.add("period", period);
	}

	protected void addTrendsLimit(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, int limitTo) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("limit", String.valueOf(limitTo));
	}

	protected void addTrendsBaseDate(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, String baseDate) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("baseDate", baseDate);
	}

	protected void addBypassTrendsThresholdsMinTotalDocs(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("minTotalDocs", String.valueOf(0));
	}

	protected void addBypassTrendsThresholdsMinTotalDocsPercentage(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("minTotalDocsPercentage", String.valueOf(0));
	}

	protected void addName(String name, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();

		name = String.format("%s", DataUtils.escapeCharsForSolrQuery(name));
		requestQueryParams.add("name", name);
	}

	private String getResultTypeQueryParam(ResultSetGenerationType resultTypeGenerationType, Integer cost) {
		String resultType = "";

		if (resultTypeGenerationType == ResultSetGenerationType.ChildResultSet) {
			resultType = (cost != null ? String.format(costParser, cost) : "") + String.format(filterFieldQueryTemplate, contentType, child);
		} else {
			resultType = (cost != null ? String.format(costParser, cost) : "") + String.format(filterFieldQueryTemplate, contentType, parent);
		}

		return resultType;
	}

	protected void addResultType(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, ResultSetGenerationType resultTypeGenerationType, Integer cost) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add(fq, this.getResultTypeQueryParam(resultTypeGenerationType, cost));
	}

	protected void addSourceType(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, String sourceType) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add(fq, String.format("Meta_s_interactionType:%s", sourceType));
	}

	protected void addResultType(final List<Pair<String, String>> requestParams, ResultSetGenerationType resultTypeGenerationType, Integer cost) {
		requestParams.add(new ImmutablePair<>(fq, this.getResultTypeQueryParam(resultTypeGenerationType, cost)));
	}

	/**
	 * Adds Search Terms to REST query parameters for each term new "fq"
	 * parameter added.
	 *
	 * @param searchContext
	 * @param language
	 * @param restRequestPathsAndQueryParams
	 * @param resultSetType
	 */
	private boolean addSearchTerms(SearchInteractionsContext searchContext, String tenant, String channel, String language, RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, ResultSetGenerationType resultSetType, Boolean calculateScore) {
		val requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		Boolean termsInSearchContext = false;
		Boolean taggedElementsInSearchContext = false;

		StringBuilder query = new StringBuilder();
		StringBuilder totalQuery = new StringBuilder();

		// generate block join for text field parser
		Pair<String, String> blockJoinParsers = this.generateTextFieldBlockJoinParser(true, calculateScore);
		String blockJoinWithScore = blockJoinParsers.getLeft();
		String blockJoinNoScore = blockJoinParsers.getRight();

		// if filter containt tagged elements : topics_f, relations_f, terms_f ...
		// the query ("q") should containt those elements for scoring
		Pair<Boolean, StringBuilder> taggedTextElementsQuery = this.getTaggedTextElements(tenant, channel, searchContext, and,  calculateScore);
		taggedElementsInSearchContext = taggedTextElementsQuery.getLeft();
		StringBuilder taggedElementsQuery = taggedTextElementsQuery.getRight();

		// Each Chiclet will generate an expression
		// all expressions are connected by AND
		List<String> contextSearchQueries = searchContext.getTerms();
		if (contextSearchQueries != null) {

			int j = 0;
			for (String searchQuery : contextSearchQueries) {
				// parse query and generate query
				VTASyntaxParsingResult queryParsingResult = this.vtaSyntaxAnalyzer.parseQuery(searchQuery, this.getVTASyntaxTASQueryConfiguration(language));

				if (queryParsingResult.getTermsCount() > 0) {
					if (contextSearchQueries.size() > 1) {

						if (queryParsingResult.getIsNOTExpression()){
							// the expression is NOT expression, might be complex but top operator is NOT
							query.append(String.format(j > 0 ? " %s" : "%s", queryParsingResult.getSolrQuery()));
						} else {
							// the top operator is some other operator then NOT
							query.append(String.format(j > 0 ? "  +(%s)" : "+(%s)", queryParsingResult.getSolrQuery()));
						}

					} else {
						query.append(queryParsingResult.getSolrQuery());
					}

					termsInSearchContext = true;
				}
				j++;
			}
		}

		if (calculateScore) {
			// If Terms present in search context or Relations, Entities or Keywords
			if (termsInSearchContext || taggedElementsInSearchContext) {
				totalQuery.append(blockJoinWithScore + luceneQueryParser);
			} else {
				totalQuery.append(luceneQueryParser);
			}

			if (termsInSearchContext) {
				totalQuery.append(query.toString());
			}

			// if tagged elements were present in Search Context, add those with "AND" to query to impact the scoring
			if (taggedElementsInSearchContext) {
				totalQuery.append(termsInSearchContext ? and : "");
				totalQuery.append(taggedElementsQuery.toString());
			}

			// if no score should be calculated, then "q" should be only "*:*"
			if (!termsInSearchContext && !taggedElementsInSearchContext) {
				totalQuery.append(luceneQueryParser + queryAll);
			}
		} else {
			// If not Score required, don't a
			if (termsInSearchContext) {
				totalQuery.append(blockJoinWithScore + luceneQueryParser + query.toString());
			} else {
				totalQuery.append(luceneQueryParser + queryAll);
			}
		}

		// first add query - "q"
		requestQueryParams.add(q, totalQuery.toString());

		return termsInSearchContext || taggedElementsInSearchContext;
	}

	/**
	 * Adds Search Terms to REST query parameters for each term new "fq"
	 * parameter added.
	 *
	 * @param searchContext
	 * @param language
	 * @param restRequestPathsAndQueryParams
	 * @param resultSetType
	 */
	private void addSearchTermsSameUtteranceMode(SearchInteractionsContext searchContext, SpeakerQueryType speakerType, String tenant, String channel, String language, RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, ResultSetGenerationType resultSetType, Boolean calculateScore) {
		val requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();

		StringBuilder termsQuery = new StringBuilder();

		// generate block join parser
		Pair<String, String> blockJoinParsers = this.generateTextFieldBlockJoinParser(false, calculateScore);
		String blockJoinWithScore = blockJoinParsers.getLeft();
		String blockJoinNoScore = blockJoinParsers.getRight();

		// extract text tagged elements : topics_f, relations_f, terms_f from Search Context
		Pair<Boolean, StringBuilder> taggedTextElementsQuery = this.getTaggedTextElements(tenant, channel, searchContext, and, calculateScore);
		Boolean taggedElementsInSearchContext = taggedTextElementsQuery.getLeft();
		StringBuilder taggedElementsQuery = taggedTextElementsQuery.getRight();

		Boolean termsInSearchContext = false;

		// Each terms expression should generate it's own "fq" as the search should extract
		// interactions with at least one utterance which match all criteria
		List<String> searchExpressions = searchContext.getTerms();
		if (searchExpressions != null) {

			int j = 0;
			for (String searchExpression : searchExpressions) {

				// parse VTA search expression
				VTASyntaxParsingResult parsingResult = this.vtaSyntaxAnalyzer.parseQuery(searchExpression, this.getVTASyntaxTASQueryConfiguration(language));

				if (parsingResult.getTermsCount() > 0) {
					if (j > 0) {
						termsQuery.append(and);
					}
					// open with "("
					termsQuery.append(openPareth);
					termsQuery.append(parsingResult.getSolrQuery());

					termsInSearchContext = true;
					// close with ")"
					termsQuery.append(closeParetn);
				}
				j++;
			}
		}

		// if tagged elements were present in Search Context, add those with "AND" to query to impact the scoring
		if (taggedElementsInSearchContext) {
			termsQuery.append(termsInSearchContext ? and : "");
			termsQuery.append(taggedElementsQuery.toString());
		}

		// If Terms present in search context or Relations, Entities or Keywords
		if (termsInSearchContext || taggedElementsInSearchContext || (speakerType == SpeakerQueryType.Agent || speakerType == SpeakerQueryType.Customer)) {
			termsQuery.insert(0, blockJoinWithScore + luceneQueryParser);

			// if terms and tagged text elements exists query
			if (termsInSearchContext || taggedElementsInSearchContext) {
				termsQuery.append(and);
			}

			if (speakerType != null && speakerType != SpeakerQueryType.Any) {
				termsQuery.append("(" + String.format(filterFieldQueryTemplate, TAConstants.SchemaFieldNames.speakerType, speakerType.toString()) + ")");
			}
		} else {
			termsQuery.insert(0, luceneQueryParser + "*:*");
		}

		// first add query - "q"
		requestQueryParams.add(q, termsQuery.toString());
	}


	/**
	 * If filter containt tagged elements : topics_f, relations_f, terms_f ...
	 * the query ("q") should containt those elements for scoring
	 *
	 * @param tenant         tenant
	 * @param channel        channel
	 * @param searchContext  search context with query and filters
	 * @param calculateScore if score is not being calculated, then no need to add tagged
	 *                       elements to query
	 * @return
	 */
	private Pair<Boolean, StringBuilder> getTaggedTextElements(String tenant, String channel, SearchInteractionsContext searchContext, String textElementsBooleanOperator, Boolean calculateScore) {

		Pair<Boolean, StringBuilder> result = null;

		// when score is required, topics_f, relations_f, terms_f filter should be added to "q"
		// to influence the score
		StringBuilder sbTaggedElementsFilters = null;
		Boolean taggedElementsInSearchContext = false;

		if (calculateScore) {
			sbTaggedElementsFilters = new StringBuilder();
			int numberOfTaggedElements = 0;

			List<FilterField> filterFields = searchContext.getFilterFields();
			if (filterFields != null) {
				List<FilterField> entitiesFilters = filterFields.stream().filter(f -> f.getName().equals(TAConstants.SchemaFieldNames.topics_f)).collect(toList());
				if (!CollectionUtils.isEmpty(entitiesFilters)) {
					taggedElementsInSearchContext = true;

					for (FilterField filterField : entitiesFilters) {
						if (numberOfTaggedElements > 0) {
							sbTaggedElementsFilters.append(textElementsBooleanOperator);
						}

						sbTaggedElementsFilters.append(this.getFilterFieldValuesQuery(filterField, tenant, channel, false, ResultSetGenerationType.ParentResultSet));
						numberOfTaggedElements++;
					}
				}

				List<FilterField> relationsFilters = filterFields.stream().filter(f -> f.getName().equals(TAConstants.SchemaFieldNames.relations_f)).collect(toList());
				if (!CollectionUtils.isEmpty(relationsFilters)) {
					taggedElementsInSearchContext = true;

					for (FilterField filterField : relationsFilters) {
						if (numberOfTaggedElements > 0) {
							sbTaggedElementsFilters.append(textElementsBooleanOperator);
						}

						sbTaggedElementsFilters.append(this.getFilterFieldValuesQuery(filterField, tenant, channel, false, ResultSetGenerationType.ParentResultSet));
						numberOfTaggedElements++;
					}
				}

				List<FilterField> keyTermsFilters = filterFields.stream().filter(f -> f.getName().equals(TAConstants.SchemaFieldNames.keyterms_f)).collect(toList());
				if (!CollectionUtils.isEmpty(keyTermsFilters)) {
					taggedElementsInSearchContext = true;

					for (FilterField filterField : keyTermsFilters) {
						if (numberOfTaggedElements > 0) {
							sbTaggedElementsFilters.append(textElementsBooleanOperator);
						}

						sbTaggedElementsFilters.append(this.getFilterFieldValuesQuery(filterField, tenant, channel, false, ResultSetGenerationType.ParentResultSet));
						numberOfTaggedElements++;
					}
				}
			}
		}

		result = Pair.of(taggedElementsInSearchContext, sbTaggedElementsFilters);

		return result;
	}

	protected void addTermsForHighlighting(List<QueryTerm> terms, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		String highlightQuery = "";

		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		if (!CollectionUtils.isEmpty(terms)) {

			// @formatter:off
			highlightQuery = terms.stream()
			                      .map(t -> {
									  String phraseForHighlight = "";
									  if (t.getType() == com.verint.textanalytics.dal.darwin.vtasyntax.TermType.Phrase) {
										  return t.getHighlightQuery() != null ? t.getHighlightQuery() : "";
									  } else {
										  return t.getHighlightQuery() != null ? String.format("(%s)", t.getHighlightQuery()) : "";
									  }
			                      })
			                      .collect(Collectors.joining(space));
			// @formatter:on
		}

		requestQueryParams.add("hl.q", highlightQuery);
	}

	protected void addInteractionFieldsList(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, String tenant, String channel, boolean searchExists, int numberOfUtterancesLimit) {
		String channelDynamicFieldsList = null, fieldsList = null;
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();

		// get list of dynamic fields from configuration, those fields are specific for channel and tenant
		List<TextSchemaField> channelDynamicFields = this.textEngineConfigurationService.getChannelDynamicFields(tenant, channel);
		if (channelDynamicFields != null) {
			channelDynamicFieldsList = channelDynamicFields.stream().map(f -> f.getName()).collect(Collectors.joining(","));
		}

		if (!StringUtils.isNullOrBlank(channelDynamicFieldsList)) {
			fieldsList = String.format("%s,%s", this.interactionPredefinedFields, channelDynamicFieldsList);
		} else {
			fieldsList = this.interactionPredefinedFields;
		}

		if (!searchExists) {
			fieldsList += "," + String.format(childDocumentsField, numberOfUtterancesLimit);
		}

		requestQueryParams.add("fl", fieldsList);
	}

	protected void addFormatedResults(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("wt", this.queryParams.getResponseFormat());
		requestQueryParams.add("indent", this.queryParams.getResponseIdentation());
		requestQueryParams.add("json.nl", "map");
	}

	protected void addFormatedResults(final List<Pair<String, String>> requestParams) {
		requestParams.add(new ImmutablePair<>("wt", this.queryParams.getResponseFormat()));
		requestParams.add(new ImmutablePair<>("indent", this.queryParams.getResponseIdentation()));
	}

	protected void addHighlighting(String language, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("hl", TAConstants.trueLowerCase);
		requestQueryParams.add("hl.fl", "text_" + language);
	}

	protected void addJsonFacet(String facetJson, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add(jsonFacet, facetJson);
	}

	protected void addFacetThreads(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add(facetThreads, String.valueOf(this.configuration.getFacetThreadLimit()));
	}

	private void addStartOfRows(Integer rangeStart, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("start", rangeStart.toString());
	}

	private void addNumberOfRows(Integer numberOfRows, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();
		requestQueryParams.add("rows", numberOfRows.toString());
	}

	/**
	 * Adds a date range query to map of query parameters.
	 *
	 * @param searchContext                  search context to extract date range filter
	 * @param restRequestPathsAndQueryParams collections of request parameters, the date filter parameter is going to added
	 * @param resultTypeGenerationType       should the query be generated for parent or child documents
	 */
	protected void addDateRange(SearchInteractionsContext searchContext, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, ResultSetGenerationType resultTypeGenerationType, Integer queryCost) {
		MultivaluedStringMap requestQueryParams = restRequestPathsAndQueryParams.getQueryParams();

		List<String> dateRangeQueryList = this.getDateRangeQueryParam(searchContext, resultTypeGenerationType);
		for (String dateRangeQuery : dateRangeQueryList) {
			requestQueryParams.add(fq, dateRangeQuery);
		}
	}

	/**
	 * Adds a date range query to map of query parameters.
	 *
	 * @param searchContext
	 * @param sbRequestBody
	 * @param resultTypeGenerationType
	 */
	protected void addDateRange(SearchInteractionsContext searchContext, List<Pair<String, String>> sbRequestBody, ResultSetGenerationType resultTypeGenerationType) {
		val dateRangeQueryList = this.getDateRangeQueryParam(searchContext, resultTypeGenerationType);

		// set resolution of date filter to 1 minute
		for (String dateRangeQuery : dateRangeQueryList) {
			sbRequestBody.add(new ImmutablePair<>(fq, String.format("%s/MINUTE", dateRangeQuery)));
		}
	}

	private FacetQuery getInteractionsDailyVolumeFacet(SearchInteractionsContext searchContext) {
		val facet = new FacetQuery();
		NestedTermsFacet rangeFacet = new NestedTermsFacet();

		rangeFacet.setAlias(TAConstants.FacetQuery.interactionsDailyVolumeAlias)
			.setFieldName(TAConstants.SchemaFieldNames.dayOfDate)
			.setFieldDataType(FieldDataType.Long)
			.setQueryType(FacetQueryType.Terms)
			.setMinCount(1)
			.setLimit(-1);

			//.setSortBy(TAConstants.SchemaFieldNames.dayOfDate)
			// .setSortDirection(asc);

		facet.addNestedFacet(rangeFacet);

		return facet;
	}

	private void addDateRangeFacetQuery(SearchInteractionsContext searchContext, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		val reqQueryParams = restRequestPathsAndQueryParams.getQueryParams();

		ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();
		reqQueryParams.add(TAConstants.FacetQuery.facetParam, TAConstants.trueLowerCase);
		reqQueryParams.add(TAConstants.FacetQuery.rangeFacetParam, TAConstants.SchemaFieldNames.parentDate);
		reqQueryParams.add(TAConstants.FacetQuery.rangeFacetStart, TAConstants.DateRangeValues.dailyVolumeLast3Years);
		reqQueryParams.add(TAConstants.FacetQuery.rangeFacetEnd, TAConstants.DateRangeValues.dailyVolumeNow);
		reqQueryParams.add(TAConstants.FacetQuery.rangeFacetGap, TAConstants.DateRangeValues.dateRangeGap1Day);
		reqQueryParams.add(TAConstants.FacetQuery.facetMincount, "1");
		reqQueryParams.add(TAConstants.FacetQuery.facetLimit, String.valueOf(TAConstants.FacetQuery.bucketsLimitNotLimited));
		reqQueryParams.add(TAConstants.FacetQuery.facetThreads, String.valueOf(appConfig.getFacetThreadLimit()));
	}

	private String getDateForInteractionsDailyVolume(DateTime date) {
		return DataUtils.getDateForDateRangeFacet(date);
	}

	/**
	 * Generates a date range query as string.
	 *
	 * @param searchContext            search interactions context
	 * @param resultTypeGenerationType type of query generation
	 * @return
	 */
	protected List<String> getDateRangeQueryParam(SearchInteractionsContext searchContext, ResultSetGenerationType resultTypeGenerationType) {
		Range range;
		String dateFieldName, dateQuery = "";
		List<String> res = new ArrayList<String>();
		ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();

		List<RangeFilterField> rangeFilterFields = searchContext.getRangeFilterFields();
		// this is for the All dates
		try {
			if (rangeFilterFields != null) {

				for (val rangeFilterField : rangeFilterFields) {

					if (rangeFilterField.getDataType().equals(FieldDataType.Constant) && rangeFilterField.getName().equals(TAConstants.SchemaFieldNames.parentDate)) {
						range = rangeFilterField.getRanges().get(0);

						switch (resultTypeGenerationType) {
							case ParentResultSet:
								// as date field is on parent documents
								dateFieldName = TAConstants.SchemaFieldNames.parentDate;
								break;

							case ChildResultSet:
								dateFieldName = TAConstants.SchemaFieldNames.childDate;
								break;
							default:
								dateFieldName = TAConstants.SchemaFieldNames.parentDate;
								break;
						}

						String rangeStart = null, rangeEnd = null;
						if (appConfig.getDateRangeRound()) {
							String roundUpTo = appConfig.getDateRangeRoundUpTo();
							rangeStart = String.format("%s/%s", range.getLowerValue(), appConfig.getDateRangeRoundUpTo());
							rangeEnd = String.format("%s/%s", range.getUpperValue(), appConfig.getDateRangeRoundUpTo());

							dateQuery = String.format("%s:[%s TO %s]", dateFieldName, DataUtils.escapeCharsForSolrQuery(rangeStart),
							                                          DataUtils.escapeCharsForSolrQuery(rangeEnd));
						} else {
							rangeStart = range.getLowerValue();
							rangeEnd = range.getUpperValue();
							// as date filter allways woking  on NOW, no need to cache this field
							dateQuery = String.format("%s:[%s TO %s]", dateFieldName, DataUtils.escapeCharsForSolrQuery(rangeStart), DataUtils.escapeCharsForSolrQuery(rangeEnd));
						}

						res.add(dateQuery);
					}
				}
			}
		} catch (Exception e) {
			dateQuery = "";
		}

		return res;
	}

	private  void addFilters(String tenant, String channel, SearchInteractionsContext searchContext,  MultivaluedStringMap requestQueryStringParams, String facetsQueryField, ResultSetGenerationType resultSetGenerationType) {
		this.addFilters(tenant, channel, searchContext.getFilterFields(), requestQueryStringParams, facetsQueryField, resultSetGenerationType);
	}

	private void addFiltersOnSameUtterance(String tenant, String channel, SearchInteractionsContext searchContext,  MultivaluedStringMap requestQueryStringParams, String facetsQueryField, ResultSetGenerationType resultSetGenerationType) {
		// the "topics_f, relations_f and terms_f were allready added to the query, so add all other filter fields
		List<FilterField> filterFields = null;

		//@formatter:off
		if (searchContext.getFilterFields() != null) {
			filterFields = searchContext.getFilterFields().stream().filter(f -> f.getName() != TAConstants.SchemaFieldNames.topics_f
																				&& f.getName() != TAConstants.SchemaFieldNames.relations_f
																				&& f.getName() != TAConstants.SchemaFieldNames.keyterms_f)
																	.collect(toList());
		}
		//@formatter:on

		addFilters(tenant, channel, filterFields, requestQueryStringParams, facetsQueryField, resultSetGenerationType);
	}

	private void addFilters(String tenant, String channel, List<FilterField> filterFields,  MultivaluedStringMap requestQueryStringParams, String facetsQueryField, ResultSetGenerationType resultSetGenerationType) {

		List<FilterField> filterFieldsSingle = null;

		if (!CollectionUtils.isEmpty(filterFields)) {
			// locate filters with no group
			filterFieldsSingle = filterFields.stream().filter(f -> StringUtils.isNullOrBlank(f.getGroupTag())).collect(toList());

			// process regular filters
			List<String> filterQueries = this.getFilters(tenant, channel, filterFieldsSingle, facetsQueryField, resultSetGenerationType);

			if (filterQueries != null) {
				for (String filterQuery : filterQueries) {
					requestQueryStringParams.add(fq, filterQuery);
				}
			}

			// add Group filters "fq"
			this.addGroupFilters(tenant, channel, filterFields, requestQueryStringParams, facetsQueryField, resultSetGenerationType);
		}

	}

	private void addGroupFilters(String tenant, String channel, List<FilterField> filterFields,  MultivaluedStringMap requestQueryStringParams, String facetsQueryField, ResultSetGenerationType resultSetGenerationType) {
		try {
			List<FilterFieldGroup> filterFieldGroups = new ArrayList<FilterFieldGroup>();

			// fetch list of group filters (filters that should apply for the same utterance)
			for (FilterField filter : filterFields) {
				if (!StringUtils.isNullOrBlank(filter.getGroupTag())) {

					// check if already has this group
					FilterFieldGroup groupToUpdate = null;
					for (FilterFieldGroup group : filterFieldGroups) {
						if (group.getGroupTag().equals(filter.getGroupTag())) {
							groupToUpdate = group;
							break;
						}
					}

					if (groupToUpdate != null) {
						// adding filter to the group
						groupToUpdate.addFilter(filter);
					} else {
						// create new group and add it to list
						groupToUpdate = new FilterFieldGroup();
						groupToUpdate.addFilter(filter);
						filterFieldGroups.add(groupToUpdate);
					}
				}
			}

			// process the group filters
			List<String> filterQuriesFromGroups = this.getGroupFilters(tenant, channel, filterFieldGroups, facetsQueryField, resultSetGenerationType);

			if (filterQuriesFromGroups != null) {
				for (String filterQuery : filterQuriesFromGroups) {
					requestQueryStringParams.add(fq, filterQuery);
				}
			}
		} catch (Exception ex) {
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryGenerationError));
		}
	}

	protected List<String> getGroupFilters(String tenant, String channel, List<FilterFieldGroup> filterFieldGroups, String facetsQueryField, ResultSetGenerationType resultSetGenerationType) {
		List<String> filterQueries = new ArrayList<String>();
		StringBuilder fieldQueryValues = new StringBuilder();

		if (filterFieldGroups != null) {

			for (FilterFieldGroup filterFieldGroup : filterFieldGroups) {
				// clear before adding values for new field
				fieldQueryValues.setLength(0);

				// for each group - go over the filters and produce new FQ
				List<FilterField> filtersInGroup = filterFieldGroup.getFilters();
				for (int i = 0; i < filtersInGroup.size(); i++) {
					FilterField filterField = filtersInGroup.get(i);

					// load StringBuilder with this specific FilterField query
					fieldQueryValues.append(this.getFilterFieldValuesQuery(filterField, tenant, channel, true, resultSetGenerationType));

					if (i + 1 < filtersInGroup.size()) {
						fieldQueryValues.append(" AND ");
					}
				}

				// *assuming* for Group Filters - filtering only Utterance level info. like Entities, Relations, text...
				String joinBlock = "";
				switch (resultSetGenerationType) {
					case AllChildrenOfParents:
						joinBlock = this.generateAllChildrenOfParentsResultSetBlockJoin;
						break;
					case ParentResultSet:
						joinBlock = this.generateParentResultSetBlockJoin;
						break;
					default:
						joinBlock = "";
				}

				filterQueries.add(String.format("%s%s", joinBlock, fieldQueryValues.toString()));
			}
		}
		return filterQueries;
	}

	protected List<String> getFilters(String tenant, String channel, List<FilterField> filterFields, String facetsQueryField, ResultSetGenerationType resultSetGenerationType) {
		List<String> filterQueries = new ArrayList<String>();

		if (filterFields != null) {
			StringBuilder fieldQuery = new StringBuilder();

			// run through each filter (AND)
			for (FilterField filterField : filterFields) {
				// don't get filter with missing field name
				String fieldName = filterField.getName();

				if (!StringUtils.isNullOrBlank(fieldName)) {
					FilterFieldValue[] fieldValues = filterField.getValues();
					if (fieldValues != null && fieldValues.length > 0) {

						fieldQuery.setLength(0);
						fieldQuery.append(this.getFilterFieldValuesQuery(filterField, tenant, channel, true, resultSetGenerationType));

						String joinBlock = this.generateJoinBlockParser(tenant, channel, filterField, resultSetGenerationType);

						// add tag for this filter, if necessary						
						if (this.addFilterFieldExclusionTag(facetsQueryField, fieldName, filterField)) {
							filterQueries.add(String.format("{!tag=%s}%s%s", NestedFacet.getFieldTagNameforExclusion(fieldName), joinBlock, fieldQuery.toString()));
						} else {
							// no need for tag element
							filterQueries.add(String.format("%s%s", joinBlock, fieldQuery.toString()));
						}
					}
				}
			}
		}

		return filterQueries;
	}

	/**
	 * Generates part of query for all values of field.
	 *
	 * @param filterField filter field
	 * @param tenant      tenant
	 * @param channel     channel
	 * @return values of filter field separated with OR. Can be added to "q" or
	 * "fq".
	 */
	private String getFilterFieldValuesQuery(FilterField filterField, String tenant, String channel, Boolean addSpeakerFilterExpression, ResultSetGenerationType resultSetGenerationType) {
		String fieldName, fieldValue;
		FilterFieldValue[] fieldValues;
		FieldDataType fieldDataType;
		StringBuilder fieldQueryValues = new StringBuilder();

		// don't get filter with missing field name
		fieldName = filterField.getName();

		if (filterField.getDataType() == null) {
			TextSchemaField textSchemeField = this.textEngineConfigurationService.getTextSchemaField(tenant, channel, fieldName);
			fieldDataType = textSchemeField.getFieldDataType();
		} else {
			fieldDataType = filterField.getDataType();
		}

		if (!StringUtils.isNullOrBlank(fieldName)) {
			fieldValues = filterField.getValues();

			if (fieldValues != null && fieldValues.length > 0) {

				// clear before adding values for new field
				fieldQueryValues.setLength(0);

				fieldQueryValues.append(openPareth);

				// run through each value (OR)
				for (int j = 0; j < fieldValues.length; j++) {

					fieldValue = fieldValues[j].getValue();
					fieldValue = this.preProcessValueForFilter(fieldValue, filterField);
					fieldValue = DataUtils.escapeCharsForSolrQuery(fieldValue);
					fieldValue = this.postProcessValueForFilter(fieldValue, filterField);

					String quoteFormatter = "\"%s\"";
					switch (fieldDataType) {
						case Text:
							break;
						case Constant:
							fieldValue = String.format(quoteFormatter, fieldValue);
							break;
						case Date:
							fieldValue = String.format(quoteFormatter, fieldValue);
							break;
						case Boolean:
							fieldValue = String.format(quoteFormatter, fieldValue);
							break;
						case Int:
						case Long:
						default:
							break;
					}

					if (j != 0) {
						fieldQueryValues.append(or);
					}
					fieldQueryValues.append(this.getFilterFieldNameByHierachy(fieldName, resultSetGenerationType));
					fieldQueryValues.append(":");
					fieldQueryValues.append(fieldValue);
				}

				fieldQueryValues.append(closeParetn);

				if (addSpeakerFilterExpression) {
					// add Speaker filter to group filter
					SpeakerQueryType speakerQuery = filterField.getSpeaker();

					switch (speakerQuery) {
						case Customer:
							fieldQueryValues.append(String.format(speakerAndQueryTemplate, TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Customer));
							break;
						case Agent:
							fieldQueryValues.append(String.format(speakerAndQueryTemplate, TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Agent));
							break;
						case Automated:
							fieldQueryValues.append(String.format(speakerAndQueryTemplate, TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Automated));
							break;
						default:
							// do not add anything
							break;
					}
				}
			}
		}

		return fieldQueryValues.toString();
	}

	private void addSort(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams, String sortField, String sortDirection) {
		restRequestPathsAndQueryParams.getQueryParams().add(sort, String.format("%s %s", sortField, !StringUtils.isNullOrBlank(sortDirection) ? sortDirection : desc));
	}

	private Boolean addFilterFieldExclusionTag(String facetField, String filterField, FilterField filterFieldObj) {
		// tag for exclusion should be placed only if facet requested on this field, but not for topics_f
		// if filter field is locked - do not add tag, so it will not affect exclusion
		return (facetField != null && facetField.equals(filterField) && !filterField.equals(TAConstants.SchemaFieldNames.topics_f) && !filterFieldObj.getLocked());
	}

	protected String generateJoinBlockParser(String tenant, String channel, FilterField filterField, ResultSetGenerationType resultSetGenerationType) {
		String joinBlock = "";

		// adjust filter field name according  to query hierachy
		// for (topics_f, Interaction) -> topic_f_total
		//     (relations_f, Interaction) -> relations_f_total
		//     (topics_f, Utterace) -> topics_f
		String fieldName = this.getFilterFieldNameByHierachy(filterField.getName(), resultSetGenerationType);

		// some fields are present both on interactions and utterance level
		// id, content_type, date, channel..
		if (this.textEngineConfigurationService.isParentDocumentField(tenant, channel, fieldName) && this.textEngineConfigurationService.isChildDocumentField(tenant, channel,
		                                                                                                                                                      fieldName)) {

			switch (resultSetGenerationType) {
				case FilterParentGenerateParentResultSet:
					joinBlock = "";
					break;
				case FilterParentGenerateChildResultSet:
					joinBlock = this.generateChildResultSetBlockJoin;
					break;
				default:
					joinBlock = "";
					break;
			}

		} else if (this.textEngineConfigurationService.isParentDocumentField(tenant, channel, fieldName)) {

			switch (resultSetGenerationType) {
				case ParentResultSet:
					joinBlock = "";
					break;
				case ChildResultSet:
				case AllChildrenOfParents:
					joinBlock = this.generateChildResultSetBlockJoin;
					break;
				default:
					joinBlock = "";
					break;
			}

		} else if (this.textEngineConfigurationService.isChildDocumentField(tenant, channel, fieldName)) {

			switch (resultSetGenerationType) {
				case AllChildrenOfParents:
					joinBlock = this.generateAllChildrenOfParentsResultSetBlockJoin;
					break;
				case ParentResultSet:
					joinBlock = this.generateParentResultSetBlockJoin;
					break;
				case ChildResultSet:
					joinBlock = "";
					break;
				default:
					joinBlock = "";
					break;
			}
		}

		return joinBlock;
	}

	/**
	 * Generates join expression for free text search.
	 *
	 * @param resultSetType
	 * @param calculateScore
	 * @return
	 */
	private Pair<String, String> generateTextFieldBlockJoinParser(Boolean filterOnParentLevel, Boolean calculateScore) {
		Pair<String, String> blockjoinParser = null;

		String blockJoinWithScore = "";
		String blockJoinNoScore = "";

		// generate JOIN  between parents/children according to the query
		if (filterOnParentLevel) {
			// text field is present on interaction level, so no need
			// to generate join block
			blockJoinWithScore = "";
			blockJoinNoScore = "";
		} else {
			blockJoinWithScore = this.generateParentResultSetBlockJoinWithScore;
			blockJoinNoScore = this.generateParentResultSetBlockJoin;
		}

		blockjoinParser = Pair.of(blockJoinWithScore, blockJoinNoScore);

		return blockjoinParser;
	}

	protected String preProcessValueForFilter(String value, FilterField filterField) {

		switch (filterField.getName()) {
			case TAConstants.SchemaFieldNames.topics_f:
				// don't remove leading number from entity path :
				// 2/device/nokia, it is being used for queries
				return value;
			default:
				return value;
		}
	}

	protected String postProcessValueForFilter(String value, FilterField filterField) {
		switch (filterField.getName()) {
			case TAConstants.SchemaFieldNames.topics_f:

				// entity name might include special characters, escape with
				// quotes
				return value;
			default:
				return value;
		}
	}

	private String getFilterFieldNameByHierachy(String fieldName, ResultSetGenerationType resultSetGenerationType) {
		String fieldNameByHierarchy = fieldName;

		switch (resultSetGenerationType) {
			case ParentResultSet:

				switch (fieldName) {
					case TAConstants.SchemaFieldNames.topics_f:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.topics_f_total;
						break;
					case TAConstants.SchemaFieldNames.relations_f:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.relations_f_total;
						break;
					case TAConstants.SchemaFieldNames.keyterms_f:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.keyterms_f_total;
						break;
					case TAConstants.SchemaFieldNames.categories:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.categories;
						break;
					default:
						break;
				}

				break;

			case ChildResultSet:
				// no change required for Child result set generatation
				switch (fieldName) {
					case TAConstants.SchemaFieldNames.topics_f:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.topics_f;
						break;
					case TAConstants.SchemaFieldNames.relations_f:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.relations_f;
						break;
					case TAConstants.SchemaFieldNames.keyterms_f:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.keyterms_f;
						break;
					case TAConstants.SchemaFieldNames.categories:
						fieldNameByHierarchy = TAConstants.SchemaFieldNames.categories;
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}

		return fieldNameByHierarchy;
	}

	@Override
	public TASQueryConfiguration getVTASyntaxTASQueryConfiguration(String language) {
		TASQueryConfiguration queryConfig = null;

		synchronized (this.vtaSyntaxQueryConfigurations) {

			if (!this.vtaSyntaxQueryConfigurations.containsKey(language)) {
				queryConfig = new TASQueryConfiguration();
				queryConfig.setTextNoSPSField(String.format(TAConstants.SchemaFieldNames.text_total_format, language))
				           .setTextAgentField(String.format(TAConstants.SchemaFieldNames.text_agent_format, language))
				           .setTextCustomerField(String.format(TAConstants.SchemaFieldNames.text_customer_format, language))
				           .setEscapeValuesEnabled(true)
				           .setCommonGramsFilterEnabled(true)
						   .setWildCardPrefixMinLength(this.configuration.getSearchWildCardPrefixMinLength())
						   .setLanguage(language);

				this.vtaSyntaxQueryConfigurations.put(language, queryConfig);
			} else {
				queryConfig = this.vtaSyntaxQueryConfigurations.get(language);
			}
		}

		return queryConfig;
	}

	protected void validateParameters(String tenant, String channel, SearchInteractionsContext searchContext, String language) {

		this.validateParameters(tenant, channel, language);

		if (searchContext == null) {
			throw new IllegalArgumentException("searchContext parameter is null empty");
		}
	}

	protected void validateParameters(String tenant, String channel, SearchInteractionsContext searchContext) {
		if (StringUtils.isNullOrBlank(tenant)) {
			throw new IllegalArgumentException(TENANT_PARAMETER_IS_NULL_EMPTY);
		}

		if (StringUtils.isNullOrBlank(channel)) {
			throw new IllegalArgumentException(CHANNEL_PARAMETER_IS_NULL_EMPTY);
		}

		if (searchContext == null) {
			throw new IllegalArgumentException("searchContext parameter is null empty");
		}

	}

	protected void validateParameters(String tenant, String channel, String language) {
		if (StringUtils.isNullOrBlank(tenant)) {
			throw new IllegalArgumentException(TENANT_PARAMETER_IS_NULL_EMPTY);
		}

		if (StringUtils.isNullOrBlank(channel)) {
			throw new IllegalArgumentException(CHANNEL_PARAMETER_IS_NULL_EMPTY);
		}

		if (StringUtils.isNullOrBlank(language)) {
			throw new IllegalArgumentException("language parameter is null empty");
		}

	}

	protected void validateTrendsParameters(String tenant, String channel, String periodName) {
		if (StringUtils.isNullOrBlank(tenant)) {
			throw new IllegalArgumentException(TENANT_PARAMETER_IS_NULL_EMPTY);
		}

		if (StringUtils.isNullOrBlank(channel)) {
			throw new IllegalArgumentException(CHANNEL_PARAMETER_IS_NULL_EMPTY);
		}

		if (StringUtils.isNullOrBlank(periodName)) {
			throw new IllegalArgumentException("periodName parameter is null empty");
		}
	}

	protected String getTextElementNextPath(String textElementValue) {
		String path = null;

		if (textElementValue == null || "".equals(textElementValue)) {
			path = "1/";
		} else {
			// TODO: StringUtils.firstIndexOf
			int fstSlash = textElementValue.indexOf("/", 0);
			int depth = Integer.parseInt(textElementValue.substring(0, fstSlash));
			path = textElementValue.substring(fstSlash, textElementValue.length());
			path = String.valueOf(++depth) + path;
		}

		return path;
	}

	private void getInteractionsPredefinedFieldsList() {
		List<String> interactionFields = new ArrayList<>();
		List<String> predefinedFields = Arrays.asList(TAConstants.SchemaFieldNames.documentId, TAConstants.SchemaFieldNames.channel, TAConstants.SchemaFieldNames.content_type,
		                                              TAConstants.SchemaFieldNames.sourceType, TAConstants.SchemaFieldNames.parentDate,
		                                              TAConstants.SchemaFieldNames.agentLocalStartTime, TAConstants.SchemaFieldNames.agentTimeZone,
		                                              TAConstants.SchemaFieldNames.agentNames, TAConstants.SchemaFieldNames.agentMessagesCount,
		                                              TAConstants.SchemaFieldNames.agentAvgResponseTime, TAConstants.SchemaFieldNames.customerLocalStartTime,
		                                              TAConstants.SchemaFieldNames.customerTimeZone, TAConstants.SchemaFieldNames.customerNames,
		                                              TAConstants.SchemaFieldNames.customerMessagesCount, TAConstants.SchemaFieldNames.customerAvgResponseTime,
		                                              TAConstants.SchemaFieldNames.robotMessagesCount, TAConstants.SchemaFieldNames.messagesCount,
		                                              TAConstants.SchemaFieldNames.handleTime, TAConstants.SchemaFieldNames.relevancyScore,
		                                              TAConstants.SchemaFieldNames.interactionSentiment, TAConstants.SchemaFieldNames.categoriesIds,
		                                              TAConstants.SchemaFieldNames.interactionSentimentIsMixed);

		interactionFields.addAll(predefinedFields);

		// add Chat fields
		List<TextSchemaField> chatFields = textEngineConfigurationService.getSourceTypeTextSchemaFields(SourceType.Chat);
		if (chatFields != null) {
			chatFields.stream().forEach(f -> interactionFields.add(f.getName()));
		}

		// add Email fields
		List<TextSchemaField> emailFields = textEngineConfigurationService.getSourceTypeTextSchemaFields(SourceType.Email);
		if (emailFields != null) {
			emailFields.stream().forEach(f -> interactionFields.add(f.getName()));
		}

		// interactionFields.add(String.format(childDocumentsField, queryParams.getChildDocumentsForParentLimit()));

		this.interactionPredefinedFields = interactionFields.stream().collect(Collectors.joining(","));
	}


	/**
	 * Disposable bean implementation.
	 */
	public void destroy() {
		if (this.vtaSyntaxAnalyzer != null) {
			this.vtaSyntaxAnalyzer.dispose();
		}
	}

	/**
	 * @author EZlotnik
	 */
	public enum ResultSetGenerationType {
		// @formatter:off
		ParentResultSet(0),		
		ChildResultSet(1), 
		AllChildrenOfParents(2), 
		FilterParentGenerateChildResultSet(3), 
		FilterParentGenerateParentResultSet(4);
		// @formatter:on

		/**
		 * Constructor.
		 *
		 * @param resultSetGenerationType
		 */
		ResultSetGenerationType(int resultSetGenerationType) {
		}
	}

	/**
	 * @author NShunewich
	 */
	public enum EntitiesTrendsPeriodType {
		// @formatter:off
		HOURLY(0), 
		DAILY(1), 
		WEEKLY(2), 
		MONTHLY(3);
		// @formatter:on

		/**
		 * Constructor.
		 *
		 * @param resultSetGenerationType
		 */
		EntitiesTrendsPeriodType(int resultSetGenerationType) {

		}
	}

	/**
	 * Query generation type.
	 *
	 * @author EZlotnik
	 */
	protected enum QueryGenerationType {
		// @formatter:off
		MultipleFqQuery(0),
		NestedQueries(1);
		// @formatter:on

		QueryGenerationType(int queryGenerationType) {
		}
	}

	/**
	 * Represents a query generation output.
	 *
	 * @author EZlotnik
	 */
	protected class TermsQueryGenerationOutput {
		@Getter
		private List<SearchTerm> terms;

		@Getter
		private List<String> termsFilterQueries;

		/**
		 * C'tor.
		 */
		public TermsQueryGenerationOutput() {
			this.terms = new ArrayList<SearchTerm>();
			this.termsFilterQueries = new ArrayList<String>();
		}

		public void addAllTerms(List<SearchTerm> termsToAdd) {
			this.terms.addAll(termsToAdd);
		}

		public void addTermsFilterQuery(String termFilterQuery) {
			this.termsFilterQueries.add(termFilterQuery);
		}

	}

	@Override
	public RestRequestPathsAndQueryParams getCreateTenantQuery(String tenant) {

		return null;
	}

	@Override
	public RestRequestPathsAndQueryParams getDeleteTenantQuery(String tenant) {

		return null;
	}
}
