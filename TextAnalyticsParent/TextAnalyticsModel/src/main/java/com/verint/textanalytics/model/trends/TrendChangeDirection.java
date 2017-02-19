package com.verint.textanalytics.model.trends;

import lombok.Getter;

/**
 * Trend Change direction.
 * @author EZlotnik
 *
 */
public enum TrendChangeDirection {
	//@formatter:off
	Increase(0), 
	Decrease(1);		
	//@formatter:on

	@Getter
	private int changeDirection;

	private TrendChangeDirection(int value) {
		this.changeDirection = value;
	}
}
