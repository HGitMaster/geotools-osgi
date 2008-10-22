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
package org.geotools.repository.shapefile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.repository.AbstractGeoResource;
import org.geotools.repository.GeoResource;
import org.geotools.repository.GeoResourceInfo;
import org.geotools.repository.Service;
import org.geotools.repository.defaults.DefaultGeoResourceInfo;
import org.geotools.util.ProgressListener;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/shapefile/src/main/java/org/geotools/catalog/shapefile/ShapefileGeoResource.java $
 */
public class ShapefileGeoResource extends AbstractGeoResource {
    private ShapefileService parent;
    private String typeName;
    private Throwable msg;
    private GeoResourceInfo info;
    private FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;

    ShapefileGeoResource(ShapefileService parent, String typeName) {
        this.parent = parent;
        this.typeName = typeName;
    }

    public boolean canResolve(Class adaptee) {
        if (adaptee == null) {
            return false;
        }

        return adaptee.isAssignableFrom(Service.class)
                || adaptee.isAssignableFrom(GeoResourceInfo.class)
                || adaptee.isAssignableFrom(FeatureStore.class)
                || adaptee.isAssignableFrom(FeatureSource.class);
    }

    public Object resolve(Class adaptee, ProgressListener monitor)
            throws IOException {
        if (adaptee == null) {
            return null;
        }

        if (adaptee.isAssignableFrom(Service.class)) {
            return parent;
        }

        if (adaptee.isAssignableFrom(GeoResource.class)) {
            return getInfo(monitor);
        }

        if (adaptee.isAssignableFrom(FeatureStore.class)) {
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = getFeatureSource(monitor);

            if (featureSource instanceof FeatureStore) {
                return featureSource;
            }
        }

        if (adaptee.isAssignableFrom(FeatureSource.class)) {
            return getFeatureSource(monitor);
        }

        return null;
    }

    public GeoResourceInfo getInfo(ProgressListener monitor) throws IOException {
        if (info == null) {
            synchronized (parent.getDataStore(monitor)) {
                if (info == null) {
                    // calculate some meta data based on the feature type
                    SimpleFeatureType type = getFeatureSource(monitor)
                            .getSchema();
                    CoordinateReferenceSystem crs = type.getCoordinateReferenceSystem();
                    String namespace = type.getName().getNamespaceURI();
                    String name = type.getTypeName();
                    String title = name;
                    String description = name;
                    String[] keywords = new String[] { ".shp", "Shapefile",
                            name, namespace };

                    // calculate bounds
                    ReferencedEnvelope bounds = null;

                    try {
                        Envelope tmpBounds = getFeatureSource(monitor)
                                .getBounds();

                        if (tmpBounds instanceof ReferencedEnvelope) {
                            bounds = (ReferencedEnvelope) tmpBounds;
                        } else {
                            bounds = new ReferencedEnvelope(tmpBounds, crs);
                        }

                        if (bounds == null) {
                            bounds = new ReferencedEnvelope(new Envelope(), crs);
                            FeatureIterator<SimpleFeature> reader = getFeatureSource(monitor)
                                    .getFeatures().features();
                            try {
                                while (reader.hasNext()) {
                                    SimpleFeature element = reader.next();

                                    if (bounds.isNull()) {
                                        bounds.init((Envelope) element
                                                .getBounds());
                                    } else {
                                        bounds.include(element.getBounds());
                                    }
                                }
                            } finally {
                                reader.close();
                            }
                        }
                    } catch (Exception e) {
                        // something bad happend, return an i dont know
                        bounds = new ReferencedEnvelope(new Envelope(), crs);
                    }
                    URI uri;
                    try {
                        uri = new URI(namespace);
                        ;
                    } catch (URISyntaxException e) {
                        uri = null;
                    }
                    info = new DefaultGeoResourceInfo(title, name, description,
                            uri, bounds, crs, keywords, null);
                }
            }
        }

        return info;
    }

    protected FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(ProgressListener monitor)
            throws IOException {
        if (featureSource == null) {
            synchronized (parent.getDataStore(monitor)) {
                if (featureSource == null) {
                    try {
                        msg = null;

                        DataStore dataStore = parent.getDataStore(monitor);

                        if (dataStore != null) {
                            featureSource = dataStore.getFeatureSource(typeName);
                        }
                    } catch (IOException ioe) {
                        msg = ioe;
                        throw ioe;
                    } catch (Throwable t) {
                        msg = t;
                        throw (IOException) new IOException().initCause(t);
                    }
                }
            }
        }

        return featureSource;
    }

    public Status getStatus() {
        if (msg == null) {
            if (featureSource != null) {
                return Status.CONNECTED;
            }

            return Status.NOTCONNECTED;
        }

        return Status.BROKEN;
    }

    public Throwable getMessage() {
        return msg;
    }

    public URI getIdentifier() {
        URI uri = parent.getIdentifier();

        if (uri != null) {
            try {
                return new URI(uri.toString() + "#" + typeName);
            } catch (URISyntaxException e) {
                return null;
            }
        }

        return null;
    }
}
