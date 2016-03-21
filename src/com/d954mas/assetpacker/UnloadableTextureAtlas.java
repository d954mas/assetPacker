package com.d954mas.assetpacker;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.*;

/*
99.9% copy-paste becouse of FUCKING PRIVATE FIELDS.I HATE YOU.
 */
public class UnloadableTextureAtlas implements Disposable {
    static final String[] tuple = new String[4];

    private final ObjectSet<Texture> textures = new ObjectSet(4);
    private final Array<AtlasRegion> regions = new Array();
    public boolean isLoad;
    public boolean isInit;


    /**
     * Creates an empty atlas to which regions can be added.
     */
    public UnloadableTextureAtlas() {
    }

    /**
     * Loads the specified pack file using {@link Files.FileType#Internal}, using the parent directory of the pack file to find the page
     * images.
     */
    public UnloadableTextureAtlas(String internalPackFile) {
        this(Gdx.files.internal(internalPackFile));
    }

    /**
     * Loads the specified pack file, using the parent directory of the pack file to find the page images.
     */
    public UnloadableTextureAtlas(FileHandle packFile) {
        this(packFile, packFile.parent());
    }

    public UnloadableTextureAtlas(FileHandle packFile, boolean flip) {
        this(packFile, packFile.parent(), flip);
    }

    public UnloadableTextureAtlas(FileHandle packFile, FileHandle imagesDir) {
        this(packFile, imagesDir, false);
    }

    /**
     * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
     */
    public UnloadableTextureAtlas(FileHandle packFile, FileHandle imagesDir, boolean flip) {
        this(new TextureAtlasData(packFile, imagesDir, flip));
    }

    /**
     * @param data May be null.
     */
    public UnloadableTextureAtlas(TextureAtlasData data) {
        initLoad(data);
    }

    private void initLoad(TextureAtlasData data) {
        ObjectMap<TextureAtlasData.Page, Texture> pageToTexture = new ObjectMap<TextureAtlasData.Page, Texture>();
        for (TextureAtlasData.Page page : data.getPages()) {
            Texture texture = null;
            if (page.texture == null) {
                texture = new Texture(page.textureFile, page.format, page.useMipMaps);
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            } else {
                texture = page.texture;
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            }
            textures.add(texture);
            pageToTexture.put(page, texture);
        }

        for (TextureAtlasData.Region region : data.getRegions()) {
            int width = region.width;
            int height = region.height;
            AtlasRegion atlasRegion = new AtlasRegion(pageToTexture.get(region.page), region.left, region.top,
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
            if (region.flip) atlasRegion.flip(false, true);
            regions.add(atlasRegion);
        }
        isInit = true;
        isLoad = true;
    }

    public void load(TextureAtlasData data) {
        if (data == null) return;
        if (!isInit) {
            initLoad(data);
            return;
        }
        if (isLoad) {
            Gdx.app.log("UnloadableTextureAtlas", "atlas is alredy load");
            return;
        }

        ObjectMap<TextureAtlasData.Page, Texture> pageToTexture = new ObjectMap<TextureAtlasData.Page, Texture>();
        for (TextureAtlasData.Page page : data.getPages()) {
            Texture texture = null;
            if (page.texture == null) {
                texture = new Texture(page.textureFile, page.format, page.useMipMaps);
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            } else {
                texture = page.texture;
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            }
            textures.add(texture);
            pageToTexture.put(page, texture);
        }
        Array<TextureAtlasData.Region> dataRegions = data.getRegions();
        for (int i = 0; i < regions.size; i++) {
            regions.get(i).setTexture(pageToTexture.get(dataRegions.get(i).page));
        }
        isLoad = true;
    }

    //region cope-paste

    /**
     * Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed.
     */
    public AtlasRegion addRegion(String name, Texture texture, int x, int y, int width, int height) {
        textures.add(texture);
        AtlasRegion region = new AtlasRegion(texture, x, y, width, height);
        region.name = name;
        region.originalWidth = width;
        region.originalHeight = height;
        region.index = -1;
        regions.add(region);
        return region;
    }

    /**
     * Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed.
     */
    public AtlasRegion addRegion(String name, TextureRegion textureRegion) {
        return addRegion(name, textureRegion.getTexture(), textureRegion.getRegionX(), textureRegion.getRegionY(),
                textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
    }

    /**
     * Returns all regions in the atlas.
     */
    public Array<AtlasRegion> getRegions() {
        return regions;
    }

    /**
     * Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
     * should be cached rather than calling this method multiple times.
     *
     * @return The region, or null.
     */
    public AtlasRegion findRegion(String name) {
        for (int i = 0, n = regions.size; i < n; i++)
            if (regions.get(i).name.equals(name)) return regions.get(i);
        return null;
    }

    /**
     * Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
     * the result should be cached rather than calling this method multiple times.
     *
     * @return The region, or null.
     */
    public AtlasRegion findRegion(String name, int index) {
        for (int i = 0, n = regions.size; i < n; i++) {
            AtlasRegion region = regions.get(i);
            if (!region.name.equals(name)) continue;
            if (region.index != index) continue;
            return region;
        }
        return null;
    }

    /**
     * Returns all regions with the specified name, ordered by smallest to largest {@link AtlasRegion#index index}. This method
     * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times.
     */
    public Array<AtlasRegion> findRegions(String name) {
        Array<AtlasRegion> matched = new Array();
        for (int i = 0, n = regions.size; i < n; i++) {
            AtlasRegion region = regions.get(i);
            if (region.name.equals(name)) matched.add(new AtlasRegion(region));
        }
        return matched;
    }

    /**
     * Returns all regions in the atlas as sprites. This method creates a new sprite for each region, so the result should be
     * stored rather than calling this method multiple times.
     *
     * @see #createSprite(String)
     */
    public Array<Sprite> createSprites() {
        Array sprites = new Array(regions.size);
        for (int i = 0, n = regions.size; i < n; i++)
            sprites.add(newSprite(regions.get(i)));
        return sprites;
    }

    /**
     * Returns the first region found with the specified name as a sprite. If whitespace was stripped from the region when it was
     * packed, the sprite is automatically positioned as if whitespace had not been stripped. This method uses string comparison to
     * find the region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
     *
     * @return The sprite, or null.
     */
    public Sprite createSprite(String name) {
        for (int i = 0, n = regions.size; i < n; i++)
            if (regions.get(i).name.equals(name)) return newSprite(regions.get(i));
        return null;
    }

    /**
     * Returns the first region found with the specified name and index as a sprite. This method uses string comparison to find the
     * region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
     *
     * @return The sprite, or null.
     * @see #createSprite(String)
     */
    public Sprite createSprite(String name, int index) {
        for (int i = 0, n = regions.size; i < n; i++) {
            AtlasRegion region = regions.get(i);
            if (!region.name.equals(name)) continue;
            if (region.index != index) continue;
            return newSprite(regions.get(i));
        }
        return null;
    }

    /**
     * Returns all regions with the specified name as sprites, ordered by smallest to largest {@link AtlasRegion#index index}. This
     * method uses string comparison to find the regions and constructs new sprites, so the result should be cached rather than
     * calling this method multiple times.
     *
     * @see #createSprite(String)
     */
    public Array<Sprite> createSprites(String name) {
        Array<Sprite> matched = new Array();
        for (int i = 0, n = regions.size; i < n; i++) {
            AtlasRegion region = regions.get(i);
            if (region.name.equals(name)) matched.add(newSprite(region));
        }
        return matched;
    }

    private Sprite newSprite(AtlasRegion region) {
        if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
            if (region.rotate) {
                Sprite sprite = new Sprite(region);
                sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
                sprite.rotate90(true);
                return sprite;
            }
            return new Sprite(region);
        }
        return new AtlasSprite(region);
    }

    /**
     * Returns the first region found with the specified name as a {@link NinePatch}. The region must have been packed with
     * ninepatch splits. This method uses string comparison to find the region and constructs a new ninepatch, so the result should
     * be cached rather than calling this method multiple times.
     *
     * @return The ninepatch, or null.
     */
    public NinePatch createPatch(String name) {
        for (int i = 0, n = regions.size; i < n; i++) {
            AtlasRegion region = regions.get(i);
            if (region.name.equals(name)) {
                int[] splits = region.splits;
                if (splits == null)
                    throw new IllegalArgumentException("Region does not have ninepatch splits: " + name);
                NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
                if (region.pads != null)
                    patch.setPadding(region.pads[0], region.pads[1], region.pads[2], region.pads[3]);
                return patch;
            }
        }
        return null;
    }

    /**
     * @return the textures of the pages, unordered
     */
    public ObjectSet<Texture> getTextures() {
        return textures;
    }
    //endregion

    /**
     * Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
     * and Sprites, which should no longer be used after calling dispose.
     */
    public void dispose() {
        isLoad = false;
        for (Texture texture : textures)
            texture.dispose();
        textures.clear();
    }


}

