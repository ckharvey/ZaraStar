// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Batch Entries: fetch for edit
// Module: AccountsBatchEntriesEdit.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

public class AccountsBatchEntriesEdit extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", code="", date="", accCode="", year="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          code = value[0];
        else
        if(elementName.equals("p2"))
          date = value[0];
        else
        if(elementName.equals("p3"))
          accCode = value[0];
        else
        if(elementName.equals("p4"))
          year = value[0];
      }

      doIt(req, res, year, unm, sid, uty, dnm, code, date, accCode, bytesOut);
    }    
    catch(Exception e)
    {      
      System.out.println(("Unexpected System Error: 6023c: " + e));
      res.getWriter().write("Unexpected System Error: 6023c");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String year, String unm, String sid, String uty, String dnm,
                    String code, String date, String accCode, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    String[] docCode    = new String[1]; docCode[0]="";
    String[] amount     = new String[1]; amount[0]="";
    String[] drCr       = new String[1]; drCr[0]="";
    String[] remark     = new String[1]; remark[0]="";
    String[] currency   = new String[1]; currency[0]="";
    String[] baseAmount = new String[1]; baseAmount[0]="";
    String[] dlm        = new String[1]; dlm[0]="";
    
    String rtn = "Unexpected System Error: 6023c";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      if(fetch(dnm, year, code, generalUtils.convertDateToSQLFormat(date), accCode, docCode, amount, drCr, remark, currency, baseAmount, dlm, localDefnsDir,
               defnsDir))
      {
        rtn = ".";
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    docCode[0] = generalUtils.sanitiseForXML(docCode[0]);
    if(docCode[0].length() == 0)
      docCode[0] = ".";
    
    if(amount[0].length() == 0)
      amount[0] = "0";
    amount[0] = generalUtils.doubleDPs(amount[0], '2');
    
    if(drCr[0].length() == 0)
      drCr[0] = "D";
    
    remark[0] = generalUtils.sanitiseForXML(remark[0]);
    if(remark[0].length() == 0)
      remark[0] = ".";
    
    if(currency[0].length() == 0)
      currency[0] = ".";
    
    if(baseAmount[0].length() == 0)
      baseAmount[0] = "0";
    baseAmount[0] = generalUtils.doubleDPs(baseAmount[0], '2');
    
    if(dlm[0].length() == 0)
      dlm[0] = ".";
    
    String s = "<msg><res>" + rtn + "</res><transactionDate>" + date + "</transactionDate><accCode>" + accCode + "</accCode>"
             + "<docCode>" + docCode[0] + "</docCode><amount>" + amount[0] + "</amount><drCr>" + drCr[0] + "</drCr><remark>" + remark[0]
             + "</remark><currency>" + currency[0] + "</currency><baseAmount>" + baseAmount[0] + "</baseAmount><dlm>" + dlm[0] + "</dlm></msg>";

    res.getWriter().write(s);

    serverUtils.totalBytes(req, unm, dnm, 6023, bytesOut[0], 0, code);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean fetch(String dnm, String year, String code, String date, String accCode, String[] docCode, String[] amount, String[] drCr,
                        String[] remark, String[] currency, String[] baseAmount, String[] dlm, String localDefnsDir,
                        String defnsDir) throws Exception
  {
    Connection con         = null;
    Statement stmt         = null;
    ResultSet rs           = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM joubatchl WHERE Code = '" + generalUtils.sanitiseForSQL(code) + "' AND TransactionDate = {d '" + date + "'} AND AccCode = '" + accCode
                           + "'"); 
      int x;
      String signOn;
      
      if(rs.next())
      {
        docCode[0]               = rs.getString(3);
        amount[0]                = rs.getString(5);
        drCr[0]                  = rs.getString(6);
        signOn                   = rs.getString(7);
        remark[0]                = rs.getString(8);
        dlm[0]                   = rs.getString(10);
        currency[0]              = rs.getString(11);
        baseAmount[0]            = rs.getString(12);

        x=0;
        while(x < dlm[0].length() && dlm[0].charAt(x) != ' ') // just-in-case
          ++x;
        dlm[0] = signOn + " on " + dlm[0].substring(0, x);
      }
      
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return true;
    }
    catch(Exception e)
    {
      System.out.println("6023c: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }

}
