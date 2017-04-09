package com.verint.itunes.index;

/**
 * Created by ezlotnik on 4/9/2017.
 */
public final class ErrorMessages {
    public static final String DOCUMENT_IS_NULL = "Document to index is null";
    public static final String DOCUMENT_HAS_NO_FIELDS = "Document to index has no fields";
    public static final String TERM_DOCUMENTS_UPDATE_ERROR  = "Failed to update documents list for term %s. Document id %s";
    public static final String TERM_DOCUMENTS_LOCK_ACQUIRE_FAILED  = "Failed to acquire lock to update documents for term %s. Document id %s";
}
