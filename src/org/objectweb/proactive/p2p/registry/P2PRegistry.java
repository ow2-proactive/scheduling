/*
 * Created on Jan 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.p2p.registry;

import org.objectweb.proactive.core.ProActiveException;


/**
 * @author jbustos
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface P2PRegistry {
    public void register(String key, Object o) throws ProActiveException;

    public void unregister(String key) throws ProActiveException;

    public void registerOverloaded(String key, Object o)
        throws ProActiveException;

    public void unregisterOverloaded(String key, Object o)
        throws ProActiveException;

    public void registerUnderloaded(String key, Object o)
        throws ProActiveException;

    public void unregisterUnderloaded(String key, Object o)
        throws ProActiveException;

    public Object getUnderloaded(String key) throws ProActiveException;

    public int getNumberOfAvailables() throws ProActiveException;

    public Object[] getAvailables(int n) throws ProActiveException;

    public Object[] getFullyAvailables(int n) throws ProActiveException;
}
