/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.instance.microservice;

import javax.enterprise.context.ApplicationScoped;

import com.sitewhere.grpc.client.asset.AssetManagementApiChannel;
import com.sitewhere.grpc.client.asset.CachedAssetManagementApiChannel;
import com.sitewhere.grpc.client.batch.BatchManagementApiChannel;
import com.sitewhere.grpc.client.device.CachedDeviceManagementApiChannel;
import com.sitewhere.grpc.client.device.DeviceManagementApiChannel;
import com.sitewhere.grpc.client.devicestate.DeviceStateApiChannel;
import com.sitewhere.grpc.client.event.DeviceEventManagementApiChannel;
import com.sitewhere.grpc.client.label.LabelGenerationApiChannel;
import com.sitewhere.grpc.client.schedule.ScheduleManagementApiChannel;
import com.sitewhere.grpc.client.spi.client.IAssetManagementApiChannel;
import com.sitewhere.grpc.client.spi.client.IBatchManagementApiChannel;
import com.sitewhere.grpc.client.spi.client.IDeviceEventManagementApiChannel;
import com.sitewhere.grpc.client.spi.client.IDeviceManagementApiChannel;
import com.sitewhere.grpc.client.spi.client.IDeviceStateApiChannel;
import com.sitewhere.grpc.client.spi.client.ILabelGenerationApiChannel;
import com.sitewhere.grpc.client.spi.client.IScheduleManagementApiChannel;
import com.sitewhere.instance.configuration.InstanceManagementConfiguration;
import com.sitewhere.instance.configuration.InstanceManagementModule;
import com.sitewhere.instance.grpc.tenant.TenantManagementGrpcServer;
import com.sitewhere.instance.grpc.user.UserManagementGrpcServer;
import com.sitewhere.instance.spi.microservice.IInstanceBootstrapper;
import com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice;
import com.sitewhere.instance.spi.tenant.grpc.ITenantManagementGrpcServer;
import com.sitewhere.instance.spi.user.grpc.IUserManagementGrpcServer;
import com.sitewhere.microservice.api.asset.IAssetManagement;
import com.sitewhere.microservice.api.device.IDeviceManagement;
import com.sitewhere.microservice.api.user.IUserManagement;
import com.sitewhere.microservice.configuration.ConfigurableMicroservice;
import com.sitewhere.microservice.lifecycle.CompositeLifecycleStep;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.MicroserviceIdentifier;
import com.sitewhere.spi.microservice.configuration.IMicroserviceModule;
import com.sitewhere.spi.microservice.configuration.IScriptSpecUpdates;
import com.sitewhere.spi.microservice.configuration.IScriptVersionSpecUpdates;
import com.sitewhere.spi.microservice.lifecycle.ICompositeLifecycleStep;
import com.sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor;

import io.sitewhere.k8s.crd.tenant.scripting.SiteWhereScript;
import io.sitewhere.k8s.crd.tenant.scripting.version.SiteWhereScriptVersion;

/**
 * Microservice that provides instance management functionality.
 */
@ApplicationScoped
public class InstanceManagementMicroservice
	extends ConfigurableMicroservice<MicroserviceIdentifier, InstanceManagementConfiguration>
	implements IInstanceManagementMicroservice<MicroserviceIdentifier> {

    /** Instance dataset bootstrapper */
    private IInstanceBootstrapper instanceBootstrapper;

    /** User management implementation */
    private IUserManagement userManagement;

    /** Responds to user management GRPC requests */
    private IUserManagementGrpcServer userManagementGrpcServer;

    /** Responds to tenant management GRPC requests */
    private ITenantManagementGrpcServer tenantManagementGrpcServer;

    /** Device management API channel */
    private CachedDeviceManagementApiChannel deviceManagementApiChannel;

    /** Device event management API channel */
    private IDeviceEventManagementApiChannel<?> deviceEventManagementApiChannel;

    /** Asset management API channel */
    private CachedAssetManagementApiChannel assetManagementApiChannel;

    /** Batch management API channel */
    private IBatchManagementApiChannel<?> batchManagementApiChannel;

    /** Schedule management API channel */
    private IScheduleManagementApiChannel<?> scheduleManagementApiChannel;

    /** Label generation API channel */
    private ILabelGenerationApiChannel<?> labelGenerationApiChannel;

    /** Device state API channel */
    private IDeviceStateApiChannel<?> deviceStateApiChannel;

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.spi.IMicroservice#getName()
     */
    @Override
    public String getName() {
	return "Instance Management";
    }

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#getIdentifier()
     */
    @Override
    public MicroserviceIdentifier getIdentifier() {
	return MicroserviceIdentifier.InstanceManagement;
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.IConfigurableMicroservice#
     * getConfigurationClass()
     */
    @Override
    public Class<InstanceManagementConfiguration> getConfigurationClass() {
	return InstanceManagementConfiguration.class;
    }

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#createConfigurationModule()
     */
    @Override
    public IMicroserviceModule<InstanceManagementConfiguration> createConfigurationModule() {
	return new InstanceManagementModule(getMicroserviceConfiguration());
    }

    /*
     * @see
     * com.sitewhere.microservice.configuration.ConfigurableMicroservice#initialize(
     * com.sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void initialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.initialize(monitor);

	// Create dataset bootstrapper.
	this.instanceBootstrapper = new InstanceBootstrapper();

	// Create management implementations.
	createManagementImplementations();

	// Create GRPC components.
	createGrpcComponents();

	// Composite step for initializing microservice.
	ICompositeLifecycleStep init = new CompositeLifecycleStep("Initialize " + getName());

	// Initialize tenant management GRPC server.
	init.addInitializeStep(this, getTenantManagementGrpcServer(), true);

	// Initialize user management implementation.
	init.addInitializeStep(this, getUserManagement(), true);

	// Initialize dataset bootstrapper.
	init.addInitializeStep(this, getInstanceBootstrapper(), true);

	// Initialize user management GRPC server.
	init.addInitializeStep(this, getUserManagementGrpcServer(), true);

	// Initialize device management API channel.
	init.addInitializeStep(this, getDeviceManagement(), true);

	// Initialize device event management API channel.
	init.addInitializeStep(this, getDeviceEventManagementApiChannel(), true);

	// Initialize asset management API channel.
	init.addInitializeStep(this, getAssetManagement(), true);

	// Initialize batch management API channel.
	init.addInitializeStep(this, getBatchManagementApiChannel(), true);

	// Initialize schedule management API channel.
	init.addInitializeStep(this, getScheduleManagementApiChannel(), true);

	// Initialize label generation API channel.
	init.addInitializeStep(this, getLabelGenerationApiChannel(), true);

	// Initialize device state API channel.
	init.addInitializeStep(this, getDeviceStateApiChannel(), true);

	// Execute initialization steps.
	init.execute(monitor);
    }

    /**
     * Create management implementations.
     */
    protected void createManagementImplementations() {
	this.userManagement = getInjector().getInstance(IUserManagement.class);
    }

    /**
     * Create components that interact via GRPC.
     * 
     * @throws SiteWhereException
     */
    protected void createGrpcComponents() throws SiteWhereException {
	this.userManagementGrpcServer = new UserManagementGrpcServer(this, getUserManagement());
	this.tenantManagementGrpcServer = new TenantManagementGrpcServer(this, getTenantManagement());

	// Device management.
	IDeviceManagementApiChannel<?> deviceManagement = new DeviceManagementApiChannel(getInstanceSettings());
	this.deviceManagementApiChannel = new CachedDeviceManagementApiChannel(deviceManagement,
		new CachedDeviceManagementApiChannel.CacheSettings());

	// Device event management.
	this.deviceEventManagementApiChannel = new DeviceEventManagementApiChannel(getInstanceSettings());

	// Asset management.
	IAssetManagementApiChannel<?> assetManagement = new AssetManagementApiChannel(getInstanceSettings());
	this.assetManagementApiChannel = new CachedAssetManagementApiChannel(assetManagement,
		new CachedAssetManagementApiChannel.CacheSettings());

	// Batch management.
	this.batchManagementApiChannel = new BatchManagementApiChannel(getInstanceSettings());

	// Schedule management.
	this.scheduleManagementApiChannel = new ScheduleManagementApiChannel(getInstanceSettings());

	// Label generation.
	this.labelGenerationApiChannel = new LabelGenerationApiChannel(getInstanceSettings());

	// Device state.
	this.deviceStateApiChannel = new DeviceStateApiChannel(getInstanceSettings());
    }

    /*
     * @see
     * com.sitewhere.microservice.lifecycle.LifecycleComponent#start(com.sitewhere.
     * spi.microservice.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.start(monitor);

	// Composite step for starting microservice.
	ICompositeLifecycleStep start = new CompositeLifecycleStep("Start " + getName());

	// Start tenant management GRPC server.
	start.addStartStep(this, getTenantManagementGrpcServer(), true);

	// Start user management implementation.
	start.addStartStep(this, getUserManagement(), true);

	// Start dataset bootstrapper.
	start.addStartStep(this, getInstanceBootstrapper(), true);

	// Start user management GRPC server.
	start.addStartStep(this, getUserManagementGrpcServer(), true);

	// Start device mangement API channel.
	start.addStartStep(this, getDeviceManagement(), true);

	// Start device event mangement API channel.
	start.addStartStep(this, getDeviceEventManagementApiChannel(), true);

	// Start asset mangement API channel.
	start.addStartStep(this, getAssetManagement(), true);

	// Start batch mangement API channel.
	start.addStartStep(this, getBatchManagementApiChannel(), true);

	// Start schedule mangement API channel.
	start.addStartStep(this, getScheduleManagementApiChannel(), true);

	// Start label generation API channel.
	start.addStartStep(this, getLabelGenerationApiChannel(), true);

	// Start device state API channel.
	start.addStartStep(this, getDeviceStateApiChannel(), true);

	// Execute startup steps.
	start.execute(monitor);
    }

    /*
     * @see
     * com.sitewhere.microservice.lifecycle.LifecycleComponent#stop(com.sitewhere.
     * spi.microservice.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Composite step for stopping microservice.
	ICompositeLifecycleStep stop = new CompositeLifecycleStep("Stop " + getName());

	// Stop device mangement API channel .
	stop.addStopStep(this, getDeviceManagement());

	// Stop device event mangement API channel.
	stop.addStopStep(this, getDeviceEventManagementApiChannel());

	// Stop asset mangement API channel.
	stop.addStopStep(this, getAssetManagement());

	// Stop batch mangement API channel.
	stop.addStopStep(this, getBatchManagementApiChannel());

	// Stop schedule mangement API channel.
	stop.addStopStep(this, getScheduleManagementApiChannel());

	// Stop label generation API channel.
	stop.addStopStep(this, getLabelGenerationApiChannel());

	// Stop device state API channel.
	stop.addStopStep(this, getDeviceStateApiChannel());

	// Stop user management GRPC server.
	stop.addStopStep(this, getUserManagementGrpcServer());

	// Stop tenant management GRPC manager.
	stop.addStopStep(this, getTenantManagementGrpcServer());

	// Stop user management implementation.
	stop.addStopStep(this, getUserManagement());

	// Stop dataset bootstrapper.
	stop.addStopStep(this, getInstanceBootstrapper());

	// Execute shutdown steps.
	stop.execute(monitor);

	super.stop(monitor);
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.configuration.IScriptConfigurationListener#
     * onScriptAdded(io.sitewhere.k8s.crd.tenant.scripting.SiteWhereScript)
     */
    @Override
    public void onScriptAdded(SiteWhereScript script) {
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.configuration.IScriptConfigurationListener#
     * onScriptUpdated(io.sitewhere.k8s.crd.tenant.scripting.SiteWhereScript,
     * com.sitewhere.spi.microservice.configuration.IScriptSpecUpdates)
     */
    @Override
    public void onScriptUpdated(SiteWhereScript script, IScriptSpecUpdates updates) {
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.configuration.IScriptConfigurationListener#
     * onScriptDeleted(io.sitewhere.k8s.crd.tenant.scripting.SiteWhereScript)
     */
    @Override
    public void onScriptDeleted(SiteWhereScript script) {
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.
     * IScriptVersionConfigurationListener#onScriptVersionAdded(io.sitewhere.k8s.crd
     * .tenant.scripting.version.SiteWhereScriptVersion)
     */
    @Override
    public void onScriptVersionAdded(SiteWhereScriptVersion version) {
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.
     * IScriptVersionConfigurationListener#onScriptVersionUpdated(io.sitewhere.k8s.
     * crd.tenant.scripting.version.SiteWhereScriptVersion,
     * com.sitewhere.spi.microservice.configuration.IScriptVersionSpecUpdates)
     */
    @Override
    public void onScriptVersionUpdated(SiteWhereScriptVersion version, IScriptVersionSpecUpdates updates) {
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.
     * IScriptVersionConfigurationListener#onScriptVersionDeleted(io.sitewhere.k8s.
     * crd.tenant.scripting.version.SiteWhereScriptVersion)
     */
    @Override
    public void onScriptVersionDeleted(SiteWhereScriptVersion version) {
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getInstanceBootstrapper()
     */
    @Override
    public IInstanceBootstrapper getInstanceBootstrapper() {
	return instanceBootstrapper;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getUserManagementGrpcServer()
     */
    @Override
    public IUserManagementGrpcServer getUserManagementGrpcServer() {
	return userManagementGrpcServer;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getUserManagement()
     */
    @Override
    public IUserManagement getUserManagement() {
	return userManagement;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getTenantManagementGrpcServer()
     */
    @Override
    public ITenantManagementGrpcServer getTenantManagementGrpcServer() {
	return tenantManagementGrpcServer;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getDeviceManagement()
     */
    @Override
    public IDeviceManagement getDeviceManagement() {
	return deviceManagementApiChannel;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getDeviceEventManagementApiChannel()
     */
    @Override
    public IDeviceEventManagementApiChannel<?> getDeviceEventManagementApiChannel() {
	return deviceEventManagementApiChannel;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getAssetManagement()
     */
    @Override
    public IAssetManagement getAssetManagement() {
	return assetManagementApiChannel;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getBatchManagementApiChannel()
     */
    @Override
    public IBatchManagementApiChannel<?> getBatchManagementApiChannel() {
	return batchManagementApiChannel;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getScheduleManagementApiChannel()
     */
    @Override
    public IScheduleManagementApiChannel<?> getScheduleManagementApiChannel() {
	return scheduleManagementApiChannel;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getLabelGenerationApiChannel()
     */
    @Override
    public ILabelGenerationApiChannel<?> getLabelGenerationApiChannel() {
	return labelGenerationApiChannel;
    }

    /*
     * @see com.sitewhere.instance.spi.microservice.IInstanceManagementMicroservice#
     * getDeviceStateApiChannel()
     */
    @Override
    public IDeviceStateApiChannel<?> getDeviceStateApiChannel() {
	return deviceStateApiChannel;
    }
}