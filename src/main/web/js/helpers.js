function id(i) {
	return document.getElementById(i);
}

function el(name) {
	return document.createElement(name);
}

function removeAllChildren(i) {
	const myNode = document.getElementById(i);
	while (myNode.lastElementChild) {
		myNode.removeChild(myNode.lastElementChild);
	}
}

window.onerror = function(a,b,c) {
	console.log("a="+a+", b="+b+", c="+c);
	// TODO Send logs to server here.
};

function log(s) {
	console.log(s);
}

function logj(s) {
	console.log(JSON.stringify(s));
}

function hide(o) {
	if (typeof o === "string") {
		log("cache "+o);
		id(o).style.display = "none";
	} else {
	    o.style.display = "none";
	}
}

function show(o) {
	if (typeof o === "string") {
		id(o).style.display = "inline";
	} else {
	    o.style.display = "inline";
	}
}

function toggle(o) {
	if (typeof o === "string") {
		if (id(o).style.display === "inline") {
			id(o).style.display = "none";
		} else {
			id(o).style.display = "inline";
		}
	}
	if (o.style.display !== "none") {
		o.style.display = "none";
	} else {
		o.style.display = "inline";
	}
}


function appendTo(...args) {
    let parent = args[0];
    for (let i = 1 ; i < args.length ; i++) {
        if (args[i].tag) {
            parent.appendChild(args[i].tag);
        } else {
            parent.appendChild(args[i]);
        }
    }
    return parent;
}

function h3(text) {
    return st('h3', text);
}


function h4(text) {
    return st('h4', text);
}

function ul() {
    return st('ul');
}

function li(text) {
    return st('li', text);
}


function div(text) {
    return st('div', text);
}

/** Stands for Simple Tag
*/
function st(type, text) {
    let t = new Tag(type)
            .andDo(tag => tag.innerHTML = text? text:'');
    return t;
}

/* Stands for Dynamic Tag */
function dt(type, id, onClick) {
    let tag = new Tag(type).
        andDo(e => {
            e.id = id;
            e.onclick = onClick? onClick : e => {};
        });

    return tag;
}

function tagOf(htmlTag) {
    let tag = new Tag(htmlTag.name);
    tag.tag = htmlTag;
    return tag;
}

function Tag(name) {
    this.tag = el(name);
    this.child = function(tag) {
        if (tag.tag) {
            this.tag.appendChild(tag.tag);
        } else {
            console.log(typeof tag + ' = ' + JSON.stringify(tag));
            this.tag.appendChild(tag);
        }
        return this;
    }
    this.text = function(txt) {
        this.tag.innerHTML = text;
        return this;
    }
    this.attr = function(k,v) {
        this.tag[k] = v;
        return this;
    }
    this.andDo = function(lambda) {
        lambda(this.tag);
        return this;
    }
    this.set = function(obj) {
        for(let k in obj) {
            console.log('key = '+k);
            this.tag[k] = obj[k];
        }
        return this;
    }
    this.get = function() {
        return this.tag;
    }
}

const PAGE_SIZE = 5;
let GLOBAL = {
	tablesPages:{},
	tables:{},
	boxTypes:{},
	counter:{
		value:0,
		reset:function() {
			GLOBAL.counter.value = 0;
		},
		incAndGet:function() {
			return ++GLOBAL.counter.value;
		}
	}
};

function ayncCall(method, url, data, callback) {
  const http = new XMLHttpRequest();

  http.onreadystatechange = function()
  {
  	if (this.readyState == 4 && this.status == 200)
  	{
  		//Use parse() method to convert JSON string to JSON object
  		var responseJsonObj = JSON.parse(this.responseText);
        callback(responseJsonObj);
  		//use response
  	}
  };

  //http.onload = callback;
  http.open(method, url);
  http.setRequestHeader("Content-type", "text/json");
  http.send(JSON.stringify(data));
}