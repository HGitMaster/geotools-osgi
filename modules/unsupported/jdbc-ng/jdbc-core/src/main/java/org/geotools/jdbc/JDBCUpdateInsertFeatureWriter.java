/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;

public class JDBCUpdateInsertFeatureWriter extends JDBCUpdateFeatureWriter {

    JDBCInsertFeatureWriter inserter;
    
    public JDBCUpdateInsertFeatureWriter(String sql, Connection cx,
            JDBCFeatureStore featureStore, Hints hints) throws SQLException,
            IOException {
        super(sql, cx, featureStore, hints);
    }
    
    public JDBCUpdateInsertFeatureWriter(PreparedStatement ps, Connection cx,
            JDBCFeatureStore featureStore, String[] attributeNames, Hints hints) throws SQLException,
            IOException {
        super(ps, cx, featureStore, hints);
    }
    
    public boolean hasNext() throws IOException {
        if ( inserter != null ) {
            return inserter.hasNext();
        }
        
        //check parent
        boolean hasNext = super.hasNext();
        if ( !hasNext ) {
            //update phase is up, switch to insert mode
            inserter = new JDBCInsertFeatureWriter( this );
            return inserter.hasNext();
        }
    
        return hasNext;
    }

    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        if ( inserter != null ) {
            return inserter.next();
        }
        
        return super.next();
    }
    
    public void remove() throws IOException {
        if ( inserter != null ) {
            inserter.remove();
            return;
        }
        
        super.remove();
    }
    
    public void write() throws IOException {
        if ( inserter != null ) {
            inserter.write();
            return;
        }
        
        super.write();
    }
    
    public void close() throws IOException {
        if ( inserter != null ) {
            //JD: do not call close because the inserter borrowed all of its state
            // from this reader... super will deal with it.
            inserter = null;
            //inserter.close();
        }
        
        super.close();
    }
    
}
