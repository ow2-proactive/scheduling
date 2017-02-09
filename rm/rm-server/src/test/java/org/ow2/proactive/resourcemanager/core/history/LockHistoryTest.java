package org.ow2.proactive.resourcemanager.core.history;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by lpellegr on 09/02/17.
 */
public class LockHistoryTest {

    private LockHistory lockHistory;

    @Before
    public void setUp() {
        lockHistory = new LockHistory("nodeSource", 42);
    }

    @Test
    public void testGetNodeSource() throws Exception {
        assertThat(lockHistory.getNodeSource()).isEqualTo("nodeSource");
    }

    @Test
    public void testGetLockCount() throws Exception {
        assertThat(lockHistory.getLockCount()).isEqualTo(42);
    }

    @Test
    public void testDecrementLockCount() throws Exception {
        assertThat(lockHistory.getLockCount()).isEqualTo(42);
        lockHistory.decrementLockCount();
        assertThat(lockHistory.getLockCount()).isEqualTo(41);
    }

    @Test
    public void testIncrementLockCount() throws Exception {
        assertThat(lockHistory.getLockCount()).isEqualTo(42);
        lockHistory.incrementLockCount();
        assertThat(lockHistory.getLockCount()).isEqualTo(43);
    }

}