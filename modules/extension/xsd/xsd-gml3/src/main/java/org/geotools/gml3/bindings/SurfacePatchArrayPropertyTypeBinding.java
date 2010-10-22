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

import org.geotools.gml3.GML;
import org.geotools.xml.*;

import com.vividsolutions.jts.geom.Polygon;

import javax.xml.namespace.QName;

/**
 * Binding object for the type http://www.opengis.net/gml:SurfacePatchArrayPropertyType.
 * 
 * <p>
 * 
 * <pre>
 *  &lt;code&gt;
 *  &lt;complexType name=&quot;SurfacePatchArrayPropertyType&quot;&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;
 *              A container for an array of surface patches.
 *           &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot; ref=&quot;gml:_SurfacePatch&quot;/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt; 
 * 	
 *   &lt;/code&gt;
 * </pre>
 * 
 * </p>
 * 
 * @generated
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/extension/xsd/xsd-gml3/src/main/java/org/geotools/gml3/bindings/SurfacePatchArrayPropertyTypeBinding.java $
 */
public class SurfacePatchArrayPropertyTypeBinding extends AbstractComplexBinding {

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.SurfacePatchArrayPropertyType;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Class getType() {
        return Polygon[].class;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {

        List<Polygon> l = node.getChildValues( Polygon.class );
        Polygon[] polygons = new Polygon[l.size()];
        for ( int i = 0; i < l.size(); i++ ) {
            polygons[i] = l.get( i );
        }
        return polygons;
    }

}
