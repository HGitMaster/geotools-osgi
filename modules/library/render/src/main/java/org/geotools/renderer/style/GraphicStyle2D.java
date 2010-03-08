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
package org.geotools.renderer.style;

import java.awt.image.BufferedImage;


/**
 * A style class used to depict a point, polygon centroid or line with a small graphic icon
 *
 * @author Andrea Aime
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/render/src/main/java/org/geotools/renderer/style/GraphicStyle2D.java $
 * @version $Id: GraphicStyle2D.java 30649 2008-06-12 19:44:08Z acuster $
 */
public class GraphicStyle2D extends Style2D {
    BufferedImage image;
    float rotation;
    float opacity;

    /**
     * Creates a new GraphicStyle2D object.
     *
     * @param image The image that will be used to depict the centroid/point/...
     * @param rotation The image rotation
     * @param opacity The image opacity
     */
    public GraphicStyle2D(BufferedImage image, float rotation, float opacity) {
        this.image = image;
        this.rotation = rotation;
        this.opacity = opacity;
    }

    /**
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * @param image
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * @param f
     */
    public void setOpacity(float f) {
        opacity = f;
    }

    /**
     * @param f
     */
    public void setRotation(float f) {
        rotation = f;
    }
}
