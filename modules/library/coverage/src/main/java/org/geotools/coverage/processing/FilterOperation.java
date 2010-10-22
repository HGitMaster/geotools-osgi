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
package org.geotools.coverage.processing;

// OpenGIS dependencies
import org.opengis.coverage.processing.OperationNotFoundException;

// Geotools dependencies
import org.geotools.coverage.GridSampleDimension;


/**
 * Common super-class for filter operation. The following is adapted from OpenGIS specification:
 *
 * <blockquote>
 * Filtering is an enhancement operation that alters the grid values on the basis of the
 * neighborhood grid values. For this reason, filtering is considered to be a spatial or
 * area operation. There are many different filters that can be applied to a grid coverage
 * but the general concept of filtering is the same. A filter window or kernel is defined,
 * its dimension being an odd number in the <var>x</var> and <var>y</var> dimensions. Each
 * cell in this window contains a co-efficient or weighting factor representative of some
 * mathematical relationship. A filtered grid coverage is generated by multiplying each
 * coefficient in the window by the grid value in the original grid coverage corresponding
 * to the windows current location and assigning the result to the central pixel location
 * of the window in the filtered grid coverage. The window is moved throughout the grid coverage
 * on pixel at a time. This window multiplication process is known as convolution. A grid coverage
 * contains both low and high spatial information. High frequencies describe rapid change from one
 * grid cell to another such as roads or other boundary conditions. Low frequencies describe gradual
 * change over a large number of cells such as water bodies. High pass filters allow only high
 * frequency information to be generated in the new grid coverage Grid coverages generated with high
 * pass filters will show edge conditions. Low pass filters allow low frequency information
 * to be generated in the new grid coverage. The grid coverage produced from a filtering
 * operation will have the same dimension as the source grid coverage. To produce filtered
 * values around the edges of the source grid coverage, edge rows and columns will be
 * duplicated to fill a complete kernel.
 * </blockquote>
 *
 * @since 2.2
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/library/coverage/src/main/java/org/geotools/coverage/processing/FilterOperation.java $
 * @version $Id: FilterOperation.java 30643 2008-06-12 18:27:03Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public class FilterOperation extends OperationJAI {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7984379314515755769L;

    /**
     * Constructs a new filter operation.
     *
     * @param  name The JAI operation name.
     * @throws OperationNotFoundException if no JAI descriptor was found for the given name.
     */
    public FilterOperation(final String name) throws OperationNotFoundException {
        super(name);
    }

    /**
     * Returns the target sample dimensions. Since filter operation do not change the range of
     * values, this method returns the same sample dimension than the first source.
     */
    protected GridSampleDimension[] deriveSampleDimension(final GridSampleDimension[][] bandLists,
                                                          final Parameters parameters)
    {
        return bandLists[0];
    }
}
