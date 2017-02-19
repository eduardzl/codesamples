package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.bl.applicationservices.facet.textelements.TextElementsFacetService;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.facets.Facet;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Setter;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Facets service.
 * 
 * @author EZlotnik
 *
 */
public class FacetsService extends ApplicationService {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	private TextElementsFacetService textElementsFacetService;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	private final String currentSearchInteractionsCountThreadKey = "CurrentSearchInteractionsCount";

	/**
	 * Empty constructor.
	 */
	public FacetsService() {

	}


	/**
	 * Perform faceted Search.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext searchContext
	 * @param facetsQueries facetsQueries
	 * @param facetMetrics  metrics to calculate
	 * @param limit         limit on number of facet groups
	 * @return List<Facet> list of Facets
	 */
	public List<Facet> facetedSearch(String tenant, String channel, SearchInteractionsContext searchContext, List<String> facetsQueries, List<FieldMetric> facetMetrics, Integer limit) {
		return facetedSearch(tenant, channel, searchContext, facetsQueries, facetMetrics, limit, false);
	}

	/**
	 * Perform faceted Search.
	 *
	 * @param tenant         tenant
	 * @param channel        channel
	 * @param searchContext  searchContext
	 * @param facetsQueries  facetsQueries
	 * @param facetMetrics   metrics to calculate
	 * @param limit          limit number of terms in facet to extract
	 * @param preventExclude preventExclude
	 * @return List<Facet> list of Facets
	 */
	public List<Facet> facetedSearch(String tenant, String channel, SearchInteractionsContext searchContext, List<String> facetsQueries, List<FieldMetric> facetMetrics, Integer limit, boolean preventExclude) {
		ExecutorService threadPool = null;
		List<Facet> facets = new ArrayList<>();

		try {
			if (facetsQueries != null && facetsQueries.size() > 0) {
				threadPool = Executors.newFixedThreadPool(facetsQueries.size());

				String requestId = ThreadContext.get(TAConstants.requestId);

				val tasks = new ArrayList<Callable<Object>>();
				TextAnalyticsProvider analyticsProvider = this.textAnalyticsProvider;

				String language = configurationService.getChannelLanguage(tenant, channel);


				for (String facetQuery : facetsQueries) {

					tasks.add(() -> {
						// place request id into logger context
						ThreadContext.put(TAConstants.requestId, requestId);

						Facet facet = analyticsProvider.facetedSearch(tenant, channel, searchContext, facetQuery, facetMetrics, limit, language, preventExclude);

						return facet;
					});
				}

				ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

				// invokeAll() returns when all tasks are complete
				List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

				if (lstFutures != null) {
					for (Future<?> future : lstFutures) {
						if (future.isDone()) {

							// get tasks result of task is done
							Object taskResult = future.get();
							if (taskResult != null) {

								// result of Integer task
								val res = (Facet) taskResult;
								facets.add(res);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return facets;
	}

	/**
	 * Facet sentiment.
	 *
	 * @param tenant                 tenant
	 * @param channel                channel
	 * @param searchContext          searchContext
	 * @param sentimentFacetsQueries sentimentFacetsQueries
	 * @return List<Facet> list of sentiment facet
	 */
	public List<Facet> getSentimentFacet(String tenant, String channel, SearchInteractionsContext searchContext, List<String> sentimentFacetsQueries) {
		List<Facet> facets = new ArrayList<>();

		if (sentimentFacetsQueries != null && sentimentFacetsQueries.size() > 0) {
			TextAnalyticsProvider analyticsProvider = this.textAnalyticsProvider;

			String language = configurationService.getChannelLanguage(tenant, channel);


			for (String sentimentFacetQuery : sentimentFacetsQueries) {
				Facet sentimentFacet = analyticsProvider.getSentimentFacet(tenant, channel, searchContext, sentimentFacetQuery, language);
				facets.add(sentimentFacet);
			}
		}

		return facets;
	}






}
