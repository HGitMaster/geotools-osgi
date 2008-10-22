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
package org.geotools.gui.swing.style.sld;

import java.awt.Color;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.SpinnerNumberModel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.geotools.styling.SLD;
import org.geotools.styling.StyleBuilder;
import org.opengis.filter.expression.Expression;

/**
 * Expression panel
 *
 * @author  Johann Sorel
 */
public class JExpressionPane extends javax.swing.JPanel{

    private static final Icon ICON_CAP_ROUND = IconBundle.getResource().getIcon("16_linecap_round");
    private static final Icon ICON_CAP_SQUARE = IconBundle.getResource().getIcon("16_linecap_square");
    private static final Icon ICON_BUTT = IconBundle.getResource().getIcon("16_linecap_butt");
    private static final Icon ICON_JOIN_ROUND = IconBundle.getResource().getIcon("16_linejoin_round");
    private static final Icon ICON_JOIN_MITRE = IconBundle.getResource().getIcon("16_linejoin_mitre");
    private static final Icon ICON_JOIN_BEVEL = IconBundle.getResource().getIcon("16_linejoin_bevel");
    
    private static final Icon ICON_COLOR = IconBundle.getResource().getIcon("JS16_color");
    private static final Icon ICON_EXP = IconBundle.getResource().getIcon("16_expression");

    public static enum EXP_TYPE {
        COLOR,
        NUMBER,
        OPACITY,
        WELL_KNOWN_NAME,
        LINE_CAP,
        LINE_JOIN,
        OTHER
    }
    private EXP_TYPE type = EXP_TYPE.OTHER;
    private MapLayer layer = null;
    private JExpressionDialog dialog = new JExpressionDialog();
    private Expression exp = null;

    /** 
     * Creates new form JExpressionPanel 
     */
    public JExpressionPane() {
        initComponents();
        init();
    }

    private void init() {
        jcb_exp.addItem("square");
        jcb_exp.addItem("circle");
        jcb_exp.addItem("triangle");
        jcb_exp.addItem("star");
        jcb_exp.addItem("cross");
        jcb_exp.addItem("x");
        jcb_exp.setSelectedItem("cross");
        parse();
    }

    public void setType(EXP_TYPE type) {
        this.type = type;
        parse();
    }

    public EXP_TYPE getType() {
        return type;
    }

    private void parse() {

        switch (type) {
            case COLOR:
                pan_exp.removeAll();
                pan_exp.add(jtf_exp);
                jtf_exp.setEditable(false);
                but_color.setVisible(true);
                but_color.setEnabled(true);
                but_color.setIcon(ICON_COLOR);
                break;
            case NUMBER:
                pan_exp.removeAll();
                pan_exp.add(jsp_exp);
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case OPACITY:
                pan_exp.removeAll();
                pan_exp.add(jsp_exp);
                jsp_exp.setModel(new SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.0d), Double.valueOf(1.0d), Double.valueOf(0.1d)));
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case WELL_KNOWN_NAME:
                pan_exp.removeAll();
                pan_exp.add(jcb_exp);
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case LINE_CAP:
                pan_exp.removeAll();
                pan_exp.add(pan_linecap);
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case LINE_JOIN:
                pan_exp.removeAll();
                pan_exp.add(pan_linejoin);
                jtf_exp.setEditable(false);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
            case OTHER:
                pan_exp.removeAll();
                pan_exp.add(jtf_exp);
                jtf_exp.setEditable(true);
                but_color.setVisible(false);
                but_color.setEnabled(false);
                but_color.setIcon(null);
                break;
        }
    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setExpression(Expression exp) {

        this.exp = exp;

        if (exp != null) {
            if (exp != Expression.NIL) {
                jtf_exp.setText(exp.toString());

                try {
                    jcb_exp.setSelectedItem(exp.toString());
                } catch (Exception e) {
                }
            }
        }

        if (exp != null) {

            if (exp.toString().startsWith("#")) {
                try {
                    Color col = SLD.color(exp);
                    if (col != null) {
                        jtf_exp.setBackground(col);
                    } else {
                        jtf_exp.setBackground(Color.WHITE);
                    }
                } catch (Exception e) {
                    jtf_exp.setBackground(Color.WHITE);
                }
            } else {
                try {
                    jsp_exp.setValue(Double.valueOf(exp.toString()));
                } catch (Exception e) {
                }
            }
        } else {
            jtf_exp.setBackground(Color.WHITE);
            jsp_exp.setValue(1);
        }
    }

    public Expression getExpression() {

        if (exp == null) {
            exp = new StyleBuilder().literalExpression(jtf_exp.getText());
        }
        
        return exp;
    }
        
    private void setLineJoin(Expression exp) {

        this.exp = exp;
        if (exp != null) {
            if (exp.toString().toLowerCase().equals("bevel")) {
                but_bevel.setSelected(true);
                but_bevel.setContentAreaFilled(true);
                but_mitre.setContentAreaFilled(false);
                but_round.setContentAreaFilled(false);
            } else if (exp.toString().toLowerCase().equals("mitre")) {
                but_mitre.setSelected(true);
                but_bevel.setContentAreaFilled(false);
                but_mitre.setContentAreaFilled(true);
                but_round.setContentAreaFilled(false);
            } else if (exp.toString().toLowerCase().equals("round")) {
                but_round.setSelected(true);
                but_bevel.setContentAreaFilled(false);
                but_mitre.setContentAreaFilled(false);
                but_round.setContentAreaFilled(true);
            } else {
                but_bevel.setContentAreaFilled(false);
                but_mitre.setContentAreaFilled(false);
                but_round.setContentAreaFilled(false);
            }
        } else {
            but_bevel.setContentAreaFilled(false);
            but_mitre.setContentAreaFilled(false);
            but_round.setContentAreaFilled(false);
        }

    }
    
    private void setLineCap(Expression exp) {
        this.exp = exp;
        if (exp != null) {
            if (exp.toString().toLowerCase().equals("butt")) {
                but_butt.setContentAreaFilled(true);
                but_round.setContentAreaFilled(false);
                but_square.setContentAreaFilled(false);
                but_butt.setSelected(true);
            } else if (exp.toString().toLowerCase().equals("square")) {
                but_butt.setContentAreaFilled(false);
                but_round.setContentAreaFilled(false);
                but_square.setContentAreaFilled(true);
                but_square.setSelected(true);
            } else if (exp.toString().toLowerCase().equals("round")) {
                but_butt.setContentAreaFilled(false);
                but_round.setContentAreaFilled(true);
                but_square.setContentAreaFilled(false);
                but_round.setSelected(true);
            }
        } else {
            but_butt.setContentAreaFilled(false);
            but_round.setContentAreaFilled(false);
            but_square.setContentAreaFilled(false);
        }
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jtf_exp = new javax.swing.JTextField();
        jsp_exp = new javax.swing.JSpinner();
        jcb_exp = new javax.swing.JComboBox();
        pan_linecap = new javax.swing.JPanel();
        but_round = new javax.swing.JToggleButton();
        but_butt = new javax.swing.JToggleButton();
        but_square = new javax.swing.JToggleButton();
        pan_linejoin = new javax.swing.JPanel();
        but_round1 = new javax.swing.JToggleButton();
        but_bevel = new javax.swing.JToggleButton();
        but_mitre = new javax.swing.JToggleButton();
        but_exp = new javax.swing.JButton();
        but_color = new javax.swing.JButton();
        pan_exp = new javax.swing.JPanel();

        jtf_exp.setOpaque(false);
        jtf_exp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtf_expActionPerformed(evt);
            }
        });
        jtf_exp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtf_expFocusLost(evt);
            }
        });

        jsp_exp.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));
        jsp_exp.setOpaque(false);
        jsp_exp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsp_expStateChanged(evt);
            }
        });

        jcb_exp.setOpaque(false);
        jcb_exp.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jcb_expItemStateChanged(evt);
            }
        });

        pan_linecap.setOpaque(false);

        but_round.setIcon(ICON_CAP_ROUND);
        but_round.setBorderPainted(false);
        but_round.setContentAreaFilled(false);
        but_round.setIconTextGap(0);
        but_round.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_round.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_roundActionPerformed(evt);
            }
        });

        but_butt.setIcon(ICON_BUTT);
        but_butt.setBorderPainted(false);
        but_butt.setContentAreaFilled(false);
        but_butt.setIconTextGap(0);
        but_butt.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_butt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_buttActionPerformed(evt);
            }
        });

        but_square.setIcon(ICON_CAP_SQUARE);
        but_square.setBorderPainted(false);
        but_square.setContentAreaFilled(false);
        but_square.setIconTextGap(0);
        but_square.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_square.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_squareActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pan_linecapLayout = new org.jdesktop.layout.GroupLayout(pan_linecap);
        pan_linecap.setLayout(pan_linecapLayout);
        pan_linecapLayout.setHorizontalGroup(
            pan_linecapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_linecapLayout.createSequentialGroup()
                .add(but_round)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_butt)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_square))
        );
        pan_linecapLayout.setVerticalGroup(
            pan_linecapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_linecapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(but_round, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .add(but_butt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .add(but_square, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );

        pan_linejoin.setOpaque(false);

        but_round1.setIcon(ICON_JOIN_ROUND);
        but_round1.setBorderPainted(false);
        but_round1.setContentAreaFilled(false);
        but_round1.setIconTextGap(0);
        but_round1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_round1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_round1ActionPerformed(evt);
            }
        });

        but_bevel.setIcon(ICON_JOIN_BEVEL);
        but_bevel.setBorderPainted(false);
        but_bevel.setContentAreaFilled(false);
        but_bevel.setIconTextGap(0);
        but_bevel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_bevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_bevelActionPerformed(evt);
            }
        });

        but_mitre.setIcon(ICON_JOIN_MITRE);
        but_mitre.setBorderPainted(false);
        but_mitre.setContentAreaFilled(false);
        but_mitre.setIconTextGap(0);
        but_mitre.setMargin(new java.awt.Insets(2, 2, 2, 2));
        but_mitre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_mitreActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pan_linejoinLayout = new org.jdesktop.layout.GroupLayout(pan_linejoin);
        pan_linejoin.setLayout(pan_linejoinLayout);
        pan_linejoinLayout.setHorizontalGroup(
            pan_linejoinLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_linejoinLayout.createSequentialGroup()
                .add(but_round1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_bevel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_mitre))
        );
        pan_linejoinLayout.setVerticalGroup(
            pan_linejoinLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(but_round1)
            .add(but_bevel)
            .add(but_mitre)
        );

        setOpaque(false);

        but_exp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/irc_channel.png"))); // NOI18N
        but_exp.setBorderPainted(false);
        but_exp.setContentAreaFilled(false);
        but_exp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDialog(evt);
            }
        });

        but_color.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/jsorel/16x16/color.png"))); // NOI18N
        but_color.setBorderPainted(false);
        but_color.setContentAreaFilled(false);
        but_color.setMaximumSize(new java.awt.Dimension(22, 22));
        but_color.setMinimumSize(new java.awt.Dimension(22, 22));
        but_color.setPreferredSize(new java.awt.Dimension(22, 22));
        but_color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_colorActionPerformed(evt);
            }
        });

        pan_exp.setOpaque(false);
        pan_exp.setPreferredSize(new java.awt.Dimension(100, 22));
        pan_exp.setLayout(new java.awt.GridLayout(1, 1));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(pan_exp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(but_exp))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(but_color, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(but_exp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(pan_exp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void actionDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionDialog
        dialog.setModal(true);
        dialog.setLocationRelativeTo(but_exp);
        dialog.setLayer(layer);
        dialog.setExpression(getExpression());
        dialog.setVisible(true);

        setExpression(dialog.getExpression());
    }//GEN-LAST:event_actionDialog

    private void jtf_expActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtf_expActionPerformed
        StyleBuilder sb = new StyleBuilder();
        setExpression(sb.literalExpression(jtf_exp.getText()));
}//GEN-LAST:event_jtf_expActionPerformed

    private void jtf_expFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtf_expFocusLost
        StyleBuilder sb = new StyleBuilder();
        setExpression(sb.literalExpression(jtf_exp.getText()));
}//GEN-LAST:event_jtf_expFocusLost

    private void but_colorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_colorActionPerformed
        StyleBuilder sb = new StyleBuilder();

        Color col = Color.WHITE;
        if (exp != null) {
            try {
                Color origin = SLD.color(exp);
                col = JColorChooser.showDialog(null, "", (origin != null) ? origin : Color.WHITE);
            } catch (Exception e) {
                col = JColorChooser.showDialog(null, "", Color.WHITE);
            }
        } else {
            col = JColorChooser.showDialog(null, "", Color.WHITE);
        }

        setExpression(sb.colorExpression(col));
    }//GEN-LAST:event_but_colorActionPerformed

    private void jsp_expStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsp_expStateChanged
        StyleBuilder sb = new StyleBuilder();
        setExpression(sb.literalExpression(jsp_exp.getValue()));
    }//GEN-LAST:event_jsp_expStateChanged

    private void jcb_expItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jcb_expItemStateChanged
        if (exp != null) {
            StyleBuilder sb = new StyleBuilder();

            Object obj = jcb_exp.getSelectedItem();
            if (obj != null) {
                exp = sb.literalExpression((String) obj);
            }
        }
    }//GEN-LAST:event_jcb_expItemStateChanged

private void but_roundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_roundActionPerformed
StyleBuilder sb = new StyleBuilder();
        setLineCap(sb.literalExpression("round"));
}//GEN-LAST:event_but_roundActionPerformed

private void but_buttActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_buttActionPerformed
StyleBuilder sb = new StyleBuilder();
        setLineCap(sb.literalExpression("butt"));
}//GEN-LAST:event_but_buttActionPerformed

private void but_squareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_squareActionPerformed
StyleBuilder sb = new StyleBuilder();
        setLineCap( sb.literalExpression("square"));
}//GEN-LAST:event_but_squareActionPerformed

private void but_round1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_round1ActionPerformed
StyleBuilder sb = new StyleBuilder();
        setLineJoin( sb.literalExpression("round"));
}//GEN-LAST:event_but_round1ActionPerformed

private void but_bevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_bevelActionPerformed
StyleBuilder sb = new StyleBuilder();
        setLineJoin( sb.literalExpression("bevel"));
}//GEN-LAST:event_but_bevelActionPerformed

private void but_mitreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_mitreActionPerformed
StyleBuilder sb = new StyleBuilder();
        setLineJoin( sb.literalExpression("mitre"));
}//GEN-LAST:event_but_mitreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton but_bevel;
    private javax.swing.JToggleButton but_butt;
    private javax.swing.JButton but_color;
    private javax.swing.JButton but_exp;
    private javax.swing.JToggleButton but_mitre;
    private javax.swing.JToggleButton but_round;
    private javax.swing.JToggleButton but_round1;
    private javax.swing.JToggleButton but_square;
    private javax.swing.JComboBox jcb_exp;
    private javax.swing.JSpinner jsp_exp;
    private javax.swing.JTextField jtf_exp;
    private javax.swing.JPanel pan_exp;
    private javax.swing.JPanel pan_linecap;
    private javax.swing.JPanel pan_linejoin;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
//        g2.setComposite();
        super.paintComponent(g);
    }

    
    
    
    
}
