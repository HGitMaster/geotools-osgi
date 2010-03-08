/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.quality;

import org.opengis.metadata.quality.QuantitativeAttributeAccuracy;


/**
 * Accuracy of quantitative attributes.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/metadata/src/main/java/org/geotools/metadata/iso/quality/QuantitativeAttributeAccuracyImpl.java $
 * @version $Id: QuantitativeAttributeAccuracyImpl.java 30640 2008-06-12 17:34:32Z acuster $
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
public class QuantitativeAttributeAccuracyImpl extends ThematicAccuracyImpl
        implements QuantitativeAttributeAccuracy
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7030401943270178746L;

    /**
     * Constructs an initially empty quantitative attribute accuracy.
     */
    public QuantitativeAttributeAccuracyImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public QuantitativeAttributeAccuracyImpl(final QuantitativeAttributeAccuracy source) {
        super(source);
    }
}
