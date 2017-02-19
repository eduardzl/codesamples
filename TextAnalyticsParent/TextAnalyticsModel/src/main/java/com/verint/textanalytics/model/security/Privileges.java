package com.verint.textanalytics.model.security;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author EZlotnik Represents a privilege of user Has a list of privileges
 *         assigned to a user
 */
public class Privileges {

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> privilegesList;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isSuperUser;

	/**
	 * Constructor of Privileges class.
	 */
	public Privileges() {
		this.privilegesList = new ArrayList<String>();
		this.isSuperUser = false;
	}

	/**
	 * Adds privilege to list of user privileges.
	 * @param privilege
	 *            - privilege to be added to list of user privileges
	 */
	public void addPrivilege(String privilege) {
		this.privilegesList.add(privilege);
	}

	/**
	 * @param privilege
	 *            - privilege to be checked.
	 * @return return an indication if user has privilege
	 */
	public boolean hasPrivilege(String privilege) {
		return this.privilegesList.contains(privilege);
	}
}
