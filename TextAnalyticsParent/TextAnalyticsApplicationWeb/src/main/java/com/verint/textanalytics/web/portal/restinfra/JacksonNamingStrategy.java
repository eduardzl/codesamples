package com.verint.textanalytics.web.portal.restinfra;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/** Jackson api is used extensively to convert json to Object and Object to JSON.
 * @author EZlotnik
 *
 */
public class JacksonNamingStrategy extends PropertyNamingStrategy {
    @Override
    public String nameForField(MapperConfig config, AnnotatedField field, String defaultName) {
        return convert(defaultName);

    }

    @Override
    public String nameForGetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        return convert(defaultName);
    }

    /*  Being used to Jackson to generate setterName when converting from json sent from ExtJs to Java request object.
     * 
     */
    @Override
    public String nameForSetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {        
        return String.format("set%s", convert(defaultName));
    }

    
    /** Generates name to appear in serialized version.
     * @param defaultName property name
     * @return serialized object property name 
     */
    public String convert(String defaultName) {
        char[] arr = defaultName.toCharArray();
        if (arr.length != 0) {
            if (Character.isLowerCase(arr[0])) {
                char upper = Character.toUpperCase(arr[0]);
                arr[0] = upper;
            }
        }
        return new StringBuilder().append(arr).toString();
    }

}
