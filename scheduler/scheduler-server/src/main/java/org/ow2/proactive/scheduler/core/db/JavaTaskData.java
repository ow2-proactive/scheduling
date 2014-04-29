package org.ow2.proactive.scheduler.core.db;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.java.JavaExecutableContainer;


@Entity
@Table(name = "JAVA_TASK_DATA")
public class JavaTaskData extends CommonJavaTaskData {

    public JavaTaskData() {
    }

    static JavaTaskData createJavaTaskData(TaskData taskData, JavaExecutableContainer container) {
        JavaTaskData javaTaskData = new JavaTaskData();
        javaTaskData.initProperties(taskData, container);
        return javaTaskData;
    }

    ExecutableContainer createExecutableContainer() throws Exception {
        JavaExecutableContainer container = new JavaExecutableContainer(getUserExecutableClassName(),
            getSearializedArguments());

        return container;
    }

}
