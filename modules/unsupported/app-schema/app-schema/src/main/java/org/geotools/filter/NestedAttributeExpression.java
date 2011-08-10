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
import java.util.HashSet;
import java.util.List;

import org.geotools.data.complex.AppSchemaDataAccessRegistry;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.NestedAttributeMapping;
import org.geotools.data.complex.filter.XPath;
import org.geotools.factory.Hints;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.expression.FeaturePropertyAccessorFactory;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This class represents a list of expressions broken up from a single XPath expression that is
 * nested in more than one feature. The purpose is to allow filtering these attributes on the parent
 * feature.
 * 
 * @author Rini Angreani, Curtin University of Technology
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/filter/NestedAttributeExpression.java $
 */
public class NestedAttributeExpression extends AttributeExpressionImpl {
    /**
     * The list of expressions
     */
    private final List<Expression> expressions;

    /**
     * The name spaces hints
     */
    private Hints namespaces;

    private CoordinateReferenceSystem crs;

    /**
     * First constructor
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
     * Constructor with no expressions supplied. Used when feature mappings may not be there, ie.
     * not from an app-schema data access.
     * 
     * @param xpath
     *            Attribute XPath
     */
    public NestedAttributeExpression(String xpath, CoordinateReferenceSystem reprojection) {
        super(xpath);
        this.expressions = Collections.<Expression> emptyList();
        this.crs = reprojection;
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

        FeatureTypeMapping mappings = null;

        String[] xPathSteps = this.attPath.split("/");
        // start from the first feature type
        if (!expressions.isEmpty()) {
            Expression expression = expressions.get(0);
            Object value = expression.evaluate((Feature) object);
            assert value instanceof Name;
            try {
                mappings = AppSchemaDataAccessRegistry.getMapping((Name) value);
                this.namespaces = new Hints(FeaturePropertyAccessorFactory.NAMESPACE_CONTEXT,
                        mappings.getNamespaces());
            } catch (IOException e) {
                throw new UnsupportedOperationException("Mapping not found for: '" + value
                        + "' type!");
            }
        } else {
            // only supported for feature chaining purposes ie. linking between
            // feature types through simple link fields such as gml:name
            if (xPathSteps.length > 1) {
                throw new UnsupportedOperationException(
                        "Filtering deep complex attributes straight on complex features aren't supported yet.");
            }
        }
        // iterate through the expressions, starting from the first attribute
        Collection<?> valueList = evaluate(rootList, 1, mappings, xPathSteps);
        if (valueList.isEmpty()) {
            return null;
        }
        String valueString = valueList.toString();
        // remove brackets
        return valueString.substring(1, valueString.length() - 1);
    }

    /**
     * Return the next expression to be evaluated.
     * 
     * @param stepIndex
     *            Index of expression
     * @param xPathSteps
     *            Broken up attribute expression xpath
     * @param namespaces
     *            Namespace support
     * @return Next expression
     */
    private Expression getExpression(int stepIndex, String[] xPathSteps) {
        // expressions are preceded with the root feature's type name, so the index
        // is always +1 ahead of the xPathSteps index
        // expression would be in the expressions list if we have access to simple features
        // ie. the nested type is configured in an app-schema data access.. otherwise make up one
        // from the xpath steps, as we
        // would have complex features to evaluate against
        return ((stepIndex < expressions.size()) ? expressions.get(stepIndex)
                : new AttributeExpressionImpl(xPathSteps[stepIndex - 1], namespaces));

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
     * @param xPathSteps
     *            Array representation of this attribute's broken up xpath steps
     * @return the values from the complex features as a list
     */
    private Collection<Object> evaluate(Collection<Feature> rootList, int stepIndex,
            FeatureTypeMapping mappings, String[] xPathSteps) {
        Collection<Object> valueList = new HashSet<Object>();

        if (stepIndex >= this.expressions.size() && stepIndex > xPathSteps.length) {
            return Collections.emptyList();
        }

        Expression expression = getExpression(stepIndex, xPathSteps);

        for (Feature root : rootList) {
            HashSet<Object> attributeValues = new HashSet<Object>();

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
            if (stepIndex <= xPathSteps.length && mappings != null) {
                // if this is not the last chain, get the next feature type
                AttributeMapping mapping = mappings.getAttributeMapping(XPath.steps(mappings
                        .getTargetFeature(), xPathSteps[stepIndex - 1], mappings.getNamespaces()));

                if (stepIndex >= this.expressions.size() - 1) {
                    // means we have more in xPathSteps than expressions, this could be
                    // because the last chained attribute is mapped as an inline attribute
                    // eg.
                    // <targetAttribute>gsml:bodyMorphology/gsml:CGI_TermValue/gsml:value</targetAttribute>
                    // if no more mapping found, then we're done
                    if (mapping == null || !(mapping instanceof NestedAttributeMapping)) {
                        if (!attributeValues.isEmpty()) {
                            valueList.addAll(attributeValues);
                        }
                        continue;
                    }
                }
                if (mapping == null) {
                    throw new UnsupportedOperationException("Mapping not found for: '"
                            + xPathSteps[stepIndex].toString() + "'");
                }
                assert mapping instanceof NestedAttributeMapping;

                FeatureTypeMapping fMapping = null;
                try {
                    ArrayList<Feature> featureList = getFeatures((NestedAttributeMapping) mapping,
                            attributeValues);
                    // get the next type if there is any
                    stepIndex++;

                    if (!featureList.isEmpty()) {
                        Expression nextExpression;
                        if (stepIndex < expressions.size() - 1) {
                            // first get the feature type mapping for the next attribute
                            nextExpression = expressions.get(stepIndex);

                            value = nextExpression.evaluate(featureList.get(0));

                            assert value instanceof Name;
                            try {
                                fMapping = AppSchemaDataAccessRegistry.getMapping((Name) value);
                                this.namespaces = new Hints(
                                        FeaturePropertyAccessorFactory.NAMESPACE_CONTEXT, fMapping
                                                .getNamespaces());
                            } catch (IOException e) {
                                throw new UnsupportedOperationException("Mapping not found for: '"
                                        + value + "' type!");
                            }
                        }
                        // then get the value of the next attribute
                        stepIndex++;
                        valueList.addAll(evaluate(featureList, stepIndex, fMapping, xPathSteps));
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
     * Return nested features from the supplied attribute mapping and foreign keys. If the mapping
     * is configured in app-schema data access, we are after the simple/input features, otherwise we
     * have to get the complex features since simple features may not be available.
     * 
     * @param mapping
     *            The nested attribute mapping
     * @param foreignKeys
     *            Foreign key values to the nested type
     * @return List of nested features
     * @throws IOException
     */
    private ArrayList<Feature> getFeatures(NestedAttributeMapping mapping,
            Collection<Object> foreignKeys) throws IOException {
        ArrayList<Feature> featureList = new ArrayList<Feature>();

        boolean hasSimpleFeatures = AppSchemaDataAccessRegistry.hasName(mapping
                .getNestedFeatureType());

        if (hasSimpleFeatures) {
            for (Object val : foreignKeys) {
                featureList.addAll(mapping.getInputFeatures(val, crs));
            }
        } else {
            for (Object val : foreignKeys) {
                featureList.addAll(mapping.getFeatures(val, crs));
            }
        }
        return featureList;

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
