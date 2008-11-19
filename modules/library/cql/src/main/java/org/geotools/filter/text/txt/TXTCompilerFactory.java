/*
 *    GeoTools - The Open Source Java GIS Tookit
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

package org.geotools.filter.text.txt;


import org.geotools.filter.text.commons.AbstractCompilerFactory;
import org.geotools.filter.text.commons.ICompiler;
import org.opengis.filter.FilterFactory;

/**
 * Provide the implementation of TXTCompiler
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
final class TXTCompilerFactory extends AbstractCompilerFactory {

    /**
     * Crates an instance of TXTCompiler
     */
    @Override
    protected ICompiler createCompiler(final String predicate, final FilterFactory filterFactory) {

        return new TXTCompiler(predicate, filterFactory);
    }

}
