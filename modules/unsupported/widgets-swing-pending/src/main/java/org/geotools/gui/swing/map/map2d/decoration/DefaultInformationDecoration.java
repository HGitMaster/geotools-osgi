/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.map.map2d.decoration;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import javax.swing.Timer;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.Map2D;

/**
 * Default information decoration
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/decoration/DefaultInformationDecoration.java $
 */
public class DefaultInformationDecoration extends JComponent implements InformationDecoration {

    private static final ImageIcon ICO_ERROR = IconBundle.getResource().getIcon("16_deco_error");
    private static final ImageIcon ICO_WARNING = IconBundle.getResource().getIcon("16_deco_warning");
    private static final ImageIcon ICO_INFO = IconBundle.getResource().getIcon("16_deco_info");
    private Map<String, LEVEL> messages = new LinkedHashMap<String, LEVEL>();
    private final BufferedImage buffer;
    private boolean drawing = false;
    private boolean lowlevel = true;

    public DefaultInformationDecoration() {
        ImageIcon anim = IconBundle.getResource().getIcon("JS_GT");
        String msg = BUNDLE.getString("drawing_wait");


        Font currentFont = new Font("Arial", Font.BOLD | Font.ITALIC, 13);
        FontMetrics currentMetrics = getFontMetrics(currentFont);
        int high = (currentMetrics.getHeight() > anim.getIconHeight()) ? currentMetrics.getHeight() : anim.getIconHeight();
        int width = currentMetrics.stringWidth(msg) + anim.getIconWidth() + 2;

        buffer = new BufferedImage(width + 9, high + 7, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.setColor(new Color(1f, 1f, 1f, 0.5f));
        g2d.fillRoundRect(2, 2, (width + 6), (high + 4), 9, 9);
        g2d.drawImage(anim.getImage(), 5, 4, this);
        g2d.setColor(new Color(0f, 0f, 0f, 0.5f));
        g2d.drawRoundRect(2, 2, (width + 6), (high + 4), 9, 9);
        g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 13));
        g2d.setColor(Color.BLACK);
        g2d.drawString(msg, (anim.getIconWidth() + 2 + 3 + 2), (buffer.getHeight() / 2 + currentMetrics.getHeight() / 2));

    }

    public void setPaintingIconVisible(boolean b) {
        drawing = b;
        revalidate();
        repaint();
    }

    public void refresh() {
        repaint();
    }

    public JComponent geComponent() {
        return this;
    }

    public void setMap2D(Map2D map) {

    }

    public Map2D getMap2D() {
        return null;
    }

    public boolean isPaintingIconVisible() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void displayMessage(final String text, int time, LEVEL level) {

        messages.put(text, level);

        repaint();

        Timer tim = new Timer(time, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                messages.remove(text);
                repaint();
            }
        });
        tim.setRepeats(false);
        tim.start();

    }

    public String getLastDisplayedMessage() {
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (drawing) {
            g2.drawImage(buffer, 0, 0, this);
        }

        Set<String> keys = messages.keySet();
        Object[] ite = keys.toArray();
        List<String> msgs = new ArrayList<String>();

        int height = 0;
        if (!lowlevel) {
            for (int n = 0; n < ite.length; n++) {                
                LEVEL lvl = messages.get(ite[n]);
                if (lvl == LEVEL.ERROR || lvl == LEVEL.WARNING) {
                    msgs.add((String) ite[n]);
                    height++;
                }
            }
        } else {
            for (int n = 0; n < ite.length; n++) {
                msgs.add((String) ite[n]);
                height++;
            }
        }

        height = (height > 0) ? (height) * 20 + 5 : 0;
        Paint gp = new Color(0, 0, 0, 0.5f);
        g2.setPaint(gp);
        g2.fillRect(0, getHeight() - height, getWidth(), height);

        g2.setPaint(Color.WHITE);

        int i = getHeight() - 22;

        for (int n = 0; n < msgs.size(); n++) {
            String text = msgs.get(n);
            LEVEL lvl = messages.get(text);
            switch (lvl) {
                case ERROR:
                    g2.drawImage(ICO_ERROR.getImage(), 3, i + 2, this);
                    g2.drawString((String) text, 20, i + 15);
                    break;
                case WARNING:
                    g2.drawImage(ICO_WARNING.getImage(), 3, i + 2, this);
                    g2.drawString((String) text, 20, i + 15);
                    break;
                case INFO:
                    g2.drawImage(ICO_INFO.getImage(), 3, i + 2, this);
                    g2.drawString((String) text, 20, i + 15);
                    break;
                case NORMAL:
                    g2.drawString((String) text, 3, i + 15);
                    break;
            }


            i -= 20;
        }


    }

    public void displayLowLevelMessages(boolean display) {
        lowlevel = display;
        repaint();
    }

    public boolean isDisplayingLowLevelMessages() {
        return lowlevel;
    }

    public void dispose() {
    }
}
