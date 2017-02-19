package com.verint.textanalytics.model.storedSearch;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

/**
 * Created by EZlotnik on 4/7/2016.
 */
public class CategoryReprocessingState {
	@Getter
	@Setter
	@Accessors(chain = true)
	private String categoryId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private CategoryReprocessingAction lastAction;

	// UTC time of last reprocess start
	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime reprocessStartTime;

	// UTC date time
	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime lastReprocessedTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean isBeingReprocessed;

	// UTC date time
	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime lastErrorTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private CategoryReprocessingStatus status;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String lastErrorMessage;
}
