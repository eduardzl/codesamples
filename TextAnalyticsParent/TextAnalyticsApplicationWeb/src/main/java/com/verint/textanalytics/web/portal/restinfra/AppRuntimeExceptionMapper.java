package com.verint.textanalytics.web.portal.restinfra;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import lombok.val;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.AppRuntimeException;
import com.verint.textanalytics.common.exceptions.AuthorizationErrorCode;
import com.verint.textanalytics.common.exceptions.AuthorizationException;
import com.verint.textanalytics.common.exceptions.ConfigurationErrorCode;
import com.verint.textanalytics.common.exceptions.ConfigurationException;
import com.verint.textanalytics.common.exceptions.StoredSearchesErrorCode;
import com.verint.textanalytics.common.exceptions.StoredSearchesException;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.ExceptionUtils;
import com.verint.textanalytics.common.utils.JSONUtils;

import java.util.List;

/**
 * AppExceptionMapper.
 * 
 * @author imor
 *
 */
@Provider
public class AppRuntimeExceptionMapper implements ExceptionMapper<AppRuntimeException> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ConfigurationManager configurationManager;

	@Override
	public Response toResponse(AppRuntimeException ex) {

		if (ex instanceof TextQueryExecutionException) {
			return textQueryExecutionExceptionHandler((TextQueryExecutionException) ex);
		}

		if (ex instanceof ConfigurationException) {
			return configurationExceptionHandler((ConfigurationException) ex);
		}

		if (ex instanceof AuthorizationException) {
			return authorizationExceptionHandler((AuthorizationException) ex);
		}

		if (ex instanceof StoredSearchesException) {
			return storedSearchesExceptionHandler((StoredSearchesException) ex);
		}


		val errorDetails = new ErrorDetails(ex);
		logger.error("Unknown Failure. Response to client - {}. Stack trace - {}.",  errorDetails, ExceptionUtils.getStackTrace(ex));

		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();
	}

	private Response textQueryExecutionExceptionHandler(TextQueryExecutionException ex) {

		Response res;
		val errorDetails = new ErrorDetails(ex);

		// add stack trace if we in debug mode
		if (configurationManager != null && configurationManager.getApplicationConfiguration().isSendExceptionDetailsToClient()) {
			errorDetails.setStackTrace(ExceptionUtils.getStackTrace(ex));
		}

		switch ((TextQueryExecutionErrorCode) ex.getAppExecutionErrorCode()) {
			case TextQueryGenerationError:
			case TextQueryExecutionError:
			case JsonResponseParsingError:
			case RESTTextQueryExecutionError:
				res = Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();
				break;

			default:
				res = Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();
				break;
		}

		logger.error("Exception raised in application. Message - {}, Stack trace - {}", ex.getMessage(), ExceptionUtils.getStackTrace(ex));

		return res;
	}

	private Response configurationExceptionHandler(ConfigurationException ex) {

		Response res;
		val errorDetails = new ErrorDetails(ex);

		// add stack trace if we in debug mode
		if (configurationManager != null && configurationManager.getApplicationConfiguration().isSendExceptionDetailsToClient()) {
			errorDetails.setStackTrace(ExceptionUtils.getStackTrace(ex));
		}

		switch ((ConfigurationErrorCode) ex.getAppExecutionErrorCode()) {
			case ApplicationConfigurationNotValidError:

				errorDetails.setMessageKey(TAConstants.ErrorMessageKeys.applicationConfigurationNotValidError);

				res = Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();
				break;

			default:
				res = Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();
				break;
		}

		logger.error("ConfigurationException. Message - {} Stack strace - {}", ex.getMessage(),  ExceptionUtils.getStackTrace(ex));

		return res;
	}

	private Response authorizationExceptionHandler(AuthorizationException ex) {
		Response res;

		switch ((AuthorizationErrorCode) ex.getAppExecutionErrorCode()) {
			case UserNotLogedInError:
				res = Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
				break;
			case InvalidAccessError:
				res = Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
				break;
			default:
				res = Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
				break;
		}

		return res;
	}

	private Response storedSearchesExceptionHandler(StoredSearchesException ex) {
		Response res;

		val errorDetails = new ErrorDetails(ex);

		// add stack trace if we in debug mode
		if (configurationManager != null && configurationManager.getApplicationConfiguration().isSendExceptionDetailsToClient()) {
			errorDetails.setStackTrace(ExceptionUtils.getStackTrace(ex));
		}

		String errorKey = "";

		switch ((StoredSearchesErrorCode) ex.getAppExecutionErrorCode()) {

			// Categories Loading errors
			case CategoriesParsingError:
				errorKey = TAConstants.ErrorMessageKeys.categoriesLoadParsingError;
				break;
			case CategoriesRetriveError:
				errorKey = TAConstants.ErrorMessageKeys.categoriesLoadLoadingError;
				break;

			// Saved Searches Loading error
			case SavedSearchesParsingError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchesLoadParsingError;
				break;
			case SavedSearchesRetriveError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchesLoadLoadingError;
				break;

			// Category Add Errors
			case CategoryAddNameAllreadyExistsError:
				errorKey = TAConstants.ErrorMessageKeys.categoryAddNameExists;
				break;
			case CategoryAddInvalidNameError:
				errorKey = TAConstants.ErrorMessageKeys.categoryAddInvalidName;
				break;
			case CategoriesAddError:
				errorKey =  TAConstants.ErrorMessageKeys.categoryAddGenericError;
				break;

			// Saved Search Add errors
			case SavedSearchAddNameAllreadyExistsError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchAddNameExists;
				break;
			case SavedSearchAddInvalidNameError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchInvalidName;
				break;
			case SavedSearchAddError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchAddGenericError;
				break;

			// Category Update errors
			case CategoryUpdateNotFoundError:
				errorKey = TAConstants.ErrorMessageKeys.categoryUpdateCategoryWasNotFound;
				break;
			case CategoryUpdateNotLatestVersionError:
				errorKey = TAConstants.ErrorMessageKeys.categoryUpdateCategoryNotLatestVersion;
				break;
			case CategoryUpdateError:
				errorKey = TAConstants.ErrorMessageKeys.categoryUpdateGenericError;
				break;

			// Saved Search Update errors
			case SavedSearchUpdateWasNotFoundError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchSavedSearchWasNotFound;
				break;
			case SavedSearchUpdateNotLatestVersionError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchSavedSearchNotLatestVersion;
				break;
			case SavedSearchUpdateError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchGenericError;
				break;

			// Category Remove error
			case CategoryRemoveNotLatestVersion:
				errorKey = TAConstants.ErrorMessageKeys.categoryRemoveCategoryNotLatestVersion;
				break;
			case CategoryRemoveError:
				errorKey = TAConstants.ErrorMessageKeys.categoryRemoveCategoryRemoveError;
				break;

			// Saved Search Removal errors
			case SavedSearchRemoveNotLatestVersion:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchRemoveSavedSearchNotLatestVersion;
				break;
			case SavedSearchesRemoveError:
				errorKey = TAConstants.ErrorMessageKeys.savedSearchRemoveSavedSearchRemoveError;
				break;

			case StoredSearchesUpdateGenericError:
				errorKey = TAConstants.ErrorMessageKeys.storedSearchUpdateGenericError;
				break;

			// Categories Facet
			case RetrieveCategoriesFacetError:
				errorKey = TAConstants.ErrorMessageKeys.categoriesFacetFacetLoadingError;
				break;

			default:
				errorKey = TAConstants.ErrorMessageKeys.categoriesGenericError;
				break;
		}

		errorDetails.setMessageKey(errorKey);

		res = Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();

		logger.error("Exception raised in Stored Searches. Message - {}, Stack trace - {}", ex.getMessage(), ExceptionUtils.getStackTrace(ex));

		return res;
	}

}