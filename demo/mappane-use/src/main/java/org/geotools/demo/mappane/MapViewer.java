/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.mappane;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.PanAction;
import org.geotools.gui.swing.ResetAction;
import org.geotools.gui.swing.SelectAction;
import org.geotools.gui.swing.ZoomInAction;
import org.geotools.gui.swing.ZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Sample application that may be used to try JMapPane from the command line.
 *
 * @author Ian Turton
 */
public class MapViewer implements ActionListener{
    JFrame frame;
    JMapPane mp;
    JToolBar jtb;
    JLabel text;
    final JFileChooser jfc = new JFileChooser();
    public MapViewer(){
        frame=new JFrame("My Map Viewer");
        frame.setBounds(20,20,450,200);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container content = frame.getContentPane();
        mp = new JMapPane();
        //mp.addZoomChangeListener(this);
        content.setLayout(new BorderLayout());
        jtb = new JToolBar();

        JButton load = new JButton("Load file");
        load.addActionListener(this);
        jtb.add(load);
        Action zoomIn = new ZoomInAction(mp);
        Action zoomOut = new ZoomOutAction(mp);
        Action pan = new PanAction(mp);
        Action select = new SelectAction(mp);
        Action reset = new ResetAction(mp);
        jtb.add(zoomIn);
        jtb.add(zoomOut);
        jtb.add(pan);
        jtb.addSeparator();
        jtb.add(reset);
        jtb.addSeparator();
        jtb.add(select);
        final JButton button= new JButton();
        button.setText("CRS");
        button.setToolTipText("Change map prjection");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            	String code = JOptionPane.showInputDialog( button, "Coordinate Reference System:", "EPSG:4326" );
            	if(code==null)return;
            	try{
             	   CoordinateReferenceSystem crs = CRS.decode( code );
                   setCRS( crs );
                }
                catch(Exception fe){
                	fe.printStackTrace();
             	   JOptionPane.showMessageDialog( button, fe.getMessage(), fe.getClass().toString(), JOptionPane.ERROR_MESSAGE );
             	   return;
                }
            }
        });
        jtb.add(button);

        content.add(jtb,BorderLayout.NORTH);


        //JComponent sp = mp.createScrollPane();
        mp.setSize(400,200);
        content.add(mp,BorderLayout.CENTER);

        content.doLayout();
        frame.setVisible(true);

    }

    /**
     * Method used to set the current map projection.
     *
     * @param crs A new CRS for the mappnae.
     */
    public void setCRS(CoordinateReferenceSystem crs){
    	mp.getContext().setAreaOfInterest(mp.getContext().getAreaOfInterest(),crs);
    	mp.setReset(true);
    	mp.repaint();
   }

    public void load(URL shape, URL sld)throws Exception{
        ShapefileDataStore ds = new ShapefileDataStore(shape);

        FeatureSource<SimpleFeatureType, SimpleFeature> fs = ds.getFeatureSource();
        com.vividsolutions.jts.geom.Envelope env = fs.getBounds();
        mp.setMapArea(env);
        StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);

        SLDParser stylereader = new SLDParser(factory,sld);
        org.geotools.styling.Style[] style = stylereader.readXML();

        CoordinateReferenceSystem crs = fs.getSchema().getCoordinateReferenceSystem();
        if(crs==null)crs=DefaultGeographicCRS.WGS84;
        MapContext context = new DefaultMapContext(crs);
        context.addLayer(fs,style[0]);
        context.getLayerBounds();
        mp.setHighlightLayer(context.getLayer(0));
        mp.setSelectionLayer(context.getLayer(0));

        GTRenderer renderer;
        if( true ){ 
        	renderer = new StreamingRenderer();
        	HashMap hints = new HashMap();
        	hints.put("memoryPreloadingEnabled", Boolean.TRUE);
            renderer.setRendererHints( hints );
        }
        else {
        	renderer = new StreamingRenderer();
        	HashMap hints = new HashMap();
        	hints.put("memoryPreloadingEnabled", Boolean.FALSE);
            renderer.setRendererHints( hints );
        }
        mp.setRenderer(renderer);
        mp.setContext(context);


//        mp.getRenderer().addLayer(new RenderedMapScale());
        frame.repaint();
        frame.doLayout();
    }
    public static URL aquireURL( String target ){
    	if( new File( target ).exists() ){
        	try {
				return new File( target ).toURL();
			} catch (MalformedURLException e) {
			}
        }
    	try {
			return new URL( target );
		} catch (MalformedURLException e) {
        	return null;
        }
    }
    public void actionPerformed(ActionEvent e) {
		int returnVal = jfc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            String pathname = jfc.getSelectedFile().getAbsolutePath();
            URL shape = aquireURL( pathname );
            if( shape == null ){
            	JOptionPane.showMessageDialog( frame, "could not find file \""+pathname+"\"", "Could not find file", JOptionPane.ERROR_MESSAGE );
        		System.err.println("Could not find shapefile: "+pathname);
            	return;
            }
            String filepart = pathname.substring(0, pathname.lastIndexOf("."));
            URL sld = aquireURL( filepart+".sld" );
            if( sld == null){
            	JOptionPane.showMessageDialog( frame, "could not find SLD file \""+filepart+".sld\"", "Could not find SLD file", JOptionPane.ERROR_MESSAGE );
            	System.err.println("Could not find sld file: "+filepart+".sld");
            	return;
            }
            try {
				this.load( shape, sld );
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
    	MapViewer mapV = new MapViewer();
        if( args.length==0 || !args[0].toLowerCase().endsWith(".shp")){
        	/*System.out.println("java org.geotools.gui.swing.MapViewer shapefile.shp");
        	System.out.println("Notes:");
        	System.out.println(" Any provided shapefile.prj file or shapefile.sld will be used");
        	System.exit(0);*/
        }else {
        String pathname = args[0];
        URL shape = aquireURL( pathname );
        if( shape == null ){
    		System.err.println("Could not find shapefile: "+pathname);
        	System.exit(1);
        }
        String filepart = pathname.substring(0, pathname.lastIndexOf("."));
        URL sld = aquireURL( filepart+".sld" );
        if( sld == null){
        	System.err.println("Could not find sld file: "+filepart+".sld");
        	System.exit(1);
        }
        mapV.load( shape, sld );
        }
    }
}
