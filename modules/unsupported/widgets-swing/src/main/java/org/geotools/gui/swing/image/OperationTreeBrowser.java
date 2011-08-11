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

import java.util.List;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.RenderedImage;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.jai.RenderedOp;
import javax.media.jai.RenderableOp;
import javax.media.jai.PropertySource;
import javax.media.jai.OperationNode;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.KernelJAI;

import org.geotools.resources.Classes;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.gui.swing.IconFactory;
import org.geotools.gui.swing.ParameterEditor;
import org.geotools.gui.swing.tree.Trees;
import org.geotools.gui.swing.tree.TreeNode;
import org.geotools.gui.swing.tree.NamedTreeNode;
import org.geotools.gui.swing.tree.MutableTreeNode;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;
import org.geotools.resources.Arguments;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Display a chain of images as a tree. It may be a chain of {@link RenderedImage} or a chain of
 * {@link RenderableImage}. Those images are often the result of some operation (i.e. are actually
 * instances of {@link RenderedOp} or {@link RenderableOp}). The image given to the constructor is
 * the root of the tree. The root contains the following children nodes:
 *
 * <ul>
 *   <li>One node for each {@linkplain RenderedImage#getSources source image}, if any.</li>
 *   <li>One node for each {@linkplain OperationNode#getParameterBlock image parameter}, if any.</li>
 * </ul>
 *
 * Each source image can have its own source and parameters. In an analogy to a file system,
 * {@linkplain RenderedImage#getSources source images} are like directories and
 * {@linkplain OperationNode#getParameterBlock image parameters} are like files.
 *
 * When a tree node is selected in the left pane, the content of the right pane is adjusted
 * accordingly. If the node is an image, a "preview" tab is show together with an "information"
 * tab. Informations include the {@linkplain java.awt.image.ColorModel color model},
 * {@linkplain java.awt.image.SampleModel sample model}, data type, etc. If the selected tree node
 * is a parameter, then the right pane show the parameter value in {@linkplain ParameterEditor
 * some widget} appropriate for the parameter type.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/OperationTreeBrowser.png"></p>
 * <p>&nbsp;</p>
 *
 * @since 2.3
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/image/OperationTreeBrowser.java $
 * @version $Id: OperationTreeBrowser.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 * @author Lionel Flahaut
 *
 * @see ImageProperties
 * @see ParameterEditor
 * @see RegisteredOperationBrowser
 */
@SuppressWarnings("serial")
public class OperationTreeBrowser extends JPanel {
    /**
     * Key for {@link PropertySource}.
     */
    private static final String IMAGE = "Image";

    /**
     * Key for parameter card.
     */
    private static final String PARAMETER = "Parameter";

    /**
     * The image properties panel. Will be constructed only when first needed,
     * and the added to the card layout with the {@code IMAGE} name.
     */
    private ImageProperties imageProperties;

    /**
     * The parameter properties panel. Will be constructed only when first needed,
     * and the added to the card layout with the {@code PARAMETER} name.
     */
    private ParameterEditor parameterEditor;

    /**
     * The properties panel. The content for this panel depends on
     * the selected tree item, but usually includes the following:
     * <ul>
     *   <li>An {@link ImageProperties} instance.</li>
     *   <li>An {@link ParameterEditor} instance.</li>
     * </ul>
     */
    private final Container cards = new JPanel(new CardLayout());

    /**
     * Constructs a new browser for the given rendered image.
     *
     * @param source The last image from the rendering chain to browse.
     */
    public OperationTreeBrowser(final RenderedImage source) {
        this(getTree(source, getDefaultLocale()));
    }

    /**
     * Constructs a new browser for the given renderable image.
     *
     * @param source The last image from the rendering chain to browse.
     */
    public OperationTreeBrowser(final RenderableImage source) {
        this(getTree(source, getDefaultLocale()));
    }

    /**
     * Constructs a new browser for the tree.
     *
     * @param model The tree model built from the rendering chain to browse.
     */
    private OperationTreeBrowser(final TreeModel model) {
        super(new BorderLayout());
        final Listeners listeners = new Listeners();
        final JTree tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new CellRenderer());
        tree.setBorder(BorderFactory.createEmptyBorder(6,6,0,0));
        tree.addTreeSelectionListener(listeners);

        final JSplitPane split;
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), cards);
        split.setDividerLocation(220);
        add(split, BorderLayout.CENTER);

        setPreferredSize(new Dimension(600,250));
    }

    /**
     * Show the operation tree for the specified rendered image in a frame.
     * This convenience method is mostly a helper for debugging purpose.
     */
    public static void show(final RenderedImage image) {
        new OperationTreeBrowser(image).showFrame(image);
    }

    /**
     * Show the operation tree for the specified renderable image in a frame.
     * This convenience method is mostly a helper for debugging purpose.
     */
    public static void show(final RenderableImage image) {
        new OperationTreeBrowser(image).showFrame(image);
    }

    /**
     * Returns a name for the given image. The default implementation returns the operation
     * name if the image is an instance of {@link RenderedOp}. Otherwise, it returns the
     * image class.
     *
     * @param  image The image.
     * @return A name for the given image.
     */
    private static String getName(final Object image) {
        if (image instanceof OperationNode) {
            return ((OperationNode) image).getOperationName();
        }
        if (image instanceof CharSequence) {
            return image.toString();
        }
        return Classes.getShortClassName(image);
    }

    /**
     * Prints to the {@linkplain System#out standard output stream} the operation chain for
     * the specified image. This convenience method is used mostly for debugging purpose.
     *
     * @since 2.4
     */
    public static void print(final RenderedImage image) {
        Trees.print(getTree(image, Locale.getDefault()));
    }

    /**
     * Prints to the {@linkplain System#out standard output stream} the operation chain for
     * the specified image. This convenience method is used mostly for debugging purpose.
     *
     * @since 2.4
     */
    public static void print(final RenderableImage image) {
        Trees.print(getTree(image, Locale.getDefault()));
    }

    /**
     * Returns a tree with all sources and parameters for the given rendered image.
     *
     * @param  image The last image from an operation chain.
     * @param  locale The locale for tree node names.
     * @return The tree for the given image and all its sources.
     */
    public static TreeModel getTree(final RenderedImage image, final Locale locale) {
        return new DefaultTreeModel(getNode(image, locale));
    }

    /**
     * Returns a tree with all sources and parameters for the given renderable image.
     *
     * @param  image The last image from an operation chain.
     * @param  locale The locale for tree node names.
     * @return The tree for the given image and all its sources.
     */
    public static TreeModel getTree(final RenderableImage image, final Locale locale) {
        return new DefaultTreeModel(getNode(image, locale));
    }

    /**
     * Returns the root node of a tree with all sources and parameters for the given source.
     *
     * @param  image The last image from an operation chain.
     * @param  locale The locale for tree node names.
     * @return The tree for the given image and all its sources.
     */
    private static MutableTreeNode getNode(final RenderedImage image, final Locale locale) {
        final DefaultMutableTreeNode root = new NamedTreeNode(getName(image), image);
        final List sources = image.getSources();
        if (sources != null) {
            final int n = sources.size();
            for (int i=0; i<n; i++) {
                root.add(getNode((RenderedImage)sources.get(i), locale));
            }
        }
        if (image instanceof OperationNode) {
            addParameters(root, (OperationNode)image, locale);
        }
        return root;
    }

    /**
     * Returns the root node of a tree with all sources and parameters for the given source.
     *
     * @param  image The last image from an operation chain.
     * @param  locale The locale for tree node names.
     * @return The tree for the given image and all its sources.
     */
    private static MutableTreeNode getNode(final RenderableImage image, final Locale locale) {
        final DefaultMutableTreeNode root = new NamedTreeNode(getName(image), image);
        final List sources = image.getSources();
        if (sources != null) {
            final int n = sources.size();
            for (int i=0; i<n; i++) {
                root.add(getNode((RenderableImage)sources.get(i), locale));
            }
        }
        if (image instanceof OperationNode) {
            addParameters(root, (OperationNode)image, locale);
        }
        return root;
    }

    /**
     * Add the parameters from the specified operation to the specified tree node.
     *
     * @param root The tree node to add parameters to.
     * @param operation The operation for which to fetch parameters.
     * @param  locale The locale for tree node names.
     */
    private static void addParameters(final DefaultMutableTreeNode root,
                                      final OperationNode     operation,
                                      final Locale            locale)
    {
        final ParameterBlock param = operation.getParameterBlock();
        final ParameterListDescriptor descriptor;
        if (param instanceof ParameterList) {
            descriptor = ((ParameterList) param).getParameterListDescriptor();
        } else {
            final String name = operation.getOperationName();
            final String mode = operation.getRegistryModeName();
            descriptor = operation.getRegistry().getDescriptor(mode, name)
                                                .getParameterListDescriptor(mode);
        }
        Vocabulary resources = null;
        final String[] names = descriptor.getParamNames();
        final int n = param.getNumParameters();
        for (int i=0; i<n; i++) {
            String name = null;
            if (names!=null && i<names.length) {
                name = names[i];
            }
            if (name == null) {
                if (resources == null) {
                    resources = Vocabulary.getResources(locale);
                }
                name = resources.getString(VocabularyKeys.PARAMETER_$1, new Integer(i));
            }
            root.add(new NamedTreeNode(name, param.getObjectParameter(i), false));
        }
    }

    /**
     * The listener for various event in the {@link OperationTreeBrowser} widget.
     *
     * @version $Id: OperationTreeBrowser.java 30655 2008-06-12 20:24:25Z acuster $
     * @author Martin Desruisseaux (IRD)
     */
    private final class Listeners implements TreeSelectionListener {
        /**
         * Called whenever the value of the selection changes. This method uses the
         * {@link TreeNode#getAllowsChildren} in order to determines if the selection
         * is a source (allows children = {@code true}) or a parameter
         * (allows children = {@code false}).
         */
        public void valueChanged(final TreeSelectionEvent event) {
            Object        selection  = null;   // The selected tree element.
            boolean       isSource   = false;  // Is 'selected' a source or a parameter?
            OperationNode operation  = null;   // The parent of the selected element as an op.
            int           paramIndex = -1;     // The index of the selected element.
            final TreePath path = event.getPath();
            if (path != null) {
                selection = path.getLastPathComponent();
                /*
                 * Some of piece of code in the following block can work with the Swing's
                 * TreeNode (i.e. it doesn't require the fixed Geotools's TreeNode).
                 */
                if (selection instanceof javax.swing.tree.TreeNode) {
                    javax.swing.tree.TreeNode node = (javax.swing.tree.TreeNode)selection;
                    isSource = node.getAllowsChildren();
                    node = node.getParent();
                    if (node instanceof TreeNode) {
                        final Object candidate = ((TreeNode)node).getUserObject();
                        if (candidate instanceof OperationNode) {
                            operation = (OperationNode) candidate;
                            final int count = node.getChildCount();
                            for (int n=-1,i=0; i<count; i++) {
                                final javax.swing.tree.TreeNode leaf=node.getChildAt(i);
                                if (!leaf.getAllowsChildren()) {
                                    n++; // Count only parameters, not sources.
                                }
                                if (leaf == selection) {
                                    paramIndex = n;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (selection instanceof TreeNode) {
                    selection = ((TreeNode) selection).getUserObject();
                }
            }
            if (isSource) {
                showSourceEditor(selection);
            } else {
                showParameterEditor(selection);
            }
            if (parameterEditor != null) {
                parameterEditor.setDescription(operation, paramIndex);
            }
        }
    }

    /**
     * Invoked when the user clicks on a source node in the operation tree (left pane).
     * This method show a properties panel in the right pane appropriate for the given
     * selection.
     *
     * @param  selection The user selection. This object is usually an instance of
     *         {@link RenderedImage}, {@link RenderableImage} or {@link PropertySource}.
     * @return {@code true} if this method has been able to find an editor, or
     *         {@code false} otherwise.
     */
    protected boolean showSourceEditor(final Object selection) {
        if (imageProperties == null) {
            imageProperties = new ImageProperties();
            cards.add(imageProperties, IMAGE);
        }
        ((CardLayout) cards.getLayout()).show(cards, IMAGE);
        if (selection instanceof RenderedImage) {
            imageProperties.setImage((RenderedImage) selection);
            return true;
        }
        if (selection instanceof RenderableImage) {
            imageProperties.setImage((RenderableImage) selection);
            return true;
        }
        if (selection instanceof PropertySource) {
            imageProperties.setImage((PropertySource) selection);
            return true;
        }
        imageProperties.setImage((PropertySource) null);
        return false;
    }

    /**
     * Invoked when the user clicks on a parameter node in the operation tree (left pane).
     * This method show a properties panel in the right pane appropriate for the given
     * selection.
     *
     * @param  selection The user selection. This object is usually an instance of
     *         {@link Number}, {@link KernelJAI}, {@link LookupTableJAI} or some other
     *         parameter object.
     * @return {@code true} if this method has been able to find an editor, or
     *         {@code false} otherwise.
     */
    protected boolean showParameterEditor(final Object selection) {
        if (parameterEditor == null) {
            parameterEditor = new ParameterEditor();
            cards.add(parameterEditor, PARAMETER);
        }
        ((CardLayout) cards.getLayout()).show(cards, PARAMETER);
        parameterEditor.setParameterValue(selection);
        return true;
    }

    /**
     * Show the operation chain in the given owner.
     *
     * @param  owner The owner widget, or {@code null} if none.
     * @param  title The widget title, or {@code null} for a default one.
     * @return {@code true} if the user clicked on the "Ok" button.
     */
    public boolean showDialog(final Component owner, String title) {
        if (title == null) {
            title = Vocabulary.getResources(getLocale()).getString(VocabularyKeys.OPERATIONS);
        }
        if (SwingUtilities.showOptionDialog(owner, this, title)) {
            // TODO: User clicked on "Ok".
            return true;
        }
        return false;
    }

    /**
     * Implementation of public {@link #show} methods.
     */
    private void showFrame(final Object image) {
        final JFrame frame = new JFrame(Classes.getShortClassName(this) + " - " + getName(image));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Display the properties for the images specified on the command line.
     *
     * @throws IOException if an error occured while reading an image.
     */
    public static void main(String[] args) throws IOException {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        for (int i=0; i<args.length; i++) {
            final File file = new File(args[i]);
            final RenderedImage image;
            try {
                image = ImageIO.read(file);
            } catch (FileNotFoundException e) {
                arguments.out.println(Errors.format(ErrorKeys.FILE_DOES_NOT_EXIST_$1, file));
                continue;
            }
            new OperationTreeBrowser(image).showFrame(file.getName());
        }
    }




    /**
     * The tree cell renderer, which select icons according the selected object type.
     *
     * @version $Id: OperationTreeBrowser.java 30655 2008-06-12 20:24:25Z acuster $
     * @author Martin Desruisseaux (IRD)
     */
    private static final class CellRenderer extends DefaultTreeCellRenderer {
        /** The icon for folder. */
        private final Icon open, closed;

        /** The icon for images, or {@code null} if none. */
        private static Icon image;

        /** The icon for parameters, or {@code null} if none. */
        private static Icon parameter;

        /**
         * Creates a cell renderer.
         */
        private CellRenderer() {
            open   = getDefaultOpenIcon();
            closed = getDefaultClosedIcon();
            if (image == null) {
                final IconFactory icons = IconFactory.DEFAULT;
                image     = icons.getIcon("toolbarButtonGraphics/general/Properties16.gif");
                parameter = icons.getIcon("toolbarButtonGraphics/general/Preferences16.gif");
            }
        }

        /**
         * Configures the renderer based on the passed in components.
         */
        @Override
        public Component getTreeCellRendererComponent(final JTree tree, final Object value,
                                                      final boolean selelected,
                                                      final boolean expanded,
                                                      final boolean leaf, final int row,
                                                      final boolean hasFocus)
        {
            if (((TreeNode) value).getUserObject() instanceof RenderedImage) {
                if (image != null) {
                    setOpenIcon  (image);
                    setClosedIcon(image);
                    setLeafIcon  (image);
                } else {
                    setLeafIcon(null);
                }
            } else if (parameter != null) {
                setOpenIcon  (open);
                setClosedIcon(closed);
                setLeafIcon  (parameter);
            }
            return super.getTreeCellRendererComponent(tree, value, selected, expanded,
                                                      leaf, row, hasFocus);
        }
    }
}
