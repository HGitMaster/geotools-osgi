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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.geotools.data.complex.filter.XPath.StepList;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * @author Gabriel Roldan, Axios Engineering
 * @author Rini Angreani, Curtin University of Technology
 * @version $Id: FeatureTypeMapping.java 32469 2009-02-11 07:53:06Z ang05a $
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/FeatureTypeMapping.java $
 * @since 2.4
 */
public class FeatureTypeMapping {
    /**
     * 
     */
    private FeatureSource<SimpleFeatureType, SimpleFeature> source;

    /**
     * Encapsulates the name and type of target Features
     */
    private AttributeDescriptor target;

    /**
     * Map of <source expression>/<target property>, where target property is an XPath expression
     * addressing the mapped property of the target schema.
     */
    List<AttributeMapping> attributeMappings;

    NamespaceSupport namespaces;

    /**
     * No parameters constructor for use by the digester configuration engine as a JavaBean
     */
    public FeatureTypeMapping() {
        this(null, null, new LinkedList<AttributeMapping>(), new NamespaceSupport());
    }

    public FeatureTypeMapping(FeatureSource<SimpleFeatureType, SimpleFeature> source,
            AttributeDescriptor target, List<AttributeMapping> mappings, NamespaceSupport namespaces) {
        this.source = source;
        this.target = target;
        this.attributeMappings = new LinkedList<AttributeMapping>(mappings);
        this.namespaces = namespaces;
    }

    public List<AttributeMapping> getAttributeMappings() {
        return Collections.unmodifiableList(attributeMappings);
    }

    /**
     * Finds the attribute mappings for the given target location path ignoring the xpath index of
     * each step.
     * 
     * @param targetPath
     * @return
     */
    public List<AttributeMapping> getAttributeMappingsIgnoreIndex(final StepList targetPath) {
        AttributeMapping attMapping;
        List<AttributeMapping> mappings = Collections.EMPTY_LIST;
        for (Iterator<AttributeMapping> it = attributeMappings.iterator(); it.hasNext();) {
            attMapping = (AttributeMapping) it.next();
            if (targetPath.equalsIgnoreIndex(attMapping.getTargetXPath())) {
                if (mappings.size() == 0) {
                    mappings = new ArrayList<AttributeMapping>(2);
                }
                mappings.add(attMapping);
            }
        }
        return mappings;
    }

    /**
     * Finds the attribute mappings for the given source expression.
     * 
     * @param sourceExpression
     * @return list of matching attribute mappings
     */
    public List<AttributeMapping> getAttributeMappingsByExpression(final Expression sourceExpression) {
        AttributeMapping attMapping;
        List<AttributeMapping> mappings = Collections.EMPTY_LIST;
        for (Iterator<AttributeMapping> it = attributeMappings.iterator(); it.hasNext();) {
            attMapping = (AttributeMapping) it.next();
            if (sourceExpression.equals(attMapping.getSourceExpression())) {
                if (mappings.size() == 0) {
                    mappings = new ArrayList<AttributeMapping>(2);
                }
                mappings.add(attMapping);
            }
        }
        return mappings;
    }

    /**
     * Finds the attribute mapping for the target expression <code>exactPath</code>
     * 
     * @param exactPath
     *                the xpath expression on the target schema to find the mapping for
     * @return the attribute mapping that match 1:1 with <code>exactPath</code> or
     *         <code>null</code> if
     */
    public AttributeMapping getAttributeMapping(final StepList exactPath) {
        AttributeMapping attMapping;
        for (Iterator<AttributeMapping> it = attributeMappings.iterator(); it.hasNext();) {
            attMapping = (AttributeMapping) it.next();
            if (exactPath.equals(attMapping.getTargetXPath())) {
                return attMapping;
            }
        }
        return null;
    }

    public NamespaceSupport getNamespaces() {
        return namespaces;
    }

    /**
     * Has to be called after {@link #setTargetType(FeatureType)}
     * 
     * @param elementName
     * @param featureTypeName
     */
    public void setTargetFeature(AttributeDescriptor feature) {
        this.target = feature;
    }

    public void setSource() {
        this.source = source;
    }

    public AttributeDescriptor getTargetFeature() {
        return this.target;
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getSource() {
        return this.source;
    }

}
