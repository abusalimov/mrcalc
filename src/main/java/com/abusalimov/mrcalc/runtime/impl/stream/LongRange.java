package com.abusalimov.mrcalc.runtime.impl.stream;

import com.abusalimov.mrcalc.runtime.AbstractSequence;
import com.abusalimov.mrcalc.runtime.Sequence;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.LongConsumer;

/**
 * Lightweight range of longs.
 *
 * @author Eldar Abusalimov
 */
public class LongRange extends AbstractSequence<Long> implements Sequence.OfLong {
    private final long start;
    private final int length;

    /**
     * Creates a new range starting from zero.
     *
     * @param endExclusive the end boundary of the range (exclusive), which is also the length of the range
     */
    public LongRange(long endExclusive) {
        this(0, endExclusive);
    }

    /**
     * Creates a new range filled by integers between the specified boundaries.
     *
     * @param startInclusive the start boundary of the range (inclusive)
     * @param endExclusive   the end boundary of the range (exclusive)
     */
    public LongRange(long startInclusive, long endExclusive) {
        this.start = startInclusive;

        long longLength = Math.max(0, endExclusive - startInclusive);
        if (longLength > Integer.MAX_VALUE) {
            throw new ArithmeticException("Sequence range size overflow");
        }
        this.length = (int) longLength;
    }

    @Override
    public Spliterator.OfLong spliterator() {
        return new RangeSpliterator(start, start + length);
    }

    @Override
    public Long get(int index) {
        return start + index;
    }

    @Override
    public int size() {
        return length;
    }

    protected static class RangeSpliterator implements Spliterator.OfLong {
        private final long fence;
        private long index;

        public RangeSpliterator(long start, long end) {
            this.fence = end;
            this.index = start;
        }

        @Override
        public Spliterator.OfLong trySplit() {
            long lo = index, mid = (lo + fence) >> 1;
            return (lo >= mid)
                    ? null
                    : new RangeSpliterator(lo, index = mid);
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            Objects.requireNonNull(action, "action");
            if (index < fence) {
                action.accept(index++);
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            long i = index;
            long hi = index = fence;
            while (i < hi) {
                action.accept(i++);
            }
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return (Spliterator.ORDERED |
                    Spliterator.DISTINCT |
                    Spliterator.SORTED |
                    Spliterator.SIZED |
                    Spliterator.NONNULL |
                    Spliterator.IMMUTABLE);
        }

        @Override
        public Comparator<? super Long> getComparator() {
            return null;
        }
    }
}
