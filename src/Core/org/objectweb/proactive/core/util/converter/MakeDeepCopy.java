package org.objectweb.proactive.core.util.converter;

import java.io.IOException;


public class MakeDeepCopy {
    protected enum ConversionMode {MARSHALL,
        OBJECT,
        PAOBJECT;
    }
    public static class WithMarshallStream {

        /**
             * Perform a deep copy of an object using a marshall stream.
             * @param o The object to be deep copied
             * @return the copy.
             * @throws IOException
             * @throws ClassNotFoundException
             */
        public static Object makeDeepCopy(Object o)
            throws IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.MarshallStream.convert(o);
            return ByteToObjectConverter.MarshallStream.convert(array);
        }
    }

    public static class WithObjectStream {

        /**
             * Perform a deep copy of an object using a regular object stream.
             * @param o The object to be deep copied
             * @return the copy.
             * @throws IOException
             * @throws ClassNotFoundException
             */
        public static Object makeDeepCopy(Object o)
            throws IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.ObjectStream.convert(o);
            return ByteToObjectConverter.ObjectStream.convert(array);
        }
    }

    public static class WithProActiveObjectStream {

        /**
             * Perform a deep copy of an object using a proactive object stream.
             * @param o The object to be deep copied
             * @return the copy.
             * @throws IOException
             * @throws ClassNotFoundException
             */
        public static Object makeDeepCopy(Object o)
            throws IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.ProActiveObjectStream.convert(o);
            return ByteToObjectConverter.ProActiveObjectStream.convert(array);
        }
    }
}
