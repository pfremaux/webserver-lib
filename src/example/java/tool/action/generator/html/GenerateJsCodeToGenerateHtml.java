package tool.action.generator.html;

import tool.utils.Assert;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class GenerateJsCodeToGenerateHtml {

    private final List<Path> inputScripts;
    private final List<Path> outputJavascript;


    public GenerateJsCodeToGenerateHtml(String inputScripts, String outputJavascript) {
        this(List.of(inputScripts), List.of(outputJavascript));
    }

    public GenerateJsCodeToGenerateHtml(List<String> inputScripts, List<String> outputJavascript) {
        Assert.requireNotEmpty(inputScripts, "You must provide at least 1 input script path.");
        Assert.requireNotEmpty(outputJavascript, "You must provide at least 1 output script path.");
        this.inputScripts = inputScripts.stream().filter(Objects::nonNull).map(Path::of).toList();
        this.outputJavascript = outputJavascript.stream().filter(Objects::nonNull).map(Path::of).toList();
        Assert.requireNotEmpty(this.inputScripts, "You must provide at least 1 input script path, NOT NULL. inputScripts=" + inputScripts);
        Assert.requireNotEmpty(this.outputJavascript, "You must provide at least 1 output script path, NOT NULL. outputJavascript=" + outputJavascript);
        Assert.requireEqual(this.inputScripts.size(), this.outputJavascript.size(), "inputScripts and outputJavascript should have the same quantity of elements. inputScripts.size() = " + this.inputScripts.size() + " ;  outputJavascript.size() = " + this.outputJavascript.size());
        this.inputScripts.forEach(path -> Assert.requireExists(path, "Path not found. Path=" + path));
        this.outputJavascript.forEach(path -> Assert.requireNotExists(path, "Output path shouldn't already exist. Path=" + path));
    }

    public void run() {
        for (int i = 0; i < inputScripts.size(); i++) {
            final Path input = inputScripts.get(i);
            final Path output = outputJavascript.get(i);
            convertOrFail(input, output);
        }
    }

    private static void convertOrFail(Path input, Path output) {
        try {
            JsToHtmlCreator.convertStringScriptToJs(input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
