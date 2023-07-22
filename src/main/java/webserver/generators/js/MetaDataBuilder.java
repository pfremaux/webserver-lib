package webserver.generators.js;

public class MetaDataBuilder {
    private static final String quote = "\"";

    private final StringBuilder code = new StringBuilder();
    private int level = 0;

    private enum Step {IN_COMPONENT, OUTSIDE, IN_METADATA}

    ;
    private Step step = Step.OUTSIDE;

    public MetaDataBuilder init() {
        step = Step.IN_METADATA;
        code.append("{\n");
        level++;
        return this;
    }

    public MetaDataBuilder close() {
        step = Step.OUTSIDE;
        code.append("}\n");
        level--;
        return this;
    }

    public MetaDataBuilder addComponent(MetadataComponent component) {
        newComponent(component.id());
        if (component.label() != null) {
            keyValueString("label", component.label());
        }
        keyValueString("type", component.type().name());
        if (component.onClick() != null) {
            keyValueString("onClick", component.onClick());
        }
        return endComponent();
    }

    public MetaDataBuilder then() {
        code.append(",\n");
        return this;
    }

    private MetaDataBuilder newComponent(String id) {
        indent().inQuotes(id);
        code.append(":{\n");
        step = Step.IN_COMPONENT;
        level++;
        return this;
    }

    private MetaDataBuilder endComponent() {
        code.append("},\n");
        step = Step.IN_METADATA;
        level--;
        return this;
    }

    private MetaDataBuilder indent() {
        code.append("\t".repeat(level));
        return this;
    }

    private MetaDataBuilder inQuotes(String data) {
        code.append(quote).append(data).append(quote);
        return this;

    }

    private MetaDataBuilder keyValueString(String key, String value) {
        if (step != Step.IN_COMPONENT)
            throw new IllegalStateException("Should be in metadata component declaration.");
        final String tab = "\t".repeat(level);
        code.append(tab).append(quote).append(key).append(quote).append(":").append(quote).append(value).append(quote).append(",\n");
        return this;
    }

    public StringBuilder getCode() {
        return code;
    }
}