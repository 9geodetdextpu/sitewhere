/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.devicestate.kafka;

import java.util.UUID;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sitewhere.grpc.model.DeviceEventModel.GEnrichedEventPayload;

/**
 * Processes event stream and persists deltas into device state storage.
 */
public class DeviceStatePersistenceProcessorSupplier implements ProcessorSupplier<UUID, GEnrichedEventPayload> {

    /** Static logger instance */
    private static Logger LOGGER = LoggerFactory.getLogger(DeviceStatePersistenceProcessorSupplier.class);

    /*
     * @see org.apache.kafka.streams.processor.ProcessorSupplier#get()
     */
    @Override
    public Processor<UUID, GEnrichedEventPayload> get() {
	return new Processor<UUID, GEnrichedEventPayload>() {

	    @SuppressWarnings("unused")
	    private ProcessorContext context;

	    /*
	     * @see
	     * org.apache.kafka.streams.processor.Processor#init(org.apache.kafka.streams.
	     * processor.ProcessorContext)
	     */
	    @Override
	    public void init(ProcessorContext context) {
		this.context = context;
	    }

	    /*
	     * @see org.apache.kafka.streams.processor.Processor#process(java.lang.Object,
	     * java.lang.Object)
	     */
	    @Override
	    public void process(UUID key, GEnrichedEventPayload value) {
		LOGGER.info(String.format("Processing device state persistence for %s", key));
	    }

	    /*
	     * @see org.apache.kafka.streams.processor.Processor#close()
	     */
	    @Override
	    public void close() {
	    }
	};
    }
}
