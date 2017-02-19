package com.verint.textanalytics.bl.security;

import com.verint.textanalytics.model.security.User;

/**
 * @author yzanis
 *
 */
public interface MembershipProvider {

	/**
	 * @param i360FoundationToken
	 *            userToken
	 * @return user data
	 */
	public User getUser(String i360FoundationToken);
}