/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.event.spi.microservice;

import com.sitewhere.event.configuration.EventManagementTenantConfiguration;
import com.sitewhere.event.spi.kafka.IPreprocessedEventsPipeline;
import com.sitewhere.event.spi.kafka.IOutboundCommandInvocationsProducer;
import com.sitewhere.event.spi.kafka.IOutboundEventsProducer;
import com.sitewhere.grpc.service.DeviceEventManagementGrpc;
import com.sitewhere.microservice.api.event.IDeviceEventManagement;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;

/**
 * Extends {@link IMicroserviceTenantEngine} with features specific to device
 * event management.
 */
public interface IEventManagementTenantEngine extends IMicroserviceTenantEngine<EventManagementTenantConfiguration> {

    /**
     * Get associated event management implementation.
     * 
     * @return
     */
    public IDeviceEventManagement getEventManagement();

    /**
     * Get implementation class that wraps event management with GRPC conversions.
     * 
     * @return
     */
    public DeviceEventManagementGrpc.DeviceEventManagementImplBase getEventManagementImpl();

    /**
     * Get Kafka Streams pipeline for events prepared by inbound processing logic.
     * 
     * @return
     */
    public IPreprocessedEventsPipeline getPreprocessedEventsPipeline();

    /**
     * Get Kafka producer that sends enriched, persisted events to a topic.
     * 
     * @return
     */
    public IOutboundEventsProducer getOutboundEventsProducer();

    /**
     * Get Kafka producer that sends enriched, persisted command invocations to a
     * topic.
     * 
     * @return
     */
    public IOutboundCommandInvocationsProducer getOutboundCommandInvocationsProducer();
}