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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.tile.cache.TileRange;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Represents tiled content avaialble at a range of zoom levels.
 * <p>
 * Each zoom level has a corrasponding TileSet.
 * </p>
 * @author jgarnett
 */
public final class TileMap {
    private TileMapInfo info;
    private Map map; // from ZoomLevel to TileSet
    private TileServer server;
    
    public TileMap( TileServer server, TileMapInfo info ){
        this.server = server;
        this.info = info;
        this.map = new HashMap();            
    }
 
    CoordinateReferenceSystem getCRS(){
        return info.getCRS();
    }
    public TileMapInfo getInfo(){
        return info;
    }

    public synchronized TileSet getTileSet( ZoomLevel zoom ){
        if( map.containsKey( zoom )){
            return (TileSet) map.get( zoom );
        }                
        TileSet tileset = new TileSet( server, this, zoom );               
        map.put( zoom, tileset );
        return tileset;
    }
    
    public String toString() {
        if( info != null ){
            return "TileMap ("+info.getName()+")";
        }
        return "TileMap no data";
    }
    
    ZoomLevel getAppropriateScale( double scaleDenominator ){
        // Sorted set so we can stop when our scaleDenominator is exceeded
        int level = 0;
        
        //System.out.println("ScaleDenominator:"+(long)scaleDenominator);        
        //System.out.println("Scale X:"+(int)(1000000000.0/scaleDenominator));
        
        ZoomLevel zoomLevel= null;
        for( Iterator i=info.getZoomLevels().iterator(); i.hasNext(); level++){
            zoomLevel = (ZoomLevel) i.next();                        
            double scale = zoomLevel.getScaleDenominator();
            //System.out.println("Scale "+level+":"+(int)(1000000000.0/scale));                        
            if( scaleDenominator >= scale ) {
                //System.out.println( "===> level "+level+" ie:"+zoomLevel);                
                return zoomLevel;                
            }            
        }
        //System.out.println( "---> level "+(level-1)+" ie:"+zoomLevel);                        
        return zoomLevel; // most detailed level we have
    }
    /**
     * Retrive data from the most appropriate TileSet for the provided scaleDenominator.
     * <p>
     * This supports "free form" zooming, the resulting TileSet will need resampling
     * to match your display.
     * <p>
     * This method will try and choose a tile range from a tile set at a greater
     * level of details then the scaleDenominator provided. This setting
     * works perfectly for raster data; text labels or line work will suffer. As
     * a work around you can "back off" your scaleDenominator a bit and work
     * with a pixelated result.
     * </p>
     * 
     * @param zoom
     * @return TileSet
     */
    public TileRange getTileRange( Envelope bounds, double scaleDenominator ) {
        ZoomLevel bestFit = getAppropriateScale( scaleDenominator );                                        
        if( bestFit == null ) return TileRange.EMPTY;
        
        // TODO confirm behavior of scaledenominator
        //bestFit = (ZoomLevel) info.getZoomLevels().iterator().next();
        
        TileSet tileSet = getTileSet( bestFit );
        return tileSet.getTileRange( bounds );
    }
}
