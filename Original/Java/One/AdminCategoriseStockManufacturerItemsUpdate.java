// ===================================================================================================================================================
// System: ZaraStar Admin: Categorize Stock - update categories for a mfr
// Module: AdminCategoriseStockManufacturerItemsUpdate.java
// Author: C.K.Harvey
// Copyright (c) 2006-07 Christopher Harvey. All Rights Reserved.
// ===================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.io.*;

public class AdminCategoriseStockManufacturerItemsUpdate extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  LibraryUtils libraryUtils = new LibraryUtils();
  AdminUtils adminUtils = new AdminUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", category="";
    
    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      byte[] itemCodes    = new byte[1000]; itemCodes[0]    = '\000';
      int[]  itemCodesLen = new int[1];     itemCodesLen[0] = 1000;

      byte[] values    = new byte[1000]; values[0]    = '\000';
      int[]  valuesLen = new int[1];     valuesLen[0] = 1000;

      int thisEntryLen, inc, numEntries=0;

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
        if(name.equals("category"))
          category = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be either checkbox OR DDL
        {
          name = name.substring(1); // ignore the 'c'
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(itemCodes, 0) + thisEntryLen) >= itemCodesLen[0])
          {
            byte[] tmp = new byte[itemCodesLen[0]];
            System.arraycopy(itemCodes, 0, tmp, 0, itemCodesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            itemCodesLen[0] += inc;
            itemCodes = new byte[itemCodesLen[0]];
            System.arraycopy(tmp, 0, itemCodes, 0, itemCodesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, itemCodes, false);

          thisEntryLen = value[0].length() + 2;
          if((generalUtils.lengthBytes(values, 0) + thisEntryLen) >= valuesLen[0])
          {
            byte[] tmp = new byte[valuesLen[0]];
            System.arraycopy(values, 0, tmp, 0, valuesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            valuesLen[0] += inc;
            values = new byte[valuesLen[0]];
            System.arraycopy(tmp, 0, values, 0, valuesLen[0] - inc);
          }

          generalUtils.catAsBytes(value[0] + "\001", 0, values, false);

          ++numEntries;
        }        
      }
    
      doIt(out, req, numEntries, itemCodes, values, category, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminCategoriseStockManufacturerItemsUpdate", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7072, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, int numEntries, byte[] itemCodes, byte[] values, String category, String unm,
                    String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir         = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir        = directoryUtils.getSupportDirs('I');

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 7071, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "7072", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7072, bytesOut[0], 0, "ACC:");
      if(con   != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "7072", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7072, bytesOut[0], 0, "SID:");
      if(con   != null) con.close();
      if(out != null) out.flush();
      return;
    }

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    adminUtils.outputPageFrame(con, stmt, rs, out, req, "AdminCategoriseStock", "", "7072", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    adminUtils.drawTitle(con, stmt, rs, req, out, "Administration - Stock Category", "7072", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    updateStockFile(numEntries, itemCodes, values, generalUtils.lengthBytes(itemCodes, 0), generalUtils.lengthBytes(values, 0), category, dnm, localDefnsDir,
                    defnsDir);
      
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>Completed</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p>" + numEntries + " stock records updated</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7072, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con   != null) con.close();
    if(out != null) out.flush();
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------
  private void updateStockFile(int numEntries, byte[] itemCodes, byte[] values, int itemCodesLen, int valuesLen, String category, String dnm,
                               String localDefnsDir, String defnsDir) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);
    Statement stmt;
        
    String itemCode, categoryCode, checkboxState;
    int y=0, z=0;
    
    for(int x=0;x<numEntries;++x)
    {  
      stmt = con.createStatement();

      itemCode = "";
      while(y < itemCodesLen && itemCodes[y] != '\001') // just-in-case
        itemCode += (char)itemCodes[y++];
      ++y;
      
      if(category.length() > 0) // only one category defined (checkboxes not DDL)
      {
        checkboxState = "";
        while(z < valuesLen && values[z] != '\001') // just-in-case
          checkboxState += (char)values[z++];
        ++z;
        
        if(checkboxState.equals("on"))
        {
          stmt.executeUpdate("UPDATE stock SET CategoryCode = '" + category + "' WHERE ItemCode = '" + itemCode + "'");
        }
      }
      else
      {
        categoryCode = "";
        while(z < valuesLen && values[z] != '\001') // just-in-case
          categoryCode += (char)values[z++];
        ++z;

     //   System.out.println("UPDATE stock SET CategoryCode = '" + categoryCode + "' WHERE ItemCode = '" + itemCode + "'");
      stmt.executeUpdate("UPDATE stock SET CategoryCode = '" + categoryCode + "' WHERE ItemCode = '" + itemCode + "'");
      }        
            
      if(stmt != null) stmt.close();
    }
    
    if(con != null) con.close();  
  }
    
  //--------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
