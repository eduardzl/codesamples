package com.verint.textanalytics.web.viewmodel;

import com.verint.textanalytics.model.storedSearch.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * Category view model.
 * @author imor
 *
 */
public class Category extends StoredSearch {

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isActive;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isPublished;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int color;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int impact;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String lastReprocessedDateTimeGMT;

	@Getter
	@Setter
	@Accessors(chain = true)
	private long lastReprocessedDateTimeGMTMillis;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String reprocessStartDateTimeGMT;

	@Getter
	@Setter
	@Accessors(chain = true)
	private long reprocessStartDateTimeGMTMillis;


	@Getter
	@Setter
	@Accessors(chain = true)
	private String lastErrorTimeGMT;

	@Getter
	@Setter
	@Accessors(chain = true)
	private long lastErrorTimeGMTMillis;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean isReprocessingAllowed = false;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean shouldBeReprocessed = false;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int reprocessingStatus;
}