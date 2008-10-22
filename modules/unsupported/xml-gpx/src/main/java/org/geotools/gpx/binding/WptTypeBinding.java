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
import org.geotools.gpx.bean.ExtensionsType;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.gpx.bean.WptType;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:wptType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="wptType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  wpt represents a waypoint, point of interest, or named feature on a map.
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;!-- elements must appear in this order --&gt;
 *          &lt;!-- Position info --&gt;
 *          &lt;xsd:element minOccurs="0" name="ele" type="xsd:decimal"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Elevation (in meters) of the point.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="time" type="xsd:dateTime"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Creation/modification timestamp for element. Date and time in are in Univeral Coordinated Time (UTC), not local time! Conforms to ISO 8601 specification for date/time representation. Fractional seconds are allowed for millisecond timing in tracklogs.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="magvar" type="degreesType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Magnetic variation (in degrees) at the point
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="geoidheight" type="xsd:decimal"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Height (in meters) of geoid (mean sea level) above WGS84 earth ellipsoid.  As defined in NMEA GGA message.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;!-- Description info --&gt;
 *          &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          The GPS name of the waypoint. This field will be transferred to and from the GPS. GPX does not place restrictions on the length of this field or the characters contained in it. It is up to the receiving application to validate the field before sending it to the GPS.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="cmt" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          GPS waypoint comment. Sent to GPS as comment.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="desc" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          A text description of the element. Holds additional information about the element intended for the user, not the GPS.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="src" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Source of data. Included to give user some idea of reliability and accuracy of data.  "Garmin eTrex", "USGS quad Boston North", e.g.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="link" type="linkType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Link to additional information about the waypoint.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="sym" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Text of GPS symbol name. For interchange with other programs, use the exact spelling of the symbol as displayed on the GPS.  If the GPS abbreviates words, spell them out.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="type" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Type (classification) of the waypoint.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;!-- Accuracy info --&gt;
 *          &lt;xsd:element minOccurs="0" name="fix" type="fixType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Type of GPX fix.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="sat" type="xsd:nonNegativeInteger"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Number of satellites used to calculate the GPX fix.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="hdop" type="xsd:decimal"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Horizontal dilution of precision.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="vdop" type="xsd:decimal"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Vertical dilution of precision.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="pdop" type="xsd:decimal"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Position dilution of precision.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="ageofdgpsdata" type="xsd:decimal"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          Number of seconds since last DGPS update.
 *                    &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="dgpsid" type="dgpsStationType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                          ID of DGPS station used in differential correction.
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
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute name="lat" type="latitudeType" use="required"&gt;
 *          &lt;xsd:annotation&gt;
 *              &lt;xsd:documentation&gt;
 *                  The latitude of the point.  Decimal degrees, WGS84 datum.
 *            &lt;/xsd:documentation&gt;
 *          &lt;/xsd:annotation&gt;
 *      &lt;/xsd:attribute&gt;
 *      &lt;xsd:attribute name="lon" type="longitudeType" use="required"&gt;
 *          &lt;xsd:annotation&gt;
 *              &lt;xsd:documentation&gt;
 *                  The latitude of the point.  Decimal degrees, WGS84 datum.
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
public class WptTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public WptTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.wptType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return WptType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        WptType wpt = factory.createWptType();

        wpt.setEle((Double) node.getChildValue("ele", Double.NaN));
        wpt.setTime((Calendar) node.getChildValue("time"));
        wpt.setMagvar((Double) node.getChildValue("magvar", Double.NaN));
        wpt.setGeoidheight((Double) node.getChildValue("geoidheight", Double.NaN));
        wpt.setName((String) node.getChildValue("name"));
        wpt.setCmt((String) node.getChildValue("cmt"));
        wpt.setDesc((String) node.getChildValue("desc"));
        wpt.setSrc((String) node.getChildValue("src"));
        wpt.getLink().addAll(node.getChildValues("link"));
        wpt.setSym((String) node.getChildValue("sym"));
        wpt.setType((String) node.getChildValue("type"));
        wpt.setFix((String) node.getChildValue("fix"));
        wpt.setSat((Integer) node.getChildValue("sat", -1));
        wpt.setHdop((Double) node.getChildValue("hdop", Double.NaN));
        wpt.setVdop((Double) node.getChildValue("vdop", Double.NaN));
        wpt.setPdop((Double) node.getChildValue("pdop", Double.NaN));
        wpt.setAgeofdgpsdata((Double) node.getChildValue("ageofdgpsdata", Double.NaN));
        wpt.setDgpsid((Integer) node.getChildValue("dgpsid", -1));
        wpt.setExtensions((ExtensionsType) node.getChildValue("extensions"));
        wpt.setLon((Double) node.getAttributeValue("lon", Double.NaN));
        wpt.setLat((Double) node.getAttributeValue("lat", Double.NaN));

        return wpt;
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        WptType wpt = (WptType) object;
        
        if("ele".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getEle()))
                return null;
            else
                return wpt.getEle();
        }
        
        if("time".equals(name.getLocalPart()))
            return wpt.getTime();
        
        if("magvar".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getMagvar()))
                return null;
            else
                return wpt.getMagvar();
        }
        
        if("geoidheight".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getGeoidheight()))
                return null;
            else
                return wpt.getGeoidheight();
        }
        
        if("name".equals(name.getLocalPart()))
            return wpt.getName();
        
        if("cmt".equals(name.getLocalPart()))
            return wpt.getCmt();
        
        if("desc".equals(name.getLocalPart()))
            return wpt.getDesc();
        
        if("src".equals(name.getLocalPart()))
            return wpt.getSrc();
        
        if("link".equals(name.getLocalPart()))
            return wpt.getLink();
        
        if("sym".equals(name.getLocalPart()))
            return wpt.getSym();
        
        if("type".equals(name.getLocalPart()))
            return wpt.getType();
        
        if("fix".equals(name.getLocalPart()))
            return wpt.getFix();
        
        if("sat".equals(name.getLocalPart())) {
            if(wpt.getSat() == -1)
                return null;
            else
                return wpt.getSat();
        }
        
        if("hdop".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getHdop()))
                return null;
            else
                return wpt.getHdop();
        }
        
        if("vdop".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getVdop()))
                return null;
            else
                return wpt.getVdop();
        }
        
        if("pdop".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getPdop()))
                return null;
            else
                return wpt.getPdop();
        }
        
        if("ageofdgpsdata".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getAgeofdgpsdata()))
                return null;
            else
                return wpt.getAgeofdgpsdata();
        }
        
        if("dgpsid".equals(name.getLocalPart())) {
            if(wpt.getDgpsid() == -1)
                return null;
            else
                return wpt.getDgpsid();
        }
        
        if("extensions".equals(name.getLocalPart()))
            return wpt.getExtensions();
        
        if("lon".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getLon()))
                return null;
            else
                return wpt.getLon();
        }
        
        if("lat".equals(name.getLocalPart())) {
            if(Double.isNaN(wpt.getLat()))
                return null;
            else
                return wpt.getLat();
        }
        
        return null;
    }
}
