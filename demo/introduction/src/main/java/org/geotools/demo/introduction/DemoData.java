/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.introduction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.repository.Catalog;
import org.geotools.repository.defaults.DefaultCatalog;

/**
 * A demo class illustrating some of the various parts of the geotools api.
 * 
 * @author Adrian Custer, gnuGIS
 * 
 * @version 0.03
 * @since   2.3-M0
 *
 */
public class DemoData {

	
    /**
     * The List of Features used to store data outside the localCatalog.
     */
    List theFeatureList;
//    List <Feature> theFeatureList;
    
    /**
     * The List of FeatureCollections used to store data outside the localCatalog.
     */
    List  theFeatureCollectionList;
//    List <FeatureCollection> theFeatureCollectionList;
    
	/**
	 * The List of DataStores used to store data outside the localCatalog.
	 */
	List theDataStoreList;
//    List <DataStore> theDataStoreList;
	
    /**
     * The local Catalog used to store the services pointing to data in data stores.
     */
    Catalog localCatalog;
    
    /**
     * A Network Catalog used to store the services pointing to data on the network.
     */
    Catalog aNetworkCatalog;
    
    /**
     * The Map of Styles used to render the different layers stored as:
     *   String name, Style s.
     */
    Map theStyleMap;
    
	/**
	 * Creates the demo class and an underlying catalog for storing data.
	 */
	public DemoData() {
        theFeatureList = new LinkedList();
        theFeatureCollectionList = new LinkedList();
        theDataStoreList = new LinkedList();
		localCatalog = new DefaultCatalog();
        aNetworkCatalog = new DefaultCatalog();//TODO: How is this done?
        theStyleMap = new HashMap();
	}
	
	/**
	 * @return The List used to store data as Features.
	 */
	public List getFeaturenList() {
		return theFeatureList;
	}
	
    /**
     * @return The List used to store data in FeatureCollections.
     */
    public List getFeatureCollectionList() {
        return theFeatureCollectionList;
    }
    
    /**
     * @return The List used to store data in DataStores.
     */
    public List getDataStoreList() {
        return theDataStoreList;
    }
    
    /**
     * @return The catalog used to store data.
     */
    public Catalog getLocalCatalog() {
        return localCatalog;
    }


    
}
