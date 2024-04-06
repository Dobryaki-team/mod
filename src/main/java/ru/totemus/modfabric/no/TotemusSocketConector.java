package ru.totemus.modfabric.no;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class TotemusSocketConector {
    WebSocketClient webSocketClient;
    public SocketConnect onConnected;
    public ResourcePack resourcePack = null;

    public void addSyncTask(Runnable r){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    while (webSocketClient == null || !webSocketClient.isOpen() || webSocketClient.isClosed())
                        Thread.sleep(500);

                    r.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface SocketConnect{
        void onConnect(WebSocketClient client);
        void onAccessDenied(WebSocketClient client, String reason);
    }

    public TotemusSocketConector(String address){
        this(address, null);
    }

    public TotemusSocketConector(String address, SocketConnect onConnected){
        this(address, onConnected, null);
    }

    public String address;

    public TotemusSocketConector(String address, SocketConnect onConnected, ResourcePack pack){
        this.onConnected = onConnected;
        this.address = address;
        resourcePack = pack;

        addSyncTask(new Runnable() {
            @Override
            public void run() {
                onConnected.onConnect(webSocketClient);
            }
        });
    }

    public void init(String addr) throws URISyntaxException {
        if(webSocketClient != null) webSocketClient.close();

        address = addr;

        webSocketClient = new WebSocketClient(new URI(addr)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {

            }

            @Override
            public void onMessage(String message) {
                try {
                    var a = new ResponsePacket(SafetyResponseToJson(message));

                    if(!a.isOk) return;

                    if(a.body.get("type").equals("send_totem") && resourcePack != null){
                        totemGetWorker(a);
                    }

                    else if(a.body.getString("type").equals("new_totem") && resourcePack != null){
                        if(!a.body.isNull("nick"))
                            sendGetTotem(a.body.getString("nick"));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                var o = checkIsAccessDenied(reason);
                if(o != null){
                    if(onConnected != null) onConnected.onAccessDenied(webSocketClient, o.errorReason);
                    else throw new AccessDenied(reason);
                }
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        synchronized (webSocketClient) {
            webSocketClient.connect();
        }
    }

    public void checkAndReconnect(){
        if(!webSocketClient.isOpen()) webSocketClient.reconnect();
    }

    public AccessDenied checkIsAccessDenied(String reason) {
        try {
            var b = new JSONObject(reason);
            if(b.getInt("err_code") == 403){
                var a = new AccessDenied();
                a.errorBody = reason;
                a.errorReason = b.getString("err");
                return a;
            }else return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static class AccessDenied extends RuntimeException{
        public String errorBody;
        public String errorReason;
        public AccessDenied(String e){
            errorBody = e;
        }
        public AccessDenied(){}
    }

    public void sendGetTotem(String nick){
        nick = nick.toLowerCase();

        var obj = new JSONObject();

        obj.put("type", "get_totem");
        obj.put("task_id", 0);
        obj.put("mode", "nick");
        obj.put("nick", nick);

        addSyncTask(new Runnable() {
            @Override
            public void run() {
                webSocketClient.send(obj.toString());
            }
        });
    }

    private void totemGetWorker(ResponsePacket pa){
        if(pa == null) return;
        if(!pa.isOk) return;
        if(pa.body.isNull("img")) return;
        if(pa.body.isNull("nick")) return;

        pa.body.put("nick", pa.body.getString("nick").toLowerCase());

        var tempDir = new File("TotemusTemp"+File.separator);
        if(!tempDir.exists()) tempDir.mkdirs();

        String tempPath = "TotemusTemp"+File.separator+pa.body.getString("nick").toLowerCase();

        ResourcePack.TotemSkin t = new ResourcePack.TotemSkin(resourcePack.citDir.getAbsolutePath()+File.separator+pa.body.getString("nick").toLowerCase()+".png",
                pa.body.getString("nick").toLowerCase());

        MyUtils.fileFormBase64(pa.body.getString("img"), tempPath);

        t.isSkinDownloaded = true;
        resourcePack.addTotem(tempPath, t.userName);

        new File(tempPath).delete();
    }

//    public ResourcePack.TotemSkin getTotemByUuid(String uuid, ResourcePack pack){
//        var obj = new JSONObject();
//
//        obj.put("type", "get_totem");
//        obj.put("mode", "uuid");
//        obj.put("uuid", uuid);
//
//        ResponsePacket pa = SyncSend(obj);
//        if(!pa.isOk) return null;
//        if(pa.body.isNull("img")) return null;
//        if(pa.body.isNull("nick")) return null;
//
//        MyUtils.fileFormBase64(pa.body.getString("img"),
//                pack.citDir.getAbsolutePath()+File.separator+pa.body.getString("nick").toLowerCase()+".png");
//
//        ResourcePack.TotemSkin t = new ResourcePack.TotemSkin(pack.citDir.getAbsolutePath()+File.separator+pa.body.getString("nick").toLowerCase()+".png",
//                pa.body.getString("nick").toLowerCase());
//
//        t.isSkinDownloaded = true;
//        pack.addTotem(t.pathToSkin, t.userName);
//
//        return t;
//    }

    private static JSONObject SafetyResponseToJson(String response){
        if(response == null) return null;
        try{
            return new JSONObject(response);
        }catch (JSONException e){
            return null;
        }
    }

    public interface Handler{
        void onGet(ResponsePacket response);
    }

    public static class ResponsePacket{
        final public boolean isOk;
        final public boolean isBroadcast;
        final public int ErrCode;
        final public int TaskId;
        final public String Error;
        final public JSONObject body;
        public ResponsePacket(JSONObject JsonBody){
            if(JsonBody == null){
                body = null;
                isOk = false;
                ErrCode = 505;
                Error = "Packet Decoding Error";
                isBroadcast = false;
                TaskId = 0;
                return;
            }

            body = JsonBody;
            isOk = JsonBody.optBoolean("OK", false);
            ErrCode = JsonBody.optInt("err_code", 200);
            Error = JsonBody.optString("err", null);
            isBroadcast = JsonBody.optBoolean("is_broadcast", false);
            TaskId = JsonBody.optInt("task_id", 0);
        }
    }
}
