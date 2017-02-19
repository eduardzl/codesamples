package com.verint.textanalytics.bl.security.membership;

import static org.junit.Assert.*;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.verint.textanalytics.bl.security.AuthorizationManager;
import com.verint.textanalytics.bl.security.FoundationMembershipProvider;
import com.verint.textanalytics.bl.security.MembershipProvider;
import com.verint.textanalytics.common.security.MethodSecurityContext;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;

public class AuthorizationManagerTest {

	@InjectMocks
	@Spy
	private AuthorizationManager authorizationManager;

	@Mock
	private MembershipProvider membershipProvider = new FoundationMembershipProvider();

	@Mock
	private User user = new User();

	/**
	 * @throws java.lang.Exception
	 *             Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isAuthorized_All1() {

		Boolean result;
		User user = new User();
		user.addPrivilege("IF.INTELLIFIND");
		user.addPrivilege("IF.ADDFORM");

		PrivilegeType[] requiredAllPrivileges = new PrivilegeType[2];
		requiredAllPrivileges[0] = PrivilegeType.INTELLIFIND;
		requiredAllPrivileges[1] = PrivilegeType.ADDFORM;

		PrivilegeType[] requiredAnyOfPrivileges = new PrivilegeType[0];

		Mockito.when(membershipProvider.getUser("user4")).thenReturn(user);

		MethodSecurityContext methodSecurityContext = new MethodSecurityContext();
		methodSecurityContext.setRequiredAllPrivileges(requiredAllPrivileges);
		methodSecurityContext.setRequiredAnyPrivileges(requiredAnyOfPrivileges);

		result = authorizationManager.checkAccess("user4", methodSecurityContext);

		Assert.assertTrue(result);
	}

	@Test
	public void isAuthorized_All2() {

		Boolean result;
		User user = new User();

		user.addPrivilege("IF.INTELLIFIND");

		PrivilegeType[] requiredAllPrivileges = new PrivilegeType[2];
		requiredAllPrivileges[0] = PrivilegeType.INTELLIFIND;
		requiredAllPrivileges[1] = PrivilegeType.ADDFORM;

		PrivilegeType[] requiredAnyOfPrivileges = new PrivilegeType[0];

		Mockito.when(membershipProvider.getUser(null)).thenReturn(user);

		MethodSecurityContext methodSecurityContext = new MethodSecurityContext();
		methodSecurityContext.setRequiredAllPrivileges(requiredAllPrivileges);
		methodSecurityContext.setRequiredAnyPrivileges(requiredAnyOfPrivileges);

		result = authorizationManager.checkAccess(null, methodSecurityContext);

		Assert.assertFalse(result);
	}

	@Test
	public void checkAccess_AnyOf1() {
		Boolean result;
		User user = new User();
		user.addPrivilege("IF.ADDFORM");
		user.addPrivilege("IF.INTELLIFIND");

		PrivilegeType[] requiredAllOfPrivileges = new PrivilegeType[1];
		requiredAllOfPrivileges[0] = PrivilegeType.INTELLIFIND;

		PrivilegeType[] requiredAnyOfPrivileges = new PrivilegeType[0];

		Mockito.when(membershipProvider.getUser("user1")).thenReturn(user);

		MethodSecurityContext methodSecurityContext = new MethodSecurityContext();
		methodSecurityContext.setRequiredAllPrivileges(requiredAllOfPrivileges);
		methodSecurityContext.setRequiredAnyPrivileges(requiredAnyOfPrivileges);

		result = authorizationManager.checkAccess("user1", methodSecurityContext);

		Assert.assertTrue(result);
	}

	@Test
	public void checkAccess_AnyOf2() {

		Boolean result;
		user = new User();
		user.addPrivilege("IF.ADDFORM");

		Mockito.when(membershipProvider.getUser("user3")).thenReturn(user);

		PrivilegeType[] requiredAnyOfPrivileges = new PrivilegeType[1];
		requiredAnyOfPrivileges[0] = PrivilegeType.INTELLIFIND;

		PrivilegeType[] requiredAllOfPrivileges = new PrivilegeType[1];
		requiredAllOfPrivileges[0] = PrivilegeType.NONE;

		MethodSecurityContext methodSecurityContext = new MethodSecurityContext();
		methodSecurityContext.setRequiredAllPrivileges(requiredAllOfPrivileges);
		methodSecurityContext.setRequiredAnyPrivileges(requiredAnyOfPrivileges);

		result = authorizationManager.checkAccess("user3", methodSecurityContext);

		Assert.assertFalse(result);
	}

	//checkChannelPermissions

	@Test
	public void checkChannelPermissionsTest() {
		Boolean result;

		user = new User();
		user.addPrivilege("IF.ADDFORM");

		ArrayList<Tenant> tenantList = new ArrayList<Tenant>();
		Tenant tenant = new Tenant().setDisplayName("Test");
		ArrayList<Channel> channelList = new ArrayList<Channel>();
		channelList.add(new Channel().setDisplayName("TestChannel").setId("TestChannel").setEmId(1));
		channelList.add(new Channel().setDisplayName("TestChannel2").setId("TestChannel2").setEmId(2));
		channelList.add(new Channel().setDisplayName("TestChannel3").setId("TestChannel3").setEmId(3));
		tenant.setChannels(channelList);
		tenantList.add(tenant);

		user.setTenantsList(tenantList);

		Mockito.when(membershipProvider.getUser("user2")).thenReturn(user);

		result = authorizationManager.checkChannelPermissions("user2", "TestChannel2");

		assertTrue(result);
	}

	@Test
	public void checkChannelPermissionsNoPermissionTest() {
		Boolean result;

		user = new User();
		user.addPrivilege("IF.ADDFORM");

		ArrayList<Tenant> tenantList = new ArrayList<Tenant>();
		Tenant tenant = new Tenant().setDisplayName("Test");
		ArrayList<Channel> channelList = new ArrayList<Channel>();
		channelList.add(new Channel().setDisplayName("TestChannel").setId("TestChannel").setEmId(1));
		channelList.add(new Channel().setDisplayName("TestChannel2").setId("TestChannel2").setEmId(2));
		channelList.add(new Channel().setDisplayName("TestChannel3").setId("TestChannel3").setEmId(3));
		tenant.setChannels(channelList);
		tenantList.add(tenant);

		user.setTenantsList(tenantList);

		Mockito.when(membershipProvider.getUser("user21")).thenReturn(user);

		result = authorizationManager.checkChannelPermissions("user21", "TestChannel24");

		assertFalse(result);
	}
}
