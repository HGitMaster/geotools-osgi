/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.collections.map.SingletonMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.JContextTreePopup;
import org.geotools.gui.swing.contexttree.TreeContextEvent;
import org.geotools.gui.swing.contexttree.TreeContextListener;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.SelectionTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.node.SourceGroup;
import org.geotools.gui.swing.contexttree.node.StyleGroup;
import org.geotools.gui.swing.contexttree.popup.ContextActiveItem;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyItem;
import org.geotools.gui.swing.contexttree.popup.CopyItem;
import org.geotools.gui.swing.contexttree.popup.CutItem;
import org.geotools.gui.swing.contexttree.popup.DeleteItem;
import org.geotools.gui.swing.contexttree.popup.DuplicateItem;
import org.geotools.gui.swing.contexttree.popup.LayerFeatureItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotools.gui.swing.contexttree.popup.LayerVisibilityItem;
import org.geotools.gui.swing.contexttree.popup.LayerZoomItem;
import org.geotools.gui.swing.contexttree.popup.PasteItem;
import org.geotools.gui.swing.contexttree.popup.RuleMaxScaleItem;
import org.geotools.gui.swing.contexttree.popup.RuleMinScaleItem;
import org.geotools.gui.swing.contexttree.popup.SeparatorItem;
import org.geotools.gui.swing.datachooser.DataPanel;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JFileDataPanel;
import org.geotools.gui.swing.datachooser.JOracleDataPanel;
import org.geotools.gui.swing.datachooser.JPostGISDataPanel;
import org.geotools.gui.swing.datachooser.JWFSDataPanel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.decoration.ColorDecoration;
import org.geotools.gui.swing.map.map2d.decoration.ImageDecoration;
import org.geotools.gui.swing.map.map2d.decoration.InformationDecoration.LEVEL;
import org.geotools.gui.swing.map.map2d.stream.JStreamEditMap;
import org.geotools.gui.swing.map.map2d.stream.SelectableMap2D;
import org.geotools.gui.swing.map.map2d.stream.StreamingMap2D;
import org.geotools.gui.swing.map.map2d.stream.control.JStreamInfoBar;
import org.geotools.gui.swing.map.map2d.stream.control.JStreamNavigationBar;
import org.geotools.gui.swing.map.map2d.stream.control.JStreamSelectionBar;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.gui.swing.propertyedit.LayerCRSPropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerFilterPropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerGeneralPanel;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPane;
import org.geotools.gui.swing.propertyedit.filterproperty.JCQLPropertyPanel;
import org.geotools.gui.swing.propertyedit.styleproperty.JSimpleStylePanel;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 *
 * @author sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/demo/widgets/StreamWidgetsDemo.java $
 */
public class StreamWidgetsDemo extends JFrame{

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private final OpacityTreeTableColumn colOpacity = new OpacityTreeTableColumn();
    private final VisibleTreeTableColumn colVisible = new VisibleTreeTableColumn();
    private final StyleTreeTableColumn colStyle = new StyleTreeTableColumn();
    private final SelectionTreeTableColumn colSelection = new SelectionTreeTableColumn(null);
    private final SourceGroup subsource = new SourceGroup();
    private final StyleGroup substyle = new StyleGroup();
    private final ImageDecoration overBackImage = new ImageDecoration();
    private final ColorDecoration overBackColor = new ColorDecoration();
    private int nb = 1;
    
    private final JStreamEditMap map = new JStreamEditMap();
    private final JContextTree tree = new JContextTree();
    private final JStreamNavigationBar guiNavBar = new JStreamNavigationBar();
    private final JStreamSelectionBar guiSelectBar = new JStreamSelectionBar();
    private final JStreamInfoBar guiInfoBar = new JStreamInfoBar();
    
    public StreamWidgetsDemo(){
        
        initComponents();        
        setLocationRelativeTo(null);

        

        final MapContext context = buildContext();
        initTree(tree, map);


        tree.addContext(context);

        guiNavBar.setMap(map);
        guiSelectBar.setMap(map);
        guiInfoBar.setMap(map);

        overBackImage.setImage(IconBundle.getResource().getIcon("about").getImage());
        overBackImage.setOpaque(true);
        overBackImage.setBackground(new Color(0.7f, 0.7f, 1f, 0.8f));
        overBackImage.setStyle(org.jdesktop.swingx.JXImagePanel.Style.CENTERED);
        map.setBackgroundDecoration(overBackColor);

        tree.addTreeContextListener(new TreeContextListener() {

            public void contextAdded(TreeContextEvent event) {
            }

            public void contextRemoved(TreeContextEvent event) {
            }

            public void contextActivated(TreeContextEvent event) {
                if (event.getContext() != null) {
                    map.getRenderingStrategy().setContext(event.getContext());
                }
            }

            public void contextMoved(TreeContextEvent event) {
            }
        });


        map.getRenderingStrategy().setContext(context);

        Thread t = new Thread() {

            @Override
            public void run() {
                map.getInformationDecoration().displayMessage("This in an information message", 25000, LEVEL.INFO);
                try {
                    sleep(5000);
                } catch (Exception e) {
                }
                map.getInformationDecoration().displayMessage("This in a warning message", 25000, LEVEL.WARNING);
                try {
                    sleep(5000);
                } catch (Exception e) {
                }
                map.getInformationDecoration().displayMessage("This in an error message", 25000, LEVEL.ERROR);
                try {
                    sleep(5000);
                } catch (Exception e) {
                }
                map.getInformationDecoration().displayMessage("This in a normal message", 25000, LEVEL.NORMAL);

            }
        };
        t.start();
        
    }
    
    
    private void initComponents(){
        setLayout(new BorderLayout());
        
        guiInfoBar.setFloatable(false);
        guiNavBar.setFloatable(false);
        guiSelectBar.setFloatable(false);
        
        JPanel panNorth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        Action newContext = new AbstractAction("Add context") {

            public void actionPerformed(ActionEvent arg0) {
                 DefaultMapContext context;
                context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
                context.setTitle("Context " + nb);
                tree.addContext(context);
                nb++;
            }
        };
        
        Action newLayer = new AbstractAction("Add layer") {

            public void actionPerformed(ActionEvent arg0) {
                 if (tree.getActiveContext() != null) {
                    List<DataPanel> lst = new ArrayList<DataPanel>();

                    lst.add(new JFileDataPanel());
                    lst.add(new JPostGISDataPanel());
                    lst.add(new JOracleDataPanel());
                    lst.add(new JWFSDataPanel());

                    JDataChooser jdc = new JDataChooser(null, lst);

                    JDataChooser.ACTION ret = jdc.showDialog();

                    if (ret == JDataChooser.ACTION.APPROVE) {
                        MapLayer[] layers = jdc.getLayers();
                        for (MapLayer layer : layers) {
                            tree.getActiveContext().addLayer(layer);
                        }
                    }

                }
            }
        };
        
        panNorth.add(new JButton(newContext));
        panNorth.add(new JButton(newLayer));
        panNorth.add(guiNavBar);
        panNorth.add(guiSelectBar);
        
        
        add(BorderLayout.NORTH,panNorth);
        add(BorderLayout.WEST,tree);
        add(BorderLayout.CENTER,map);
        add(BorderLayout.SOUTH,guiInfoBar);
        
        
    }
    
    
    
    private MapContext buildContext() {
        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            DataStore store = DataStoreFinder.getDataStore(new SingletonMap("url", StreamWidgetsDemo.class.getResource("/org/geotools/test-data/shapes/roads.shp")));
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_polygon.shp");
            context.addLayer(layer);

            context.setTitle("DemoContext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
    }

    private void initTree(JContextTree tree, StreamingMap2D map) {
        JContextTreePopup popup = tree.getPopupMenu();

        popup.addItem(new LayerVisibilityItem());           //layer         
        popup.addItem(new SeparatorItem());
        popup.addItem(new LayerZoomItem(map));              //layer
        popup.addItem(new LayerFeatureItem());              //layer
        popup.addItem(new ContextActiveItem(tree));         //context
        popup.addItem(new SeparatorItem());
        popup.addItem(new CutItem(tree));                   //all
        popup.addItem(new CopyItem(tree));                  //all
        popup.addItem(new PasteItem(tree));                 //all
        popup.addItem(new DuplicateItem(tree));             //all        
        popup.addItem(new SeparatorItem());
        popup.addItem(new DeleteItem(tree));                //all
        popup.addItem(new SeparatorItem());

        LayerPropertyItem property = new LayerPropertyItem();
        List<PropertyPane> lstproperty = new ArrayList<PropertyPane>();
        lstproperty.add(new LayerGeneralPanel());
        lstproperty.add(new LayerCRSPropertyPanel());

        LayerFilterPropertyPanel filters = new LayerFilterPropertyPanel();
        filters.addPropertyPanel(new JCQLPropertyPanel());
        lstproperty.add(filters);

        LayerStylePropertyPanel styles = new LayerStylePropertyPanel();
        styles.addPropertyPanel(new JSimpleStylePanel());
        lstproperty.add(styles);

        property.setPropertyPanels(lstproperty);
        
        popup.addItem(property);             //layer
        popup.addItem(new ContextPropertyItem());           //context

        popup.addItem(new RuleMinScaleItem());
        popup.addItem(new RuleMaxScaleItem());


        if (map instanceof SelectableMap2D) {
            colSelection.setMap((SelectableMap2D) map);
        }


        tree.addColumn(colVisible);
        tree.addColumn(colOpacity);
        tree.addColumn(colStyle);
        tree.addColumn(colSelection);

        tree.addSubNodeGroup(subsource);
        tree.addSubNodeGroup(substyle);

        tree.revalidate();
    }
    
    
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }


        final StreamWidgetsDemo demo= new StreamWidgetsDemo();
        demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        javax.swing.SwingUtilities.invokeLater(new Runnable(){

            public void run() {
                demo.pack();
                demo.setVisible(true);
            }});

    }
    
}
