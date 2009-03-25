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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.geotools.data.complex.AppSchemaDataAccessRegistry;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.NestedAttributeMapping;
import org.geotools.filter.AttributeExpressionImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
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
     *            Attribute XPath
     * @param expressions
     *            List of broken up expressions
     */
    public NestedAttributeExpression(String xpath, List<Expression> expressions) {
        super(xpath);
        if (expressions == null || expressions.size() <= 1 || expressions.size() % 2 != 0) {
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
        if (object == null) {
            return null;
        }
            
        // only simple/complex features are supported
        if (!(object instanceof Feature)) {
            throw new UnsupportedOperationException(
                    "Expecting a feature to apply filter, but found: " + object);
        }
        ArrayList<Feature> rootList = new ArrayList<Feature>();
        rootList.add((Feature) object);

        // start from the first feature type
        Expression expression = expressions.get(0);
        Object value = expression.evaluate((Feature) object);
        assert value instanceof Name;
        FeatureTypeMapping mappings;
        try {
            mappings = AppSchemaDataAccessRegistry.getMapping((Name) value);
        } catch (IOException e) {
            throw new UnsupportedOperationException("Mapping not found for: '" + value + "' type!");
        }
        // iterate through the expressions, starting from the first attribute
        Collection<?> valueList = evaluate(rootList, 1, mappings);

        if (valueList.isEmpty()) {
            return null;
        }
        String valueString = valueList.toString();
        // remove brackets
        return valueString.substring(1, valueString.length() - 1);
    }

    /**
     * This is an iterative method to evaluate expressions on complex features that can have
     * multi-valued properties. The values from the multi-valued properties are returned together as
     * a string, separated by comma.
     * 
     * @param rootList
     *            List of complex features
     * @param nextIndex
     *            Next index of expression to evaluate
     * @param mappings
     *            The feature type mapping of the features
     * @return the values from the complex features as a list
     */
    private Collection<Object> evaluate(Collection<Feature> rootList, int nextIndex,
            FeatureTypeMapping mappings) {
        Collection<Object> valueList = new ArrayList<Object>();

        if (nextIndex >= expressions.size()) {
            return Collections.emptyList();
        }

        // this should be an attribute expression
        Expression expression = expressions.get(nextIndex);
        assert expression instanceof AttributeExpressionImpl;

        for (Feature root : rootList) {
            ArrayList<Object> attributeValues = new ArrayList<Object>();

            Object value = expression.evaluate(root);

            if (value == null) {
                // value is legitimately null
                continue;
            }
            if (value instanceof Attribute) {
                value = ((Attribute) value).getValue();
            }
            if (value == null) {
                continue;
            }
            if (value instanceof Collection) {
                for (Object val : (Collection<?>) value) {
                    if (val instanceof Attribute) {
                        val = ((Attribute) val).getValue();
                    }
                    attributeValues.add(val);
                }
            } else {
                attributeValues.add(value);
            }
            if (nextIndex < expressions.size() - 1) {
                // if this is not the last, get the next feature type in the chain
                List<AttributeMapping> attMappings = mappings
                        .getAttributeMappingsByExpression(expression);

                if (attMappings.isEmpty()) {
                    throw new UnsupportedOperationException("Mapping not found for: '"
                            + expression.toString() + "'");
                }

                if (attMappings.size() > 1) {
                    // feature chaining only supports exact XPath with index so this shouldn't
                    // happen
                    throw new UnsupportedOperationException("Filtering attributes that map "
                            + "to more than one source expressions is not supported yet");
                }

                AttributeMapping mapping = (AttributeMapping) attMappings.get(0);
                assert mapping instanceof NestedAttributeMapping;

                FeatureTypeMapping fMapping = null;
                try {
                    ArrayList<Feature> featureList = new ArrayList<Feature>();

                    // get the features by feature id values
                    for (Object val : attributeValues) {
                        featureList.addAll(((NestedAttributeMapping) mapping).getInputFeatures(val));
                    }

                    // get the next attribute if there is any
                    nextIndex++;

                    if (!featureList.isEmpty() && nextIndex < expressions.size() - 1) {
                        // first get the feature type mapping for the next attribute
                        Expression nextExpression = expressions.get(nextIndex);

                        value = nextExpression.evaluate(featureList.get(0));

                        assert value instanceof Name;
                        try {
                            fMapping = AppSchemaDataAccessRegistry.getMapping((Name) value);
                        } catch (IOException e) {
                            throw new UnsupportedOperationException("Mapping not found for: '"
                                    + value + "' type!");
                        }

                        nextIndex++;
                        // then get the value of the next attribute
                        valueList.addAll(evaluate(featureList, nextIndex, fMapping));
                    }
                } catch (IOException e) {
                    throw new UnsupportedOperationException(
                            "Nested feature not found while filtering nested attribute: '"
                                    + Arrays.toString(expressions.toArray()) + "' cause :"
                                    + e.getMessage());
                }
            } else if (!attributeValues.isEmpty()) {
                valueList.addAll(attributeValues);
            }
        }
        return valueList;
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
