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
package org.geotools.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.geotools.data.complex.AppSchemaDataAccessRegistry;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.NestedAttributeMapping;
import org.geotools.filter.AttributeExpressionImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;

/**
 * This class represents a list of expressions broken up from a single XPath expression that is
 * nested in more than one feature. The purpose is to allow filtering these attributes on the parent
 * feature.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public class NestedAttributeExpression extends AttributeExpressionImpl {
    /**
     * The list of expressions
     */
    private final List<Expression> expressions;

    /**
     * Sole constructor
     * 
     * @param xpath
     *            Attribute xpath
     * @param expressions
     *            List of broken up Sexpressions
     */
    public NestedAttributeExpression(String xpath, List<Expression> expressions) {
        super(xpath);
        if (expressions.size() <= 1 || expressions.size() % 2 != 0) {
            // this shouldn't happen if this was called by
            // UnmappingFilterVisitor.visit(PropertyName, Object)
            // since it also checks for this condition there
            throw new UnsupportedOperationException("Unmapping nested filter expressions fail!");
        }
        this.expressions = expressions;
    }

    /**
     * see {@link org.geotools.filter.AttributeExpressionImpl#evaluate(Object)}
     */
    @Override
    public Object evaluate(Object object) {
        if (!(object instanceof SimpleFeature)) {
            throw new UnsupportedOperationException(
                    "Expecting a simple feature to apply filter, but found: " + object);
        }
        Feature root = (Feature) object;

        Object value = null;
        // the expressions should be in pairs, in specific order : feature type, attribute name
        for (int i = 0; i < expressions.size(); i += 2) {
            Expression expression = expressions.get(i);
            value = expression.evaluate(root);
            // the first one in the pair should be a feature type
            assert value instanceof Name;
            FeatureTypeMapping mappings;
            try {
                mappings = AppSchemaDataAccessRegistry.getMapping((Name) value);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Mapping not found for: '" + value
                        + "' type!");
            }
            if (i < expressions.size() - 1) {

                // the second half of the pair should be an attribute
                expression = expressions.get(i + 1);

                assert expression instanceof AttributeExpressionImpl;

                value = expression.evaluate(root);
                if (value == null) {
                    // value is legitimately null
                    return null;
                }
                if (i < expressions.size() - 2) {
                    // if this is not the last pair, get the next feature in the chain
                    List attMappings = mappings.getAttributeMappingsByExpression(expression);

                    if (attMappings.isEmpty()) {
                        throw new UnsupportedOperationException("Mapping not found for: '"
                                + expression.toString());
                    }

                    if (attMappings.size() > 1) {
                        // feature chaining only supports exact xpath with index so this shouldn't
                        // happen
                        throw new UnsupportedOperationException("Filtering attributes that map "
                                + "to more than one source expressions is not supported yet");
                    }

                    AttributeMapping mapping = (AttributeMapping) attMappings.get(0);

                    assert mapping instanceof NestedAttributeMapping;

                    try {
                        root = ((NestedAttributeMapping) mapping).getSimpleFeature(value);
                    } catch (IOException e) {
                        throw new UnsupportedOperationException(
                                "Nested feature not found while filtering nested attribute: '"
                                        + Arrays.toString(expressions.toArray()) + "' cause :"
                                        + e.getMessage());
                    }
                }
            }
        }

        return value;
    }

    /**
     * Returns the list of broken up expressions
     * 
     * @return list of expressions
     */
    public List<Expression> getExpressions() {
        return this.expressions;
    }

    /**
     * @see org.geotools.filter.AttributeExpressionImpl#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof NestedAttributeExpression)) {
            return false;
        }
        return this.expressions.equals(((NestedAttributeExpression) o).getExpressions());
    }
}
