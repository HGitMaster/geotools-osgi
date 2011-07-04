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
package org.geotools.validation.attributes;

import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * DomainValidation purpose.
 * 
 * <p>
 * TODO Explain this, no idea.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/validation/src/main/java/org/geotools/validation/attributes/DomainValidation.java $
 * @version $Id: DomainValidation.java 37300 2011-05-25 05:32:39Z mbedward $
 */
public class DomainValidation extends DefaultFeatureValidation {
    /**
     * DomainValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public DomainValidation() {
        super();
    }

    /**
     * Validation test for feature.
     * 
     * <p>
     * Description of test ...
     * </p>
     *
     * @param feature The Feature to be validated
     * @param type The FeatureType of the feature
     * @param results The storage for error messages.
     *
     * @return <code>true</code> if the feature is a valid geometry.
     *
     * @see org.geotools.validation.FeatureValidation#validate
     */
    public boolean validate(SimpleFeature feature, SimpleFeatureType type,
        ValidationResults results) {
        return false;
    }
}
