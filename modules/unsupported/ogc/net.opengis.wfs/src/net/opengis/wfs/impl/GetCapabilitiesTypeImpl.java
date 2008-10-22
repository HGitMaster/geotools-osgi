/**
 * <copyright>
 * </copyright>
 *
 * $Id: GetCapabilitiesTypeImpl.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.wfs.impl;

import net.opengis.wfs.GetCapabilitiesType;
import net.opengis.wfs.WfsPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Capabilities Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wfs.impl.GetCapabilitiesTypeImpl#getService <em>Service</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GetCapabilitiesTypeImpl extends net.opengis.ows10.impl.GetCapabilitiesTypeImpl implements GetCapabilitiesType {
	/**
     * The default value of the '{@link #getService() <em>Service</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getService()
     * @generated
     * @ordered
     */
	protected static final String SERVICE_EDEFAULT = "WFS";

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
        return WfsPackage.Literals.GET_CAPABILITIES_TYPE;
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
            eNotify(new ENotificationImpl(this, Notification.SET, WfsPackage.GET_CAPABILITIES_TYPE__SERVICE, oldService, service, !oldServiceESet));
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
            eNotify(new ENotificationImpl(this, Notification.UNSET, WfsPackage.GET_CAPABILITIES_TYPE__SERVICE, oldService, SERVICE_EDEFAULT, oldServiceESet));
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
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case WfsPackage.GET_CAPABILITIES_TYPE__SERVICE:
                return getService();
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
            case WfsPackage.GET_CAPABILITIES_TYPE__SERVICE:
                setService((String)newValue);
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
            case WfsPackage.GET_CAPABILITIES_TYPE__SERVICE:
                unsetService();
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
            case WfsPackage.GET_CAPABILITIES_TYPE__SERVICE:
                return isSetService();
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
        result.append(" (service: ");
        if (serviceESet) result.append(service); else result.append("<unset>");
        result.append(')');
        return result.toString();
    }

} //GetCapabilitiesTypeImpl