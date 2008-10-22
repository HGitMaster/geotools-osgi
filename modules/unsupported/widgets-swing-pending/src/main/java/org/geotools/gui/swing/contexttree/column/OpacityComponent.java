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
package org.geotools.gui.swing.contexttree.column;

import java.awt.GridLayout;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.filter.Filters;
import org.geotools.gui.swing.contexttree.renderer.RenderAndEditComponent;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.expression.Expression;

/**
 * Component used to present opacity
 * 
 * @author Johann Sorel
 */
public final class OpacityComponent extends RenderAndEditComponent {

    private MapLayer layer = null;
    private Symbolizer symb = null;
    private JSlider slide = new JSlider(0, 100);

    /**
     * opacity component, slider
     */
    public OpacityComponent() {
        super();
        setLayout(new GridLayout(1, 1));
        slide.setOpaque(false);
        slide.setPaintTicks(true);

        slide.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                applyOpacity(slide.getValue() / 100d);
            }
        });
    }

    public void parse(Object value) {
        double valeur = 1;
        boolean correct = false;

        layer = null;
        symb = null;
        if (value instanceof MapLayer) {

            valeur = format((MapLayer) value);

            if (valeur > 1) {
                valeur = 1d;
            } else if (valeur < 0) {
                valeur = 0d;
            }

            correct = true;
            valeur *= 100;
            slide.setValue(Double.valueOf(valeur).intValue());

            this.layer = (MapLayer) value;
        } else if (value instanceof Symbolizer) {
            
            if (value instanceof PolygonSymbolizer) {
                PolygonSymbolizer sym = (PolygonSymbolizer) value;
                valeur = Filters.asDouble(sym.getFill().getOpacity());
            } else if (value instanceof PointSymbolizer) {
                PointSymbolizer sym = (PointSymbolizer) value;
                valeur = Filters.asDouble(sym.getGraphic().getOpacity());
            } else if (value instanceof LineSymbolizer) {
                LineSymbolizer sym = (LineSymbolizer) value;
                valeur = Filters.asDouble(sym.getStroke().getOpacity());
            } else if (value instanceof RasterSymbolizer) {
                RasterSymbolizer sym = (RasterSymbolizer) value;
                valeur = Filters.asDouble(sym.getOpacity());
            }
            
            correct = true;
            valeur *= 100;
            slide.setValue(Double.valueOf(valeur).intValue());
            
            this.symb = (Symbolizer) value;
        }



        removeAll();
        if (correct) {
            add(slide);
        }

    }

    public Object getValue() {
        return slide.getValue() / 100d;
    }

    private Double format(MapLayer layer) {

        FeatureTypeStyle[] sty = layer.getStyle().getFeatureTypeStyles();
        double valeur = 1d;

        Rule[] rules = sty[0].getRules();
        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];

            //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
            if (r.getFilter() == null) {
                Symbolizer[] symbolizers = r.getSymbolizers();
                for (int j = 0; j < symbolizers.length; j++) {

                    if (symbolizers[j] instanceof PolygonSymbolizer) {
                        PolygonSymbolizer sym = (PolygonSymbolizer) symbolizers[j];
                        valeur = Filters.asDouble(sym.getFill().getOpacity());
                    } else if (symbolizers[j] instanceof PointSymbolizer) {
                        PointSymbolizer sym = (PointSymbolizer) symbolizers[j];
                        //valeur = SLD.pointOpacity(sym);
                        valeur = Filters.asDouble(sym.getGraphic().getOpacity());
                    } else if (symbolizers[j] instanceof LineSymbolizer) {
                        LineSymbolizer sym = (LineSymbolizer) symbolizers[j];
                        valeur = Filters.asDouble(sym.getStroke().getOpacity());
                    } else if (symbolizers[j] instanceof RasterSymbolizer) {
                        RasterSymbolizer sym = (RasterSymbolizer) symbolizers[j];
                        valeur = Filters.asDouble(sym.getOpacity());
                    }
                }
            }

        }

        return valeur;

    }

    private void applyOpacity(Double d) {
        StyleBuilder sb = new StyleBuilder();
        Expression opa = sb.literalExpression(d);

        if (layer != null) {

            FeatureTypeStyle[] sty = layer.getStyle().getFeatureTypeStyles();

            Rule[] rules = sty[0].getRules();
            for (int i = 0; i < rules.length; i++) {
                Rule r = rules[i];

                //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
                if (r.getFilter() == null) {
                    Symbolizer[] symbolizers = r.getSymbolizers();
                    for (int j = 0; j < symbolizers.length; j++) {
                        applyOpacity(symbolizers[j], opa);
                    }
                }
            }

            layer.setStyle(layer.getStyle());
        } else if (symb != null) {
            applyOpacity(symb, opa);
        }

    }

    private void applyOpacity(Symbolizer symbol, Expression opa) {
        if (symbol instanceof PolygonSymbolizer) {
            PolygonSymbolizer sym = (PolygonSymbolizer) symbol;
            sym.getFill().setOpacity(opa);
            sym.getStroke().setOpacity(opa);
        } else if (symbol instanceof PointSymbolizer) {
            PointSymbolizer sym = (PointSymbolizer) symbol;
            sym.getGraphic().setOpacity(opa);

            Mark[] marks = sym.getGraphic().getMarks();

            for (int k = 0; k < marks.length; k++) {
                marks[k].getFill().setOpacity(opa);
                marks[k].getStroke().setOpacity(opa);
            }

        } else if (symbol instanceof LineSymbolizer) {
            LineSymbolizer sym = (LineSymbolizer) symbol;
            sym.getStroke().setOpacity(opa);
        } else if (symbol instanceof RasterSymbolizer) {
            RasterSymbolizer sym = (RasterSymbolizer) symbol;
            sym.setOpacity(opa);
        }
    }
}
