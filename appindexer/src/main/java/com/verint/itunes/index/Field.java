package com.verint.itunes.index;

/**
 * Created by ezlotnik on 4/9/2017.
 */
public class Field {
    private String fieldName;
    private String fieldValue;

    public Field(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getName() {
        return this.fieldName;
    }

    public String getValue() {
        return this.fieldValue;
    }
}
