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
package org.geotools.feature;


/**
 * Interface to be implemented by all listeners of CollectionEvents.
 *
 * @author Ray Gallagher
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/api/src/main/java/org/geotools/feature/CollectionListener.java $
 * @version $Id: CollectionListener.java 30642 2008-06-12 17:52:06Z acuster $
 */
public interface CollectionListener {
    /**
     * Gets called when a CollectionEvent is fired. Typically fired to signify
     * that a change has occurred in the collection.
     *
     * @param tce The CollectionEvent
     */
    void collectionChanged(CollectionEvent tce);
}
