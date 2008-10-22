/**
 * <copyright>
 * </copyright>
 *
 * $Id: GetCapabilitiesType.java 30778 2008-06-20 17:56:03Z gdavis $
 */
package net.opengis.wps;

import net.opengis.ows11.AcceptVersionsType;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Get Capabilities Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.GetCapabilitiesType#getAcceptVersions <em>Accept Versions</em>}</li>
 *   <li>{@link net.opengis.wps.GetCapabilitiesType#getLanguage <em>Language</em>}</li>
 *   <li>{@link net.opengis.wps.GetCapabilitiesType#getService <em>Service</em>}</li>
 *   <li>{@link net.opengis.wps.GetCapabilitiesType#getBaseUrl <em>Base Url</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getGetCapabilitiesType()
 * @model extendedMetaData="name='GetCapabilities_._type' kind='elementOnly'"
 * @generated
 */
public interface GetCapabilitiesType extends EObject {
	/**
	 * Returns the value of the '<em><b>Accept Versions</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When omitted, server shall return latest supported version.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Accept Versions</em>' containment reference.
	 * @see #setAcceptVersions(AcceptVersionsType)
	 * @see net.opengis.wps.WpsPackage#getGetCapabilitiesType_AcceptVersions()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='AcceptVersions' namespace='##targetNamespace'"
	 * @generated
	 */
	AcceptVersionsType getAcceptVersions();

	/**
	 * Sets the value of the '{@link net.opengis.wps.GetCapabilitiesType#getAcceptVersions <em>Accept Versions</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Accept Versions</em>' containment reference.
	 * @see #getAcceptVersions()
	 * @generated
	 */
	void setAcceptVersions(AcceptVersionsType value);

	/**
	 * Returns the value of the '<em><b>Language</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * RFC 4646 language code of the human-readable text (e.g. "en-CA").
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Language</em>' attribute.
	 * @see #setLanguage(String)
	 * @see net.opengis.wps.WpsPackage#getGetCapabilitiesType_Language()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='language'"
	 * @generated
	 */
	String getLanguage();

	/**
	 * Sets the value of the '{@link net.opengis.wps.GetCapabilitiesType#getLanguage <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Language</em>' attribute.
	 * @see #getLanguage()
	 * @generated
	 */
	void setLanguage(String value);

	/**
	 * Returns the value of the '<em><b>Service</b></em>' attribute.
	 * The default value is <code>"WPS"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OGC service type identifier (WPS).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Service</em>' attribute.
	 * @see #isSetService()
	 * @see #unsetService()
	 * @see #setService(String)
	 * @see net.opengis.wps.WpsPackage#getGetCapabilitiesType_Service()
	 * @model default="WPS" unsettable="true" dataType="net.opengis.ows11.ServiceType" required="true"
	 *        extendedMetaData="kind='attribute' name='service'"
	 * @generated
	 */
	String getService();

	/**
	 * Sets the value of the '{@link net.opengis.wps.GetCapabilitiesType#getService <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Service</em>' attribute.
	 * @see #isSetService()
	 * @see #unsetService()
	 * @see #getService()
	 * @generated
	 */
	void setService(String value);

	/**
	 * Unsets the value of the '{@link net.opengis.wps.GetCapabilitiesType#getService <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetService()
	 * @see #getService()
	 * @see #setService(String)
	 * @generated
	 */
	void unsetService();

	/**
	 * Returns whether the value of the '{@link net.opengis.wps.GetCapabilitiesType#getService <em>Service</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Service</em>' attribute is set.
	 * @see #unsetService()
	 * @see #getService()
	 * @see #setService(String)
	 * @generated
	 */
	boolean isSetService();
	
	/**
	 * Returns the base url of the request.
	 * <p>
	 * This is a special property added manually.
	 * </p>
	 * @model
	 */
	String getBaseUrl();

	/**
	 * Sets the value of the '{@link net.opengis.wps.GetCapabilitiesType#getBaseUrl <em>Base Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Base Url</em>' attribute.
	 * @see #getBaseUrl()
	 * @generated
	 */
	void setBaseUrl(String value);	

} // GetCapabilitiesType
