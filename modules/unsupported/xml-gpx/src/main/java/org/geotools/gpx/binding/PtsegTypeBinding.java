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
import org.geotools.gpx.bean.PtType;
import org.geotools.gpx.bean.PtsegType;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:ptsegType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="ptsegType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *           An ordered sequence of points.  (for polygons or polylines, e.g.)
 *      &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;!-- elements must appear in this order --&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="pt" type="ptType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                   Ordered list of geographic points.
 *                  &lt;/xsd:documentation&gt;
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
public class PtsegTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public PtsegTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.ptsegType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return PtsegType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        PtsegType ptseg = factory.createPtsegType();

        ptseg.getPt().addAll(node.getChildValues(PtType.class));

        return ptseg;
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        PtsegType ptseg = (PtsegType) object;
        
        if("pt".equals(name.getLocalPart()))
            return ptseg.getPt();
        
        return null;
    }
}
