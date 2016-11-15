package org.ow2.proactive.scheduler.task.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map managing variables for scripts use
 * 
 * @author ActiveEon Team
 *
 */
public class VariablesMap implements Map<String, Serializable>, Serializable{

    /**
     * Map containing propagated variables (job and system variables)
     */
    private Map<String, Serializable> inheritedMap;

    /**
     * Map containing task variables, accessible from task scope
     */
    private Map<String, Serializable> scopeMap;

    /**
     * Map containing variables overriden into scripts
     */
    private Map<String, Serializable> scriptMap;
    
    /**
     * Constructor
     */
    public VariablesMap(){
        this.scriptMap = new HashMap<>();
        inheritedMap = new HashMap<>();
        scopeMap = new HashMap<>();
    }

    /**
     * Getter of the inheritedMap
     * @return the inheritedMap
     */
    public Map<String, Serializable> getInheritedMap() {
        return inheritedMap;
    }

    /**
     * Setter of the inheritedMap
     * @param inheritedMap the new inheritedMap
     */
    public void setInheritedMap(Map<String, Serializable> inheritedMap) {
        this.inheritedMap = inheritedMap;
    }

    /**
     * Getter of the scopeMap
     * @return the scopeMap
     */
    public Map<String, Serializable> getScopeMap() {
        return scopeMap;
    }

    /**
     * Setter of the scopeMap
     * @param scopeMap the new scopeMap
     */
    public void setScopeMap(Map<String, Serializable> scopeMap) {
        this.scopeMap = scopeMap;
    }

    /**
     * Getter of the scriptMap
     * @return the scriptMap
     */
    public Map<String, Serializable> getScriptMap() {
        return scriptMap;
    }
    
    /**
     * Getter of the propagated variables: only inheritedMap and scriptMap variables are propagated.
     * scriptMap variables override inheritedMap variables
     * @return the variables which will be propagated
     */
    public Map<String, Serializable> getPropagatedVariables(){
        Map<String, Serializable> variables = new HashMap<>(inheritedMap);
        variables.putAll(scriptMap);
        return variables;
    }
    
    /**
     * Getter of the merged variables map: scopeMap variables override inheritedMap variables and scriptMap variables override scopeMap variables
     * @return the merged variables map
     */
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
