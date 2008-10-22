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
import java.util.Collections;
import java.util.Set;

import org.geotools.geometry.Envelope2D;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;

public interface TileRange {
    public TileRange EMPTY = new TileRange(){
        public Envelope getBounds() {
            return new Envelope(); // empty!
        }

        public Envelope2D getEnvelope2D() {
            return new Envelope2D( null, 0, 0, 0, 0 );
        }

        public Set getTiles() {
            return Collections.EMPTY_SET;
        }

        public boolean isLoaded() {
            return true; // as loaded as we will ever be
        }

        public void load( ProgressListener monitor ) {
            if( monitor != null ) monitor.complete();
        }

        public void refresh( ProgressListener monitor ) {
            if( monitor != null ) monitor.complete();
        }

        public Rectangle getRange() {
            return new Rectangle(0,0,0,0);
        }
        
    };
    /**
     * Bounds of this tile range.
     * 
     * @return bounds of tiles in this range
     */
    Envelope getBounds();
    
    /**
     * Envelope2D for this tile range.
     * 
     * @return bounds of tiles in this range
     */
    Envelope2D getEnvelope2D();
    
    /**
     * Range in row/col.
     *
     * @return Range in row col;
     */
    Rectangle getRange();

    void load( ProgressListener monitor ); // monitor advances as each tile is available
    boolean isLoaded();
    void refresh( ProgressListener monitor ); // leaves tiles as is, but redraws

    /**
     * Tiles in range
     * 
     * @return Set of GridCoverage2d
     */
    Set getTiles();
}
