/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.db;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.MetaValue;
import org.hibernate.cfg.Configuration;
import org.ow2.proactive.db.DatabaseManager.FilteredExceptionCallback;
import org.ow2.proactive.db.DatabaseManagerExceptionHandler.DBMEHandler;
import org.ow2.proactive.db.annotation.Alterable;
import org.ow2.proactive.db.annotation.Unloadable;


/**
 * DatabaseManager is responsible of every database transaction.<br />
 * Hibernate entities can be managed by this manager. It provides method to register, delete, synchronize
 * objects in database.
 * Each method will work on the object and its inheritance.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public abstract class HibernateDatabaseManager implements DatabaseManager, FilteredExceptionCallback {

    //Alterable field name pattern for HQL request
    private static final String ALTERABLE_REQUEST_FIELD = "alterable";
    //ObjectID field name for HQL request
    private static final String OBJECTID_REQUEST_FIELD = "objectId";
    //locks
    private Object sessionlock;
    //Memory for id field name by class
    private Map<Class<?>, Field> idFields;
    //Hibernate Session factory
    private SessionFactory sessionFactory;
    //Hibernate configuration
    private Configuration configuration;
    //Next variable is used to start a session manually outside of this class.
    //It can be used to make more than one action in a single transaction.
    //this object will store transaction by thread.
    private ThreadLocal<Session> globalSession;
    //exception handler used to replace "throw new ... " when doing stuff that will connect to database
    //allow this class to handle every exception that can be due to database disconnection.
    protected DatabaseManagerExceptionHandler exceptionHandler;
    //callback object to be notified of exception filtered if needed, can be null
    protected FilteredExceptionCallback callback = null;

    /**
     * Create a new instance of HibernateDatabaseManager
     */
    @SuppressWarnings("unchecked")
    public HibernateDatabaseManager() {
        //Create configuration from hibernate.cfg.xml using XML file for mapping
        //configuration = new Configuration().configure(new File(configurationFile));
        //Create configuration from hibernate.cfg.xml using Hibernate annotation
        this.configuration = new Configuration().configure(new File(getConfigFile()));
        this.sessionlock = new Object();
        this.idFields = new HashMap<Class<?>, Field>();
        this.globalSession = new ThreadLocal<Session>();
        this.exceptionHandler = new DatabaseManagerExceptionHandler(
            new Class[] { org.hibernate.exception.JDBCConnectionException.class }, DBMEHandler.FILTER_ALL,
            this);
        this.sessionFactory = null;
    }

    /**
     * Get the session factory.
     *
     * @return the session factory.
     */
    protected SessionFactory getSessionFactory() {
        //build if not
        getLogger().debug("Request to build the Hibernate session Factory");
        build();
        return sessionFactory;
    }

    /**
     * Set user define property for the session factory.<br />
     * Properties that are set after building process won't be used. Set property before building the session factory.<br />
     * This method override the properties define in the 'hibernate.cfg.xml' file.
     *
     * @param propertyName the name of the property to set.
     * @param value the value of the property.
     */
    public void setProperty(String propertyName, String value) {
        if (sessionFactory != null) {
            getLogger().warn(
                    "WARNING : Property set while session factory have already been built ! (" +
                        propertyName + ")" + "\n\t -> Build the session factory after setting properties !");
        }
        configuration.setProperty(propertyName, value);
    }

    /**
     * Build the Hibernate session Factory.<br/>
     * Call this method when you want to build the Hibernate session factory. If not called,
     * it will be automatically build when needed.<br />
     * For performance reason, It is recommended to call this method during your application building process.<br /><br/>
     * It is also possible to set some properties before building the session.
     *
     * If you want to add personal behavior, just use the configuration associated to the Database Manager.
     * For example you may have to set up the DB with a special behavior on startup :<br />
     * Use the associated configuration to set the desired property to the desired value.<br />
     * See hibernate.cfg.xml for more details about the keys and values.
     */
    public void build() {
        try {
            if (sessionFactory == null) {
                // Build the SessionFactory
                getLogger().info(
                        "Building Hibernate session Factory (configuration File : " + getConfigFile() + " )");
                sessionFactory = configuration.buildSessionFactory();
                globalSession = new ThreadLocal<Session>();
            }
        } catch (Throwable ex) {
            getLogger().error("Initial SessionFactory creation failed.", ex);
            throw new DatabaseManagerException("Initial SessionFactory creation failed.", ex);
        }
    }

    /**
     * Close the hibernate session factory.
     */
    public void close() {
        try {
            getLogger().info("Closing current session");
            sessionFactory.getCurrentSession().close();
            getLogger().info("Closing session factory");
            sessionFactory.close();
        } catch (Exception e) {
            getLogger().error("Error while closing database", e);
        } finally {
            globalSession = null;
            sessionFactory = null;
        }
    }

    @Override
    public void runAsSingleTransaction(DatabaseCallback callback) {
        startTransaction();
        try {
            callback.workWithDatabase(this);
            commitTransaction();
        } catch (Throwable t) {
            getLogger().error("", t);
            rollbackTransaction();
        }
    }

    /**
     * Force a transaction to be started. This method has to be used only when multiple calls
     * to methods of this class have to be performed.<br />
     * For simple atomic call, transaction is implicit.<br /><br />
     *
     * To use the manual transaction, call this forceStartTransaction() method,<br/>
     * then, (when multiple modifications are done) a call to {@link #commitTransaction()} OR {@link #rollbackTransaction()}
     * will terminate the transaction.
     */
    public void startTransaction() {
        synchronized (sessionlock) {
            Session s = globalSession.get();
            if (s == null) {
                try {
                    s = getSessionFactory().openSession();
                    s.beginTransaction();
                    globalSession.set(s);
                } catch (Exception e) {
                    getLogger().error("Error while starting transaction", e);
                    this.exceptionHandler.handle("Error while starting transaction", e);
                }
            }
        }
    }

    /**
     * Force a manually opened transaction to be committed.
     * See {@link #startTransaction()} for details.
     */
    public void commitTransaction() {
        synchronized (sessionlock) {
            Session s = globalSession.get();
            if (s == null) {
                throw new RuntimeException("No current opened session to commit");
            }
            try {
                s.getTransaction().commit();
                s.close();
            } catch (Exception e) {
                getLogger().error("Error while committing transaction", e);
                this.exceptionHandler.handle("Error while committing transaction", e);
            } finally {
                globalSession.set(null);
            }
        }
    }

    /**
     * Force a manually opened transaction to be rolledback.
     * See {@link #startTransaction()} for details.
     */
    public void rollbackTransaction() {
        synchronized (sessionlock) {
            Session s = globalSession.get();
            if (s == null) {
                throw new RuntimeException("No current opened session to rollback");
            }
            try {
                s.getTransaction().rollback();
                s.close();
            } catch (Exception e) {
                getLogger().error("Error while rollbacking", e);
                this.exceptionHandler.handle("Error while rollbacking", e);
            } finally {
                globalSession.set(null);
            }
        }
    }

    /**
     * Close the given session
     *
     * @param session the session to be closed
     */
    protected void closeSession(Session session) {
        try {
            session.close();
        } catch (Exception e) {
            getLogger().error("Error while closing session", e);
            this.exceptionHandler.handle("Error while closing session", e);
        }
    }

    protected void closeSession(StatelessSession session) {
        try {
            session.close();
        } catch (Exception e) {
            getLogger().error("Error while closing session", e);
            this.exceptionHandler.handle("Error while closing session", e);
        }
    }

    /**
     * Begin a transaction
     * First open a session, the begin a transaction on this session.
     *
     * @return the new opened session
     */
    private Session beginTransaction() {
        Session s = globalSession.get();
        //if a session has been manually opened by a user, use it
        getLogger().debug("Open new session, global session is " + ((s == null) ? "null" : "set"));
        if (s != null) {
            return s;
        }
        //if no session is opened, open a new one dedicated to the current thread
        s = getSessionFactory().openSession();
        s.beginTransaction();
        return s;
    }

    /**
     * Commit and close the given session.
     *
     * @param session the session to be committed.
     */
    private void commitTransaction(Session session) {
        //if a session has been manually opened by a user, don't commit, wait for user to commit
        if (globalSession.get() != null) {
            return;
        }
        session.getTransaction().commit();
        session.close();
        getLogger().debug("Transaction committed and closed");
    }

    /**
     * Rollback and close the given session.
     * A call to this method is a safe operation (ie : no exception, even Runtime will be thrown)
     *
     * @param session the session to be rolledback.
     */
    private void rollbackTransaction(Session session) {
        //if a session has been manually opened by a user, don't rollback, wait for user to commit
        if (globalSession.get() != null) {
            return;
        }
        try {
            session.getTransaction().rollback();
            closeSession(session);
            getLogger().debug("Transaction rolledback and closed");
        } catch (Exception e) {
            getLogger().error("Error while rollback", e);
        }
    }

    /**
     * Register an object. The object must be an Hibernate entity.
     * This method will persist the given object and store it in the database.
     *
     * @param o the new object to store.
     */
    public void register(Object o) {
        checkIsEntity(o);
        Session session = beginTransaction();
        try {
            getLogger().debug("Registering new Object : " + o.getClass().getName());
            session.save(o);
            commitTransaction(session);
        } catch (Throwable e) {
            getLogger().error("", e);
            rollbackTransaction(session);
            this.exceptionHandler.handle("Unable to store the given object !", e);
        }
    }

    /**
     * Delete an object. The object must be an Hibernate entity.
     * This method will delete the given object and also every dependences.
     *
     * @param o the new object to delete.
     */
    public void delete(Object o) {
        checkIsEntity(o);
        Session session = beginTransaction();
        try {
            getLogger().info("Deleting Object : " + o.getClass().getName());
            session.delete(o);
            commitTransaction(session);
        } catch (Throwable e) {
            getLogger().error("", e);
            rollbackTransaction(session);
            this.exceptionHandler.handle("Unable to delete the given object !", e);
        }
    }

    /**
     * Update the given entity and every dependences.<br />
     * Just call the Hibernate update procedure.
     * The object must be a Hibernate entity.
     *
     * @param o the entity to update.
     */
    public void update(Object o) {
        checkIsEntity(o);
        Session session = beginTransaction();
        try {
            getLogger().debug("Updating Object : " + o.getClass().getName());
            session.update(o);
            commitTransaction(session);
        } catch (Throwable e) {
            getLogger().error("", e);
            rollbackTransaction(session);
            this.exceptionHandler.handle("Unable to update the given object !", e);
        }
    }

    /**
     * Return a list of every <T> type stored in the database.
     *
     * @param <T> The type to be returned by the recovering call.
     * @param egClass The class that represents the real type to be recover. (This type must be an Hibernate entity)
     * @return a list of every <T> type stored in the database.
     */
    public <T> List<T> recover(Class<T> egClass) {
        return recover(egClass, new Condition[] {});
    }

    /**
     * Return a list of every <T> type stored matching the given conditions.
     *
     * @param <T> The type to be returned by the recovering call.
     * @param egClass The class that represents the real type to be recover. (This type must be an Hibernate entity)
     * @param conditions a list of condition that represents the conditions of the request.
     * @return a list of every <T> type stored matching the given conditions.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> recover(Class<T> egClass, Condition... conditions) {
        //the goal is take advantage of polymorphism, so don't check if the class is an entity
        //A given class may return its object and every sub-object of this class.
        //checkIsEntity(egClass);
        getLogger().debug("Recovering " + egClass + " using " + conditions.length + " conditions");
        Session session = getSessionFactory().openSession();
        try {
            getLogger().debug("Creating query...");
            StringBuilder conds = new StringBuilder("");
            if (conditions != null && conditions.length > 0) {
                conds.append(" WHERE");
                for (int i = 0; i < conditions.length; i++) {
                    conds.append((i == 0 ? "" : " AND") + " c." + conditions[i].getField() + " " +
                        conditions[i].getComparator().getSymbol() + " :C" + i);
                }
            }
            String squery = "SELECT c from " + egClass.getName() + " c" + conds.toString();
            Query query = session.createQuery(squery);
            getLogger().debug("Created query : " + squery);
            if (conditions != null && conditions.length > 0) {
                for (int i = 0; i < conditions.length; i++) {
                    query.setParameter("C" + i, conditions[i].getValue());
                    getLogger().debug("Set parameter '" + "C" + i + "' value=" + conditions[i].getValue());
                }
            }
            return query.list();
        } catch (org.hibernate.exception.LockAcquisitionException LockAcq) {
            getLogger().info("", LockAcq);
            throw LockAcq;
        } catch (Exception e) {
            getLogger().error("", e);
            this.exceptionHandler.handle("Unable to recover the objects !", e);
            return null;//should not be reached as the previous line will throw an exception
        } finally {
            closeSession(session);
            getLogger().debug("Session closed");
        }
    }

    /**
     * Synchronize the given object in the database.<br />
     * The object must be an Hibernate entity, already stored in database, and
     * must have @alterable field(s).
     * In fact this method will only update the database for @alterable field for this object.
     *
     * @param o the object entity to synchronize.
     */
    public void synchronize(Object o) {
        Class<?> clazz = o.getClass();
        checkIsEntity(clazz);
        Field id = getIdField(clazz);
        String hibernateId = id.getName();
        //prepare HQL request for update
        String hqlUpdate = "UPDATE " + clazz.getName() + " c SET ";
        String hql;
        Field[] fields = getDeclaredFields(clazz, true);
        boolean hasAlterable = false;
        getLogger().debug("Synchronizing " + o.getClass().getName());
        //start transaction
        Session session = beginTransaction();
        try {
            //get id value to set it in the request
            id.setAccessible(true);
            for (Field f : fields) {
                if (f.isAnnotationPresent(Alterable.class)) {
                    getLogger().debug("Found alterable field : " + f.getName());
                    hasAlterable = true;
                    //create the request (ie:SET c.field=:alterableXX) with a specific id
                    hql = hqlUpdate + "c." + f.getName() + " = :" + ALTERABLE_REQUEST_FIELD;
                    hql += " WHERE c." + hibernateId + " = :" + OBJECTID_REQUEST_FIELD;
                    Query query = session.createQuery(hql);
                    getLogger().debug("Created query : " + hql);
                    f.setAccessible(true);
                    query.setParameter(ALTERABLE_REQUEST_FIELD, f.get(o));
                    getLogger().debug("Set parameter '" + ALTERABLE_REQUEST_FIELD + "' value=" + f.get(o));
                    //Set identifier for WHERE clause
                    query.setParameter(OBJECTID_REQUEST_FIELD, id.get(o));
                    getLogger().debug(
                            "Set WHERE clause parameter '" + OBJECTID_REQUEST_FIELD + "' value=" + id.get(o));
                    //execute request
                    query.executeUpdate();
                }
            }
            if (!hasAlterable) {
                //if there is no alterable fields (open session is closed in the 'finally')
                getLogger()
                        .warn(
                                "Synchronize has been called on an object that does not contains any @alterable field");
                return;
            }
            //commit
            commitTransaction(session);
            getLogger().debug("Transaction committed");
        } catch (Throwable e) {
            getLogger().error("", e);
            rollbackTransaction(session);
            this.exceptionHandler.handle("Unable to synchronize this object !", e);
        }
    }

    /**
     * Load the @unloadable field in the given object.<br />
     * This method will set, from the values in the database, every NULL fields that have the @unloadable annotation.
     * The object must also be an Hibernate entity.
     *
     * @param o the object entity to load.
     */
    public void load(Object o) {
        Class<?> clazz = o.getClass();
        checkIsEntity(clazz);
        Field fid = getIdField(clazz);
        String hibernateId = fid.getName();
        Field[] fields = getDeclaredFields(clazz, true);
        Session session = getSessionFactory().openSession();
        try {
            //get hibernate ID field of the given object
            fid.setAccessible(true);
            //for each field in the given object
            for (Field f : fields) {
                f.setAccessible(true);
                //if it is unloadable and null
                if (f.isAnnotationPresent(Unloadable.class) && f.get(o) == null) {
                    getLogger().debug("Found unloadable null field : " + f.getName());
                    Object value;
                    //related to improvement SCHEDULING-161 - to FIX : REMOVE FROM HERE
                    Any any = f.getAnnotation(Any.class);
                    if (any != null) {
                        //if @any is an annotation of this field (@JoinColumn exist as well)
                        //this condition is here only to fix a Hibernate bug :
                        //It cannot provide the given object if the field is annotated '@ANY'
                        //Check this bug in next Hibernate annotation, and remove this condition if the bug is fixed.
                        //
                        //Create Native SQL query
                        String squery = "SELECT c." + any.metaColumn().name() + ", c." +
                            f.getAnnotation(JoinColumn.class).name() + " FROM " +
                            clazz.getAnnotation(Table.class).name() + " c" + " WHERE c." + hibernateId +
                            " = " + fid.get(o).toString();
                        getLogger().debug("Created query : " + squery);
                        Query query = session.createSQLQuery(squery);
                        //get results
                        Object[] values = (Object[]) query.uniqueResult();
                        if (values == null || values.length == 0 || values[0] == null) {
                            //nothing to recover (null field in database) -> continue with the next unloadable field
                            continue;
                        }
                        //search for the real class of the given token 'table type'
                        for (MetaValue mv : f.getAnnotation(AnyMetaDef.class).metaValues()) {
                            if (mv.value().equals(values[0])) {
                                //store in the same place : avoid new declaration
                                values[0] = mv.targetEntity();
                                break;
                            }
                        }
                        squery = "FROM " + ((Class<?>) values[0]).getName() + " WHERE " + hibernateId +
                            " = :" + OBJECTID_REQUEST_FIELD;
                        //create HQL request
                        query = session.createQuery(squery);
                        getLogger().debug("Created query : " + squery);
                        //In SQL language, hibernate id is stored as a BigInteger -> in HQL as a long.
                        long lvalue;
                        try {
                            lvalue = ((BigInteger) values[1]).longValue();
                        } catch (ClassCastException e) {
                            lvalue = ((BigDecimal) values[1]).longValue();
                        }
                        query.setParameter(OBJECTID_REQUEST_FIELD, lvalue);
                        getLogger().debug("Set parameter '" + OBJECTID_REQUEST_FIELD + "=" + lvalue);
                        value = query.uniqueResult();
                    } else {
                        //SCHEDULING-161 - to FIX : TO HERE
                        //get the field value in the database and...
                        String squery = "SELECT c." + f.getName() + " FROM " + clazz.getName() +
                            " c WHERE c." + hibernateId + " = :" + OBJECTID_REQUEST_FIELD;
                        Query query = session.createQuery(squery);
                        getLogger().debug("Created query : " + squery);
                        query.setParameter(OBJECTID_REQUEST_FIELD, fid.get(o));
                        getLogger().debug("Set parameter '" + OBJECTID_REQUEST_FIELD + "=" + fid.get(o));
                        value = query.uniqueResult();
                    }//SCHEDULING-161 - to FIX : and HERE
                    //... store it in the object
                    f.set(o, value);
                }
            }
        } catch (Exception e) {
            getLogger().error("", e);
            this.exceptionHandler.handle("Unable to load this object [" + o + "]", e);
        } finally {
            closeSession(session);
            getLogger().debug("Session closed");
        }
    }

    /**
     * Cause every @unloadable fields in the given object to be set to NULL.<br />
     * Primitive field type won't be unloaded.
     *
     * @param o the object to unload.
     */
    public void unload(Object o) {
        Class<?> clazz = o.getClass();
        Field[] fields = getDeclaredFields(clazz, true);
        getLogger().debug("Unloading object : " + o.getClass().getName());
        try {
            //for each @unloadable field and non-primitive type
            for (Field f : fields) {
                if (f.isAnnotationPresent(Unloadable.class) && !f.getType().isPrimitive()) {
                    getLogger().debug("SET null to unloadable non primitive field : " + f.getName());
                    //the the value to null
                    f.setAccessible(true);
                    f.set(o, null);
                }
            }
        } catch (Exception e) {
            getLogger().error("", e);
            this.exceptionHandler.handle("Unable to unload one or more fields !", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public List sqlQuery(String nativeQuery) {
        if (!nativeQuery.startsWith("SELECT")) {
            throw new IllegalArgumentException(
                "Native Query string must be a read request, ie:start with 'SELECT'");
        }
        StatelessSession session = getSessionFactory().openStatelessSession();
        try {
            getLogger().debug("Executing query '" + nativeQuery + "'");
            return session.createSQLQuery(nativeQuery).list();
        } catch (Exception e) {
            getLogger().error("", e);
            //this exception will not be handled by exception manager as it is read only and not error prone
            //as it is in a separate thread
            //we let exceptionhandler handle only exception coming from core request
            throw new DatabaseManagerException("Unable to execute sqlQuery !", e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Get every fields contained in the given class and in the inheritance if needed.
     *
     * @param clazz the class where to find the fields.
     * @param inheritedFields if true, every inherited fields will be returned also.
     * @return every fields contained in the given class.
     */
    private Field[] getDeclaredFields(Class<?> clazz, boolean inheritedFields) {
        if (!inheritedFields) {
            return clazz.getDeclaredFields();
        }
        List<Field> fields = new ArrayList<Field>();
        do {
            for (Field f : clazz.getDeclaredFields()) {
                fields.add(f);
            }
        } while (!(clazz = clazz.getSuperclass()).equals(Object.class));
        return fields.toArray(new Field[] {});
    }

    /**
     * Get the name of the @Id annotated field.<br />
     * The given class must be an Hibernate entity with an @Id field.
     *
     * @param clazz the class where to find the ID field.
     * @return the name of the @Id annotated field.
     * @throws DatabaseManagerException if ID field not found.
     */
    private Field getIdField(Class<?> clazz) throws DatabaseManagerException {
        checkIsEntity(clazz);
        //check if this class has its ID field already saved in the map
        Field fieldId = idFields.get(clazz);
        if (fieldId != null) {
            //if so, return it
            return fieldId;
        }
        //if not, get the ID fields from all the fields
        Field[] fields = getDeclaredFields(clazz, true);
        for (Field f : fields) {
            if (f.isAnnotationPresent(Id.class)) {
                //store the new ID in the map
                idFields.put(clazz, f);
                return f;
            }
        }
        //If ID field not found...
        throw new DatabaseManagerException("Id field not found in : " + clazz.getName());
    }

    /**
     * Check if the given object is an Hibernate entity.
     *
     * @param o the object to check.
     * @return true if the given object is an Hibernate entity.
     */
    private boolean checkIsEntity(Object o) {
        return checkIsEntity(o.getClass());
    }

    /**
     * Check if the given class is an Hibernate entity.
     *
     * @param c the class to check.
     * @return true if the given class is an Hibernate entity.
     */
    private boolean checkIsEntity(Class<?> c) {
        if (c.isAnnotationPresent(Entity.class)) {
            return true;
        } else {
            throw new DatabaseManagerException("This object is not an Hibernate entity : " + c);
        }
    }

    /**
     * @return hibernate configuration file
     */
    public abstract String getConfigFile();

    /**
     * @return logger for release purposes
     */
    public abstract Logger getLogger();

    /**
     * {@inheritDoc}
     *
     * Default behavior is to notify the callback if there is one, and throw the given exception.
     */
    public void notify(DatabaseManagerException dme) {
        if (this.callback != null) {
            this.callback.notify(dme);
        }
        throw dme;
    }

    /**
     * {@inheritDoc}
     */
    public void setCallback(FilteredExceptionCallback callback) {
        this.callback = callback;
    }

}
