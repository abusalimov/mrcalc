package com.abusalimov.mrcalc.runtime;

import java.util.AbstractList;

/**
 * Implements pretty-printing {@link #toString()} method.
 *
 * @author Eldar Abusalimov
 */
public abstract class AbstractSequence<E> extends AbstractList<E> {
    protected static final int MAX_TO_STRING_ELEMENTS = 20;
    protected static final int TO_STRING_FOLD_BOUNDARY_ELEMENTS = Math.max(1, (MAX_TO_STRING_ELEMENTS - 5) / 2);

    @Override
    public String toString() {
        int length = size();
        if (length <= MAX_TO_STRING_ELEMENTS) {
            return super.toString();
        }

        StringBuilder sb = new StringBuilder().append('[');

        for (int i = 0; i < length; i++) {
            Object o;

            if (TO_STRING_FOLD_BOUNDARY_ELEMENTS <= i && i < length - TO_STRING_FOLD_BOUNDARY_ELEMENTS) {
                /* Skip the mid elements. */
                i = length - TO_STRING_FOLD_BOUNDARY_ELEMENTS;
                o = String.format("(%d more elements)...", length - TO_STRING_FOLD_BOUNDARY_ELEMENTS * 2);
            } else {
                E e = get(i);
                o = (e == this) ? "(this Collection)" : e;
            }
            sb.append(o);
            if (i < length - 1) {
                sb.append(',').append(' ');
            }
        }

        return sb.append(']').toString();
    }
}
