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
package org.geotools.repository.wfs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.ows.FeatureSetDescription;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.repository.AbstractGeoResource;
import org.geotools.repository.Catalog;
import org.geotools.repository.GeoResource;
import org.geotools.repository.GeoResourceInfo;
import org.geotools.repository.ResolveChangeEvent;
import org.geotools.repository.ResolveDelta;
import org.geotools.repository.Service;
import org.geotools.repository.defaults.DefaultGeoResourceInfo;
import org.geotools.repository.defaults.DefaultResolveChangeEvent;
import org.geotools.repository.defaults.DefaultResolveDelta;
import org.geotools.util.ProgressListener;
import org.geotools.wfs.v_1_0_0.data.WFS_1_0_0_DataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Access a feature type in a wfs.
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/wfs/WFSGeoResource.java $
 */
public class WFSGeoResource extends AbstractGeoResource {
    WFSService parent;
    String typename = null;
    private GeoResourceInfo info;

    private WFSGeoResource() { /*not for use*/
    }

    /**
     * Construct <code>WFSGeoResourceImpl</code>.
     *
     * @param parent
     * @param typename
     */
    public WFSGeoResource(WFSService parent, String typename) {
        this.parent = parent;
        this.typename = typename;
    }

    public URI getIdentifier() {
        try {
            return new URI(parent.getIdentifier().toString() + "#" + typename);
        } catch (URISyntaxException e) {
            return parent.getIdentifier();
        }
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatus()
     */
    public Status getStatus() {
        return parent.getStatus();
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatusMessage()
     */
    public Throwable getMessage() {
        return parent.getMessage();
    }

    /*
     * Required adaptions:
     * <ul>
     * <li>IGeoResourceInfo.class
     * <li>IService.class
     * </ul>
     */
    public Object resolve(Class adaptee, ProgressListener monitor)
        throws IOException {
        if (adaptee == null) {
            return null;
        }

        if (adaptee.isAssignableFrom(Service.class)) {
            return parent;
        }

        if (adaptee.isAssignableFrom(WFS_1_0_0_DataStore.class)) {
            return parent.resolve(adaptee, monitor);
        }

        if (adaptee.isAssignableFrom(GeoResource.class)) {
            return this;
        }

        if (adaptee.isAssignableFrom(GeoResourceInfo.class)) {
            return getInfo(monitor);
        }

        if (adaptee.isAssignableFrom(FeatureStore.class)) {
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = parent.getDS().getFeatureSource(typename);

            if (fs instanceof FeatureStore) {
                return fs;
            }

            if (adaptee.isAssignableFrom(FeatureSource.class)) {
                return parent.getDS().getFeatureSource(typename);
            }
        }

        return null;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public boolean canResolve(Class adaptee) {
        if (adaptee == null) {
            return false;
        }

        return (adaptee.isAssignableFrom(GeoResourceInfo.class)
        || adaptee.isAssignableFrom(FeatureStore.class)
        || adaptee.isAssignableFrom(FeatureSource.class)
        || adaptee.isAssignableFrom(WFS_1_0_0_DataStore.class)
        || adaptee.isAssignableFrom(Service.class));
    }

    public GeoResourceInfo getInfo(ProgressListener monitor)
        throws IOException {
        if ((info == null) && (getStatus() != Status.BROKEN)) {
            synchronized (parent.getDS()) {
                if (info == null) {
                    info = new IGeoResourceWFSInfo();
                }
            }

            ResolveDelta delta = new DefaultResolveDelta(this,
                    ResolveDelta.Kind.CHANGED);
            ResolveChangeEvent event = new DefaultResolveChangeEvent(this,
                    ResolveChangeEvent.Type.POST_CHANGE, delta);

            ((Catalog) parent(monitor).parent(monitor)).fire(event);
        }

        return info;
    }

    class IGeoResourceWFSInfo extends DefaultGeoResourceInfo {
        CoordinateReferenceSystem crs = null;

        IGeoResourceWFSInfo() throws IOException {
            SimpleFeatureType ft = parent.getDS().getSchema(typename);

            List fts = parent.getDS().getCapabilities().getFeatureTypes();
            FeatureSetDescription fsd = null;

            if (fts != null) {
                Iterator i = fts.iterator();

                while (i.hasNext() && (fsd == null)) {
                    FeatureSetDescription t = (FeatureSetDescription) i.next();

                    if ((t != null) && typename.equals(t.getName())) {
                        fsd = t;
                    }
                }
            }

            bounds = new ReferencedEnvelope(fsd.getLatLongBoundingBox(),
                    DefaultGeographicCRS.WGS84);
            description = fsd.getAbstract();

            try {
                crs = ft.getGeometryDescriptor().getCoordinateReferenceSystem();
            } catch (Exception e) {
                crs = DefaultGeographicCRS.WGS84;
            }

            name = typename;
            try {
                schema = new URI( ft.getName().getNamespaceURI());
            } catch (URISyntaxException e) {
                schema = null;
            }
            title = fsd.getTitle();

            keywords = new String[] {
                "wfs", //$NON-NLS-1$
                typename,
                ft.getName().getNamespaceURI()
            };
        }

        /*
         * @see net.refractions.udig.catalog.IGeoResourceInfo#getCRS()
         */
        public CoordinateReferenceSystem getCRS() {
            if (crs != null) {
                return crs;
            }

            return super.getCRS();
        }
    }
}
