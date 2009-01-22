/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.spatialindex.grid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.caching.spatialindex.Data;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.util.SimpleFeatureMarshaller;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.expression.ThisPropertyAccessorFactory;


/** Associates data with its shape and id, as to be stored in the index.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class GridData implements Data, Externalizable {
    /**
     *
     */
    private static final long serialVersionUID = 2435341100521921266L;
    private static SimpleFeatureMarshaller marshaller = new SimpleFeatureMarshaller();
    
    private Shape shape;
    private Object data;


    public GridData() {
    }

    public GridData(Shape shape, Object data) {
        this.shape = shape;
        this.data = data;
    }
    
    public Object getData() {
        return data;
    }

    public Shape getShape() {
        return shape;
    }

    public int hashCode() {
        int hash = 17;
        hash = (37 * hash) + shape.hashCode();
        hash = (37 * hash) + data.hashCode();

        return hash;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (o instanceof GridData) {
            return shape.equals(((GridData) o).shape) && data.equals(((GridData) o).data);
        } else {
            return false;
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	try {
    		this.shape = (Shape) in.readObject();
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	}

        if (in.readBoolean()) {
            try {
                this.data = this.marshaller.unmarshall(in);
            } catch (IllegalAttributeException e) {
            	e.printStackTrace();
                throw (IOException) new IOException().initCause(e);
            } catch (IOException e) {
            	e.printStackTrace();
            	throw e;
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
        	try {
        		this.data = in.readObject();
        	} catch (IOException e) {
        		e.printStackTrace();
        		throw e;
        	}
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(shape);

        if (data instanceof SimpleFeature) {
            out.writeBoolean(true);
            try{
            this.marshaller.marshall((SimpleFeature) data, out);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        } else {
            out.writeBoolean(false);
            out.writeObject(data);
        }
    }
    
    public static SimpleFeatureMarshaller getFeatureMarshaller(){
        return GridData.marshaller;
    }
}
