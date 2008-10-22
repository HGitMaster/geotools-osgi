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
 *    
 *    THIS IS AN AUTOMATICALLY GENERATED FILE. DO NOT EDIT!
 *    Generated with: org.geotools.resources.IndexedResourceCompiler
 */
package org.geotools.resources.i18n;


/**
 * Resource keys. This class is used when compiling sources, but
 * no dependencies to {@code ResourceKeys} should appear in any
 * resulting class files.  Since Java compiler inlines final integer
 * values, using long identifiers will not bloat constant pools of
 * classes compiled against the interface, provided that no class
 * implements this interface.
 *
 * @see org.geotools.resources.IndexedResourceBundle
 * @see org.geotools.resources.IndexedResourceCompiler
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/metadata/src/main/java/org/geotools/resources/i18n/ErrorKeys.java $
 */
public final class ErrorKeys {
    private ErrorKeys() {
    }

    /**
     * Ambiguous axis length.
     */
    public static final int AMBIGIOUS_AXIS_LENGTH = 0;

    /**
     * Angle {0} is too high.
     */
    public static final int ANGLE_OVERFLOW_$1 = 1;

    /**
     * Latitudes {0} and {1} are opposite.
     */
    public static final int ANTIPODE_LATITUDES_$2 = 2;

    /**
     * Azimuth {0} is out of range (±180°).
     */
    public static final int AZIMUTH_OUT_OF_RANGE_$1 = 3;

    /**
     * Band number {0} is not valid.
     */
    public static final int BAD_BAND_NUMBER_$1 = 5;

    /**
     * Coefficient {0}={1} can't be NaN or infinity.
     */
    public static final int BAD_COEFFICIENT_$2 = 6;

    /**
     * Illegal coordinate: {0}
     */
    public static final int BAD_COORDINATE_$1 = 7;

    /**
     * Bad entry
     */
    public static final int BAD_ENTRY = 8;

    /**
     * Illegal grid range [{1} .. {2}] for dimension {0}.
     */
    public static final int BAD_GRID_RANGE_$3 = 9;

    /**
     * Illegal data at line {1} in file "{0}".
     */
    public static final int BAD_LINE_IN_FILE_$2 = 4;

    /**
     * Bad local: {0}
     */
    public static final int BAD_LOCALE_$1 = 10;

    /**
     * Parameter "{0}" can't have value "{1}".
     */
    public static final int BAD_PARAMETER_$2 = 11;

    /**
     * Parameter "{0}" can't be of type '{1}'.
     */
    public static final int BAD_PARAMETER_TYPE_$2 = 12;

    /**
     * Range [{0} .. {1}] is not valid.
     */
    public static final int BAD_RANGE_$2 = 13;

    /**
     * Empty or invalid rectangle: {0}
     */
    public static final int BAD_RECTANGLE_$1 = 14;

    /**
     * Illegal transform of type "{0}".
     */
    public static final int BAD_TRANSFORM_$1 = 15;

    /**
     * Multiplication or division of "{0}" by "{1}" not allowed.
     */
    public static final int BAD_UNIT_OPERATION_$2 = 16;

    /**
     * Unit "{1}" can't be raised to power {0}.
     */
    public static final int BAD_UNIT_POWER_$2 = 17;

    /**
     * Bursa wolf parameters required.
     */
    public static final int BURSA_WOLF_PARAMETERS_REQUIRED = 18;

    /**
     * Can't compute derivative.
     */
    public static final int CANT_COMPUTE_DERIVATIVE = 19;

    /**
     * Can't concatenate transforms "{0}" and "{1}".
     */
    public static final int CANT_CONCATENATE_TRANSFORMS_$2 = 20;

    /**
     * Failed to connect to the {0} database.
     */
    public static final int CANT_CONNECT_DATABASE_$1 = 154;

    /**
     * Can't convert value from type '{0}'.
     */
    public static final int CANT_CONVERT_FROM_TYPE_$1 = 21;

    /**
     * Can't create a factory of type "{0}".
     */
    public static final int CANT_CREATE_FACTORY_$1 = 184;

    /**
     * Can't create object of type '{0}' from a text.
     */
    public static final int CANT_CREATE_FROM_TEXT_$1 = 22;

    /**
     * An error occurred while cropping.
     */
    public static final int CANT_CROP = 187;

    /**
     * Can't evaluate a value for coordinate ({0}).
     */
    public static final int CANT_EVALUATE_$1 = 23;

    /**
     * Failed to get the data source for name "{0}".
     */
    public static final int CANT_GET_DATASOURCE_$1 = 155;

    /**
     * Can't read file "{0}".
     */
    public static final int CANT_READ_$1 = 163;

    /**
     * Can't reduce "{0}" to a two-dimensional coordinate system.
     */
    public static final int CANT_REDUCE_TO_TWO_DIMENSIONS_$1 = 24;

    /**
     * Can't reproject grid coverage "{0}".
     */
    public static final int CANT_REPROJECT_$1 = 25;

    /**
     * Can't separate CRS "{0}".
     */
    public static final int CANT_SEPARATE_CRS_$1 = 164;

    /**
     * Can't set a value to the parameter "{0}".
     */
    public static final int CANT_SET_PARAMETER_VALUE_$1 = 177;

    /**
     * Can't transform envelope.
     */
    public static final int CANT_TRANSFORM_ENVELOPE = 26;

    /**
     * Can't transform some points that should be valid.
     */
    public static final int CANT_TRANSFORM_VALID_POINTS = 199;

    /**
     * Graphic "{0}" is owned by an other canvas.
     */
    public static final int CANVAS_NOT_OWNER_$1 = 121;

    /**
     * Axis {0} and {1} are colinear.
     */
    public static final int COLINEAR_AXIS_$2 = 27;

    /**
     * Coverage returned by '{0}' is already the view of coverage "{1}".
     */
    public static final int COVERAGE_ALREADY_BOUND_$2 = 28;

    /**
     * Database failure while creating a '{0}' object for code "{1}".
     */
    public static final int DATABASE_FAILURE_$2 = 165;

    /**
     * Date {0} is outside the range of available data.
     */
    public static final int DATE_OUTSIDE_COVERAGE_$1 = 29;

    /**
     * The destination has not been set.
     */
    public static final int DESTINATION_NOT_SET = 157;

    /**
     * The direction has not been set.
     */
    public static final int DIRECTION_NOT_SET = 158;

    /**
     * The factory has been disposed.
     */
    public static final int DISPOSED_FACTORY = 161;

    /**
     * The distance {0} is out of range ({1} to {2} {3})
     */
    public static final int DISTANCE_OUT_OF_RANGE_$4 = 30;

    /**
     * Duplicated values for code "{0}".
     */
    public static final int DUPLICATED_VALUES_$1 = 31;

    /**
     * Elliptical projection not supported.
     */
    public static final int ELLIPTICAL_NOT_SUPPORTED = 32;

    /**
     * The array should contains at least one element.
     */
    public static final int EMPTY_ARRAY = 33;

    /**
     * Envelope must be at least two-dimensional and non-empty.
     */
    public static final int EMPTY_ENVELOPE = 34;

    /**
     * Premature end of data file
     */
    public static final int END_OF_DATA_FILE = 35;

    /**
     * No factory of kind "{0}" found.
     */
    public static final int FACTORY_NOT_FOUND_$1 = 189;

    /**
     * File does not exist or is unreadable: {0}
     */
    public static final int FILE_DOES_NOT_EXIST_$1 = 36;

    /**
     * File has too few data.
     */
    public static final int FILE_HAS_TOO_FEW_DATA = 37;

    /**
     * Geotools extension required for "{0}" operation.
     */
    public static final int GEOTOOLS_EXTENSION_REQUIRED_$1 = 166;

    /**
     * Latitude and Longitude grid locations are not equal
     */
    public static final int GRID_LOCATIONS_UNEQUAL = 38;

    /**
     * Grid header has unexpected length: {0}
     */
    public static final int HEADER_UNEXPECTED_LENGTH_$1 = 39;

    /**
     * Hole is not inside polygon.
     */
    public static final int HOLE_NOT_INSIDE_POLYGON = 40;

    /**
     * Illegal angle pattern: {0}
     */
    public static final int ILLEGAL_ANGLE_PATTERN_$1 = 41;

    /**
     * Illegal value for argument "{0}".
     */
    public static final int ILLEGAL_ARGUMENT_$1 = 167;

    /**
     * Illegal argument: "{0}={1}".
     */
    public static final int ILLEGAL_ARGUMENT_$2 = 42;

    /**
     * Illegal array length for {0} dimensional points.
     */
    public static final int ILLEGAL_ARRAY_LENGTH_FOR_DIMENSION_$1 = 43;

    /**
     * Axis can't be oriented toward {0} for coordinate system of class "{1}".
     */
    public static final int ILLEGAL_AXIS_ORIENTATION_$2 = 44;

    /**
     * Class '{0}' is illegal. It must be '{1}' or a derivated class.
     */
    public static final int ILLEGAL_CLASS_$2 = 45;

    /**
     * Illegal coordinate reference system.
     */
    public static final int ILLEGAL_COORDINATE_REFERENCE_SYSTEM = 46;

    /**
     * Coordinate system of type '{0}' are incompatible with CRS of type '{1}'.
     */
    public static final int ILLEGAL_COORDINATE_SYSTEM_FOR_CRS_$2 = 168;

    /**
     * Coordinate system can't have {0} dimensions.
     */
    public static final int ILLEGAL_CS_DIMENSION_$1 = 47;

    /**
     * Illegal descriptor for parameter "{0}".
     */
    public static final int ILLEGAL_DESCRIPTOR_FOR_PARAMETER_$1 = 48;

    /**
     * Bad ordinates at dimension {0}.
     */
    public static final int ILLEGAL_ENVELOPE_ORDINATE_$1 = 49;

    /**
     * "{0}" is not a valid identifier.
     */
    public static final int ILLEGAL_IDENTIFIER_$1 = 169;

    /**
     * Illegal instruction "{0}".
     */
    public static final int ILLEGAL_INSTRUCTION_$1 = 170;

    /**
     * Illegal key: {0}
     */
    public static final int ILLEGAL_KEY_$1 = 190;

    /**
     * Illegal matrix size.
     */
    public static final int ILLEGAL_MATRIX_SIZE = 191;

    /**
     * Parameter "{0}" occurs {1} time, while the expected range of occurences was [{2}..{3}].
     */
    public static final int ILLEGAL_OCCURS_FOR_PARAMETER_$4 = 50;

    /**
     * This operation can't be applied to values of class '{0}'.
     */
    public static final int ILLEGAL_OPERATION_FOR_VALUE_CLASS_$1 = 51;

    /**
     * Incompatible coordinate system type.
     */
    public static final int INCOMPATIBLE_COORDINATE_SYSTEM_TYPE = 149;

    /**
     * Projection parameter "{0}" is incompatible with ellipsoid "{1}".
     */
    public static final int INCOMPATIBLE_ELLIPSOID_$2 = 52;

    /**
     * Incompatible grid geometries.
     */
    public static final int INCOMPATIBLE_GRID_GEOMETRY = 53;

    /**
     * Incompatible unit: {0}
     */
    public static final int INCOMPATIBLE_UNIT_$1 = 54;

    /**
     * Direction "{1}" is inconsistent with axis "{0}".
     */
    public static final int INCONSISTENT_AXIS_ORIENTATION_$2 = 60;

    /**
     * Property "{0}" has a value inconsistent with other properties.
     */
    public static final int INCONSISTENT_PROPERTY_$1 = 55;

    /**
     * Index {0} is out of bounds.
     */
    public static final int INDEX_OUT_OF_BOUNDS_$1 = 56;

    /**
     * {0} value is infinite
     */
    public static final int INFINITE_VALUE_$1 = 147;

    /**
     * Inseparable transform.
     */
    public static final int INSEPARABLE_TRANSFORM = 57;

    /**
     * {0} points were specified, while {1} are required.
     */
    public static final int INSUFFICIENT_POINTS_$2 = 198;

    /**
     * This "{0}" object is too complex for WKT syntax.
     */
    public static final int INVALID_WKT_FORMAT_$1 = 159;

    /**
     * Error in "{0}":
     */
    public static final int IN_$1 = 58;

    /**
     * Latitude {0} is out of range (±90°).
     */
    public static final int LATITUDE_OUT_OF_RANGE_$1 = 62;

    /**
     * The line contains {0} columns while only {1} was expected. Characters "{2}" seem to be
     * extra.
     */
    public static final int LINE_TOO_LONG_$3 = 63;

    /**
     * The line contains only {0} columns while {1} was expected.
     */
    public static final int LINE_TOO_SHORT_$2 = 64;

    /**
     * Longitude {0} is out of range (±180°).
     */
    public static final int LONGITUDE_OUT_OF_RANGE_$1 = 65;

    /**
     * Malformed envelope
     */
    public static final int MALFORMED_ENVELOPE = 179;

    /**
     * All rows doesn't have the same length.
     */
    public static final int MATRIX_NOT_REGULAR = 66;

    /**
     * Mismatched array length.
     */
    public static final int MISMATCHED_ARRAY_LENGTH = 67;

    /**
     * The coordinate reference system must be the same for all objects.
     */
    public static final int MISMATCHED_COORDINATE_REFERENCE_SYSTEM = 197;

    /**
     * Mismatched object dimension: {0}D and {1}D.
     */
    public static final int MISMATCHED_DIMENSION_$2 = 68;

    /**
     * Argument "{0}" has {1} dimensions, while {2} was expected.
     */
    public static final int MISMATCHED_DIMENSION_$3 = 69;

    /**
     * The envelope uses an incompatible CRS: was "{1}" while we expected "{0}".
     */
    public static final int MISMATCHED_ENVELOPE_CRS_$2 = 186;

    /**
     * No authority was defined for code "{0}". Did you forget "AUTHORITY:NUMBER"?
     */
    public static final int MISSING_AUTHORITY_$1 = 182;

    /**
     * Character '{0}' was expected.
     */
    public static final int MISSING_CHARACTER_$1 = 70;

    /**
     * This operation requires the "{0}" module.
     */
    public static final int MISSING_MODULE_$1 = 194;

    /**
     * Parameter "{0}" is missing.
     */
    public static final int MISSING_PARAMETER_$1 = 71;

    /**
     * Missing value for parameter "{0}".
     */
    public static final int MISSING_PARAMETER_VALUE_$1 = 72;

    /**
     * Missing WKT definition.
     */
    public static final int MISSING_WKT_DEFINITION = 171;

    /**
     * Geophysics categories mixed with non-geophysics ones.
     */
    public static final int MIXED_CATEGORIES = 73;

    /**
     * Column number for "{0}" ({1}) can't be negative.
     */
    public static final int NEGATIVE_COLUMN_$2 = 74;

    /**
     * Scaling affine transform is not invertible.
     */
    public static final int NONINVERTIBLE_SCALING_TRANSFORM = 188;

    /**
     * Transform is not invertible.
     */
    public static final int NONINVERTIBLE_TRANSFORM = 75;

    /**
     * The grid to coordinate system transform must be affine.
     */
    public static final int NON_AFFINE_TRANSFORM = 76;

    /**
     * Not an angular unit: "{0}".
     */
    public static final int NON_ANGULAR_UNIT_$1 = 77;

    /**
     * Coordinate system "{0}" is not cartesian.
     */
    public static final int NON_CARTESIAN_COORDINATE_SYSTEM_$1 = 78;

    /**
     * Can't convert value from units "{1}" to "{0}".
     */
    public static final int NON_CONVERTIBLE_UNITS_$2 = 79;

    /**
     * Unmatched parenthesis in "{0}": missing '{1}'.
     */
    public static final int NON_EQUILIBRATED_PARENTHESIS_$2 = 80;

    /**
     * Some categories use non-integer sample values.
     */
    public static final int NON_INTEGER_CATEGORY = 81;

    /**
     * Relation is not linear.
     */
    public static final int NON_LINEAR_RELATION = 82;

    /**
     * "{0}" is not a linear unit.
     */
    public static final int NON_LINEAR_UNIT_$1 = 83;

    /**
     * Unit conversion from "{0}" to "{1}" is non-linear.
     */
    public static final int NON_LINEAR_UNIT_CONVERSION_$2 = 150;

    /**
     * Axis directions {0} and {1} are not perpendicular.
     */
    public static final int NON_PERPENDICULAR_AXIS_$2 = 59;

    /**
     * "{0}" is not a scale unit.
     */
    public static final int NON_SCALE_UNIT_$1 = 176;

    /**
     * "{0}" is not a time unit.
     */
    public static final int NON_TEMPORAL_UNIT_$1 = 84;

    /**
     * Transform is not affine.
     */
    public static final int NOT_AN_AFFINE_TRANSFORM = 85;

    /**
     * Can't format object of class "{1}" as an angle.
     */
    public static final int NOT_AN_ANGLE_OBJECT_$1 = 86;

    /**
     * Value "{0}" is not a valid integer.
     */
    public static final int NOT_AN_INTEGER_$1 = 87;

    /**
     * Points dont seem to be distributed on a regular grid.
     */
    public static final int NOT_A_GRID = 88;

    /**
     * Value "{0}" is not a valid real number.
     */
    public static final int NOT_A_NUMBER_$1 = 89;

    /**
     * {0} is not a comparable class.
     */
    public static final int NOT_COMPARABLE_CLASS_$1 = 90;

    /**
     * Value {0} is not a real, non-null number.
     */
    public static final int NOT_DIFFERENT_THAN_ZERO_$1 = 91;

    /**
     * Number {0} is invalid. Expected a number greater than 0.
     */
    public static final int NOT_GREATER_THAN_ZERO_$1 = 92;

    /**
     * Not a 3D coordinate system.
     */
    public static final int NOT_THREE_DIMENSIONAL_CS = 153;

    /**
     * Can't wrap a {0} dimensional object into a 2 dimensional one.
     */
    public static final int NOT_TWO_DIMENSIONAL_$1 = 93;

    /**
     * No category for value {0}.
     */
    public static final int NO_CATEGORY_FOR_VALUE_$1 = 94;

    /**
     * Transformation doesn't convergence.
     */
    public static final int NO_CONVERGENCE = 95;

    /**
     * No convergence for points {0} and {1}.
     */
    public static final int NO_CONVERGENCE_$2 = 96;

    /**
     * No data source found.
     */
    public static final int NO_DATA_SOURCE = 156;

    /**
     * No input set.
     */
    public static final int NO_IMAGE_INPUT = 97;

    /**
     * No output set.
     */
    public static final int NO_IMAGE_OUTPUT = 202;

    /**
     * No suitable image reader for this input.
     */
    public static final int NO_IMAGE_READER = 98;

    /**
     * No suitable image writer for this output.
     */
    public static final int NO_IMAGE_WRITER = 192;

    /**
     * No source axis match {0}.
     */
    public static final int NO_SOURCE_AXIS_$1 = 99;

    /**
     * No object of type "{0}" has been found for code "{1}".
     */
    public static final int NO_SUCH_AUTHORITY_CODE_$2 = 100;

    /**
     * No code "{0}" from authority "{1}" found for object of type "{2}".
     */
    public static final int NO_SUCH_AUTHORITY_CODE_$3 = 162;

    /**
     * No two-dimensional transform available for this geometry.
     */
    public static final int NO_TRANSFORM2D_AVAILABLE = 101;

    /**
     * No transformation available from system "{0}" to "{1}".
     */
    public static final int NO_TRANSFORMATION_PATH_$2 = 102;

    /**
     * No transform for classification "{0}".
     */
    public static final int NO_TRANSFORM_FOR_CLASSIFICATION_$1 = 103;

    /**
     * Unit must be specified.
     */
    public static final int NO_UNIT = 104;

    /**
     * Argument "{0}" should not be null.
     */
    public static final int NULL_ARGUMENT_$1 = 105;

    /**
     * Attribute "{0}" should not be null.
     */
    public static final int NULL_ATTRIBUTE_$1 = 61;

    /**
     * Format #{0} (on {1}) is not defined.
     */
    public static final int NULL_FORMAT_$2 = 106;

    /**
     * "{0}" parameter should be not null and of type "{1}".
     */
    public static final int NULL_PARAMETER_$2 = 185;

    /**
     * Unexpected null value in record "{0}" for the column "{1}" in table "{2}".
     */
    public static final int NULL_VALUE_IN_TABLE_$3 = 107;

    /**
     * The number of image bands ({0}) differs from the number of supplied '{2}' objects ({1}).
     */
    public static final int NUMBER_OF_BANDS_MISMATCH_$3 = 108;

    /**
     * Bad array length: {0}. An even array length was expected.
     */
    public static final int ODD_ARRAY_LENGTH_$1 = 109;

    /**
     * Operation "{0}" is already bounds.
     */
    public static final int OPERATION_ALREADY_BOUNDS_$1 = 110;

    /**
     * Operation "{0}" is already binds in this grid processor.
     */
    public static final int OPERATION_ALREADY_BOUND_$1 = 111;

    /**
     * No such "{0}" operation for this processor.
     */
    public static final int OPERATION_NOT_FOUND_$1 = 112;

    /**
     * Possible use of "{0}" projection outside its valid area.
     */
    public static final int OUT_OF_PROJECTION_VALID_AREA_$1 = 204;

    /**
     * Name or alias for parameter "{0}" at index {1} conflict with name "{2}" at index {3}.
     */
    public static final int PARAMETER_NAME_CLASH_$4 = 113;

    /**
     * Unparsable string: "{0}". Please check characters "{1}".
     */
    public static final int PARSE_EXCEPTION_$2 = 114;

    /**
     * Coordinate ({0}) is outside coverage.
     */
    public static final int POINT_OUTSIDE_COVERAGE_$1 = 115;

    /**
     * Point is outside grid
     */
    public static final int POINT_OUTSIDE_GRID = 116;

    /**
     * Point outside hemisphere of projection.
     */
    public static final int POINT_OUTSIDE_HEMISPHERE = 117;

    /**
     * Latitude {0} is too close to a pole.
     */
    public static final int POLE_PROJECTION_$1 = 118;

    /**
     * Can't add point to a closed polygon.
     */
    public static final int POLYGON_CLOSED = 119;

    /**
     * The transform result may be {0} meters away from the expected position. Are you sure that
     * the input coordinates are inside this map projection area of validity? The point is located
     * {1} away from the central meridian and {2} away from the latitude of origin. The projection
     * is "{3}".
     */
    public static final int PROJECTION_CHECK_FAILED_$4 = 181;

    /**
     * Ranges [{0}..{1}] and [{2}..{3}] overlap.
     */
    public static final int RANGE_OVERLAP_$4 = 120;

    /**
     * Recursive call while creating a '{0}' object.
     */
    public static final int RECURSIVE_CALL_$1 = 193;

    /**
     * Recursive call while creating a '{0}' object for code "{1}".
     */
    public static final int RECURSIVE_CALL_$2 = 172;

    /**
     * RGB value {0} is out of range.
     */
    public static final int RGB_OUT_OF_RANGE_$1 = 122;

    /**
     * Execution on a remote machine failed.
     */
    public static final int RMI_FAILURE = 123;

    /**
     * Expected {0}={1} but got {2}.
     */
    public static final int TEST_FAILURE_$3 = 195;

    /**
     * Tolerance error.
     */
    public static final int TOLERANCE_ERROR = 196;

    /**
     * Too many occurences of "{0}". There is already {1} of them.
     */
    public static final int TOO_MANY_OCCURENCES_$2 = 124;

    /**
     * Undefined property.
     */
    public static final int UNDEFINED_PROPERTY = 125;

    /**
     * Property "{0}" is not defined.
     */
    public static final int UNDEFINED_PROPERTY_$1 = 126;

    /**
     * Unexpected argument for operation "{0}".
     */
    public static final int UNEXPECTED_ARGUMENT_FOR_INSTRUCTION_$1 = 173;

    /**
     * Unexpected dimension for a "{0}" coordinate system.
     */
    public static final int UNEXPECTED_DIMENSION_FOR_CS_$1 = 174;

    /**
     * Unexpected end of string.
     */
    public static final int UNEXPECTED_END_OF_STRING = 127;

    /**
     * Image doesn't have the expected size.
     */
    public static final int UNEXPECTED_IMAGE_SIZE = 128;

    /**
     * Parameter "{0}" was not expected.
     */
    public static final int UNEXPECTED_PARAMETER_$1 = 129;

    /**
     * Matrix row {0} has a length of {1}, while {2} was expected.
     */
    public static final int UNEXPECTED_ROW_LENGTH_$3 = 130;

    /**
     * Transformation doesn't produce the expected values.
     */
    public static final int UNEXPECTED_TRANSFORM_RESULT = 175;

    /**
     * Parameter "{0}" has no unit.
     */
    public static final int UNITLESS_PARAMETER_$1 = 131;

    /**
     * Authority "{0}" is unknown or doesn't match the supplied hints. Maybe it is defined in an
     * unreachable JAR file?
     */
    public static final int UNKNOW_AUTHORITY_$1 = 183;

    /**
     * Unknow axis direction: "{0}".
     */
    public static final int UNKNOW_AXIS_DIRECTION_$1 = 152;

    /**
     * Image format "{0}" is unknown.
     */
    public static final int UNKNOW_IMAGE_FORMAT_$1 = 203;

    /**
     * Interpolation "{0}" is unknown.
     */
    public static final int UNKNOW_INTERPOLATION_$1 = 132;

    /**
     * Unknow parameter: {0}
     */
    public static final int UNKNOW_PARAMETER_$1 = 133;

    /**
     * Unknow parameter name: {0}
     */
    public static final int UNKNOW_PARAMETER_NAME_$1 = 134;

    /**
     * Unknow projection type.
     */
    public static final int UNKNOW_PROJECTION_TYPE = 160;

    /**
     * Type "{0}" is unknow in this context.
     */
    public static final int UNKNOW_TYPE_$1 = 135;

    /**
     * This affine transform is unmodifiable.
     */
    public static final int UNMODIFIABLE_AFFINE_TRANSFORM = 136;

    /**
     * Unmodifiable geometry.
     */
    public static final int UNMODIFIABLE_GEOMETRY = 137;

    /**
     * Unmodifiable metadata.
     */
    public static final int UNMODIFIABLE_METADATA = 200;

    /**
     * Can't parse "{0}" as a number.
     */
    public static final int UNPARSABLE_NUMBER_$1 = 138;

    /**
     * Can't parse "{0}" because "{1}" is unrecognized.
     */
    public static final int UNPARSABLE_STRING_$2 = 139;

    /**
     * Coordinate reference system is unspecified.
     */
    public static final int UNSPECIFIED_CRS = 140;

    /**
     * Unspecified image's size.
     */
    public static final int UNSPECIFIED_IMAGE_SIZE = 141;

    /**
     * Unspecified coordinates transform.
     */
    public static final int UNSPECIFIED_TRANSFORM = 178;

    /**
     * Coordinate system "{0}" is unsupported.
     */
    public static final int UNSUPPORTED_COORDINATE_SYSTEM_$1 = 151;

    /**
     * Coordinate reference system "{0}" is unsupported.
     */
    public static final int UNSUPPORTED_CRS_$1 = 180;

    /**
     * Unsupported data type.
     */
    public static final int UNSUPPORTED_DATA_TYPE = 142;

    /**
     * Data type "{0}" is not supported.
     */
    public static final int UNSUPPORTED_DATA_TYPE_$1 = 143;

    /**
     * Unsupported file type: {0} or {1}
     */
    public static final int UNSUPPORTED_FILE_TYPE_$2 = 144;

    /**
     * Value {0} is out of range [{1}..{2}].
     */
    public static final int VALUE_OUT_OF_BOUNDS_$3 = 145;

    /**
     * Numerical value tend toward infinity.
     */
    public static final int VALUE_TEND_TOWARD_INFINITY = 146;

    /**
     * No variable "{0}" found in file "{1}".
     */
    public static final int VARIABLE_NOT_FOUND_IN_FILE_$2 = 201;

    /**
     * Value {1} is outside the domain of coverage "{0}".
     */
    public static final int ZVALUE_OUTSIDE_COVERAGE_$2 = 148;
}
