package com.d954mas.assetpacker.resource.assets.assetFilters;

import com.d954mas.assetpacker.resource.assets.assetTypes.Asset;
import com.d954mas.assetpacker.resource.assets.assetTypes.FileHandleAsset;

import java.io.File;

public class FileHandleFilter implements AssetFilter {

    public boolean isAsset(File file){
        return true;
    }

    @Override
    public Asset getAsset(File file) {
        return new FileHandleAsset(file);
    }
}
