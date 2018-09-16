// =======================================================================================================================================================================================================
// System: ZaraStar Cart: ExtUser - Checkout
// Module: ExternalUserCartCheckout.java
// Author: C.K.Harvey
// Copyright (c) 2001-09 Christopher Harvey. All Rights Reserved.
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

public class ExternalUserCartCheckout extends HttpServlet
{
  GeneralUtils  generalUtils = new GeneralUtils();
  MessagePage  messagePage = new MessagePage();
  ServerUtils  serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils  directoryUtils = new DirectoryUtils();
  Profile  profile = new Profile();
  DrawingUtils  drawingUtils = new DrawingUtils();
  Customer customer = new Customer();
  AdminControlUtils adminControlUtils = new AdminControlUtils();
  
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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");      

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ExternalUserCartCheckout", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 914, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);

    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! adminControlUtils.notDisabled(con, stmt, rs, 914))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ExternalUserCartCheckout", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 914, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ExternalUserCartCheckout", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 914, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 914, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                  throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Product Cart</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    scoutln(out, bytesOut, "function submit(which){document.forms[0].p1.value=which;document.go.submit();}");

    scoutln(out, bytesOut, "function sameAddr(){");
    scoutln(out, bytesOut, "document.forms[0].shipAddr1.value=document.forms[0].billAddr1.value;");
    scoutln(out, bytesOut, "document.forms[0].shipAddr2.value=document.forms[0].billAddr2.value;");
    scoutln(out, bytesOut, "document.forms[0].shipAddr3.value=document.forms[0].billAddr3.value;");
    scoutln(out, bytesOut, "document.forms[0].shipAddr4.value=document.forms[0].billAddr4.value;");
    scoutln(out, bytesOut, "document.forms[0].shipAddr5.value=document.forms[0].billAddr5.value;");
    scoutln(out, bytesOut, "document.forms[0].shipCountry.value=document.forms[0].billCountry.value;}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else if(escape(code.charAt(x))=='%0A')code2+='\003';");
    scoutln(out, bytesOut, "else if(escape(code.charAt(x))=='%0D');");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    drawingUtils.outputPageFrame(con, stmt, rs, out, req, "914", "", "ExternalUserCartCheckout", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    drawingUtils.drawTitle(out, false, false, "ExternalUserCartCheckout", "", "Cart CheckOut", "914", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "<form name=go action=\"ExternalUserCartInbox\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\"" +men + "\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value=" + bnm + ">");
    scoutln(out, bytesOut, "<input type=hidden name=p1  value=''>");

    scoutln(out, bytesOut, "<table id=\"page\" border=0 cellspacing=1 cellpadding=1>");

    String[] customerCode = new String[1];  customerCode[0] = "";
    String[] companyName  = new String[1];  companyName[0] = "";
    String[] name         = new String[1];  name[0] = "";
    String[] jobTitle     = new String[1];  jobTitle[0] = "";
    String[] eMail        = new String[1];  eMail[0] = "";

    String phone="", fax="", shipAddr1="", shipAddr2="", shipAddr3="", shipAddr4="", shipAddr5="", shipCountry="", addr1="", addr2="", addr3="", addr4="", addr5="", country="", band="";
    
    if(uty.equals("R"))
    {
      profile.getExternalAccessDetails(unm.substring(0, unm.length() - 1), dnm, localDefnsDir, defnsDir, name, companyName, jobTitle, customerCode, eMail);
      byte[] data = new byte[5000]; // plenty
      if(customer.getCompanyRecGivenCode(con, stmt, rs, customerCode[0], '\000', dnm, data, localDefnsDir, defnsDir) == 0)
      {
        phone       = generalUtils.dfsAsStr(data, (short)13);
        fax         = generalUtils.dfsAsStr(data, (short)15);
        shipAddr1   = generalUtils.dfsAsStr(data, (short)39);
        shipAddr1   = generalUtils.dfsAsStr(data, (short)40);
        shipAddr2   = generalUtils.dfsAsStr(data, (short)41);
        shipAddr3   = generalUtils.dfsAsStr(data, (short)42);
        shipAddr4   = generalUtils.dfsAsStr(data, (short)43);
        shipAddr5   = generalUtils.dfsAsStr(data, (short)44);
        addr1       = generalUtils.dfsAsStr(data, (short)5);
        addr2       = generalUtils.dfsAsStr(data, (short)6);
        addr3       = generalUtils.dfsAsStr(data, (short)7);
        addr4       = generalUtils.dfsAsStr(data, (short)8);
        addr5       = generalUtils.dfsAsStr(data, (short)9);
        country     = generalUtils.dfsAsStr(data, (short)12);
        shipCountry = country;
      }
      
      band = generalUtils.intToStr(customer.getPriceBand(con, stmt, rs, customerCode[0]));
    }
    
    scoutln(out, bytesOut, "<input type=hidden name=band value=" + band + ">");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Company Code: &nbsp;</td><td><p>" + customerCode[0] + "</td></tr>");
    scoutln(out, bytesOut, "<input type=hidden name=companyCode value=" + customerCode[0] + ">");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Company Name: &nbsp;</td><td colspan=3><input type=text name=companyName maxlength=60 size=60 value=\"" + companyName[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Your Reference: &nbsp;</td><td colspan=3><input type=text name=reference maxlength=30 size=30></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Your Name: </td><td nowrap><input type=text name=fao maxlength=40 size=25 value=\"" + name[0] + "\">");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Designation: &nbsp;</td><td nowrap><input type=text name=designation maxlength=40 size=25 value=\"" + jobTitle[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>eMail: </td><td colspan=3><input type=text name=eMail maxlength=40 size=40 value=\"" + eMail[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Phone: </td><td nowrap><input type=text name=phone maxlength=40 size=25 value=\"" + phone + "\">");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Fax: </td><td nowrap><input type=text name=fax maxlength=40 size=25 value=\"" + fax + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Notes (upto 300 characters): &nbsp;</td>");
    scoutln(out, bytesOut, "<td colspan=3><textarea name=notes cols=40 rows=6></textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap colspan=2><p><b>If you are submitting a <i>Confirmed Order</i>, please provide the following:</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Billing Address: &nbsp;</td><td colspan=3><input type=text name=billAddr1 maxlength=40 size=40 value=\"" + addr1 + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=billAddr2 maxlength=40 size=40 value=\"" + addr2 + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=billAddr3 maxlength=40 size=40 value=\"" + addr3 + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=billAddr4 maxlength=40 size=40 value=\"" + addr4 + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=billAddr5 maxlength=40 size=40 value=\"" + addr5 + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td align=right nowrap><p>Country: &nbsp;</td><td>");
    getCountries(con, stmt, rs, out, "bill", country, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><p><a href=\"javascript:sameAddr()\">Click here</a> if the shipping address is the same as the billing address</td></tr>");
    scoutln(out, bytesOut, "<tr><td nowrap><p>Shipping Address: &nbsp;</td><td colspan=3><input type=text name=shipAddr1 maxlength=40 size=40 value=\""
                           + shipAddr1 + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=shipAddr2 maxlength=40 size=40 value=\"" + shipAddr2 + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=shipAddr3 maxlength=40 size=40 value=\"" + shipAddr3 + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=shipAddr4 maxlength=40 size=40 value=\"" + shipAddr4 + "\"></td></tr>");
    scoutln(out, bytesOut, "<tr><td></td><td colspan=3><input type=text name=shipaddr5 maxlength=40 size=40 value=\"" + shipAddr5 + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td align=right nowrap><p>Country: &nbsp;</td><td>");
    getCountries(con, stmt, rs, out, "ship", shipCountry, bytesOut);
    scoutln(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=3><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p><b>Options:&nbsp;&nbsp;</td>");

    scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:submit('E')\">Submit as Enquiry</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"javascript:submit('S')\">Submit as Confirmed Order</a></td></tr>\n");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCountries(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String which, String statedCountry, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT Name FROM country ORDER BY Position");

    scoutln(out, bytesOut, "<select name=\"" + which + "Country\">");
    
    String name;
    
    while(rs.next())
    {
      name = rs.getString(1);
      scoutln(out, bytesOut, "<option value=\"" + name + "\"");
      if(name.equals(statedCountry))        
        scoutln(out, bytesOut, " selected");
      scoutln(out, bytesOut, ">" + name);
    }

    scoutln(out, bytesOut, "</select>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
