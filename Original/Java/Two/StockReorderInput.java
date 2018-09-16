// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock ReOrder
// Module: StockReorderInput.java
// Author: C.K.Harvey
// Copyright (c) 2004-08 Christopher Harvey. All Rights Reserved.
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

public class StockReorderInput extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="";

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
 
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockReorderInput", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men,
                    String den, String dnm, String bnm, int[] bytesOut) throws Exception
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
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 1021, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "1021", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "1021", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1021, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, reportsDir, imagesDir, localDefnsDir, defnsDir, bytesOut);
  
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1021, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty,
                   String men, String den, String dnm, String bnm, String reportsDir, String imagesDir, String localDefnsDir, String defnsDir,
                   int[] bytesOut) throws Exception
  {
    out.println("<html><head><title>Stock ReOrder</title></head>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                           + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1021", "", "StockReorderInput", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir,
                          hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock ReOrder", "1021",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100%>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/StockReorderGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                           + dnm + "&bnm=" + bnm + "\">");
    scoutln(out, bytesOut, "Generate</a> a new report</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Reports on file:</td></tr>");

    File path = new File(reportsDir);
    String names[] = new String[0];
    names = path.list();
    String dateStr, title, status;
    boolean first = true;
    long len;
    RandomAccessFile fh;
    byte[] b = new byte[81];

    for(int i=0;i<names.length;++i)
    {
      if(names[i].endsWith("290"))
      {
        dateStr = generalUtils.yymmddExpand(true, names[i]);
        if(first)
        {
          scoutln(out, bytesOut, "<tr>");
          first = false;
        }
        scoutln(out, bytesOut, "<td></td>");

        scoutln(out, bytesOut, "<td nowrap><p>" + dateStr + " (" + names[i].substring(6, 8) + ")&nbsp;&nbsp;</td><td width=90% nowrap><p>");

        // check title
        if((fh = generalUtils.fileOpen(reportsDir + names[i])) == null) // just-in-case
        {
          title = "Unknown";
          status = "Unknown";
        }
        else
        {
          if(fh.length() > 0)
          {
            fh.seek(0);
            fh.read(b, 0, 80);
            title="";
            int x=14; // ignore "<tr><td><p><b>"
            while(b[x] != '\000' && b[x] != '<') // until start of "</td></tr>"
              title += (char)b[x++];
          }
          else title = "";
        }

        // check status
        len = fh.length();
        if(len > 30)
        {
          fh.seek(len - 30);
          fh.read(b, 0, 30);
          if(generalUtils.contains(b, "*** END ***"))
           status = "Complete";
          else status = "In Progress";
        }
        else status = "In Progress";

        generalUtils.fileClose(fh);

        scoutln(out, bytesOut, title); 
        
        if(! status.equals("In Progress"))
        {
          scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;<a href=\"/central/servlet/StockReorderDisplay?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + names[i] + "&bnm=" + bnm + "\">Screen</a>");
      
          scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;<a href=\"/central/servlet/RemoveReport?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + names[i] + "&p2=StockReorderInput&bnm=" + bnm + "\">Remove</a>");
        }
        
        scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;" + status + "</td></tr>"); 
      }
    }

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td><a href=\"/central/servlet/StockReorderInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den="
                           + den + "&dnm=" + dnm + "&bnm=" + bnm+"\">");
    scoutln(out, bytesOut, "<img src=\"" + imagesDir + "z358.gif\" border=0></a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
