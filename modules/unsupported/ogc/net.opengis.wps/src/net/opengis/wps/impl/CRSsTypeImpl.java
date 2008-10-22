/**
 * <copyright>
 * </copyright>
 *
 * $Id: CRSsTypeImpl.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps.impl;

import java.util.Collection;

import net.opengis.wps.CRSsType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>CR Ss Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps.impl.CRSsTypeImpl#getCRS <em>CRS</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CRSsTypeImpl extends EObjectImpl implements CRSsType {
	/**
	 * The default value of the '{@link #getCRS() <em>CRS</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCRS()
	 * @generated
	 * @ordered
	 */
	protected static final String CRS_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getCRS() <em>CRS</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCRS()
	 * @generated
	 * @ordered
	 */
	protected String cRS = CRS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CRSsTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getCRSsType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCRS() {
		return cRS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCRS(String newCRS) {
		String oldCRS = cRS;
		cRS = newCRS;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.CR_SS_TYPE__CRS, oldCRS, cRS));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case WpsPackage.CR_SS_TYPE__CRS:
				return getCRS();
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
			case WpsPackage.CR_SS_TYPE__CRS:
				setCRS((String)newValue);
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
			case WpsPackage.CR_SS_TYPE__CRS:
				setCRS(CRS_EDEFAULT);
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
			case WpsPackage.CR_SS_TYPE__CRS:
				return CRS_EDEFAULT == null ? cRS != null : !CRS_EDEFAULT.equals(cRS);
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
		result.append(" (cRS: ");
		result.append(cRS);
		result.append(')');
		return result.toString();
	}

} //CRSsTypeImpl
