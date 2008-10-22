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
import java.util.Collections;
import java.util.List;

import org.geotools.repository.GeoResource;
import org.geotools.repository.GeoResourceInfo;
import org.geotools.repository.Resolve;
import org.geotools.repository.ResolveChangeEvent;
import org.geotools.repository.ResolveChangeListener;
import org.geotools.repository.Service;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;

public class TileResource implements GeoResource {
    private TileService parent;
    private URI id;
    private TileMapInfo info;
    private TileMap tileMap;
    
    public TileResource( TileService service, URI id ) {
        parent = service;
        this.id = id;        
    }
    
    public synchronized GeoResourceInfo getInfo( ProgressListener monitor ) throws IOException {        
        return parent.getServer( monitor ).getTileMapInfo(id);
    }
    /**
     * This is the resource the handle is pointing to.
     *
     * @param monitor
     * @return
     * @throws IOException
     */
    public synchronized TileMap getTileMap( ProgressListener monitor ) throws IOException {        
        return parent.getServer( monitor ).getTileMap(id);
    }

    public Object resolve( Class adaptee, ProgressListener monitor ) throws IOException {
        if( adaptee == TileMap.class ){
            return getTileMap( monitor );
        }
        if( Service.class.isAssignableFrom( adaptee )){
            return parent( monitor ); 
        }
        if( GeoResourceInfo.class.isAssignableFrom( adaptee )){
            return getInfo( monitor ); 
        }
        return null;
    }

    public boolean canResolve( Class adaptee ) {
        return Service.class.isAssignableFrom( adaptee ) ||
               GeoResourceInfo.class.isAssignableFrom( adaptee ) ||
               adaptee == TileMap.class;
    }

    public URI getIdentifier() {
        return id;
    }

    public Throwable getMessage() {
        return null;
    }

    public Status getStatus() {
        if( tileMap == null ) return Status.NOTCONNECTED;
        return Status.CONNECTED;
    }

    public List members( ProgressListener arg0 ) throws IOException {
        return Collections.EMPTY_LIST;
    }

    public Resolve parent( ProgressListener monitor) throws IOException {
        if( monitor == null ) monitor = new NullProgressListener();
        try {
            return parent;
        }
        finally {
            monitor.complete();
        }
    }
    
    public void fire( ResolveChangeEvent arg0 ) {
    }

    public void addListener( ResolveChangeListener arg0 ) throws UnsupportedOperationException {
    }

    public void removeListener( ResolveChangeListener arg0 ) {
    }
}
