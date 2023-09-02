package tools;


import webserver.annotations.JsonField;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonMapper {

    private JsonMapper() {

    }

    public static StringBuilder fillWithJsonFormat(StringBuilder builder, Map<String, String> stringStringMap) {
        builder.append("{");
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            builder.append("\"");
            builder.append(entry.getKey());
            builder.append("\":\"");
            builder.append(entry.getValue());
            builder.append("\"");
            builder.append(",");
        }
        if (!stringStringMap.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("}");
        return builder;
    }

    public static <T> T jsonToObject(StringBuilder data, Class<T> tClass)
            throws NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String currentField = null;
        StringBuilder currentValueBeingBuilt = new StringBuilder();
        Class<?> currentObject = tClass;
        ReadingMode readingMode = ReadingMode.NONE;
        Map<String, Object> attributes = new HashMap<>();
        StringBuilder fieldNameBuilder = new StringBuilder();
        List<Object> arrayBeingBuilt = new ArrayList<>();
        boolean isTypeNumber = false;
        boolean isReadingObject = false;
        boolean isInArray = false;
        int i = -1;
        int bypassIndex = -1;
        int bracketOpened = 0;
        for (char c : data.toString().toCharArray()) {
            i++;
            if (bypassIndex > -1) {
                if (i < bypassIndex) {
                    continue;
                } else {
                    bypassIndex = -1;
                }
            }
            if (c == '{') {
                bracketOpened++;
                if (currentField == null) {
                    isReadingObject = true;
                    attributes = new HashMap<>();
                } else {
                    // in an inner object we need to figure out which object it is.
                    final Field declaredField = currentObject.getDeclaredField(currentField);
                    final int closingCharacterIndex = findClosingCharacter(data, i);
                    bypassIndex = closingCharacterIndex;
                    final Object subObject = jsonToObject(new StringBuilder(data.substring(i, closingCharacterIndex)),
                            declaredField.getType());
                    if (isInArray) {
                        arrayBeingBuilt.add(subObject);
                    } else {
                        attributes.put(currentField, subObject);
                    }
                }
            } else if (c == '}') {
                if (isTypeNumber) {
                    Field declaredField = currentObject.getDeclaredField(currentField);
                    attributes.put(currentField, jsonToObject(currentValueBeingBuilt, declaredField.getType()));
                    currentValueBeingBuilt.setLength(0);
                    isTypeNumber = false;
                    currentField = null;
                    readingMode = ReadingMode.NONE;
                    Object[] parameters = new Object[currentObject.getDeclaredConstructors()[0].getParameters().length];
                    int counter = 0;
                    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                        parameters[counter] = entry.getValue();
                        counter++;
                    }
                    return (T) currentObject.getDeclaredConstructors()[0].newInstance(parameters);
                } else {
                    currentObject = tClass;
                    Object[] parameters = new Object[currentObject.getDeclaredConstructors()[0].getParameters().length];
                    int counter = 0;
                    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                        parameters[counter] = entry.getValue();
                        counter++;
                    }
                    return (T) currentObject.getDeclaredConstructors()[0].newInstance(parameters);
                }
            } else if (c == ',') {
                if (isInArray) {

                } else if (isTypeNumber) {
                    Field declaredField = currentObject.getDeclaredField(currentField);
                    attributes.put(currentField, jsonToObject(currentValueBeingBuilt, declaredField.getType()));
                    currentValueBeingBuilt.setLength(0);
                    isTypeNumber = false;
                    currentField = null;
                    readingMode = ReadingMode.NONE;
                }
            } else if (c == '[') {
                isInArray = true;
                readingMode = ReadingMode.VALUE;
                arrayBeingBuilt.clear();
            } else if (c == ':') {

            } else if (c == ']') {
                attributes.put(currentField, new ArrayList<>(arrayBeingBuilt));
                arrayBeingBuilt.clear();
                isInArray = false;
            } else if (c == '"') {
                if (!isReadingObject) {
                    currentValueBeingBuilt.append(c);
                } else if (readingMode == ReadingMode.NONE && currentField == null) {
                    fieldNameBuilder.setLength(0);
                    readingMode = ReadingMode.FIELD;
                } else if (readingMode == ReadingMode.FIELD) {
                    currentField = fieldNameBuilder.toString();
                    readingMode = ReadingMode.WAITING_FOR_VALUE;
                } else if (readingMode == ReadingMode.WAITING_FOR_VALUE) {
                    currentValueBeingBuilt.setLength(0);
                    readingMode = ReadingMode.VALUE;
                } else if (readingMode == ReadingMode.VALUE) {
                    if (isInArray) {
                        Field declaredField = currentObject.getDeclaredField(currentField);
                        arrayBeingBuilt.add(jsonToObject(currentValueBeingBuilt, declaredField.getType()));
                        currentField = null;
                        currentValueBeingBuilt.setLength(0);
                        readingMode = ReadingMode.VALUE;
                    } else {
                        Field declaredField = currentObject.getDeclaredField(currentField);
                        attributes.put(currentField, jsonToObject(currentValueBeingBuilt, declaredField.getType()));
                        currentField = null;
                        currentValueBeingBuilt.setLength(0);
                        readingMode = ReadingMode.NONE;
                    }

                } else {
                    // TODO error ?
                }
            } else {
                if (readingMode == ReadingMode.FIELD) {
                    fieldNameBuilder.append(c);
                } else if (readingMode == ReadingMode.VALUE) {
                    currentValueBeingBuilt.append(c);
                } else if (!isReadingObject) {
                    currentValueBeingBuilt.append(c);
                } else if (readingMode == ReadingMode.WAITING_FOR_VALUE && c != ' ') {
                    isTypeNumber = true;
                    currentValueBeingBuilt.append(c);
                }
            }
        }

        if (!isReadingObject) {
            String s = currentValueBeingBuilt.toString();
            if (tClass.getSimpleName().equals("int") || tClass.getSimpleName().equals("Integer")) {
                return (T) Integer.valueOf(s);
            } else if (tClass.getSimpleName().equals("Long")) {
                return (T) Long.valueOf(s);
            } else if (tClass.getSimpleName().equals("String")) {
                return (T) s;
            }
            // tClass.getDeclaredConstructors()[0].newInstance();
        }
        return null;
    }

    private static int findClosingCharacter(StringBuilder builder, int fromIndex) {
        final char closingCharacter;
        final char openingCharacter = builder.charAt(fromIndex);
        if (openingCharacter == '{') {
            closingCharacter = '}';
        } else if (openingCharacter == '[') {
            closingCharacter = ']';
        } else {
            throw new IllegalStateException(
                    "Can't figure out the closing character since the opening character is not valid. ["
                            + "expectedOpeningCharacter={ / [ ; " + "fromIndex=" + fromIndex + " ; "
                            + "openingCharacter=" + openingCharacter + " ; ");
        }
        int countInnerOpenedCharacter = 0;
        for (int i = fromIndex; i < builder.length(); i++) {
            if (builder.charAt(i) == closingCharacter && countInnerOpenedCharacter == 0) {
                return i;
            } else if (builder.charAt(i) == closingCharacter && countInnerOpenedCharacter > 0) {
                countInnerOpenedCharacter--;
            } else if (builder.charAt(i) == openingCharacter) {
                countInnerOpenedCharacter++;
            }
        }
        throw new IllegalStateException("No closing character found. [data=" + builder + "]");
    }

    public static StringBuilder objectToJsonExample(Class<?> result) throws ClassNotFoundException {
        final StringBuilder builder = new StringBuilder();
        final Set<String> nameToField = Stream.of(result.getDeclaredFields())
                .filter(field -> field.getAnnotation(JsonField.class) != null)
                .map(field -> field.getName().toLowerCase()).collect(Collectors.toSet());

        builder.append("{");
        for (Method declaredMethod : result.getDeclaredMethods()) {
            if (!declaredMethod.getName().startsWith("get")) {
                continue;
            }
            if (declaredMethod.getName().equals("toto")) {
                continue;
            }
            final String attributeName = declaredMethod.getName().substring(3).toLowerCase();
            if (!nameToField.contains(attributeName)) {
                throw new IllegalArgumentException("Unexpected attribute name: %s in class %s. Did you forget to add @JsonField above the attribute declaration ?".formatted(attributeName, result.getName()));
            }

            Class<?> value = declaredMethod.getReturnType();

            final String simpleName = value.getSimpleName();
            builder.append("\"");

            builder.append(declaredMethod.getName().substring(3, 4));
            builder.append(declaredMethod.getName().substring(4));
            builder.append("\":");
            if (value.getSimpleName().equals("List")) {
                builder.append("[");
                Type genericReturnType = declaredMethod.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType) {
                    final ParameterizedType paramType = (ParameterizedType) genericReturnType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    /*If an unsupported type is passed, it would be good to log it here
                    if (argTypes.length > 0) {
                        System.out.println("Generic type is " + argTypes[0]);
                    }*/
                    String typeName = argTypes[0].getTypeName();
                    String simpleNameGeneric = typeName.substring(typeName.lastIndexOf(".") + 1);
                    if (BASE_TYPES.contains(simpleNameGeneric)) {
                        builder.append(simpleNameGeneric);
                    } else {
                        builder.append(objectToJsonExample(Class.forName(typeName)));
                    }
                }

                builder.append("]");
            } else {
                if (value.isAssignableFrom(Number.class)
                        || BASE_TYPES.contains(value.getSimpleName())) {
                    builder.append(simpleName);
                } else if (value.isAssignableFrom(String.class)) {
                    builder.append("\"String\"");
                } else {
                    builder.append(objectToJsonExample(value));
                }
            }

            builder.append(",");

        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder;
    }

    public static StringBuilder objectToJson(Object result) {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Method declaredMethod : result.getClass().getDeclaredMethods()) {
            if (!declaredMethod.getName().startsWith("get")) {
                continue;
            }
            try {
                final Object value = declaredMethod.invoke(result);
                final String simpleName = value.getClass().getSimpleName();
                builder.append("\"");
                builder.append(declaredMethod.getName().substring(3, 4).toLowerCase());
                builder.append(declaredMethod.getName().substring(4));
                builder.append("\":");
                addValue(builder, value, simpleName);

                builder.append(",");
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder;
    }

    public static Map<String, String> objectToMapDescriptor(Class<?> object) {
        return Arrays.stream(object.getDeclaredFields())
                .collect(Collectors.toMap(
                        Field::getName,
                        f -> f.getType().getSimpleName()));
    }

    private static void addValue(StringBuilder builder, Object value, String simpleName) {
        switch (simpleName) {
            case "int", "Integer", "Long" -> builder.append(value);
            case "String" -> {
                builder.append("\"");
                builder.append(value);
                builder.append("\"");
            }
            case "List12", "ArrayList" -> {

                List<Object> lst = (List<Object>) value;
                builder.append("[");
                String simpleName1 = null;
                for (Object o : lst) {
                    simpleName1 = simpleName1 == null ? o.getClass().getSimpleName() : simpleName1;
                    if (BASE_TYPES.contains(simpleName1)) {
                        switch (simpleName1) {
                            case "int", "Integer", "Long", "Double", "Float" -> builder.append(o);
                            case "String" -> {
                                builder.append("\"");
                                builder.append(o);
                                builder.append("\"");
                            }
                            default ->
                                    throw new IllegalArgumentException("expects only base types here. Type=" + simpleName1);
                        }
                    } else {
                        builder.append(objectToJson(o));
                    }

                    builder.append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append("]");
            }
            case "HashSet" -> {

                Set<Object> lst = (Set<Object>) value;
                builder.append("[");
                String simpleName1 = null;
                for (Object o : lst) {
                    simpleName1 = simpleName1 == null ? o.getClass().getSimpleName() : simpleName1;
                    if (BASE_TYPES.contains(simpleName1)) {
                        switch (simpleName1) {
                            case "int", "Integer", "Long", "Double", "Float" -> builder.append(o);
                            case "String" -> {
                                builder.append("\"");
                                builder.append(o);
                                builder.append("\"");
                            }
                            default ->
                                    throw new IllegalArgumentException("expects only base types here. Type=" + simpleName1);
                        }
                    } else {
                        builder.append(objectToJson(o));
                    }

                    builder.append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append("]");
            }
            default -> builder.append(objectToJson(value));
        }
    }

    private final static List<String> BASE_TYPES = List.of("int", "Integer", "Long", "Double", "Float", "String");

    enum ReadingMode {
        FIELD, VALUE, WAITING_FOR_VALUE, NONE
    }


}
