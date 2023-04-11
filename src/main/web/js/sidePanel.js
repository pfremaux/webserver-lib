function installSideMenu() {
	let panelMainGlobalDiv = el('div');
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
	return panelMainDiv;
}