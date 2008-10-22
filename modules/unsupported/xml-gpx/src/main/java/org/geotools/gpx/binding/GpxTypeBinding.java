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
import org.geotools.gpx.bean.GpxType;
import org.geotools.gpx.bean.MetadataType;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:gpxType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="gpxType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  GPX documents contain a metadata header, followed by waypoints, routes, and tracks.  You can add your own elements
 *                  to the extensions section of the GPX document.
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element minOccurs="0" name="metadata" type="metadataType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Metadata about the file.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="wpt" type="wptType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  A list of waypoints.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="rte" type="rteType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  A list of routes.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="trk" type="trkType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  A list of tracks.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="extensions" type="extensionsType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  You can add extend GPX by adding your own elements from another schema here.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute fixed="1.1" name="version" type="xsd:string" use="required"&gt;
 *          &lt;xsd:annotation&gt;
 *              &lt;xsd:documentation&gt;
 *                  You must include the version number in your GPX document.
 *            &lt;/xsd:documentation&gt;
 *          &lt;/xsd:annotation&gt;
 *      &lt;/xsd:attribute&gt;
 *      &lt;xsd:attribute name="creator" type="xsd:string" use="required"&gt;
 *          &lt;xsd:annotation&gt;
 *              &lt;xsd:documentation&gt;
 *                  You must include the name or URL of the software that created your GPX document.  This allows others to
 *                  inform the creator of a GPX instance document that fails to validate.
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
public class GpxTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public GpxTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.gpxType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return GpxType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        GpxType gpx = factory.createGpxType();

        gpx.setCreator((String) node.getChildValue("creator"));
        gpx.setMetadata((MetadataType) node.getChildValue("metadata"));
        gpx.setVersion((String) node.getChildValue("version"));
        gpx.setExtensions((ExtensionsType) node.getChildValue("extensions"));

        gpx.getWpt().addAll(node.getChildValues("wpt"));
        gpx.getRte().addAll(node.getChildValues("rte"));
        gpx.getTrk().addAll(node.getChildValues("trk"));

        return gpx;
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        GpxType gpx = (GpxType) object;
        
        if("creator".equals(name.getLocalPart()))
            return gpx.getCreator();
        
        if("metadata".equals(name.getLocalPart()))
            return gpx.getMetadata();
        
        if("version".equals(name.getLocalPart()))
            return gpx.getVersion();
        
        if("extensions".equals(name.getLocalPart()))
            return gpx.getExtensions();
        
        if("wpt".equals(name.getLocalPart()))
            return gpx.getWpt();
        
        if("rte".equals(name.getLocalPart()))
            return gpx.getRte();
        
        if("trk".equals(name.getLocalPart()))
            return gpx.getTrk();
        
        return null;
    }
}
