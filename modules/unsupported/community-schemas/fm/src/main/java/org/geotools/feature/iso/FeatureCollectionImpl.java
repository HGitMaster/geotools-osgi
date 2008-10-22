/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.feature.iso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.feature.iso.collection.AbstractFeatureCollection;
import org.geotools.feature.iso.collection.AbstractResourceCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureCollectionType;

/**
 * This is is a sample FeatureCollection implementation.
 * <p>
 * If you are a user - yes this FeatureCollection can be used to hold content in
 * memory.
 * <p>
 * <p>
 * Note/Rant to data providers:
 * <p>
 * If you are a data provider (implementing a DataStore?) please don't use this
 * class, you should be doing your own thing.
 * </p>
 * As an example of doing your own thing, JDBC DataStore should be making a
 * JDBCFeatureCollection that just maintains a Filter defining the contents.
 * Until such time as content is accessed for the first time; at which point the
 * collection can be relized by fetching a ResultSet. If possible (for a paged
 * result set) this may be cached for subsequent access.
 * <p>
 * Note that in a good implementation FeatureCollection will form a "chain" that
 * is grounded by a FeatureStore that holds the Transaction etc... It is up to
 * the implementor to decide what to do when an FeatureCollection in the chain
 * actually relizes content? Collections downstream should be able to work off
 * the FeatureCollection that is already relized. Note additional API may be
 * used, as the FeatureCollection directly creates subCollections that act views
 * on the origional content.
 * </p>
 * If this is too complicated (aka you don't want to break out data mining
 * techniques) please consider working with the following - A three tiered
 * approach with different assumptions at each level:
 * <ol>
 * <li>Level 1 - All <br>
 * Example:<code>FeatureStore.getFeatures()</code>
 * <ul>
 * <li>represents all the content, assume this cannot fit into memory.
 * <li>don't cache unless high latency w/ modification notification or
 * timestamp available (aka WFS)
 * <li>use metadata for aggregate function results if available (bounds, count)
 * </ul>
 * <li>Level 2 - Collection <br>
 * Example:<code>FeatureStore.getFeatures().getSubCollection( Filter )</code>
 * <br>
 * Example:<code>FeatureStore.getFeatures( Filter )</code>
 * <ul>
 * <li>- represents the results of a query, may cache
 * <li>- consider cache result of aggregate functions
 * <li>- consider cache data (database resultset, local hsql cache, whatever)
 * <li>- consider cache in memory (for small count)
 * </ul>
 * <li>Level 3 - Transient <br>
 * Example:<code>FeatureStore.getFeatures().getSubCollection( Filter ).getSubCollection( Filter )</code>
 * <br>
 * Example:<code>FeatureCollection.getSubCollection( Filter )</code>
 * <ul>
 * <li>temporary collection (used to hold a Filter for subsequent opperation
 * and cut down on API) <br>
 * <b>Example:</b><code>collection.getSubCollection( Filter ).remove()</code>
 * <li>don't cache result, see above use
 * <li>if getSubCollection( Filter) then they are breaking out assumption, take
 * appropriate action. <br>
 * <b>Appropriate Action:</b?construct a Level 2 collection, and wrap it (aka
 * switch over to delegation), and provide the client code with another Level 3
 * <li>
 * </ul>
 * </ul>
 * The above breakdown would be a good breakdown of abstract classes for
 * implementors to work against. However even if this is provided, there is no
 * excuse not to do the right thing for your datasource. And for a data source
 * the right thing is never to burn memory.
 * </p>
 * 
 * @author Jody Garnett
 */
public class FeatureCollectionImpl extends AbstractFeatureCollection
		implements FeatureCollection {

	List/*<Feature>*/ features;

	Set/*<String>*/ fids = new HashSet();

	public FeatureCollectionImpl(Collection values, AttributeDescriptor desc, String id) {
		super(values,desc,id);
		features = new ArrayList/*<Feature>*/();
	}

	public FeatureCollectionImpl(Collection values, FeatureCollectionType type, String id) {
		super(values,type,id);
		features = new ArrayList/*<Feature>*/();
	}
	
	/**
	 * Implements Collection.size()
	 */
	public int size() {
		return features.size();
	}

	/**
	 * Implements {@link AbstractResourceCollection#openIterator()}
	 */
	protected Iterator openIterator() {
		return features.iterator();
	}
	
	/**
	 * Implements {@link AbstractResourceCollection#closeIterator(Iterator)}
	 */
	protected void closeIterator(Iterator close) {
		//noop
	}
	
	/**
	 * Implemens Collection.add(E)
	 * 
	 * @param f
	 * @return
	 */
	public boolean add(Object/*Feature*/ object) {
		Feature f = (Feature)object;
		boolean added = false;
		if (f != null && !fids.contains(f.getID())) {
			added = features.add(f);
			if (added) {
				fids.add(f.getID());
			}
		}
		return added;
	}

	public void clear() {
		features.clear();
		fids.clear();
	}

	public boolean remove(Object/*Feature*/ object) {
		Feature f = (Feature) object;
		boolean removed = features.remove(f);
		if (removed) {
			fids.remove(f.getID());
		}
		return removed;
	}

	public boolean removeAll(Collection/*<?>*/ c) {
		boolean changed = features.removeAll(c);
		if (changed) {
			for (Iterator itr = c.iterator(); itr.hasNext();) {
				Object o = itr.next();
				if (o instanceof Feature)
					fids.remove(((Feature) o).getID());
			}
		}
		return changed;
	}
}
