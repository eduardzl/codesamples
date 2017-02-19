package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.dal.darwin.TextEngineSchemaService;
import com.verint.textanalytics.dal.darwin.vtasyntax.QueryTerm;
import com.verint.textanalytics.dal.darwin.vtasyntax.TASQueryConfiguration;
import com.verint.textanalytics.dal.darwin.vtasyntax.VTASyntaxAnalyzer;
import com.verint.textanalytics.dal.darwin.vtasyntax.VTASyntaxParsingResult;
import com.verint.textanalytics.model.interactions.*;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Generates a highlights for interactions.
 *
 * @author EZlotnik
 */

public class HighlightsGenerator {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private VTASyntaxAnalyzer vtaSyntaxQueryAnalyzer;

	@Autowired
	private HighlightsMerger highlightsMerger;

	@Autowired
	private TextEngineSchemaService textEngineConfigurationService;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	private final String slash = "/";

	/**
	 * Constructor.
	 */
	public HighlightsGenerator() {

	}

	/**
	 * Generates highlights for speaker separated terms.
	 *
	 * @param tenant                           tenant
	 * @param channel                          channel
	 * @param interactions                     interactions list
	 * @param searchContext                    search context
	 * @param generateUtterancesFromHighlights should utterances be generated from highlights
	 * @param needHighlightsWhenNoSearch       should return highlights even when no searched was done
	 */
	@SuppressWarnings("unchecked")
	public void generateSPSHighlights(final String tenant, final String channel, final List<Interaction> interactions, final SearchInteractionsContext searchContext, boolean generateUtterancesFromHighlights, boolean needHighlightsWhenNoSearch) {

		ExecutorService threadPool = null;

		try {
			if (interactions != null && interactions.size() > 0) {

				// get ids of all documents
				List<FilterFieldValue> documentIds = interactions.stream().map(i -> new FilterFieldValue(i.getId(), "")).collect(toList());

				String language = configurationService.getChannelLanguage(tenant, channel);

				logger.debug("Generating SPS Highlights for interactions result set with {} interactions", interactions.size());

				List<QueryTerm> locatedTerms = new ArrayList<>();

				TASQueryConfiguration queryConfiguration = this.textAnalyticsProvider.getVTASyntaxTASQueryConfiguration(language);

				List<String> termsExpressions = searchContext.getTerms();
				// generate list of all search terms
				if (termsExpressions != null) {
					for (String termsExpression : termsExpressions) {
						VTASyntaxParsingResult parsingResult = this.vtaSyntaxQueryAnalyzer.parseQuery(termsExpression, queryConfiguration);
						locatedTerms.addAll(parsingResult.getTerms());
					}
				}

				String requestId = ThreadContext.get(TAConstants.requestId);
				ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

				logger.debug("Generating SPS  Highlights for Search Interactions request. Tenant - {}, channel - {}, terms - {}", tenant, channel,
				             this.getSearchTermsDescription(locatedTerms));

				val tasks = new ArrayList<HighlightsforSpeakerCallable>();

				boolean needToGetTextElementHighlighting = true;

				// Terms tasks
				// @formatter:off
				List<QueryTerm> agentTerms = locatedTerms.stream()
				                                      .filter(t -> t.getSpeakerType().equals(com.verint.textanalytics.dal.darwin.vtasyntax.SpeakerType.Agent))
				                                      .distinct()
				                                      .collect(toList());
				if (agentTerms.size() > 0) {
					tasks.add(new HighlightsforSpeakerCallable(tenant, channel, documentIds, language, agentTerms, SpeakerType.Agent, requestId));
					needToGetTextElementHighlighting = false;
				}

				List<QueryTerm> customerTerms = locatedTerms.stream()
				                                      .filter(t -> t.getSpeakerType().equals(com.verint.textanalytics.dal.darwin.vtasyntax.SpeakerType.Customer))
				                                      .distinct()
				                                      .collect(toList());
				if (customerTerms.size() > 0) {
					tasks.add(new HighlightsforSpeakerCallable(tenant, channel, documentIds, language, customerTerms, SpeakerType.Customer, requestId));
					needToGetTextElementHighlighting = false;
				}

				List<QueryTerm> noSPSTerms = locatedTerms.stream()
				                                      .filter(t -> t.getSpeakerType().equals(com.verint.textanalytics.dal.darwin.vtasyntax.SpeakerType.NoSPS))
				                                      .distinct()
				                                      .collect(toList());
				if (noSPSTerms.size() > 0) {
					tasks.add(new HighlightsforSpeakerCallable(tenant, channel, documentIds, language, noSPSTerms, SpeakerType.Unknown, requestId));
					needToGetTextElementHighlighting = false;
				}
				// @formatter:on

				// get Entities and Relations filters from Search Context
				List<FilterField> filterFields = searchContext.getFilterFields();
				List<FilterFieldValue> filterEntities = new ArrayList<>();
				List<FilterFieldValue> filterRelations = new ArrayList<>();
				List<FilterFieldValue> filterKeyTerms = new ArrayList<>();
				List<FilterFieldValue> filterKeyTermsAgent = new ArrayList<>();
				List<FilterFieldValue> filterKeyTermsCustomer = new ArrayList<>();
				List<FilterFieldValue> filterEntitiesAgent = new ArrayList<>();
				List<FilterFieldValue> filterEntitiesCustomer = new ArrayList<>();
				List<FilterFieldValue> filterRelationsAgent = new ArrayList<>();
				List<FilterFieldValue> filterRelationsCustomer = new ArrayList<>();

				Boolean isFilterExist = false;

				if (filterFields != null) {

					for (FilterField filterField : filterFields) {

						if ((TAConstants.SchemaFieldNames.topics_f).equals(filterField.getName())) {
							filterEntities.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.relations_f).equals(filterField.getName())) {
							filterRelations.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.keyterms_f).equals(filterField.getName())) {
							filterKeyTerms.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.keyterms_f_agent).equals(filterField.getName())) {
							filterKeyTermsAgent.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.keyterms_f_customer).equals(filterField.getName())) {
							filterKeyTermsCustomer.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.topics_f_agent).equals(filterField.getName())) {
							filterEntitiesAgent.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.relations_f_agent).equals(filterField.getName())) {
							filterRelationsAgent.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.topics_f_customer).equals(filterField.getName())) {
							filterEntitiesCustomer.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}

						if ((TAConstants.SchemaFieldNames.relations_f_customer).equals(filterField.getName())) {
							filterRelationsCustomer.addAll(Arrays.asList(filterField.getValues()));
							isFilterExist = true;
						}
					}
				}

				// If no terms were searched, we wouldn't get highlights
				// so lets invoke additinal search with no terms in order to get highlights
				if (isFilterExist || (tasks.size() == 0 && needHighlightsWhenNoSearch)) {
					tasks.add(new HighlightsforSpeakerCallable(tenant, channel, documentIds, language, new ArrayList<QueryTerm>(), SpeakerType.Unknown, requestId));
				}

					if (!CollectionUtils.isEmpty(tasks)) {
					// invokeAll() returns when all tasks are complete
					threadPool = Executors.newFixedThreadPool(tasks.size());
					List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

					List<UtteranceHighlights> agentHighLights = null, customerHighlights = null;
					List<Utterance> agentUtterances = null, customerUtterances = null;

					// Collection for holding 1 or 2 results of noSPS tasks (for terms with no SPS or topics/relations tasks)
					List<List<UtteranceHighlights>> noSpeakerHighlightsResults = new ArrayList<List<UtteranceHighlights>>();
					List<List<Utterance>> noSPSUtterancesResults = new ArrayList<List<Utterance>>();

					if (lstFutures != null) {
						for (Future<?> future : lstFutures) {
							if (future.isDone()) {

								// get tasks result of task is done
								Object taskResult = future.get();
								if (taskResult != null && taskResult instanceof Triple<?, ?, ?>) {

									// result of Integer task
									val res = (Triple<SpeakerType, Object, Object>) taskResult;

									switch (res.getLeft()) {
										case Agent:
											agentHighLights = (List<UtteranceHighlights>) res.getMiddle();
											agentUtterances = (List<Utterance>) res.getRight();
											break;
										case Customer:
											customerHighlights = (List<UtteranceHighlights>) res.getMiddle();
											customerUtterances = (List<Utterance>) res.getRight();
											break;
										case Unknown:
											// The result can come either  from Text with no SPS task or from Relation/Topic task.

											noSpeakerHighlightsResults.add((List<UtteranceHighlights>) res.getMiddle());
											noSPSUtterancesResults.add((List<Utterance>) res.getRight());

											break;
										default:
											break;
									}
								}
							}
						}

						if (generateUtterancesFromHighlights) {
							this.generateInteractionsUtterances(interactions, agentUtterances, customerUtterances, noSPSUtterancesResults);
						}

						List<UtteranceHighlights> finalMergedHighlighting = highlightsMerger.mergeHighlights(agentHighLights, customerHighlights, noSpeakerHighlightsResults);

						List<UtteranceHighlights> mergedHighlights = new ArrayList<UtteranceHighlights>();

						mergedHighlights = this.filterUnrelevantHighlightsByCurrentSearch(finalMergedHighlighting, filterEntities, filterRelations, filterEntitiesAgent,
						                                                                  filterRelationsAgent, filterEntitiesCustomer, filterRelationsCustomer, filterKeyTerms, filterKeyTermsAgent, filterKeyTermsCustomer);

						this.updateRelationsHighlights(mergedHighlights);

 						this.addHighlightingToInteractions(interactions, mergedHighlights);
					}
				}
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.UtteranceHighlightsBuildError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}
	}

	/**
	 * The Search API generates highlights for all entities and relations, not
	 * only for those in search so filtering should be performed to include
	 * highlights only for entities and relations that were searched.
	 *
	 * @param utterances      list of highlights for all elements : terms, entities,
	 *                        relations
	 * @param filterEntities  entities in filter
	 * @param filterRelations relations in filter
	 *
	 */
	@SuppressWarnings("all")
	private List<UtteranceHighlights> filterUnrelevantHighlightsByCurrentSearch(List<UtteranceHighlights> utterances,
			List<FilterFieldValue> filterEntities, List<FilterFieldValue> filterRelations, List<FilterFieldValue> filterEntitiesAgent,
			List<FilterFieldValue> filterRelationsAgent, List<FilterFieldValue> filterEntitiesCustomer, List<FilterFieldValue> filterRelationsCustomer,
			List<FilterFieldValue> filterKeyTerms, List<FilterFieldValue> filterKeyTermsAgent, List<FilterFieldValue> filterKeyTermsCustomer) {
		List<UtteranceHighlights> filteredUtterances = new ArrayList<>();

		if (utterances != null) {
			// for each utterance
			for (UtteranceHighlights utterance : utterances) {

				// if utterance has entities highlights
				if (!CollectionUtils.isEmpty(utterance.getEntitiesHighlights()) && (filterEntities != null || filterEntitiesAgent != null || filterEntitiesCustomer != null)) {

					// filter entities highlights which are not for entities that were searched
					List<EntityHighlight> filteredEntityHighlights = new ArrayList<EntityHighlight>(),
							filteredEntityAgentHighlights = new ArrayList<EntityHighlight>(),
							filteredEntityCustomerHighlights = new ArrayList<EntityHighlight>();

					// @formatter:off
					filteredEntityHighlights = utterance.getEntitiesHighlights()
					                                    .stream()
					                                    .filter(eh -> {
						                                    Boolean matchFound = false;

															for (FilterFieldValue filterEntity : filterEntities) {
																if (eh.getTopic().startsWith(filterEntity.getValue().substring(1))) {
																	matchFound = true;
																}
															}
															return matchFound;
														})
					                                    .collect(toList());

					if (utterance.getSpeakerType() == SpeakerType.Agent)
						filteredEntityAgentHighlights = utterance.getEntitiesHighlights()
						                                    .stream()
						                                    .filter(eh -> {
							                                    Boolean matchFound = false;

																for (FilterFieldValue filterEntityAgent : filterEntitiesAgent) {
																	if (eh.getTopic().startsWith(filterEntityAgent.getValue().substring(1))) {
																		matchFound = true;
																	}
																}
																return matchFound;
															})
						                                    .collect(toList());

					if (utterance.getSpeakerType() == SpeakerType.Customer)
						filteredEntityCustomerHighlights = utterance.getEntitiesHighlights()
						                                    .stream()
						                                    .filter(eh -> {
							                                    Boolean matchFound = false;

																for (FilterFieldValue filterEntityCustomer : filterEntitiesCustomer) {
																	if (eh.getTopic().startsWith(filterEntityCustomer.getValue().substring(1))) {
																		matchFound = true;
																	}
																}
																return matchFound;
															})
						                                    .collect(toList());

					// @formatter:on

					utterance.setEntitiesHighlights(new ArrayList<EntityHighlight>());
					utterance.getEntitiesHighlights().addAll(filteredEntityHighlights);
					utterance.getEntitiesHighlights().addAll(filteredEntityAgentHighlights);
					utterance.getEntitiesHighlights().addAll(filteredEntityCustomerHighlights);

				} else {
					// no entities filters, so purge all entities highlights
					utterance.setEntitiesHighlights(null);
				}

				if (!CollectionUtils.isEmpty(utterance.getRelationsHighlights())
						&& (filterRelations != null || filterRelationsAgent != null || filterRelationsCustomer != null)) {

					// filter relations highlights which are not for relations that were searched
					List<RelationHighlight> filteredRelationHighlights = new ArrayList<RelationHighlight>(),
							filteredRelationAgentHighlights = new ArrayList<RelationHighlight>(),
							filteredRelationCustomerHighlights = new ArrayList<RelationHighlight>();

					filteredRelationHighlights = utterance.getRelationsHighlights().stream().filter(rh -> {
						Boolean matchFound = false;
						for (FilterFieldValue filterRelation : filterRelations) {
							if (rh.getRelation().startsWith(filterRelation.getValue().substring(1))) {
									matchFound = true;
							}
						}

						return matchFound;
					}).collect(toList());

					if (utterance.getSpeakerType() == SpeakerType.Agent)
						filteredRelationAgentHighlights = utterance.getRelationsHighlights().stream().filter(rh -> {
							Boolean matchFound = false;
							for (FilterFieldValue filterRelationAgent : filterRelationsAgent) {
								if (rh.getRelation().startsWith(filterRelationAgent.getValue().substring(1))) {
									matchFound = true;
								}
							}

							return matchFound;
						}).collect(toList());

					if (utterance.getSpeakerType() == SpeakerType.Customer)
						filteredRelationCustomerHighlights = utterance.getRelationsHighlights().stream().filter(rh -> {
							Boolean matchFound = false;
							for (FilterFieldValue filterRelationCustomer : filterRelationsCustomer) {
								if (rh.getRelation().startsWith(filterRelationCustomer.getValue().substring(1))) {
									matchFound = true;
								}
							}

							return matchFound;
						}).collect(toList());

					utterance.setRelationsHighlights(new ArrayList<RelationHighlight>());
					utterance.getRelationsHighlights().addAll(filteredRelationHighlights);
					utterance.getRelationsHighlights().addAll(filteredRelationAgentHighlights);
					utterance.getRelationsHighlights().addAll(filteredRelationCustomerHighlights);
				} else {
					utterance.setRelationsHighlights(null);
				}

				// if utterance has keyterms highlights
				if (!CollectionUtils.isEmpty(utterance.getKeyTermsHighlights())
						&& (filterKeyTerms != null || filterKeyTermsAgent != null || filterKeyTermsCustomer != null)) {

					// filter entities highlights which are not for entities that were searched
					List<KeyTermHighlight> filteredKeyTermsHighlights = new ArrayList<KeyTermHighlight>(),
							filteredKeyTermsAgentHighlights = new ArrayList<KeyTermHighlight>(),
							filteredKeyTermsCustomerHighlights = new ArrayList<KeyTermHighlight>();

					// @formatter:off
					filteredKeyTermsHighlights = utterance.getKeyTermsHighlights()
					                                    .stream()
					                                    .filter(eh -> {
						                                    Boolean matchFound = false;

															for (FilterFieldValue filterKeyTerm : filterKeyTerms) {
																if (eh.getKeyterm().equals(filterKeyTerm.getValue().substring(1))) {
																	matchFound = true;
																}
															}
															return matchFound;
														})
					                                    .collect(toList());

					// @formatter:off
					filteredKeyTermsAgentHighlights = utterance.getKeyTermsHighlights()
					                                    .stream()
					                                    .filter(eh -> {
						                                    Boolean matchFound = false;

															for (FilterFieldValue filterKeyTerm : filterKeyTermsAgent) {
																if (eh.getKeyterm().equals(filterKeyTerm.getValue().substring(1))) {
																	matchFound = true;
																}
															}
															return matchFound;
														})
					                                    .collect(toList());

					// @formatter:off
					filteredKeyTermsCustomerHighlights = utterance.getKeyTermsHighlights()
					                                    .stream()
					                                    .filter(eh -> {
						                                    Boolean matchFound = false;

															for (FilterFieldValue filterKeyTerm : filterKeyTermsCustomer) {
																if (eh.getKeyterm().equals(filterKeyTerm.getValue().substring(1))) {
																	matchFound = true;
																}
															}
															return matchFound;
														})
					                                    .collect(toList());
					// @formatter:on
					utterance.setKeyTermsHighlights(new ArrayList<KeyTermHighlight>());
					utterance.getKeyTermsHighlights().addAll(filteredKeyTermsHighlights);
					utterance.getKeyTermsHighlights().addAll(filteredKeyTermsAgentHighlights);
					utterance.getKeyTermsHighlights().addAll(filteredKeyTermsCustomerHighlights);

				} else {
					// no entities filters, so purge all entities highlights
					utterance.setKeyTermsHighlights(null);
				}

				if (!CollectionUtils.isEmpty(utterance.getTermHighlights()) || !CollectionUtils.isEmpty(utterance.getEntitiesHighlights()) || !CollectionUtils.isEmpty(
						utterance.getRelationsHighlights()) || !CollectionUtils.isEmpty(utterance.getSentimentHighlights()) || !CollectionUtils.isEmpty(
						utterance.getAllEntitiesHighlights()) || !CollectionUtils.isEmpty(utterance.getAllRelationsHighlights())
						|| !CollectionUtils.isEmpty(utterance.getKeyTermsHighlights())
						|| !CollectionUtils.isEmpty(utterance.getAllKeyTermsHighlights())) {

					if (filteredUtterances == null) {
						filteredUtterances = new ArrayList<>();
					}

					filteredUtterances.add(utterance);
				}
			}
		}

		return filteredUtterances;
	}

	private void updateRelationsHighlights(List<UtteranceHighlights> finalMergedHighlighting) {
		int length;
		List<RelationHighlight> relationsHighlights;
		RelationHighlight relationHighlight;
		List<RelationHighlight> relationsHighlightListToAdd;

		if (finalMergedHighlighting != null) {
			for (UtteranceHighlights utteranceHighlights : finalMergedHighlighting) {
				relationsHighlights = utteranceHighlights.getRelationsHighlights();

				if (relationsHighlights != null) {
					length = relationsHighlights.size();
					for (int i = 0; i < length; i++) {
						relationHighlight = relationsHighlights.get(i);
						relationsHighlightListToAdd = relationHighlight.getRelationHierarchy();

						for (RelationHighlight relationHighlightListToAdd : relationsHighlightListToAdd) {
							relationsHighlights.add(relationHighlightListToAdd);
						}
					}
				}
			}
		}
	}

	private String getSearchTermsDescription(List<QueryTerm> terms) {
		StringBuilder sbTerms = new StringBuilder();

		terms.stream().forEach(t -> {
			switch (t.getSpeakerType()) {
				case Agent:
					sbTerms.append(String.format(" [Term - %s, Speaker type - Agent]", t.getTerm()));
					break;
				case Customer:
					sbTerms.append(String.format(" [Term - %s, Speaker type - Customer]", t.getTerm()));
					break;
				case NoSPS:
					sbTerms.append(String.format(" [Term - %s, Speaker type - NoSPS]", t.getTerm()));
					break;
				default:
					break;
			}
		});

		return sbTerms.toString();
	}

	private void addHighlightingToInteractions(List<Interaction> interactions, List<UtteranceHighlights> highlights) {

		if (!CollectionUtils.isEmpty(interactions)) {

			HashSet<String> uniqueKeys = new HashSet<String>();
			// generate map of utterance id to utterance
			Map<String, Utterance> utterancesMap = interactions.stream().filter(i -> i.getUtterances() != null).flatMap(i -> i.getUtterances().stream().filter(j -> {
				if (uniqueKeys.contains(j.getId()))
					return false;
				else {
					uniqueKeys.add(j.getId());
					return true;
				}
			})).collect(Collectors.toMap(u -> u.getId(), u -> u));

			if (highlights != null) {
				for (UtteranceHighlights highlight : highlights) {

					Utterance utterance = utterancesMap.get(highlight.getDocumentId());
					if (utterance != null) {
						utterance.setTermsHighlighting(highlight.getTermHighlights());
						utterance.setEntitiesHighlighting(highlight.getEntitiesHighlights());
						utterance.setRelationsHighlighting(highlight.getRelationsHighlights());
						utterance.setKeytermsHighlighting(highlight.getKeyTermsHighlights());
						utterance.setSentimentHighlighting(highlight.getSentimentHighlights());
						utterance.setAllEntitiesHighlighting(highlight.getAllEntitiesHighlights());
						utterance.setAllRelationsHighlighting(highlight.getAllRelationsHighlights());
						utterance.setAllKeytermsHighlighting(highlight.getAllKeyTermsHighlights());
					}
				}
			}
		}
	}

	private void generateInteractionsUtterances(List<Interaction> interactions, List<Utterance> agentUtterances, List<Utterance> customerUtterances, List<List<Utterance>> noSPSUtterances) {
		Map<String, Utterance> utterancesMap = new HashMap<>();

		// merge agent utterances
		if (!CollectionUtils.isEmpty(agentUtterances)) {
			for (Utterance utterance : agentUtterances) {
				if (!utterancesMap.containsKey(utterance.getId())) {
					utterancesMap.put(utterance.getId(), utterance);
				}
			}
		}

		// merge customer utterances
		if (!CollectionUtils.isEmpty(customerUtterances)) {
			for (Utterance utterance : customerUtterances) {
				if (!utterancesMap.containsKey(utterance.getId())) {
					utterancesMap.put(utterance.getId(), utterance);
				}
			}
		}

		// merge no SPS utterances
		if (!CollectionUtils.isEmpty(noSPSUtterances)) {
			for (val arr : noSPSUtterances) {
				for (Utterance utterance : arr) {
					if (!utterancesMap.containsKey(utterance.getId())) {
						utterancesMap.put(utterance.getId(), utterance);
					}
				}
			}
		}

		if (!CollectionUtils.isEmpty(interactions)) {
			// generate hash of interactions
			Map<String, Interaction> interactionsMap = interactions.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));

			for (Utterance utterance : utterancesMap.values()) {
				Interaction interaction = interactionsMap.get(utterance.getParentId());
				if (interaction != null) {
					// add utterance to interaction
					interaction.addUtterance(utterance);
				}
			}
		}
	}

	/**
	 * HighlightsforSpeakerCallable.
	 *
	 * @author EZlotnik
	 */
	@AllArgsConstructor
	class HighlightsforSpeakerCallable implements Callable<Object> {
		@Setter
		private String tenant;

		@Setter
		private String channel;

		@Setter
		private List<FilterFieldValue> parentDocumentIds;

		@Setter
		private String language;

		@Setter
		private List<QueryTerm> speakerSearchTerms;

		@Setter
		private SpeakerType speakerType;

		@Setter
		private String requestId;

		@Override
		public Triple<SpeakerType, Object, Object> call() {
			val result = new MutableTriple<SpeakerType, Object, Object>(this.speakerType, null, null);

			try {
				ThreadContext.put(TAConstants.requestId, this.requestId);

				HighlightResult highlightResult = textAnalyticsProvider.getInteractionsHighlightsForSpeaker(tenant, channel, parentDocumentIds, speakerType, speakerSearchTerms,
				                                                                                            language);
				result.setLeft(speakerType);
				result.setMiddle(highlightResult.getHighlights());
				result.setRight(highlightResult.getUtterances());
			} catch (Exception ex) {
				Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
				Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
			}

			return result;
		}
	}

}
