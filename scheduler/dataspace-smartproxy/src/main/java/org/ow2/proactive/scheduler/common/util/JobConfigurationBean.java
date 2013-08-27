package org.ow2.proactive.scheduler.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobState;


public abstract class JobConfigurationBean {

    public static JobConfigurationBean createjobConfigurationFromGeneralInfo(JobState job,
            Class<? extends JobConfigurationBean> clazz) throws InstantiationException,
            IllegalAccessException {

        // Class clazz = arg.getClass();

        // if (!JobConfiguration.class.getClass().isAssignableFrom(clazz))
        // {
        // throw new IllegalArgumentException("The expected class in argument
        // should be of type "+JobConfiguration.class.getName());
        // }

        JobConfigurationBean jobConfiguration = clazz.newInstance();

        Method[] ms = clazz.getDeclaredMethods();

        Map<String, String> generalInfo = job.getGenericInformations();

        for (String key : generalInfo.keySet()) {
            // the key is the property name
            // i.e. for a couple of methods getToto()/setToto(String toto) we
            // have key.equals("Toto")
            String setterName = "set" + key;
            Method setter = getMethodbyName(ms, setterName);
            if (setter != null) {
                String value = generalInfo.get(key);
                Object[] args = new Object[1];
                args[0] = value;

                try {
                    setter.invoke(jobConfiguration, args);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return jobConfiguration;
    }

    protected static Method getMethodbyName(Method[] ms, String methodName) {
        for (int i = 0; i < ms.length; i++) {
            Method m = ms[i];
            if (m.getName().equals(methodName))
                return m;
        }

        return null;
    }

}
