package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * TAS Project.
 * @author yzanis
 *
 */
public class Channel {

	@Setter
	@Getter
	@Accessors(chain = true)
	private String displayName;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DataSourceStatus status;
}