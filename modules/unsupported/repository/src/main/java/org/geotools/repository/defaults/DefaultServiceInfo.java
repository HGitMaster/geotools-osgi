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

import org.geotools.repository.ServiceInfo;

/**
 * Implementation of ServiceInfo.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/defaults/DefaultServiceInfo.java $
 */
public class DefaultServiceInfo implements ServiceInfo {

    protected String title, description, _abstract;
    protected URI schema;
    protected URI source, publisher;
    protected String[] keywords;
    protected Icon icon;

    protected DefaultServiceInfo() {
        // to be used in an over-ride
    }

    public DefaultServiceInfo( String title, String description, String _abstract, URI source,
            URI publisher, URI schema, String[] keywords, Icon icon ) {
        this.title = title;
        this.description = description;
        this._abstract = _abstract;
        this.schema = schema;
        this.source = source;
        this.publisher = publisher;
        this.keywords = keywords;
        this.icon = icon;
    }

    /**
     * Returns the service title, may be empty or null if unsupported.
     * <p>
     * Note this is always metadata, and is in user terms.
     * </p>
     * 
     * @return title, may be empty, null if unsupported.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
		this.title = title;
	}
	
    /**
     * Returns the service keywords. Maps to the Dublin Core Subject element.
     * 
     */
    public String[] getKeywords() { // aka Subject
        return keywords;
    }

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	/**
     * Returns the service description
     * 
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
		this.description = description;
	}
    
    /**
     * Return the service abstract
     * 
     */
    public String getAbstract() {
        return _abstract;
    }

    public void setAbstract(String _abstract) {
		this._abstract = _abstract;
	}

    /**
     * Return the service publisher
     * 
     */
    public URI getPublisher() {
        return publisher;
    }

    public void setPublisher(URI publisher) {
		this.publisher = publisher;
	}
    
    /**
     * Returns the xml schema namespace for this service type. Maps to the Dublin Code Format
     * element
     * 
     */
    public URI getSchema() { // aka format
        return schema;
    }

    public void setSchema(URI schema) {
		this.schema = schema;
	}
    
    /**
     * Returns the service source. Maps to the Dublin Core Server Element
     * 
     */
    public URI getSource() { // aka server
        return source;
    }
    
	public void setSource(URI source) {
		this.source = source;
	}

    /**
     * Base symbology (with out decorators) representing this IService.
     * <p>
     * The Icon returned should conform the the Eclipse User Interface Guidelines (16x16
     * image with a 16x15 glyph centered).
     * </p>
     * <p>
     * This plug-in provides default images based on service type:
     * 
     * <pre><code>
     *  &lt;b&gt;return&lt;/b&gt; ISharedImages.getImagesDescriptor( IService );
     * </code></pre>
     * 
     * <ul>
     * <p>
     * Any LabelProvider should use the default image, a label decorator should be used to pick up
     * these images in a separate thread. This allows services like WFS make blocking request to
     * pick up the image from their GetCapabilities.
     * </p>
     * 
     * @return Icon symbolizing this IService.
     */
    public Icon getIcon() {
        return icon;
    }


	public void setIcon(Icon icon) {
		this.icon = icon;
	}

}
