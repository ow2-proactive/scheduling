package org.ow2.proactive_grid_cloud_portal;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.hibernate.collection.PersistentMap;

@Provider

public class PersistentMapConverter extends XmlAdapter<Map,PersistentMap> {

@Override
public PersistentMap unmarshal(Map arg0) throws Exception {
  return null;
}

@Override
public HashMap marshal(PersistentMap arg0) throws Exception {
    // TODO Auto-generated method stub
    return new HashMap(arg0);

}
}
