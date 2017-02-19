package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/***
 * 
 * @author yzanis
 *
 */
public enum StoredSearchesErrorCode implements AppExecutionErrorCode {

	// @formatter:off

	// Categories Loading errors
	CategoriesParsingError(0),
	CategoriesRetriveError(1),

	// Saved Sarches Loading errors
	SavedSearchesParsingError(2),
	SavedSearchesRetriveError(3),

	// Categories Add erros
	CategoryAddNameAllreadyExistsError(4),
	CategoryAddInvalidNameError(5),
	CategoriesAddError(6),

	// Saved Searches Add errors
	SavedSearchAddNameAllreadyExistsError(7),
	SavedSearchAddInvalidNameError(8),
	SavedSearchAddError(9),

	// Category Update Error
	CategoryUpdateNotFoundError(10),
	CategoryUpdateNotLatestVersionError(11),
	CategoryUpdateError(12),

	// Saved Search Update Error
	SavedSearchUpdateWasNotFoundError(13),
	SavedSearchUpdateNotLatestVersionError(14),
	SavedSearchUpdateError(15),

	// Remove Category Error
	CategoryRemoveNotLatestVersion(16),
	CategoryRemoveError(17),

	// Saved Search Remove error
	SavedSearchRemoveNotLatestVersion(18),
	SavedSearchesRemoveError(19),

	StoredSearchesUpdateGenericError(20),

	RetrieveCategoriesFacetError(21),

	CategoryReprocessingInvocationError(22),
	CategoryReprocessingIsNotAllowed(23),

	CategoriesReprocessingStatusParsingError(24),
	CategoriesReprocessingStatusRetrieveError(25);
	// @formatter:on

	@Getter
	private final int errorCode;

	private StoredSearchesErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}