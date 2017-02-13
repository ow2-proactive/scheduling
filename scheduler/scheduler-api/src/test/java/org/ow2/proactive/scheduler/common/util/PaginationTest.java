/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.util;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;


/**
 * Unit tests related to {@link Pagination}.
 *
 * @author ActiveEon Team
 */
public class PaginationTest {

    @Test
    public void testGetBoundariesZeroZeroTenOneThousand() {
        PageBoundaries boundaries = Pagination.getPageBoundaries(0, 0, 10, 1000);

        assertOffsetAndLimit(boundaries, 0, 10);
    }

    @Test
    public void testGetBoundariesMinusOneMinusOneTenOneThousand() {
        PageBoundaries boundaries = Pagination.getPageBoundaries(-1, -1, 10, 1000);

        assertOffsetAndLimit(boundaries, 0, 10);
    }

    @Test
    public void testGetBoundariesFiveFiveTenOneThousand() {
        PageBoundaries boundaries = Pagination.getPageBoundaries(5, 5, 10, 1000);

        assertOffsetAndLimit(boundaries, 5, 5);
    }

    @Test
    public void testGetBoundariesTenFiveTenOneThousand() {
        PageBoundaries boundaries = Pagination.getPageBoundaries(10, 5, 10, 1000);

        assertOffsetAndLimit(boundaries, 10, 5);
    }

    @Test
    public void testGetBoundariesFourtyTwoIntegerMaxValueTenOneThousand() {
        PageBoundaries boundaries = Pagination.getPageBoundaries(42, Integer.MAX_VALUE, 10, 1000);

        assertOffsetAndLimit(boundaries, 42, 1000);
    }

    public void assertOffsetAndLimit(PageBoundaries pageBoundaries, int expectedOffset, int expectedLimit) {
        assertThat(pageBoundaries.getOffset()).isEqualTo(expectedOffset);
        assertThat(pageBoundaries.getLimit()).isEqualTo(expectedLimit);
    }

}
