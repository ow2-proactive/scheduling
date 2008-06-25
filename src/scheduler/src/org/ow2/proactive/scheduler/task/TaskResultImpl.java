/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.scheduler.task;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JPanel;

import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.task.ResultPreview;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;


/**
 * Class representing the task result.
 * A task result can be an exception or an object that you have to cast into your own type.
 * Before getting the object it is recommended that you call the hadException() method.
 * It will tell you if an exception occurred in the task that generate this result.
 *
 * @author The ProActive Team
 * @version 3.9, Aug 3, 2007
 * @since ProActive 3.9
 */
public class TaskResultImpl implements TaskResult {

    /** The task identification of the result */
    private TaskId id = null;

    /** The value of the result if no exception occurred */
    public byte[] serializedValue = null;
    public transient Object value = null;

    /** The exception thrown by the task */
    public byte[] serializedException = null;
    private transient Throwable exception = null;

    /** Task output */
    public TaskLogs output = null;

    /** Description definition of this result */
    private String previewerClassName = null;
    private transient ResultPreview descriptor = null;
    // this classpath is used on client side to instanciate the previewer (can be null)
    private String[] jobClasspath;

    /** ProActive empty constructor. */
    public TaskResultImpl() {
    }

    /**
     * Return a new instance of task result represented by a task id and its result.
     *
     * @param id the identification of the task that send this result.
     * @param value the result of the task.
     */
    public TaskResultImpl(TaskId id, Object value, TaskLogs output) {
        this.id = id;
        this.value = value;
        try {
            this.serializedValue = ObjectToByteConverter.ObjectStream.convert(value);
        } catch (IOException e) {
            // TODO cdelbe : exception ?
            e.printStackTrace();
        }
        this.output = output;
    }

    /**
     * Return a new instance of task result represented by a task id and its exception.
     *
     * @param id the identification of the task that send this result.
     * @param exception the exception that occurred in the task.
     */
    public TaskResultImpl(TaskId id, Throwable exception, TaskLogs output) {
        this.id = id;
        this.exception = exception;
        try {
            this.serializedException = ObjectToByteConverter.ObjectStream.convert(exception);
        } catch (IOException e) {
            // TODO cdelbe : exception ?
            e.printStackTrace();
        }
        this.output = output;
    }

    /**
     * <font color='red'>** FOR INTERNAL USE ONLY **</font> - Must be called only by the scheduler to
     * improve memory usage. This method will removed the value and output of this result.
     */
    public void clean() {
        this.value = null;
        this.exception = null;
        this.output = null;
        this.serializedException = null;
        this.serializedValue = null;
    }

    /**
     * Return true if the result has been stored in database, false if not.
     * 
     * @return true if the result has been stored in database, false if not.
     */
    public boolean isInDataBase() {
        return value == null && exception == null && output == null && serializedValue == null &&
            serializedException == null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskResult#hadException()
     */
    public boolean hadException() {
        return serializedException != null;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskResult#getTaskId()
     */
    public TaskId getTaskId() {
        return id;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskResult#value()
     */
    public Object value() throws Throwable {
        if (hadException()) {
            try {
                throw this.instanciateException(null);
            } catch (IOException e) {
                throw new SchedulerException("Cannot instanciate exception thrown by the task " + this.id +
                    " : " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new SchedulerException("Cannot instanciate exception thrown by the task " + this.id +
                    " : " + e.getMessage());
            }
        } else {
            try {
                return this.instanciateValue(null);
            } catch (IOException e) {
                e.printStackTrace();
                throw new SchedulerException("Cannot instanciate result of the task " + this.id + " : " +
                    e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new SchedulerException("Cannot instanciate result of the task " + this.id + " : " +
                    e.getMessage());
            }
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskResult#getException()
     */
    public Throwable getException() {
        if (hadException()) {
            try {
                return this.instanciateException(null);
            } catch (IOException e) {
                return new SchedulerException("Cannot instanciate exception thrown by the task " + this.id +
                    " : " + e.getMessage());
            } catch (ClassNotFoundException e) {
                return new SchedulerException("Cannot instanciate exception thrown by the task " + this.id +
                    " : " + e.getMessage());
            }
        } else {
            return null;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskResult#getOutput()
     */
    public TaskLogs getOuput() {
        return this.output;
    }

    /**
     *  @see org.ow2.proactive.scheduler.common.task.TaskResult#setPreviewerClassName(String)
     */
    public void setPreviewerClassName(String descClass) {
        if (this.previewerClassName != null) {
            throw new RuntimeException("Descriptor class cannot be changed");
        } else {
            this.previewerClassName = descClass;
        }
    }

    /**
     *  @see org.ow2.proactive.scheduler.common.task.TaskResult#setJobClasspath(String)
     */
    public void setJobClasspath(String[] jcp) {
        this.jobClasspath = jcp;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskResult#getGraphicalDescription()
     */
    public JPanel getGraphicalDescription() {
        boolean instanciation = false;

        try {
            instanciation = this.instanciateDescriptor();
        } catch (InstantiationException e) {
            return new SimpleTextPanel("[SCHEDULER] Cannot create descriptor : " + e.getMessage());
        } catch (IllegalAccessException e) {
            return new SimpleTextPanel("[SCHEDULER] Cannot create descriptor : " + e.getMessage());
        }

        if (instanciation) {
            return this.descriptor.getGraphicalDescription(this);
        } else {
            return new SimpleTextPanel(this.getTextualDescription());
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskResult#getTextualDescription()
     */
    public String getTextualDescription() {
        boolean instanciation = false;

        try {
            instanciation = this.instanciateDescriptor();
        } catch (InstantiationException e) {
            return "[SCHEDULER] Cannot create descriptor : " + e.getMessage();
        } catch (IllegalAccessException e) {
            return "[SCHEDULER] Cannot create descriptor : " + e.getMessage();
        }

        if (instanciation) {
            return this.descriptor.getTextualDescription(this);
        } else if (!this.hadException()) {
            return "[DEFAULT DESCRIPTION] " + value;
        } else {
            // yes, Guillaume, I know...
            return "[DEFAULT DESCRIPTION] " + exception;
        }
    }

    /**
     * Create the descriptor instance if descriptor class is available.
     * This descriptor is instanciated in a dedicated URLClassloader build on
     * the job classpath.
     * @return true if the creation occurs, false otherwise
     */
    private boolean instanciateDescriptor() throws InstantiationException, IllegalAccessException {
        if (this.descriptor == null) {
            try {
                ClassLoader cl = null;
                boolean isInstanciated = false;
                if (this.jobClasspath != null) {
                    // load previewer in a classloader build over job classpath
                    URL[] urls = new URL[this.jobClasspath.length];
                    for (int i = 0; i < this.jobClasspath.length; i++) {
                        urls[i] = new File(this.jobClasspath[i]).toURL();
                    }
                    cl = new URLClassLoader(urls, this.getClass().getClassLoader());
                }
                // if a specific previewer is defined, instanciate it
                if (this.previewerClassName != null) {
                    Class<?> previewClass = Class.forName(this.previewerClassName, true, cl);
                    this.descriptor = (ResultPreview) (previewClass.newInstance());
                    isInstanciated = true;
                }
                // in any case, instanciate value and exception
                if (this.serializedException != null) {
                    this.exception = this.instanciateException(cl);
                } else {
                    this.value = this.instanciateValue(cl);
                }
                return isInstanciated;
            } catch (ClassNotFoundException e) {
                System.err.println("Cannot create ResultPreview : " + e.getMessage());
                e.printStackTrace();
                return false;
            } catch (MalformedURLException e) {
                System.err.println("Cannot create ResultPreview : " + e.getMessage());
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                System.err.println("Cannot create ResultPreview : " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Instanciate thrown exception if any from the serialized version.
     * This instanciation must not be performed in a dedicated classloader to
     * avoid ClassCastException with the user's code.
     * @param cl the classloader where to instanciate the object. Can be null : object is instanciated 
     * in the default caller classloader. 
     * @return the exception that has been thrown if any.
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    private Throwable instanciateException(ClassLoader cl) throws IOException, ClassNotFoundException {
        if (this.serializedException != null && this.exception == null) {
            this.exception = (Throwable) ByteToObjectConverter.ObjectStream.convert(this.serializedException,
                    cl);
        }
        return this.exception;
    }

    /**
     * Instanciate value if any from the serialized version.
     * This instanciation must not be performed in a dedicated classloader to
     * avoid ClassCastException with the user's code.
     * @param cl the classloader where to instanciate the object. Can be null : object is instanciated 
     * in the default caller classloader. 
     * @return the value if no exception has been thown.
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    private Object instanciateValue(ClassLoader cl) throws IOException, ClassNotFoundException {
        if (this.serializedValue != null && this.value == null) {
            this.value = ByteToObjectConverter.ObjectStream.convert(this.serializedValue, cl);
        }
        return this.value;
    }

}