// =======================================================================================================================================================================================================
// System: ZaraStar Catalogs: Catalog List Definition
// Module: CatalogsListDefinition.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CatalogsListDefinition extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      String urlBit="";
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "CatalogsListDefinition", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7032, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7032, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "CatalogsListDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7032, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "CatalogsListDefinition", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7032, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    create(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7032, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Admin - Catalog List Definition</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    // update record to DB
    scoutln(out, bytesOut, "var req4;");    
    scoutln(out, bytesOut, "function initRequest4(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req4=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req4=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function updateN(){update('N');}");
    scoutln(out, bytesOut, "function updateE(){update('E');}");
    scoutln(out, bytesOut, "function update(newOrEdit){");
    scoutln(out, bytesOut, "var mfr=sanitise(document.forms[0].mfr.value);");
    scoutln(out, bytesOut, "var title=sanitise(document.forms[0].title.value);");
    scoutln(out, bytesOut, "var image=sanitise(document.forms[0].image.value);");
    scoutln(out, bytesOut, "var desc=sanitise(document.forms[0].desc.value);");
    scoutln(out, bytesOut, "var type;if(document.forms[0].type[0].checked)type='O';else type='I';");
    scoutln(out, bytesOut, "var catalogType;if(document.forms[0].catalogType[0].checked)catalogType='C';else if(document.forms[0].catalogType[1].checked)catalogType='L';else catalogType='B';");
    scoutln(out, bytesOut, "var publishCatalog;if(document.forms[0].publishCatalog.checked)publishCatalog='Y';else publishCatalog='N';");
    scoutln(out, bytesOut, "var publishPricing;if(document.forms[0].publishPricing.checked)publishPricing='Y';else publishPricing='N';");
    scoutln(out, bytesOut, "var publishAvailability;if(document.forms[0].publishAvailability.checked)publishAvailability='Y';else publishAvailability='N';");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/CatalogsListDefinitionUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + mfr + \"&p2=\" + title + \"&p3=\" + image + \"&p4=\" + desc "
                         + "+ \"&p5=\" + type + \"&p6=\" + catalogType + \"&p7=\" + publishCatalog + \"&p8=\" + publishPricing + \"&p9=\" "
                         +" + publishAvailability + \"&p10=\" + newOrEdit + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest4(url);");
    scoutln(out, bytesOut, "req4.onreadystatechange = processRequest4;");
    scoutln(out, bytesOut, "req4.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req4.send(null);}");

    scoutln(out, bytesOut, "function processRequest4(){");
    scoutln(out, bytesOut, "if(req4.readyState==4){");
    scoutln(out, bytesOut, "if(req4.status==200){");
    scoutln(out, bytesOut, "var res=req4.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
    // delete record from DB
    scoutln(out, bytesOut, "var req3;");    
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function del(mfr,type){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/CatalogsListDefinitionDelete?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape(mfr) + \"&p2=\" + type + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3(){");
    scoutln(out, bytesOut, "if(req3.readyState==4){");
    scoutln(out, bytesOut, "if(req3.status==200){");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{ var messageElement = document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML = '<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
    
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function edit(mfr,type){");
    scoutln(out, bytesOut, ";var url=\"http://" + men + "/central/servlet/CatalogsListDefinitionEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + sanitise(mfr) + \"&p2=\" + type + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open('GET',url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status==200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var mfr=req2.responseXML.getElementsByTagName(\"mfr\")[0].childNodes[0].nodeValue;document.forms[0].mfr.value=mfr;");
    scoutln(out, bytesOut, "var title=req2.responseXML.getElementsByTagName('title')[0].childNodes[0].nodeValue;document.forms[0].title.value=title;");
    scoutln(out, bytesOut, "var image=req2.responseXML.getElementsByTagName(\"image\")[0].childNodes[0].nodeValue;document.forms[0].image.value=image;");
    scoutln(out, bytesOut, "var desc=req2.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;document.forms[0].desc.value=desc;");
    scoutln(out, bytesOut, "var type=req2.responseXML.getElementsByTagName(\"type\")[0].childNodes[0].nodeValue;if(type=='O')document.forms[0].type[0].checked=true;else document.forms[0].type[1].checked=true;");
    scoutln(out, bytesOut, "var catalogType=req2.responseXML.getElementsByTagName(\"catalogType\")[0].childNodes[0].nodeValue;if(catalogType=='C')document.forms[0].catalogType[0].checked=true;else if(catalogType=='L')document.forms[0].catalogType[1].checked=true;else document.forms[0].catalogType[2].checked=true;");
    scoutln(out, bytesOut, "var publishCatalog=req2.responseXML.getElementsByTagName(\"publishCatalog\")[0].childNodes[0].nodeValue;if(publishCatalog=='Y')document.forms[0].publishCatalog.checked=true;");
    scoutln(out, bytesOut, "var publishPricing=req2.responseXML.getElementsByTagName(\"publishPricing\")[0].childNodes[0].nodeValue;if(publishPricing=='Y')document.forms[0].publishPricing.checked=true;");
    scoutln(out, bytesOut, "var publishAvailability=req2.responseXML.getElementsByTagName(\"publishAvailability\")[0].childNodes[0].nodeValue;if(publishAvailability=='Y')document.forms[0].publishAvailability.checked=true;");
    scoutln(out, bytesOut, "var messageElement=document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Catalog Entry</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('mfrbox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=mfr value=\"'+mfr+'\">'+mfr;");
    
    scoutln(out, bytesOut, "if(desc.length>0){");
    scoutln(out, bytesOut, "var messageElement=document.getElementById(desc);");
    scoutln(out, bytesOut, "messageElement.value+=desc;}");
    
    scoutln(out, bytesOut, "}else{var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='+')code2+='%2b';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "function refetchPage(){");
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/CatalogsListDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "CatalogsListDefinition", "", "7032", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - Catalog List Definition", "7032", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=7><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Manufacturer</td>");
    scoutln(out, bytesOut, "<td><p>Location</td>");
    scoutln(out, bytesOut, "<td><p>Catalog Type</td>");
    scoutln(out, bytesOut, "<td><p>Publish Catalog</td>");
    scoutln(out, bytesOut, "<td><p>Publish Pricing</td>");
    scoutln(out, bytesOut, "<td><p>Publish Availability</td>");
    scoutln(out, bytesOut, "<td><p>Title</td>");
    scoutln(out, bytesOut, "<td><p>Image</td>");
    scoutln(out, bytesOut, "<td><p>Description</td></tr>");
        
    list(con, stmt, rs, out, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=9><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td valign=top><span id=\"mfrbox\"><input type=text name=mfr size=20 maxlength=30></span></td>");

    scoutln(out, bytesOut, "<td xnowrap valign=top><p><input type=radio name=type>&nbsp;&nbsp;Local");
    scoutln(out, bytesOut, "<br><input type=radio name=type>&nbsp;&nbsp;Linked</td>");
    
    scoutln(out, bytesOut, "<td xnowrap valign=top><p><input type=radio name=catalogType>&nbsp;&nbsp;Catalog");
    scoutln(out, bytesOut, "<br><input type=radio name=catalogType>&nbsp;&nbsp;Listing");
    scoutln(out, bytesOut, "<br><input type=radio name=catalogType>&nbsp;&nbsp;Both</td>");
    
    scoutln(out, bytesOut, "<td xnowrap valign=top><p><input type=checkbox name=publishCatalog>&nbsp;&nbsp;Publish Catalog</td>");
    scoutln(out, bytesOut, "<td xnowrap valign=top><p><input type=checkbox name=publishPricing>&nbsp;&nbsp;Publish Pricing</td>");
    scoutln(out, bytesOut, "<td xnowrap valign=top><p><input type=checkbox name=publishAvailability>&nbsp;&nbsp;Publish Availabilty</td>");

    scoutln(out, bytesOut, "<td valign=top><input type=text name=title size=40 maxlength=100></td>");
    scoutln(out, bytesOut, "<td valign=top><input type=text name=image size=40 maxlength=100></td>");

    scoutln(out, bytesOut, "<td valign=top><textarea id=\"desc\" name=\"desc\" cols=\"50\" rows=\"3\"></textarea></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Catalog Entry</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM cataloglist ORDER BY Manufacturer");

      String mfr, title, image, desc, type, catalogType, publishCatalog, publishPricing, publishAvailability, cssFormat;
      boolean line1=true;

      while(rs.next())                  
      {
        mfr                 = rs.getString(1);
        title               = rs.getString(2);
        image               = rs.getString(3);
        desc                = rs.getString(4);
        type                = rs.getString(5);
        catalogType         = rs.getString(6);
        publishCatalog      = rs.getString(7);
        publishPricing      = rs.getString(8);
        publishAvailability = rs.getString(9);
        
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        scoutln(out, bytesOut, "<td xnowrap>" + mfr + "</td>");
        
        String s;
        if(type.equals("I"))
          s = "Linked";
        else s = "Local";
        
        if(catalogType.equals("C"))
          catalogType = "Catalog";
        else
        if(catalogType.equals("L"))
          catalogType = "Listing";
        else catalogType = "Both";
        
        if(publishCatalog.equals("Y"))
          publishCatalog = "Yes";
        else publishCatalog = "No";
        
        if(publishPricing.equals("Y"))
          publishPricing = "Yes";
        else publishPricing = "No";
        
        if(publishAvailability.equals("Y"))
          publishAvailability = "Yes";
        else publishAvailability = "No";
        
        scoutln(out, bytesOut, "<td xnowrap>" + s + "</td>");
        scoutln(out, bytesOut, "<td xnowrap>" + catalogType + "</td>");
        scoutln(out, bytesOut, "<td xnowrap>" + publishCatalog + "</td>");
        scoutln(out, bytesOut, "<td xnowrap>" + publishPricing + "</td>");
        scoutln(out, bytesOut, "<td xnowrap>" + publishAvailability + "</td>");
        scoutln(out, bytesOut, "<td xnowrap>" + title + "</td>");
        scoutln(out, bytesOut, "<td xnowrap>" + image + "</td>");
        scoutln(out, bytesOut, "<td xnowrap>" + desc + "</td>");

        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + mfr + "','" + type + "')\">Edit</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        scoutln(out, bytesOut, "<a href=\"javascript:del('" + mfr + "','" + type + "')\">Delete</a></td></tr>");
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
