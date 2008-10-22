/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.gpx;

import java.io.IOException;
import java.util.ListIterator;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.gpx.GPX;
import org.geotools.gpx.GPXConfiguration;
import org.geotools.gpx.bean.RteType;
import org.geotools.gpx.bean.TrkType;
import org.geotools.gpx.bean.WptType;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class GpxFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

    private final String typeName;
    private final GpxDataStore dataStore;
    private final Transaction transaction;
    private final FeatureTranslator translator;
    
    private Object currentBean;
    private SimpleFeature currentFeature;
    private ListIterator beans;
    
    
    public GpxFeatureWriter(GpxDataStore dataStore, String typeName, Transaction transaction) {
        
        this.dataStore = dataStore;
        this.typeName = typeName;
        this.transaction = transaction;
        
        dataStore.getMemoryLock().acquireWriteLock();
        
        if(GpxDataStore.TYPE_NAME_POINT.equals(typeName))
            beans = dataStore.getGpxData().getWpt().listIterator();
        else if(GpxDataStore.TYPE_NAME_TRACK.equals(typeName))
            beans = dataStore.getGpxData().getTrk().listIterator();
        else if(GpxDataStore.TYPE_NAME_ROUTE.equals(typeName))
            beans = dataStore.getGpxData().getRte().listIterator();
        else
            throw new IllegalArgumentException("Illegal featureType: " + typeName);
        
        translator = new FeatureTranslator(dataStore.getSchema(typeName));
    }

    public synchronized void close() throws IOException {
        
        if(beans == null) {
            // this indicates, that we were already closed
            GpxDataStore.LOGGER.fine("GpxFeatureWriter.close(): called second time.");
            return;
        }
        
        try {
            // TODO write lock for the file
            
            // serialize file
            GPXConfiguration configuration = new GPXConfiguration();
            
            Encoder encoder = new Encoder(configuration);
            encoder.setNamespaceAware(false);
            encoder.encode(dataStore.getGpxData(), GPX.gpx, System.out);
            
            // TODO drop file lock
    
        } finally {
            // we have to drop memory lock even if serializing fails
            dataStore.getMemoryLock().releaseWriteLock();
            beans = null;
        }
    }

    public SimpleFeatureType getFeatureType() {
        return dataStore.getSchema(typeName);
    }

    public boolean hasNext() throws IOException {
        currentBean = null;
        currentFeature = null;
        return beans.hasNext();
    }

    public SimpleFeature next() throws IOException {
        
        if(!beans.hasNext()) {
            // new element
            if(GpxDataStore.TYPE_NAME_POINT.equals(typeName))
                currentFeature = translator.convertFeature(new WptType());
            else if(GpxDataStore.TYPE_NAME_TRACK.equals(typeName))
                currentFeature = translator.convertFeature(new TrkType());
            else if(GpxDataStore.TYPE_NAME_ROUTE.equals(typeName))
                currentFeature = translator.convertFeature(new RteType());
            else
                throw new IllegalArgumentException("Illegal featureType: " + typeName);
            
            return currentFeature;
            
        } else {
            // next element
            
            currentBean = beans.next();
            
            if(GpxDataStore.TYPE_NAME_POINT.equals(typeName))
                currentFeature = translator.convertFeature((WptType) currentBean);
            else if(GpxDataStore.TYPE_NAME_TRACK.equals(typeName))
                currentFeature = translator.convertFeature((TrkType) currentBean);
            else if(GpxDataStore.TYPE_NAME_ROUTE.equals(typeName))
                currentFeature = translator.convertFeature((RteType) currentBean);
            else
                throw new IllegalArgumentException("Illegal featureType: " + typeName);
            
            return currentFeature;
        }
    }

    public void remove() throws IOException {
        if(currentBean != null) {
            beans.remove();
            dataStore.listenerManager.fireFeaturesRemoved(typeName, transaction, null, false);
        }
        
        currentBean = null;
        currentFeature = null;
    }

    public void write() throws IOException {
        if(currentBean != null && currentFeature!= null) {
            // feature modification may occured
            boolean changed = false;
            if(GpxDataStore.TYPE_NAME_POINT.equals(typeName))
                changed = translator.convertFeature(currentFeature, (WptType) currentBean);
            else if(GpxDataStore.TYPE_NAME_TRACK.equals(typeName))
                changed = translator.convertFeature(currentFeature, (TrkType) currentBean);
            else if(GpxDataStore.TYPE_NAME_ROUTE.equals(typeName))
                changed = translator.convertFeature(currentFeature, (RteType) currentBean);
            else
                throw new IllegalArgumentException("Unknown feature type: " + typeName);
            
            if(changed)
                dataStore.listenerManager.fireFeaturesChanged(typeName, transaction, null, false);
        } else if(currentBean == null && currentFeature != null) {
            // saving a new feature instance
            if(GpxDataStore.TYPE_NAME_POINT.equals(typeName)) {
                WptType point = translator.createWpt(currentFeature);
                beans.add(point);
            } else if(GpxDataStore.TYPE_NAME_TRACK.equals(typeName)) {
                TrkType line = translator.createTrk(currentFeature);
                beans.add(line);
            } else if(GpxDataStore.TYPE_NAME_ROUTE.equals(typeName)) {
                RteType area = translator.createRte(currentFeature);
                beans.add(area);
            } else {
                throw new IllegalArgumentException("Unknown feature type: " + typeName);
            }

            dataStore.listenerManager.fireFeaturesAdded(typeName, transaction, null, false);
            
        }
    }

}
