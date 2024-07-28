
function buildTagComponent(containerIdMustExist, tagContainerId, inputTextId, tagNameAccumulator) {
        const mainContainer = document.getElementById(containerIdMustExist);
        mainContainer.classList.add("mainContainer");
        const tagContainer = document.createElement("span");
        tagContainer.id = tagContainerId;
        mainContainer.appendChild(tagContainer);
        const inputText = document.createElement("input");
        inputText.id = inputTextId;
        inputText.classList.add("tagInputText");
        inputText.placeholder="input here..";
        mainContainer.appendChild(inputText);


        inputText.onkeypress = (evt) => {
        if (event.which == 13) {
            event.preventDefault();
        } else if (event.which == 32) {
            event.preventDefault();
            let tagContainer = document.getElementById(tagContainerId);
            let span = document.createElement("span");
            span.classList.add("tag");
            span.innerHTML = inputText.value;
            tagNameAccumulator(inputText.value);
            let deleteTag = document.createElement("a");
            deleteTag.href = "#";
            deleteTag.onclick = (e) => tagContainer.removeChild(span);
            deleteTag.innerHTML = "X";
            span.appendChild(deleteTag);
            tagContainer.appendChild(span);
            inputText.value = "";
        }
    }
}