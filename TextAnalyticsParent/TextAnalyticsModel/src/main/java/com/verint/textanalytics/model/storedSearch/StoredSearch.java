package com.verint.textanalytics.model.storedSearch;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

/***
 * 
 * @author yzanis
 *
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
	private DateTime lastChangeDateTimeGMT;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SearchInteractionsContext searchContext;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String searchContextVersion;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String query;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String debugQuery;
}