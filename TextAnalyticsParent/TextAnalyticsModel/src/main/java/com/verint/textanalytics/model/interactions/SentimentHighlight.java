package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by YHemi on 12/10/2015.
 */
public class SentimentHighlight extends BaseHighlight {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int value;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected List<Position> positions;
}
