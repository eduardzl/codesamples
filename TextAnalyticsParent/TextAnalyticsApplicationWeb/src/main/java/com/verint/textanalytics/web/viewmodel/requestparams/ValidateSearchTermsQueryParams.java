package com.verint.textanalytics.web.viewmodel.requestparams;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** VTA Syntax Validation Query parameters.
 * @author EZlotnik
 */
public class ValidateSearchTermsQueryParams  extends RestRequestParams implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Setter
    @Getter
    private String searchTermsQuery;
}
