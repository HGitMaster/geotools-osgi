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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * 
 * (DataStore class for handling a directory of GML files.
 * Each file should have the extension .gml or .xml and
 * contain the data of one layer (one file - one layer).)
 * ...CURRENTLY this is just one dir - one layer...
 * 
 * The datastore is "Read Only" as it should only be used
 * to open GML files in Geoserver. There are better techniques
 * to write GML file than the one used in this class to read the
 * data. 
 * 
 * @author adanselm
 * @author dzwiers
 * @author jeichar
 * 
 * TODO Check for a cleaner way to do
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/gml/src/main/java/org/geotools/data/gml/GMLDataStore.java $
 */
public class GMLDataStore extends AbstractDataStore {

	protected File directory;
	private URI uri;
	private Map datastores=new HashMap();

	/** the number of features that are cached */
	private int bufferSize;
	/** length of time before the datastore gives up. */
	private int timeout;
	
	/**
	 * New instance
	 * 
	 * @param udir URI to a directory containing GML files.  Only files ending in XML or GML will be processed.
	 * @param featureBufferSize the number of features that are cached
	 * @param timeout length of time before the datastore gives up.
	 */
	public GMLDataStore(URI udir, int featureBufferSize, int timeout){
		super(false); //does not allow writing
		
		File dir = new File(udir.getPath());
		if( !dir.isDirectory() ){
			throw new IllegalArgumentException(dir + " is not a Directory");
		}
		directory = dir;
		this.bufferSize=featureBufferSize;
		this.timeout=timeout;
	}
	
	/** Gets the name of all the layers from the filenames
	 * in the directory.
	 * @see org.geotools.data.DataStore#getTypeNames()
	 */
	public String[] getTypeNames() throws IOException {
		// Create a filter
		FilenameFilter f = new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.endsWith(".gml") || name.endsWith(".xml");
			}
		};
		
		// Get the list of files
		String list[] = directory.list(f);
		
		for(int i=0;i < list.length;i++){
			list[i] = list[i].substring(0, list[i].lastIndexOf('.'));
		}
		
		return list;
	}

	/** Provides access to a FeatureType referenced by a type name.
	 * 
	 * Basically it parses the file using SAX, gets the featureReader
	 * and returns the result of the getFeatureType() method of
	 * the featureReader.
	 * 
	 * @see org.geotools.data.DataStore#getSchema(java.lang.String)
	 */
	public SimpleFeatureType getSchema(String typeName) throws IOException {
		return getFileDataStore(typeName).getSchema();
	}
	
	private synchronized FileGMLDataStore getFileDataStore(String typeName) throws IOException {
		FileGMLDataStore gmlDatastore = (FileGMLDataStore) datastores.get(typeName);
		if( gmlDatastore==null ){
			// dirty temporary way to support multiple extensions
			// I really don't like this kind of "if" forests...
			File file = new File( directory, typeName+".gml");
			if(!file.exists()){
				file = new File(directory, typeName+".xml");
			}
			if(!file.exists())
				throw new IOException("GML file doesn't exist: "+file.getName());
			
			// file to parse
			uri = file.toURI();
			
			gmlDatastore = new FileGMLDataStore(uri, bufferSize, timeout );
			datastores.put(typeName, gmlDatastore);
		}
		return gmlDatastore;
	}

	/* Returns a reference on the featureReader of the current FCBuffer.
	 * 
	 * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
	 */
	protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName)
			throws IOException {
		return getFileDataStore(typeName).getFeatureReader(typeName);
	}
	
	public ReferencedEnvelope getBounds(Query query) throws IOException {
		if( query.getTypeName()==null )
			throw new NullPointerException("TypeName in query may not be null");
		
		return getFileDataStore(query.getTypeName()).getBounds(query);
	}
}
