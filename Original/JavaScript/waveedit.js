// ============================================================================
// System: ZaraStar
// File: waveedit.js
// Author: C.K.Harvey
// Copyright (c) 2010-15 Christopher Harvey. All Rights Reserved.
// ============================================================================
var dataEditWaveReceived, mousePosEditWave, formIDEditWave;
var waveIDEditWave, waveTypeEditWave;
var unmEditWave, sesEditWave, dnmEditWave, menEditWave;
var imagesDirEditWave;
var dataEditWaveINFO, dataEditWaveHTML, dataEditWaveSCRIPTS, resEditWave, libWaveletIDEditWave, libCallTypeEditWave, libLibTypeEditWave, libWaveTypeEditWave;
var waveEditAlbumName, topicEditWave;

function stopPropagation(e) {
    if (typeof e === 'undefined') e = window.event;


    if (!e) var e = window.event;
    e.cancelBubble = true;
    if (e.stopPropagation) e.stopPropagation();
}

var data1042bINFO, data1042bHTML, data1042bSCRIPTS, res1042b;

function getProjectCells(projectID, storyID, men) {
    dataEditWaveINFO = '';
    var u = "/_1042b/dataEditWave/" + projectID + "/" + storyID + ".call";
    socketSend('GET', u, men);

    resEditWave = setTimeout("testGetProjectCells('" + projectID + "','" + storyID + "','" + men + "')", 100);
}

function testGetProjectCells(projectID, storyID, men) {
    if (dataEditWaveINFO.length > 0) {
        clearTimeout(resEditWave);
        processGetProjectCells(projectID, storyID, men);
    } else resEditWave = setTimeout("testGetProjectCells('" + projectID + "','" + storyID + "','" + men + "')", 100);
}

function processGetProjectCells(projectID, storyID, men) {
    if (!dataEditWaveINFO.startsWith('ERR')) {
        dataEditWaveHTML = decodeURIComponent(dataEditWaveHTML);
        var d = document.getElementById('editProject-' + projectID);
        var d1 = document.createElement('DIV');
        if (isIE) d1.setAttribute('className', 'page');
        else d1.setAttribute('class', 'page');
        d1.style.width = '100%';

        insertAfter(d.parentNode, d1, d);
        d.style.background = 'transparent';

        var div, len = dataEditWaveHTML.length,
            x = 0,
            cellID, selected, title;

        while (x < len) {
            cellID = selected = title = '';
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') cellID += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') selected += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') title += dataEditWaveHTML.charAt(x++);
            ++x;

            div = document.createElement('DIV');

            div.id = 'editCell-' + cellID;
            div.style.padding = '3px';
            if (selected === 'Y') {
                div.style.background = 'lightgreen';
                op = 'D';
            } else {
                div.style.background = 'transparent';
                op = 'I';
            }
            div.style.cursor = 'pointer';
            div.onclick = (function(storyID, cellID, op, men) {
                return function() {
                    updateTaggingProjects(storyID, cellID, op, men, 'P');
                }
            })(storyID, cellID, op, men);

            div3 = document.createElement('DIV');
            div3.style.display = 'inline-block';
            div3.style.verticalAlign = 'top';

            p2 = document.createElement('p');
            p2.style.paddingLeft = '50px';
            p2.innerHTML = title;

            div3.appendChild(p2);
            div.appendChild(div3);
            d1.appendChild(div);
        }
    } else displayMessage(dataEditWaveINFO);
}

function openTaggingProjects(storyID, taggingType, men) {
    dataEditWaveINFO = '';
    var u = "/_1042/dataEditWave/" + storyID + "/" + taggingType + ".call";
    socketSend('GET', u, men);

    resEditWave = setTimeout("testOpenTaggingProjects('" + storyID + "','" + men + "','" + taggingType + "')", 100);
}

function testOpenTaggingProjects(storyID, men, taggingType) {
    if (dataEditWaveINFO.length > 0) {
        clearTimeout(resEditWave);
        if (taggingType === 'P')
            processOpenTaggingProjects(storyID, men);
        else processOpenTaggingOthers(storyID, men, taggingType);
    } else resEditWave = setTimeout("testOpenTaggingProjects('" + storyID + "','" + men + "','" + taggingType + "')", 100);
}

function processOpenTaggingProjects(storyID, men) {
    if (!dataEditWaveINFO.startsWith('ERR')) {
        dataEditWaveHTML = decodeURIComponent(dataEditWaveHTML);
        var d = document.getElementById('taggingPosnProjects');
        var d1 = document.createElement('DIV');
        if (isIE) d1.setAttribute('className', 'page');
        else d1.setAttribute('class', 'page');
        d1.style.width = '100%';
        if (d.firstChild)
            d.replaceChild(d1, d.firstChild);
        else d.appendChild(d1);
        var d2 = document.createElement('DIV');
        d2.align = 'right';
        var p = document.createElement('p');
        var a = document.createElement('a');
        a.href = 'javascript:closeTaggingProjects();';
        a.innerHTML = 'Close';
        p.appendChild(a);
        d2.appendChild(p);
        d1.appendChild(d2);

        var op, len = dataEditWaveHTML.length,
            x = 0,
            which, projectID, selected, desc, div, div3, br, p, p2;
        while (x < len) {
            projectID = selected = desc = '';
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') projectID += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') selected += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') desc += dataEditWaveHTML.charAt(x++);
            ++x;

            div = document.createElement('DIV');

            div.id = 'editProject-' + projectID;
            div.style.padding = '3px';
            if (selected === 'Y') {
                div.style.background = 'lightgreen';
                op = 'D';
            } else {
                div.style.background = 'transparent';
                op = 'I';
            }
            div.style.cursor = 'pointer';
            div.onclick = (function(storyID, projectID, op) {
                return function() {
                    getProjectCells(projectID, storyID, men);
                };
            })(storyID, projectID, op);

            div3 = document.createElement('DIV');
            div3.style.display = 'inline-block';
            div3.style.verticalAlign = 'top';

            p2 = document.createElement('p');
            p2.innerHTML = desc;

            div3.appendChild(p2);
            div.appendChild(div3);
            d1.appendChild(div);
        }

        d2 = document.createElement('div');
        d2.align = 'right';
        p = document.createElement('p');
        a = document.createElement('a');
        a.href = 'javascript:closeTaggingProjects();';
        a.innerHTML = 'Close';
        p.appendChild(a);
        d2.appendChild(p);
        d1.appendChild(d2);
    } else displayMessage(dataEditWaveINFO);
}

function processOpenTaggingOthers(storyID, men, taggingType) {
    if (!dataEditWaveINFO.startsWith('ERR')) {
        dataEditWaveHTML = decodeURIComponent(dataEditWaveHTML);
        var d = document.getElementById('taggingPosnProjects');
        var d1 = document.createElement('DIV');
        if (isIE) d1.setAttribute('className', 'page');
        else d1.setAttribute('class', 'page');
        d1.style.width = '100%';
        if (d.firstChild)
            d.replaceChild(d1, d.firstChild);
        else d.appendChild(d1);
        var d2 = document.createElement('DIV');
        d2.align = 'right';
        var p = document.createElement('p');
        var a = document.createElement('a');
        a.href = 'javascript:closeTaggingProjects();';
        a.innerHTML = 'Close';
        p.appendChild(a);
        d2.appendChild(p);
        d1.appendChild(d2);

        var op, len = dataEditWaveHTML.length,
            x = 0,
            which, id, selected, desc, div, div3, br, p, p2;
        while (x < len) {
            id = selected = desc = '';
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') id += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') selected += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') desc += dataEditWaveHTML.charAt(x++);
            ++x;

            div = document.createElement('DIV');

            div.id = 'editCell-' + id;
            div.style.padding = '3px';
            if (selected === 'Y') {
                div.style.background = 'lightgreen';
                op = 'D';
            } else {
                div.style.background = 'transparent';
                op = 'I';
            }
            div.style.cursor = 'pointer';
            div.onclick = (function(storyID, id, op, men, taggingType) {
                return function() {
                    updateTaggingProjects(storyID, id, op, men, taggingType);
                }
            })(storyID, id, op, men, taggingType);

            div3 = document.createElement('DIV');
            div3.style.display = 'inline-block';
            div3.style.verticalAlign = 'top';

            p2 = document.createElement('p');
            p2.innerHTML = desc;

            div3.appendChild(p2);
            div.appendChild(div3);
            d1.appendChild(div);
        }

        d2 = document.createElement('div');
        d2.align = 'right';
        p = document.createElement('p');
        a = document.createElement('a');
        a.href = 'javascript:closeTaggingProjects();';
        a.innerHTML = 'Close';
        p.appendChild(a);
        d2.appendChild(p);
        d1.appendChild(d2);
    } else displayMessage(dataEditWaveINFO);
}

function closeTaggingProjects() {
    removeAllChildren(document.getElementById('taggingPosnProjects'));
}

function openLibEditWave(trID, trIDToUse, unm, men, imagesDir, isWriter) {
    var formID = +Math.floor(Math.random() * 10000);
    var f = document.createElement('form');

    var d = document.createElement('div');
    d.id = 'uploadDiv2';

    var i3 = document.createElement('i');
    i3.id = 'albumName';
    i3.value = 'Images';
    i3.visibility = 'hidden';
    d.appendChild(i3);
    f.appendChild(d);

    var ddd = document.getElementById(trIDToUse);

    ddd.appendChild(f);

    uploadDir(trID, trIDToUse, unm, men, imagesDir, isWriter);
}

function closeUpload012000() {
    try {
        var d = document.getElementById('uploadDiv');
        d.parentNode.removeChild(d);
    } catch (Exception) {}
}

function uploadDir(trID, trIDToUse, unm, men, imagesDir, isWriter) {
    var album = document.getElementById('a' + trIDToUse);

    album = album.innerHTML;

    closeUpload012000();

    var formID = Math.floor(Math.random() * 10000);
    var f = document.createElement('form');
    f.id = formID;
    f.name = formID;

    var d = document.createElement('div');
    d.id = 'uploadDiv';
    d.style.width = '100%';
    d.style.borderWidth = '3px';
    d.style.borderStyle = 'dotted';
    d.style.borderColor = 'green';
    var t = document.createElement('table');
    t.style.width = '100%';

    tr = document.createElement('tr');
    td = document.createElement('td');

    var i;
    i = document.createElement('input');
    i.type = 'file';
    i.id = 'userfile';
    i.multiple = 'multiple';
    i.style.cursor = 'pointer';
    i.onchange = function(e) {
        doFilesUpload('A', '', '', unm, men, imagesDir, isWriter, album);
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
            doFilesUploadAndroid44('A', file, '', '', unm, men, imagesDir, isWriter, album);
        };
    }

    td.appendChild(i);
    tr.appendChild(td);

    var td3 = document.createElement('td');
    td3.align = 'right';
    var p3 = document.createElement('p');
    var a3 = document.createElement('a');
    a3.innerHTML = 'Cancel';
    a3.href = 'javascript:closeUpload012000()';
    p3.appendChild(a3);
    td3.appendChild(a3);
    tr.appendChild(td3);
    t.appendChild(tr);
    d.appendChild(t);
    f.appendChild(d);




    t.appendChild(tr);
    d.appendChild(t);
    f.appendChild(d);

    tr = document.createElement('TR');
    td = document.createElement('TD');
    td.colSpan = '2';
    //t.appendChild(tr);
    tr.appendChild(td);
    td.appendChild(f);
    var ddd = document.getElementById(trID);
    if (ddd) {
        insertAfter(ddd.parentNode, tr, ddd);
    }
}

function testOpenLibEditWave() {
    if (dataEditWaveINFO.length > 0) {
        clearTimeout(resEditWave);
        processOpenLibEditWave();
    } else resEditWave = setTimeout('testOpenLibEditWave()', 1000);
}

function processOpenLibEditWave() {
    if (dataEditWaveINFO.startsWith('OK')) {
        dataEditWaveHTML = decodeURIComponent(dataEditWaveHTML);
        var d = document.getElementById('libDiv');
        if (!d.firstChild) {
            var y = 0,
                len = dataEditWaveHTML.length,
                scripts = '';
            while (y < len && dataEditWaveHTML.charAt(y) !== '~')
                scripts += dataEditWaveHTML.charAt(y++);
            var html = dataEditWaveHTML.substring(y + 1);
            var dx = document.createElement('div');
            d.appendChild(dx);
            if (isIE)
                dx.innerHTML = sanitiseHTML(scripts);
            else dx.innerHTML = scripts;
            execScripts(dx);
            d.innerHTML = html;
            d.style.borderStyle = 'solid';
            d.style.borderColor = 'black';
            d.style.borderWidth = '1px';
            d.style.backgroundColor = 'lightgray';
        }
    } else displayMessage(dataEditWaveHTML);
}

var data1042aINFO, data1042aHTML, data1042aSCRIPTS, res1042a;

function updateTaggingProjects(storyID, cellID, op, men, taggingType) {
    data1042aINFO = '';

    socketSend('GET', '/_1042a/data1042a/' + storyID + '/' + cellID + '/' + op + '/' + taggingType + '.call', men);

    res1042a = setTimeout("testUpdateTaggingProjects('" + storyID + "','" + men + "','" + taggingType + "')", 100);
}

function testUpdateTaggingProjects(storyID, men, taggingType) {
    if (data1042aINFO.length > 0) {
        clearTimeout(res1042a);
        processUpdateTaggingProjects(storyID, men, taggingType);
    } else res1042a = setTimeout("testUpdateTaggingProjects('" + storyID + "','" + men + "','" + taggingType + "')", 100);
}

function processUpdateTaggingProjects(storyID, men, taggingType) {
    if (data1042aINFO.startsWith("ERR")) return;

    var lastOp = '',
        cellID = '',
        desc = '',
        len = data1042aHTML.length,
        x = 0;
    while (x < len && data1042aHTML.charAt(x) !== '`') lastOp += data1042aHTML.charAt(x++);
    ++x;
    while (x < len && data1042aHTML.charAt(x) !== '`') cellID += data1042aHTML.charAt(x++);
    ++x;
    while (x < len) desc += data1042aHTML.charAt(x++);

    desc = deSanitise(desc);

    var d = document.getElementById('editCell-' + cellID);
    if (d) {
        if (lastOp === 'D') {
            d.style.background = 'transparent';
            d.onclick = function(e) {
                updateTaggingProjects(storyID, cellID, 'I', men, taggingType);
            };
        } else {
            d.style.background = 'lightgreen';
            d.onclick = function(e) {
                updateTaggingProjects(storyID, cellID, 'D', men, taggingType);
            };
        }
    }

    var d2 = document.getElementById('storyProject-' + cellID);
    if (lastOp === 'D') {
        if (d2) d2.parentNode.removeChild(d2);
        d2 = document.getElementById('storyProjects');
        if (!d2.firstChild) {
            var s = document.createElement('SPAN');
            s.id = 'storyNotPublished';
            if (isIE) s.setAttribute('className', 'editHere');
            else s.setAttribute('class', 'editHere');
            s.innerHTML = 'Not Published';
            d2.appendChild(s);
        }
    } else {
        var s = document.getElementById('storyNotPublished');
        if (s) s.parentNode.removeChild(s);

        var s = document.createElement('SPAN');
        s.id = 'storyProject-' + cellID;
        if (taggingType === 'C')
            s.innerHTML = "Club: " + desc + "<br/>";
        else
        if (taggingType === 'S')
            s.innerHTML = "School: " + desc + "<br/>";
        else
        if (taggingType === 'F')
            s.innerHTML = "Family: " + desc + "<br/>";
        else s.innerHTML = "Project: " + desc + "<br/>";

        var dd = document.getElementById('storyProjects');
        dd.appendChild(s);
    }
}

// --------------------------------------------------------------------------------
function openTaggingEditDates(editing, storyID, spanID, date, dateStr, whichDate, men) {
    var yyyy = date.substring(0, 4);
    var mm = date.substring(5, 7);
    var dd = date.substring(8, 10);

    var sp = document.getElementById(spanID);

    var s = document.createElement('SPAN');
    s.id = spanID;
    s.style.paddingTop = '10px';

    sp.parentNode.replaceChild(s, sp);

    var tDate = document.createElement('TABLE');
    var trDate = document.createElement('TR');
    var tdDate1 = document.createElement('TD');
    var tdDate2 = document.createElement('TD');

    var p00 = document.createElement('P');
    p00.innerHTML = date;
    p00.id = 'p00-' + spanID;
    p00.style.visibility = 'hidden';

    var p01 = document.createElement('P');
    p01.innerHTML = dateStr;
    p01.id = 'p01-' + spanID;

    var p02 = document.createElement('P');
    p02.id = 'p02-' + spanID;
    p02.style.fontSize = '10px';
    p02.style.color = 'red';
    tdDate2.style.verticalAlign = 'top';
    tdDate2.style.paddingLeft = '30px';

    var p03 = document.createElement('P');
    p03.id = 'p03-' + spanID;

    tdDate1.appendChild(p01);
    tdDate2.appendChild(p02);
    tdDate1.appendChild(p03);
    tdDate1.appendChild(p00);
    trDate.appendChild(tdDate1);
    trDate.appendChild(tdDate2);
    tDate.appendChild(trDate);
    s.appendChild(tDate);

    doCalendar("p01-" + spanID, "p00-" + spanID, "p02-" + spanID, "p03-" + spanID, dd, mm, yyyy);

    resEditWave = setTimeout("watchForCalendarClose('" + editing + "','" + storyID + "','" + spanID + "','p02-" + spanID + "','p00-" + spanID + "','" + whichDate + "','" + men + "')", 100);
}

function watchForCalendarClose(editing, storyID, spanID, callerID, dateID, whichDate, men) {
    var state = document.getElementById(callerID).style.visibility;

    if (state === 'visible') {
        clearTimeout(resEditWave);

        var date = document.getElementById(dateID).innerHTML;

        if (editing === 'Y')
            resEditWave = setTimeout("updateTagDate('" + storyID + "','" + spanID + "','" + date + "','" + whichDate + "','" + men + "')", 100);
        else {
            var s = document.getElementById(spanID);
            s.onclick = function(e) {
                openTaggingEditDates('N', storyID, spanID, date, date);
            };
        }
    } else resEditWave = setTimeout("watchForCalendarClose('" + editing + "','" + storyID + "','" + spanID + "','" + callerID + "','" + dateID + "','" + whichDate + "','" + men + "')", 100);
}

function updateTagDate(storyID, spanID, date, whichDate, men) {
    dataEditWaveINFO = '';
    var u = "/_1002e/dataEditWave/" + storyID + "/" + date + "/" + whichDate + ".call";

    socketSend('GET', u, men);

    resEditWave = setTimeout("testUpdateTagDate('" + storyID + "','" + spanID + "','" + men + "')", 100);
}

function testUpdateTagDate(storyID, spanID, men) {
    if (dataEditWaveINFO.length > 0) {
        clearTimeout(resEditWave);
        processUpdateTagDate(storyID, spanID, men);
    } else resEditDates = setTimeout("testUpdateTagDate('" + storyID + "','" + spanID + "','" + men + "')", 100);
}

function processUpdateTagDate(storyID, spanID, men) {
    dataEditWaveHTML = decodeURIComponent(dataEditWaveHTML);

    if (dataEditWaveINFO.startsWith('ERR')) {
        displayMessage(dataEditWaveINFO.substring(3));
        return;
    }

    var len = dataEditWaveHTML.length,
        x = 0,
        dateFrom = '',
        dateFromStr = '';
    while (x < len && dataEditWaveHTML.charAt(x) !== '`') dateFrom += dataEditWaveHTML.charAt(x++);
    ++x;
    while (x < len) dateFromStr += dataEditWaveHTML.charAt(x++);

    var s = document.getElementById(spanID);
    var s2 = document.createElement('SPAN');
    s2.id = spanID;
    if (isIE) s2.setAttribute('className', 'editHere');
    else s2.setAttribute('class', 'editHere');
    s2.onclick = function(e) {
        openTaggingEditDates('Y', storyID, spanID, dateFrom, dateFromStr, 'F', men);
    };
    s2.style.cursor = 'pointer';
    s2.innerHTML = dateFromStr;
    s.parentNode.replaceChild(s2, s);
}

function repEditWave(msg) {
    var msg2 = '';
    for (var x = 0; x < msg.length; ++x) {
        if (msg.charAt(x) === '/') msg2 += '`';
        else msg2 += msg.charAt(x);
    }
    return msg2;
}

var data1041bINFO, data1041bHTML, data1041bSCRIPTS, res1041b;

function getCells(projectID, qort, questionID, men) {
    dataEditWaveINFO = '';
    var u = "/_1041b/dataEditWave/" + projectID + "/" + qort + "/" + questionID + ".call";
    socketSend('GET', u, men);

    resEditWave = setTimeout("testGetCells('" + projectID + "','" + qort + "','" + questionID + "','" + men + "')", 100);
}

function testGetCells(projectID, qort, questionID, men) {
    if (dataEditWaveINFO.length > 0) {
        clearTimeout(resEditWave);
        processGetCells(projectID, qort, questionID, men);
    } else resEditWave = setTimeout("testGetCells('" + projectID + "','" + qort + "','" + questionID + "','" + men + "')", 100);
}

function processGetCells(projectID, qort, questionID, men) {
    if (!dataEditWaveINFO.startsWith('ERR')) {
        dataEditWaveHTML = decodeURIComponent(dataEditWaveHTML);
        var d = document.getElementById('editProject-' + projectID);
        var d1 = document.createElement('DIV');
        if (isIE) d1.setAttribute('className', 'page');
        else d1.setAttribute('class', 'page');
        d1.style.width = '100%';

        insertAfter(d.parentNode, d1, d);
        d.style.background = 'transparent';

        var div, len = dataEditWaveHTML.length,
            x = 0,
            cellID, selected, title;

        while (x < len) {
            cellID = selected = title = '';
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') cellID += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') selected += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') title += dataEditWaveHTML.charAt(x++);
            ++x;

            div = document.createElement('DIV');

            div.id = 'editCell-' + cellID;
            div.style.padding = '3px';
            if (selected === 'Y') {
                div.style.background = 'lightgreen';
                op = 'D';
            } else {
                div.style.background = 'transparent';
                op = 'I';
            }
            div.style.cursor = 'pointer';
            div.onclick = (function(questionID, cellID, op, men) {
                return function() {
                    updateTaggingCell(qort, questionID, cellID, op, men);
                }
            })(questionID, cellID, op, men);

            div3 = document.createElement('DIV');
            div3.style.display = 'inline-block';
            div3.style.verticalAlign = 'top';

            p2 = document.createElement('p');
            p2.style.paddingLeft = '50px';
            p2.innerHTML = title;

            div3.appendChild(p2);
            div.appendChild(div3);
            d1.appendChild(div);
        }
    } else displayMessage(dataEditWaveINFO);
}

function openTaggingCell(qort, questionID, men) {
    dataEditWaveINFO = '';
    var u = "/_1041/dataEditWave/" + qort + "/" + questionID + ".call";
    socketSend('GET', u, men);

    resEditWave = setTimeout("testOpenTaggingCell('" + qort + "','" + questionID + "','" + men + "')", 100);
}

function testOpenTaggingCell(qort, questionID, men) {
    if (dataEditWaveINFO.length > 0) {
        clearTimeout(resEditWave);
        processOpenTaggingCell(qort, questionID, men);
    } else resEditWave = setTimeout("testOpenTaggingCell('" + qort + "','" + questionID + "','" + men + "')", 100);
}

function processOpenTaggingCell(qort, questionID, men) {
    if (!dataEditWaveINFO.startsWith('ERR')) {
        dataEditWaveHTML = decodeURIComponent(dataEditWaveHTML);
        var d = document.getElementById('taggingPosnCell');
        var d1 = document.createElement('DIV');
        if (isIE) d1.setAttribute('className', 'page');
        else d1.setAttribute('class', 'page');
        d1.style.width = '100%';
        if (d.firstChild)
            d.replaceChild(d1, d.firstChild);
        else d.appendChild(d1);
        var d2 = document.createElement('DIV');
        d2.align = 'right';
        var p = document.createElement('p');
        var a = document.createElement('a');
        a.href = 'javascript:closeTaggingCell();';
        a.innerHTML = 'Close';
        p.appendChild(a);
        d2.appendChild(p);
        d1.appendChild(d2);

        var op, len = dataEditWaveHTML.length,
            x = 0,
            which, projectID, selected, desc, div, div3, br, p, p2;
        while (x < len) {
            projectID = selected = title = desc = imgPath = '';
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') projectID += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') selected += dataEditWaveHTML.charAt(x++);
            ++x;
            while (x < len && dataEditWaveHTML.charAt(x) !== '`') desc += dataEditWaveHTML.charAt(x++);
            ++x;

            div = document.createElement('DIV');

            div.id = 'editProject-' + projectID;
            div.style.padding = '3px';
            if (selected === 'Y') {
                div.style.background = 'lightgreen';
                op = 'D';
            } else {
                div.style.background = 'transparent';
                op = 'I';
            }
            div.style.cursor = 'pointer';
            div.onclick = (function(questionID, projectID, op) {
                return function() {
                    getCells(projectID, qort, questionID, men);
                };
            })(questionID, projectID, op);

            div3 = document.createElement('DIV');
            div3.style.display = 'inline-block';
            div3.style.verticalAlign = 'top';

            p2 = document.createElement('p');
            p2.innerHTML = desc;

            div3.appendChild(p2);
            div.appendChild(div3);
            d1.appendChild(div);
        }

        d2 = document.createElement('div');
        d2.align = 'right';
        p = document.createElement('p');
        a = document.createElement('a');
        a.href = 'javascript:closeTaggingCell();';
        a.innerHTML = 'Close';
        p.appendChild(a);
        d2.appendChild(p);
        d1.appendChild(d2);
    } else displayMessage(dataEditWaveINFO);
}

function closeTaggingCell() {
    removeAllChildren(document.getElementById('taggingPosnCell'));
}

var data1041aINFO, data1041aHTML, data1041aSCRIPTS, res1041a;

function updateTaggingCell(qort, questionID, cellID, op, men) {
    data1041aINFO = '';

    socketSend('GET', '/_1041a/data1041a/' + qort + "/" + questionID + '/' + cellID + '/' + op + '.call', men);

    res1041a = setTimeout("testUpdateTaggingCell('" + qort + "','" + questionID + "','" + men + "')", 100);
}

function testUpdateTaggingCell(qort, questionID, men) {
    if (data1041aINFO.length > 0) {
        clearTimeout(res1041a);
        processUpdateTaggingCell(qort, questionID, men);
    } else res1041a = setTimeout("testUpdateTaggingCell('" + qort + "','" + questionID + "','" + men + "')", 100);
}

function processUpdateTaggingCell(qort, questionID, men) {
    if (data1041aINFO.startsWith("ERR")) return;

    var lastOp = '',
        cellID = '',
        desc = '',
        len = data1041aHTML.length,
        x = 0;
    while (x < len && data1041aHTML.charAt(x) !== '`') lastOp += data1041aHTML.charAt(x++);
    ++x;
    while (x < len && data1041aHTML.charAt(x) !== '`') cellID += data1041aHTML.charAt(x++);
    ++x;
    while (x < len) desc += data1041aHTML.charAt(x++);

    desc = deSanitise(desc);

    var d = document.getElementById('editCell-' + cellID);
    if (lastOp === 'D') {
        d.style.background = 'transparent';
        d.onclick = function(e) {
            updateTaggingCell(qort, questionID, cellID, 'I', men);
        };
    } else {
        var d2 = document.getElementById('taggingPosnCell');
        var n = d2.getElementsByTagName('*');
        var i, len = n.length;
        for (i = 0; i < len; ++i) {
            if (n[i].id.startsWith('editCell-'))
                n[i].style.background = 'transparent';
        }

        d.style.background = 'lightgreen';
        d.onclick = function(e) {
            updateTaggingCell(qort, questionID, cellID, 'D', men);
        };
    }

    var d2 = document.getElementById('questionProject-' + cellID);
    if (lastOp === 'D') {
        if (d2) d2.parentNode.removeChild(d2);
        d2 = document.getElementById('questionProjects');
        if (!d2.firstChild) {
            var s = document.createElement('SPAN');
            s.id = 'questionNotAllocated';
            if (isIE) s.setAttribute('className', 'editHere');
            else s.setAttribute('class', 'editHere');
            s.innerHTML = 'Not Allocated';
            d2.appendChild(s);
        }
    } else {
        var s = document.getElementById('questionNotAllocated');
        if (s) s.parentNode.removeChild(s);

        var s = document.createElement('SPAN');
        s.id = 'questionProject-' + cellID;
        s.innerHTML = desc + "<br/>";

        var dd = document.getElementById('questionProjects');
        if (dd.firstChild) dd.replaceChild(s, dd.firstChild);
    }
}
