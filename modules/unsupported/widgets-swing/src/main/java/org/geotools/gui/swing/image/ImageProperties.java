/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.lang.reflect.Array;

import java.awt.Image;
import java.awt.image.DataBuffer;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.renderable.RenderableImage;
import javax.media.jai.OperationNode;
import javax.media.jai.PropertySource;
import javax.media.jai.PropertyChangeEmitter;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.OperationDescriptor;

import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * A panel showing image properties. An image can actually be any instance of
 * {@link PropertySource}, {@link RenderedImage} or {@link RenderableImage} interfaces.
 * The method {@link PropertySource#getProperty} will be invoked only when a property
 * is first required, in order to avoid the computation of deferred properties before
 * needed. If the source implements also the {@link PropertyChangeEmitter} interface,
 * then this widget will register a listener for property changes. The changes can be
 * emitted from any thread, which may or may not be the <cite>Swing</cite> thread.
 * <p>
 * If the image is an instance of {@link RenderedImage}, then this panel will also show
 * informations about the {@linkplain ColorModel color model}, {@linkplain SampleModel
 * sample model}, image size, tile size, etc.
 *
 * @since 2.3
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/image/ImageProperties.java $
 * @version $Id: ImageProperties.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 *
 * @see org.geotools.gui.swing.ParameterEditor
 * @see OperationTreeBrowser
 */
public class ImageProperties extends JPanel {
    /**
     * The operation name, or the image class name if the image is not an instance of
     * {@link OperationNode}.
     */
    private final JLabel operationName = new JLabel(" ");

    /**
     * The operation description.
     */
    private final JLabel operationDescription = new JLabel(" ");

    /**
     * The operation vendor and version.
     */
    private final JLabel operationVersion = new JLabel(" ");

    /**
     * The text area for image size.
     */
    private final JLabel imageSize = new JLabel();

    /**
     * The text area for tile size.
     */
    private final JLabel tileSize = new JLabel();

    /**
     * The text area for sample type (e.g. "8 bits unsigned integer".
     */
    private final JLabel dataType = new JLabel();

    /**
     * The text area for the sample model.
     */
    private final JLabel sampleModel = new JLabel();

    /**
     * The text area for the color model.
     */
    private final JLabel colorModel = new JLabel();

    /**
     * The color bar for {@link IndexColorModel}.
     */
    private final ColorRamp colorRamp = new ColorRamp();

    /**
     * The table model for image's properties.
     */
    private final Table properties;

    /**
     * The table for sample values.
     */
    private final ImageSampleValues samples;

    /**
     * The viewer for an image quick look.
     */
    private final ImagePane viewer;

    /**
     * Create a new instance of {@code ImageProperties} with no image.
     * One of {@link #setImage(PropertySource) setImage(...)} methods must
     * be invoked in order to set the property source.
     */
    public ImageProperties() {
        super(new BorderLayout());
        final Vocabulary resources = Vocabulary.getResources(getLocale());
        final JTabbedPane     tabs = new JTabbedPane();
        final GridBagConstraints c = new GridBagConstraints();
        /*
         * Build the informations tab.
         */
        if (true) {
            final JPanel panel = new JPanel(new GridBagLayout());
            c.anchor=c.WEST; c.fill=c.HORIZONTAL; c.insets.left=9;
            c.gridx=0; c.gridwidth=2; c.weightx=1;
            c.gridy=0; panel.add(operationName,        c);
            c.gridy++; panel.add(operationDescription, c); c.insets.bottom=15;
            c.gridy++; panel.add(operationVersion,     c);

            final int ytop = c.gridy;
            c.gridwidth=1; c.weightx=0; c.insets.bottom=0;
            c.gridy++; panel.add(getLabel(VocabularyKeys.IMAGE_SIZE,   resources), c);
            c.gridy++; panel.add(getLabel(VocabularyKeys.TILES_SIZE,   resources), c);
            c.gridy++; panel.add(getLabel(VocabularyKeys.DATA_TYPE,    resources), c);
            c.gridy++; panel.add(getLabel(VocabularyKeys.SAMPLE_MODEL, resources), c);
            c.gridy++; panel.add(getLabel(VocabularyKeys.COLOR_MODEL,  resources), c);
            c.gridy++; panel.add(getLabel(VocabularyKeys.COLORS,       resources), c);

            c.gridx=1; c.gridy=ytop; c.weightx=1;
            c.gridy++; panel.add(imageSize,   c);
            c.gridy++; panel.add(tileSize,    c);
            c.gridy++; panel.add(dataType,    c);
            c.gridy++; panel.add(sampleModel, c);
            c.gridy++; panel.add(colorModel,  c);
            c.gridy++; c.anchor=c.CENTER; c.insets.right=6;

            panel.add(colorRamp, c);
            tabs.addTab(resources.getString(VocabularyKeys.INFORMATIONS), panel);
        }
        /*
         * Build the image's properties tab.
         */
        if (true) {
            properties = new Table(resources);
            final JTable table = new JTable(properties);
            tabs.addTab(resources.getString(VocabularyKeys.PROPERTIES), new JScrollPane(table));
        }
        /*
         * Build the image sample value tab.
         */
        if (true) {
            samples = new ImageSampleValues();
            tabs.addTab(resources.getString(VocabularyKeys.PIXELS), samples);
        }
        /*
         * Build the image preview tab.
         */
        if (true) {
            viewer = new ImagePane();
            viewer.setPaintingWhileAdjusting(true);
            tabs.addTab(resources.getString(VocabularyKeys.PREVIEW), viewer.createScrollPane());
        }
        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(400,250));
    }

    /**
     * Returns the localized label for the given key.
     */
    private static JLabel getLabel(final int key, final Vocabulary resources) {
        return new JLabel(resources.getLabel(key));
    }

    /**
     * Create a new instance of {@code ImageProperties} for the specified
     * rendered image.
     *
     * @param image The image, or {@code null} if none.
     */
    public ImageProperties(final RenderedImage image) {
        this();
        if (image != null) {
            setImage(image);
        }
    }

    /**
     * Set the operation name, description and version for the given image. If the image is
     * an instance of {@link OperationNode}, then a description of the operation will be fetch
     * from its resources bundle.
     *
     * @param image The image, or {@code null} if none.
     */
    private void setDescription(final Object image) {
        String name        = " ";
        String description = " ";
        String version     = " ";
        final Locale     locale    = getLocale();
        final Vocabulary resources = Vocabulary.getResources(locale);
        if (image instanceof OperationNode) {
            final String mode;
            final RegistryElementDescriptor descriptor;
            final OperationNode operation = (OperationNode) image;
            name       = operation.getOperationName();
            mode       = operation.getRegistryModeName();
            descriptor = operation.getRegistry().getDescriptor(mode, name);
            if (descriptor instanceof OperationDescriptor) {
                final ResourceBundle bundle;
                bundle      = ((OperationDescriptor) descriptor).getResourceBundle(locale);
                name        = bundle   .getString("LocalName");
                description = bundle   .getString("Description");
                version     = resources.getString(VocabularyKeys.VERSION_$1,
                              bundle   .getString("Version")) + ", " +
                              bundle   .getString("Vendor");
                name = resources.getString(VocabularyKeys.OPERATION_$1, name);
            }
        } else if (image != null) {
            name = Classes.getShortClassName(image);
            name = resources.getString(VocabularyKeys.IMAGE_CLASS_$1, name);
        }
        operationName       .setText(name       );
        operationDescription.setText(description);
        operationVersion    .setText(version    );
    }

    /**
     * Set all text fields to {@code null}. This method do not set the {@link #properties}
     * table; this is left to the caller.
     */
    private void clear() {
        imageSize  .setText(null);
        tileSize   .setText(null);
        dataType   .setText(null);
        sampleModel.setText(null);
        colorModel .setText(null);
        colorRamp  .setColors((IndexColorModel)null);
    }

    /**
     * Set the {@linkplain PropertySource property source} for this widget. If the source is a
     * {@linkplain RenderedImage rendered} or a {@linkplain RenderableImage renderable} image,
     * then the widget will be set as if the most specific flavor of {@code setImage(...)}
     * was invoked.
     *
     * @param image The image, or {@code null} if none.
     */
    public void setImage(final PropertySource image) {
        if (image instanceof RenderedImage) {
            setImage((RenderedImage) image);
            return;
        }
        if (image instanceof RenderableImage) {
            setImage((RenderableImage) image);
            return;
        }
        clear();
        setDescription(image);
        properties.setSource(image);
        viewer    .setImage((RenderedImage) null);
        samples   .setImage((RenderedImage) null);
    }

    /**
     * Set the specified {@linkplain RenderableImage renderable image} as the properties source.
     *
     * @param image The image, or {@code null} if none.
     */
    public void setImage(final RenderableImage image) {
        clear();
        if (image != null) {
            final Vocabulary resources = Vocabulary.getResources(getLocale());
            imageSize.setText(resources.getString(VocabularyKeys.SIZE_$2,
                              new Float(image.getWidth()),
                              new Float(image.getHeight())));
        }
        setDescription(image);
        properties.setSource(image);
        viewer    .setImage (image);
        samples   .setImage ((RenderedImage) null);
    }

    /**
     * Set the specified {@linkplain RenderedImage rendered image} as the properties source.
     *
     * @param image The image, or {@code null} if none.
     */
    public void setImage(final RenderedImage image) {
        if (image == null) {
            clear();
        } else {
            final Vocabulary resources = Vocabulary.getResources(getLocale());
            final  ColorModel cm = image.getColorModel();
            final SampleModel sm = image.getSampleModel();
            imageSize.setText(resources.getString(VocabularyKeys.IMAGE_SIZE_$3,
                              new Integer(image.getWidth()),
                              new Integer(image.getHeight()),
                              new Integer(sm.getNumBands())));
            tileSize.setText(resources.getString(VocabularyKeys.TILE_SIZE_$4,
                              new Integer(image.getNumXTiles()),
                              new Integer(image.getNumYTiles()),
                              new Integer(image.getTileWidth()),
                              new Integer(image.getTileHeight())));
            dataType   .setText(getDataType(sm.getDataType(), cm, resources));
            sampleModel.setText(formatClassName(sm, resources));
            colorModel .setText(formatClassName(cm, resources));
            if (cm instanceof IndexColorModel) {
                colorRamp.setColors((IndexColorModel) cm);
            } else {
                colorRamp.setColors((IndexColorModel) null);
            }
        }
        setDescription(image);
        properties.setSource(image);
        viewer    .setImage (image);
        samples   .setImage (image);
    }

    /**
     * Returns a string representation for the given data type.
     *
     * @param  type The data type (one of {@link DataBuffer} constants).
     * @param  cm The color model, for computing the pixel size in bits.
     * @param  resources The resources to use for formatting the type.
     * @return The data type as a localized string.
     */
    private static String getDataType(final int        type,
                                      final ColorModel cm,
                                      final Vocabulary resources)
    {
        final int key;
        switch (type) {
            case DataBuffer.TYPE_BYTE:      // Fall through
            case DataBuffer.TYPE_USHORT:    key=VocabularyKeys.UNSIGNED_INTEGER_$2; break;
            case DataBuffer.TYPE_SHORT:     // Fall through
            case DataBuffer.TYPE_INT:       key=VocabularyKeys.SIGNED_INTEGER_$1; break;
            case DataBuffer.TYPE_FLOAT:     // Fall through
            case DataBuffer.TYPE_DOUBLE:    key=VocabularyKeys.REAL_NUMBER_$1; break;
            case DataBuffer.TYPE_UNDEFINED: // Fall through
            default: return resources.getString(VocabularyKeys.UNDEFINED);
        }
        final Integer  typeSize = new Integer(DataBuffer.getDataTypeSize(type));
        final Integer pixelSize = (cm!=null) ? new Integer(cm.getPixelSize()) : typeSize;
        return resources.getString(key, typeSize, pixelSize);
    }

    /**
     * Split a class name into a more human readeable sentence
     * (e.g. "PixelInterleavedSampleModel" into "Pixel interleaved sample model").
     *
     * @param  object The object to format.
     * @param  resources The resources to use for formatting localized text.
     * @return The object class name.
     */
    private static String formatClassName(final Object object, final Vocabulary resources) {
        if (object == null) {
            return resources.getString(VocabularyKeys.UNDEFINED);
        }
        final String name = Classes.getShortClassName(object);
        final int length = name.length();
        final StringBuilder buffer = new StringBuilder(length + 8);
        int last = 0;
        for (int i=1; i<=length; i++) {
            if (i==length ||
                (Character.isUpperCase(name.charAt(i  )) &&
                 Character.isLowerCase(name.charAt(i-1))))
            {
                final int pos = buffer.length();
                buffer.append(name.substring(last, i));
                buffer.append(' ');
                if (pos!=0 && last<length-1 && Character.isLowerCase(name.charAt(last+1))) {
                    buffer.setCharAt(pos, Character.toLowerCase(buffer.charAt(pos)));
                }
                last = i;
            }
        }
        if (object instanceof IndexColorModel) {
            final IndexColorModel cm = (IndexColorModel) object;
            buffer.append(" (").append(resources.getString(VocabularyKeys.COLOR_COUNT_$1, cm.getMapSize()));
            buffer.append(')');
        }
        return buffer.toString().trim();
    }

    /**
     * The table model for image's properties. The image can actually be any of
     * {@link PropertySource}, {@link RenderedImage} or {@link RenderableImage}
     * interface. The method {@link PropertySource#getProperty} will be invoked
     * only when a property is first required, in order to avoid the computation
     * of deferred properties before needed. If the source implements also the
     * {@link PropertyChangeEmitter} interface, then this table will be registered
     * as a listener for property changes. The changes can be emitted from any thread,
     * which may or may not be the <cite>Swing</cite> thread.
     *
     * @version $Id: ImageProperties.java 30655 2008-06-12 20:24:25Z acuster $
     * @author Martin Desruisseaux (IRD)
     *
     * @todo Check for {@code WritablePropertySource} and make cells editable accordingly.
     */
    private static final class Table extends AbstractTableModel implements PropertyChangeListener {
        /**
         * The resources for formatting localized strings.
         */
        private final Vocabulary resources;

        /**
         * The property sources. Usually (but not always) the same object than
         * {@link #changeEmitter}. May be {@code null} if no source has been set.
         */
        private PropertySource source;

        /**
         * The property change emitter, or {@code null} if none. Usually (but not always)
         * the same object than {@link #source}.
         */
        private PropertyChangeEmitter changeEmitter;

        /**
         * The properties names, or {@code null} if none.
         */
        private String[] names;

        /**
         * Constructs a default table with no properties source. The method {@link #setSource}
         * must be invoked after the construction in order to display some image's properties.
         *
         * @param resources The resources for formatting localized strings.
         */
        public Table(final Vocabulary resources) {
            this.resources = resources;
        }

        /**
         * Wrap the specified {@link RenderedImage} into a {@link PropertySource}.
         */
        private static PropertySource wrap(final RenderedImage image) {
            return new PropertySource() {
                public String[] getPropertyNames() {
                    return image.getPropertyNames();
                }
                public String[] getPropertyNames(final String prefix) {
                    // TODO: Not the real answer, but this method
                    // is not needed by this Table implementation.
                    return getPropertyNames();
                }
                public Class getPropertyClass(final String name) {
                    return null;
                }
                public Object getProperty(final String name) {
                    return image.getProperty(name);
                }
            };
        }

        /**
         * Wrap the specified {@link RenderableImage} into a {@link PropertySource}.
         */
        private static PropertySource wrap(final RenderableImage image) {
            return new PropertySource() {
                public String[] getPropertyNames() {
                    return image.getPropertyNames();
                }
                public String[] getPropertyNames(final String prefix) {
                    // TODO: Not the real answer, but this method
                    // is not needed by this Table implementation.
                    return getPropertyNames();
                }
                public Class getPropertyClass(final String name) {
                    return null;
                }
                public Object getProperty(final String name) {
                    return image.getProperty(name);
                }
            };
        }

        /**
         * Set the source as a {@link PropertySource}, a {@link RenderedImage} or a
         * {@link RenderableImage}. If the source implements the {@link PropertyChangeEmitter}
         * interface, then this table will be registered as a listener for property changes.
         * The changes can be emitted from any thread (may or may not be the Swing thread).
         *
         * @param image The properties source, or {@code null} for removing any source.
         */
        public void setSource(final Object image) {
            if (image == source) {
                return;
            }
            if (changeEmitter != null) {
                changeEmitter.removePropertyChangeListener(this);
                changeEmitter = null;
            }
            if (image instanceof PropertySource) {
                source = (PropertySource) image;
            } else if (image instanceof RenderedImage) {
                source = wrap((RenderedImage) image);
            } else if (image instanceof RenderableImage) {
                source = wrap((RenderableImage) image);
            } else {
                source = null;
            }
            names = (source!=null) ? source.getPropertyNames() : null;
            if (image instanceof PropertyChangeEmitter) {
                changeEmitter = (PropertyChangeEmitter) image;
                changeEmitter.addPropertyChangeListener(this);
            }
            fireTableDataChanged();
        }

        /**
         * Returns the number of rows, which is equals to the number of properties.
         */
        public int getRowCount() {
            return (names!=null) ? names.length : 0;
        }

        /**
         * Returns the number of columns, which is 2 (the property name and its value).
         */
        public int getColumnCount() {
            return 2;
        }

        /**
         * Returns the column name for the given index.
         */
        public String getColumnName(final int column) {
            final int key;
            switch (column) {
                case 0: key=VocabularyKeys.NAME;  break;
                case 1: key=VocabularyKeys.VALUE; break;
                default: throw new IndexOutOfBoundsException(String.valueOf(column));
            }
            return resources.getString(key);
        }

        /**
         * Returns the most specific superclass for all the cell values in the column.
         */
        public Class getColumnClass(final int column) {
            switch (column) {
                case 0: return String.class;
                case 1: return Object.class;
                default: throw new IndexOutOfBoundsException(String.valueOf(column));
            }
        }

        /**
         * Returns the property for the given cell.
         *
         * @param  row The row index.
         * @param  column The column index.
         * @return The cell value at the given index.
         * @throws IndexOutOfBoundsException if the row or the column is out of bounds.
         */
        public Object getValueAt(int row, int column) throws IndexOutOfBoundsException {
            final String name = names[row];
            switch (column) {
                case 0: {
                    return name;
                }
                case 1: {
                    Object value = source.getProperty(name);
                    if (value == Image.UndefinedProperty) {
                        value = resources.getString(VocabularyKeys.UNDEFINED);
                    }
                    return expandArray(value);
                }
                default: {
                    throw new IndexOutOfBoundsException(String.valueOf(column));
                }
            }
        }

        /**
         * If the specified object is an array, enumerate the array components.
         * Otherwise, returns the object unchanged. This method is sligtly different
         * than {@link java.util.Arrays#toString(Object[])} in that it expands inner
         * array components recursively.
         */
        private static Object expandArray(final Object array) {
            if (array!=null && array.getClass().isArray()) {
                final StringBuffer buffer = new StringBuffer();
                buffer.append('{');
                final int length = Array.getLength(array);
                for (int i=0; i<length; i++) {
                    if (i != 0) {
                        buffer.append(", ");
                    }
                    buffer.append(expandArray(Array.get(array, i)));
                }
                buffer.append('}');
                return buffer.toString();
            }
            return array;
        }

        /**
         * Invoked when a property changed. This method find the row for the modified
         * property and fire a table change event.
         *
         * @param The property change event.
         */
        public void propertyChange(final PropertyChangeEvent event) {
            /*
             * Make sure that we are running in the Swing thread.
             */
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        propertyChange(event);
                    }
                });
                return;
            }
            /*
             * Find the rows for the modified property, and fire a "table updated' event.
             */
            final String name = event.getPropertyName();
            int first = getRowCount(); // Past the last row.
            int last  = -1;            // Before the first row.
            if (name == null) {
                last  = first-1;
                first = 0;
            } else {
                for (int i=first; --i>=0;) {
                    if (names[i].equalsIgnoreCase(name)) {
                        first = i;
                        if (last < 0) {
                            last = i;
                        }
                    }
                }
            }
            if (first <= last) {
                fireTableRowsUpdated(first, last);
            }
        }
    }
}
