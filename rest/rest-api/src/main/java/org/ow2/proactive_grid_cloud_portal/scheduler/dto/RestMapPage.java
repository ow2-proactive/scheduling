package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RestMapPage<K extends Serializable, V extends Serializable> {

    private int size;

    private HashMap<K,V> map;

    public RestMapPage() {

    }

    public RestMapPage(Map<K,V> map, int size) {
        this.map = new HashMap<K,V>(map);
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public Map<K,V> getMap() {
        return map;
    }

    public void setMap(Map<K,V> map) {
        this.map = new HashMap<K,V>(map);
    }

    @Override
    public String toString() {
        return "RestPage{" + "size=" + size + ", map='" + map + '\'' + '}';
    }

}
