package org.ow2.proactive.scheduler.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ow2.proactive.db.SortParameter;

/**
 * Default values for the embedded criterias are the following:
 * <ul>
 *     <li>no tag filtering</li>
 *     <li>no dates filtering</li>
 *     <li>no pagination</li>
 *     <li>all status are selected</li>
 *     <li>no sort parameter</li>
 * </ul>
 */
public class TaskFilterCriteria implements Serializable {

    private String tag = null;
    private long from = 0;
    private long to = 0;
    private int offset = 0;
    private int limit = 0;
    private String user = null;
    private boolean running = true;
    private boolean pending = true;
    private boolean finished = true;
    private ArrayList<SortParameter<TaskSortParameter>> sortParameters = null;
    
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<SortParameter<TaskSortParameter>> getSortParameters() {
        if (sortParameters != null)
            return Collections.unmodifiableList(sortParameters);
        else
            return null;
    }

    public void setSortParameters(List<SortParameter<TaskSortParameter>> sortParameters) {
        if (sortParameters != null)
            this.sortParameters = new ArrayList<SortParameter<TaskSortParameter>>(sortParameters);
    }

    public static class TFCBuilder {
        
        private TaskFilterCriteria criterias = null;
        
        private TFCBuilder() {
            criterias = new TaskFilterCriteria();
        }
        
        public static TFCBuilder newInstance() {
            return new TFCBuilder();
        }
        
        /**
         * Default value is <code>null</code> (no task tag filtering)
         */
        public TFCBuilder tag(String tag) {
            criterias.setTag(tag);
            return this;
        }
        
        /**
         * Default value is <code>0L</code> (no date filtering)
         */
        public TFCBuilder from(long from) {
            criterias.setFrom(from);
            return this;
        }
        
        /**
         * Default value is <code>0L</code> (no date filtering)
         */
        public TFCBuilder to(long to) {
            criterias.setTo(to);
            return this;
        }
        
        /**
         * Default value is <code>0</code> (no pagination)
         */
        public TFCBuilder offset(int offset) {
            criterias.setOffset(offset);
            return this;
        }
        
        /**
         * Default value is <code>0</code> (no pagination)
         */
        public TFCBuilder limit(int limit) {
            criterias.setLimit(limit);
            return this;
        }

        /**
         * Default value is <code>true</code> (fetch all tasks)
         */
        public TFCBuilder running(boolean running) {
            criterias.setRunning(running);
            return this;
        }

        /**
         * Default value is <code>true</code> (fetch all tasks)
         */
        public TFCBuilder pending(boolean pending) {
            criterias.setPending(pending);
            return this;
        }

        /**
         * Default value is <code>true</code> (fetch all tasks)
         */
        public TFCBuilder finished(boolean finished) {
            criterias.setFinished(finished);
            return this;
        }

        /**
         * Default value is <code>null</code> (no user specific filtering)
         */
        public TFCBuilder user(String user) {
            criterias.setUser(user);
            return this;
        }

        /**
         * Default value is <code>null</code> (no sort parameters)
         */
        public TFCBuilder sortParameters(List<SortParameter<TaskSortParameter>> sortParameters) {
            criterias.setSortParameters(sortParameters);
            return this;
        }

        public TaskFilterCriteria criterias() {
            return criterias;
        }
        
    }
    
}
