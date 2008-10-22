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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.repository.AbstractService;
import org.geotools.repository.Catalog;
import org.geotools.repository.ServiceInfo;
import org.geotools.repository.defaults.DefaultServiceInfo;
import org.geotools.util.ProgressListener;

/**
 * Implementation of a service handle for shapefiles.
 * 
 * @see org.geotools.gtcatalog.Service
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/shapefile/src/main/java/org/geotools/catalog/shapefile/ShapefileService.java $
 */
public class ShapefileService extends AbstractService {
    private ServiceInfo info;
    private ShapefileDataStore dataStore;
    private Throwable msg;
    private URI uri;
    private Map params;
    private List members;
    private static ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

    public ShapefileService(Catalog parent, URI uri, Map params) {
        super(parent);
        this.params = params;
        this.uri = uri;
    }

    public boolean canResolve(Class adaptee) {
        if (adaptee == null) {
            return false;
        }

        return adaptee.isAssignableFrom(ServiceInfo.class)
                || adaptee.isAssignableFrom(List.class)
                || adaptee.isAssignableFrom(ShapefileDataStore.class);
    }

    public Object resolve(Class adaptee, ProgressListener monitor)
            throws IOException {
        if (adaptee == null) {
            return null;
        }

        if (adaptee.isAssignableFrom(ServiceInfo.class)) {
            return getInfo(monitor);
        }

        if (adaptee.isAssignableFrom(List.class)) {
            return members(monitor);
        }

        if (adaptee.isAssignableFrom(ShapefileDataStore.class)) {
            return getDataStore(monitor);
        }

        return null;
    }

    public ServiceInfo getInfo(ProgressListener monitor) throws IOException {
        if (info == null) {
            synchronized (getDataStore(monitor)) {
                if (info == null) {
                    String title = getIdentifier().getPath();
                    String description = getIdentifier().toString();
                    String[] keywords = new String[] { ".shp", "Shapefile",
                            getDataStore(monitor).getTypeNames()[0] };

                    info = new DefaultServiceInfo(title, description, null,
                            null, null, null, keywords, null);
                }
            }
        }

        return info;
    }

    protected ShapefileDataStore getDataStore(ProgressListener monitor)
            throws IOException {
        if (dataStore == null) {
            synchronized (ShapefileDataStore.class) {
                if (dataStore == null) {
                    if (factory.canProcess(params)) {
                        try {
                            msg = null;
                            dataStore = (ShapefileDataStore) factory
                                    .createDataStore(params);
                        } catch (IOException io) {
                            msg = io; // save message for later
                            throw io;
                        } catch (Throwable t) {
                            msg = t; // save error to report back later
                            throw (IOException) new IOException().initCause(t);
                        }
                    }
                }
            }
        }
        return dataStore;
    }

    public List members(ProgressListener monitor) throws IOException {
        if (members == null) {
            synchronized (getDataStore(monitor)) {
                if (members == null) {
                    members = new LinkedList();

                    String[] typenames = getDataStore(monitor).getTypeNames();

                    if (typenames != null) {
                        for (int i = 0; i < typenames.length; i++) {
                            members.add(new ShapefileGeoResource(this,
                                    typenames[i]));
                        }
                    }
                }
            }
        }

        return members;
    }

    public Map getConnectionParams() {
        return params;
    }

    public Status getStatus() {
        if (msg == null) {
            if (dataStore != null) {
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
        return uri;
    }
}
