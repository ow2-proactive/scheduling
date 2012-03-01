package org.ow2.proactive.scheduler.ext.matsci.client.common.data;

import java.io.Serializable;
import java.util.Comparator;


/**
 * IntStrComparator
 *
 * @author The ProActive Team
 */
public class IntStrComparator implements Comparator<String>, Serializable {

    public IntStrComparator() {

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
            answer = Integer.parseInt(o1) - Integer.parseInt(o2);
        } catch (NumberFormatException e) {
            answer = o1.compareTo(o2);
        }
        return answer;
    }
}
