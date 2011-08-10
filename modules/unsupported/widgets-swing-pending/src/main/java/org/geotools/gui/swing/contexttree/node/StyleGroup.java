/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.contexttree.node;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.LightContextTreeModel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListener;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;

/**
 * subnode showing styles
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/contexttree/node/StyleGroup.java $
 */
public class StyleGroup implements SubNodeGroup {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private static final Icon ICON_STYLE = IconBundle.getResource().getIcon("16_style");
    private static final Icon ICON_FTS = IconBundle.getResource().getIcon("16_style_fts");
    private static final Icon ICON_RULE = IconBundle.getResource().getIcon("16_style_rule");

    public boolean isValid(Object target) {
        return (target instanceof MapLayer);
    }
    
    private class PackStyleNode extends ContextTreeNode {

        private LightContextTreeModel model;
        private String name;
        private MapLayer layer;

        PackStyleNode(LightContextTreeModel model, String name, MapLayer target) {
            super(model);
            this.model = model;
            this.name = name;
            this.layer = target;
            setUserObject(target.getStyle());
            

            target.addMapLayerListener(new MapLayerListener() {

                public void layerChanged(MapLayerEvent event) {
                    updateStyleNodes();
                }

                public void layerShown(MapLayerEvent event) {
                }

                public void layerHidden(MapLayerEvent event) {
                }

                public void layerSelected(MapLayerEvent event) {
                }

                public void layerDeselected(MapLayerEvent event) {
                }

            });
        }

        private void updateStyleNodes() {
            
            while(!isLeaf()){
                model.removeNodeFromParent( (ContextTreeNode)getChildAt(0));
            }
            
            Style style = layer.getStyle();
            setUserObject(style);
            
            FeatureTypeStyle[] ftss = style.getFeatureTypeStyles();

            for (FeatureTypeStyle fts : ftss) {
                ContextTreeNode ftsnode = new FeatureTypeStyleNode(model, fts);
                model.insetNodeInto(ftsnode, this, getChildCount());
//                root.add(ftsnode);

                Rule[] rules = fts.getRules();
                for (Rule rule : rules) {
                    ContextTreeNode rulenode = new RuleNode(model, rule);
                    model.insetNodeInto(rulenode, ftsnode, ftsnode.getChildCount());
                    
                    Symbolizer[] symbs = rule.getSymbolizers();
                    for (Symbolizer symb : symbs) {
                        Icon ico = new ImageIcon(RANDOM_STYLE_FACTORY.createGlyph(symb));
                        SymbolizerNode symbnode = new SymbolizerNode(model, ico, symb);
                        model.insetNodeInto(symbnode, rulenode, symbnode.getChildCount());
//                        rulenode.add(symbnode);
                    }
//                    ftsnode.add(rulenode);
                }
            }
        }

        @Override
        public Icon getIcon() {
            return ICON_STYLE;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public Object getValue() {
            return name;
        }

        @Override
        public void setValue(Object obj) {
        }
    }

    private class FeatureTypeStyleNode extends ContextTreeNode {

        FeatureTypeStyleNode(LightContextTreeModel model, FeatureTypeStyle target) {
            super(model);
            setUserObject(target);
        }

        @Override
        public Icon getIcon() {
            return ICON_FTS;
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public Object getValue() {
            return ((FeatureTypeStyle) userObject).getTitle();
        }

        @Override
        public void setValue(Object obj) {
            ((FeatureTypeStyle) userObject).setTitle(obj.toString());
        }
    }

    private class RuleNode extends ContextTreeNode {

        RuleNode(LightContextTreeModel model, Rule target) {
            super(model);
            setUserObject(target);
        }

        @Override
        public Icon getIcon() {
            return ICON_RULE;
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public Object getValue() {
            return ((Rule) userObject).getTitle();
        }

        @Override
        public void setValue(Object obj) {
            ((Rule) userObject).setTitle(obj.toString());
        }
    }

    private class SymbolizerNode extends ContextTreeNode {

        private Icon icon;

        SymbolizerNode(LightContextTreeModel model, Icon icon, Object target) {
            super(model);
            this.icon = icon;
            setUserObject(target);
        }

        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public Object getValue() {
            return "";
        }

        @Override
        public void setValue(Object obj) {
        }
    }

    public void installInNode(LightContextTreeModel model, ContextTreeNode parentnode) {
        final MapLayer layer = (MapLayer) parentnode.getUserObject();
        Style style = layer.getStyle();

        ContextTreeNode root = new PackStyleNode(model, "Style", layer);

        FeatureTypeStyle[] ftss = style.getFeatureTypeStyles();

        for (FeatureTypeStyle fts : ftss) {
            ContextTreeNode ftsnode = new FeatureTypeStyleNode(model, fts);
            root.add(ftsnode);

            Rule[] rules = fts.getRules();
            for (Rule rule : rules) {
                ContextTreeNode rulenode = new RuleNode(model, rule);
                Symbolizer[] symbs = rule.getSymbolizers();
                for (Symbolizer symb : symbs) {
                    Icon ico = new ImageIcon(RANDOM_STYLE_FACTORY.createGlyph(symb));
                    SymbolizerNode symbnode = new SymbolizerNode(model, ico, symb);
                    rulenode.add(symbnode);
                }
                ftsnode.add(rulenode);
            }
        }

        model.insetNodeInto(root, parentnode, parentnode.getChildCount());
    }


    public void removeForNode(LightContextTreeModel model, ContextTreeNode parentnode) {
        
        for(int max=parentnode.getChildCount(), i=max-1; i>=0;i--){
            ContextTreeNode node = (ContextTreeNode) parentnode.getChildAt(i);
            if(node.getUserObject() instanceof Style){
                model.removeNodeFromParent(node);
            }
        }
        
    }
}
