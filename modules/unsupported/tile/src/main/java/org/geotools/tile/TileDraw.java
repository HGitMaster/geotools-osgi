/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.tile;

import org.geotools.coverage.grid.GridCoverage2D;

/**
 * Please note implementations should be threadsafe as TileCache
 * implementations will often have a pool of worker threads assigned
 * to tile creation.
 * <p>
 * This construct is captured as an abstract class to allow
 * for the addition of scheduling or event notification as
 * future needs dictate. Any additional methods will not be abstract;
 * allowing your code to function without modification.
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 */
public abstract class TileDraw {
     public abstract String name( int row, int col );
     public abstract GridCoverage2D drawPlaceholder( int row, int col );
     public abstract GridCoverage2D drawTile( int row, int col );
}
