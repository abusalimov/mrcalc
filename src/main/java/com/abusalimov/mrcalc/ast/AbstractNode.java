package com.abusalimov.mrcalc.ast;

import com.abusalimov.mrcalc.location.Location;

/**
 * An abstract implementation of the {@link Node} that provides basic {@link Location} support.
 *
 * @author Eldar Abusalimov
 */
public abstract class AbstractNode implements Node {
    private Location location;

    /**
     * Creates a new Node with no location attached. It can be set later using the {@link
     * #setLocation(Location)} method though.
     */
    public AbstractNode() {
    }

    /**
     * Creates a new Node and sets a given location.
     *
     * @param location {@link Location} to attach.
     */
    public AbstractNode(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }
}
