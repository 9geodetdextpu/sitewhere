/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.rdb.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.sitewhere.rdb.entities.RdbPersistentEntity;
import com.sitewhere.spi.device.element.IDeviceElementSchema;

@Entity
@Table(name = "device_element_schema", uniqueConstraints = @UniqueConstraint(columnNames = { "token" }))
public class RdbDeviceElementSchema extends RdbPersistentEntity implements IDeviceElementSchema, Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1393000821566977680L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    /** List of device slots */
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "deviceElementSchema")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<RdbDeviceSlot> deviceSlots = new ArrayList<>();

    /** List of device units */
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "deviceUnitParent")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<RdbDeviceUnit> deviceUnits = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @CollectionTable(name = "device_element_schema_metadata", joinColumns = @JoinColumn(name = "device_element_schema_id"))
    @MapKeyColumn(name = "prop_key")
    @Column(name = "prop_value")
    private Map<String, String> metadata = new HashMap<>();

    public UUID getId() {
	return id;
    }

    public void setId(UUID id) {
	this.id = id;
    }

    /*
     * @see com.sitewhere.spi.device.element.IDeviceElement#getName()
     */
    @Override
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    /*
     * @see com.sitewhere.spi.device.element.IDeviceElement#getPath()
     */
    @Override
    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }

    /*
     * @see com.sitewhere.spi.device.element.IDeviceUnit#getDeviceSlots()
     */
    @Override
    public List<RdbDeviceSlot> getDeviceSlots() {
	return deviceSlots;
    }

    public void setDeviceSlots(List<RdbDeviceSlot> deviceSlots) {
	this.deviceSlots = deviceSlots;
    }

    /*
     * @see com.sitewhere.spi.device.element.IDeviceUnit#getDeviceUnits()
     */
    @Override
    public List<RdbDeviceUnit> getDeviceUnits() {
	return deviceUnits;
    }

    public void setDeviceUnits(List<RdbDeviceUnit> deviceUnits) {
	this.deviceUnits = deviceUnits;
    }

    /*
     * @see com.sitewhere.spi.common.IMetadataProvider#getMetadata()
     */
    @Override
    public Map<String, String> getMetadata() {
	return metadata;
    }

    /*
     * @see
     * com.sitewhere.rdb.entities.RdbPersistentEntity#setMetadata(java.util.Map)
     */
    @Override
    public void setMetadata(Map<String, String> metadata) {
	this.metadata = metadata;
    }
}
