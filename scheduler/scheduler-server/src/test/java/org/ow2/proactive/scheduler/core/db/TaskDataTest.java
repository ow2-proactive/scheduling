package org.ow2.proactive.scheduler.core.db;

import com.google.common.truth.Truth;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TaskDataTest {

    @Test
    public void testThatGetInErrorTimeReturnsMinusOneByDefault() throws Exception {
        assertThat(new TaskData().getInErrorTime()).isEqualTo(-1);
    }

}