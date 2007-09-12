package functionalTests.component.conform.components;

import java.util.List;

import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface Master {
    void computeOneWay(List<String> args, String other);

    List<StringWrapper> computeAsync(List<String> args, String other);

    List<GenericTypeWrapper<String>> computeAsyncGenerics(List<String> args,
        String other);

    List<String> computeSync(List<String> args, String other);
}
