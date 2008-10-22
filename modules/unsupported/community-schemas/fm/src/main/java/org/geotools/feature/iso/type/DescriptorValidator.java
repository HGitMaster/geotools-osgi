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

package org.geotools.feature.iso.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.iso.Descriptors;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

class DescriptorValidator {

	private DescriptorValidator() {
		// no-op
	}

	public static void validate(AttributeDescriptor schema,
			List/* <Attribute> */content) {
		if (schema == null) {
			throw new NullPointerException("schema");
		}
		if (content == null) {
			throw new NullPointerException("content");
		}

		List/* <AttributeType> */allowedTypes = Descriptors.types((AttributeType) schema.type());

		int index = 0;
		for (Iterator itr = content.iterator(); itr.hasNext();) {
			Attribute att = (Attribute) itr.next();
			// att shall not be null
			checkAttIsNotNull(index, att);
			// and has to be of one of the allowed types
			checkAttIsOfAllowedType(allowedTypes, index, att);
			index++;
		}

		if (schema.type() instanceof ComplexType) {
			validateAll(schema, content);
		} else {
			validateNode(schema, content);
		}

	}


	private static void validateAll(AttributeDescriptor all,
			List/* <Attribute> */content) throws NullPointerException,
			IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("content");
		}

		ComplexType ctype = (ComplexType) all.type();
		List/* <AttributeType> */usedTypes = new ArrayList/* <AttributeType> */();
		for (Iterator itr = content.iterator(); itr.hasNext();) {
			Attribute att = (Attribute) itr.next();
			AttributeType/* <?> */type = att.getType();

			// cannot be more than one instance of its type
			// (shortcut to multiplicity rangecheck)
			if (usedTypes.contains(type)) {
				throw new IllegalArgumentException("Attribute of type "
						+ type.getName() + " encountered more than once.");
			}
			usedTypes.add(type);
		}
		// and the multiplicity specified in each AttributeDescriptor respected
		for (Iterator itr = ctype.attributes().iterator(); itr.hasNext();) {
			AttributeDescriptor node = (AttributeDescriptor) itr.next();
			int min = node.getMinOccurs();
			int max = node.getMaxOccurs();
			AttributeType/* <?> */expectedType = (AttributeType) node.type();
			if (max == 0 && usedTypes.contains(expectedType)) {
				throw new IllegalArgumentException(
						expectedType.getName()
								+ " was fund, thus it is not allowed since maxOccurs is set to 0");
			}
			if (min == 1 && !usedTypes.contains(expectedType)) {
				throw new IllegalArgumentException(
						expectedType.getName()
								+ " was not fund, thus it have to since minOccurs is set to 1");
			}
		}

	}

	private static void validateNode(AttributeDescriptor schema,
			List/* <Attribute> */content) {
		// no-op
	}

	/**
	 * @param allowedTypes
	 * @param index
	 * @param att
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static void checkAttIsOfAllowedType(
			List/* <AttributeType> */allowedTypes, int index, Attribute att)
			throws IllegalArgumentException {
		AttributeType/* <?> */type = att.getType();
		if (!allowedTypes.contains(type)) {
			throw new IllegalArgumentException("Attribute of type "
					+ type.getName() + " found at index " + index
					+ " but this type is not allowed by this descriptor");
		}
	}

	private static void checkAttIsNotNull(int index, Attribute att) {
		if (att == null) {
			throw new NullPointerException(
					"Attribute at index "
							+ index
							+ " is null. Attributes can't be null. Do you mean Attribute.get() == null?");
		}
	}
}
