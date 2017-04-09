package com.verint.itunes.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ezlotnik on 4/9/2017.
 */
public class InvertedIndex {
    private Logger logger = LogManager.getLogger(this.getClass());

    private Map<String, Set<String>> termsInDocuments;
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     *
     */
    public InvertedIndex() {
        termsInDocuments = new HashMap<>();
    }

    public void setTermAsPresentInDocument(String term, String documentId) {
        try {
            boolean lockAcquired = rwLock.writeLock().tryLock(200, TimeUnit.MILLISECONDS);
            if (lockAcquired) {
                Set<String> documentsIdsForTerm = null;

                if (this.termsInDocuments.containsKey(term)) {
                    // update documents list for term
                    documentsIdsForTerm = this.termsInDocuments.get(term);
                    documentsIdsForTerm.add(documentId);
                } else {
                    // no document for term exist, so create new Set
                    // add first value to it
                    documentsIdsForTerm = new HashSet<>();
                    documentsIdsForTerm.add(documentId);

                    this.termsInDocuments.put(term, documentsIdsForTerm);
                }
            } else {
                logger.error("Failed to acquire lock for updating document for term - {}, document id - {}", term, documentId);

                throw new TermDocumentsUpdateException(String.format(ErrorMessages.TERM_DOCUMENTS_LOCK_ACQUIRE_FAILED, term, documentId));
            }
        } catch (TermDocumentsUpdateException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception adding document to terms to documents mapping");

            throw new TermDocumentsUpdateException(String.format(ErrorMessages.TERM_DOCUMENTS_UPDATE_ERROR, term, documentId), ex);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
