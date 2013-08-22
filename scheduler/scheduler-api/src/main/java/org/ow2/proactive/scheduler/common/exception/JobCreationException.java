/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.exception;

import java.util.Stack;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.factories.XMLAttributes;
import org.ow2.proactive.scheduler.common.job.factories.XMLTags;


/**
 * Exceptions Generated if a problem occurred while creating a job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class JobCreationException extends SchedulerException {

    private String taskName = null;
    private Stack<XMLTags> tags = new Stack<XMLTags>();
    private XMLAttributes attribute = null;
    private boolean isSchemaException = false;

    /**
     * Create a new instance of JobCreationException using the given message string
     *
     * @param message the reason of the exception
     */
    public JobCreationException(String message) {
        super(message);
    }

    /**
     * Create a new instance of JobCreationException using the given message string
     *
     * @param message the reason of the exception
     */
    public JobCreationException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    /**
     * Create a new instance of JobCreationException using the given message string and cause
     *
     * @param message the reason of the exception
     * @param cause the cause of this exception
     */
    public JobCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new instance of JobCreationException and tell if it was a schema exception
     *
     * @param schemaException true if this exception is due to a Schema exception, false otherwise.
     * @param cause the cause of this exception
     */
    public JobCreationException(boolean schemaException, Throwable cause) {
        super(cause.getMessage(), cause);
        this.isSchemaException = schemaException;
    }

    /**
     * Create a new instance of JobCreationException using the given tag, attribute and cause.
     * Tag and attribute can be null.
     *
     * @param tag the XML tag name where the exception is thrown
     * @param attribute the XML attribute name where the exception is thrown
     * @param cause the cause of this exception
     */
    public JobCreationException(String tag, String attribute, Exception cause) {
        this(tag == null ? null : XMLTags.getFromXMLName(tag), attribute, cause);
    }

    /**
     * Create a new instance of JobCreationException using the given tag, attribute and cause.
     * Tag and attribute can be null.
     *
     * @param tag the XML tag where the exception is thrown
     * @param attribute the XML attribute name where the exception is thrown
     * @param cause the cause of this exception
     */
    public JobCreationException(XMLTags tag, String attribute, Exception cause) {
        super(cause.getMessage(), cause);
        if (tag != null) {
            this.tags.push(tag);
        }
        if (attribute != null) {
            this.attribute = XMLAttributes.getFromXMLName(attribute);
        }
    }

    /**
     * Set the task name on which the problem was found
     *
     * @param taskName the name of the task that generate the problem
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Push a new tag on the stack of tag for this exception
     *
     * @param currentTag the tag name to stack.
     */
    public void pushTag(String currentTag) {
        this.pushTag(XMLTags.getFromXMLName(currentTag));
    }

    /**
     * Push a new tag on the stack of tag for this exception
     *
     * @param currentTag the tag to stack.
     */
    public void pushTag(XMLTags currentTag) {
        this.tags.push(currentTag);
    }

    /**
     * Return a stack that contains every tags (path) to the element that causes the exception.
     * The first element is always the 'job' tag.
     *
     * @return a stack that contains every tags (path) to the element that causes the exception.
     */
    public Stack<XMLTags> getXMLTagsStack() {
        return this.tags;
    }

    /**
     * Return the detail message string of this exception.
     * This message contains the task name, tag hierarchy, and attribute that generate the exception and
     * then the short message associate to this exception.
     *
     * @return the detail message string of this exception.
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (taskName != null) {
            sb.append("[task=" + taskName + "] ");
        }
        if (tags != null && tags.size() > 0) {
            sb.append("[tag:" + tags.elementAt(tags.size() - 1));
            for (int i = tags.size() - 2; i >= 0; i--) {
                sb.append("/" + tags.elementAt(i));
            }
            sb.append("] ");
        }
        if (attribute != null) {
            sb.append("[attribute:" + attribute + "] ");
        }
        if (sb.length() > 0) {
            sb = new StringBuilder("At " + sb.toString() + ": ");
        }
        sb.append(getShortMessage());
        return sb.toString();
    }

    /**
     * Return only the message that is the cause of the exception.
     *
     * @return the message that is the cause of the exception.
     */
    public String getShortMessage() {
        return super.getMessage();
    }

    /**
     * Get the isSchemaException
     *
     * @return the isSchemaException
     */
    public boolean isSchemaException() {
        return isSchemaException;
    }

    /**
     * Get the taskName where the exception has been thrown.
     *
     * @return the taskName where the exception has been thrown, null if no taskName was set (ie: exception
     * 			was thrown in job header.)
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Get the attribute where the exception has been thrown.
     *
     * @return the attribute where the exception has been thrown, null if no attribute was set (ie: exception
     * 			was thrown in a tag that does not contains an attribute.)
     */
    public XMLAttributes getAttribute() {
        return attribute;
    }
}
