package com.verint.itunes.index;

/**
 * Created by ezlotnik on 4/9/2017.
 */
public class InvalidDocumentException extends Exception {

    public InvalidDocumentException(Exception ex) {
        super(ex);
    }

    public InvalidDocumentException(String message) {
        super(message);
    }
}
