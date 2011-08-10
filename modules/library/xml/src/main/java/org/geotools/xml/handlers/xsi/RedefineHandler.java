/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.xml.handlers.xsi;

import java.util.LinkedList;
import java.util.List;

import org.geotools.xml.XSIElementHandler;
import org.xml.sax.Attributes;


/**
 * RedefineHandler purpose.
 * 
 * <p>
 * represents a 'redefine' element
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/xml/src/main/java/org/geotools/xml/handlers/xsi/RedefineHandler.java $
 * @version $Id: RedefineHandler.java 37296 2011-05-25 05:02:48Z mbedward $
 */
public class RedefineHandler extends XSIElementHandler {
    /** 'redefine' */
    public final static String LOCALNAME = "redefine";
    private static int offset = 0;
    private String id;
    private String schemaLocation;
    private List simpleTypes;
    private List complexTypes;
    private List groups;
    private List attributeGroups;
    private int hashCodeOffset = getOffset();

    /*
     * helper for hashCode();
     */
    private static int getOffset() {
        return offset++;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((id == null) ? 1 : id.hashCode()) * ((schemaLocation == null)
        ? 1 : schemaLocation.hashCode())) + hashCodeOffset;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String,
     *      java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName){
        if (SchemaHandler.namespaceURI.equalsIgnoreCase(namespaceURI)) {
            // child types
            //
            // simpleType
            if (SimpleTypeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (simpleTypes == null) {
                    simpleTypes = new LinkedList();
                }

                SimpleTypeHandler sth = new SimpleTypeHandler();
                simpleTypes.add(sth);

                return sth;
            }

            // complexType
            if (ComplexTypeHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (complexTypes == null) {
                    complexTypes = new LinkedList();
                }

                ComplexTypeHandler sth = new ComplexTypeHandler();
                complexTypes.add(sth);

                return sth;
            }

            // group
            if (GroupHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (groups == null) {
                    groups = new LinkedList();
                }

                GroupHandler sth = new GroupHandler();
                groups.add(sth);

                return sth;
            }

            // attributeGroup
            if (AttributeGroupHandler.LOCALNAME.equalsIgnoreCase(localName)) {
                if (attributeGroups == null) {
                    attributeGroups = new LinkedList();
                }

                AttributeGroupHandler sth = new AttributeGroupHandler();
                attributeGroups.add(sth);

                return sth;
            }
        }

        return null;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes atts){
        id = atts.getValue("", "id");

        if (id == null) {
            id = atts.getValue(namespaceURI, "id");
        }

        schemaLocation = atts.getValue("", "schemaLocation");

        if (schemaLocation == null) {
            schemaLocation = atts.getValue(namespaceURI, "schemaLocation");
        }
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getLocalName()
     */
    public String getLocalName() {
        return LOCALNAME;
    }

    /**
     * <p>
     * Returns a list of AttributeGroupHandlers
     * </p>
     *
     */
    public List getAttributeGroups() {
        return attributeGroups;
    }

    /**
     * <p>
     * Returns a list of ComplexTypeHandlers
     * </p>
     *
     */
    public List getComplexTypes() {
        return complexTypes;
    }

    /**
     * <p>
     * Returns a list of GroupHandlers
     * </p>
     *
     */
    public List getGroups() {
        return groups;
    }

    /**
     * <p>
     * Returns the id attribute
     * </p>
     *
     */
    public String getId() {
        return id;
    }

    /**
     * <p>
     * Returns the schemaLocation attribute
     * </p>
     *
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * <p>
     * Returns a list of SimpleTypeHandlers
     * </p>
     *
     */
    public List getSimpleTypes() {
        return simpleTypes;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return DEFAULT;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String,
     *      java.lang.String)
     */
    public void endElement(String namespaceURI, String localName){
        // do nothing
    }
}