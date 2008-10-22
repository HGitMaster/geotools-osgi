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
package org.geotools.gui.swing.misc;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * facilities factory
 * 
 * @author Johann Sorel
 */
public class FacilitiesFactory {

    
    public MapContext[] duplicateContexts(MapContext[] contexts){
        
        if(contexts != null){
            MapContext[] copys = new MapContext[contexts.length];
            
            for(int i=0;i<contexts.length;i++){
                copys[i] = duplicateContext(contexts[i]);
            }
            
            return copys;
        }else{
            return null;
        }
        
    }
    
    public MapContext duplicateContext(MapContext context) {

        if (context != null) {
            DefaultMapContext copycontext = new DefaultMapContext(context.getCoordinateReferenceSystem());
            MapLayer[] layers = duplicateLayers(context.getLayers());
            copycontext.addLayers(  layers );
            copycontext.setTitle(context.getTitle());

            return copycontext;
        } else {
            return null;
        }

    }
    
    public MapLayer[] duplicateLayers(MapLayer[] layers){
        
        if(layers != null){
            MapLayer[] copys = new MapLayer[layers.length];
            
            for(int i=0;i<layers.length;i++){
                copys[i] = duplicateLayer(layers[i]);
            }
            
            return copys;
        }else{
            return null;
        }
        
    }
    
    public MapLayer duplicateLayer(MapLayer layer) {

        if (layer != null) {

            MapLayer copy = new DefaultMapLayer((FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource(), layer.getStyle(), layer.getTitle());
            copy.setQuery(layer.getQuery());
            copy.setVisible(layer.isVisible());

            return copy;
        } else {
            return null;
        }

    }
    
    /**
     * reproject a geometry from the current Mapcontext to layer CRS
     * @param geom
     * @param context provide the CRS of the geometry
     * @param layer provide output CRS
     * @return
     */
    public Geometry projectGeometry(Geometry geom, MapContext context, MapLayer layer) {

        CoordinateReferenceSystem contextCRS = context.getCoordinateReferenceSystem();
        CoordinateReferenceSystem layerCRS = layer.getFeatureSource().getSchema().getCoordinateReferenceSystem();

        if (layerCRS == null) {
            layerCRS = contextCRS;
        }
                
        return projectGeometry(geom, contextCRS, layerCRS);
    }

    /**
     * reproject a geometry from a CRS to another
     * @param geom
     * @param inCRS
     * @param outCRS
     * @return
     */
    public Geometry projectGeometry(Geometry geom, CoordinateReferenceSystem inCRS, CoordinateReferenceSystem outCRS) {
        MathTransform transform = null;

        if (outCRS == null) {
            outCRS = inCRS;
        }


        if (!inCRS.equals(outCRS)) {
            try {
                transform = CRS.findMathTransform(inCRS, outCRS, true);
                geom = JTS.transform(geom, transform);
            } catch (Exception ex) {
                System.out.println("Error using default layer CRS, searching for a close CRS");

                try {
                    Integer epsgId = CRS.lookupEpsgCode(outCRS, true);
                    if (epsgId != null) {
                        System.out.println("Close CRS found, will replace original CRS for convertion");
                        CoordinateReferenceSystem newCRS = CRS.decode("EPSG:" + epsgId);
                        outCRS = newCRS;
                        transform = CRS.findMathTransform(inCRS, outCRS);
                    } else {
                        System.out.println("No close CRS found, will force convert");
                        try {
                            transform = CRS.findMathTransform(inCRS, outCRS, true);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Search Error, no close CRS found, will force convertion");

                    try {
                        transform = CRS.findMathTransform(inCRS, outCRS, true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                ex.printStackTrace();
            }
        }

        return geom;
    }
}
