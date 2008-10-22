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
package org.geotools.repository;

import java.net.URI;

import javax.swing.Icon;


/**
 * Provides metadata information about a service.
 * <p>
 * Information is provided in the form of a single, simple, Java bean.
 * You can treat this bean as a "view" on more complete metadata information
 * that may be accessable via a subclass (or other resolve target). This
 * bean offers up service metadata information to the the GeoTools catalog
 * implementations for searching.
 * </p>
 * <p>
 * Much of the names and motivation have been taken from Dublin Code
 * and it's application profile for RDF.
 * </p>
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/ServiceInfo.java $
 */
public interface ServiceInfo {
    /**
     * Returns the service title, may be empty or null if unsupported.
     * <p>
     * Note this is always metadata, and is in user terms.
     * </p>
     *
     * @return title, may be empty, null if unsupported.
     */
    String getTitle();

    /**
     * Returns the service keywords. Maps to the Dublin Core Subject element.
     *
     */
    String[] getKeywords();

    /**
     * Returns the service description.
     *
     * This use is understood to be in agreement with "dublin-core",
     * implementors may use either abstract or description as needed.
     * <p>
     * Dublin Core:
     * <quote>
     * A textual description of the content of the resource, including
     * abstracts in the case of document-like objects or content
     * descriptions in the case of visual resources.
     * </quote>
     *
     * When providing actual dublin-core metadata you can gather up
     * all the description information into a single string for
     * searching.
     *
     * @return Description of visual contents
     */
    String getDescription();

    /**
     * Return the service abstract.
     *
     * This use is understood to be in agreement with OGC Open Web Services,
     * implementors may use either abstract or description as needed.
     * <p>
     * When working with an Open Web Service this method is a direct match,
     * you may also choose it when providing actual dublin-core information
     * if the description element is specifically an abstract.
     * </p>
     *
     * @return text Abstract of document-like services
     */
    String getAbstract();

    /**
     * Return the service publisher
     *
     */
    URI getPublisher();

    /**
     * Returns the xml schema namespace for this service type. Maps to the Dublin Code Format
     * element
     *
     */
    URI getSchema();

    /**
     * Returns the service source. Maps to the Dublin Core Server Element
     *
     */
    URI getSource();

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
    Icon getIcon();
}
