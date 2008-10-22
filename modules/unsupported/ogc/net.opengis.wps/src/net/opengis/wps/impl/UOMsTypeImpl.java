/**
 * <copyright>
 * </copyright>
 *
 * $Id: UOMsTypeImpl.java 30760 2008-06-18 14:28:24Z desruisseaux $
 */
package net.opengis.wps.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.measure.unit.BaseUnit;
import javax.measure.unit.Unit;

import net.opengis.wps.UOMsType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>UO Ms Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class UOMsTypeImpl extends EObjectImpl implements UOMsType {
	/**
	 * The cached value of the '{@link #getUOM() <em>UOM</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUOM()
	 */
	protected List uOM;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UOMsTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getUOMsType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 */
	public List getUOM() {
		if (uOM == null) {
			uOM = new ArrayList();
		}
		return uOM;
	}

} //UOMsTypeImpl
