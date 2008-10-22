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
import java.util.List;

import org.opengis.util.ProgressListener;

public abstract class TileStratagy {

    public abstract TileServiceInfo getInfo( ProgressListener monitor );    

    public abstract TileMapInfo getTileMapInfo( TileServiceInfo info, URI id, ProgressListener monitor );
    
    /**
     * TileDraw used to render indicated TileSet.
     * <p>
     * @param tileset
     * @return TileDraw
     */
    public abstract TileDraw getTileDraw( TileSet tileset );
    
    /**
     * List<URI> of children identifiers.
     * <p>
     * Each uri indicates a valid TileMap that may be aquired from this
     * service.
     * </p>
     * @param info
     * @param monitor
     * @return List<URI>
     */
    public abstract List getTileMapIds( TileServiceInfo info, ProgressListener monitor );
}
