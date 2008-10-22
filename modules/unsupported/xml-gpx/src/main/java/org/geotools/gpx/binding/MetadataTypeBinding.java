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
import org.geotools.gpx.bean.BoundsType;
import org.geotools.gpx.bean.CopyrightType;
import org.geotools.gpx.bean.ExtensionsType;
import org.geotools.gpx.bean.MetadataType;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.gpx.bean.PersonType;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:metadataType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="metadataType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  Information about the GPX file, author, and copyright restrictions goes in the metadata section.  Providing rich,
 *                  meaningful information about your GPX files allows others to search for and use your GPS data.
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;!-- elements must appear in this order --&gt;
 *          &lt;xsd:element minOccurs="0" name="name" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  The name of the GPX file.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="desc" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  A description of the contents of the GPX file.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="author" type="personType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  The person or organization who created the GPX file.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="copyright" type="copyrightType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Copyright and license information governing use of the file.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element maxOccurs="unbounded" minOccurs="0" name="link" type="linkType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  URLs associated with the location described in the file.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="time" type="xsd:dateTime"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  The creation date of the file.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="keywords" type="xsd:string"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Keywords associated with the file.  Search engines or databases can use this information to classify the data.
 *             &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="bounds" type="boundsType"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  Minimum and maximum coordinates which describe the extent of the coordinates in the file.
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
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class MetadataTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public MetadataTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.metadataType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return MetadataType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        MetadataType metadata = factory.createMetadataType();

        metadata.setName((String) node.getChildValue("name"));
        metadata.setDesc((String) node.getChildValue("desc"));
        metadata.setAuthor((PersonType) node.getChildValue("author"));
        metadata.setCopyright((CopyrightType) node.getChildValue("copyright"));
        metadata.getLink().addAll(node.getChildValues("link"));
        metadata.setTime((Calendar) node.getChildValue("time"));
        metadata.setKeywords((String) node.getChildValue("keywords"));
        metadata.setBounds((BoundsType) node.getChildValue("bounds"));
        metadata.setExtensions((ExtensionsType) node.getChildValue("extensions"));

        return metadata;
    }
    
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        MetadataType metadata = (MetadataType) object;
        
        if("name".equals(name.getLocalPart()))
            return metadata.getName();
        
        if("desc".equals(name.getLocalPart()))
            return metadata.getDesc();
        
        if("author".equals(name.getLocalPart()))
            return metadata.getAuthor();
        
        if("copyright".equals(name.getLocalPart()))
            return metadata.getCopyright();
        
        if("link".equals(name.getLocalPart()))
            return metadata.getLink();
        
        if("time".equals(name.getLocalPart()))
            return metadata.getTime();
        
        if("keywords".equals(name.getLocalPart()))
            return metadata.getKeywords();
        
        if("bounds".equals(name.getLocalPart()))
            return metadata.getBounds();
        
        if("extensions".equals(name.getLocalPart()))
            return metadata.getExtensions();
        
        return null;
    }
}
