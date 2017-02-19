package com.verint.textanalytics.web.portal.restinfra;

import lombok.Getter;
import lombok.NonNull;

/**
 * Encapsulates information about request.
 * 
 * @author EZlotnik
 *
 */
public class RequestMethodInfo {

    @Getter
    private String className;

    @Getter
    private String methodName;

    /**
     * Extracts Service name and Service method from url.
     * @param urlPath url of the REST method
     * @throws Exception exception thrown
     */
    public void parseUrl(@NonNull String urlPath) throws Exception {

        // extract class and method names from URI request
        String[] pathArr = urlPath.split("/");

        if (pathArr == null || pathArr.length < 2) {
            throw new Exception("Invalid URL format: could not extract classname / methodname");
        } else {
            this.className = pathArr[pathArr.length - 2];
            this.methodName = pathArr[pathArr.length - 1];
        }
    }

}
