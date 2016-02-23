package com.d954mas.assetpacker.packer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Packer {

    //move files and pack atlases from assetsFolder
    public void packAssets(File assetsFolder) throws IOException {
        for(File file:assetsFolder.listFiles()){
            if(file.isDirectory()){
                FolderPacker folderPacker=new FolderPacker(file);
                folderPacker.pack();
            }
        }
    }

    //deleate all files from adnroid/assets
    public void clearAndroidAssets() throws IOException {
        FileUtils.cleanDirectory(new File("./android/assets"));
    }
}
