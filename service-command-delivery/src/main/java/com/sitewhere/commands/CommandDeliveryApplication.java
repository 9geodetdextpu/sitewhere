/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.commands;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sitewhere.commands.spi.microservice.ICommandDeliveryMicroservice;
import com.sitewhere.microservice.MicroserviceApplication;

/**
 * Main application which runs the command delivery microservice.
 */
@ApplicationScoped
public class CommandDeliveryApplication extends MicroserviceApplication<ICommandDeliveryMicroservice> {

    @Inject
    private ICommandDeliveryMicroservice microservice;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IMicroserviceApplication#getMicroservice()
     */
    @Override
    public ICommandDeliveryMicroservice getMicroservice() {
	return microservice;
    }
}