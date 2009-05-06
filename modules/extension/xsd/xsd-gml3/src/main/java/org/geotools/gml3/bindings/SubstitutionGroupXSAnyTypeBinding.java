/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.gml3.bindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDParticle;
import org.geotools.feature.NameImpl;
import org.geotools.xml.Schemas;
import org.geotools.xs.bindings.XSAnyTypeBinding;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;

/**
 * A binding that searches the substitution group of XSD element children to find properties of a
 * complex attribute. This is necessary to support the GML property type pattern, in which a
 * property (a property-type type) contains a property that is a member of a substitution group.
 * gml:AttributeType is the canonical example of the property type pattern.
 * 
 * <p>
 * 
 * gml:FeaturePropertyType is an example of the property type pattern that has an explicit binding
 * {@link FeaturePropertyTypeBinding}, but because an application schema may define more property
 * types whose names are not known at compile time, a binding like
 * {@link FeaturePropertyTypeBinding} cannot be written. This class exists to handle these
 * application-schema-defined property types.
 * 
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 * 
 */
public class SubstitutionGroupXSAnyTypeBinding extends XSAnyTypeBinding {

    /**
     * @see org.geotools.xml.AbstractComplexBinding#getProperties(java.lang.Object,
     *      org.eclipse.xsd.XSDElementDeclaration)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List getProperties(Object object, XSDElementDeclaration element) throws Exception {
        if (object instanceof ComplexAttribute) {
            ComplexAttribute complex = (ComplexAttribute) object;
            List<Object[/* 2 */]> properties = new ArrayList<Object[/* 2 */]>();
            for (XSDParticle childParticle : (List<XSDParticle>) Schemas.getChildElementParticles(
                    element.getTypeDefinition(), true)) {
                XSDElementDeclaration childElement = (XSDElementDeclaration) childParticle
                        .getContent();
                if (childElement.isElementDeclarationReference()) {
                    childElement = childElement.getResolvedElementDeclaration();
                }
                for (XSDElementDeclaration e : (List<XSDElementDeclaration>) childElement
                        .getSubstitutionGroup()) {
                    Name name = new NameImpl(e.getTargetNamespace(), e.getName());
                    Collection<Property> nameProperties = complex.getProperties(name);
                    if (!nameProperties.isEmpty()) {
                        // Particle creation stolen from BindingPropertyExtractor.
                        // I do not know why a wrapper is required; monkey see, monkey do.
                        // Without the wrapper, get an NPE in BindingPropertyExtractor.
                        XSDParticle substitutedChildParticle = XSDFactory.eINSTANCE
                                .createXSDParticle();
                        substitutedChildParticle.setMaxOccurs(childParticle.getMaxOccurs());
                        substitutedChildParticle.setMinOccurs(childParticle.getMinOccurs());
                        XSDElementDeclaration wrapper = XSDFactory.eINSTANCE
                                .createXSDElementDeclaration();
                        wrapper.setResolvedElementDeclaration(e);
                        substitutedChildParticle.setContent(wrapper);
                        for (Property property : nameProperties) {
                            if (property instanceof ComplexAttribute) {
                                properties.add(new Object[] { substitutedChildParticle, property });
                            } else {
                                properties.add(new Object[] { substitutedChildParticle,
                                        property.getValue() });
                            }
                        }
                    }
                }
            }
            return properties;
        }
        return null;
    }

}
