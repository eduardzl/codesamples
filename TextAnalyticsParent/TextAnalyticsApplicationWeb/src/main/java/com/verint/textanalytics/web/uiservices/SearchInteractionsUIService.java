package com.verint.textanalytics.web.uiservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.bl.applicationservices.SearchInteractionsService;
import com.verint.textanalytics.dal.darwin.SolrQueryParameters;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionException;
import com.verint.textanalytics.model.interactions.Entity;
import com.verint.textanalytics.model.interactions.Relation;
import com.verint.textanalytics.model.interactions.ResultsQuantity;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.web.portal.restinfra.ErrorDetails;
import com.verint.textanalytics.web.portal.restinfra.VTASyntaxExceptionMapper;
import com.verint.textanalytics.web.viewmodel.Interaction;
import com.verint.textanalytics.web.viewmodel.InteractionsListDataResult;
import com.verint.textanalytics.web.viewmodel.SearchQueryValidationResult;
import com.verint.textanalytics.web.viewmodel.Utterance;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Comparator.comparing;

/**
 * Search Documents UI Service.
 *
 * @author EZlotnik
 */
public class SearchInteractionsUIService extends BaseUIService {

	@Autowired
	private SearchInteractionsService searchInteractionsService;

	@Autowired
	private ViewModelConverter viewModelConverter;

	@Autowired
	private SolrQueryParameters queryParameters;

	@Autowired
	private VTASyntaxExceptionMapper vtaSyntaxExceptionMapper;

	/**
	 * C'tor.
	 */
	public SearchInteractionsUIService() {
		super();
	}

	/**
	 * Redirects request to BL service.
	 *
	 * @param i360FoundationToken foundation token
	 * @param channel             channel of data to search in
	 * @param searchContext       search context : filter fields and search terms
	 * @param pageStart           start of interactions range
	 * @param pageSize            number of interactions in page
	 * @param sortProperty        sort field
	 * @param sortDirection       sort direction
	 * @return list of documents
	 */
	public InteractionsListDataResult<Interaction> searchInteractions(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, int pageStart, int pageSize, String sortProperty, String sortDirection) {

		val result = new InteractionsListDataResult<com.verint.textanalytics.web.viewmodel.Interaction>();

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		// retrieve list of interactions matching the search
		val searchInteractionsResult = searchInteractionsService.searchInteractions(userTenant, channel, searchContext, pageStart, pageSize, viewModelConverter.mapMetadataFieldName(sortProperty), sortDirection);

		val interactions = new ArrayList<com.verint.textanalytics.web.viewmodel.Interaction>();

		// convert list of interactions to ViewModel document
		if (searchInteractionsResult != null && searchInteractionsResult.getInteractions() != null) {

			//@formatter:off
									
			for (val interaction : searchInteractionsResult.getInteractions()) {
				interactions.add(viewModelConverter.convertToViewModelInteraction(interaction));
			}

			result.setMaxRelevancyScore(searchInteractionsResult.getMaxScore());
			result.setData(interactions)					
				  .setTotalCount(searchInteractionsResult.getTotalNumberFound() > this.queryParameters.getSearchInteractionsResultSetSize() ? this.queryParameters.getSearchInteractionsResultSetSize() 
						  																													: searchInteractionsResult.getTotalNumberFound());
			//@formatter:on
		}

		result.setSuccess(true);
		return result;
	}

	/**
	 * Retrieves interaction data for review.
	 *
	 * @param i360FoundationToken I360 foundation token
	 * @param channel             channel
	 * @param interactionId       interaction's document id
	 * @param searchContext       search context : filter fields and search terms
	 * @return interaction's data for review
	 */
	public com.verint.textanalytics.web.viewmodel.InteractionPreview getInteractionPreview(String i360FoundationToken, String channel, String interactionId, SearchInteractionsContext searchContext) {

		com.verint.textanalytics.web.viewmodel.InteractionPreview interaction = null;

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		val rsInteraction = searchInteractionsService.getInteractionPreview(userTenant, channel, interactionId, searchContext);

		if (rsInteraction != null) {
			interaction = viewModelConverter.convertToViewModelInteractionPreview(rsInteraction);

			// Sort Entities and Relations of each utterance according to level number
			if (interaction.getUtterances() != null) {
				for (Utterance utterance : interaction.getUtterances()) {
					Collections.sort(utterance.getEntities(), comparing(Entity::getLevelNumber));
					Collections.sort(utterance.getRelations(), comparing(Relation::getLevelNumber));
				}
			}
		}

		return interaction;
	}

	/**
	 * Validates Search Terms Query.
	 *
	 * @param i360FoundationToken I360 foundation token
	 * @param searchQuery    search query
	 * @return indication of terms search validity
	 */
	public SearchQueryValidationResult validateSearchTermsQuery(String i360FoundationToken, final String searchQuery) {
		SearchQueryValidationResult validationResult = new SearchQueryValidationResult();

		try {
			searchInteractionsService.validateSearchTermsQuery(searchQuery);

			// if no exception is thrown, the query was parsed successefully
			validationResult.setValid(true);
		} catch (VTASyntaxProcessingException ex) {
			// query is invalid
			ErrorDetails errorDetails = vtaSyntaxExceptionMapper.vtaSyntaxProcessingToErrorDetails(ex);
			validationResult.setValid(false);
			validationResult.setErrorKey(errorDetails.getMessageKey());
		} catch (VTASyntaxRecognitionException ex) {
			// query is invalid
			ErrorDetails errorDetails = vtaSyntaxExceptionMapper.vtaSyntaxRecognitionToErrorDetails(ex);
			validationResult.setValid(false);
			validationResult.setErrorKey(errorDetails.getMessageKey());
		} catch (Exception ex) {
			Throwables.propagate(ex);
		}

		return validationResult;
	}

	/**
	 * getResultsQuantity.
	 *
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param searchContext       searchContext
	 * @param backgroundContext   backgroundContext
	 * @return ResultsQuantity
	 */
	public ResultsQuantity getResultsQuantity(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		// retrieve list of interactions matching the search
		return searchInteractionsService.getResultSetsInteractionsQuantity(userTenant, channel, searchContext, backgroundContext);
	}

	/**
	 * Retrieves interaction data for review.
	 *
	 * @param i360FoundationToken I360 foundation token
	 * @param channel             channel
	 * @param interactionId       interaction's document id
	 * @param searchContext       search context : filter fields and search terms
	 * @return interaction's data for review
	 */
	public com.verint.textanalytics.web.viewmodel.InteractionPreviewCIV getInteractionPreviewCIV(String i360FoundationToken, String channel, String interactionId, SearchInteractionsContext searchContext) {
		com.verint.textanalytics.web.viewmodel.InteractionPreviewCIV interaction = null;

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		val rsInteraction = searchInteractionsService.getInteractionPreview(userTenant, channel, interactionId, searchContext);
		if (rsInteraction != null) {
			interaction = viewModelConverter.convertToViewModelInteractionPreviewCIV(rsInteraction);
		}

		return interaction;
	}
}
