package org.ow2.proactive.scheduler.permissions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class HandleOnlyMyJobsPermissionTest {

    @Test
    public void testHandleOnlyMyJobsPermissionFalse() {
        //permission org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission "false";
        //In this case the user belonging to the group with the above configuration have permission (implies returns true) by default.
        HandleOnlyMyJobsPermission permissionDefinedAtJaasConfigurationLevel = new HandleOnlyMyJobsPermission(
            "false");

        assertThat(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(false)),
                is(true));

        assertThat(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(true)),
                is(true));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionTrue() {
        //permission org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission "true";
        //In this case the user belonging to the group with the above configuration have no permission (implies returns false)
        //unless the permission is given at runtime with new HandleOnlyMyJobsPermission(true).
        HandleOnlyMyJobsPermission permissionDefinedAtJaasConfigurationLevel = new HandleOnlyMyJobsPermission(
            "true");

        assertThat(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(false)),
                is(false));

        assertThat(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(true)),
                is(true));

    }

}
