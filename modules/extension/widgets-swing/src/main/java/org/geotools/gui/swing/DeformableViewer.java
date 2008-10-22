/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 1998-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing;

// J2SE dependencies
import java.awt.geom.Point2D;


/**
 * An interface for viewers that may be deformed by some artefacts. For example the
 * {@link org.geotools.gui.swing.ZoomPane} viewer  is capable to show a {@linkplain
 * org.geotools.gui.swing.ZoomPane#setMagnifierVisible magnifying glass}  on top of
 * the usual content. The presence of a magnifying glass deforms the viewer in that
 * the apparent position of pixels within the glass are moved. The interface allows
 * for corrections of apparent pixel position in order to get the position we would
 * have if no deformations existed.
 *
 * @since 2.2
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/widgets-swing/src/main/java/org/geotools/gui/swing/DeformableViewer.java $
 * @version $Id: DeformableViewer.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (PMO, IRD)
 */
public interface DeformableViewer {
    /**
     * Corrects a pixel's coordinates for removing the effect of the any kind of deformations.
     * An example of deformation is the zoom pane's {@linkplain
     * org.geotools.gui.swing.ZoomPane#setMagnifierVisible magnifying glass}. Without this
     * method,  transformations from pixels to geographic coordinates would not give exact
     * results for pixels inside the magnifier since the magnifier moves the pixel's apparent
     * position. Invoking this method will remove any deformation effects using the following
     * steps:
     * <ul>
     *   <li>If the pixel's coordinate <code>point</code> is outside deformed areas (for example
     *       outside the magnifier), then this method do nothing.</li>
     *   <li>Otherwise, if the pixel's coordinate is inside some area that has been deformed,
     *       then this method update <code>point</code> in such a way that it contains the
     *       position that the exact same pixel would have in the absence of deformations.</li>
     * </ul>
     *
     * @param point In input, a pixel's coordinate as it appears on the screen.
     *              In output, the coordinate that the same pixel would have if
     *              the deformation wasn't presents.
     */
    void correctApparentPixelPosition(Point2D point);
}
