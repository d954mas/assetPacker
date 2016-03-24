package com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes;


import com.d954mas.assetpacker.resource.assets.assetFilters.Cs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AssetWithInlineClass extends Asset {
    protected final int depth;
    protected final List<Asset> assets;
    protected static final String TAB = "    ";

    public AssetWithInlineClass(File file) {
        super(file);
        assets = new ArrayList<>();
        String path = file.getPath().replace("\\", "/");
        depth = path.length() - path.replace("/", "").length() - 2;
    }
    //todo переделать диспозе добавить проверку на инит и лоад отдельно

    @Override
    public List<String> getConstructor() {
        if (depth == 0) throw new RuntimeException("AssetWithInlineClass: do not use this method if depth==0 ");
        else {
            return Cs.of(
                    String.format(TAB + "%s.init(manager);", getAssetName()));
        }
    }


    @Override
    public boolean needUnload() {
        return true;
    }

    @Override
    public boolean needDestructor() {
        return false;
    }

    @Override
    public List<String> getDestructor() {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("%s.dispose(manager);", getAssetName()));
        // lines.addAll(super.getDestructor());
        return lines;
    }

    @Override
    public List<String> getLoadDestructor() {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("%s.dispose(manager);", getAssetName()));
        // lines.addAll(super.getDestructor());
        return lines;
    }

    @Override
    public boolean needInitAfterLoading() {
        return true;
    }

    @Override
    public List<String> getInitAfterLoadingConstructor() {
        return Cs.of(String.format("%s.onLoadDone(manager);", getAssetName()));
    }

    public String getAssetClass() {
        return "Res" + super.getAssetClass();
    }

    public String getAdditionalClass() {
        return "Res" + super.getAssetClass() + "A";
    }

    public String getAdditionalClassString() {
        StringBuilder builder = new StringBuilder();
        builder.append("package com.generated;\n");
        builder.append("/*\n");
        builder.append("This is class where you can change values data from your generated res class\n");
        builder.append("*/\n");

        builder.append(String.format("class %s {\n\n", getAdditionalClass()));

        builder.append(TAB + "static void onLoadDone(){\n");
        builder.append(TAB + "}\n\n");
        builder.append(TAB + "static void dispose(){\n");
        builder.append(TAB + "}\n");
        builder.append("}");

        return builder.toString();
    }

    public String getInlineClassString() {
        String additionalTab = "";
        for (int i = 0; i < depth; i++) {
            additionalTab += TAB;
        }
        StringBuilder result = new StringBuilder();
        List<String> fields = new ArrayList<>();
        List<String> constructors = new ArrayList<>();
        List<String> destructors = new ArrayList<>();
        List<String> onLoaders = new ArrayList<>();
        List<String> loaders = new ArrayList<>();
        List<String> unloaders = new ArrayList<>();

        if (depth == 0) {
            fields.add(TAB + String.format("public static %s %s=new %s();", getAssetClass(), "res", getAssetClass()));
        }
        fields.add(TAB + "private boolean isLoad;");

        fields.add(TAB + "private boolean isInit;");

        //todo заменить instanceof возможно следует добавить метод getField() в Asset;
        for (Asset asset : assets) {
            if (asset instanceof AssetWithInlineClass) {
                fields.add(String.format(TAB + String.format("public %s %s=new %s();", asset.getAssetClass(), asset.getAssetName(), asset.getAssetClass())));
            } else {
                fields.add(String.format(TAB + String.format("public %s %s;", asset.getAssetClass(), asset.getAssetName())));
            }

            if (asset.needConstructor()) {
                constructors.addAll(asset.getConstructor());
            }
            if (asset.needDestructor()) {
                destructors.addAll(asset.getDestructor());
            }
            if (asset.needLoad()) {
                loaders.addAll(asset.getLoadConstructor());
            }
            if (asset.needUnload()) {
                unloaders.addAll(asset.getLoadDestructor());
            }
            if (asset.needInitAfterLoading()) {
                onLoaders.addAll(asset.getInitAfterLoadingConstructor());
            }
        }
        if (depth != 0) {
            result.append(String.format(additionalTab + "public static class %s {" + "\n", getAssetClass()));
        } else {
            result.append(String.format(additionalTab + "public class %s {" + "\n", getAssetClass()));
        }


        for (String field : fields) {
            result.append(additionalTab + field + "\n");
        }
        result.append("\n");


        String type = depth == 0 ? "public" : "protected";
        result.append(additionalTab + TAB + type + " void init(AssetManager manager){\n");
        result.append(additionalTab + TAB + TAB + "init(manager,false);\n");
        result.append(additionalTab + TAB + "}\n");
        result.append(additionalTab + TAB + type + " void init(AssetManager manager,boolean finishLoading){\n");
        result.append(additionalTab + TAB + TAB + "if(isInit)return;\n");
        //METHOD INIT
        addLines(result, additionalTab + TAB + TAB, constructors);
        addLines(result, additionalTab + TAB + TAB, loaders);
        result.append(additionalTab + TAB + TAB + "if(finishLoading){\n");
        result.append(additionalTab + TAB + TAB + TAB + "manager.finishLoading();\n");
        result.append(additionalTab + TAB + TAB + TAB + "onLoadDone(manager);\n");
        result.append(additionalTab + TAB + TAB + "}\n");

        result.append(additionalTab + TAB + TAB + "isInit=true;\n");
        result.append(additionalTab + TAB + "}\n");

        //METHOD ONLOADDONE
        result.append(additionalTab + TAB + type + " void onLoadDone(AssetManager manager){\n");
        result.append(additionalTab + TAB + TAB + "if(isLoad)return;\n");
        addLines(result, additionalTab + TAB + TAB, onLoaders);
        if (depth == 0) {
            result.append(additionalTab + TAB + TAB + getAdditionalClass() + ".onLoadDone();\n");
        }
        result.append(additionalTab + TAB + TAB + "isLoad=true;\n");
        result.append(additionalTab + TAB + "}\n");

        //METHOD DISPOSE
        result.append(additionalTab + TAB + type + " void dispose(AssetManager manager){\n");
        result.append(additionalTab + TAB + TAB + "if(isInit){\n");
        addLines(result, additionalTab + TAB + TAB + TAB, unloaders);
        result.append(additionalTab + TAB + TAB + "}\n");

        result.append(additionalTab + TAB + TAB + "if(isLoad){\n");
        addLines(result, additionalTab + TAB + TAB + TAB, destructors);
        result.append(additionalTab + TAB + TAB + "}\n");

        if (depth == 0) {
            result.append(additionalTab + TAB + TAB + getAdditionalClass() + ".dispose();\n");
        }

        result.append(additionalTab + TAB + TAB + "isInit=false;\n");
        result.append(additionalTab + TAB + TAB + "isLoad=false;\n");
        result.append(additionalTab + TAB + "}\n");

        //add static classes
        for (Asset asset : assets) {
            if (asset instanceof AssetWithInlineClass) {
                result.append(((AssetWithInlineClass) asset).getInlineClassString());
            }
        }
        result.append(additionalTab + "}" + "\n");

        return result.toString();
    }

    protected void addLines(StringBuilder builder, String tab, Iterable<String> lines) {
        for (String line : lines) {
            builder.append(tab + line + "\n");
        }
    }


}
