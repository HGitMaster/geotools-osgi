/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.referencing;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;   // For javadoc
import javax.swing.table.DefaultTableCellRenderer;

// OpenGIS dependencies
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.TransformedDirectPosition;


/**
 * A table of {@linkplain DirectPosition direct positions}. All coordinates contained in this
 * table have the same {@linkplain CoordinateReferenceSystem coordinate reference system}, which
 * is specified at construction time.
 * <p>
 * This table model provides a way to display invalid coordinates in a different color.
 * <cite>Invalide coordinates</cite> are defined here as coordinates outside the CRS
 * {@linkplain CoordinateReferenceSystem#getValidArea valid area}. This color display
 * can be enabled by the following code:
 *
 * <blockquote><pre>
 * CoordinateTableModel model = new CoordinateTableModel(crs);
 * {@linkplain JTable}               view  = new JTable(model);
 * {@linkplain TableCellRenderer} renderer = new {@linkplain CellRenderer}();
 * view.setDefaultRenderer({@linkplain Double}.class, renderer);
 * </pre></blockquote>
 *
 * @since 2.3
 * @version $Id: CoordinateTableModel.java 30655 2008-06-12 20:24:25Z acuster $
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/referencing/CoordinateTableModel.java $
 * @author Cédric Briançon
 * @author Hoa Nguyen
 * @author Martin Desruisseaux
 */
public class CoordinateTableModel extends AbstractTableModel {
    /**
     * The CRS for all coordinates in this table. This is specified by the user
     * at construction time.
     */
    private final CoordinateReferenceSystem crs ;

    /**
     * The columns table names. They are inferred from the table CRS specified
     * at construction time.
     */
    private final String[] columnNames;

    /**
     * The direct positions to display in the table.
     */
    private final List/*<DirectPosition>*/ positions = new ArrayList/*<DirectPosition>*/();

    /**
     * An unmodifiable view of the positions list. This is the view returned by public accessors.
     * We do not allow addition or removal of positions through this list because such changes
     * would not invoke the proper {@code fire} method.
     */
    private final List/*<DirectPosition>*/ unmodifiablePositions = Collections.unmodifiableList(positions);

    /**
     * The CRS valid area.
     */
    private final GeneralEnvelope validArea;

    /**
     * For transformation frop the table CRS to WGS84.
     */
    private final TransformedDirectPosition toWGS84 = new TransformedDirectPosition();

    /**
     * Creates an initially empty table model using the specified coordinate reference system.
     */
    public CoordinateTableModel(final CoordinateReferenceSystem crs) {
        this.crs = crs;
        final CoordinateSystem cs = crs.getCoordinateSystem();
        columnNames = new String[cs.getDimension()];
        for (int i=0; i<columnNames.length; i++){
            columnNames[i] = crs.getCoordinateSystem().getAxis(i).getName().getCode();
        }
        validArea = new GeneralEnvelope(CRS.getEnvelope(crs));
    }

    /**
     * Returns the CRS for this table model
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Returns all direct positions in this table.
     *
     * @see #add(DirectPosition)
     * @see #add(Collection)
     */
    public List/*<DirectPosition>*/ getPositions() {
        return unmodifiablePositions;
    }

    /**
     * Returns the number of rows in the table.
     */
    public int getRowCount() {
        return positions.size();
    }

    /**
     * Returns the number of columns.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Returns the name for the specified column. The default implementation
     * returns the name of the corresponding axis in the table CRS.
     */
    public String getColumnName(final int columnIndex){
        if (columnIndex>=0 && columnIndex<columnNames.length) {
            return columnNames[columnIndex];
        } else {
            return super.getColumnName(columnIndex);
        }
    }

    /**
     * Returns tye type of data for the specified column. For coordinate table,
     * this is always {@code Double.class}.
     */
    public Class getColumnClass(int columnIndex) {
        return Double.class;
    }

    /**
     * Adds a direct position to this table. The position is not cloned. Any cell edited in this
     * table will write its change directly into the corresponding {@code DirectPosition} object.
     */
    public void add(final DirectPosition newPosition) {
        final int index = positions.size();
        positions.add(newPosition);
        fireTableRowsInserted(index, index);
    }

    /**
     * Adds a collection of direct positions to this table. The position is not cloned.
     * Any cell edited in this table will write its change directly into the corresponding
     * {@code DirectPosition} object.
     */
    public void add(final Collection/*<DirectPosition>*/ newPositions) {
        final int lower = positions.size();
        positions.addAll(newPositions);
        final int upper = positions.size();
        fireTableRowsInserted(lower, upper-1);
    }

    /**
     * Returns the value in the table at the specified postion.
     *
     * @param  rowIndex     Cell row number.
     * @param  columnIndex  Cell column number.
     * @return The ordinate value, or {@code null} if no value is available for the specified cell.
     */
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (rowIndex >= 0 && rowIndex < positions.size()) {
            final DirectPosition position = (DirectPosition) positions.get(rowIndex);
            if (position != null && columnIndex >= 0 && columnIndex < position.getDimension()) {
                final double ordinate = position.getOrdinate(columnIndex);
                if (!Double.isNaN(ordinate)) {
                    return new Double(ordinate);
                }
            }
        }
        return null;
    }

    /**
     * Sets the value for the specified cell.
     *
     * @param value         The new value for the cell.
     * @param rowIndex      Row number of the cell modified.
     * @param columnIndex   Column number of the cell modified.
     */
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        final double ordinate = ((Number) value).doubleValue();
        ((DirectPosition) positions.get(rowIndex)).setOrdinate(columnIndex, ordinate);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Specifies that the user can fill all rows in the table.
     */
    public boolean isCellEditable(int rowIndex, int colIndex) {
        return true;
    }

    /**
     * Returns {@code true} if the position at the specified row is inside the CRS
     * {@linkplain CoordinateReferenceSystem#getValidArea valid area}. This method
     * is invoked by {@link CellRenderer} in order to determine if this row should
     * be colorized.
     */
    public boolean isValidCoordinate(final int rowIndex) {
        final DirectPosition position = (DirectPosition) positions.get(rowIndex);
        try {
            toWGS84.transform(position);
        } catch (TransformException e) {
            /*
             * If the coordinate can't be transformed, then there is good chances
             * that the the coordinate is outside the CRS valid area.
             */
            return false;
        }
        return validArea.contains(toWGS84);
    }

    /**
     * Returns a string representation of this table. The default implementation
     * list all coordinates.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final int size = positions.size();
        for (int i=0; i<size; i++) {
            buffer.append(positions.get(i));
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }

    /**
     * A cell renderer for the {@linkplain CoordinateTableModel coordinate table model}.
     * This cell renderer can display in a different color coordinates outside the CRS
     * {@linkplain CoordinateReferenceSystem#getValidArea valid area}. Coordinate validity
     * is determined by invoking {@link CoordinateTableModel#isValidCoordinate}.
     *
     * @since 2.3
     * @version $Id: CoordinateTableModel.java 30655 2008-06-12 20:24:25Z acuster $
     * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/referencing/CoordinateTableModel.java $
     * @author Cédric Briançon
     * @author Martin Desruisseaux
     */
    public static class CellRenderer extends DefaultTableCellRenderer {
        /**
         * The default text and background color.
         */
        private Color foreground, background;

        /**
         * The text and background color for invalid coordinates.
         */
        private Color invalidForeground = Color.RED, invalidBackground;

        /**
         * Creates a default cell renderer for {@link CoordinateTableModel}.
         */
        public CellRenderer() {
            super();
            foreground = super.getForeground();
            background = super.getBackground();			
        }

        /**
         * Specifies the text color for valid coordinates.
         */
        public void setForeground(final Color foreground){
            this.foreground = foreground;
            super.setForeground(foreground);
        }

        /**
         * Specifies the background color for valid coordinates.
         */
        public void setBackground(final Color background){
            this.background = background;
            super.setBackground(background);
        }

        /**
         * Specified the text and background colors for invalid coordinates,
         * or {@code null} for the same color than valid coordinates.
         */
        public void setInvalidColor(final Color foreground, final Color background) {
            this.invalidForeground = foreground;
            this.invalidBackground = background;
        }

        /**
         * Returns the component for cell rendering.
         */
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                final boolean isSelected, final boolean hasFocus, final int row, final int column)
        {
            Color foreground = this.foreground;
            Color background = this.background;
            final TableModel candidate = table.getModel();
            if (candidate instanceof CoordinateTableModel) {
                final CoordinateTableModel model = (CoordinateTableModel) candidate;
                if (!model.isValidCoordinate(row)) {
                    if (invalidForeground != null) foreground = invalidForeground;
                    if (invalidBackground != null) background = invalidBackground;
                }
            }
            super.setBackground(background);
            super.setForeground(foreground);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
