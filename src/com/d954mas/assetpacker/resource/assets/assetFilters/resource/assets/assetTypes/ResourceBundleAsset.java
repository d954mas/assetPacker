package com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes;

import com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.AssetFactory;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ResourceBundleAsset extends AssetWithInlineClass {

    public ResourceBundleAsset(File file) {
        super(file);

        if (!file.isDirectory()) {
            throw new RuntimeException("Can't create resourceBundle from not directory");
        }
        create(file);
    }


    protected void create(File root) {
        for (File file : root.listFiles()) {
            assets.add(AssetFactory.getAssetForFile(file));
        }
        Collections.sort(assets, new Comparator<Asset>() {
            @Override
            public int compare(Asset o1, Asset o2) {
                int compare = o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
                if (compare == 0) return o1.getAssetName().compareTo(o2.getAssetName());
                else return compare;
            }
        });
    }

    public Set<String> getImports() {
        Set<String> imports = new HashSet<String>();
        imports.add("import com.badlogic.gdx.assets.AssetManager;");
        for (Asset asset : assets) {
            imports.addAll(asset.getImports());
        }
        return imports;
    }


    public String getClassString() {
        String TAB = "    ";
        StringBuilder result = new StringBuilder();
        Set<String> imports = getImports();
        //create imports
        result.append("package com.generated;\n");
        for (String importt : imports) {
            result.append(importt + "\n");
        }
        //METHOD INIT
        result.append(getInlineClassString());
        return result.toString();
    }


}
