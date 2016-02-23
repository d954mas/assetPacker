package com.d954mas.assetpacker.resource.assets.assetTypes;


import com.d954mas.assetpacker.Cs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AssetWithInlineClass extends Asset {
    protected final int depth;
    protected final List<Asset> assets;
    protected static final String TAB="    ";
    public AssetWithInlineClass(File file) {
        super(file);
        assets=new ArrayList<>();
        String path=file.getPath().replace("\\","/");
        depth=path.length() - path.replace("/", "").length()-2;
    }

    @Override
    public List<String> getConstructor() {
        if(depth==0)throw new RuntimeException("AssetWithInlineClass: do not use this method if depth==0 ");
        else{
            return Cs.of(String.format("%s = new %s(manager);",getAssetName(),getAssetClass()));
        }
    }


    @Override
    public List<String> getDestructor() {
        List<String> lines=new ArrayList<>();
        lines.add(String.format("%s.dispose(manager);",getAssetName()));
        lines.addAll(super.getDestructor());
        return lines;
    }

    @Override
    public boolean needInitAfterLoading() {
        return true;
    }

    @Override
    public List<String> getInitAfterLoadingConstructor() {
        return Cs.of(String.format("%s.onLoadDone(manager);",getAssetName()));
    }
    public String getAssetClass(){
        return "Res"+super.getAssetClass();
    }
    public String getAdditionalClass(){
        return "Res"+super.getAssetClass()+"A";
    }
    public String getAdditionalClassString(){
        StringBuilder builder=new StringBuilder();
        builder.append("package com.generated;\n");
        builder.append("/*\n");
        builder.append("This is class where you can change values data from your generated res class\n");
        builder.append("*/\n");

        builder.append(String.format("class %s {\n\n",getAdditionalClass()));

        builder.append(TAB+"static void onLoadDone(){\n");
        builder.append(TAB+"}\n\n");
        builder.append(TAB+"static void dispose(){\n");
        builder.append(TAB+"}\n");
        builder.append("}");

        return builder.toString();
    }

    public String getInlineClassString(){
        String additionalTab="";
        for(int i=0;i<depth;i++){
            additionalTab+=TAB;
        }
        StringBuilder result=new StringBuilder();
        List<String> fields=new ArrayList<>();
        List<String> constructors=new ArrayList<>();
        List<String> destructors=new ArrayList<>();
        List<String> onLoaders=new ArrayList<>();
        List<String> loaders=new ArrayList<>();
        List<String> unloaders=new ArrayList<>();

        for(Asset asset:assets){
            if(depth!=0){
                fields.add(String.format(TAB+ String.format("public %s %s;",asset.getAssetClass(),asset.getAssetName())));
            }else{
                fields.add(String.format(TAB+ String.format("public static %s %s;",asset.getAssetClass(),asset.getAssetName())));
            }

            if(asset.needConstructor()){
                constructors.addAll(asset.getConstructor());
            }
            if(asset.needDestructor()){
                destructors.addAll(asset.getDestructor());
            }
            if(asset.needLoad()){
                loaders.addAll(asset.getLoadConstructor());
            }
            if(asset.needUnload()){
                unloaders.addAll(asset.getLoadDestructor());
            }
            if(asset.needInitAfterLoading()){
                onLoaders.addAll(asset.getInitAfterLoadingConstructor());
            }
        }
        if(depth!=0){
            result.append(String.format(additionalTab+"public static class %s {"+"\n",getAssetClass()));
        }else{
            result.append(String.format(additionalTab+"public  class %s {"+"\n",getAssetClass()));

        }

        for(String field:fields){
            result.append(additionalTab+field+"\n");
        }
        result.append("\n");

        //add inline constructor
        if(depth!=0){
            result.append(additionalTab+TAB+ String.format("public %s(AssetManager manager){\n",getAssetClass()));
            result.append(additionalTab+TAB+TAB+"init(manager);\n");
            result.append(additionalTab+TAB+"}\n");
        }

        //METHOD INIT
        if(depth!=0){
            result.append(additionalTab + TAB + "protected void init(AssetManager manager){\n");
        }
        else {
            result.append(additionalTab+TAB+"public static void init(AssetManager manager){\n");
            result.append(additionalTab+TAB+TAB+"init(manager,false);\n");
            result.append(additionalTab+TAB+"}\n");
            result.append(additionalTab+TAB+"public static void init(AssetManager manager,boolean finishLoading){\n");
        }
        addLines(result,additionalTab+TAB+TAB,constructors);
        addLines(result,additionalTab+TAB+TAB,loaders);
        if(depth==0) {
            result.append(additionalTab+TAB+TAB + "if(finishLoading){\n");
            result.append(additionalTab+TAB+TAB+TAB + "manager.finishLoading();\n");
            result.append(additionalTab+TAB+TAB+TAB + "onLoadDone(manager);\n");
            result.append(additionalTab+TAB+TAB + "}\n");
        }
        result.append(additionalTab+TAB + "}\n");

        //METHOD ONLOADDONE
        if(depth!=0){
            result.append(additionalTab+TAB+"protected void onLoadDone(AssetManager manager){\n");
        }else{
            result.append(additionalTab+TAB+"public static void onLoadDone(AssetManager manager){\n");
        }
        addLines(result,additionalTab+TAB+TAB,onLoaders);
        if(depth==0){
            result.append(additionalTab+TAB+TAB+getAdditionalClass()+".onLoadDone();");
        }
        result.append(additionalTab+TAB + "}\n");

        //METHOD DISPOSE
        if(depth!=0){
            result.append(additionalTab+TAB+"protected void dispose(AssetManager manager){\n");
        }else{
            result.append(additionalTab+TAB+"public static void dispose(AssetManager manager){\n");
        }
        addLines(result,additionalTab+TAB+TAB,unloaders);
        addLines(result,additionalTab+TAB+TAB,destructors);
        if(depth==0){
            result.append(additionalTab+TAB+TAB+getAdditionalClass()+".dispose();");
        }
        result.append(additionalTab+TAB+TAB + "}\n");

        //add static classes
        for(Asset asset:assets){
            if(asset instanceof AssetWithInlineClass){
                result.append(((AssetWithInlineClass) asset).getInlineClassString());
            }
        }
        result.append(additionalTab+"}"+"\n");
        return result.toString();
    }

    protected void addLines(StringBuilder builder, String tab, Iterable<String> lines){
        for(String line:lines){
            builder.append(tab+line+"\n");
        }
    }





}
