/**
 * <copyright>
 * </copyright>
 *
 * $Id: WpsPackage.java 30810 2008-06-25 17:29:43Z jdeolive $
 */
package net.opengis.wps;

import net.opengis.ows11.Ows11Package;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * 
 * 			<description>This XML Schema Document encodes the WPS GetCapabilities operation response.</description>
 * 			<copyright>Copyright (c) 2007 OGC, All Rights Reserved.
 * For conditions, see OGC Software Notice http://www.opengeospatial.org/ogc/software</copyright>
 * 
 * 
 * 			<description>Location of a WSDL document.</description>
 * 			<copyright>Copyright (c) 2007 OGC, All Rights Reserved.
 * For conditions, see OGC Software Notice http://www.opengeospatial.org/ogc/software</copyright>
 * 
 * 
 * 			<description>Brief description of a Process, designed for Process discovery. </description>
 * 			<copyright>Copyright (c) 2007 OGC, All Rights Reserved.
 * For conditions, see OGC Software Notice http://www.opengeospatial.org/ogc/software</copyright>
 * 
 * 
 * 			<description>This XML Schema Document encodes elements and types that are shared by multiple WPS operations.</description>
 * 			<copyright>Copyright (c) 2007 OGC, All Rights Reserved.
 * For conditions, see OGC Software Notice http://www.opengeospatial.org/ogc/software</copyright>
 * 
 * This XML Schema Document includes and imports, directly and indirectly, all the XML Schemas defined by the OWS Common Implemetation Specification.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the GetResourceByID operation request message. This typical operation is specified as a base for profiling in specific OWS specifications. For information on the allowed changes and limitations in such profiling, see Subclause 9.4.1 of the OWS Common specification.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the parts of the MD_DataIdentification class of ISO 19115 (OGC Abstract Specification Topic 11) which are expected to be used for most datasets. This Schema also encodes the parts of this class that are expected to be useful for other metadata. Both may be used within the Contents section of OWS service metadata (Capabilities) documents.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes various parameters and parameter types that can be used in OWS operation requests and responses.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the parts of ISO 19115 used by the common "ServiceIdentification" and "ServiceProvider" sections of the GetCapabilities operation response, known as the service metadata XML document. The parts encoded here are the MD_Keywords, CI_ResponsibleParty, and related classes. The UML package prefixes were omitted from XML names, and the XML element names were all capitalized, for consistency with other OWS Schemas. This document also provides a simple coding of text in multiple languages, simplified from Annex J of ISO 19115.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document defines the GetCapabilities operation request and response XML elements and types, which are common to all OWSs. This XML Schema shall be edited by each OWS, for example, to specify a specific value for the "service" attribute.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the common "ServiceIdentification" section of the GetCapabilities operation response, known as the Capabilities XML document. This section encodes the SV_ServiceIdentification class of ISO 19119 (OGC Abstract Specification Topic 12).
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the common "ServiceProvider" section of the GetCapabilities operation response, known as the Capabilities XML document. This section encodes the SV_ServiceProvider class of ISO 19119 (OGC Abstract Specification Topic 12).
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the basic contents of the "OperationsMetadata" section of the GetCapabilities operation response, also known as the Capabilities XML document.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the allowed values (or domain) of a quantity, often for an input or output parameter to an OWS. Such a parameter is sometimes called a variable, quantity, literal, or typed literal. Such a parameter can use one of many data types, including double, integer, boolean, string, or URI. The allowed values can also be encoded for a quantity that is not explicit or not transferred, but is constrained by a server implementation.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document encodes the Exception Report response to all OWS operations.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema  Document encodes the typical Contents section of an OWS service metadata (Capabilities) document. This  Schema can be built upon to define the Contents section for a specific OWS. If the ContentsBaseType in this XML Schema cannot be restricted and extended to define the Contents section for a specific OWS, all other relevant parts defined in owsContents.xsd shall be used by the �ContentsType� in the wxsContents.xsd prepared for the specific OWS.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document specifies types and elements for input and output of operation data, allowing including multiple data items with each data item either included or referenced. The contents of each type and element specified here can be restricted and/or extended for each use in a specific OWS specification.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * This XML Schema Document specifies types and elements for document or resource references and for package manifests that contain multiple references. The contents of each type and element specified here can be restricted and/or extended for each use in a specific OWS specification.
 * 		Copyright (c) 2006 Open Geospatial Consortium, Inc. All Rights Reserved.
 * 
 * 			<description>This XML Schema Document encodes elements and types that are shared by multiple WPS operations.</description>
 * 			<copyright>Copyright (c) 2007 OGC, All Rights Reserved.
 * For conditions, see OGC Software Notice http://www.opengeospatial.org/ogc/software</copyright>
 * <!-- end-model-doc -->
 * @see net.opengis.wps.WpsFactory
 * @model kind="package"
 * @generated
 */
public interface WpsPackage extends EPackage {
	/**
     * The package name.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	String eNAME = "wps";

	/**
     * The package namespace URI.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	String eNS_URI = "http://www.opengis.net/wps/1.0.0";

	/**
     * The package namespace name.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	String eNS_PREFIX = "wps";

	/**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	WpsPackage eINSTANCE = net.opengis.wps.impl.WpsPackageImpl.init();

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.BodyReferenceTypeImpl <em>Body Reference Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.BodyReferenceTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getBodyReferenceType()
     * @generated
     */
	int BODY_REFERENCE_TYPE = 0;

	/**
     * The feature id for the '<em><b>Href</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int BODY_REFERENCE_TYPE__HREF = 0;

	/**
     * The number of structural features of the '<em>Body Reference Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int BODY_REFERENCE_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ComplexDataCombinationsTypeImpl <em>Complex Data Combinations Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ComplexDataCombinationsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getComplexDataCombinationsType()
     * @generated
     */
	int COMPLEX_DATA_COMBINATIONS_TYPE = 1;

	/**
     * The feature id for the '<em><b>Format</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_COMBINATIONS_TYPE__FORMAT = 0;

	/**
     * The number of structural features of the '<em>Complex Data Combinations Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_COMBINATIONS_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ComplexDataCombinationTypeImpl <em>Complex Data Combination Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ComplexDataCombinationTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getComplexDataCombinationType()
     * @generated
     */
	int COMPLEX_DATA_COMBINATION_TYPE = 2;

	/**
     * The feature id for the '<em><b>Format</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_COMBINATION_TYPE__FORMAT = 0;

	/**
     * The number of structural features of the '<em>Complex Data Combination Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_COMBINATION_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ComplexDataDescriptionTypeImpl <em>Complex Data Description Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ComplexDataDescriptionTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getComplexDataDescriptionType()
     * @generated
     */
	int COMPLEX_DATA_DESCRIPTION_TYPE = 3;

	/**
     * The feature id for the '<em><b>Mime Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_DESCRIPTION_TYPE__MIME_TYPE = 0;

	/**
     * The feature id for the '<em><b>Encoding</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_DESCRIPTION_TYPE__ENCODING = 1;

	/**
     * The feature id for the '<em><b>Schema</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_DESCRIPTION_TYPE__SCHEMA = 2;

	/**
     * The number of structural features of the '<em>Complex Data Description Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_DESCRIPTION_TYPE_FEATURE_COUNT = 3;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ComplexDataTypeImpl <em>Complex Data Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ComplexDataTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getComplexDataType()
     * @generated
     */
	int COMPLEX_DATA_TYPE = 4;

	/**
     * The feature id for the '<em><b>Mixed</b></em>' attribute list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_TYPE__MIXED = 0;

	/**
     * The feature id for the '<em><b>Encoding</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_TYPE__ENCODING = 1;

	/**
     * The feature id for the '<em><b>Mime Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_TYPE__MIME_TYPE = 2;

	/**
     * The feature id for the '<em><b>Schema</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_TYPE__SCHEMA = 3;

	/**
     * The feature id for the '<em><b>Data</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPLEX_DATA_TYPE__DATA = 4;

    /**
     * The number of structural features of the '<em>Complex Data Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int COMPLEX_DATA_TYPE_FEATURE_COUNT = 5;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.CRSsTypeImpl <em>CR Ss Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.CRSsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getCRSsType()
     * @generated
     */
	int CR_SS_TYPE = 5;

	/**
     * The feature id for the '<em><b>CRS</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int CR_SS_TYPE__CRS = 0;

	/**
     * The number of structural features of the '<em>CR Ss Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int CR_SS_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DataInputsTypeImpl <em>Data Inputs Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DataInputsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDataInputsType()
     * @generated
     */
	int DATA_INPUTS_TYPE = 6;

	/**
     * The feature id for the '<em><b>Input</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_INPUTS_TYPE__INPUT = 0;

	/**
     * The number of structural features of the '<em>Data Inputs Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_INPUTS_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DataInputsType1Impl <em>Data Inputs Type1</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DataInputsType1Impl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDataInputsType1()
     * @generated
     */
	int DATA_INPUTS_TYPE1 = 7;

	/**
     * The feature id for the '<em><b>Input</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_INPUTS_TYPE1__INPUT = 0;

	/**
     * The number of structural features of the '<em>Data Inputs Type1</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_INPUTS_TYPE1_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DataTypeImpl <em>Data Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DataTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDataType()
     * @generated
     */
	int DATA_TYPE = 8;

	/**
     * The feature id for the '<em><b>Complex Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_TYPE__COMPLEX_DATA = 0;

	/**
     * The feature id for the '<em><b>Literal Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_TYPE__LITERAL_DATA = 1;

	/**
     * The feature id for the '<em><b>Bounding Box Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_TYPE__BOUNDING_BOX_DATA = 2;

	/**
     * The number of structural features of the '<em>Data Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DATA_TYPE_FEATURE_COUNT = 3;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DefaultTypeImpl <em>Default Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DefaultTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDefaultType()
     * @generated
     */
	int DEFAULT_TYPE = 9;

	/**
     * The feature id for the '<em><b>CRS</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DEFAULT_TYPE__CRS = 0;

	/**
     * The number of structural features of the '<em>Default Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DEFAULT_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DefaultType1Impl <em>Default Type1</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DefaultType1Impl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDefaultType1()
     * @generated
     */
	int DEFAULT_TYPE1 = 10;

	/**
     * The feature id for the '<em><b>UOM</b></em>' reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DEFAULT_TYPE1__UOM = 0;

	/**
     * The number of structural features of the '<em>Default Type1</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DEFAULT_TYPE1_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DefaultType2Impl <em>Default Type2</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DefaultType2Impl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDefaultType2()
     * @generated
     */
	int DEFAULT_TYPE2 = 11;

	/**
     * The feature id for the '<em><b>Language</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DEFAULT_TYPE2__LANGUAGE = 0;

	/**
     * The number of structural features of the '<em>Default Type2</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DEFAULT_TYPE2_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.RequestBaseTypeImpl <em>Request Base Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.RequestBaseTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getRequestBaseType()
     * @generated
     */
	int REQUEST_BASE_TYPE = 41;

	/**
     * The feature id for the '<em><b>Language</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int REQUEST_BASE_TYPE__LANGUAGE = 0;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int REQUEST_BASE_TYPE__SERVICE = 1;

	/**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int REQUEST_BASE_TYPE__VERSION = 2;

	/**
     * The feature id for the '<em><b>Base Url</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int REQUEST_BASE_TYPE__BASE_URL = 3;

	/**
     * The number of structural features of the '<em>Request Base Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int REQUEST_BASE_TYPE_FEATURE_COUNT = 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DescribeProcessTypeImpl <em>Describe Process Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DescribeProcessTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDescribeProcessType()
     * @generated
     */
	int DESCRIBE_PROCESS_TYPE = 12;

	/**
     * The feature id for the '<em><b>Language</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIBE_PROCESS_TYPE__LANGUAGE = REQUEST_BASE_TYPE__LANGUAGE;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIBE_PROCESS_TYPE__SERVICE = REQUEST_BASE_TYPE__SERVICE;

	/**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIBE_PROCESS_TYPE__VERSION = REQUEST_BASE_TYPE__VERSION;

	/**
     * The feature id for the '<em><b>Base Url</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIBE_PROCESS_TYPE__BASE_URL = REQUEST_BASE_TYPE__BASE_URL;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIBE_PROCESS_TYPE__IDENTIFIER = REQUEST_BASE_TYPE_FEATURE_COUNT + 0;

	/**
     * The number of structural features of the '<em>Describe Process Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIBE_PROCESS_TYPE_FEATURE_COUNT = REQUEST_BASE_TYPE_FEATURE_COUNT + 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DescriptionTypeImpl <em>Description Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DescriptionTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDescriptionType()
     * @generated
     */
	int DESCRIPTION_TYPE = 13;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIPTION_TYPE__IDENTIFIER = 0;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIPTION_TYPE__TITLE = 1;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIPTION_TYPE__ABSTRACT = 2;

	/**
     * The feature id for the '<em><b>Metadata</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIPTION_TYPE__METADATA = 3;

	/**
     * The number of structural features of the '<em>Description Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DESCRIPTION_TYPE_FEATURE_COUNT = 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.OutputDefinitionTypeImpl <em>Output Definition Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.OutputDefinitionTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getOutputDefinitionType()
     * @generated
     */
	int OUTPUT_DEFINITION_TYPE = 30;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITION_TYPE__IDENTIFIER = 0;

	/**
     * The feature id for the '<em><b>Encoding</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITION_TYPE__ENCODING = 1;

	/**
     * The feature id for the '<em><b>Mime Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITION_TYPE__MIME_TYPE = 2;

	/**
     * The feature id for the '<em><b>Schema</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITION_TYPE__SCHEMA = 3;

	/**
     * The feature id for the '<em><b>Uom</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITION_TYPE__UOM = 4;

	/**
     * The number of structural features of the '<em>Output Definition Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITION_TYPE_FEATURE_COUNT = 5;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DocumentOutputDefinitionTypeImpl <em>Document Output Definition Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DocumentOutputDefinitionTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDocumentOutputDefinitionType()
     * @generated
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE = 14;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__IDENTIFIER = OUTPUT_DEFINITION_TYPE__IDENTIFIER;

	/**
     * The feature id for the '<em><b>Encoding</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__ENCODING = OUTPUT_DEFINITION_TYPE__ENCODING;

	/**
     * The feature id for the '<em><b>Mime Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__MIME_TYPE = OUTPUT_DEFINITION_TYPE__MIME_TYPE;

	/**
     * The feature id for the '<em><b>Schema</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__SCHEMA = OUTPUT_DEFINITION_TYPE__SCHEMA;

	/**
     * The feature id for the '<em><b>Uom</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__UOM = OUTPUT_DEFINITION_TYPE__UOM;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__TITLE = OUTPUT_DEFINITION_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__ABSTRACT = OUTPUT_DEFINITION_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>As Reference</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE__AS_REFERENCE = OUTPUT_DEFINITION_TYPE_FEATURE_COUNT + 2;

	/**
     * The number of structural features of the '<em>Document Output Definition Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_OUTPUT_DEFINITION_TYPE_FEATURE_COUNT = OUTPUT_DEFINITION_TYPE_FEATURE_COUNT + 3;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.DocumentRootImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getDocumentRoot()
     * @generated
     */
	int DOCUMENT_ROOT = 15;

	/**
     * The feature id for the '<em><b>Mixed</b></em>' attribute list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__MIXED = 0;

	/**
     * The feature id for the '<em><b>XMLNS Prefix Map</b></em>' map.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__XMLNS_PREFIX_MAP = 1;

	/**
     * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = 2;

	/**
     * The feature id for the '<em><b>Capabilities</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__CAPABILITIES = 3;

	/**
     * The feature id for the '<em><b>Describe Process</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__DESCRIBE_PROCESS = 4;

	/**
     * The feature id for the '<em><b>Execute</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__EXECUTE = 5;

	/**
     * The feature id for the '<em><b>Execute Response</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__EXECUTE_RESPONSE = 6;

	/**
     * The feature id for the '<em><b>Get Capabilities</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__GET_CAPABILITIES = 7;

	/**
     * The feature id for the '<em><b>Languages</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__LANGUAGES = 8;

	/**
     * The feature id for the '<em><b>Process Descriptions</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__PROCESS_DESCRIPTIONS = 9;

	/**
     * The feature id for the '<em><b>Process Offerings</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__PROCESS_OFFERINGS = 10;

	/**
     * The feature id for the '<em><b>WSDL</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__WSDL = 11;

	/**
     * The feature id for the '<em><b>Process Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT__PROCESS_VERSION = 12;

	/**
     * The number of structural features of the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int DOCUMENT_ROOT_FEATURE_COUNT = 13;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ResponseBaseTypeImpl <em>Response Base Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ResponseBaseTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getResponseBaseType()
     * @generated
     */
	int RESPONSE_BASE_TYPE = 42;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_BASE_TYPE__SERVICE = 0;

	/**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_BASE_TYPE__VERSION = 1;

	/**
     * The number of structural features of the '<em>Response Base Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_BASE_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ExecuteResponseTypeImpl <em>Execute Response Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ExecuteResponseTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getExecuteResponseType()
     * @generated
     */
	int EXECUTE_RESPONSE_TYPE = 16;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__SERVICE = RESPONSE_BASE_TYPE__SERVICE;

	/**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__VERSION = RESPONSE_BASE_TYPE__VERSION;

	/**
     * The feature id for the '<em><b>Process</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__PROCESS = RESPONSE_BASE_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Status</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__STATUS = RESPONSE_BASE_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>Data Inputs</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__DATA_INPUTS = RESPONSE_BASE_TYPE_FEATURE_COUNT + 2;

	/**
     * The feature id for the '<em><b>Output Definitions</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS = RESPONSE_BASE_TYPE_FEATURE_COUNT + 3;

	/**
     * The feature id for the '<em><b>Process Outputs</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS = RESPONSE_BASE_TYPE_FEATURE_COUNT + 4;

	/**
     * The feature id for the '<em><b>Service Instance</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__SERVICE_INSTANCE = RESPONSE_BASE_TYPE_FEATURE_COUNT + 5;

	/**
     * The feature id for the '<em><b>Status Location</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE__STATUS_LOCATION = RESPONSE_BASE_TYPE_FEATURE_COUNT + 6;

	/**
     * The number of structural features of the '<em>Execute Response Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_RESPONSE_TYPE_FEATURE_COUNT = RESPONSE_BASE_TYPE_FEATURE_COUNT + 7;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ExecuteTypeImpl <em>Execute Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ExecuteTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getExecuteType()
     * @generated
     */
	int EXECUTE_TYPE = 17;

	/**
     * The feature id for the '<em><b>Language</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE__LANGUAGE = REQUEST_BASE_TYPE__LANGUAGE;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE__SERVICE = REQUEST_BASE_TYPE__SERVICE;

	/**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE__VERSION = REQUEST_BASE_TYPE__VERSION;

	/**
     * The feature id for the '<em><b>Base Url</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE__BASE_URL = REQUEST_BASE_TYPE__BASE_URL;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE__IDENTIFIER = REQUEST_BASE_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Data Inputs</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE__DATA_INPUTS = REQUEST_BASE_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>Response Form</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE__RESPONSE_FORM = REQUEST_BASE_TYPE_FEATURE_COUNT + 2;

	/**
     * The number of structural features of the '<em>Execute Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int EXECUTE_TYPE_FEATURE_COUNT = REQUEST_BASE_TYPE_FEATURE_COUNT + 3;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.GetCapabilitiesTypeImpl <em>Get Capabilities Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.GetCapabilitiesTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getGetCapabilitiesType()
     * @generated
     */
	int GET_CAPABILITIES_TYPE = 18;

	/**
     * The feature id for the '<em><b>Accept Versions</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS = 0;

	/**
     * The feature id for the '<em><b>Language</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int GET_CAPABILITIES_TYPE__LANGUAGE = 1;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int GET_CAPABILITIES_TYPE__SERVICE = 2;

	/**
     * The feature id for the '<em><b>Base Url</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int GET_CAPABILITIES_TYPE__BASE_URL = 3;

	/**
     * The number of structural features of the '<em>Get Capabilities Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int GET_CAPABILITIES_TYPE_FEATURE_COUNT = 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.HeaderTypeImpl <em>Header Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.HeaderTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getHeaderType()
     * @generated
     */
	int HEADER_TYPE = 19;

	/**
     * The feature id for the '<em><b>Key</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int HEADER_TYPE__KEY = 0;

	/**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int HEADER_TYPE__VALUE = 1;

	/**
     * The number of structural features of the '<em>Header Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int HEADER_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.InputDescriptionTypeImpl <em>Input Description Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.InputDescriptionTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getInputDescriptionType()
     * @generated
     */
	int INPUT_DESCRIPTION_TYPE = 20;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__IDENTIFIER = DESCRIPTION_TYPE__IDENTIFIER;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__TITLE = DESCRIPTION_TYPE__TITLE;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__ABSTRACT = DESCRIPTION_TYPE__ABSTRACT;

	/**
     * The feature id for the '<em><b>Metadata</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__METADATA = DESCRIPTION_TYPE__METADATA;

	/**
     * The feature id for the '<em><b>Complex Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__COMPLEX_DATA = DESCRIPTION_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Literal Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__LITERAL_DATA = DESCRIPTION_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>Bounding Box Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__BOUNDING_BOX_DATA = DESCRIPTION_TYPE_FEATURE_COUNT + 2;

	/**
     * The feature id for the '<em><b>Max Occurs</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__MAX_OCCURS = DESCRIPTION_TYPE_FEATURE_COUNT + 3;

	/**
     * The feature id for the '<em><b>Min Occurs</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE__MIN_OCCURS = DESCRIPTION_TYPE_FEATURE_COUNT + 4;

	/**
     * The number of structural features of the '<em>Input Description Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_DESCRIPTION_TYPE_FEATURE_COUNT = DESCRIPTION_TYPE_FEATURE_COUNT + 5;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.InputReferenceTypeImpl <em>Input Reference Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.InputReferenceTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getInputReferenceType()
     * @generated
     */
	int INPUT_REFERENCE_TYPE = 21;

	/**
     * The feature id for the '<em><b>Header</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__HEADER = 0;

	/**
     * The feature id for the '<em><b>Body</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__BODY = 1;

	/**
     * The feature id for the '<em><b>Body Reference</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__BODY_REFERENCE = 2;

	/**
     * The feature id for the '<em><b>Encoding</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__ENCODING = 3;

	/**
     * The feature id for the '<em><b>Href</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__HREF = 4;

	/**
     * The feature id for the '<em><b>Method</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__METHOD = 5;

	/**
     * The feature id for the '<em><b>Mime Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__MIME_TYPE = 6;

	/**
     * The feature id for the '<em><b>Schema</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE__SCHEMA = 7;

	/**
     * The number of structural features of the '<em>Input Reference Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_REFERENCE_TYPE_FEATURE_COUNT = 8;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.InputTypeImpl <em>Input Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.InputTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getInputType()
     * @generated
     */
	int INPUT_TYPE = 22;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_TYPE__IDENTIFIER = 0;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_TYPE__TITLE = 1;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_TYPE__ABSTRACT = 2;

	/**
     * The feature id for the '<em><b>Reference</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_TYPE__REFERENCE = 3;

	/**
     * The feature id for the '<em><b>Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_TYPE__DATA = 4;

	/**
     * The number of structural features of the '<em>Input Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int INPUT_TYPE_FEATURE_COUNT = 5;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.LanguagesTypeImpl <em>Languages Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.LanguagesTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getLanguagesType()
     * @generated
     */
	int LANGUAGES_TYPE = 23;

	/**
     * The feature id for the '<em><b>Language</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LANGUAGES_TYPE__LANGUAGE = 0;

	/**
     * The number of structural features of the '<em>Languages Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LANGUAGES_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.LanguagesType1Impl <em>Languages Type1</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.LanguagesType1Impl
     * @see net.opengis.wps.impl.WpsPackageImpl#getLanguagesType1()
     * @generated
     */
	int LANGUAGES_TYPE1 = 24;

	/**
     * The feature id for the '<em><b>Default</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LANGUAGES_TYPE1__DEFAULT = 0;

	/**
     * The feature id for the '<em><b>Supported</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LANGUAGES_TYPE1__SUPPORTED = 1;

	/**
     * The number of structural features of the '<em>Languages Type1</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LANGUAGES_TYPE1_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.LiteralDataTypeImpl <em>Literal Data Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.LiteralDataTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getLiteralDataType()
     * @generated
     */
	int LITERAL_DATA_TYPE = 25;

	/**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_DATA_TYPE__VALUE = 0;

	/**
     * The feature id for the '<em><b>Data Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_DATA_TYPE__DATA_TYPE = 1;

	/**
     * The feature id for the '<em><b>Uom</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_DATA_TYPE__UOM = 2;

	/**
     * The number of structural features of the '<em>Literal Data Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_DATA_TYPE_FEATURE_COUNT = 3;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.LiteralOutputTypeImpl <em>Literal Output Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.LiteralOutputTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getLiteralOutputType()
     * @generated
     */
	int LITERAL_OUTPUT_TYPE = 27;

	/**
     * The feature id for the '<em><b>Data Type</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_OUTPUT_TYPE__DATA_TYPE = 0;

	/**
     * The feature id for the '<em><b>UO Ms</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_OUTPUT_TYPE__UO_MS = 1;

	/**
     * The number of structural features of the '<em>Literal Output Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_OUTPUT_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.LiteralInputTypeImpl <em>Literal Input Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.LiteralInputTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getLiteralInputType()
     * @generated
     */
	int LITERAL_INPUT_TYPE = 26;

	/**
     * The feature id for the '<em><b>Data Type</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_INPUT_TYPE__DATA_TYPE = LITERAL_OUTPUT_TYPE__DATA_TYPE;

	/**
     * The feature id for the '<em><b>UO Ms</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_INPUT_TYPE__UO_MS = LITERAL_OUTPUT_TYPE__UO_MS;

	/**
     * The feature id for the '<em><b>Allowed Values</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_INPUT_TYPE__ALLOWED_VALUES = LITERAL_OUTPUT_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Any Value</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_INPUT_TYPE__ANY_VALUE = LITERAL_OUTPUT_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>Values Reference</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_INPUT_TYPE__VALUES_REFERENCE = LITERAL_OUTPUT_TYPE_FEATURE_COUNT + 2;

	/**
     * The feature id for the '<em><b>Default Value</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_INPUT_TYPE__DEFAULT_VALUE = LITERAL_OUTPUT_TYPE_FEATURE_COUNT + 3;

	/**
     * The number of structural features of the '<em>Literal Input Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int LITERAL_INPUT_TYPE_FEATURE_COUNT = LITERAL_OUTPUT_TYPE_FEATURE_COUNT + 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.OutputDataTypeImpl <em>Output Data Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.OutputDataTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getOutputDataType()
     * @generated
     */
	int OUTPUT_DATA_TYPE = 28;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DATA_TYPE__IDENTIFIER = DESCRIPTION_TYPE__IDENTIFIER;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DATA_TYPE__TITLE = DESCRIPTION_TYPE__TITLE;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DATA_TYPE__ABSTRACT = DESCRIPTION_TYPE__ABSTRACT;

	/**
     * The feature id for the '<em><b>Metadata</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DATA_TYPE__METADATA = DESCRIPTION_TYPE__METADATA;

	/**
     * The feature id for the '<em><b>Reference</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DATA_TYPE__REFERENCE = DESCRIPTION_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Data</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DATA_TYPE__DATA = DESCRIPTION_TYPE_FEATURE_COUNT + 1;

	/**
     * The number of structural features of the '<em>Output Data Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DATA_TYPE_FEATURE_COUNT = DESCRIPTION_TYPE_FEATURE_COUNT + 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.OutputDefinitionsTypeImpl <em>Output Definitions Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.OutputDefinitionsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getOutputDefinitionsType()
     * @generated
     */
	int OUTPUT_DEFINITIONS_TYPE = 29;

	/**
     * The feature id for the '<em><b>Output</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITIONS_TYPE__OUTPUT = 0;

	/**
     * The number of structural features of the '<em>Output Definitions Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DEFINITIONS_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.OutputDescriptionTypeImpl <em>Output Description Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.OutputDescriptionTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getOutputDescriptionType()
     * @generated
     */
	int OUTPUT_DESCRIPTION_TYPE = 31;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE__IDENTIFIER = DESCRIPTION_TYPE__IDENTIFIER;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE__TITLE = DESCRIPTION_TYPE__TITLE;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE__ABSTRACT = DESCRIPTION_TYPE__ABSTRACT;

	/**
     * The feature id for the '<em><b>Metadata</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE__METADATA = DESCRIPTION_TYPE__METADATA;

	/**
     * The feature id for the '<em><b>Complex Output</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT = DESCRIPTION_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Literal Output</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT = DESCRIPTION_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>Bounding Box Output</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT = DESCRIPTION_TYPE_FEATURE_COUNT + 2;

	/**
     * The number of structural features of the '<em>Output Description Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_DESCRIPTION_TYPE_FEATURE_COUNT = DESCRIPTION_TYPE_FEATURE_COUNT + 3;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.OutputReferenceTypeImpl <em>Output Reference Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.OutputReferenceTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getOutputReferenceType()
     * @generated
     */
	int OUTPUT_REFERENCE_TYPE = 32;

	/**
     * The feature id for the '<em><b>Encoding</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_REFERENCE_TYPE__ENCODING = 0;

	/**
     * The feature id for the '<em><b>Href</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_REFERENCE_TYPE__HREF = 1;

	/**
     * The feature id for the '<em><b>Mime Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_REFERENCE_TYPE__MIME_TYPE = 2;

	/**
     * The feature id for the '<em><b>Schema</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_REFERENCE_TYPE__SCHEMA = 3;

	/**
     * The number of structural features of the '<em>Output Reference Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int OUTPUT_REFERENCE_TYPE_FEATURE_COUNT = 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessBriefTypeImpl <em>Process Brief Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessBriefTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessBriefType()
     * @generated
     */
	int PROCESS_BRIEF_TYPE = 33;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE__IDENTIFIER = DESCRIPTION_TYPE__IDENTIFIER;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE__TITLE = DESCRIPTION_TYPE__TITLE;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE__ABSTRACT = DESCRIPTION_TYPE__ABSTRACT;

	/**
     * The feature id for the '<em><b>Metadata</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE__METADATA = DESCRIPTION_TYPE__METADATA;

	/**
     * The feature id for the '<em><b>Profile</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE__PROFILE = DESCRIPTION_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>WSDL</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE__WSDL = DESCRIPTION_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>Process Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE__PROCESS_VERSION = DESCRIPTION_TYPE_FEATURE_COUNT + 2;

	/**
     * The number of structural features of the '<em>Process Brief Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_BRIEF_TYPE_FEATURE_COUNT = DESCRIPTION_TYPE_FEATURE_COUNT + 3;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessDescriptionsTypeImpl <em>Process Descriptions Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessDescriptionsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessDescriptionsType()
     * @generated
     */
	int PROCESS_DESCRIPTIONS_TYPE = 34;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTIONS_TYPE__SERVICE = RESPONSE_BASE_TYPE__SERVICE;

	/**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTIONS_TYPE__VERSION = RESPONSE_BASE_TYPE__VERSION;

	/**
     * The feature id for the '<em><b>Process Description</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTIONS_TYPE__PROCESS_DESCRIPTION = RESPONSE_BASE_TYPE_FEATURE_COUNT + 0;

	/**
     * The number of structural features of the '<em>Process Descriptions Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTIONS_TYPE_FEATURE_COUNT = RESPONSE_BASE_TYPE_FEATURE_COUNT + 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessDescriptionTypeImpl <em>Process Description Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessDescriptionTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessDescriptionType()
     * @generated
     */
	int PROCESS_DESCRIPTION_TYPE = 35;

	/**
     * The feature id for the '<em><b>Identifier</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__IDENTIFIER = PROCESS_BRIEF_TYPE__IDENTIFIER;

	/**
     * The feature id for the '<em><b>Title</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__TITLE = PROCESS_BRIEF_TYPE__TITLE;

	/**
     * The feature id for the '<em><b>Abstract</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__ABSTRACT = PROCESS_BRIEF_TYPE__ABSTRACT;

	/**
     * The feature id for the '<em><b>Metadata</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__METADATA = PROCESS_BRIEF_TYPE__METADATA;

	/**
     * The feature id for the '<em><b>Profile</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__PROFILE = PROCESS_BRIEF_TYPE__PROFILE;

	/**
     * The feature id for the '<em><b>WSDL</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__WSDL = PROCESS_BRIEF_TYPE__WSDL;

	/**
     * The feature id for the '<em><b>Process Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__PROCESS_VERSION = PROCESS_BRIEF_TYPE__PROCESS_VERSION;

	/**
     * The feature id for the '<em><b>Data Inputs</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__DATA_INPUTS = PROCESS_BRIEF_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Process Outputs</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__PROCESS_OUTPUTS = PROCESS_BRIEF_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>Status Supported</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__STATUS_SUPPORTED = PROCESS_BRIEF_TYPE_FEATURE_COUNT + 2;

	/**
     * The feature id for the '<em><b>Store Supported</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE__STORE_SUPPORTED = PROCESS_BRIEF_TYPE_FEATURE_COUNT + 3;

	/**
     * The number of structural features of the '<em>Process Description Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_DESCRIPTION_TYPE_FEATURE_COUNT = PROCESS_BRIEF_TYPE_FEATURE_COUNT + 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessFailedTypeImpl <em>Process Failed Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessFailedTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessFailedType()
     * @generated
     */
	int PROCESS_FAILED_TYPE = 36;

	/**
     * The feature id for the '<em><b>Exception Report</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_FAILED_TYPE__EXCEPTION_REPORT = 0;

	/**
     * The number of structural features of the '<em>Process Failed Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_FAILED_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessOfferingsTypeImpl <em>Process Offerings Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessOfferingsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessOfferingsType()
     * @generated
     */
	int PROCESS_OFFERINGS_TYPE = 37;

	/**
     * The feature id for the '<em><b>Process</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_OFFERINGS_TYPE__PROCESS = 0;

	/**
     * The number of structural features of the '<em>Process Offerings Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_OFFERINGS_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessOutputsTypeImpl <em>Process Outputs Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessOutputsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessOutputsType()
     * @generated
     */
	int PROCESS_OUTPUTS_TYPE = 38;

	/**
     * The feature id for the '<em><b>Output</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_OUTPUTS_TYPE__OUTPUT = 0;

	/**
     * The number of structural features of the '<em>Process Outputs Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_OUTPUTS_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessOutputsType1Impl <em>Process Outputs Type1</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessOutputsType1Impl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessOutputsType1()
     * @generated
     */
	int PROCESS_OUTPUTS_TYPE1 = 39;

	/**
     * The feature id for the '<em><b>Output</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_OUTPUTS_TYPE1__OUTPUT = 0;

	/**
     * The number of structural features of the '<em>Process Outputs Type1</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_OUTPUTS_TYPE1_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ProcessStartedTypeImpl <em>Process Started Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ProcessStartedTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getProcessStartedType()
     * @generated
     */
	int PROCESS_STARTED_TYPE = 40;

	/**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_STARTED_TYPE__VALUE = 0;

	/**
     * The feature id for the '<em><b>Percent Completed</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_STARTED_TYPE__PERCENT_COMPLETED = 1;

	/**
     * The number of structural features of the '<em>Process Started Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int PROCESS_STARTED_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ResponseDocumentTypeImpl <em>Response Document Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ResponseDocumentTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getResponseDocumentType()
     * @generated
     */
	int RESPONSE_DOCUMENT_TYPE = 43;

	/**
     * The feature id for the '<em><b>Output</b></em>' containment reference list.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_DOCUMENT_TYPE__OUTPUT = 0;

	/**
     * The feature id for the '<em><b>Lineage</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_DOCUMENT_TYPE__LINEAGE = 1;

	/**
     * The feature id for the '<em><b>Status</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_DOCUMENT_TYPE__STATUS = 2;

	/**
     * The feature id for the '<em><b>Store Execute Response</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_DOCUMENT_TYPE__STORE_EXECUTE_RESPONSE = 3;

	/**
     * The number of structural features of the '<em>Response Document Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_DOCUMENT_TYPE_FEATURE_COUNT = 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ResponseFormTypeImpl <em>Response Form Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ResponseFormTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getResponseFormType()
     * @generated
     */
	int RESPONSE_FORM_TYPE = 44;

	/**
     * The feature id for the '<em><b>Response Document</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_FORM_TYPE__RESPONSE_DOCUMENT = 0;

	/**
     * The feature id for the '<em><b>Raw Data Output</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_FORM_TYPE__RAW_DATA_OUTPUT = 1;

	/**
     * The number of structural features of the '<em>Response Form Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int RESPONSE_FORM_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.StatusTypeImpl <em>Status Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.StatusTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getStatusType()
     * @generated
     */
	int STATUS_TYPE = 45;

	/**
     * The feature id for the '<em><b>Process Accepted</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int STATUS_TYPE__PROCESS_ACCEPTED = 0;

	/**
     * The feature id for the '<em><b>Process Started</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int STATUS_TYPE__PROCESS_STARTED = 1;

	/**
     * The feature id for the '<em><b>Process Paused</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int STATUS_TYPE__PROCESS_PAUSED = 2;

	/**
     * The feature id for the '<em><b>Process Succeeded</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int STATUS_TYPE__PROCESS_SUCCEEDED = 3;

	/**
     * The feature id for the '<em><b>Process Failed</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int STATUS_TYPE__PROCESS_FAILED = 4;

	/**
     * The feature id for the '<em><b>Creation Time</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int STATUS_TYPE__CREATION_TIME = 5;

	/**
     * The number of structural features of the '<em>Status Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int STATUS_TYPE_FEATURE_COUNT = 6;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.SupportedComplexDataTypeImpl <em>Supported Complex Data Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.SupportedComplexDataTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getSupportedComplexDataType()
     * @generated
     */
	int SUPPORTED_COMPLEX_DATA_TYPE = 47;

	/**
     * The feature id for the '<em><b>Default</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_COMPLEX_DATA_TYPE__DEFAULT = 0;

	/**
     * The feature id for the '<em><b>Supported</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_COMPLEX_DATA_TYPE__SUPPORTED = 1;

	/**
     * The number of structural features of the '<em>Supported Complex Data Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_COMPLEX_DATA_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.SupportedComplexDataInputTypeImpl <em>Supported Complex Data Input Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.SupportedComplexDataInputTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getSupportedComplexDataInputType()
     * @generated
     */
	int SUPPORTED_COMPLEX_DATA_INPUT_TYPE = 46;

	/**
     * The feature id for the '<em><b>Default</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_COMPLEX_DATA_INPUT_TYPE__DEFAULT = SUPPORTED_COMPLEX_DATA_TYPE__DEFAULT;

	/**
     * The feature id for the '<em><b>Supported</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_COMPLEX_DATA_INPUT_TYPE__SUPPORTED = SUPPORTED_COMPLEX_DATA_TYPE__SUPPORTED;

	/**
     * The feature id for the '<em><b>Maximum Megabytes</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_COMPLEX_DATA_INPUT_TYPE__MAXIMUM_MEGABYTES = SUPPORTED_COMPLEX_DATA_TYPE_FEATURE_COUNT + 0;

	/**
     * The number of structural features of the '<em>Supported Complex Data Input Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_COMPLEX_DATA_INPUT_TYPE_FEATURE_COUNT = SUPPORTED_COMPLEX_DATA_TYPE_FEATURE_COUNT + 1;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.SupportedCRSsTypeImpl <em>Supported CR Ss Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.SupportedCRSsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getSupportedCRSsType()
     * @generated
     */
	int SUPPORTED_CR_SS_TYPE = 48;

	/**
     * The feature id for the '<em><b>Default</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_CR_SS_TYPE__DEFAULT = 0;

	/**
     * The feature id for the '<em><b>Supported</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_CR_SS_TYPE__SUPPORTED = 1;

	/**
     * The number of structural features of the '<em>Supported CR Ss Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_CR_SS_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.SupportedUOMsTypeImpl <em>Supported UO Ms Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.SupportedUOMsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getSupportedUOMsType()
     * @generated
     */
	int SUPPORTED_UO_MS_TYPE = 49;

	/**
     * The feature id for the '<em><b>Default</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_UO_MS_TYPE__DEFAULT = 0;

	/**
     * The feature id for the '<em><b>Supported</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_UO_MS_TYPE__SUPPORTED = 1;

	/**
     * The number of structural features of the '<em>Supported UO Ms Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int SUPPORTED_UO_MS_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.UOMsTypeImpl <em>UO Ms Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.UOMsTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getUOMsType()
     * @generated
     */
	int UO_MS_TYPE = 50;

	/**
     * The number of structural features of the '<em>UO Ms Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int UO_MS_TYPE_FEATURE_COUNT = 0;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.ValuesReferenceTypeImpl <em>Values Reference Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.ValuesReferenceTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getValuesReferenceType()
     * @generated
     */
	int VALUES_REFERENCE_TYPE = 51;

	/**
     * The feature id for the '<em><b>Reference</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int VALUES_REFERENCE_TYPE__REFERENCE = 0;

	/**
     * The feature id for the '<em><b>Values Form</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int VALUES_REFERENCE_TYPE__VALUES_FORM = 1;

	/**
     * The number of structural features of the '<em>Values Reference Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int VALUES_REFERENCE_TYPE_FEATURE_COUNT = 2;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.WPSCapabilitiesTypeImpl <em>WPS Capabilities Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.WPSCapabilitiesTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getWPSCapabilitiesType()
     * @generated
     */
	int WPS_CAPABILITIES_TYPE = 52;

	/**
     * The feature id for the '<em><b>Service Identification</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__SERVICE_IDENTIFICATION = Ows11Package.CAPABILITIES_BASE_TYPE__SERVICE_IDENTIFICATION;

	/**
     * The feature id for the '<em><b>Service Provider</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__SERVICE_PROVIDER = Ows11Package.CAPABILITIES_BASE_TYPE__SERVICE_PROVIDER;

	/**
     * The feature id for the '<em><b>Operations Metadata</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__OPERATIONS_METADATA = Ows11Package.CAPABILITIES_BASE_TYPE__OPERATIONS_METADATA;

	/**
     * The feature id for the '<em><b>Update Sequence</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__UPDATE_SEQUENCE = Ows11Package.CAPABILITIES_BASE_TYPE__UPDATE_SEQUENCE;

	/**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__VERSION = Ows11Package.CAPABILITIES_BASE_TYPE__VERSION;

	/**
     * The feature id for the '<em><b>Process Offerings</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__PROCESS_OFFERINGS = Ows11Package.CAPABILITIES_BASE_TYPE_FEATURE_COUNT + 0;

	/**
     * The feature id for the '<em><b>Languages</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__LANGUAGES = Ows11Package.CAPABILITIES_BASE_TYPE_FEATURE_COUNT + 1;

	/**
     * The feature id for the '<em><b>WSDL</b></em>' containment reference.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__WSDL = Ows11Package.CAPABILITIES_BASE_TYPE_FEATURE_COUNT + 2;

	/**
     * The feature id for the '<em><b>Service</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE__SERVICE = Ows11Package.CAPABILITIES_BASE_TYPE_FEATURE_COUNT + 3;

	/**
     * The number of structural features of the '<em>WPS Capabilities Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WPS_CAPABILITIES_TYPE_FEATURE_COUNT = Ows11Package.CAPABILITIES_BASE_TYPE_FEATURE_COUNT + 4;

	/**
     * The meta object id for the '{@link net.opengis.wps.impl.WSDLTypeImpl <em>WSDL Type</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.impl.WSDLTypeImpl
     * @see net.opengis.wps.impl.WpsPackageImpl#getWSDLType()
     * @generated
     */
	int WSDL_TYPE = 53;

	/**
     * The feature id for the '<em><b>Href</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WSDL_TYPE__HREF = 0;

	/**
     * The number of structural features of the '<em>WSDL Type</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int WSDL_TYPE_FEATURE_COUNT = 1;

	/**
     * The meta object id for the '{@link javax.measure.unit.Unit <em>Unit</em>}' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see javax.measure.unit.Unit
     * @see net.opengis.wps.impl.WpsPackageImpl#getUnit()
     * @generated
     */
	int UNIT = 54;

	/**
     * The number of structural features of the '<em>Unit</em>' class.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
	int UNIT_FEATURE_COUNT = 0;

	/**
     * The meta object id for the '{@link net.opengis.wps.MethodType <em>Method Type</em>}' enum.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.MethodType
     * @see net.opengis.wps.impl.WpsPackageImpl#getMethodType()
     * @generated
     */
	int METHOD_TYPE = 55;

	/**
     * The meta object id for the '<em>Method Type Object</em>' data type.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see net.opengis.wps.MethodType
     * @see net.opengis.wps.impl.WpsPackageImpl#getMethodTypeObject()
     * @generated
     */
	int METHOD_TYPE_OBJECT = 56;

	/**
     * The meta object id for the '<em>Percent Completed Type</em>' data type.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see java.math.BigInteger
     * @see net.opengis.wps.impl.WpsPackageImpl#getPercentCompletedType()
     * @generated
     */
	int PERCENT_COMPLETED_TYPE = 57;


	/**
     * Returns the meta object for class '{@link net.opengis.wps.BodyReferenceType <em>Body Reference Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Body Reference Type</em>'.
     * @see net.opengis.wps.BodyReferenceType
     * @generated
     */
	EClass getBodyReferenceType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.BodyReferenceType#getHref <em>Href</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Href</em>'.
     * @see net.opengis.wps.BodyReferenceType#getHref()
     * @see #getBodyReferenceType()
     * @generated
     */
	EAttribute getBodyReferenceType_Href();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ComplexDataCombinationsType <em>Complex Data Combinations Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Complex Data Combinations Type</em>'.
     * @see net.opengis.wps.ComplexDataCombinationsType
     * @generated
     */
	EClass getComplexDataCombinationsType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.ComplexDataCombinationsType#getFormat <em>Format</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Format</em>'.
     * @see net.opengis.wps.ComplexDataCombinationsType#getFormat()
     * @see #getComplexDataCombinationsType()
     * @generated
     */
	EReference getComplexDataCombinationsType_Format();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ComplexDataCombinationType <em>Complex Data Combination Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Complex Data Combination Type</em>'.
     * @see net.opengis.wps.ComplexDataCombinationType
     * @generated
     */
	EClass getComplexDataCombinationType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ComplexDataCombinationType#getFormat <em>Format</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Format</em>'.
     * @see net.opengis.wps.ComplexDataCombinationType#getFormat()
     * @see #getComplexDataCombinationType()
     * @generated
     */
	EReference getComplexDataCombinationType_Format();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ComplexDataDescriptionType <em>Complex Data Description Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Complex Data Description Type</em>'.
     * @see net.opengis.wps.ComplexDataDescriptionType
     * @generated
     */
	EClass getComplexDataDescriptionType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ComplexDataDescriptionType#getMimeType <em>Mime Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Mime Type</em>'.
     * @see net.opengis.wps.ComplexDataDescriptionType#getMimeType()
     * @see #getComplexDataDescriptionType()
     * @generated
     */
	EAttribute getComplexDataDescriptionType_MimeType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ComplexDataDescriptionType#getEncoding <em>Encoding</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Encoding</em>'.
     * @see net.opengis.wps.ComplexDataDescriptionType#getEncoding()
     * @see #getComplexDataDescriptionType()
     * @generated
     */
	EAttribute getComplexDataDescriptionType_Encoding();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ComplexDataDescriptionType#getSchema <em>Schema</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Schema</em>'.
     * @see net.opengis.wps.ComplexDataDescriptionType#getSchema()
     * @see #getComplexDataDescriptionType()
     * @generated
     */
	EAttribute getComplexDataDescriptionType_Schema();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ComplexDataType <em>Complex Data Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Complex Data Type</em>'.
     * @see net.opengis.wps.ComplexDataType
     * @generated
     */
	EClass getComplexDataType();

	/**
     * Returns the meta object for the attribute list '{@link net.opengis.wps.ComplexDataType#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see net.opengis.wps.ComplexDataType#getMixed()
     * @see #getComplexDataType()
     * @generated
     */
	EAttribute getComplexDataType_Mixed();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ComplexDataType#getEncoding <em>Encoding</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Encoding</em>'.
     * @see net.opengis.wps.ComplexDataType#getEncoding()
     * @see #getComplexDataType()
     * @generated
     */
	EAttribute getComplexDataType_Encoding();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ComplexDataType#getMimeType <em>Mime Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Mime Type</em>'.
     * @see net.opengis.wps.ComplexDataType#getMimeType()
     * @see #getComplexDataType()
     * @generated
     */
	EAttribute getComplexDataType_MimeType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ComplexDataType#getSchema <em>Schema</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Schema</em>'.
     * @see net.opengis.wps.ComplexDataType#getSchema()
     * @see #getComplexDataType()
     * @generated
     */
	EAttribute getComplexDataType_Schema();

	/**
     * Returns the meta object for the attribute list '{@link net.opengis.wps.ComplexDataType#getData <em>Data</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Data</em>'.
     * @see net.opengis.wps.ComplexDataType#getData()
     * @see #getComplexDataType()
     * @generated
     */
    EAttribute getComplexDataType_Data();

    /**
     * Returns the meta object for class '{@link net.opengis.wps.CRSsType <em>CR Ss Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>CR Ss Type</em>'.
     * @see net.opengis.wps.CRSsType
     * @generated
     */
	EClass getCRSsType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.CRSsType#getCRS <em>CRS</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>CRS</em>'.
     * @see net.opengis.wps.CRSsType#getCRS()
     * @see #getCRSsType()
     * @generated
     */
	EAttribute getCRSsType_CRS();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DataInputsType <em>Data Inputs Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Data Inputs Type</em>'.
     * @see net.opengis.wps.DataInputsType
     * @generated
     */
	EClass getDataInputsType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.DataInputsType#getInput <em>Input</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Input</em>'.
     * @see net.opengis.wps.DataInputsType#getInput()
     * @see #getDataInputsType()
     * @generated
     */
	EReference getDataInputsType_Input();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DataInputsType1 <em>Data Inputs Type1</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Data Inputs Type1</em>'.
     * @see net.opengis.wps.DataInputsType1
     * @generated
     */
	EClass getDataInputsType1();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.DataInputsType1#getInput <em>Input</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Input</em>'.
     * @see net.opengis.wps.DataInputsType1#getInput()
     * @see #getDataInputsType1()
     * @generated
     */
	EReference getDataInputsType1_Input();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DataType <em>Data Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Data Type</em>'.
     * @see net.opengis.wps.DataType
     * @generated
     */
	EClass getDataType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DataType#getComplexData <em>Complex Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Complex Data</em>'.
     * @see net.opengis.wps.DataType#getComplexData()
     * @see #getDataType()
     * @generated
     */
	EReference getDataType_ComplexData();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DataType#getLiteralData <em>Literal Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Literal Data</em>'.
     * @see net.opengis.wps.DataType#getLiteralData()
     * @see #getDataType()
     * @generated
     */
	EReference getDataType_LiteralData();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DataType#getBoundingBoxData <em>Bounding Box Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Bounding Box Data</em>'.
     * @see net.opengis.wps.DataType#getBoundingBoxData()
     * @see #getDataType()
     * @generated
     */
	EReference getDataType_BoundingBoxData();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DefaultType <em>Default Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Default Type</em>'.
     * @see net.opengis.wps.DefaultType
     * @generated
     */
	EClass getDefaultType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.DefaultType#getCRS <em>CRS</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>CRS</em>'.
     * @see net.opengis.wps.DefaultType#getCRS()
     * @see #getDefaultType()
     * @generated
     */
	EAttribute getDefaultType_CRS();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DefaultType1 <em>Default Type1</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Default Type1</em>'.
     * @see net.opengis.wps.DefaultType1
     * @generated
     */
	EClass getDefaultType1();

	/**
     * Returns the meta object for the reference '{@link net.opengis.wps.DefaultType1#getUOM <em>UOM</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the reference '<em>UOM</em>'.
     * @see net.opengis.wps.DefaultType1#getUOM()
     * @see #getDefaultType1()
     * @generated
     */
	EReference getDefaultType1_UOM();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DefaultType2 <em>Default Type2</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Default Type2</em>'.
     * @see net.opengis.wps.DefaultType2
     * @generated
     */
	EClass getDefaultType2();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.DefaultType2#getLanguage <em>Language</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Language</em>'.
     * @see net.opengis.wps.DefaultType2#getLanguage()
     * @see #getDefaultType2()
     * @generated
     */
	EAttribute getDefaultType2_Language();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DescribeProcessType <em>Describe Process Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Describe Process Type</em>'.
     * @see net.opengis.wps.DescribeProcessType
     * @generated
     */
	EClass getDescribeProcessType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.DescribeProcessType#getIdentifier <em>Identifier</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Identifier</em>'.
     * @see net.opengis.wps.DescribeProcessType#getIdentifier()
     * @see #getDescribeProcessType()
     * @generated
     */
	EReference getDescribeProcessType_Identifier();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DescriptionType <em>Description Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Description Type</em>'.
     * @see net.opengis.wps.DescriptionType
     * @generated
     */
	EClass getDescriptionType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DescriptionType#getIdentifier <em>Identifier</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Identifier</em>'.
     * @see net.opengis.wps.DescriptionType#getIdentifier()
     * @see #getDescriptionType()
     * @generated
     */
	EReference getDescriptionType_Identifier();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DescriptionType#getTitle <em>Title</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Title</em>'.
     * @see net.opengis.wps.DescriptionType#getTitle()
     * @see #getDescriptionType()
     * @generated
     */
	EReference getDescriptionType_Title();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DescriptionType#getAbstract <em>Abstract</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Abstract</em>'.
     * @see net.opengis.wps.DescriptionType#getAbstract()
     * @see #getDescriptionType()
     * @generated
     */
	EReference getDescriptionType_Abstract();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.DescriptionType#getMetadata <em>Metadata</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Metadata</em>'.
     * @see net.opengis.wps.DescriptionType#getMetadata()
     * @see #getDescriptionType()
     * @generated
     */
	EReference getDescriptionType_Metadata();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DocumentOutputDefinitionType <em>Document Output Definition Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Output Definition Type</em>'.
     * @see net.opengis.wps.DocumentOutputDefinitionType
     * @generated
     */
	EClass getDocumentOutputDefinitionType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentOutputDefinitionType#getTitle <em>Title</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Title</em>'.
     * @see net.opengis.wps.DocumentOutputDefinitionType#getTitle()
     * @see #getDocumentOutputDefinitionType()
     * @generated
     */
	EReference getDocumentOutputDefinitionType_Title();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentOutputDefinitionType#getAbstract <em>Abstract</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Abstract</em>'.
     * @see net.opengis.wps.DocumentOutputDefinitionType#getAbstract()
     * @see #getDocumentOutputDefinitionType()
     * @generated
     */
	EReference getDocumentOutputDefinitionType_Abstract();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.DocumentOutputDefinitionType#isAsReference <em>As Reference</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>As Reference</em>'.
     * @see net.opengis.wps.DocumentOutputDefinitionType#isAsReference()
     * @see #getDocumentOutputDefinitionType()
     * @generated
     */
	EAttribute getDocumentOutputDefinitionType_AsReference();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see net.opengis.wps.DocumentRoot
     * @generated
     */
	EClass getDocumentRoot();

	/**
     * Returns the meta object for the attribute list '{@link net.opengis.wps.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see net.opengis.wps.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
	EAttribute getDocumentRoot_Mixed();

	/**
     * Returns the meta object for the map '{@link net.opengis.wps.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see net.opengis.wps.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_XMLNSPrefixMap();

	/**
     * Returns the meta object for the map '{@link net.opengis.wps.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see net.opengis.wps.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_XSISchemaLocation();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getCapabilities <em>Capabilities</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Capabilities</em>'.
     * @see net.opengis.wps.DocumentRoot#getCapabilities()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_Capabilities();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getDescribeProcess <em>Describe Process</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Describe Process</em>'.
     * @see net.opengis.wps.DocumentRoot#getDescribeProcess()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_DescribeProcess();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getExecute <em>Execute</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Execute</em>'.
     * @see net.opengis.wps.DocumentRoot#getExecute()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_Execute();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getExecuteResponse <em>Execute Response</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Execute Response</em>'.
     * @see net.opengis.wps.DocumentRoot#getExecuteResponse()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_ExecuteResponse();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getGetCapabilities <em>Get Capabilities</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Get Capabilities</em>'.
     * @see net.opengis.wps.DocumentRoot#getGetCapabilities()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_GetCapabilities();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getLanguages <em>Languages</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Languages</em>'.
     * @see net.opengis.wps.DocumentRoot#getLanguages()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_Languages();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getProcessDescriptions <em>Process Descriptions</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Descriptions</em>'.
     * @see net.opengis.wps.DocumentRoot#getProcessDescriptions()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_ProcessDescriptions();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getProcessOfferings <em>Process Offerings</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Offerings</em>'.
     * @see net.opengis.wps.DocumentRoot#getProcessOfferings()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_ProcessOfferings();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.DocumentRoot#getWSDL <em>WSDL</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>WSDL</em>'.
     * @see net.opengis.wps.DocumentRoot#getWSDL()
     * @see #getDocumentRoot()
     * @generated
     */
	EReference getDocumentRoot_WSDL();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.DocumentRoot#getProcessVersion <em>Process Version</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Process Version</em>'.
     * @see net.opengis.wps.DocumentRoot#getProcessVersion()
     * @see #getDocumentRoot()
     * @generated
     */
	EAttribute getDocumentRoot_ProcessVersion();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ExecuteResponseType <em>Execute Response Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Execute Response Type</em>'.
     * @see net.opengis.wps.ExecuteResponseType
     * @generated
     */
	EClass getExecuteResponseType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteResponseType#getProcess <em>Process</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process</em>'.
     * @see net.opengis.wps.ExecuteResponseType#getProcess()
     * @see #getExecuteResponseType()
     * @generated
     */
	EReference getExecuteResponseType_Process();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteResponseType#getStatus <em>Status</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Status</em>'.
     * @see net.opengis.wps.ExecuteResponseType#getStatus()
     * @see #getExecuteResponseType()
     * @generated
     */
	EReference getExecuteResponseType_Status();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteResponseType#getDataInputs <em>Data Inputs</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Data Inputs</em>'.
     * @see net.opengis.wps.ExecuteResponseType#getDataInputs()
     * @see #getExecuteResponseType()
     * @generated
     */
	EReference getExecuteResponseType_DataInputs();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteResponseType#getOutputDefinitions <em>Output Definitions</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Output Definitions</em>'.
     * @see net.opengis.wps.ExecuteResponseType#getOutputDefinitions()
     * @see #getExecuteResponseType()
     * @generated
     */
	EReference getExecuteResponseType_OutputDefinitions();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteResponseType#getProcessOutputs <em>Process Outputs</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Outputs</em>'.
     * @see net.opengis.wps.ExecuteResponseType#getProcessOutputs()
     * @see #getExecuteResponseType()
     * @generated
     */
	EReference getExecuteResponseType_ProcessOutputs();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ExecuteResponseType#getServiceInstance <em>Service Instance</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Service Instance</em>'.
     * @see net.opengis.wps.ExecuteResponseType#getServiceInstance()
     * @see #getExecuteResponseType()
     * @generated
     */
	EAttribute getExecuteResponseType_ServiceInstance();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ExecuteResponseType#getStatusLocation <em>Status Location</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Status Location</em>'.
     * @see net.opengis.wps.ExecuteResponseType#getStatusLocation()
     * @see #getExecuteResponseType()
     * @generated
     */
	EAttribute getExecuteResponseType_StatusLocation();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ExecuteType <em>Execute Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Execute Type</em>'.
     * @see net.opengis.wps.ExecuteType
     * @generated
     */
	EClass getExecuteType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteType#getIdentifier <em>Identifier</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Identifier</em>'.
     * @see net.opengis.wps.ExecuteType#getIdentifier()
     * @see #getExecuteType()
     * @generated
     */
	EReference getExecuteType_Identifier();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteType#getDataInputs <em>Data Inputs</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Data Inputs</em>'.
     * @see net.opengis.wps.ExecuteType#getDataInputs()
     * @see #getExecuteType()
     * @generated
     */
	EReference getExecuteType_DataInputs();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ExecuteType#getResponseForm <em>Response Form</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Response Form</em>'.
     * @see net.opengis.wps.ExecuteType#getResponseForm()
     * @see #getExecuteType()
     * @generated
     */
	EReference getExecuteType_ResponseForm();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.GetCapabilitiesType <em>Get Capabilities Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Get Capabilities Type</em>'.
     * @see net.opengis.wps.GetCapabilitiesType
     * @generated
     */
	EClass getGetCapabilitiesType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.GetCapabilitiesType#getAcceptVersions <em>Accept Versions</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Accept Versions</em>'.
     * @see net.opengis.wps.GetCapabilitiesType#getAcceptVersions()
     * @see #getGetCapabilitiesType()
     * @generated
     */
	EReference getGetCapabilitiesType_AcceptVersions();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.GetCapabilitiesType#getLanguage <em>Language</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Language</em>'.
     * @see net.opengis.wps.GetCapabilitiesType#getLanguage()
     * @see #getGetCapabilitiesType()
     * @generated
     */
	EAttribute getGetCapabilitiesType_Language();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.GetCapabilitiesType#getService <em>Service</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Service</em>'.
     * @see net.opengis.wps.GetCapabilitiesType#getService()
     * @see #getGetCapabilitiesType()
     * @generated
     */
	EAttribute getGetCapabilitiesType_Service();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.GetCapabilitiesType#getBaseUrl <em>Base Url</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Base Url</em>'.
     * @see net.opengis.wps.GetCapabilitiesType#getBaseUrl()
     * @see #getGetCapabilitiesType()
     * @generated
     */
	EAttribute getGetCapabilitiesType_BaseUrl();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.HeaderType <em>Header Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Header Type</em>'.
     * @see net.opengis.wps.HeaderType
     * @generated
     */
	EClass getHeaderType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.HeaderType#getKey <em>Key</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Key</em>'.
     * @see net.opengis.wps.HeaderType#getKey()
     * @see #getHeaderType()
     * @generated
     */
	EAttribute getHeaderType_Key();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.HeaderType#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see net.opengis.wps.HeaderType#getValue()
     * @see #getHeaderType()
     * @generated
     */
	EAttribute getHeaderType_Value();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.InputDescriptionType <em>Input Description Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Input Description Type</em>'.
     * @see net.opengis.wps.InputDescriptionType
     * @generated
     */
	EClass getInputDescriptionType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputDescriptionType#getComplexData <em>Complex Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Complex Data</em>'.
     * @see net.opengis.wps.InputDescriptionType#getComplexData()
     * @see #getInputDescriptionType()
     * @generated
     */
	EReference getInputDescriptionType_ComplexData();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputDescriptionType#getLiteralData <em>Literal Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Literal Data</em>'.
     * @see net.opengis.wps.InputDescriptionType#getLiteralData()
     * @see #getInputDescriptionType()
     * @generated
     */
	EReference getInputDescriptionType_LiteralData();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputDescriptionType#getBoundingBoxData <em>Bounding Box Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Bounding Box Data</em>'.
     * @see net.opengis.wps.InputDescriptionType#getBoundingBoxData()
     * @see #getInputDescriptionType()
     * @generated
     */
	EReference getInputDescriptionType_BoundingBoxData();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.InputDescriptionType#getMaxOccurs <em>Max Occurs</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Max Occurs</em>'.
     * @see net.opengis.wps.InputDescriptionType#getMaxOccurs()
     * @see #getInputDescriptionType()
     * @generated
     */
	EAttribute getInputDescriptionType_MaxOccurs();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.InputDescriptionType#getMinOccurs <em>Min Occurs</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Min Occurs</em>'.
     * @see net.opengis.wps.InputDescriptionType#getMinOccurs()
     * @see #getInputDescriptionType()
     * @generated
     */
	EAttribute getInputDescriptionType_MinOccurs();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.InputReferenceType <em>Input Reference Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Input Reference Type</em>'.
     * @see net.opengis.wps.InputReferenceType
     * @generated
     */
	EClass getInputReferenceType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.InputReferenceType#getHeader <em>Header</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Header</em>'.
     * @see net.opengis.wps.InputReferenceType#getHeader()
     * @see #getInputReferenceType()
     * @generated
     */
	EReference getInputReferenceType_Header();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputReferenceType#getBody <em>Body</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Body</em>'.
     * @see net.opengis.wps.InputReferenceType#getBody()
     * @see #getInputReferenceType()
     * @generated
     */
	EReference getInputReferenceType_Body();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputReferenceType#getBodyReference <em>Body Reference</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Body Reference</em>'.
     * @see net.opengis.wps.InputReferenceType#getBodyReference()
     * @see #getInputReferenceType()
     * @generated
     */
	EReference getInputReferenceType_BodyReference();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.InputReferenceType#getEncoding <em>Encoding</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Encoding</em>'.
     * @see net.opengis.wps.InputReferenceType#getEncoding()
     * @see #getInputReferenceType()
     * @generated
     */
	EAttribute getInputReferenceType_Encoding();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.InputReferenceType#getHref <em>Href</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Href</em>'.
     * @see net.opengis.wps.InputReferenceType#getHref()
     * @see #getInputReferenceType()
     * @generated
     */
	EAttribute getInputReferenceType_Href();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.InputReferenceType#getMethod <em>Method</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Method</em>'.
     * @see net.opengis.wps.InputReferenceType#getMethod()
     * @see #getInputReferenceType()
     * @generated
     */
	EAttribute getInputReferenceType_Method();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.InputReferenceType#getMimeType <em>Mime Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Mime Type</em>'.
     * @see net.opengis.wps.InputReferenceType#getMimeType()
     * @see #getInputReferenceType()
     * @generated
     */
	EAttribute getInputReferenceType_MimeType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.InputReferenceType#getSchema <em>Schema</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Schema</em>'.
     * @see net.opengis.wps.InputReferenceType#getSchema()
     * @see #getInputReferenceType()
     * @generated
     */
	EAttribute getInputReferenceType_Schema();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.InputType <em>Input Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Input Type</em>'.
     * @see net.opengis.wps.InputType
     * @generated
     */
	EClass getInputType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputType#getIdentifier <em>Identifier</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Identifier</em>'.
     * @see net.opengis.wps.InputType#getIdentifier()
     * @see #getInputType()
     * @generated
     */
	EReference getInputType_Identifier();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputType#getTitle <em>Title</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Title</em>'.
     * @see net.opengis.wps.InputType#getTitle()
     * @see #getInputType()
     * @generated
     */
	EReference getInputType_Title();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputType#getAbstract <em>Abstract</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Abstract</em>'.
     * @see net.opengis.wps.InputType#getAbstract()
     * @see #getInputType()
     * @generated
     */
	EReference getInputType_Abstract();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputType#getReference <em>Reference</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Reference</em>'.
     * @see net.opengis.wps.InputType#getReference()
     * @see #getInputType()
     * @generated
     */
	EReference getInputType_Reference();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.InputType#getData <em>Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Data</em>'.
     * @see net.opengis.wps.InputType#getData()
     * @see #getInputType()
     * @generated
     */
	EReference getInputType_Data();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.LanguagesType <em>Languages Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Languages Type</em>'.
     * @see net.opengis.wps.LanguagesType
     * @generated
     */
	EClass getLanguagesType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.LanguagesType#getLanguage <em>Language</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Language</em>'.
     * @see net.opengis.wps.LanguagesType#getLanguage()
     * @see #getLanguagesType()
     * @generated
     */
	EAttribute getLanguagesType_Language();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.LanguagesType1 <em>Languages Type1</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Languages Type1</em>'.
     * @see net.opengis.wps.LanguagesType1
     * @generated
     */
	EClass getLanguagesType1();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.LanguagesType1#getDefault <em>Default</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Default</em>'.
     * @see net.opengis.wps.LanguagesType1#getDefault()
     * @see #getLanguagesType1()
     * @generated
     */
	EReference getLanguagesType1_Default();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.LanguagesType1#getSupported <em>Supported</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Supported</em>'.
     * @see net.opengis.wps.LanguagesType1#getSupported()
     * @see #getLanguagesType1()
     * @generated
     */
	EReference getLanguagesType1_Supported();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.LiteralDataType <em>Literal Data Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Literal Data Type</em>'.
     * @see net.opengis.wps.LiteralDataType
     * @generated
     */
	EClass getLiteralDataType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.LiteralDataType#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see net.opengis.wps.LiteralDataType#getValue()
     * @see #getLiteralDataType()
     * @generated
     */
	EAttribute getLiteralDataType_Value();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.LiteralDataType#getDataType <em>Data Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Data Type</em>'.
     * @see net.opengis.wps.LiteralDataType#getDataType()
     * @see #getLiteralDataType()
     * @generated
     */
	EAttribute getLiteralDataType_DataType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.LiteralDataType#getUom <em>Uom</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Uom</em>'.
     * @see net.opengis.wps.LiteralDataType#getUom()
     * @see #getLiteralDataType()
     * @generated
     */
	EAttribute getLiteralDataType_Uom();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.LiteralInputType <em>Literal Input Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Literal Input Type</em>'.
     * @see net.opengis.wps.LiteralInputType
     * @generated
     */
	EClass getLiteralInputType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.LiteralInputType#getAllowedValues <em>Allowed Values</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Allowed Values</em>'.
     * @see net.opengis.wps.LiteralInputType#getAllowedValues()
     * @see #getLiteralInputType()
     * @generated
     */
	EReference getLiteralInputType_AllowedValues();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.LiteralInputType#getAnyValue <em>Any Value</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Any Value</em>'.
     * @see net.opengis.wps.LiteralInputType#getAnyValue()
     * @see #getLiteralInputType()
     * @generated
     */
	EReference getLiteralInputType_AnyValue();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.LiteralInputType#getValuesReference <em>Values Reference</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Values Reference</em>'.
     * @see net.opengis.wps.LiteralInputType#getValuesReference()
     * @see #getLiteralInputType()
     * @generated
     */
	EReference getLiteralInputType_ValuesReference();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.LiteralInputType#getDefaultValue <em>Default Value</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Default Value</em>'.
     * @see net.opengis.wps.LiteralInputType#getDefaultValue()
     * @see #getLiteralInputType()
     * @generated
     */
	EAttribute getLiteralInputType_DefaultValue();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.LiteralOutputType <em>Literal Output Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Literal Output Type</em>'.
     * @see net.opengis.wps.LiteralOutputType
     * @generated
     */
	EClass getLiteralOutputType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.LiteralOutputType#getDataType <em>Data Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Data Type</em>'.
     * @see net.opengis.wps.LiteralOutputType#getDataType()
     * @see #getLiteralOutputType()
     * @generated
     */
	EReference getLiteralOutputType_DataType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.LiteralOutputType#getUOMs <em>UO Ms</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>UO Ms</em>'.
     * @see net.opengis.wps.LiteralOutputType#getUOMs()
     * @see #getLiteralOutputType()
     * @generated
     */
	EReference getLiteralOutputType_UOMs();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.OutputDataType <em>Output Data Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Output Data Type</em>'.
     * @see net.opengis.wps.OutputDataType
     * @generated
     */
	EClass getOutputDataType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.OutputDataType#getReference <em>Reference</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Reference</em>'.
     * @see net.opengis.wps.OutputDataType#getReference()
     * @see #getOutputDataType()
     * @generated
     */
	EReference getOutputDataType_Reference();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.OutputDataType#getData <em>Data</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Data</em>'.
     * @see net.opengis.wps.OutputDataType#getData()
     * @see #getOutputDataType()
     * @generated
     */
	EReference getOutputDataType_Data();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.OutputDefinitionsType <em>Output Definitions Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Output Definitions Type</em>'.
     * @see net.opengis.wps.OutputDefinitionsType
     * @generated
     */
	EClass getOutputDefinitionsType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.OutputDefinitionsType#getOutput <em>Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Output</em>'.
     * @see net.opengis.wps.OutputDefinitionsType#getOutput()
     * @see #getOutputDefinitionsType()
     * @generated
     */
	EReference getOutputDefinitionsType_Output();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.OutputDefinitionType <em>Output Definition Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Output Definition Type</em>'.
     * @see net.opengis.wps.OutputDefinitionType
     * @generated
     */
	EClass getOutputDefinitionType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.OutputDefinitionType#getIdentifier <em>Identifier</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Identifier</em>'.
     * @see net.opengis.wps.OutputDefinitionType#getIdentifier()
     * @see #getOutputDefinitionType()
     * @generated
     */
	EReference getOutputDefinitionType_Identifier();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputDefinitionType#getEncoding <em>Encoding</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Encoding</em>'.
     * @see net.opengis.wps.OutputDefinitionType#getEncoding()
     * @see #getOutputDefinitionType()
     * @generated
     */
	EAttribute getOutputDefinitionType_Encoding();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputDefinitionType#getMimeType <em>Mime Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Mime Type</em>'.
     * @see net.opengis.wps.OutputDefinitionType#getMimeType()
     * @see #getOutputDefinitionType()
     * @generated
     */
	EAttribute getOutputDefinitionType_MimeType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputDefinitionType#getSchema <em>Schema</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Schema</em>'.
     * @see net.opengis.wps.OutputDefinitionType#getSchema()
     * @see #getOutputDefinitionType()
     * @generated
     */
	EAttribute getOutputDefinitionType_Schema();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputDefinitionType#getUom <em>Uom</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Uom</em>'.
     * @see net.opengis.wps.OutputDefinitionType#getUom()
     * @see #getOutputDefinitionType()
     * @generated
     */
	EAttribute getOutputDefinitionType_Uom();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.OutputDescriptionType <em>Output Description Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Output Description Type</em>'.
     * @see net.opengis.wps.OutputDescriptionType
     * @generated
     */
	EClass getOutputDescriptionType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.OutputDescriptionType#getComplexOutput <em>Complex Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Complex Output</em>'.
     * @see net.opengis.wps.OutputDescriptionType#getComplexOutput()
     * @see #getOutputDescriptionType()
     * @generated
     */
	EReference getOutputDescriptionType_ComplexOutput();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.OutputDescriptionType#getLiteralOutput <em>Literal Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Literal Output</em>'.
     * @see net.opengis.wps.OutputDescriptionType#getLiteralOutput()
     * @see #getOutputDescriptionType()
     * @generated
     */
	EReference getOutputDescriptionType_LiteralOutput();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.OutputDescriptionType#getBoundingBoxOutput <em>Bounding Box Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Bounding Box Output</em>'.
     * @see net.opengis.wps.OutputDescriptionType#getBoundingBoxOutput()
     * @see #getOutputDescriptionType()
     * @generated
     */
	EReference getOutputDescriptionType_BoundingBoxOutput();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.OutputReferenceType <em>Output Reference Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Output Reference Type</em>'.
     * @see net.opengis.wps.OutputReferenceType
     * @generated
     */
	EClass getOutputReferenceType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputReferenceType#getEncoding <em>Encoding</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Encoding</em>'.
     * @see net.opengis.wps.OutputReferenceType#getEncoding()
     * @see #getOutputReferenceType()
     * @generated
     */
	EAttribute getOutputReferenceType_Encoding();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputReferenceType#getHref <em>Href</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Href</em>'.
     * @see net.opengis.wps.OutputReferenceType#getHref()
     * @see #getOutputReferenceType()
     * @generated
     */
	EAttribute getOutputReferenceType_Href();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputReferenceType#getMimeType <em>Mime Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Mime Type</em>'.
     * @see net.opengis.wps.OutputReferenceType#getMimeType()
     * @see #getOutputReferenceType()
     * @generated
     */
	EAttribute getOutputReferenceType_MimeType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.OutputReferenceType#getSchema <em>Schema</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Schema</em>'.
     * @see net.opengis.wps.OutputReferenceType#getSchema()
     * @see #getOutputReferenceType()
     * @generated
     */
	EAttribute getOutputReferenceType_Schema();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessBriefType <em>Process Brief Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Brief Type</em>'.
     * @see net.opengis.wps.ProcessBriefType
     * @generated
     */
	EClass getProcessBriefType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ProcessBriefType#getProfile <em>Profile</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Profile</em>'.
     * @see net.opengis.wps.ProcessBriefType#getProfile()
     * @see #getProcessBriefType()
     * @generated
     */
	EAttribute getProcessBriefType_Profile();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ProcessBriefType#getWSDL <em>WSDL</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>WSDL</em>'.
     * @see net.opengis.wps.ProcessBriefType#getWSDL()
     * @see #getProcessBriefType()
     * @generated
     */
	EReference getProcessBriefType_WSDL();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ProcessBriefType#getProcessVersion <em>Process Version</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Process Version</em>'.
     * @see net.opengis.wps.ProcessBriefType#getProcessVersion()
     * @see #getProcessBriefType()
     * @generated
     */
	EAttribute getProcessBriefType_ProcessVersion();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessDescriptionsType <em>Process Descriptions Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Descriptions Type</em>'.
     * @see net.opengis.wps.ProcessDescriptionsType
     * @generated
     */
	EClass getProcessDescriptionsType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.ProcessDescriptionsType#getProcessDescription <em>Process Description</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Process Description</em>'.
     * @see net.opengis.wps.ProcessDescriptionsType#getProcessDescription()
     * @see #getProcessDescriptionsType()
     * @generated
     */
	EReference getProcessDescriptionsType_ProcessDescription();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessDescriptionType <em>Process Description Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Description Type</em>'.
     * @see net.opengis.wps.ProcessDescriptionType
     * @generated
     */
	EClass getProcessDescriptionType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ProcessDescriptionType#getDataInputs <em>Data Inputs</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Data Inputs</em>'.
     * @see net.opengis.wps.ProcessDescriptionType#getDataInputs()
     * @see #getProcessDescriptionType()
     * @generated
     */
	EReference getProcessDescriptionType_DataInputs();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ProcessDescriptionType#getProcessOutputs <em>Process Outputs</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Outputs</em>'.
     * @see net.opengis.wps.ProcessDescriptionType#getProcessOutputs()
     * @see #getProcessDescriptionType()
     * @generated
     */
	EReference getProcessDescriptionType_ProcessOutputs();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ProcessDescriptionType#isStatusSupported <em>Status Supported</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Status Supported</em>'.
     * @see net.opengis.wps.ProcessDescriptionType#isStatusSupported()
     * @see #getProcessDescriptionType()
     * @generated
     */
	EAttribute getProcessDescriptionType_StatusSupported();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ProcessDescriptionType#isStoreSupported <em>Store Supported</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Store Supported</em>'.
     * @see net.opengis.wps.ProcessDescriptionType#isStoreSupported()
     * @see #getProcessDescriptionType()
     * @generated
     */
	EAttribute getProcessDescriptionType_StoreSupported();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessFailedType <em>Process Failed Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Failed Type</em>'.
     * @see net.opengis.wps.ProcessFailedType
     * @generated
     */
	EClass getProcessFailedType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ProcessFailedType#getExceptionReport <em>Exception Report</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Exception Report</em>'.
     * @see net.opengis.wps.ProcessFailedType#getExceptionReport()
     * @see #getProcessFailedType()
     * @generated
     */
	EReference getProcessFailedType_ExceptionReport();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessOfferingsType <em>Process Offerings Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Offerings Type</em>'.
     * @see net.opengis.wps.ProcessOfferingsType
     * @generated
     */
	EClass getProcessOfferingsType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.ProcessOfferingsType#getProcess <em>Process</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Process</em>'.
     * @see net.opengis.wps.ProcessOfferingsType#getProcess()
     * @see #getProcessOfferingsType()
     * @generated
     */
	EReference getProcessOfferingsType_Process();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessOutputsType <em>Process Outputs Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Outputs Type</em>'.
     * @see net.opengis.wps.ProcessOutputsType
     * @generated
     */
	EClass getProcessOutputsType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.ProcessOutputsType#getOutput <em>Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Output</em>'.
     * @see net.opengis.wps.ProcessOutputsType#getOutput()
     * @see #getProcessOutputsType()
     * @generated
     */
	EReference getProcessOutputsType_Output();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessOutputsType1 <em>Process Outputs Type1</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Outputs Type1</em>'.
     * @see net.opengis.wps.ProcessOutputsType1
     * @generated
     */
	EClass getProcessOutputsType1();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.ProcessOutputsType1#getOutput <em>Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Output</em>'.
     * @see net.opengis.wps.ProcessOutputsType1#getOutput()
     * @see #getProcessOutputsType1()
     * @generated
     */
	EReference getProcessOutputsType1_Output();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ProcessStartedType <em>Process Started Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Process Started Type</em>'.
     * @see net.opengis.wps.ProcessStartedType
     * @generated
     */
	EClass getProcessStartedType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ProcessStartedType#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see net.opengis.wps.ProcessStartedType#getValue()
     * @see #getProcessStartedType()
     * @generated
     */
	EAttribute getProcessStartedType_Value();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ProcessStartedType#getPercentCompleted <em>Percent Completed</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Percent Completed</em>'.
     * @see net.opengis.wps.ProcessStartedType#getPercentCompleted()
     * @see #getProcessStartedType()
     * @generated
     */
	EAttribute getProcessStartedType_PercentCompleted();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.RequestBaseType <em>Request Base Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Request Base Type</em>'.
     * @see net.opengis.wps.RequestBaseType
     * @generated
     */
	EClass getRequestBaseType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.RequestBaseType#getLanguage <em>Language</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Language</em>'.
     * @see net.opengis.wps.RequestBaseType#getLanguage()
     * @see #getRequestBaseType()
     * @generated
     */
	EAttribute getRequestBaseType_Language();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.RequestBaseType#getService <em>Service</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Service</em>'.
     * @see net.opengis.wps.RequestBaseType#getService()
     * @see #getRequestBaseType()
     * @generated
     */
	EAttribute getRequestBaseType_Service();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.RequestBaseType#getVersion <em>Version</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Version</em>'.
     * @see net.opengis.wps.RequestBaseType#getVersion()
     * @see #getRequestBaseType()
     * @generated
     */
	EAttribute getRequestBaseType_Version();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.RequestBaseType#getBaseUrl <em>Base Url</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Base Url</em>'.
     * @see net.opengis.wps.RequestBaseType#getBaseUrl()
     * @see #getRequestBaseType()
     * @generated
     */
	EAttribute getRequestBaseType_BaseUrl();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ResponseBaseType <em>Response Base Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Response Base Type</em>'.
     * @see net.opengis.wps.ResponseBaseType
     * @generated
     */
	EClass getResponseBaseType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ResponseBaseType#getService <em>Service</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Service</em>'.
     * @see net.opengis.wps.ResponseBaseType#getService()
     * @see #getResponseBaseType()
     * @generated
     */
	EAttribute getResponseBaseType_Service();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ResponseBaseType#getVersion <em>Version</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Version</em>'.
     * @see net.opengis.wps.ResponseBaseType#getVersion()
     * @see #getResponseBaseType()
     * @generated
     */
	EAttribute getResponseBaseType_Version();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ResponseDocumentType <em>Response Document Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Response Document Type</em>'.
     * @see net.opengis.wps.ResponseDocumentType
     * @generated
     */
	EClass getResponseDocumentType();

	/**
     * Returns the meta object for the containment reference list '{@link net.opengis.wps.ResponseDocumentType#getOutput <em>Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Output</em>'.
     * @see net.opengis.wps.ResponseDocumentType#getOutput()
     * @see #getResponseDocumentType()
     * @generated
     */
	EReference getResponseDocumentType_Output();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ResponseDocumentType#isLineage <em>Lineage</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Lineage</em>'.
     * @see net.opengis.wps.ResponseDocumentType#isLineage()
     * @see #getResponseDocumentType()
     * @generated
     */
	EAttribute getResponseDocumentType_Lineage();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ResponseDocumentType#isStatus <em>Status</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Status</em>'.
     * @see net.opengis.wps.ResponseDocumentType#isStatus()
     * @see #getResponseDocumentType()
     * @generated
     */
	EAttribute getResponseDocumentType_Status();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ResponseDocumentType#isStoreExecuteResponse <em>Store Execute Response</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Store Execute Response</em>'.
     * @see net.opengis.wps.ResponseDocumentType#isStoreExecuteResponse()
     * @see #getResponseDocumentType()
     * @generated
     */
	EAttribute getResponseDocumentType_StoreExecuteResponse();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ResponseFormType <em>Response Form Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Response Form Type</em>'.
     * @see net.opengis.wps.ResponseFormType
     * @generated
     */
	EClass getResponseFormType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ResponseFormType#getResponseDocument <em>Response Document</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Response Document</em>'.
     * @see net.opengis.wps.ResponseFormType#getResponseDocument()
     * @see #getResponseFormType()
     * @generated
     */
	EReference getResponseFormType_ResponseDocument();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.ResponseFormType#getRawDataOutput <em>Raw Data Output</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Raw Data Output</em>'.
     * @see net.opengis.wps.ResponseFormType#getRawDataOutput()
     * @see #getResponseFormType()
     * @generated
     */
	EReference getResponseFormType_RawDataOutput();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.StatusType <em>Status Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Status Type</em>'.
     * @see net.opengis.wps.StatusType
     * @generated
     */
	EClass getStatusType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.StatusType#getProcessAccepted <em>Process Accepted</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Process Accepted</em>'.
     * @see net.opengis.wps.StatusType#getProcessAccepted()
     * @see #getStatusType()
     * @generated
     */
	EAttribute getStatusType_ProcessAccepted();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.StatusType#getProcessStarted <em>Process Started</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Started</em>'.
     * @see net.opengis.wps.StatusType#getProcessStarted()
     * @see #getStatusType()
     * @generated
     */
	EReference getStatusType_ProcessStarted();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.StatusType#getProcessPaused <em>Process Paused</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Paused</em>'.
     * @see net.opengis.wps.StatusType#getProcessPaused()
     * @see #getStatusType()
     * @generated
     */
	EReference getStatusType_ProcessPaused();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.StatusType#getProcessSucceeded <em>Process Succeeded</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Process Succeeded</em>'.
     * @see net.opengis.wps.StatusType#getProcessSucceeded()
     * @see #getStatusType()
     * @generated
     */
	EAttribute getStatusType_ProcessSucceeded();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.StatusType#getProcessFailed <em>Process Failed</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Failed</em>'.
     * @see net.opengis.wps.StatusType#getProcessFailed()
     * @see #getStatusType()
     * @generated
     */
	EReference getStatusType_ProcessFailed();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.StatusType#getCreationTime <em>Creation Time</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Creation Time</em>'.
     * @see net.opengis.wps.StatusType#getCreationTime()
     * @see #getStatusType()
     * @generated
     */
	EAttribute getStatusType_CreationTime();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.SupportedComplexDataInputType <em>Supported Complex Data Input Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Supported Complex Data Input Type</em>'.
     * @see net.opengis.wps.SupportedComplexDataInputType
     * @generated
     */
	EClass getSupportedComplexDataInputType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.SupportedComplexDataInputType#getMaximumMegabytes <em>Maximum Megabytes</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Maximum Megabytes</em>'.
     * @see net.opengis.wps.SupportedComplexDataInputType#getMaximumMegabytes()
     * @see #getSupportedComplexDataInputType()
     * @generated
     */
	EAttribute getSupportedComplexDataInputType_MaximumMegabytes();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.SupportedComplexDataType <em>Supported Complex Data Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Supported Complex Data Type</em>'.
     * @see net.opengis.wps.SupportedComplexDataType
     * @generated
     */
	EClass getSupportedComplexDataType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.SupportedComplexDataType#getDefault <em>Default</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Default</em>'.
     * @see net.opengis.wps.SupportedComplexDataType#getDefault()
     * @see #getSupportedComplexDataType()
     * @generated
     */
	EReference getSupportedComplexDataType_Default();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.SupportedComplexDataType#getSupported <em>Supported</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Supported</em>'.
     * @see net.opengis.wps.SupportedComplexDataType#getSupported()
     * @see #getSupportedComplexDataType()
     * @generated
     */
	EReference getSupportedComplexDataType_Supported();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.SupportedCRSsType <em>Supported CR Ss Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Supported CR Ss Type</em>'.
     * @see net.opengis.wps.SupportedCRSsType
     * @generated
     */
	EClass getSupportedCRSsType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.SupportedCRSsType#getDefault <em>Default</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Default</em>'.
     * @see net.opengis.wps.SupportedCRSsType#getDefault()
     * @see #getSupportedCRSsType()
     * @generated
     */
	EReference getSupportedCRSsType_Default();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.SupportedCRSsType#getSupported <em>Supported</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Supported</em>'.
     * @see net.opengis.wps.SupportedCRSsType#getSupported()
     * @see #getSupportedCRSsType()
     * @generated
     */
	EReference getSupportedCRSsType_Supported();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.SupportedUOMsType <em>Supported UO Ms Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Supported UO Ms Type</em>'.
     * @see net.opengis.wps.SupportedUOMsType
     * @generated
     */
	EClass getSupportedUOMsType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.SupportedUOMsType#getDefault <em>Default</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Default</em>'.
     * @see net.opengis.wps.SupportedUOMsType#getDefault()
     * @see #getSupportedUOMsType()
     * @generated
     */
	EReference getSupportedUOMsType_Default();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.SupportedUOMsType#getSupported <em>Supported</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Supported</em>'.
     * @see net.opengis.wps.SupportedUOMsType#getSupported()
     * @see #getSupportedUOMsType()
     * @generated
     */
	EReference getSupportedUOMsType_Supported();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.UOMsType <em>UO Ms Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>UO Ms Type</em>'.
     * @see net.opengis.wps.UOMsType
     * @generated
     */
	EClass getUOMsType();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.ValuesReferenceType <em>Values Reference Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Values Reference Type</em>'.
     * @see net.opengis.wps.ValuesReferenceType
     * @generated
     */
	EClass getValuesReferenceType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ValuesReferenceType#getReference <em>Reference</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Reference</em>'.
     * @see net.opengis.wps.ValuesReferenceType#getReference()
     * @see #getValuesReferenceType()
     * @generated
     */
	EAttribute getValuesReferenceType_Reference();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.ValuesReferenceType#getValuesForm <em>Values Form</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Values Form</em>'.
     * @see net.opengis.wps.ValuesReferenceType#getValuesForm()
     * @see #getValuesReferenceType()
     * @generated
     */
	EAttribute getValuesReferenceType_ValuesForm();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.WPSCapabilitiesType <em>WPS Capabilities Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>WPS Capabilities Type</em>'.
     * @see net.opengis.wps.WPSCapabilitiesType
     * @generated
     */
	EClass getWPSCapabilitiesType();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.WPSCapabilitiesType#getProcessOfferings <em>Process Offerings</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Process Offerings</em>'.
     * @see net.opengis.wps.WPSCapabilitiesType#getProcessOfferings()
     * @see #getWPSCapabilitiesType()
     * @generated
     */
	EReference getWPSCapabilitiesType_ProcessOfferings();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.WPSCapabilitiesType#getLanguages <em>Languages</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Languages</em>'.
     * @see net.opengis.wps.WPSCapabilitiesType#getLanguages()
     * @see #getWPSCapabilitiesType()
     * @generated
     */
	EReference getWPSCapabilitiesType_Languages();

	/**
     * Returns the meta object for the containment reference '{@link net.opengis.wps.WPSCapabilitiesType#getWSDL <em>WSDL</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>WSDL</em>'.
     * @see net.opengis.wps.WPSCapabilitiesType#getWSDL()
     * @see #getWPSCapabilitiesType()
     * @generated
     */
	EReference getWPSCapabilitiesType_WSDL();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.WPSCapabilitiesType#getService <em>Service</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Service</em>'.
     * @see net.opengis.wps.WPSCapabilitiesType#getService()
     * @see #getWPSCapabilitiesType()
     * @generated
     */
	EAttribute getWPSCapabilitiesType_Service();

	/**
     * Returns the meta object for class '{@link net.opengis.wps.WSDLType <em>WSDL Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>WSDL Type</em>'.
     * @see net.opengis.wps.WSDLType
     * @generated
     */
	EClass getWSDLType();

	/**
     * Returns the meta object for the attribute '{@link net.opengis.wps.WSDLType#getHref <em>Href</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Href</em>'.
     * @see net.opengis.wps.WSDLType#getHref()
     * @see #getWSDLType()
     * @generated
     */
	EAttribute getWSDLType_Href();

	/**
     * Returns the meta object for class '{@link javax.measure.unit.Unit <em>Unit</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for class '<em>Unit</em>'.
     * @see javax.measure.unit.Unit
     * @model instanceClass="javax.measure.unit.Unit"
     * @generated
     */
	EClass getUnit();

	/**
     * Returns the meta object for enum '{@link net.opengis.wps.MethodType <em>Method Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for enum '<em>Method Type</em>'.
     * @see net.opengis.wps.MethodType
     * @generated
     */
	EEnum getMethodType();

	/**
     * Returns the meta object for data type '{@link net.opengis.wps.MethodType <em>Method Type Object</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for data type '<em>Method Type Object</em>'.
     * @see net.opengis.wps.MethodType
     * @model instanceClass="net.opengis.wps.MethodType"
     *        extendedMetaData="name='method_._type:Object' baseType='method_._type'"
     * @generated
     */
	EDataType getMethodTypeObject();

	/**
     * Returns the meta object for data type '{@link java.math.BigInteger <em>Percent Completed Type</em>}'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the meta object for data type '<em>Percent Completed Type</em>'.
     * @see java.math.BigInteger
     * @model instanceClass="java.math.BigInteger"
     *        extendedMetaData="name='percentCompleted_._type' baseType='http://www.eclipse.org/emf/2003/XMLType#integer' minInclusive='0' maxInclusive='99'"
     * @generated
     */
	EDataType getPercentCompletedType();

	/**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
	WpsFactory getWpsFactory();

} //WpsPackage
