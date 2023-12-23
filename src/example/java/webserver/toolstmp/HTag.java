package webserver.toolstmp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record HTag(String name, Map<String, String> keys, String text, String onClickAction, boolean isMagic, Map<String, String> magicContext) {

    public HTag {
        Objects.requireNonNull(name);
        Objects.requireNonNull(keys);
        Objects.requireNonNull(text);
        Objects.requireNonNull(onClickAction);
        Objects.requireNonNull(magicContext);
    }


    public HTag(String tagName, Map<String, String> kv, String text, String onClickAction, Map<String, String> magicContext) {
        this(tagName, kv, text, onClickAction, tagName.startsWith("%"), magicContext);
    }

    public HTag(String tagName) {
        this(tagName, new HashMap<>(), "", "", tagName.startsWith("%"), Map.of());
    }
}
