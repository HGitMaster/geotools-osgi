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
package org.geotools.gui.swing.tree;

import java.util.List;
import java.util.ArrayList;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import org.w3c.dom.Node;

import org.geotools.resources.XArray;
import org.geotools.resources.Arguments;
import org.geotools.resources.OptionalDependencies;


/**
 * Convenience static methods for trees operations.
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/tree/Trees.java $
 * @version $Id: Trees.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public final class Trees {
    /**
     * Interdit la création d'objets de cette classe.
     */
    private Trees() {
    }

    /**
     * Returns the path to the specified
     * {@linkplain org.geotools.gui.swing.tree.TreeNode#getUserObject user object}. For each tree
     * node which are actually instance of Geotools {@link org.geotools.gui.swing.tree.TreeNode},
     * this method compares the specified {@code value} against the user object returned by the
     * {@link org.geotools.gui.swing.tree.TreeNode#getUserObject} method.
     *
     * @param  model The tree model to inspect.
     * @param  value User object to compare to
     *         {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @return The paths to the specified value, or an empty array if none.
     */
    public static TreePath[] getPathsToUserObject(final TreeModel model, final Object value) {
        final List<TreePath> paths = new ArrayList<TreePath>(8);
        final Object[] path = new Object[8];
        path[0] = model.getRoot();
        getPathsToUserObject(model, value, path, 1, paths);
        return paths.toArray(new TreePath[paths.size()]);
    }

    /**
     * Implémentation de la recherche des chemins. Cette
     * méthode s'appele elle-même d'une façon récursive.
     *
     * @param  model  Modèle dans lequel rechercher le chemin.
     * @param  value  Objet à rechercher dans
     *                {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @param  path   Chemin parcouru jusqu'à maintenant.
     * @param  length Longueur valide de {@code path}.
     * @param  list   Liste dans laquelle ajouter les {@link TreePath} trouvés.
     * @return {@code path}, ou un nouveau tableau s'il a fallu l'agrandir.
     */
    private static Object[] getPathsToUserObject(final TreeModel model, final Object value,
            Object[] path, final int length, final List<TreePath> list)
    {
        final Object parent = path[length-1];
        if (parent instanceof org.geotools.gui.swing.tree.TreeNode) {
            final Object nodeValue = ((org.geotools.gui.swing.tree.TreeNode)parent).getUserObject();
            if (nodeValue==value || (value!=null && value.equals(nodeValue))) {
                list.add(new TreePath(XArray.resize(path, length)));
            }
        }
        final int count = model.getChildCount(parent);
        for (int i=0; i<count; i++) {
            if (length >= path.length) {
                path = XArray.resize(path, length << 1);
            }
            path[length] = model.getChild(parent, i);
            path = getPathsToUserObject(model, value, path, length+1, list);
        }
        return path;
    }

    /**
     * Creates a Swing root tree node from a XML root tree node. Together with
     * {@link #toString(TreeNode)}, this method provides a convenient way to print
     * the content of a XML document for debugging purpose.
     */
    public static TreeNode xmlToSwing(final Node node) {
        return OptionalDependencies.xmlToSwing(node);
    }

    /**
     * Returns a graphical representation of the specified tree model. This representation can
     * be printed to the {@linkplain System#out standard output stream} (for example) if it uses
     * a monospaced font and supports unicode.
     *
     * @param  tree The tree to format.
     * @return A string representation of the tree, or {@code null} if it doesn't contain any node.
     */
    public static String toString(final TreeModel tree) {
        return OptionalDependencies.toString(tree);
    }

    /**
     * Returns a graphical representation of the specified tree. This representation can be
     * printed to the {@linkplain System#out standard output stream} (for example) if it uses
     * a monospaced font and supports unicode.
     *
     * @param  node The root node of the tree to format.
     * @return A string representation of the tree, or {@code null} if it doesn't contain any node.
     */
    public static String toString(final TreeNode node) {
        return OptionalDependencies.toString(node);
    }

    /**
     * Prints the specified tree model to the {@linkplain System#out standard output stream}.
     * This method is mostly a convenience for debugging purpose.
     *
     * @since 2.4
     */
    public static void print(final TreeModel tree) {
        Arguments.getPrintWriter(System.out).println(toString(tree));
    }

    /**
     * Prints the specified tree to the {@linkplain System#out standard output stream}.
     * This method is mostly a convenience for debugging purpose.
     *
     * @since 2.4
     */
    public static void print(final TreeNode node) {
        Arguments.getPrintWriter(System.out).println(toString(node));
    }
}
