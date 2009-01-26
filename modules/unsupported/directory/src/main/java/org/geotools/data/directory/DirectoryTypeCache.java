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

package org.geotools.data.directory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.util.logging.Logging;

/**
 * <p>Handles all of the data stores that a directory data store relies onto,
 * centralizing the gathering and caching policies and code.</p>
 * <p>The class is completely thread safe</p>
 * 
 * @author Andrea Aime - OpenGeo
 */
class DirectoryTypeCache {
    static final Logger LOGGER = Logging.getLogger(DirectoryTypeCache.class);

    Set<String> featureTypes = new HashSet<String>();

    Map<String, FileEntry> ftCache = new ConcurrentHashMap<String, FileEntry>();

    File directory;

    URI namespaceURI;

    DirectoryWatcher watcher;
    
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Builds a new cache.
     * 
     * @param directory
     *            a non null File pointing to an existing directory
     * @throws IOException
     */
    DirectoryTypeCache(File directory, URI namespaceURI) throws IOException {
        // some basic checks
        if (directory == null)
            throw new NullPointerException(
                    "Directory parameter should be not null");

        if (!directory.exists()) {
            throw new IllegalArgumentException(
                    "Specified directory does not exists: "
                            + directory.getAbsolutePath());
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(
                    "Specified path is not a directory, it'a s file instead: "
                            + directory.getAbsolutePath());
        }
        
        this.directory = directory;
        this.namespaceURI = namespaceURI;

        this.watcher = new ImmediateDirectoryWatcher(directory);
    }

    /**
     * Returns the data store containing a specific feature type, or null if not
     * found
     * 
     * @param typeName
     * @param forceUpdate If true, it will force the update
     * @return
     */
    DataStore getDataStore(String typeName, boolean forceUpdate) throws IOException {
        lock.readLock().lock();
        try {
            if(forceUpdate)
                updateCache();
            // TODO: check about re-creating the datastore when the cache
            // is turned into a soft map
            return ftCache.get(typeName).store;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Returns all the type names known
     * @return
     */
    Set<String> getTypeNames() throws IOException {
        lock.readLock().lock();
        
        try {
            updateCache();
            return ftCache.keySet();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Returns all active data stores available in the cache. 
     * Won't force the creation of a new data store if it has been disposed of and
     * it's currently not needed for the functionality of the whole thing
     */
    List<DataStore> getDataStores() {
        List<DataStore> stores = new ArrayList<DataStore>();
        lock.readLock().lock();
        
        try {
            for (FileEntry entry : ftCache.values()) {
                // TODO: remember to avoid creating a data store when the cache
                // is moved to a lazy
                stores.add(entry.store);
            }
        } finally {
            lock.readLock().unlock();
        }
        
        return stores;
    }
    
    /**
     * Checks if the feature type cache contents needs updating, does so in case.
     * The code assumes the caller already owns a read only lock that needs upgrading
     * in case the information is stale.
     */
    private void updateCache() throws IOException {
        if(watcher.isStale()) {
            // upgrade lock so that we have exclusive access to ftCache
            lock.readLock().unlock();
            lock.writeLock().lock();
            
            // still stale?
            if(watcher.isStale()) {
                watcher.mark();
                refreshCacheContents();
            }
            
            // downgrade lock
            lock.readLock().lock();
            lock.writeLock().unlock();
        }
    }

    /**
     * Here we try to refresh the contents of the feature type cache.
     * <p>
     * Basically we want to:
     * <ul>
     * <li>remove all data stores associated to files that have been removed</li>
     * <li>add all data stores associated to new files</li>
     * <li>remove all feature types that are no more there, and add all feature
     * types that are new
     * <li>
     * </ul>
     * All of this should be done trying to avoid re-creating all of the
     * datastores already loaded. We assume a properly written datastore will be
     * able to detect changes in its own feature type list and feature type
     * schemas on its own.
     * 
     * @throws IOException
     */
    void refreshCacheContents() throws IOException {
        // prepare the replacement ft cache
        Map<String, FileEntry> result = new TreeMap<String, FileEntry>();

        // build support structure used to quickly find files that need updating
        Map<File, FileEntry> fileCache = new HashMap<File, FileEntry>();
        Set<FileEntry> dirCache = new HashSet<FileEntry>();
        for (FileEntry entry : ftCache.values()) {
            if(!entry.file.isDirectory())
                fileCache.put(entry.file, entry);
            else
                dirCache.add(entry);
        }
        
        // grab all the factories that can look like file data store ones
        List<FactoryAdapter> factories = lookupFileDataStores();
        LOGGER.log(Level.FINE, "Found the following file capable factories: {0}", factories);
        
        // grab all the files we have to check and scan them with a 
        // reduction algorithm, each time you find what a file is for, 
        // remove it from the list
        TreeSet<File> files = new TreeSet<File>(Arrays.asList(directory
                .listFiles(new DirectoryFilter())));
        
        // first scan the factories that can deal with the whole directory (there
        // may be more than one for the same root directory, we cannot assume
        // one factory per file)
        for (FactoryAdapter factoryAdapter : factories) {
            DataStore store = factoryAdapter.getStore(directory, namespaceURI);
            if(store != null) {
                // do we have the same datastore in the current cache?
                FileEntry entry = null;
                for (FileEntry cachedEntry : dirCache) {
                    if(cachedEntry.store.getClass().equals(store.getClass())) {
                        entry = cachedEntry;
                        break;
                    }
                }
                
                // if not found, then create a new one
                if(entry == null) {
                    entry = new FileEntry(directory, store);
                }
                
                // if we have an entry, then grab all the feature types in it
                // and build the corresponding entries in the ft cache
                if (entry != null) {
                    for (String typeName : entry.store.getTypeNames()) {
                        // don't override existing entries
                        if (!result.containsKey(typeName))
                            result.put(typeName, entry);
                        else {
                            LOGGER.log(Level.WARNING, "Type name " + typeName
                                    + " is available from multiple datastores");
                        }
                    }

                    // TODO: remember to remove other files that have been grabbed
                    // by a certain feature type when the FileDataStore interface is up
                }
            }
        }

        // the scan each file assuming there is only one datastore capable of handling it
        while (!files.isEmpty()) {
            File curr = files.first();

            // cache check
            FileEntry entry = fileCache.get(curr);
            if (entry == null) {
                // look for datastore
                DataStore store = null;
                for (FactoryAdapter factoryAdapter : factories) {
                    store = factoryAdapter.getStore(curr, namespaceURI);
                    if (store != null)
                        break;
                }
                if (store != null) {
                    entry = new FileEntry(curr, store);
                }
            }

            // if we have an entry, then grab all the feature types in it
            // and build the corresponding entries in the ft cache
            if (entry != null) {
                for (String typeName : entry.store.getTypeNames()) {
                    // don't override existing entries
                    if (!result.containsKey(typeName))
                        result.put(typeName, entry);
                    else {
                        LOGGER.log(Level.WARNING, "Type name " + typeName
                                + " is available from multiple datastores");
                    }
                }

                // TODO: remember to remove other files that have been grabbed
                // by a certain feature type when the FileDataStore interface is up
            } else {
                LOGGER.log(Level.FINE, "Could not find any datastore able to process {0}", curr);
            }

            // move on
            files.remove(curr);
            continue;
        }
        
        // update the cache. We need to remove the missing elements, disposing
        // the data stores that are not referenced anymore, and add the new ones
        // we are going to update the ftCache as we go, this is thread safe
        // since we are using a concurrent hash map for ftCache, and won't
        // hinder users of live data stores since we are not going to touch
        // the ones that are not being removed (the ones that we are going to
        // remove should be not working anyways)
        Set<String> removed = new HashSet<String>(ftCache.keySet());
        removed.removeAll(result.keySet());
        
        // collect all data stores that are referred by a feature type that we 
        // are going to remove, but are not referred by any feature type we're
        // going to keep. Clean the ftCache from removed feature types at the same
        // time.
        Set<DataStore> disposable = new HashSet<DataStore>(); 
        for (String removedFT : removed) {
            FileEntry entry = ftCache.get(removedFT);
            disposable.add(entry.store);
            ftCache.remove(removedFT);
        }
        for (FileEntry entry : result.values()) {
            disposable.remove(entry.store);
        }
        for (DataStore dataStore : disposable) {
            dataStore.dispose();
        }
        
        // now let's add all the new ones
        Set<String> added = new HashSet<String>(result.keySet());
        added.removeAll(ftCache.keySet());
        for (String newFeatureType : added) {
            ftCache.put(newFeatureType, result.get(newFeatureType));
        }
    }

    /**
     * Looks up in the registry data store factories that do look like file data
     * store ones, that is, they accept a File/URL and a namespace, and returns
     * an adapter that can be used to build a datastore given a File and a
     * namespace.
     */
    List<FactoryAdapter> lookupFileDataStores() {
        List<FactoryAdapter> adapters = new ArrayList<FactoryAdapter>();

        // look for factories that do accept a file/url and a namespace
        Iterator<DataStoreFactorySpi> it = DataStoreFinder.getAllDataStores();
        while(it.hasNext()) {
            DataStoreFactorySpi factory = it.next();
            Param[] params = factory.getParametersInfo();
            
            if(params == null) {
                LOGGER.fine("DataStore factory " + factory + " returns null from getParametersInfo!");
                continue;
            }
            
            // avoid self loop
            if(factory instanceof DirectoryDataStoreFactory)
                continue;
            
            Param fileParam = null;
            Param nsParam = null;
            for (Param param : params) {
                Class<?> type = param.type;
                String key = param.key;
                if (File.class.isAssignableFrom(type)
                        || URL.class.isAssignableFrom(type))
                    fileParam = param;
                else if (key.equalsIgnoreCase("namespace")
                        && (String.class.isAssignableFrom(type) || URI.class
                                .isAssignableFrom(type)))
                    nsParam = param;
            }
    
            if (fileParam != null) {
                adapters.add(new FactoryAdapter(factory, fileParam, nsParam));
            }
        }
        return adapters;
    }
    
    /**
     * Disposes of the file cache and all the cached data stores
     */
    void dispose() {
        // first collect all data stores to have a unique list
        // of all the ones around
        Set<DataStore> stores = new HashSet<DataStore>();
        for (FileEntry entry : ftCache.values()) {
            // TODO: don't force re-creation when we  
            // switch to lazyly handled data stores
            stores.add(entry.store);
        }
        // now dispose each one just once
        for (DataStore dataStore : stores) {
            dataStore.dispose();
        }
    }

    /**
     * Excludes directories from a file listing
     * 
     * @author Administrator
     * 
     */
    class DirectoryFilter implements FileFilter {

        public boolean accept(File pathname) {
            return !pathname.isDirectory();
        }

    }

    class FileEntry {
        File file;

        DataStore store;

        long lastModified;

        public FileEntry(File file, DataStore store) {
            this.file = file;
            this.store = store;
            lastModified = file.lastModified();
        }
    }
}
