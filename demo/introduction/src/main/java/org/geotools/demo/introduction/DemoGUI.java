/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.introduction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.action.JMapPanePanAction;
import org.geotools.gui.swing.action.JMapPaneResetAction;
import org.geotools.gui.swing.action.JMapPaneZoomInAction;
import org.geotools.gui.swing.action.JMapPaneZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * The DemoGUI class which produces the top level JFrame for the introductory 
 * demo application. 
 * 
 * @author  Adrian Custer
 * 
 * @version 0.03
 * @since   2.3-M0
 *
 */
public class DemoGUI {
    
    /* A link to the DemoBase instance, to pass control back to the 'button*'
     * methods in that instance. */
    final DemoBase demoBase;
    
    /* GUI frame, pane and extras */
    final JFrame frame;
    JPanel visPanel;
    ScrollPane infoSP;
    JToolBar jtb;
    JLabel text;
    JButton quitButton;
    JButton createButton;
    JButton styleButton;
    JButton renderButton;
    JButton projectButton;
    JButton filterButton;
    JButton captureButton;
    JButton saveButton;
    JButton commitButton;
    JButton analyzeButton;
    JTextArea textArea;
    
    /* Display elements */
    JMapPane jmp;
    MapContext context;
    GTRenderer renderer;
    
    com.vividsolutions.jts.geom.Envelope worldbounds;
    
    
    /*
     * Create the Demo's GUI.
     * 
     * Geotools users can skip this swing code, go to create_FeatureSource...
     * 
     */
    public DemoGUI(DemoBase db) {

        this.demoBase = db; 
        
        frame=new JFrame("Geotools Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBounds(20,20,800,500);
//        frame.setBackground(Color.cyan);
        
        Container contentPane = frame.getContentPane();
        BoxLayout layout = new BoxLayout(contentPane, BoxLayout.X_AXIS);
        contentPane.setLayout(layout);
//        contentPane.setBackground(Color.red);
        
        JPanel buttonPanel = new JPanel();
//        buttonPanel.setBackground(Color.blue);
        buttonPanel.setVisible(true);
        visPanel = new JPanel();
//        visPanel.setBackground(Color.gray);
        visPanel.setVisible(true);
        
        contentPane.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPane.add(buttonPanel);
        contentPane.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPane.add(visPanel);
        contentPane.add(Box.createRigidArea(new Dimension(10, 0)));
        
        /* The action button Panel */
        JPanel actionButtonPanel = new JPanel();
//        actionButtonPanel.setBackground(Color.green);
        actionButtonPanel.setVisible(true);
        
        BoxLayout aBPlayout = new BoxLayout(actionButtonPanel, BoxLayout.Y_AXIS);
        actionButtonPanel.setLayout(aBPlayout);
        
        
        int BUTTON_WIDTH = 100;
        createButton = new JButton("1. Create Features");
        createButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(createButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        styleButton = new JButton("2. Style Features");
        styleButton.setEnabled(false);
        styleButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(styleButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        renderButton = new JButton("3. Render Map");
        renderButton.setEnabled(false);
        renderButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(renderButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        projectButton = new JButton("4. Project Map");
        projectButton.setEnabled(false);
        projectButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(projectButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        /*
        filterButton = new JButton("5. Filter Features");
        filterButton.setEnabled(false);
        filterButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(filterButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        captureButton = new JButton("6. Capture Image");
        captureButton.setEnabled(false);
        captureButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(captureButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        saveButton = new JButton("7. Save to file");
        saveButton.setEnabled(false);
        saveButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(saveButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        commitButton = new JButton("8. Commit to WFS");
        commitButton.setEnabled(false);
        commitButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(commitButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        analyzeButton = new JButton("9. Analyze network");
        analyzeButton.setEnabled(false);
        analyzeButton.setMinimumSize(new Dimension(BUTTON_WIDTH,1));
        actionButtonPanel.add(analyzeButton);
        */
        
        /* The button Panel */
        BoxLayout buttonPanelBoxLayout = new BoxLayout(buttonPanel,BoxLayout.Y_AXIS);
        buttonPanel.setLayout(buttonPanelBoxLayout);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        //TODO: verify the file can be found
        java.net.URL imgURL = 
            DemoGUI.class.getResource("GeotoolsBoxLogo.png");
//        System.out.println(imgURL);
        ImageIcon icon = new ImageIcon(imgURL,"The Geotools Logo");
        JLabel iconLabel = new JLabel(icon);
        buttonPanel.add(iconLabel);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(actionButtonPanel);
        buttonPanel.add(Box.createVerticalGlue());
        JButton quitButton = new JButton("QUIT");
        buttonPanel.add(quitButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                frame.dispose();
                
              }
          });
        
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                createButton.setEnabled(false);
                demoBase.buttonCreateFeatures();
                styleButton.setEnabled(true);
              }
          });
        styleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                styleButton.setEnabled(false);
                demoBase.buttonCreateStyles();
                renderButton.setEnabled(true);
              }
          });
        renderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                renderButton.setEnabled(false);
                demoBase.buttonCreateMap();
                projectButton.setEnabled(true);
              }
          });
        projectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                projectButton.setEnabled(false);
                demoBase.buttonProjectMap();
                //DemoApp.filterButton.setEnabled(true);
                
              }
          });
/*
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                filterButton.setEnabled(false);
                
                captureButton.setEnabled(true);
              }
          });
        captureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                captureButton.setEnabled(false);
//              DemoBase.captureImage();
                saveButton.setEnabled(true);
              }
          });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                saveButton.setEnabled(false);
                
                commitButton.setEnabled(true);
                
              }
          });
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                commitButton.setEnabled(false);
                
                analyzeButton.setEnabled(true);
                
              }
          });
        analyzeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                analyzeButton.setEnabled(false);
                
              }
          });
*/
        
        /* The info Text Area */
        textArea = new JTextArea();
        textArea.append("Welcome to the Geotools Demo.\n\n");
        textArea.append("Click on the \"Create\" button to start.\n\n");
        infoSP = new ScrollPane();
        infoSP.add(textArea);
        
	//TOOD: use a Logger to output to the textArea. Not sure how to do this
	//without using classes: annonymous inner class?
//        OutputStream os = new  anOutputStream() extends OutputStream {
//              public void write( int b ) throws IOException {
//                  // append the data as characters to the JTextArea control
//                  DemoApp.textArea.append( String.valueOf( ( char )b ) );
//              }
//        };
//        StreamHandler sh = new StreamHandler(os , new Formatter());
        
        /* The visuals Panel */
        BoxLayout visPanelBoxLayout = new BoxLayout(visPanel,BoxLayout.Y_AXIS);
        visPanel.setLayout(visPanelBoxLayout);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        visPanel.add(infoSP);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        
        contentPane.setVisible(true);
        contentPane.doLayout();
        frame.doLayout();
        frame.setVisible(true);
        frame.repaint();
        
        
    }
    
    
    
    
    
    /*
     * Create a GUI map displayer.
     * 
     * This is all Swing stuff for the JMapPane.
     * 
     */
    public void initialize_JMapPane(){
        textArea.append("Start: Initialize the GUI.\n");

        Panel mapGUI = new Panel();
        mapGUI.setLayout(new BorderLayout());
        jmp = new JMapPane();
        jmp.setBackground(Color.white);
        
        /* Renderer */
        renderer = new StreamingRenderer();
        
        /* Context */
        context = new DefaultMapContext(DefaultGeographicCRS.WGS84); 
        context.setAreaOfInterest(demoBase.envNoEdges);
        
        /* Add to JMapPane */
        jmp.setRenderer(renderer);
        jmp.setContext(context);
        
        
        
        /* The toolbar */
        jtb = new JToolBar();

        jtb.addSeparator();
        
        JButton resetBtn = new JButton(new JMapPaneResetAction(jmp));
        jtb.add(resetBtn);
        
        jtb.addSeparator();
        
        ButtonGroup cursorGrp = new ButtonGroup();
        JToggleButton zoomInBtn = new JToggleButton(new JMapPaneZoomInAction(jmp));
        jtb.add(zoomInBtn);
        cursorGrp.add(zoomInBtn);
        
        JToggleButton zoomOutBtn = new JToggleButton(new JMapPaneZoomOutAction(jmp));
        jtb.add(zoomOutBtn);
        cursorGrp.add(zoomOutBtn);

        JToggleButton panBtn = new JToggleButton(new JMapPanePanAction(jmp));
        jtb.add(panBtn);
        cursorGrp.add(panBtn);

        jtb.addSeparator();
        
        final JButton crsBtn = new JButton("CRS");
        crsBtn.setToolTipText("Change map prjection");
        crsBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

              String code = JOptionPane.showInputDialog( crsBtn, "Coordinate Reference System:", "EPSG:4326" ); 
              try{
                 CoordinateReferenceSystem crs = CRS.decode( code );
                 jmp.getContext().setAreaOfInterest(jmp.getContext().getAreaOfInterest(),crs);
                 jmp.reset();
                   
                }
                catch(FactoryException fe){
                 JOptionPane.showMessageDialog( crsBtn, fe.getMessage(), fe.getClass().toString(), JOptionPane.ERROR_MESSAGE );
                 return;
                }
            }
        });
        jtb.add(crsBtn);

        mapGUI.add(jtb,BorderLayout.NORTH);
        mapGUI.add(jmp);
        
        infoSP.setSize(new Dimension(300,60));        
        BoxLayout visPanelBoxLayout = new BoxLayout(visPanel,BoxLayout.Y_AXIS);
        visPanel.setLayout(visPanelBoxLayout);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        visPanel.add(infoSP);
	    visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        visPanel.add(mapGUI);
        visPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        frame.getContentPane().doLayout();
        infoSP.setSize(new Dimension(3,3));
        frame.getContentPane().doLayout();
        frame.setVisible(true);

        textArea.append("  End: Initialized the GUI.\n");
        
    }
    
}
