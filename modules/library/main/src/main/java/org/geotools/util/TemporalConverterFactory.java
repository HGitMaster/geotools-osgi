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

import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.geotools.factory.Hints;
/**
 * Converter factory which created converting between the various temporal types.
 * <p>
 * Supported converstions:
 * <ul>
 * 	<li>{@link java.util.Date} to {@link Calendar}
 *  <li>{@link java.sql.Timestamp} to {@link java.util.Calendar} 
 *  <li>{@link java.sql.Time} to {@link java.util.Calendar}
 *  <li>{@link java.util.Date} to {@link java.sql.Timestamp}
 *  <li>{@link java.util.Date} to {@link java.sql.Time}
 *  <li>{@link java.util.Date} to {@link java.sql.Date}
 *  <li>{@link java.util.Calendar} to {@link java.util.Date}
 *  <li>{@link java.util.Calendar} to {@link java.sql.Timestamp}
 *  <li>{@link java.util.Calendar} to {@link java.sql.Time}
 *  <li>{@link XMLGregorianCalendar} to {@link Calendar}
 *  <li>{@link Calendar} to {@link XMLGregorianCalendar}
 *  <li>{@link XMLGregorianCalendar} to {@link Date}
 *  <li>{@link Date} to {@link XMLGregorianCalendar}
 * </ul>
 * </p>
 * <p>
 * The hint {@link #DATE_FORMAT} can be used to control the format of converting a temporal value
 * to a String.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 * @since 2.4
 */
public class TemporalConverterFactory implements ConverterFactory {

	public Converter createConverter(Class source, Class target, Hints hints) {
		
		if ( Date.class.isAssignableFrom( source ) ) {
			// handle all of (java.util.Date,java.sql.Timestamp,and java.sql.Time) -> java.util.Calendar 
			if ( Calendar.class.isAssignableFrom( target ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
						Calendar calendar = Calendar.getInstance();
						calendar.setTime( (Date) source );
						
						return calendar;
					}
				};
			}
			
			//handle all of (java.util.Date) -> (java.sql.Timestamp,java.sql.Time)
			if ( Timestamp.class.isAssignableFrom( target ) || Time.class.isAssignableFrom( target ) || 
                                java.sql.Date.class.isAssignableFrom( target ) ) {
                            
				return new Converter() {

					public Object convert(Object source, Class target) throws Exception {
						Date date = (Date) source;
						return target.getConstructor( new Class[] { Long.TYPE } )
							.newInstance( new Object[]{ new Long( date.getTime() ) } );
					}
					
				};
			}
			
			if ( XMLGregorianCalendar.class.isAssignableFrom( target ) ) {
                            return new Converter() {
                                public <T> T convert(Object source, Class<T> target)
                                        throws Exception {
                                    Date date = (Date) source;
                                    Calendar calendar = createConverter(Date.class, Calendar.class,null)
                                        .convert( date, Calendar.class);
                                    
                                    return (T) createConverter( Calendar.class, XMLGregorianCalendar.class, null )
                                        .convert( calendar, XMLGregorianCalendar.class );
                                        
                                }
                            };
                        }
//			if ( String.class.equals( target ) ) {
//				final DateFormat fFormat;
//				if ( dateFormat != null ) {
//					fFormat = dateFormat;
//				}
//				else {
//					//create a default
//					fFormat = DateFormat.getDateInstance();
//				}
//				
//				return new Converter() {
//					public Object convert(Object source, Class target) throws Exception {
//						return fFormat.format( (Date)source );
//					}
//				};
//			}
		}
		
		//this should handle java.util.Calendar to (java.util.Date,java.sql.Timestamp,java.util.Time}
		if ( Calendar.class.isAssignableFrom( source ) ) {
			if ( Date.class.isAssignableFrom( target ) ) {
				final Class fTarget = target;
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
						Calendar calendar = (Calendar) source;
						
						return target.getConstructor( new Class[] { Long.TYPE } ).newInstance( 
							new Object[]{ new Long( calendar.getTimeInMillis() ) }
						);
					}
				};
			}
			if ( XMLGregorianCalendar.class.isAssignableFrom( target ) ) {
			    return new Converter() {
			        public <T> T convert(Object source, Class<T> target)
                                        throws Exception {
			            if( source instanceof GregorianCalendar ) {
			                return (T) DatatypeFactory.newInstance().newXMLGregorianCalendar( (GregorianCalendar) source );
			            }
			            
			            return null;
			        }
			    };
			}
//			if ( String.class.equals( target ) ) {
//				final DateFormat fFormat;
//				if ( dateTimeFormat != null ) {
//					fFormat = dateTimeFormat;
//				}
//				else {
//					//create a default
//					fFormat = DateFormat.getDateTimeInstance();
//				}
//				
//				return new Converter() {
//					public Object convert(Object source, Class target) throws Exception {
//						Date date = ((Calendar)source).getTime();
//						return fFormat.format( date );
//					}
//				};
//			}
		}
		
		if ( XMLGregorianCalendar.class.isAssignableFrom( source ) ) {
		    if ( Calendar.class.isAssignableFrom( target ) ) {
		        return new Converter() {
                            public <T> T convert(Object source, Class<T> target)
                                    throws Exception {
                                XMLGregorianCalendar calendar = (XMLGregorianCalendar) source;
                                return (T) calendar.toGregorianCalendar();
                            }
		        };
		    }
		    if ( Date.class.isAssignableFrom( target ) ) {
		        return new Converter() {
		            public <T> T convert(Object source, Class<T> target)
                                    throws Exception {
		                Calendar calendar = createConverter(XMLGregorianCalendar.class, Calendar.class, null)
	                            .convert( source, Calendar.class );
	                        if ( calendar != null ) {
	                            return (T) createConverter( Calendar.class, Date.class, null )
	                                .convert( calendar, Date.class );
	                        }
	                        return null;
                            }
		        };
		    }
		}
		return null;
	}

}
