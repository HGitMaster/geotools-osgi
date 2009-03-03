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
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.geometry.Envelope2D;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.PanAction;
import org.geotools.gui.swing.ResetAction;
import org.geotools.gui.swing.ZoomInAction;
import org.geotools.gui.swing.ZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Sample application that may be used to try JMapPane 
 * to view WMS Layers
 * 
 * @author Ian Turton
 */
public class WMSViewer implements ActionListener {
	JFrame frame;

	JMapPane mp;

	JToolBar jtb;

	JLabel text;

	GTRenderer renderer;

	MapContext context;

	final JFileChooser jfc = new JFileChooser();

	public WMSViewer() {
		frame = new JFrame("My WMS Viewer");
		frame.setBounds(20, 20, 450, 200);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		mp = new JMapPane();
		// mp.addZoomChangeListener(this);
		content.setLayout(new BorderLayout());
		jtb = new JToolBar();

		JButton load = new JButton("Load file");
		load.addActionListener(this);
		jtb.add(load);
		Action zoomIn = new ZoomInAction(mp);
		Action zoomOut = new ZoomOutAction(mp);
		Action pan = new PanAction(mp);
		// Action select = new SelectAction(mp);
		Action reset = new ResetAction(mp);
		jtb.add(zoomIn);
		jtb.add(zoomOut);
		jtb.add(pan);
		jtb.addSeparator();
		jtb.add(reset);
		jtb.addSeparator();
		// jtb.add(select);
		final JButton button = new JButton();
		button.setText("CRS");
		button.setToolTipText("Change map prjection");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String code = JOptionPane.showInputDialog(button,
						"Coordinate Reference System:", "EPSG:4326");
				if (code == null)
					return;
				try {
					CoordinateReferenceSystem crs = CRS.decode(code);
					setCRS(crs);
				} catch (Exception fe) {
					fe.printStackTrace();
					JOptionPane.showMessageDialog(button, fe.getMessage(), fe
							.getClass().toString(), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
		jtb.add(button);

		content.add(jtb, BorderLayout.NORTH);

		// JComponent sp = mp.createScrollPane();
		mp.setSize(400, 200);
		content.add(mp, BorderLayout.CENTER);

		content.doLayout();
		frame.setVisible(true);

	}

	/**
	 * Method used to set the current map projection.
	 * 
	 * @param crs
	 *            A new CRS for the mappnae.
	 */
	public void setCRS(CoordinateReferenceSystem crs) {
		mp.getContext().setAreaOfInterest(mp.getContext().getAreaOfInterest(),
				crs);
		mp.setReset(true);
		mp.repaint();
	}

	public void load(WMSMapLayer layer) throws Exception {

		Envelope2D env = layer.getGrid().getEnvelope2D();
		Envelope en = new Envelope(env.x, env.y, env.width, env.height);
		mp.setMapArea(en);

		CoordinateReferenceSystem crs = layer.getGrid()
				.getCoordinateReferenceSystem();
		if (crs == null)
			crs = DefaultGeographicCRS.WGS84;
		if (context == null) {
			context = new DefaultMapContext(crs);
			mp.setContext(context);
		}
		
		//this allows us to listen for resize events and ask for the right size image
		mp.addComponentListener(layer);
		context.addLayer(layer);
		context.addMapBoundsListener(layer);
		// System.out.println(context.getLayerBounds());
		// mp.setHighlightLayer(context.getLayer(0));

		if (renderer == null) {
			if (false) {
				renderer = new StreamingRenderer();
				HashMap hints = new HashMap();
				hints.put("memoryPreloadingEnabled", Boolean.TRUE);
				renderer.setRendererHints(hints);
			} else {
				renderer = new StreamingRenderer();
				HashMap hints = new HashMap();
				hints.put("memoryPreloadingEnabled", Boolean.FALSE);
				renderer.setRendererHints(hints);
				RenderingHints rhints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
				((StreamingRenderer)renderer).setJava2DHints(rhints);
			}
			mp.setRenderer(renderer);
			
		}
		// mp.getRenderer().addLayer(new RenderedMapScale());
		frame.repaint();
		frame.doLayout();
	}

	public void actionPerformed(ActionEvent e) {
		WMSChooser wmc = new WMSChooser(frame, "Select WMS", true);
		//wmc.setServer("http://www.geovista.psu.edu/geoserver/wms");
		wmc.setVisible(true);
		int returnVal = wmc.getLayer();
		if (returnVal == -1) {
			return;
		}
		WebMapServer wms = wmc.getWms();
		Layer l = (Layer) wmc.getLayers().get(returnVal);
		WMSMapLayer layer = new WMSMapLayer(wms, l);
		try {
			load(layer);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		WMSViewer mapV = new WMSViewer();

	}
}
