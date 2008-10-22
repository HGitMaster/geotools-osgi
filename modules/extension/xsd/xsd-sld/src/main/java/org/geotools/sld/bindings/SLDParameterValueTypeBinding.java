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
package org.geotools.sld.bindings;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/sld:ParameterValueType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="ParameterValueType" mixed="true"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;         The &quot;ParameterValueType&quot;
 *              uses WFS-Filter expressions to give         values for SLD
 *              graphic parameters.  A &quot;mixed&quot; element-content
 *              model is used with textual substitution for values.       &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence minOccurs="0" maxOccurs="unbounded"&gt;
 *          &lt;xsd:element ref="ogc:expression"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class SLDParameterValueTypeBinding extends AbstractComplexBinding {
    FilterFactory filterFactory;

    public SLDParameterValueTypeBinding(FilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SLD.PARAMETERVALUETYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public int getExecutionMode() {
        return OVERRIDE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Expression.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {
    }

    /**
     * <!-- begin-user-doc -->
     * Even though the spec allows an instance of ParamterValueType to have
     * multiple expressions as children, it is more often that there is only
     * one. Therefore this binding returns the first expression it finds or
     * null. If a subtype needs multiple expressions they should ovveride.
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //first look for an expresion in the child text
        String text = instance.getText();

        if ((text != null) && !"".equals(text)) {
            return filterFactory.createLiteralExpression(text);
        }

        for (Iterator itr = node.getChildren().iterator(); itr.hasNext();) {
            Node child = (Node) itr.next();

            if (child.getValue() instanceof Expression) {
                return child.getValue();
            }
        }

        return null;
    }
}
