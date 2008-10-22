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
package org.geotools.sld;

import java.util.ArrayList;
import java.util.List;


/**
 * A Cascading Style Sheet parameter.
 * <p>
 * This class is internal to the sld binding project. It should be replaced
 * with a geotools styling model object if one becomes available.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class CssParameter {
    String name;
    List expressions;

    public CssParameter(String name) {
        this.name = name;
        expressions = new ArrayList();
    }

    public String getName() {
        return name;
    }

    public List getExpressions() {
        return expressions;
    }
}
