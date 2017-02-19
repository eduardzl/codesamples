package com.verint.textanalytics.bl.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.verint.textanalytics.common.security.MethodSecurityContext;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;

/**
 * @author EZlotnik
 *
 */
@Component
public class AuthorizationManager {

	@Autowired
	private MembershipProvider membershipProvider;


	/**
	 * Checks if user has access to method with specified required authorization
	 * annotations.
	 * 
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param requiredPrivileges
	 *            an object that has the list of privileges that user needs to
	 *            have in order to run the operation.
	 * @return Indication whether user was granted with access
	 */
	public Boolean checkAccess(String i360FoundationToken, MethodSecurityContext requiredPrivileges) {
		User user = this.getUser(i360FoundationToken);
		if (user.isSuperUser()) {
			return true;
		}

		return checkAccess(user, requiredPrivileges);
	}

	/***
	 * 
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param channelID
	 *            channel id to check if user can access it.
	 * @return Indication if the user can access this channel or not.
	 */
	public Boolean checkChannelPermissions(String i360FoundationToken, String channelID) {
		User user = this.getUser(i360FoundationToken);

		if (user.isSuperUser()) {
			return true;
		}

		for (Tenant tenant : user.getTenantsList()) {
			for (Channel curChannel : tenant.getChannels()) {
				if (curChannel.getId().equals(channelID)) {
					return true;
				}
			}
		}

		return false;
	}

	protected User getUser(String i360FoundationToken) {
		return membershipProvider.getUser(i360FoundationToken);
	}

	protected Boolean isAuthorized(User user, PrivilegeType[] requiredPrivileges, Boolean isRequiredAllPrivileges) {
		if (user.isSuperUser()) {
			return true;
		}

		Boolean isNoPrivileges = true;

		if (requiredPrivileges.length == 1 && requiredPrivileges[0] == PrivilegeType.NONE) {
			return true;
		}

		for (PrivilegeType pr : requiredPrivileges) {
			isNoPrivileges = false;

			if (isRequiredAllPrivileges && !user.hasPrivilege(pr.getPrivilegeName())) {
				return false;
			} else if (!isRequiredAllPrivileges && user.hasPrivilege(pr.getPrivilegeName())) {
				return true;
			}
		}

		return isNoPrivileges || isRequiredAllPrivileges;
	}

	/**
	 * Checks if user has access to method with specified required authorization
	 * annotations.
	 *
	 * @param user
	 *            User object
	 * @param requiredPrivileges
	 *            an object that has the list of privileges that user needs to
	 *            have in order to run the operation.
	 * @return Indication whether user was granted with access
	 */
	private Boolean checkAccess(User user, MethodSecurityContext requiredPrivileges) {
		if (user.isSuperUser()) {
			return true;
		}

		boolean hasAll = true;
		boolean hasAny = true;

		if (requiredPrivileges.getRequiredAllPrivileges() != null) {
			hasAll = this.isAuthorized(user, requiredPrivileges.getRequiredAllPrivileges(), true);
		}

		if (requiredPrivileges.getRequiredAnyPrivileges() != null) {
			hasAny = this.isAuthorized(user, requiredPrivileges.getRequiredAnyPrivileges(), false);
		}

		return hasAll && hasAny;
	}
}
