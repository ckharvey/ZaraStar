//====================================================================================================================================================================================================================================================
// System: ZaraStar: Fax: get status data from local and fax server
// Module: FaxGetStatusData.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
//====================================================================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;

public class FaxGetStatusData extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  FaxUtils faxUtils = new FaxUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
      }

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 11050: " + e));
      res.getWriter().write("Unexpected System Error: 11050");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String faxServer     = serverUtils.serverToCall("FAX", localDefnsDir);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    String rtn="Unexpected System Error: 11050";
    
    String stuff="";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      stuff = mergeData(faxServer, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir);
      if(stuff.length() > 0)
        rtn = ".";
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    String s = "<msg><res>" + rtn + "</res><stuff><![CDATA[" + stuff + "]]></stuff></msg>";
    res.getWriter().write(s);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11050, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String mergeData(String faxServer, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir) throws Exception
  {        
    String faxes = getFaxes(faxServer, unm, sid, uty, men, den, dnm, bnm);

    String[] faxCode      = new String[1];
    String[] dateTime     = new String[1];
    String[] number       = new String[1];
    String[] company      = new String[1];
    String[] signOn       = new String[1];
    String[] documentType = new String[1];
    String[] documentCode = new String[1];
    String[] companyCode  = new String[1];
    String[] companyType  = new String[1];
            
    String rtn = "", hylafaxID, status, cssFormat="";
    
    int x=0, len=faxes.length();
    while(x < len)
    {
      hylafaxID="";
      while(x < len && faxes.charAt(x) != '\001')
        hylafaxID += faxes.charAt(x++);
      ++x;

      status="";
      while(x < len && faxes.charAt(x) != '\001')
        status += faxes.charAt(x++);
      ++x;
      
      if(status.length() == 0)
      {
        if(hylafaxID.charAt(0) == 's')
          status = "Sending";
        else status = "Sent"; // 'd'
      }

      faxUtils.getDetailsGivenHylafaxCode(hylafaxID.substring(1), faxCode, dateTime, number, company, signOn, documentType, documentCode, companyCode, companyType, dnm, localDefnsDir, defnsDir);

      String documentName;
      
      switch(documentType[0].charAt(0))
      {
        case 'Q' : documentName = "Quotation";          break;
        case 'O' : documentName = "Order Confirmation"; break;
        case 'P' : documentName = "Picking List";       break;
        case 'R' : documentName = "Proforma Invoice";   break;
        case 'Y' : documentName = "Purchase Order";     break;
        case 'Z' : documentName = "Local Requisition";  break;
        case 'I' : documentName = "Invoice";            break;
        case 'E' : documentName = "Receipt";            break;
        case 'C' : documentName = "Credit Note";        break;
        case 'A' : documentName = "Payment Advice";     break;
        default  : documentName = "";                   break;       
      }
      
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      rtn += "<tr id=\"" + cssFormat + "\"><td valign=top><p>" + faxCode[0] + "</td><td valign=top><p>" + signOn[0] + "</td><td valign=top><p>" + companyCode[0] + "</td><td valign=top><p>" + company[0] + "</td><td valign=top><p>" + number[0]
          + "</td><td valign=top><p>" + documentName + "</td><td valign=top><p><a href=\"javascript:view('" + documentType[0] + "','" + documentCode[0] + "')\">" + documentCode[0] + "</a></td><td valign=top><p>" + dateTime[0]
          + "</td><td valign=top><p>" + status + "</td>";
      
      if(status.equals("Sending"))
        rtn += "<td><a href=\"javascript:abort('" + hylafaxID.substring(1) + "')\">Abort</a></td>";
                
      rtn += "</tr>";
    }            

    if(rtn.length() == 0)
      rtn = "<tr><td>&nbsp;</td></tr><tr><td><p>The Fax Queue is Empty.</td></tr><tr><td>&nbsp;</td></tr><tr><td><p>Completed faxes are listed under the <i>History</i> option.</td></tr>";
    
    return rtn;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getFaxes(String faxServer, String unm, String sid, String uty, String men, String den, String dnm, String bnm) throws Exception
  {
    URL url = new URL("http://" + faxServer + "/central/servlet/FaxGetQueue?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    di.close();
    
    return s;
  }

}
