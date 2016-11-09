package org.ow2.proactive.scheduler.task.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VariablesMap implements Map<String, Serializable>, Serializable{

    private Map<String, Serializable> inheritedMap;

    private Map<String, Serializable> scopeMap;

    private Map<String, Serializable> scriptMap;
    
    public VariablesMap(){
        this.scriptMap = new HashMap<>();
        inheritedMap = new HashMap<>();
        scopeMap = new HashMap<>();
    }

    public Map<String, Serializable> getInheritedMap() {
        return inheritedMap;
    }

    public void setInheritedMap(Map<String, Serializable> inheritedMap) {
        this.inheritedMap = inheritedMap;
    }

    public Map<String, Serializable> getScopeMap() {
        return scopeMap;
    }

    public void setScopeMap(Map<String, Serializable> scopeMap) {
        this.scopeMap = scopeMap;
    }

    public Map<String, Serializable> getScriptMap() {
        return scriptMap;
    }
    
    public Map<String, Serializable> getPropagatedVariables(){
        Map<String, Serializable> variables = new HashMap<>(inheritedMap);
        variables.putAll(scriptMap);
        return variables;
    }
    
    public Map<String, Serializable> getMergedMap(){
        Map<String, Serializable> variables = new HashMap<>(inheritedMap);
        variables.putAll(scopeMap);
        variables.putAll(scriptMap);
        return variables;
    }

    @Override
    public Serializable put(String key, Serializable value){
        return scriptMap.put(key, value);
    }

    @Override
    public int size() {
        return getMergedMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getMergedMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getMergedMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getMergedMap().containsValue(value);
    }

    @Override
    public Serializable get(Object key) {
        return getMergedMap().get(key);
    }

    @Override
    public Serializable remove(Object key) {
        Serializable scriptValue = scriptMap.remove(key);
        Serializable scopeValue = scopeMap.remove(key);
        Serializable inheritedValue = inheritedMap.remove(key);
        
        if (scriptValue != null)
            return scriptValue;
        if (scopeValue != null)
            return scopeValue;
        return inheritedValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Serializable> m) {
        scriptMap.putAll(m);
    }

    @Override
    public void clear() {
        scriptMap.clear();
        scopeMap.clear();
        inheritedMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return getMergedMap().keySet();
    }

    @Override
    public Collection<Serializable> values() {
        return getMergedMap().values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Serializable>> entrySet() {
        return getMergedMap().entrySet();
    }
    
}
