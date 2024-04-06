package ru.totemus.modfabric.no;

import java.util.HashMap;

public class TotemsTimeCache {
    private HashMap<String, Long> hs = new HashMap<>();
    public boolean getTotem(String nick) {
        try {
            nick = nick.toLowerCase();
            Long l = hs.get(nick);
            if(l == null) return true;
            return (System.currentTimeMillis() - l) > 2 * 60 * 1000;
        } finally {
            cacheTotem(nick);
        }
    }

    public void cacheTotem(String nick) {
        nick = nick.toLowerCase();
        hs.put(nick, System.currentTimeMillis());
    }
}
