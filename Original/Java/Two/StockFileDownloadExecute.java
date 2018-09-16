// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock File Download
// Module: StockFileDownloadExecute.java
// Author: C.K.Harvey
// Copyright (c) 2002-08 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class StockFileDownloadExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Inventory inventory = new Inventory();
  MiscDefinitions miscDefinitions = new MiscDefinitions();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", mfr="", levels="", levelsDate="", ignoreObsolete="";

    try
    {
      byte[] fieldNames    = new byte[1000]; fieldNames[0]    = '\000';
      int[]  fieldNamesLen = new int[1];     fieldNamesLen[0] = 1000;

      int thisEntryLen, inc;
      
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
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
        else // must be checkbox value
        if(name.equals("levels"))
          levels = value[0];
        else
        if(name.equals("levelsDate"))
          levelsDate = value[0];
        else
        if(name.equals("ignoreObsolete"))
          ignoreObsolete = value[0];
        else
        {
          thisEntryLen = name.length() + 2;
          if((generalUtils.lengthBytes(fieldNames, 0) + thisEntryLen) >= fieldNamesLen[0])
          {
            byte[] tmp = new byte[fieldNamesLen[0]];
            System.arraycopy(fieldNames, 0, tmp, 0, fieldNamesLen[0]);
            if(thisEntryLen > 1000)
              inc = thisEntryLen;
            else inc = 1000;
            fieldNamesLen[0] += inc;
            fieldNames = new byte[fieldNamesLen[0]];
            System.arraycopy(tmp, 0, fieldNames, 0, fieldNamesLen[0] - inc);
          }

          generalUtils.catAsBytes(name + "\001", 0, fieldNames, false);
        }
      }
      
      if(levelsDate == null) levelsDate = "";
      
      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, mfr, levels, levelsDate, fieldNames, ignoreObsolete, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockFileDownload", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3057, bytesOut[0], 0, "ERR:" + mfr);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String mfr, String levels, String levelsDate, byte[] fieldNames, String ignoreObsolete,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);
    
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + userName + "&password=" + passWord);

    Statement stmt = con.createStatement();
    ResultSet rs = null;

    if(! generalUtils.isDirectory(workingDir))
      generalUtils.createDir(workingDir);
    
    if(levels.equals("on"))
    {
      if(levelsDate.length() == 0)
        levelsDate = generalUtils.today(localDefnsDir, defnsDir);
    }
        
    RandomAccessFile fh;
    
    String fileName;

    if(mfr.equals("___ALL___"))
     fileName = "AllManufacturersStockFileDownload.csv";
    else fileName = mfr + "StockFileDownload.csv";

    fh = generalUtils.create(workingDir + fileName);

    fh.writeBytes("\"ItemCode\"");

    mfr = generalUtils.deSanitise(mfr);
    
    String fieldNamesStock = inventory.getFieldNamesStock();
    String field, fieldSelectList="";
    boolean first=true;  
    int numFields = 0, x=0, len = fieldNamesStock.length();
    
    while(x < len)
    {
      field="";
      while(x < len && fieldNamesStock.charAt(x) != ',')
        field += fieldNamesStock.charAt(x++);
      ++x;
 
      while(x < len && fieldNamesStock.charAt(x) == ' ')
        ++x;
      
      if(fieldIsWanted(field, fieldNames))
      {      
        fh.writeBytes(",");
        if(! first)
          fieldSelectList += ", ";
        else first = false;
            
        fieldSelectList += field;
        
        fh.writeBytes("\"" + field + "\"");
        
        ++numFields;        
      }
    }
    
    if(levels.equals("on"))
      fh.writeBytes(",\"Level\"");

    fh.writeBytes("\n");

    if(mfr.equals("___ALL___"))
      rs = stmt.executeQuery("SELECT Status, ItemCode, " + fieldSelectList + " FROM stock ORDER BY Manufacturer, ManufacturerCode");
    else rs = stmt.executeQuery("SELECT Status, ItemCode, " + fieldSelectList + " FROM stock WHERE Manufacturer = '" + mfr + "'");
    char dpOnQuantities = miscDefinitions.dpOnQuantities(con, stmt, rs, "5");
    
    ++numFields; // itemCode

    String itemCode, value, status;
    double level;
    
    while(rs.next())
    {
      status = rs.getString(1);
      if(status == null) status = "L";   

      if(ignoreObsolete.equals("on") && status.equals("C"))
        ;
      else
      {
        itemCode = rs.getString(2);
        first = true;    
        for(int z=1;z<=numFields;++z)
        {
          if(! first)
            fh.writeBytes(",");
          else first = false;

          value = rs.getString(z + 1);
          if(value == null)
            value = "";
          else
          if(value.equals("1970-01-01"))
            value = "";

          fh.writeBytes("\"" + generalUtils.sanitise3(value) + "\"");    
        }

        if(levels.equals("on"))
        {
          level = totalStoresLevels(getStockLevelsViaTrace(itemCode, levelsDate, unm, uty, sid, men, den, dnm, bnm, localDefnsDir));
          fh.writeBytes(",\"" + generalUtils.formatNumeric(level, dpOnQuantities) + "\"");
        }

        fh.writeBytes("\n");
      }
    }

    generalUtils.fileClose(fh);
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    download(res, workingDir, fileName, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3057, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), mfr);
    if(con  != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean fieldIsWanted(String field, byte[] fieldNames) throws Exception
  {
    String thisFieldName;
    int x=0, len = generalUtils.lengthBytes(fieldNames, 0);
    while(x < len)
    {
      thisFieldName = "";
      while(x < len && fieldNames[x] != '\001')
        thisFieldName += (char)fieldNames[x++];
      
      if(thisFieldName.equals(field))
        return true;
      
      ++x;
    }
    
    return false;  
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
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getStockLevelsViaTrace(String itemCode, String dateTo, String unm, String uty, String sid, String men, String den, String dnm, String bnm, String localDefnsDir) throws Exception
  { 
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/StockLevelsGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&p2=" + dateTo  + "&bnm="
                    + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String t="", s = di.readLine();
    while(s != null)
    {
      t += s;
      s = di.readLine();
    }

    di.close();

    return t;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double totalStoresLevels(String traceList) throws Exception
  {
    int y=0, len = traceList.length();
    String thisQty;
    double totalStockLevel = 0.0;
        
    while(y < len) // just-in-case
    {
      while(y < len && traceList.charAt(y) != '\001')
        ++y;
      ++y;

      thisQty = "";
      while(y < len && traceList.charAt(y) != '\001')
        thisQty += traceList.charAt(y++);
      ++y;

      totalStockLevel += generalUtils.doubleFromStr(thisQty);
    }
    
    return totalStockLevel;
  }

}

