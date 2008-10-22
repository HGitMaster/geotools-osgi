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

import org.geotools.repository.CatalogInfo;


/**
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/defaults/DefaultCatalogInfo.java $
 */
public class DefaultCatalogInfo implements CatalogInfo {
    protected String title;
    protected String description;
    protected URI source;
    protected String[] keywords;

    public DefaultCatalogInfo() {
        // for sub-classes
    }

    public DefaultCatalogInfo(String title, String description, URI source,
        String[] keywords) {
        this.title = title;
        this.description = description;
        this.source = source;
        this.keywords = keywords;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.CatalogInfo#getTitle()
     */
    public String getTitle() {
        return title;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.CatalogInfo#getKeywords()
     */
    public String[] getKeywords() { // aka Subject

        return keywords;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.CatalogInfo#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.geotools.catalog.CatalogInfo#getSource()
     */
    public URI getSource() { // aka server

        return source;
    }

    /**
     * DOCUMENT ME!
     *
     * @param desc The desc to set.
     */
    public void setDesc(String desc) {
        this.description = desc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param keywords The keywords to set.
     */
    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param source The source to set.
     */
    public void setSource(URI source) {
        this.source = source;
    }

    /**
     * DOCUMENT ME!
     *
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
