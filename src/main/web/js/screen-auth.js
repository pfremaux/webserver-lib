

function showAuth(container, onSuccess){
    removeAllChildren("containerId");
    let labelName = new Element('label')
            .withText('Name:')
            .forId('idName')
            .get();
    container.appendChild(labelName);
    let name = new Element('input')
            .withId('idName')
            .get();
    container.appendChild(name);
    let labelPwd = new Element('label')
            .withText('Password:')
            .forId('idPwd')
            .get();
    container.appendChild(labelPwd);
    let password = new Element('input')
            .withId('idPwd')
            .get();
    password.type = 'password';
    container.appendChild(password);

    let button = new Element('button')
            .withText('Authenticate')
            .onClick(e => {
                let name = id('idName').value;
                let pwd = id('idPwd').value;
                // The method bellow is generated automatically by the server. The method is declared in lib.js
                auth(name, pwd, obj => {
                    id('output').innerHTML = obj;
                    onSuccess();
                });
            }).get();
    container.appendChild(button);

    simpleFormRow("IdTruc", "Un truc a dire : ", "text").insertIn(container);
}