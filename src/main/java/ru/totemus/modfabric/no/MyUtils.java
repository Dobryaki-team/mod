package ru.totemus.modfabric.no;

import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyUtils {

    public static void writeFile(String file, String data){
        try{
            var fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static String md5OfFile(String path){
        try (InputStream is = Files.newInputStream(Paths.get(path))) {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        }catch (Exception ignore){}
        return null;
    }

    public static String readFile(String file){
        try{
            TotemusMod.logger.info("AAA: "+file);
            var fis = new FileInputStream(file);
            String str = new String(fis.readAllBytes());

            fis.close();
            return str;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(String fileSource, String fileDest){
        try{
            var fis = new FileInputStream(fileSource);

            var fos = new FileOutputStream(fileDest);
            fos.write(fis.readAllBytes());
            fos.close();

            fis.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String encodeFileToBase64(String fileName){
        try {
            FileInputStream file = new FileInputStream(fileName);
            byte[] encoded = Base64.encodeBase64(file.readAllBytes());
            return new String(encoded, StandardCharsets.UTF_8);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод для декодинга файлов
     */
    public static void fileFormBase64(String file, String pathToFile){
        try {
            byte[] data = Base64.decodeBase64(file);
            OutputStream stream = new FileOutputStream(pathToFile);

            stream.write(data);
            stream.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
