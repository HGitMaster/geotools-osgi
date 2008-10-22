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
package org.geotools.referencing.operation.projection;

import java.awt.geom.Point2D;
import java.util.Collection;
import javax.measure.unit.Unit;
import javax.measure.unit.NonSI;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.ConicProjection;
import org.opengis.referencing.operation.MathTransform;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.resources.i18n.ErrorKeys;

import static java.lang.Math.*;


/**
 * Krovak Oblique Conformal Conic projection (EPSG code 9819). This projection is used in the
 * Czech Republic and Slovakia under the name "Krovak" projection. The geographic coordinates
 * on the ellipsoid are first reduced to conformal coordinates on the conformal (Gaussian) sphere.
 * These spherical coordinates are then projected onto the oblique cone and converted to grid
 * coordinates. The pseudo standard parallel is defined on the conformal sphere after its rotation,
 * to obtain the oblique aspect of the projection. It is then the parallel on this sphere at which
 * the map projection is true to scale; on the ellipsoid it maps as a complex curve.
 * <p>
 * The compulsory parameters are just the ellipsoid characteristics. All other parameters are
 * optional and have defaults to match the common usage with Krovak projection.
 * <p>
 * In general the axis of Krovak projection are defined as westing and southing (not easting and
 * northing) and they are also reverted, so if the value of projected coordinates should (and in
 * <var>y</var>, <var>x</var> order in Krovak) be positive the 'Axis' parameter for projection
 * should be defined explicitly like this (in wkt):
 *
 * <pre>PROJCS["S-JTSK (Ferro) / Krovak",
 *         .
 *         .
 *         .
 *     PROJECTION["Krovak"]
 *     PARAMETER["semi_major", 6377397.155],
 *     PARAMETER["semi_minor", 6356078.963],
 *     UNIT["meter",1.0],
 *     AXIS["x", WEST],
 *     AXIS["y", SOUTH]]
 *     </pre>
 * Axis in Krovak:
 * <pre>
 *   y<------------------+
 *                       |
 *    Czech. Rep.        |
 *                       |
 *                       x
 * </pre>
 *
 * By default, the axis are 'easting, northing' so the values of projected coordinates are negative
 * (and in <var>y</var>, <var>x</var> order in Krovak - it is cold Krovak GIS version).
 * <p>
 * <b>References:</b>
 * <ul>
 *   <li>Proj-4.4.7 available at <A HREF="http://www.remotesensing.org/proj">www.remotesensing.org/proj</A><br>
 *       Relevant files is: {@code PJ_krovak.c}</li>
 *   <li>"Coordinate Conversions and Transformations including Formulas" available at,
 *       <A HREF="http://www.remotesensing.org/geotiff/proj_list/guid7.html">http://www.remotesensing.org/geotiff/proj_list/guid7.html</A></li>
 * </ul>
 * </p>
 *
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/krovak.html">Krovak on RemoteSensing.org</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/guid7.html">Krovak on "Coordinate
 *       Conversions and Transformations including Formulas"</A>
 * @see <A HREF="http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs34e2.html">Krovak on POSC</A>
 *
 * @since 2.4
 * @version $Id: Krovak.java 30760 2008-06-18 14:28:24Z desruisseaux $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/referencing/src/main/java/org/geotools/referencing/operation/projection/Krovak.java $
 * @author Jan Jezek
 * @author Martin Desruisseaux
 */
public class Krovak extends MapProjection {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8359105634355342212L;

    /**
     * Maximum number of iterations for iterative computations.
     */
    private static final int MAXIMUM_ITERATIONS = 15;

    /**
     * When to stop the iteration.
     */
    private static final double ITERATION_TOLERANCE = 1E-11;

    /**
     * Azimuth of the centre line passing through the centre of the projection.
     * This is equals to the co-latitude of the cone axis at point of intersection
     * with the ellipsoid.
     */
    protected final double azimuth;

    /**
     * Latitude of pseudo standard parallel.
     */
    protected final double pseudoStandardParallel;

    /**
     * Useful variables calculated from parameters defined by user.
     */
    private final double sinAzim, cosAzim, n, tanS2, alfa, hae, k1, ka, ro0, rop;

    /**
     * Useful constant - 45° in radians.
     */
    private static final double s45 = 0.785398163397448;

    /**
     * Constructs a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected Krovak(final ParameterValueGroup parameters) throws ParameterNotFoundException {
        super(parameters);
        final Collection<GeneralParameterDescriptor> expected = getParameterDescriptors().descriptors();
        // Fetch parameters from user input.
        latitudeOfOrigin       = doubleValue(expected, Provider.LATITUDE_OF_CENTER,       parameters);
        centralMeridian        = doubleValue(expected, Provider.LONGITUDE_OF_CENTER,      parameters);
        azimuth                = doubleValue(expected, Provider.AZIMUTH,                  parameters);
        pseudoStandardParallel = doubleValue(expected, Provider.PSEUDO_STANDARD_PARALLEL, parameters);
        scaleFactor            = doubleValue(expected, Provider.SCALE_FACTOR,             parameters);
        ensureLatitudeInRange (Provider.LATITUDE_OF_CENTER,  latitudeOfOrigin, false);
        ensureLongitudeInRange(Provider.LONGITUDE_OF_CENTER, centralMeridian,  false);

        // Calculates useful constants.
        sinAzim = sin(azimuth);
        cosAzim = cos(azimuth);
        n       = sin(pseudoStandardParallel);
        tanS2   = tan(pseudoStandardParallel / 2 + s45);

        final double sinLat, cosLat, cosL2, u0;
        sinLat = sin(latitudeOfOrigin);
        cosLat = cos(latitudeOfOrigin);
        cosL2  = cosLat * cosLat;
        alfa   = sqrt(1 + ((excentricitySquared * (cosL2*cosL2)) / (1 - excentricitySquared)));
        hae    = alfa * excentricity / 2;
        u0     = asin(sinLat / alfa);

        final double g, esl;
        esl = excentricity * sinLat;
        g   = pow((1 - esl) / (1 + esl), (alfa * excentricity) / 2);
        k1  = pow(tan(latitudeOfOrigin/2 + s45), alfa) * g / tan(u0/2 + s45);
        ka  = pow(1 / k1, -1 / alfa);

        final double radius;
        radius = sqrt(1 - excentricitySquared) / (1 - (excentricitySquared * (sinLat * sinLat)));

        ro0 = scaleFactor * radius / tan(pseudoStandardParallel);
        rop = ro0 * pow(tanS2, n);
    }

    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParameterValueGroup getParameterValues() {
        final Collection<GeneralParameterDescriptor> expected = getParameterDescriptors().descriptors();
        final ParameterValueGroup values = super.getParameterValues();
        set(expected, Provider.LATITUDE_OF_CENTER,       values, latitudeOfOrigin      );
        set(expected, Provider.LONGITUDE_OF_CENTER,      values, centralMeridian       );
        set(expected, Provider.AZIMUTH,                  values, azimuth               );
        set(expected, Provider.PSEUDO_STANDARD_PARALLEL, values, pseudoStandardParallel);
        set(expected, Provider.SCALE_FACTOR,             values, scaleFactor           );
        return values;
    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
     * (units in radians) and stores the result in {@code ptDst} (linear distance
     * on a unit sphere).
     */
    protected Point2D transformNormalized(final double lambda, final double phi, Point2D ptDst)
            throws ProjectionException
    {
        final double esp = excentricity * sin(phi);
        final double gfi = pow(((1. - esp) / (1. + esp)), hae);
        final double u   = 2 * (atan(pow(tan(phi/2 + s45), alfa) / k1 * gfi) - s45);
        final double deltav = -lambda * alfa;
        final double cosU = cos(u);
        final double s = asin((cosAzim * sin(u)) + (sinAzim * cosU * cos(deltav)));
        final double d = asin(cosU * sin(deltav) / cos(s));
        final double eps = n * d;
        final double ro = rop / pow(tan(s/2 + s45), n);

        // x and y are reverted.
        final double y = -(ro * cos(eps));
        final double x = -(ro * sin(eps));

        if (ptDst != null) {
            ptDst.setLocation(x, y);
            return ptDst;
        }
        return new Point2D.Double(x, y);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in {@code ptDst}.
     */
    protected Point2D inverseTransformNormalized(final double x, final double y, Point2D ptDst)
            throws ProjectionException
    {
        // x -> southing, y -> westing
        final double ro  = hypot(x, y);
        final double eps = atan2(-x, -y);
        final double d   = eps / n;
        final double s   = 2 * (atan(pow(ro0/ro, 1/n) * tanS2) - s45);
        final double cs  = cos(s);
        final double u   = asin((cosAzim * sin(s)) - (sinAzim * cs * cos(d)));
        final double kau = ka * pow(tan((u / 2.) + s45), 1 / alfa);
        final double deltav = asin((cs * sin(d)) / cos(u));
        final double lambda = -deltav / alfa;
        double phi = 0;
        double fi1 = u;

        // iteration calculation
        for (int i=MAXIMUM_ITERATIONS;;) {
            fi1 = phi;
            final double esf = excentricity * sin(fi1);
            phi = 2. * (atan(kau * pow((1. + esf) / (1. - esf), excentricity/2.)) - s45);
            if (abs(fi1 - phi) <= ITERATION_TOLERANCE) {
                break;
            }
            if (--i < 0) {
                throw new ProjectionException(ErrorKeys.NO_CONVERGENCE);
            }
        }
        if (ptDst != null) {
            ptDst.setLocation(lambda, phi);
            return ptDst;
        }
        return new Point2D.Double(lambda, phi);
    }




    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                                 PROVIDERS                                ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The {@linkplain org.geotools.referencing.operation.MathTransformProvider math transform
     * provider} for an {@linkplain Krovak Krovak} projection (EPSG code 9819).
     *
     * @since 2.4
     * @version $Id: Krovak.java 30760 2008-06-18 14:28:24Z desruisseaux $
     * @author Jan Jezek
     *
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     */
    public static class Provider extends AbstractProvider {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = -278392856661204734L;

        /**
         * The operation parameter descriptor for the {@linkplain #latitudeOfOrigin
         * latitude of origin} parameter value. Valid values range is from -90 to 90.
         * Default value is 49.5.
         */
        public static final ParameterDescriptor LATITUDE_OF_CENTER = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,     "latitude_of_center"),
                    new NamedIdentifier(Citations.EPSG,    "Latitude of projection centre"),
                    new NamedIdentifier(Citations.GEOTIFF, "CenterLat")
                }, 49.5, -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@linkplain #centralMeridian central
         * meridian} parameter value. Valid values range is from -180 to 180. Default value
         * is 24ï¿½50' (= 42ï¿½50' from Ferro prime meridian).
         */
        public static final ParameterDescriptor LONGITUDE_OF_CENTER = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,     "longitude_of_center"),
                    new NamedIdentifier(Citations.EPSG,    "Longitude of projection centre"),
                    new NamedIdentifier(Citations.GEOTIFF, "CenterLong")
                }, 42.5-17.66666666666667, -180, 180, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@linkplain #azimuth azimuth} parameter
         * value. Valid values range is from -90 to 90. Default value is 30.28813972222.
         */
        public static final ParameterDescriptor AZIMUTH = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,     "azimuth"),
                    new NamedIdentifier(Citations.EPSG,    "Azimuth of initial line"),
                    new NamedIdentifier(Citations.GEOTIFF, "AzimuthAngle")
                }, 30.28813972222222, 0, 360, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the pseudo {@linkplain #pseudoStandardParallel
         * pseudi standard parallel} parameter value. Valid values range is from -90 to 90.
         * Default value is 78.5.
         */
        public static final ParameterDescriptor PSEUDO_STANDARD_PARALLEL =
                createDescriptor(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,  "pseudo_standard_parallel_1"),
                    new NamedIdentifier(Citations.EPSG, "Latitude of Pseudo Standard Parallel")
                }, 78.5, -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@link #scaleFactor scaleFactor}
         * parameter value. Valid values range is from 0 to infinity. Default value is 1.
         */
        public static final ParameterDescriptor SCALE_FACTOR = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,  "scale_factor"),
                    new NamedIdentifier(Citations.EPSG, "Scale factor on pseudo standard parallel"),
                    new NamedIdentifier(Citations.GEOTIFF, "ScaleAtCenter")
                }, 0.9999, 0, Double.POSITIVE_INFINITY, Unit.ONE);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
            new NamedIdentifier(Citations.OGC,     "Krovak"),
            new NamedIdentifier(Citations.GEOTIFF, "Krovak"),
            new NamedIdentifier(Citations.EPSG,    "Krovak Oblique Conformal Conic"),
            new NamedIdentifier(Citations.EPSG,    "Krovak Oblique Conic Conformal"),
            new NamedIdentifier(Citations.EPSG,    "9819"),
                },
                new ParameterDescriptor[] {
                    SEMI_MAJOR, SEMI_MINOR, LATITUDE_OF_CENTER, LONGITUDE_OF_CENTER,
                    AZIMUTH, PSEUDO_STANDARD_PARALLEL, SCALE_FACTOR,
                    FALSE_EASTING, FALSE_NORTHING
                });

        /**
         * Constructs a new provider.
         */
        public Provider() {
            super(PARAMETERS);
        }

        /**
         * Returns the operation type for this map projection.
         */
        @Override
        public Class<ConicProjection> getOperationType() {
            return ConicProjection.class;
        }

        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup parameters)
                throws ParameterNotFoundException
        {
            return new Krovak(parameters);
        }
    }

    /**
     * Returns a hash value for this projection.
     */
    @Override
    public int hashCode() {
        final long code = Double.doubleToLongBits(azimuth) ^
                          Double.doubleToLongBits(pseudoStandardParallel);
        return ((int)code ^ (int)(code >>> 32)) + 37*super.hashCode();
    }

    /**
     * Compares the specified object with this map projection for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final Krovak that = (Krovak) object;
            return equals(azimuth, that.azimuth) &&
                   equals(pseudoStandardParallel, that.pseudoStandardParallel);
        }
        return false;
    }
}
