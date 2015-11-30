package org.ow2.proactive.scheduler.common.util;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Unit tests related to {@link Pagination}.
 *
 * @author ActiveEon Team
 */
public class PaginationTest {

    @Test
    public void testGetBoundariesZeroZeroTenOneThousand() {
        PageBoundaries boundaries =
               Pagination.getPageBoundaries(0, 0, 10, 1000);

        assertOffsetAndLimit(boundaries, 0, 10);
    }

    @Test
    public void testGetBoundariesMinusOneMinusOneTenOneThousand() {
        PageBoundaries boundaries =
                Pagination.getPageBoundaries(-1, -1, 10, 1000);

        assertOffsetAndLimit(boundaries, 0, 10);
    }

    @Test
    public void testGetBoundariesFiveFiveTenOneThousand() {
        PageBoundaries boundaries =
                Pagination.getPageBoundaries(5, 5, 10, 1000);

        assertOffsetAndLimit(boundaries, 5, 5);
    }

    @Test
    public void testGetBoundariesTenFiveTenOneThousand() {
        PageBoundaries boundaries =
                Pagination.getPageBoundaries(10, 5, 10, 1000);

        assertOffsetAndLimit(boundaries, 10, 5);
    }

    @Test
    public void testGetBoundariesFourtyTwoIntegerMaxValueTenOneThousand() {
        PageBoundaries boundaries =
                Pagination.getPageBoundaries(42, Integer.MAX_VALUE, 10, 1000);

        assertOffsetAndLimit(boundaries, 42, 1000);
    }

    public void assertOffsetAndLimit(PageBoundaries pageBoundaries, int expectedOffset, int expectedLimit) {
        assertThat(pageBoundaries.getOffset()).isEqualTo(expectedOffset);
        assertThat(pageBoundaries.getLimit()).isEqualTo(expectedLimit);
    }

}