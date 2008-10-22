/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.graph.structure;


/**
 * An interface in which to implement a visitor pattern with components of a
 * graph.
 *
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/graph/src/main/java/org/geotools/graph/structure/GraphVisitor.java $
 */
public interface GraphVisitor {
  
  /**
   * Presents the visitor with the component to visit. 
   * 
   * @param component The component being visited.
   * 
   * @return An integer signal value back to the caller.
   */
  public int visit(Graphable component);
}
