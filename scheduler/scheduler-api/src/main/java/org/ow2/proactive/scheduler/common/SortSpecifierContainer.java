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
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Utility container to pass multiple parameters for tasks sorting.
 *
 * @author ActiveEon Team
 */
public final class SortSpecifierContainer implements Serializable {

    public static final SortSpecifierContainer EMPTY_CONTAINER = new SortSpecifierContainer();

    private final ArrayList<SortSpecifierItem> sortParameters;

    public SortSpecifierContainer() {
        sortParameters = new ArrayList<>();
    }

    public final class SortSpecifierItem implements Serializable {

        private final String field;

        private final String order;

        SortSpecifierItem(String field, String order) {
            this.field = field;
            this.order = order;
        }

        public SortSpecifierItem() {
            this.field = "NOTSET";
            this.order = "ASCENDING";
        }

        public String toString() {
            return field + "," + order;
        }

        public String getField() {
            return field;
        }

        public String getOrder() {
            return order;
        }
    }

    public SortSpecifierContainer(int size) {
        sortParameters = new ArrayList<>(size);
    }

    public SortSpecifierContainer(String values) {
        sortParameters = new ArrayList<>();
        if (values != null && "".compareTo(values) != 0) {
            for (String s : values.split(";")) {
                String[] sortParam = s.split(",");
                add(sortParam[0], sortParam[1]);
            }
        }

    }

    public void add(String field, String order) {
        sortParameters.add(new SortSpecifierItem(field, order));
    }

    public List<SortSpecifierItem> getSortParameters() {
        return sortParameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int paddedSize = sortParameters.size() - 1;
        for (int i = 0; i < sortParameters.size(); i++) {
            sb.append(sortParameters.get(i).toString());
            if (i < paddedSize) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

}
