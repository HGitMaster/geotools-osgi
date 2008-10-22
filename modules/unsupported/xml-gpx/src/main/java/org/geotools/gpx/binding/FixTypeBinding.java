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
 * Binding object for the type http://www.topografix.com/GPX/1/1:fixType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="fixType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  Type of GPS fix.  none means GPS had no fix.  To signify "the fix info is unknown, leave out fixType entirely. pps = military signal used
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:restriction base="xsd:string"&gt;
 *          &lt;xsd:enumeration value="none"/&gt;
 *          &lt;xsd:enumeration value="2d"/&gt;
 *          &lt;xsd:enumeration value="3d"/&gt;
 *          &lt;xsd:enumeration value="dgps"/&gt;
 *          &lt;xsd:enumeration value="pps"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class FixTypeBinding extends AbstractSimpleBinding {
    ObjectFactory factory;

    public FixTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.fixType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return String.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        String fixType = (String) value;

        if (!"none".equals(value) && !"2d".equals(value) && !"3d".equals(value)
                && !"dgps".equals(value) && !"pps".equals(value)) {
            throw new IllegalArgumentException(
                "FixType can only be one of (none, 2d, 3d, dgps, pps), not: " + fixType);
        }

        return fixType;
    }
    
    @Override
    public String encode(Object object, String value) throws Exception {
        String fixType = (String) object;
        
        if (!"none".equals(value) && !"2d".equals(value) && !"3d".equals(value)
                && !"dgps".equals(value) && !"pps".equals(value)) {
            throw new IllegalArgumentException(
                "FixType can only be one of (none, 2d, 3d, dgps, pps), not: " + fixType);
        }

        return fixType;
    }
}
