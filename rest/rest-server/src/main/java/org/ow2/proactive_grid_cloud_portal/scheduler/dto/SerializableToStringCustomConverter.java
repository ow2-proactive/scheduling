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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.dozer.DozerConverter;
import org.ow2.proactive.utils.ObjectByteConverter;


/**
 * @author ActiveEon Team
 * @since 16/06/2017
 */
public class SerializableToStringCustomConverter extends DozerConverter<Serializable, String> {

    private static final Logger logger = Logger.getLogger(SerializableToStringCustomConverter.class);

    public SerializableToStringCustomConverter() {
        super(Serializable.class, String.class);
    }

    @Override
    public String convertTo(Serializable source, String destination) {
        if (source == null) {
            return null;
        }
        try {
            return source.toString();
        } catch (Exception e) {
            logger.error("Error when converting result to json", e);
            return null;
        }
    }

    @Override
    public byte[] convertFrom(String source, Serializable destination) {
        return null;
    }
}
