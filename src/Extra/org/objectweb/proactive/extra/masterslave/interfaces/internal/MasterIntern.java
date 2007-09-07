package org.objectweb.proactive.extra.masterslave.interfaces.internal;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.extra.masterslave.interfaces.Master;


public interface MasterIntern extends Master<TaskIntern<ResultIntern<Serializable>>, ResultIntern<Serializable>> {
    public void solveIds(final List<Long> taskIds);
}
