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
 * Binding object for the type http://www.topografix.com/GPX/1/1:dgpsStationType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="dgpsStationType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *           Represents a differential GPS station.
 *      &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:restriction base="xsd:integer"&gt;
 *          &lt;xsd:minInclusive value="0"/&gt;
 *          &lt;xsd:maxInclusive value="1023"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class DgpsStationTypeBinding extends AbstractSimpleBinding {
    ObjectFactory factory;

    public DgpsStationTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.dgpsStationType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Integer.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        Integer dgpsid = (Integer) value;

        if ((dgpsid.intValue() < 0) || (dgpsid.intValue() > 1023)) {
            throw new IllegalArgumentException("dgpsid must be within 0 and 1023: " + dgpsid);
        }

        return dgpsid;
    }
    
    @Override
    public String encode(Object object, String value) throws Exception {
        Integer dgpsid = (Integer) object;
        
        if ((dgpsid.intValue() < 0) || (dgpsid.intValue() > 1023)) {
            throw new IllegalArgumentException("dgpsid must be within 0 and 1023: " + dgpsid);
        }

        return dgpsid.toString();
    }
}
