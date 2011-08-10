/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing;

// J2SE dependencies
import java.io.Serializable;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel; // For JavaDoc only


/**
 * Scroll down a panel when new lines are added. This helper class require only the reference
 * to the underlying {@link BoundedRangeModel}. If the model's value is equals to its maximal
 * value and this maximal value increase, then this class increase the model's value as well.
 * Example of use:
 *
 * <blockquote><pre>
 * {@link DefaultTableModel} table = new DefaultTableModel();
 * {@link JScrollPane} pane = new JScrollPane(new JTable(table));
 * {@link AutoScroll} autos = new AutoScrool(pane.getVerticalScrollBar().getModel());
 * // etc...
 *
 * // Now, add the new item to the table. The table
 * // will be scrolled down automatically if needed.
 * table.addRow(...);
 * </pre></blockquote>
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/AutoScroll.java $
 * @version $Id: AutoScroll.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
final class AutoScroll implements ChangeListener, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8932928616386789102L;

    /**
      * The model for the vertical scrollbar.
      */
    private final BoundedRangeModel model;

    /**
     * Properties of the {@link BoundedRangeModel} the last time {@link #sync} has been invoked.
     */
    private int value, extent, maximum;

    /**
     * Constructs a new {@code AutoScroll} for the specified model.
     */
    public AutoScroll(final BoundedRangeModel model) {
        this.model = model;
        model.addChangeListener(this);
        sync();
    }

    /**
     * Disposes any resources hold by this object.
     * This method deregisters any listeners.
     */
    public void dispose() {
        model.removeChangeListener(this);
    }

    /**
     * Copies current model's state into {@link #value},
     * {@link #extent} and {@link #maximum} fields.
     */
    private void sync() {
        value   = model.getValue();
        extent  = model.getExtent();
        maximum = model.getMaximum();
    }

    /**
     * Invoked automatically when the upper limit of {@link BoundedRangeModel} has increased.
     * If the last row was visible prior the addition of new rows, then this method scrolls
     * down the model in order to show the new rows.
     */
    public void stateChanged(final ChangeEvent event) {
        final int oldValue   = value;
        final int oldExtent  = extent;
        final int oldMaximum = maximum;
        sync();
        if (oldValue+oldExtent >= oldMaximum) {
            if (value==oldValue && extent>=oldExtent && maximum>oldMaximum) {
                model.setValue(oldValue + (maximum-oldMaximum));
            }
        }
    }
}
