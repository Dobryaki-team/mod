package ru.totemus.modfabric.no;

import java.util.HashMap;

public class TotemsTimeCache extends HashMap<String, Long>{
    public Long getTotem(String nick) {
        Long l = get(nick);
        if(l == null) return null;
        if((System.currentTimeMillis() - l) > 5 * 60 * 1000) return null;
        return l;
    }

    public Long cacheTotem(String nick) {
        return put(nick, System.currentTimeMillis());
    }
}
