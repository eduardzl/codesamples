package com.verint.textanalytics.model.security;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Created by EZlotnik on 12/22/2015.
 */
public class UserApplicationSessionUpdateEvent extends ApplicationEvent {

	private UserApplicationSessionUpdateData sessionUpdateData;

	@Getter
	private String userName;

	/**
	 * Constructor.
	 * @param source the source of event
	 * @param sessionUpdateData the session update data
	 */
	public UserApplicationSessionUpdateEvent(Object source, UserApplicationSessionUpdateData sessionUpdateData) {
		super(source);

		this.sessionUpdateData = sessionUpdateData;
	}

	/**
	 * Constructor.
	 * @param source the source of session update event
	 * @param userName username
	 */
	public UserApplicationSessionUpdateEvent(Object source, String userName) {
		super(source);

		this.userName = userName;
	}
}
