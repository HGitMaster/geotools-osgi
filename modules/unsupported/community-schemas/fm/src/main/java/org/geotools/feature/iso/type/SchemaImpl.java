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

package org.geotools.feature.iso.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.iso.Types;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Namespace;
import org.opengis.feature.type.Schema;

public class SchemaImpl implements Schema {
	HashMap contents;		
	String uri;
	
	Namespace namespace = new Namespace(){
		public String getURI() {
			return uri;
		}
		public Name lookup(String lookupName) {
			if (lookupName == null) 
				return null;
			
			Name name = Types.typeName(getURI(), lookupName ); 
			
			for (Iterator itr = iterator(); itr.hasNext();) {
				Name n = (Name) itr.next();
				if (name.equals( n )) return n;
			}
			
            name = Types.typeName(getURI(), lookupName ); 
            
            for (Iterator itr = iterator(); itr.hasNext();) {
                Name n = (Name) itr.next();
                if (name.equals( n )) return n;
            }

            return null;
		}
		public int size() {
			return contents.size();
		}

		public boolean isEmpty() {
			return contents.isEmpty();
		}

		public boolean contains(Object o) {
			return contents.keySet().contains( o );
		}
		public Iterator iterator() {
			return contents.keySet().iterator();
		}
		public Object[] toArray() {
			return contents.keySet().toArray();
		}
		public Object[] toArray(Object[] array ) {
			return contents.keySet().toArray( array );
		}
		public boolean add(Object arg0) {
			throw new UnsupportedOperationException("You may only added a Type directly to the schema");
		}
		public boolean remove(Object o) {
			return contents.keySet().remove( o );
		}
		public boolean containsAll(Collection stuff) {
			return contents.keySet().containsAll( stuff );
		}
		public boolean addAll(Collection arg0) {
			throw new UnsupportedOperationException("You may only added a Type directly to the schema");
		}
		public boolean retainAll(Collection keep) {
			return contents.keySet().retainAll( keep );
		}

		public boolean removeAll(Collection arg0) {
			return contents.keySet().removeAll( arg0  );
		}
		public void clear() {
			contents.clear();
		}		
		public String toString() {
			return contents.keySet().toString();
		}
	};
	
	/** Schema constructed w/ respect to provided URI */
	public SchemaImpl( String uri) {
		super();		
		this.uri = uri;
		this.contents = new HashMap();
	}

	public Set keySet() {
		return namespace();
	}
	public Namespace namespace() {
		return namespace;
	}

	public String toURI() {
		return namespace().getURI();
	}

	public int size() {
		return contents.size();
	}

	public boolean isEmpty() {
		return contents.isEmpty();
	}

	public boolean containsKey(Object key) {
		return contents.containsKey( key );
	}

	public boolean containsValue(Object value) {
		return contents.containsValue( value );
	}

	public Object get(Object key) {
		return contents.get( key );
	}

	public Object put(Object name, Object type) {
		if( !(name instanceof Name) ){
			throw new IllegalArgumentException("Please use a TypeName");
		}
		Name n = (Name) name;
		if( !(n.toString().startsWith(uri.toString() ))){
			throw new IllegalArgumentException("Provided name was not in schema:"+uri );
		}
		if( !(type instanceof AttributeType) ){
			throw new IllegalArgumentException("Please use an AttributeType");
		}
		return contents.put( name, type );
	}

	public Object remove(Object key) {
		return contents.remove( key );
	}

	public void putAll(Map arg0) {
		contents.putAll( arg0 );
	}

	public void clear() {
		contents.clear();
	}

	public Collection values() {
		return contents.values();
	}

	public Set entrySet() {
		return contents.entrySet();
	}
	public int hashCode() {
		return contents.hashCode();
	}
	public boolean equals(Object obj) {
		return contents.equals(obj);
	}
	public String toString() {
		return contents.toString();
	}
	public Schema profile(Namespace profile) {
		return new ProfileImpl(this, profile);
	}
}