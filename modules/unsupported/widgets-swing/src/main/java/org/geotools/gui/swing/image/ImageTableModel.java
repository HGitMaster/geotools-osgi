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
package org.geotools.gui.swing.image;

// J2SE dependencies
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;


/**
 * A table model for image sample values (or pixels). This model is serialiable if the
 * underlying {@link RenderedImage} is serializable.
 *
 * @since 2.3
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/image/ImageTableModel.java $
 * @version $Id: ImageTableModel.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 *
 * @todo Should supports deferred execution: request for a new tile should wait some maximal amount
 *       of time (e.g. 0.1 seconds). If the tile is not yet available after that time, the model
 *       should returns {@code null} at this time and send a "data changed" event later when the
 *       tile is finally available.
 *
 * @see ImageSampleValues
 */
public class ImageTableModel extends AbstractTableModel {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -408603520054548181L;

    /**
     * The image to display.
     */
    private RenderedImage image;

    /**
     * The format to use for formatting sample values.
     */
    private NumberFormat format = NumberFormat.getNumberInstance();

    /**
     * The format to use for formatting line and column labels.
     */
    private NumberFormat titleFormat = NumberFormat.getIntegerInstance();

    /**
     * The band to show.
     */
    private int band;

    /**
     * Image properites computed by {@link #update}. Those properties are used everytime
     * {@link #getValueAt} is invoked, which is why we cache them.
     */
    private transient int minX, minY, maxX, maxY,
            tileGridXOffset, tileGridYOffset, tileWidth, tileHeight, dataType;
    
    /**
     * The type of sample values. Is computed by {@link #update}.
     */
    private transient Class type = Number.class;

    /**
     * The row and column names. Will be created only when first needed.
     */
    private transient String[] rowNames, columnNames;

    /**
     * The pixel values as an object of the color model transfert type.
     * Cached for avoiding to much creation of the same object.
     */
    private transient Object pixel;

    /**
     * Creates a new table model.
     */
    public ImageTableModel() {
    }

    /**
     * Creates a new table model for the specified image.
     */
    public ImageTableModel(final RenderedImage image) {
        setRenderedImage(image);
    }

    /**
     * Returns the image to display, or {@code null} if none.
     */
    public RenderedImage getRenderedImage() {
        return image;
    }

    /**
     * Sets the image to display.
     */
    public void setRenderedImage(final RenderedImage image) {
        this.image = image;
        pixel       = null;
        rowNames    = null;
        columnNames = null;
        final int digits = update();
        format.setMinimumFractionDigits(digits);
        format.setMaximumFractionDigits(digits);
        fireTableStructureChanged();
    }

    /**
     * Updates transient fields after an image change. Also invoked after deserialization.
     * Returns the number of fraction digits to use for the format (to be ignored in the
     * case of deserialization, since the format is serialized).
     */
    private int update() {
        int digits = 0;
        if (image != null) {
            minX            = image.getMinX();
            minY            = image.getMinY();
            maxX            = image.getWidth()  + minX;
            maxY            = image.getHeight() + minY;
            tileGridXOffset = image.getTileGridXOffset();
            tileGridYOffset = image.getTileGridYOffset();
            tileWidth       = image.getTileWidth();
            tileHeight      = image.getTileHeight();
            dataType        = image.getSampleModel().getDataType();
            switch (dataType) {
                case DataBuffer.TYPE_BYTE:    // Fall through
                case DataBuffer.TYPE_SHORT:   // Fall through
                case DataBuffer.TYPE_USHORT:  // Fall through
                case DataBuffer.TYPE_INT:     type=Integer.class;           break;
                case DataBuffer.TYPE_FLOAT:   type=Float  .class; digits=2; break;
                case DataBuffer.TYPE_DOUBLE:  type=Double .class; digits=3; break;
                default:                      type=Number .class;           break;
            }
        } else {
            type = Number.class;
        }
        return digits;
    }

    /**
     * Recomputes transient fields after deserializations.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        update();
    }

    /**
     * Returns the band to display.
     */
    public int getBand() {
        return band;
    }

    /**
     * Set the band to display.
     */
    public void setBand(final int band) {
        if (band<0 || (image!=null && band>=image.getSampleModel().getNumBands())) {
            throw new IndexOutOfBoundsException();
        }
        this.band = band;
        fireTableDataChanged();
    }

    /**
     * Returns the format to use for formatting sample values.
     */
    public NumberFormat getNumberFormat() {
        return format;
    }

    /**
     * Sets the format to use for formatting sample values.
     */
    public void setNumberFormat(final NumberFormat format) {
        this.format = format;
        fireTableDataChanged();
    }

    /**
     * Returns the number of rows in the model, which is
     * the {@linkplain RenderedImage#getHeight image height}.
     */
    public int getRowCount() {
        return (image!=null) ? image.getHeight() : 0;
    }

    /**
     * Returns the number of columns in the model, which is
     * the {@linkplain RenderedImage#getWidth image width}.
     */
    public int getColumnCount() {
        return (image!=null) ? image.getWidth() : 0;
    }

    /**
     * Returns the row name. The names are the pixel row number, starting at
     * the {@linkplain RenderedImage#getMinY min y} value.
     */
    public String getRowName(final int row) {
        if (rowNames == null) {
            rowNames = new String[image.getHeight()];
        }
        String candidate = rowNames[row];
        if (candidate == null) {
            rowNames[row] = candidate = titleFormat.format(minY + row);
        }
        return candidate;
    }

    /**
     * Returns the column name. The names are the pixel column number, starting at
     * the {@linkplain RenderedImage#getMinX min x} value.
     */
    public String getColumnName(final int column) {
        if (columnNames == null) {
            if (image == null) {
                return super.getColumnName(column);
            }
            columnNames = new String[image.getWidth()];
        }
        String candidate = columnNames[column];
        if (candidate == null) {
            columnNames[column] = candidate = titleFormat.format(minX + column);
        }
        return candidate;
    }

    /**
     * Returns a column given its name.
     */
    public int findColumn(final String name) {
        if (image!=null) try {
            return titleFormat.parse(name).intValue() - minX;
        } catch (ParseException exception) {
            // Ignore; fallback on the default algorithm.
        }
        return super.findColumn(name);
    }

    /**
     * Returns the type of sample values regardless of column index.
     */
    public Class getColumnClass(final int column) {
        return type;
    }

    /**
     * Returns the raster at the specified pixel location, or {@code null} if none.
     * The (<var>x</var>, <var>y</var>) <strong>must</strong> be additionned with
     * {@link #minX} and {@link #minY}.
     */
    private final Raster getRasterAt(final int y, final int x) {
        if (x<minX || x>=maxX || y<minY || y>=maxY) {
            return null;
        }
        int tx = x-tileGridXOffset; if (x<0) tx += 1-tileWidth;
        int ty = y-tileGridYOffset; if (y<0) ty += 1-tileHeight;
        return image.getTile(tx/tileWidth, ty/tileHeight);
    }

    /**
     * Returns the sample value at the specified row and column.
     */
    public Object getValueAt(int y, int x) {
        final Raster raster = getRasterAt(y+=minY, x+=minX);
        if (raster == null) {
            return null;
        }
        switch (dataType) {
            default:                      return new Integer(raster.getSample      (x,y,band));
            case DataBuffer.TYPE_FLOAT:   return new Float  (raster.getSampleFloat (x,y,band));
            case DataBuffer.TYPE_DOUBLE:  return new Double (raster.getSampleDouble(x,y,band));
        }
    }

    /**
     * Returns the color at the specified row and column.
     */
    public Color getColorAt(int y, int x) {
        final Raster raster = getRasterAt(y+=minY, x+=minX);
        if (raster == null) {
            return null;
        }
        pixel = raster.getDataElements(x, y, pixel);
        return new Color(image.getColorModel().getRGB(pixel), true);
    }

    /**
     * A table model for row headers. This model has only one column, and each cell values
     * is the {@linkplain ImageTableModel#getRowName row name} defined in the enclosing class.
     * A table using this model can be set as the
     * {@linkplain javax.swing.JScrollPane#setRowHeaderView scroll pane's row header} for an
     * image table.
     *
     * @since 2.2
     * @version $Id: ImageTableModel.java 30655 2008-06-12 20:24:25Z acuster $
     * @author Martin Desruisseaux (IRD)
     *
     * @see javax.swing.JScrollPane#setRowHeader
     */
    public class RowHeaders extends AbstractTableModel implements TableModelListener {
        /**
         * Serial number for compatibility with different versions.
         */
        private static final long serialVersionUID = 5162324745024331522L;

        /**
         * Creates a new instance of row headers. This constructor immediately register
         * the new instance as a listener of the enclosing {@link ImageTableModel}.
         */
        public RowHeaders() {
            ImageTableModel.this.addTableModelListener(this);
        }

        /**
         * Returns the number of rows in the model. This is identical to
         * the number of rows in the enclosing {@link ImageTableModel}.
         */
        public int getRowCount() {
             return ImageTableModel.this.getRowCount();
        }

        /**
         * Returns the number of columns in the model, which is 1.
         */
        public int getColumnCount() {
            return 1;
        }

        /**
         * Returns the type of row headers, which is {@code String.class}.
         */
        public Class getColumnClass(final int column) {
            return String.class;
        }
 
        /**
         * Returns the row name for the given index, regardless of the column.
         */
        public Object getValueAt(final int row, final int column) {
            return getRowName(row);
        }

        /**
         * Invoked when the enclosing {@link ImageTableModel} data changed. This method fires
         * an event for this model as well except if the change was not a change in the table
         * structure.
         */
        public void tableChanged(final TableModelEvent event) {
            final int firstRow = event.getFirstRow();
            final int  lastRow = event.getLastRow();
            final int     type = event.getType();
            if (type!=TableModelEvent.UPDATE || lastRow==Integer.MAX_VALUE) {
                fireTableChanged(new TableModelEvent(this, firstRow, lastRow, 0, type));
            }
        }
    }
}
