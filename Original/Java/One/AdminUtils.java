// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Utilities
// Module: adminUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;

public class AdminUtils
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String callingServlet, String bodyStr, String service, String unm, String sid, String uty, String men, String den, String dnm,
                              String bnm, String otherSetup, String localDefnsDir, String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);
            
    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitle(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, PrintWriter out, String title, String service, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir,
                        String defnsDir, int[] hmenuCount, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(service) + "</td></tr></table>");

    scoutln(out, bytesOut, "<table id='submenu' width=100%><tr><td>" + buildSubMenuText(con, stmt, rs, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount) + "</td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void drawTitleW(PrintWriter out, String title, String service, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id='title' width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title + directoryUtils.buildHelp(service) + "</span></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] hmenuCount)
                                  throws Exception
  {
    String s = "<div id='submenu'>";

    hmenuCount[0] = 1;

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 112, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/AdminServicesAccess?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Users</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 6400, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/CatalogsSteelclawsList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">SC Catalog</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 114, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/CatalogDefinitionServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Catalog</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 7024, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/_7024d?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Buyer Catalog</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 116, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/DataBaseServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">DataBase</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 117, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/SystemServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">System</a></dt></dl>";
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 12705, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/ChannelServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Channels</a></dt></dl>";
    }

    if(serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.isDBAdmin(con, stmt, rs, unm))
    {
      s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
      s += "<a href=\"http://" + men + "/central/servlet/AdminControl?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Control</a></dt></dl>";
    }

    s += "</div>";

    --hmenuCount[0];

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void createAnAccountsDB(String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      stmt = con.createStatement();
      q = "CREATE DATABASE " + dnm + "_accounts_" + year;

      stmt.executeUpdate(q);
    }
    catch(Exception e) { }

    if(stmt != null) stmt.close();
    if(con  != null) con.close(); 
  }  
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void showStats(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password="
                                                 + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES "
                                   + "where table_schema='" + dnm + "_ofsa'");

    String tableName="", createDate="", numRows="", modifyDate="", cssFormat="";
    
    while(rs.next())                  
    {
      tableName  = rs.getString(1);
      createDate = rs.getString(2);
      modifyDate = rs.getString(3);
      numRows    = rs.getString(4);
        
     if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td>" + tableName + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + numRows + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + createDate + "</td>");
      scoutln(out, bytesOut, "<td align=center>" + modifyDate + "</td></tr>");
    }   
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int listDataBases(String dnm, String[] dbNames) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
   
    ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + dnm + "_accounts_%'");

    dbNames[0] = "";
    int count = 0;
    
    while(rs.next())                  
    {
      dbNames[0] += (rs.getString(1) + "\001");
      ++count;
    }   
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
    
    return count;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean createTable(String dataBase, boolean dropTable, String tableName, String tableCreateString, int numIndexes, String[] indexCreateStrings, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
   
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_" + dataBase + "?user=" + uName + "&password=" + pWord);
    Statement stmt = con.createStatement();
    String q;
   
    try
    {
      if(dropTable)
      {  
        q = "DROP TABLE " + tableName;
        stmt.executeUpdate(q);
      }
    }
    catch(Exception e) { }
    if(stmt != null) stmt.close();

    try
    {
      stmt = con.createStatement();

      stmt.executeUpdate("CREATE TABLE " + tableCreateString);
    }
    catch(Exception e)
    {
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
      return false;
    }
 
    if(stmt != null) stmt.close();

    // for each index createString
    for(int x=0;x<numIndexes;++x)
    {
      try
      {
        stmt = con.createStatement();
        stmt.executeUpdate("CREATE INDEX " + indexCreateStrings[x]);
      }
      catch(Exception e)
      {
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
        return false;
      }
 
      if(stmt != null) stmt.close();
    }
 
    if(con  != null) con.close();

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean importFile(String tableName, String fieldNames, String fieldTypes, String exportDir, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String inputName = tableName + ".asc";
    
    System.out.print("sqlImport Appending: to table " + tableName + " from " + exportDir + inputName);

    RandomAccessFile fhi = generalUtils.fileOpenD(inputName, exportDir);
    if(fhi == null)
    {
      System.out.println("File Not Found: " + exportDir + inputName);
      return false;
    }
    
    byte[] tmp        = new byte[5000]; // plenty - max rec size
    String[] fldEntry = new String[1];
    int x, y, len, count=0, errorCount=0;
    int numFlds = fieldTypes.length();
    String s, firstField="";
  
    Connection con = null;
    Statement stmt = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);
    }
    catch(Exception e)
    {
      System.out.println(e.toString());
      System.out.println("Abort!!!");
      try
      {
        fhi.close();
      }
      catch(Exception e2) { } 
      return false;
    }
    
    while(getNextLine(fhi, tmp, 5000))
    {
      try
      {
        ++count;  
        x = 0;
        s = "";
        while(x < numFlds)
        {
          switch(getEntry(tmp, x, fldEntry))
          {
            case   -1 : // illegal
                        System.out.println("Line " + count + " not legal");
                        System.out.println("Abort!!!");
                        fhi.close();
                        return false;
            case -888 : // premature end of imported flds ???
            default   : if(x > 0)
                          s += ",";

                        if(fieldTypes.charAt(x) == 'D') // DATE
                        {
                          if(fldEntry[0].length() == 0)
                            s += " {d '1970-01-01'}";
                          else s += " {d '" + fldEntry[0] + "'}";
                        }
                        else
                        if(fieldTypes.charAt(x) == 'T') // TIME
                        {
                          if(fldEntry[0].length() == 0)
                            s += "{t '00:00:00'}";
                          else
                          if(fldEntry[0].equals("0"))
                            s += "{t '00:00:00'}";
                          else s += "{t '" + fldEntry[0] + "'}";
                        }
                        else
                        if(fieldTypes.charAt(x) == 'S') // DLM
                        {
                          if(fldEntry[0].length() == 0 || fldEntry[0].equals("1970-01-01"))
                            s += " {ts '1971-01-01 00:00:01'}";
                          else 
                          {
                            if(generalUtils.strToInt(fldEntry[0].substring(0,4)) < 1970)
                              s += " {ts '1971-01-01 00:00:01'}";
                            else s += " {ts '" + fldEntry[0] + " 00:00:00'}"; 
                          }
                        }                          
                        else
                        if(fieldTypes.charAt(x) == 'I') // INTEGER
                        {
                          if(fldEntry[0].equals("0"))
                            s += "0";
                          else s += generalUtils.strToInt(fldEntry[0]);
                        }  
                        else
                        if(fieldTypes.charAt(x) == 'F') // FLOAT (DECIMAL)
                        {
                          if(fldEntry[0].equals("0"))
                            s += "0";
                          else s += generalUtils.doubleFromStr(fldEntry[0]);
                        }
                        else // fieldType is CHAR
                        {
                          s += "'";
                        
                          len = fldEntry[0].length();
                          for(y=0;y<len;++y)
                          {
                            if(fldEntry[0].charAt(y) == '\'')
                              s += "''";
                            else s += fldEntry[0].charAt(y);
                          }
                      
                          s += "'";
                        }
         
                        if(x == 0)
                          firstField = fldEntry[0];
          }
        
          ++x;
        }
          
        if(stmt != null) stmt.close();

        stmt = con.createStatement();

        stmt.executeUpdate("INSERT INTO " + tableName + " (" + fieldNames + ") VALUES (" + s + ")");
      }
      catch(Exception e)
      {
        System.out.println(e.toString());
        ++errorCount;
      }
    }     
        
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
    
    fhi.close();

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getNextLine(RandomAccessFile fh, byte[] buf, int bufSize) throws Exception
  {
    int x=0, red=0;
    boolean inQuote = false;

    long curr = fh.getFilePointer();
    long high = fh.length();

    fh.seek(curr);

    if(curr == high)
      return false;

    fh.read(buf, 0, 1);
    while(curr < high && x < (bufSize - 1))
    {
      if(buf[x] == '"')
      {
        if(inQuote)
          inQuote = false;
        else inQuote = true;
      }
      
      if((buf[x] == 10 || buf[x] == 13 || buf[x] == 26) && ! inQuote)
      {
        while(buf[x] == 10 || buf[x] == 13 || buf[x] == 26)
        {
          red = fh.read(buf, x, 1);
          if(red < 0)
            break;
        }

        if(buf[x] == 26)
          ;
        else
          if(red > 0)
            fh.seek(fh.getFilePointer() -1);

        buf[x] = '\000';

        return true;
      }

      ++x;
      fh.read(buf, x, 1);
      ++curr;
    }

    // remove trailing spaces
    x = bufSize - 1;
    while(buf[x] == 32)
      --x;
    buf[++x] = '\000';

    return true;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int getEntry(byte[] line, int num, String[] entry)
  {
    boolean quit = false, inQuotes, complete;
    int z, y, count = 0, x = 0;

    while(true)
    {
      entry[0] = "";

      // loop until sat on first char of entry text contents
      quit = complete = inQuotes = false;
      while(! quit)
      {
        if(line[x] == '\000')
          return -888; // legal EOL
        
        if(line[x] == '"') // on opening quote, maybe
        {  
          if(inQuotes)
          {
            if(line[x + 1] == '"') // then we have "" == "
              ++x;
            else inQuotes = false; // on closing quote; end of entry
          }
          else inQuotes = true;
        
          ++x;
        }
        else
        if(line[x] == ',') // on separator of next entry; i.e., this entry is empty
        {  
          quit = true;          
          complete = true;
          ++x;
        }
        else
        if(line[x] == ' ' || line[x] == '\t') // on whitespace between entries
        {  
          ++x;
          while(line[x] == ' ' || line[x] == '\t')
            ++x;
        }
        else // sat on entry contents
          quit = true;
      }
      
      y = 0;
      while(! complete)
      {
        if(line[x] == '\000')
        {  
          if(inQuotes)
            return -1; // illegal EOL
          return -888; // legal EOL
        }
                
        if(line[x] == '"') // hit closing quote, maybe
        {
          if(line[x + 1] == '"') // then we have "" == "
          {
            entry[0] += "\"";
            ++x; ++x;
          }
          else
          {
            complete = true;
            ++x;
            while(line[x] == ' ' || line[x] == '\t')
              ++x;
            if(line[x] == ',') // just-in-case: may be EOL
              ++x;
          }
        }
        else
        if(line[x] == ',')
        {
          if(inQuotes)
          {  
            entry[0] += ",";
            ++x;
          }
          else // on next separator, so entry contents complete
          {
            complete = true;
            ++x;
            while(line[x] == ' ' || line[x] == '\t')
              ++x;
          }
        }
//        else 
//        if(line[x] == '\'')
//        {
//          entry[0] += "''";
//          ++x;
//        }
        else entry[0] += (char)line[x++];
      }        
        
      if(count == num)
      {
        if(y > 0)
        {  
          --y;
          while(y > 0 && entry[0].charAt(y) == ' ') // remove trailing spaces
            --y;
          entry[0] = entry[0].substring(0, y+1);
          
          z = 0;
          while(z < y && entry[0].charAt(z) == ' ') // remove leading spaces
            ++z;
          entry[0] = entry[0].substring(z);
        }
        
        return 0;
      }
      
      ++count;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean exportTable(String tableName, String fieldNames, String fieldTypes, String exportDir, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
    RandomAccessFile fh = null;

    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT * FROM " + tableName;

      rs = stmt.executeQuery(q);
      rsmd = rs.getMetaData();

      fh = generalUtils.create(exportDir + tableName + ".out");

      int x=0, y=0, numFields = fieldTypes.length();
      int len = fieldNames.length();
      String value, fieldName;

      while(x < len)
      {
        fieldName = "";
        while(x < len && fieldNames.charAt(x) != ',')
          fieldName += fieldNames.charAt(x++);
        ++x;

        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;

        outputField(fh, y++, fieldName);
      }

      fh.writeBytes("\n");

      while(rs.next())
      {
        for(x=0;x<numFields;++x)
        {
          if(fieldTypes.charAt(x) == 'D')
            value = getValue(x+1, 'D', rs, rsmd);
          else value = getValue(x+1, ' ', rs, rsmd);

          outputField(fh, x, value);
        }

        fh.writeBytes("\n");
      }

      if(fh != null) generalUtils.fileClose(fh);

      rs.close();

      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();

      if(fh != null) generalUtils.fileClose(fh);

      return false;
    }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean exportTableToXML(String tableName, String fieldNames, String fieldTypes, String exportDir, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    RandomAccessFile fh = null;

    try
    {
      fh = generalUtils.create(exportDir + tableName + ".xml");

      fh.writeBytes("<?xml version='1.0' encoding='UTF-8'?>\n");
      fh.writeBytes("<DOCTYPE " + tableName + ">\n");
      fh.writeBytes("<RECORDS>\n");

      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();

      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();

      String q = "SELECT * FROM " + tableName;

      rs = stmt.executeQuery(q);

      int x, y=0, numFields = fieldTypes.length(), fldNum;
      int len = fieldNames.length();
      String value, fieldName;

      while(rs.next())
      {
        fh.writeBytes("<RECORD>\n");

        x = 0;
        fldNum = 1;
        while(x < len)
        {
          fieldName = "";
          while(x < len && fieldNames.charAt(x) != ',')
            fieldName += fieldNames.charAt(x++);
          ++x;

          while(x < len && fieldNames.charAt(x) == ' ')
            ++x;

          value = rs.getString(fldNum++);

          outputFieldXML(fh, fieldName, value);
        }

        fh.writeBytes("\n</RECORD>\n");
      }

      fh.writeBytes("</RECORDS>\n");

      if(fh != null) generalUtils.fileClose(fh);

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

      if(fh != null) generalUtils.fileClose(fh);

      return false;
    }

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputFieldXML(RandomAccessFile fh, String fieldName, String value) throws Exception
  {
    if(value == null) value = "";
    
    fh.writeBytes("<" + fieldName + ">");
    fh.writeBytes(generalUtils.replaceNewLinesByN(generalUtils.sanitiseForXML(value)));
    fh.writeBytes("</" + fieldName + ">");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputField(RandomAccessFile fh, int fieldNum, String value) throws Exception
  {
    int len = value.length();
    String str="";

    for(int x=0;x<len;++x)
    {
      if(value.charAt(x) == '"')
        str += "''";
      else str += value.charAt(x);
    }

    if(fieldNum > 0)
      fh.writeBytes(",");

    fh.writeBytes("\"" + str + "\"");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getValue(int colNum, char type, ResultSet rs, ResultSetMetaData rsmd)
  {
    if(colNum < 0)
      return "";
     
    try
    {
      Integer f;
      java.sql.Date d;
      Time t;

      String str="";

      switch(rsmd.getColumnType(colNum))
      {
        case java.sql.Types.CHAR    : str = rs.getString(colNum);
                                      str = generalUtils.stripTrailingSpacesStr(str);
                                      break;
        case java.sql.Types.INTEGER : f   = rs.getInt(colNum);
                                      str = f.toString();
                                      break;
        case 91                     : if(type == 'D')
                                      {
                                        d  = rs.getDate(colNum);
                                        str = d.toString();
                                      }  
                                      else 
                                      {
                                        t = rs.getTime(colNum);
                                        str = t.toString();
                                      }  
                                      break;
      }

      return str;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    return "";
  }

}
