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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.commons.collections.map.SingletonMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.JContextTreePopup;
import org.geotools.gui.swing.contexttree.LightContextTreeModel;
import org.geotools.gui.swing.contexttree.TreeContextEvent;
import org.geotools.gui.swing.contexttree.TreeContextListener;
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.node.SubNodeGroup;
import org.geotools.gui.swing.contexttree.popup.TreePopupItem;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellEditor;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellRenderer;
import org.geotools.gui.swing.contexttree.renderer.RenderAndEditComponent;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
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
 * @author johann sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/demo/widgets/Demo_ContextTree.java $
 */
public class Demo_ContextTree extends JPanel {

    public Demo_ContextTree() {

        JContextTree tree = new JContextTree();
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, tree);

        //manipulating contexts-------------------------------------------------
        MapContext context = buildContext();

        tree.addContext(context);
//        tree.removeContext(context);
//        MapContext aContext = tree.getContext(0);
//        int nbContexts = tree.getContextCount();
//        int aContextIndex = tree.getContextIndex(context);
//        MapContext[] contexts = tree.getContexts();
//        MapContext activeContext = tree.getActiveContext();
//        tree.setActiveContext(context);

        TreeContextListener treeContextListener = new TreeContextListener() {

            public void contextAdded(TreeContextEvent event) {
                System.out.println("Context added :" + event.getContext().getTitle() + " at position :" + event.getToIndex());
            }

            public void contextRemoved(TreeContextEvent event) {
                System.out.println("Context removed :" + event.getContext().getTitle() + " at position :" + event.getFromIndex());
            }

            public void contextActivated(TreeContextEvent event) {
                System.out.println("Context activated :" + event.getContext().getTitle() + " at position :" + event.getFromIndex());
            }

            public void contextMoved(TreeContextEvent event) {
                System.out.println("Context moved :" + event.getContext().getTitle() + " from : " + event.getFromIndex() + " to : " + event.getToIndex());
            }
        };

        tree.addTreeContextListener(treeContextListener);
//        tree.removeTreeContextListener(treeContextListener);
//        TreeContextListener[] treeContextListeners = tree.getTreeContextListeners();


        //manipulating selections listener--------------------------------------

        TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                System.out.println("Selection Paths : " + e.getPaths());

                if (e.getPath() != null) {
                    ContextTreeNode node = (ContextTreeNode) e.getPath().getLastPathComponent();
                    Object obj = node.getUserObject();
                    if (obj instanceof MapLayer) {
                        System.out.println("It's a layer node");
                    } else if (obj instanceof MapContext) {
                        System.out.println("It's a context node");
                    } else {
                        System.out.println("It's something else node");
                    }
                }
            }
        };

        tree.getTreeSelectionModel().addTreeSelectionListener(treeSelectionListener);
        //tree.getTreeSelectionModel().removeTreeSelectionListener(treeSelectionListener);


        //Copy/Cut/Paste/Delete/Duplicate functions-----------------------------
        String prefix = tree.getPrefixString();
        tree.setPrefixString("It is a copy element - ");

        //see if he can do the action
        boolean canCopy = tree.canCopySelection();
        boolean canCut = tree.canCutSelection();
        boolean canDelete = tree.canDeleteSelection();
        boolean canDuplicate = tree.canDuplicateSelection();
        boolean canPaste = tree.canPasteBuffer();

        //do the action, return true if succeed
        boolean copySucceed = tree.copySelectionInBuffer();
        boolean cutSucceed = tree.cutSelectionInBuffer();
        boolean deleteSucceed = tree.deleteSelection();
        boolean duplicateSucceed = tree.duplicateSelection();
        boolean pasteSucceed = tree.pasteBuffer();

        //and other stuff
        boolean selection = tree.hasSelection();
        boolean empty = tree.isBufferEmpty();
        Object[] datas = tree.getBuffer();
        tree.clearBuffer();



        //Manipulating columns--------------------------------------------------
        TreeTableColumn mycolumn = buildColumn();

        tree.addColumn(mycolumn);
//        tree.removeColumn(mycolumn);
//        tree.removeColumn(0);
//        int nbcolumns = tree.getColumnCount()
//        int index = tree.getColumnIndex(mycolumn);
//        TreeTableColumn[] cols = tree.getColumns();


        //Manipulating popup menu-----------------------------------------------
        JContextTreePopup popup = tree.getPopupMenu();

        TreePopupItem myTreePopupItem = buildPopupItem();
        
        popup.addItem(myTreePopupItem);
//      popup.addItem( index, myTreePopupItem);
//      popup.addAllItem( index, Collection <? extends TreePopupItem>);
//      popup.addAllItem(Collection <? extends TreePopupItem>);
//      popup.removeItem( myTreePopupItem );
//      popup.removeItem( index );
//      TreePopupItem[]  tpi = popup.getControls();
        
        //Manipulating sub nodes------------------------------------------------
        SubNodeGroup myGroup = buildSubNodeGroup();
        
        tree.addSubNodeGroup(myGroup);
//        tree.removeSubNodeGroup(myGroup);
//        tree.removeSubNodeGroup(0);
//        int nbgroups = tree.getSubNodeGroupCount();
//        int index = tree.getSubNodeGroupIndex(myGroup);
//        SubNodeGroup[] groups = tree.getSubNodeGroups();
        
        
        


    }

    public MapContext buildContext() {
        RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();

        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            DataStore store = DataStoreFinder.getDataStore(new SingletonMap("url", Demo_ContextTree.class.getResource("/org/geotools/gui/swing/demo/shape/test_polygon.shp")));
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_polygon.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url", Demo_ContextTree.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url", Demo_ContextTree.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_point.shp");
            context.addLayer(layer);
            context.setTitle("DemoContext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
    }

    public TreeTableColumn buildColumn() {

        class MyCellComponent extends RenderAndEditComponent {

            private MapLayer layer = null;
            private JButton button = new JButton("map");
            private boolean edited = false;

            MyCellComponent() {
                setLayout(new GridLayout(1, 1));
                add(button);

                button.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (layer != null) {
                            edited = true;
                            JOptionPane.showMessageDialog(null, "hello layer : " + layer.getTitle());
                        }
                    }
                });

            }

            // we edit our component depending on what is been passed
            // This component is used for all cell so be sure to
            // put back value to default if needed
            @Override
            public void parse(Object obj) {
                removeAll();
                edited = false;

                if (obj instanceof MapLayer) {
                    layer = (MapLayer) obj;
                    add(button);
                } else {
                    layer = null;
                }
            }

            //   the returned value when edition stop     

            @Override
            public Object getValue() {
                return edited;
            }
        }

        class FlyingColumn extends TreeTableColumn {

            public FlyingColumn() {

                //properties of our column
                setWidth(70);
                setPreferredWidth(70);
                setMaxWidth(70);
                setResizable(false);
                // This is interesting, if you set it to true the cell will switch
                // to edit mode on mouseOver. It makes the column much more dynamic
                // but also need a bit more performance 
                setEditableOnMouseOver(true);

                //... and many others available...


                //THE RENDERER AND EDITOR COMPONANTS
                //
                // If you are used to CellRenderer and CellEditor you can use :
                // - setCellEditor( myCellEditor );
                // - setCellRenderer( myCellRenderer );
                // 
                // If you are not used to, I prepare a simplified solution used below
                //
                setCellEditor(new DefaultCellEditor(new MyCellComponent()));
                setCellRenderer(new DefaultCellRenderer(new MyCellComponent()));
                //
                // The same component type is used for rendering and editing 
                //

                //THE HEADER
                setHeaderValue("FreeMap");
            // If you want an icon in the header use :
            // setHeaderValue( new HeaderInfo( "FreeMap_id", "FreeMap", myIcon ) ); 
            // - "FreeMap_id" will be visible in the topleft control menu of the JContextTree
            // - "FreeMap" is the title seen in the header, you can use null
            // - myIcon is your Icon, you can use null 
            // 
            // If you're not satisfied yet you can rewrite the header renderer
            // setHeaderRenderer( myRenderer );

            }

            @Override
            public void setValue(Object target, Object value) {
                // here the Editor component returns a Value
                // We made it return a Boolean.
                // We'll just set visible or unvisible the 

                boolean edited = (Boolean) value;

                if (edited) {
                //do some stuff
                }

            }

            @Override
            public Object getValue(Object target) {
                // we return the MapLayer, but we can return anything
                // just be sure the Renderer and Editor Component will
                // handle it the same way
                if (target instanceof MapLayer) {
                    return target;
                } else {
                    return null;
                }
            }

            @Override
            public Class getColumnClass() {
                // the class of the object returned
                return MapLayer.class;
            }

            @Override
            public boolean isCellEditable(Object target) {
                // We choose on what kind of Object (MapContext or MapLayer)
                // the component will be visible.
                // Here we choose only MapLayers
                if (target instanceof MapLayer) {
                    return isEditable();
                } else {
                    return false;
                }
            }
        }

        return new FlyingColumn();
    }

    public TreePopupItem buildPopupItem() {

        class ActionItem implements TreePopupItem {

            private JMenu menu = new JMenu("Grid Menu");

            public ActionItem() {

                menu.add(new JMenuItem("Action1"));
                menu.add(new JMenuItem("Action2"));
                menu.add(new JCheckBoxMenuItem("check this"));

            }

            public boolean isValid(TreePath[] selection) {
                //We said it could be visible only with rasters
                // and only if there is one selected 
                if (selection.length == 1) {
                    ContextTreeNode node = (ContextTreeNode) selection[0].getLastPathComponent();
                    
                    if (node.getUserObject() instanceof MapLayer) {
                            return true;
                        
                    }
                }
                return false;
            }

            public Component getComponent(TreePath[] selection) {
                // We can return whatever we want, a classic JMenuItem
                // or much more complicate components. we could imagine
                // a JTable or an animated component. there's no limit.
                return menu;
            }
        }
        
        return new ActionItem();

    }

    public SubNodeGroup buildSubNodeGroup() {
        
        
        class TitleNode extends ContextTreeNode{

            String title = "";
            
            TitleNode(String title,LightContextTreeModel model){
                super(model);
                this.title = title;
            }
            
            @Override
            public Object getValue() {
                return title;
            }

            @Override
            public void setValue(Object obj) {                
            }

            @Override
            public Icon getIcon() {
                return IconBundle.EMPTY_ICON;
            }

            @Override
            public boolean isEditable() {
                return false;
            }
            
        }
        
        class TitleNodeGroup implements SubNodeGroup{

            public boolean isValid(Object target) {
                return (target instanceof MapLayer);
            }

            public void installInNode(LightContextTreeModel model, ContextTreeNode node) {
                MapLayer layer = (MapLayer) node.getUserObject();
                TitleNode mynode = new TitleNode(layer.getTitle(),model);
                
                model.insetNodeInto(mynode, node, 0);
            }

            public void removeForNode(LightContextTreeModel model, ContextTreeNode node) {
                int max = node.getChildCount();
                
                for(int i=0; i<max; i++){
                    ContextTreeNode aNode = (ContextTreeNode) node.getChildAt(i);
                    if( aNode instanceof TitleNode){
                        model.removeNodeFromParent(aNode);
                    }
                }
                
            }
            
        };
        
        
        
        return new TitleNodeGroup();
        
    }
    
    
    public static void main(String[] args) {

        JFrame frm = new JFrame("Demo : JContextTree");
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frm.setContentPane(new Demo_ContextTree());

        frm.pack();
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);
    }

    
}
