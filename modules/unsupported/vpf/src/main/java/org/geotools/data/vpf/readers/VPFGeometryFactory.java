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
package org.geotools.data.vpf.readers;
import java.io.IOException;
import java.sql.SQLException;

import org.geotools.data.vpf.VPFFeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/vpf/src/main/java/org/geotools/data/vpf/readers/VPFGeometryFactory.java $
 */
public abstract class VPFGeometryFactory {
	private static final GeometryFactory m_GeometryFactory = new GeometryFactory();
    /**
     * Constructs a geometry for the appropriate feature type based on values currently on the object,
     * retrieving values as needed from the various VPFFile objects
     * @param featureType the VPFFeatureType to use
     * @param values the current feature
     * @throws SQLException
     * @throws IOException
     * @throws IllegalAttributeException
     */
	public abstract void createGeometry(VPFFeatureType featureType, SimpleFeature values) throws SQLException, IOException, IllegalAttributeException;
}
