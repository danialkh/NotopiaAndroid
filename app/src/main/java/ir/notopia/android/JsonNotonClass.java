package ir.notopia.android;

import java.util.Map;

public class JsonNotonClass {

    private Map<String, Map<String, String>> map;

    public JsonNotonClass() {
    }

    public JsonNotonClass(Map<String, Map<String, String>> map) {
        this.map = map;
    }

    public Map<String, Map<String, String>> getMap() {
        return map;
    }

    public void setMap(Map<String, Map<String, String>> map) {
        this.map = map;
    }
}
