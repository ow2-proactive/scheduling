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
package org.ow2.proactive.scheduler.util;

import java.util.List;
import java.util.Properties;

import org.ow2.proactive.addons.email.EmailSender;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class SendMail {

    /**
     * Throws EmailException whenever configuration is wrong
     *
     * @param to recipient
     * @param subject email subject
     * @param body email body
     */
    public void sender(String to, String subject, String body) {
        final Properties properties = EmailConfiguration.getConfiguration().getProperties();

        EmailSender.Builder builder = new EmailSender.Builder(properties);
        builder.setFrom(PASchedulerProperties.EMAIL_NOTIFICATIONS_SENDER_ADDRESS.getValueAsString());
        builder.addRecipient(to);
        builder.setSubject(subject);
        builder.setBody(body);
        builder.build().sendPlainTextEmail();
    }

    public void sender(List<String> to, String subject, String body) {
        final Properties properties = EmailConfiguration.getConfiguration().getProperties();

        EmailSender.Builder builder = new EmailSender.Builder(properties);
        builder.setFrom(PASchedulerProperties.EMAIL_NOTIFICATIONS_SENDER_ADDRESS.getValueAsString());
        to.forEach(address -> builder.addRecipient(address));
        builder.setSubject(subject);
        builder.setBody(body);
        builder.build().sendPlainTextEmail();
    }

    public void sender(List<String> to, String subject, String body, String fileToAttach, String fileName) {
        final Properties properties = EmailConfiguration.getConfiguration().getProperties();

        EmailSender.Builder builder = new EmailSender.Builder(properties);
        builder.setFrom(PASchedulerProperties.EMAIL_NOTIFICATIONS_SENDER_ADDRESS.getValueAsString());
        to.forEach(address -> builder.addRecipient(address));
        builder.setSubject(subject);
        builder.setBody(body);
        builder.setAttachmentPath(fileToAttach);
        builder.setAttachmentName(fileName);
        builder.build().sendPlainTextEmailWithAttachment();
    }
}
