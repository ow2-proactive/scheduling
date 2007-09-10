package org.objectweb.proactive.extra.gcmdeployment.process.hostinfo;

import java.io.Serializable;


public class Tool implements Serializable {
    private String id;
    private String path;

    public Tool(String id, String path) {
        this.id = id;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    protected void setId(String id) {
        this.id = id;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tool other = (Tool) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tool<" + super.toString() + "> ");
        sb.append(" id=" + id);
        sb.append(" path=" + path);
        return super.toString();
    }
}
