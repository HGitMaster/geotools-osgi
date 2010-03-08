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
package org.geotools.gui.swing.referencing;

// Time
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;

// Geometry and coordinates
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

// User interface (Swing)
import java.awt.Insets;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.BorderFactory;
import javax.swing.AbstractButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JFormattedTextField;
import javax.swing.text.InternationalFormatter;

// Events
import java.awt.EventQueue;
import java.util.EventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// Parsing and formating
import java.text.Format;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;

// Miscellaneous
import java.util.Arrays;
import java.util.Locale;

// Geotools dependencies
import org.geotools.measure.Angle;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.measure.AngleFormat;

// Resources
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.geometry.XDimension2D;


/**
 * A pane of controls designed to allow a user to select spatio-temporal coordinates.
 * Current implementation uses geographic coordinates (longitudes/latitudes) and dates
 * according some locale calendar. Future version may allow the use of user-specified
 * coordinate system. Latitudes are constrained in the range 90°S to 90°N inclusive.
 * Longitudes are constrained in the range 180°W to 180°E inclusive. By default, dates
 * are constrained in the range January 1st, 1970 up to the date at the time the widget
 * was created.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/CoordinateChooser.png"></p>
 * <p>&nbsp;</p>
 *
 * @since 2.3
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/referencing/CoordinateChooser.java $
 * @version $Id: CoordinateChooser.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public class CoordinateChooser extends JPanel {
    /**
     * An enumeration constant for showing or hidding the geographic area selector.
     * Used as argument for {@link #isSelectorVisible} and {@link #setSelectorVisible}.
     *
     * @see #TIME_RANGE
     * @see #RESOLUTION
     * @see #isSelectorVisible
     * @see #setSelectorVisible
     * @see #addChangeListener
     * @see #removeChangeListener
     */
    public static final int GEOGRAPHIC_AREA = 1;

    /**
     * An enumeration constant for showing or hidding the time range selector.
     * Used as argument for {@link #isSelectorVisible} and {@link #setSelectorVisible}.
     *
     * @see #GEOGRAPHIC_AREA
     * @see #RESOLUTION
     * @see #isSelectorVisible
     * @see #setSelectorVisible
     * @see #addChangeListener
     * @see #removeChangeListener
     */
    public static final int TIME_RANGE = 2;

    /**
     * An enumeration constant for showing or hidding the resolution selector.
     * Used as argument for {@link #isSelectorVisible} and {@link #setSelectorVisible}.
     *
     * @see #GEOGRAPHIC_AREA
     * @see #TIME_RANGE
     * @see #isSelectorVisible
     * @see #setSelectorVisible
     * @see #addChangeListener
     * @see #removeChangeListener
     */
    public static final int RESOLUTION = 4;

    /**
     * The three mean panels in this dialog box:
     * geographic area, time and preferred resolution.
     */
    private final JComponent areaPanel, timePanel, resoPanel;

    /**
     * Liste de choix dans laquelle l'utilisateur
     * choisira le fuseau horaire de ses dates.
     */
    private final JComboBox timezone;

    /**
     * Dates de début et de fin de la plage de temps demandée par l'utilisateur.
     * Ces dates sont gérées par un modèle {@link SpinnerDateModel}.
     */
    private final JSpinner tmin, tmax;

    /**
     * Longitudes et latitudes minimales et maximales demandées par l'utilisateur.
     * Ces coordonnées sont gérées par un modèle {@link SpinnerNumberModel}.
     */
    private final JSpinner xmin, xmax, ymin, ymax;

    /**
     * Résolution (en minutes de longitudes et de latitudes) demandée par l'utilisateur.
     * Ces résolution sont gérées par un modèle {@link SpinnerNumberModel}.
     */
    private final JSpinner xres, yres;

    /**
     * Bouton radio pour sélectioner la meilleure résolution possible.
     */
    private final AbstractButton radioBestRes;

    /**
     * Bouton radio pour sélectioner la résolution spécifiée.
     */
    private final AbstractButton radioPrefRes;

    /**
     * Composante facultative à afficher à la droite du paneau {@code CoordinateChooser}.
     */
    private JComponent accessory;

    /**
     * Class encompassing various listeners for users selections.
     *
     * @version $Id: CoordinateChooser.java 30655 2008-06-12 20:24:25Z acuster $
     * @author Martin Desruisseaux (IRD)
     */
    private final class Listeners implements ActionListener, ChangeListener {
        /**
         * List of components to toggle.
         */
        private final JComponent[] toggle;

        /**
         * Constructs a {@code Listeners} object.
         */
        public Listeners(final JComponent[] toggle) {
            this.toggle=toggle;
        }

        /**
         * Invoked when user select a new timezone.
         */
        public void actionPerformed(final ActionEvent event) {
            update(getTimeZone());
        }

        /**
         * Invoked when user change the button radio state
         * ("use best resolution" / "set resolution").
         */
        public void stateChanged(final ChangeEvent event) {
            setEnabled(radioPrefRes.isSelected());
        }

        /**
         * Enable or disable {@link #toggle} components.
         */
        final void setEnabled(final boolean state) {
            for (int i=0; i<toggle.length; i++) {
                toggle[i].setEnabled(state);
            }
        }
    }

    /**
     * Constructs a default coordinate chooser. Date will be constrained in the range from
     * January 1st, 1970 00:00 UTC up to the {@linkplain System#currentTimeMillis current time}.
     */
    public CoordinateChooser() {
        this(new Date(0), new Date());
    }

    /**
     * Constructs a coordinate chooser with date constrained in the specified range.
     * Note that the {@code [minTime..maxTime]} range is not the same than the
     * range given to {@link #setTimeRange}. The later set only the time range shown
     * in the widget, while this constructor set also the minimum and maximum dates
     * allowed.
     *
     * @param minTime The minimal date allowed.
     * @param maxTime the maximal date allowed.
     */
    public CoordinateChooser(final Date minTime, final Date maxTime) {
        super(new GridBagLayout());
        final Locale locale = getDefaultLocale();
        final int timeField = Calendar.DAY_OF_YEAR;
        final Vocabulary resources = Vocabulary.getResources(locale);

        radioBestRes = new JRadioButton(resources.getString(VocabularyKeys.USE_BEST_RESOLUTION), true);
        radioPrefRes = new JRadioButton(resources.getString(VocabularyKeys.SET_PREFERRED_RESOLUTION));

        tmin = new JSpinner(new SpinnerDateModel(minTime, minTime, maxTime, timeField));
        tmax = new JSpinner(new SpinnerDateModel(maxTime, minTime, maxTime, timeField));
        xmin = new JSpinner(new SpinnerAngleModel(new Longitude(Longitude.MIN_VALUE)));
        xmax = new JSpinner(new SpinnerAngleModel(new Longitude(Longitude.MAX_VALUE)));
        ymin = new JSpinner(new SpinnerAngleModel(new  Latitude( Latitude.MIN_VALUE)));
        ymax = new JSpinner(new SpinnerAngleModel(new  Latitude( Latitude.MAX_VALUE)));
        xres = new JSpinner(new SpinnerNumberModel(1, 0, 360*60, 1));
        yres = new JSpinner(new SpinnerNumberModel(1, 0, 180*60, 1));

        final AngleFormat   angleFormat = new AngleFormat("D°MM.m'", locale);
        final DateFormat     dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        xmin.setEditor(new SpinnerAngleModel.Editor(xmin, angleFormat));
        xmax.setEditor(new SpinnerAngleModel.Editor(xmax, angleFormat));
        ymin.setEditor(new SpinnerAngleModel.Editor(ymin, angleFormat));
        ymax.setEditor(new SpinnerAngleModel.Editor(ymax, angleFormat));

        setup(tmin, 10,   dateFormat);
        setup(tmax, 10,   dateFormat);
        setup(xmin,  7,         null);
        setup(xmax,  7,         null);
        setup(ymin,  7,         null);
        setup(ymax,  7,         null);
        setup(xres,  3, numberFormat);
        setup(yres,  3, numberFormat);

        final String[] timezones = TimeZone.getAvailableIDs();
        Arrays.sort(timezones);
        timezone = new JComboBox(timezones);
        timezone.setSelectedItem(dateFormat.getTimeZone().getID());

        final JLabel labelSize1 = new JLabel(resources.getLabel(VocabularyKeys.SIZE_IN_MINUTES));
        final JLabel labelSize2 = new JLabel("\u00D7"  /* Multiplication symbol */);
        final ButtonGroup group = new ButtonGroup();
        group.add(radioBestRes);
        group.add(radioPrefRes);

        final Listeners listeners = new Listeners(new JComponent[] {
                                                  labelSize1, labelSize2, xres, yres});
        listeners   .setEnabled(false);
        timezone    .addActionListener(listeners);
        radioPrefRes.addChangeListener(listeners);

        areaPanel = getPanel(resources.getString(VocabularyKeys.GEOGRAPHIC_COORDINATES));
        timePanel = getPanel(resources.getString(VocabularyKeys.TIME_RANGE            ));
        resoPanel = getPanel(resources.getString(VocabularyKeys.PREFERRED_RESOLUTION  ));
        final GridBagConstraints c = new GridBagConstraints();

        c.weightx=1;
        c.gridx=1; c.gridy=0; areaPanel.add(ymax, c);
        c.gridx=0; c.gridy=1; areaPanel.add(xmin, c);
        c.gridx=2; c.gridy=1; areaPanel.add(xmax, c);
        c.gridx=1; c.gridy=2; areaPanel.add(ymin, c);

        JLabel label;
        c.gridx=0; c.anchor=c.WEST; c.insets.right=3; c.weightx=0;
        c.gridy=0; timePanel.add(label=new JLabel(resources.getLabel(VocabularyKeys.START_TIME)), c); label.setLabelFor(tmin);
        c.gridy=1; timePanel.add(label=new JLabel(resources.getLabel(VocabularyKeys.END_TIME  )), c); label.setLabelFor(tmax);
        c.gridy=2; timePanel.add(label=new JLabel(resources.getLabel(VocabularyKeys.TIME_ZONE )), c); label.setLabelFor(timezone); c.gridwidth=4;
        c.gridy=0; resoPanel.add(radioBestRes,  c);
        c.gridy=1; resoPanel.add(radioPrefRes,  c);
        c.gridy=2; c.gridwidth=1; c.anchor=c.EAST; c.insets.right=c.insets.left=1; c.weightx=1;
        c.gridx=0; resoPanel.add(labelSize1, c); labelSize1.setLabelFor(xres);  c.weightx=0;
        c.gridx=1; resoPanel.add(xres,       c);
        c.gridx=2; resoPanel.add(labelSize2, c); labelSize2.setLabelFor(yres);
        c.gridx=3; resoPanel.add(yres,       c);

        c.gridx=1; c.fill=c.HORIZONTAL; c.insets.right=c.insets.left=0; c.weightx=1;
        c.gridy=0; timePanel.add(tmin,     c);
        c.gridy=1; timePanel.add(tmax,     c);
        c.gridy=2; timePanel.add(timezone, c);

        c.insets.right=c.insets.left=c.insets.top=c.insets.bottom=3;
        c.gridx=0; c.anchor=c.CENTER; c.fill=c.BOTH; c.weighty=1;
        c.gridy=0; add(areaPanel, c);
        c.gridy=1; add(timePanel, c);
        c.gridy=2; add(resoPanel, c);
    }

    /**
     * Retourne un panneau avec une bordure titrée.
     */
    private static JPanel getPanel(final String title) {
        final JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(title),
                        BorderFactory.createEmptyBorder(6,6,6,6)));
        return panel;
    }

    /**
     * Définit la largeur (en nombre de colonnes) d'un champ.
     * Eventuellement, cette méthode peut aussi redéfinir le format.
     */
    private static void setup(final JSpinner spinner, final int width, final Format format) {
        final JFormattedTextField field=((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        field.setMargin(new Insets(/*top*/0, /*left*/6, /*bottom*/0, /*right*/3));
        field.setColumns(width);
        if (format!=null) {
            ((InternationalFormatter)field.getFormatter()).setFormat(format);
        }
    }

    /**
     * Tells if a selector is currently visible or not. The default {@code CoordinateChooser}
     * contains three selectors: one for geographic area, one for time range and one for the
     * preferred resolution.
     *
     * @param selector One of the following constants:
     *                 {@link #GEOGRAPHIC_AREA},
     *                 {@link #TIME_RANGE} or
     *                 {@link #RESOLUTION}.
     * @return {@code true} if the specified selector is visible, or {@code false} otherwise.
     * @throws IllegalArgumentException if {@code selector} is not legal.
     */
    public boolean isSelectorVisible(final int selector) {
        switch (selector) {
            case GEOGRAPHIC_AREA: return areaPanel.isVisible();
            case TIME_RANGE:      return timePanel.isVisible();
            case RESOLUTION:      return resoPanel.isVisible();
            default: throw new IllegalArgumentException();
                     // TODO: provide some error message.
        }
    }

    /**
     * Set the visible state of one or many selectors.
     * All selectors are visible by default.
     *
     * @param selectors Any bitwise combinaisons of
     *                  {@link #GEOGRAPHIC_AREA},
     *                  {@link #TIME_RANGE} and/or
     *                  {@link #RESOLUTION}.
     * @param visible {@code true} to show the selectors, or {@code false} to hide them.
     * @throws IllegalArgumentException if {@code selectors} contains illegal bits.
     */
    public void setSelectorVisible(final int selectors, final boolean visible) {
        ensureValidSelectors(selectors);
        if ((selectors & GEOGRAPHIC_AREA) != 0) areaPanel.setVisible(visible);
        if ((selectors & TIME_RANGE     ) != 0) timePanel.setVisible(visible);
        if ((selectors & RESOLUTION     ) != 0) resoPanel.setVisible(visible);
    }

    /**
     * Ensure that the specified bitwise combinaison of selectors is valid.
     *
     * @param selectors Any bitwise combinaisons of
     *                  {@link #GEOGRAPHIC_AREA},
     *                  {@link #TIME_RANGE} and/or
     *                  {@link #RESOLUTION}.
     * @throws IllegalArgumentException if {@code selectors} contains illegal bits.
     *
     * @todo Provide a better error message.
     */
    private static void ensureValidSelectors(final int selectors) throws IllegalArgumentException {
        if ((selectors & ~(GEOGRAPHIC_AREA | TIME_RANGE | RESOLUTION)) != 0) {
            throw new IllegalArgumentException(String.valueOf(selectors));
        }
    }

    /**
     * Returns the value for the specified number, or NaN if {@code value} is not a number.
     */
    private static double doubleValue(final JSpinner spinner) {
        final Object value = spinner.getValue();
        return (value instanceof Number) ? ((Number)value).doubleValue() : Double.NaN;
    }

    /**
     * Returns the value for the specified angle, or NaN if {@code value} is not an angle.
     */
    private static double degrees(final JSpinner spinner, final boolean expectLatitude) {
        final Object value = spinner.getValue();
        if (value instanceof Angle) {
            if (expectLatitude ? (value instanceof Longitude) : (value instanceof Latitude)) {
                return Double.NaN;
            }
            return ((Angle) value).degrees();
        }
        return Double.NaN;
    }

    /**
     * Gets the geographic area, in latitude and longitude degrees.
     */
    public Rectangle2D getGeographicArea() {
        final double xmin = degrees(this.xmin, false);
        final double ymin = degrees(this.ymin,  true);
        final double xmax = degrees(this.xmax, false);
        final double ymax = degrees(this.ymax,  true);
        return new Rectangle2D.Double(Math.min(xmin,xmax), Math.min(ymin,ymax),
                                      Math.abs(xmax-xmin), Math.abs(ymax-ymin));
    }

    /**
     * Sets the geographic area, in latitude and longitude degrees.
     */
    public void setGeographicArea(final Rectangle2D area) {
        xmin.setValue(new Longitude(area.getMinX()));
        xmax.setValue(new Longitude(area.getMaxX()));
        ymin.setValue(new  Latitude(area.getMinY()));
        ymax.setValue(new  Latitude(area.getMaxY()));
    }

    /**
     * Returns the preferred resolution. A {@code null} value means that the
     * best available resolution should be used.
     */
    public Dimension2D getPreferredResolution() {
        if (radioPrefRes.isSelected()) {
            return new XDimension2D.Double(doubleValue(xres), doubleValue(yres));
        }
        return null;
    }

    /**
     * Sets the preferred resolution. A {@code null} value means that the best
     * available resolution should be used.
     */
    public void setPreferredResolution(final Dimension2D resolution) {
        if (resolution!=null) {
            xres.setValue(new Double(resolution.getWidth ()*60));
            yres.setValue(new Double(resolution.getHeight()*60));
            radioPrefRes.setSelected(true);
        }  else {
            radioBestRes.setSelected(true);
        }
    }

    /**
     * Returns the time zone used for displaying dates.
     */
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(timezone.getSelectedItem().toString());
    }

    /**
     * Sets the time zone. This method change the control's display.
     * It doesn't change the date values, i.e. it have no effect
     * on previous or future call to {@link #setTimeRange}.
     */
    public void setTimeZone(final TimeZone timezone) {
        this.timezone.setSelectedItem(timezone.getID());
    }

    /**
     * Updates the time zone in text fields. This method is automatically invoked
     * by {@link JComboBox} on user's selection. It is also (indirectly) invoked
     * on {@link #setTimeZone} call.
     */
    private void update(final TimeZone timezone) {
        boolean refresh=true;
        try {
            tmin.commitEdit();
            tmax.commitEdit();
        } catch (ParseException exception) {
            refresh = false;
        }
        ((JSpinner.DateEditor)tmin.getEditor()).getFormat().setTimeZone(timezone);
        ((JSpinner.DateEditor)tmax.getEditor()).getFormat().setTimeZone(timezone);
        if (refresh) {
            // TODO: If a "JSpinner.reformat()" method was available, we would use it here.
            fireStateChanged((AbstractSpinnerModel)tmin.getModel());
            fireStateChanged((AbstractSpinnerModel)tmax.getModel());
        }
    }

    /**
     * Run each {@link ChangeListener#stateChanged()} method for the specified spinner model.
     */
    private static void fireStateChanged(final AbstractSpinnerModel model) {
        final ChangeEvent   changeEvent = new ChangeEvent(model);
        final EventListener[] listeners = model.getListeners(ChangeListener.class);
        for (int i=listeners.length; --i>=0;) {
            ((ChangeListener)listeners[i]).stateChanged(changeEvent);
        }
    }

    /**
     * Returns the start time, or {@code null} if there is none.
     */
    public Date getStartTime() {
        return (Date) tmin.getValue();
    }

    /**
     * Returns the end time, or {@code null} if there is none.
     */
    public Date getEndTime() {
        return (Date) tmax.getValue();
    }

    /**
     * Sets the time range.
     *
     * @param startTime The start time.
     * @param   endTime The end time.
     *
     * @see #getStartTime
     * @see #getEndTime
     */
    public void setTimeRange(final Date startTime, final Date endTime) {
        tmin.setValue(startTime);
        tmax.setValue(  endTime);
    }

    /**
     * Returns the accessory component.
     *
     * @return The accessory component, or {@code null} if there is none.
     */
    public JComponent getAccessory() {
        return accessory;
    }

    /**
     * Sets the accessory component. An accessory is often used to show available data.
     * However, it can be used for anything that the programmer wishes, such as extra
     * custom coordinate chooser controls.
     * <p>
     * <strong>Note:</strong> If there was a previous accessory, you should unregister any
     * listeners that the accessory might have registered with the coordinate chooser.
     *
     * @param accessory The accessory component, or {@code null} to remove any previous accessory.
     */
    public void setAccessory(final JComponent accessory) {
        synchronized (getTreeLock()) {
            if (this.accessory!=null) {
                remove(this.accessory);
            }
            this.accessory = accessory;
            if (accessory!=null) {
                final GridBagConstraints c=new GridBagConstraints();
                c.insets.right=c.insets.left=c.insets.top=c.insets.bottom=3;
                c.gridx=1; c.weightx=1; c.gridwidth=1;
                c.gridy=0; c.weighty=1; c.gridheight=3;
                c.anchor=c.CENTER; c.fill=c.BOTH;
                add(accessory, c);
            }
            validate();
        }
    }

    /**
     * Check if an angle is of expected type (latitude or longitude).
     */
    private void checkAngle(final JSpinner field, final boolean expectLatitude) throws ParseException {
        final Object angle=field.getValue();
        if (expectLatitude ? (angle instanceof Longitude) : (angle instanceof Latitude)) {
            throw new ParseException(Errors.getResources(getLocale()).getString(
                                     ErrorKeys.BAD_COORDINATE_$1, angle), 0);
        }
    }

    /**
     * Commits the currently edited values. If commit fails, focus will be set on the offending
     * field.
     *
     * @throws ParseException If at least one of currently edited value couldn't be commited.
     */
    public void commitEdit() throws ParseException {
        JSpinner focus=null;
        try {
            (focus=tmin).commitEdit();
            (focus=tmax).commitEdit();
            (focus=xmin).commitEdit();
            (focus=xmax).commitEdit();
            (focus=ymin).commitEdit();
            (focus=ymax).commitEdit();
            (focus=xres).commitEdit();
            (focus=yres).commitEdit();

            checkAngle(focus=xmin, false);
            checkAngle(focus=xmax, false);
            checkAngle(focus=ymin,  true);
            checkAngle(focus=ymax,  true);
        } catch (ParseException exception) {
            focus.requestFocus();
            throw exception;
        }
    }

    /**
     * Prend en compte les valeurs des champs édités par l'utilisateur.
     * Si les entrés ne sont pas valide, affiche un message d'erreur en
     * utilisant la fenêtre parente {@code owner} spécifiée.
     *
     * @param  owner Fenêtre dans laquelle faire apparaître d'eventuels messages d'erreur.
     * @return {@code true} si la prise en compte des paramètres à réussie.
     */
    private boolean commitEdit(final Component owner) {
        try {
            commitEdit();
        } catch (ParseException exception) {
            SwingUtilities.showMessageDialog(owner, exception.getLocalizedMessage(),
                           Errors.getResources(getLocale()).getString(ErrorKeys.BAD_ENTRY),
                           JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Adds a change listener to the listener list. This change listener will be notify when
     * a value changed. The change may be in a geographic coordinate field, a date field, a
     * resolution field, etc. The watched values depend on the {@code selectors} arguments:
     * {@link #GEOGRAPHIC_AREA} will watches for the bounding box (East, West, North and South
     * value); {@link #TIME_RANGE} watches for start time and end time; {@link #RESOLUTION}
     * watches for the resolution along East-West and North-South axis. Bitwise combinaisons
     * are allowed. For example, <code>GEOGRAPHIC_AREA | TIME_RANGE</code> will register a
     * listener for both geographic area and time range.
     * <p>
     * The source of {@link ChangeEvent}s delivered to {@link ChangeListener}s will be in most
     * case the {@link SpinnerModel} for the edited field.
     *
     * @param  selectors Any bitwise combinaisons of
     *                   {@link #GEOGRAPHIC_AREA},
     *                   {@link #TIME_RANGE} and/or
     *                   {@link #RESOLUTION}.
     * @param  listener The listener to add to the specified selectors.
     * @throws IllegalArgumentException if {@code selectors} contains illegal bits.
     */
    public void addChangeListener(final int selectors, final ChangeListener listener) {
        ensureValidSelectors(selectors);
        if ((selectors & GEOGRAPHIC_AREA) != 0) {
            xmin.getModel().addChangeListener(listener);
            xmax.getModel().addChangeListener(listener);
            ymin.getModel().addChangeListener(listener);
            ymax.getModel().addChangeListener(listener);
        }
        if ((selectors & TIME_RANGE) != 0) {
            tmin.getModel().addChangeListener(listener);
            tmax.getModel().addChangeListener(listener);
        }
        if ((selectors & RESOLUTION) != 0) {
            xres.getModel().addChangeListener(listener);
            yres.getModel().addChangeListener(listener);
            radioPrefRes.getModel().addChangeListener(listener);
        }
    }

    /**
     * Removes a change listener from the listener list.
     *
     * @param  selectors Any bitwise combinaisons of
     *                   {@link #GEOGRAPHIC_AREA},
     *                   {@link #TIME_RANGE} and/or
     *                   {@link #RESOLUTION}.
     * @param  listener The listener to remove from the specified selectors.
     * @throws IllegalArgumentException if {@code selectors} contains illegal bits.
     */
    public void removeChangeListener(final int selectors, final ChangeListener listener) {
        ensureValidSelectors(selectors);
        if ((selectors & GEOGRAPHIC_AREA) != 0) {
            xmin.getModel().removeChangeListener(listener);
            xmax.getModel().removeChangeListener(listener);
            ymin.getModel().removeChangeListener(listener);
            ymax.getModel().removeChangeListener(listener);
        }
        if ((selectors & TIME_RANGE) != 0) {
            tmin.getModel().removeChangeListener(listener);
            tmax.getModel().removeChangeListener(listener);
        }
        if ((selectors & RESOLUTION) != 0) {
            xres.getModel().removeChangeListener(listener);
            yres.getModel().removeChangeListener(listener);
            radioPrefRes.getModel().removeChangeListener(listener);
        }
    }

    /**
     * Shows a dialog box requesting input from the user. The dialog box will be
     * parented to {@code owner}. If {@code owner} is contained into a
     * {@link javax.swing.JDesktopPane}, the dialog box will appears as an internal
     * frame. This method can be invoked from any thread (may or may not be the
     * <cite>Swing</cite> thread).
     *
     * @param  owner The parent component for the dialog box, or {@code null} if there is no parent.
     * @return {@code true} if user pressed the "Ok" button, or {@code false} otherwise
     *         (e.g. pressing "Cancel" or closing the dialog box from the title bar).
     */
    public boolean showDialog(final Component owner) {
        return showDialog(owner, Vocabulary.getResources(getLocale()).
                                 getString(VocabularyKeys.COORDINATES_SELECTION));
    }

    /**
     * Shows a dialog box requesting input from the user. If {@code owner} is contained into a
     * {@link javax.swing.JDesktopPane}, the dialog box will appears as an internal frame. This
     * method can be invoked from any thread (may or may not be the <cite>Swing</cite> thread).
     *
     * @param  owner The parent component for the dialog box, or {@code null} if there is no parent.
     * @param  title The dialog box title.
     * @return {@code true} if user pressed the "Ok" button, or {@code false} otherwise
     *         (e.g. pressing "Cancel" or closing the dialog box from the title bar).
     */
    public boolean showDialog(final Component owner, final String title) {
        while (SwingUtilities.showOptionDialog(owner, this, title)) {
            if (commitEdit(owner)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show the dialog box. This method is provided only as an easy
     * way to test the dialog appearance from the command line.
     */
    public static void main(final String[] args) {
        new CoordinateChooser().showDialog(null);
    }
}
