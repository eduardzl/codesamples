package com.verint.textanalytics;

/**
 * Created by EZlotnik on 2/18/2017.
 */
public class Category {
    private String name;
    private Integer id;

    public Category(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Integer getId() {
        return this.id;
    }
}
