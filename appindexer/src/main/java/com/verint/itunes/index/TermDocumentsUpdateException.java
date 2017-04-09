package com.verint.itunes.index;

/**
 * Created by ezlotnik on 4/9/2017.
 */
public class TermDocumentsUpdateException extends RuntimeException {

    public TermDocumentsUpdateException(String message, Exception ex) {
        super(message, ex);
    }

    public TermDocumentsUpdateException(String message) {
        super(message);
    }
}
