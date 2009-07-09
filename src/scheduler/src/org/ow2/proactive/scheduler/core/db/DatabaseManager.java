/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core.db;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.MetaValue;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.db.annotation.Alterable;
import org.ow2.proactive.scheduler.common.db.annotation.Unloadable;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * DatabaseManager is responsible of every database transaction.<br />
 * Hibernate entities can be managed by this manager. It provides method to register, delete, synchronize
 * objects in database.
 * Each method will work on the object and its inheritance.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class DatabaseManager {

    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.DATABASE);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.DATABASE);

    //Hibernate configuration file
    private static final String configurationFile = PASchedulerProperties
            .getAbsolutePath(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getValueAsString());
    //Alterable field name pattern for HQL request
    private static final String ALTERABLE_REQUEST_FIELD = "alterable";
    //ObjectID field name for HQL request
    private static final String OBJECTID_REQUEST_FIELD = "objectId";
    //locks
    private static Object sessionlock = new Object();
    //Memory for id field name by class
    private static Map<Class<?>, String> idFields = new HashMap<Class<?>, String>();
    //Hibernate Session factory
    private static SessionFactory sessionFactory;
    //Hibernate configuration
    private static Configuration configuration;
    //Next variable is used to start a session manually outside of this class.
    //It can be used to make more than one action in a single transaction.
    //this object will store transaction by thread.
    private static ThreadLocal<Session> globalSession = new ThreadLocal<Session>();

    static {
        //Create configuration from hibernate.cfg.xml using XML file for mapping
        //configuration = new Configuration().configure(new File(configurationFile));
        //Create configuration from hibernate.cfg.xml using Hibernate annotation
        configuration = new AnnotationConfiguration().configure(new File(configurationFile));
    }

    /**
     * Get the session factory.
     *
     * @return the session factory.
     */
    private static SessionFactory getSessionFactory() {
        //build if not
        logger_dev.debug("Request to build the Hibernate session Factory");
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
    public static void setProperty(String propertyName, String value) {
        if (sessionFactory != null) {
            logger.warn("WARNING : Property set while session factory have already been built ! (" +
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
    public static void build() {
        try {
            if (sessionFactory == null) {
                // Build the SessionFactory
                logger_dev.info("Building Hibernate session Factory (configuration File : " +
                    configurationFile + " )");
                sessionFactory = configuration.buildSessionFactory();
                globalSession = new ThreadLocal<Session>();
            }
        } catch (Throwable ex) {
            logger_dev.error("Initial SessionFactory creation failed.", ex);
            throw new DatabaseManagerException("Initial SessionFactory creation failed.", ex);
        }
    }

    /**
     * Close the hibernate session factory.
     */
    public static void close() {
        try {
            logger_dev.info("Closing current session");
            sessionFactory.getCurrentSession().close();
            logger_dev.info("Closing session factory");
            sessionFactory.close();
        } catch (Exception e) {
            logger_dev.error("Error while closing database", e);
        } finally {
            globalSession = null;
            sessionFactory = null;
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
    public static void startTransaction() {
        synchronized (sessionlock) {
            Session s = globalSession.get();
            if (s == null) {
                s = DatabaseManager.getSessionFactory().openSession();
                s.beginTransaction();
                globalSession.set(s);
            }
        }
    }

    /**
     * Force a manually opened transaction to be committed.
     * See {@link #startTransaction()} for details.
     */
    public static void commitTransaction() {
        synchronized (sessionlock) {
            Session s = globalSession.get();
            if (s == null) {
                throw new RuntimeException("No current opened session to commit");
            }
            try {
                s.getTransaction().commit();
                s.close();
            } finally {
                globalSession.set(null);
            }
        }
    }

    /**
     * Force a manually opened transaction to be rolledback.
     * See {@link #startTransaction()} for details.
     */
    public static void rollbackTransaction() {
        synchronized (sessionlock) {
            Session s = globalSession.get();
            if (s == null) {
                throw new RuntimeException("No current opened session to rollback");
            }
            try {
                s.getTransaction().rollback();
                s.close();
            } finally {
                globalSession.set(null);
            }
        }
    }

    /**
     * Begin a transaction
     * First open a session, the begin a transaction on this session.
     *
     * @return the new opened session
     */
    private static Session beginTransaction() {
        Session s = globalSession.get();
        //if a session has been manually opened by a user, use it
        logger_dev.debug("Open new session, global session is " + ((s == null) ? "null" : "set"));
        if (s != null) {
            return s;
        }
        //if no session is opened, open a new one dedicated to the current thread
        s = DatabaseManager.getSessionFactory().openSession();
        s.beginTransaction();
        return s;
    }

    /**
     * Commit and close the given session.
     *
     * @param session the session to be committed.
     */
    private static void commitTransaction(Session session) {
        //if a session has been manually opened by a user, don't commit, wait for user to commit
        if (globalSession.get() != null) {
            return;
        }
        session.getTransaction().commit();
        session.close();
        logger_dev.debug("Transaction committed and closed");
    }

    /**
     * Rollback and close the given session.
     *
     * @param session the session to be rolledback.
     */
    private static void rollbackTransaction(Session session) {
        //if a session has been manually opened by a user, don't rollback, wait for user to commit
        if (globalSession.get() != null) {
            return;
        }
        session.getTransaction().rollback();
        session.close();
        logger_dev.debug("Transaction rolledback and closed");
    }

    /**
     * Register an object. The object must be an Hibernate entity.
     * This method will persist the given object and store it in the database.
     *
     * @param o the new object to store.
     */
    public static void register(Object o) {
        checkIsEntity(o);
        Session session = beginTransaction();
        try {
            logger_dev.info("Registering new Object : " + o.getClass().getName());
            session.save(o);
            commitTransaction(session);
        } catch (Exception e) {
            rollbackTransaction(session);
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to store the given object !", e);
        }
    }

    /**
     * Delete an object. The object must be an Hibernate entity.
     * This method will delete the given object and also every dependences.
     *
     * @param o the new object to store.
     */
    public static void delete(Object o) {
        checkIsEntity(o);
        Session session = beginTransaction();
        try {
            logger_dev.info("Deleting Object : " + o.getClass().getName());
            session.delete(o);
            commitTransaction(session);
        } catch (Exception e) {
            rollbackTransaction(session);
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to delete the given object !", e);
        }
    }

    /**
     * Update the given entity and every dependences.<br />
     * Just call the Hibernate update procedure.
     * The object must be a Hibernate entity.
     *
     * @param o the entity to update.
     */
    public static void update(Object o) {
        checkIsEntity(o);
        Session session = beginTransaction();
        try {
            logger_dev.info("Updating Object : " + o.getClass().getName());
            session.update(o);
            commitTransaction(session);
        } catch (Exception e) {
            rollbackTransaction(session);
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to update the given object !", e);
        }
    }

    /**
     * Return a list of every <T> type stored in the database.
     *
     * @param <T> The type to be returned by the recovering call.
     * @param egClass The class that represents the real type to be recover. (This type must be an Hibernate entity)
     * @return a list of every <T> type stored in the database.
     */
    public static <T> List<T> recover(Class<T> egClass) {
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
    public static <T> List<T> recover(Class<T> egClass, Condition... conditions) {
        //the goal is take advantage of polymorphism, so don't check if the class is an entity
        //A given class may return its object and every sub-object of this class.
        //checkIsEntity(egClass);
        logger_dev.info("Trying to recover " + egClass + " using " + conditions.length + " conditions");
        Session session = DatabaseManager.getSessionFactory().openSession();
        try {
            logger_dev.info("Creating query...");
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
            logger_dev.debug("Created query : " + squery);
            if (conditions != null && conditions.length > 0) {
                for (int i = 0; i < conditions.length; i++) {
                    query.setParameter("C" + i, conditions[i].getValue());
                    logger_dev.debug("Set parameter '" + "C" + i + "' value=" + conditions[i].getValue());
                }
            }
            return query.list();
        } catch (org.hibernate.exception.LockAcquisitionException LockAcq) {
            logger_dev.info("", LockAcq);
            throw LockAcq;
        } catch (Exception e) {
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to recover the objects !", e);
        } finally {
            session.close();
            logger_dev.debug("Session closed");
        }
    }

    /**
     * This method is specially design to recover all jobs that have to be recovered.
     * It is also a way to get them without taking care of conditions.
     * This method also ensure that every returned jobs have their @unloadable fields unloaded.
     *
     * @return The list of unloaded job entities.
     */
    @SuppressWarnings("unchecked")
    public static List<InternalJob> recoverAllJobs() {
        List<InternalJob> jobs = new ArrayList<InternalJob>();
        //class to recover
        Class<?> egClass = InternalJob.class;
        //create condition of recovering : recover only non-removed job
        Condition condition = new Condition("jobInfo.removedTime", ConditionComparator.LESS_EQUALS_THAN,
            (long) 0);
        logger_dev.info("Recovering all jobs using the removeTime condition");
        Session session = DatabaseManager.getSessionFactory().openSession();
        try {
            String conds = " WHERE c." + condition.getField() + " " + condition.getComparator().getSymbol() +
                " :C0";
            //find ID to recover
            String squery = "SELECT c.jobInfo.jobId from " + egClass.getName() + " c" + conds;
            Query query = session.createQuery(squery);
            logger_dev.info("Created query to find IDs : " + squery);
            query.setParameter("C0", condition.getValue());
            logger_dev.debug("Set parameter " + "'C0' value=" + condition.getValue());
            List<JobId> ids = (List<JobId>) query.list();
            logger_dev.info("Creating queries for each job to recover");
            //for each ID get the entity
            for (JobId jid : ids) {
                squery = "SELECT c from " + egClass.getName() + " c WHERE c.jobInfo.jobId=:C0";
                query = session.createQuery(squery);
                logger_dev.debug("Created query : " + squery);
                query.setParameter("C0", jid);
                logger_dev.debug("Set parameter " + "'C0' value=" + jid);
                InternalJob job = (InternalJob) query.uniqueResult();

                try {
                    Collection<TaskResult> results = job.getJobResult().getAllResults().values();
                    //unload taskResult
                    logger_dev.debug("Unloading taskResult for job " + jid);
                    for (TaskResult r : results) {
                        unload(r);
                    }
                } catch (NullPointerException e) {
                    logger_dev.debug("No result yet in this job " + jid);
                    //this exception means their is no results in this job
                }
                //unload InternalTask
                logger_dev.debug("Unloading internalTask for job " + jid);
                Collection<InternalTask> tasks = job.getITasks();
                for (InternalTask it : tasks) {
                    unload(it);
                }
                jobs.add(job);
                logger_dev.info("Job " + jid + " added to the final list");
            }
            return jobs;
        } catch (Exception e) {
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to recover a job !", e);
        } finally {
            session.close();
            logger_dev.debug("Session closed");
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
    public static void synchronize(Object o) {
        Class<?> clazz = o.getClass();
        checkIsEntity(clazz);
        String hibernateId = getIdFieldName(clazz);
        //prepare HQL request for update
        String hqlUpdate = "UPDATE " + clazz.getName() + " c SET ";
        String hql;
        Field[] fields = getDeclaredFields(clazz, true);
        boolean hasAlterable = false;
        logger_dev.info("Synchronizing " + o.getClass().getName());
        //start transaction
        Session session = beginTransaction();
        try {
            //get id value to set it in the request
            Field id = clazz.getDeclaredField(hibernateId);
            id.setAccessible(true);
            for (Field f : fields) {
                if (f.isAnnotationPresent(Alterable.class)) {
                    logger_dev.debug("Found alterable field : " + f.getName());
                    hasAlterable = true;
                    //create the request (ie:SET c.field=:alterableXX) with a specific id
                    hql = hqlUpdate + "c." + f.getName() + " = :" + ALTERABLE_REQUEST_FIELD;
                    hql += " WHERE c." + hibernateId + " = :" + OBJECTID_REQUEST_FIELD;
                    Query query = session.createQuery(hql);
                    logger_dev.debug("Created query : " + hql);
                    f.setAccessible(true);
                    query.setParameter(ALTERABLE_REQUEST_FIELD, f.get(o));
                    logger_dev.debug("Set parameter '" + ALTERABLE_REQUEST_FIELD + "' value=" + f.get(o));
                    //Set identifier for WHERE clause
                    query.setParameter(OBJECTID_REQUEST_FIELD, id.get(o));
                    logger_dev.debug("Set WHERE clause parameter '" + OBJECTID_REQUEST_FIELD + "' value=" +
                        id.get(o));
                    //execute request
                    query.executeUpdate();
                }
            }
            if (!hasAlterable) {
                //if there is no alterable fields (open session is closed in the 'finally')
                logger_dev
                        .warn("Synchronize has been called on an object that does not contains any @alterable field");
                return;
            }
            //commit
            commitTransaction(session);
            logger_dev.debug("Transaction committed");
        } catch (Exception e) {
            rollbackTransaction(session);
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to synchronize this object !", e);
        }
    }

    /**
     * Load the @unloadable field in the given object.<br />
     * This method will set, from the values in the database, every NULL fields that have the @unloadable annotation.
     * The object must also be an Hibernate entity.
     *
     * @param o the object entity to load.
     */
    public static void load(Object o) {
        Class<?> clazz = o.getClass();
        checkIsEntity(clazz);
        String hibernateId = getIdFieldName(clazz);
        Field[] fields = getDeclaredFields(clazz, true);
        Session session = DatabaseManager.getSessionFactory().openSession();
        try {
            //get hibernate ID field of the given object
            Field fid = clazz.getDeclaredField(hibernateId);
            fid.setAccessible(true);
            //for each field in the given object
            for (Field f : fields) {
                f.setAccessible(true);
                //if it is unloadable and null
                if (f.isAnnotationPresent(Unloadable.class) && f.get(o) == null) {
                    logger_dev.debug("Found unloadable null field : " + f.getName());
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
                        logger_dev.debug("Created query : " + squery);
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
                        logger_dev.debug("Created query : " + squery);
                        //In SQL language, hibernate id is stored as a BigInteger -> in HQL as a long.
                        long lvalue = ((BigInteger) values[1]).longValue();
                        query.setParameter(OBJECTID_REQUEST_FIELD, lvalue);
                        logger_dev.debug("Set parameter '" + OBJECTID_REQUEST_FIELD + "=" + lvalue);
                        value = query.uniqueResult();
                    } else {
                        //SCHEDULING-161 - to FIX : TO HERE
                        //get the field value in the database and...
                        String squery = "SELECT c." + f.getName() + " FROM " + clazz.getName() +
                            " c WHERE c." + hibernateId + " = :" + OBJECTID_REQUEST_FIELD;
                        Query query = session.createQuery(squery);
                        logger_dev.debug("Created query : " + squery);
                        query.setParameter(OBJECTID_REQUEST_FIELD, fid.get(o));
                        logger_dev.debug("Set parameter '" + OBJECTID_REQUEST_FIELD + "=" + fid.get(o));
                        value = query.uniqueResult();
                    }//SCHEDULING-161 - to FIX : and HERE
                    //... store it in the object
                    f.set(o, value);
                }
            }
        } catch (Exception e) {
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to load this object [" + o + "]", e);
        } finally {
            session.close();
            logger_dev.debug("Session closed");
        }
    }

    /**
     * Cause every @unloadable fields in the given object to be set to NULL.<br />
     * Primitive field type won't be unloaded.
     *
     * @param o the object to unload.
     */
    public static void unload(Object o) {
        Class<?> clazz = o.getClass();
        Field[] fields = getDeclaredFields(clazz, true);
        logger_dev.info("Unloading object : " + o.getClass().getName());
        try {
            //for each @unloadable field and non-primitive type
            for (Field f : fields) {
                if (f.isAnnotationPresent(Unloadable.class) && !f.getType().isPrimitive()) {
                    logger_dev.debug("SET null to unloadable non primitive field : " + f.getName());
                    //the the value to null
                    f.setAccessible(true);
                    f.set(o, null);
                }
            }
        } catch (Exception e) {
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to unload one or more fields !", e);
        }
    }

    /**
     * Get every fields contained in the given class and in the inheritance if needed.
     *
     * @param clazz the class where to find the fields.
     * @param inheritedFields if true, every inherited fields will be returned also.
     * @return every fields contained in the given class.
     */
    private static Field[] getDeclaredFields(Class<?> clazz, boolean inheritedFields) {
        if (!inheritedFields) {
            return clazz.getDeclaredFields();
        }
        List<Field> fields = new ArrayList<Field>();
        for (; !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
            for (Field f : clazz.getDeclaredFields()) {
                fields.add(f);
            }
        }
        return fields.toArray(new Field[] {});
    }

    /**
     * Get the name of the @Id annoted field.<br />
     * The given class must be an Hibernate entity with an @Id field.
     *
     * @param clazz the class where to find the ID field.
     * @return the name of the @Id annoted field.
     * @throws DatabaseManagerException if ID field not found.
     */
    private static String getIdFieldName(Class<?> clazz) throws DatabaseManagerException {
        checkIsEntity(clazz);
        //check if this class has its ID field already saved in the map
        String fieldIdName = idFields.get(clazz);
        if (fieldIdName != null) {
            //if so, return it
            return fieldIdName;
        }
        //if not, get the ID fields from all the fields
        Field[] fields = getDeclaredFields(clazz, false);
        for (Field f : fields) {
            if (f.isAnnotationPresent(Id.class)) {
                //store the new ID in the map
                idFields.put(clazz, f.getName());
                return f.getName();
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
    private static boolean checkIsEntity(Object o) {
        return checkIsEntity(o.getClass());
    }

    /**
     * Check if the given class is an Hibernate entity.
     *
     * @param c the class to check.
     * @return true if the given class is an Hibernate entity.
     */
    private static boolean checkIsEntity(Class<?> c) {
        if (c.isAnnotationPresent(Entity.class)) {
            return true;
        } else {
            throw new DatabaseManagerException("This object is not an Hibernate entity : " + c);
        }
    }

}
