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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.feature.NameImpl;
import org.geotools.xml.Schemas;
import org.geotools.xs.XS;
import org.geotools.xs.bindings.XSAnyTypeBinding;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A replacement for {@link XSAnyTypeBinding} that adds support for {@link ComplexAttribute} and
 * related behaviours.
 * 
 * <p>
 * 
 * This binding that searches the substitution group of XSD element children to find properties of a
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
 * <p>
 * 
 * This class supports the encoding of XML complexType with simpleContent through extraction of a
 * simpleContent property, as well as encoding XML attributes stored in the UserData map.
 * 
 * @author Ben Caradoc-Davies, CSIRO Earth Science and Resource Engineering
 */
public class ComplexSupportXSAnyTypeBinding extends XSAnyTypeBinding {

    /**
     * @see org.geotools.xml.AbstractComplexBinding#getProperty(java.lang.Object,
     *      javax.xml.namespace.QName)
     */
    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        if (object instanceof ComplexAttribute) {
            ComplexAttribute complex = (ComplexAttribute) object;
            Property property = complex.getProperty(toTypeName(name));
            if (property != null && !(property instanceof ComplexAttribute)) {
                return property.getValue();
            }
        }
        return null;
    }

    /**
     * Convert a {@link QName} to a {@link Name}.
     * 
     * @param name
     * @return
     */
    private static Name toTypeName(QName name) {
        if (XMLConstants.NULL_NS_URI.equals(name.getNamespaceURI())) {
            return new NameImpl(name.getLocalPart());
        } else {
            return new NameImpl(name.getNamespaceURI(), name.getLocalPart());
        }
    }

    /**
     * @see org.geotools.xml.AbstractComplexBinding#getProperties(java.lang.Object,
     *      org.eclipse.xsd.XSDElementDeclaration)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List getProperties(Object object, XSDElementDeclaration element) throws Exception {
        List<Object[/* 2 */]> properties = new ArrayList<Object[/* 2 */]>();
        XSDTypeDefinition typeDef = element.getTypeDefinition();
        boolean isAnyType = typeDef.getName() != null && typeDef.getTargetNamespace() != null
                && typeDef.getName().equals(XS.ANYTYPE.getLocalPart())
                && typeDef.getTargetNamespace().equals(XS.NAMESPACE);
        if (isAnyType) {
            Collection complexAtts;
            if (object instanceof Collection) {
                // collection of features
                complexAtts = (Collection) object;
            } else if (object instanceof ComplexAttribute) {
                // get collection of features from this attribute
                complexAtts = ((ComplexAttribute) object).getProperties();
            } else {
                return null;
            }
            for (Object complex : complexAtts) {
                if (complex instanceof ComplexAttribute) {
                    PropertyDescriptor descriptor = ((Attribute) complex).getDescriptor();
                    if (descriptor.getUserData() != null) {
                        Object propertyElement = descriptor.getUserData().get(
                                XSDElementDeclaration.class);
                        if (propertyElement != null
                                && propertyElement instanceof XSDElementDeclaration) {
                            XSDParticle substitutedChildParticle = XSDFactory.eINSTANCE
                                    .createXSDParticle();
                            substitutedChildParticle.setMaxOccurs(descriptor.getMaxOccurs());
                            substitutedChildParticle.setMinOccurs(descriptor.getMinOccurs());
                            XSDElementDeclaration wrapper = XSDFactory.eINSTANCE
                                    .createXSDElementDeclaration();
                            wrapper
                                    .setResolvedElementDeclaration((XSDElementDeclaration) propertyElement);
                            substitutedChildParticle.setContent(wrapper);
                            properties.add(new Object[] { substitutedChildParticle, complex });
                        }
                    }
                }
            }
            return properties;
        }
        if (object instanceof ComplexAttribute) {
            ComplexAttribute complex = (ComplexAttribute) object;
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

    /**
     * @see org.geotools.xml.AbstractComplexBinding#encode(java.lang.Object, org.w3c.dom.Document,
     *      org.w3c.dom.Element)
     */
    @Override
    public Element encode(Object object, Document document, Element value) throws Exception {
        if (object instanceof ComplexAttribute) {
            ComplexAttribute complex = (ComplexAttribute) object;
            GML3EncodingUtils.encodeClientProperties(complex, value);
            GML3EncodingUtils.encodeSimpleContent(complex, document, value);
        }
        return value;
    }

}
