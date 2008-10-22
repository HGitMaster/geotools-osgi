/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.openoffice;

// OpenOffice dependencies
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.registry.XRegistryKey;


/**
 * The registration of all formulas provided in this package.
 *
 * @since 2.2
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/openoffice/src/main/java/org/geotools/openoffice/Registration.java $
 * @version $Id: Registration.java 30654 2008-06-12 20:19:03Z acuster $
 * @author Martin Desruisseaux
 */
public final class Registration {
    /**
     * Do not allows instantiation of this class.
     */
    private Registration() {
    }

    /**
     * Returns a factory for creating the service.
     * This method is called by the {@code com.sun.star.comp.loader.JavaLoader}; do not rename!
     *
     * @param   implementation The name of the implementation for which a service is desired.
     * @param   factories      The service manager to be used if needed.
     * @param   registry       The registry key
     * @return  A factory for creating the component.
     */
    public static XSingleServiceFactory __getServiceFactory(
                                        final String               implementation,
                                        final XMultiServiceFactory factories,
                                        final XRegistryKey         registry)
    {
        XSingleServiceFactory factory;
        factory = Referencing.__getServiceFactory(implementation, factories, registry);
        if (factory == null) {
            factory = Nature.__getServiceFactory(implementation, factories, registry);
        }
        return factory;
    }

    /**
     * Writes the service information into the given registry key.
     * This method is called by the {@code com.sun.star.comp.loader.JavaLoader}; do not rename!
     *
     * @param  registry     The registry key.
     * @return {@code true} if the operation succeeded.
     */
    public static boolean __writeRegistryServiceInfo(final XRegistryKey registry) {
        return Referencing.__writeRegistryServiceInfo(registry) &&
                    Nature.__writeRegistryServiceInfo(registry);
    }
}
