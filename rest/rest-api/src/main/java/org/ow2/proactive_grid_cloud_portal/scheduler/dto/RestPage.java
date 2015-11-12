package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RestPage<T extends Serializable> {

    private int size;

    private ArrayList<T> list;

    public RestPage() {

    }

    public RestPage(List<T> tasks, int size) {
        this.list = new ArrayList<T>(tasks);
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public List<T> getList() {
        return list;
    }

    public void setTasks(List<T> tasks) {
        this.list = new ArrayList<T>(tasks);
    }

    @Override
    public String toString() {
        return "RestPage{" + "size=" + size + ", tasks='" + list + '\'' + '}';
    }

}
