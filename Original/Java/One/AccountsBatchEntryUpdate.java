// =======================================================================================================================================================================================================
// System: ZaraStar Accounts: Batch Entry: Update rec
// Module: AccountsBatchEntryUpdate.java
// Author: C.K.Harvey
// Copyright (c) 1998-2007 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class AccountsBatchEntryUpdate extends HttpServlet
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

    String unm="", sid="", uty="", dnm="", code="", date="", docCode="", accCode="", amount="", drCr="", remark="", journal="", currency="", baseAmount="", newOrEdit="", year="";

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
          docCode = value[0];
        else
        if(elementName.equals("p4"))
          accCode = value[0];
        else
        if(elementName.equals("p5"))
          amount = value[0];
        else
        if(elementName.equals("p6"))
          drCr = value[0];
        else
        if(elementName.equals("p8"))
          remark = value[0];
        else
        if(elementName.equals("p9"))
          journal = value[0];
        else
        if(elementName.equals("p10"))
          currency = value[0];
        else
        if(elementName.equals("p11"))
          baseAmount = value[0];
        else
        if(elementName.equals("p12"))
          newOrEdit = value[0];
        else
        if(elementName.equals("p13"))
          year = value[0];
      }

      doIt(req, res, year, unm, sid, uty, dnm, code, date, docCode, accCode, amount, drCr, remark, journal, currency, baseAmount, newOrEdit,
           bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 6023a: " + e));
      res.getWriter().write("Unexpected System Error: 6023a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String year, String unm, String sid, String uty, String dnm,
                    String code, String date, String docCode, String accCode, String amount, String drCr, String remark, String journal,
                    String currency, String baseAmount, String newOrEdit, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
 
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
 
    String rtn="Unexpected System Error: 6023a";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      rtn = "Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      date       = generalUtils.stripAllSpaces(date);
      docCode    = generalUtils.stripAllSpaces(docCode);
      amount     = generalUtils.stripAllSpaces(amount);
      baseAmount = generalUtils.stripAllSpaces(baseAmount);
      remark     = generalUtils.stripLeadingAndTrailingSpaces(generalUtils.deSanitise(remark));

      if(code.length() == 0) // just-in-case
        rtn = "No Batch Code Entered";
      else
      if(amount.length() == 0)
        rtn = "No Amount Entered";
      else
      if(baseAmount.length() == 0)
        rtn = "No Base Amount Entered";
      else
      if(date.length() == 0)
        rtn = "No Date Entered";
      else
      if(! generalUtils.validateDate(true, date, localDefnsDir, defnsDir))
        rtn = "Invalid Date Entered";
      else
      if(! generalUtils.isNumeric(amount))
        rtn = "Invalid Amount Entered";
      else
      if(! generalUtils.isNumeric(baseAmount))
        rtn = "Invalid Base Amount Entered";
      else
      {
        if(! serverUtils.passLockCheck(con, stmt, rs, "journalbatch", generalUtils.convertDateToSQLFormat(date), unm))
          rtn = "Cannot use a Date in a Locked Period";
        else
        {
          boolean newRec;
          if(newOrEdit.equals("N"))
            newRec = true;
          else newRec = false;
        
          date = generalUtils.convertDateToSQLFormat(date);
        
          switch(update(newRec, year, unm, dnm, code, date, docCode, accCode, amount, drCr, remark, journal, currency, baseAmount))
          {
            case ' ' : rtn = ".";                    break;
            case 'E' : rtn = "Entry Already Exists"; break;
            case 'X' : rtn = "Failed to Update";     break;
          }
        }
      }
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");
    
    String s = "<msg><res>" + rtn + "</res></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6022, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), code);
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns: ' ' if rec added/updated ok
  //       'E' if adding a rec that already exists
  //       'X' other error
  private char update(boolean newRec, String year, String unm, String dnm, String code, String date, String docCode, String accCode, String amount, String drCr, String remark, String journal, String currency, String baseAmount)
                      throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      if(newRec)
      {    
        // if adding a new rec, check if accCode already exists
        boolean alreadyExists=false;
        stmt = con.createStatement();
  
        rs = stmt.executeQuery("SELECT TransactionDate, AccCode FROM joubatchl WHERE Code = '" + generalUtils.sanitiseForSQL(code) + "' AND TransactionDate = {d '" + date + "'} AND AccCode = '" + accCode + "'");
        if(rs.next())
          alreadyExists = true;
        rs.close();
  
        if(stmt != null) stmt.close();
      
        if(alreadyExists)
        {
          if(con != null) con.close();
          return 'E';
        }
  
        stmt = con.createStatement();
           
        String q = "INSERT INTO joubatchl ( Code, TransactionDate, DocCode, AccCode, Amount, DrCr, Remark, Journal, SignOn, "
                 + "Currency, BaseAmount, DateLastModified ) VALUES ( '" + generalUtils.sanitiseForSQL(code) + "','" + date + "','" + docCode + "','" + accCode + "','" + amount
                 + "','" + drCr + "','" + generalUtils.sanitiseForSQL(remark) + "','" + journal + "','" + unm + "','" + currency + "','" + baseAmount + "',NULL)";
        stmt.executeUpdate(q);
 
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        
        return ' ';
      }
  
      // else update existing rec
      stmt = con.createStatement();
        
      String q = "UPDATE joubatchl SET DocCode = '" + generalUtils.sanitiseForSQL(docCode) + "', AccCode = '" + accCode + "', Amount = '" + amount + "', DrCr = '" + drCr
               + "', Remark = '" + generalUtils.sanitiseForSQL(remark) + "', Journal = '" + journal + "', SignOn = '"
               + unm + "', Currency = '" + currency + "', BaseAmount = '" + baseAmount + "', DateLastModified = NULL WHERE Code = '" + generalUtils.sanitiseForSQL(code)
               + "' AND TransactionDate = {d '" + date + "'} AND AccCode = '" + accCode + "'";
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();        
      if(con  != null) con.close();
            
      return ' ';
    }
    catch(Exception e)
    {
      System.out.println("6023a: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return 'X';
  }

}

