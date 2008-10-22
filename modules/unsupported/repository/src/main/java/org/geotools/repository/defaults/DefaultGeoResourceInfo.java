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
package org.geotools.repository.defaults;

import java.net.URI;

import javax.swing.Icon;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.repository.GeoResourceInfo;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Implementation of GeoResourceInfo.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/defaults/DefaultGeoResourceInfo.java $
 */
public class DefaultGeoResourceInfo implements GeoResourceInfo {
    protected String title;
    protected String description;
    protected String name;
    protected String[] keywords;
    protected URI schema;
    protected Icon icon;
    protected ReferencedEnvelope bounds;

    protected DefaultGeoResourceInfo() {
        // for over-riding
    }

    public DefaultGeoResourceInfo(String title, String name,
        String description, URI schema, Envelope bounds,
        CoordinateReferenceSystem crs, String[] keywords, Icon icon) {
    		this( title, name, description, schema, new ReferencedEnvelope( bounds,crs ), keywords, icon );
    }
    
    public DefaultGeoResourceInfo( String title, String name, String description, 
    		URI schema, ReferencedEnvelope bounds, String[] keywords, Icon icon
	) {
    		this.title = title;
        this.description = description;
        this.name = name;
        this.keywords = keywords;
        this.schema = schema;
        this.icon = icon;
        this.bounds = bounds;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getTitle()
     */
    public String getTitle() {
        return title;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getKeywords()
     */
    public String[] getKeywords() { // aka Subject

        return keywords;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getSchema()
     */
    public URI getSchema() { // aka namespace

        return schema;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getName()
     */
    public String getName() { // aka layer/type name

        return name;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getIcon()
     */
    public Icon getIcon() {
        return icon;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getBounds()
     */
    public Envelope getBounds() { // part of Coverage

        return bounds;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.GeoResourceInfo#getCRS()
     */
    public CoordinateReferenceSystem getCRS() { // part of Coverage

        return bounds.getCoordinateReferenceSystem();
    }
}
