package com.verint.textanalytics.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotations class.
 * @author EZlotnik
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperationPrivelegesAnnotation {

	/**
	 * PrivilegeType.
	 * @author EZlotnik
	 *
	 */
	public enum PrivilegeType {
		USEAPPLICATION, INTELLIFIND, SEARCHBYKEYWORDS, VIEWTRANSCRIP, ADDFORM, TEXTMODELMANAGEMENT, NONE;

		public String getPrivilegeName() {
			return "IF." + this.name();
		}
	};

	/***
	 * require.
	 * @return list of Privileges
	 */
	PrivilegeType[] requiredAllPrivileges() default PrivilegeType.NONE;

	/***
	 * require.
	 * @return list of Privileges
	 */
	PrivilegeType[] requiredAnyOfPrivileges() default PrivilegeType.NONE;
}
