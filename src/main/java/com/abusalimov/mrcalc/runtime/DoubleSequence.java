package com.abusalimov.mrcalc.runtime;

import java.util.Spliterator;

/**
 * The {@link Sequence} implementation specialized to primitive doubles.
 *
 * @author Eldar Abusalimov
 */
public interface DoubleSequence extends Sequence<Double> {
    @Override
    Spliterator.OfDouble spliterator();
}
