/**
 * <copyright>
 * </copyright>
 *
 * $Id: WpsFactoryImpl.java 30810 2008-06-25 17:29:43Z jdeolive $
 */
package net.opengis.wps.impl;

import java.math.BigInteger;

import net.opengis.wps.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class WpsFactoryImpl extends EFactoryImpl implements WpsFactory {
	/**
     * Creates the default factory implementation.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public static WpsFactory init() {
        try {
            WpsFactory theWpsFactory = (WpsFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.opengis.net/wps/1.0.0"); 
            if (theWpsFactory != null) {
                return theWpsFactory;
            }
        }
        catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new WpsFactoryImpl();
    }

	/**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public WpsFactoryImpl() {
        super();
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case WpsPackage.BODY_REFERENCE_TYPE: return createBodyReferenceType();
            case WpsPackage.COMPLEX_DATA_COMBINATIONS_TYPE: return createComplexDataCombinationsType();
            case WpsPackage.COMPLEX_DATA_COMBINATION_TYPE: return createComplexDataCombinationType();
            case WpsPackage.COMPLEX_DATA_DESCRIPTION_TYPE: return createComplexDataDescriptionType();
            case WpsPackage.COMPLEX_DATA_TYPE: return createComplexDataType();
            case WpsPackage.CR_SS_TYPE: return createCRSsType();
            case WpsPackage.DATA_INPUTS_TYPE: return createDataInputsType();
            case WpsPackage.DATA_INPUTS_TYPE1: return createDataInputsType1();
            case WpsPackage.DATA_TYPE: return createDataType();
            case WpsPackage.DEFAULT_TYPE: return createDefaultType();
            case WpsPackage.DEFAULT_TYPE1: return createDefaultType1();
            case WpsPackage.DEFAULT_TYPE2: return createDefaultType2();
            case WpsPackage.DESCRIBE_PROCESS_TYPE: return createDescribeProcessType();
            case WpsPackage.DESCRIPTION_TYPE: return createDescriptionType();
            case WpsPackage.DOCUMENT_OUTPUT_DEFINITION_TYPE: return createDocumentOutputDefinitionType();
            case WpsPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case WpsPackage.EXECUTE_RESPONSE_TYPE: return createExecuteResponseType();
            case WpsPackage.EXECUTE_TYPE: return createExecuteType();
            case WpsPackage.GET_CAPABILITIES_TYPE: return createGetCapabilitiesType();
            case WpsPackage.HEADER_TYPE: return createHeaderType();
            case WpsPackage.INPUT_DESCRIPTION_TYPE: return createInputDescriptionType();
            case WpsPackage.INPUT_REFERENCE_TYPE: return createInputReferenceType();
            case WpsPackage.INPUT_TYPE: return createInputType();
            case WpsPackage.LANGUAGES_TYPE: return createLanguagesType();
            case WpsPackage.LANGUAGES_TYPE1: return createLanguagesType1();
            case WpsPackage.LITERAL_DATA_TYPE: return createLiteralDataType();
            case WpsPackage.LITERAL_INPUT_TYPE: return createLiteralInputType();
            case WpsPackage.LITERAL_OUTPUT_TYPE: return createLiteralOutputType();
            case WpsPackage.OUTPUT_DATA_TYPE: return createOutputDataType();
            case WpsPackage.OUTPUT_DEFINITIONS_TYPE: return createOutputDefinitionsType();
            case WpsPackage.OUTPUT_DEFINITION_TYPE: return createOutputDefinitionType();
            case WpsPackage.OUTPUT_DESCRIPTION_TYPE: return createOutputDescriptionType();
            case WpsPackage.OUTPUT_REFERENCE_TYPE: return createOutputReferenceType();
            case WpsPackage.PROCESS_BRIEF_TYPE: return createProcessBriefType();
            case WpsPackage.PROCESS_DESCRIPTIONS_TYPE: return createProcessDescriptionsType();
            case WpsPackage.PROCESS_DESCRIPTION_TYPE: return createProcessDescriptionType();
            case WpsPackage.PROCESS_FAILED_TYPE: return createProcessFailedType();
            case WpsPackage.PROCESS_OFFERINGS_TYPE: return createProcessOfferingsType();
            case WpsPackage.PROCESS_OUTPUTS_TYPE: return createProcessOutputsType();
            case WpsPackage.PROCESS_OUTPUTS_TYPE1: return createProcessOutputsType1();
            case WpsPackage.PROCESS_STARTED_TYPE: return createProcessStartedType();
            case WpsPackage.REQUEST_BASE_TYPE: return createRequestBaseType();
            case WpsPackage.RESPONSE_BASE_TYPE: return createResponseBaseType();
            case WpsPackage.RESPONSE_DOCUMENT_TYPE: return createResponseDocumentType();
            case WpsPackage.RESPONSE_FORM_TYPE: return createResponseFormType();
            case WpsPackage.STATUS_TYPE: return createStatusType();
            case WpsPackage.SUPPORTED_COMPLEX_DATA_INPUT_TYPE: return createSupportedComplexDataInputType();
            case WpsPackage.SUPPORTED_COMPLEX_DATA_TYPE: return createSupportedComplexDataType();
            case WpsPackage.SUPPORTED_CR_SS_TYPE: return createSupportedCRSsType();
            case WpsPackage.SUPPORTED_UO_MS_TYPE: return createSupportedUOMsType();
            case WpsPackage.UO_MS_TYPE: return createUOMsType();
            case WpsPackage.VALUES_REFERENCE_TYPE: return createValuesReferenceType();
            case WpsPackage.WPS_CAPABILITIES_TYPE: return createWPSCapabilitiesType();
            case WpsPackage.WSDL_TYPE: return createWSDLType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Object createFromString(EDataType eDataType, String initialValue) {
        switch (eDataType.getClassifierID()) {
            case WpsPackage.METHOD_TYPE:
                return createMethodTypeFromString(eDataType, initialValue);
            case WpsPackage.METHOD_TYPE_OBJECT:
                return createMethodTypeObjectFromString(eDataType, initialValue);
            case WpsPackage.PERCENT_COMPLETED_TYPE:
                return createPercentCompletedTypeFromString(eDataType, initialValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public String convertToString(EDataType eDataType, Object instanceValue) {
        switch (eDataType.getClassifierID()) {
            case WpsPackage.METHOD_TYPE:
                return convertMethodTypeToString(eDataType, instanceValue);
            case WpsPackage.METHOD_TYPE_OBJECT:
                return convertMethodTypeObjectToString(eDataType, instanceValue);
            case WpsPackage.PERCENT_COMPLETED_TYPE:
                return convertPercentCompletedTypeToString(eDataType, instanceValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public BodyReferenceType createBodyReferenceType() {
        BodyReferenceTypeImpl bodyReferenceType = new BodyReferenceTypeImpl();
        return bodyReferenceType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ComplexDataCombinationsType createComplexDataCombinationsType() {
        ComplexDataCombinationsTypeImpl complexDataCombinationsType = new ComplexDataCombinationsTypeImpl();
        return complexDataCombinationsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ComplexDataCombinationType createComplexDataCombinationType() {
        ComplexDataCombinationTypeImpl complexDataCombinationType = new ComplexDataCombinationTypeImpl();
        return complexDataCombinationType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ComplexDataDescriptionType createComplexDataDescriptionType() {
        ComplexDataDescriptionTypeImpl complexDataDescriptionType = new ComplexDataDescriptionTypeImpl();
        return complexDataDescriptionType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ComplexDataType createComplexDataType() {
        ComplexDataTypeImpl complexDataType = new ComplexDataTypeImpl();
        return complexDataType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public CRSsType createCRSsType() {
        CRSsTypeImpl crSsType = new CRSsTypeImpl();
        return crSsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DataInputsType createDataInputsType() {
        DataInputsTypeImpl dataInputsType = new DataInputsTypeImpl();
        return dataInputsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DataInputsType1 createDataInputsType1() {
        DataInputsType1Impl dataInputsType1 = new DataInputsType1Impl();
        return dataInputsType1;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DataType createDataType() {
        DataTypeImpl dataType = new DataTypeImpl();
        return dataType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DefaultType createDefaultType() {
        DefaultTypeImpl defaultType = new DefaultTypeImpl();
        return defaultType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DefaultType1 createDefaultType1() {
        DefaultType1Impl defaultType1 = new DefaultType1Impl();
        return defaultType1;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DefaultType2 createDefaultType2() {
        DefaultType2Impl defaultType2 = new DefaultType2Impl();
        return defaultType2;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DescribeProcessType createDescribeProcessType() {
        DescribeProcessTypeImpl describeProcessType = new DescribeProcessTypeImpl();
        return describeProcessType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DescriptionType createDescriptionType() {
        DescriptionTypeImpl descriptionType = new DescriptionTypeImpl();
        return descriptionType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DocumentOutputDefinitionType createDocumentOutputDefinitionType() {
        DocumentOutputDefinitionTypeImpl documentOutputDefinitionType = new DocumentOutputDefinitionTypeImpl();
        return documentOutputDefinitionType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public DocumentRoot createDocumentRoot() {
        DocumentRootImpl documentRoot = new DocumentRootImpl();
        return documentRoot;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ExecuteResponseType createExecuteResponseType() {
        ExecuteResponseTypeImpl executeResponseType = new ExecuteResponseTypeImpl();
        return executeResponseType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ExecuteType createExecuteType() {
        ExecuteTypeImpl executeType = new ExecuteTypeImpl();
        return executeType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public GetCapabilitiesType createGetCapabilitiesType() {
        GetCapabilitiesTypeImpl getCapabilitiesType = new GetCapabilitiesTypeImpl();
        return getCapabilitiesType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public HeaderType createHeaderType() {
        HeaderTypeImpl headerType = new HeaderTypeImpl();
        return headerType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public InputDescriptionType createInputDescriptionType() {
        InputDescriptionTypeImpl inputDescriptionType = new InputDescriptionTypeImpl();
        return inputDescriptionType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public InputReferenceType createInputReferenceType() {
        InputReferenceTypeImpl inputReferenceType = new InputReferenceTypeImpl();
        return inputReferenceType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public InputType createInputType() {
        InputTypeImpl inputType = new InputTypeImpl();
        return inputType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public LanguagesType createLanguagesType() {
        LanguagesTypeImpl languagesType = new LanguagesTypeImpl();
        return languagesType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public LanguagesType1 createLanguagesType1() {
        LanguagesType1Impl languagesType1 = new LanguagesType1Impl();
        return languagesType1;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public LiteralDataType createLiteralDataType() {
        LiteralDataTypeImpl literalDataType = new LiteralDataTypeImpl();
        return literalDataType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public LiteralInputType createLiteralInputType() {
        LiteralInputTypeImpl literalInputType = new LiteralInputTypeImpl();
        return literalInputType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public LiteralOutputType createLiteralOutputType() {
        LiteralOutputTypeImpl literalOutputType = new LiteralOutputTypeImpl();
        return literalOutputType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public OutputDataType createOutputDataType() {
        OutputDataTypeImpl outputDataType = new OutputDataTypeImpl();
        return outputDataType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public OutputDefinitionsType createOutputDefinitionsType() {
        OutputDefinitionsTypeImpl outputDefinitionsType = new OutputDefinitionsTypeImpl();
        return outputDefinitionsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public OutputDefinitionType createOutputDefinitionType() {
        OutputDefinitionTypeImpl outputDefinitionType = new OutputDefinitionTypeImpl();
        return outputDefinitionType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public OutputDescriptionType createOutputDescriptionType() {
        OutputDescriptionTypeImpl outputDescriptionType = new OutputDescriptionTypeImpl();
        return outputDescriptionType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public OutputReferenceType createOutputReferenceType() {
        OutputReferenceTypeImpl outputReferenceType = new OutputReferenceTypeImpl();
        return outputReferenceType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessBriefType createProcessBriefType() {
        ProcessBriefTypeImpl processBriefType = new ProcessBriefTypeImpl();
        return processBriefType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessDescriptionsType createProcessDescriptionsType() {
        ProcessDescriptionsTypeImpl processDescriptionsType = new ProcessDescriptionsTypeImpl();
        return processDescriptionsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessDescriptionType createProcessDescriptionType() {
        ProcessDescriptionTypeImpl processDescriptionType = new ProcessDescriptionTypeImpl();
        return processDescriptionType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessFailedType createProcessFailedType() {
        ProcessFailedTypeImpl processFailedType = new ProcessFailedTypeImpl();
        return processFailedType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessOfferingsType createProcessOfferingsType() {
        ProcessOfferingsTypeImpl processOfferingsType = new ProcessOfferingsTypeImpl();
        return processOfferingsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessOutputsType createProcessOutputsType() {
        ProcessOutputsTypeImpl processOutputsType = new ProcessOutputsTypeImpl();
        return processOutputsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessOutputsType1 createProcessOutputsType1() {
        ProcessOutputsType1Impl processOutputsType1 = new ProcessOutputsType1Impl();
        return processOutputsType1;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ProcessStartedType createProcessStartedType() {
        ProcessStartedTypeImpl processStartedType = new ProcessStartedTypeImpl();
        return processStartedType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public RequestBaseType createRequestBaseType() {
        RequestBaseTypeImpl requestBaseType = new RequestBaseTypeImpl();
        return requestBaseType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ResponseBaseType createResponseBaseType() {
        ResponseBaseTypeImpl responseBaseType = new ResponseBaseTypeImpl();
        return responseBaseType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ResponseDocumentType createResponseDocumentType() {
        ResponseDocumentTypeImpl responseDocumentType = new ResponseDocumentTypeImpl();
        return responseDocumentType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ResponseFormType createResponseFormType() {
        ResponseFormTypeImpl responseFormType = new ResponseFormTypeImpl();
        return responseFormType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public StatusType createStatusType() {
        StatusTypeImpl statusType = new StatusTypeImpl();
        return statusType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public SupportedComplexDataInputType createSupportedComplexDataInputType() {
        SupportedComplexDataInputTypeImpl supportedComplexDataInputType = new SupportedComplexDataInputTypeImpl();
        return supportedComplexDataInputType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public SupportedComplexDataType createSupportedComplexDataType() {
        SupportedComplexDataTypeImpl supportedComplexDataType = new SupportedComplexDataTypeImpl();
        return supportedComplexDataType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public SupportedCRSsType createSupportedCRSsType() {
        SupportedCRSsTypeImpl supportedCRSsType = new SupportedCRSsTypeImpl();
        return supportedCRSsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public SupportedUOMsType createSupportedUOMsType() {
        SupportedUOMsTypeImpl supportedUOMsType = new SupportedUOMsTypeImpl();
        return supportedUOMsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public UOMsType createUOMsType() {
        UOMsTypeImpl uoMsType = new UOMsTypeImpl();
        return uoMsType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ValuesReferenceType createValuesReferenceType() {
        ValuesReferenceTypeImpl valuesReferenceType = new ValuesReferenceTypeImpl();
        return valuesReferenceType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public WPSCapabilitiesType createWPSCapabilitiesType() {
        WPSCapabilitiesTypeImpl wpsCapabilitiesType = new WPSCapabilitiesTypeImpl();
        return wpsCapabilitiesType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public WSDLType createWSDLType() {
        WSDLTypeImpl wsdlType = new WSDLTypeImpl();
        return wsdlType;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public MethodType createMethodTypeFromString(EDataType eDataType, String initialValue) {
        MethodType result = MethodType.get(initialValue);
        if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
        return result;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public String convertMethodTypeToString(EDataType eDataType, Object instanceValue) {
        return instanceValue == null ? null : instanceValue.toString();
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public MethodType createMethodTypeObjectFromString(EDataType eDataType, String initialValue) {
        return createMethodTypeFromString(WpsPackage.eINSTANCE.getMethodType(), initialValue);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public String convertMethodTypeObjectToString(EDataType eDataType, Object instanceValue) {
        return convertMethodTypeToString(WpsPackage.eINSTANCE.getMethodType(), instanceValue);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public BigInteger createPercentCompletedTypeFromString(EDataType eDataType, String initialValue) {
        return (BigInteger)XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.Literals.INTEGER, initialValue);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public String convertPercentCompletedTypeToString(EDataType eDataType, Object instanceValue) {
        return XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.Literals.INTEGER, instanceValue);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public WpsPackage getWpsPackage() {
        return (WpsPackage)getEPackage();
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
	public static WpsPackage getPackage() {
        return WpsPackage.eINSTANCE;
    }

} //WpsFactoryImpl
