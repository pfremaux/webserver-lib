function installSideMenu(panelMainGlobalDiv, bodyId, menu) {
	let panelMainDiv = el('div');
	panelMainGlobalDiv.appendChild(panelMainDiv);
	panelMainGlobalDiv.className = 'sp-panel-right';
	panelMainDiv.className = 'sp-panel-form';
	let changesideButton = el('button');
	changesideButton.innerHTML = '<-->';
	changesideButton.className = 'sp-button-right';
	changesideButton.onclick = e => { 
		panelMainGlobalDiv.className = panelMainGlobalDiv.className === 'sp-panel-right' ? 'sp-panel-left' : 'sp-panel-right';
	}
	panelMainDiv.appendChild(changesideButton);
	let br = el('br');
	panelMainDiv.appendChild(br);
	
	let hideButton = el('button');
	hideButton.innerHTML = "hide/show";
	hideButton.className = 'sp-hide-btn';
	hideButton.onclick = e => toggle(panelMainGlobalDiv);
	
	document.body.appendChild(panelMainGlobalDiv);
	document.body.appendChild(hideButton);
	let body = el('div');
	body.id = bodyId;
	// TODO PFR better approach
	body.innerHTML = 'empty body';
	body.style.width = '100%';
	body.style.height = '100%';
	document.body.appendChild(body);


	let menuDiv = el('div');
	for (let indexMenu in menu) {
	    let option = el('a');
	    option.href = '#';
	    option.onclick = e => menu[indexMenu].action();
	    option.innerHTML = menu[indexMenu].label;
	    menuDiv.appendChild(option);
	    menuDiv.appendChild(el('br'));
	}
	panelMainDiv.appendChild(menuDiv);
	return panelMainDiv;
}