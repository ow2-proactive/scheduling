package org.ow2.proactive.scheduler.ext.matsci.client.common.data;

import java.io.Serializable;
import java.util.Comparator;


/**
 * TaskNameComparator
 *
 * @author The ProActive Team
 */
public class TaskNameComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = 32L;

    public TaskNameComparator() {

    }

    public int compare(String o1, String o2) {
        if (o1 == null && o2 == null)
            return 0;
        // assuming you want null values shown last
        if (o1 != null && o2 == null)
            return -1;
        if (o1 == null && o2 != null)
            return 1;
        int answer = 0;
        try {
            String[] arr1 = o1.split("_");
            String[] arr2 = o2.split("_");
            answer = Integer.parseInt(arr1[1]) - Integer.parseInt(arr2[1]);
            if (answer == 0) {
                answer = Integer.parseInt(arr1[0]) - Integer.parseInt(arr2[0]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return answer;
    }
}
