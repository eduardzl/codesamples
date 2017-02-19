package com.verint.textanalytics.dal.darwin;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Bean for representing values to be used in Solr requests and responses.
 * 
 * @author EZlotnik
 *
 */
public class SolrQueryParameters {

    @Getter
    @Setter
    @Accessors(chain = true)
    private Integer facetMinCount;

    @Getter
    @Setter
    @Accessors(chain = true)
    private Integer facetLimit;

    @Getter
    @Setter
    @Accessors(chain = true)
    private Integer topicsFacetLimit;

    @Getter
    @Setter
    @Accessors(chain = true)
    private int searchInteractionsResultSetSize;

    @Getter
    @Setter
    @Accessors(chain = true)
    private Integer searchInteractionsResultSetSizeInFacetQueries;

    @Getter
    @Setter
    @Accessors(chain = true)
    private Integer childDocumentsForParentLimit;

    @Getter
    @Setter
    @Accessors(chain = true)
    private String responseFormat;

    @Getter
    @Setter
    @Accessors(chain = true)
    private String responseIdentation;

}
