package webserver.toolstmp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsToHtmlCreator {


    private final static Set<String> dynamiList = Set.of("button");
    private final static String input = """
installWelcome:param1
h3 'Hello save text now'
ul
    li
        input type='text' id='idTextToSave'
    li
        button =>saveText() innerHTML='Save'
h2 'Ok again'
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
        String[] split1 = input.split("\\\\n");
        final List<String> lines = Arrays.asList(split1);
        return convertLinesScriptToJs(lines);
    }
    private static StringBuilder convertLinesScriptToJs(List<String> lines) {
        final StringBuilder outputSource = new StringBuilder();
        int innerChild = 0;
        String sourceIndentation = "    ";
        boolean waitOptionally = false;

        String methodName = null; // TODO PFR ne reset que si double ligne vide
        // All declaration bellow need to be reset for each lines
        final Set<String> parameters = new HashSet<>();
        int previousIndendationCount = 0;
        boolean inDynamicTag = false;
        final Map<String, String> attributesToSetup = new HashMap<>();
        boolean firstInstruction = false;
        for (final String line : lines) {
            if (methodName == null) {
                String[] split = line.trim().split(":");
                methodName = split[0];
                if (split[1] != null && split[1].trim().length() > 0) {
                    String[] parametersSplit = split[1].split(",");
                    parameters.addAll(Arrays.asList(parametersSplit));
                }
                outputSource.append("\n").append(sourceIndentation).append("function ");
                outputSource.append(methodName);
                outputSource.append("(containerId,");
                for (String parameter : parameters) {
                    outputSource.append(parameter);
                    outputSource.append(",");
                }
                outputSource.deleteCharAt(outputSource.length() - 1);
                sourceIndentation += "    ";
                outputSource.append(") {\n").append(sourceIndentation);
                outputSource.append("\tconst container = document.getElementById(containerId);\n")
                        .append(sourceIndentation).append("return appendTo(container");
                firstInstruction = true;
                //innerChild++;
            } else {
                String properLine = line.replace("\t", "    ").replace("\\t", "    ");
                final int currentIndentation = countIndentation(properLine);
                if (currentIndentation < previousIndendationCount) {
                    // TODO PFR calculer le nombre de retour en arriere
                    int indentationGroupCount = previousIndendationCount - currentIndentation;
                    outputSource.append(".tag\n").append(sourceIndentation);
                    outputSource.append(")".repeat(Math.max(0, indentationGroupCount / 4)));

                    innerChild -= indentationGroupCount / 4;
                    sourceIndentation = sourceIndentation.substring(0, sourceIndentation.length() - indentationGroupCount);
                    if (innerChild > 0) {
                        outputSource.append(")\n");
                        outputSource.append(sourceIndentation);
                        outputSource.append(".child(");
                    }
                } else if (currentIndentation > previousIndendationCount) {
                    outputSource.append("\n");
                    sourceIndentation += "    ";
                    outputSource.append(sourceIndentation);
                    outputSource.append(".child(");
                    innerChild++;
                } else {
                    outputSource.append("\n").append(sourceIndentation);
                }
                String[] words = cutAttributes(properLine.trim()).toArray(new String[0]);
                String tagName = words[0];

                if (dynamiList.contains(tagName)) {
                    if (innerChild == 0) {
                        //outputSource.append(")");// TODO PFR recement commnenté a rme
                        //outputSource.append("\n");
                        outputSource.append(sourceIndentation);
                        outputSource.append(",");
                    }
                    outputSource.append("dt('");
                    outputSource.append(tagName);
                    outputSource.append("'");
                    inDynamicTag = true;
                } else {
                    if (innerChild == 0) {
                        //outputSource.append(")");// TODO PFR recement commnenté a rme
                        //outputSource.append("\n");
                        outputSource.append(sourceIndentation);
                        outputSource.append(",");
                    }
                    outputSource.append("st('");
                    outputSource.append(tagName);
                    outputSource.append("'");
                    waitOptionally = true;
                }

                final StringBuilder textBuffer = new StringBuilder();
                for (int i = 1; i < words.length; i++) {
                    final String word = words[i];
                    if (textBuffer.length() > 0) {
                        textBuffer.append(" ").append(word);
                        if (word.endsWith("'")) {
                            outputSource.append(textBuffer);
                            textBuffer.delete(0, textBuffer.length());
                        }
                    } else if (word.startsWith("=>")) {
                        outputSource.append(",");
                        outputSource.append("evt => ").append(word.substring(2));
                        outputSource.append(")");
                    } else if (word.contains("=")) {
                        String attrib = word.substring(0, word.indexOf('='));
                        String val = word.substring(word.indexOf('=') + 1);
                        if (val.isEmpty()) {
                            i++;
                            val = words[i];
                            attributesToSetup.put("tag." + attrib, val);
                        }
                        attributesToSetup.put("tag." + attrib, val);
                    }
                    if (word.startsWith("'")) {
                        textBuffer.append(", ").append(word);
                    } else {
                        System.out.println("impossible to process " + line);
                    }
                }
                if (waitOptionally) {
                    if (textBuffer.length() > 0) {
                        outputSource.append(textBuffer);
                    }
                    outputSource.append(")");
                }
                finalizeDeclaration(outputSource, attributesToSetup);

                previousIndendationCount = currentIndentation;

                parameters.clear();
                inDynamicTag = false;
                waitOptionally = false;
                attributesToSetup.clear();

                firstInstruction = false;
            }
        }
        outputSource.append(".tag");
        while (innerChild >= 0) {
            outputSource.append(")");
            innerChild--;
        }
        outputSource.append(";\n");
        sourceIndentation = sourceIndentation.substring(0, sourceIndentation.length()-4);
        outputSource.append(sourceIndentation).append("}");
        return outputSource;
    }

    @Deprecated
    private static String[] cutAttributesLegacy(String properLine) {
        return properLine.trim().split(" ");
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
        String message = properLine.substring(i, endIndexMessage+1);
        words.add(message);
        final List<String> subListWords = cutAttributes(properLine.substring(endIndexMessage+1));
        words.addAll(subListWords);
        return words;
    }

    private static void finalizeDeclaration(StringBuilder outputSource, Map<String, String> attributesToSetup) {
        if (attributesToSetup.isEmpty()) {
            return;
        }
        outputSource.append(".andDo(tag => {");
        for (Map.Entry<String, String> entry : attributesToSetup.entrySet()) {
            outputSource.append(entry.getKey());
            outputSource.append("=");
            outputSource.append(entry.getValue());
            outputSource.append("; ");
        }
        outputSource.append("})");
    }

    private static int countIndentation(String cleanLine) {
        int i = 0;
        while (cleanLine.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

}