package webserver.generators;


public class JsGenerator {
    private JsGenerator() {

    }

    public static String asyncCallSource() {
        // First declare ACCOUNT. This object will contain account information when the user authenticated.
        // Below, there is the function that will do a async call.
        // All generated methods (for each endpoints) will call this function.
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
        // Authentication call...
        return """
                function auth(login, pass, fn) {
                	_auth(login, pass, strObj => {
                		let obj = JSON.parse(strObj);
                		ACCOUNT = {
                			token: obj.token,
                			roles: obj.roles
                		};
                		return fn(obj);
                	});
                }
                				
                function hasRole(strRole) {
                	if (!ACCOUNT) {
                		return false;
                	}
                	if (!ACCOUNT.roles) {
                		return false;
                	}
                	for (let i in ACCOUNT.roles) {
                		if (ACCOUNT.roles[i] == strRole) {
                			return true;
                		}
                	}
                	return false;
                }
                				
                function logout(redirect) {
                	ACCOUNT = undefined;
                	redirect();
                }
                """;
    }

    /**
     * Generate a function based on an endpoint metadata a programmers created outside of this library.
     *
     * @param info
     * @return
     */
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

    /**
     * Generates a chunk of json object, in Javascript.
     * TODO PFR clarifier le contenu de ce qui est généré
     * Takes all parameters registered in the endpoint metadata, and groups them in a json object.
     *
     * @param builder
     * @param doc
     */
    private static void groupInputFieldToObjects(StringBuilder builder, DocumentedEndpoint doc) {
        doc.getParameters().forEach((key, value) ->
                builder.append("\"")
                        .append(key)
                        .append("\":")
                        .append(key)
                        .append(","));
        if (!doc.getParameters().isEmpty()) {

            builder.deleteCharAt(builder.length() - 1);
        }
    }

}
