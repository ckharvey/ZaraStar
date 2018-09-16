// ============================================================================
// System: ZaraStar
// File: wavechat.js
// Author: C.K.Harvey
// Copyright (c) 2010-17 Christopher Harvey. All Rights Reserved.
// ============================================================================
var NUMCOLS1003;
var data1051dINFO, data1051dHTML, data1051dSCRIPTS, data1051dReceived;
var wavePanelCARETPOSN;

// ----------------------------------------------------------------------------
function doDeleteAttachment(which, id, sorc, scletID, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols) {
    data1051dReceived = false;

    if (confirm('Are you sure you want to remove this ' + which + '?')) {
        if (sorc === 'table') {
            socketSend("GET", "/_1003f/data1051d/" + scletID + ".call", men);
            drtout = testDeleteAttachment(id, sorc, scletID, men, imagesDir, isWriter, callerServlet, callerArgs);
        } else {
            socketSend("GET", "/_1051d/data1051d/" + sorc + "/" + scletID + ".call", men);
            drtout = testDeleteAttachment(id, sorc, scletID, men, imagesDir, isWriter, callerServlet, callerArgs);
        }
    }
}

// ----------------------------------------------------------------------------
function testDeleteAttachment(id, sorc, scletID, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols) {
    if (data1051dReceived) {
        clearTimeout(drtout);

        if (!data1051dINFO.startsWith('OK'))
            displayMessage("Unknown Error: Cannot Delete");
        else {
            var node = document.getElementById(id + "-" + scletID + '-1');
            node.parentNode.removeChild(node);

            node = document.getElementById(id + "-" + scletID);
            if (node) node.parentNode.removeChild(node);

            if (sorc === 'table') {
                node = document.getElementById(id + "-" + scletID);
                if (node) node.parentNode.removeChild(node);
                node = document.getElementById(id + "-" + scletID + '+');
                if (node) node.parentNode.removeChild(node);
            }

            var len = data1051dHTML.length,
                x = 0,
                pseudonym = '';
            while (x < len) pseudonym += data1051dHTML.charAt(x++);

            if (pseudonym.length > 0) {
                var d = document.getElementById('td-' + scletID + '-1');
                d.style.visibility = 'visible';
                var s = document.createElement('SPAN');
                s.id = 'td-' + scletID + '-1a';
                if (isIE) s.setAttribute('className', 'editHere');
                else s.setAttribute('class', 'editHere');
                s.innerHTML = '&nbsp;Add Attachment Here&nbsp;';
                s.style.cursor = 'pointer';
                s.onclick = function(e) {
                    doInsertOptionsChat(sorc, scletID, pseudonym, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols);
                };
                d.appendChild(s);
            }
        }
    } else drtout = setTimeout("testDeleteAttachment('" + id + "','" + sorc + "','" + scletID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "','" + qnNumCols + "')", 100);
}

var data1051bINFO, data1051bHTML, data1051bSCRIPTS, data1051bReceived;

// ----------------------------------------------------------------------------
function doDeleteChat(sorc, scletID, men, imagesDir, isWriter) {
    data1051bReceived = false;

    if (confirm('Are you sure you want to delete this entry?')) {
        socketSend("GET", "/_1051b/data1051b/" + sorc + "/" + scletID + ".call", men);

        drtout = testDeleteChatlet(sorc, scletID, men, imagesDir, isWriter);
    }
}

// ----------------------------------------------------------------------------
function testDeleteChatlet(sorc, scletID, men, imagesDir, isWriter) {
    if (data1051bReceived) {
        clearTimeout(drtout);

        if (!data1051bINFO.startsWith('OK'))
            displayMessage("Unknown Error: Cannot Delete");
        else {
            node = document.getElementById("div-" + scletID);
            node.parentNode.removeChild(node);

            var len = data1051bHTML.length,
                x = 0,
                scID = '',
                newLastChatletID = '';
            while (x < len && data1051bHTML.charAt(x) !== '`') scID += data1051bHTML.charAt(x++);
            ++x;
            while (x < len) newLastChatletID += data1051bHTML.charAt(x++);

            if (newLastChatletID.length > 0) {
                var d = document.getElementById('insertChatlet');
                d.onclick = function(e) {
                    doInsertChat(sorc, scID, newLastChatletID, men, imagesDir, isWriter);
                };
            }
        }
    } else drtout = setTimeout("testDeleteChatlet('" + sorc + "','" + scletID + "','" + men + "','" + imagesDir + "')", 100);
}

var data1051aINFO, data1051aHTML, data1051aSCRIPTS, data1051aReceived;

// --------------------------------------------------------------------------------
function closeEditChat(sorc, scletID, men, imagesDir, fromCreate, isWriter) {
    var text = document.getElementById('inputText' + scletID).value;

    text = replaceNewlinesByBackticksPlus(text);

    text = doSanitise(text);

    data1051aReceived = false;

    socketSend("POST", "/_1051a/data1051a/" + sorc + "/" + scletID + "/" + text + ".call", men);

    drtout = testCloseEditChat(sorc, scletID, men, imagesDir, fromCreate, isWriter);
}

// --------------------------------------------------------------------------------
function testCloseEditChat(sorc, scletID, men, imagesDir, fromCreate, isWriter) {
    if (data1051aReceived) {
        clearTimeout(drtout);

        data1051aHTML = decodeURIComponent(data1051aHTML);

        if (!data1051aINFO.startsWith("ERR")) {
            var len = data1051aHTML.length,
                x = 0,
                text = '',
                pseudonym = '',
                ownerName = '';
            while (x < len && data1051aHTML.charAt(x) !== '`') pseudonym += data1051aHTML.charAt(x++);
            ++x;
            while (x < len) text += data1051aHTML.charAt(x++);

            closeAnEditChat(sorc, scletID, text, men, imagesDir, fromCreate, pseudonym, isWriter);
        }
    } else drtout = setTimeout("testCloseEditChat('" + sorc + "','" + scletID + "','" + men + "','" + imagesDir + "','" + fromCreate + "')", 100);
}

// --------------------------------------------------------------------------------
function closeAnEditChat(sorc, scletID, text, men, imagesDir, fromCreate, pseudonym, isWriter) {
    text = decodeURIComponent(text);

    var td2 = document.createElement('DIV');
    td2.id = 'td-' + scletID + '-2';
    td2.style.width = '90%';
    td2.style.verticalAlign = 'top';
    td2.style.paddingBottom = '10px';
    td2.onclick = function(e) {
        doEditChat('T', sorc, scletID, pseudonym, men, imagesDir, fromCreate, isWriter);
    };
    td2.style.cursor = 'pointer';

    var node = document.getElementById('td-' + scletID + '-2');
    node.parentNode.replaceChild(td2, node);

    var p = document.createElement('P');
    td2.appendChild(p);

    var s = document.createElement('SPAN');

    if (text.length === 0) {
        if (isIE) s.setAttribute('className', 'editHere');
        else s.setAttribute('class', 'editHere');
        s.innerHTML = '&nbsp;Add Text Here&nbsp;';
    } else {
        if (fromCreate === 'N') {
            s.innerHTML = text;
        }
    }

    p.appendChild(s);

    if (fromCreate === 'Y') {
        var n = document.getElementById("td-" + scletID + '-3');
        if (n) removeAllChildren(n);
    } else {
        var td3 = document.createElement('TD');
        td3.id = 'td-' + scletID + '-3';

        var n = document.getElementById("td-" + scletID + '-3');
        n.parentNode.replaceChild(td3, n);
    }

    var d = document.getElementById('td-' + scletID + '-1a');
    if (d) d.style.visibility = 'visible';
}

var data1051INFO, data1051HTML, data1051SCRIPTS, data1051Received;

// --------------------------------------------------------------------------------
function doEditChat(type, sorc, scletID, unm, men, imagesDir, fromCreate, isWriter) {
    closeAllDoEditsChat(sorc, scletID);

    if (type === 'T') {
        d = document.getElementById('td-' + scletID + '-1a');
        if (d) d.style.visibility = 'hidden';
    }

    data1051Received = false;

    socketSend("GET", "/_1051/data1051/" + sorc + "/" + scletID + ".call", men);

    drtout = testEditChat(type, sorc, scletID, unm, men, imagesDir, fromCreate, isWriter);
}

// --------------------------------------------------------------------------------
function testEditChat(type, sorc, scletID, unm, men, imagesDir, fromCreate, isWriter) {
    if (data1051Received) {
        clearTimeout(drtout);

        if (!data1051INFO.startsWith("ERR")) {
            var len = data1051HTML.length,
                x = 0,
                attachment = '',
                attachmentType = '',
                text = '';
            while (x < len && data1051HTML.charAt(x) !== '`') attachment += data1051HTML.charAt(x++);
            ++x;
            while (x < len && data1051HTML.charAt(x) !== '`') attachmentType += data1051HTML.charAt(x++);
            ++x;
            while (x < len) text += data1051HTML.charAt(x++);

            editAChatlet(type, sorc, scletID, attachment, attachmentType, text, unm, men, imagesDir, fromCreate, isWriter);
        }
    } else drtout = setTimeout("testEditChat('" + type + "','" + sorc + "','" + scletID + "','" + unm + "','" + men + "','" + imagesDir + "','" + fromCreate + "','" + isWriter + "')", 100);
}

// --------------------------------------------------------------------------------
function refetchImageChat(sorc, scletID, men, imagesDir, isWriter) {
    data1051Received = false;

    socketSend("GET", "/_1051c/data1051/" + sorc + "/" + scletID + ".call", men);

    drtout = testRefetchImageChat(sorc, scletID, men, imagesDir, isWriter);
}

// --------------------------------------------------------------------------------
function testRefetchImageChat(sorc, scletID, men, imagesDir, isWriter) {
    if (data1051Received) {
        clearTimeout(drtout);

        if (!data1051INFO.startsWith("ERR")) {
            var len = data1051HTML.length,
                x = 0,
                attachment = '',
                actualImageWidth = '',
                actualImageHeight = '',
                imageWidth = '',
                imageHeight = '';
            while (x < len) attachment += data1051HTML.charAt(x++);

            var i = document.createElement('IMG');
            i.id = 'chatImage3014-' + scletID;
            i.src = men + attachment;
            i.style.border = '0';
            i.style.maxWidth = '100%';
            i.style.height = 'auto';
            //i.style.cursor='pointer';
            var n = document.getElementById('chatImage3014-' + scletID);
            if (n) n.parentNode.replaceChild(i, n);
            else {
                var td = document.createElement('TD');
                td.id = 'td-' + scletID + '-1';
                td.appendChild(i);
                var td0 = document.getElementById('td-' + scletID + '-3');
                if (td0) insertAfter(td0.parentNode, td, td0);
            }

            var td1 = document.createElement('TD');
            td1.id = 'chatImage3014-' + scletID + '-1';
            td1.style.verticalAlign = 'top';
            var i = document.createElement('IMG');
            i.style.cursor = 'pointer';
            i.onclick = function(e) {
                doDeleteAttachment('Image', 'chatImage3014', sorc, scletID, men, imagesDir, isWriter);
            };
            i.src = men + imagesDir + 'terminated.png';
            i.style.border = '0';
            i.style.paddingLeft = '20px';
            td1.appendChild(i);
            if (td) insertAfter(td.parentNode, td1, td);

            var dd = document.getElementById('td-' + scletID + '-1a');
            if (dd) dd.parentNode.removeChild(dd);
            closeDoEditImageChat(sorc, scletID);

            closeAllDoEditsChat(sorc, scletID);
        }
    } else drtout = setTimeout("testRefetchImageChat('" + sorc + "','" + scletID + "','" + men + "','" + imagesDir + "','" + isWriter + "')", 100);
}

var data1050INFO, data1050HTML, data1050SCRIPTS, data1050Received;

// --------------------------------------------------------------------------------
function doInsertChat(sorc, scID, afterChatletID, men, imagesDir, isWriter, callerServlet, callerArgs) {
    data1050Received = false;

    socketSend("GET", "/_1050/data1050/" + sorc + "/" + scID + "/" + afterChatletID + ".call", men);

    drtout = testNewChatlet(sorc, scID, afterChatletID, men, imagesDir, isWriter, callerServlet, callerArgs);
}

// --------------------------------------------------------------------------------
function testNewChatlet(sorc, scID, afterSCletID, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols) {
    if (data1050Received) {
        clearTimeout(drtout);

        if (!data1050INFO.startsWith("ERR")) {
            var len = data1050HTML.length,
                x = 0,
                newSCletID = '',
                scletPosn = '',
                owner = '',
                ownerImage = '',
                ownerName = '';
            while (x < len && data1050HTML.charAt(x) !== '`') newSCletID += data1050HTML.charAt(x++);
            ++x;
            while (x < len && data1050HTML.charAt(x) !== '`') scletPosn += data1050HTML.charAt(x++);
            ++x;
            while (x < len && data1050HTML.charAt(x) !== '`') owner += data1050HTML.charAt(x++);
            ++x;
            while (x < len && data1050HTML.charAt(x) !== '`') ownerName += data1050HTML.charAt(x++);
            ++x;
            while (x < len) ownerImage += data1050HTML.charAt(x++);

            ownerName = deSanitise(ownerName);

            var isAtBeginning = 'N';
            if (newSCletID.charAt(0) === '-') {
                isAtBeginning = 'Y';
                newSCletID = newSCletID.substring(1);
            }

            insertChatlet(isAtBeginning, afterSCletID, sorc, newSCletID, scletPosn, owner, ownerImage, ownerName, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols);

            doEditChat('T', sorc, newSCletID, owner, men, imagesDir, 'N', isWriter);

            var d = document.getElementById('insertChatlet');
            d.onclick = function(e) {
                doInsertChat(sorc, scID, newSCletID, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols);
            }
        }
    } else drtout = setTimeout("testNewChatlet('" + sorc + "','" + scID + "','" + afterSCletID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "','" + qnNumCols + "')", 100);
}

// --------------------------------------------------------------------------------
function insertChatlet(isAtBeginning, afterChatletID, sorc, scletID, chatletPosn, pseudonym, ownerImage, ownerName, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols) {
    var d = document.createElement('DIV');
    d.id = 'div-' + scletID;
    if (isIE) d.setAttribute('className', 'storylet');
    else d.setAttribute('class', 'storylet');

    var t = document.createElement('DIV');
    t.id = 'table-' + scletID;
    t.cellSpacing = '0';
    t.cellPadding = '0';
    t.style.width = '100%';

    var td1 = document.createElement('DIV');
    td1.id = 'td-' + scletID + '-0';
    td1.vAlign = 'top';
    t.appendChild(td1);

    var d8 = document.createElement('DIV');
    d8.style.display = 'inline-block';

    var p8 = document.createElement('P');
    p8.style.fontSize = '0.7em';

    d8.appendChild(p8);

    var i1 = document.createElement('IMG');
    i1.style.width = '28px';
    i1.style.height = '28px';
    i1.style.border = '0';
    i1.style.paddingRight = '2px';
    i1.src = ownerImage;
    d8.appendChild(i1);

    td1.appendChild(d8);

    var d22 = document.createElement('DIV');
    d22.style.display = 'inline-block';
    d22.innerHTML = ownerName + '<br>&nbsp;';
    td1.appendChild(d22);

    var d2 = document.createElement('DIV');
    d2.style.float = 'right';
    d2.style.cursor = 'pointer';
    d2.onclick = function(e) {
        doDeleteChat(sorc, scletID, men, imagesDir, isWriter);
    };
    td1.appendChild(d2);

    var i2 = document.createElement('IMG');
    i2.style.border = '0';
    i2.style.height = '35px';
    i2.style.width = '35px';
    i2.src = men + imagesDir + 'trash.png';
    d2.appendChild(i2);

    var td2 = document.createElement('DIV');
    td2.id = 'td-' + scletID + '-2';
    td2.vAlign = 'top';
    td2.style.paddingBottom = '10px';
    td2.style.width = '99%';
    td2.style.cursor = 'pointer';
    td2.onclick = function(e) {
        doEditChat('T', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    t.appendChild(td2);

    var p2 = document.createElement('P');
    td2.appendChild(p2);
    var s2 = document.createElement('SPAN');
    if (isIE) s2.setAttribute('className', 'editHere');
    else s2.setAttribute('class', 'editHere');
    s2.innerHTML = '&nbsp;Add Text Here&nbsp;';
    p2.appendChild(s2);

    var d3 = document.createElement('DIV');
    d3.id = 'td-' + scletID + '-1';
    t.appendChild(d3);

    var s1 = document.createElement('SPAN');
    s1.id = 'td-' + scletID + '-1a';
    if (isIE) s1.setAttribute('className', 'editHere');
    else s1.setAttribute('class', 'editHere');
    s1.onclick = function(e) {
        doInsertOptionsChat(sorc, scletID, pseudonym, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols);
    };
    s1.innerHTML = '&nbsp;Add Attachment Here&nbsp;';
    d3.appendChild(s1);

    var td3 = document.createElement('DIV');
    td3.id = 'td-' + scletID + '-3';
    td3.vAlign = 'top';
    t.appendChild(td3);

    d.appendChild(t);

    if (isAtBeginning === 'Y') {
        n = document.getElementById('insertChatlet');
        n.parentNode.insertBefore(d, n);
    } else {
        var n = document.getElementById('div-' + afterChatletID);
        if (n)
            insertAfter(n.parentNode, d, n);
    }
}

// --------------------------------------------------------------------------------
function editAChatlet(type, sorc, scletID, attachment, attachmentType, text, unm, men, imagesDir, fromCreate, isWriter) {
    text = decodeURIComponent(text);

    if (type === 'T') doEditTextChat(text, sorc, scletID, men, imagesDir, fromCreate, isWriter);
    else
    if (type === 'AI' || type === 'CI') doEditImageChat(type, attachment, sorc, scletID, unm, men, imagesDir, isWriter);
    else
    if (type === 'AU' || type === 'CU') doEditURLEnterChat(sorc, scletID, unm, men, imagesDir, isWriter, 'U');
    else
    if (type === 'AR' || type === 'CR') doEditURLEnterChat(sorc, scletID, unm, men, imagesDir, isWriter, 'R');
    else
    if (type === 'AW' || type === 'CW') doEditURLEnterChat(sorc, scletID, unm, men, imagesDir, isWriter, 'W');
    else
    if (type === 'AM' || type === 'CM') doEditMapEnterChat(sorc, scletID, unm, men, imagesDir, isWriter);
    else
    if (type === 'AD' || type === 'CD') doEditDocChat(sorc, scletID, unm, men, imagesDir, isWriter);
    else
    if (type === 'AQ' || type === 'CQ') doEditQnEnterChat(sorc, scletID, unm, men, imagesDir, isWriter);
}

function doEditImageChat(type, attachment, sorc, scletID, unm, men, imagesDir, isWriter) {
    var d = document.createElement('DIV');
    d.id = 'editDiv+' + scletID;
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');
    var t = document.createElement('TABLE');
    t.style.width = '100%';
    var tbody = document.createElement('TBODY');
    var tr = document.createElement('TR');

    if (isWriter) {
        var td1 = document.createElement('TD');
        var a1 = document.createElement('A');
        a1.innerHTML = 'Library';
        a1.href = 'javascript:doEditImageLibraryChat(\'' + sorc + '\',\'' + scletID + '\',\'' + men + '\')';
        td1.appendChild(a1);
        tr.appendChild(a1);
    }

    if (navigator.camera) {
        var td4 = document.createElement('TD');
        var a4 = document.createElement('SPAN');
        a4.innerHTML = 'Camera';
        a4.onclick = function(e) {
            takePicture(e, sorc, scletID, unm, men, imagesDir, isWriter);
        };
        td4.appendChild(a4);
        tr.appendChild(td4);
    }

    var td2 = document.createElement('TD');
    var a2 = document.createElement('A');
    a2.innerHTML = 'Upload';
    a2.href = 'javascript:doEditImageUploadChat(\'' + sorc + '\',\'' + scletID + '\',\'' + unm + '\',\'' + men + '\',\'' + imagesDir + '\',\'' + isWriter + '\')';
    td2.appendChild(a2);

    var a3 = document.createElement('A');
    a3.style.cssFloat = 'right';
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeDoEditImageChat(\'' + sorc + '\',\'' + scletID + '\')';
    td2.appendChild(a3);
    tr.appendChild(td2);

    tbody.appendChild(tr);
    t.appendChild(tbody);
    d.appendChild(t);

    var n = document.getElementById('div-' + scletID);
    insertAfter(n.parentNode, d, n);
}

var dataLibINFO, dataLibHTML, dataLibSCRIPTS, dataLibReceived, resDataLib;

function doEditImageLibraryChat(sorc, scletID, men) {
    var dd = document.getElementById('editDiv+' + scletID);
    if (dd) dd.parentNode.removeChild(dd);

    var afterID = 'div-' + scletID;
    dataLibReceived = false;
    socketSend("GET", "/_1014/dataLib/E:" + scletID + "/" + afterID + "/" + sorc + ".call", men);
    resDataLib = testDoEditImageLibraryChat(afterID);
}

function testDoEditImageLibraryChat(id) {
    if (dataLibReceived) {
        clearTimeout(resDataLib);
        dataLibHTML = decodeURIComponent(dataLibHTML);
        updateScreen('OK' + id + ';uploadDiv;' + dataLibINFO, dataLibHTML, dataLibSCRIPTS);
    } else resDataLib = setTimeout('testDoEditImageLibraryChat("' + id + '")', 100);
}

function doEditImageUploadChat(sorc, scletID, unm, men, imagesDir, isWriter) {
    if (document.getElementById('uploadDiv') !== null) {
        alert('Cannot modify more than one at a time');
        return
    }

    var formID = Math.floor(Math.random() * 10000);
    var f = document.createElement('form');
    f.id = formID;
    f.name = formID;

    var d = document.createElement('div');
    d.id = 'uploadDiv';
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');

    var t = document.createElement('table');
    t.style.width = '100%';

    var tr = document.createElement('tr');
    var td = document.createElement('td');

    var i;
    i = document.createElement('input');
    i.type = 'file';
    i.id = 'userfile' + scletID;
    i.style.cursor = 'pointer';
    i.onchange = function(e) {
        doFilesUpload('I', sorc, scletID, unm, men, imagesDir, isWriter, '');
    };

    if (typeof device !== 'undefined' && device.platform.toLowerCase() === 'android' && device.version.indexOf('4.4') === 0) {
        i.onclick = function(e) {
            filechooser.open({}, function(uri) {
                display(uri.filepath);
            }, function() {
                alert('Unknown Error');
            });
        };

        display = function(file) {
            doFilesUploadAndroid44('I', file, sorc, scletID, unm, men, imagesDir, isWriter, '');
        };
    }

    td.appendChild(i);
    tr.appendChild(td);

    var td3 = document.createElement('td');
    td3.align = 'right';
    var p3 = document.createElement('p');
    var a3 = document.createElement('a');
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeDoEditImageChat(\'' + sorc + '\',\'' + scletID + '\')';
    p3.appendChild(a3);
    td3.appendChild(a3);
    tr.appendChild(td3);
    t.appendChild(tr);
    f.appendChild(t);
    d.appendChild(f);

    var dd = document.getElementById('editDiv+' + scletID);
    if (dd) dd.parentNode.replaceChild(d, dd);
}

function closeDoEditImageChat(sorc, scletID) {
    try {
        var d = document.getElementById('uploadDiv');
        d.parentNode.removeChild(d);
    } catch (Exception) {}

    var n = document.getElementById('editDiv+' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('optionEdit-' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('td-' + scletID + '-1a');
    if (n) n.style.visibility = 'visible';
}

function doEditURLEnterChat(sorc, scletID, unm, men, imagesDir, isWriter, attType) {
    if (document.getElementById('uploadDiv') !== null) {
        alert('Cannot modify more than one at a time');
        return
    }

    var d = document.createElement('div');
    d.id = 'uploadDiv';
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');

    var t = document.createElement('table');
    t.style.width = '100%';

    var tr = document.createElement('tr');
    var td = document.createElement('td');

    var p1 = document.createElement('P');
    if (attType === 'R')
        p1.innerHTML = 'Enter TrailID:';
    else
    if (attType === 'W')
        p1.innerHTML = 'Enter Tweet Text:';
    else
        p1.innerHTML = 'Enter URL:';
    var tr2 = document.createElement('TR');
    var td2 = document.createElement('TD');
    td2.appendChild(p1);
    tr2.appendChild(td2);

    var i = document.createElement('input');
    i.type = 'text';
    i.size = '60';
    i.maxlength = '2000';
    i.style.width = '100%';
    i.id = 'newURL-' + scletID;
    td.appendChild(i);
    tr.appendChild(td);

    var tr1 = document.createElement('tr');
    var td1 = document.createElement('td');
    var a = document.createElement('A');
    a.href = 'javascript:updateURLInChatlet(\'' + sorc + '\',\'' + scletID + '\',\'' + men + '\',\'' + imagesDir + '\',\'' + isWriter + '\',\'' + attType + '\')';
    a.innerHTML = 'Update';
    td1.appendChild(a);
    tr1.appendChild(td1);

    var td3 = document.createElement('td');
    td3.align = 'right';
    var p3 = document.createElement('p');
    var a3 = document.createElement('a');
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeDoEditURLChat(\'' + sorc + '\',\'' + scletID + '\')';
    p3.appendChild(a3);
    td3.appendChild(p3);
    tr2.appendChild(td3);

    t.appendChild(tr2);
    t.appendChild(tr);
    t.appendChild(tr1);
    d.appendChild(t);

    var dd = document.getElementById('div-' + scletID);
    if (dd) insertAfter(dd.parentNode, d, dd);
}

// --------------------------------------------------------------------------------
function updateURLInChatlet(sorc, scletID, men, imagesDir, isWriter, attType) {
    var d = document.getElementById('newURL-' + scletID);
    if (!d) return;

    var url = d.value;
    if (url.length == 0) return;
    url = doSanitise(url);
    dataLibReceived = false;

    socketSend("GET", "/_1014e/dataLib/" + sorc + "/" + scletID + "/" + attType + "/" + url + ".call", men);

    drtout = testUpdateURLInChatlet(sorc, scletID, url, men, imagesDir, isWriter);
}

// --------------------------------------------------------------------------------
function testUpdateURLInChatlet(sorc, scletID, url, men, imagesDir, isWriter) {
    if (dataLibReceived) {
        clearTimeout(drtout);

        if (!dataLibINFO.startsWith("ERR")) {
            url = deSanitise(url);

            if (url.substring(0, 7) !== 'http://' && url.substring(0, 8) !== 'https://') url = 'http://' + url;

            var x = 0;
            if (url.startsWith("http://"))
                x = 7;
            else
            if (url.startsWith("https://"))
                x = 8;

            var a, len = url.length;

            if (url.indexOf("youtube.") !== -1 || url.indexOf("youtu.be") !== -1) {
                a = document.createElement('A');
                if (sorc === 'S')
                    a.href = "javascript:openURLYT1002('" + url + "')";
                else a.href = "javascript:openURLYT3505('" + url + "')";
                var i = document.createElement('IMG');
                //i.id='chatURL3014-'+scletID;
                i.src = men + imagesDir + 'youtubeplay.png';
                i.border = '0';
                a.appendChild(i);
            } else {
                var linkURL = "";
                while (x < len && url.charAt(x) !== '/')
                    linkURL += url.charAt(x++);
                a = document.createElement('A');
                if (sorc === 'S')
                    a.href = "javascript:openURL1002('" + url + "')";
                else a.href = "javascript:openURL3505('" + url + "')";
                a.innerHTML = 'Link to: ' + linkURL;
            }

            var d = document.createElement('DIV');
            d.id = 'chatURL3014-' + scletID;
            d.appendChild(a);

            var n = document.getElementById('chatURL3014-' + scletID);
            if (n) n.parentNode.replaceChild(d, n);
            else {
                var td = document.createElement('TD');
                td.id = 'td-' + scletID + '-1';
                td.appendChild(d);
                var td0 = document.getElementById('td-' + scletID + '-3');
                if (td0) insertAfter(td0.parentNode, td, td0);

                var td1 = document.createElement('TD');
                td1.id = 'chatURL3014-' + scletID + '-1';
                td1.style.verticalAlign = 'top';
                var i = document.createElement('IMG');
                i.style.cursor = 'pointer';
                i.onclick = function(e) {
                    doDeleteAttachment('Link', 'chatURL3014', sorc, scletID, men, imagesDir, isWriter);
                };
                i.src = men + imagesDir + 'terminated.png';
                i.style.border = '0';
                i.style.paddingLeft = '20px';
                td1.appendChild(i);
                if (td) insertAfter(td.parentNode, td1, td);
            }

            var dd = document.getElementById('td-' + scletID + '-1a');
            if (dd) dd.parentNode.removeChild(dd);

            closeDoEditURLChat(sorc, scletID);
            closeAllDoEditsChat(sorc, scletID);
        }
    } else drtout = setTimeout("testUpdateURLInChatlet('" + sorc + "','" + scletID + "','" + url + "','" + men + "','" + imagesDir + "','" + isWriter + "')", 100);
}

function closeDoEditURLChat(sorc, scletID) {
    try {
        var d = document.getElementById('uploadDiv');
        d.parentNode.removeChild(d);
    } catch (Exception) {}

    var n = document.getElementById('editDiv+' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('optionEdit-' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('td-' + scletID + '-1a');
    if (n) n.style.visibility = 'visible';
}

function doEditQnEnterChat(sorc, scletID, unm, men, imagesDir, isWriter) {
    if (document.getElementById('uploadDiv') !== null) {
        alert('Cannot modify more than one at a time');
        return;
    }

    var d = document.createElement('div');
    d.id = 'uploadDiv';
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');

    var t = document.createElement('table');
    t.style.width = '100%';

    var tr = document.createElement('tr');
    var td = document.createElement('td');
    td.colSpan = '2';

    var p1 = document.createElement('P');
    p1.innerHTML = 'Enter Question number:';
    var tr2 = document.createElement('TR');
    var td2 = document.createElement('TD');
    td2.appendChild(p1);
    tr2.appendChild(td2);

    var i = document.createElement('input');
    i.type = 'text';
    i.size = '6';
    i.maxlength = '10';
    i.id = 'newQn-' + scletID;
    td.appendChild(i);
    tr.appendChild(td);

    var tr1 = document.createElement('tr');
    var td1 = document.createElement('td');
    var p2 = document.createElement('p');
    var a = document.createElement('A');
    a.href = 'javascript:updateQnInChatlet(\'' + sorc + '\',\'' + scletID + '\',\'' + men + '\',\'' + imagesDir + '\',\'' + isWriter + '\')';
    a.innerHTML = 'Update';
    p2.appendChild(a);
    td1.appendChild(p2);
    tr1.appendChild(td1);

    var td3 = document.createElement('td');
    td3.align = 'right';
    var p3 = document.createElement('p');
    var a3 = document.createElement('a');
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeDoEditQnChat(\'' + sorc + '\',\'' + scletID + '\')';
    p3.appendChild(a3);
    td3.appendChild(p3);
    tr2.appendChild(td3);

    t.appendChild(tr2);
    t.appendChild(tr);
    t.appendChild(tr1);
    d.appendChild(t);

    var dd = document.getElementById('div-' + scletID);
    if (dd) insertAfter(dd.parentNode, d, dd);
}

// --------------------------------------------------------------------------------
function updateQnInChatlet(sorc, scletID, men, imagesDir, isWriter) {
    var d = document.getElementById('newQn-' + scletID);
    if (!d) return;

    var qn = d.value;
    if (qn.length === 0) return;

    dataLibReceived = false;

    socketSend("GET", "/_1014e/dataLib/" + sorc + "/" + scletID + "/Q/" + qn + ".call", men);
    //fred
    drtout = testUpdateQnInChatlet(sorc, scletID, qn, men, imagesDir, isWriter);
}

// --------------------------------------------------------------------------------
function testUpdateQnInChatlet(sorc, scletID, qn, men, imagesDir, googleKey, isWriter) {
    if (dataLibReceived) {
        clearTimeout(drtout);

        if (!dataLibINFO.startsWith("ERR")) {
            var len = dataLibHTML.length,
                x = 0,
                googleKey = '';
            while (x < len) googleKey += dataLibHTML.charAt(x++);

            var d = document.createElement('DIV');
            d.id = 'chatQn3014-' + scletID;
            if (isIE) d.setAttribute('className', 'editsmap');
            else d.setAttribute('class', 'editsmap');

            var i = document.createElement('span');
            i.innerHTML = 'Question ' + qn;
            d.appendChild(i);
            var n = document.getElementById('chatQn3014-' + scletID);
            if (n) n.parentNode.replaceChild(d, n);
            else {
                var td = document.createElement('TD');
                td.id = 'td-' + scletID + '-1';
                td.appendChild(d);
                var td0 = document.getElementById('td-' + scletID + '-3');
                if (td0) insertAfter(td0.parentNode, td, td0);

                var td1 = document.createElement('TD');
                td1.id = 'chatQn3014-' + scletID + '-1';
                td1.style.verticalAlign = 'top';
                var i = document.createElement('IMG');
                i.style.cursor = 'pointer';
                i.onclick = function(e) {
                    doDeleteAttachment('Question', 'chatQn3014', sorc, scletID, men, imagesDir, isWriter);
                };
                i.src = men + imagesDir + 'terminated.png';
                i.style.border = '0';
                td1.appendChild(i);
                if (td) insertAfter(td.parentNode, td1, td);
            }

            var dd = document.getElementById('td-' + scletID + '-1a');
            if (dd) dd.parentNode.removeChild(dd);

            closeDoEditQnChat(sorc, scletID);
            closeAllDoEditsChat(sorc, scletID);
        }
    } else drtout = setTimeout("testUpdateQnInChatlet('" + sorc + "','" + scletID + "','" + qn + "','" + men + "','" + imagesDir + "')", 100);
}

function closeDoEditQnChat(sorc, scletID) {
    try {
        var d = document.getElementById('uploadDiv');
        d.parentNode.removeChild(d);
    } catch (Exception) {}

    var n = document.getElementById('editDiv+' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('optionEdit-' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('td-' + scletID + '-1a');
    if (n) n.style.visibility = 'visible';
}

function doEditMapEnterChat(sorc, scletID, unm, men, imagesDir, isWriter) {
    if (document.getElementById('uploadDiv') !== null) {
        alert('Cannot modify more than one at a time');
        return
    }

    var d = document.createElement('div');
    d.id = 'uploadDiv';
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');

    var t = document.createElement('table');
    t.style.width = '100%';

    var tr = document.createElement('tr');
    var td = document.createElement('td');
    td.colSpan = '2';

    var p1 = document.createElement('P');
    p1.innerHTML = 'Enter description of location:';
    var tr2 = document.createElement('TR');
    var td2 = document.createElement('TD');
    td2.appendChild(p1);
    tr2.appendChild(td2);

    var i = document.createElement('input');
    i.type = 'text';
    i.size = '60';
    i.maxlength = '200';
    i.id = 'newMap-' + scletID;
    td.appendChild(i);
    tr.appendChild(td);

    var tr1 = document.createElement('tr');
    var td1 = document.createElement('td');
    var p2 = document.createElement('p');
    var a = document.createElement('A');
    a.href = 'javascript:updateMapInChatlet(\'' + sorc + '\',\'' + scletID + '\',\'' + men + '\',\'' + imagesDir + '\',\'' + isWriter + '\')';
    a.innerHTML = 'Update';
    p2.appendChild(a);
    td1.appendChild(p2);
    tr1.appendChild(td1);

    var td3 = document.createElement('td');
    td3.align = 'right';
    var p3 = document.createElement('p');
    var a3 = document.createElement('a');
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeDoEditMapChat(\'' + sorc + '\',\'' + scletID + '\')';
    p3.appendChild(a3);
    td3.appendChild(p3);
    tr2.appendChild(td3);

    t.appendChild(tr2);
    t.appendChild(tr);
    t.appendChild(tr1);
    d.appendChild(t);

    var dd = document.getElementById('div-' + scletID);
    if (dd) insertAfter(dd.parentNode, d, dd);
}

// --------------------------------------------------------------------------------
function updateMapInChatlet(sorc, scletID, men, imagesDir, isWriter) {
    var d = document.getElementById('newMap-' + scletID);
    if (!d) return;

    var map = d.value;
    if (map.length === 0) return;

    dataLibReceived = false;

    socketSend("GET", "/_1014e/dataLib/" + sorc + "/" + scletID + "/M/" + map + ".call", men);

    drtout = testUpdateMapInChatlet(sorc, scletID, map, men, imagesDir, isWriter);
}

// --------------------------------------------------------------------------------
function testUpdateMapInChatlet(sorc, scletID, map, men, imagesDir, googleKey, isWriter) {
    if (dataLibReceived) {
        clearTimeout(drtout);

        if (!dataLibINFO.startsWith("ERR")) {
            var len = dataLibHTML.length,
                x = 0,
                googleKey = '';
            while (x < len) googleKey += dataLibHTML.charAt(x++);

            var d = document.createElement('DIV');
            d.id = 'chatMap3014-' + scletID;
            if (isIE) d.setAttribute('className', 'editsmap');
            else d.setAttribute('class', 'editsmap');

            var i = document.createElement('IFRAME');
            i.frameborder = '0';
            i.src = 'https://www.google.com/maps/embed/v1/place?key=' + googleKey + '&q=' + encodeURIComponent(map);
            d.appendChild(i);
            var n = document.getElementById('chatMap3014-' + scletID);
            if (n) n.parentNode.replaceChild(d, n);
            else {
                var td = document.createElement('TD');
                td.id = 'td-' + scletID + '-1';
                td.appendChild(d);
                var td0 = document.getElementById('td-' + scletID + '-3');
                if (td0) insertAfter(td0.parentNode, td, td0);

                var td1 = document.createElement('TD');
                td1.id = 'chatMap3014-' + scletID + '-1';
                td1.style.verticalAlign = 'top';
                var i = document.createElement('IMG');
                i.style.cursor = 'pointer';
                i.onclick = function(e) {
                    doDeleteAttachment('Map', 'chatMap3014', sorc, scletID, men, imagesDir, isWriter);
                };
                i.src = men + imagesDir + 'terminated.png';
                i.style.border = '0';
                td1.appendChild(i);
                if (td) insertAfter(td.parentNode, td1, td);
            }

            var dd = document.getElementById('td-' + scletID + '-1a');
            if (dd) dd.parentNode.removeChild(dd);

            closeDoEditMapChat(sorc, scletID);
            closeAllDoEditsChat(sorc, scletID);
        }
    } else drtout = setTimeout("testUpdateMapInChatlet('" + sorc + "','" + scletID + "','" + map + "','" + men + "','" + imagesDir + "')", 100);
}

function closeDoEditMapChat(sorc, scletID) {
    try {
        var d = document.getElementById('uploadDiv');
        d.parentNode.removeChild(d);
    } catch (Exception) {}

    var n = document.getElementById('editDiv+' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('optionEdit-' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('td-' + scletID + '-1a');
    if (n) n.style.visibility = 'visible';
}

function doEditDocChat(sorc, scletID, unm, men, imagesDir, isWriter) {
    var d = document.createElement('DIV');
    d.id = 'editDiv+' + scletID;
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');
    var t = document.createElement('TABLE');
    t.style.width = '100%';
    var tbody = document.createElement('TBODY');
    var tr = document.createElement('TR');

    if (isWriter) {
        var td1 = document.createElement('TD');
        var a1 = document.createElement('A');
        a1.innerHTML = 'Library';
        a1.href = 'javascript:doEditDocLibraryChat(\'' + sorc + '\',\'' + scletID + '\',\'' + men + '\')';
        td1.appendChild(a1);
        tr.appendChild(td1);
    }

    var td2 = document.createElement('TD');
    var a2 = document.createElement('A');
    a2.innerHTML = 'Upload';
    a2.href = 'javascript:doEditDocUploadChat(\'' + sorc + '\',\'' + scletID + '\',\'' + unm + '\',\'' + men + '\',\'' + imagesDir + '\',\'' + isWriter + '\')';
    td2.appendChild(a2);

    var a3 = document.createElement('A');
    a3.style.cssFloat = 'right';
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeDoEditDocChat(\'' + sorc + '\',\'' + scletID + '\')';
    td2.appendChild(a3);

    tr.appendChild(td2);

    tbody.appendChild(tr);
    t.appendChild(tbody);
    d.appendChild(t);

    var n = document.getElementById('div-' + scletID);
    insertAfter(n.parentNode, d, n);
}

function doEditDocLibraryChat(sorc, scletID, men) {
    var dd = document.getElementById('editDiv+' + scletID);
    if (dd) dd.parentNode.removeChild(dd);

    var afterID = 'div-' + scletID;
    dataLibReceived = false;
    socketSend("GET", "/_1014/dataLib/E:" + scletID + "/" + afterID + "/" + sorc + ".call", men);
    resDataLib = testDoEditDocLibraryChat(afterID);
}

function testDoEditDocLibraryChat(id) {
    if (dataLibReceived) {
        clearTimeout(resDataLib);
        dataLibHTML = decodeURIComponent(dataLibHTML);
        updateScreen('OK' + id + ';uploadDiv;' + dataLibINFO, dataLibHTML, dataLibSCRIPTS);
    } else resDataLib = setTimeout('testDoEditDocLibraryChat("' + id + '")', 100);
}

function doEditDocUploadChat(sorc, scletID, unm, men, imagesDir, isWriter) {
    if (document.getElementById('uploadDiv') !== null) {
        alert('Cannot modify more than one at a time');
        return
    }

    var formID = Math.floor(Math.random() * 10000);
    var f = document.createElement('form');
    f.id = formID;
    f.name = formID;

    var d = document.createElement('div');
    d.id = 'uploadDiv';
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');

    var t = document.createElement('table');
    t.style.width = '100%';

    var tr = document.createElement('tr');
    var td = document.createElement('td');

    var i = document.createElement('input');
    i.type = 'file';
    i.id = 'userfile' + scletID;
    i.style.cursor = 'pointer';
    i.onchange = function(e) {
        doFilesUpload('D', sorc, scletID, unm, men, imagesDir, isWriter, '');
    };

    if (typeof device !== 'undefined' && device.platform.toLowerCase() === 'android' && device.version.indexOf('4.4') === 0) {
        i.onclick = function(e) {
            filechooser.open({}, function(uri) {
                display(uri.filepath);
            }, function() {
                alert('Unknown Error');
            });
        };

        display = function(file) {
            doFilesUploadAndroid44('D', file, sorc, scletID, unm, men, imagesDir, isWriter, '');
        };
    }

    td.appendChild(i);
    tr.appendChild(td);

    var td3 = document.createElement('td');
    td3.align = 'right';
    var p3 = document.createElement('p');
    var a3 = document.createElement('a');
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeDoEditDocChat(\'' + sorc + '\',\'' + scletID + '\')';
    p3.appendChild(a3);
    td3.appendChild(a3);
    tr.appendChild(td3);

    t.appendChild(tr);
    f.appendChild(t);
    d.appendChild(f);

    var dd = document.getElementById('editDiv+' + scletID);
    if (dd) dd.parentNode.replaceChild(d, dd);
}

function closeDoEditDocChat(sorc, scletID) {
    try {
        var d = document.getElementById('uploadDiv');
        d.parentNode.removeChild(d);
    } catch (Exception) {}

    var n = document.getElementById('editDiv+' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('optionEdit-' + scletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('td-' + scletID + '-1a');
    if (n) n.style.visibility = 'visible';
}

// --------------------------------------------------------------------------------
function refetchDocChat(sorc, scletID, men, imagesDir, isWriter) {
    data1051Received = false;

    socketSend("GET", "/_1051c/data1051/" + sorc + "/" + scletID + ".call", men);

    drtout = testRefetchDocChat(sorc, scletID, men, imagesDir, isWriter);
}

// --------------------------------------------------------------------------------
function testRefetchDocChat(sorc, scletID, men, imagesDir, isWriter) {
    if (data1051Received) {
        clearTimeout(drtout);

        if (!data1051INFO.startsWith("ERR")) {
            var len = data1051HTML.length,
                x = 0,
                mimeName = '',
                attachment = '';
            while (x < len && data1051HTML.charAt(x) !== '`') mimeName += data1051HTML.charAt(x++);
            ++x;
            while (x < len) attachment += data1051HTML.charAt(x++);

            var d = document.createElement('DIV');
            d.id = 'chatDoc3014-' + scletID;
            var i = document.createElement('IMG');
            if (isIE) d.setAttribute('className', 'editsmap');
            else d.setAttribute('class', 'editsmap');

            var a = document.createElement('A');
            i.onclick = function(e) {
                download3014(attachment);
            };
            i.src = men + imagesDir + mimeName;
            i.style.border = '0';
            a.appendChild(i);
            d.appendChild(a);
            var n = document.getElementById('chatDoc3014-' + scletID);
            if (n) n.parentNode.replaceChild(d, n);
            else {
                var td = document.createElement('TD');
                td.id = 'td-' + scletID + '-1';
                td.appendChild(d);
                var td0 = document.getElementById('td-' + scletID + '-3');
                if (td0) insertAfter(td0.parentNode, td, td0);

                var td1 = document.createElement('TD');
                td1.id = 'chatDoc3014-' + scletID + '-1';
                td1.style.verticalAlign = 'top';
                var i = document.createElement('IMG');
                i.style.cursor = 'pointer';
                i.onclick = function(e) {
                    doDeleteAttachment('Document', 'chatDoc3014', sorc, scletID, men, imagesDir, isWriter);
                };
                i.src = men + imagesDir + 'terminated.png';
                i.style.border = '0';
                i.style.paddingLeft = '20px';
                td1.appendChild(i);
                if (td) insertAfter(td.parentNode, td1, td);
            }

            var dd = document.getElementById('td-' + scletID + '-1a');
            if (dd) dd.parentNode.removeChild(dd);

            closeDoEditDocChat(sorc, scletID);
            closeAllDoEditsChat(sorc, scletID);
        }
    } else drtout = setTimeout("testRefetchDocChat('" + sorc + "','" + scletID + "','" + men + "','" + imagesDir + "','" + isWriter + "')", 100);
}

function doEditTextChat(text, sorc, scletID, men, imagesDir, fromCreate, isWriter) {
        var td2 = document.createElement('DIV');
        td2.id = 'td-' + scletID + '-2';
        td2.style.width = '90%';
        var iTextArea = document.createElement('textarea');
        iTextArea.id = 'inputText' + scletID;
        if (isIE) iTextArea.setAttribute('className', 'iTextArea');
        else iTextArea.setAttribute('class', 'iTextArea');
        iTextArea.style.height = "200px";
        iTextArea.style.width = '100%';
        iTextArea.onclick = function(e) {
            stopPropagation(e);
        };
        text = deSanitise(text);
        iTextArea.style.visibility = 'visible';
        iTextArea.style.zIndex = '10';
        iTextArea.value = replaceBackticks(text);

        td2.appendChild(iTextArea);

        detectPaste(iTextArea, function(pasteInfo) {
            var val = iTextArea.value;
            iTextArea.value = val.slice(0, pasteInfo.start) + pasteInfo.replacedText + val.slice(pasteInfo.end);
            handlePaste(pasteInfo.text, pasteInfo.replacedText, scletID);
        });

        detectCut(iTextArea, function(cutInfo) {
            var val = iTextArea.value;
            iTextArea.value = val.slice(0, cutInfo.start) + cutInfo.replacedText + val.slice(cutInfo.end);
            handleCut(cutInfo.replacedText, scletID);
        });
        wavePanelCARETPOSN = 0;
        iTextArea.onclick = function(e) {
            wavePanelCARETPOSN = getCaretPosn(this);
        };

        var tdExisting = document.getElementById('td-' + scletID + '-2');
        tdExisting.parentNode.replaceChild(td2, tdExisting);

        var td3 = document.createElement('DIV');
        td3.id = 'td-' + scletID + '-3';
        var s3 = document.createElement('span');
        td3.appendChild(s3);
        s3.id = 'optionClose-' + scletID;
        s3.style.visibility = 'visible';
        var a3 = document.createElement('a');
        a3.onclick = function(e) {
            closeEditChat(sorc, scletID, men, imagesDir, fromCreate, isWriter);
        };
        a3.onmouseover = function(e) {
            this.style.cursor = 'pointer';
        };

        var img3 = document.createElement('img');
        img3.style.border = '0';
        img3.src = men + imagesDir + "closewavelet.png";
        a3.appendChild(img3);
        s3.appendChild(a3);

        tdExisting = document.getElementById('td-' + scletID + '-3');
        tdExisting.parentNode.replaceChild(td3, tdExisting);
        iTextArea.focus();
        var d = document.getElementById('optionEdit-' + scletID + '-1');
        if (d) d.style.visibility = 'hidden';
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
function handleCut(text, id) {
    if (text.length === 0) return;
    var ctrl = document.getElementById('inputText' + id);
    var posn = wavePanelCARETPOSN;

    var v = ctrl.value;
    ctrl.value = v.substring(0, posn) + v.substring(text.length + posn);

    setCaretPosn(ctrl, posn);

    //  wavePanelObj.mutation('DST',posn,text);
}

// --------------------------------------------------------------------------------
function handlePaste(text, replacedText, id) {
        var ctrl = document.getElementById('inputText' + id);
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
function setCaretPosnn(ctrl, pos) {
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

function doInsertOptionsChat(sorc, scletID, pseudonym, men, imagesDir, isWriter, callerServlet, callerArgs, qnNumCols) {
    var d = document.getElementById('td-' + scletID + '-1a');
    if (d) d.style.visibility = 'hidden';

    d = document.getElementById('td-' + scletID + '-1');

    var d2 = document.createElement('DIV');
    d2.id = 'optionEdit-' + scletID;
    if (isIE) d2.setAttribute('className', 'insertOptions');
    else d2.setAttribute('class', 'insertOptions');

    var s1 = document.createElement('SPAN');
    s1.style.paddingRight = '4px';
    s1.onclick = function(e) {
        doEditChat('AI', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    var i1 = document.createElement('IMG');
    i1.src = men + imagesDir + 'image.png';
    i1.style.border = '0';
    i1.style.cursor = 'pointer';
    s1.appendChild(i1);
    d2.appendChild(s1);

    if (isWriter === 'Y' && sorc !== 'Q' && sorc !== 'T' && sorc !== 'P') {
        var s8 = document.createElement('SPAN');
        s8.style.paddingRight = '4px';
        s8.onclick = function(e) {
            doEditAlbum(scletID, pseudonym, men, imagesDir);
        };
        var i8 = document.createElement('IMG');
        i8.src = men + imagesDir + 'album.png';
        i8.style.border = '0';
        i8.style.cursor = 'pointer';
        s8.appendChild(i8);
        d2.appendChild(s8);
    }

    if (isWriter === 'Y' && sorc === 'Q') {
        s8 = document.createElement('SPAN');
        s8.style.paddingRight = '4px';
        s8.onclick = function(e) {
            doEditTable(scletID, men, imagesDir, callerServlet, callerArgs, qnNumCols);
        };
        i8 = document.createElement('IMG');
        i8.src = men + imagesDir + 'table.png';
        i8.style.border = '0';
        i8.style.cursor = 'pointer';
        s8.appendChild(i8);
        d2.appendChild(s8);
    }

    var s6a = document.createElement('SPAN');
    s6a.style.paddingRight = '4px';
    s6a.onclick = function(e) {
        doEditChat('AQ', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    var i6a = document.createElement('IMG');
    i6a.src = men + imagesDir + 'qn.png';
    i6a.style.border = '0';
    i6a.style.cursor = 'pointer';
    s6a.appendChild(i6a);
    d2.appendChild(s6a);

    var s6 = document.createElement('SPAN');
    s6.style.paddingRight = '4px';
    s6.onclick = function(e) {
        doEditChat('AM', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    var i6 = document.createElement('IMG');
    i6.src = men + imagesDir + 'map.png';
    i6.style.border = '0';
    i6.style.cursor = 'pointer';
    s6.appendChild(i6);
    d2.appendChild(s6);

    var s3 = document.createElement('SPAN');
    s3.onclick = function(e) {
        doEditChat('AU', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    var i3 = document.createElement('IMG');
    i3.src = men + imagesDir + 'weblink.png';
    i3.style.border = '0';
    i3.style.cursor = 'pointer';
    s3.appendChild(i3);
    d2.appendChild(s3);

    var s4 = document.createElement('SPAN');
    s4.onclick = function(e) {
        doEditChat('AR', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    var i4 = document.createElement('IMG');
    i4.src = men + imagesDir + 'trail.png';
    i4.style.border = '0';
    i4.style.cursor = 'pointer';
    s4.appendChild(i4);
    d2.appendChild(s4);

    var s4a = document.createElement('SPAN');
    s4a.onclick = function(e) {
        doEditChat('AW', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    var i4a = document.createElement('IMG');
    i4a.src = men + imagesDir + 'tweet.png';
    i4a.style.border = '0';
    i4a.style.cursor = 'pointer';
    s4a.appendChild(i4a);
    d2.appendChild(s4a);

    var s5 = document.createElement('SPAN');
    s5.onclick = function(e) {
        doEditChat('AD', sorc, scletID, pseudonym, men, imagesDir, 'N', isWriter);
    };
    var i5 = document.createElement('IMG');
    i5.src = men + imagesDir + 'doc.png';
    i5.style.border = '0';
    i5.style.cursor = 'pointer';
    s5.appendChild(i5);
    d2.appendChild(s5);

    var s7 = document.createElement('SPAN');
    s7.style.verticalAlign = 'top';
    s7.onclick = function(e) {
        var d = document.getElementById('td-' + scletID + '-1a');
        if (d) d.style.visibility = 'visible';
        closeAllDoEditsChat(sorc, scletID);
    };
    var i7 = document.createElement('IMG');
    i7.src = men + imagesDir + 'terminated.png';
    i7.style.border = '0';
    i7.style.cursor = 'pointer';
    s7.appendChild(i7);
    d2.appendChild(s7);

    d.appendChild(d2);
}

function closeAllDoEditsChat(sorc, scletID) {
    var d = document.getElementById('optionEdit-' + scletID);
    if (d) d.parentNode.removeChild(d);
}

// ----------------------------------------------------------------------------
var uploadDone, uploadTO;

function doFilesUpload(which, sorc, scletID, unm, men, imagesDir, isWriter, dir) {
    var dd = document.getElementById('uploadDiv');
    if (!dd) return;

    var fi = document.getElementById('userfile' + scletID);

    var t = document.createElement('TABLE');
    t.style.width = '100%';
    var tbody = document.createElement('TBODY');
    t.appendChild(tbody);

    dd.firstChild.style.visibility = 'hidden';
    dd.appendChild(t);

    var x, n, i, tr, td, sp, fName;

    for (i = 0; i < fi.files.length; ++i) {
        fName = fi.files[i].name;
        var x = fName.length - 1;
        while (x >= 0 && fName.charAt(x) !== '/')
            --x;
        n = fName.substring(x + 1);

        tr = document.createElement('TR');
        tr.id = 'fileU' + i;
        td = document.createElement('TD');
        td.width = '90%';
        sp = document.createElement('SPAN');
        sp.innerHTML = n;
        td.appendChild(sp);
        tr.appendChild(td);
        tbody.appendChild(tr);
    }

    submitUpload(which, fi, sorc, scletID, unm, men, imagesDir, isWriter, dir);
}

function submitUpload(which, fi, sorc, scletID, unm, men, imagesDir, isWriter, dir) {
    uploadDone = 'N';

    doOneUpload(which, 0, fi.files[0], fi.files[0].name, sorc, scletID, men, imagesDir, unm, dir);

    uploadWait(which, 0, fi.files.length, sorc, scletID, men, imagesDir, unm, dir, isWriter);
}

function doOneUpload(which, count, file, fileName, sorc, scletID, men, imagesDir, unm, dir) {
    var tr = document.getElementById('fileU' + count);
    if (!tr) return;

    var td = document.createElement('TD');
    tr.appendChild(td);
    var pro = document.createElement('PROGRESS');
    pro.id = 'progressBar' + count;
    pro.max = '100';
    pro.value = '0';
    pro.style.visibility = 'hidden';
    td.appendChild(pro);

    var pro2 = document.createElement('IMG');
    pro2.id = 'progressGIF' + count;
    pro2.style.border = '0';
    pro2.src = men + imagesDir + 'ajax-loader.gif';
    td.appendChild(pro2);

    var s = document.createElement('SPAN');
    pro.appendChild(s);
    var xhr = new XMLHttpRequest();

    var data = new FormData();
    data.append(fileName, file);

    xhr.upload.addEventListener("progress", onprogress, false);
    xhr.addEventListener('progress', onprogress, false);

    xhr.upload.onprogress = function(e) {
        var p = document.getElementById('progressBar' + count);
        var p2 = document.getElementById('progressGIF' + count);
        if (p2) {
            p2.parentNode.removeChild(p2);
            p2 = null;
            p.style.visibility = 'visible';
        };
        var per = (e.loaded / e.total) * 100;
        p.value = per;
        s.innerHTML = per;
    };

    xhr.onload = function() {
        if (xhr.status === 200) {
            var td1 = document.createElement('TD');
            td1.textAlign = 'right';
            td1.innerHTML = 'Done';
            td.parentNode.replaceChild(td1, td);
            uploadDone = 'Y';
        } else {
            var td1 = document.createElement('TD');
            td1.innerHTML = 'Error';
            td.parentNode.replaceChild(td1, td);
            uploadDone = 'E';
        }
    };

    xhr.onerror = function() {
        var td1 = document.createElement('TD');
        td1.innerHTML = 'Error';
        td.parentNode.replaceChild(td1, td);
        uploadDone = 'E';
    };

    pro.value = 0;

    var option;
    if (which === 'I') {
        if (sorc === 'S') option = 'S';
        else option = 'C';
    } else
    if (which === 'D') {
        if (sorc === 'S') option = 'd';
        else option = 'D';
    }

    if (which === 'A')
        xhr.open("POST", men + '/___Upload/A/' + unm + '/' + dir, true);
    else xhr.open("POST", men + '/___Upload/' + option + '/' + scletID + '/' + unm, true);

    xhr.setRequestHeader("Accept", "text/xml");
    if (isIE) xhr.setRequestHeader("Connection", "close"); // for IE

    xhr.send(data);
}

function uploadWait(which, count, numFiles, sorc, scletID, men, imagesDir, unm, dir, isWriter) {
    if (uploadDone !== 'N') {
        clearTimeout(uploadTO);
        ++count;
        if (count < numFiles) {
            var fi = document.getElementById('userfile' + scletID);
            uploadDone = 'N';
            doOneUpload(which, count, fi.files[count], fi.files[count].name, sorc, scletID, men, imagesDir, unm, dir);

            uploadTO = setTimeout("uploadWait('" + which + "','" + count + "','" + numFiles + "','" + sorc + "','" + scletID + "','" + men + "','" + imagesDir + "','" + unm + "','" + dir + "','" + isWriter + "')", 1000);
        } else {
            if (which === 'C')
                refetchImageChat(sorc, scletID, men, imagesDir, isWriter);
            else
            if (which === 'D')
                refetchDocChat(sorc, scletID, men, imagesDir, isWriter);
        }
    } else uploadTO = setTimeout("uploadWait('" + which + "','" + count + "','" + numFiles + "','" + sorc + "','" + scletID + "','" + men + "','" + imagesDir + "','" + unm + "','" + dir + "','" + isWriter + "')", 1000);
}

cleanup = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, "Camera", "cleanup", []);
};

function takePicture(event, sorc, scletID, unm, men, imagesDir, isWriter) {
    event.preventDefault();

    if (!navigator.camera) return;

    navigator.camera.getPicture(
        function(fileURI) {
            doFilesUploadAndroid44('I', fileURI, sorc, scletID, unm, men, imagesDir, isWriter, '');
        },
        function() {

        }, {
            quality: 75,
            destinationType: 1,
            sourceType: 1,
            targetWidth: -1,
            targetHeight: -1,
            encodingType: 1,
            mediaType: 0,
            allowEdit: false,
            correctOrientation: true,
            saveToPhotoAlbum: false,
            popoverOptions: null,
            cameraDirection: 1
        }
    );
}

function doFilesUploadAndroid44(which, filePath, sorc, scletID, unm, men, imagesDir, isWriter, dir) {
    var dd = document.createElement('div');
    dd.id = 'uploadDiv';
    if (isIE) dd.setAttribute('className', 'edits');
    else dd.setAttribute('class', 'edits');

    var t = document.createElement('TABLE');
    t.style.width = '100%';
    var tbody = document.createElement('TBODY');
    t.appendChild(tbody);

    dd.appendChild(t);

    var n = filePath.substr(filePath.lastIndexOf('/') + 1);

    var tr = document.createElement('TR');
    var td = document.createElement('TD');
    td.width = '90%';
    var sp = document.createElement('SPAN');
    sp.innerHTML = n;
    td.appendChild(sp);
    var sp1 = document.createElement('SPAN');
    sp1.id = 'fileU';
    sp1.style.visibility = 'hidden';
    sp1.style.paddingLeft = '20px';
    sp1.innerHTML = '#';
    td.appendChild(sp1);
    var pro2 = document.createElement('IMG');
    pro2.id = 'progressGIF';
    pro2.style.visibility = 'hidden';
    pro2.style.border = '0';
    pro2.style.verticalAlign = 'middle';
    pro2.src = men + imagesDir + 'ajax-loader.gif';
    td.appendChild(pro2);

    tr.appendChild(td);
    tbody.appendChild(tr);

    var ddd = document.getElementById('editDiv+' + scletID);
    if (ddd) ddd.parentNode.replaceChild(dd, ddd);
    else {
        ddd = document.getElementById('uploadDiv');
        if (ddd) ddd.parentNode.replaceChild(dd, ddd);
    }

    function win(r) {
        var pro2 = document.getElementById('progressGIF');
        if (pro2) pro2.style.visibility = 'hidden';

        var s = document.getElementById('fileU');
        if (s) s.innerHTML = 'processing...';
        s.style.visibility = 'visible';

        refetchImageChat(sorc, scletID, men, imagesDir, isWriter);
    }

    function fail(error) {
        alert("Upload Failed");
    }

    var option;
    if (which === 'I') {
        if (sorc === 'S') option = 'S';
        else option = 'C';
    } else
    if (which === 'D') {
        if (sorc === 'S') option = 'd';
        else option = 'D';
    }

    var uri;

    if (which === 'A')
        uri = encodeURI(men + '/___Upload/A/' + unm + '/' + dir);
    else uri = encodeURI(men + '/___Upload/' + option + '/' + scletID + '/' + unm);

    var options = new FileUploadOptions();
    options.fileKey = "file";
    options.fileName = filePath.substr(filePath.lastIndexOf('/') + 1);

    var ft = new FileTransfer();

    ft.onprogress = function(progressEvent) {
        var s = document.getElementById('fileU');

        if (progressEvent.lengthComputable) {
            var s = document.getElementById('fileU');
            s.style.visibility = 'visible';
            var pc = ((progressEvent.loaded / progressEvent.total) * 100).toFixed();
            if (pc === '100')
                s.innerHTML = 'processing...';
            else s.innerHTML = ((progressEvent.loaded / progressEvent.total) * 100).toFixed() + '%';
        } else {
            var pro2 = document.getElementById('progressGIF');
            if (pro2) pro2.style.visibility = 'visible';
        }
    };

    ft.upload(filePath, uri, win, fail, options);
}

function doEditAlbum(storyletID, unm, men, imagesDir) {
    var d = document.createElement('DIV');
    d.id = 'editDiv+' + storyletID;
    if (isIE) d.setAttribute('className', 'edits');
    else d.setAttribute('class', 'edits');
    var t = document.createElement('TABLE');
    t.style.width = '100%';
    var tbody = document.createElement('TBODY');
    var tr = document.createElement('TR');
    var td1 = document.createElement('TD');
    var a1 = document.createElement('A');
    a1.innerHTML = 'Select from Library';
    a1.href = 'javascript:doEditAlbumLibrary(\'' + storyletID + '\',\'' + men + '\')';

    var td2 = document.createElement('TD');

    var a3 = document.createElement('A');
    a3.style.cssFloat = 'right';
    a3.innerHTML = 'Close';
    a3.href = 'javascript:closeDoEditAlbum(\'' + storyletID + '\')';

    d.appendChild(t);
    t.appendChild(tbody);
    tbody.appendChild(tr);
    tr.appendChild(td1);
    td1.appendChild(a1);
    tr.appendChild(td2);
    td2.appendChild(a3);

    var n = document.getElementById('div-' + storyletID);
    insertAfter(n.parentNode, d, n);
}

function doEditAlbumLibrary(storyletID, men) {
    var dd = document.getElementById('editDiv+' + storyletID);
    if (dd) dd.parentNode.removeChild(dd);

    var afterID = 'div-' + storyletID;
    dataLibReceived = false;
    socketSend("GET", "/_1014/dataLib/A:" + storyletID + "/" + afterID + "/S.call", men);
    resDataLib = testDoEditAlbumLibrary(afterID);
}

function testDoEditAlbumLibrary(id) {
    if (dataLibReceived) {
        clearTimeout(resDataLib);
        dataLibHTML = decodeURIComponent(dataLibHTML);
        var n = document.getElementById(id);
        var d = document.createElement('DIV');
        d.id = 'uploadDiv';
        if (isIE) d.setAttribute('className', 'edits');
        else d.setAttribute('class', 'edits');
        insertAfter(n.parentNode, d, n);
        dataLibINFO = 'OKuploadDiv;uploadDiv;';
        updateScreen(dataLibINFO, dataLibHTML, dataLibSCRIPTS);
    } else resDataLib = setTimeout('testDoEditAlbumLibrary("' + id + '")', 100);
}

function closeDoEditAlbum(storyletID) {
    try {
        var d = document.getElementById('uploadDiv');
        d.parentNode.removeChild(d);
    } catch (Exception) {}

    var n = document.getElementById('editDiv+' + storyletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('optionEdit-' + storyletID);
    if (n) n.parentNode.removeChild(n);

    n = document.getElementById('td-' + storyletID + '-1a');
    if (n) n.style.visibility = 'visible';
}

// -----------------------------------------------------------------------------
function doEditTable(questionID, men, imagesDir, callerServlet, callerArgs, numCols) {
    var t = document.createElement('TABLE');
    if (isIE) t.setAttribute('className', 'projectcell');
    else t.setAttribute('class', 'projectcell');
    var tbody = document.createElement('TBODY');
    tbody.id = 'qbody-1003-' + questionID;
    var tr = document.createElement('TR');
    t.appendChild(tbody);
    tbody.appendChild(tr);

    var d1 = document.getElementById('td-' + questionID + '-1a');
    if (d1) d1.style.visibility = 'hidden';

    d1 = document.getElementById('optionEdit-' + questionID);
    if (d1) d1.parentNode.removeChild(d1);

    var n = document.getElementById('td-' + questionID + '-8');
    n.parentNode.replaceChild(t, n);

    var d4 = document.createElement('DIV');
    d4.id = 'qbody-1003-' + questionID + '+';
    if (isIE) d4.setAttribute('className', 'projectcell');
    else d4.setAttribute('class', 'projectcell');
    d4.style.padding = '5px';
    var d2 = document.createElement('DIV');
    d2.style.display = 'inline-block';
    var p = document.createElement('p');
    var a = document.createElement('a');
    a.href = "javascript:closeEditTable('" + questionID + "','" + men + "','" + callerServlet + "','" + callerArgs + "');";
    a.innerHTML = 'Close';
    p.appendChild(a);
    d2.appendChild(p);
    d4.appendChild(d2);

    var d3 = document.createElement('DIV');
    d3.style.display = 'inline-block';
    d3.style.float = 'right';
    d3.id = 'qbody-1003-' + questionID + '-1';
    var i = document.createElement('IMG');
    i.style.cursor = 'pointer';
    i.onclick = function(e) {
        doDeleteAttachment('Table', 'qbody-1003', 'table', questionID, men, imagesDir, 'Y', callerServlet, callerArgs, numCols);
    };
    i.src = men + imagesDir + 'terminated.png';
    i.style.border = '0';
    i.style.paddingLeft = '20px';
    d3.appendChild(i);
    d4.appendChild(d3);
    t.parentNode.insertBefore(d4, t);

    NUMCOLS1003 = numCols;
    reEditTable(questionID, men, imagesDir, callerServlet, callerArgs, numCols);
}

// -----------------------------------------------------------------------------
function reEditTable(questionID, men, imagesDir, callerServlet, callerArgs, numCols) {
    var tbody = document.getElementById('qbody-1003-' + questionID);
    removeAllChildren(tbody);
    var tr = document.createElement('TR');
    tbody.appendChild(tr);
    var td, i;

    if (numCols === 0) {
        td = document.createElement('TD');
        td.id = 'r1003a-0';
        td.style.verticalAlign = 'top';
        td.style.textAlign = 'center';
        td.style.cursor = 'pointer';
        td.style.background = 'black';
        td.onclick = function(e) {
            newRow1003(0, questionID, men, imagesDir, callerServlet, callerArgs);
        };
        i = document.createElement('IMG');
        i.src = men + imagesDir + 'mc.png';
        i.style.border = '0';
        i.style.padding = '5px';
        td.appendChild(i);
        tr.appendChild(td);
        return;
    }

    td = document.createElement('TD');
    td.style.background = 'transparent';
    tr.appendChild(td);

    for (var x = 0; x < numCols; ++x) {
        td = document.createElement('TD');
        td.style.verticalAlign = 'top';
        td.style.textAlign = 'center';
        td.style.cursor = 'pointer';
        td.style.background = 'black';
        td.onclick = (function(x, questionID, men, imagesDir, callerServlet, callerArgs) {
            return function() {
                newCol1003(x, questionID, men, imagesDir, callerServlet, callerArgs);
            }
        })(x, questionID, men, imagesDir, callerServlet, callerArgs);

        i = document.createElement('IMG');
        i.src = men + imagesDir + 'mc.png';
        i.style.border = '0';
        i.style.padding = '5px';
        td.appendChild(i);
        tr.appendChild(td);

        td = document.createElement('TD');
        td.style.verticalAlign = 'top';
        td.style.textAlign = 'center';
        td.style.cursor = 'pointer';
        td.style.background = 'black';
        td.onclick = (function(x, questionID, men, imagesDir, callerServlet, callerArgs) {
            return function() {
                delCol1003(x, questionID, men, imagesDir, callerServlet, callerArgs);
            }
        })(x, questionID, men, imagesDir, callerServlet, callerArgs);
        i = document.createElement('IMG');
        i.src = men + imagesDir + 'terminated.png';
        i.style.border = '0';
        i.style.padding = '5px';
        td.appendChild(i);
        tr.appendChild(td);
    }

    td = document.createElement('TD');
    td.style.verticalAlign = 'top';
    td.style.textAlign = 'center';
    td.style.cursor = 'pointer';
    td.style.background = 'black';
    td.onclick = function(e) {
        newCol1003(numCols, questionID, men, imagesDir, callerServlet, callerArgs);
    };
    i = document.createElement('IMG');
    i.src = men + imagesDir + 'mc.png';
    i.style.border = '0';
    i.style.padding = '5px';
    td.appendChild(i);
    tr.appendChild(td);

    tr = document.createElement('TR');
    td = document.createElement('TD');
    td.id = 'r1003-0';
    td.style.verticalAlign = 'top';
    td.style.textAlign = 'center';
    td.style.cursor = 'pointer';
    td.style.background = 'black';
    td.onclick = function(e) {
        newRow1003(0, questionID, men, imagesDir, callerServlet, callerArgs);
    };
    i = document.createElement('IMG');
    i.src = men + imagesDir + 'mc.png';
    i.style.border = '0';
    i.style.padding = '5px';
    td.appendChild(i);
    tr.appendChild(td);
    tbody.appendChild(tr);

    doEachCell(questionID, men, imagesDir, callerServlet, callerArgs);
}

var data1003aINFO, data1003aHTML, data1003aSCRIPTS, data1003aReceived;

// ----------------------------------------------------------------------------
function doEachCell(questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003aReceived = false;

    socketSend("GET", "/_1003a/data1003a/" + questionID + ".call", men);
    drtout = testDoEachCell(questionID, men, imagesDir, callerServlet, callerArgs);
}

// ----------------------------------------------------------------------------
function testDoEachCell(questionID, men, imagesDir, callerServlet, callerArgs) {
    if (data1003aReceived) {
        clearTimeout(drtout);

        if (!data1003aINFO.startsWith('OK'))
            displayMessage("Unknown Error: Cannot Fetch Table");
        else {
            var len = data1003aHTML.length,
                x = 0,
                cellID, title, rowNum, colNum, rowNum = 0;
            while (x < len) {
                cellID = title = rowNum = colNum = '';
                while (x < len && data1003aHTML.charAt(x) !== '`') cellID += data1003aHTML.charAt(x++);
                ++x;
                while (x < len && data1003aHTML.charAt(x) !== '`') title += data1003aHTML.charAt(x++);
                ++x;
                while (x < len && data1003aHTML.charAt(x) !== '`') rowNum += data1003aHTML.charAt(x++);
                ++x;
                while (x < len && data1003aHTML.charAt(x) !== '`') colNum += data1003aHTML.charAt(x++);
                ++x;

                forACell(questionID, cellID, title, rowNum, colNum, men, imagesDir, callerServlet, callerArgs);

                ++rowNum;
            }
        }
    } else drtout = setTimeout("testDoEachCell('" + questionID + "','" + men + "','" + imagesDir + "')", 100);
}

function forACell(questionID, cellID, title, rowNum, colNum, men, imagesDir, callerServlet, callerArgs) {
    var tr, td, i, p;
    var tbody = document.getElementById('qbody-1003-' + questionID);

    if (colNum === 0) {
        tr = document.createElement('TR');
        tr.id = 'tr-' + questionID + '-' + rowNum;
        td = document.createElement('TD');
        td.style.cursor = 'pointer';
        td.style.background = 'black';
        td.onclick = function(e) {
            delRow1003(rowNum, questionID, men, imagesDir, callerServlet, callerArgs);
        };
        i = document.createElement('IMG');
        i.src = men + imagesDir + 'terminated.png';
        i.style.border = '0';
        i.style.padding = '5px';
        td.appendChild(i);
        tr.appendChild(td);
        tbody.appendChild(tr);
    } else tr = document.getElementById('tr-' + questionID + '-' + rowNum);

    td = document.createElement('TD');
    td.style.background = 'transparent';
    tr.appendChild(td);

    if (title.length === 0) title = '&nbsp;&nbsp;&nbsp;';
    td = document.createElement('TD');
    td.id = 'c1003-' + cellID;
    td.style.verticalAlign = 'top';
    td.style.cursor = 'pointer';
    td.onclick = function(e) {
        editCell1003(cellID, men, imagesDir);
    };
    p = document.createElement('P');
    p.innerHTML = title;
    td.appendChild(p);
    tr.appendChild(td);

    if ((parseInt(colNum) + 1) === NUMCOLS1003) {
        tr = document.createElement('TR');
        td = document.createElement('TD');
        td.id = 'r1003-' + (++rowNum);
        td.style.verticalAlign = 'top';
        td.style.textAlign = 'center';
        td.style.cursor = 'pointer';
        td.style.background = 'black';
        td.onclick = function(e) {
            newRow1003(rowNum, questionID, men, imagesDir, callerServlet, callerArgs);
        };
        i = document.createElement('IMG');
        i.src = men + imagesDir + 'mc.png';
        i.style.border = '0';
        i.style.padding = '5px';
        td.appendChild(i);
        tr.appendChild(td);
        tbody.appendChild(tr);
    }
}

function newCol1003(col, questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003INFO = '';
    updateNew1003('C', col, questionID, men, imagesDir, callerServlet, callerArgs);
}

function updateNew1003(rowOrCol, rowOrColNum, questionID, men, imagesDir, callerServlet, callerArgs) {
    if (rowOrCol === 'C' || (rowOrCol === 'R' && rowOrColNum === 0)) ++NUMCOLS1003;
    data1003INFO = '';

    var u = "/_1003d/data1003/" + questionID + "/" + rowOrCol + "/" + rowOrColNum + ".call";
    socketSend('GET', u, men);
    res1003 = setTimeout("testUpdateNew1003('" + questionID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "')", 100);
}

function testUpdateNew1003(questionID, men, imagesDir, callerServlet, callerArgs) {
    if (data1003INFO.length > 0) {
        clearTimeout(res1003);
        processUpdateNew1003(questionID, men, imagesDir, callerServlet, callerArgs);
    } else res1003 = setTimeout("testUpdateNew1003('" + questionID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "')", 100);
}

function processUpdateNew1003(questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003INFO = decodeURIComponent(data1003INFO);
    data1003SCRIPTS = decodeURIComponent(data1003SCRIPTS);
    data1003HTML = decodeURIComponent(data1003HTML);

    if (data1003INFO.startsWith('OK'))
        reEditTable(questionID, men, imagesDir, callerServlet, callerArgs, NUMCOLS1003);
    else displayMessage(data1003INFO.substring(3));
}

function newRow1003(row, questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003INFO = '';
    updateNew1003('R', row, questionID, men, imagesDir, callerServlet, callerArgs);
}

function editCell1003(cellID, men, imagesDir) {
    data1003INFO = '';
    var d = document.getElementById('c1003-' + cellID);
    var i = document.createElement('INPUT');
    i.size = '50';
    i.maxLength = '50';
    i.id = 'i1003-' + cellID;
    var v = d.firstChild.innerHTML;
    v = v.replace(/&nbsp;/g, ' ');
    if (v === '   ') v = '';
    i.value = v;
    d.replaceChild(i, d.firstChild);
    d.onclick = '';
    var img = document.createElement('IMG');
    img.style.border = '0';
    img.src = men + imagesDir + "closewavelet.png";
    img.onclick = (function(cellID, men, imagesDir) {
        return function() {
            var d = document.getElementById('i1003-' + cellID);
            var t = d.value;
            updateCell1003(cellID, t, men, imagesDir);
        }
    })(cellID, men, imagesDir);
    d.appendChild(img);
}

function updateCell1003(cellID, title, men, imagesDir) {
    data1003INFO = '';
    var u = "/_1003c/data1003/" + cellID + "/" + encodeURIComponent(title) + ".call";
    socketSend('GET', u, men);
    res1003 = setTimeout("testUpdateCell1003('" + men + "','" + imagesDir + "')", 100);
}

function testUpdateCell1003(men, imagesDir) {
    if (data1003INFO.length > 0) {
        clearTimeout(res1003);
        processUpdateCell1003(men, imagesDir);
    } else res1003 = setTimeout("testUpdateCell1003('" + men + "','" + imagesDir + "')", 100);
}

function processUpdateCell1003(men, imagesDir) {
    data1003INFO = decodeURIComponent(data1003INFO);
    data1003SCRIPTS = decodeURIComponent(data1003SCRIPTS);
    data1003HTML = decodeURIComponent(data1003HTML);
    if (data1003INFO.startsWith('OK')) {
        var x = 9,
            cellID = '',
            title = '',
            len = data1003INFO.length;
        while (x < len && data1003INFO.charAt(x) !== '`') cellID += data1003INFO.charAt(x++);
        ++x;
        while (x < len) title += data1003INFO.charAt(x++);
        var p = document.createElement('P');
        p.innerHTML = title;
        var d = document.getElementById('c1003-' + cellID);
        d.onclick = (function(cellID, men, imagesDir) {
            return function() {
                editCell1003(cellID, men, imagesDir);
            }
        })(cellID, men, imagesDir);
        removeAllChildren(d);
        d.appendChild(p);
    } else displayMessage(data1003INFO.substring(3));
}

// --------------------------------------------------------------------------------
function closeEditTable(questionID, men, callerServlet, callerArgs) {
    data1003INFO = '';

    var u = "/_1003/data1003/" + questionID + "/" + callerServlet + "/" + callerArgs + ".call";
    socketSend('GET', u, men);
    res1003 = setTimeout("testCloseEditTable()", 100);
}

function testCloseEditTable() {
    if (data1003INFO.length > 0) {
        clearTimeout(res1003);
        processCloseEditTable();
    } else res1003 = setTimeout("testCloseEditTable()", 100);
}

function processCloseEditTable() {
    data1003INFO = decodeURIComponent(data1003INFO);
    data1003SCRIPTS = decodeURIComponent(data1003SCRIPTS);
    data1003HTML = decodeURIComponent(data1003HTML);

    if (data1003INFO.startsWith('OK'))
        updateScreen(data1003INFO, data1003HTML, data1003SCRIPTS);
    else displayMessage(data1003INFO.substring(3));
}

function delRow1003(rowNum, questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003INFO = '';
    if (confirm('Are you sure you want to delete this row?')) {
        var u = "/_1003e/data1003/" + questionID + "/" + rowNum + ".call";
        socketSend('GET', u, men);
        res1003 = setTimeout("testDelRow1003('" + questionID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "')", 100);
    }
}

function testDelRow1003(questionID, men, imagesDir, callerServlet, callerArgs) {
    if (data1003INFO.length > 0) {
        clearTimeout(res1003);
        processDelRow1003(questionID, men, imagesDir, callerServlet, callerArgs);
    } else res1003 = setTimeout("testDelRow1003('" + questionID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "')", 100);
}

function processDelRow1003(questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003INFO = decodeURIComponent(data1003INFO);
    data1003SCRIPTS = decodeURIComponent(data1003SCRIPTS);
    data1003HTML = decodeURIComponent(data1003HTML);
    if (data1003INFO.startsWith('OK'))
        reEditTable(questionID, men, imagesDir, callerServlet, callerArgs, NUMCOLS1003);
    else displayMessage(data1003INFO.substring(3));
}

function delCol1003(colNum, questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003INFO = '';
    if (confirm('Are you sure you want to delete this column?')) {
        var u = "/_1003b/data1003/" + questionID + "/" + colNum + ".call";
        socketSend('GET', u, men);
        res1003 = setTimeout("testDelCol1003('" + questionID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "')", 100);
    }
}

function testDelCol1003(questionID, men, imagesDir, callerServlet, callerArgs) {
    if (data1003INFO.length > 0) {
        clearTimeout(res1003);
        processDelCol1003(questionID, men, imagesDir, callerServlet, callerArgs);
    } else res1003 = setTimeout("testDelCol1003('" + questionID + "','" + men + "','" + imagesDir + "','" + callerServlet + "','" + callerArgs + "')", 100);
}

function processDelCol1003(questionID, men, imagesDir, callerServlet, callerArgs) {
    data1003INFO = decodeURIComponent(data1003INFO);
    data1003SCRIPTS = decodeURIComponent(data1003SCRIPTS);
    data1003HTML = decodeURIComponent(data1003HTML);
    if (data1003INFO.startsWith('OK')) {
        --NUMCOLS1003;
        reEditTable(questionID, men, imagesDir, callerServlet, callerArgs, NUMCOLS1003);
    } else displayMessage(data1003INFO.substring(3));
}

