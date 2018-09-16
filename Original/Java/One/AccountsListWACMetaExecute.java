// =======================================================================================================================================================================================================
// System: ZaraStar Utils: CSV
// Module: AccountsListWACMetaExecute.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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

public class AccountsListWACMetaExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // year

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsListWACMetaExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7002, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir    = directoryUtils.getUserDir('W', dnm, "/" + unm);

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    Connection conAcc = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + p1 + "?user=" + uName + "&password=" + pWord);
    Statement stmtAcc = null;
    ResultSet rsAcc   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      serverUtils.etotalBytes(req, unm, dnm, 7002, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      return;
    }

    RandomAccessFile fh = generalUtils.create(workingDir + "7002.csv");

    generate(con, conAcc, stmt, stmtAcc, rs, rsAcc, fh, p1, dnm, localDefnsDir, defnsDir);

    generalUtils.fileClose(fh);

    download(res, workingDir, "7002.csv", bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7002, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void generate(Connection con, Connection conAcc, Statement stmt, Statement stmtAcc, ResultSet rs, ResultSet rsAcc, RandomAccessFile fh, String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String[] yearStartDate = new String[1];
    String[] yearEndDate   = new String[1];

    accountsUtils.getAccountingYearStartAndEndDatesForAYear(con, stmt, rs, year, dnm, localDefnsDir, defnsDir, yearStartDate, yearEndDate);

    int x, yearStartDateEncoded = generalUtils.encodeFromYYYYMMDD(yearStartDate[0]);
    boolean comma = true, newLine = false;

    writeEntry(fh, "ItemCode", true, false);
    writeEntry(fh, "Opening",  true, false);

    for(x=0;x<366;++x)
    {
      if(x == 365)
      {
        comma   = false;
        newLine = true;
      }

      writeEntry(fh, generalUtils.decodeToYYYYMMDD(yearStartDateEncoded + x), comma, newLine);
    }

    comma = true;
    newLine = false;

    try
    {
      stmtAcc = conAcc.createStatement();

      rsAcc = stmtAcc.executeQuery("SELECT * FROM wacmeta ORDER BY ItemCode");

      String itemCode, openingWAC;
      double openingWACD, lastWAC, wac;
      double[] wacs = new double[368];
      boolean atLeastOneValue;

      while(rsAcc.next())
      {
        comma   = true;
        newLine = false;
     
        itemCode    = rsAcc.getString(1);
        openingWAC  = generalUtils.deNull(rsAcc.getString(2));
        openingWACD = generalUtils.doubleFromStr(openingWAC);

        for(x=2;x<368;++x)
          wacs[x] = generalUtils.doubleFromStr(generalUtils.deNull(rsAcc.getString(x)));

        atLeastOneValue = false;
        for(x=2;x<368;++x)
          if(wacs[x] != 0.0)
            atLeastOneValue = true;

        if(atLeastOneValue)
        {
          writeEntry(fh, itemCode, true, false);
          writeEntry(fh, generalUtils.doubleDPs(openingWAC, '4'), true, false);

          lastWAC = openingWACD;

          for(x=2;x<368;++x)
          {
            if(x == 365)
            {
              comma   = false;
              newLine = true;
            }

            wac = wacs[x];

            if(x == 367 && wac == 0.0)
              wac = lastWAC;
            else wac = wacs[x];

            if(wac == 0.0)
              writeEntry(fh, "", comma, newLine);
            else
            {
              writeEntry(fh, generalUtils.doubleDPs('4', wac), comma, newLine);

              lastWAC = wac;
            }
          }
        }
      }

      if(rsAcc   != null) rsAcc.close();
      if(stmtAcc != null) stmtAcc.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsAcc   != null) rsAcc.close();
      if(stmtAcc != null) stmtAcc.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeEntry(RandomAccessFile fh, String entry, boolean comma, boolean newLine) throws Exception
  {
    fh.writeBytes("\"");
    for(int x=0;x<entry.length();++x)
    {
      if(entry.charAt(x) == '"')
        fh.writeBytes("''");
      else fh.writeBytes("" + entry.charAt(x));
    }

    fh.writeBytes("\"");

    if(comma)
      fh.writeBytes(",");

    if(newLine)
      fh.writeBytes("\n");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void download(HttpServletResponse res, String dirName, String fileName, int[] bytesOut) throws Exception
  {

    res.setContentType("application/x-download");
    res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    OutputStream out = res.getOutputStream();

    InputStream in = null;
    try
    {
      in = new BufferedInputStream(new FileInputStream(dirName + fileName));
      byte[] buf = new byte[4096];
      int bytesRead;
      while((bytesRead = in.read(buf)) != -1)
        out.write(buf, 0, bytesRead);
    }
    catch(Exception e) //finally
    {
      if(in != null)
        in.close();
    }

    File file = new File(dirName + fileName);
    long fileSize = file.length();

    bytesOut[0] += (int)fileSize;
  }

}

