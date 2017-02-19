package com.verint.textanalytics.common.security;

import lombok.Getter;
import lombok.Setter;

/***
 * 
 * @author yzanis
 *
 */
public class MethodSecurityContext {

	@Setter
	@Getter
	private OperationPrivelegesAnnotation.PrivilegeType[] requiredAllPrivileges;

	@Setter
	@Getter
	private OperationPrivelegesAnnotation.PrivilegeType[] requiredAnyPrivileges;

	@Setter
	@Getter
	private boolean isChannelAuthenticationRequired;

}