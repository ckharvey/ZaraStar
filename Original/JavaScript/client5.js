// ============================================================================
// System: ZaraStar
// File: client5.js
// Author: C.K.Harvey
// Copyright (c) 2010-15 Christopher Harvey. All Rights Reserved.
// ============================================================================
var columnsCount = 1;
var colToUse = 1;
var touch = false;
var colSetSizeTO;

function colSetSize(col) {
    var c = parseInt(col) - 1;

    var d = document.getElementById('columndiv' + col);

    var x = 0,
        ele = d.getElementsByTagName('IMG');
    if (ele.length > 0) {
        var found = false;
        while (!found) {
            if (ele[x].id.startsWith('img'))
                found = true;
            else ++x;
        }

        colSetSizeTO = setTimeout("colSetSizeWait('" + col + "','" + ele[x].id + "')", 500);
    }
}

function colSetSizeWait(col, id) {
    var i = document.getElementById(id);

    if (i.naturalHeight > 0) {
        clearTimeout(colSetSizeTO);

        var d = document.getElementById('columndiv' + col);

        var ch = i.clientHeight + 40;
        var cw = i.clientWidth + 20;

        d.style.height = ch;
        d.style.width = cw;

        var t = (sHeight / 2) - (ch / 2);
        if (t < 0) t = 0;
        d.style.top = t;

        t = (sWidth / 2) - (cw / 2);
        if (t < 0) t = 0;
        d.style.left = t;
    } else colSetSizeTO = setTimeout("colSetSizeWait('" + col + "','" + id + "')", 500);
}

function insertCol(col, type, title, html, scripts) {
    try {
        touch = ('ontouchstart' in window);
        col = parseInt(col);
        title = deSanitise(title);

        if (type.charAt(0) === '-') {
            var d = document.getElementById("columnBar" + type.substring(1));
            if (d) {
                col = (d.parentNode.id).substring(9);
                d.id = '';
                colReplaceContents(col, type, title, html, scripts);
                return col;
            }
        }

        var barID = '';
        if (col === 888) {
            barID = type;
            type = 'G';
            col = 0;
        }

        if (col > 0) {
            colToUse = colCheckForLayout(col, type, columnsCount);
        } else
        if (col < 0) {
            col *= -1;
            if (!document.getElementById("columndiv" + col))
                colToUse = columnsCount;
            else {
                colReplaceContents(col, type, title, html, scripts);
                return col;
            }
        } else {
            colToUse = columnsCount;
        }

        insertColItself(colToUse, type, title, barID, html, scripts);

        columnsCount = parseInt(colToUse) + 1;

        return columnsCount - 1;
    } catch (Exception) {
        displayMessage("81: " + Exception.name);
    }
}

function colReplaceContents(col, type, title, html, scripts) {
    if (document.getElementById("columntitle" + col))
        document.getElementById("columntitle" + col).innerHTML = title;

    if (!scripts) scripts = '';

    var dx = document.getElementById("columnscripts" + col);
    dx.innerHTML = '';
    if (isIE)
        dx.innerHTML = sanitiseHTML(scripts);
    else dx.innerHTML = scripts;

    var colContents = document.getElementById('columncontent' + col);

    colContents.innerHTML = html;

    execScripts(dx);
}

function colAddToContents(tmpID, str) {
    var d = document.getElementById(tmpID);
    if (!d) return;

    var p = d.parentNode.parentNode;
    var p2 = p.parentNode;

    str = str.replace(/<x>/g, '\1').replace(/<y>/g, '\2');

    var x = 0,
        s, t, tr, td, first = true,
        z, a, v, slen, st, len = str.length;
    while (x < len) {
        tr = document.createElement('TR');
        while (x < len && str.charAt(x) !== '\2') {
            s = '';
            while (x < len && str.charAt(x) !== '\1')
                s += str.charAt(x++);
            ++x;

            z = 0;
            slen = s.length;
            st = '';
            while (z < slen) {
                a = '';
                while (z < slen && s.charAt(z) !== ':')
                    a += s.charAt(z++);
                while (z < slen && s.charAt(z) === ' ')
                    ++z;
                ++z;
                v = '';
                while (z < slen && s.charAt(z) !== ';')
                    v += s.charAt(z++);
                while (z < slen && s.charAt(z) === ' ')
                    ++z;
                ++z;
                st += "td.style." + a + "='" + v + "';";
            }

            t = '';
            while (x < len && str.charAt(x) !== '\1')
                t += str.charAt(x++);
            ++x;
            td = document.createElement('TD');
            td.innerHTML = t;
            eval(st);
            tr.appendChild(td);
        }
        ++x;

        if (first) {
            p2.replaceChild(tr, p);
            first = false;
        } else p2.appendChild(tr);
    }
}

function onMouseMoveCol(e) {
    try {
        if (movingCol) {
            var d = document.getElementById("columndiv" + movingCol);

            var l = d.style.left;
            l = l.substring(0, l.length - 2);

            var t = d.style.top;
            t = t.substring(0, t.length - 2);

            var mousePos = getMouseCoords(e);

            if (colLastX === null) {
                colLastX = mousePos.x;
                colLastY = mousePos.y;
                colToTop(e);
            }

            var x = l - (colLastX - mousePos.x);
            var y = t - (colLastY - mousePos.y);

            if (x < 0) x = 0;
            if (y < 0) y = 0;

            d.style.left = x + "px";
            d.style.top = y + "px";

            colLastX = mousePos.x;
            colLastY = mousePos.y;
        }
    } catch (Exception) {
        displayMessage(Exception.name);
    }
}

var movingCol = null,
    colLastX = null,
    colLastY = null;

function insertColItself(colToUse, type, title, barID, html, scripts) {
    var d = document.createElement('div');
    d.id = "columndiv" + colToUse;
    d.style.zIndex = colToUse + 2000;

    if (!touch) d.onmousemove = function(e) {
        if (!e) e = window.event;
        onMouseMoveCol(e);
    };
    else d.ontouchmove = function(e) {
        if (!e) e = window.event;
        onMouseMoveCol(e);
    };

    var topPosn = document.getElementById('mainDiv').scrollTop;
    if (!topPosn) topPosn = 0;

    d.name = type;
    if (isIE) d.setAttribute("className", "overlaidContent");
    else d.setAttribute("class", "overlaidContent");

    var colTR = document.createElement('div');
    if (isIE) colTR.setAttribute("className", "columnBar");
    else colTR.setAttribute("class", "columnBar");
    colTR.id = 'columnBar' + barID;

    var colZap = document.createElement('div');
    if (isIE) colZap.setAttribute("className", "columnZap");
    else colZap.setAttribute("class", "columnZap");
    colZap.style.right = '00px';
    var i = document.createElement('img');
    i.id = "columnzap" + colToUse;
    i.src = 'http://' + men + '/' + dnm + '/iotaWave/Support/Images/zap.png?token=' + sid;
    i.name = colToUse;
    i.title = "Close This";
    i.onclick = function(e) {
        if (!e) e = window.event;
        colZapIt(e)
    };
    i.onmouseover = function(e) {
        onMouseOverIcon(e);
    };
    colZap.appendChild(i);
    colTR.appendChild(colZap);

    d.appendChild(colTR);

    var dx = document.createElement('div');
    dx.id = 'columnscripts' + colToUse;

    if (!scripts) scripts = '';

    dx.innerHTML = '';
    if (isIE)
        dx.innerHTML = sanitiseHTML(scripts);
    else dx.innerHTML = scripts;

    d.appendChild(dx);

    var colContents = document.createElement('div');

    d.appendChild(colContents);

    var m = document.getElementById('mainDiv');

    if (!touch) {
        m.onmousemove = function(e) {
            if (!e) e = window.event;
            onMouseMoveCol(e);
        };
        m.onmouseup = function(e) {
            movingCol = null;
            colLastX = colLastY = null;
        };
    } else {
        m.ontouchmove = function(e) {
            if (!e) e = window.event;
            onMouseMoveCol(e);
        };
        m.ontouchstop = function(e) {
            movingCol = null;
            colLastX = colLastY = null;
        };
    }

    var newDOver = false;
    if (!document.getElementById('dOver')) {
        var dOver = document.createElement('DIV');
        dOver.id = 'dOver';
        if (isIE) dOver.setAttribute("className", "blackOverlay");
        else dOver.setAttribute("class", "blackOverlay");
        newDOver = true;
    }

    if (isIE || isWebKit) {
        if (newDOver) m.appendChild(dOver);
        m.appendChild(d);
    }

    if (isIE) colContents.setAttribute("className", "mainColumnContents");
    else colContents.setAttribute("class", "mainColumnContents");
    colContents.id = 'columncontent' + colToUse;

    colContents.innerHTML = html;

    execScripts(dx);

    if (!isIE && !isWebKit) {
        if (newDOver) m.appendChild(dOver);
        m.appendChild(d);
    }

    colSetSize(colToUse);
}

function sanitiseHTML(s) {
    return "<pre>" + s + "</pre>";
}

function colCheckForLayout(col, type, columnsCount) {
    var colToUse;
    colToUse = col;
    colRemove(colToUse);

    return colToUse;
}

function colSetWidth(type, colContents) {
    if (type === "C")
        colContents.style.width = (sWidth * 0.4) + 'px';
    else
    if (type === "F")
        colContents.style.width = '200px';
    else
    if (type === "X") {

        colContents.style.width = sWidth + 'px';
    } else
    if (type === "x") {
        var w = sWidth * 0.4;
        document.getElementById('mainTable').style.left = (((sWidth / 2) - (w / 2)) - 75) + 'px';
        colContents.style.width = w + 'px';
    } else
    if (type === "B")
    ; //colContents.style.width=(sWidth*0.4)+'px';
    else
    if (type === "M")
        colContents.style.width = (sWidth * 0.2) + 'px';
    else colContents.style.width = (sWidth * 0.4) + 'px';
}

function currentColumnTypes(pattern, columnsCount) {
    var len = pattern.length;
    if (columnsCount < len) return false;

    var n, x = 1,
        match = true;

    while (match && x < columnsCount) {
        n = document.getElementById("columndiv" + x).name;

        if (pattern.charAt(x) !== n) {
            match = false;
        } else {
            ++x;
            if (x === len)
                match = false;
        }
    }
    return match;
}

function execScripts(colContents) {
    try {
        var a, cs = colContents.getElementsByTagName("script");

        var t = cs.length;

        for (var x = 0; x < t; x++) {
            var newScript = document.createElement('script');
            newScript.type = "text/javascript";
            var strExec;

            if (isMoz) {
                strExec = cs[x].textContent;
                cs[x].textContent = "";
            } else if (isWebKit) {
                strExec = cs[x].innerHTML;
                cs[x].innerHTML = "";
            } else {
                strExec = cs[x].text;
                cs[x].text = "";
            }
            newScript.text = strExec;

            a = colContents.appendChild(newScript);
        }
        return a;
    } catch (Exception) {
        displayMessage(Exception.name + " " + Exception.message);
    }
}

String.prototype.startsWith = function(str) {
    return (this.match("^" + str) == str)
}

function getCurrentCol(idOrName) {
    if (idOrName.length === 0) return -1;
    var e = document.getElementById(idOrName);
    if (!e) e = document.getElementsByName(idOrName);
    if (!e) return 0;

    var i, j, p = e.parentNode;

    while (p) {
        i = p.id;

        if (i && i.startsWith('columndiv')) {
            if (i.length > 9) {
                j = i.charAt(9);

                if (j >= '0' && j <= '9') {
                    return i.substring(9);
                }
            }
        }
        p = p.parentNode;
    }

    return -1;
}

function colRemove(col) {
    if (col === -1) return;
    try {
        var d, j;
        --columnsCount;
        while (columnsCount >= col) {
            d = document.getElementById("columndiv" + columnsCount);
            d.parentNode.removeChild(d);

            j = document.getElementById("columndivmax" + columnsCount);
            if (j) j.parentNode.removeChild(j);
            --columnsCount;
        }
        ++columnsCount;
    } catch (Exception) {
        displayMessage(Exception.message);
    }
}

function colZapIt(e) {
    var n = detectEvent(e).name;
    colZapIt2(n, columnsCount, 'U');
}

function colZapIt2(n, to, which) {
    if (n < 0) return;
    try {
        n = document.getElementById("columnzap" + n);
        if (!n) return;
        n = parseInt(n.id.substring(9));

        var z, j = document.getElementById("columndiv" + n);
        j.parentNode.removeChild(j);

        --columnsCount;

        var dOver = document.getElementById('dOver');
        if (dOver) dOver.parentNode.removeChild(dOver);

    } catch (Exception) {
        alert(Exception.message);
    }
}

function colZapIt3() {
    try {
        n = '1';
        n = document.getElementById("columnzap" + n);
        if (!n) return;
        n = parseInt(n.id.substring(9));

        var z, j = document.getElementById("columndiv" + n);
        j.parentNode.removeChild(j);
        --columnsCount;

        var dOver = document.getElementById('dOver');
        if (dOver) dOver.parentNode.removeChild(dOver);
    } catch (Exception) {}
}

function colCutAll2() {
    try {
        for (var i = columnsCount; i >= 1; --i) colZapIt2(i, columnsCount, 'X');
        columnsCount = 1;
    } catch (Exception) {}
}

function onMouseOverIcon(e) {
    var i = detectEvent(e);
    i.style.cursor = 'pointer';
}

var moving;

function detectEvent(e) {
    var targ, e;
    if (!e) e = window.event;

    if (e.target) targ = e.target;
    else
    if (e.srcElement) targ = e.srcElement;
    if (targ.nodeType === 3) // Safari bug
        targ = targ.parentNode;
    return targ;
}

function onMouseUpSeparator(e) {
    moving = false;
    document.onmousemove = null;
}

function stopBubble(e) {
    if (!e) var e = window.event;

    if (e.stopPropagation)
        e.stopPropagation();
    else
    if (window.event)
        window.event.cancelBubble = true;
}

function onMouseMoveSeparator(ev) {
    if (moving) {
        var mousePos = getMouseCoords(ev);
        moving.style.right = (mousePos.x + 1) + "px";
        moving.style.width = (mousePos.x + 1) + "px";
    }
}

function getMouseCoords(e) {
    var posx = 0;
    var posy = 0;
    if (!e) var e = window.event;
    if (e.pageX || e.pageY) {
        posx = e.pageX;
        posy = e.pageY;
    } else
    if (e.clientX || e.clientY) {
        posx = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
        posy = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    } else
    if (e.touches[0].pageX || e.touches[0].pageY) {
        posx = e.touches[0].pageX;
        posy = e.touches[0].pageY;
    }
    return {
        x: posx,
        y: posy
    };
}

function screenWidth() {
    return sWidth;
}

function screenHeight() {
    return sHeight;
}

var sHeight, sWidth;

function setTagButtonCallBack(menuName, menuEntry, module, svc, svcArgs) {
    dockMenuName = menuName;
    dockMenuEntry = menuEntry;
    dockModule = module;
    dockSvc = svc;
    dockSvcArgs = svcArgs;
}

var dockMenuName, dockMenuEntry, dockOpType, dockModule, dockSvc, dockSvcArgs;

function executeAddTag(histOrTag) {
    addTag(dockMenuName, dockMenuEntry, dockOpType, dockModule, dockSvc, dockSvcArgs, 'B', histOrTag);
}

function executeRemoveTag() {
    removeTag(dockMenuName, dockMenuEntry);
}

function addBackTag(tagEntry, module, svc, svcArgs) {
    try {
        var h = document.getElementById('trailPanel');

        if (!h) return;

        var a = document.createElement('a');
        a.href = "javascript:connObj.enQueue('N','" + module + "','" + svc + "','" + svcArgs + "')";
        a.innerHTML = tagEntry;

        var span = document.createElement('span');
        span.innerHTML = '<br>';

        if (tagEntry !== 'Home') h.appendChild(span);
        h.appendChild(a);
    } catch (Exception) {
        displayMessage(tagEntry + " " + Exception.name + " " + Exception.message);
    }
}

function onMouseOverLWC(i) {
    lwcHideAll();
    i = i.nextSibling;
    i.parentNode.style.cursor = 'pointer';
    if (i.style.display === 'none') i.style.display = 'block';
}

function lwcHideAll() {
    try {
        var a = document.getElementsByName('lwcDD');
        if (!a) return;

        var j = 0,
            k = a.length;

        while (j < k) {
            a[j].style.display = 'none';
            ++j;
        }

    } catch (Exception) {}
}

function removeTag(menuName, menuEntry) {
    var h = document.getElementById('history');

    var m = null;
    if (h.hasChildNodes()) {
        var i = h.firstChild,
            quit = false;
        while (!quit) {
            if (i.firstChild.firstChild.innerHTML === menuName) {
                m = i;
                quit = true;
            } else {
                i = i.nextSibling;
                if (!i) quit = true;
            }
        }
    }

    if (m) {
        var l = document.getElementById(menuName + 'LI');
        var d = l.firstChild.nextSibling;

        var a = d.firstChild;
        while (true) {
            if (a.innerHTML === menuEntry) {
                if (d.childNodes.length === 1) {
                    d.removeChild(a);
                    h.removeChild(document.getElementById(menuName + 'UL'));
                } else d.removeChild(a);
                return;
            } else {
                a = a.nextSibling;
                if (!a) return;
            }
        }
    }
}

function enQueueBack(menuEntry, module, svc, svcArgs) {
    removeTag("Back", menuEntry);
    connObj.enQueue('N', module, svc, svcArgs);
}

function displayMessage(msg, time) {
    alertify.okBtn('OK').alert('<font size="5">' + msg + '</font>');
}

function doSanitise(s) {
    if (!s) return "";

    var d = document.createElement("div");
    d.value = s.replace(/'/g, "%27").replace(/#/g, "%23").replace(/%/g, "%25").replace(/ /g, "%20").replace(/"/g, "%22").replace(/&/g, "%26").replace(/\?/g, "%3f").replace(/\+/g, "%2b").replace(/\//g, "___SLASH___");
    var s = d.value;
    var t = '';
    for (var i = 0, l = s.length; i < l; ++i) {
        if (s.charCodeAt(i) === 233)
            t += "%99";
        else
        if (s.charCodeAt(i) === 8216)
            t += "%23";
        else
        if (s.charCodeAt(i) === 8217)
            t += "%23";
        else t += s.charAt(i);
    }
    return t;
}

function deSanitise(s) {
    if (!s) return "";
    var ta = document.createElement("div");
    ta.value = s.replace(/&apos;/g, "'").replace(/%25/g, "%").replace(/%20/g, " ").replace(/%22/g, "\"").replace(/%27/g, "'").replace(/%26/g, "&").replace(/%3f/g, "?").replace(/%2b/g, "+").replace(/___SLASH___/g, "/");
    return ta.value;
}

function mainScripts(d, scripts) {
    if (!scripts) scripts = '';

    var dx = document.createElement('div');

    d.appendChild(dx);

    if (isIE)
        dx.innerHTML = sanitiseHTML(scripts);
    else dx.innerHTML = scripts;

    execScripts(dx);
}

function updateScreen(info, html, scripts) {
    html = deSanitise(html);

    scripts = deSanitise(scripts);
    var s = document.createElement('script');
    s.title = 'temp';
    s.type = 'text/javascript';
    s.text = scripts;

    if (info.startsWith('OKBODY')) {
        document.body.innerHTML = html;

        var h = document.getElementsByTagName('head')[0];
        var t = h.getElementsByTagName('SCRIPT');
        for (var i = 0; i < t.length; ++i) {
            if (t[i].title === 'temp') t[i].parentNode.removeChild(t[i]);
        }

        document.head.appendChild(s);
    } else {
        var x = 2,
            len = info.length,
            divID = '',
            divIDToUse = '';
        while (x < len && info.charAt(x) !== ';') divID += info.charAt(x++);
        ++x;
        while (x < len && info.charAt(x) !== ';') divIDToUse += info.charAt(x++);

        var n = document.getElementById(divID);
        if (n) {
            var div = document.createElement('DIV');
            div.id = divIDToUse;
            div.innerHTML = html;
            insertAfter(n.parentNode, div, n);

            div.appendChild(s);
        }
    }
}
