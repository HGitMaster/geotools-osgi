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
package org.geotools.data.gml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.FileDataStoreFactorySpi;

/**
 * <p> 
 * This creates GML DataStores based for the directory provided. By 
 * convention the name of the file x.gml represents the data type x.
 * </p>
 * 
 * @author dzwiers
 * @author adanselm
 * 
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/gml/src/main/java/org/geotools/data/gml/GMLDataStoreFactory.java $
 */
public class GMLDataStoreFactory implements FileDataStoreFactorySpi {

	/** Caches all the instances of datastore created by createDataStore() */
    private Map datastores=new HashMap();
    
	/**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public synchronized DataStore createDataStore(Map params) throws IOException {
    	if( datastores.get(params)==null  ){
    		DataStore ds = createNewDataStore(params);
    		datastores.put(params, ds);
    	}
    	return (DataStore) datastores.get(params);
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        URL url = (URL) URLP.lookUp( params ); // try early error
        boolean retvalue = testURL(url);
        if( retvalue){
        	Integer timeout=(Integer) params.get(TIMEOUT.key);
        	if( timeout==null ){
        		timeout=new Integer(10000);
        	}
        	Integer buffer=(Integer) params.get(TIMEOUT.key);
        	if( buffer==null ){
        		buffer=new Integer(10);
        	}
            try{
            	if( "file".equals(url.getProtocol() ) ){
            		File file=new File(url.getFile());
            		if( file.isDirectory())
            			return new GMLDataStore( new URI(url.getPath()), buffer.intValue(), timeout.intValue() );
            		else
            			return new FileGMLDataStore( new URI(url.getPath()), buffer.intValue(), timeout.intValue() );
            	}else{
        				return new FileGMLDataStore( new URI(url.getPath()), buffer.intValue(), timeout.intValue() );
            	}
            	
            }catch(URISyntaxException e){
                throw new IOException(e.toString());
            }
        }
        throw new IOException( "Provided url:"+url+" was not valid");  
    }

    public String getDisplayName() {
        return "GML";
    }
    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "Read only data store for validating gml 2.x data";
    }

    public static final Param URLP = new Param("url", URL.class,
    "url to a gml file");
    public static final Param TIMEOUT = new Param("timeout", Integer.class,
    "length of time out", false);
    public static final Param BUFFER_SIZEP = new Param("featureBufferSize", Integer.class,
    "Number of features to load into the buffer", false);
    /**
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { URLP, };
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(Map params) {
        if(params != null && params.containsKey("url")){
        	Object url = params.get("url");
    			URL tempurl = null;
    			if (url instanceof URL)
    				tempurl = (URL) url;
    			else if (url instanceof String)
    			  try {
    					tempurl = new URL((String)url);
    				} catch (MalformedURLException e) {
    					return false;
    				}
    			else
    				return false;
    			if(canProcess(tempurl))
    			    return true;
    			
        }
        return false;
        
        //&& params.get("url") instanceof URL 
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getFileExtensions()
     */
    public String[] getFileExtensions() {
        return new String[] {".xml",".gml"};
    }

    /**
     * @throws IOException
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#canProcess(java.net.URL)
     */
    public boolean canProcess(URL f) {
        try {
            return testURL( f );
        } catch (IOException e) {
            return false;
        }
    }
    public boolean testURL( URL f ) throws IOException {
        
        if( "file".equals(f.getProtocol()) ){

    		File file=new File(f.getFile());
    		if( file.isDirectory()){
    			return true;
    		}
            if(f.getFile().toUpperCase().endsWith(".XML")){
                return true;
            }
            if(f.getFile().toUpperCase().endsWith(".GML")){
                return true;            
            }
            throw new IOException("*.xml or *.gml file required");
        }
        if( "http".equals(f.getProtocol()) ){
            URLConnection conn = f.openConnection();
            if( "text/xml".equals( conn.getContentType() )){
                return true;
            }
            if( "application/gml".equals( conn.getContentType() )){
                return true;
            }
            throw new IOException("text/xml or application/gml mime type required");
        }
        return false;
    }

    /**
     * @throws IOException
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#createDataStore(java.net.URL)
     */
    public DataStore createDataStore(URL url) throws IOException {        
        Map params=new HashMap();
        params.put(URLP.key, url);
        return createDataStore(params);
    }

    /**
     * @throws IOException
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getTypeName(java.net.URL)
     */
    public String getTypeName(URL url) throws IOException {
        DataStore ds = createDataStore(url);
        String[] names = ds.getTypeNames();
        return ((names==null || names.length==0)?null:names[0]);
    }

    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

}
