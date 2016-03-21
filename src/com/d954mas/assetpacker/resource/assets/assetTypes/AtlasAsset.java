package com.d954mas.assetpacker.resource.assets.assetTypes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.d954mas.assetpacker.Cs;

import java.io.File;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtlasAsset extends AssetWithInlineClass {
    public AtlasAsset(final File file) {
        super(file);
        for(final File file1:file.listFiles()) {
            if (file1.getName().endsWith(".atlas")) {
                final AtlasFileAsset atlasFileAsset = new AtlasFileAsset(file1);
                assets.add(atlasFileAsset);
                path = path.replace("android/assets/", "");
                LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                LwjglApplicationConfiguration.disableAudio = true;
                config.forceExit=false;
                final CyclicBarrier cyclicBarrier=new CyclicBarrier(2);
                final LwjglApplication lwjglApplication = new LwjglApplication(new ApplicationAdapter() {
                    @Override
                    public void create() {
                        super.create();
                        TextureAtlas textureAtlas = new TextureAtlas(file1.getPath());
                        while (textureAtlas.getRegions().size!=0){
                            TextureAtlas.AtlasRegion region=textureAtlas.getRegions().first();
                            List<TextureAtlas.AtlasRegion> regions=getRegionsByName(region.name,textureAtlas);
                            if(regions.size()==1){
                                if(region.splits==null){
                                    String animationName=region.name;
                                    textureAtlas.findRegions(animationName);
                                    assets.add(new AtlasRegionAsset(new File(region.name)
                                            ,atlasFileAsset.getAssetName()));
                                }
                                else{
                                    assets.add(new NinePatchAsset(new File(region.name)
                                            ,atlasFileAsset.getAssetName()));
                                }
                            }else{
                                assets.add(new AnimationAsset(new File(region.name)
                                        ,atlasFileAsset.getAssetName(),regions));
                            }
                        }
                        try {
                            cyclicBarrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Gdx.app.exit();
                    }

                }, config);
                try {
                    cyclicBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public Set<String> getImports(){
        Set<String> imports=new HashSet<String>();
        for(Asset asset:assets){
            imports.addAll(asset.getImports());
        }
        return imports;
    }

    private List<TextureAtlas.AtlasRegion> getRegionsByName(String name, TextureAtlas atlas){
        name=name.replaceAll("\\d*$", "");
        Pattern p = Pattern.compile("^"+name+"[0-9]*");
        Iterator<TextureAtlas.AtlasRegion> iterator=atlas.getRegions().iterator();
        List<TextureAtlas.AtlasRegion> list=new ArrayList<TextureAtlas.AtlasRegion>();
        while (iterator.hasNext()){
            TextureAtlas.AtlasRegion atlasRegion=iterator.next();
            Matcher matcher = p.matcher(atlasRegion.name);
            if(matcher.matches()){
                list.add(atlasRegion);
                iterator.remove();
            }
        }
        return list;
    }

    protected static class AnimationAsset extends Asset {
        private final String atlasName;
        private final List<TextureAtlas.AtlasRegion> regions;

        public AnimationAsset(File file, String atlasName, List<TextureAtlas.AtlasRegion> regions) {
            super(file);
            this.atlasName = atlasName;
            this.regions = regions;
        }

        @Override
        public Set<String> getImports() {
            Set<String> imports=super.getImports();
            imports.add("import com.badlogic.gdx.graphics.g2d.Animation;");
            imports.add("import com.badlogic.gdx.utils.Array;");
            return imports;
        }

        @Override
        public String getAssetClass() {
            return "Animation";
        }

        @Override
        public boolean needLoad() {
            return false;
        }

        @Override
        public boolean needConstructor() {
            return false;
        }

        @Override
        public boolean needInitAfterLoading() {
            return true;
        }

        //TODO заменить имя на индекс для скорости
        @Override
        public List<String> getInitAfterLoadingConstructor() {
            List<String> lines=new ArrayList<>();
            lines.add(String.format("//region animation %s",getAssetName()));
            lines.add(String.format("Array<AtlasRegion> %sRegions = new Array<>();",getAssetName()));
            for(TextureAtlas.AtlasRegion atlasRegion:regions){
                lines.add(String.format("%sRegions.add(%s.findRegion(\"%s\"));",getAssetName(),atlasName,atlasRegion.name));
            }
            lines.add(String.format("%s = new %s(1f,%sRegions);",getAssetName(),getAssetClass(),getAssetName()));
            lines.add("//endregion");
            return lines;
        }
    }

    protected static class AtlasFileAsset extends Asset {

        public AtlasFileAsset(File file) {
            super(file);
        }
        @Override
        public Set<String> getImports() {
            Set<String> imports=super.getImports();
            imports.add("import com.d954mas.assetpacker.UnloadableTextureAtlas;");
            imports.add("import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;");
            return imports;
        }

        @Override
        public String getAssetClass() {
            return "UnloadableTextureAtlas";
        }

        @Override
        public boolean needLoad() {
            return true;
        }

        @Override
        public boolean needConstructor() {
            return false;
        }
        @Override
        public boolean needDestructor() {
            return true;
        }
        @Override
        public boolean needInitAfterLoading() {
            return true;
        }

        @Override
        public List<String> getInitAfterLoadingConstructor() {
            List<String> lines=new ArrayList<>();
            lines.add(String.format("if(%s==null){",getAssetName()));
            lines.add(String.format("    %s=new %s();",getAssetName(),getAssetClass(),getPath()));
            lines.add("}");
            lines.add(String.format("%s.load((TextureAtlasData)manager.get(\"%s\"));",getAssetName(),getPath()));
            return lines;
        }

        @Override
        public boolean needUnload() {
            return true;
        }

        @Override
        public List<String> getLoadDestructor() {
            return super.getLoadDestructor();
        }

        @Override
        public List<String> getLoadConstructor() {
            return Cs.of(String.format("manager.load(\"%s\", TextureAtlasData.class);", getPath(), getAssetClass()));
        }


        @Override
        public List<String> getDestructor() {
            List<String> lines=new ArrayList<>();
            lines.add(String.format("%s.dispose();",getAssetName()));
         //   lines.addAll(super.getDestructor());
            return lines;
        }





    }

    protected static class AtlasRegionAsset extends Asset {
        private final String atlasName;

        public AtlasRegionAsset(File file, String atlasName) {
            super(file);
            this.atlasName = atlasName;
        }

        @Override
        public Set<String> getImports() {
            Set<String> imports=super.getImports();
            imports.add("import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;");
            return imports;
        }

        @Override
        public String getAssetClass() {
            return "AtlasRegion";
        }

        @Override
        public boolean needLoad() {
            return false;
        }

        @Override
        public boolean needConstructor() {
            return false;
        }

        @Override
        public boolean needInitAfterLoading() {
            return true;
        }

        @Override
        public boolean needDestructor() {
            return false;
        }

        @Override
        public List<String> getInitAfterLoadingConstructor() {
            return Cs.of(String.format("%s = %s.findRegion(\"%s\");", getAssetName(), atlasName, getAssetName()));
        }
    }

    protected static class NinePatchAsset extends Asset {
        private final String atlasName;

        public NinePatchAsset(File file, String atlasName) {
            super(file);
            this.atlasName = atlasName;
        }

        @Override
        public Set<String> getImports() {
            Set<String> imports=super.getImports();
            imports.add("import com.badlogic.gdx.graphics.g2d.NinePatch;");
            return imports;
        }

        @Override
        public String getAssetClass() {
            return "NinePatch";
        }

        @Override
        public boolean needLoad() {
            return false;
        }

        @Override
        public boolean needConstructor() {
            return false;
        }

        @Override
        public boolean needInitAfterLoading() {
            return true;
        }

        @Override
        public boolean needDestructor() {
            return false;
        }

        @Override
        public List<String> getInitAfterLoadingConstructor() {
            return Cs.of(String.format("%s = %s.createPatch(\"%s\");", getAssetName(), atlasName, getAssetName()));
        }
    }
}
