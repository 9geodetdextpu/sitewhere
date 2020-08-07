/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.batch.microservice;

import javax.enterprise.context.ApplicationScoped;

import com.sitewhere.batch.configuration.BatchOperationsConfiguration;
import com.sitewhere.batch.configuration.BatchOperationsModule;
import com.sitewhere.batch.grpc.BatchManagementGrpcServer;
import com.sitewhere.batch.spi.grpc.IBatchManagementGrpcServer;
import com.sitewhere.batch.spi.microservice.IBatchOperationsMicroservice;
import com.sitewhere.batch.spi.microservice.IBatchOperationsTenantEngine;
import com.sitewhere.grpc.client.device.CachedDeviceManagementApiChannel;
import com.sitewhere.grpc.client.device.DeviceManagementApiChannel;
import com.sitewhere.grpc.client.event.DeviceEventManagementApiChannel;
import com.sitewhere.grpc.client.spi.client.IDeviceEventManagementApiChannel;
import com.sitewhere.grpc.client.spi.client.IDeviceManagementApiChannel;
import com.sitewhere.microservice.api.device.IDeviceManagement;
import com.sitewhere.microservice.lifecycle.CompositeLifecycleStep;
import com.sitewhere.microservice.multitenant.MultitenantMicroservice;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.MicroserviceIdentifier;
import com.sitewhere.spi.microservice.configuration.IMicroserviceModule;
import com.sitewhere.spi.microservice.lifecycle.ICompositeLifecycleStep;
import com.sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor;

import io.sitewhere.k8s.crd.tenant.engine.SiteWhereTenantEngine;

/**
 * Microservice that provides batch operations functionality.
 */
@ApplicationScoped
public class BatchOperationsMicroservice extends
	MultitenantMicroservice<MicroserviceIdentifier, BatchOperationsConfiguration, IBatchOperationsTenantEngine>
	implements IBatchOperationsMicroservice {

    /** Provides server for batch management GRPC requests */
    private IBatchManagementGrpcServer batchManagementGrpcServer;

    /** Device management API demux */
    private CachedDeviceManagementApiChannel deviceManagementApiChannel;

    /** Device event management API demux */
    private IDeviceEventManagementApiChannel<?> deviceEventManagementApiChannel;

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#getName()
     */
    @Override
    public String getName() {
	return "Batch Operations";
    }

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#getIdentifier()
     */
    @Override
    public MicroserviceIdentifier getIdentifier() {
	return MicroserviceIdentifier.BatchOperations;
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.IConfigurableMicroservice#
     * getConfigurationClass()
     */
    @Override
    public Class<BatchOperationsConfiguration> getConfigurationClass() {
	return BatchOperationsConfiguration.class;
    }

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#createConfigurationModule()
     */
    @Override
    public IMicroserviceModule<BatchOperationsConfiguration> createConfigurationModule() {
	return new BatchOperationsModule(getMicroserviceConfiguration());
    }

    /*
     * @see com.sitewhere.spi.microservice.multitenant.IMultitenantMicroservice#
     * createTenantEngine(io.sitewhere.k8s.crd.tenant.engine.SiteWhereTenantEngine)
     */
    @Override
    public IBatchOperationsTenantEngine createTenantEngine(SiteWhereTenantEngine engine) throws SiteWhereException {
	return new BatchOperationsTenantEngine(engine);
    }

    /*
     * @see
     * com.sitewhere.microservice.multitenant.MultitenantMicroservice#initialize(com
     * .sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void initialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.initialize(monitor);

	// Create GRPC components.
	createGrpcComponents();

	// Composite step for initializing microservice.
	ICompositeLifecycleStep init = new CompositeLifecycleStep("Initialize " + getName());

	// Initialize batch management GRPC server.
	init.addInitializeStep(this, getBatchManagementGrpcServer(), true);

	// Initialize device management API channel.
	init.addInitializeStep(this, getDeviceManagement(), true);

	// Initialize device event management API channel.
	init.addInitializeStep(this, getDeviceEventManagementApiChannel(), true);

	// Execute initialization steps.
	init.execute(monitor);
    }

    /*
     * @see
     * com.sitewhere.microservice.multitenant.MultitenantMicroservice#start(com.
     * sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.start(monitor);

	// Composite step for starting microservice.
	ICompositeLifecycleStep start = new CompositeLifecycleStep("Start " + getName());

	// Start batch management GRPC server.
	start.addStartStep(this, getBatchManagementGrpcServer(), true);

	// Start device mangement API channel.
	start.addStartStep(this, getDeviceManagement(), true);

	// Start device event mangement API channel.
	start.addStartStep(this, getDeviceEventManagementApiChannel(), true);

	// Execute startup steps.
	start.execute(monitor);
    }

    /*
     * @see com.sitewhere.microservice.multitenant.MultitenantMicroservice#stop(com.
     * sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Composite step for stopping microservice.
	ICompositeLifecycleStep stop = new CompositeLifecycleStep("Stop " + getName());

	// Stop batch management GRPC server.
	stop.addStopStep(this, getBatchManagementGrpcServer());

	// Stop device mangement API channel.
	stop.addStopStep(this, getDeviceManagement());

	// Stop device event mangement API channel.
	stop.addStopStep(this, getDeviceEventManagementApiChannel());

	// Execute shutdown steps.
	stop.execute(monitor);

	super.stop(monitor);
    }

    /**
     * Create GRPC components required by the microservice.
     */
    private void createGrpcComponents() {
	// Create batch management GRPC server.
	this.batchManagementGrpcServer = new BatchManagementGrpcServer(this);

	// Device management.
	IDeviceManagementApiChannel<?> wrapped = new DeviceManagementApiChannel(getInstanceSettings());
	this.deviceManagementApiChannel = new CachedDeviceManagementApiChannel(wrapped,
		new CachedDeviceManagementApiChannel.CacheSettings());

	// Device event management.
	this.deviceEventManagementApiChannel = new DeviceEventManagementApiChannel(getInstanceSettings());
    }

    /*
     * @see com.sitewhere.batch.spi.microservice.IBatchOperationsMicroservice#
     * getBatchManagementGrpcServer()
     */
    @Override
    public IBatchManagementGrpcServer getBatchManagementGrpcServer() {
	return batchManagementGrpcServer;
    }

    /*
     * @see com.sitewhere.batch.spi.microservice.IBatchOperationsMicroservice#
     * getDeviceManagement()
     */
    @Override
    public IDeviceManagement getDeviceManagement() {
	return deviceManagementApiChannel;
    }

    /*
     * @see com.sitewhere.batch.spi.microservice.IBatchOperationsMicroservice#
     * getDeviceEventManagementApiChannel()
     */
    @Override
    public IDeviceEventManagementApiChannel<?> getDeviceEventManagementApiChannel() {
	return deviceEventManagementApiChannel;
    }
}