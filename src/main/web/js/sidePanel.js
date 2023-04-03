function installSideMenu() {
	let panelMainGlobalDiv = el('div');
	let panelMainDiv = el('div');
	panelMainGlobalDiv.appendChild(panelMainDiv);
	panelMainGlobalDiv.className = 'panel-right';
	panelMainDiv.className = 'panel-form';
	let changesideButton = el('button');
	changesideButton.innerHTML = '<-->';
	changesideButton.className = 'button-right';
	changesideButton.onclick = e => { 
		panelMainGlobalDiv.className = panelMainGlobalDiv.className === 'panel-right' ? 'panel-left' : 'panel-right';
	}
	panelMainDiv.appendChild(changesideButton);
	let br = el('br');
	panelMainDiv.appendChild(br);
	
	let hideButton = el('button');
	hideButton.innerHTML = "hide/show";
	hideButton.className = 'hide-btn';
	hideButton.onclick = e => toggle(panelMainGlobalDiv);
	
	document.body.appendChild(panelMainGlobalDiv);
	document.body.appendChild(hideButton);
	return panelMainDiv;
}