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
package org.ow2.proactive.scheduler.job;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


/**
 * Represents an external endpoint attached with a job
 * @author ActiveEon Team
 * @since 12/23/2021
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExternalEndpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String url;

    private String iconUri;

    public ExternalEndpoint() {
    }

    public ExternalEndpoint(String name, String url, String iconUri) {
        this.name = name;
        this.url = url;
        this.iconUri = iconUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconUri() {
        return iconUri;
    }

    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ExternalEndpoint that = (ExternalEndpoint) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;
        return iconUri != null ? iconUri.equals(that.iconUri) : that.iconUri == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (iconUri != null ? iconUri.hashCode() : 0);
        return result;
    }
}
