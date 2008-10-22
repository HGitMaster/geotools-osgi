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

package org.geotools.filter;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Map;

import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.feature.FeatureType;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Geometry;

public class UnaliasSQLEncoder extends FilterToSQL {

	public static final int ORACLE = 1; // sql DIALECT
	public static final int ANSI = 0; // sql DIALECT
	public static final int MSSQL = 2; // sql DIALECT
	public static final int MYSQL = 3; // sql DIALECT
	public static final int POSTGRES = 4; // sql DIALECT
		

    private Map aliases;

    private int dialect = ANSI;	/* set if a specific database dialect is registered */

    public UnaliasSQLEncoder(Map aliases) {
        super();
        this.aliases = aliases;
    }

    public UnaliasSQLEncoder() {
        aliases = null;
    }

    public UnaliasSQLEncoder(Map aliases, Writer out) throws SQLEncoderException {
        super(out);
        this.aliases = aliases;
    }

    public void setAliases(Map aliases) {
        this.aliases = aliases;
    }

    /**
     * Sets the SQL dialect to control literal handling - dates especially
     * 
     * @param dialect 
     * 			the sql dialect
     */
    public void setDialect(int dialect)
    {
    	this.dialect = dialect;
    }
    
    /**
     * Writes the SQL for the attribute Expression.
     * 
     * @param expression
     *            the attribute to turn to SQL.
     * 
     * @throws RuntimeException
     *             for io exception with writer
     */
    public Object visit(PropertyName expression, Object extraData) throws RuntimeException {
        if (this.aliases == null) {
            super.visit(expression, extraData);
            return null;
        }
        String alias = expression.getPropertyName();
        String sqlExpression = (String) this.aliases.get(alias);
        if (sqlExpression == null) {
            throw new IllegalStateException("Unkown sql expression for attribute " + alias);
        }
        try {
            out.write(escapeName(sqlExpression));
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("IO problems writing attribute exp", ioe);
        }
        return extraData;
    }

    /**
     * Override date handling if Dialect
     */
    public Object visit(Literal expression, Object context) throws RuntimeException {
  
    	
        if( this.dialect == ORACLE )
        	return visitOracleLiteral(expression, context);
        else
        	return super.visit(expression, context);
    	}
    	
    	   /**
         * Export the contents of a Literal Expresion
         *
         * @param expression the Literal to export
         *
         * @throws RuntimeException for io exception with writer
         */
        public Object visitOracleLiteral(Literal expression, Object context) throws RuntimeException {
 
            //type to convert the literal to
            Class target = (Class)context;
            
          
    			
    		
    			if ( target == java.sql.Date.class ) {
    				// need to put it into a Date
    				String literal = (String) (expression.getValue());
                	try {
                    	SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
	                    Date dateliteral = iso.parse(literal);
						out.write( "DATE '" + iso.format(dateliteral) + "'" );
	    				return context;
                    } catch (Exception e) { }
                	try {
                    	SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                    Date dateliteral = iso.parse(literal);
						out.write( "TIMESTAMP '" + iso.format(dateliteral) + "'" );
	    				return context;
                    } catch (Exception e) { }
                	try {
                    	SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	                    Date dateliteral = iso.parse(literal);
						out.write( "TIMESTAMP '" + iso.format(dateliteral) + "'" );
	    				return context;
                    } catch (Exception e) { }

                    throw new RuntimeException ("Cannot parse ISO date literal: " + literal);
    			}
    			else
    				return super.visit(expression, context);
            }

		public FeatureType getFeatureType() {
			return this.featureType;
		}
            
}
