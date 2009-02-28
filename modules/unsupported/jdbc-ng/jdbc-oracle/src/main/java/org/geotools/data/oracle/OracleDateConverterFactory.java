package org.geotools.data.oracle;

import java.util.Date;

import oracle.sql.DATE;
import oracle.sql.TIMESTAMP;

import org.geotools.factory.Hints;
import org.geotools.util.Converter;
import org.geotools.util.ConverterFactory;

public class OracleDateConverterFactory implements ConverterFactory {
	
	OracleDateConverter converter = new OracleDateConverter();

	public Converter createConverter(Class<?> source, Class<?> target,
			Hints hints) {
		// can only convert towards java.util.Date && subclasses
		if (!(Date.class.isAssignableFrom(target)))
			return null;

		// can only deal with oracle specific date classes
		if (!(TIMESTAMP.class.isAssignableFrom(source)) && !(DATE.class.isAssignableFrom(source)))
			return null;
		
		// converter is thread safe, so cache and return just one
		return converter;
	}
	
	class OracleDateConverter implements Converter {

		public <T> T convert(Object source, Class<T> target) throws Exception {
			if (source instanceof TIMESTAMP) {
				TIMESTAMP ts = (TIMESTAMP) source;
				if (java.sql.Date.class.isAssignableFrom(target))
					return (T) ts.dateValue();
				else
					return (T) ts.timestampValue();
			} else {
				DATE date = (DATE) source;
				if (java.sql.Date.class.isAssignableFrom(target))
					return (T) date.dateValue();
				else
					return (T) date.timestampValue();
			}
		}

	}

	
}
