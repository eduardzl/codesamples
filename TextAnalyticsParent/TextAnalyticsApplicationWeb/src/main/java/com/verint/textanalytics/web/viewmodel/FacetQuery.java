package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/** FacetQuery.
 * @author EZlotnik
 *
 */
public class FacetQuery {
    // private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    @Accessors(chain = true)
    private String fieldName;

    @Getter
    @Setter
    @Accessors(chain = true)
    private int filterType;    
}
