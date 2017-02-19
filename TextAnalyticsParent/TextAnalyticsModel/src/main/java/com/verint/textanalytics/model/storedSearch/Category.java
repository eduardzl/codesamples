package com.verint.textanalytics.model.storedSearch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

/***
 * 
 * @author yzanis
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
	private CategoryReprocessingState reprocessingState;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean isReprocessingAllowed;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean shouldBeReprocessed;
}