package com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetFilters;

import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes.Asset;
import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes.ResourceBundleAsset;

import java.io.File;

public class ResourceBundleFilter implements AssetFilter {
    @Override
    public boolean isAsset(File file) {
        return file.isDirectory();
    }

    @Override
    public Asset getAsset(File file) {
        return new ResourceBundleAsset(file);
    }
}
