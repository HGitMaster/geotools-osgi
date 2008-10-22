/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.map.map2d.stream.strategy;

import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapBoundsListener;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A MapContext with only one layer
 * 
 * @author Johann Sorel
 */
final class OneLayerContext implements MapContext {

    private MapLayer[] layer = new MapLayer[1];
    private CoordinateReferenceSystem crs = null;
    private ReferencedEnvelope areaOfInterest = null;
    private ReferencedEnvelope bounds = null;

    OneLayerContext() {
        this.crs = DefaultGeographicCRS.WGS84;
    }

    public boolean addLayer(MapLayer layer) {
        this.layer[0] = layer;
        return true;
    }

    public boolean removeLayer(MapLayer layer) {
        return true;
    }

    public MapLayer removeLayer(int index) {
        return null;
    }

    public void clearLayerList() {
        layer[0] = null;
    }

    public MapLayer[] getLayers() {
        return layer;
    }

    public MapLayer getLayer(int index) throws IndexOutOfBoundsException {
        return layer[0];
    }

    public Iterator iterator() {
        return null;
    }

    public int indexOf(MapLayer layer) {
        return 0;
    }

    public int getLayerCount() {
        return 1;
    }

    public ReferencedEnvelope getLayerBounds() throws IOException {

        ReferencedEnvelope env;
        CoordinateReferenceSystem sourceCrs;

        env = layer[0].getBounds();
        sourceCrs = env.getCoordinateReferenceSystem();

        if (env != null) {
            try {
                if ((sourceCrs != null) && (crs != null) && !CRS.equalsIgnoreMetadata(sourceCrs, crs)) {
                    env = env.transform(crs, true);
                }
            } catch (FactoryException e) {
                e.printStackTrace();
            } catch (TransformException e) {
                e.printStackTrace();
            }

        }

        return env;
    }

    public void setAreaOfInterest(Envelope areaOfInterest, CoordinateReferenceSystem coordinateReferenceSystem) throws IllegalArgumentException {
        if ((areaOfInterest == null) || (coordinateReferenceSystem == null)) {
            throw new IllegalArgumentException("Input arguments cannot be null");
        }

        final Envelope oldAreaOfInterest = this.areaOfInterest;
        final CoordinateReferenceSystem oldCrs = this.crs;

        this.areaOfInterest = new ReferencedEnvelope(areaOfInterest, coordinateReferenceSystem);
        this.crs = coordinateReferenceSystem;
        // force computation of bounds next time someone asks them
        bounds = null;

    }

    public void setAreaOfInterest(Envelope areaOfInterest) throws IllegalArgumentException {
        if (areaOfInterest == null) {
            throw new IllegalArgumentException("Input argument cannot be null");
        }

        final Envelope oldAreaOfInterest = this.areaOfInterest;
        // this is a bad guess, I use the context crs, hopint that it is going
		// to be the right one
        this.areaOfInterest = new ReferencedEnvelope(areaOfInterest, this.crs);
        System.out.println("USing a deprecated method!");
    }

    public void setAreaOfInterest(ReferencedEnvelope areaOfInterest) {
        if (areaOfInterest == null) {
            throw new IllegalArgumentException("Input argument cannot be null");
        }

        final CoordinateReferenceSystem tempCRS = areaOfInterest.getCoordinateReferenceSystem();
        if (tempCRS == null) {
            throw new IllegalArgumentException("CRS of the provided AOI cannot be null");
        }
        this.crs = tempCRS;
        this.areaOfInterest = new ReferencedEnvelope((Envelope) areaOfInterest, this.crs);
        // force computation of bounds next time someone asks them
        bounds = null;
    }

    public ReferencedEnvelope getAreaOfInterest() {
        if (areaOfInterest == null) {
            try {
                final Envelope e = getLayerBounds();
                if (e != null) {
                    areaOfInterest = new ReferencedEnvelope(e, this.crs);
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (areaOfInterest == null) {
            return null;
        } else {
            return this.areaOfInterest;
        }
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public void setCoordinateReferenceSystem(CoordinateReferenceSystem crs) throws TransformException, FactoryException {
        if (crs == null) {
            throw new IllegalArgumentException("Input argument cannot be null");
        }

        this.bounds = null;
        if (!CRS.equalsIgnoreMetadata(crs, this.crs) && this.areaOfInterest != null) {
            this.areaOfInterest = this.areaOfInterest.transform(crs, true);
        }
        this.crs = crs;
        this.bounds = null;
    }

    public void transform(AffineTransform transform) {
        double[] coords = new double[4];
        coords[0] = areaOfInterest.getMinX();
        coords[1] = areaOfInterest.getMinY();
        coords[2] = areaOfInterest.getMaxX();
        coords[3] = areaOfInterest.getMaxY();
        transform.transform(coords, 0, coords, 0, 2);
        this.areaOfInterest = new ReferencedEnvelope(coords[0], coords[2], coords[1], coords[3], this.crs);
    }

    public void moveLayer(int sourcePosition, int destPosition) {
    }

    public int addLayers(MapLayer[] layers) {
        return 0;
    }

    public void removeLayers(MapLayer[] layers) {
    }

    public boolean addLayer(int index, MapLayer layer) {
        return true;
    }

    public void addLayer(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource, Style style) {
    }

    public void addLayer(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, Style style) {
    }

    public void addLayer(Collection collection, Style style) {
    }

    public void addLayer(GridCoverage gridCoverage, Style style) {
    }

    public void addLayer(AbstractGridCoverage2DReader gridCoverage, Style style) {
    }

    public void addMapLayerListListener(MapLayerListListener listener) {
    }

    public void removeMapLayerListListener(MapLayerListListener listener) {
    }

    public void addMapBoundsListener(MapBoundsListener listener) {
    }

    public void removeMapBoundsListener(MapBoundsListener listener) {
    }

    public String getAbstract() {
        return "";
    }

    public void setAbstract(String conAbstract) {
    }

    public String getContactInformation() {
        return "";
    }

    public void setContactInformation(String contactInformation) {
    }

    public String[] getKeywords() {
        return new String[0];
    }

    public void setKeywords(String[] keywords) {
    }

    public String getTitle() {
        return "";
    }

    public void setTitle(String title) {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}
