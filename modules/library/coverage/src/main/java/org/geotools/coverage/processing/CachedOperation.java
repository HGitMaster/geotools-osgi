/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.processing;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.opengis.coverage.Coverage;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;

import org.geotools.util.Utilities;
import org.geotools.parameter.Parameters;
import org.geotools.coverage.CoverageCache;


/**
 * An {@link Operation}-{@link ParameterValueGroup} pair, used by
 * {@link DefaultOperation#doOperation} for caching the result of operations.
 * Reusing previous computation outputs should be okay since grid coverage
 * (both the sources and the result) are immutable by default.
 *
 * @todo There is a tricky issue for grid coverage backed by a writable rendered
 *       image. The OpenGIS specification allows to change sample values. What
 *       should be the semantic for operation using those images as sources?
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/coverage/src/main/java/org/geotools/coverage/processing/CachedOperation.java $
 * @version $Id: CachedOperation.java 30643 2008-06-12 18:27:03Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
final class CachedOperation {
    /**
     * The operation to apply on grid coverages.
     */
    private final Operation operation;

    /**
     * The parameters names in alphabetical order, including source coverages.
     */
    private final String[] names;

    /**
     * The parameters values. {@link Coverage} objects will be retained as weak references.
     */
    private final Object[] values;

    /**
     * The hash code value for this key.
     */
    private final int hashCode;

    /**
     * Constructs a new key for the specified operation and parameters.
     *
     * @param operation  The operation to apply on grid coverages.
     * @param parameters The parameters, including source grid coverages.
     */
    CachedOperation(final Operation operation, final ParameterValueGroup parameters) {
        final Map<String,Object> param =
                Parameters.toNameValueMap(parameters, new TreeMap<String,Object>());
        this.operation  = operation;
        int   hashCode  = operation.hashCode();
        this.names      = new String[param.size()];
        this.values     = new Object[names.length];
        int  index      = 0;
        for (final Iterator it=param.entrySet().iterator(); it.hasNext(); index++) {
            final Map.Entry entry = (Map.Entry) it.next();
            names[index] = ((String) entry.getKey()).toLowerCase();
            Object value = entry.getValue();
            if (value instanceof Coverage) {
                value = CoverageCache.DEFAULT.reference((Coverage) value);
            }
            if (value != null) {
                hashCode = 37*hashCode + value.hashCode();
            }
            values[index] = value;
        }
        this.hashCode = hashCode;
    }

    /**
     * Compares the specified object with this key for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof CachedOperation) {
            final CachedOperation that = (CachedOperation) object;
            return Utilities.equals(this.operation, that.operation) &&
                             Arrays.equals(this.names,     that.names)     &&
                             Arrays.equals(this.values,    that.values);
            /*
             * NOTE: values array may contains WeakReference, in which case the 'equals'
             *       method applies to reference instead of referent (i.e. Coverage). It
             *       should work anyway, since CoverageCache should returns the same
             *       WeakReference instance for the same Coverage.
             */
        }
        return false;
    }

    /**
     * Returns a hash code value for this key.
     */
    @Override
    public int hashCode() {
        return hashCode;
    }
}
