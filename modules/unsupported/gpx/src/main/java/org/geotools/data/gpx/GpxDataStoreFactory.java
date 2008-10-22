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
package org.geotools.data.gpx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.xml.sax.SAXException;


public class GpxDataStoreFactory implements FileDataStoreFactorySpi {
    public static final Param URLP = new Param("url", URL.class, "url to a .gpx file");
    public static final Param NAMESPACEP = new Param("namespace", String.class, "uri to a the namespace", false);
    
    private Map liveStores = new HashMap();

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#canProcess(java.net.URL)
     */
    public boolean canProcess(URL f) {
        return f.getFile().toUpperCase().endsWith(".GPX");
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#createDataStore(java.net.URL)
     */
    public DataStore createDataStore(URL url) throws IOException {
        Map params = new HashMap();
        params.put(URLP.key, url);

        return createDataStore(params);
    }

    public String[] getFileExtensions() {
        return new String[] { ".gpx", };
    }

    /*
     * (non-Javadoc)
     * @see org.geotools.data.FileDataStoreFactorySpi#getTypeName(java.net.URL)
     *
     * I don't really understand, what this method is for.  What should I return? "WGS84"?
     * Or some "EPSG:xxx"? It's always wgs84, so wouldn't be hard to implement,
     * just I don't understand what this method is for.
     */
    public String getTypeName(URL arg0) throws IOException {
        throw new UnsupportedOperationException("hoped not required");
    }

    /**
     * Takes a list of params which describes how to access a restore and
     * determins if it can be read by the Shapefile Datastore.
     *
     * @param params A set of params describing the location of a restore.
     *        Files should be pointed to by a 'url' param.
     *
     * @return true iff params contains a url param which points to a file
     *         ending in shp
     */
    public boolean canProcess(Map params) {
        boolean accept = false;

        if (params.containsKey(URLP.key)) {
            try {
                URL url = (URL) URLP.lookUp(params);
                accept = canProcess(url);
            } catch (IOException ioe) {
                // yes, I am eating this
            }
        }

        return accept;
    }

    public DataStore createDataStore(Map params) throws IOException {
        DataStore ds = null;

        if (!liveStores.containsKey(params)) {
            URL url = null;

            try {
                ds = createNewDataStore(params);
                liveStores.put(params, ds);
            } catch (MalformedURLException mue) {
                throw new DataSourceException("Unable to attatch datastore to " + url, mue);
            }
        } else {
            ds = (DataStore) liveStores.get(params);
        }

        return ds;
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        DataStore ds = null;

        URL url = null;

        try {
            url = (URL) URLP.lookUp(params);

            String namespace = (String) NAMESPACEP.lookUp(params);
            ds = new GpxDataStore(url, namespace);
            
        } catch (MalformedURLException e) {
            throw new DataSourceException("Unable to attatch datastore to " + url, e);
        } catch (SAXException e) {
            throw new DataSourceException("Unable to attatch datastore to " + url, e);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException("Unable to attatch datastore to " + url, e);
        } catch (URISyntaxException e) {
            throw new DataSourceException("Unable to attatch datastore to " + url, e);
        }

        return ds;
    }

    public String getDescription() {
        return "GPS Exchange data files (*.gpx)";
    }

    public String getDisplayName() {
        return "Gpx file";
    }

    public Param[] getParametersInfo() {
        return new Param[] { URLP };
    }

    public boolean isAvailable() {
        return true;
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
