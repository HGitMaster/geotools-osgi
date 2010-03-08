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
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * @author Gabriel Roldan, Axios Engineering
 * @author Rini Angreani, Curtin University of Technology
 * @version $Id: FeatureTypeMapping.java 34495 2009-11-25 12:24:06Z ang05a $
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/FeatureTypeMapping.java $
 * @since 2.4
 */
public class FeatureTypeMapping {
    /**
     * It's bad to leave this with no parameters type, but we should allow for both complex and
     * simple feature source as we could now take in a data access instead of a data store as
     * the source data store
     */
    private FeatureSource source;

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

    String itemXpath;
    
    /**
     * No parameters constructor for use by the digester configuration engine as a JavaBean
     */
    public FeatureTypeMapping() {
        this(null, null, new LinkedList<AttributeMapping>(), new NamespaceSupport());
    }

    public FeatureTypeMapping(FeatureSource source, AttributeDescriptor target,
            List<AttributeMapping> mappings, NamespaceSupport namespaces) {
        this.source = source;
        this.target = target;
        this.attributeMappings = new LinkedList<AttributeMapping>(mappings);
        this.namespaces = namespaces;
    }

    public FeatureTypeMapping(FeatureSource source, AttributeDescriptor target,
            List<AttributeMapping> mappings, NamespaceSupport namespaces,
            String itemXpath) {
        this(source, target, mappings, namespaces);
        this.itemXpath = itemXpath;
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
        List<AttributeMapping> mappings = Collections.emptyList();
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
        List<AttributeMapping> mappings = Collections.emptyList();
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

    public String getItemXpath() {
        return itemXpath;
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

    public AttributeDescriptor getTargetFeature() {
        return this.target;
    }

    public FeatureSource getSource() {
        return this.source;
    }
    
    public FeatureTypeMapping getUnderlyingComplexMapping() {
        if (source instanceof MappingFeatureSource) {
            return ((MappingFeatureSource) source).getMapping();
        }
        return null;
    }

}
