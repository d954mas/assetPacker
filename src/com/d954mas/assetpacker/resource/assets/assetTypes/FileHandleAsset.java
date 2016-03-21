package com.d954mas.assetpacker.resource.assets.assetTypes;


import com.d954mas.assetpacker.Cs;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileHandleAsset extends Asset {
    public FileHandleAsset(File file) {
        super(file);
    }

    @Override
    public String getAssetClass() {
        return "FileHandle";
    }

    @Override
    public List<String> getConstructor() {
        return Cs.of(getAssetName() + " = " + "Gdx.files.internal(\"" + file.getPath().replace("\\", "/").replace("android/assets/", "") + "\");");
    }

    @Override
    public Set<String> getImports() {
        Set<String> imports=new HashSet<String>();
        imports.addAll(super.getImports());
        imports.add("import com.badlogic.gdx.Gdx;");
        imports.add("import com.badlogic.gdx.files.FileHandle;");
        return imports;
    }
}
