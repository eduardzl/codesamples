package com.verint.textanalytics.web.viewmodel;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/*** Base Stored Search.
 * @author imor
 */
public abstract class StoredSearch {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String description;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int lastModifiedByUserId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String lastModifiedByUserName;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String lastChangeDateTimeGMT;

	/**
	 * The number of milliseconds since the epoch, 1970-01-01T00:00:00Z.
	 */
	@Getter
	@Setter
	@Accessors(chain = true)
	private long lastChangeDateTimeGMTMillis;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SearchInteractionsContext searchContext;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String searchContextVersion;

}