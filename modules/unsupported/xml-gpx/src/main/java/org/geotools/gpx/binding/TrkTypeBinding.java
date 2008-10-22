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
import org.geotools.gpx.bean.TrkType;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:trkType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="trkType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  trk represents a track - an ordered list of points describing a path.
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          GPS name of track.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="cmt" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          GPS comment for track.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="desc" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          User description of track.
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
 *                          Links to external information about track.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="number" type="xsd:nonNegativeInteger"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          GPS track number.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="type" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Type (classification) of track.
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
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="trkseg" type="trksegType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  A Track Segment holds a list of Track Points which are logically connected in order. To represent a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span of track data.
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
public class TrkTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public TrkTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.trkType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return TrkType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        TrkType track = factory.createTrkType();

        track.setName((String) node.getChildValue("name"));
        track.setCmt((String) node.getChildValue("cmt"));
        track.setDesc((String) node.getChildValue("desc"));
        track.setSrc((String) node.getChildValue("src"));
        track.getLink().addAll(node.getChildValues("link"));
        track.setNumber((Integer) node.getChildValue("number", -1));
        track.setType((String) node.getChildValue("type"));
        track.setExtensions((ExtensionsType) node.getChildValue("extensions"));
        track.getTrkseg().addAll(node.getChildValues("trkseg"));

        return track;
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        TrkType track = (TrkType) object;
        
        if("name".equals(name.getLocalPart()))
            return track.getName();
        
        if("cmt".equals(name.getLocalPart()))
            return track.getCmt();
        
        if("desc".equals(name.getLocalPart()))
            return track.getDesc();
        
        if("src".equals(name.getLocalPart()))
            return track.getSrc();
        
        if("link".equals(name.getLocalPart()))
            return track.getLink();
        
        if("number".equals(name.getLocalPart())) {
            if(track.getNumber() == -1)
                return null;
            else
                return track.getNumber();
        }
        
        if("type".equals(name.getLocalPart()))
            return track.getType();
        
        if("extensions".equals(name.getLocalPart()))
            return track.getExtensions();
        
        if("trkseg".equals(name.getLocalPart()))
            return track.getTrkseg();
        
        return null;
    }
}
