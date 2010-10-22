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


/**
 * Defines the requirements for a tree node object that can change. It may changes by adding or
 * removing child nodes, or by changing the contents of a user object stored in the node. This
 * interface inherits the {@link #getUserObject getUserObject()} method from Geotools's
 * {@link TreeNode}. This is needed because the Swing's {@link javax.swing.tree.MutableTreeNode}
 * interface defines a {@link #setUserObject(Object) setUserObject(Object)} method but doesn't
 * define or inherit any {@code getUserObject()}.
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/tree/MutableTreeNode.java $
 * @version $Id: MutableTreeNode.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public interface MutableTreeNode extends javax.swing.tree.MutableTreeNode, TreeNode {
}
