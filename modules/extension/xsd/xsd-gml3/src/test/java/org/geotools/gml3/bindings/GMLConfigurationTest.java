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
package org.geotools.gml3.bindings;

import junit.framework.TestCase;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import org.geotools.gml3.GML;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Binding;


public class GMLConfigurationTest extends TestCase {
    public void testBindingTypes() throws Exception {
        GMLConfiguration configuration = new GMLConfiguration();
        assertEquals(GML.NAMESPACE, configuration.getNamespaceURI());

        PicoContainer bindings = new DefaultPicoContainer();
        bindings = configuration.setupBindings((MutablePicoContainer) bindings);

        do {
            for (Iterator i = bindings.getComponentAdapters().iterator(); i.hasNext();) {
                ComponentAdapter adapter = (ComponentAdapter) i.next();
                Class type = adapter.getComponentImplementation();

                if (Binding.class.isAssignableFrom(type)) {
                    Constructor c = type.getConstructors()[0];
                    Object[] params = new Object[c.getParameterTypes().length];

                    Binding binding = (Binding) c.newInstance(params);
                    assertNotNull(binding.getTarget());

                    if (binding.getTarget().getNamespaceURI().equals(GML.NAMESPACE)) {
                        assertNotNull(binding.getTarget() + " has a null type", binding.getType());
                    }
                }
            }

            bindings = bindings.getParent();
        } while (bindings != null);
    }
}
