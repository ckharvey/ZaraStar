// =======================================================================================================================================================================================================
// System: ZaraStar: Admin: db manager - create fixed files page
// Module: AdminDataBaseFixedFiles.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class AdminDataBaseFixedFiles extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  SignOnAdministrator  signOnAdministrator = new SignOnAdministrator();

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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminDataBaseFixedFiles", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7052, bytesOut[0], 0, "ERR:");
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

    if(! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "7052", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7052, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(true, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "7052", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7052, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
        
    set(con, stmt, rs, out, unm, sid, uty, men, den, dnm, bnm, imagesDir, defnsDir, bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7052, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty,  String men, String den, String dnm, String bnm, String imagesDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>DataBase Manager: Fixed Files</title>");
   
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    signOnAdministrator.heading(out, true, unm, sid, uty, bnm, dnm, men, den, imagesDir, bytesOut);

    dashboardUtils.drawTitle(out, "DataBase Manager", "7052", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<FORM ACTION=\"Admin:DataBaseFixedFilesCreate\" ENCTYPE=\"application/x-www-form-urlencoded\" METHOD=POST>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value="+bnm+">");

    scoutln(out, bytesOut, "<table id=\"page\" cellspacing=2 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td width=99%><p>Table Name</td></tr>");

    String name, namesList = names();
    int x = 0, len = namesList.length();
    while(x < len)
    {
      name = "";
      while(x < len && namesList.charAt(x) != '\001') // just-in-case
        name += namesList.charAt(x++);
      ++x;  
      forATableName(out, name, bytesOut);
    }
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td width=70><input type=image src=\"" + imagesDir + "go.gif\" name=C></td><td nowrap valign=center><p>Create New Table</td></tr>");

    scoutln(out, bytesOut, "<tr><td><input type=image src=\"" + imagesDir + "go.gif\" name=E></td><td nowrap valign=center><p>Export Table</td></tr>");

    scoutln(out, bytesOut, "<tr><td><input type=image src=\"" + imagesDir + "go.gif\" name=X></td><td nowrap valign=center><p>Export Table to XML</td></tr>");

    scoutln(out, bytesOut, "<tr><td><input type=image src=\"" + imagesDir + "go.gif\" name=I></td><td nowrap valign=center><p>Import Table</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></body></html>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void forATableName(PrintWriter out, String desc, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><p><input type=checkbox name=" + desc + "></td>");
    scoutln(out, bytesOut, "<td nowrap><p>" + desc + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += str.length() + 2;    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String names() throws Exception
  {      
    return "appconfig\001bankaccount\001buyerscatalog\001catalog\001catalogc\001catalogl\001cataloglist\001catalogs\001chat\001chatunread\001channelalerts\001channellinks\001channels\001channelsmine\001channelsopen\001"
         + "channelusers\001codes\001company\001companytype\001contacts\001contactgroups\001contactsharing\001country\001credit\001credita\001creditl\001creditll\001currency\001currrate\001cssstyles\001debit\001cycle\001cyclel\001"
         + "cycled\001debita\001debitl\001debitll\001demousers\001documentoptions\001documentsettings\001do\001doa\001dol\001doll\001driver\001enquiry\001enquirya\001enquiryl\001enquiryll\001externalaccess\001gr\001grl\001"
         + "gstrate\001iat\001inbox\001inboxl\001industrytype\001invoice\001invoicel\001invoicell\001invoicea\001lasttrail\001linkedcat\001locks\001lp\001lpa\001lpl\001lpll\001lr\001lra\001lrl\001lrll\001managepeople\001moduleservices\001oa\001oaa\001oal\001oall\001oc\001oca\001ocl\001"
         + "ocll\001organizations\001payment\001paymentl\001pcredit\001pcredita\001pcreditl\001pcreditll\001pdebit\001pdebita\001pdebitl\001pdebitll\001personposition\001pinvoice\001pinvoicel\001pinvoicell\001pinvoicea\001pl\001pla\001pll\001"
         + "plll\001po\001poa\001pol\001poll\001profiles\001proforma\001proformaa\001proformal\001proformall\001quolike\001quoreasons\001quostate\001quote\001quotea\001quotel\001"
         + "quotell\001receipt\001receiptl\001registrants\001robottext\001rvoucher\001rvouchera\001rvoucherl\001rvoucherll\001salesper\001scbrands\001scsites\001scsitesbrands\001so\001servers\001soa\001sol\001soll\001stock\001stocka\001stockc\001stockcat\001"
         + "stockopen\001stockx\001store\001storeman\001styling\001supplier\001system\001systemmodules\001systemsuspend\001trail\001usergroups\001usermodules\001users\001userservices\001voucher\001vouchera\001voucherl\001voucherll\001"
         + "wo\001woa\001wol\001woll\001";
  }

}
