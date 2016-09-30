package org.ow2.proactive.scheduler.permissions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class HandleOnlyMyJobsPermissionTest {

    @Test
    public void testHandleOnlyMyJobsPermissionFalse() {
        //on security.java.policy-server file > permission org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission "false";
        //In this case the user belonging to the group with the above configuration have permission (implies returns true) by default.
        HandleOnlyMyJobsPermission permissionDefinedAtJaasConfigurationLevel = new HandleOnlyMyJobsPermission(
            "false");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(false)),
                (true));

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(true)),
                (true));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionTrue() {
        //on security.java.policy-server file > permission org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission "true";
        //In this case the user belonging to the group with the above configuration have no permission (implies returns false)
        //unless the permission is given at runtime with new HandleOnlyMyJobsPermission(true).
        HandleOnlyMyJobsPermission permissionDefinedAtJaasConfigurationLevel = new HandleOnlyMyJobsPermission(
            "true");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(false)),
                (false));

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(true)),
                (true));

    }

}
