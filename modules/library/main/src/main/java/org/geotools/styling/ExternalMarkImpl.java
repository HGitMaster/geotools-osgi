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
package org.geotools.styling;


import javax.swing.Icon;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.ExternalMark;
import org.opengis.style.StyleVisitor;
import org.opengis.util.Cloneable;


/**
 * Default implementation of ExternalMark.
 * 
 * @source $URL: http://svn.geotools.org/trunk/modules/library/main/src/main/java/org/geotools/styling/MarkImpl.java $
 * @version $Id: MarkImpl.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public class ExternalMarkImpl implements ExternalMark {

    private OnLineResource onlineResource;
    private Icon inlineContent;
    private int index;
    private String format;

    public ExternalMarkImpl() {
        
    }
    
    public ExternalMarkImpl(Icon icon) {
        this.inlineContent = icon;
        this.index = -1;
        this.onlineResource = null;
        this.format = null;
    }

    public ExternalMarkImpl(OnLineResource resource, String format, int markIndex) {
        this.inlineContent = null;
        this.index = markIndex;
        this.onlineResource = resource;
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public Icon getInlineContent() {
        return inlineContent;
    }

    public int getMarkIndex() {
        return index;
    }

    public OnLineResource getOnlineResource() {
        return onlineResource;
    }

    public Object accept(StyleVisitor visitor, Object extraData) {
        return visitor.visit( this, extraData );
    }

}
