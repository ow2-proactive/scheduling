package org.objectweb.proactive.ext.scilab;

public class SciEventSource {
	   
    protected javax.swing.event.EventListenerList listListener =
        new javax.swing.event.EventListenerList();

    
    public void addSciEventListener(SciEventListener listener) {
        listListener.add(SciEventListener.class, listener);
    }

    
    public void removeSciEventListener(SciEventListener listener) {
        listListener.remove(SciEventListener.class, listener);
    }

    void fireSciEvent(SciEvent evt) {
        Object[] listeners = listListener.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==SciEventListener.class) {
                ((SciEventListener)listeners[i+1]).actionPerformed(evt);
            }
        }
    }
}
