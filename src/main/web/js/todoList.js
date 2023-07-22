function installTodoList(panelMainDiv) {
    append(
        panelMainDiv,
        st('h3', 'Todo list'),
        dt('div', 'listId')
    );
    loadAndFillTodos('listId');

}

function loadAndFillTodos(tagId) {
    loadTodos(response => {
        let container = id(tagId);
        response = JSON.parse(response);
        // "todoList":[{"text":"Already finished","creationTimestamp":1684420891959},{"text":"Finish this","creationTimestamp":1684420901959}]}
        if (response.todoList && response.todoList.length > 0) {
            // container.innerHTML = response;
            let ul = el("ul");
            append(
                container,
                ul);
            for (let i = 0 ; i < response.todoList.length ; i++) {
                append(
                    ul,
                    st("li", response.todoList[i].text),
                )

            }
        }
    });
}

function append(...args) {
    let parent = args[0];
    for (let i = 1 ; i < args.length ; i++) {
        parent.appendChild(args[i]);
    }
}

function li(container, ) {

}

/** Stands for Simple Tag
*/
function st(type, text) {
    let t = el(type);
    t.innerHTML = text;
    return t;
}

/* Stands for Dynamic Tag */
function dt(type, id, onClick) {
    let tag = el(type);
    tag.id = id;
    if (onClick) {
        tag.onclick = onClick;
    }
    return tag;
}