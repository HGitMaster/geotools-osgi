/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

/**
 * Another Quick hack of a DataRepository as a bridge to the Opperations api.
 * 
 * This time we are storing by FeatureType, not DataStores will be harned in
 * the configuration of this class.
 * 
 * @author Jody Garnett
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/data/FeatureSourceRepository.java $
 */
public class FeatureSourceRepository implements Repository {	    
	
	/** Map of FeatuerSource by dataStoreId:typeName */
    protected SortedMap<String, FeatureSource<?,?>> featuresources = new TreeMap<String, FeatureSource<?,?>>();
    
    /**
     * All FeatureTypes by dataStoreId:typeName 
     */
	public SortedMap<String, FeatureSource<?,?>> getFeatureSources() {
		return Collections.unmodifiableSortedMap( featuresources );
	}
    /**
     * Retrieve prefix set.
     * 
     * @see org.geotools.data.Catalog#getPrefixes()
     * 
     * @return Set of namespace prefixes
     * @throws IOException
     */
    public Set<String> getPrefixes() throws IOException {
    	Set<String> prefix = new HashSet<String>();
    	for( Iterator<FeatureSource<?,?>> i=featuresources.values().iterator(); i.hasNext();){
    		FeatureSource<?,?> fs = i.next();
    		FeatureType schema = fs.getSchema();
    		prefix.add( schema.getName().getNamespaceURI());
    	}
        return prefix;
    }
    private SortedSet<String> typeNames() throws IOException {
    	SortedSet<String> typeNames = new TreeSet<String>();
    	for (Iterator<FeatureSource<?,?>> i = featuresources.values()
                .iterator(); i.hasNext();) {
    		FeatureSource<?,?> fs = i.next();
    		FeatureType schema = fs.getSchema();
    		typeNames.add( schema.getName().getLocalPart() );
    	}
        return typeNames;    	    	
    }
    
    /** Map of dataStores by dataStoreId */
    private Map<String, DataStore> dataStores() {
    	SortedMap<String, DataStore> dataStores = new TreeMap<String, DataStore>();    	
    	for (Map.Entry<String, FeatureSource<?,?>> entry : featuresources.entrySet()) {
    		String key = entry.getKey();
    		String dataStoreId = key.split(":")[0];
    		FeatureSource<?,?> fs = entry.getValue();
    		dataStores.put( dataStoreId, (DataStore) fs.getDataStore() );    		
    	}        
        return dataStores;    	    	
    }
    private SortedMap<String, SimpleFeatureType> types( DataStore ds ) throws IOException {
    	SortedMap<String, SimpleFeatureType> map = new TreeMap<String, SimpleFeatureType>();
    	String typeNames[] = ds.getTypeNames();
    	for( int i=0; i<typeNames.length; i++){
    		try {
    			map.put( typeNames[i], ds.getSchema( typeNames[i]));
    		}
    		catch (IOException ignore ){
    			// ignore broken featureType
    		}
    	}
    	return map;
    }
    
    /**
     * All FeatureTypes by dataStoreId:typeName 
     */
    public SortedMap<String, FeatureSource<?,?>> types() {
    	return new TreeMap<String, FeatureSource<?,?>>( featuresources );    	
    }

    
    /**
     * Implement lockExists.
     * 
     * @see org.geotools.data.Catalog#lockExists(java.lang.String)
     * 
     * @param lockID
     */
    public boolean lockExists(String lockID) {
        if( lockID == null ) return false;
        DataStore store;
        LockingManager lockManager;
                
        for( Iterator<DataStore> i=dataStores().values().iterator(); i.hasNext(); ){
             store = i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
             if( lockManager.exists( lockID ) ){
                 return true;
             }
        }
        return false;
    }
    /**
     * Implement lockRefresh.
     * <p>
     * Currently it is an error if the lockID is not found. Because if
     * we can't find it we cannot refresh it.
     * </p>
     * <p>
     * Since locks are time sensitive it is impossible to check
     * if a lockExists and then be sure it will still exist when you try to
     * refresh it. Nothing we do can protect client code from this fact, they
     * will need to do with the IOException when (not if) this situation
     * occurs.
     * </p>
     * @see org.geotools.data.Catalog#lockRefresh(java.lang.String, org.geotools.data.Transaction)
     * 
     * @param lockID Authorizataion of lock to refresh
     * @param transaction Transaction used to authorize refresh
     * @throws IOException If opperation encounters problems, or lock not found
     * @throws IllegalArgumentException if lockID is <code>null</code>
     */
    public boolean lockRefresh(String lockID, Transaction transaction) throws IOException{
        if( lockID == null ){
            throw new IllegalArgumentException("lockID required");
        }
        if( transaction == null || transaction == Transaction.AUTO_COMMIT ){
            throw new IllegalArgumentException("Tansaction required (with authorization for "+lockID+")");        
        }
        
        DataStore store;
        LockingManager lockManager;
        
        boolean refresh = false;
        for( Iterator<DataStore> i=dataStores().values().iterator(); i.hasNext(); ){
             store = i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
                          
             if( lockManager.release( lockID, transaction )){
                 refresh = true;    
             }                           
        }
        return refresh;        
    }

    /**
     * Implement lockRelease.
     * <p>
     * Currently it is <b>not</b> and error if the lockID is not found, it may
     * have expired. Since locks are time sensitive it is impossible to check
     * if a lockExists and then be sure it will still exist when you try to
     * release it.
     * </p>
     * @see org.geotools.data.Catalog#lockRefresh(java.lang.String, org.geotools.data.Transaction)
     * 
     * @param lockID Authorizataion of lock to refresh
     * @param transaction Transaction used to authorize refresh
     * @throws IOException If opperation encounters problems
     * @throws IllegalArgumentException if lockID is <code>null</code>
     */
    public boolean lockRelease(String lockID, Transaction transaction) throws IOException{
        if( lockID == null ){
            throw new IllegalArgumentException("lockID required");
        }
        if( transaction == null || transaction == Transaction.AUTO_COMMIT ){
            throw new IllegalArgumentException("Tansaction required (with authorization for "+lockID+")");        
        }
    
        DataStore store;
        LockingManager lockManager;
                
        boolean release = false;                
        for( Iterator<DataStore> i=dataStores().values().iterator(); i.hasNext(); ){
             store = i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
         
             if( lockManager.release( lockID, transaction )){
                 release = true;    
             }             
        }
        return release;        
    }

    /**
     * Implement registerDataStore.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.Catalog#registerDataStore(org.geotools.data.DataStore)
     * 
     * @param id
     * @param featureSource
     * 
     * @throws IOException
     */
    public void register(String id, FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) throws IOException {
    	featuresources.put( id+":"+featureSource.getSchema().getTypeName(), featureSource );        
    }

    /**
     * Implement getDataStores.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.Catalog#getDataStores(java.lang.String)
     * 
     * @param id
     */
    public DataStore datastore(String id ) {    	
    	for(Map.Entry<String, FeatureSource<?,?>> entry : featuresources.entrySet()){
    		String key = (String) entry.getKey();
    		String dataStoreId = key.split(":")[0];
    		if( id.equals( dataStoreId )){
    			FeatureSource<?,?> fs = entry.getValue();
    			return (DataStore) fs.getDataStore();
    		}
    	}
        return null;        
    }
    
    /**
     * Access to the set of registered DataStores.
     * <p>
     * The provided Set may not be modified :-)
     * </p>
     * @see org.geotools.data.Catalog#getDataStores(java.lang.String)
     * 
     * 
     */
    public Map<String, DataStore> getDataStores() {
    	return Collections.unmodifiableMap( dataStores() );
    }
    public FeatureSource<?,?> source( String dataStoreId, String typeName ) throws IOException{
    	String typeRef = dataStoreId+":"+typeName;
    	return featuresources.get( typeRef );
    }
}
