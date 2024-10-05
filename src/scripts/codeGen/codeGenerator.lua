------------------------------------------
-- PATTERN CONTENT
------------------------------------------
MAIN_RUNNER = [[

import webserver.generators.DocumentedEndpoint;
import webserver.handlers.ConfigHandler;
import webserver.handlers.web.auth.AuthenticationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static webserver.handlers.ServerHandler.runServer;

public class $$0 {

    public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ConfigHandler.loadConfigFile("server-config.properties");
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler(AuthenticationHandler.MOCKED_AUTH, AuthenticationHandler.MOCKED_PASSWORD_ENCRYPTOR);
        runServer(args, authenticationHandler, $$0::customExtension, $$0::customWelcomeLogs);
    }

    private static void customExtension(DocumentedEndpoint documentedEndpoint) {
    }

     private static String[] customWelcomeLogs(String baseURL) {
        return new String[] {};
     }

}

]]

JAVA_CLASS_PATTERN = [[
package $$package;

$$imports

public class $$className {
    $$classBody
}
]]

JAVA_METHOD_ENDPOINT_PATTERN = [[
    @Endpoint(path = "$$relativeWebPath", method = "$$httpMethod")
    public $$responseClassName $$javaMethodName(Map<String, List<String>> headers, $$bodyClassName body) {
        return new $$responseClassName();
    }
]]

JAVA_JSON_ATTRIBUTE = [[
@JsonField
    private final $$attribType $$attribName;
]]

JAVA_CONSTRUCTOR = [[
    public $$className($$parameters) {
        $$body
    }
]]

JAVA_GETTER_PATTERN = [[
    public $$type get$$nameUpper() {
        return $$name;
    }
]]


------------------------------------------
-- UTILITY METHODS
------------------------------------------

function mapSize(mymap)
    n = 0
    for _ in pairs(mymap) do n=n+1 end
    return n
end

function isWindows()
    return not (os.getenv("windir") == nil)
end

function createPackageDir(path)
    print("CREATE DIR")
    if isWindows() then
        os.execute("mkdir "..string.gsub(path, "/", "\\"))
    else
        os.execute("mkdir -p "..path)
    end
    print("END OF CREATE DIR")
end

function firstToUpper(str)
    return (str:gsub("^%l", string.upper))
end


function insert(str1, str2, pos)
    return str1:sub(1,pos)..str2..str1:sub(pos+1)
end

------------------------------------------
-- CODE GENERATOR
------------------------------------------

function generate_main_class(packageLocation, className)
    local javaClass = string.gsub(MAIN_RUNNER, "$$0", className)
    local fileContent = "package ".. packageLocation .. ";\n" .. javaClass
    local filePath = "src/main/java/" .. string.gsub(packageLocation, "%.", "/")
    print(filePath)
    createPackageDir(filePath)
    local file = io.open(filePath .."/"..className..".java", "w+")
    io.output(file)
    io.write(fileContent)
    io.close(file)
end

function generate_endpoints_class(packageLocation, className, endpoints)
    local javaClass = string.gsub(JAVA_CLASS_PATTERN, "$$package", packageLocation)
    javaClass = string.gsub(javaClass, "$$className", className);
    classBody, classImports = processAllEndpoints(packageLocation, endpoints)
    javaClass = string.gsub(javaClass, "$$classBody", classBody);
    javaClass = string.gsub(javaClass, "$$imports", classImports);

    fileContent = javaClass

    local filePath = "src/main/java/" .. string.gsub(packageLocation, "%.", "/")
    createPackageDir(filePath)
    file = io.open(filePath.."/"..className..".java", "w+")
    io.output(file)
    io.write(fileContent)
    io.close(file)
end

function processAllEndpoints(packageEndpoint, endpoints)
    local classBody = ""
    local classImports = [[
import webserver.annotations.Endpoint;

import java.util.List;
import java.util.Map;
]]
    for i,endpoint in pairs(endpoints) do
        print("Creating endpoint: "..endpoints[i].methodName)
        method = string.gsub(JAVA_METHOD_ENDPOINT_PATTERN, "$$relativeWebPath", endpoints[i].path)
        method = string.gsub(method, "$$httpMethod", endpoints[i].httpMethod)

        if mapSize(endpoints[i].response.attributes) > 0 then
            method = string.gsub(method, "$$responseClassName", endpoints[i].response.name)
            classImports = "import ".. packageEndpoint ..".models."..endpoints[i].response.name ..";\n" .. classImports
            generate_model(packageEndpoint..".models", endpoints[i].response)
            -- TODO PFR handle attributes
        else
            method = string.gsub(method, "$$responseClassName", "EmptyBody")
            classImports = "import webserver.EmptyBody;\n" .. classImports
        end

        method = string.gsub(method, "$$javaMethodName", endpoints[i].methodName)
        if mapSize(endpoints[i].request.attributes) > 0 then
            method = string.gsub(method, "$$bodyClassName", endpoints[i].request.name)
            classImports = "import ".. packageEndpoint ..".models."..endpoints[i].request.name ..";\n" .. classImports
            generate_model(packageEndpoint..".models", endpoints[i].request)
        else
            method = string.gsub(method, "$$bodyClassName", "EmptyBody")
            classImports = "import webserver.EmptyBody;\n" .. classImports
        end

        classBody = classBody .. "\n" .. method
    end
    return classBody,classImports
end

function append_endpoints_to(packageLocation, className, endpoints)
    local filePath = "src/main/java/" .. string.gsub(packageLocation, "%.", "/")
    filePath = filePath.."/"..className..".java"
    file = io.open(filePath, "r")
    -- print("file = "..file)
    -- io.input(file)
    fileContent = file:read("*a")
    file:close()

    fileContent = string.sub(fileContent, 1, string.len(fileContent) - 2)
    classBody,classImports = processAllEndpoints(packageLocation , endpoints)
    fileContent = fileContent .. classBody
    fileContent = fileContent .. "\n}"

    index = string.find(fileContent, "\n")
    fileContent = insert(fileContent, classImports, index)

    print(filePath)



    file2,err = io.open(filePath, "w")


    file2:write(fileContent)
    file2:close()
end

function generate_model(packageLocation, data)
    local classBody = ""
    local classImports = "import webserver.annotations.JsonField;\n"
    local constructorParameters = ""
    local constructorBody = ""
    local getters = ""
    local fileContent = string.gsub(JAVA_CLASS_PATTERN, "$$package", packageLocation)
    fileContent = string.gsub(fileContent, "$$className", data.name)
    for i,attribute in pairs(data.attributes) do
        -- DECLARATION
        strAttrib = string.gsub(JAVA_JSON_ATTRIBUTE, "$$attribType", attribute.type)
        classImports = updateImportBlock(classImports, attribute.type, {})
        strAttrib = string.gsub(strAttrib, "$$attribName", attribute.name)
        classBody = classBody .. strAttrib .. "\n"
        -- CONSTRUCTOR PARAMETERS
        constructorParameters = constructorParameters .. attribute.type .. " " .. attribute.name.. ", "
        -- CONSTRUCTOR BODY
        constructorBody = constructorBody  .. "\t\t" .. "this.".. attribute.name .. " = " .. attribute.name..";".. "\n";
        -- GETTERS
        getter = string.gsub(JAVA_GETTER_PATTERN, "$$nameUpper", firstToUpper(attribute.name))
        getter = string.gsub(getter, "$$name", attribute.name)
        getter = string.gsub(getter, "$$type", attribute.type)
        getters = getters .. getter .. "\n"
    end
    if string.len(constructorParameters) > 0 then
        constructorParameters = string.sub(constructorParameters, 1, string.len(constructorParameters) - 2)
        constructorBody = string.sub(constructorBody, 1, string.len(constructorBody) - 1)
    end

    classBody = classBody .. string.gsub(JAVA_CONSTRUCTOR, "$$className", data.name)
    classBody = string.gsub(classBody, "$$parameters", constructorParameters)
    classBody = string.gsub(classBody, "$$body", constructorBody)
    classBody = classBody .. getters

    fileContent = string.gsub(fileContent, "$$classBody", classBody)
    fileContent = string.gsub(fileContent, "$$imports", classImports)
    filePath = "src/main/java/" .. string.gsub(packageLocation, "%.", "/")
    createPackageDir(filePath)
    file = io.open(filePath.."/"..data.name..".java", "w+")
    io.output(file)
    io.write(fileContent)
    io.close(file)
end

function updateImportBlock(importBlock, type, customRef)
    if type:find("^Map") then
        return importBlock .. "import java.util.Map;\n"
    elseif type:find("^List") then
        return importBlock .. "import java.util.List;\n"
    elseif customRef[type] ~= nil then
        return customRef[type] .. "\n" .. importBlock
    else
        return importBlock
    end
end



------------------------------------------
-- DECLARATION (should be replaced by interaction with user)
------------------------------------------
endpoints = {}
endpoints[0] = {}
endpoints[0].methodName = "getTodos"
endpoints[0].httpMethod = "GET"
endpoints[0].path = "/todos"
endpoints[0].request = {}
endpoints[0].request.name = "EmptyBody"
endpoints[0].request.attributes = {} -- no attributes
endpoints[0].response = {}
endpoints[0].response.name = "GetTodosResponse"
endpoints[0].response.attributes = {}
endpoints[0].response.attributes[0] = {}
endpoints[0].response.attributes[0].type = "List<String>"
endpoints[0].response.attributes[0].name = "todos"

endpoints_v2 = {}
endpoints_v2[0] = {}
endpoints_v2[0].methodName = "addTodo"
endpoints_v2[0].httpMethod = "POST"
endpoints_v2[0].path = "/todos"
endpoints_v2[0].request = {}
endpoints_v2[0].request.name = "AddTodoRequest"
endpoints_v2[0].request.attributes = {}
endpoints_v2[0].request.attributes[0] = {}
endpoints_v2[0].request.attributes[0].name = "text"
endpoints_v2[0].request.attributes[0].type = "String"
endpoints_v2[0].response = {}
endpoints_v2[0].response.name = "anyways"
endpoints_v2[0].response.attributes = {}

------------------------------------------
-- EXECUTION
------------------------------------------
-- generate_main_class("packamoi", "ClassAMoi")
generate_endpoints_class("packamoi.endpoints", "ClassEndpoints", endpoints)
append_endpoints_to("packamoi.endpoints", "ClassEndpoints", endpoints_v2)