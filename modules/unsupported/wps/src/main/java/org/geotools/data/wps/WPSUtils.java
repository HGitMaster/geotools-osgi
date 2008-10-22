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
package org.geotools.data.wps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.geotools.data.Parameter;
import org.geotools.text.Text;
import org.geotools.util.Converters;

import com.vividsolutions.jts.geom.Geometry;

import net.opengis.wps.ComplexDataCombinationsType;
import net.opengis.wps.ComplexDataDescriptionType;
import net.opengis.wps.ComplexDataType;
import net.opengis.wps.DataInputsType;
import net.opengis.wps.DataType;
import net.opengis.wps.ExecuteResponseType;
import net.opengis.wps.InputDescriptionType;
import net.opengis.wps.LiteralDataType;
import net.opengis.wps.LiteralInputType;
import net.opengis.wps.LiteralOutputType;
import net.opengis.wps.OutputDataType;
import net.opengis.wps.OutputDescriptionType;
import net.opengis.wps.ProcessDescriptionType;
import net.opengis.wps.ProcessOutputsType;
import net.opengis.wps.SupportedComplexDataInputType;
import net.opengis.wps.SupportedComplexDataType;
import net.opengis.wps.WpsFactory;

/**
 * Contains helpful static util methods for the WPS module
 * 
 * @author gdavis
 */
public class WPSUtils {
	
	/**
	 * static ints representing the input types
	 */
	public static final int INPUTTYPE_LITERAL = 1;
	public static final int INPUTTYPE_COMPLEXDATA = 2;
	
    /**
     * Creates a DataType input object from the given object and
     * InputDescriptionType (from a describeprocess) and decides if 
     * the input is a literal or complex data based on its type.
	 * 
	 * @param obj the base input object
	 * @param idt input description type defining the input
	 * @return the created DataType input object
     */
    public static DataType createInputDataType(Object obj, InputDescriptionType idt) {
    	int inputtype = 0;
    	
    	// first try to figure out if the input is a literal or complex based
    	// on the data in the idt
    	LiteralInputType literalData = idt.getLiteralData();
    	SupportedComplexDataInputType complexData = idt.getComplexData();
    	if (literalData != null) {
    		inputtype = INPUTTYPE_LITERAL;
    	}
    	else if (complexData != null) {
    		inputtype = INPUTTYPE_COMPLEXDATA;
    	}
    	else {
    		// is the value a literal?  Do a very basic test here for common
        	// literal types.  TODO:  figure out a more thorough test here
    		if (obj instanceof String ||
    				obj instanceof Double ||
    				obj instanceof Float ||
    				obj instanceof Integer ) {
    			inputtype = INPUTTYPE_LITERAL;
    		}
    		else {
    			// assume complex data
    			inputtype = INPUTTYPE_COMPLEXDATA;
    		}
    	}
    	
		// now create the input based on its type
    	String schema = null;
		if (inputtype == INPUTTYPE_COMPLEXDATA) {
			ComplexDataCombinationsType supported = complexData.getSupported();
			ComplexDataDescriptionType cddt = (ComplexDataDescriptionType) supported.getFormat().get(0);
			schema = cddt.getSchema(); 
		}
		
		return createInputDataType(obj, inputtype, schema);
	}	

    /**
     * Creates a DataType input object from the given object, schema and
     * type (complex or literal).
	 * 
	 * @param obj the base input object
	 * @param type the input type (literal or complexdata)
	 * @param schema only used for type complexdata
	 * @return the created DataType input object
     */
    public static DataType createInputDataType(Object obj, int type, String schema) {
    	DataType dt = WpsFactory.eINSTANCE.createDataType();
    	
    	if (type == INPUTTYPE_LITERAL) {
			
			LiteralDataType ldt = WpsFactory.eINSTANCE.createLiteralDataType();
			ldt.setValue(obj.toString());
			dt.setLiteralData(ldt);
		}
		else {
			// assume complex data
			ComplexDataType cdt = WpsFactory.eINSTANCE.createComplexDataType();
			
			// do I need to add a FeatureMap object, or Entry object, or what?
			//EStructuralFeature eStructuralFeature = null;
			//Entry createEntry = FeatureMapUtil.createEntry(eStructuralFeature, obj);
			//cdt.getMixed().add(obj);
			cdt.getData().add(obj);
			
			if (schema != null) {
				cdt.setSchema(schema);
			}
			dt.setComplexData(cdt);
		}
		return dt;
	}
    
    /**
     * Create a map of <String name, Parameter> inputs for a process based on its
     * describeProcess.
     * 
     * @param processDesc
     * @param map add the inputs to the given map (create it if null)
     * @return map of name,Parameter representing the input params for this process
     */
    public static Map<String, Parameter<?>> createInputParamMap(ProcessDescriptionType processDesc, Map<String, Parameter<?>> map) {
        if (map == null) {
            map = new TreeMap<String, Parameter<?>>();
        }
        
        // loop through the process desc and setup each input param
        DataInputsType dataInputs = processDesc.getDataInputs();
        if (dataInputs == null) return null;
        EList inputs = dataInputs.getInput();
        if (inputs == null || inputs.isEmpty()) return null;
        Iterator iterator = inputs.iterator();
        while (iterator.hasNext()) {
            InputDescriptionType idt = (InputDescriptionType) iterator.next();
            // determine if the input is a literal or complex data, and from that
            // find out what type the object should be
            LiteralInputType literalData = idt.getLiteralData();
            SupportedComplexDataInputType complexData = idt.getComplexData();
            Class type = Object.class;
            if (literalData != null) {
                String reference = literalData.getDataType().getReference();
                type = getLiteralTypeFromReference(reference);
            }
            else if (complexData != null) {
                // TODO: get all supported types and determine how to handle that, not just the
                // default.                
                ComplexDataDescriptionType format = complexData.getDefault().getFormat();
                String encoding = format.getEncoding();
                String mimetype = format.getMimeType();
                String schema = format.getSchema();
                if (encoding == null) encoding = "";
                if (mimetype == null) mimetype = "";
                if (schema == null) schema = "";
                type = getComplexType(encoding, mimetype, schema);
            }
            // create the parameter
            boolean required = true;
            if (idt.getMinOccurs().intValue() < 1) required = false;            
            Parameter param = new Parameter(idt.getIdentifier().getValue(), type, 
                    Text.text(idt.getTitle().getValue()),
                    Text.text(idt.getAbstract().getValue()), required, 
                    idt.getMinOccurs().intValue(), idt.getMaxOccurs().intValue(), 
                    null, null);           
            map.put(idt.getIdentifier().getValue(), param);
        }
        
        return map;
    }
    
    /**
     * Create a map of <String name, Parameter> outputs for a process based on its
     * describeProcess.
     * 
     * @param processDesc
     * @param map add the outputs to the given map (create it if null)
     * @return map of name,Parameter representing the output results for this process
     */
    public static Map<String, Parameter<?>> createOutputParamMap(ProcessDescriptionType processDesc, Map<String, Parameter<?>> map) {
        if (map == null) {
            map = new TreeMap<String, Parameter<?>>();
        }
        
        // loop through the process desc and setup each output param
        ProcessOutputsType processOutputs = processDesc.getProcessOutputs();
        if (processOutputs == null) return null;
        EList outputs = processOutputs.getOutput();
        if (outputs == null || outputs.isEmpty()) return null;
        Iterator iterator = outputs.iterator();
        while (iterator.hasNext()) {
            OutputDescriptionType odt = (OutputDescriptionType) iterator.next();
            // determine if the output is a literal or complex data, and from that
            // find out what type the object should be
            LiteralOutputType literalOutput = odt.getLiteralOutput();
            SupportedComplexDataType complexOutput = odt.getComplexOutput();
            Class type = Object.class;
            if (literalOutput != null) {
                String reference = literalOutput.getDataType().getReference();
                type = getLiteralTypeFromReference(reference);
            }
            else if (complexOutput != null) {
                // TODO: get all supported types and determine how to handle that, not just the
                // default.
                ComplexDataDescriptionType format = complexOutput.getDefault().getFormat();
                String encoding = format.getEncoding();
                String mimetype = format.getMimeType();
                String schema = format.getSchema();
                if (encoding == null) encoding = "";
                if (mimetype == null) mimetype = "";
                if (schema == null) schema = "";                
                type = getComplexType(encoding, mimetype, schema);
            }
            // create the parameter         
            Parameter param = new Parameter(odt.getIdentifier().getValue(), type, 
                    Text.text(odt.getTitle().getValue()),
                    Text.text(odt.getAbstract().getValue()) );           
            map.put(odt.getIdentifier().getValue(), param);
        }
        
        return map;
    }    

    /**
     * Take a reference string and determine the literal type based from that
     *
     * @param reference string
     * @return class type it maps to
     */
    private static Class getLiteralTypeFromReference( String reference ) {
        if ((reference.toUpperCase()).contains("DOUBLE")) {
            return Double.class;
        }
        else if ((reference.toUpperCase()).contains("INTEGER")) {
            return Integer.class;
        }     
        else if ((reference.toUpperCase()).contains("FLOAT")) {
            return Float.class;
        }    
        else if ((reference.toUpperCase()).contains("BOOLEAN")) {
            return boolean.class;
        }  
        else if ((reference.toUpperCase()).contains("CHAR")) {
            return Character.class;
        }          
        else if ((reference.toUpperCase()).contains("STRING")) {
            return String.class;
        }                
        
        // default to string
        return String.class;
    }
    
    /**
     * Take the encoding, mimetype and schema and determine the complex type based from that
     *
     * @param encoding string
     * @param mimetype string
     * @param schema string
     * @return class type it maps to
     */    
    private static Class getComplexType( String encoding, String mimetype, String schema ) {
        if ((encoding.toUpperCase()).contains("GML") ||
                (mimetype.toUpperCase()).contains("GML") || 
                (schema.toUpperCase()).contains("GML") ) {
            return Geometry.class;
        }
        else if ((encoding.toUpperCase()).contains("POLYGON") ||
                (mimetype.toUpperCase()).contains("POLYGON") || 
                (schema.toUpperCase()).contains("POLYGON") ) {
            return Geometry.class;
        }
        else if ((encoding.toUpperCase()).contains("POINT") ||
                (mimetype.toUpperCase()).contains("POINT") || 
                (schema.toUpperCase()).contains("POINT") ) {
            return Geometry.class;
        }  
        else if ((encoding.toUpperCase()).contains("LINE") ||
                (mimetype.toUpperCase()).contains("LINE") || 
                (schema.toUpperCase()).contains("LINE") ) {
            return Geometry.class;
        }   
        else if ((encoding.toUpperCase()).contains("RING") ||
                (mimetype.toUpperCase()).contains("RING") || 
                (schema.toUpperCase()).contains("RING") ) {
            return Geometry.class;
        }             
        
        // default to big O
        return Object.class;
    }        
    
    /**
     * Go through the ExecuteResponseType response object and put all the output
     * results into a result map.
     * 
     * @param ert the execute response object
     * @param map the map to store the results in (will be created if null)
     * @return the results in a key,Object map
     */
    public static Map<String, Object> createResultMap(ExecuteResponseType ert, Map<String, Object> map) {
        if (map == null) {
            map = new TreeMap<String, Object>();
        }
    	EList outputs = ert.getProcessOutputs().getOutput();
    	if (outputs == null) return null;
    	Iterator iterator = outputs.iterator();
    	while (iterator.hasNext()) {
    		OutputDataType odt = (OutputDataType) iterator.next();
    		DataType data = odt.getData();
    		ComplexDataType complexData = data.getComplexData();
    		LiteralDataType literalData = data.getLiteralData();
    		if (literalData != null) {
    			// use the converters api to try and create an object of the type
    			// we want (default to the String value if it failed).
    			Object value = literalData.getValue();
    			if (literalData.getDataType() != null) {
	    			Class type = getLiteralTypeFromReference(literalData.getDataType());
	    			Object convertedValue = Converters.convert(literalData.getValue(), type);
	    			if (convertedValue != null) value = convertedValue;
    			}
    			map.put(odt.getIdentifier().getValue(), value);
    		}
    		else if (complexData != null) {
    			// if we have a list of values for this output, store it as a arraylist
    			EList datas = complexData.getData();
    			if (datas.size() > 1) {
        			Iterator iterator2 = datas.iterator();
        			List<Object> values = new ArrayList<Object>();
	    			while (iterator2.hasNext()) {
	    				Object value = iterator2.next();
	    				values.add(value);
	    			}
	    			map.put(odt.getIdentifier().getValue(), values);
    			}
    			else {
    				map.put(odt.getIdentifier().getValue(), datas.get(0));
    			}
    		}
    		
    	}
    	
    	return map;
    }
}
