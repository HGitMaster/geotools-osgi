/**
 * <copyright>
 * </copyright>
 *
 * $Id: GetCapabilitiesTypeImpl.java 30778 2008-06-20 17:56:03Z gdavis $
 */
package net.opengis.wps.impl;

import net.opengis.ows11.AcceptVersionsType;

import net.opengis.wps.GetCapabilitiesType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Capabilities Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps.impl.GetCapabilitiesTypeImpl#getAcceptVersions <em>Accept Versions</em>}</li>
 *   <li>{@link net.opengis.wps.impl.GetCapabilitiesTypeImpl#getLanguage <em>Language</em>}</li>
 *   <li>{@link net.opengis.wps.impl.GetCapabilitiesTypeImpl#getService <em>Service</em>}</li>
 *   <li>{@link net.opengis.wps.impl.GetCapabilitiesTypeImpl#getBaseUrl <em>Base Url</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GetCapabilitiesTypeImpl extends EObjectImpl implements GetCapabilitiesType {
	/**
	 * The cached value of the '{@link #getAcceptVersions() <em>Accept Versions</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAcceptVersions()
	 * @generated
	 * @ordered
	 */
	protected AcceptVersionsType acceptVersions;

	/**
	 * The default value of the '{@link #getLanguage() <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLanguage()
	 * @generated
	 * @ordered
	 */
	protected static final String LANGUAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLanguage() <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLanguage()
	 * @generated
	 * @ordered
	 */
	protected String language = LANGUAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getService() <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getService()
	 * @generated
	 * @ordered
	 */
	protected static final String SERVICE_EDEFAULT = "WPS";

	/**
	 * The cached value of the '{@link #getService() <em>Service</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getService()
	 * @generated
	 * @ordered
	 */
	protected String service = SERVICE_EDEFAULT;

	/**
	 * This is true if the Service attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean serviceESet;

	/**
	 * The default value of the '{@link #getBaseUrl() <em>Base Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String BASE_URL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBaseUrl() <em>Base Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseUrl()
	 * @generated
	 * @ordered
	 */
	protected String baseUrl = BASE_URL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GetCapabilitiesTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getGetCapabilitiesType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AcceptVersionsType getAcceptVersions() {
		return acceptVersions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAcceptVersions(AcceptVersionsType newAcceptVersions, NotificationChain msgs) {
		AcceptVersionsType oldAcceptVersions = acceptVersions;
		acceptVersions = newAcceptVersions;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS, oldAcceptVersions, newAcceptVersions);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAcceptVersions(AcceptVersionsType newAcceptVersions) {
		if (newAcceptVersions != acceptVersions) {
			NotificationChain msgs = null;
			if (acceptVersions != null)
				msgs = ((InternalEObject)acceptVersions).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS, null, msgs);
			if (newAcceptVersions != null)
				msgs = ((InternalEObject)newAcceptVersions).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS, null, msgs);
			msgs = basicSetAcceptVersions(newAcceptVersions, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS, newAcceptVersions, newAcceptVersions));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLanguage(String newLanguage) {
		String oldLanguage = language;
		language = newLanguage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.GET_CAPABILITIES_TYPE__LANGUAGE, oldLanguage, language));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getService() {
		return service;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setService(String newService) {
		String oldService = service;
		service = newService;
		boolean oldServiceESet = serviceESet;
		serviceESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.GET_CAPABILITIES_TYPE__SERVICE, oldService, service, !oldServiceESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetService() {
		String oldService = service;
		boolean oldServiceESet = serviceESet;
		service = SERVICE_EDEFAULT;
		serviceESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, WpsPackage.GET_CAPABILITIES_TYPE__SERVICE, oldService, SERVICE_EDEFAULT, oldServiceESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetService() {
		return serviceESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBaseUrl(String newBaseUrl) {
		String oldBaseUrl = baseUrl;
		baseUrl = newBaseUrl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.GET_CAPABILITIES_TYPE__BASE_URL, oldBaseUrl, baseUrl));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS:
				return basicSetAcceptVersions(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS:
				return getAcceptVersions();
			case WpsPackage.GET_CAPABILITIES_TYPE__LANGUAGE:
				return getLanguage();
			case WpsPackage.GET_CAPABILITIES_TYPE__SERVICE:
				return getService();
			case WpsPackage.GET_CAPABILITIES_TYPE__BASE_URL:
				return getBaseUrl();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS:
				setAcceptVersions((AcceptVersionsType)newValue);
				return;
			case WpsPackage.GET_CAPABILITIES_TYPE__LANGUAGE:
				setLanguage((String)newValue);
				return;
			case WpsPackage.GET_CAPABILITIES_TYPE__SERVICE:
				setService((String)newValue);
				return;
			case WpsPackage.GET_CAPABILITIES_TYPE__BASE_URL:
				setBaseUrl((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eUnset(int featureID) {
		switch (featureID) {
			case WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS:
				setAcceptVersions((AcceptVersionsType)null);
				return;
			case WpsPackage.GET_CAPABILITIES_TYPE__LANGUAGE:
				setLanguage(LANGUAGE_EDEFAULT);
				return;
			case WpsPackage.GET_CAPABILITIES_TYPE__SERVICE:
				unsetService();
				return;
			case WpsPackage.GET_CAPABILITIES_TYPE__BASE_URL:
				setBaseUrl(BASE_URL_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case WpsPackage.GET_CAPABILITIES_TYPE__ACCEPT_VERSIONS:
				return acceptVersions != null;
			case WpsPackage.GET_CAPABILITIES_TYPE__LANGUAGE:
				return LANGUAGE_EDEFAULT == null ? language != null : !LANGUAGE_EDEFAULT.equals(language);
			case WpsPackage.GET_CAPABILITIES_TYPE__SERVICE:
				return isSetService();
			case WpsPackage.GET_CAPABILITIES_TYPE__BASE_URL:
				return BASE_URL_EDEFAULT == null ? baseUrl != null : !BASE_URL_EDEFAULT.equals(baseUrl);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (language: ");
		result.append(language);
		result.append(", service: ");
		if (serviceESet) result.append(service); else result.append("<unset>");
		result.append(", baseUrl: ");
		result.append(baseUrl);
		result.append(')');
		return result.toString();
	}

} //GetCapabilitiesTypeImpl
