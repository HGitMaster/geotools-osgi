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

import java.net.URI;
import java.util.SortedSet;


/**
 * Captures all the information used to define a tile map.
 * 
 * @author Jody Garnett
 */
public interface TileMapInfo extends GeoResourceInfo {   
    /**
     * Describes the range of ZoomLevels supported.
     * 
     * @return SortedSet of ZoomLevel
     */
    SortedSet getZoomLevels();
    
    /** Identifier used to tag georesource */
    URI getIdentifier();
}
