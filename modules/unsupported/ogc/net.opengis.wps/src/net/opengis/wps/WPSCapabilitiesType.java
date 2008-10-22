/**
 * <copyright>
 * </copyright>
 *
 * $Id: WPSCapabilitiesType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import net.opengis.ows11.CapabilitiesBaseType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>WPS Capabilities Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.WPSCapabilitiesType#getProcessOfferings <em>Process Offerings</em>}</li>
 *   <li>{@link net.opengis.wps.WPSCapabilitiesType#getLanguages <em>Languages</em>}</li>
 *   <li>{@link net.opengis.wps.WPSCapabilitiesType#getWSDL <em>WSDL</em>}</li>
 *   <li>{@link net.opengis.wps.WPSCapabilitiesType#getService <em>Service</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getWPSCapabilitiesType()
 * @model extendedMetaData="name='WPSCapabilitiesType' kind='elementOnly'"
 * @generated
 */
public interface WPSCapabilitiesType extends CapabilitiesBaseType {
	/**
	 * Returns the value of the '<em><b>Process Offerings</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List of brief descriptions of the processes offered by this WPS server.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Process Offerings</em>' containment reference.
	 * @see #setProcessOfferings(ProcessOfferingsType)
	 * @see net.opengis.wps.WpsPackage#getWPSCapabilitiesType_ProcessOfferings()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='ProcessOfferings' namespace='##targetNamespace'"
	 * @generated
	 */
	ProcessOfferingsType getProcessOfferings();

	/**
	 * Sets the value of the '{@link net.opengis.wps.WPSCapabilitiesType#getProcessOfferings <em>Process Offerings</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Process Offerings</em>' containment reference.
	 * @see #getProcessOfferings()
	 * @generated
	 */
	void setProcessOfferings(ProcessOfferingsType value);

	/**
	 * Returns the value of the '<em><b>Languages</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List of the default and other languages supported by this service.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Languages</em>' containment reference.
	 * @see #setLanguages(LanguagesType1)
	 * @see net.opengis.wps.WpsPackage#getWPSCapabilitiesType_Languages()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Languages' namespace='##targetNamespace'"
	 * @generated
	 */
	LanguagesType1 getLanguages();

	/**
	 * Sets the value of the '{@link net.opengis.wps.WPSCapabilitiesType#getLanguages <em>Languages</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Languages</em>' containment reference.
	 * @see #getLanguages()
	 * @generated
	 */
	void setLanguages(LanguagesType1 value);

	/**
	 * Returns the value of the '<em><b>WSDL</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Location of a WSDL document which describes the entire service.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>WSDL</em>' containment reference.
	 * @see #setWSDL(WSDLType)
	 * @see net.opengis.wps.WpsPackage#getWPSCapabilitiesType_WSDL()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='WSDL' namespace='##targetNamespace'"
	 * @generated
	 */
	WSDLType getWSDL();

	/**
	 * Sets the value of the '{@link net.opengis.wps.WPSCapabilitiesType#getWSDL <em>WSDL</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>WSDL</em>' containment reference.
	 * @see #getWSDL()
	 * @generated
	 */
	void setWSDL(WSDLType value);

	/**
	 * Returns the value of the '<em><b>Service</b></em>' attribute.
	 * The default value is <code>"WPS"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Service</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Service</em>' attribute.
	 * @see #isSetService()
	 * @see #unsetService()
	 * @see #setService(Object)
	 * @see net.opengis.wps.WpsPackage#getWPSCapabilitiesType_Service()
	 * @model default="WPS" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.AnySimpleType" required="true"
	 *        extendedMetaData="kind='attribute' name='service'"
	 * @generated
	 */
	Object getService();

	/**
	 * Sets the value of the '{@link net.opengis.wps.WPSCapabilitiesType#getService <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Service</em>' attribute.
	 * @see #isSetService()
	 * @see #unsetService()
	 * @see #getService()
	 * @generated
	 */
	void setService(Object value);

	/**
	 * Unsets the value of the '{@link net.opengis.wps.WPSCapabilitiesType#getService <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetService()
	 * @see #getService()
	 * @see #setService(Object)
	 * @generated
	 */
	void unsetService();

	/**
	 * Returns whether the value of the '{@link net.opengis.wps.WPSCapabilitiesType#getService <em>Service</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Service</em>' attribute is set.
	 * @see #unsetService()
	 * @see #getService()
	 * @see #setService(Object)
	 * @generated
	 */
	boolean isSetService();

} // WPSCapabilitiesType
