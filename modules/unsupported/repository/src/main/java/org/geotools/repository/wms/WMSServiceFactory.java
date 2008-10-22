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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.repository.Catalog;
import org.geotools.repository.Service;
import org.geotools.repository.ServiceFactory;


/**
 * Provides ...TODO summary sentence
 * 
 * <p>
 * TODO Description
 * </p>
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/wms/WMSServiceFactory.java $
 */
public class WMSServiceFactory implements ServiceFactory {
    /**
     * TODO summary sentence for createService ...
     *
     * @param parent DOCUMENT ME!
     * @param id
     * @param params
     *
     * @return x
     */
    public Service createService(Catalog parent, URI id, Map params) {
        if (params == null) {
            return null;
        }

        if ((!params.containsKey(WMSService.WMS_URL_KEY) && (id == null))
                && !params.containsKey(WMSService.WMS_WMS_KEY)) {
            return null; // nope we don't have a WMS_URL_KEY
        }

        if (params.containsKey(WMSService.WMS_URL_KEY)) {
            URL base = null; // base url for service

            if (params.get(WMSService.WMS_URL_KEY) instanceof URL) {
                base = (URL) params.get(WMSService.WMS_URL_KEY); // use provided url for base
            } else {
                try {
                    base = new URL((String) params.get(WMSService.WMS_URL_KEY)); // upcoverting string to url for base
                } catch (MalformedURLException e1) {
                    // log this?
                    e1.printStackTrace();

                    return null;
                }

                params.remove(params.get(WMSService.WMS_URL_KEY));
                params.put(WMSService.WMS_URL_KEY, base);
            }

            // params now has a valid url
            if (id == null) {
                try {
                    id = new URI(base.toExternalForm());
                } catch (URISyntaxException e) {
                    return null;
                }
            }

            return new WMSService(parent, id, params);
        }

        return null;
    }

    public boolean canProcess(URI uri) {
        try {
            return isWMS(uri.toURL());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public Map createParams(URI uri) {
        URL url;

        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            return null;
        }

        if (!isWMS(url)) {
            return null;
        }

        // wms check
        Map params2 = new HashMap();
        params2.put(WMSService.WMS_URL_KEY, url);

        return params2;
    }

    public static boolean isWMS(URL url) {
        if (url == null) {
            return false;
        }

        String PATH = url.getPath();
        String QUERY = url.getQuery();
        String PROTOCOL = url.getProtocol();

        if (PROTOCOL.indexOf("http") == -1) { //$NON-NLS-1$ supports 'https' too.

            return false;
        }

        if ((QUERY != null) && (QUERY.toUpperCase().indexOf("SERVICE=") != -1)) { //$NON-NLS-1$
                                                                                  // we have a service! It better be WMS            

            return QUERY.toUpperCase().indexOf("SERVICE=WMS") != -1; //$NON-NLS-1$
        } else if ((PATH != null)
                && (PATH.toUpperCase().indexOf("GEOSERVER/WMS") != -1)) { //$NON-NLS-1$

            return true;
        }

        return true; // try it anyway
    }
}
