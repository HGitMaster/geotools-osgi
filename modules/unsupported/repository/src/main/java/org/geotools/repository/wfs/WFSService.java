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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.WFSServiceInfo;
import org.geotools.repository.AbstractService;
import org.geotools.repository.Catalog;
import org.geotools.repository.ResolveChangeEvent;
import org.geotools.repository.ResolveDelta;
import org.geotools.repository.ServiceInfo;
import org.geotools.repository.defaults.DefaultResolveChangeEvent;
import org.geotools.repository.defaults.DefaultResolveDelta;
import org.geotools.repository.defaults.DefaultServiceInfo;
import org.geotools.util.ProgressListener;
import org.geotools.xml.wfs.WFSSchema;


/**
 * Handle for a WFS service.
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/wfs/WFSService.java $
 */
public class WFSService extends AbstractService {
    private URI uri = null;
    private Map params = null;
    private List members = null;
    private ServiceInfo info = null;
    private Throwable msg = null;
    private WFSDataStore ds = null;

    public WFSService(Catalog parent, URI uri, Map params) {
        super(parent);

        this.uri = uri;
        this.params = params;
    }

    /*
     * Required adaptions:
     * <ul>
     * <li>IServiceInfo.class
     * <li>List.class <IGeoResource>
     * </ul>
     */
    public Object resolve(Class adaptee, ProgressListener monitor)
        throws IOException {
        if (adaptee == null) {
            return null;
        }

        if (adaptee.isAssignableFrom(ServiceInfo.class)) {
            return this.getInfo(monitor);
        }

        if (adaptee.isAssignableFrom(List.class)) {
            return this.members(monitor);
        }

        if (adaptee.isAssignableFrom(WFSDataStore.class)) {
            return getDS();
        }

        return null;
    }

    public boolean canResolve(Class adaptee) {
        if (adaptee == null) {
            return false;
        }

        return (adaptee.isAssignableFrom(ServiceInfo.class)
        || adaptee.isAssignableFrom(List.class)
        || adaptee.isAssignableFrom(WFSDataStore.class));
    }

    public List members(ProgressListener monitor) throws IOException {
        if (members == null) {
            synchronized (getDS()) {
                if (members == null) {
                    members = new LinkedList();

                    String[] typenames = ds.getTypeNames();

                    if (typenames != null) {
                        for (int i = 0; i < typenames.length; i++) {
                            members.add(new WFSGeoResource(this, typenames[i]));
                        }
                    }
                }
            }
        }

        return members;
    }

    public ServiceInfo getInfo(ProgressListener monitor)
        throws IOException {
        getDS(); // load ds

        if ((info == null) && (ds != null)) {
            synchronized (ds) {
                if (info == null) {
                    info = new IServiceWFSInfo(ds);
                }
            }

            Catalog parent = (Catalog) parent(monitor);

            if (parent != null) {
                ResolveDelta delta = new DefaultResolveDelta(this,
                        ResolveDelta.Kind.CHANGED);
                ResolveChangeEvent event = new DefaultResolveChangeEvent(this,
                        ResolveChangeEvent.Type.POST_CHANGE, delta);
                parent.fire(event);
            }
        }

        return info;
    }

    /*
     * @see net.refractions.udig.catalog.IService#getConnectionParams()
     */
    public Map getConnectionParams() {
        return params;
    }

    WFSDataStore getDS() throws IOException {
        if (ds == null) {
            synchronized (WFSDataStore.class) {
                if (ds == null) {
                    WFSDataStoreFactory dsf = new WFSDataStoreFactory();

                    if (dsf.canProcess(params)) {
                        try {
                            ds = (WFSDataStore) dsf.createDataStore(params);
                        } catch (IOException e) {
                            msg = e;
                            throw e;
                        }
                    }
                }
            }

            Catalog parent = (Catalog) parent(null);

            if (parent != null) {
                ResolveDelta delta = new DefaultResolveDelta(this,
                        ResolveDelta.Kind.CHANGED);
                ResolveChangeEvent event = new DefaultResolveChangeEvent(this,
                        ResolveChangeEvent.Type.POST_CHANGE, delta);

                parent.fire(event);
            }
        }

        return ds;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        return (msg != null) ? Status.BROKEN
                             : ((ds == null) ? Status.NOTCONNECTED
                                             : Status.CONNECTED);
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return msg;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URI getIdentifier() {
        return uri;
    }

    private class IServiceWFSInfo extends DefaultServiceInfo {
        private WFSServiceInfo caps = null;

        IServiceWFSInfo(WFSDataStore resource) {
            super();

            try {
                caps = resource.getInfo();
            } catch (Throwable t) {
                t.printStackTrace();
                caps = null;
            }
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getAbstract()
         */
        public String getAbstract() {
            return (caps == null) ? null : caps.getDescription();
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getIcon()
         */
        public Icon getIcon() {
            //TODO: get an icon
            return null;
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getKeywords()
         */
        public String[] getKeywords() {
            Set<String> kws = caps == null? null : caps.getKeywords();
            return (kws == null) ? null : new ArrayList<String>(kws).toArray(new String[kws.size()]);
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getSchema()
         */
        public URI getSchema() {
            return WFSSchema.NAMESPACE;
        }

        public String getDescription() {
            return getIdentifier().toString();
        }

        public URI getSource() {
            return getIdentifier();
        }

        public String getTitle() {
            return (caps == null) ? "BROKEN" : caps.getTitle(); 
        }
    }
}
