package webserver.toolstmp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record HTag(String name, Map<String, String> keys, String text, String onClickAction, boolean isMagic) {

    public HTag {
        Objects.requireNonNull(name);
        Objects.requireNonNull(keys);
        Objects.requireNonNull(text);
        Objects.requireNonNull(onClickAction);
    }


    public HTag(String tagName, Map<String, String> kv, String text, String onClickAction) {
        this(tagName, kv, text, onClickAction, tagName.startsWith("%"));
    }

    public HTag(String tagName) {
        this(tagName, new HashMap<>(), "", "", tagName.startsWith("%"));
    }
}
