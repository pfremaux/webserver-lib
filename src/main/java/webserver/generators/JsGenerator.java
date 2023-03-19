package webserver.generators;


public class JsGenerator {
	private JsGenerator() {

	}

	public static String asyncCallSource() {
		return """
				let ACCOUNT = undefined;
				
				function asyncCall(method, data, fn) {
					let xhttp = new XMLHttpRequest();
					//xhttp.setRequestHeader(header, value);
					xhttp.onreadystatechange = function() {
						if (this.readyState == 4 && this.status == 200) {
							fn(this.responseText);
							xhttp.open(method, path, true);
						}
					};
					xhttp.send();
				}
				""";
	}

	public static StringBuilder generateJsCall(DocumentedEndpoint info) {
		final StringBuilder builder = new StringBuilder();
		final String methodName = info.getJavaMethodName();
		builder.append("// Calls ");
		builder.append(info.getHttpMethod());
		builder.append(" ");
		builder.append(info.getPath());
		builder.append("\nfunction ");
		builder.append(methodName);
		builder.append("(");
		info.getParameters().keySet().forEach(p -> builder.append(p).append(", "));
		builder.append("callBack");
		builder.append(") {\n");
		builder.append("\tasyncCall(\"");
		builder.append(info.getHttpMethod());
		builder.append("\", ");
		builder.append("{");
		groupInputFieldToObjects(builder, info);
		builder.append("}");

		builder.append(", callBack");
		builder.append(");\n");
		builder.append("}\n\n");

		return builder;
	}

	private static void groupInputFieldToObjects(StringBuilder builder, DocumentedEndpoint doc) {
		doc.getParameters().forEach((key, value) ->
				builder.append("\"")
				.append(key)
				.append("\":")
				.append(key)
				.append(","));
		if (doc.getParameters().isEmpty()) {
			builder.append("{}");
		} else {
			builder.deleteCharAt(builder.length() - 1);
		}
	}
}
