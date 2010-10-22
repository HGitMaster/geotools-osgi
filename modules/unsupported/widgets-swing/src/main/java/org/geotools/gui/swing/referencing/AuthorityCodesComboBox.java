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

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.resources.Classes;
import org.geotools.resources.Arguments;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.FallbackAuthorityFactory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.gui.swing.IconFactory;


/**
 * A combox box for selecting a coordinate reference system from a list. This component also
 * provides a search button (for filtering the CRS name that contain the specified keywords)
 * and a info button displaying the CRS {@linkplain PropertiesSheet properties sheet}.
 *
 * @since 2.3
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/referencing/AuthorityCodesComboBox.java $
 * @version $Id: AuthorityCodesComboBox.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
@SuppressWarnings("serial")
public class AuthorityCodesComboBox extends JComponent {
    /**
     * The authority factory responsible for creating objects from a list of codes.
     */
    private final AuthorityFactory factory;

    /**
     * The list of authority codes, as a combo box model.
     */
    private final CodeList codeList;

    /**
     * The type of CRS object to includes in the list.
     */
    private Class type;

    /**
     * The list of CRS objects.
     */
    private final JComboBox list;

    /**
     * The text field for searching item.
     */
    private final JTextField search;

    /**
     * The {@link #search} or {@link #list} field.
     */
    private final JPanel searchOrList;

    /**
     * The card layout showing either {@link #list} or {@link #search}.
     */
    private final CardLayout cards;

    /**
     * The button to press for showing properties.
     */
    private final JButton showProperties;

    /**
     * Info about the currently selected item.
     */
    private PropertiesSheet properties;

    /**
     * The window that contains {@link #properties}.
     */
    private Component propertiesWindow;

    /**
     * Creates a CRS chooser backed by the EPSG authority factory.
     *
     * @throws FactoryRegistryException if no EPSG authority factory has been found.
     * @throws FactoryException if the factory can't provide CRS codes.
     */
    public AuthorityCodesComboBox() throws FactoryRegistryException, FactoryException {
        this("EPSG");
    }

    /**
     * Creates a CRS chooser backed by the specified authority factory.
     *
     * @param  authority The authority identifier (e.g. {@code "EPSG"}).
     * @throws FactoryRegistryException if no authority factory has been found.
     * @throws FactoryException if the factory can't provide CRS codes.
     *
     * @since 2.4
     */
    public AuthorityCodesComboBox(final String authority)
            throws FactoryRegistryException, FactoryException
    {
        this(FallbackAuthorityFactory.create(CRSAuthorityFactory.class,
             filter(ReferencingFactoryFinder.getCRSAuthorityFactories(null), authority)));
    }

    /**
     * Returns a collection containing only the factories of the specified authority.
     */
    private static Collection<CRSAuthorityFactory> filter(
            final Collection<? extends CRSAuthorityFactory> factories, final String authority)
    {
        final List<CRSAuthorityFactory> filtered = new ArrayList<CRSAuthorityFactory>();
        for (final CRSAuthorityFactory factory : factories) {
            if (Citations.identifierMatches(factory.getAuthority(), authority)) {
                filtered.add(factory);
            }
        }
        return filtered;
    }

    /**
     * Creates a CRS chooser backed by the specified authority factory.
     *
     * @param  factory The authority factory responsible for creating objects from a list of codes.
     * @throws FactoryException if the factory can't provide CRS codes.
     */
    public AuthorityCodesComboBox(final AuthorityFactory factory) throws FactoryException {
        this(factory, CoordinateReferenceSystem.class);
    }

    /**
     * Creates a CRS chooser backed by the specified authority factory.
     *
     * @param  factory The authority factory responsible for creating objects from a list of codes.
     * @param  type The type of CRS object to includes in the list.
     * @throws FactoryException if the factory can't provide CRS codes.
     */
    public AuthorityCodesComboBox(final AuthorityFactory factory, final Class type) throws FactoryException {
        this.factory = factory;
        this.type    = type;
        final Locale locale = SwingUtilities.getLocale(this);
        final Vocabulary resources = Vocabulary.getResources(locale);

        setLayout(new BorderLayout());
        cards        = new CardLayout();
        searchOrList = new JPanel(cards);
        codeList     = new CodeList(factory, type);
        list         = new JComboBox(codeList);
        list.setPrototypeDisplayValue("Unknown datum based upon the Average Terrestrial System 1977 ellipsoid");
        search       = new JTextField();
        search.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                search(false);
            }
        });
        searchOrList.add(list,   "List");
        searchOrList.add(search, "Search");
        add(searchOrList, BorderLayout.CENTER);
        /*
         * Adds the "Info" button.
         */
        JButton button;
        final Dimension size = new Dimension(24, 20);
        final IconFactory icons = IconFactory.DEFAULT;
        String label = resources.getString(VocabularyKeys.INFORMATIONS);
        button = icons.getButton("toolbarButtonGraphics/general/Information16.gif", label, label);
        button.setFocusable(false);
        button.setPreferredSize(size);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                showProperties();
            }
        });
        add(button, BorderLayout.WEST);
        showProperties = button;
        /*
         * Adds the "Search" button.
         */
        label = resources.getString(VocabularyKeys.SEARCH);
        button = icons.getButton("toolbarButtonGraphics/general/Find16.gif", label, label);
        button.setFocusable(false);
        button.setPreferredSize(size);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                search(true);
            }
        });
        add(button, BorderLayout.EAST);
    }

    /**
     * Returns the authority name. Useful for providing a window title for example.
     */
    public String getAuthority() {
        final Locale locale = SwingUtilities.getLocale(this);
        return factory.getAuthority().getTitle().toString(locale);
    }

    /**
     * Returns the code for the selected object, or {@code null} if none.
     */
    public String getSelectedCode() {
        final Code code = (Code) list.getModel().getSelectedItem();
        return (code != null) ? code.code : null;
    }

    /**
     * Returns the selected object, usually as a {@link CoordinateReferenceSystem}.
     *
     * @throws FactoryException if the factory can't create the selected object.
     */
    public IdentifiedObject getSelectedItem() throws FactoryException {
        final String code = getSelectedCode();
        return (code != null) ? factory.createObject(code) : null;
    }

    /**
     * Display information about the currently selected item in a separated window.
     * The default implementation show the <cite>Well Know Text</cite>.
     */
    public void showProperties() {
        if (properties == null) {
            properties = new PropertiesSheet();
        }
        IdentifiedObject item;
        try {
            item = getSelectedItem();
        } catch (FactoryException e) {
            String message = e.getLocalizedMessage();
            if (message == null) {
                message = Classes.getShortClassName(e);
            }
            properties.setErrorMessage(message);
            return;
        }
        final String title = item.getName().getCode();
        if (propertiesWindow == null) {
            propertiesWindow = SwingUtilities.toFrame(this, properties, title, null);
            if (propertiesWindow instanceof JFrame) {
                ((JFrame) propertiesWindow).setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            } else if (propertiesWindow instanceof JInternalFrame) {
                ((JInternalFrame) propertiesWindow).setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
            }
            propertiesWindow.setSize(600, 500);
        } else {
            SwingUtilities.setTitle(propertiesWindow, title);
        }
        properties.setIdentifiedObject(item);
        propertiesWindow.setVisible(true);
    }

    /**
     * Enable or disable the search field.
     */
    private void search(final boolean enable) {
        final JComponent component;
        final String name;
        if (enable) {
            component = search;
            name = "Search";
        } else {
            component = list;
            name = "List";
            filter(search.getText());
        }
        showProperties.setEnabled(!enable);
        cards.show(searchOrList, name);
        component.requestFocus();
    }

    /**
     * Display only the CRS name that contains the specified keywords. The {@code keywords}
     * argument is a space-separated list, usually provided by the user after he pressed the
     * "Search" button.
     *
     * @param keywords space-separated list of keywords to look for.
     */
    public void filter(String keywords) {
        ComboBoxModel model = codeList;
        if (keywords != null) {
            final Locale locale = SwingUtilities.getLocale(this);
            keywords = keywords.toLowerCase(locale).trim();
            final String[] tokens = keywords.split("\\s+");
            if (tokens.length != 0) {
                final DefaultComboBoxModel filtered;
                model = filtered = new DefaultComboBoxModel();
                final int size = codeList.getSize();
        scan:   for (int i=0; i<size; i++) {
                    final Code code = (Code) codeList.getElementAt(i);
                    final String name = code.toString().toLowerCase(locale);
                    for (int j=0; j<tokens.length; j++) {
                        if (name.indexOf(tokens[j]) < 0) {
                            continue scan;
                        }
                    }
                    filtered.addElement(code);
                }
            }
        }
        list.setModel(model);
    }

    /**
     * Display the chooser. This method is provided mainly for testing purpose.
     * <p>
     * If the {@code -prototype} argument is provided on the command line, then this method
     * display the longuest CRS name found in the database. This is useful for setting the
     * combo box {@linkplain JComboBox#setPrototypeDisplayValue prototype display value}.
     *
     * @throws FactoryRegistryException if no EPSG authority factory has been found.
     * @throws FactoryException if the factory can't provide CRS codes.
     */
    public static void main(String[] args) throws FactoryRegistryException, FactoryException {
        final Arguments arguments = new Arguments(args);
        final boolean prototype = arguments.getFlag("-prototype");
        args = arguments.getRemainingArguments(0);
        final AuthorityCodesComboBox chooser = new AuthorityCodesComboBox();
        if (prototype) {
            System.out.println(((CodeList) chooser.list.getModel()).getPrototypeItem());
        }
        final JFrame frame = new JFrame(chooser.getAuthority());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chooser, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }
}
