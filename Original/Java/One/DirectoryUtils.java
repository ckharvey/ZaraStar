// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Directories etc
// Module: DirectoryUtils.java
// Author: C.K.Harvey
// Copyright (c) 2006-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;
import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

public class DirectoryUtils extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  DefinitionTables definitionTables = new DefinitionTables();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // check in this app's current css directory first
  // if not present, looks in the Support Default css directory
  public String getCssDocumentDirectory(Connection con, Statement stmt, ResultSet rs, String dnm) throws Exception
  {
    try
    {
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    definitionTables.getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator,
                       description, companyEMail, terseName, latitude, longitude, googleMapsKey, currentStyle);

      if(currentStyle[0].startsWith("Zara"))
        return "/Zara/Support/Css/" + currentStyle[0] + "/";
      
      return "/Zara/" + dnm + "/Css/" + currentStyle[0] + "/";
    }
    catch(Exception e)
    {
      return "/Zara/Support/Css/Zara Default/";
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCurrentStyleForAUser(Connection con, Statement stmt, ResultSet rs, String unm, String dnm) throws Exception
  {
    String styleName = "";

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT StyleName FROM cssstyles WHERE UserCode = '" + unm + "'");

      if(rs.next())
        styleName = rs.getString(1);

      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("authenticationUtils: style: " + e);
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();

      if(! tableExists(con, stmt, "ofsa", "cssstyles", dnm))
      {
        AdminDataBaseFixedFilesCreate adminDataBaseFixedFilesCreate = new AdminDataBaseFixedFilesCreate();
        adminDataBaseFixedFilesCreate.processATable(con, stmt, false, 'C', "cssstyles", dnm, null, null, null);
      }
    }

    return styleName;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean tableExists(Connection con, Statement stmt, String dbName, String tableName, String dnm) throws Exception
  {
    boolean res = false;

    ResultSet rs = null;

    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) As count FROM INFORMATION_SCHEMA.TABLES where table_schema='" + dnm + "_" + dbName + "' AND table_name = '" + tableName + "'");

      if(rs.next())
      {
        if(rs.getInt("count") > 0)
          res = true;
      }
    }
    catch(Exception e) { System.out.println("tableExists: " + e); }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // checks in user's directory for any local style defn
  // if not present, checks in app config table for style defn
  // if not present, uses Support Default sytle (which must be present, otherwise no styling can (will) be applied)
  public String getCssGeneralDirectory(Connection con, Statement stmt, ResultSet rs, String unm, String dnm, String defnsDir) throws Exception
  {
    String s = getCurrentStyleForAUser(con, stmt, rs, unm, dnm);

    if(s.length() > 0)
    {
      if(s.startsWith("Zara"))
        return "/Zara/Support/Css/" + s + "/";

      if(styleExists(s, dnm)) // in case style has been deleted after user selected it
        return "/Zara/" + dnm + "/Css/" + generalUtils.capitalize(s) + "/";
    }
    
    try
    {
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    definitionTables.getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                 longitude, googleMapsKey, currentStyle);

      if(currentStyle[0].startsWith("Zara"))
        return "/Zara/Support/Css/" + currentStyle[0] + "/";
      
      return "/Zara/" + dnm + "/Css/" + currentStyle[0] + "/";
    }
    catch(Exception e)
    {
      return "/Zara/Support/Css/Default/";     
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCatalogCssDirectory(String mfr) throws Exception
  {
    return "http://www.xxx.com/Zara/Catalogs/Css/Default/" + mfr + ".css";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean styleExists(String reqdStyle, String dnm) throws Exception
  {
    String dir = "/Zara/" + dnm + "/Css/";
    File path = new File(dir);
    String fs[];
    fs = path.list();

    for(int x=0;x<fs.length;++x)
    {
      if(generalUtils.isDirectory(dir + fs[x])) // just-in-case
      {
        if(reqdStyle.equals(fs[x]))
          return true;
      }
    }
    
    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getEditorDirectory() throws Exception
  {
    return "/Zara/Support/Scripts/Editor/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getGoogleScriptsDirectory() throws Exception
  {
    return "/Zara/Support/Scripts/Google/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getScriptsDirectory() throws Exception
  {
    return "/Zara/Support/Scripts/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFlashDirectory(String dnm) throws Exception
  {
    return "/Zara/" + dnm + "/Flash/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getUserDir(String dnm)
  {
    return "/Zara/" + dnm + "/Users"; // no trailing slash
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getSessionsDir(String dnm)
  {
    return "/Zara/" + dnm + "/Sessions/";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFaxTmpDir()
  {
    return "/Zara/Fax/Tmp/";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getReferenceLibraryDir(String dnm)
  {
    return "/Zara/" + dnm + "/Reference/";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getCatalogImagesDir(String dnm, String mfr)
  {
    return "/Zara/" + dnm + "/Catalogs/" + mfr + "/";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getImagesDir(String dnm)
  {
    return "/Zara/" + dnm + "/Images/";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTextsDir(String dnm)
  {
    return "/Zara/" + dnm + "/Texts/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getSignaturesDir(String dnm)
  {
    return "/Zara/" + dnm + "/Signatures/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getUserDir(char param, String dnm, String unm)
  {
    String dir="";
    
    switch(param)
    {
      case 'U' : dir = "/Zara/" + dnm + "/Users/" + unm + "/";                     break;
      case 'R' : dir = "/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/"; break;
      case 'E' : dir = "/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Export/";  break;
      case 'W' : dir = "/Zara/" + dnm + "/Users/" + unm + "/Working/";             break;
      case 'L' : dir = "/Zara/" + dnm + "/Users/" + unm + "/Library/";             break;
      case 'M' : dir = "/Zara/" + dnm + "/Users/" + unm + "/Mail/";                break;
    }
    
    if(! generalUtils.fileExists(dir))
      generalUtils.createDir(dir, true);

    return dir;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getSupportDirs(char param)
  {
    switch(param)
    {
      case 'H' : return "/Zara/Support/Html/";
      case 'I' : return "/Zara/Support/Images/";
      case 'S' : return "/Zara/Support/Scripts/";
      case 'D' : return "/Zara/Support/Defns/";
    }
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getExportDir(String dnm)
  {
    return "/Zara/" + dnm + "/Export/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getLocalOverrideDir(String dnm)
  {
    return "/Zara/" + dnm + "/Defns/";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void setContentHeaders(HttpServletResponse res)
  {
    res.setHeader("Header", "HTTP/1.0 200 OK");
    res.setStatus(200);
    res.setContentType("text/html");
    res.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");              // Set to expire far in the past
    res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // Set standard HTTP/1.1 no-cache headers
    res.addHeader("Cache-Control", "post-check=0, pre-check=0");           // Set IE extended HTTP/1.1 no-cache (use addHeader)
    res.setHeader("Pragma", "no-cache");                                   // Set standard HTTP/1.0 no-cache header
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void setContentHeaders2(HttpServletResponse res)
  {
    res.setHeader("Header", "HTTP/1.0 200 OK");
    res.setStatus(200);
    res.setContentType("text/html");
    res.setHeader("Expires", "Sat, 6 May 2995 12:00:00 GMT");              // Set to expire far in the future !!!!!!!!!!!!!
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean createTmpTable(boolean removeExisting, Connection con, Statement stmt, String createString, String indexString, String tableName) throws Exception
  {
    try
    {
      if(removeExisting)
      {
        stmt = con.createStatement();
        stmt.executeUpdate("DROP TABLE " + tableName);
        if(stmt != null) stmt.close();  
      }
    }
    catch(Exception e) { if(stmt != null) stmt.close(); }

    try
    {
      stmt = con.createStatement();
      stmt.executeUpdate("CREATE TABLE " + tableName + " (" + createString + ")");
      if(stmt != null) stmt.close();
    }
    catch(Exception e) // throws error if already created
    {
      System.out.println("directoryUtils: createTmpTable (1): " + e);
      if(stmt != null) stmt.close();
      return true;////////////false;
    }

    if(indexString.length() > 0)
    {
      try
      {
        stmt = con.createStatement();
        stmt.executeUpdate("CREATE INDEX " + indexString);
        if(stmt != null) stmt.close();
      }
      catch(Exception e)
      {
        System.out.println("directoryUtils: createTmpTable (2): " + e);
        if(stmt != null) stmt.close();
      return true;////////////false;
      }
    }  
  
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean removeTmpTable(Connection con, Statement stmt, String tableName) throws Exception
  {
    stmt = con.createStatement();
   
    try
    {
      stmt.executeUpdate("DROP TABLE " + tableName);
    }
    catch(Exception e) { }
    
    if(stmt != null) stmt.close();

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean clearTmpTable(Connection con, Statement stmt, String tableName) throws Exception
  {
    stmt = con.createStatement();
   
    try
    {
      stmt.executeUpdate("TRUNCATE TABLE " + tableName);
    }
    catch(Exception e) { }
    
    if(stmt != null) stmt.close();

    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean usesFlash(String dnm)
  {
    String[] headerLogo       = new String[1];
    String[] headerLogoRepeat = new String[1];
    String[] usesFlash        = new String[1];
    String[] footerText       = new String[1];
    String[] pageHeaderImage1 = new String[1];
    String[] pageHeaderImage2 = new String[1];
    String[] pageHeaderImage3 = new String[1];
    String[] pageHeaderImage4 = new String[1];
    String[] pageHeaderImage5 = new String[1];
    String[] watermark        = new String[1];
    
    getStyling(dnm, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);
  
    if(usesFlash[0].equals("Y"))
      return true;

    return false;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getLayoutType(RandomAccessFile fh) throws Exception
  {
    fh.seek(0);
    
    String s = fh.readLine(); // e.g., /* 1 */
    
    int x=2; // /*
    while(x < s.length() && s.charAt(x) == ' ') // just-in-case
      ++x;

    String t = "";
    while(x < s.length() && s.charAt(x) != ' ') // just-in-case
      t += s.charAt(x++);
    
    int  i = 1;
    try
    {
      i = generalUtils.intFromStr(t);
    }
    catch(Exception e) { }
    
    return i;    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getMySQLUserName() throws Exception
  {
    String s;

    try
    {
      s = generalUtils.getFromDefnFile("SQLUSERNAME", "mysql.dfn", "/Zara/Support/Defns/", "");
    }
    catch(Exception e)
    {
      System.out.println("directoryUtils: Failure to get SQL UserName");
      return "";
    }

    return s;  
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getMySQLPassWord() throws Exception
  {
    String s;

    try
    {
      s = generalUtils.getFromDefnFile("SQLPASSWORD", "mysql.dfn", "/Zara/Support/Defns/", "");
    }
    catch(Exception e)
    {
      System.out.println("directoryUtils: Failure to get SQL PassWord");
      return "";
    }

    return s;  
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getAppConfigInfo(char which, String dnm) throws Exception
  {
    Connection[] conp = new Connection[1];Connection con = createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    String[] companyName             = new String[1];
    String[] applicationStartDate    = new String[1];
    String[] financialYearStartMonth = new String[1];
    String[] financialYearEndMonth   = new String[1];
    String[] effectiveStartDate      = new String[1];
    String[] companyPhone            = new String[1];
    String[] companyFax              = new String[1];
    String[] dateStyle               = new String[1];
    String[] dateSeparator           = new String[1];
    String[] description             = new String[1];
    String[] companyEMail            = new String[1];
    String[] terseName               = new String[1];
    String[] latitude                = new String[1];
    String[] longitude               = new String[1];
    String[] googleMapsKey           = new String[1];
    String[] currentStyle            = new String[1];

    definitionTables.getAppConfig(con, stmt, rs, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator, description, companyEMail, terseName, latitude,
                       longitude, googleMapsKey, currentStyle);
    
    if(con  != null) con.close();
    
    switch(which)
    {
      case 'N' : return companyName[0];
      case 'L' : return "";//logo[0];
    }
    
    return "";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getStyling(String dnm, String[] headerLogo, String[] headerLogoRepeat, String[] usesFlash, String[] footerText, String[] pageHeaderImage1, String[] pageHeaderImage2, String[] pageHeaderImage3, String[] pageHeaderImage4,
                          String[] pageHeaderImage5, String[] watermark)
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = getMySQLUserName();
      String pWord = getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_sitewiki?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT * FROM styling");   
      if(rs.next())
      {
        headerLogo[0]       = rs.getString(1); 
        headerLogoRepeat[0] = rs.getString(2);
        usesFlash[0]        = rs.getString(3);
        footerText[0]       = rs.getString(4);
        pageHeaderImage1[0] = rs.getString(5);
        pageHeaderImage2[0] = rs.getString(6);
        pageHeaderImage3[0] = rs.getString(7);
        pageHeaderImage4[0] = rs.getString(8);
        pageHeaderImage5[0] = rs.getString(9);
        watermark[0]        = rs.getString(10);
      }
      else
      {
        headerLogo[0]       = "";
        headerLogoRepeat[0] = "";
        usesFlash[0]        = "N";
        footerText[0]       = "Copyright (c)";
        pageHeaderImage1[0] = "";
        pageHeaderImage2[0] = "";
        pageHeaderImage3[0] = "";
        pageHeaderImage4[0] = "";
        pageHeaderImage5[0] = "";
        watermark[0]        = "";
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      headerLogo[0]       = "";
      headerLogoRepeat[0] = "";
      usesFlash[0]        = "N";
      footerText[0]       = "Copyright (c)";
      pageHeaderImage1[0] = "";
      pageHeaderImage2[0] = "";
      pageHeaderImage3[0] = "";
      pageHeaderImage4[0] = "";
      pageHeaderImage5[0] = "";
      watermark[0]        = "";

      System.out.println("DirectoryUtils: " + e);

      try
      {
        if(rs   != null) rs.close();
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildHelp(int service)
  {
    return buildHelp("" + service);
  }
  public String buildHelp(String service)
  {
    return "<table cellspacing='2' cellpadding='2' border='0'><tr><td nowrap='nowrap'><a href=\"javascript:help('" + service + "')\">" + service + "</a></td></tr></table>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateState(String fileName, String str) 
  {
    try
    {
      RandomAccessFile fh = generalUtils.fileOpen(fileName);
      fh.writeBytes(str + "\n");
      fh.close();
    }
    catch(Exception e) { }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void xxxcreateNotification(Connection con, Statement stmt, ResultSet rs, String entry, String userCode, String type, String typeReference) throws Exception
  {
    try
    {
      stmt = con.createStatement();

      // determine next docCode
      rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM notifications");

      int rowCount = 0;

      if(rs.next())
        rowCount = rs.getInt("rowcount");

      if(rs   != null) rs.close();
      String code = generalUtils.intToStr(rowCount + 1);

      if(stmt != null) stmt.close();

      stmt = con.createStatement();
      stmt.executeUpdate("INSERT INTO notifications (Code, UserCode, Sent, TimeStamp, Type, Entry, Viewed, TypeReference)"
                       + " VALUES ('" + code + "','" + userCode + "','F',NULL,'" +  type + "','" + generalUtils.sanitiseForSQL(entry) + "','F','" + typeReference + "')");
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("createNotification " + e);
      if(stmt != null) stmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public Connection createConnections(String db, Connection[] conp)
  {
    Connection con = null;
    conp[0] = null;

    try
    {
      String uName = getMySQLUserName();
      String pWord = getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + db + "?user=" + uName + "&password=" + pWord);

      conp[0] = con;
    }
    catch(Exception e)
    {
      System.out.println("createConnections: " + e);
    }

    return con;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean usePostgres() throws Exception
  {
    try
    {
      String s = generalUtils.getFromDefnFile("USEPOSTGRES", "mysql.dfn", "/Zara/Support/Defns/", "");

      if(s.equalsIgnoreCase("yes"))
        return true;
      
      return false;
    }
    catch(Exception e)
    {
      System.out.println("directoryUtils: Failure to get UsePostgres");
    }

    return false;
  }


}
