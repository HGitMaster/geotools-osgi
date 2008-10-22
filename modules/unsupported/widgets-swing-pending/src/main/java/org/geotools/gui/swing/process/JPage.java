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

import java.util.Map;

import javax.swing.JPanel;

/**
 * This is a descriptor; identifying a page by id (and lazily creating it as needed).
 * 
 * @author Jody, gdavis
 */
public abstract class JPage {
    /**
     * Used to indicate which page we should start with.
     */
    public static final String DEFAULT = "Default";

    /**
     * Used to indicate that we are done and the wizard should close
     */
    public static final String FINISH = "Finish";
    
    /**
     * Used to indicate that there is a next step to complete
     */
    public static final String NEXT = "Next";    

    protected final JPanel page;
    protected final String identifier;
    /**
     * Wizard hosting this process page; we will access wizard.model directly to look up our friends
     * for next and previous.
     */
    private JWizard wizard;
    /**
     * Create a default page.
     */
    public JPage() {
        this( DEFAULT );
    }

    /**
     * Create a default page.
     */
    public JPage(String id ) {
        this( id, new JPanel() );
    }
    
    /**
     * Create a page.
     * 
     * @param id identifier
     * @param panel JPanel to use as wizard page
     */
    public JPage( String id, JPanel page ) {
        identifier = id;
        this.page = page;
    }

    public final JPanel getPage() {
        return page;
    }

    public final String getIdentifier() {
        return identifier;
    }

    final void setJProcessWizard( JWizard w ) {
        wizard = w;
    }

    public final JWizard getJProcessWizard() {
        return wizard;
    }
    public Map<String, JPage> getModel() {
        return wizard.model;
    }

    /**
     * Identifier of the panel to use Next.
     * 
     * @return Return id of the next JProcessPage or null if next should be disabled. You can use
     *         FINISH to indicate the wizard is complete and may be closed.
     */
    public abstract String getNextPageIdentifier();

    /**
     * Identifier of the panel to use Back.
     * 
     * @return Return id of the next JProcessPage or null if next should be disabled.
     */
    public abstract String getBackPageIdentifier();

    /**
     * Called just before the panel is to be displayed.
     */
    public void aboutToDisplayPanel() {
    }

    /**
     * Override this method to perform functionality when the panel itself is displayed.
     */
    public void displayingPanel() {

    }

    /**
     * Override this method to perform functionality just before the panel is to be hidden.
     */
    public void aboutToHidePanel() {

    }

}
