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
import java.util.Arrays;
import java.util.Locale;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.logging.Level;

import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.Box;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.image.renderable.ParameterBlock; // For javadoc

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RegistryElementDescriptor;

import org.geotools.resources.Classes;
import org.geotools.resources.Arguments;
import org.geotools.gui.swing.IconFactory;
import org.geotools.gui.swing.tree.Trees;
import org.geotools.gui.swing.tree.TreeNode;
import org.geotools.gui.swing.tree.NamedTreeNode;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.util.logging.Logging;


/**
 * Browse through the registered JAI operations. This widget display a tree build from a
 * JAI's {@link OperationRegistry}. The tree has the following hierarchy:
 * <p>
 * <ul>
 *   <li>At the first level, all {@linkplain OperationRegistry#getRegistryModes() registry modes}
 *       (e.g. "rendered", "renderable", etc.) in alphabetical order.</li>
 *   <li>At the second level, all {@linkplain OperationRegistry#getDescriptors(String) operation
 *       descriptors} (e.g. "Affine", "Multiply", etc.) registered in each
 *       registry mode, in alphabetical order. This is the operation name to be given to
 *       {@link JAI#create(String,ParameterBlock) JAI.create(...)} methods.</li>
 *   <li>At the third level, a list of
 *       {@linkplain RegistryElementDescriptor#getParameterListDescriptor(String) parameters}
 *       as leafs, and the list of
 *       {@linkplain OperationRegistry#getOrderedProductList implementing products} as nodes.
 *       This level is not sorted in alphabetical order, since the ordering is relevant.</li>
 *   <li>At the last level, a list of {@linkplain OperationRegistry#getOrderedFactoryList
 *       factories} as leafs. This level is not sorted in alphabetical order, since the ordering
 *       is relevant.</li>
 * </ul>
 *
 * @since 2.3
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/widgets-swing/src/main/java/org/geotools/gui/swing/image/RegisteredOperationBrowser.java $
 * @version $Id: RegisteredOperationBrowser.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
@SuppressWarnings("serial")
public class RegisteredOperationBrowser extends JPanel {
    /**
     * The text area for operation's description.
     */
    private final JLabel description = new JLabel(" ");

    /**
     * The text area for the version and vendor.
     */
    private final JLabel version = new JLabel(" ");

    /**
     * Constructs a new operation browser for the default {@link JAI} instance.
     */
    public RegisteredOperationBrowser() {
        this(getTree());
    }

    /**
     * Constructs a new operation browser for the specified operation registry.
     *
     * @param registry The operation registry to use for fetching operations.
     */
    public RegisteredOperationBrowser(final OperationRegistry registry) {
        this(getTree(registry, getDefaultLocale()));
    }

    /**
     * Constructs a new operation browser for operations from the specified tree.
     *
     * @param model The tree model built by one of {@link #getTree} methods.
     */
    private RegisteredOperationBrowser(final TreeModel model) {
        super(new BorderLayout());
        final JTree tree = new JTree(model);
        tree.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 0));
        add(new JScrollPane(tree), BorderLayout.CENTER);
        /*
         * Add labels (description and version number).
         */
        final Box labels = Box.createVerticalBox();
        labels.add(description);
        labels.add(version);
        labels.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 0));
        add(labels, BorderLayout.SOUTH);
        /*
         * Configure the operations tree.
         */
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(final TreeSelectionEvent event) {
                selected(event.getNewLeadSelectionPath());
            }
        });
        /*
         * Set icons (optional)
         */
        tree.setCellRenderer(new CellRenderer());
    }

    /**
     * Invoked when the user selected a new operation in the tree. This method find the
     * {@link OperationDescriptor} for the selected node and invokes {@link #selected}.
     *
     * @param path The selected tree path, or {@code null} if none.
     */
    private void selected(final TreePath path) {
        if (path != null) {
            for (int i=path.getPathCount(); --i>=0;) {
                final Object component = path.getPathComponent(i);
                Object candidate = component;
                if (candidate instanceof TreeNode) {
                    candidate = ((TreeNode) candidate).getUserObject();
                    /*
                     * Note: The missing 'getUserObject()' method is fixed
                     *       in Geotools TreeNode, not the Swing one...
                     */
                }
                if (candidate instanceof OperationDescriptor) {
                    int index = -1;
                    /*
                     * Fetch the parameter index. Note: the Swing TreeNode is suffisient
                     * for this task (no need for the fixed Geotools's TreeNode).
                     */
                    if (component instanceof javax.swing.tree.TreeNode) {
                        final javax.swing.tree.TreeNode node = (javax.swing.tree.TreeNode)component;
                        final Object leaf = path.getLastPathComponent();
                        for (index=node.getChildCount(); --index>=0;) {
                            final javax.swing.tree.TreeNode param = node.getChildAt(index);
                            if (param==leaf && !param.getAllowsChildren()) {
                                break;
                            }
                        }
                    }
                    selected((OperationDescriptor) candidate, index);
                    return;
                }
            }
        }
        selected(null, -1);
    }

    /**
     * Invoked when the user selected a new operation in the tree. The default implementation
     * display the operation or parameter description in the text area.
     *
     * @param operation The selected operation, or {@code null} if no operation is
     *        selected.
     * @param param Index of the selected parameter, or {@code -1} if no parameter
     *        is selected.
     */
    protected void selected(final OperationDescriptor operation, final int param) {
        String description = " ";
        String version     = " ";
        if (operation != null) {
            final String key;
            final Locale locale = getLocale();
            final ResourceBundle resources = operation.getResourceBundle(locale);
            if (param >= 0) {
                key = "arg"+param+"Desc";
            } else {
                key = "Description";
            }
            try {
                description = resources.getString(key);
                version     = Vocabulary.getResources(locale).getString(VocabularyKeys.VERSION_$1,
                              resources.getString("Version")) + ", " +
                              resources.getString("Vendor");
            } catch (MissingResourceException exception) {
                /*
                 * A description was missing for this operation or parameter. This is not a big
                 * deal; just left some label blank. Log the exception with a low level, since
                 * this warning is not really important.
                 */
                Logging.getLogger(RegisteredOperationBrowser.class).log(Level.FINER,
                                 exception.getLocalizedMessage(), exception);
            }
        }
        this.description.setText(description);
        this.version    .setText(version    );
    }

    /**
     * Returns a tree view of all operations registered in the default {@link JAI} instance.
     * Labels will be formatted in the Swing's {@linkplain #getDefaultLocale default locale}.
     *
     * @return All JAI operations as a tree.
     */
    public static TreeModel getTree() {
        return getTree(JAI.getDefaultInstance().getOperationRegistry(), getDefaultLocale());
    }

    /**
     * Returns a tree view of all operations registered in the given registry.
     *
     * @param  registry The registry (e.g. {@link JAI#getOperationRegistry()}).
     * @param  locale The locale (e.g. {@link Locale#getDefault()}).
     * @return All JAI operations as a tree.
     *
     * @see #getTree()
     * @see JAI#getDefaultInstance()
     * @see JAI#getOperationRegistry()
     * @see Locale#getDefault()
     */
    public static TreeModel getTree(final OperationRegistry registry, final Locale locale) {
        final Vocabulary resources = Vocabulary.getResources(locale);
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                                                resources.getString(VocabularyKeys.OPERATIONS));
        /*
         * Add registry modes ("rendered", "renderable", etc.),
         * and gets the operation descriptors for each mode.
         */
        final String[] modes = registry.getRegistryModes();
        Arrays.sort(modes);
        for (int i=0; i<modes.length; i++) {
            final String mode = modes[i];
            final DefaultMutableTreeNode modeNode = new DefaultMutableTreeNode(mode);
            @SuppressWarnings("unchecked")
            final List<RegistryElementDescriptor> descriptors = registry.getDescriptors(mode);
            Collections.sort(descriptors, new Comparator<RegistryElementDescriptor>() {
                public int compare(final RegistryElementDescriptor desc1,
                                   final RegistryElementDescriptor desc2)
                {
                    return desc1.getName().compareTo(desc2.getName());
                }
            });
            /*
             * Add the operations ("add", "convolve", etc.) and their parameters.
             */
            for (final Iterator it=descriptors.iterator(); it.hasNext();) {
                final RegistryElementDescriptor descriptor;
                final DefaultMutableTreeNode descriptorNode;
                final ParameterListDescriptor param;
                descriptor     = (RegistryElementDescriptor)it.next();
                descriptorNode = new NamedTreeNode(getName(descriptor, locale), descriptor);
                param          = descriptor.getParameterListDescriptor(mode);
                if (param != null) {
                    final String[] names = param.getParamNames();
                    if (names != null) {
                        // No sorting; the order is relevant
                        for (int j=0; j<names.length; j++) {
                            // Should not be NamedTreeNode, because the later is used for
                            // differentiating parameters and implementations (see below).
                            descriptorNode.add(new DefaultMutableTreeNode(names[j], false));
                        }
                    }
                }
                /*
                 * Add the implementing products and the factories, if any.
                 */
                final String operationName = descriptor.getName();
                final List/*<String>*/ products = registry.getOrderedProductList(mode, operationName);
                if (products != null) {
                    final DefaultMutableTreeNode productsNode;
                    productsNode = new DefaultMutableTreeNode(
                                   resources.getString(VocabularyKeys.IMPLEMENTATIONS));
                    for (final Iterator itp=products.iterator(); itp.hasNext();) {
                        final String product = (String) itp.next();
                        final DefaultMutableTreeNode productNode;
                        productNode = new DefaultMutableTreeNode(product);

                        final List factories;
                        factories = registry.getOrderedFactoryList(mode, operationName, product);
                        if (factories != null) {
                            for (final Iterator itf=factories.iterator(); itf.hasNext();) {
                                final Object factory = itf.next();
                                productNode.add(new NamedTreeNode(
                                        Classes.getShortClassName(factory), factory, false));
                                // The node class (NamedTreeNode) should be different from the
                                // node for parameters (see above), in order to differentiate
                                // those leafs in the cell renderer.
                            }
                        }
                        productsNode.add(productNode);
                    }
                    descriptorNode.add(productsNode);
                }
                modeNode.add(descriptorNode);
            }
            root.add(modeNode);
        }
        return new DefaultTreeModel(root, true);
    }

    /**
     * Returns the localized name for the given descriptor. The name will be fecth from the
     * "{@link OperationDescriptor#getResourceBundle LocalName}" resource, if available.
     * Otherwise, the {@linkplain RegistryElementDescriptor#getName non-localized name} is returned.
     */
    private static String getName(final RegistryElementDescriptor descriptor, final Locale locale) {
        if (descriptor instanceof OperationDescriptor) {
            ResourceBundle resources = ((OperationDescriptor)descriptor).getResourceBundle(locale);
            if (resources != null) try {
                return resources.getString("LocalName");
            } catch (MissingResourceException exception) {
                // No localized name. Fallback on the default (non-localized) descriptor name.
                // No warning to report here, this exception is really not a problem.
            }
        }
        return descriptor.getName();
    }

    /**
     * The tree cell renderer, which select icons according the selected object type.
     */
    private static final class CellRenderer extends DefaultTreeCellRenderer {
        /** The icon for folder. */
        private final Icon open, closed;

        /** The icon for an operation, or {@code null} if none. */
        private static Icon operation;

        /** The icon for parameters, or {@code null} if none. */
        private static Icon parameter;

        /** The icon for implementations, or {@code null} if none. */
        private static Icon implementation;

        /**
         * Creates a cell renderer.
         */
        private CellRenderer() {
            open   = getDefaultOpenIcon();
            closed = getDefaultClosedIcon();
            if (operation == null) {
                final IconFactory icons = IconFactory.DEFAULT;
                operation      = icons.getIcon("toolbarButtonGraphics/general/Information16.gif");
                parameter      = icons.getIcon("toolbarButtonGraphics/general/Preferences16.gif");
                implementation = icons.getIcon("toolbarButtonGraphics/general/About16.gif");
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
            final boolean isOp;
            isOp = ((TreeNode) value).getUserObject() instanceof RegistryElementDescriptor;
            Icon icon = isOp ? operation : open;
            if (icon != null) {
                setOpenIcon(icon);
            }
            icon = isOp ? operation : closed;
            if (icon != null) {
                setClosedIcon(icon);
            }
            icon = (value instanceof NamedTreeNode ? implementation : parameter);
            if (icon != null) {
                setLeafIcon(icon);
            }
            return super.getTreeCellRendererComponent(tree, value, selected, expanded,
                                                      leaf, row, hasFocus);
        }
    }

    /**
     * Display the operation browser from the command line. This method is usefull for checking
     * the widget appearance and the list of registered {@link JAI} operations. If this method
     * is launch with the {@code -print} argument, then the tree of operations will be sent
     * to standard output.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        if (arguments.getFlag("-print")) {
            arguments.out.println(Trees.toString(getTree()));
        } else {
            final JFrame frame = new JFrame(Vocabulary.format(VocabularyKeys.OPERATIONS));
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(new RegisteredOperationBrowser());
            frame.pack();
            frame.setVisible(true);
        }
    }
}
