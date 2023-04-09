package webserver.generators;


public class JsGenerator {
	private JsGenerator() {

	}

	public static String asyncCallSource() {
		return """
				let ACCOUNT = undefined;
				
				function asyncCall(method, path, data, fn) {
					let xhttp = new XMLHttpRequest();
					
					xhttp.onreadystatechange = function() {
						if (this.readyState == 4 && this.status == 200) {
							fn(this.responseText);
							
						}
					};
					xhttp.open(method, path, true);
					//xhttp.setRequestHeader(header, value);
					xhttp.setRequestHeader("Content-type", "text/json");
					xhttp.send(JSON.stringify(data));
				}
				""";
	}

	public static String authSource() {// TODO PFR bien commenter pour dire que ce code est dependant du code genere
		return """
				function auth(login, pass, fn) {
					_auth(login, pass, obj => {
						ACCOUNT = {
							token: obj.token
						};
						return fn(obj);
					});
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
		builder.append("\", '");
		builder.append(info.getPath());
		builder.append("', ");
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
