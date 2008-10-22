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

import java.util.Calendar;

import javax.xml.namespace.QName;

import org.geotools.gpx.GPX;
import org.geotools.gpx.bean.CopyrightType;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:copyrightType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="copyrightType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *           Information about the copyright holder and any license governing use of this file.  By linking to an appropriate license,
 *           you may place your data into the public domain or grant additional usage rights.
 *      &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;!-- elements must appear in this order --&gt;
 *          &lt;xsd:element minOccurs="0" name="year" type="xsd:gYear"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Year of copyright.
 *            &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="license" type="xsd:anyURI"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Link to external file containing license text.
 *            &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute name="author" type="xsd:string" use="required"&gt;
 *          &lt;xsd:annotation&gt;
 *              &lt;xsd:documentation&gt;
 *                  Copyright holder (TopoSoft, Inc.)
 *            &lt;/xsd:documentation&gt;
 *          &lt;/xsd:annotation&gt;
 *      &lt;/xsd:attribute&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class CopyrightTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public CopyrightTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.copyrightType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return CopyrightType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        CopyrightType copyright = factory.createCopyrightType();

        copyright.setAuthor((String) node.getChildValue("author"));
        copyright.setYear((Calendar) node.getChildValue("year"));
        copyright.setLicense((String) node.getChildValue("license"));

        return copyright;
    }

    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        CopyrightType cpy = (CopyrightType) object;
        
        if("author".equals(name.getLocalPart()))
            return cpy.getAuthor();
        
        if("year".equals(name.getLocalPart()))
            return cpy.getYear();
        
        if("license".equals(name.getLocalPart()))
            return cpy.getLicense();
        
        return null;
    }
    
}
