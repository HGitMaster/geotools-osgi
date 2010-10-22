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
package org.geotools.graph.io.standard;

import org.geotools.graph.io.GraphReaderWriter;

/**
 * Represents a GraphReaderWriter that reads/writes from/to files.
 *  
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/extension/graph/src/main/java/org/geotools/graph/io/standard/FileReaderWriter.java $
 */
public interface FileReaderWriter extends GraphReaderWriter {
  
  /** filename key **/
  public static final String FILENAME = "FILENAME";
    
}
