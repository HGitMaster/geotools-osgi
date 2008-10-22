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
 */
package org.geotools.validation.spatial;

import java.util.Map;

import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;


/**
 * LineNoDanglesValidation purpose.
 * 
 * <p>
 * Ensures Line does not have dangles.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/validation/src/main/java/org/geotools/validation/spatial/LineNoDanglesValidation.java $
 * @version $Id: LineNoDanglesValidation.java 30662 2008-06-12 21:44:16Z acuster $
 */
public class LineNoDanglesValidation extends LineAbstractValidation {
    /**
     * LineNoDanglesValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public LineNoDanglesValidation() {
        super();
    }

    /**
     * Ensure Line does not have dangles.
     * 
     * <p></p>
     *
     * @param layers a HashMap of key="TypeName" value="FeatureSource"
     * @param envelope The bounding box of modified features
     * @param results Storage for the error and warning messages
     *
     * @return True if no features intersect. If they do then the validation
     *         failed.
     *
     * @throws Exception DOCUMENT ME!
     *
     * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map,
     *      com.vividsolutions.jts.geom.Envelope,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Map layers, Envelope envelope,
        ValidationResults results) throws Exception {
        //TODO Fix Me
        return false;
    }
}
