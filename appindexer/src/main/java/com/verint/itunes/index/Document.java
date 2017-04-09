package com.verint.itunes.index;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ezlotnik on 4/9/2017.
 */
public class Document {
    private List<Field> fields;

    public Document() {
        fields = new ArrayList<>();
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public List<Field> getFields() {
        return this.fields;
    }
}
