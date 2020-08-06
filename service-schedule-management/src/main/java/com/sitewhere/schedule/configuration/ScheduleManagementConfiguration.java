/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.schedule.configuration;

import com.sitewhere.spi.microservice.IMicroserviceConfiguration;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Maps schedule management microservice YAML configuration to objects.
 */
@RegisterForReflection
public class ScheduleManagementConfiguration implements IMicroserviceConfiguration {
}
