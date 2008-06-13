/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.controller.MulticastController;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;


/**
 * <p>This enumeration defines the various dispatch modes available for
 * parameters. 
 * </p>
 * <p>It uses the "strategy" pattern: it implements the methods of
 * the <code>ParamDispatch</code> interface depending on the selected mode.
 *
 * @author The ProActive Team
 * 
 */
@PublicAPI
public enum ParamDispatchMode implements ParamDispatch, Serializable {
    /**
     * The default dispatch mode. All parameters are send to all
     * bounded server interfaces.
     */
    BROADCAST,
    /**
     * Sends the i<sup>th</sup> member of a List arguments
     * to i<sup>th</sup>connected server interface. The length of the List argument
     * and the number of bounded server interface must be the same.
     */
    ONE_TO_ONE,
    /**
     * Sends the i/n<sup>th</sup> member of a List arguments
     * to n<sup>th</sup>connected server interface.
     */
    ROUND_ROBIN,

    /**
     * sends members of a List arguments in a random manner to connected server
     * interfaces
     */
    RANDOM,

    /**
     * sends only one of the List arguments to one of the connected server interfaces.
     * 
     * Which argument and which server interface is determined by a custom controller that 
     * extends {@link MulticastController}
     */
    UNICAST,

    /**
     * The dispatch mode is given as a
     * parameter, as a class signature.
     */
    CUSTOM;
    /*
     * 
     * @see org.objectweb.proactive.core.component.type.annotations.ParametersDispatch#dispatch(java.util.List,
     *      int, int)
     */
    private List<Object> partition(List<?> inputParameter, int nbOutputReceivers)
            throws ParameterDispatchException {
        List<Object> result = new ArrayList<Object>();

        switch (this) {
            case BROADCAST:
                for (int i = 0; i < nbOutputReceivers; i++) {
                    result.add(inputParameter);
                }
                break;
            case ONE_TO_ONE:
                if (inputParameter.size() != nbOutputReceivers) {
                    throw new ParameterDispatchException(
                        "in a one-to-one distribution, the list of parameters on the client side"
                            + "must have a size equal to the number of connected receivers");
                }
                for (int i = 0; i < nbOutputReceivers; i++) {
                    result.add(inputParameter.get(i));
                }

                break;
            case ROUND_ROBIN:
                for (int i = 0; i < inputParameter.size(); i++) {
                    result.add(inputParameter.get(i));
                }
                break;
            case RANDOM:
                for (int i = 0; i < inputParameter.size(); i++) {
                    result.add(inputParameter.get(i));
                }
                Collections.shuffle(result);
                break;
            case UNICAST:
                if (inputParameter.size() != 1) {
                    throw new ParameterDispatchException("unicast only applies to input lists of size 1");
                }
                result.add(inputParameter.iterator().next());
                break;

            default:
                result = BROADCAST.partition(inputParameter, nbOutputReceivers);
                break;
        }

        return result;
    }

    public List<Object> partition(Object inputParameter, int nbOutputReceivers)
            throws ParameterDispatchException {
        if (inputParameter instanceof List) {
            return partition((List<?>) inputParameter, nbOutputReceivers);
        }

        // no dispatch in case of non-list parameters
        List<Object> result = new ArrayList<Object>();

        switch (this) {
            case UNICAST:
                result.add(inputParameter);
                break;
            default:
                // simple copy for multicast modes
                for (int i = 0; i < nbOutputReceivers; i++) {
                    result.add(inputParameter);
                }
        }

        return result;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#getDispatchSize(java.util.List,
     *      int)
     */
    private int expectedPartitionsSize(List<?> inputParameter, int nbOutputReceivers)
            throws ParameterDispatchException {
        int result = 0;

        switch (this) {
            case BROADCAST:
                result = nbOutputReceivers;
                break;
            case ONE_TO_ONE:
                if (inputParameter.size() != nbOutputReceivers) {
                    throw new ParameterDispatchException(
                        "in a one-to-one distribution, the list of parameters on the client side"
                            + "must have a size equal to the number of connected receivers");
                }
                result = nbOutputReceivers;
                break;
            case ROUND_ROBIN:
                result = inputParameter.size();
                break;
            case UNICAST:
                result = 1;
                break;
            case RANDOM:
                result = ROUND_ROBIN.expectedPartitionsSize(inputParameter, nbOutputReceivers);
            default:
                result = BROADCAST.expectedPartitionsSize(inputParameter, nbOutputReceivers);
        }

        return result;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#getDispatchSize(java.lang.Object,
     *      int)
     */
    public int expectedDispatchSize(Object inputParameter, int nbOutputReceivers)
            throws ParameterDispatchException {
        if (inputParameter instanceof List) {
            return expectedPartitionsSize((List<?>) inputParameter, nbOutputReceivers);
        } else {
            switch (this) {
                case UNICAST:
                    return 1;
                default:
                    return -1;
            }
        }

    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#matchesClientSideParameterType(java.lang.reflect.Type)
     */
    public boolean match(Type clientSideInputParameterType, Type serverSideInputParameterType)
            throws ParameterDispatchException {
        boolean result = false;
        boolean clientSideParamTypeIsParameterizedType = (clientSideInputParameterType instanceof ParameterizedType);
        boolean serverSideParamTypeIsParameterizedType = (serverSideInputParameterType instanceof ParameterizedType);
        Class<?> clientSideClass = null;
        Class<?> clientSideElementsType = null;
        Class<?> clientSideRawType = null;

        Class<?> serverSideClass = null;
        Class<?> serverSideElementsType = null;
        Class<?> serverSideRawType = null;

        if (clientSideParamTypeIsParameterizedType) {
            clientSideRawType = (Class<?>) ((ParameterizedType) clientSideInputParameterType).getRawType();
            if (!(((ParameterizedType) clientSideInputParameterType).getActualTypeArguments().length == 1)) {
                throw new ParameterDispatchException("client side input parameter type " +
                    clientSideInputParameterType + " can only be parameterized with one type");
            }
            Type cType = ((ParameterizedType) clientSideInputParameterType).getActualTypeArguments()[0];
            if (cType instanceof ParameterizedType) {
                clientSideElementsType = (Class<?>) ((ParameterizedType) cType).getRawType();
            } else {
                clientSideElementsType = (Class<?>) cType;
            }
        } else {
            if (clientSideInputParameterType instanceof Class<?>) {
                clientSideClass = (Class<?>) clientSideInputParameterType;
            } else {
                throw new ParameterDispatchException("client side input parameter type " +
                    clientSideInputParameterType + " can only be either a parameterized type or a class");
            }
        }

        if (serverSideParamTypeIsParameterizedType) {
            serverSideRawType = ((Class<?>) ((ParameterizedType) serverSideInputParameterType).getRawType());
            if (!(((ParameterizedType) serverSideInputParameterType).getActualTypeArguments().length == 1)) {
                throw new ParameterDispatchException("server side input parameter type " +
                    serverSideInputParameterType + " can only be parameterized with one type");
            }
            Type sType = ((ParameterizedType) serverSideInputParameterType).getActualTypeArguments()[0];
            if (sType instanceof ParameterizedType) {
                serverSideElementsType = (Class<?>) ((ParameterizedType) sType).getRawType();
            } else {
                serverSideElementsType = (Class<?>) sType;
            }

            // serverSideElementsType = ((Class<?>) ((ParameterizedType)
            // serverSideInputParameterType).getOwnerType());
        } else {
            if (serverSideInputParameterType instanceof Class<?>) {
                serverSideClass = (Class<?>) serverSideInputParameterType;
            } else {
                throw new ParameterDispatchException("server side input parameter type " +
                    serverSideInputParameterType + " is incompatible with " +
                    "client side input parameter type " + clientSideInputParameterType);
            }
        }

        switch (this) {
            case BROADCAST:
                if (clientSideParamTypeIsParameterizedType) {
                    if (serverSideParamTypeIsParameterizedType) {
                        result = clientSideRawType.isAssignableFrom(serverSideRawType) &&
                            clientSideElementsType.isAssignableFrom(clientSideElementsType);
                    } else {
                        result = true; // maybe this constraint should be softened
                    }
                } else {
                    result = clientSideClass.isAssignableFrom(serverSideClass);
                }
                break;
            case ONE_TO_ONE:
                if (clientSideParamTypeIsParameterizedType) {
                    if (serverSideParamTypeIsParameterizedType) {
                        result = clientSideElementsType.isAssignableFrom(serverSideRawType);
                    } else {
                        result = clientSideElementsType.isAssignableFrom(serverSideClass);
                    }
                } else {
                    result = false; // maybe this constraint should be softened
                }
                break;
            case ROUND_ROBIN:
                result = ONE_TO_ONE.match(clientSideInputParameterType, serverSideInputParameterType);
                break;
            case RANDOM:
                result = ROUND_ROBIN.match(clientSideInputParameterType, serverSideInputParameterType);
                break;
            case UNICAST:
                // actually same as broadcast mode
                result = BROADCAST.match(clientSideInputParameterType, serverSideInputParameterType);
                break;
            default:
                result = BROADCAST.match(clientSideInputParameterType, serverSideInputParameterType);
        }

        return result;
    }
}
