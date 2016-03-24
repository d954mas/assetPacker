package com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets;

import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetFilters.AssetFilter;
import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetFilters.AtlasFilter;
import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetFilters.ResourceBundleFilter;
import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes.Asset;
import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes.FileHandleAsset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AssetFactory {
    protected static List<AssetFilter> filters = getDefaultFilters();

    public static Asset getAssetForFile(File file) {
        for (AssetFilter assetFilter : filters) {
            if (assetFilter.isAsset(file)) return assetFilter.getAsset(file);
        }
        return new FileHandleAsset(file);
        //throw new RuntimeException(String.format("no Asset for %s",file.getPath()));
    }

    public static List<AssetFilter> getDefaultFilters() {
        List<AssetFilter> filters = new ArrayList<AssetFilter>();
        filters.add(new AtlasFilter());
        filters.add(new ResourceBundleFilter());

        return filters;
    }

    public static List<AssetFilter> getFilters() {
        return filters;
    }
}
