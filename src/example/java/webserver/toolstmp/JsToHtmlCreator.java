package webserver.toolstmp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/*
 * TODO PFR this solution seems awful. might be good to just give it up.
 * keep working on html script where I should iterate over arrays of maps thanks to first character in line '%'
 * example of generate JS in:
 * main/web/example/example.html
 */

public class JsToHtmlCreator {


    private final static Set<String> dynamiList = Set.of("button");
    private final static String input = """
            installWelcome:param1\\n
            h3 'Hello save text now'\\n
            ul\\n
                li\\n
                    input type='text' id='idTextToSave'\\n
                li\\n
                    button =>saveText() innerHTML='Save'\\n
            h2 'Ok again'\\n
                        """;

    public static void main(String[] args) throws IOException {
        String outputFile = "out.js";
        final String f = "file.txt";
        StringBuilder jsFunctions = convertStringScriptToJs(input);
        System.out.println(jsFunctions.toString());
    }

    public static String convert(String script) {
        return convertStringScriptToJs(script).toString();
    }

    public static void convertStringScriptToJs(Path input, Path output) throws IOException {
        final List<String> lines = Files.readAllLines(input);
        final StringBuilder stringBuilder = convertLinesScriptToJs(lines);
        Files.writeString(output, stringBuilder);
    }

    private static StringBuilder convertStringScriptToJs(String input) {
        String[] split1 = input.replaceAll("\\t", "    ").split("\\\\n");
        final List<String> lines = new ArrayList<>(Arrays.asList(split1));
        return convertLinesScriptToJs(lines);
    }

    private static StringBuilder convertLinesScriptToJs(List<String> lines) {
        final NodeV2<HTag> root = NodeV2.root();
        NodeV2<HTag> currentNode = root.addDir("rootCode");
        int previousIndentationSpaces = 0;
        // TODO PFR remove remove() et gerer first line autrement
        final String firstLineDeclarationFunction = lines.remove(0);
        final Map<String, String> magicContext = new HashMap<>();
        for (String line : lines) {
            final int countIndentationSpaces = countIndentation(line.replace("\\t", "    "));
            line = line.trim().replaceAll("\\n", "").replace("\\t", "").replace("\t", "");
            if (line.isEmpty()) {
                continue;
            }

            final HTag tag;
            if (isTagName(line.trim().replaceAll("\\t", ""))) {
                tag = toTag(line, magicContext);
            } else {
                tag = updateTag(currentNode.getValue(), line, magicContext);
                currentNode.setValue(tag);
                previousIndentationSpaces = countIndentationSpaces;
                continue;
            }

            if (countIndentationSpaces == previousIndentationSpaces) {
                currentNode = currentNode.addSibling(tag);
            } else if (countIndentationSpaces < previousIndentationSpaces) {
                int parentCount = (previousIndentationSpaces - countIndentationSpaces) / 4;
                for (int i = 0; i < parentCount; i++) {
                    currentNode = currentNode.getParent();
                }
                currentNode = currentNode.addSibling(tag);
            } else if (countIndentationSpaces > previousIndentationSpaces) {
                // There's no scenario where we're deeper more than 1
                currentNode = currentNode.addLeaf(tag);
            }

            previousIndentationSpaces = countIndentationSpaces;
        }
        final StringBuilder functionSource = new StringBuilder();
        return functionSource.append(getJsFunctionDeclaration(firstLineDeclarationFunction)).append(" {\n")
                .append("\tconst container = document.getElementById(containerId);\n    return appendTo(container")
                .append(toJs(root, 1).append(");\n}"));
    }

    private static boolean isTagName(String s) {
        // TODO PFR completer
        return Set.of("%", "ul", "li", "input", "h1", "h2", "h3").contains(s);
    }

    private static StringBuilder getJsFunctionDeclaration(String firstLineDeclarationFunction) {
        final StringBuilder builder = new StringBuilder("function ");
        final String[] split = firstLineDeclarationFunction.split(":");
        builder.append(split[0]).append("(containerId, ");
        for (int i = 1; i < split.length; i++) {
            builder.append(split[i]).append(", ");
        }
        if (split.length > 1) {
            builder.setLength(builder.length() - 2);
        }
        builder.append(")");
        return builder;
    }

    private static StringBuilder toJs(NodeV2<HTag> currentTag, int indentationLevel) {
        final StringBuilder b = new StringBuilder();

        for (NodeV2<HTag> child : currentTag.getChildren()) {
            // Converts child node to a JS code
            final StringBuilder jsPiece = toJs(child, indentationLevel + 1);
            if (jsPiece.isEmpty()) {
                continue;
            }
            if (currentTag.getParent() == null) {
                b.append(",\n").append(indent(indentationLevel, true)).append(jsPiece);
            } else {
                if (child.getValue().isMagic()) {
                    // todo pfr %
                    final String dataIteration = child.getValue().keys().get("data");
                    final String[] split = dataIteration.split(" ");
                    final String childrenFunctionsParameters = "%s.map(e=>%s), e=>%s, e=>%s".formatted(split[0], split[1],  split[2], jsPiece);
                    /*
TODO PFR following should work. now we must insert variables in children
ul
	% test.obj true e
		li 'pi'
                    * */
                    // st('ul').children(testss.titles.map(e=>e.main), e=>true, (c=>st('li', c)) )
                    b.append("\n").append(indent(indentationLevel, true)).append(".children(").append(childrenFunctionsParameters).append("\n").append(indent(indentationLevel, true)).append(")");
                } else {
                    b.append("\n").append(indent(indentationLevel, true));
                    if (!currentTag.getValue().isMagic()) {
                        b.append(".child(");
                    }
                    b.append(jsPiece).append("\n").append(indent(indentationLevel, true));
                    if (!currentTag.getValue().isMagic()) {
                        b.append(")");
                    }
                }
            }
        }
        if (currentTag.getValue() != null) {
            return tagToJs(currentTag.getValue()).append(b);
        }
        return b;
    }

    private static StringBuilder tagToJs(HTag tag) {
        StringBuilder b = new StringBuilder();
        if (tag.isMagic()) {
            // TODO PFR valider
            final String dataIteration = tag.keys().get("data");
            final String[] split = dataIteration.split(" ");
            final String childrenFunctionsParameters = "%s.map(e=>%s), e=>%s, e=>%s".formatted(split[0], split[1],  split[2], "---");
            return b;
        }
        if (tag.onClickAction().length() > 0) {
            b.append("dt('");
        } else {
            b.append("st('");
        }
        b.append(tag.name()).append("'");
        if (tag.text().length() > 0) {
            b.append(", '").append(tag.text()).append("'");
        }
        if (tag.onClickAction().length() > 0) {
            b.append(", evt => ").append(tag.onClickAction());
        }
        b.append(")");
        if (!tag.keys().isEmpty()) {
            b.append(".andDo(tag => {");
            for (Map.Entry<String, String> entry : tag.keys().entrySet()) {
                b.append("tag.").append(entry.getKey()).append("=").append("'").append(entry.getValue()).append("';");
            }
            b.append("})");
        }
        return b;
    }

    private static HTag toTag(String line, Map<String, String> magicContext) {
        final int firstSpaceIndex = line.indexOf(" ");
        /*if (firstSpaceIndex == -1) {
            return new HTag(line.trim().replaceAll("\\t", ""));
        }*/
        final String tagName;
        if (firstSpaceIndex == -1) {
            tagName = line.trim().replaceAll("\\t", "");
        } else {
            tagName= line.replaceAll("\\t", "").substring(0, firstSpaceIndex);
        }
        if (tagName.startsWith("%")) {
            return new HTag(tagName, Map.of("data", line.substring(firstSpaceIndex + 1)), "", "", true, magicContext);
        }
        final String remainingLine = line.substring(firstSpaceIndex + 1);
        List<String> groupwords = cutAttributes(remainingLine);
        String text = "";
        String onClickAction = "";
        Map<String, String> kv = new HashMap<>();
        for (int i = 0; i < groupwords.size(); i++) {
            final String groupword = groupwords.get(i);
            if (groupword.isEmpty()) {
                continue;
            }
            if (groupword.startsWith("'")) {
                text = groupword.substring(1, groupword.length() - 1);
            } else if (groupword.startsWith("=>")) {
                onClickAction = groupword.substring(2);
            } else {
                int equalIndex = groupword.indexOf("=");
                if (equalIndex > -1) {
                    String key = groupword.substring(0, equalIndex);
                    String value = groupword.substring(equalIndex + 1);
                    if (value.isEmpty()) {
                        i++;
                        String val = groupwords.get(i);
                        kv.put(key, val.substring(1, val.length() - 1));
                    } else {
                        kv.put(key, value);
                    }
                } else {
                    System.out.println("Can't process word " + groupword);
                }

            }
        }
        return new HTag(tagName, kv, text, onClickAction, magicContext);
    }

    private static HTag updateTag(HTag currentTag, String line, Map<String, String> magicContext) {
        final int firstSpaceIndex = line.indexOf(" ");
        /*if (firstSpaceIndex == -1) {
            return new HTag(line.trim().replaceAll("\\t", ""));
        }*/
        final String tagName = currentTag.name();

        final String remainingLine = line.trim();
        List<String> groupwords = cutAttributes(remainingLine);
        String text = "";
        String onClickAction = "";
        Map<String, String> kv = new HashMap<>(currentTag.keys());
        for (int i = 0; i < groupwords.size(); i++) {
            final String groupword = groupwords.get(i);
            if (groupword.isEmpty()) {
                continue;
            }
            if (groupword.startsWith("'")) {
                text = groupword.substring(1, groupword.length() - 1);
            } else if (groupword.startsWith("=>")) {
                onClickAction = groupword.substring(2);
            } else {
                int equalIndex = groupword.indexOf("=");
                if (equalIndex > -1) {
                    String key = groupword.substring(0, equalIndex);
                    String value = groupword.substring(equalIndex + 1);
                    if (value.isEmpty()) {
                        i++;
                        String val = groupwords.get(i);
                        kv.put(key, val.substring(1, val.length() - 1));
                    } else {
                        kv.put(key, value);
                    }
                } else {
                    System.out.println("Can't process word " + groupword);
                }

            }
        }
        return new HTag(tagName, kv, text, onClickAction, magicContext);
    }

    private static List<String> cutAttributes(String properLine) {
        if (properLine.isEmpty()) {
            return List.of();
        }
        int i = properLine.indexOf("'");
        if (i == -1) {
            return Arrays.asList(properLine.trim().split(" "));
        }
        List<String> words = new ArrayList<>();
        if (i > 0) {
            String beforeText = properLine.substring(0, i);
            String[] split = beforeText.split(" ");
            words.addAll(Arrays.asList(split));
        }
        int endIndexMessage = properLine.indexOf("'", i + 1);
        String message = properLine.substring(i, endIndexMessage + 1);// string paramter bien coupe
        words.add(message);
        final List<String> subListWords = cutAttributes(properLine.substring(endIndexMessage + 1));
        words.addAll(subListWords);
        return words;
    }

    private static int countIndentation(String cleanLine) {
        int i = 0;
        // Should always be 0 but when running mock values it includes unwanted \n
        int lineBreakCount = 0;
        while (cleanLine.charAt(i) == ' ' || cleanLine.charAt(i) == '\n') {
            if (cleanLine.charAt(i) == '\n') {
                lineBreakCount++;
            }
            i++;
            if (i == cleanLine.length()) break;
        }
        return i - lineBreakCount;
    }

    private static StringBuilder indent(int tabCount, boolean asSpaces) {
        final StringBuilder builder = new StringBuilder();
        if (asSpaces) {
            builder.append("    ".repeat(Math.max(0, tabCount)));
        } else {
            builder.append("\t".repeat(Math.max(0, tabCount)));
        }
        return builder;
    }

}