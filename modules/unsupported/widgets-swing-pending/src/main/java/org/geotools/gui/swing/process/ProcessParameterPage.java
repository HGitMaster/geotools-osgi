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
package org.geotools.gui.swing.process;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.geotools.data.Parameter;
import org.geotools.process.ProcessFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This page is responsible making a user interface based on the provided ProcessFactory.
 * 
 * @author Jody, gdavis
 */
public class ProcessParameterPage extends JPage {
    ProcessFactory factory;
    Map<String, Object> input;
    
    // map of ParamWidget to keys, but value is a list of ParamWidgets
    Map<String, List<ParamWidget>> fields = new HashMap<String, List<ParamWidget>>();
    
    // map of keys with values for the input parameters, the value is a list of "values"
    Map<String, Object> paramMap = null;

    public ProcessParameterPage(ProcessFactory factory) {
        this(factory, null);
    }
    public ProcessParameterPage( ProcessFactory factory, Map<String, Object> input ) {
        super("Enter Params");
        this.factory = factory;
    }

    public void setProcessFactory(ProcessFactory factory) {
    	this.factory = factory;
    }  
    public ProcessFactory getProcessFactory() {
    	return this.factory;
    }        
    public String getBackPageIdentifier() {   	
    	ProcessSelectionPage selectionPage = new ProcessSelectionPage();    	
    	this.getJProcessWizard().registerWizardPanel( selectionPage );
        selectionPage.setJProcessWizard(this.getJProcessWizard());
        return selectionPage.getIdentifier();
    }
    public String getNextPageIdentifier() {
    	createParamMap();
    	// validate the params first...
//    	if (paramMap == null) {
//    		return null;
//    	}
    	ProcessRunPage resultPage = new ProcessRunPage(factory, paramMap);    	
    	this.getJProcessWizard().registerWizardPanel( resultPage );
    	resultPage.setJProcessWizard(this.getJProcessWizard());
        return resultPage.getIdentifier();
    }
    /**
     * Run through the current values in the widgets and make a process param map
     * out of them for running an actual process
     */
    private void createParamMap() {
    	if (fields.size() == 0) {
    		return;
    	}
        paramMap = new HashMap<String, Object>();
        for (String key : fields.keySet()) {
        	List<ParamWidget> pws = (List<ParamWidget>) (fields.get(key));
        	if (pws.size() > 1) {
        		// param has a list of values from multiple widgets
        		List<Object> values = new ArrayList<Object>();
        		 for (ParamWidget pw : pws) {
        			 if (pw.getValue() != null) {
        				 values.add(pw.getValue());
        			 }
        		 }
        		 paramMap.put( key, values );
        	}
        	else { // only a single value/widget
        		ParamWidget paramWidget = pws.get(0);
        		if (paramWidget.getValue() != null) {
        				paramMap.put( key, paramWidget.getValue() );
        		}
        	}

        }
	}
	public void aboutToDisplayPanel() {
        page.removeAll();
        page.setLayout(new GridLayout(0, 2));

        JLabel title = new JLabel(factory.getTitle().toString());
        page.add(title);
        JLabel description = new JLabel(factory.getDescription().toString());
        page.add(description);
        for( Entry<String, Parameter< ? >> entry : factory.getParameterInfo().entrySet() ) {
            Parameter< ? > parameter = entry.getValue();
            
            // if the minOccurs of the param is -1, it can be any number so default
            // with 1 for now.
            int min = parameter.minOccurs;
            if (min < 1) min = 1;
            
            // if the maxOccurs of the parameter is -1, then it can have any number
            // of widgets equal to or greater than the minOccurs.  So add + buttons
            if (parameter.maxOccurs == -1) {
            	createAddButton(parameter);
            }
            
            // create a list for the widgets this parameter (even if there is only 
            // one widget) 
            List<ParamWidget> widgets = new ArrayList<ParamWidget>();
            
            // loop through and create the min number of widgets for this param
            for (int i=0; i<min; i++) {
            	ParamWidget newWidget = createNewField(parameter, true);
	            widgets.add(newWidget);
            }
            // add the widget(s) to the fields map
        	fields.put( parameter.key, widgets );
        }
    }
	private void createAddButton(final Parameter<?> parameter) {
		JLabel buttLabel;
		buttLabel = new JLabel("Press '+' to add a new " + parameter.title + " field: ");
		page.add(buttLabel);
		
		JButton butt = new JButton("+");
		butt.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	createNewField(parameter, true);
		    }
		});
		page.add(butt);
	}
	
	/**
	 * Create a new widget and label for the given parameter
	 * @param parameter
	 * @param resize whether to resize the wizard after adding or not
	 */
	private ParamWidget createNewField(Parameter<?> parameter, boolean resize) {
    	JLabel label;
        label = new JLabel(parameter.title.toString());
        page.add(label);
        
        ParamWidget widget;
        if (Double.class.isAssignableFrom( parameter.type )) {
            widget = new JDoubleField(parameter);
        } else if (Geometry.class.isAssignableFrom( parameter.type)){
            widget = new JGeometryField(parameter);
        }
        else {
            // We got nothing special, let's hope the converter api can deal
            widget = new JField( parameter );
        }
        JComponent field = widget.doLayout();
        page.add(field);
        page.validate();
        
        // resize the wizard to fit new component
        if (resize) {
        	getJProcessWizard().pack();
        	getJProcessWizard().validate();
        }
     
        return widget;
	}
}
