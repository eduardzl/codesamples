package com.verint.textanalytics.web.viewmodel.requestparams;

import lombok.Getter;
import lombok.Setter;

/** Base REST request parameters object.
 * @author EZlotnik
 *
 */
public class RestRequestParams {
    @Getter
    @Setter
    private String channel;
}
