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

import java.io.IOException;
import java.net.URI;

import javax.net.ssl.SSLEngineResult$Status;

import org.geotools.util.ProgressListener;

public class TileMapResource extends AbstractGeoResource {
    
    public TileMap tileMap(){
        return null;
    }
    
    public GeoResourceInfo getInfo( ProgressListener monitor ) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object resolve( Class adaptee, ProgressListener monitor ) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean canResolve( Class adaptee ) {
        // TODO Auto-generated method stub
        return false;
    }

    public URI getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    public Status getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

}
