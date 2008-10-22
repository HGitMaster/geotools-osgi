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

package org.geotools.util;

import java.util.HashSet;
import java.util.Iterator;

import org.geotools.feature.iso.Types;
import org.opengis.feature.type.Name;

public class Namespace extends HashSet implements
        org.opengis.feature.type.Namespace {

    /**
     * Name of the namespace, usually maps to a uri.
     */
    Name name;

    /**
     * Construct a namespace from a pre-existing name.
     * 
     * @param name
     *            The name of this namespace.
     */
    public Namespace(Name name) {
        this.name = name;
    }

    /**
     * Construct a namespace from a uri.
     * 
     * @param uri
     *            The uri of the namespace.
     */
    public Namespace(String uri) {
        this.name = Types.typeName(uri);
    }

    public Name getName() {
        return name;
    }

    public String getURI() {
        return getName().getURI();
    }

    public Name lookup(String lookupName) {
        if (lookupName == null)
            return null;

        Name name = new org.geotools.feature.Name(getURI(), lookupName);

        for (Iterator itr = iterator(); itr.hasNext();) {
            Name n = (Name) itr.next();
            if (name.equals(n))
                return n;
        }

        return null;
    }

}
