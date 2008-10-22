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
package org.geotools.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;

import org.geotools.factory.Hints;

/**
 * ConverterFactory which converts between the "standard" numeric types.
 * <p>
 * 	Supported types:
 * <ul>
 * <li>{@link java.lang.Long}
 * <li>{@link java.lang.Integer}
 * <li>{@link java.lang.Short}
 * <li>{@link java.lang.Byte}
 * <li>{@link java.lang.BigInteger}
 * <li>{@link java.lang.Double}
 * <li>{@link java.lang.Float}
 * <li>{@link java.lang.BigDecimal}
 * </ul>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * @since 2.4
 */
public class NumericConverterFactory implements ConverterFactory {

	public Converter createConverter(Class source, Class target, Hints hints) {
	
		//check if source is a number or a string.  We can't convert to a number
	    // from anything else.
		if ( !(Number.class.isAssignableFrom( source )) &&
		     !(String.class.isAssignableFrom( source )) ) 
			return null;
		
		//check if target is one of supported
		if ( 
			Long.class.equals( target ) || 
			Integer.class.equals( target ) || 
			Short.class.equals( target ) || 
			Byte.class.equals( target ) || 
			BigInteger.class.equals( target ) || 
			BigDecimal.class.equals( target ) || 
			Double.class.equals( target ) || 
			Float.class.equals( target ) ||
			Number.class.equals( target )
		) {
			return new NumericConverter();
		}
		
		return null;
	}
	
	class NumericConverter implements Converter {

		public Object convert(Object source, Class target) throws Exception {
		    if (source instanceof Number) {
    			Number s = (Number) source;
    			
    			//integral
    			if ( Long.class.equals( target ) ) {
    				return new Long( s.longValue() );
    			}
    			if ( Integer.class.equals( target ) ) {
    				return new Integer( s.intValue() );
    			}
    			if ( Short.class.equals( target ) ) {
    				return new Short( s.shortValue() );
    			}
    			if ( Byte.class.equals( target ) ) {
    				return new Byte( s.byteValue() );
    			}
    			if ( BigInteger.class.equals( target ) ) {
    				return BigInteger.valueOf( s.longValue() );
    			}
    		
    			//floating point
                        // JD: we use the string reprensentation to avoid coordinate
                        // drift due to precision issues, there could be some 
                        // performance issues with this.
    			if ( Double.class.equals( target ) ) {
    				return new Double( s.toString() );
    			}
    			if ( Float.class.equals( target ) ) {
    				return new Float( s.toString() );
    			}
    			if ( BigDecimal.class.equals( target ) ) {
    				return new BigDecimal( s.toString() );
    			}
    			
    			if (Number.class.equals( target )) {
    			    try {
                        return new Integer(s.toString());
                    } catch (Exception e) {
                    }
                    
                    try {
                        return new BigInteger(s.toString());
                    } catch (Exception e) {
                    }
                    
                    try {
                        return new Double(s.toString());
                    } catch (Exception e) {
                    }
                    
                    try {
                        return new BigDecimal(s.toString());
                    } catch (Exception e) {
                    }
    			}
		    } else if (source instanceof String) {
		        String s = (String) source;		        
		        //ensure we trim any space off the string
		        s = s.trim();

                String integral = toIntegral(s);
                
                //floating point
                if ( Double.class.equals( target ) ) {
                    return new Double(s);
                }
                if ( Float.class.equals( target ) ) {
                    return new Float(s);
                }
                if ( BigDecimal.class.equals( target ) ) {
                    return new BigDecimal(s);
                }
                

                
		        //textual
                if ( Long.class.equals( target ) ) {
                    return new Long(integral);
                }
                if ( Integer.class.equals( target ) ) {
                    return new Integer(integral);
                }
                if ( Short.class.equals( target ) ) {
                    return new Short(integral);
                }
                if ( Byte.class.equals( target ) ) {
                    return new Byte(integral);
                }
                if ( BigInteger.class.equals( target ) ) {
                    return new BigInteger(integral);
                }
                            
                // fallback.  If you ask for Number, you get our 'best guess'
                if (Number.class.equals( target )) {
                    if( integral.equals(s)){
                        // we think this is an integer of some sort
                        try {
                            return new Integer(integral);
                        } catch (Exception e) {
                        }
                        
                        try {
                            return new BigInteger(integral);
                        } catch (Exception e) {
                        }                            
                    }                    
                    try {
                        return new Double(s);
                    } catch (Exception e) {
                    }
                    
                    try {
                        return new BigDecimal(s);
                    } catch (Exception e) {
                    }
                }
		    }
		    //nothing matched.  Return null.
		    return null;
		}

	}

	/**
	 * Extract the integral part out of a decimal format string. 
	 * @param s
	 * @return integral component of decimal representation
	 */
	static String toIntegral( String s ) {
	    //NumberFormat format = NumberFormat.getInstance();
	    
	    int radex = -1; // last non numeric character to account for "." vs "," seperators
        for( int i=s.length()-1; i>0; i--){
            char ch = s.charAt(i);
            if( !Character.isDigit(ch) && '-' != ch){
                radex = i;
                break;
            }
        }
        if( radex != -1 ){
           // text is formatted in decimal but floating point format supplied
            return s.substring(0, radex );                    
        }
        else {
            return s;
        }
    }
}
