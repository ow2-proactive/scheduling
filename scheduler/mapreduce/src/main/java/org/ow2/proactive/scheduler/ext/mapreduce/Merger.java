package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ChecksumFileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.util.PriorityQueue;
import org.apache.hadoop.util.Progress;


/**
 * The {@link Merger} class is a simply copy and paste of the
 * Hadoop class org.apache.hadoop.mapred.Merger. We need to do
 * the copy and paste because the visibility of the Hadoop class
 * org.apache.hadoop.mapred.Merger is limited to the package,
 * so that we cannot re-use directly that class.
 *
 * @author The ProActive Team
 *
 */
public class Merger {

    /**
     *
     * @param <K>
     * @param <V>
     * @param conf
     * @param fs
     * @param keyClass
     * @param valueClass
     * @param inputs
     * @param deleteInputs
     * @param mergeFactor
     * @param tmpDir: is not used (TODO maybe we must modify the signature of the method)
     * @param comparator
     * @return
     * @throws IOException
     */
    public static <K extends Object, V extends Object> RawKeyValueIterator merge(Configuration conf,
            FileSystem fs, Class<K> keyClass, Class<V> valueClass, Path[] inputs, boolean deleteInputs,
            int mergeFactor, Path tmpDir, RawComparator<K> comparator) throws IOException {
        return new MergeQueue<K, V>(conf, fs, inputs, deleteInputs, comparator).merge(keyClass, valueClass,
                mergeFactor, tmpDir);
    }

    public static <K extends Object, V extends Object> RawKeyValueIterator merge(Configuration conf,
            FileSystem fs, Class<K> keyClass, Class<V> valueClass, List<Segment<K, V>> segments,
            int mergeFactor, Path tmpDir, RawComparator<K> comparator) throws IOException {
        return new MergeQueue<K, V>(conf, fs, segments, comparator, false).merge(keyClass, valueClass,
                mergeFactor, tmpDir);

    }

    public static <K extends Object, V extends Object> RawKeyValueIterator merge(Configuration conf,
            FileSystem fs, Class<K> keyClass, Class<V> valueClass, List<Segment<K, V>> segments,
            int mergeFactor, Path tmpDir, RawComparator<K> comparator, boolean sortSegments)
            throws IOException {
        return new MergeQueue<K, V>(conf, fs, segments, comparator, sortSegments).merge(keyClass, valueClass,
                mergeFactor, tmpDir);
    }

    static <K extends Object, V extends Object> RawKeyValueIterator merge(Configuration conf, FileSystem fs,
            Class<K> keyClass, Class<V> valueClass, List<Segment<K, V>> segments, int mergeFactor,
            int inMemSegments, Path tmpDir, RawComparator<K> comparator, boolean sortSegments)
            throws IOException {
        return new MergeQueue<K, V>(conf, fs, segments, comparator, sortSegments).merge(keyClass, valueClass,
                mergeFactor, inMemSegments, tmpDir);
    }

    public static <K extends Object, V extends Object> void writeFile(RawKeyValueIterator records,
            IntermediateFile.Writer<K, V> writer, Configuration conf) throws IOException {
        while (records.next()) {
            writer.append(records.getKey(), records.getValue());
        }
    }

    public static class Segment<K extends Object, V extends Object> {
        IntermediateFile.Reader<K, V> reader = null;
        DataInputBuffer key = new DataInputBuffer();
        DataInputBuffer value = new DataInputBuffer();

        Configuration conf = null;
        FileSystem fs = null;
        Path file = null;
        boolean preserve = false;
        long segmentOffset = 0;
        long segmentLength = -1;

        public Segment(Configuration conf, FileSystem fs, Path file, boolean preserve) throws IOException {
            this(conf, fs, file, 0, fs.getFileStatus(file).getLen(), preserve);
        }

        public Segment(Configuration conf, FileSystem fs, Path file, long segmentOffset, long segmentLength,
                boolean preserve) throws IOException {
            this.conf = conf;
            this.fs = fs;
            this.file = file;
            this.preserve = preserve;

            this.segmentOffset = segmentOffset;
            this.segmentLength = segmentLength;
        }

        public Segment(IntermediateFile.Reader<K, V> reader, boolean preserve) {
            this.reader = reader;
            this.preserve = preserve;

            this.segmentLength = reader.getLength();
        }

        private void init() throws IOException {
            if (reader == null) {
                FSDataInputStream in = fs.open(file);
                in.seek(segmentOffset);
                reader = new IntermediateFile.Reader<K, V>(conf, in, segmentLength);
            }
        }

        DataInputBuffer getKey() {
            return key;
        }

        DataInputBuffer getValue() {
            return value;
        }

        long getLength() {
            return (reader == null) ? segmentLength : reader.getLength();
        }

        boolean next() throws IOException {
            return reader.next(key, value);
        }

        void close() throws IOException {
            reader.close();

            if (!preserve && fs != null) {
                fs.delete(file, false);
            }
        }

        public long getPosition() throws IOException {
            return reader.getPosition();
        }
    }

    private static class MergeQueue<K extends Object, V extends Object> extends PriorityQueue<Segment<K, V>>
            implements RawKeyValueIterator {
        Configuration conf;
        FileSystem fs;

        List<Segment<K, V>> segments = new ArrayList<Segment<K, V>>();

        RawComparator<K> comparator;

        private long totalBytesProcessed;
        private float progPerByte;

        DataInputBuffer key;
        DataInputBuffer value;

        Segment<K, V> minSegment;
        Comparator<Segment<K, V>> segmentComparator = new Comparator<Segment<K, V>>() {
            public int compare(Segment<K, V> o1, Segment<K, V> o2) {
                if (o1.getLength() == o2.getLength()) {
                    return 0;
                }

                return o1.getLength() < o2.getLength() ? -1 : 1;
            }
        };

        public MergeQueue(Configuration conf, FileSystem fs, Path[] inputs, boolean deleteInputs,
                RawComparator<K> comparator) throws IOException {
            this.conf = conf;
            this.fs = fs;
            this.comparator = comparator;

            for (Path file : inputs) {
                segments.add(new Segment<K, V>(conf, fs, file, !deleteInputs));
            }

            // Sort segments on file-lengths
            Collections.sort(segments, segmentComparator);
        }

        public MergeQueue(Configuration conf, FileSystem fs, List<Segment<K, V>> segments,
                RawComparator<K> comparator) {
            this(conf, fs, segments, comparator, false);
        }

        public MergeQueue(Configuration conf, FileSystem fs, List<Segment<K, V>> segments,
                RawComparator<K> comparator, boolean sortSegments) {
            this.conf = conf;
            this.fs = fs;
            this.comparator = comparator;
            this.segments = segments;
            if (sortSegments) {
                Collections.sort(segments, segmentComparator);
            }
        }

        public void close() throws IOException {
            Segment<K, V> segment;
            while ((segment = pop()) != null) {
                segment.close();
            }
        }

        public DataInputBuffer getKey() throws IOException {
            return key;
        }

        public DataInputBuffer getValue() throws IOException {
            return value;
        }

        private void adjustPriorityQueue(Segment<K, V> reader) throws IOException {
            long startPos = reader.getPosition();
            boolean hasNext = reader.next();
            long endPos = reader.getPosition();
            totalBytesProcessed += endPos - startPos;
            if (hasNext) {
                adjustTop();
            } else {
                pop();
                reader.close();
            }
        }

        public boolean next() throws IOException {
            if (size() == 0)
                return false;

            if (minSegment != null) {
                /*
                 * minSegment is non-null for all invocations of next except the
                 * first one. For the first invocation, the priority queue is
                 * ready for use but for the subsequent invocations, first
                 * adjust the queue
                 */
                adjustPriorityQueue(minSegment);
                if (size() == 0) {
                    minSegment = null;
                    return false;
                }
            }
            minSegment = top();

            key = minSegment.getKey();
            value = minSegment.getValue();

            return true;
        }

        @SuppressWarnings("unchecked")
        protected boolean lessThan(Object a, Object b) {
            DataInputBuffer key1 = ((Segment<K, V>) a).getKey();
            DataInputBuffer key2 = ((Segment<K, V>) b).getKey();
            int s1 = key1.getPosition();
            int l1 = key1.getLength() - s1;
            int s2 = key2.getPosition();
            int l2 = key2.getLength() - s2;

            return comparator.compare(key1.getData(), s1, l1, key2.getData(), s2, l2) < 0;
        }

        public RawKeyValueIterator merge(Class<K> keyClass, Class<V> valueClass, int factor, Path tmpDir)
                throws IOException {
            return merge(keyClass, valueClass, factor, 0, tmpDir);
        }

        RawKeyValueIterator merge(Class<K> keyClass, Class<V> valueClass, int factor, int inMem, Path tmpDir)
                throws IOException {

            // create the MergeStreams from the sorted map created in the
            // constructor
            // and dump the final output to a file
            int numSegments = segments.size();
            int origFactor = factor;
            int passNo = 1;
            do {
                // get the factor for this pass of merge. We assume in-memory
                // segments
                // are the first entries in the segment list and that the pass
                // factor
                // doesn't apply to them
                factor = getPassFactor(factor, passNo, numSegments - inMem);
                if (1 == passNo) {
                    factor += inMem;
                }
                List<Segment<K, V>> segmentsToMerge = new ArrayList<Segment<K, V>>();
                int segmentsConsidered = 0;
                int numSegmentsToConsider = factor;
                long startBytes = 0; // starting bytes of segments of this merge
                while (true) {
                    // extract the smallest 'factor' number of segments
                    // Call cleanup on the empty segments (no key/value data)
                    List<Segment<K, V>> mStream = getSegmentDescriptors(numSegmentsToConsider);
                    for (Segment<K, V> segment : mStream) {
                        // Initialize the segment at the last possible moment;
                        // this helps in ensuring we don't use buffers until we
                        // need them
                        segment.init();
                        long startPos = segment.getPosition();
                        boolean hasNext = segment.next();
                        long endPos = segment.getPosition();
                        startBytes += endPos - startPos;

                        if (hasNext) {
                            segmentsToMerge.add(segment);
                            segmentsConsidered++;
                        } else {
                            segment.close();
                            numSegments--; // we ignore this segment for the
                            // merge
                        }
                    }
                    // if we have the desired number of segments
                    // or looked at all available segments, we break
                    if (segmentsConsidered == factor || segments.size() == 0) {
                        break;
                    }

                    numSegmentsToConsider = factor - segmentsConsidered;
                }

                // feed the streams to the priority queue
                initialize(segmentsToMerge.size());
                clear();
                for (Segment<K, V> segment : segmentsToMerge) {
                    put(segment);
                }

                // if we have lesser number of segments remaining, then just
                // return the
                // iterator, else do another single level merge
                if (numSegments <= factor) {
                    // Reset totalBytesProcessed to track the progress of the
                    // final merge.
                    // This is considered the progress of the reducePhase, the
                    // 3rd phase
                    // of reduce task. Currently totalBytesProcessed is not used
                    // in sort
                    // phase of reduce task(i.e. when intermediate merges
                    // happen).
                    totalBytesProcessed = startBytes;

                    // calculate the length of the remaining segments. Required
                    // for
                    // calculating the merge progress
                    long totalBytes = 0;
                    for (int i = 0; i < segmentsToMerge.size(); i++) {
                        totalBytes += segmentsToMerge.get(i).getLength();
                    }
                    if (totalBytes != 0) // being paranoid
                        progPerByte = 1.0f / (float) totalBytes;

                    return this;
                } else {

                    // we want to spread the creation of temp files on multiple
                    // disks if
                    // available under the space constraints
                    long approxOutputSize = 0;
                    for (Segment<K, V> s : segmentsToMerge) {
                        approxOutputSize += s.getLength() +
                            ChecksumFileSystem.getApproxChkSumLength(s.getLength());
                    }

                    Path outputFile = new Path(PAMapReduceFramework.MAPPER_INTERMEDIATE_FILE_NAME_PREFIX +
                        PAMapReduceFramework.FILE_EXTENSION_SEPARATOR + passNo);

                    IntermediateFile.Writer<K, V> writer = new IntermediateFile.Writer<K, V>(conf, fs,
                        outputFile, keyClass, valueClass);
                    writeFile(this, writer, conf);
                    writer.close();

                    // we finished one single level merge; now clean up the
                    // priority
                    // queue
                    this.close();

                    // Add the newly create segment to the list of segments to
                    // be merged
                    Segment<K, V> tempSegment = new Segment<K, V>(conf, fs, outputFile, false);
                    segments.add(tempSegment);
                    numSegments = segments.size();
                    Collections.sort(segments, segmentComparator);

                    passNo++;
                }
                // we are worried about only the first pass merge factor. So
                // reset the
                // factor to what it originally was
                factor = origFactor;
            } while (true);
        }

        /**
         * Determine the number of segments to merge in a given pass. Assuming
         * more than factor segments, the first pass should attempt to bring the
         * total number of segments - 1 to be divisible by the factor - 1 (each
         * pass takes X segments and produces 1) to minimize the number of
         * merges.
         */
        private int getPassFactor(int factor, int passNo, int numSegments) {
            if (passNo > 1 || numSegments <= factor || factor == 1)
                return factor;
            int mod = (numSegments - 1) % (factor - 1);
            if (mod == 0)
                return factor;
            return mod + 1;
        }

        /**
         * Return (& remove) the requested number of segment descriptors from
         * the sorted map.
         */
        private List<Segment<K, V>> getSegmentDescriptors(int numDescriptors) {
            if (numDescriptors > segments.size()) {
                List<Segment<K, V>> subList = new ArrayList<Segment<K, V>>(segments);
                segments.clear();
                return subList;
            }

            List<Segment<K, V>> subList = new ArrayList<Segment<K, V>>(segments.subList(0, numDescriptors));
            for (int i = 0; i < numDescriptors; ++i) {
                segments.remove(0);
            }
            return subList;
        }

        @Override
        public Progress getProgress() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
