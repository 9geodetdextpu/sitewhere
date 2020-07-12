/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.search.configuration;

import com.sitewhere.microservice.multitenant.TenantEngineModule;
import com.sitewhere.search.spi.microservice.IEventSearchTenantEngine;

/**
 * Guice module used for configuring components associated with an event search
 * tenant engine.
 */
public class EventSearchTenantEngineModule extends TenantEngineModule<EventSearchTenantConfiguration> {

    public EventSearchTenantEngineModule(IEventSearchTenantEngine tenantEngine,
	    EventSearchTenantConfiguration configuration) {
	super(tenantEngine, configuration);
    }
}
