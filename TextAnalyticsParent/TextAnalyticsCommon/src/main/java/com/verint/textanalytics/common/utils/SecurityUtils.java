package com.verint.textanalytics.common.utils;

import lombok.NonNull;


/** Security utils.
 * @author EZlotnik
 *
 */
public final class SecurityUtils {
    
    private SecurityUtils() {
        
    }
    
    /**
     * Extracts user name from Foundation token.
     * 
     * @param foundationToken
     * @return
     */
    
    /** Extracts username from foundation token.
     * @param foundationToken token
     * @return username
     */
    public static String getUserNameFromFoundationToken(@NonNull String foundationToken) {
        return "wsuperuser";
    }
}
