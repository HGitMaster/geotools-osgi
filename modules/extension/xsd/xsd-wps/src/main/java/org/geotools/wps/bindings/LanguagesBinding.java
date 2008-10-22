/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wps.bindings;

import javax.xml.namespace.QName;

import net.opengis.wps.LanguagesType1;
import net.opengis.wps.WpsFactory;

import org.geotools.wps.WPS;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

/**
 * Manually implemented binding due to type naming conflicts.
 * <pre>
 * &lt;element name="Languages">
 *   &lt;annotation>
 *       &lt;documentation>Listing of the default and other languages supported by this service. &lt;/documentation>
 *   &lt;/annotation>
 *   &lt;complexType>
 *       &lt;sequence>
 *         &lt;element name="Default">
 *           &lt;annotation>
 *              &lt;documentation>Identifies the default language that will be used unless the operation request specifies another supported language. &lt;/documentation>
 *           &lt;/annotation>
 *           &lt;complexType>
 *              &lt;sequence>
 *                  &lt;element ref="ows:Language">
 *                  &lt;/element>
 *               &lt;/sequence>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Supported" type="wps:LanguagesType">
 *           &lt;annotation>
 *                    &lt;documentation>Unordered list of references to all of the languages supported by this service. The default language shall be included in this list.&lt;/documentation>
 *           &lt;/annotation>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/complexType>
 *   &lt;/element>
 *
 * </pre>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class LanguagesBinding extends AbstractComplexBinding {

    WpsFactory factory;
    public LanguagesBinding( WpsFactory factory ) {
        this.factory = factory;
    }
    
    public QName getTarget() {
        return WPS._Languages;
    }

    public Class getType() {
        return LanguagesType1.class;
    }
    
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
        LanguagesType1 langs = factory.createLanguagesType1();
        
        //TODO: impelement me
        
        return langs;
    }
    

}
