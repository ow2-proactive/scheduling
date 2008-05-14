package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;

import org.objectweb.proactive.core.remoteobject.adapter.Adapter;

public class RemoteObjectProperties implements Serializable {

	String className;
	Class<?> targetClass;
	String proxyName;
	Class<?> adapterClass;
	Adapter<?> adapter;

	public RemoteObjectProperties() {}

	public RemoteObjectProperties(String className,Class<?> targetClass,String proxyName,Class<?> adapterClass,	Adapter<?>  adapter) {
		this.className = className;
		this.targetClass = targetClass;
		this.proxyName = proxyName;
		this.adapterClass = adapterClass;
		this.adapter = adapter;
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



}
