/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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

// JUnit dependencies
import javax.swing.tree.TreeModel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.metadata.AbstractMetadata;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Tests {@link NamedTreeNode}, especially the part that instantiate them using Java reflection.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/test/java/org/geotools/gui/swing/tree/NamedTreeNodeTest.java $
 * @version $Id: NamedTreeNodeTest.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (Geomatys)
 */
public class NamedTreeNodeTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(NamedTreeNodeTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public NamedTreeNodeTest(final String name) {
        super(name);
    }

    /**
     * Verifies that {@link AbstractMetadata#asTree} uses instances of {@link NamedTreeNode}.
     * Because those instances were created by Java reflection, the compiler will not detect
     * broken link, so we need to check with this test case.
     */
    public void testCitations() {
        final AbstractMetadata citation = (AbstractMetadata) Citations.EPSG;
        final TreeModel tree = citation.asTree();
        final Object root = tree.getRoot();
        assertTrue(root instanceof javax.swing.tree.TreeNode);
        assertTrue(root instanceof NamedTreeNode);
        final TreeNode node = (TreeNode) root;
        assertEquals("Citation", node.toString());
        assertSame(citation, node.getUserObject());
    }
}
