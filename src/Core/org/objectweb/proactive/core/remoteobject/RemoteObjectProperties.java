package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.remoteobject.adapter.Adapter;


public class RemoteObjectProperties implements Serializable {

    protected String className;
    protected Class<?> targetClass;
    protected String proxyName;
    protected Class<?> adapterClass;
    protected Adapter<?> adapter;
    protected URI uri;
    protected Object stub;

    public RemoteObjectProperties() {
    }

    public RemoteObjectProperties(Object stub, URI uri, String className, Class<?> targetClass,
            String proxyName, Class<?> adapterClass, Adapter<?> adapter) {
        this.className = className;
        this.targetClass = targetClass;
        this.proxyName = proxyName;
        this.adapterClass = adapterClass;
        this.adapter = adapter;
        this.uri = uri;
        this.stub = stub;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public Class<?> getAdapterClass() {
        return adapterClass;
    }

    public void setAdapterClass(Class<?> adapterClass) {
        this.adapterClass = adapterClass;
    }

    public Adapter<?> getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter<?> adapter) {
        this.adapter = adapter;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Object getStub() {
        return stub;
    }

    public void setStub(Object stub) {
        this.stub = stub;
    }

}
