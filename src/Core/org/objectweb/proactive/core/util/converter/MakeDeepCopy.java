package org.objectweb.proactive.core.util.converter;

import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;


public class MakeDeepCopy {
    protected enum ConversionMode {MARSHALL,
        OBJECT,
        PAOBJECT;
    }
    public static class WithMarshallStream {
        public static Object makeDeepCopy(Object o)
            throws ProActiveException, IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.MarshallStream.convert(o);
            return ByteToObjectConverter.MarshallStream.convert(array);
        }
    }

    public static class WithObjectStream {
        public static Object makeDeepCopy(Object o)
            throws ProActiveException, IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.ObjectStream.convert(o);
            return ByteToObjectConverter.ObjectStream.convert(array);
        }
    }

    public static class WithProActiveObjectStream {
        public static Object makeDeepCopy(Object o)
            throws ProActiveException, IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.ProActiveObjectStream.convert(o);
            return ByteToObjectConverter.ProActiveObjectStream.convert(array);
        }
    }
}
