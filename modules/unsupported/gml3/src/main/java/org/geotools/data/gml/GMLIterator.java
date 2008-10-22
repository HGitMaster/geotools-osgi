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
package org.geotools.data.gml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.geotools.xml.StreamingParser;
import org.opengis.feature.simple.SimpleFeature;

public class GMLIterator implements Iterator {

	/**
	 * type entry
	 */
	GMLTypeEntry entry;
	/**
	 * The parser + input
	 */
	InputStream input;
	StreamingParser parser;
	
	/**
	 * The next feature 
	 */
	SimpleFeature feature;
	
	GMLIterator( GMLTypeEntry entry ) throws IOException {

		this.entry = entry;
		
		try {
			input = entry.parent().document();
			parser = new StreamingParser( 
				entry.parent().configuration(), input, "//" + entry.getTypeName()  
			);
		} 
		catch( Exception e ) {
			throw (IOException) new IOException().initCause( e );
		}
	}
	
	public Object next() {
		return feature;
	}

	public boolean hasNext() {
		feature = (SimpleFeature) parser.parse();
		return feature != null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public void close() throws IOException {
		input.close();
		input = null;
		parser = null;
		feature = null;
	}
}
