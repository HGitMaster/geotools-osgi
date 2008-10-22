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
package org.geotools.jdbc;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.data.jdbc.FilterToSQL;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Id;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 * Extension of FilterToSQL intended for use with prepared statements.
 * <p>
 * Each time a {@link Literal} is visited, a '?' is encoded, and the 
 * value and type of the literal are stored, available after the fact 
 * via {@link #getLiteralValues()} and {@link #getLiteralTypes()}. 
 * 
 * </p>
 * @author Justin Deoliveira, OpenGEO
 * @author Andrea Aime, OpenGEO
 *
 */
public class PreparedFilterToSQL extends FilterToSQL {
    
    protected Integer currentSRID;

    /**
     * ordered list of literal values encountered and their types
     */
    protected List<Object> literalValues = new ArrayList<Object>();
    protected List<Class> literalTypes = new ArrayList<Class>();
    protected List<Integer> SRIDs = new ArrayList<Integer>();
    boolean prepareEnabled = true;
    
    public PreparedFilterToSQL() {
        super();
    }

    /**
     * If true (default) a sql statement with literal placemarks is created, otherwise
     * a normal statement is created
     * @return
     */
    public boolean isPrepareEnabled() {
        return prepareEnabled;
    }

    public void setPrepareEnabled(boolean prepareEnabled) {
        this.prepareEnabled = prepareEnabled;
    }

    public PreparedFilterToSQL(Writer out) {
        super(out);
    }

    public Object visit(Literal expression, Object context)
            throws RuntimeException {
        if(!prepareEnabled)
            return super.visit(expression, context);
        
        //evaluate the literal and store it for later
        Object literalValue = evaluateLiteral( expression, (context instanceof Class ? (Class) context : null) );
        literalValues.add(literalValue);
        SRIDs.add(currentSRID);
        if(literalValue != null)
            literalTypes.add(literalValue.getClass());
        else if(context instanceof Class)
            literalTypes.add((Class) context);
        
        try {
            out.write( "?" );
        } 
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        
        return context;
    }
    
    /**
     * Encodes an Id filter
     *
     * @param filter the
     *
     * @throws RuntimeException If there's a problem writing output
     *
     */
    public Object visit(Id filter, Object extraData) {
        if (mapper == null) {
            throw new RuntimeException(
                "Must set a fid mapper before trying to encode FIDFilters");
        }

        Set ids = filter.getIdentifiers();
        
        // prepare column name array
        String[] colNames = new String[mapper.getColumnCount()];

        for (int i = 0; i < colNames.length; i++) {
            colNames[i] = mapper.getColumnName(i);
        }

        for (Iterator i = ids.iterator(); i.hasNext(); ) {
            try {
                Identifier id = (Identifier) i.next();
                Object[] attValues = mapper.getPKAttributes(id.toString());

                out.write("(");

                for (int j = 0; j < attValues.length; j++) {
                    out.write( escapeName(colNames[j]) );
                    out.write(" = ");
                    out.write('?');
                    
                    // store the value for later usage
                    literalValues.add(attValues[j]);
                    // no srid, pk are not formed with geometry values
                    SRIDs.add(-1);
                    // if it's not null, we can also infer the type
                    literalTypes.add(attValues[j] != null ?  attValues[j].getClass() : null);

                    if (j < (attValues.length - 1)) {
                        out.write(" AND ");
                    }
                }

                out.write(")");

                if (i.hasNext()) {
                    out.write(" OR ");
                }
            } catch (java.io.IOException e) {
                throw new RuntimeException(IO_ERROR, e);
            }
        }
        
        return extraData;
    }
    
    public List<Object> getLiteralValues() {
        return literalValues;
    }
    
    public List<Class> getLiteralTypes() {
        return literalTypes;
    }
    
    /**
     * Returns the list of native SRID for each literal that happens to be a geometry, or null otherwise
     * @return
     */
    public List<Integer> getSRIDs() {
        return SRIDs;
    }
    
    @Override
    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            Object extraData) {
        // basic checks
        if(filter == null)
            throw new NullPointerException("Filter to be encoded cannot be null");
        if(!(filter instanceof BinaryComparisonOperator))
            throw new IllegalArgumentException("This filter is not a binary comparison, " +
                    "can't do SDO relate against it: " + filter.getClass());
        
        // extract the property name and the geometry literal
        PropertyName property;
        Literal geometry;
        BinaryComparisonOperator op = (BinaryComparisonOperator) filter;
        if(op.getExpression1() instanceof PropertyName && op.getExpression2() instanceof Literal) {
            property = (PropertyName) op.getExpression1();
            geometry = (Literal) op.getExpression2();
        } else if(op.getExpression2() instanceof PropertyName && op.getExpression1() instanceof Literal) {
            property = (PropertyName) op.getExpression2();
            geometry = (Literal) op.getExpression1();
        } else {
            throw new IllegalArgumentException("Can only encode spatial filters that do " +
                    "compare a property name and a geometry");
        }
        
        // handle native srid
        currentSRID = null;
        if(featureType != null) {
            // going thru evaluate ensures we get the proper result even if the name has 
            // not been specified (convention -> the default geometry)
            AttributeDescriptor descriptor = (AttributeDescriptor) property.evaluate(featureType);
            if(descriptor instanceof GeometryDescriptor) {
                currentSRID = (Integer) descriptor.getUserData().get(JDBCDataStore.JDBC_NATIVE_SRID);
            }
        }
        
        return visitBinarySpatialOperator(filter, property, geometry, 
                filter.getExpression1() instanceof Literal, extraData);
    }

    /**
     * Subclasses should override this, the property and the geometry have already been separated out
     * @param filter the original filter to be encoded
     * @param property the property name
     * @param geometry the geometry name
     * @param swapped if true, the operation is <code>literal op name</code>, if false it's the normal
     *        <code>name op literal</code>
     * @param extraData the context
     */
    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter, PropertyName property,
            Literal geometry, boolean swapped, Object extraData) {
        return super.visitBinarySpatialOperator(filter, extraData);
    }
}
