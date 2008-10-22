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
package org.geotools.gpx.binding;

import javax.xml.namespace.QName;

import org.geotools.gpx.GPX;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.xml.AbstractSimpleBinding;
import org.geotools.xml.InstanceComponent;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:longitudeType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="longitudeType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  The longitude of the point.  Decimal degrees, WGS84 datum.
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:restriction base="xsd:decimal"&gt;
 *          &lt;xsd:minInclusive value="-180.0"/&gt;
 *          &lt;xsd:maxExclusive value="180.0"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class LongitudeTypeBinding extends AbstractSimpleBinding {
    ObjectFactory factory;

    public LongitudeTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.longitudeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Double.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        Double lon = (Double) value;

        if ((lon >= 180) || (lon < -180)) {
            throw new IllegalArgumentException("Longitude over 180 or under -180 degrees: " + lon);
        }

        return lon;
    }
    
    @Override
    public String encode(Object object, String value) throws Exception {
        Double lon = (Double) object;

        if ((lon >= 180) || (lon < -180)) {
            //throw new IllegalArgumentException("Longitude over 180 or under -180 degrees: " + lon);
            // instead of failing, we snap the coordinates.
            while(lon >= 180)
                lon -= 360;
            while(lon < -180)
                lon += 360;
        }

        return lon.toString();
    }
}
