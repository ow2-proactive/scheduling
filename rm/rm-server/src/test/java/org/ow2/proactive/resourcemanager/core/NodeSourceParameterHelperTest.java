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
package org.ow2.proactive.resourcemanager.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;

import com.google.common.collect.Lists;


@RunWith(MockitoJUnitRunner.class)
public class NodeSourceParameterHelperTest {

    public static final String APPLIED_CHANGE_VALUE = "changeApplied";

    public static final String ATTEMPTED_CHANGE_VALUE = "changeNotApplied";

    public static final String TO_BE_CHANGED_VALUE = "toBeChanged";

    public static final String TO_BE_KEPT_VALUE = "toBeKept";

    public static final String TO_BE_KEPT_WITHOUT_TRIM_VALUE = "toBeKept\n";

    @Mock
    private ConfigurableField dynamicField;

    @Mock
    private Configurable dynamicMeta;

    @Mock
    private ConfigurableField notDynamicField;

    @Mock
    private Configurable notDynamicMeta;

    private NodeSourceParameterHelper nodeSourceParameterHelper;

    @Before
    public void setup() {
        this.nodeSourceParameterHelper = new NodeSourceParameterHelper();

        when(this.dynamicField.getMeta()).thenReturn(this.dynamicMeta);
        when(this.dynamicMeta.dynamic()).thenReturn(true);
        when(this.notDynamicField.getMeta()).thenReturn(this.notDynamicMeta);
        when(this.notDynamicMeta.dynamic()).thenReturn(false);
    }

    @Test
    public void emptyUpdate() {

        List<Serializable> mergedParameters = this.nodeSourceParameterHelper.getParametersWithDynamicParametersUpdatedOnly(Collections.emptyList(),
                                                                                                                           new Object[] {},
                                                                                                                           Collections.emptyList());
        assertThat(mergedParameters).hasSize(0);
    }

    @Test
    public void testDynamicParameterUpdated() {

        List<ConfigurableField> configurableFields = Lists.asList(this.dynamicField, new ConfigurableField[] {});
        Object[] newParameters = { APPLIED_CHANGE_VALUE };
        List<Serializable> oldParameters = Lists.asList(TO_BE_CHANGED_VALUE, new Serializable[] {});

        List<Serializable> mergedParameters = this.nodeSourceParameterHelper.getParametersWithDynamicParametersUpdatedOnly(configurableFields,
                                                                                                                           newParameters,
                                                                                                                           oldParameters);
        assertThat(mergedParameters).hasSize(1);
        assertThat(mergedParameters.get(0)).isEqualTo(APPLIED_CHANGE_VALUE);
    }

    @Test
    public void testNotDynamicParameterNotUpdated() {

        List<ConfigurableField> configurableFields = Lists.asList(this.notDynamicField, new ConfigurableField[] {});
        Object[] newParameters = { ATTEMPTED_CHANGE_VALUE };
        List<Serializable> oldParameters = Lists.asList(TO_BE_KEPT_VALUE, new Serializable[] {});

        List<Serializable> mergedParameters = this.nodeSourceParameterHelper.getParametersWithDynamicParametersUpdatedOnly(configurableFields,
                                                                                                                           newParameters,
                                                                                                                           oldParameters);
        assertThat(mergedParameters).hasSize(1);
        assertThat(mergedParameters.get(0)).isEqualTo(TO_BE_KEPT_VALUE);
    }

    @Test
    public void testDynamicParametersAndNotDynamicParametersAreMixed() {

        List<ConfigurableField> configurableFields = Lists.asList(this.dynamicField,
                                                                  this.notDynamicField,
                                                                  new ConfigurableField[] {});
        Object[] newParameters = { APPLIED_CHANGE_VALUE, ATTEMPTED_CHANGE_VALUE };
        List<Serializable> oldParameters = Lists.asList(TO_BE_CHANGED_VALUE, TO_BE_KEPT_VALUE, new Serializable[] {});

        List<Serializable> mergedParameters = this.nodeSourceParameterHelper.getParametersWithDynamicParametersUpdatedOnly(configurableFields,
                                                                                                                           newParameters,
                                                                                                                           oldParameters);
        assertThat(mergedParameters).hasSize(2);
        assertThat(mergedParameters.get(0)).isEqualTo(APPLIED_CHANGE_VALUE);
        assertThat(mergedParameters.get(1)).isEqualTo(TO_BE_KEPT_VALUE);
    }

    @Test
    public void testDynamicParameterWithoutTrimInNewNotUpdated() {

        List<ConfigurableField> configurableFields = Lists.asList(this.dynamicField, new ConfigurableField[] {});
        Object[] newParameters = { TO_BE_KEPT_WITHOUT_TRIM_VALUE };
        List<Serializable> oldParameters = Lists.asList(TO_BE_KEPT_VALUE, new Serializable[] {});

        List<Serializable> mergedParameters = this.nodeSourceParameterHelper.getParametersWithDynamicParametersUpdatedOnly(configurableFields,
                                                                                                                           newParameters,
                                                                                                                           oldParameters);
        assertThat(mergedParameters).hasSize(1);
        assertThat(mergedParameters.get(0)).isEqualTo(TO_BE_KEPT_VALUE);
    }

    @Test
    public void testDynamicParameterWithoutTrimInOldNotUpdated() {

        List<ConfigurableField> configurableFields = Lists.asList(this.dynamicField, new ConfigurableField[] {});
        Object[] newParameters = { TO_BE_KEPT_VALUE };
        List<Serializable> oldParameters = Lists.asList(TO_BE_KEPT_WITHOUT_TRIM_VALUE, new Serializable[] {});

        List<Serializable> mergedParameters = this.nodeSourceParameterHelper.getParametersWithDynamicParametersUpdatedOnly(configurableFields,
                                                                                                                           newParameters,
                                                                                                                           oldParameters);
        assertThat(mergedParameters).hasSize(1);
        assertThat(mergedParameters.get(0)).isEqualTo(TO_BE_KEPT_WITHOUT_TRIM_VALUE);
    }

}
