/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.swing.process;

import javax.swing.JLabel;

import org.geotools.gui.swing.process.JPage;
import org.geotools.gui.swing.process.JWizard;

/**
 * This is a quick example to show how JProcessWizard works.
 * <p>
 * While having a Swing wizard is fun and everything; we would rather make
 * use of an existing library for this stuff (so if you can recommend something
 * let us know). In the meantime we need this class to show how process
 * parameters can be handled.
 * <p>
 * @author Jody
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/demo/swing/process/JProcessWizardExample.java $
 */
public class JProcessWizardExample {
    public static void main( String args[] ){
        JWizard wizard = new JWizard("JProcessWizard Example");
        wizard.registerWizardPanel( new JPage(){
            public String getBackPageIdentifier() {
                return null; // first page cannot go back
            }
            public String getNextPageIdentifier() {
                return "page2";
            }
            public void aboutToDisplayPanel() {
                page.removeAll();
                page.add( new JLabel("Default Page"));
            }
        });
        wizard.registerWizardPanel( new JPage("page2"){
            public String getBackPageIdentifier() {
                return DEFAULT; // first page cannot go back
            }
            public String getNextPageIdentifier() {
                return FINISH;
            }
            public void aboutToDisplayPanel() {
                page.removeAll();                
                page.add( new JLabel("Page 2"));
            }
            
        }); 
        System.out.println("Show wizard "+wizard.getTitle());
        int result = wizard.showModalDialog();
        System.out.print("Wizard completed with:");
        switch( result ){
        case JWizard.CANCEL: System.out.println( "CANEL" ); break;
        case JWizard.FINISH: System.out.println( "FINISH" ); break;
        case JWizard.ERROR: System.out.println( "ERROR" ); break;
        default:
            System.out.println( "unexpected "+ result );
        }        
    }
    
}
