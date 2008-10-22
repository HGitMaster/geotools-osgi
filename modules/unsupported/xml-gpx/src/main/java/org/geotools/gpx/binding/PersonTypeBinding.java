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
import org.geotools.gpx.bean.EmailType;
import org.geotools.gpx.bean.LinkType;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.gpx.bean.PersonType;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:personType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="personType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *           A person or organization.
 *      &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;!-- elements must appear in this order --&gt;
 *          &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Name of person or organization.
 *            &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="email" type="emailType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Email address.
 *            &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="link" type="linkType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Link to Web site or other external information about person.
 *            &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class PersonTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public PersonTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.personType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return PersonType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        PersonType person = factory.createPersonType();

        person.setName((String) node.getChildValue("name"));
        person.setEmail((EmailType) node.getChildValue("email"));
        person.setLink((LinkType) node.getChildValue("link"));

        return person;
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        PersonType person = (PersonType) object;
        
        if("name".equals(name.getLocalPart()))
            return person.getName();
        
        if("email".equals(name.getLocalPart()))
            return person.getEmail();
        
        if("link".equals(name.getLocalPart()))
            return person.getLink();
        
        return null;
    }
}
