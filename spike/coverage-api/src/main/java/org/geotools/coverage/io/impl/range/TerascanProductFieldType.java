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
package org.geotools.coverage.io.impl.range;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.measure.Measure;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.FieldType;
import org.geotools.feature.NameImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

/**
 * Skeletal implementation  of a {@link FieldType} which can be used to map
 * products as produced by the  Terascan software
 * 
 * <p>
 * This sample implementation should be extended in the future in order to 
 * account for different products as well as for different source (APS, 
 * probably netCDF-CF and the like).
 * 
 * @author Simone Giannecchini, GeoSolutions 
 * 
 * TODO: Improve the mapping pairs.
 *         (leveraging on class) 
 * TODO: Improve the contents leveraging on some
 *         Standard like, as an instance: UCUM or NetCDF CF Standard Names
 *         table: http://cf-pcmdi.llnl.gov/documents/cf-standard-names/
 */
public class TerascanProductFieldType implements FieldType {
	
	/**
	 * Simple class to hold basic descriptions and {@link Unit} for a certain product.
	 * 
	 * <p>
	 * In the future it would be nice to extend this class in order to use to make
	 * mappings between standard name tables like the netCDF-CF one.
	 * 
	 * @author Simone Giannecchini, GeoSolutions
	 *
	 */
	public static class Product{
		private String id;
		
		private Unit<?> UoM;
		
		private String description;

		public Product(String id, Unit<?> uoM,
				String description) {
			super();
			this.id = id;
			UoM = uoM;
			this.description = description;
		}

		public String getId() {
			return id;
		}

		public Unit<?> getUoM() {
			return UoM;
		}

		public String getDescription() {
			return description;
		}
		
	}
	
	private final static HashMap<String, Product> productMap;


    static {
        productMap = new HashMap<String, TerascanProductFieldType.Product>();
        //TODO add more products
        productMap.put("mcsst",new TerascanProductFieldType.Product("mcsst",SI.CELSIUS,""));
        productMap.put("lowcloud",new TerascanProductFieldType.Product("lowcloud",Unit.ONE,""));
    }

    public static boolean isSupportedProduct(final String product) {
        return productMap.containsKey(product);
    }


	public static Product getProduct(final String product){
        if (isSupportedProduct(product))
                return productMap.get(product);
            
        throw new IllegalArgumentException("Unsupported or null product");
    }    
    
    private static SimpleScalarAxis createSimpleScalarAxis(
    		final Name productName,
    		final InternationalString productDescription){
    	
    	return new SimpleScalarAxis(
    			new NameImpl(new StringBuilder("axis:").append(productName.toString()).toString()),
    			new SimpleInternationalString(new StringBuilder("axis for product:").append(productDescription.toString()).toString()));
    }
	
	private BaseFieldType wrappedFieldType;

	public  TerascanProductFieldType(
			final String productName,
			final SampleDimension sd){
			this(
					new NameImpl(productName),
					new SimpleInternationalString(getProduct(productName).getDescription()), 
					getProduct(productName).getUoM(), 
					sd);
		}
	
	public  TerascanProductFieldType(
		final Name name,
		final InternationalString description,
		final Unit<?> UoM,
		final SampleDimension sd){
		this(
				name, 
				description, 
				UoM, 
				createSimpleScalarAxis(name,description),
				sd);
	}
	
	public  TerascanProductFieldType(
			final Name name,
			final InternationalString description,
			final SampleDimension sd){
			this(
					name, 
					description, 
					sd.getUnits(), 
					createSimpleScalarAxis(name,description),
					sd);
		}	
	
	/**
	 * 
	 */
	public  TerascanProductFieldType(
			final Name name,
			final InternationalString description,
			final Unit<?>  UoM,
			final SimpleScalarAxis axis,
			final SampleDimension sd) {
		

		this.wrappedFieldType= new BaseFieldType(name,description,UoM,Collections.singletonList(axis),Collections.singletonMap(axis.getKey(0), sd));
	}
	
	public  TerascanProductFieldType(
			final Name name,
			final InternationalString description,
			final SimpleScalarAxis axis,
			final SampleDimension sd) {
		
		this(name,description,sd.getUnits(),axis,sd);
	}



	/* (non-Javadoc)
	 * @see org.geotools.coverage.io.range.FieldType#getAxes()
	 */
	public List<Axis<?, ?>> getAxes() {
		return  this.wrappedFieldType.getAxes();
	}

	/* (non-Javadoc)
	 * @see org.geotools.coverage.io.range.FieldType#getAxesNames()
	 */
	public List<Name> getAxesNames() {
		return this.wrappedFieldType.getAxesNames();
	}

	/* (non-Javadoc)
	 * @see org.geotools.coverage.io.range.FieldType#getAxis(org.opengis.feature.type.Name)
	 */
	public Axis<?, ?> getAxis(Name name) {
		
		return this.wrappedFieldType.getAxis(name);
	}

	/* (non-Javadoc)
	 * @see org.geotools.coverage.io.range.FieldType#getDescription()
	 */
	public InternationalString getDescription() {
		
		return this.wrappedFieldType.getDescription();
	}


	/* (non-Javadoc)
	 * @see org.geotools.coverage.io.range.FieldType#getName()
	 */
	public Name getName() {
		
		return this.wrappedFieldType.getName();
	}


	/* (non-Javadoc)
	 * @see org.geotools.coverage.io.range.FieldType#getSampleDimensions()
	 */
	public Set<SampleDimension> getSampleDimensions() {
		
		return this.wrappedFieldType.getSampleDimensions();
	}

	/* (non-Javadoc)
	 * @see org.geotools.coverage.io.range.FieldType#getUnitOfMeasure()
	 */
	public Unit<?> getUnitOfMeasure() {
		return this.wrappedFieldType.getUnitOfMeasure();
	}

	public SampleDimension getSampleDimension(Measure<?,?> key) {
		return this.wrappedFieldType.getSampleDimension(key);
	}


	public String toString(){
	    return this.wrappedFieldType.toString();
	}
}
