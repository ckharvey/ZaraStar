//============================================================================
// System: ZaraStar
// File: waveclient6.js
// Author: C.K.Harvey
// Copyright (c) 2010-18 Christopher Harvey. All Rights Reserved.
// ============================================================================
var bnm = browserSniff();

var isMoz = bnm.basetype === "mozilla" ? true : false;
var isWebKit = bnm.basetype === "webkit" ? true : false;
var isIE = bnm.basetype === "ie" ? true : false;
var wavePanelCARETPOSN, wavePanelDELBKSP;
var TARGET, COLTYPE, MENUNAME, FORMID, WAVEID, CHANIMG, MEN, RESPONSEHTML, RESPONSESCRIPTS;
var MEN;

var dataSlots = [];
var resSlots = [];
var drtout;

function getSlot() {
    try {
        var i = 0;
        if (dataSlots !== null)
            for (i = 0; i < dataSlots.length; ++i)
                if (dataSlots[i] === 'EMPTY') {
                    dataSlots[i] = '';
                    return i;
                }
        dataSlots.push('');
        resSlots.push('');
        return i;
    } catch (Exception) {}
}

function stripQuote(code) {
    var code2 = '',
        x, len = code.length;
    for (x = 0; x < len; ++x)
        if (code.charAt(x) !== '\'')
            code2 += code.charAt(x);
    return code2;
};

// --------------------------------------------------------------------------------
function insertAfter(parent, node, referenceNode) {
    parent.insertBefore(node, referenceNode.nextSibling);
}

// --------------------------------------------------------------------------------
function copyStyle(ele, style) {
    for (var iStyle in style) {
        if (style[iStyle] !== undefined)
            ele.style[iStyle] = style[iStyle];
    }
    ele.bHasStyle = true;
}

// --------------------------------------------------------------------------------
function replaceBackticks(text) {
    var s = '',
        c, len = text.length,
        x = 0;
    while (x < len) {
        c = text.charAt(x);
        if (c === '`')
            s += '\n';
        else s += c;
        ++x;
    }

    return s;
}

// --------------------------------------------------------------------------------
function replaceNewlinesByBackticksPlus(text) {
    var s = '',
        c, len = text.length,
        x = 0;
    while (x < len) {
        c = text.charAt(x);
        if (c === '\n')
            s += '`';
        else s += c;
        ++x;
    }

    return s;
}

// --------------------------------------------------------------------------------
function closeAnyEdits(node) {
    var btDiv;

    node = node.childNodes[0];
    while (node !== null) {
        if (node && node.id && node.id.indexOf('be+') === 0) {
            var wID = node.id.substring(3);

            node.parentNode.removeChild(node);

            btDiv = document.getElementById('bet+' + wID);

            if (btDiv) btDiv.id = 'bt+' + wID;
        }

        closeAnyEdits(node);
        node = node.nextSibling;
    }
}

// --------------------------------------------------------------------------------
function handleCut(text, storyletID) {
    if (text.length === 0) return;
    var ctrl = document.getElementById('inputText' + storyletID);
    var posn = wavePanelCARETPOSN;

    var v = ctrl.value;
    ctrl.value = v.substring(0, posn) + v.substring(text.length + posn);

    setCaretPosn(ctrl, posn);
}

// --------------------------------------------------------------------------------
function handlePaste(text, replacedText, storyletID) {
    var ctrl = document.getElementById('inputText' + storyletID);
    var posn = wavePanelCARETPOSN;

    var v = ctrl.value;

    if (replacedText.length > 0) {
        v = v.substring(0, posn) + v.substring(replacedText.length + posn);
    }

    v = v.substring(0, posn) + text + v.substring(posn);

    ctrl.value = v;

    setCaretPosn(ctrl, posn);
}

// --------------------------------------------------------------------------------
function getSelectionBoundary(el, start) {
    var property = start ? "selectionStart" : "selectionEnd";
    var originalValue, textInputRange, precedingRange, pos, bookmark, isAtEnd;

    if (typeof el[property] === "number") {
        return el[property];
    } else
    if (document.selection && document.selection.createRange) {
        el.focus();

        var range = document.selection.createRange();
        if (range) {
            // Collapse the selected range if the selection is not a caret
            if (document.selection.type === "Text")
                range.collapse(!!start);

            originalValue = el.value;
            textInputRange = el.createTextRange();
            precedingRange = el.createTextRange();
            pos = 0;

            bookmark = range.getBookmark();
            textInputRange.moveToBookmark(bookmark);

            if (/[\r\n]/.test(originalValue)) {
                // Trickier case where input value contains line breaks
                // Test whether the selection range is at the end of the input by moving it on by one character and
                // checking if it's still within the text input.
                try {
                    range.move("character", 1);
                    isAtEnd = (range.parentElement() != el);
                } catch (ex) {
                    displayMessage("Error moving range: " + ex);
                    isAtEnd = true;
                }
                range.moveToBookmark(bookmark);

                if (isAtEnd) {
                    pos = originalValue.length;
                } else {
                    // Insert a character in the text input range and use that as a marker
                    textInputRange.text = " ";
                    precedingRange.setEndPoint("EndToStart", textInputRange);
                    pos = precedingRange.text.length - 1;

                    // Delete the inserted character
                    textInputRange.moveStart("character", -1);
                    textInputRange.text = "";
                }
            } else {
                // Easier case where input value contains no line breaks
                precedingRange.setEndPoint("EndToStart", textInputRange);
                pos = precedingRange.text.length;
            }
            return pos;
        }
    }
    return 0;
}

// --------------------------------------------------------------------------------
function getTextAreaSelection(textarea) {
    var start = getSelectionBoundary(textarea, true),
        end = getSelectionBoundary(textarea, false);

    return {
        start: start,
        end: end,
        length: end - start,
        text: textarea.value.slice(start, end)
    };
}

// --------------------------------------------------------------------------------
function detectPaste(textarea, callback) {
    textarea.onpaste = function() {
        var sel = getTextAreaSelection(textarea);
        var initialLength = textarea.value.length;
        window.setTimeout(function() {
            var val = textarea.value;
            var pastedTextLength = val.length - (initialLength - sel.length);
            var end = sel.start + pastedTextLength;
            callback({
                start: sel.start,
                end: end,
                length: pastedTextLength,
                text: val.slice(sel.start, end),
                replacedText: sel.text
            });
        }, 1);
    };
}

// --------------------------------------------------------------------------------
function detectCut(textarea, callback) {
    textarea.oncut = function() {
        var sel = getTextAreaSelection(textarea);
        if (sel.length === 0) return;

        var initialLength = textarea.value.length;
        window.setTimeout(function() {
            var val = textarea.value;
            var cutTextLength = val.length - (initialLength - sel.length);
            var end = sel.start + cutTextLength;
            callback({
                start: sel.start,
                end: end,
                length: cutTextLength,
                text: val.slice(sel.start, end),
                replacedText: sel.text
            });
        }, 1);
    };
}

// --------------------------------------------------------------------------------
function fireEvent(element, event) {
    if (document.createEventObject) // IE
    {
        var evt = document.createEventObject();
        return element.fireEvent('on' + event, evt);
    } else {
        var evt = document.createEvent("HTMLEvents");
        evt.initEvent(event, true, true);
        return !element.dispatchEvent(evt);
    }
}

// --------------------------------------------------------------------------------
function getCaretPosn(ctrl) {
    var CaretPos = 0;
    if (document.selection) {
        ctrl.focus();
        var Sel = document.selection.createRange();
        Sel.moveStart('character', -ctrl.value.length);
        CaretPos = Sel.text.length;
    } else
    if (ctrl.selectionStart || ctrl.selectionStart === '0')
        CaretPos = ctrl.selectionStart;
    return (CaretPos);
}

// --------------------------------------------------------------------------------
function setCaretPosn(ctrl, pos) {
    if (ctrl.setSelectionRange) {
        ctrl.focus();
        ctrl.setSelectionRange(pos, pos);
    } else
    if (ctrl.createTextRange) {
        var range = ctrl.createTextRange();
        range.collapse(true);
        range.moveEnd('character', pos);
        range.moveStart('character', pos);
        range.select();
    }
}

// --------------------------------------------------------------------------------
function onKeyDown(evt) {
    if (evt.altKey) return true;

    if (evt.ctrlKey || evt.metaKey) return true;

    var ret = true;
    switch (evt.keyCode) {
        case 8: // backspace
            wavePanelDELBKSP = true;
            var ctrl = document.getElementById('inputText');
            fireEvent(ctrl, 'cut');
            break;
        case 46: // del
            wavePanelDELBKSP = false;
            var ctrl = document.getElementById('inputText');
            fireEvent(ctrl, 'cut');
            break;
        default:
            ret = false;
            break;
    }

    if (isWebKit || isMoz)
        return ret;
    return false;
}

// --------------------------------------------------------------------------------
function removeAllChildren(node) {
    if (node && node.hasChildNodes()) {
        while (node.childNodes.length >= 1)
            node.removeChild(node.firstChild);
    }
}

// ---------------------------------------------------------------------------------

var arrXhr = [];
var reqSlots = [];

function getReqSlot() {
    try {
        var i = 0;
        if (reqSlots === null)
            for (i = 0; i < reqSlots.length; ++i)
                if (reqSlots[i] === 'EMPTY') {
                    reqSlots[i] = '';
                    return i;
                }
        reqSlots.push('');
        return i;
    } catch (Exception) {
        alert("ex: " + Exception.name);
    }
}

function socketSend(verb, msg, men) {
    var helperFunc = function(arrIndex) {
        return function() {
            try {
                if (arrXhr[arrIndex].readyState === 4) {
                    if (arrXhr[arrIndex].status === 200) {
                        var res = '';
                        try {
                            if (arrXhr[arrIndex].responseXML.getElementsByTagName("res")[0])
                                res = arrXhr[arrIndex].responseXML.getElementsByTagName("res")[0].childNodes[0].nodeValue;
                        } catch (Exception) {};

                        if (res.length > 0) {
                            try {
                                responseReceive(arrIndex, res);
                            } catch (Exception) {}
                        }
                    }
                }
            } catch (Exception) {
                displayMessage('processrequest44 ' + Exception);
            }
        };
    };

    var nextSlot = getReqSlot();
    arrXhr[nextSlot] = initReq(men);
    arrXhr[nextSlot].onreadystatechange = helperFunc(nextSlot); // must be before open (for chrome)

    arrXhr[nextSlot].open(verb, men + msg, true);
    arrXhr[nextSlot].setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset:UTF-8");
    arrXhr[nextSlot].setRequestHeader("Accept", "text/xml");

    if (isIE) arrXhr[nextSlot].setRequestHeader("Connection", "close"); // essential for IE

    arrXhr[nextSlot].send(msg);
    ++nextSlot;
}

function socketSendPOST(verb, req, men, msg) {
    var helperFunc = function(arrIndex) {
        return function() {
            try {
                if (arrXhr[arrIndex].readyState === 4) {
                    if (arrXhr[arrIndex].status === 200) {
                        var res = '';
                        try {
                            if (arrXhr[arrIndex].responseXML.getElementsByTagName("res")[0])
                                res = arrXhr[arrIndex].responseXML.getElementsByTagName("res")[0].childNodes[0].nodeValue;
                        } catch (Exception) {}

                        if (res.length > 0) {
                            try {
                                responseReceive(arrIndex, res);
                            } catch (Exception) {}
                        }
                    }
                }
            } catch (Exception) {
                displayMessage('processrequest44 ' + Exception);
            }
        };
    };

    var nextSlot = getReqSlot();
    arrXhr[nextSlot] = initReq(men);
    arrXhr[nextSlot].onreadystatechange = helperFunc(nextSlot); // must be before open (for chrome)
    arrXhr[nextSlot].open("POST", men + req, true);
    arrXhr[nextSlot].setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset:UTF-8");
    arrXhr[nextSlot].setRequestHeader("Accept", "text/xml");
    if (isIE) arrXhr[nextSlot].setRequestHeader("Connection", "close"); // essential for IE

    arrXhr[nextSlot].send(msg);

    ++nextSlot;
}

function initReq(url) {
    var req = null;
    if (window.ActiveXObject) {
        try {
            req = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
            req = new ActiveXObject("Microsoft.XMLHTTP");
        }
    } else
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    }

    return req;
}

function delay(millis) {
    var date = new Date();
    var curDate = null;

    do {
        curDate = new Date();
    }
    while (curDate - date < millis);
}

// ------------------------------------------------------------------------------------------------------------------------------
String.prototype.startsWith = function(str) {
    return (this.match("^" + str) === str)
}

// ------------------------------------------------------------------------------------------------------------------------------
function responseReceive(arrIndex, res) {
    if (res.startsWith("DATA:")) {
        var res5 = res.substring(5);
        var d = document.createElement('SCRIPT');
        d.type = "text/javascript";
        var i = res5.indexOf("Received");
        var dn = '';
        if (i !== -1) {
            dn = res5.substring(0, i);
            d.title = 'temp-' + dn;
        }
        d.text = res5;

        var h = document.getElementsByTagName('body')[0];
        var t = h.getElementsByTagName('SCRIPT');
        for (var i = 0; i < t.length; ++i) {
            if (t[i].title === 'temp-' + dn) t[i].parentNode.removeChild(t[i]);
        }

        h.appendChild(d);
    } else
    if (res.startsWith("HTML:")) {}
    reqSlots[arrIndex] = 'EMPTY';
}

function handleHTML() {
    if (RESPONSEHTML !== undefined)
        return (insertCol(TARGET, COLTYPE, MENUNAME, FORMID, WAVEID, CHANIMG, MEN, RESPONSEHTML, RESPONSESCRIPTS));
}

function processFORM(formID) {
        var iName, i, s = "{";

        var f = document.getElementById(formID);

        var inputs = f.getElementsByTagName("input");
        for (i = 0; i < inputs.length; ++i) {
            iName = inputs[i].name;
            if (!iName || iName === 'undefined') iName = '';

            if (inputs[i].type.toLowerCase() === 'checkbox') {
                val = '';
                if (inputs[i].checked)
                    val = "on";
                if (val.length > 0) {
                    if (s.length > 1) s += ",";
                    s += "\"" + inputs[i].name + "\":\"" + val + "\"";
                }
            } else
            if (inputs[i].type.toLowerCase() === 'text') {
                if (s.length > 1) s += ",";
                s += "\"" + inputs[i].name + "\":\"" + inputs[i].value + "\"";
            } else
            if (inputs[i].type.toLowerCase() === 'password') {
                if (s.length > 1) s += ",";
                s += "\"" + inputs[i].name + "\":\"" + inputs[i].value + "\"";
            } else
            if (inputs[i].type.toLowerCase() === 'hidden') {
                if (s.length > 1) s += ",";
                s += "\"" + inputs[i].name + "\":\"" + inputs[i].value + "\"";
            } else
            if (inputs[i].type.toLowerCase() === 'radio') {
                val = '';
                if (inputs[i].checked) {
                    val = inputs[i].value;
                    if (s.length > 1) s += ",";
                    s += "\"" + inputs[i].name + "\":\"" + val + "\"";
                }
            }
        }

        var selects = f.getElementsByTagName("select");
        for (i = 0; i < selects.length; ++i) {
            if (s.length > 1) s += ",";
            s += "\"" + selects[i].name + "\":\"" + selects[i].value + "\"";
        }

        var textareas = f.getElementsByTagName("textarea");
        for (i = 0; i < textareas.length; ++i) {
            if (s.length > 1) s += ",";
            var t = textareas[i].value,
                len = t.length,
                t2 = '',
                tt = 0;
            while (tt < len) {
                if (t.charAt(tt) === '\n')
                    t2 += '`';
                else t2 += t.charAt(tt);
                ++tt;
            }
            s += "\"" + textareas[i].name + "\":\"" + t2 + "\"";
        }

        s += "}";

        return s;
    }
    // --------------------------------------------------------------------------------------------
function getNatural(ele) {
    var img = new Image();
    img.src = ele.src;
    return {
        width: img.width,
        height: img.height
    };
}

var data1033INFO, data1033HTML, data1033SCRIPTS, data1033Received;

// --------------------------------------------------------------------------------
function doEditAnswer(whichAnswer, questionID, men, imagesDir, fromCreate) {
    data1033Received = false;

    socketSend("GET", "/_1033/data1033/" + questionID + "/" + whichAnswer + ".call", men);

    drtout = testEditAnswer(whichAnswer, questionID, men, imagesDir, fromCreate);
}

// --------------------------------------------------------------------------------
function testEditAnswer(whichAnswer, questionID, men, imagesDir, fromCreate) {
    if (data1033Received) {
        clearTimeout(drtout);

        if (!data1033INFO.startsWith("ERR")) {
            var len = data1033HTML.length,
                x = 0,
                text = '';
            while (x < len) text += data1033HTML.charAt(x++);

            doEditAnswerText(whichAnswer, text, questionID, men, imagesDir, fromCreate);
        }
    } else drtout = setTimeout("testEditAnswer('" + whichAnswer + "','" + questionID + "','" + men + "','" + imagesDir + "','" + fromCreate + "')", 100);
}

function doEditAnswerText(whichAnswer, text, questionID, men, imagesDir, fromCreate) {
    text = decodeURIComponent(text);

    var td2 = document.createElement('DIV');
    td2.id = 'td-' + questionID + '-4-' + whichAnswer;
    td2.style.width = '90%';
    var iTextArea = document.createElement('textarea');
    iTextArea.id = 'inputText' + questionID + '-' + whichAnswer;
    if (isIE) iTextArea.setAttribute('className', 'iTextArea');
    else iTextArea.setAttribute('class', 'iTextArea');
    iTextArea.style.height = "200px";
    iTextArea.style.width = '100%';
    iTextArea.onclick = function(e) {
        stopPropagation(e);
    };

    iTextArea.style.visibility = 'visible';
    iTextArea.style.zIndex = '10';
    iTextArea.value = replaceBackticks(text);

    td2.appendChild(iTextArea);

    iTextArea.focus();

    detectPaste(iTextArea, function(pasteInfo) {
        var val = iTextArea.value;
        iTextArea.value = val.slice(0, pasteInfo.start) + pasteInfo.replacedText + val.slice(pasteInfo.end);
        handlePaste(pasteInfo.text, pasteInfo.replacedText, questionID);
    });

    detectCut(iTextArea, function(cutInfo) {
        var val = iTextArea.value;
        iTextArea.value = val.slice(0, cutInfo.start) + cutInfo.replacedText + val.slice(cutInfo.end);
        handleCut(cutInfo.replacedText, questionID);
    });

    var tdExisting = document.getElementById('td-' + questionID + '-4-' + whichAnswer);
    tdExisting.parentNode.replaceChild(td2, tdExisting);

    var td3 = document.createElement('DIV');
    td3.id = 'td-' + questionID + '-5-' + whichAnswer;
    var s3 = document.createElement('span');
    td3.appendChild(s3);
    s3.id = 'optionClose-' + questionID + '-' + whichAnswer;
    s3.style.visibility = 'visible';
    var a3 = document.createElement('a');
    a3.onclick = function(e) {
        closeEditAnswer(whichAnswer, questionID, men, imagesDir, fromCreate);
    };
    a3.onmouseover = function(e) {
        this.style.cursor = 'pointer';
    };

    var img3 = document.createElement('img');
    img3.style.border = '0';
    img3.src = men + imagesDir + "closewavelet.png";
    a3.appendChild(img3);
    s3.appendChild(a3);

    tdExisting = document.getElementById('td-' + questionID + '-5-' + whichAnswer);
    tdExisting.parentNode.replaceChild(td3, tdExisting);
}

var data1033eINFO, data1033eHTML, data1033eSCRIPTS, data1033eReceived;

// --------------------------------------------------------------------------------
function doEditLink(whichLink, questionID, men, imagesDir, fromCreate) {
    data1033eReceived = false;

    socketSend("GET", "/_1033e/data1033e/" + questionID + "/" + whichLink + ".call", men);

    drtout = testEditLink(whichLink, questionID, men, imagesDir, fromCreate);
}

// --------------------------------------------------------------------------------
function testEditLink(whichLink, questionID, men, imagesDir, fromCreate) {
    if (data1033eReceived) {
        clearTimeout(drtout);

        if (!data1033eINFO.startsWith("ERR")) {
            var len = data1033eHTML.length,
                x = 0,
                text = '';
            while (x < len) text += data1033eHTML.charAt(x++);

            doEditLinkText(whichLink, text, questionID, men, imagesDir, fromCreate);
        }
    } else drtout = setTimeout("testEditLink('" + whichLink + "','" + questionID + "','" + men + "','" + imagesDir + "','" + fromCreate + "')", 100);
}

function doEditLinkText(whichLink, text, questionID, men, imagesDir, fromCreate) {
    text = decodeURIComponent(text);

    var td2 = document.createElement('DIV');
    td2.id = 'answerLink-' + questionID + '-a-' + whichLink;
    td2.style.width = '90%';
    var i = document.createElement('input');
    i.id = 'answerLink-' + questionID + '-i-' + whichLink;
    i.value = text;
    td2.appendChild(i);

    i.focus();

    var tdExisting = document.getElementById('answerLink-' + questionID + '-aa-' + whichLink);
    tdExisting.parentNode.replaceChild(td2, tdExisting);

    var td3 = document.createElement('DIV');
    td3.id = 'answerLink-' + questionID + '-aa-' + whichLink;
    var s3 = document.createElement('span');
    td3.appendChild(s3);
    s3.id = 'optionClose-' + questionID + '-a-' + whichLink;
    s3.style.visibility = 'visible';
    var a3 = document.createElement('a');
    a3.onclick = function(e) {
        closeEditLink(whichLink, questionID, men, imagesDir, fromCreate);
    };
    a3.onmouseover = function(e) {
        this.style.cursor = 'pointer';
    };

    var img3 = document.createElement('img');
    img3.style.border = '0';
    img3.src = men + imagesDir + "closewavelet.png";
    a3.appendChild(img3);
    s3.appendChild(a3);

    tdExisting = document.getElementById('answerLink-' + questionID + '-a-' + whichLink);
    tdExisting.parentNode.replaceChild(td3, tdExisting);
}

var data1033fINFO, data1033fHTML, data1033fSCRIPTS, data1033fReceived;

// --------------------------------------------------------------------------------
function closeEditLink(whichLink, questionID, men, imagesDir, fromCreate) {
    var text = document.getElementById('answerLink-' + questionID + '-i-' + whichLink).value;

    text = doSanitise(text);

    data1033fReceived = false;

    socketSend("POST", "/_1033f/data1033f/" + questionID + "/" + whichLink + "/" + text + ".call", men);

    drtout = testCloseEditLink(whichLink, questionID, men, imagesDir, fromCreate);
}

// --------------------------------------------------------------------------------
function testCloseEditLink(whichLink, questionID, men, imagesDir, fromCreate) {
    if (data1033fReceived) {
        clearTimeout(drtout);

        data1033fHTML = decodeURIComponent(data1033fHTML);

        if (!data1033fINFO.startsWith("ERR")) {
            var len = data1033fHTML.length,
                x = 0,
                text = '';
            while (x < len) text += data1033fHTML.charAt(x++);

            closeAnEditLink(whichLink, questionID, text, men, imagesDir, fromCreate);
        }
    } else drtout = setTimeout("testCloseEditLink('" + whichLink + "','" + questionID + "','" + men + "','" + imagesDir + "','" + fromCreate + "')", 100);
}

// --------------------------------------------------------------------------------
function closeAnEditLink(whichLink, questionID, text, men, imagesDir, fromCreate) {
    text = decodeURIComponent(text);

    var td2 = document.createElement('DIV');
    td2.id = 'answerLink-' + questionID + '-aa-' + whichLink;

    if (isIE) td2.setAttribute('className', 'divRow');
    else td2.setAttribute('class', 'divRow');

    td2.onclick = function(e) {
        doEditLink(whichLink, questionID, men, imagesDir, fromCreate);
    };
    td2.style.cursor = 'pointer';

    var node = document.getElementById('answerLink-' + questionID + '-aa-' + whichLink);
    node.parentNode.replaceChild(td2, node);

    var p = document.createElement('P');
    td2.appendChild(p);

    var b = document.createElement('B');
    b.innerHTML = 'Story/Question Link: ';
    p.appendChild(b);

    var s = document.createElement('SPAN');

    if (text.length === 0) {
        if (isIE) s.setAttribute('className', 'editHere');
        else s.setAttribute('class', 'editHere');
        s.innerHTML = '&nbsp;Add Link Here&nbsp;';
    } else {
        if (fromCreate === 'N')
            s.innerHTML = text;
    }

    p.appendChild(s);

    if (fromCreate === 'Y') {
        var n = document.getElementById("answerLink-" + questionID + '-a-' + whichLink);
        if (n) removeAllChildren(n);
    } else {
        var td3 = document.createElement('TD');
        td3.id = 'answerLink-' + questionID + '-a-' + whichLink;
        var n = document.getElementById("answerLink-" + questionID + '-a-' + whichLink);
        n.parentNode.replaceChild(td3, n);
    }
}

var data1033aINFO, data1033aHTML, data1033aSCRIPTS, data1033aReceived;

// --------------------------------------------------------------------------------
function closeEditAnswer(whichAnswer, questionID, men, imagesDir, fromCreate) {
    var text = document.getElementById('inputText' + questionID + '-' + whichAnswer).value;

    text = replaceNewlinesByBackticksPlus(text);

    text = doSanitise(text);

    data1033aReceived = false;

    socketSend("POST", "/_1033a/data1033a/" + questionID + "/" + whichAnswer + "/" + text + ".call", men);

    drtout = testCloseEditAnswer(questionID, men, imagesDir, fromCreate);
}

// --------------------------------------------------------------------------------
function testCloseEditAnswer(questionID, men, imagesDir, fromCreate) {
    if (data1033aReceived) {
        clearTimeout(drtout);

        data1033aHTML = decodeURIComponent(data1033aHTML);

        if (!data1033aINFO.startsWith("ERR")) {
            var len = data1033aHTML.length,
                x = 0,
                text = '',
                whichAnswer = '';
            while (x < len && data1033aHTML.charAt(x) !== '`') whichAnswer += data1033aHTML.charAt(x++);
            ++x;
            while (x < len) text += data1033aHTML.charAt(x++);

            closeAnEditAnswer(questionID, whichAnswer, text, men, imagesDir, fromCreate);
        }
    } else drtout = setTimeout("testCloseEditAnswer('" + questionID + "','" + men + "','" + imagesDir + "','" + fromCreate + "')", 100);
}

// --------------------------------------------------------------------------------
function closeAnEditAnswer(questionID, whichAnswer, text, men, imagesDir, fromCreate) {
    text = decodeURIComponent(text);

    var td2 = document.createElement('DIV');
    td2.id = 'td-' + questionID + '-4-' + whichAnswer;

    if (isIE) td2.setAttribute('className', 'divRow');
    else td2.setAttribute('class', 'divRow');

    td2.onclick = function(e) {
        doEditAnswer(whichAnswer, questionID, men, imagesDir, fromCreate);
    };
    td2.style.cursor = 'pointer';

    var node = document.getElementById('td-' + questionID + '-4-' + whichAnswer);
    node.parentNode.replaceChild(td2, node);

    var p = document.createElement('P');
    td2.appendChild(p);

    var whichAnswer2;
    if (whichAnswer === '2') whichAnswer2 = 'B';
    else
    if (whichAnswer === '3') whichAnswer2 = 'C';
    else
    if (whichAnswer === '4') whichAnswer2 = 'D';
    else
    if (whichAnswer === '5') whichAnswer2 = 'E';
    else
    if (whichAnswer === '6') whichAnswer2 = 'F';
    else
    if (whichAnswer === '7') whichAnswer2 = 'G';
    else
    if (whichAnswer === '8') whichAnswer2 = 'H';
    else whichAnswer2 = 'A';

    var b = document.createElement('B');
    b.innerHTML = whichAnswer2 + ': ';
    p.appendChild(b);

    var s = document.createElement('SPAN');

    if (text.length === 0) {
        if (isIE) s.setAttribute('className', 'editHere');
        else s.setAttribute('class', 'editHere');
        s.innerHTML = '&nbsp;Add Answer ' + whichAnswer2 + ' Here&nbsp;';
    } else {
        if (fromCreate === 'N')
            s.innerHTML = text;
    }

    p.appendChild(s);

    if (fromCreate === 'Y') {
        var n = document.getElementById("td-" + questionID + '-5-' + whichAnswer);
        if (n) removeAllChildren(n);
    } else {
        var td3 = document.createElement('TD');
        td3.id = 'td-' + questionID + '-5-' + whichAnswer;
        var n = document.getElementById("td-" + questionID + '-5-' + whichAnswer);
        n.parentNode.replaceChild(td3, n);
    }
}

// --------------------------------------------------------------------------------
function doEditCorrect(whichCorrect, questionID, men, imagesDir) {
    var d = document.createElement('DIV');
    d.id = 'answercorrect-' + questionID;
    if (isIE) d.setAttribute('className', 'divRow');
    else d.setAttribute('class', 'divRow');
    d.style.cursor = 'pointer';

    var p1 = document.createElement('P');
    p1.innerHTML = 'A:';
    d.appendChild(p1);

    var r1 = document.createElement('INPUT');
    r1.type = 'radio';
    r1.name = 'correct-' + questionID;
    r1.id = 'correct-' + questionID + '-1';
    r1.style.paddingLeft = '20px';
    if (whichCorrect === 1) r1.checked = true;
    d.appendChild(r1);

    var p2 = document.createElement('P');
    p2.innerHTML = 'B:';
    d.appendChild(p2);

    var r2 = document.createElement('INPUT');
    r2.type = 'radio';
    r2.name = 'correct-' + questionID;
    r2.id = 'correct-' + questionID + '-2';
    r2.style.paddingLeft = '20px';
    if (whichCorrect === 2) r2.checked = true;
    d.appendChild(r2);

    var p3 = document.createElement('P');
    p3.innerHTML = 'C:';
    d.appendChild(p3);

    var r3 = document.createElement('INPUT');
    r3.type = 'radio';
    r3.name = 'correct-' + questionID;
    r3.id = 'correct-' + questionID + '-3';
    r3.style.paddingLeft = '20px';
    if (whichCorrect === 3) r3.checked = true;
    d.appendChild(r3);

    var p4 = document.createElement('P');
    p4.innerHTML = 'D:';
    d.appendChild(p4);

    var r4 = document.createElement('INPUT');
    r4.type = 'radio';
    r4.name = 'correct-' + questionID;
    r4.id = 'correct-' + questionID + '-4';
    r4.style.paddingLeft = '20px';
    if (whichCorrect === 4) r4.checked = true;
    d.appendChild(r4);

    var p5 = document.createElement('P');
    p5.innerHTML = 'E:';
    d.appendChild(p5);

    var r5 = document.createElement('INPUT');
    r5.type = 'radio';
    r5.name = 'correct-' + questionID;
    r5.id = 'correct-' + questionID + '-5';
    r5.style.paddingLeft = '20px';
    if (whichCorrect === 5) r5.checked = true;
    d.appendChild(r5);

    var p6 = document.createElement('P');
    p6.innerHTML = 'F:';
    d.appendChild(p6);

    var r6 = document.createElement('INPUT');
    r6.type = 'radio';
    r6.name = 'correct-' + questionID;
    r6.id = 'correct-' + questionID + '-6';
    r6.style.paddingLeft = '20px';
    if (whichCorrect === 6) r6.checked = true;
    d.appendChild(r6);

    var p7 = document.createElement('P');
    p7.innerHTML = 'G:';
    d.appendChild(p7);

    var r7 = document.createElement('INPUT');
    r7.type = 'radio';
    r7.name = 'correct-' + questionID;
    r7.id = 'correct-' + questionID + '-7';
    r7.style.paddingLeft = '20px';
    if (whichCorrect === 7) r7.checked = true;
    d.appendChild(r7);

    var p8 = document.createElement('P');
    p8.innerHTML = 'H:';
    d.appendChild(p8);

    var r8 = document.createElement('INPUT');
    r8.type = 'radio';
    r8.name = 'correct-' + questionID;
    r8.id = 'correct-' + questionID + '-8';
    r8.style.paddingLeft = '20px';
    if (whichCorrect === 8) r8.checked = true;
    d.appendChild(r8);

    var d3 = document.createElement('DIV');
    d3.onclick = function(e) {
        closeEditCorrect(questionID, men, imagesDir);
    };
    d3.onmouseover = function(e) {
        this.style.cursor = 'pointer';
    };

    var img3 = document.createElement('img');
    img3.style.border = '0';
    img3.src = men + imagesDir + "closewavelet.png";
    d3.appendChild(img3);
    d.appendChild(d3);

    var node = document.getElementById('answercorrect-' + questionID);
    node.parentNode.replaceChild(d, node);
}

var data1033bINFO, data1033bHTML, data1033bSCRIPTS, data1033bReceived;

// --------------------------------------------------------------------------------
function closeEditCorrect(questionID, men, imagesDir) {
    var whichCorrect = '1';
    if (document.getElementById('correct-' + questionID + '-2').checked) whichCorrect = '2';
    else
    if (document.getElementById('correct-' + questionID + '-3').checked) whichCorrect = '3';
    else
    if (document.getElementById('correct-' + questionID + '-4').checked) whichCorrect = '4';
    else
    if (document.getElementById('correct-' + questionID + '-5').checked) whichCorrect = '5';
    else
    if (document.getElementById('correct-' + questionID + '-6').checked) whichCorrect = '6';
    else
    if (document.getElementById('correct-' + questionID + '-7').checked) whichCorrect = '7';
    else
    if (document.getElementById('correct-' + questionID + '-8').checked) whichCorrect = '8';

    data1033bReceived = false;

    socketSend("POST", "/_1033b/data1033b/" + questionID + "/" + whichCorrect + ".call", men);

    drtout = testCloseEditCorrect(whichCorrect, questionID, men, imagesDir);
}

// --------------------------------------------------------------------------------
function testCloseEditCorrect(whichCorrect, questionID, men, imagesDir) {
    if (data1033bReceived) {
        clearTimeout(drtout);

        data1033bHTML = decodeURIComponent(data1033bHTML);

        if (!data1033bINFO.startsWith("ERR")) {
            closeAnEditCorrect(whichCorrect, questionID, men, imagesDir);
        }
    } else drtout = setTimeout("testCloseEditCorrect('" + whichCorrect + "','" + questionID + "','" + men + "','" + imagesDir + "')", 100);
}

// --------------------------------------------------------------------------------
function closeAnEditCorrect(whichCorrect, questionID, men, imagesDir) {
    var d = document.createElement('DIV');
    d.id = 'answercorrect-' + questionID;
    d.style.width = '90%';
    d.style.verticalAlign = 'top';
    d.style.paddingBottom = '10px';
    d.style.cursor = 'pointer';
    d.onclick = function(e) {
        doEditCorrect(whichCorrect, questionID, men, imagesDir);
    };

    var p = document.createElement('P');
    if (whichCorrect === 1) whichCorrect = 'A';
    else
    if (whichCorrect === 2) whichCorrect = 'B';
    else
    if (whichCorrect === 3) whichCorrect = 'C';
    else
    if (whichCorrect === 4) whichCorrect = 'D';
    else
    if (whichCorrect === 5) whichCorrect = 'E';
    else
    if (whichCorrect === 6) whichCorrect = 'F';
    else
    if (whichCorrect === 7) whichCorrect = 'G';
    else
    if (whichCorrect === 8) whichCorrect = 'H';

    var s = document.createElement('SPAN');
    s.innerHTML = 'Correct Answer: ' + whichCorrect;
    p.appendChild(s);
    d.appendChild(p);

    var node = document.getElementById('answercorrect-' + questionID);
    node.parentNode.replaceChild(d, node);
}

var data1033cINFO, data1033cHTML, data1033cSCRIPTS, data1033cReceived;

// --------------------------------------------------------------------------------
function doEditNote(whichNote, questionID, men, imagesDir, fromCreate) {
    data1033cReceived = false;

    socketSend("GET", "/_1033c/data1033c/" + questionID + "/" + whichNote + ".call", men);

    drtout = testEditNote(whichNote, questionID, men, imagesDir, fromCreate);
}

// --------------------------------------------------------------------------------
function testEditNote(whichNote, questionID, men, imagesDir, fromCreate) {
    if (data1033cReceived) {
        clearTimeout(drtout);

        if (!data1033cINFO.startsWith("ERR")) {
            var len = data1033cHTML.length,
                x = 0,
                text = '';
            while (x < len) text += data1033cHTML.charAt(x++);

            doEditNoteText(whichNote, text, questionID, men, imagesDir, fromCreate);
        }
    } else drtout = setTimeout("testEditNote('" + whichNote + "','" + questionID + "','" + men + "','" + imagesDir + "','" + fromCreate + "')", 100);
}

function doEditNoteText(whichNote, text, questionID, men, imagesDir, fromCreate) {
    text = decodeURIComponent(text);

    var td2 = document.createElement('DIV');
    td2.id = 'td-' + questionID + '-4n-' + whichNote;
    td2.style.width = '90%';
    var iTextArea = document.createElement('textarea');
    iTextArea.id = 'inputText' + questionID + '-n-' + whichNote;
    if (isIE) iTextArea.setAttribute('className', 'iTextArea');
    else iTextArea.setAttribute('class', 'iTextArea');
    iTextArea.style.height = "200px";
    iTextArea.style.width = '100%';
    iTextArea.onclick = function(e) {
        stopPropagation(e);
    };

    iTextArea.style.visibility = 'visible';
    iTextArea.style.zIndex = '10';
    iTextArea.value = replaceBackticks(text);

    td2.appendChild(iTextArea);

    iTextArea.focus();

    detectPaste(iTextArea, function(pasteInfo) {
        var val = iTextArea.value;
        iTextArea.value = val.slice(0, pasteInfo.start) + pasteInfo.replacedText + val.slice(pasteInfo.end);
        handlePaste(pasteInfo.text, pasteInfo.replacedText, questionID);
    });

    detectCut(iTextArea, function(cutInfo) {
        var val = iTextArea.value;
        iTextArea.value = val.slice(0, cutInfo.start) + cutInfo.replacedText + val.slice(cutInfo.end);
        handleCut(cutInfo.replacedText, questionID);
    });

    var tdExisting = document.getElementById('td-' + questionID + '-4n-' + whichNote);
    tdExisting.parentNode.replaceChild(td2, tdExisting);

    var td3 = document.createElement('DIV');
    td3.id = 'td-' + questionID + '-5n-' + whichNote;
    var s3 = document.createElement('span');
    td3.appendChild(s3);
    s3.id = 'optionClose-' + questionID + '-n-' + whichNote;
    s3.style.visibility = 'visible';
    var a3 = document.createElement('a');
    a3.onclick = function(e) {
        closeEditNote(whichNote, questionID, men, imagesDir, fromCreate);
    };
    a3.onmouseover = function(e) {
        this.style.cursor = 'pointer';
    };

    var img3 = document.createElement('img');
    img3.style.border = '0';
    img3.src = men + imagesDir + "closewavelet.png";
    a3.appendChild(img3);
    s3.appendChild(a3);

    tdExisting = document.getElementById('td-' + questionID + '-5n-' + whichNote);
    tdExisting.parentNode.replaceChild(td3, tdExisting);
}

var data1033dINFO, data1033dHTML, data1033dSCRIPTS, data1033dReceived;

// --------------------------------------------------------------------------------
function closeEditNote(whichNote, questionID, men, imagesDir, fromCreate) {
    var text = document.getElementById('inputText' + questionID + '-n-' + whichNote).value;

    text = replaceNewlinesByBackticksPlus(text);

    text = doSanitise(text);

    data1033aReceived = false;

    socketSend("POST", "/_1033d/data1033d/" + questionID + "/" + whichNote + "/" + text + ".call", men);

    drtout = testCloseEditNote(questionID, men, imagesDir, fromCreate);
}

// --------------------------------------------------------------------------------
function testCloseEditNote(questionID, men, imagesDir, fromCreate) {
    if (data1033dReceived) {
        clearTimeout(drtout);

        data1033dHTML = decodeURIComponent(data1033dHTML);

        if (!data1033dINFO.startsWith("ERR")) {
            var len = data1033dHTML.length,
                x = 0,
                text = '',
                whichNote = '';
            while (x < len && data1033dHTML.charAt(x) !== '`') whichNote += data1033dHTML.charAt(x++);
            ++x;
            while (x < len) text += data1033dHTML.charAt(x++);

            closeAnEditNote(questionID, whichNote, text, men, imagesDir, fromCreate);
        }
    } else drtout = setTimeout("testCloseEditNote('" + questionID + "','" + men + "','" + imagesDir + "','" + fromCreate + "')", 100);
}

// --------------------------------------------------------------------------------
function closeAnEditNote(questionID, whichNote, text, men, imagesDir, fromCreate) {
    text = decodeURIComponent(text);

    var td2 = document.createElement('DIV');
    td2.id = 'td-' + questionID + '-4n-' + whichNote;

    if (isIE) td2.setAttribute('className', 'divRow');
    else td2.setAttribute('class', 'divRow');

    td2.onclick = function(e) {
        doEditNote(whichNote, questionID, men, imagesDir, fromCreate);
    };
    td2.style.cursor = 'pointer';

    var node = document.getElementById('td-' + questionID + '-4n-' + whichNote);
    node.parentNode.replaceChild(td2, node);

    var p = document.createElement('P');
    td2.appendChild(p);

    var whichNote2;
    if (whichNote === '2') whichNote2 = 'B';
    else
    if (whichNote === '3') whichNote2 = 'C';
    else
    if (whichNote === '4') whichNote2 = 'D';
    else
    if (whichNote === '5') whichNote2 = 'E';
    else
    if (whichNote === '6') whichNote2 = 'F';
    else
    if (whichNote === '7') whichNote2 = 'G';
    else
    if (whichNote === '8') whichNote2 = 'H';
    else whichNote2 = 'A';

    var b = document.createElement('B');
    b.innerHTML = whichNote2 + ': ';
    p.appendChild(b);

    var s = document.createElement('SPAN');

    if (text.length === 0) {
        if (isIE) s.setAttribute('className', 'editHere');
        else s.setAttribute('class', 'editHere');
        s.innerHTML = '&nbsp;Add Note ' + whichNote2 + ' Here&nbsp;';
    } else {
        if (fromCreate === 'N')
            s.innerHTML = text;
    }

    p.appendChild(s);

    if (fromCreate === 'Y') {
        var n = document.getElementById("td-" + questionID + '-5n-' + whichNote);
        if (n) removeAllChildren(n);
    } else {
        var td3 = document.createElement('TD');
        td3.id = 'td-' + questionID + '-5n-' + whichNote;
        var n = document.getElementById("td-" + questionID + '-5n-' + whichNote);
        n.parentNode.replaceChild(td3, n);
    }
}
