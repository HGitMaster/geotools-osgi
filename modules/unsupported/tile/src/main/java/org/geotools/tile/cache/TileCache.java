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
package org.geotools.tile.cache;

import java.awt.Rectangle;

import org.geotools.tile.TileDraw;

public interface TileCache {
    
    /**
     * Create a TileRange capturing the provided range.
     * <p>
     * The TileRange will be returned immidiately, the TileCache will schedule calling the provided TileDraw
     * class when TileRange.load( monitor ) is called.
     * <p>
     * The definition of row/cols is defined according the provided TileDraw stratagy object.
     * 
     * @param draw
     * @param range
     * @return
     */
    TileRange createRange( TileDraw draw, Rectangle range );
    
    /**
     * Indicate lack of interest in the tiles created by the provided TileDraw.
     * <p>
     * Used when user turns a layer off; or when changing between scales to indicate tiles that are unlikly
     * to be revisited.
     * </p> 
     * @param draw
     */
    void retireTiles( TileDraw draw );
    
    /**
     * Mark for clean up tiles created by the provided TileDraw.
     * <p>
     * Used when user removes a layer; or when shutting down.
     * </p>
     * @param draw
     */ 
    void flushTiles( TileDraw draw );
    
    /**
     * Used during shutdown.
     */
    void close();
}
