// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Print statements 
// Module: StatementsPrint.java
// Author: C.K.Harvey
// Copyright (c) 1999-2007 Christopher Harvey. All Rights Reserved.
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

public class StatementsPrint extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
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
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // fileName
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StatementsPrint", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, String p1, int[] bytesOut) throws Exception
   {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir       = directoryUtils.getUserDir('R', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1012, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1012c", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "ACC:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1012c", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1012, bytesOut[0], 0, "SID:" + p1);
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, p1, unm, sid, uty, men, den, dnm, bnm, imagesDir, reportsDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String fileName, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String imagesDir, String reportsDir, String localDefnsDir,
                   String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Statements of Account Report Print</title>");

    scoutln(out, bytesOut, "<SCRIPT LANGUAGE=\"JavaScript\">");

    scoutln(out, bytesOut, "function printIt(type){");
    scoutln(out, bytesOut, "var codeFrom=document.forms[0].codefrom.value;");
    scoutln(out, bytesOut, "var codeTo=document.forms[0].codeto.value;");
    scoutln(out, bytesOut, "var p6;if(document.forms[0].ignoreNo.checked)p6='Y';else p6='N';");
    scoutln(out, bytesOut, "var p7;if(document.forms[0].ignoreZero.checked)p7='Y';else p7='N';");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/StatementsReport?unm=" + unm + "&sid=" + sid + "&uty="
                           + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                           + "&p1=\"+codeFrom+\"&p2=\"+codeTo+\"&p4=\"+type+\"&p6=\"+p6+\"&p7=\"+p7+\"&p3=" + fileName + "\";}");

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code2+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1012", "", "Statements", unm, sid, uty, men, den, dnm, bnm, localDefnsDir,
                          defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Statements of Account Print", "1012",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    RandomAccessFile fh;
    String type = "C";
    if((fh = generalUtils.fileOpen(reportsDir + fileName)) != null) // just-in-case
    {
      String line = getNextLine(fh);
      if(line.length() != 0) // just-in-case
      {
        if(line.startsWith("T:")) // just-in-case
          type = line.substring(2);
      }
    }
    
    if(type.equals("C")) // code
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>For Customer Code Starting &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input name=codefrom type=text maxlength=10 size=10></td>");
      scoutln(out, bytesOut, "<td width=90% align=left><p>&nbsp; (Optional)</td></tr>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

      scoutln(out, bytesOut, "<tr><td nowrap><p>For Customer Code Ending &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input name=codeto type=text maxlength=10 size=10></td>");
      scoutln(out, bytesOut, "<td><p>&nbsp; (Optional)</td></tr>");
    }
    else // name
    {
      scoutln(out, bytesOut, "<tr><td nowrap><p>For Customer Name Starting With &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input name=codefrom type=text maxlength=60 size=40></td>");
      scoutln(out, bytesOut, "<td width=90% align=left><p>&nbsp; (Optional)</td></tr>");

      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

      scoutln(out, bytesOut, "<tr><td nowrap><p>For Customer Name Ending With &nbsp;</td>");
      scoutln(out, bytesOut, "<td><input name=codeto type=text maxlength=60 size=40></td>");
      scoutln(out, bytesOut, "<td><p>&nbsp; (Optional)</td></tr>");
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><input name=ignoreNo type=checkbox checked></td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Ignore No-Transaction Statements</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><input name=ignoreZero type=checkbox checked></td>");
    scoutln(out, bytesOut, "<td nowrap><p>&nbsp; Ignore Zero-Outstanding-Balance Statements</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=4 nowrap><p><a href=\"javascript:printIt('" + type
                         + "')\">Print</a> Statements</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private String getNextLine(RandomAccessFile fh)
  {
    byte b;
    String str="";

    try
    {
      long curr = fh.getFilePointer();
      long high = fh.length();

      if(curr == high)
        return "";

      fh.seek(curr);

      b = fh.readByte();

      while(b == (byte)';') // comment line
      {
        while(curr < high && b != (byte)10 && b != (byte)13 && b != (byte)26)
        {
          b = fh.readByte();
          ++curr;
        }

        while(b == (byte)10 || b == (byte)13 || b == (byte)26)
        {
          try
          {
            b = fh.readByte();
          }
          catch(Exception exEof)
          {
            return str;
          }
          ++curr;
        }
        if(b == (byte)26)
          return str;
      }

      while(curr < high)
      {
        if(b == (byte)10 || b == (byte)13 || b == (byte)26)
        {
          while(b == (byte)10 || b == (byte)13 || b == (byte)26)
          {
            try
            {
              b = fh.readByte();
            }
            catch(Exception exEof2)
            {
              return str;
            }
          }

          if(b == (byte)26)
           ; // --x;
          else fh.seek(fh.getFilePointer() - 1);

          return str;
        }

        str += (char)b;
        ++curr;

        try
        {
          b = fh.readByte();
        }
        catch(Exception exEof2)
        {
          return str;
        }
      }
    }
    catch(Exception e)
    {
      return str;
    }

    return str;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
