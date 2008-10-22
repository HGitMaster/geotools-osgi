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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * {@link DataAccessFactory} for the ComplexDataStore implementation.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ComplexDataAccessFactory.java 31514 2008-09-15 08:36:50Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/ComplexDataAccessFactory.java $
 * @since 2.4
 */
public class ComplexDataAccessFactory extends ComplexDataStoreFactory implements DataAccessFactory {

    public boolean canAccess(Object bean) {
        if (!(bean instanceof Map)) {
            return false;
        }
        return super.canProcess((Map) bean);
    }

    public boolean canCreateContent(Object arg0) {
        return false;
    }

    public DataAccess createAccess(Object params) throws IOException {
        return super.createDataStore((Map) params);
    }

    public Object createAccessBean() {
        return new HashMap();
    }

    public DataAccess createContent(Object params) {
        throw new UnsupportedOperationException();
    }

    public Object createContentBean() {
        return null;
    }

    public InternationalString getName() {
        return new SimpleInternationalString(super.getDisplayName());
    }

}
