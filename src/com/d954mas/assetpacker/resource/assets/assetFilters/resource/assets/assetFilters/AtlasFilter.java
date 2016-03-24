package com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetFilters;

import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes.Asset;
import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes.AtlasAsset;

import java.io.File;

public class AtlasFilter implements AssetFilter {
    @Override
    public boolean isAsset(File file) {
        return file.isDirectory() && file.getName().endsWith("_atlas");
    }

    @Override
    public Asset getAsset(File file) {
        return new AtlasAsset(file);
    }
}
