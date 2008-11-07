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

package org.geotools.data.complex.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration object for the mapping of a community schema attribute.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: AttributeMapping.java 31784 2008-11-06 06:20:21Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/config/AttributeMapping.java $
 * @since 2.4
 */
public class AttributeMapping implements Serializable {
    private static final long serialVersionUID = 3624951889528331592L;

    /**
     * XPath expression addressing the target attribute in a target FeatureType.
     */
    private String targetAttributePath;

    /**
     * Expression whose evaluation result against a Feature of the source FeatureType is going to be
     * the value of the target attribute in output FeatureType.
     * 
     * <p>
     * At this stage, the expression must be a valid OpenGIS Common Query Language expression.
     * </p>
     */
    private String sourceExpression;

    /**
     * Expression whose evaluation result against a Feature of the source FeatureType is going to be
     * the value of the id attribute property
     * 
     * <p>
     * At this stage, the expression must be a valid OpenGIS Common Query Language expression.
     * </p>
     */
    private String identifierExpression;

    /**
     * Name of the target element instance this attribute mapping applies to, or <code>null</code>
     * if its fully addressable by the FeatureType.
     * 
     * <p>
     * for example, the target FeatureType may define a property as GeometryAttributeType, but the
     * actual instance should be PointPropertyType.
     * </p>
     */
    private String targetAttributeSchemaElement;

    /**
     * If <code>true</code>, indicates that one instance of this attribute mapping must be
     * created for every repeating group of attributes. In other words, indicates wether this
     * attribute corresponds to a multivalued or a single valued attribute.
     */
    private boolean isMultiple;

    /**
     * Client properties definitions for instances of the target attribute. The map is keys are
     * strings representing the name of the client properties, and the map values are strings
     * representing OCG's CQL expressions whose evaluated value against the instances of the source
     * features are going to be the client properties values.
     * <p>
     * for example: srsName/strConcat("#bh.", BGS_ID)
     * </p>
     */
    private Map clientProperties;

    /**
     * Returns the expression whose evaluation result against a Feature of the source FeatureType is
     * going to be the value of the target attribute in output FeatureType.
     * 
     * <p>
     * At this stage, the expression must be a valid OpenGIS Common Query Language expression.
     * </p>
     * 
     * @return OGC CQL expression for the attribute value
     */
    public String getSourceExpression() {
        return sourceExpression;
    }

    /**
     * Sets the OGC CQL expression for the attribute value.
     * 
     * @param sourceExpression
     *                OGC CQL expression for the attribute value.
     */
    public void setSourceExpression(String sourceExpression) {
        this.sourceExpression = sourceExpression;
    }

    /**
     * Returns the XPath expression addressing the target attribute in a target FeatureType.
     * 
     * @return the XPath location path for the target attribute of the mapping.
     */
    public String getTargetAttributePath() {
        return targetAttributePath;
    }

    /**
     * Sets the XPath expression addressing the target attribute in a target FeatureType.
     * 
     * @param targetAttributePath
     *                the XPath location path for the target attribute of the mapping.
     */
    public void setTargetAttributePath(String targetAttributePath) {
        this.targetAttributePath = targetAttributePath;
    }

    /**
     * Returns the name of the target element instance this attribute mapping applies to, or
     * <code>null</code> if its fully addressable by the FeatureType.
     * 
     * <p>
     * For example, the target FeatureType may define a property as GeometryAttributeType, but the
     * actual instance should be PointPropertyType. In which case, it should be set to
     * "gml:PointPropertyType" so ComplexDataStore knows it should create a point property an thus
     * its subelements are to be addressable by subsequent mappings.
     * </p>
     * 
     * @return name of the target element instance in the output schema or <code>null</code> if
     *         not set.
     */
    public String getTargetAttributeSchemaElement() {
        return targetAttributeSchemaElement;
    }

    /**
     * Sets the name of the target element instance in the output schema.
     * 
     * @param targetAttributeSchemaElement
     *                name of the target element instance in the output schema. Could be prefixed,
     *                in which case the prefix mapping has to be available in the corresponding
     *                {@link AppSchemaDataAccessDTO#getNamespaces()}
     */
    public void setTargetAttributeSchemaElement(String targetAttributeSchemaElement) {
        this.targetAttributeSchemaElement = targetAttributeSchemaElement;
    }

    /**
     * Returns wether this attribute should be treated as a single or multi valued property.
     * 
     * @return <code>true</code> if this attribute corresponds to a multivalued property,
     *         <code>false</code> otherwise.
     */
    public boolean isMultiple() {
        return isMultiple;
    }

    /**
     * Sets wether this attribute should be treated as a single or multi valued property.
     * 
     * @param isMultiple
     *                <code>true</code> if this attribute corresponds to a multivalued property,
     *                <code>false</code> otherwise.
     */
    public void setMultiple(boolean isMultiple) {
        this.isMultiple = isMultiple;
    }

    /**
     * Helper method to allow config digester passing a string.
     * 
     * @see #setMultiple(boolean)
     * @param isMultiple
     */
    public void setMultiple(String isMultiple) {
        boolean multiple = Boolean.valueOf(isMultiple).booleanValue();
        setMultiple(multiple);
    }

    /**
     * Returns a string representation of this config object.
     * 
     * @return String representation of this config object.
     */
    public String toString() {
        return "AttributeMappingDTO[id > "
                + identifierExpression
                + ", "
                + sourceExpression
                + " -> "
                + targetAttributePath
                + ", isMultiple: "
                + isMultiple
                + ((targetAttributeSchemaElement == null) ? ""
                        : (", target node: " + targetAttributeSchemaElement)) + "]";
    }

    public Map getClientProperties() {
        return clientProperties == null ? Collections.EMPTY_MAP : clientProperties;
    }

    public void setClientProperties(Map clientProperties) {
        this.clientProperties = clientProperties == null ? null : new HashMap(clientProperties);
    }

    public void putClientProperty(String name, String expression) {
        if (name == null || expression == null) {
            throw new NullPointerException("name=" + name + ", expression=" + expression);
        }
        if (clientProperties == null) {
            clientProperties = new HashMap();
        }
        clientProperties.put(name, expression);
    }

    public String getIdentifierExpression() {
        return identifierExpression;
    }

    public void setIdentifierExpression(String identifierExpression) {
        this.identifierExpression = identifierExpression;
    }
}
