package ru.totemus.modfabric.no;

import org.json.JSONObject;

import java.io.File;

public class TotemusSettings {
    private JSONObject object;
    public String UserToken;
    private String pathToFile;
    public TotemusSettings(File fileToSettings){
        pathToFile = fileToSettings.getAbsolutePath();

        if(!fileToSettings.exists()){
            MyUtils.writeFile(pathToFile, "{}");
        }

        object = new JSONObject(MyUtils.readFile(fileToSettings.getAbsolutePath()));

        UserToken = object.optString("UserToken", null);
    }
    public void setToken(String s){
        UserToken = s;
        object.put("UserToken", s);
        MyUtils.writeFile(pathToFile, object.toString(1));
    }
}
