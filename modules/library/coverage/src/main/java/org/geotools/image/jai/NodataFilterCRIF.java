/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.image.jai;

// J2SE dependencies
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

 
/**
 * The factory for the {@link NodataFilter} operation.
 *
 * @since 2.1
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/library/coverage/src/main/java/org/geotools/image/jai/NodataFilterCRIF.java $
 * @version $Id: NodataFilterCRIF.java 30643 2008-06-12 18:27:03Z acuster $
 * @author Lionel Flahaut (2ie Technologie, IRD)
 */
public class NodataFilterCRIF extends CRIFImpl {
    /**
     * Constructs a default factory.
     */
    public NodataFilterCRIF() {
    }

    /**
     * Creates a {@link RenderedImage} representing the results of an imaging
     * operation for a given {@link ParameterBlock} and {@link RenderingHints}.
     */
    public RenderedImage create(final ParameterBlock param,
                                final RenderingHints hints)
    {
        final RenderedImage   image = (RenderedImage)param.getSource(0);
        final ImageLayout    layout = (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT);
        final int           padding = param.getIntParameter(0);
        final int validityThreshold = param.getIntParameter(1);
        return new NodataFilter(image, layout, hints, padding, validityThreshold);
    }
}	
