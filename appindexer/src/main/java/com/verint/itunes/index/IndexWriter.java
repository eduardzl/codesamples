package com.verint.itunes.index;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;

/**
 * Created by ezlotnik on 4/9/2017.
 */
public class IndexWriter {
    private FieldsIndexes fieldsIndexes;

    public IndexWriter () {
        fieldsIndexes = new FieldsIndexes();
    }

    /**
     * Adds document to the index
     * @param document
     */
    public void addDocument(Document document) {
        try {
            this.validateDocument(document);

            for (Field documentField : document.getFields()) {
                fieldsIndexes.indexField(documentField);
            }

        } catch (Exception ex) {

        }
    }

    private boolean validateDocument(Document document) throws InvalidDocumentException {
        if (document == null) {
            throw new InvalidDocumentException(ErrorMessages.DOCUMENT_IS_NULL);
        }

        if (CollectionUtils.isEmpty(document.getFields())) {
            throw new InvalidDocumentException(ErrorMessages.DOCUMENT_HAS_NO_FIELDS);
        }

        return true;
    }
}
