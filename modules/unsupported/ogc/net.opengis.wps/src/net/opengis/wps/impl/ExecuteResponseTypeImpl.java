/**
 * <copyright>
 * </copyright>
 *
 * $Id: ExecuteResponseTypeImpl.java 29861 2008-04-09 04:55:54Z jdeolive $
 */
package net.opengis.wps.impl;

import net.opengis.wps.DataInputsType1;
import net.opengis.wps.ExecuteResponseType;
import net.opengis.wps.OutputDefinitionsType;
import net.opengis.wps.ProcessBriefType;
import net.opengis.wps.ProcessOutputsType1;
import net.opengis.wps.StatusType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Execute Response Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps.impl.ExecuteResponseTypeImpl#getProcess <em>Process</em>}</li>
 *   <li>{@link net.opengis.wps.impl.ExecuteResponseTypeImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link net.opengis.wps.impl.ExecuteResponseTypeImpl#getDataInputs <em>Data Inputs</em>}</li>
 *   <li>{@link net.opengis.wps.impl.ExecuteResponseTypeImpl#getOutputDefinitions <em>Output Definitions</em>}</li>
 *   <li>{@link net.opengis.wps.impl.ExecuteResponseTypeImpl#getProcessOutputs <em>Process Outputs</em>}</li>
 *   <li>{@link net.opengis.wps.impl.ExecuteResponseTypeImpl#getServiceInstance <em>Service Instance</em>}</li>
 *   <li>{@link net.opengis.wps.impl.ExecuteResponseTypeImpl#getStatusLocation <em>Status Location</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExecuteResponseTypeImpl extends ResponseBaseTypeImpl implements ExecuteResponseType {
	/**
	 * The cached value of the '{@link #getProcess() <em>Process</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProcess()
	 * @generated
	 * @ordered
	 */
	protected ProcessBriefType process;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected StatusType status;

	/**
	 * The cached value of the '{@link #getDataInputs() <em>Data Inputs</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataInputs()
	 * @generated
	 * @ordered
	 */
	protected DataInputsType1 dataInputs;

	/**
	 * The cached value of the '{@link #getOutputDefinitions() <em>Output Definitions</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputDefinitions()
	 * @generated
	 * @ordered
	 */
	protected OutputDefinitionsType outputDefinitions;

	/**
	 * The cached value of the '{@link #getProcessOutputs() <em>Process Outputs</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProcessOutputs()
	 * @generated
	 * @ordered
	 */
	protected ProcessOutputsType1 processOutputs;

	/**
	 * The default value of the '{@link #getServiceInstance() <em>Service Instance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getServiceInstance()
	 * @generated
	 * @ordered
	 */
	protected static final String SERVICE_INSTANCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getServiceInstance() <em>Service Instance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getServiceInstance()
	 * @generated
	 * @ordered
	 */
	protected String serviceInstance = SERVICE_INSTANCE_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatusLocation() <em>Status Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatusLocation()
	 * @generated
	 * @ordered
	 */
	protected static final String STATUS_LOCATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStatusLocation() <em>Status Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatusLocation()
	 * @generated
	 * @ordered
	 */
	protected String statusLocation = STATUS_LOCATION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExecuteResponseTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getExecuteResponseType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProcessBriefType getProcess() {
		return process;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetProcess(ProcessBriefType newProcess, NotificationChain msgs) {
		ProcessBriefType oldProcess = process;
		process = newProcess;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS, oldProcess, newProcess);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProcess(ProcessBriefType newProcess) {
		if (newProcess != process) {
			NotificationChain msgs = null;
			if (process != null)
				msgs = ((InternalEObject)process).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS, null, msgs);
			if (newProcess != null)
				msgs = ((InternalEObject)newProcess).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS, null, msgs);
			msgs = basicSetProcess(newProcess, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS, newProcess, newProcess));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StatusType getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStatus(StatusType newStatus, NotificationChain msgs) {
		StatusType oldStatus = status;
		status = newStatus;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS, oldStatus, newStatus);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStatus(StatusType newStatus) {
		if (newStatus != status) {
			NotificationChain msgs = null;
			if (status != null)
				msgs = ((InternalEObject)status).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS, null, msgs);
			if (newStatus != null)
				msgs = ((InternalEObject)newStatus).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS, null, msgs);
			msgs = basicSetStatus(newStatus, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS, newStatus, newStatus));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DataInputsType1 getDataInputs() {
		return dataInputs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDataInputs(DataInputsType1 newDataInputs, NotificationChain msgs) {
		DataInputsType1 oldDataInputs = dataInputs;
		dataInputs = newDataInputs;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS, oldDataInputs, newDataInputs);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDataInputs(DataInputsType1 newDataInputs) {
		if (newDataInputs != dataInputs) {
			NotificationChain msgs = null;
			if (dataInputs != null)
				msgs = ((InternalEObject)dataInputs).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS, null, msgs);
			if (newDataInputs != null)
				msgs = ((InternalEObject)newDataInputs).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS, null, msgs);
			msgs = basicSetDataInputs(newDataInputs, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS, newDataInputs, newDataInputs));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OutputDefinitionsType getOutputDefinitions() {
		return outputDefinitions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetOutputDefinitions(OutputDefinitionsType newOutputDefinitions, NotificationChain msgs) {
		OutputDefinitionsType oldOutputDefinitions = outputDefinitions;
		outputDefinitions = newOutputDefinitions;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS, oldOutputDefinitions, newOutputDefinitions);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputDefinitions(OutputDefinitionsType newOutputDefinitions) {
		if (newOutputDefinitions != outputDefinitions) {
			NotificationChain msgs = null;
			if (outputDefinitions != null)
				msgs = ((InternalEObject)outputDefinitions).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS, null, msgs);
			if (newOutputDefinitions != null)
				msgs = ((InternalEObject)newOutputDefinitions).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS, null, msgs);
			msgs = basicSetOutputDefinitions(newOutputDefinitions, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS, newOutputDefinitions, newOutputDefinitions));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProcessOutputsType1 getProcessOutputs() {
		return processOutputs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetProcessOutputs(ProcessOutputsType1 newProcessOutputs, NotificationChain msgs) {
		ProcessOutputsType1 oldProcessOutputs = processOutputs;
		processOutputs = newProcessOutputs;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS, oldProcessOutputs, newProcessOutputs);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProcessOutputs(ProcessOutputsType1 newProcessOutputs) {
		if (newProcessOutputs != processOutputs) {
			NotificationChain msgs = null;
			if (processOutputs != null)
				msgs = ((InternalEObject)processOutputs).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS, null, msgs);
			if (newProcessOutputs != null)
				msgs = ((InternalEObject)newProcessOutputs).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS, null, msgs);
			msgs = basicSetProcessOutputs(newProcessOutputs, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS, newProcessOutputs, newProcessOutputs));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getServiceInstance() {
		return serviceInstance;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setServiceInstance(String newServiceInstance) {
		String oldServiceInstance = serviceInstance;
		serviceInstance = newServiceInstance;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__SERVICE_INSTANCE, oldServiceInstance, serviceInstance));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getStatusLocation() {
		return statusLocation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStatusLocation(String newStatusLocation) {
		String oldStatusLocation = statusLocation;
		statusLocation = newStatusLocation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS_LOCATION, oldStatusLocation, statusLocation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS:
				return basicSetProcess(null, msgs);
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS:
				return basicSetStatus(null, msgs);
			case WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS:
				return basicSetDataInputs(null, msgs);
			case WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS:
				return basicSetOutputDefinitions(null, msgs);
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS:
				return basicSetProcessOutputs(null, msgs);
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
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS:
				return getProcess();
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS:
				return getStatus();
			case WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS:
				return getDataInputs();
			case WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS:
				return getOutputDefinitions();
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS:
				return getProcessOutputs();
			case WpsPackage.EXECUTE_RESPONSE_TYPE__SERVICE_INSTANCE:
				return getServiceInstance();
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS_LOCATION:
				return getStatusLocation();
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
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS:
				setProcess((ProcessBriefType)newValue);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS:
				setStatus((StatusType)newValue);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS:
				setDataInputs((DataInputsType1)newValue);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS:
				setOutputDefinitions((OutputDefinitionsType)newValue);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS:
				setProcessOutputs((ProcessOutputsType1)newValue);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__SERVICE_INSTANCE:
				setServiceInstance((String)newValue);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS_LOCATION:
				setStatusLocation((String)newValue);
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
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS:
				setProcess((ProcessBriefType)null);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS:
				setStatus((StatusType)null);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS:
				setDataInputs((DataInputsType1)null);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS:
				setOutputDefinitions((OutputDefinitionsType)null);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS:
				setProcessOutputs((ProcessOutputsType1)null);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__SERVICE_INSTANCE:
				setServiceInstance(SERVICE_INSTANCE_EDEFAULT);
				return;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS_LOCATION:
				setStatusLocation(STATUS_LOCATION_EDEFAULT);
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
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS:
				return process != null;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS:
				return status != null;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__DATA_INPUTS:
				return dataInputs != null;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__OUTPUT_DEFINITIONS:
				return outputDefinitions != null;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__PROCESS_OUTPUTS:
				return processOutputs != null;
			case WpsPackage.EXECUTE_RESPONSE_TYPE__SERVICE_INSTANCE:
				return SERVICE_INSTANCE_EDEFAULT == null ? serviceInstance != null : !SERVICE_INSTANCE_EDEFAULT.equals(serviceInstance);
			case WpsPackage.EXECUTE_RESPONSE_TYPE__STATUS_LOCATION:
				return STATUS_LOCATION_EDEFAULT == null ? statusLocation != null : !STATUS_LOCATION_EDEFAULT.equals(statusLocation);
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
		result.append(" (serviceInstance: ");
		result.append(serviceInstance);
		result.append(", statusLocation: ");
		result.append(statusLocation);
		result.append(')');
		return result.toString();
	}

} //ExecuteResponseTypeImpl
