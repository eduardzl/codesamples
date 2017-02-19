package com.verint.textanalytics.common.logger;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple log entry bean.
 * @author Todd Nine
 *
 */
public class Entry {

	@Getter
	@Setter
	private long id;

	@Getter
	@Setter
	private String message;

	@Getter
	@Setter
	private long time;
}
