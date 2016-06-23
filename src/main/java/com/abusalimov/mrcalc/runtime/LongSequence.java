package com.abusalimov.mrcalc.runtime;

import java.util.Spliterator;

/**
 * The {@link Sequence} implementation specialized to primitive longs.
 *
 * @author Eldar Abusalimov
 */
public interface LongSequence extends Sequence<Long> {
    @Override
    Spliterator.OfLong spliterator();
}
