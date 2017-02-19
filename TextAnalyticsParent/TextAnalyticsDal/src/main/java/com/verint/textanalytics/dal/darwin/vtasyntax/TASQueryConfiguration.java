package com.verint.textanalytics.dal.darwin.vtasyntax;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by EZlotnik on 2/24/2016.
 */
public class TASQueryConfiguration {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String textNoSPSField;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String textAgentField;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String textCustomerField;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean escapeValuesEnabled = true;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean commonGramsFilterEnabled = true;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int wildCardPrefixMinLength = 3;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String language;
}
