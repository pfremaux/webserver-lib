import java.io.IOException;
import java.util.*;

public class JsToHtmlCreator {


    private final static Set<String> dynamiList = Set.of("button");
    private final static String input = """
installWelcome:param1
h3 'Hello save text now'
ul
    li
        input type='text' id='idTextToSave' value=param1
    li
        button =>saveText() innerHTML='Save'
h2 'Ok again'
            """;

    public static void main(String[] args) throws IOException {
        final String f = "file.txt";
        //final List<String> lines = Files.readAllLines(Paths.get(f));
        final List<String> lines = Arrays.asList(input.split("\n"));
        String outputFile = "out.js";
        StringBuilder outputSource = new StringBuilder();
        int innerChild = 0;
        String sourceIndentation = "    ";
        boolean waitOptionally = false;

        String methodName = null; // TODO PFR ne reset que si double ligne vide
        // All declaration bellow need to be reset for each lines
        final Set<String> parameters = new HashSet<>();
        int previousIndendationCount = 0;
        boolean inDynamicTag = false;
        final Map<String, String> attributesToSetup = new HashMap<>();
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

            } else {
                String properLine = line.replace("\t", "    ");
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
                String[] words = properLine.trim().split(" ");
                String tagName = words[0];

                if (dynamiList.contains(tagName)) {
                    if (innerChild == 0) {
                        outputSource.append(")\n");
                        outputSource.append(sourceIndentation);
                        outputSource.append(",");
                    }
                    outputSource.append("dt('");
                    outputSource.append(tagName);
                    outputSource.append("'");
                    inDynamicTag = true;
                } else {
                    if (innerChild == 0) {
                        outputSource.append(")\n");
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
                        attributesToSetup.put("tag." + attrib, val);
                    }
                    if (word.startsWith("'")) {
                        textBuffer.append(", ").append(word);
                    } else {
                        System.out.println("impossible to process " + line);
                    }
                }
                if (waitOptionally) {
                    outputSource.append(")");
                }
                finalizeDeclaration(outputSource, attributesToSetup);

                previousIndendationCount = currentIndentation;

                parameters.clear();
                inDynamicTag = false;
                waitOptionally = false;
                attributesToSetup.clear();

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
        System.out.println(outputSource.toString());
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
