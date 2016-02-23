# assetPacker
libgdx util to pack asset
The main idea of utils is generate classes with link to asset.  
The generate class can use fileHandle,atlasRegion from atlas and Animation
#Example Class
 ```
package com.generated;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
public  class ResImages {
    public static ResFiles_atlas files_atlas;
    public static ResGame_atlas game_atlas;
    public static FileHandle coin_down;
    public static FileHandle coin_down2;

    public static void init(AssetManager manager){
        init(manager,false);
    }
    public static void init(AssetManager manager,boolean finishLoading){
        files_atlas = new ResFiles_atlas(manager);
        game_atlas = new ResGame_atlas(manager);
        coin_down = Gdx.files.internal("images/coin_down.png");
        coin_down2 = Gdx.files.internal("images/coin_down2.png");
        if(finishLoading){
            manager.finishLoading();
            onLoadDone(manager);
        }
    }
    public static void onLoadDone(AssetManager manager){
        files_atlas.onLoadDone(manager);
        game_atlas.onLoadDone(manager);
        ResImagesA.onLoadDone();    }
    public static void dispose(AssetManager manager){
        files_atlas.dispose(manager);
        files_atlas = null;
        game_atlas.dispose(manager);
        game_atlas = null;
        coin_down = null;
        coin_down2 = null;
        ResImagesA.dispose();        }
    public static class ResFiles_atlas {
        public TextureAtlas atlas;
        public NinePatch border_bottom;
        public Animation coin;

        public ResFiles_atlas(AssetManager manager){
            init(manager);
        }
        protected void init(AssetManager manager){
            manager.load("images/files_atlas/atlas.atlas", TextureAtlas.class);
        }
        protected void onLoadDone(AssetManager manager){
            atlas = manager.get("images/files_atlas/atlas.atlas");
            border_bottom = atlas.createPatch("border_bottom");
            //region animation coin
            Array<AtlasRegion> coinRegions = new Array<>();
            coinRegions.add(atlas.findRegion("coin"));
            coinRegions.add(atlas.findRegion("coin1"));
            coinRegions.add(atlas.findRegion("coin2"));
            coinRegions.add(atlas.findRegion("coin3"));
            coinRegions.add(atlas.findRegion("coin4"));
            coin = new Animation(1f,coinRegions);
            //endregion
        }
        protected void dispose(AssetManager manager){
            manager.unload("images/files_atlas/atlas.atlas");
            atlas = null;
            border_bottom = null;
            coin = null;
            }
    }
    public static class ResGame_atlas {
        public TextureAtlas atlas;
        public AtlasRegion background;
        public AtlasRegion bankIcon;
        public AtlasRegion bitcoinIcon;
        public AtlasRegion casinoIcon;
        public AtlasRegion cursorIcon;
        public AtlasRegion factoryIcon;
        public AtlasRegion farmIcon;
        public AtlasRegion governmentIcon;
        public AtlasRegion internetStartupIcon;
        public AtlasRegion mineIcon;
        public AtlasRegion moneyTreeIcon;
        public AtlasRegion rainbowIcon;
        public AtlasRegion timeMachineIcon;

        public ResGame_atlas(AssetManager manager){
            init(manager);
        }
        protected void init(AssetManager manager){
            manager.load("images/game_atlas/atlas.atlas", TextureAtlas.class);
        }
        protected void onLoadDone(AssetManager manager){
            atlas = manager.get("images/game_atlas/atlas.atlas");
            background = atlas.findRegion("background");
            bankIcon = atlas.findRegion("bankIcon");
            bitcoinIcon = atlas.findRegion("bitcoinIcon");
            casinoIcon = atlas.findRegion("casinoIcon");
            cursorIcon = atlas.findRegion("cursorIcon");
            factoryIcon = atlas.findRegion("factoryIcon");
            farmIcon = atlas.findRegion("farmIcon");
            governmentIcon = atlas.findRegion("governmentIcon");
            internetStartupIcon = atlas.findRegion("internetStartupIcon");
            mineIcon = atlas.findRegion("mineIcon");
            moneyTreeIcon = atlas.findRegion("moneyTreeIcon");
            rainbowIcon = atlas.findRegion("rainbowIcon");
            timeMachineIcon = atlas.findRegion("timeMachineIcon");
        }
        protected void dispose(AssetManager manager){
            manager.unload("images/game_atlas/atlas.atlas");
            atlas = null;
            background = null;
            bankIcon = null;
            bitcoinIcon = null;
            casinoIcon = null;
            cursorIcon = null;
            factoryIcon = null;
            farmIcon = null;
            governmentIcon = null;
            internetStartupIcon = null;
            mineIcon = null;
            moneyTreeIcon = null;
            rainbowIcon = null;
            timeMachineIcon = null;
            }
    }
}
 ```
#How to use
  1.  download lib assetPacker-1.0-SNAPSHOT.jar  
  2.  Add it to your project in build.gradle  
    1.  copy it to desktop/lib  
    2.  add libs to desktop dependencies, do not forget to add commons-io it used by assetPacker  
      project(":desktop") {  
      ...  
      dependencies {  
        ...  
        compile 'commons-io:commons-io:2.4'  
        compile fileTree(dir: 'libs', include: '*.jar')  
        }  
      }  
  3.  create a class with main method  
    
      
   ```
    public class PackUI {
    public static void main(String[] args) throws IOException {
        Packer packer=new Packer();
        //delete all assets in ./android/assets
        packer.clearAndroidAssets();
        //pack all assets form ./assets
        packer.packAssets(new File("assets"));
        //generate classes
        BundlesCreator creator=new BundlesCreator();
        creator.create(new File("android/assets"));
      }
    }
   ```
