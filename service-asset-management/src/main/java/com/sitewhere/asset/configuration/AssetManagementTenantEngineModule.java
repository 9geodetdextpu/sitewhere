/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.asset.configuration;

import com.sitewhere.asset.persistence.rdb.RdbAssetManagement;
import com.sitewhere.asset.spi.microservice.IAssetManagementTenantEngine;
import com.sitewhere.microservice.api.asset.IAssetManagement;
import com.sitewhere.microservice.multitenant.TenantEngineModule;

/**
 * Guice module used for configuring components associated with an asset
 * management tenant engine.
 */
public class AssetManagementTenantEngineModule extends TenantEngineModule<AssetManagementTenantConfiguration> {

    public AssetManagementTenantEngineModule(IAssetManagementTenantEngine tenantEngine,
	    AssetManagementTenantConfiguration configuration) {
	super(tenantEngine, configuration);
    }

    /*
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
	bind(IAssetManagementTenantEngine.class).toInstance((IAssetManagementTenantEngine) getTenantEngine());
	bind(IAssetManagement.class).to(RdbAssetManagement.class);
    }
}
