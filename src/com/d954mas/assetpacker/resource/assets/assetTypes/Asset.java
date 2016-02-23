package com.d954mas.assetpacker.resource.assets.assetTypes;

import com.d954mas.assetpacker.Cs;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Asset {
    protected File file;
    protected String path;

    public Asset(File file) {
        if( file==null){
            throw new RuntimeException(String.format("file can't be null",file));
        }
        this.file=file;
        path=file.getPath().replace("\\","/").replace("android/assets/","");
    }
    public String getPath() {
        return path;
    }

    public Set<String> getImports(){
        Set<String> imports=new HashSet<String>();
        return imports;
    }
    public String getAssetName() {
        String name = file.getName();
        name= Character.toLowerCase(name.charAt(0))+name.substring(1);
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
             name = name.substring(0, pos);
        }
        return name;
    }
    public String getAssetClass(){
        String name=path.substring(path.lastIndexOf("/")+1);
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        name= Character.toUpperCase(name.charAt(0))+name.substring(1);
        return name;
    }
    public boolean needConstructor(){
        return true;
    }
    public List<String> getConstructor(){
        return Cs.of(String.format("%s = %s;",getAssetName(),"new "+getAssetClass()+"();"));
    };
    public boolean needDestructor(){
        return true;
    }
    public List<String> getDestructor(){
        return Cs.of(String.format("%s = null;",getAssetName()));
    }
    public boolean needInitAfterLoading(){
        return false;
    }
    public List<String> getInitAfterLoadingConstructor(){
        return Cs.of(String.format("%s = manager.get(\"%s\");",getAssetName(),getPath()));
    }


    //is getPath() asset should load
    //for example Atlas needLoad but atlasRegion not needLoad;
    public boolean needLoad(){
        return false;
    }
    public boolean needUnload(){
        return false;
    }
    public List<String> getLoadConstructor() {
        return Cs.of(String.format("manager.load(\"%s\", %s.class);", getPath(), getAssetClass()));
    }
    public List<String> getLoadDestructor(){
        return Cs.of(String.format("manager.unload(\"%s\");", getPath()));
    }





}
