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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

/**
 * Swing does not provide a wizard construct (boo hiss) so this is a quick dialog that can step us
 * through a series of pages.
 * <p>
 * This code is based on <a
 * href="http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/">Creating Wizard Dialogs
 * with Java</a>.
 * 
 * @author Jody, gdavis
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/process/JWizard.java $
 */
public class JWizard extends JDialog {
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates that the 'Finish' button was pressed to close the dialog.
     */    
    public static final int FINISH = 0;
    /**
     * Indicates that the 'Cancel' button was pressed to close the dialog, or
     * the user pressed the close box in the corner of the window.
     */    
    public static final int CANCEL = 1;
    /**
     * Indicates that the dialog closed due to an internal error.
     */    
    public static final int ERROR = 2;
    
    Controller controller = new Controller();
    HashMap<String,JPage> model = new HashMap<String, JPage>();
    JPage current;
    
    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    private int returnCode;

    public JWizard( String title ) throws HeadlessException {
        super();
        setTitle( title );
        initComponents();
    }
    public JWizard( Dialog owner, String title )
            throws HeadlessException {
        super(owner, title, true, null  );
        initComponents();
    }

    private void initComponents() {
        // Code omitted
        JPanel buttonPanel = new JPanel();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        backButton = new JButton("Back");
        nextButton = new JButton("Next");
        cancelButton = new JButton("Cancel");

        backButton.addActionListener(controller);
        nextButton.addActionListener(controller);
        cancelButton.addActionListener(controller);

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);
    }
    
    public Boolean isCancelEnabled(){
        return cancelButton == null ? null : cancelButton.isEnabled();
    }    
    public void setCancelEnabled(Boolean isEnabled) {
        Boolean oldValue = cancelButton.isEnabled();        
        if (isEnabled != oldValue) {
            firePropertyChange("isCancelEnabled", oldValue, isEnabled);
            cancelButton.setEnabled( isEnabled );
        }
    }
    public Boolean isNextEnabled(){
        return nextButton == null ? null : nextButton.isEnabled();
    }    
    public void setNextEnabled(Boolean isEnabled) {
        Boolean oldValue = nextButton.isEnabled();        
        if (isEnabled != oldValue) {
            firePropertyChange("isNextEnabled", oldValue, isEnabled);
            nextButton.setEnabled( isEnabled );
        }
    }
    public Boolean isBackEnabled(){
        return backButton == null ? null : backButton.isEnabled();
    }    
    public void setBackEnabled(Boolean isEnabled) {
        Boolean oldValue = backButton.isEnabled();        
        if (isEnabled != oldValue) {
            firePropertyChange("isBackEnabled", oldValue, isEnabled);
            backButton.setEnabled( isEnabled );
        }
    }
    /**
     * Opens the JWizard
     * 
     * @param args
     */
    public static void main( String args[] ) {
        // we need to open up the process wizard as an example...
    }
    
    /**
     * Closes the dialog and sets the return code to the integer parameter.
     * @param code The return code.
     */    
    void close(int code) {
        returnCode = code;
        dispose();
    }
    
    public void windowClosing(WindowEvent e) {
        returnCode = CANCEL;
    }
    
    /**
     * Retrieves the last return code set by the dialog.
     * @return An integer that identifies how the dialog was closed. See the *_RETURN_CODE
     * constants of this class for possible values.
     */    
    public int getReturnCode() {
        return returnCode;
    }
    
    /**
     * Convenience method that displays a modal wizard dialog and blocks until the dialog
     * has completed.
     * @return Indicates how the dialog was closed one of CANCEL, ERROR, FINISH
     */    
    public int showModalDialog() {        
        setModal(true);
        pack();
        setVisible(true);        
        return returnCode;
    }
    
    public void setCurrentPanel(String id) {
        if (id == null){
            close(ERROR);
        }
        JPage old = current;
        
        if (old != null)
            old.aboutToHidePanel();
        
        if( !model.containsKey(id)){
            close(ERROR);
        }
        JPage page = model.get(id);
        if( page == null ) close( ERROR );
        
        current = page;
        syncWizardButtons();
        
        page.aboutToDisplayPanel();
        
        //  Show the panel in the dialog.
        cardLayout.show(cardPanel, id );
        page.displayingPanel();        
    }  
    
    public void syncWizardButtons() {
    	controller.syncButtonsToPage();
	}
	public void registerWizardPanel(JPage page) {
        cardPanel.add(page.getPage(), page.getIdentifier());
        page.setJProcessWizard( this );
        model.put( page.getIdentifier(), page );
        if( page.getIdentifier() == JPage.DEFAULT ){
            setCurrentPanel( page.getIdentifier() );
        }
    }  
    
    /** The controller listens to everything and updates the buttons */
    class Controller implements ActionListener {
        public void actionPerformed( ActionEvent e ) {
            if( e.getSource() == cancelButton || e.getActionCommand().equals( "Canel" ))
                cancelButtonPressed();
            else if (e.getSource() == backButton ||  e.getActionCommand().equals("Back"))
                backButtonPressed();
            else if (e.getSource() == nextButton || e.getActionCommand().equals("Next"))
                nextButtonPressed();
        }
        private void cancelButtonPressed() {            
            close( CANCEL );
        }
        private void nextButtonPressed() {
            //  If it is a finishable panel, close down the dialog. Otherwise,
            //  get the ID that the current panel identifies as the next panel,
            //  and display it.
            
            String nextId = current.getNextPageIdentifier();            
            if (nextId == JPage.FINISH ) {
                close( FINISH );
            } else {        
                setCurrentPanel( nextId  );
            }            
        }
        private void backButtonPressed() {            
            String backId = current.getBackPageIdentifier();        
            setCurrentPanel( backId );        
        }
        void syncButtonsToPage() {
            String backPageId = current.getBackPageIdentifier();
            String nextPageId = current.getNextPageIdentifier();
            
            setBackEnabled( backPageId != null );
            setNextEnabled( nextPageId != null );
            if( nextPageId == JPage.FINISH ){
                nextButton.setText("Finish");
            }
            else {
                nextButton.setText("Next");
            }            
        }
    }
}
