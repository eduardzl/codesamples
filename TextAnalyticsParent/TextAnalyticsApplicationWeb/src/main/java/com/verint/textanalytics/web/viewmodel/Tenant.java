package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * Tenant model.
 * @author yzanis
 *
 */
public class Tenant {

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

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Channel> channels;
}