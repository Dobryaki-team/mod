package ru.totemus.modfabric.no;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import static ru.totemus.modfabric.no.MyUtils.md5OfFile;

public class ResourcePack {
    File citDir;
    File dirToResPack;
    static private final String s = File.separator;
    public static final TotemsTimeCache TotemsTimeCacheVar = new TotemsTimeCache();
    public ResourcePack(File d){
        this.dirToResPack = d;
        citDir = new File(dirToResPack.getAbsolutePath() + s + "assets" + s + "minecraft" + s + "optifine" + s + "cit" + s + "totems");
        checkResources();
    }

    public boolean isNeedReloadResources = false;

    public void checkResources(){
        synchronized (this){
            //Если папка ресурсов не существует то создаем ее
            if (!dirToResPack.exists()) {
                if (!dirToResPack.mkdirs())
                    throw new RuntimeException("Cant be load Totemus Res-Pack dir .1");
            }else
                //Если папка ресурсов не является директорией то
                if (!dirToResPack.isDirectory()) {

                    //Удаляеем этот файл
                    if (dirToResPack.delete())
                        throw new RuntimeException("Cant be load Totemus Res-Pack dir .2");

                    //Создаем папку
                    if (dirToResPack.mkdirs())
                        throw new RuntimeException("Cant be load Totemus Res-Pack dir .3");
                }

            //Если файла pack.mcmeta нету то
            if(!new File(dirToResPack.getAbsolutePath() + s + "pack.mcmeta").exists())
                //Создаем его
                MyUtils.writeFile(dirToResPack.getAbsolutePath() + s + "pack.mcmeta", """
                    {
                      "pack": {
                        "pack_format": 15,
                        "description": "Тотемы людей от ToTeMuS.space"
                      }
                    }""");

            //Если папки CIT ресурсов не существует то
            if (!citDir.exists()) {
                //Создаем ее
                if (!citDir.mkdirs())
                    throw new RuntimeException("Cant be load Totemus Assets dir .1");
            }else
                //Если она является файлом то
                if (!citDir.isDirectory()) {
                    //Удаляем файл
                    if (citDir.delete())
                        throw new RuntimeException("Cant be load Totemus Assets dir .2");

                    //Создаем на его месте папку
                    if (citDir.mkdirs())
                        throw new RuntimeException("Cant be load Totemus Assets dir .3");
                }
        }
    }

    public String checkIsTotemExist(String nick){
        try {
            nick = nick.toLowerCase();
            var f = new File(citDir.getAbsolutePath()+s+nick+".png");
            var attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);

            if(f.exists() && !f.isDirectory() && (System.currentTimeMillis() - attr.creationTime().toMillis()) < 60 * 1000) return f.getAbsolutePath();
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void getTotemWithConnector(String nick, TotemusSocketConector connector){
        nick = nick.toLowerCase();
        if(checkIsTotemExist(nick) != null) return;
        connector.sendGetTotem(nick);
    }

    public void addTotem(String file, String name){
        name = name.toLowerCase().trim();
        if(!file.trim().equals((citDir.getAbsolutePath()+s+name+".png").trim())){
            File f = new File(citDir.getAbsolutePath()+s+name+".png");
            if(f.exists()) {
                String md5NewFile = md5OfFile(file);
                if(md5NewFile == null) throw new RuntimeException("Cant be add totem .3.1");

                String md5OldFile = md5OfFile(citDir.getAbsolutePath()+s+name+".png");
                if(md5OldFile == null) throw new RuntimeException("Cant be add totem .3.2");

                if(md5OldFile.equals(md5NewFile)) return; //if file no changed - skip

                if (!f.delete()) throw new RuntimeException("Cant be add totem .4");
            }

            MyUtils.copyFile(file, citDir.getAbsolutePath()+s+name+".png");
        }

        if(!new File(citDir.getAbsolutePath()+s+name+".properties").exists())
            MyUtils.writeFile(citDir.getAbsolutePath()+s+name+".properties", "type=item\n" +
                "matchItems=minecraft:totem_of_undying\n" +
                "texture="+name+"\n" +
                "nbt.display.Name="+name);

        isNeedReloadResources = true;
    }

    public static class TotemSkin{
        public final String pathToSkin;
        public final String userName;
        public boolean isSkinDownloaded = false;
        public TotemSkin(String pathToSkin, String userName){
            this.pathToSkin = pathToSkin;
            this.userName = userName;
        }
        public void downloadSkin(TotemusSocketConector connector){
            if(isSkinDownloaded) return;
            //connector.webSocketClient.send();
        }
    }
}
