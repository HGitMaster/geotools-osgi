/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.spatialindex;

import java.io.Serializable;


/** Instances of this class provide unique identifiers for nodes,
 * and are used to store and retrieve nodes from their storage.
 * Implementors must take care that instances have to be immutable.
 * Nodes are basically identified by the region they represent.
 * Kinds of nodes or kinds of storage may require to use other elements to identify
 * nodes. Implementors must take care to override hashCode() and equals() accordingly.
 * NodeIdentifier should not reference the node they identify,
 * as they are likely to be used to passivate nodes in secondary storage.
 *
 * @author crousson
 *
 */
public abstract class NodeIdentifier implements Serializable {
    //    transient boolean visited = false;
    boolean valid = false;

    public abstract Shape getShape();

    //    public void setVisited(boolean visited) {
    //        this.visited = visited;
    //    }
    //
    //    public boolean isVisited() {
    //        return visited;
    //    }
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
