# webserver-lib
## Description
The goal of this project is to provide a light java webserver, with an easy way to create new endpoints.
This server is light because it's mostly using what the JDK already provides, and the way to create endpoint is like what some other framework are proposing: annotations.
I made this project because of some projects do at home and I wanted to have a "toolbox" with what I commonly need.

## Security and privacy
It's quite trivial but largely enough for a project at home: 
- this server supports HTTPS
- authentications with login/password associated to a list of roles.

## Installation
### From sources
The jar is not stored in repositories. So you'll need to manually paste the jar in a valid dependency path.
- checkout the right branch (each version should have a branch).
- run `build.bat` or `build.sh`.
- you should get `weberserver-lib.jar` and `server-config.properties`.
- put the jar in your project's libs directory and server-config.properties in your resources directory.
- read the documenation.

## Examples
### Declare a new endpoint
Create a new class and type:
```
@Endpoint(path = "/endpoint/path", method = "GET")
@MdDoc(description = "Description of what the endpoint does")
@Role(value = "requiredRole")
public Response entryPointMethod(Map<String, List<String>> headers, Body body) {
    return new Response("Ok !");
}
```

To start the server use this code:
```
public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException,
NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler(
                AuthenticationHandler.MOCKED_AUTH,
                AuthenticationHandler.MOCKED_PASSWORD_ENCRYPTOR
        );
        // TODO replace in Set.of() any class you declared with endpoints
        ServerHandler.runServer(args, Set.of("webserver.example.MyEndpoints", "classpath.to.class.with.endpoint.annotation"), authenticationHandler);
}
```

Accounts can be set this way:
```
// TODO 
```

## Other functionalities
### Javascript library
If you're using raw Javascript, the server generates Javascript code to call endpoints you created with this library.
For example, with the following key:
```
server.generate.js.lib.endpoint=/lib.js
```
You'd have to do this call:
```
https://127.0.0.1:8081/lib.js
```

### Self describe



### Video streaming
In server-config.properties you can use the following keys:
- log.level: can be FINE, INFO, WARN, ERROR 
- log.file: if not set, logs will be written to the console

### Expose static file


### Logs

