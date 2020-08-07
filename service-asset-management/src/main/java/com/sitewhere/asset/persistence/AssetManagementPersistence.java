/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.asset.persistence;

import java.util.Collections;

import com.sitewhere.microservice.api.asset.IAssetManagement;
import com.sitewhere.microservice.api.device.IDeviceManagement;
import com.sitewhere.microservice.persistence.Persistence;
import com.sitewhere.rest.model.asset.Asset;
import com.sitewhere.rest.model.asset.AssetType;
import com.sitewhere.rest.model.search.asset.AssetSearchCriteria;
import com.sitewhere.rest.model.search.device.DeviceAssignmentSearchCriteria;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.asset.IAsset;
import com.sitewhere.spi.asset.IAssetType;
import com.sitewhere.spi.asset.request.IAssetCreateRequest;
import com.sitewhere.spi.asset.request.IAssetTypeCreateRequest;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.search.ISearchResults;

/**
 * Persistence logic for asset management components.
 */
public class AssetManagementPersistence extends Persistence {

    /**
     * Handle base logic for creating an asset type.
     * 
     * @param assetType
     * @param request
     * @return
     * @throws SiteWhereException
     */
    public static AssetType assetTypeCreateLogic(IAssetTypeCreateRequest request) throws SiteWhereException {
	AssetType type = new AssetType();
	Persistence.brandedEntityCreateLogic(request, type);

	require("Name", request.getName());
	type.setName(request.getName());

	type.setDescription(request.getDescription());

	return type;
    }

    /**
     * Handle common asset type create logic.
     * 
     * @param target
     * @param request
     * @throws SiteWhereException
     */
    public static void assetTypeUpdateLogic(AssetType target, IAssetTypeCreateRequest request)
	    throws SiteWhereException {
	Persistence.brandedEntityUpdateLogic(request, target);

	if (request.getName() != null) {
	    target.setName(request.getName());
	}
	if (request.getDescription() != null) {
	    target.setDescription(request.getDescription());
	}
    }

    /**
     * Common logic for deleting an asset type.
     * 
     * @param assetType
     * @param assetManagement
     * @throws SiteWhereException
     */
    public static void assetTypeDeleteLogic(IAssetType assetType, IAssetManagement assetManagement)
	    throws SiteWhereException {
	AssetSearchCriteria criteria = new AssetSearchCriteria(1, 1);
	criteria.setAssetTypeToken(assetType.getToken());
	ISearchResults<? extends IAsset> assets = assetManagement.listAssets(criteria);
	if (assets.getNumResults() > 0) {
	    throw new SiteWhereSystemException(ErrorCode.AssetTypeNoDeleteHasAssets, ErrorLevel.ERROR);
	}
    }

    /**
     * Handle base logic for creating an asset.
     * 
     * @param assetType
     * @param request
     * @return
     * @throws SiteWhereException
     */
    public static Asset assetCreateLogic(IAssetType assetType, IAssetCreateRequest request) throws SiteWhereException {
	Asset asset = new Asset();
	Persistence.brandedEntityCreateLogic(request, asset);

	require("Name", request.getName());
	asset.setName(request.getName());

	asset.setAssetTypeId(assetType.getId());

	return asset;
    }

    /**
     * Handle base logic for updating an asset.
     * 
     * @param assetType
     * @param target
     * @param request
     * @throws SiteWhereException
     */
    public static void assetUpdateLogic(IAssetType assetType, Asset target, IAssetCreateRequest request)
	    throws SiteWhereException {
	Persistence.brandedEntityUpdateLogic(request, target);

	if (request.getAssetTypeToken() != null) {
	    target.setAssetTypeId(assetType.getId());
	}
	if (request.getName() != null) {
	    target.setName(request.getName());
	}
    }

    /**
     * Common logic for deleting an asset.
     * 
     * @param asset
     * @param assetManagement
     * @param deviceManagement
     * @throws SiteWhereException
     */
    public static void assetDeleteLogic(IAsset asset, IAssetManagement assetManagement,
	    IDeviceManagement deviceManagement) throws SiteWhereException {
	DeviceAssignmentSearchCriteria criteria = new DeviceAssignmentSearchCriteria(1, 1);
	criteria.setAssetTokens(Collections.singletonList(asset.getToken()));

	ISearchResults<? extends IDeviceAssignment> assignments = deviceManagement.listDeviceAssignments(criteria);
	if (assignments.getNumResults() > 0) {
	    throw new SiteWhereSystemException(ErrorCode.AssetNoDeleteHasAssignments, ErrorLevel.ERROR);
	}
    }
}