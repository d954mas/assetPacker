package com.d954mas.assetpacker.packer;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Json;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

/*
Folder packer is packing all folders of his parent
if folder is ends by atlas it means that in folder only images.
You can set your own TexturePacker.Settings by json with name __settings
else folder will be copy to android/assets/...

 */

public class FolderPacker {
    protected File folder;
    private static final String ASSETS_PATH="android/assets/";
    public FolderPacker(File folder){
        this.folder = folder;
    }

    public void pack() throws IOException {
        for(File file:folder.listFiles()){
            String name=file.getName();
            String path=file.getPath().replace("\\","/");//fixed window
            String moveToName=ASSETS_PATH+path.substring(path.indexOf("/")+1);
            if(file.isDirectory()){

                if(name.endsWith("atlas")){
                    File[] files=file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.equals("__settings");
                    }
                    });
                    if(files.length==1){
                        TexturePacker.process(new Json().fromJson(TexturePacker.Settings.class, new FileReader(files[0])), file.getPath(), moveToName, "atlas");
                    }else{
                      //  TexturePacker.Settings setting=new TexturePacker.Settings();
                      //  Json json=new Json();
                        //json.setUsePrototypes(false);
                    //    System.out.println(json.prettyPrint(setting));
                        TexturePacker.process(file.getPath(), moveToName, "atlas");
                    }
                }else {
                    moveToName=ASSETS_PATH+path.substring(path.indexOf("/"),path.lastIndexOf("/"));
                    FileUtils.copyDirectory(file,new File(moveToName));
                }
               // System.out.println(String.format("new directory:"+file.getPath()));
              //  FolderPacker folderPacker=new FolderPacker(file);
             //   folderPacker.pack();
            }else{
                FileUtils.copyFileToDirectory(file,new File(moveToName).getParentFile());
               // Asset asset= AssetFactory.getAssetForFile(file);
           //     System.out.println("asset:"+asset);
            }
        }
    }
}
