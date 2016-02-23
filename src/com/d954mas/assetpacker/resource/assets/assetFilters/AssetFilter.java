package com.d954mas.assetpacker.resource.assets.assetFilters;

import com.d954mas.assetpacker.resource.assets.assetTypes.Asset;

import java.io.File;

public interface AssetFilter {
    boolean isAsset(File file);

    Asset getAsset(File file);
}
