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
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.geotools.data.Parameter;
import org.geotools.gui.swing.ProgressWindow;
import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.text.Text;
import org.opengis.feature.type.Name;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.process.ProcessException;


/**
 * This page is responsible for actually executing the process with the given
 * parameters and then displaying the result.
 * 
 * @author gdavis
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/process/ProcessRunPage.java $
 */
public class ProcessRunPage extends JPage {
    ProcessFactory factory;
    Name name;
    Map<String, Object> paramMap;

    public ProcessRunPage(ProcessFactory factory, Name name) {
        this(factory, name, null);
    }
    public ProcessRunPage( ProcessFactory factory, Name name, Map<String, Object> params ) {
        super("Run Process");
        this.factory = factory;
        this.name = name;
        this.paramMap = params;
    }
    
    public String getBackPageIdentifier() {   	
    	if (this.factory == null) {
        	ProcessSelectionPage selectionPage = new ProcessSelectionPage();    	
        	this.getJProcessWizard().registerWizardPanel( selectionPage );
            selectionPage.setJProcessWizard(this.getJProcessWizard());
            return selectionPage.getIdentifier();
    	}
        ProcessParameterPage inputPage = new ProcessParameterPage(this.factory);
        this.getJProcessWizard().registerWizardPanel( inputPage );  
        inputPage.setJProcessWizard(this.getJProcessWizard());
        return inputPage.getIdentifier();
    }
    public String getNextPageIdentifier() {
    	return FINISH;
    }
 
	public void aboutToDisplayPanel() {
		
		page.removeAll();
		page.setLayout(new GridLayout(0, 2));
		
		Process process = this.factory.create(name);
		
		final ProgressListener progress = new ProgressWindow(this.getJProcessWizard());	
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(paramMap, progress );
        } catch (ProcessException ex) {
            Logger.getLogger(ProcessRunPage.class.getName()).log(Level.SEVERE, "Error preparing panel", ex);
            return;
        }

		// when we get here, the processing is over so show the result
        JLabel title = new JLabel(factory.getTitle(name).toString());
        page.add(title);
        JLabel description = new JLabel("Your process results are below:");
        page.add(description);
        for( Entry<String, Object> entry : resultMap.entrySet() ) {
        	Parameter< ? > parameter = new Parameter(entry.getKey(), entry.getValue().getClass(), Text.text("Result"), Text.text("Result of process"));
            JLabel label = new JLabel(entry.getKey());
            page.add(label);

            ParamWidget widget;
            if (Double.class.isAssignableFrom( parameter.type )) {
                widget = new JDoubleField(parameter);
            } else if (Geometry.class.isAssignableFrom( parameter.type )) {
                widget = new JGeometryField(parameter);
            }
            else {
                // We got nothing special, let's hope the converter api can deal
                widget = new JField( parameter );
            }
            JComponent field = widget.doLayout();
            widget.setValue(entry.getValue());            
            page.add(field);

        }
    }
}
