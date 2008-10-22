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

package org.geotools.data.complex;

import java.util.Collections;
import java.util.Map;

import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.resources.Utilities;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.expression.Expression;

/**
 * DOCUMENT ME!
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: AttributeMapping.java 31514 2008-09-15 08:36:50Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/AttributeMapping.java $
 * @since 2.4
 */
public class AttributeMapping {

    /** Expression to set the Attribute's ID from, or {@linkplain Expression#NIL} */
    private Expression identifierExpression;

    /** DOCUMENT ME! */
    private Expression sourceExpression;

    /** DOCUMENT ME! */
    // private String targetXPath;
    private StepList targetXPath;

    private boolean isMultiValued;

    /**
     * If present, represents our way to deal polymorphic attribute instances, so this node should
     * be of a subtype of the one referenced by {@link  #targetXPath}
     */
    AttributeType targetNodeInstance;

    private Map /* <Name,Expression> */clientProperties;

    /**
     * Creates a new AttributeMapping object.
     * 
     * @param sourceExpression
     *                DOCUMENT ME!
     * @param targetXPath
     *                DOCUMENT ME!
     */
    public AttributeMapping(Expression idExpression, Expression sourceExpression,
            StepList targetXPath) {
        this(idExpression, sourceExpression, targetXPath, null, false, null);
    }

    public AttributeMapping(Expression idExpression, Expression sourceExpression,
            StepList targetXPath, AttributeType targetNodeInstance, boolean isMultiValued,
            Map clientProperties) {

        this.identifierExpression = idExpression == null ? Expression.NIL : idExpression;
        this.sourceExpression = sourceExpression == null ? Expression.NIL : sourceExpression;
        this.isMultiValued = isMultiValued;
        if (this.sourceExpression == null) {
            this.sourceExpression = Expression.NIL;
        }
        this.targetXPath = targetXPath;
        this.targetNodeInstance = targetNodeInstance;
        this.clientProperties = clientProperties == null ? Collections.EMPTY_MAP : clientProperties;
    }

    public boolean isMultiValued() {
        return isMultiValued;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Expression getSourceExpression() {
        return sourceExpression;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public StepList getTargetXPath() {
        return targetXPath;
    }

    public AttributeType getTargetNodeInstance() {
        return targetNodeInstance;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param o
     *                DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AttributeMapping)) {
            return false;
        }

        AttributeMapping other = (AttributeMapping) o;

        return Utilities.equals(identifierExpression, other.identifierExpression)
                && Utilities.equals(sourceExpression, other.sourceExpression)
                && Utilities.equals(targetXPath, other.targetXPath)
                && Utilities.equals(targetNodeInstance, other.targetNodeInstance);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int hashCode() {
        return (37 * identifierExpression.hashCode() + 37 * sourceExpression.hashCode())
                ^ targetXPath.hashCode();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("AttributeMapping[");
        sb.append("sourceExpression='").append(sourceExpression).append("', targetXPath='").append(
                targetXPath);
        if (targetNodeInstance != null) {
            sb.append(", target instance type=").append(targetNodeInstance);
        }
        sb.append("']");

        return sb.toString();
    }

    public Map getClientProperties() {
        return clientProperties == null ? Collections.EMPTY_MAP : clientProperties;
    }

    public Expression getIdentifierExpression() {
        return identifierExpression;
    }

    public void setIdentifierExpression(Expression identifierExpression) {
        this.identifierExpression = identifierExpression;
    }
}
