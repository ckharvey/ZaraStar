// =======================================================================================================================================================================================================
// System: ZaraStar Projects: Create/edit new project 
// Module: ProjectCreateEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProjectCreateEdit extends HttpServlet
{
  AccountsUtils accountsUtils = new AccountsUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ProjectUtils projectUtils = new ProjectUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // code (if edit)
      p2  = req.getParameter("p2"); // newOrEdit

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "N";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ProjectCreateEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6801, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String code, String newOrEdit, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ProjectCreateEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6801, bytesOut[0], 0, "ACC:" + code);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ProjectCreateEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6801, bytesOut[0], 0, "SID:" + code);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }    

    set(con, stmt, rs, out, req, code, newOrEdit, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6801, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String code, String newOrEdit, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  { 
    if(newOrEdit.equals("N"))
      scoutln(out, bytesOut, "<html><head><title>Projects - Create</title>");
    else scoutln(out, bytesOut, "<html><head><title>Projects - Edit</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    scoutln(out, bytesOut, "<script javascript=\"JavaScript\">function save(){document.forms[0].submit()}</script>");

    scoutln(out, bytesOut, "<form action=\"ProjectUpdateProject\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    
    int[] hmenuCount = new int[1];
    projectUtils.outputPageFrame(con, stmt, rs, out, req, "6801", "ProjectCreateEdit", "", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    projectUtils.drawTitle(con, stmt, rs, req, out, "ProjectCreateEdit", "Options", "6801", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"newOrEdit\"  value='" + newOrEdit + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"code\"  value='" + code + "'>");
      
    scoutln(out, bytesOut, "<table id=\"page\" width=100% cellpadding=3 cellspacing=3>");
      
    String[] title                 = new String[1]; title[0]                 = "";
    String[] requestedDeliveryDate = new String[1]; requestedDeliveryDate[0] = "";
    String[] enquiryDate           = new String[1]; enquiryDate[0]           = "";
    String[] customerReference     = new String[1]; customerReference[0]     = "";
    String[] product               = new String[1]; product[0]               = "";
    String[] note                  = new String[1]; note[0]                  = "";
    String[] endUser               = new String[1]; endUser[0]               = "";
    String[] contractor            = new String[1]; contractor[0]            = "";
    String[] country               = new String[1]; country[0]               = "";
    String[] currency              = new String[1]; currency[0]              = "";
    String[] quotedValue           = new String[1]; quotedValue[0]           = "";
    String[] remark                = new String[1]; remark[0]                = "";
    String[] dateOfPO              = new String[1]; dateOfPO[0]              = "";
    String[] dateIssuedToContracts = new String[1]; dateIssuedToContracts[0] = "";
    String[] status                = new String[1]; status[0]                = "";
    String[] dateOfReview          = new String[1]; dateOfReview[0]          = "";
    String[] reviewedBy            = new String[1]; reviewedBy[0]            = "";
    String[] statedDeliveryDate    = new String[1]; statedDeliveryDate[0]    = "";
    String[] companyCode           = new String[1]; companyCode[0]           = "";
    String[] owner                 = new String[1]; owner[0]                 = "";
    String[] checkedBy             = new String[1]; checkedBy[0]             = "";
    String[] dateCompleted         = new String[1]; dateCompleted[0]         = "";
    String[] dateIssuedToWorkshop  = new String[1]; dateIssuedToWorkshop[0]  = "";
    
    if(newOrEdit.equals("E"))
    {
      projectUtils.fetchProjectRec(code, title, requestedDeliveryDate, enquiryDate, customerReference, product, note, endUser, contractor, country, currency, quotedValue, remark, dateOfPO, dateIssuedToContracts, status, dateOfReview, reviewedBy,
                            statedDeliveryDate, companyCode, owner, checkedBy, dateCompleted, dateIssuedToWorkshop, dnm, localDefnsDir, defnsDir);
    }
    
    if(newOrEdit.equals("N"))
      scoutln(out, bytesOut, "<tr><td nowrap><p>Project Code</td><td><p>(New - Unallocated)</td>");
    else scoutln(out, bytesOut, "<tr><td nowrap><p>Project Code</td><td><p>" + code + "</td>");
    
    buildTitleDDL(out, title[0], dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td nowrap><p>Customer Code</td><td><input name=companyCode type=text size=20 value=\"" + companyCode[0]
                         + "\"></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Requested Delivery Date</td><td><input name=requestedDeliveryDate type=text size=10 "
                         + "value=\"" + generalUtils.convertFromYYYYMMDD(requestedDeliveryDate[0]) + "\"></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Stated Delvery Date</td><td><input name=statedDeliveryDate type=text size=10 value=\""
                         + generalUtils.convertFromYYYYMMDD(statedDeliveryDate[0]) + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Enquiry Date</td><td><input name=enquiryDate type=text size=10 value=\""
                         + generalUtils.convertFromYYYYMMDD(enquiryDate[0]) + "\"></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Customer Reference</td><td><input name=customerReference type=text size=25 value=\""
                         + customerReference[0] + "\" maxlength=40></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Product</td><td><input name=product value=\"" + product[0] + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Note</td><td colspan=5><input name=note type=text size=80 value=\"" + note[0] + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Owner</td><td><input name=owner type=text size=30 value=\"" + owner[0] + "\"></td>");
    
    scoutln(out, bytesOut, "<td nowrap><p>End User</td><td><input name=endUser type=text size=25 value=\"" + endUser[0]
                         + "\" maxlength=40></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Contractor</td><td><input name=contractor type=text size=25 value=\"" + contractor[0]
                         + "\" maxlength=40></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Country</td><td colspan=5>"
                           + documentUtils.getCountryDDL(con, stmt, rs, "country", country[0]) + "</td></tr>");

    if(newOrEdit.equals("N"))
      currency[0] = accountsUtils.getBaseCurrency(con, stmt, rs, dnm, localDefnsDir, defnsDir);

    scoutln(out, bytesOut, "<tr><td nowrap><p>Currency</td><td>" + accountsUtils.getCurrencyNamesDDL(con, stmt, rs, "currency", currency[0], dnm,
                                                                                              localDefnsDir, defnsDir)
                         + "</td>");
    
    scoutln(out, bytesOut, "<td nowrap><p>Quoted Value</td><td><input name=quotedValue type=text size=10 value=\""
                         + quotedValue[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap valign=top><p>Remark</td><td colspan=5><textarea name=remark rows=6 cols=50>" + remark[0]
                         + "</textarea></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>Date of PO</td><td><input name=dateOfPO type=text size=10 value=\""
                         + generalUtils.convertFromYYYYMMDD(dateOfPO[0]) + "\"></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date To Contracts</td><td><input name=dateIssuedToContracts type=text size=10 "
                         + "value=\"" + generalUtils.convertFromYYYYMMDD(dateIssuedToContracts[0]) + "\"></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date To Workshop</td><td><input name=dateIssuedToWorkshop type=text size=10 "
                         + "value=\"" + generalUtils.convertFromYYYYMMDD(dateIssuedToWorkshop[0]) + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Status</td><td>" + buildStatusDDL(status[0]) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date Of Review</td><td><input name=dateOfReview type=text size=10 value=\""
                         + generalUtils.convertFromYYYYMMDD(dateOfReview[0]) + "\"></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Reviewed By</td><td><input name=reviewedBy type=text size=30 value=\"" + reviewedBy[0]
                         + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td nowrap><p>Checked By</td><td><input name=checkedBy type=text size=30 value=\"" + checkedBy[0]
                         + "\"></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date Completed</td><td><input name=dateCompleted type=text size=10 value=\""
                         + generalUtils.convertFromYYYYMMDD(dateCompleted[0]) + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void buildTitleDDL(PrintWriter out, String thisTitle, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_project?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT Name FROM title ORDER BY Name");

    scoutln(out, bytesOut, "<td nowrap><p>Project Title</td><td colspan=3><select name=title>");
    
    String title;

    while(rs.next())
    {
      title = rs.getString(1);
      scoutln(out, bytesOut, "<option value=\"" + title + "\"");

      if(thisTitle.equals(title))
        scoutln(out, bytesOut, " selected");
      
      scoutln(out, bytesOut, ">" + title);
    }

    scoutln(out, bytesOut, "</select></td></tr>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildStatusDDL(String thisOption) throws Exception
  {
    String s = "<select name=\"status\">";

    s += "<option value=\"O\"";
    if(thisOption.equals("O"))
      s += " selected";      
    s += ">Open\n";      

    s += "<option value=\"C\"";
    if(thisOption.equals("C"))
      s += " selected";      
    s += ">Completed\n";      

    s += "<option value=\"P\"";
    if(thisOption.equals("P"))
      s += " selected";      
    s += ">Proposed\n";      

    s += "<option value=\"R\"";
    if(thisOption.equals("R"))
      s += " selected";      
    s += ">Rejected\n";      

    s += "<option value=\"A\"";
    if(thisOption.equals("A"))
      s += " selected";      
    s += ">Abandoned\n";      

    s += "<option value=\"X\"";
    if(thisOption.equals("X"))
      s += " selected";      
    s += ">Cancelled\n";      

    s += "</select>";
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}
