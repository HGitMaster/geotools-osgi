/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.repository.wms;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.ows.GetCapabilitiesRequest;
import org.geotools.data.ows.GetCapabilitiesResponse;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.Specification;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMS1_0_0;
import org.geotools.data.wms.WMS1_1_0;
import org.geotools.data.wms.WMS1_1_1;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetFeatureInfoResponse;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.ows.ServiceException;
import org.geotools.repository.AbstractService;
import org.geotools.repository.Catalog;
import org.geotools.repository.ResolveChangeEvent;
import org.geotools.repository.ResolveDelta;
import org.geotools.repository.ServiceInfo;
import org.geotools.repository.defaults.DefaultResolveChangeEvent;
import org.geotools.repository.defaults.DefaultResolveDelta;
import org.geotools.repository.defaults.DefaultServiceInfo;
import org.geotools.util.ProgressListener;
import org.xml.sax.SAXException;


/**
 * Connect to a WMS.
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/wms/WMSService.java $
 */
public class WMSService extends AbstractService {
    /**
     * <code>WMS_URL_KEY</code> field Magic param key for Catalog WMS
     * persistence.
     */
    public static final String WMS_URL_KEY = "net.refractions.udig.catalog.internal.wms.WMSServiceImpl.WMS_URL_KEY"; //$NON-NLS-1$
    public static final String WMS_WMS_KEY = "net.refractions.udig.catalog.internal.wms.WMSServiceImpl.WMS_WMS_KEY"; //$NON-NLS-1$
    private Map params;
    private Throwable error;
    private URI uri;
    private WebMapServer wms = null;
    private WMSServiceInfo info;
    private List members = null;

    /**
     * Construct <code>WMSServiceImpl</code>.
     *
     * @param parent
     * @param uri DOCUMENT ME!
     * @param params
     */
    public WMSService(Catalog parent, URI uri, Map params) {
        super(parent);
        this.params = params;
        this.uri = uri;

        if (params.containsKey(WMS_WMS_KEY)) {
            Object obj = params.get(WMS_WMS_KEY);

            if (obj instanceof WebMapServer) {
                this.wms = (WebMapServer) obj;
            }
        }
    }

    public Status getStatus() {
        return (error != null) ? Status.BROKEN
                               : ((wms == null) ? Status.NOTCONNECTED
                                                : Status.CONNECTED);
    }

    /**
     * Aquire the actual geotools WebMapServer instance.
     * 
     * <p>
     * Note this method is blocking and throws an IOException to indicate such.
     * </p>
     *
     * @param theUserIsWatching
     *
     * @return WebMapServer instance
     *
     * @throws IOException
     */
    protected WebMapServer getWMS(ProgressListener theUserIsWatching)
        throws IOException {
        if (wms == null) {
            try {
                URL url1 = (URL) getConnectionParams().get(WMS_URL_KEY);
                wms = new CustomWMS(url1);
            } catch (IOException persived) {
                error = persived;
                throw persived;
            } catch (Throwable nak) {
                IOException broken = new IOException(
                        "Could not connect to WMS. Possible reason: "
                        + nak.getLocalizedMessage());

                broken.initCause(nak);
                error = broken;
                throw broken;
            }

            ResolveDelta delta = new DefaultResolveDelta(this,
                    ResolveDelta.Kind.CHANGED);
            parent(theUserIsWatching)
                .fire(new DefaultResolveChangeEvent(this,
                    ResolveChangeEvent.Type.POST_CHANGE, delta));
        }

        return wms;
    }

    public ServiceInfo getInfo(ProgressListener monitor)
        throws IOException {
        if (info == null) {
            synchronized (getWMS(monitor)) {
                if (info == null) {
                    info = new WMSServiceInfo(monitor);

                    ResolveDelta delta = new DefaultResolveDelta(this,
                            ResolveDelta.Kind.CHANGED);
                    parent(monitor)
                        .fire(new DefaultResolveChangeEvent(this,
                            ResolveChangeEvent.Type.POST_CHANGE, delta));
                }
            }
        }

        return info;
    }

    /*
     * @see net.refractions.udig.catalog.IService#resolve(java.lang.Class, org.eclipse.core.runtime.IProgressMonitor)
     */
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

        if (adaptee.isAssignableFrom(WebMapServer.class)) {
            return getWMS(monitor);
        }

        return null;
    }

    /**
     * @see net.refractions.udig.catalog.IService#getConnectionParams()
     */
    public Map getConnectionParams() {
        return params;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public boolean canResolve(Class adaptee) {
        if (adaptee == null) {
            return false;
        }

        if (adaptee.isAssignableFrom(WebMapServer.class)
                || adaptee.isAssignableFrom(ServiceInfo.class)
                || adaptee.isAssignableFrom(List.class)) {
            return true;
        }

        return false;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.IProgressMonitor)
     */
    public List members(ProgressListener monitor) throws IOException {
        if (members == null) {
            synchronized (getWMS(monitor)) {
                if (members == null) {
                    getWMS(monitor); // load ds
                    members = new LinkedList();

                    Layer[] layers = WMSUtils.getNamedLayers(getWMS(monitor)
                                                                 .getCapabilities());

                    /*
                     * Retrieved no layers from the WMS - something is wrong,
                     * either the WMS doesn't work, or it has no named layers.
                     */
                    if (layers != null) {
                        for (int i = 0; i < layers.length; i++) {
                            /*
                             * suppress layers that have children
                             * TODO some people might not like this behavior
                             *      maybe we should make a preference for it.
                             * TODO should add hasChildren() to geotools
                             */
                            if (layers[i].getChildren().length == 0) {
                                Layer layer = layers[i];
                                members.add(new WMSGeoResource(this, layer));
                            }
                        }
                    }
                }
            }
        }

        return members;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return error;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URI getIdentifier() {
        return uri;
    }

    class WMSServiceInfo extends DefaultServiceInfo {
        private WMSCapabilities caps = null;

        WMSServiceInfo(ProgressListener monitor) {
            try {
                caps = getWMS(monitor).getCapabilities();
            } catch (Throwable t) {
                t.printStackTrace();
                caps = null;
            }

            keywords = (caps == null) ? null
                                      : ((caps.getService() == null) ? null
                                                                     : caps.getService()
                                                                           .getKeywordList());

            String[] t;

            if (keywords == null) {
                t = new String[2];
            } else {
                t = new String[keywords.length + 2];
                System.arraycopy(keywords, 0, t, 2, keywords.length);
            }

            t[0] = "WMS"; //$NON-NLS-1$
            t[1] = getIdentifier().toString();
            keywords = t;
        }

        public String getAbstract() {
            return (caps == null) ? null
                                  : ((caps.getService() == null) ? null
                                                                 : caps.getService()
                                                                       .get_abstract());
        }

        public String getDescription() {
            return getIdentifier().toString();
        }

        public URI getSchema() {
            return WMSSchema.NAMESPACE;
        }

        public URI getSource() {
            return getIdentifier();
        }

        public String getTitle() {
            return ((caps == null) || (caps.getService() == null))
            ? ((getIdentifier() == null) ? "BROKEN" //$NON-NLS-1$
                                         : getIdentifier().toString())
            : caps.getService().getTitle();
        }
    }

    public class CustomWMS extends WebMapServer {
        /**
         * DOCUMENT ME!
         *
         * @param serverURL
         *
         * @throws IOException
         * @throws ServiceException
         * @throws SAXException
         */
        public CustomWMS(URL serverURL)
            throws IOException, ServiceException, SAXException {
            super(serverURL);

            if (getCapabilities() == null) {
                throw new IOException("Unable to parse capabilities document."); //$NON-NLS-1$
            }
        }

        public GetCapabilitiesResponse issueRequest(GetCapabilitiesRequest arg0)
            throws IOException, ServiceException {
            return super.issueRequest(arg0);
        }

        public GetFeatureInfoResponse issueRequest(GetFeatureInfoRequest arg0)
            throws IOException, ServiceException {
            return super.issueRequest(arg0);
        }

        public GetMapResponse issueRequest(GetMapRequest arg0)
            throws IOException, ServiceException {
            return super.issueRequest(arg0);
        }

        protected void setupSpecifications() {
            specs = new Specification[3];
            specs[0] = new WMS1_0_0();
            specs[1] = new WMS1_1_0();
            specs[2] = new WMS1_1_1();
        }
    }
}
