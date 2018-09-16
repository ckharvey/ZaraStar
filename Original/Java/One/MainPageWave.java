// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Main Page - Wave
// Module: MainPageWave.java
// Author: C.K.Harvey
// Copyright (c) 2002-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class MainPageWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MessagePage messagePage = new MessagePage();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  Profile profile = new Profile();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String uty="", men="", den="", dnm="", bnm="", urlBit="", p1="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1");

      uty = "A";
      if(men == null) men = "";
      if(dnm == null) dnm = "";
      if(den == null) den = dnm;
      if(bnm == null) bnm = "";
      if(p1  == null) p1 = "";

      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      doIt(out, req, p1, urlBit, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      messagePage.errorPage(out, req, e, "Communications Error", "", "", dnm, bnm, urlBit, men, den, uty, "MainPageUtilsk", bytesOut);
      serverUtils.etotalBytes(req, "", dnm, 100, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String urlBit, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String sessionsDir   = directoryUtils.getSessionsDir(dnm);

    String sid = serverUtils.newSessionID("", uty, dnm, sessionsDir, localDefnsDir, "");
    String unm = "_" + sid;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con      = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);
    Connection conAdmin = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_admin?user=" + uName + "&password=" + pWord);
    
    Statement stmt = null;
    ResultSet rs   = null;

    authenticationUtils.checkTables(con, stmt, dnm, localDefnsDir, defnsDir);

    set(conAdmin, stmt, rs, out, p1, urlBit, unm, sid, uty, men, den, dnm, bnm, imagesDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 100, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(conAdmin != null) conAdmin.close();
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection conAdmin, Statement stmt, ResultSet rs, PrintWriter out, String p1, String urlBit, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir, int[] bytesOut) throws Exception
  {
    String companyName = directoryUtils.getAppConfigInfo('N', dnm);

    String[] headerLogo       = new String[1];  headerLogo[0] = "";
    String[] headerLogoRepeat = new String[1];
    String[] usesFlash        = new String[1];
    String[] footerText       = new String[1];
    String[] pageHeaderImage1 = new String[1];
    String[] pageHeaderImage2 = new String[1];
    String[] pageHeaderImage3 = new String[1];
    String[] pageHeaderImage4 = new String[1];
    String[] pageHeaderImage5 = new String[1];
    String[] watermark        = new String[1];

    String imageLibraryDir = directoryUtils.getImagesDir(dnm);

    try
    {
      authenticationUtils.getStyling(dnm, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);
    }
    catch(Exception e) { }

    String watermarkImage = "";
    if(watermark[0].length() > 0)
      watermarkImage = "http://" + men + imageLibraryDir + watermark[0];

    boolean[] activeOperations = new boolean[1];
    boolean[] activeDocuments  = new boolean[1];
    boolean[] activeMail       = new boolean[1];
    boolean[] activeBlogs      = new boolean[1];
    boolean[] activeWaves      = new boolean[1];

    getSysTypeStates(conAdmin, stmt, rs, activeOperations, activeDocuments, activeMail, activeBlogs, activeWaves);

    scoutln(out, bytesOut, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">");
    scoutln(out, bytesOut, "<html><head><title>" + companyName + "</title>");

    String scriptsDir = directoryUtils.getScriptsDirectory();
    scoutln(out, bytesOut, "<script>document.domain=document.domain;</script>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://" + men + ":9000/static/Orbited.js\"></script>");
    scoutln(out, bytesOut, "<script>Orbited.settings.port=9000;TCPSocket=Orbited.TCPSocket;</script>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://" + men + ":9000/static/protocols/stomp/stomp.js\"></script>");
    
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    scoutln(out, bytesOut, "var stompServer = \"" + men + "\";");
    scoutln(out, bytesOut, "var stompPort = 61613;");
    scoutln(out, bytesOut, "var stompUserName = \"guest\";");
    scoutln(out, bytesOut, "var stompPassWord = \"guest\";");
    scoutln(out, bytesOut, "var rabbitRoutingKey = \"" + sid + "\";");
    scoutln(out, bytesOut, "</script>");
    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://" + men + scriptsDir + "zarawave.js\"></script>");

    scriptsDir += "dojo-release-1.3.2/";

    scoutln(out, bytesOut, "<style type=\"text/css\">");
    scoutln(out, bytesOut, "@import \"http://" + men + scriptsDir + "dijit/themes/soria/soria.css\";");

    scoutln(out, bytesOut, "</style>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://" + men + directoryUtils.getEditorDirectory() + "editor.js\"></script>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://" + men + scriptsDir + "dojox/image/resources/ThumbnailPicker.css\">");
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://" + men + scriptsDir + "dojo/resources/dojo.css\">");
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://" + men + scriptsDir + "dojox/image/resources/image.css\">");

    scoutln(out, bytesOut, "<SCRIPT TYPE=\"text/javascript\" SRC=\"http://" + men + scriptsDir + "dojo/dojo.js\" djConfig=\"parseOnLoad: true \"></SCRIPT>");

    scoutln(out, bytesOut, "<style type=\"text/css\">");

    scoutln(out, bytesOut, "html { overflow: hidden; }");

    if(watermark[0].length() == 0)
      scoutln(out, bytesOut, "body {overflow: hidden; padding: 0; margin: 0; width: 100%; height: 100%; background-color: lightgray; } ");
    else scoutln(out, bytesOut, "body {overflow: hidden; padding: 0; margin: 0; width: 100%; height: 100%; background: url(\"" + watermarkImage + "\"); }");

    scoutln(out, bytesOut, "body p { padding: 0; margin: 0; background-color: transparent; } ");

    scoutln(out, bytesOut, "#topx { padding: 0; margin: 0; position: absolute; top: 0px; left: 0px; width: 100%; height: 80px; overflow: hidden; }");

    scoutln(out, bytesOut, "#topLogo { padding: 0; margin: 0; position: absolute; top: 0px; height: 80px; overflow: hidden; }");
    scoutln(out, bytesOut, "#topUser { padding: 0; margin: 0; position: absolute; right: 0px; top: 0px; left: 200px; height: 20px; overflow: hidden; }");

    scoutln(out, bytesOut, ".user { color: darkgray; position:absolute; right: 0px; }");

    scoutln(out, bytesOut, "#history { padding: 0; margin: 0; position: absolute; top: 80px; left: 0px; width: 100%; height: 22px; overflow: hidden; }");

    scoutln(out, bytesOut, "#main {	padding: 0; margin: 0; position: absolute; top: 102px; left: 0px; right: 0px; bottom: 0px; overflow: auto; }");

    scoutln(out, bytesOut, ".soria .dijitMenu { background-color: #E0E0E0; }"); // overrides
    scoutln(out, bytesOut, ".soria .dijitAccordionContainer-dijitContentPane { background-color: transparent; }"); // left and right panes
    scoutln(out, bytesOut, ".soria .dijitAccordionContainer { background-color: transparent; }"); // left and right panes
    scoutln(out, bytesOut, ".soria .dijitBorderContainer-dijitContentPane { background-color: transparent; }"); // centre pane
    scoutln(out, bytesOut, ".soria .dijitMenuBar { background-color: #f0f0f0; }");

    scoutln(out, bytesOut, "#dockButton{");
    scoutln(out, bytesOut, "background: url(http://" + men + imagesDir + "z147.gif) center center no-repeat;");
    scoutln(out, bytesOut, "text-indent: -4em; overflow: hidden; padding: 0 .75em; width: 2em;");
    scoutln(out, bytesOut, "*margin-left: 4em;  // IE only");
    scoutln(out, bytesOut, "*padding: 0 1.75em; // IE only");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "#undockButton{");
    scoutln(out, bytesOut, "background: url(http://" + men + imagesDir + "z0212.gif) center center no-repeat;");
    scoutln(out, bytesOut, "text-indent: -4em; overflow: hidden; padding: 0 .75em; width: 2em;");
    scoutln(out, bytesOut, "*margin-left: 4em;  // IE only");
    scoutln(out, bytesOut, "*padding: 0 1.75em; // IE only");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "</style>");

    scoutln(out, bytesOut, "<!--[if lt IE 7]>");
    scoutln(out, bytesOut, "<style type=\"text/css\">");
    scoutln(out, bytesOut, "#main {	height:expression(document.body.clientHeight-120); /* 80+20+20=120 */ width:expression(document.body.clientWidth-260); /* 200+20+20+20=260 */ }");
    scoutln(out, bytesOut, "</style>");
    scoutln(out, bytesOut, "<![endif]-->");

    scoutln(out, bytesOut, "<script type=\"text/javascript\">");
    scoutln(out, bytesOut, "dojo.require(\"dijit.layout.ContentPane\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.layout.BorderContainer\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.layout.AccordionContainer\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.MenuBar\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.MenuBarItem\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.PopupMenuBarItem\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.Menu\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.MenuItem\");");
    scoutln(out, bytesOut, "dojo.require(\"dijit.PopupMenuItem\");");
    scoutln(out, bytesOut, "dojo.require(\"dojo.data.ItemFileReadStore\");");
    scoutln(out, bytesOut, "dojo.require(\"dojox.image.ThumbnailPicker\");");
    scoutln(out, bytesOut, "dojo.require(\"dojo.parser\");");

    scoutln(out, bytesOut, "dojo.require(\"dijit.layout.StackContainer\");");

    scoutln(out, bytesOut, "dojo.require(\"dojo.back\");");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script>");

    scoutln(out, bytesOut, "function screenWidth(){return window.innerWidth;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "var appendedChild=null;");

    scoutln(out, bytesOut, "  function execJS(node){");
    scoutln(out, bytesOut, "  try{if(appendedChild != null)document.getElementsByTagName(\"head\")[0].removeChild(appendedChild);}catch(e){};");
    scoutln(out, bytesOut, "  var bSaf = (navigator.userAgent.indexOf('Safari') != -1);");
    scoutln(out, bytesOut, "  var bOpera = (navigator.userAgent.indexOf('Opera') != -1);");
    scoutln(out, bytesOut, "  var bMoz = (navigator.appName == 'Netscape');");
    scoutln(out, bytesOut, "  if (!node) return;");
    scoutln(out, bytesOut, "  var st = node.getElementsByTagName('SCRIPT');");
    scoutln(out, bytesOut, "  var strExec;");
    scoutln(out, bytesOut, "  for(var i=0;i<st.length; i++){");
    scoutln(out, bytesOut, "    if (bSaf) {");
    scoutln(out, bytesOut, "      strExec = st[i].innerHTML;");
    scoutln(out, bytesOut, "      st[i].innerHTML = \"\";");
    scoutln(out, bytesOut, "    } else if (bOpera) {");
    scoutln(out, bytesOut, "      strExec = st[i].text;");
    scoutln(out, bytesOut, "      st[i].text = \"\";");
    scoutln(out, bytesOut, "    } else if (bMoz) {");
    scoutln(out, bytesOut, "      strExec = st[i].textContent;");
    scoutln(out, bytesOut, "      st[i].textContent = \"\";");
    scoutln(out, bytesOut, "    } else {");
    scoutln(out, bytesOut, "      strExec = st[i].text;");
    scoutln(out, bytesOut, "      st[i].text = \"\";");
    scoutln(out, bytesOut, "    }");
    scoutln(out, bytesOut, "    try {");
    scoutln(out, bytesOut, "      var x = document.createElement(\"script\");");
    scoutln(out, bytesOut, "      x.type = \"text/javascript\";");
    scoutln(out, bytesOut, "      if ((bSaf) || (bOpera) || (bMoz))");
    scoutln(out, bytesOut, "        x.innerHTML = strExec;");
    scoutln(out, bytesOut, "      else x.text = strExec;");
    scoutln(out, bytesOut, "      document.getElementsByTagName(\"head\")[0].appendChild(x);");
    scoutln(out, bytesOut, "      appendedChild = x;");
    scoutln(out, bytesOut, "    } catch(e) {");
    scoutln(out, bytesOut, "      alert(e);");
    scoutln(out, bytesOut, "    }");
    scoutln(out, bytesOut, "  }");
    scoutln(out, bytesOut, "};");

    scoutln(out, bytesOut, "String.prototype.startsWith=function(str){return(this.match(\"^\"+str)==str)}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script>");
    scoutln(out, bytesOut, "var rtnService, rtnMenuName, rtnMenuEntry, rtnURL, rtnOptions, rtnDockable, rtnMessage, tree, contacts;");
    scoutln(out, bytesOut, "function stripInfo(responseText){");

    scoutln(out, bytesOut, "var x=0;var len=responseText.length;rtnService='';");
    scoutln(out, bytesOut, "try{");
    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "rtnService+=responseText.charAt(x++);");

    scoutln(out, bytesOut, "++x;rtnMenuName='';");
    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "rtnMenuName+=responseText.charAt(x++);");

    scoutln(out, bytesOut, "++x;rtnMenuEntry='';");
    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "rtnMenuEntry+=responseText.charAt(x++);");

    scoutln(out, bytesOut, "++x;rtnURL='';");
    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "rtnURL+=responseText.charAt(x++);");

    scoutln(out, bytesOut, "++x;rtnOptions='';");
    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "rtnOptions+=responseText.charAt(x++);");

    scoutln(out, bytesOut, "++x;rtnDockable='';");
    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "rtnDockable+=responseText.charAt(x++);");

    scoutln(out, bytesOut, "++x;rtnMessage='';");
    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "rtnMessage+=responseText.charAt(x++);");

    scoutln(out, bytesOut, "while(x<len&&responseText.charAt(x)!='\003')");
    scoutln(out, bytesOut, "++x;++x;");
    scoutln(out, bytesOut, "}catch(e){alert(e);};return x;}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script>");

    // function global variables =========================================

    scoutln(out, bytesOut, "var unm='" + unm + "',uty='" + uty + "',sid='" + sid + "';");
    scoutln(out, bytesOut, "var signedIn = false, centreDiv, pMenuBar, histMenu = null, historyMenu = null, hMenu = new Array(), hMenuPtr = 1, historySubMenu = null, backButton=null, backCount=0;");
    scoutln(out, bytesOut, "var histservs = new Array(), numHistItems=0;");
    scoutln(out, bytesOut, "var mlabs = new Array(), mservs = new Array(), mparms = new Array(), numMenuItems=0;");
    scoutln(out, bytesOut, "var blabs = new Array(), bservs = new Array(), bparms = new Array(), numBlogItems=0;");
    scoutln(out, bytesOut, "var clabs = new Array(), cservs = new Array(), cparms = new Array(), numContactItems=0;");
    scoutln(out, bytesOut, "var llabs = new Array(), lservs = new Array(), lparms = new Array(), numLibraryItems=0;");
    scoutln(out, bytesOut, "var alabs = new Array(), aservs = new Array(), aparms = new Array(), numAdminItems=0;");
    scoutln(out, bytesOut, "var wlabsR = new Array(), wservsR = new Array(), wparmsR = new Array(), numWaveRItems=0;");
    scoutln(out, bytesOut, "var ilabsR = new Array(), iservsR = new Array(), iparmsR = new Array(), numIMRItems=0;");
    scoutln(out, bytesOut, "var mlabsR = new Array(), mservsR = new Array(), mparmsR = new Array(), numMailRItems=0;");
    scoutln(out, bytesOut, "var olabsR = new Array(), oservsR = new Array(), oparmsR = new Array(), numOnlineRItems=0;");
    scoutln(out, bytesOut, "var opMenu,bMenu,cMenu,lMenu,aMenu,wMenuR,iMenuR,mMenuR,oMenuR,initCount,initCountR;");

    // function getHistItem =========================================

    scoutln(out, bytesOut, "function getHistItem(lab){var n=lab.target.attributes.id.value; var x=0,len=n.length; var r=''; while(x < len && n.charAt(x) != '_')r+=n.charAt(x++);  if(! isInteger(r))r=0;");
    scoutln(out, bytesOut, "if(r<numHistItems)eval(histservs[r]);}");

    // function getBackItem =========================================

    scoutln(out, bytesOut, "function getBackItem(){--backCount;if(backCount>=0&&backCount<numHistItems){eval(histservs[backCount]);}}");

    // function isInteger =========================================

    scoutln(out, bytesOut, "function isInteger(s) { return (s.toString().search(/^-?[0-9]+$/) == 0); }");

    // function getMenuItem =========================================

    scoutln(out, bytesOut, "function getMenuItem(lab){var x=0;while(x<numMenuItems&&mlabs[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numMenuItems)getHTML(mservs[x],mparms[x]);}");

    // function getBlogItem =========================================

    scoutln(out, bytesOut, "function getBlogItem(lab){var x=numBlogItems;while(x>=0&&blabs[x]!=lab.target.innerHTML)--x;"); // read backwards to pickup guide before a same-named blog entry
    scoutln(out, bytesOut, "if(x>=0)getHTML(bservs[x],bparms[x]);}");

    // function getContactItem =========================================

    scoutln(out, bytesOut, "function getContactItem(lab){var x=0;while(x<numContactItems&&clabs[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numContactItems)getHTML(cservs[x],cparms[x]);}");

    // function getLibraryItem =========================================

    scoutln(out, bytesOut, "function getLibraryItem(lab){var x=0;while(x<numLibraryItems&&llabs[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numLibraryItems)getHTML(lservs[x],lparms[x]);}");

    // function getAdminItem =========================================

    scoutln(out, bytesOut, "function getAdminItem(lab){var x=0;while(x<numAdminItems&&alabs[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numAdminItems)getHTML(aservs[x],aparms[x]);}");

    // function getWaveRItem =========================================

    scoutln(out, bytesOut, "function getWaveRItem(lab){var x=0;while(x<numWaveRItems&&wlabsR[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numWaveRItems)getHTML(wservsR[x],wparmsR[x]);}");

    // function getIMRItem =========================================

    scoutln(out, bytesOut, "function getIMRItem(lab){var x=0;while(x<numIMRItems&&ilabsR[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numIMRItems)getHTML(iservsR[x],iparmsR[x]);}");

    // function getMailRItem =========================================

    scoutln(out, bytesOut, "function getMailRItem(lab){var x=0;while(x<numMailRItems&&mlabsR[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numMailRItems)getHTML(mservsR[x],mparmsR[x]);}");

    // function getOnlineRItem =========================================

    scoutln(out, bytesOut, "function getOnlineRItem(lab){var x=0;while(x<numOnlineRItems&&olabsR[x]!=lab.target.innerHTML)++x;");
    scoutln(out, bytesOut, "if(x<numOnlineRItems)getHTML(oservsR[x],oparmsR[x]);}");

    // function postHTML =========================================

    scoutln(out, bytesOut, "function postHTML(servlet,postData){dojo.back.addToHistory(state);");
    scoutln(out, bytesOut, "var p=\"unm=" + unm + "&sid=\" + sid + \"&uty=\" + uty + \"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\" + postData;");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/\" + servlet;");
    scoutln(out, bytesOut, "dojo.xhrPost({ url: url, postData: p, sync: true, load: function(response){ handleSuccess(response); return response; },");
    scoutln(out, bytesOut, "error: function(response){ handleFailure(response); return response; },");
    scoutln(out, bytesOut, "handleAs: \"text\" });}");

    // function postForm =========================================

    scoutln(out, bytesOut, "function postForm(servlet,formID){dojo.back.addToHistory(state);");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/\" + servlet;");
    scoutln(out, bytesOut, "dojo.xhrPost({ url: url, form: formID, sync: true, load: function(response){ handleSuccess(response); try{initEditor();}catch(e){}; return response; },");
    scoutln(out, bytesOut, "error: function(response){ handleFailure(response); return response; },");
    scoutln(out, bytesOut, "handleAs: \"text\" });}");

    // function postFormScript =========================================

    scoutln(out, bytesOut, "function postFormScript(script,formID){dojo.back.addToHistory(state);");
    scoutln(out, bytesOut, "var url=\"http://" + men + "\" + script;");
    scoutln(out, bytesOut, "dojo.xhrPost({ url: url, form: formID, sync: true, load: function(response){ handleSuccess(response); return response; },");
    scoutln(out, bytesOut, "error: function(response){ handleFailure(response); return response; },");
    scoutln(out, bytesOut, "handleAs: \"text\" });}");

    // function getJSON =========================================

    scoutln(out, bytesOut, "function getJSON(servlet,args,callBack){dojo.back.addToHistory(state);");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/\" + servlet + \"?unm=\" + unm + \"&sid=\" + sid + \"&uty=\" + uty + \"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\" + args;");
    scoutln(out, bytesOut, "dojo.xhrGet({ url: url, load: eval(callBack), sync: true,");
    scoutln(out, bytesOut, "error: function(data){ return data; },");
    scoutln(out, bytesOut, "handleAs: \"json\" });}");

    // function getHTML =========================================

    scoutln(out, bytesOut, "function getHTML(servlet,args){dojo.back.addToHistory(state);");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/\" + servlet + \"?unm=\" + unm + \"&sid=\" + sid + \"&uty=\" + uty + \"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\" + args;");
    scoutln(out, bytesOut, "dojo.xhrGet({ url: url, sync: true, load: function(response){ handleSuccess(response); try{initEditor();}catch(e){}; return response; },");
    scoutln(out, bytesOut, "error: function(response){ handleFailure(response); return response; },");
    scoutln(out, bytesOut, "handleAs: \"text\" });}");

    // function putFORM =========================================

    scoutln(out, bytesOut, "function putFORM(formID,mod,cls){dojo.back.addToHistory(state);");
    scoutln(out, bytesOut, "enQueue('FORM',mod,cls,dojo.formToJson(formID));");
    scoutln(out, bytesOut, "}");

    // function handleSuccess ====================================

    scoutln(out, bytesOut, "var handleSuccess = function(responseText)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, " 	if(responseText != undefined) { ");
    scoutln(out, bytesOut, "      var infoLen = stripInfo(responseText);");

    scoutln(out, bytesOut, "      if(rtnDockable.length==0)centreDiv.innerHTML='';else centreDiv.innerHTML=\"<span style='position:absolute;right:0px;'>"
                               + "<input class='dockButton' type='button' id='dockButton' onclick='onDockButton()'><input class='undockButton' type='button' id='undockButton' onclick='onUnDockButton()'></span>\";");

    scoutln(out, bytesOut, "	  centreDiv.innerHTML += (responseText).substring(infoLen);         ");
    scoutln(out, bytesOut, "	      execJS(centreDiv);        ");

    scoutln(out, bytesOut, "      if(historyMenu==null){");
    scoutln(out, bytesOut, "        historyMenu = new dijit.Menu({});");
    scoutln(out, bytesOut, "        backButton=new dijit.PopupMenuBarItem({label: \"Back\", id: \"BackButton\"  , onClick: function(){getBackItem()}                                    });");
    scoutln(out, bytesOut, "        pMenuBar.addChild(backButton);");
    scoutln(out, bytesOut, "        histMenu=new dijit.PopupMenuBarItem({label: \"History\", id: \"History\", popup: historyMenu});");
    scoutln(out, bytesOut, "        pMenuBar.addChild(histMenu);");
    scoutln(out, bytesOut, "	  }");

    scoutln(out, bytesOut, "histservs[numHistItems]=rtnURL;");

    scoutln(out, bytesOut, "     historyMenu.addChild(  new dijit.MenuItem( { label: rtnMenuEntry, id: numHistItems++,  onClick: function(e){getHistItem(e)} }) , 0  );");

    scoutln(out, bytesOut, " ++backCount;	}");
    scoutln(out, bytesOut, "}");

    // function handleFailure ====================================

    scoutln(out, bytesOut, "var handleFailure = function(responseText)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "	if(responseText != undefined)");// ?
    scoutln(out, bytesOut, "		centreDiv.innerHTML = \"An Error Occurred: \" + responseText;");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "function onDockButton(){");
    scoutln(out, bytesOut, "if(rtnMenuName.length>0){"); // just-in-case
    scoutln(out, bytesOut, "var array=pMenuBar.getChildren();");
    scoutln(out, bytesOut, "var x=0,len=array.length;");
    scoutln(out, bytesOut, "var found=-1;");
    scoutln(out, bytesOut, "while(x<len){");
    scoutln(out, bytesOut, "if((array[x++].id)==rtnMenuName)found=x-1;}");
    scoutln(out, bytesOut, "if(found==-1){"); // still
    scoutln(out, bytesOut, "pMenuBar.addChild(new dijit.PopupMenuBarItem({label: rtnMenuName,id: rtnMenuName,popup: hMenu[hMenuPtr++]}));");

    scoutln(out, bytesOut, "found=len;}}");
    scoutln(out, bytesOut, "else {"); // menubar entry already exists

    scoutln(out, bytesOut, "                        var a = oMenuBar.getSubmenus();");

    scoutln(out, bytesOut, "var x=0,len=a.length;");
    scoutln(out, bytesOut, "var found=-1;");
    scoutln(out, bytesOut, "var s = \"Menu \" + rtnMenuName + \"yui\";");
    scoutln(out, bytesOut, "while(x<len){if((String)(a[x++]).startsWith(s))found=x-1;}"); // search all menubar submenus for the one we want
    scoutln(out, bytesOut, "if(found!=-1){"); // found the one we want

    scoutln(out, bytesOut, "a = hMenu[found].getItems();"); // check for duplicates
    scoutln(out, bytesOut, "x=0,len=a.length;");
    scoutln(out, bytesOut, "var found2=false;");

    scoutln(out, bytesOut, "while(x<len){   if((a[x].value) == rtnMenuEntry)found2=true; ++x;}"); // search all entries

    scoutln(out, bytesOut, "if(!found2){"); // no existing entry

    scoutln(out, bytesOut, "                        var id = oMenuBar.getItem(found).id;");
    scoutln(out, bytesOut, "                        hMenu[found] = new YAHOO.widget.Menu(rtnMenuName + id);");
    scoutln(out, bytesOut, "                        oMenuBar.getItem(found).cfg.setProperty(\"submenu\", hMenu[found]);");

    scoutln(out, bytesOut, "                        hMenu[found].addItems( eval(\"[[ { text: \\\"\" + rtnMenuEntry + \"\\\", url: \\\"\" + rtnURL + \"\\\" } ]] \") );");
    scoutln(out, bytesOut, "                        hMenu[found].render(id);  hMenu[found].hide(); oMenuBar.render(); ");
 
    scoutln(out, bytesOut, "           }");

    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "}");

    // function handleSuccessJSON ====================================

    scoutln(out, bytesOut, "var handleSuccessJSON = function(type,responseText,event,kwArgs)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, " 	if(responseText != undefined)");
    scoutln(out, bytesOut, "    {");
    scoutln(out, bytesOut, "      var infoLen = stripInfo(responseText);");
    scoutln(out, bytesOut, "    }");
    scoutln(out, bytesOut, "}");

    // function handleFailureJSOn ====================================

    scoutln(out, bytesOut, "var handleFailureJSON = function(type,responseText,event,kwArgs)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "      alert('F  ' + responseText);  ");
    scoutln(out, bytesOut, "}");

    // function signIn ==========================================

    scoutln(out, bytesOut, "function signIn(){dojo.back.addToHistory(state);getHTML('MainPageUtilscw','&p1=&p2=" + urlBit + "&p3=" + dnm + "');}");

    // function whatIsZaraStar ==========================================

    scoutln(out, bytesOut, "function whatIs(){getHTML('AboutZaraw','');}");

    // function postSignOn =========================================

    scoutln(out, bytesOut, "function postSignOn(servlet,formID){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/\" + servlet;");
    scoutln(out, bytesOut, "dojo.xhrPost({ url: url, form: formID, sync: true, load: function(data){");
    scoutln(out, bytesOut, "if(data!=undefined)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(data.res[0].msg=='AD'){centreDiv.innerHTML=\"<center><font color='red' size='7'><br/><br/><br/>Access Denied</font></center>\";}else");
    scoutln(out, bytesOut, "if(data.res[0].msg=='NL'){centreDiv.innerHTML=\"<center><font color='pink' size='7'><br/><br/><br/>Under Maintenance</font></center>\";}else");
    scoutln(out, bytesOut, "if((data.res[0].msg).startsWith('OK')){"); // { res: [ { "msg":"OKISysadmin\001d5dc24c5b915c0cc } ]}
    scoutln(out, bytesOut, "uty=data.res[0].msg.charAt(2);");
    scoutln(out, bytesOut, "var len=data.res[0].msg.length;var x=3;unm='';while(x<len&&data.res[0].msg.charAt(x)!='\001')unm+=data.res[0].msg.charAt(x++);");
    scoutln(out, bytesOut, "++x;sid='';while(x<len)sid+=data.res[0].msg.charAt(x++);");
    scoutln(out, bytesOut, "signedIn=true;document.getElementById('userText').innerHTML='User: <b>'+unm+'</b> &nbsp;';");// '<a href=\"\">Sign Out</a>'

    scoutln(out, bytesOut, "getHTML('SiteDisplayPageWave','');");
    if(activeOperations[0])
      scoutln(out, bytesOut, "var ch=opMenu.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
    if(activeBlogs[0] || activeOperations[0])
      scoutln(out, bytesOut, "ch=bMenu.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
    if(activeOperations[0] || activeWaves[0] || activeMail[0])
      scoutln(out, bytesOut, "ch=cMenu.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
    if(activeOperations[0] || activeWaves[0] || activeMail[0] || activeDocuments[0])
      scoutln(out, bytesOut, "ch=lMenu.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
    scoutln(out, bytesOut, "ch=aMenu.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
    if(activeWaves[0])
    {
      scoutln(out, bytesOut, "ch=oMenuR.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
      scoutln(out, bytesOut, "ch=wMenuR.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
    }
    if(activeMail[0])
    {
      scoutln(out, bytesOut, "ch=iMenuR.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
      scoutln(out, bytesOut, "ch=mMenuR.getChildren();if(ch&&ch!=\"undefined\"){dojo.forEach(ch,function(child){child.destroyRecursive();});}");
    }

    scoutln(out, bytesOut, "initCount=1;initCountR=1;");
    if(activeBlogs[0] || activeOperations[0])
      scoutln(out, bytesOut, "initBlogs();");
    if(activeOperations[0])
      scoutln(out, bytesOut, "initOperations();");
    if(activeOperations[0] || activeWaves[0] || activeMail[0])
      scoutln(out, bytesOut, "initContacts();");
    if(activeOperations[0] || activeWaves[0] || activeMail[0] || activeDocuments[0])
      scoutln(out, bytesOut, "initLibraries();");

    scoutln(out, bytesOut, "initAdmin();");
    if(activeWaves[0])
    {
      scoutln(out, bytesOut, "initOnlinesR();");
      scoutln(out, bytesOut, "initWavesR();");
    }
    if(activeMail[0])
      scoutln(out, bytesOut, "initIMsR();initMailsR();");

    scoutln(out, bytesOut, "}else{centreDiv.innerHTML=\"<center><font color='red' size='7'><br/><br/><br/>Unknown Access Denied</font></center>\";}");
    scoutln(out, bytesOut, "}},");
    scoutln(out, bytesOut, "error: function(data){centreDiv.innerHTML=\"<center><font color='red' size='7'><br/><br/><br/>Error: Access Denied: \"+data+\"</font></center>\";return data;},");
    scoutln(out, bytesOut, "handleAs: \"json\"});}");

    // function initEditor ==========================================

    scoutln(out, bytesOut, "function initEditor(){var code,type;try{type=document.forms['form8103'].typeForEditor.value;code=document.forms['form8103'].codeForEditor.value;}");
    scoutln(out, bytesOut, "catch(e){ type=document.forms['form5902'].typeForEditor.value;code=document.forms['form5902'].codeForEditor.value;        }");

    scoutln(out, bytesOut, "if(type=='B')getJSON('BlogsFetchPage','&p1='+code,'initEditorCallBack');else if(type=='I')getJSON('RATPageEditWave','&p1='+code,'initEditorCallBack');else ");
    scoutln(out, bytesOut, "{var d=document.getElementById('wikiview').contentWindow.document;d.designMode='on';d.write('.');}}");

    scoutln(out, bytesOut, "function initEditorCallBack(data){var text=data.res[0].msg;");
    scoutln(out, bytesOut, "var d=document.getElementById('wikiview').contentWindow.document;d.designMode='on';d.write(text); return data; /* stop hourglass cursor */ }");

    // function initOperations ==========================================

    scoutln(out, bytesOut, "function initOperations(){getJSON('AdminControlUtilsMain','','initOperationsCallBack');}");

    scoutln(out, bytesOut, "function initOperationsCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var operations = new dijit.layout.AccordionPane({ title:'Operations',  id: 'acc_operations' ,  refreshOnShow:true  }, dojo.doc.createElement('div')  );");
    scoutln(out, bytesOut, "dijit.byId('leftAccordion').addChild(operations,initCount++);  }");
    scoutln(out, bytesOut, "opMenu = new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "var lab;numMenuItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')opMenu.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ mlabs[numMenuItems]=z;mservs[numMenuItems]=eval('data.res[0].x'+x+'2');mparms[numMenuItems]=eval('data.res[0].x'+x+'3');++numMenuItems;");
    scoutln(out, bytesOut, "  opMenu.addChild(new dijit.MenuItem({ label: z, onClick: function(e) {getMenuItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "opMenu.placeAt(\"acc_operations\");opMenu.startup();");
    scoutln(out, bytesOut, "}");

    // function initBlogs ==========================================

    scoutln(out, bytesOut, "function initBlogs(){getJSON('AdminControlUtilsBlogs','','initBlogsCallBack');}");

    scoutln(out, bytesOut, "function initBlogsCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var blogs = new dijit.layout.AccordionPane({ title:'Blogs and Guides', id:'acc_blogs',  refreshOnShow:true    }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('leftAccordion').addChild(blogs,initCount++); } ");
    scoutln(out, bytesOut, "bMenu=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "var lab;numBlogItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')bMenu.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ blabs[numBlogItems]=z;bservs[numBlogItems]=eval('data.res[0].x'+x+'2');bparms[numBlogItems]=eval('data.res[0].x'+x+'3');++numBlogItems;");
    scoutln(out, bytesOut, "  bMenu.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getBlogItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "bMenu.placeAt(\"acc_blogs\"); bMenu.startup();     ");
    scoutln(out, bytesOut, "}");

    // function initContacts ==========================================

    scoutln(out, bytesOut, "function initContacts(){getJSON('AdminControlUtilsc','','initContactsCallBack');}");

    scoutln(out, bytesOut, "function initContactsCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var contacts = new dijit.layout.AccordionPane({ title:'Communications', id:'acc_contacts', refreshOnShow:true }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('leftAccordion').addChild(contacts,initCount++);   }");
    scoutln(out, bytesOut, "var lab;cMenu=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "numContactItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')cMenu.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ clabs[numContactItems]=z;cservs[numContactItems]=eval('data.res[0].x'+x+'2');cparms[numContactItems]=eval('data.res[0].x'+x+'3');++numContactItems;");
    scoutln(out, bytesOut, "  cMenu.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getContactItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "cMenu.placeAt(\"acc_contacts\"); cMenu.startup();    ");
    scoutln(out, bytesOut, "}");

    // function initLibraries ==========================================

    scoutln(out, bytesOut, "function initLibraries(){getJSON('AdminControlUtilsLibraries','','initLibrariesCallBack');}");

    scoutln(out, bytesOut, "function initLibrariesCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var libraries = new dijit.layout.AccordionPane({ title:'Libraries', id:'acc_libraries', refreshOnShow:true }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('leftAccordion').addChild(libraries,initCount++);   }");
    scoutln(out, bytesOut, "var lab;lMenu=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "numLibraryItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')lMenu.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ llabs[numLibraryItems]=z;lservs[numLibraryItems]=eval('data.res[0].x'+x+'2');lparms[numLibraryItems]=eval('data.res[0].x'+x+'3');++numLibraryItems;");
    scoutln(out, bytesOut, "  lMenu.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getLibraryItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "lMenu.placeAt(\"acc_libraries\"); lMenu.startup();    ");
    scoutln(out, bytesOut, "}");

    // function initAdmin ==========================================

    scoutln(out, bytesOut, "function initAdmin(){getJSON('AdminControlUtilsAdmin','','initAdminCallBack');}");

    scoutln(out, bytesOut, "function initAdminCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var admin=new dijit.layout.AccordionPane({ title:'Admin', id:'acc_admin', refreshOnShow:true }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('leftAccordion').addChild(admin,initCount++); }");
    scoutln(out, bytesOut, "var lab;aMenu=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "numAdminItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')aMenu.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ alabs[numAdminItems]=z;aservs[numAdminItems]=eval('data.res[0].x'+x+'2');aparms[numAdminItems]=eval('data.res[0].x'+x+'3');++numAdminItems;");
    scoutln(out, bytesOut, "  aMenu.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getAdminItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "aMenu.placeAt(\"acc_admin\");aMenu.startup();");
    scoutln(out, bytesOut, "}");

    // function initWavesR ==========================================

    scoutln(out, bytesOut, "function initWavesR(){getJSON('AdminControlUtilsWavesIncoming','','initWavesRCallBack');}");

    scoutln(out, bytesOut, "function initWavesRCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var waves=new dijit.layout.AccordionPane({ title:'Waves', id:'acc_wavesR', refreshOnShow:true }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('rightAccordion').addChild(waves,initCountR++); }");
    scoutln(out, bytesOut, "var lab;wMenuR=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "numWavesRItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')wMenuR.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ wlabsR[numWaveRItems]=z;wservsR[numWaveRItems]=eval('data.res[0].x'+x+'2');wparmsR[numWaveRItems]=eval('data.res[0].x'+x+'3');++numWaveRItems;");
    scoutln(out, bytesOut, "  wMenuR.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getWaveRItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "wMenuR.placeAt(\"acc_wavesR\");wMenuR.startup();");
    scoutln(out, bytesOut, "}");

    // function initIMsR ==========================================

    scoutln(out, bytesOut, "function initIMsR(){getJSON('AdminControlUtilsIMsIncoming','','initIMsRCallBack');}");

    scoutln(out, bytesOut, "function initIMsRCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var ims=new dijit.layout.AccordionPane({ title:'IMs', id:'acc_imsR', refreshOnShow:true }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('rightAccordion').addChild(ims,initCountR++); }");
    scoutln(out, bytesOut, "var lab;iMenuR=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "numIMsRItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')iMenuR.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ ilabsR[numIMRItems]=z;iservsR[numIMRItems]=eval('data.res[0].x'+x+'2');iparmsR[numIMRItems]=eval('data.res[0].x'+x+'3');++numIMRItems;");
    scoutln(out, bytesOut, "  iMenuR.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getIMRItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "iMenuR.placeAt(\"acc_imsR\");iMenuR.startup();");
    scoutln(out, bytesOut, "}");

    // function initMailsR ==========================================

    scoutln(out, bytesOut, "function initMailsR(){getJSON('AdminControlUtilsMailIncoming','','initMailsRCallBack');}");

    scoutln(out, bytesOut, "function initMailsRCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var mails=new dijit.layout.AccordionPane({ title:'Mail', id:'acc_mailsR', refreshOnShow:true }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('rightAccordion').addChild(mails,initCountR++); }");
    scoutln(out, bytesOut, "var lab;mMenuR=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "numMailsRItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')mMenuR.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ mlabsR[numMailRItems]=z;mservsR[numMailRItems]=eval('data.res[0].x'+x+'2');mparmsR[numMailRItems]=eval('data.res[0].x'+x+'3');++numMailRItems;");
    scoutln(out, bytesOut, "  mMenuR.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getMailRItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "mMenuR.placeAt(\"acc_mailsR\");mMenuR.startup();");
    scoutln(out, bytesOut, "}");

    // function initOnlinesR ==========================================

    scoutln(out, bytesOut, "function initOnlinesR(){getJSON('AdminControlUtilsOnline','','initOnlinesRCallBack');}");

    scoutln(out, bytesOut, "function initOnlinesRCallBack(data){var c=data.res[0].msg;");
    scoutln(out, bytesOut, "if(!signedIn){var onlines=new dijit.layout.AccordionPane({ title:'Online', id:'acc_onlinesR', refreshOnShow:true }, dojo.doc.createElement('div') );");
    scoutln(out, bytesOut, "dijit.byId('rightAccordion').addChild(onlines,initCountR++); }");
    scoutln(out, bytesOut, "var lab;oMenuR=new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");
    scoutln(out, bytesOut, "numOnlinesRItems=0;for(var x=1;x<=c;++x){");
    scoutln(out, bytesOut, "lab='data.res[0].x'+x+'1';z=eval(lab);if(z.charAt(0)=='-')oMenuR.addChild(new dijit.MenuSeparator());else");
    scoutln(out, bytesOut, "{ olabsR[numOnlineRItems]=z;oservsR[numOnlineRItems]=eval('data.res[0].x'+x+'2');oparmsR[numOnlineRItems]=eval('data.res[0].x'+x+'3');++numOnlineRItems;");
    scoutln(out, bytesOut, "  oMenuR.addChild(new dijit.MenuItem({ label: z, onClick: function(e){getOnlineRItem(e)}}));}");
    scoutln(out, bytesOut, "}");
    scoutln(out, bytesOut, "oMenuR.placeAt(\"acc_onlinesR\");oMenuR.startup();");
    scoutln(out, bytesOut, "}");

    // help =====================================================

    scoutln(out, bytesOut, "function help(service){getHTML('HelpguideJumpw','&p1='+service);}");

    // heartbeat ================================================

    scoutln(out, bytesOut, "var olTimerID = null;");
    scoutln(out, bytesOut, "var olSecs;");
    scoutln(out, bytesOut, "var olTimerRunning = false;");
    scoutln(out, bytesOut, "var olDelay = 5000;");
    scoutln(out, bytesOut, "var olReq;");

    scoutln(out, bytesOut, "function olInitializeTimer(secs)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "olStopClock();");
    scoutln(out, bytesOut, " try{ ol(); } catch(err) { } ");
    scoutln(out, bytesOut, "olStartTimer();");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "function olStopClock()");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "if(olTimerRunning) clearTimeout(olTimerID);");
    scoutln(out, bytesOut, "olTimerRunning = false;");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "function olStartTimer()");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "olTimerRunning = true;");
    scoutln(out, bytesOut, "olTimerID = self.setTimeout(\"olInitializeTimer()\", olDelay);");
    scoutln(out, bytesOut, "}   ");

    // fetch info
    scoutln(out, bytesOut, "var olreq;");
    scoutln(out, bytesOut, "function olinitRequest(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){olreq=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){olreq=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function ol(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/OnlineUsersaw?unm=\" + unm + \"&sid=\" + sid + \"&uty=\" + uty + \"&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";");
    scoutln(out, bytesOut, "var request = YAHOO.util.Connect.asyncRequest('GET', url, callbackHeartBeat);");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "var handleSuccessHB = function(o)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "	if(o.responseText !== undefined)");
    scoutln(out, bytesOut, "    {");

    scoutln(out, bytesOut, "var isExpanded;if(contacts.expanded==true)isExpanded=true;else isExpanded=false;");
    scoutln(out, bytesOut, "tree.removeChildren(contacts);");
    scoutln(out, bytesOut, "var len=o.responseText.length;");
    scoutln(out, bytesOut, "var name,status,x=0;while(x<len){");
    scoutln(out, bytesOut, "name='';");
    scoutln(out, bytesOut, "while(x<len&&o.responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "name+=o.responseText.charAt(x++);");
    scoutln(out, bytesOut, "++x;status='';");
    scoutln(out, bytesOut, "while(x<len&&o.responseText.charAt(x)!='\001')");
    scoutln(out, bytesOut, "status+=o.responseText.charAt(x++);++x;");
    scoutln(out, bytesOut, "var myobj = { label: name + ' ' + status, myID: \"name\" }; ");////////
    scoutln(out, bytesOut, "var xW = new YAHOO.widget.TextNode(myobj, contacts);");
    scoutln(out, bytesOut, "if(isExpanded)");
    scoutln(out, bytesOut, "contacts.setNodesProperty(\"expanded\", true, true);");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "    tree.draw();");

    scoutln(out, bytesOut, "	}");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "var handleFailureHB = function(o)");
    scoutln(out, bytesOut, "{");
    scoutln(out, bytesOut, "	if(o.responseText !== undefined)");
    scoutln(out, bytesOut, "    {");
    scoutln(out, bytesOut, "		rightDiv.innerHTML = \"<ul><li>Transaction id: \" + o.tId + \"</li>\";");
    scoutln(out, bytesOut, "		rightDiv.innerHTML += \"<li>HTTP status: \" + o.status + \"</li>\";");
    scoutln(out, bytesOut, "		rightDiv.innerHTML += \"<li>Status code message: \" + o.statusText + \"</li></ul>\";");
    scoutln(out, bytesOut, "	}");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "var callbackHeartBeat = { success: handleSuccessHB, failure: handleFailureHB };");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script type=\"text/javascript\">");
    scoutln(out, bytesOut, "var state = {");
    scoutln(out, bytesOut, "    back: function() { alert(\"Use the ZaraStar menubar options instead of the BACK button\"); },");
    scoutln(out, bytesOut, "    forward: function() { alert(\"Use the ZaraStar menubar options instead of the FORWARD button\"); }};");
    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script type=\"text/javascript\">");
    scoutln(out, bytesOut, "dojo.addOnLoad(function(){");

    scoutln(out, bytesOut, "  centreDiv=dojo.byId(\"centreDiv\");");

    scoutln(out, bytesOut, "    pMenuBar=new dijit.MenuBar({});");
    scoutln(out, bytesOut, "    pMenuBar.placeAt(\"history\");");

    if(p1.length() == 0)
      scoutln(out, bytesOut, "    getHTML('SiteDisplayPageWave','&p1=');");
    else scoutln(out, bytesOut, "    getHTML('BlogsDisplayBlogBuideDisplayWave','&p1=" + p1 + "');"); // call for help

    scoutln(out, bytesOut, "var accContainer=dijit.byId(\"leftAccordion\");");
    scoutln(out, bytesOut, "initCount=1;initCountR=1;");
    if(activeBlogs[0] || activeOperations[0])
      scoutln(out, bytesOut, "initBlogs();");
    if(activeOperations[0])
      scoutln(out, bytesOut, "initOperations();");
    if(activeOperations[0] || activeWaves[0] || activeMail[0])
      scoutln(out, bytesOut, "initContacts();");
    if(activeOperations[0] || activeWaves[0] || activeMail[0] || activeDocuments[0])
      scoutln(out, bytesOut, "initLibraries();");
    scoutln(out, bytesOut, "initAdmin();");
    scoutln(out, bytesOut, "accContainer.removeChild(dijit.byId(\"dummy\"));");
    scoutln(out, bytesOut, "accContainer.startup();");

    if(activeWaves[0] || activeMail[0])
    {
      scoutln(out, bytesOut, "accContainer=dijit.byId(\"rightAccordion\");");
      if(activeWaves[0])
        scoutln(out, bytesOut, "initOnlinesR();initWavesR();");
      if(activeMail[0])
        scoutln(out, bytesOut, "initIMsR();initMailsR();");
      scoutln(out, bytesOut, "accContainer.removeChild(dijit.byId(\"dummyR\"));");
      scoutln(out, bytesOut, "accContainer.startup();");
    }
    
    scoutln(out, bytesOut, "dojo.back.setInitialState(state);");

    scoutln(out, bytesOut, "});");

    scoutln(out, bytesOut, "function enQueue(type, mod, cls, args)");
    scoutln(out, bytesOut, "{");
    
    scoutln(out, bytesOut, "var env=\"unm:\" + unm + \",sid:\" + sid + \",uty:\" + uty + \",men:" + men + ",den:" + den + ",dnm:" + generalUtils.sanitise(dnm) + ",bnm:" + generalUtils.sanitise(bnm) + "\";");

    scoutln(out, bytesOut, "my_send('" + sid + "', type, mod, cls, args, env);");
    scoutln(out, bytesOut, "}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "</head>");

    // body =====================================================

    String release = authenticationUtils.getRelease();

    String logo = "";
    if(headerLogo[0].length() > 0)
      logo = "<a href=\"javascript:enQueue('POST','info','8101','')\"><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0' /></a>";

    scoutln(out, bytesOut, "<body class=\"soria\"    >");

    scoutln(out, bytesOut, "<script type=\"text/javascript\">stompConnect('" + sid + "');");
    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<div id=\"topx\">"); // topx

    scoutln(out, bytesOut, "<div id=\"topLogo\">" + logo + "</div>");

    scoutln(out, bytesOut, "<div>"); // rightStuff

    scoutln(out, bytesOut, "<div id=\"topUser\">"); // topUser
    scoutln(out, bytesOut, "<span class=\"user\"><b>ZaraStar " + release + "</b> &nbsp; <span id='userText'></span></span>");
    scoutln(out, bytesOut, "<script type=\"text/javascript\">var messageElement=document.getElementById('userText');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<a href=\"javascript:whatIs()\">What is ZaraStar?</a> &nbsp; <a href=\"javascript:signIn()\">Sign In</a>';");
    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "</div>");               // topUser

    scoutln(out, bytesOut, "</div>");  // rightStuff
    scoutln(out, bytesOut, "</div>"); // topx

    // History menubar ===================================================================================================

    scoutln(out, bytesOut, "</div>"); // history

    scoutln(out, bytesOut, "<div id=\"main\" dojoType=\"dijit.layout.BorderContainer\" gutters=\"true\">");

    scoutln(out, bytesOut, "<div dojoType=\"dijit.layout.AccordionContainer\" minSize=\"10\" style=\"width: 180px;\" id=\"leftAccordion\" region=\"left\" splitter=\"true\">");
    scoutln(out, bytesOut, "<div id=\"dummy\" dojoType=\"dijit.layout.AccordionPane\" title=\"\"></div>");
    scoutln(out, bytesOut, "</div>");

    scoutln(out, bytesOut, "<div id=\"centreDiv\" dojoType=\"dijit.layout.ContentPane\" splitter=\"true\" minSize=\"50\" sizeShare=\"50\" region=\"center\"></div>");

    if(activeWaves[0] || activeMail[0])
    {
      scoutln(out, bytesOut, "<div dojoType=\"dijit.layout.AccordionContainer\" minSize=\"10\" style=\"width: 180px;\" id=\"rightAccordion\" region=\"right\" splitter=\"true\">");
      scoutln(out, bytesOut, "<div id=\"dummyR\" dojoType=\"dijit.layout.AccordionPane\" title=\"\"></div>");
      scoutln(out, bytesOut, "</div>");
    }

    scoutln(out, bytesOut, "</div>");
    scoutln(out, bytesOut, "</div>");

    scoutln(out, bytesOut, "</body>");
    scoutln(out, bytesOut, "</html>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += (str.length() + 2);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getLibraryMenuItems(Connection con, Statement stmt, ResultSet rs, String unm, String uty) throws Exception
  {
    String s = "";

    s += "one\001OptionTabsWave\001&p1=A\001\001";
    s += "two\001OptionTabsWave\001&p1=A\001\002";
    s += "two-one\001OptionTabsWave\001&p1=A\001\003";
    s += "two-two\001OptionTabsWave\001&p1=A\001\001";
    s += "three\001OptionTabsWave\001&p1=A\001\002";
    s += "three-one\001OptionTabsWave\001&p1=A\001\002";
    s += "three-one-one\001OptionTabsWave\001&p1=A\001\003";
    s += "three-one-two\001OptionTabsWave\001&p1=A\001\001";
    s += "four\001OptionTabsWave\001&p1=A\001\001";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getAdminMenuItems(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String s = "";

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      s += "Access\001OptionTabsWave\001&p1=A\001";
      s += "Customise\001OptionTabsWave\001&p1=C\001";
      s += "Users\001OptionTabsWave\001&p1=U\001";
      s += "Backup\001OptionTabsWave\001&p1=B\001";
      s += "DataBase\001OptionTabsWave\001&p1=D\001";
    }
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getMailAccounts(String unm, String dnm)
  {
    String account, s = "";
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + pWord);
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AccountName FROM mailaccounts WHERE Owner = '" + unm + "'");

      while(rs.next())
      {
        account = rs.getString(1);
      
        s += (account + "MailZaraListMailWave\001&p1=" + account + "\001");
      }

      if(rs != null) rs.close();
      if(stmt != null) stmt.close();
      if(con != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("100k: " + e);
      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      }
      catch(Exception e2) { }
    }

    account = "xxx@xxx.com"; s += (account + "\001MailZaraListMailWave\001&p1=" + account + "\001");
 
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getForumCats(String dnm) throws Exception
  {
    String s = "";

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      String q = "SELECT Name FROM ratcat ORDER BY Position";

      rs = stmt.executeQuery(q);

      String name;

      while(rs.next())
      {
        name = rs.getString(1);

        s += (name + "\001RATCreateEditIssueWave\001&p1=" + name + "\001");
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getContactsMenuItems(Connection con, Statement stmt, ResultSet rs, String unm, String uty) throws Exception
  {
    String s = "";

    if(authenticationUtils.havePersonnel(con, stmt, rs))
    {
      s += "People\001CompanyPersonnelDirectoryw\001\001";
      s += "-\001\001\001";
    }
    
    if(uty.equals("I"))
    {
      try
      {
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT DISTINCT CompanyName FROM contacts WHERE Owner = '" + unm + "' ORDER BY CompanyName");

        String companyName;

        while(rs.next())
        {
          companyName = rs.getString(1);

          s += (companyName + "\001_8w\001&p1=" + companyName + "\001"); ///////
        }

        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
      catch(Exception e)
      {
        System.out.println(e);
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
      }
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildMenu(PrintWriter out, String menuItems, int[] bytesOut)
  {
    try
    {
      scoutln(out, bytesOut, "<script type=\"text/javascript\">");

      scoutln(out, bytesOut, "var lMenu;");
      scoutln(out, bytesOut, "dojo.addOnLoad(function(){");
      scoutln(out, bytesOut, "lMenu = new dijit.Menu({ style: \"border-style: none; background-color: transparent;\" });");

      int[] count = new int[1];  count[0] = 0;
      int[] upto  = new int[1];  upto[0] = 0;
      
      scoutln(out, bytesOut, buildMenu(menuItems, "lMenu", count, 0, upto));

      scoutln(out, bytesOut, "lMenu.placeAt(\"library\");");
      scoutln(out, bytesOut, "lMenu.startup();");
      scoutln(out, bytesOut, "});");
      scoutln(out, bytesOut, "</script>");
    }
    catch(Exception e) { System.out.println("100k: " + e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildMenu(String menuItems, String menuName, int[] count, int depth, int[] upto)
  {
    String s = "";
    int x = upto[0];
    
    try
    {
      int len = menuItems.length();
      char term;
      String label, servlet, args;

      while(x < len)
      {
        label = "";
        while(menuItems.charAt(x) != '\001')
          label += menuItems.charAt(x++);
        ++x;

        servlet = "";
        while(menuItems.charAt(x) != '\001')
          servlet += menuItems.charAt(x++);
        ++x;

        args = "";
        while(menuItems.charAt(x) != '\001')
          args += menuItems.charAt(x++);
        ++x;

        term = menuItems.charAt(x++);

        if(term == '\001')
        {
          // if has no subMenu
          if(label.startsWith("-"))
            s += menuName + ".addChild(new dijit.MenuSeparator());\n";
          else s += menuName + ".addChild(new dijit.MenuItem({ label: \"" + label + "\", onClick: function() {getHTML('" + servlet + "','" + args + "')} }));\n";
          if(depth > 0)
          {
            upto[0] = x;
            return s;
          }
        }
        else
        if(term == '\002') // subMenu coming...
        {
          s += "var p" + count[0] + "=lMenu.addChild(new dijit.Menu({}));\n";
          s += buildMenu(menuItems.substring(x), "p" + count[0], count, depth + 1, upto);
          //upto[0];
          s += menuName + ".addChild(new dijit.PopupMenuItem({ label: \"" + label + "\", popup: p" + count[0]++ + "}));\n";
        }
        else
        if(term == '\003') // not yet end of subMenu
        {
          if(label.startsWith("-"))
            s += menuName + ".addChild(new dijit.MenuSeparator());v";
          else s += menuName + ".addChild(new dijit.MenuItem({ label: \"" + label + "\", onClick: function() {getHTML('" + servlet + "','" + args + "')} }));\n";
        }
      }
    }
    catch(Exception e) { System.out.println("100k (2): " + e); }

    upto[0] = x;
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getSysTypeStates(Connection con, Statement stmt, ResultSet rs, boolean[] activeOperations, boolean[] activeDocuments, boolean[] activeMail, boolean[] activeBlogs, boolean[] activeWaves) throws Exception
  {
    activeOperations[0] = activeDocuments[0] = activeMail[0] = activeBlogs[0] = activeWaves[0] = false;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Type, Active FROM SysTypes");

      String type, active;

      while(rs.next())
      {
        type   = rs.getString(1);
        active = rs.getString(2);

        if(type.equals("Operations"))
        {
          if(active.equals("Y"))
            activeOperations[0] = true;
        }
        else
        if(type.equals("Documents"))
        {
          if(active.equals("Y"))
            activeDocuments[0] = true;
        }
        else
        if(type.equals("Mail"))
        {
          if(active.equals("Y"))
            activeMail[0] = true;
        }
        else
        if(type.equals("Blogs"))
        {
          if(active.equals("Y"))
            activeBlogs[0] = true;
        }
        else
        if(type.equals("Waves"))
        {
          if(active.equals("Y"))
            activeWaves[0] = true;
        }
      }

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }
}
