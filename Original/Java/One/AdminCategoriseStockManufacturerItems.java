// ===================================================================================================================================================
// System: ZaraStar AdminEngine: Categorize Stock - get items for a mfr
// Module: AdminCategoriseStockManufacturerItems.java
// Author: C.K.Harvey
// Copyright (c) 1998-2007 Christopher Harvey. All Rights Reserved.
// ===================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminCategoriseStockManufacturerItems extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminUtils adminUtils = new AdminUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="", option="", individual="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();
      
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
        else
        if(name.equals("mfr"))
          mfr = value[0];
        else
        if(name.equals("option"))
          option = value[0];
        else
        if(name.equals("individual"))
          individual = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
      }

      doIt(out, req, unm, sid, uty, dnm, men, den, bnm, mfr, option, individual, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdmingategoriseStockManufacturerItems", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7072, bytesOut[0], 0, "ERR:" + mfr);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String dnm, String men, String den,
                    String bnm, String mfr, String option, String individual, int[] bytesOut) throws Exception
  {
    String imagesDir        = directoryUtils.getSupportDirs('I');
    String defnsDir         = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 7071, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "7072", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 7072, bytesOut[0], 0, "SID:");
        if(con   != null) con.close();
        if(out != null) out.flush();
        return;
      }

      boolean indiv;
      if(individual.equals("Y"))
        indiv = true;
      else indiv = false;
      
      analyze(con, stmt, rs, out, req, mfr, option, indiv, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    }
    else
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "7072", imagesDir, localDefnsDir, defnsDir, bytesOut);
    }
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7072, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con   != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void analyze(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String mfr, String option, boolean individual, String unm, String sid,
                       String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                       throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Categorize Stock</title>");
    
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function search(){document.go.submit();}");
    
    if(! individual)
    {
      scoutln(out, bytesOut, "function checkAll(){");
      scoutln(out, bytesOut, "var e,x;for(x=0;x<document.forms[0].length;++x)");
      scoutln(out, bytesOut, "{e=document.forms[0].elements[x];");
      scoutln(out, bytesOut, "if(e.type=='checkbox' && e.name != 'all')");
      scoutln(out, bytesOut, "if(e.checked)");
      scoutln(out, bytesOut, "e.checked=false;");
      scoutln(out, bytesOut, "else e.checked=true;}}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 3001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function view(code){var p1=sanitize(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProductStockRecord?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\"></head>");
  
    scoutln(out, bytesOut, "<form name=\"go\" action=\"AdminCategoriseStockManufacturerItemsUpdate\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=unm value=" + unm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=sid value=" + sid + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=uty value=" + uty + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=men value=\"" + men + "\">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=den value=" + den + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=dnm value=" + dnm + ">");
    scoutln(out, bytesOut, "<INPUT TYPE=hidden name=bnm value=" + bnm + ">");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminCategoriseStock", "", "7072", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - Stock Category", "7072", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td colspan=3 nowrap><p>Manufacturer: " + mfr + "</td></tr>");
  
    if(option.equals("on"))
      scoutln(out, bytesOut, "<tr><td colspan=3><p>All stock items NOT categorized</td></tr>");
    else scoutln(out, bytesOut, "<tr><td colspan=3><p>All stock items</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");

    int[] numCategories = new int[1];
    String[][] categories   = new String[1][];
    String[][] descriptions = new String[1][];
    
    getCategoriesForMfr(mfr, dnm, localDefnsDir, defnsDir, bytesOut, numCategories, categories, descriptions);

    if(! individual)
    {
      createOneDDL(out, numCategories[0], categories[0], descriptions[0], bytesOut);
      scoutln(out, bytesOut, "<td></td>");
    }
    else scoutln(out, bytesOut, "<td><p>Category</td>");

    scoutln(out, bytesOut, "<td><p>Item Code</td>");

    scoutln(out, bytesOut, "<td><p>Manufacturer Code</td>");
    scoutln(out, bytesOut, "<td><p>Description</td></tr>");

    getItems(out, mfr, option, individual, numCategories[0], categories, descriptions, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><p><a href=\"javascript:search()\">Update</a> Stock File</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getItems(PrintWriter out, String mfr, String option, boolean individual, int numCategories, String[][] categories,
                        String[][] descriptions, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      String where = "";
        
      if(option.equals("on"))
        where = "AND (CategoryCode IS NULL OR CategoryCode = '0')";
      
      rs = stmt.executeQuery("SELECT ItemCode, CategoryCode, ManufacturerCode, Description, Description2 FROM stock WHERE Manufacturer = '" + mfr
                           + "' " + where + " ORDER BY ManufacturerCode");

      String itemCode, category, mfrCode, desc, desc2, cssFormat;
      boolean line1=true;

      while(rs.next())
      {
        itemCode = rs.getString(1);
        category = rs.getString(2);
        mfrCode  = rs.getString(3);
        desc     = rs.getString(4);
        desc2    = rs.getString(5);
        
        if(category == null || category.length() == 0 ||category.equals("0"))
          category = "";          
          
        if(line1)
        {
          cssFormat = "line1";
          line1 = false;
        }
        else
        {
          cssFormat = "line2";
          line1 = true;              
        }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");

        if(individual)
          createDDL(out, category, itemCode, numCategories, categories[0], descriptions[0], bytesOut);
        else createCheckbox(out, itemCode, category, numCategories, categories[0], descriptions[0], bytesOut);

        scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:view('" + itemCode + "')\">" + itemCode + "</a></td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + mfrCode + "</td>");
        scout(out, bytesOut, "<td nowrap><p>" + desc);
        if(desc2.length() > 0)
          scout(out, bytesOut, ": " + desc2);
        scoutln(out, bytesOut, "</td></tr>");
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }    
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCategoriesForMfr(String mfr, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut, int[] numCategories,
                                   String[][] categories, String[][] descriptions) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      String where = "";
        
      rs = stmt.executeQuery("SELECT Count(*) AS rowcount FROM stockcat WHERE Manufacturer = '" + mfr + "'");

      if(rs.next())
        numCategories[0] = rs.getInt("rowcount");
      else numCategories[0] = 0;
      
      if(rs != null) rs.close();                                 

      if(numCategories[0] > 0)
      {
        categories[0]   = new String[numCategories[0]];
        descriptions[0] = new String[numCategories[0]];        
      
        rs = stmt.executeQuery("SELECT CategoryCode, Description FROM stockcat WHERE Manufacturer = '" + mfr + "' ORDER BY CategoryCode");

        int count = 0;
        while(rs.next())
        {
          categories[0][count]   = rs.getString(1);
          descriptions[0][count] = rs.getString(2);
          
          ++count;
        }

        if(rs   != null) rs.close();                                 
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createDDL(PrintWriter out, String category, String itemCode, int numCategories, String[] categories, String[] descriptions,
                         int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<td><select name=\"c" + itemCode + "\">");
    
    scoutln(out, bytesOut, "<option value='0'>");

    for(int x=0;x<numCategories;++x)
    {
      scout(out, bytesOut, "<option value='" + categories[x] + "'");
      
      if(categories[0].equals(category))
        scout(out, bytesOut, " selected");
      
      scoutln(out, bytesOut, ">" + categories[x] + ": " + descriptions[x]);
    }
    
    scoutln(out, bytesOut, "</select></td>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createOneDDL(PrintWriter out, int numCategories, String[] categories, String[] descriptions, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr><td><select name=\"category\">");
    
    scoutln(out, bytesOut, "<option value='0'>");

    for(int x=0;x<numCategories;++x)
      scout(out, bytesOut, "<option value='" + categories[x] + "'>" + categories[x] + ": " + descriptions[x]);
    
    scoutln(out, bytesOut, "</select></td></tr>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void createCheckbox(PrintWriter out, String itemCode, String category, int numCategories, String[] categories, String[] descriptions,
                              int[] bytesOut) throws Exception
  {
    String desc = "";
    
    for(int x=0;x<numCategories;++x)
    {
      if(categories[x].equals(category))
        desc = categories[x] + ": " + descriptions[x];
    }
      
    scoutln(out, bytesOut, "<td><input type=checkbox name=\"c" + itemCode + "\">" + desc + "</td>");
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}


