/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.registration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sitewhere.microservice.MicroserviceApplication;
import com.sitewhere.registration.spi.microservice.IDeviceRegistrationMicroservice;

/**
 * Main application which runs the device registration microservice.
 */
@ApplicationScoped
public class DeviceRegistrationApplication extends MicroserviceApplication<IDeviceRegistrationMicroservice> {

    @Inject
    private IDeviceRegistrationMicroservice microservice;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IMicroserviceApplication#getMicroservice()
     */
    @Override
    public IDeviceRegistrationMicroservice getMicroservice() {
	return microservice;
    }
}