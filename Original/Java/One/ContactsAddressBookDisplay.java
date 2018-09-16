// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: Upload address book - display
// Module: ContactsAddressBookDisplay.java
// Author: C.K.Harvey
// Copyright (c) 2000-07 Christopher Harvey. All Rights Reserved.
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

public class ContactsAddressBookDisplay extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="";

    try
    {
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);
      directoryUtils.setContentHeaders(res);

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
        if(name.equals("p1")) // full filename of tmp file
          p1 = value[0];
      }

      doIt(req, res, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
    }    
    catch(Exception e)
    {
      serverUtils.etotalBytes(req, unm, dnm, 8812, bytesOut[0], 0, "ERR:" + p1);
      System.out.println(("Unexpected System Error: 8812a: " + e));
      res.getWriter().write("Unexpected System Error: 8812a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    String[] opStr = new String[1];  opStr[0] = "";

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      opStr[0] = "<br><br><p>Access Denied";
    }
    else
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      opStr[0] = "<br><br><p>Access Denied - Duplicate SignOn or Session Timeout";
    }
    else
    {
      analyze(con, stmt, rs, req, p1, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, opStr, bytesOut);
     
      File file = new File(p1);

      serverUtils.etotalBytes(req, unm, dnm, 8812, bytesOut[0], (int)file.length(), p1);
      generalUtils.fileDelete(p1);

      opStr[0] += "<tr><td>&nbsp;</td></tr>\n";

      opStr[0] += "<tr><td></td><td colspan=3><p><a href=\"javascript:update()\">Add to Contacts</a></td></tr>\n";
      opStr[0] += "<tr><td>&nbsp;</td></tr>\n";
      opStr[0] += "<tr><td>&nbsp;</td></tr>\n";

      opStr[0] += "</table></form></div></body></html>";

    }
    
    res.setContentLength(opStr[0].length());

    res.setContentType("text/html");
    res.setHeader("Cache-Control", "no-cache");
    
    res.getWriter().write(opStr[0]);
  
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8812, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void analyze(Connection con, Statement stmt, ResultSet rs, HttpServletRequest req, String tmpFile, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir,
                       String[] opStr, int[] bytesOut) throws Exception
  {   
    opStr[0] += "<html><head><title>Upload Address Book</title>\n";

    opStr[0] += "<script language=\"JavaScript\">\n";

    opStr[0] += "function update(){document.forms[0].submit()}\n";

    opStr[0] += "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
             + "general.css\"></head>\n";

    opStr[0] += pageFrameUtils.outputPageFrame(con, stmt, rs, req, "", false, false, "", "", "", "", "", "ContactsAddressBookUpload", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);

    opStr[0] += pageFrameUtils.drawTitle("Upload Address Book", "8812", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    opStr[0] += "<form action=\"/central/servlet/ContactsAddressBookUpdate\" enctype=\"application/x-www-form-urlencoded\" method=post>\n";
    opStr[0] += "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>\n";
    opStr[0] += "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>\n";
    opStr[0] += "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>\n";
    opStr[0] += "<input type=\"hidden\" name=\"men\" value='" + men + "'>\n";
    opStr[0] += "<input type=\"hidden\" name=\"den\" value='" + den + "'>\n";
    opStr[0] += "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>\n";
    opStr[0] += "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>\n";

    opStr[0] += "<table id=\"page\" width=100% border=0>\n";

    opStr[0] += "<tr><td></td><td id=\"pageColumn\" nowrap><p>eMail Address</td><td id=\"pageColumn\" nowrap><p>Names</td>"
             + "<td id=\"pageColumn\" nowrap><p>Company Name</td></tr>\n";
    
    // ensure that first line of file has legal field names
    
    RandomAccessFile fh;
    if((fh = generalUtils.fileOpen(tmpFile)) == null) // just-in-case: file not found
    {
      opStr[0] += "<tr><td><p>Uploaded File Not Found</td></tr>\n";
      generalUtils.fileClose(fh);
      return;
    }

    String tmp;
    
    try
    {
      tmp = fh.readLine();
    }
    catch(Exception e) // File Empty
    {
      opStr[0] += "<tr><td><p>Uploaded File Empty!</td></tr>\n";
      generalUtils.fileClose(fh);
      return;
    }

    String[] cssFormat = new String[1];  cssFormat[0] = "line2";

    String[] tag   = new String[1];
    String[] entry = new String[1];
    String familyName="", eMailAddr="", givenName="", companyName="";
    boolean oneDone = false;

    try
    {
      while(tmp != null)
      {
        if(tmp.length() == 0)
        {
          outputLine(eMailAddr, givenName + " " + familyName, companyName, cssFormat, opStr);
          eMailAddr   = "";
          familyName  = "";
          givenName   = "";
          companyName = "";
          oneDone = false;
        }
        else
        {  
          getEntry(tmp, tag, entry);

          if(tag[0].equalsIgnoreCase("mail"))
            eMailAddr = entry[0];

          if(tag[0].equalsIgnoreCase("sn"))
            familyName = entry[0];

          if(tag[0].equalsIgnoreCase("givenName"))
            givenName = entry[0];

          if(tag[0].equalsIgnoreCase("cn"))
            companyName = entry[0];
        
          oneDone = true;
        }
      
        tmp = fh.readLine();
      }
    }
    catch(Exception e) { }

    if(oneDone)
    {
      outputLine(eMailAddr, givenName + " " + familyName, companyName, cssFormat, opStr);
    }

    generalUtils.fileClose(fh);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputLine(String eMailAddr, String names, String companyName, String[] cssFormat, String[] opStr) throws Exception
  {
    if(cssFormat[0].equals("line2"))
      cssFormat[0] = "line1";
    else cssFormat[0] = "line2";

    opStr[0] += "<tr id=\"" + cssFormat[0] + "\">";

    String checkboxName = generalUtils.sanitise(eMailAddr + "\001" + names + "\001" + companyName + "\001");
    opStr[0] += "<tr id=\"" + cssFormat[0] + "\"><td nowrap><p><input type=checkbox name=\"" + checkboxName + "\" checked></td><td nowrap><p>" + eMailAddr
             + "</td><td nowrap><p>" + names + "</td><td nowrap><p>" + companyName + "</td></tr>\n";

  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getEntry(String line, String[] tag, String[] entry)
  {
    int len = line.length();
    
    tag[0] = "";
    int x=0;
    while(x < len && line.charAt(x) != ':')
    {
      if(generalUtils.isASCII(line.charAt(x)))
        tag[0] += line.charAt(x);
      ++x;
    }
    
    ++x;
    while(x < len && line.charAt(x) == ' ')
      ++x;

    entry[0] = "";
    while(x < len)
    {
      if(generalUtils.isASCII(line.charAt(x)))
        entry[0] += line.charAt(x);
      ++x;
    }
  }

}
