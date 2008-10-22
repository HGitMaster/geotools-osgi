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
 * Binding object for the type http://www.topografix.com/GPX/1/1:degreesType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="degreesType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  Used for bearing, heading, course.  Units are decimal degrees, true (not magnetic).
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:restriction base="xsd:decimal"&gt;
 *          &lt;xsd:minInclusive value="0.0"/&gt;
 *          &lt;xsd:maxExclusive value="360.0"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class DegreesTypeBinding extends AbstractSimpleBinding {
    ObjectFactory factory;

    public DegreesTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.degreesType;
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
        Double deg = (Double) value;

        if ((deg < 0) || (deg >= 360)) {
            throw new IllegalArgumentException("degree value out of bounds [0..360): " + value);
        }

        return deg;
    }

    @Override
    public String encode(Object object, String value) throws Exception {
        Double deg = (Double) object;

        if ((deg < 0) || (deg >= 360)) {
            throw new IllegalArgumentException("degree value out of bounds [0..360): " + value);
        }
       
        return deg.toString(); // TODO: I guess we dont want full precision, so cutting the decimals would be fine.
    }
    
}
