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
import org.geotools.gpx.bean.ExtensionsType;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.gpx.bean.RteType;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:rteType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="rteType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  rte represents route - an ordered list of waypoints representing a series of turn points leading to a destination.
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          GPS name of route.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="cmt" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          GPS comment for route.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="desc" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Text description of route for user.  Not sent to GPS.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="src" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Source of data. Included to give user some idea of reliability and accuracy of data.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="link" type="linkType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Links to external information about the route.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="number" type="xsd:nonNegativeInteger"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          GPS route number.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="type" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Type (classification) of route.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="extensions" type="extensionsType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  You can add extend GPX by adding your own elements from another schema here.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="rtept" type="wptType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  A list of route points.
 *             &lt;/xsd:documentation&gt;
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
public class RteTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public RteTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.rteType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return RteType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        RteType route = factory.createRteType();

        route.setName((String) node.getChildValue("name"));
        route.setCmt((String) node.getChildValue("cmt"));
        route.setDesc((String) node.getChildValue("desc"));
        route.setSrc((String) node.getChildValue("src"));
        route.getLink().addAll(node.getChildValues("link"));
        route.setNumber((Integer) node.getChildValue("number", -1));
        route.setType((String) node.getChildValue("type"));
        route.setExtensions((ExtensionsType) node.getChildValue("extensions"));
        route.getRtept().addAll(node.getChildValues("rtept"));

        return route;
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        RteType route = (RteType) object;
        
        if("name".equals(name.getLocalPart()))
            return route.getName();
        
        if("cmt".equals(name.getLocalPart()))
            return route.getCmt();
        
        if("desc".equals(name.getLocalPart()))
            return route.getDesc();
        
        if("src".equals(name.getLocalPart()))
            return route.getSrc();
        
        if("link".equals(name.getLocalPart()))
            return route.getLink();
        
        if("number".equals(name.getLocalPart())) {
            if(route.getNumber() == -1)
                return null;
            else
                return route.getNumber();
        }
        
        if("type".equals(name.getLocalPart()))
            return route.getType();
        
        if("extensions".equals(name.getLocalPart()))
            return route.getExtensions();
        
        if("rtept".equals(name.getLocalPart()))
            return route.getRtept();
        
        return null;
    }
}
