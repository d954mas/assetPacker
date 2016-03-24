package com.d954mas.assetpacker.resource.assets.assetFilters.resource.assets.assetTypes;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.d954mas.assetpacker.resource.assets.assetFilters.Cs;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtlasAsset extends AssetWithInlineClass {
    public AtlasAsset(final File file) {
        super(file);
        for (final File file1 : file.listFiles()) {
            if (file1.getName().endsWith(".atlas")) {
                final AtlasFileAsset atlasFileAsset = new AtlasFileAsset(file1);
                assets.add(atlasFileAsset);
                path = path.replace("android/assets/", "");

                FileHandle atlasFileHandle = new FileHandle("./" + file1.getPath());
                TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(atlasFileHandle, atlasFileHandle.parent(), false);
                while (data.getRegions().size != 0) {
                    MyAtlasRegion region = getAtlasRegion(data.getRegions().first());
                    List<MyAtlasRegion> regions = getRegionsByName(region.name, data.getRegions());
                    if (regions.size() == 1) {
                        if (region.splits == null) {
                            assets.add(new AtlasRegionAsset(new File(region.name)
                                    , atlasFileAsset.getAssetName()));
                        } else {
                            assets.add(new NinePatchAsset(new File(region.name)
                                    , atlasFileAsset.getAssetName()));
                        }
                    } else {
                        assets.add(new AnimationAsset(new File(region.name)
                                , atlasFileAsset.getAssetName(), regions));
                    }
                }
            }
        }
    }


    protected MyAtlasRegion getAtlasRegion(TextureAtlas.TextureAtlasData.Region region) {
        int width = region.width;
        int height = region.height;
        MyAtlasRegion atlasRegion = new MyAtlasRegion(region.left, region.top,
                region.rotate ? height : width, region.rotate ? width : height);
        atlasRegion.index = region.index;
        atlasRegion.name = region.name;
        atlasRegion.offsetX = region.offsetX;
        atlasRegion.offsetY = region.offsetY;
        atlasRegion.originalHeight = region.originalHeight;
        atlasRegion.originalWidth = region.originalWidth;
        atlasRegion.rotate = region.rotate;
        atlasRegion.splits = region.splits;
        atlasRegion.pads = region.pads;
        //if (region.flip) atlasRegion.flip(false, true);
        return atlasRegion;
    }

    public Set<String> getImports() {
        Set<String> imports = new HashSet<String>();
        for (Asset asset : assets) {
            imports.addAll(asset.getImports());
        }
        return imports;
    }

    private List<MyAtlasRegion> getRegionsByName(String name, Array<TextureAtlas.TextureAtlasData.Region> regions) {
        name = name.replaceAll("\\d*$", "");
        Pattern p = Pattern.compile("^" + name + "[0-9]*");
        Iterator<TextureAtlas.TextureAtlasData.Region> iterator = regions.iterator();
        List<MyAtlasRegion> list = new ArrayList<>();
        while (iterator.hasNext()) {
            TextureAtlas.TextureAtlasData.Region region = iterator.next();
            Matcher matcher = p.matcher(region.name);
            if (matcher.matches()) {
                list.add(getAtlasRegion(region));
                iterator.remove();
            }
        }
        return list;
    }

    protected static class AnimationAsset extends Asset {
        private final String atlasName;
        private final List<MyAtlasRegion> regions;

        public AnimationAsset(File file, String atlasName, List<MyAtlasRegion> regions) {
            super(file);
            this.atlasName = atlasName;
            this.regions = regions;
        }

        @Override
        public Set<String> getImports() {
            Set<String> imports = super.getImports();
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
            List<String> lines = new ArrayList<>();
            lines.add(String.format("//region animation %s", getAssetName()));
            lines.add(String.format("Array<AtlasRegion> %sRegions = new Array<>();", getAssetName()));
            for (MyAtlasRegion atlasRegion : regions) {
                lines.add(String.format("%sRegions.add(%s.findRegion(\"%s\"));", getAssetName(), atlasName, atlasRegion.name));
            }
            lines.add(String.format("%s = new %s(1f,%sRegions);", getAssetName(), getAssetClass(), getAssetName()));
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
            Set<String> imports = super.getImports();
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
            List<String> lines = new ArrayList<>();
            lines.add(String.format("if(%s==null){", getAssetName()));
            lines.add(String.format("    %s=new %s();", getAssetName(), getAssetClass(), getPath()));
            lines.add("}");
            lines.add(String.format("%s.load((TextureAtlasData)manager.get(\"%s\"));", getAssetName(), getPath()));
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
            List<String> lines = new ArrayList<>();
            lines.add(String.format("%s.dispose();", getAssetName()));
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
            Set<String> imports = super.getImports();
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
            Set<String> imports = super.getImports();
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

    protected static class MyAtlasRegion {
        /**
         * The number at the end of the original image file name, or -1 if none.<br>
         * <br>
         * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
         * part of the sprite's name. This is useful for keeping animation frames in order.
         *
         * @see TextureAtlas#findRegions(String)
         */
        public int index;
        /**
         * The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
         * packer.
         */
        public String name;
        /**
         * The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing.
         */
        public float offsetX;
        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
         * packing.
         */
        public float offsetY;
        /**
         * The width of the image, after whitespace was removed for packing.
         */
        public int packedWidth;
        /**
         * The height of the image, after whitespace was removed for packing.
         */
        public int packedHeight;
        private final int x;
        private final int y;
        /**
         * The width of the image, before whitespace was removed and rotation was applied for packing.
         */
        public int originalWidth;
        /**
         * The height of the image, before whitespace was removed for packing.
         */
        public int originalHeight;
        /**
         * If true, the region has been rotated 90 degrees counter clockwise.
         */
        public boolean rotate;
        /**
         * The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom.
         */
        public int[] splits;
        /**
         * The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom.
         */
        public int[] pads;


        public MyAtlasRegion(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            originalWidth = width;
            originalHeight = height;
            packedWidth = width;
            packedHeight = height;
        }

        /**
         * Returns the packed width considering the rotate value, if it is true then it returns the packedHeight, otherwise it
         * returns the packedWidth.
         */
        public float getRotatedPackedWidth() {
            return rotate ? packedHeight : packedWidth;
        }

        /**
         * Returns the packed height considering the rotate value, if it is true then it returns the packedWidth, otherwise it
         * returns the packedHeight.
         */
        public float getRotatedPackedHeight() {
            return rotate ? packedWidth : packedHeight;
        }

        public String toString() {
            return name;
        }
    }

}
