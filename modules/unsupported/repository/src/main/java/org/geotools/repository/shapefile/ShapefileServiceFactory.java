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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.repository.Catalog;
import org.geotools.repository.Service;
import org.geotools.repository.ServiceFactory;

public class ShapefileServiceFactory implements ServiceFactory {

    private static ShapefileDataStoreFactory shpDSFactory = new ShapefileDataStoreFactory();

    public Service createService(Catalog parent, URI id, Map params) {
        if (params.containsKey(ShapefileDataStoreFactory.URLP.key)) {
            // shapefile ...

            URL url = null;
            if (params.get(ShapefileDataStoreFactory.URLP.key) instanceof URL) {
                url = (URL) params.get(ShapefileDataStoreFactory.URLP.key);
            } else {
                try {
                    String surl = params
                            .get(ShapefileDataStoreFactory.URLP.key).toString();
                    url = (URL) ShapefileDataStoreFactory.URLP.parse(surl);
                    params.put(ShapefileDataStoreFactory.URLP.key, url);
                } catch (Throwable e) {
                    // TODO: log exception
                    return null;
                }
            }
            if (!shpDSFactory.canProcess(url))
                return null;

            if (id == null) {
                try {
                    id = new URI(url.toExternalForm());
                } catch (URISyntaxException e) {
                }

                return new ShapefileService(parent, id, params);
            }

            return new ShapefileService(parent, id, params);
        }

        return null;
    }

    public boolean canProcess(URI uri) {
        try {
            return shpDSFactory.canProcess(uri.toURL());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public Map createParams(URI uri) {
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            return null;
        }

        if (shpDSFactory.canProcess(url)) {
            // shapefile
            HashMap params = new HashMap();
            params.put(ShapefileDataStoreFactory.URLP.key, url);
            return params;
        }
        return null;
    }
}
