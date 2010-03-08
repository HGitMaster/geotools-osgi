/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.swing.control;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JTextField;

/**
 * Abstract base class for text field controls that work with a simple
 * value such as {@code JIntegerField}. Provides methods to add and
 * remove listeners for value changes offering a simpler API than
 * messing about with Swing {@code KeyListener} and {@code DocumentListener}
 * classes.
 *
 * @author Michael Bedward
 * @since 2.6.1
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/swing/src/main/java/org/geotools/swing/control/JValueField.java $
 * @version $Id: JValueField.java 34290 2009-10-30 12:36:40Z mbedward $
 */
public abstract class JValueField extends JTextField {

    private final Set<ValueChangedListener> listeners;

    /**
     * Default constructor
     */
    public JValueField() {
        listeners = new HashSet<ValueChangedListener>();
    }

    /**
     * Register a new listener. If the listener is already registered
     * this method does nothing.
     *
     * @param listener the listener to register
     */
    public void addValueChangedListener( ValueChangedListener listener ) {
        listeners.add(listener);
    }

    /**
     * Remove the given listener. If the listener is not registered
     * this method does nothing.
     *
     * @param listener the listener to remove
     */
    public void removeValueChangedListener( ValueChangedListener listener ) {
        listeners.remove(listener);
    }

    /**
     * Notify listeners of a value change
     *
     * @param ev the event with details of the value change
     */
    protected void fireValueChangedEvent( ValueChangedEvent ev ) {
        for (ValueChangedListener listener : listeners) {
            listener.onValueChanged(ev);
        }
    }

}
