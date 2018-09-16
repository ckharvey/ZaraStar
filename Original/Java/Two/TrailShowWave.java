// =======================================================================================================================================================================================================
// System: ZaraStar: Util: Show trail
// Module: TrailShow.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class TrailShowWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();

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
      p1  = req.getParameter("p1"); // docCode
      p2  = req.getParameter("p2"); // docType

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      System.out.println("11800: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "TrailShow", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11800, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 11800, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 13, unm, sid, uty, men, den, dnm, bnm, "TrailShow", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11800, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "TrailShow", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11800, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    scoutln(out, bytesOut, "11800\001Trail\001Trail: " + p1 + "\001javascript:getHTML('TrailShowWave','" + p1 + "')\001\001Y\001\001\003");

    set(con, stmt, stmt2, rs, rs2, out, p1, p2, unm, sid, uty, men, den, dnm, bnm, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11800, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String docCode, String docType, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    pageFrameUtils.drawTitleW(out, false, false, "", "", "", "", "", "", "Document Trail", "11800", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width='100%' border='0'>");

    scoutln(out, bytesOut, "<tr><td><p>For Document Code: &nbsp; &nbsp; " + docCode + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>User</td>");
    scoutln(out, bytesOut, "<td><p>Date</td>");
    scoutln(out, bytesOut, "<td><p>Service</td>");
    scoutln(out, bytesOut, "<td><p>Detail</td></tr>");

    getTrail(con, stmt, stmt2, rs, rs2, out, docCode, docType, unm, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getTrail(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, String docCode, String docType, String unm, int[] bytesOut) throws Exception
  {
    String services="";
    if(docType.equals("P")) // pl
    {
      services = "AND (Service = '3038' OR Service = '3039' OR Service = '3043' OR Service = '3044' OR Service = '3045' OR Service = '3046' OR Service = '3047' OR Service = '3048' OR Service = '3054' OR Service = '3056' OR Service = '4125')";
    }
    else
    if(docType.equals("Q")) // quote
    {
      services = "AND (Service = '4019' OR Service = '4020' OR Service = '4021' OR Service = '4022' OR Service = '4023' OR Service = '4024' OR Service = '4025' OR Service = '4026' OR Service = '4027' OR Service = '4028' OR Service = '4180')";
    }
    else
    if(docType.equals("Q")) // enquiry
    {
      services = "AND (Service = '4519' OR Service = '4520' OR Service = '4523' OR Service = '4524' OR Service = '4525' OR Service = '4526' OR Service = '4527' OR Service = '4528' OR Service = '4531')";
    }
    else
    if(docType.equals("C")) // cn
    {
      services = "AND (Service = '4101' OR Service = '4102' OR Service = '4103' OR Service = '4104' OR Service = '4105' OR Service = '4106' OR Service = '4107' OR Service = '4108')";
    }
    else
    if(docType.equals("W")) // pcn
    {
      services = "AND (Service = '5026' OR Service = '5027' OR Service = '5028' OR Service = '5029' OR Service = '5030' OR Service = '5031' OR Service = '5032' OR Service = '5033')";
    }
    else
    if(docType.equals("D")) // do
    {
      services = "AND (Service = '4054' OR Service = '4055' OR Service = '4056' OR Service = '4057' OR Service = '4058' OR Service = '4059' "
               + "OR Service = '4060' OR Service = '4061' OR Service = '4062' OR Service = '4063' OR Service = '4064' OR Service = '4123')";
    }
    else
    if(docType.equals("G")) // grn
    {
      services = "AND (Service = '3025' OR Service = '3026' OR Service = '3027' OR Service = '3028' OR Service = '3029' OR Service = '3030' OR Service = '3031' OR Service = '3032' OR Service = '3033' OR Service = '3034')";
    }
    else
    if(docType.equals("X")) // stock
    {
      services = "AND (Service = '3001' OR Service = '3002' OR Service = '3003' OR Service = '3004' OR Service = '3005' OR Service = '3006' OR Service = '3007' OR Service = '3008')";
    }
    else
    if(docType.equals("I")) // invoice
    {
      services = "AND (Service = '4067' OR Service = '4224' OR Service = '4068' OR Service = '4069' OR Service = '4070' OR Service = '4071' "
               + "OR Service = '4072' OR Service = '4073' OR Service = '4074' OR Service = '4075' OR Service = '4076' OR Service = '4077' OR Service = '4124')";
    }
    else
    if(docType.equals("J")) // purchase invoice
    {
      services = "AND (Service = '5080' OR Service = '5081' OR Service = '5082' OR Service = '5083' OR Service = '5084' OR Service = '5085' OR Service = '5086' OR Service = '5087' OR Service = '5088')";
    }
    else
    if(docType.equals("N")) // dn
    {
      services = "AND (Service = '4111' OR Service = '4112' OR Service = '4113' OR Service = '4114' OR Service = '4115' OR Service = '4116' OR Service = '4117' OR Service = '4118')";
    }
    else
    if(docType.equals("M")) // pdn
    {
      services = "AND (Service = '5036' OR Service = '5037' OR Service = '5038' OR Service = '5039' OR Service = '5040' OR Service = '5041' OR Service = '5042' OR Service = '5043')";
    }
    else
    if(docType.equals("R")) // proforma
    {
      services = "AND (Service = '4080' OR Service = '4081' OR Service = '4082' OR Service = '4083' OR Service = '4084' OR Service = '4085' OR Service = '4086' OR Service = '4087' OR Service = '4088' OR Service = '4089')";
    }
    else
    if(docType.equals("V")) // payment voucher
    {
      services = "AND (Service = '6067' OR Service = '6068' OR Service = '6069' OR Service = '6070' OR Service = '6071' OR Service = '6072' OR Service = '6073' OR Service = '6075')";
    }
    else
    if(docType.equals("U")) // receipt voucher
    {
      services = "AND (Service = '6056' OR Service = '6057' OR Service = '6058' OR Service = '6059' OR Service = '6060' OR Service = '6061' OR Service = '6062' OR Service = '6063' OR Service = '6065')";
    }
    else
    if(docType.equals("E")) // receipt
    {
      services = "AND (Service = '4205' OR Service = '4206' OR Service = '4207' OR Service = '4208' OR Service = '4209' OR Service = '4210' OR Service = '4211' OR Service = '4212')";
    }
    else
    if(docType.equals("A")) // payment
    {
      services = "AND (Service = '5049' OR Service = '5050' OR Service = '5051' OR Service = '5052' OR Service = '5053' OR Service = '5054' OR Service = '5055' OR Service = '5056')";
    }
    else
    if(docType.equals("Y")) // po
    {
      services = "AND (Service = '5006' OR Service = '5007' OR Service = '5008' OR Service = '5009' OR Service = '5010' OR Service = '5011' "
               + "OR Service = '5012' OR Service = '5013' OR Service = '5072' OR Service = '4028' OR Service = '4180')";
    }
    else
    if(docType.equals("O")) // oc
    {
      services = "AND (Service = '4043' OR Service = '4044' OR Service = '4045' OR Service = '4046' OR Service = '4047' OR Service = '4048' OR Service = '4049' OR Service = '4050' OR Service = '4051')";
    }
    else
    if(docType.equals("B")) // oa
    {
      services = "AND (Service = '4130' OR Service = '4131' OR Service = '4132' OR Service = '4133' OR Service = '4134' OR Service = '4135' OR Service = '4136' OR Service = '4137' OR Service = '4138')";
    }
    else
    if(docType.equals("L")) // lp
    {
      services = "AND (Service = '5016' OR Service = '5017' OR Service = '5018' OR Service = '5019' OR Service = '5020' OR Service = '5021' OR Service = '5022' OR Service = '5023')";
    }
    else
    if(docType.equals("S")) // so
    {
      services = "AND (Service = '4031' OR Service = '4032' OR Service = '4033' OR Service = '4034' OR Service = '4035' OR Service = '4036' "
               + "OR Service = '4037' OR Service = '4038' OR Service = '4039' OR Service = '4040' OR Service = '4121')";
    }

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT UserCode, Service, DateTime, Text FROM trail WHERE Text LIKE '" + docCode + "%' " + services + " ORDER BY DateTime");

    String userCode, service, dateTime, text, cssFormat="";
    boolean wanted;

    while(rs.next())
    {
      userCode = rs.getString(1);
      service  = rs.getString(2);
      dateTime = rs.getString(3);
      text     = rs.getString(4);

      if(userCode.equals("Sysadmin"))
      {
        if(unm.equals("Sysadmin"))
          wanted = true;
        else wanted = false;
      }
      else wanted = true;

      if(wanted)
      {
        if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + authenticationUtils.getUserNameGivenUserCode(con, stmt2, rs2, userCode) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromTimestamp(dateTime) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + service + ": " + definitionTables.getDescriptionGivenService(con, stmt2, rs2, service) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + text + "</td></tr>");
      }
    }

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
