package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Configuration as exposed to ExtJs application.
 * @author EZlotnik
 *
 */
public class Configuration {

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean customerUsageAnalyticsEnabled;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<com.verint.textanalytics.model.security.Channel> tenantChannels;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int autoCompletePrefixMinLengthConfiguration;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int searchTermsSuggestionType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int ajaxRequestTimeout;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int storedSearchNameMaxLength;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int storedSearchDescriptionMaxLength;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String modelEditorServiceURL;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean quickMenuMetadataAvailable;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean quickMenuOrgAvailable;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int searchTermsLimit;

}
