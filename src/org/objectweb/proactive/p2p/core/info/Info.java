/*
 * Created on Jul 26, 2004
 *
*/
package org.objectweb.proactive.p2p.core.info;

import java.io.Serializable;

import org.objectweb.proactive.p2p.core.service.P2PService;

/**
 * @author vcave
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Info implements Serializable
{
    
    private P2PService service = null;
    private int load = 0;
    private long lastUpdate = 0;
    private String key = null;
    
    public Info()
    {
        
    }
    
    public Info(int load, long lastUpdate, String key)
    {
        this.load = load;
        this.lastUpdate = lastUpdate;
        this.key = key;
    }
  
    /**
     * @return Returns the key.
     */
    public String getKey()
    {
        return key;
    }
    /**
     * @param key The key to set.
     */
    public void setKey(String key)
    {
        this.key = key;
    }
    /**
     * @return Returns the lastUpdate.
     */
    public long getLastUpdate()
    {
        return lastUpdate;
    }
    /**
     * @param lastUpdate The lastUpdate to set.
     */
    public void setLastUpdate(long lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }
    /**
     * @return Returns the free load.
     */
    public int getFreeLoad()
    {
        return P2PService.MAX_LOAD - load;
    }

    /**
     * @param load The free load to set.
     */
    public void setFreeLoad(int freeload)
    {
            this.load = P2PService.MAX_LOAD - freeload;
    }
    
    /**
     * @param load The load to set.
     */
    public void setLoad(int load)
    {
        this.load = load;
    }
    
    /**
     * @return Returns the service.
     */
    public P2PService getService()
    {
        return service;
    }
    /**
     * @param service The service to set.
     */
    public void setService(P2PService service)
    {
        this.service = service;
    }
}
