# webserver-lib
## Description
The goal of this project is to provide a light java web server, with an easy way to create new endpoints.
This server is light because it's mostly using what the JDK already provides, and the way to create endpoint is like what some other framework are proposing: annotations.
I made this server because of some projects I'm creating at home and I wanted to have a "toolbox" with what I commonly need.
I found the design principle KISS (Keep It Simple, Stupid) was a good approach here.

## Security and privacy
It's quite trivial but largely enough for a project at home: 
- this server supports HTTPS
- authentications with login/password associated to a list of roles. Passwords are hashed in SHA-256 and stored in a properties file.

## Installation
### From sources
The jar is not stored in a repository (this project doesn't require any dependency, just a JDK >= 17). 
So you'll need to manually paste the jar in a valid dependency path.
- checkout the right branch (each version should have a branch).
- run `build.bat` or `build.sh`.
- you should get `weberserver-lib.jar` and `server-config.properties` (might be worth spending time on reading this file).
- put the jar in your project's libs directory and server-config.properties in your resources directory.
- read the documentation for more information.


## Examples
### Declare a new endpoint
Create your Java project. Put `weberserver-lib.jar` in its classpath and `server-config.properties` in its resources path.
Create a new class and type:
```
@Endpoint(path = "/endpoint/path", method = "GET")
// A description that will be used to describe the endpoint (see self-describe)
@MdDoc(description = "Description of what the endpoint does")
// It's not mandatory but if you're using accounts, you'd better to define roles 
// If you're just beginning ignore this part for now.
// @Role(value = "requiredRole")
public Response entryPointMethod(Map<String, List<String>> headers, Body body) {
    // It's important to always declare headers. The webserver-lib ALWAYS expects it.
    return new Response("Ok !");
}
```

```
// Records are not supported yet.
public class Response {
    // This annotation is required. You must put it on ALL attributes. The library doesn't allow attributes that are not part of the response.
    @JsonField
    private final String data;
    public Response(String data) {
        this.data = data;
    }
    
    // Good old getter style is important
    public String getData() {
        return this.data;
    }
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
        ServerHandler.runServer(args, authenticationHandler, baseUrl -> new String[]{
                "Welcome!",
                baseUrl + "/self-describe"
        });
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
Assuming you enabled HTTPS, you'd have to do this call:
```
https://127.0.0.1:8081/lib.js
```

### Self describe
The server returns the list of endpoints available for the client.
- server.handler.self.describe.endpoint: /self-describe
The server will return HTML page with all endpoints it knows. You should have body request and response. There should be a Javascript command you can copy
to implement your call on client side.


### Expose static file
Simply expose files.
- server.handler.static.files.endpoint.relative.path: relative path the client has to use to call this service (For example: if you set /statics, the client will have to call https://<ip>)
- server.handler.static.files.base.directory: Absolute or relative local path of the files you want to expose (For example: ./src/main/web)
- server.handler.static.files.exploration.enabled: if set to true, allows the caller to explore files by putting a start * at the end of the URL (For example: /statics/*)

### Video streaming
This functionality relies on the functionality above: "Expose static file". The path server.handler.static.files.base.directory
will be used to retrieve the requested video.
server.handler.video.files.base.directory: Usually should be equal to server.handler.static.files.base.directory unless you're handling video path differently

### Logs
In server-config.properties you can use the following keys:
- log.level: can be FINE, INFO, WARN, ERROR
- log.file: if not set, logs will be written to the console


It's always hard to determine when an app is finished (it's usually never perfect, we always want to do more improvements).
As I have other projects, I have to define a limit to that.
My current limit is:
- be able to check out, build, initialize HTTPS, run easily (thanks to bash script),
- be able to authenticate to an account with a specific role,
- configure accounts in command line in an acceptable secured way for personal use.