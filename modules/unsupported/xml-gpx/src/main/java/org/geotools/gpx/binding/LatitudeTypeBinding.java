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
 * Binding object for the type http://www.topografix.com/GPX/1/1:latitudeType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="latitudeType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  The latitude of the point.  Decimal degrees, WGS84 datum.
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:restriction base="xsd:decimal"&gt;
 *          &lt;xsd:minInclusive value="-90.0"/&gt;
 *          &lt;xsd:maxInclusive value="90.0"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class LatitudeTypeBinding extends AbstractSimpleBinding {
    ObjectFactory factory;

    public LatitudeTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.latitudeType;
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
        Double lat = (Double) value;

        if ((lat > 90) || (lat < -90)) {
            throw new IllegalArgumentException("Latitude over 90 or under -90 degrees: " + lat);
        }

        return lat;
    }
    
    @Override
    public String encode(Object object, String value) throws Exception {
        Double lat = (Double) object;

        if ((lat > 90) || (lat < -90)) {
            throw new IllegalArgumentException("Latitude over 90 or under -90 degrees: " + lat);
        }

        return lat.toString();
    }
}
