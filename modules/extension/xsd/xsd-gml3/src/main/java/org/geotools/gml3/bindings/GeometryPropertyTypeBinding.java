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
package org.geotools.gml3.bindings;

import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.gml3.GML;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Binding object for the type http://www.opengis.net/gml:GeometryPropertyType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="GeometryPropertyType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A geometric property can either be any geometry element encapsulated in an element of this type or an XLink reference
 *                          to a remote geometry element (where remote includes geometry elements located elsewhere in the same document). Note that either
 *                          the reference or the contained element must be given, but not both or none.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence minOccurs="0"&gt;
 *          &lt;element ref="gml:_Geometry"/&gt;
 *      &lt;/sequence&gt;
 *      &lt;attributeGroup ref="gml:AssociationAttributeGroup"&gt;
 *          &lt;annotation&gt;
 *              &lt;documentation&gt;This attribute group includes the XLink attributes (see xlinks.xsd). XLink is used in GML to reference
 *                                  remote resources (including those elsewhere in the same document). A simple link element can be constructed by
 *                                  including a specific set of XLink attributes. The XML Linking Language (XLink) is currently a Proposed Recommendation
 *                                  of the World Wide Web Consortium. XLink allows elements to be inserted into XML documents so as to create
 *                                  sophisticated links between resources; such links can be used to reference remote properties. A simple link element
 *                                  can be used to implement pointer functionality, and this functionality has been built into various GML 3 elements by
 *                                  including the gml:AssociationAttributeGroup.&lt;/documentation&gt;
 *          &lt;/annotation&gt;
 *      &lt;/attributeGroup&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GeometryPropertyTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.GeometryPropertyType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Geometry.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        return node.getChildValue(Geometry.class);
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        return GML3EncodingUtils.getProperty((Geometry) object, name );
    }
    
    public List getProperties(Object object) throws Exception {
        return GML3EncodingUtils.getProperties((Geometry) object);
    }
}
