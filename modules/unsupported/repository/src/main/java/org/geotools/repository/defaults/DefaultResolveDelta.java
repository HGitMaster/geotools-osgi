/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.repository.defaults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.repository.Resolve;
import org.geotools.repository.ResolveDelta;
import org.geotools.repository.ResolveDeltaVisitor;


/**
 * Catalog delta.
 *
 * @since 0.6.0
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/defaults/DefaultResolveDelta.java $
 */
public class DefaultResolveDelta implements ResolveDelta {
    private List children;
    private Kind kind = Kind.NO_CHANGE;
    private Resolve handle = null;
    private Resolve newHandle = null;

    /**
     * Delta for a changed handle, ie handle state refresh.
     * 
     * <p>
     * Used to communicate that new Info is available and labels should be
     * refreshed.
     * </p>
     *
     * @param handle DOCUMENT ME!
     * @param changes DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public DefaultResolveDelta(Resolve handle, List changes) {
        this.kind = Kind.CHANGED;
        this.children = Collections.unmodifiableList(changes);
        this.handle = handle;

        if (kind == Kind.REPLACED) {
            throw new IllegalArgumentException(
                "New handle required in replace event.");
        }

        newHandle = null;
    }

    /**
     * Simple change used for Add and Remove with no children
     *
     * @param handle DOCUMENT ME!
     * @param kind DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public DefaultResolveDelta(Resolve handle, Kind kind) {
        this.kind = kind;
        this.children = NO_CHILDREN;
        this.handle = handle;

        if (kind == Kind.REPLACED) {
            throw new IllegalArgumentException(
                "New handle required in replace event.");
        }

        newHandle = null;
    }

    /**
     * Delta for a specific change
     *
     * @param handle DOCUMENT ME!
     * @param kind DOCUMENT ME!
     * @param changes DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public DefaultResolveDelta(Resolve handle, Kind kind, List changes) {
        this.kind = kind;

        if (changes == null) {
            changes = new ArrayList();
        }

        this.children = Collections.unmodifiableList(changes);
        this.handle = handle;

        if (kind == Kind.REPLACED) {
            throw new IllegalArgumentException(
                "New handle required in replace event.");
        }

        newHandle = null;
    }

    /**
     * Delta for handle repalcement.
     * 
     * <p>
     * Used to indicate the actual connection used by the handle has been
     * replaced. Layers should foget everything they no and latch onto the
     * newHandle.
     * </p>
     *
     * @param handle DOCUMENT ME!
     * @param newHandle DOCUMENT ME!
     * @param changes DOCUMENT ME!
     */
    public DefaultResolveDelta(Resolve handle, Resolve newHandle, List changes) {
        this.kind = Kind.REPLACED;

        if (changes == null) {
            changes = new ArrayList();
        }

        this.children = Collections.unmodifiableList(changes);
        this.handle = handle;
        this.newHandle = newHandle;
    }

    /*
     * @see net.refractions.udig.catalog.ICatalogDelta#accept(net.refractions.udig.catalog.IServiceVisitor)
     */
    public void accept(ResolveDeltaVisitor visitor) throws IOException {
        visitor.visit(this);

        for (Iterator itr = children.iterator(); itr.hasNext();) {
            DefaultResolveDelta delta = (DefaultResolveDelta) itr.next();

            if ((delta != null) && visitor.visit(delta)) {
                delta.accept(visitor);
            }
        }
    }

    /*
     * @see net.refractions.udig.catalog.ICatalogDelta#getAffected()
     */
    public List getChildren() {
        return children;
    }

    /*
     * @see net.refractions.udig.catalog.ICatalogDelta#getAffected(int, int)
     */
    public List getChildren(Set kindMask) {
        List list = new ArrayList();

        for (Iterator itr = children.iterator(); itr.hasNext();) {
            DefaultResolveDelta delta = (DefaultResolveDelta) itr.next();

            if ((delta != null) && kindMask.contains(delta.getKind())) {
                list.add(delta);
            }
        }

        return list;
    }

    /*
     * @see net.refractions.udig.catalog.IDelta#getKind()
     */
    public Kind getKind() {
        return kind;
    }

    public Resolve getResolve() {
        return handle;
    }

    public Resolve getNewResolve() {
        return newHandle;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        /*
         * private List<IResolveDelta> children; private Kind kind = Kind.NO_CHANGE; private
         * IResolve handle = null; private IResolve newHandle = null;
         */
        buffer.append(" ResolveDelta: ["); //$NON-NLS-1$
        buffer.append(kind);

        if (handle != null) {
            buffer.append(","); //$NON-NLS-1$
            buffer.append(handle);
        }

        if (newHandle != null) {
            buffer.append(","); //$NON-NLS-1$
            buffer.append(newHandle);
        }

        if (children != null) {
            buffer.append("children ["); //$NON-NLS-1$

            for (int i = 0; i < children.size(); i++) {
                DefaultResolveDelta delta = (DefaultResolveDelta) children.get(i);
                buffer.append(delta.getKind());
            }

            buffer.append("] "); //$NON-NLS-1$
        }

        buffer.append("] "); //$NON-NLS-1$

        return buffer.toString();
    }
}
