/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.processing.operation;

// JAI dependencies (for javadoc)
import javax.media.jai.operator.MaxFilterDescriptor;
import javax.media.jai.operator.MaxFilterShape;

// Geotools dependencies
import org.geotools.coverage.processing.OperationJAI;


/**
 * For each position of the mask, replaces the center pixel by the maximum of the pixel
 * values covered by the mask.
 *
 * <P><STRONG>Name:</STRONG>&nbsp;<CODE>"MaxFilter"</CODE><BR>
 *    <STRONG>JAI operator:</STRONG>&nbsp;<CODE>"{@linkplain MaxFilterDescriptor MaxFilter}"</CODE><BR>
 *    <STRONG>Parameters:</STRONG></P>
 * <table border='3' cellpadding='6' bgcolor='F4F8FF'>
 *   <tr bgcolor='#B9DCFF'>
 *     <th>Name</th>
 *     <th>Class</th>
 *     <th>Default value</th>
 *     <th>Minimum value</th>
 *     <th>Maximum value</th>
 *   </tr>
 *   <tr>
 *     <td>{@code "Source"}</td>
 *     <td>{@link org.geotools.coverage.grid.GridCoverage2D}</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 *   <tr>
 *     <td>{@code "maskShape"}</td>
 *     <td>{@link MaxFilterShape}</td>
 *     <td>{@link MaxFilterDescriptor#MAX_MASK_SQUARE}</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 *   <tr>
 *     <td>{@code "maskSize"}</td>
 *     <td>{@link Integer}</td>
 *     <td>{@code 3}</td>
 *     <td align="center">N/A</td>
 *     <td align="center">N/A</td>
 *   </tr>
 * </table>
 *
 * @since 2.3
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/coverage/src/main/java/org/geotools/coverage/processing/operation/MaxFilter.java $
 * @version $Id: MaxFilter.java 37285 2011-05-25 01:51:51Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class MaxFilter extends OperationJAI {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3368109980974496342L;

    /**
     * Constructs a default {@code "MaxFilter"} operation.
     */
    public MaxFilter() {
        super("MaxFilter");
    }
}
