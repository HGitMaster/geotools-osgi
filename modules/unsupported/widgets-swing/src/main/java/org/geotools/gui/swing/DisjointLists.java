/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.awt.Font;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.AbstractListModel;
import java.awt.IllegalComponentStateException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.Locale;
import static java.awt.GridBagConstraints.*;

import org.geotools.resources.XArray;
import org.geotools.resources.Classes;
import org.geotools.resources.SwingUtilities;


/**
 * A widget showing selected and unselected items in two disjoint list. The list on the left
 * side shows items available for selection. The list on the right side shows items already
 * selected. User can move items from one list to the other using buttons in the middle.
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/DisjointLists.java $
 * @version $Id: DisjointLists.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
@SuppressWarnings("serial")
public class DisjointLists extends JPanel {
    /**
     * The list model. Each {@link DisjointLists} object will use two instances
     * of this class.  Both instances share the same list of elements, but have
     * their own list of index of visibles elements.
     */
    private static final class Model extends AbstractListModel {
        /**
         * The list of elements shared by both lists. Not all elements in this list will be
         * displayed. The index of elements to shown are enumerated in the {@link #visibles}
         * array.
         * <p>
         * Note: this list is read by {@link DisjointLists#selectElements}. The content
         *       of this list should never be modified from any method outside this class.
         */
        final List<Object> choices;

        /**
         * The index of valids elements in the {@link #choice} list. This array will growth
         * as needed. Elements in this array should always be in strictly increasing order.
         */
        private int[] visibles = new int[12];

        /**
         * The number of valid elements in the {@link #visibles} array.
         */
        private int size;

        /**
         * Constructs a model for the specified list of elements.
         */
        public Model(final List<Object> choices) {
            this.choices = choices;
        }

        /**
         * Returns {@code true} if all elements in the {@link #visible} array
         * are in strictly increasing order. This is used for assertions.
         */
        private boolean isSorted() {
            for (int i=1; i<size; i++) {
                if (visibles[i] <= visibles[i-1]) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Searchs the insertion point. We should use Arrays.binarySearch(...), but
         * unfortunatly J2SE 1.4 do not provides an API for searching in a subarray.
         */
        private static int search(final int[] array, int lower, final int upper, final int value) {
            while (lower<upper && value>array[lower]) {
                lower++;
            }
            return lower;
        }

        /**
         * Returns the number of valid elements.
         */
        public int getSize() {
            assert size>=0 && size<=choices.size() : size;
            return size;
        }

        /**
         * Returns all elements in this list.
         */
        public Collection getElements() {
            final Object[] list = new Object[getSize()];
            for (int i=0; i<list.length; i++) {
                list[i] = ListElement.unwrap(getElementAt(i));
            }
            return Arrays.asList(list);
        }

        /**
         * Returns the element at the specified index.
         */
        public Object getElementAt(final int index) {
            assert index>=0 && index<size : index;
            return choices.get(visibles[index]);
        }

        /**
         * Makes sure that the {@link #visibles} array has the specified capacity.
         */
        private void ensureCapacity(final int capacity) {
            if (visibles.length < capacity) {
                visibles = XArray.resize(visibles, Math.max(size*2, capacity));
            }
        }

        /**
         * Removes a range of visible elements. The {@code lower} and {@code upper}
         * indices are index (not values) in the {@link #visibles} array.
         */
        private void hide(final int lower, final int upper) {
            if (lower != upper) {
                System.arraycopy(visibles, upper, visibles, lower, size-upper);
                size -= (upper-lower);
                fireIntervalRemoved(this, lower, upper-1);
            }
            assert isSorted();
        }

        /**
         * Moves elements in the specified range from the specified model to this model.
         *
         * @param source The source model.
         * @param lower  Lower index (inclusive) in the source model.
         * @param upper  Upper index (exclusive) in the source model.
         */
        public void move(final Model source, final int lower, final int upper) {
            assert lower>=0 && upper<=source.size;
            ensureCapacity(size + (upper-lower));
            int insertAt = 0;
            int subUpper = lower;
            while (subUpper < upper) {
                final int subLower = subUpper;
                assert isSorted();
                insertAt = search(visibles, insertAt, size, source.visibles[subLower]);
                if (insertAt == size) {
                    subUpper = upper;
                } else {
                    subUpper = search(source.visibles, subLower, upper, visibles[insertAt]);
                }
                final int length = subUpper - subLower;
                System.arraycopy(visibles, insertAt, visibles, insertAt+length, size-insertAt);
                System.arraycopy(source.visibles, subLower, visibles, insertAt, length);
                size += length;
                assert isSorted();
                fireIntervalAdded(this, insertAt, insertAt+length-1);
            }
            source.hide(lower, upper);
        }

        /**
         * Moves elements at the specified indices from the specified model to this model.
         * Note: the indice array will be overwritten.
         *
         * @param source  The source model.
         * @param indices Indices of elements in the source model to move.
         */
        public void move(final Model source, final int[] indices) {
            Arrays.sort(indices);
            for (int i=0; i<indices.length;) {
                int lower = indices[i];
                int upper = lower+1;
                while (++i<indices.length && indices[i]==upper) {
                    // Collapses consecutive indices in a single move operation.
                    upper++;
                }
                move(source, lower, upper);
                final int length = (upper-lower);
                for (int j=i; j<indices.length; j++) {
                    // Adjusts the remaining indices. Since we just moved previous
                    // elements, the indices of remaining elements are shifted.
                    indices[j] -= length;
                }
            }
        }

        /**
         * Adds all elements from the specified collection.
         */
        public void addAll(final Collection<?> items) {
            if (!items.isEmpty()) {
                choices.addAll(items);
                final int length = items.size();
                ensureCapacity(size + length);
                final int max = choices.size();
                for (int i=max-length; i<max; i++) {
                    visibles[size++] = i;
                }
                assert isSorted();
                fireIntervalAdded(this, size-length, size-1);
            }
        }

        /**
         * Removes all elements from this model.
         */
        public void clear() {
            choices.clear();
            if (size != 0) {
                final int oldSize = size;
                size = 0;
                fireIntervalRemoved(this, 0, oldSize-1);
            }
        }
    }

    /**
     * Action invoked when the user pressed a button. This action
     * invokes {@link Model#move} with selected indices.
     */
    private static final class Action implements ActionListener {
        /**
         * The source and target lists.
         */
        private final JList source, target;

        /**
         * {@code true} if we should move all items on action.
         */
        private final boolean all;

        /**
         * Constructs a new "move" action.
         */
        public Action(final JList source, final JList target, final boolean all) {
            this.source = source;
            this.target = target;
            this.all    = all;
        }

        /**
         * Invoked when the user pressed a "move" button.
         */
        public void actionPerformed(final ActionEvent event) {
            final Model source = (Model)this.source.getModel();
            final Model target = (Model)this.target.getModel();
            if (all) {
                target.move(source, 0, source.getSize());
                return;
            }
            final int[] indices = this.source.getSelectedIndices();
            target.move(source, indices);
        }
    }

    /**
     * The list on the left side. This is the list that contains
     * the element selectable by the user.
     */
    private final JList left;

    /**
     * The list on the right side. This list is initially empty.
     */
    private final JList right;

    /**
     * {@code true} if elements should be automatically sorted.
     */
    private boolean autoSort = true;

    /**
     * Construct a new list.
     */
    public DisjointLists() {
        super(new GridBagLayout());
        /*
         * Setup lists
         */
        final List<Object> choices = new ArrayList<Object>();
        left  = new JList(new Model(choices));
        right = new JList(new Model(choices));
        final JScrollPane  leftPane = new JScrollPane( left);
        final JScrollPane rightPane = new JScrollPane(right);
        final Dimension size = new Dimension(160, 200);
        leftPane .setPreferredSize(size);
        rightPane.setPreferredSize(size);
        /*
         * Setup buttons
         */
        final JButton add       = getButton("StepForward", ">",  "Add selected elements");
        final JButton remove    = getButton("StepBack",    "<",  "Remove selected elements");
        final JButton addAll    = getButton("FastForward", ">>", "Add all");
        final JButton removeAll = getButton("Rewind",      "<<", "Remove all");
        add      .addActionListener(new Action(left, right, false));
        remove   .addActionListener(new Action(right, left, false));
        addAll   .addActionListener(new Action(left, right,  true));
        removeAll.addActionListener(new Action(right, left,  true));
        /*
         * Build UI
         */
        final GridBagConstraints c = new GridBagConstraints();
        c.gridy=0; c.gridwidth=1; c.gridheight=4; c.weightx=c.weighty=1; c.fill=BOTH;
        c.gridx=0; add( leftPane,  c);
        c.gridx=2; add(rightPane, c);

        c.insets.left = c.insets.right = 9;
        c.gridx=1; c.gridheight=1; c.weightx=0; c.fill=HORIZONTAL;
        c.gridy=0; c.anchor=SOUTH;    add(add,       c);
        c.gridy=3; c.anchor=NORTH;    add(removeAll, c);
        c.gridy=2; c.weighty=0;       add(addAll,    c);
        c.gridy=1; c.insets.bottom=9; add(remove,    c);
    }

    /**
     * Returns a button.
     *
     * @param loader The class loader for loading the button's image.
     * @param image  The image name to load in the "media" category from the
     *               <A HREF="http://developer.java.sun.com/developer/techDocs/hi/repository/">Swing
     *               graphics repository</A>.
     * @param fallback The fallback to use if the image is not found.
     * @param description a brief description to use for tooltips.
     * @return The button.
     */
    private static JButton getButton(String image, final String fallback, final String description) {
        image = "toolbarButtonGraphics/media/" + image + "16.gif";
        return IconFactory.DEFAULT.getButton(image, description, fallback);
    }

    /**
     * Returns {@code true} if elements are automatically sorted when added to this list.
     * The default value is {@code true}.
     *
     * @since 2.2
     */
    public boolean isAutoSortEnabled() {
        return autoSort;
    }

    /**
     * Sets to {@code true} if elements should be automatically sorted when added to this list.
     *
     * @since 2.2
     */
    public void setAutoSortEnabled(final boolean autoSort) {
        if (autoSort != this.autoSort) {
            this.autoSort = autoSort;
            if (autoSort) {
                final List<Object> elements = new ArrayList<Object>(((Model) left.getModel()).choices);
                clear();
                addElements(elements);
            }
            firePropertyChange("autoSort", !autoSort, autoSort);
        }
    }

    /**
     * Removes all elements from this list.
     *
     * @since 2.2
     */
    public void clear() {
        ((Model)left .getModel()).clear();
        ((Model)right.getModel()).clear();
    }

    /**
     * Add all elements from the specified collection into the list on the left side.
     * Elements are sorted if {@link #isAutoSortEnabled} returns {@code true}.
     *
     * @param items Items to add.
     */
    public void addElements(final Collection items) {
        addElements(items.toArray());
    }

    /**
     * Add all elements from the specified array into the list on the left side.
     * Elements are sorted if {@link #isAutoSortEnabled} returns {@code true}.
     *
     * @param items Items to add.
     *
     * @since 2.2
     */
    public void addElements(final Object[] items) {
        Locale locale;
        try {
            locale = getLocale();
        } catch (IllegalComponentStateException e) {
            locale = getDefaultLocale();
        }
        final List<Object> list = new ArrayList<Object>(items.length);
        for (int i=0; i<items.length; i++) {
            Object candidate = items[i];
            if (!(candidate instanceof String)) {
                candidate = new ListElement(candidate, locale);
            }
            list.add(candidate);
        }
        final Model left  = (Model) this.left .getModel();
        final Model right = (Model) this.right.getModel();
        if (autoSort) {
            list.addAll(left.choices);
            Collections.sort((List) list);
            left .clear();
            right.clear();
        }
        left.addAll(list);
    }

    /**
     * Returns all elements with the specified selection state. If {@code selected} is {@code true},
     * then this method returns the selected elements on the right side. If {@code selected} is
     * {@code false}, then this method returns the unselected elements on the left side.
     *
     * @since 2.3
     */
    public Collection getElements(final boolean selected) {
        return ((Model) (selected ? right : left).getModel()).getElements();
    }

    /**
     * Add the specified elements to the selection list (the one to appears on the right side). If
     * an element specified in the {@code selected} collection has not been previously {@linkplain
     * #addElements(Collection) added}, it will be ignored.
     *
     * @since 2.3
     */
    public void selectElements(final Collection selected) {
        final Model source = (Model) left .getModel();
        final Model target = (Model) right.getModel();
        int[] indices = new int[Math.min(selected.size(), source.choices.size())];
        int indice=0, count=0;
        for (final Iterator it=source.choices.iterator(); it.hasNext(); indice++) {
            if (selected.contains(ListElement.unwrap(it.next()))) {
                indices[count++] = indice;
            }
        }
        indices = XArray.resize(indices, count);
        target.move(source, indices);
    }

    /**
     * Set the font for both lists on the left and right side.
     */
    @Override
    public void setFont(final Font font) {
        // Note: 'left' and 'right' may be null during JComponent initialisation.
        if (left  != null) left .setFont(font);
        if (right != null) right.setFont(font);
        super.setFont(font);
    }

    /**
     * Display this component in a dialog box and wait for the user to press "Ok".
     * This method can be invoked from any thread.
     *
     * @param  owner The owner (may be null).
     * @param  title The title to write in the window bar.
     * @return {@code true} if the user pressed "okay", or {@code false} otherwise.
     *
     * @since 2.2
     */
    public boolean showDialog(final Component owner, final String title) {
        return SwingUtilities.showOptionDialog(owner, this, title);
    }

    /**
     * Show the dialog box. This method is provided only as an easy
     * way to test the dialog appearance from the command line.
     *
     * @since 2.2
     */
    public static void main(final String[] args) {
        final DisjointLists list = new DisjointLists();
        list.addElements(Locale.getAvailableLocales());
        list.showDialog(null, Classes.getShortClassName(list));
        System.out.println(list.getElements(true));
    }
}
