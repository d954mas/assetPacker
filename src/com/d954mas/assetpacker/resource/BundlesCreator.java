package com.d954mas.assetpacker.resource;

import com.d954mas.assetpacker.resource.assets.assetTypes.ResourceBundleAsset;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BundlesCreator {

    public void create(File bundlesFolder) throws IOException {
        System.out.println("I am a lib mafaka");
        FileUtils.forceMkdir(new File("core/src/com/generated"));
       // FileUtils.cleanDirectory(new File("core/src/com/generated"));
        for(File file:bundlesFolder.listFiles()){
            if(file.isDirectory()){
                ResourceBundleAsset resourceBundle=new ResourceBundleAsset(file);
                File resFile=new File("core/src/com/generated/"+resourceBundle.getAssetClass()+".java");
                if(resFile.exists()){
                    resFile.delete();
                }
                resFile.createNewFile();
                FileWriter fileWriter=new FileWriter(resFile);
                fileWriter.write(resourceBundle.getClassString());
                fileWriter.close();
                File aresFile=new File("core/src/com/generated/"+resourceBundle.getAdditionalClass()+".java");
                if(!aresFile.exists()){
                    aresFile.createNewFile();
                    fileWriter=new FileWriter(aresFile);
                    fileWriter.write(resourceBundle.getAdditionalClassString());
                    fileWriter.close();
                }
            }
        }
    }
}
