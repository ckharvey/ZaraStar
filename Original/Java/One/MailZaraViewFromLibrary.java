// =======================================================================================================================================================================================================
// System: ZaraStar MailEngine: View a Mail from Library
// Module: MailZaraViewFromLibrary.java
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
import java.io.*;
import java.sql.*;

public class MailZaraViewFromLibrary extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();
  
  static int attnum = 1;
  static int level = 0;

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
      uty = req.getParameter("uty");
      sid = req.getParameter("sid");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // msgNum

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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraViewFromLibrary", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8012, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, String p1, int[] bytesOut) throws Exception
  {
    String dataDir          = directoryUtils.getUserDir('U', dnm, unm);
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingMailDir   = directoryUtils.getUserDir('W', dnm, unm) + "Mail/";

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

   if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
   {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraViewFromLibrary", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8012, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraViewFromLibrary", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8012, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }    

    if(! generalUtils.isDirectory(directoryUtils.getUserDir('W', dnm, unm)))
      generalUtils.createDir(directoryUtils.getUserDir('W', dnm, unm), "755");

    generalUtils.directoryHierarchyDelete(workingMailDir);

    if(! generalUtils.isDirectory(workingMailDir))
      generalUtils.createDir(workingMailDir, "755");

    viewMail(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, workingMailDir, dataDir, localDefnsDir, defnsDir, p1, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8012, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void viewMail(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                        String bnm, String imagesDir, String workingMailDir, String dataDir, String localDefnsDir, String defnsDir, 
                        String msgNum, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>View Mail</title>");

    String cssDirectory = directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir);
            
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + cssDirectory + "general.css\">");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function unattach(fName){");
    scoutln(out, bytesOut, "document.forms[0].fileName.value=sanitise(fName);");
    scoutln(out, bytesOut, "document.attachment.submit();}");
    
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

    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];

    mailUtils.outputPageFrame(con, stmt, rs, out, req, false, "", "", "MailZaraViewFromLibrary", "8012", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount,
                          bytesOut);

    mailUtils.drawTitle(con, stmt, rs, req, out, false, "", "View Mail", "8012", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
 
    scoutln(out, bytesOut, "<form name=\"attachment\" enctype=\"multipart/form-data\" action=\"" + directoryUtils.getScriptsDirectory() + "unattach.php\" "
                         + "method=\"post\">");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"workingDir\" value='" + workingMailDir + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"fileName\">");
   
    try // in case backing onto this page after having deleted the msg
    {
      int[]    outputCount = new int[1];    outputCount[0] = 0;
      int[]    imageCount  = new int[1];    imageCount[0] = 0;
      String[] imageList    = new String[1]; imageList[0] = "";
      String[] usedImageList = new String[1]; usedImageList[0] = "";

      String message = getMessageTextFromFile(msgNum, unm, dnm, localDefnsDir, defnsDir);
      
      scoutln(out, bytesOut, message);
    }
    catch(Exception e) { System.out.println(e); }

    scoutln(out, bytesOut, "</td></tr></table></div></body></html>");
  }

  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String getMessageTextFromFile(String msgCode, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con  = null;
    Statement  stmt = null;
    ResultSet  rs   = null;
    
    String text = "";
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + uName + "&password=" + passWord);

      stmt = con.createStatement();
  
      String q = "SELECT Text FROM mail WHERE MsgCode='" + msgCode + "'";

      rs = stmt.executeQuery(q);
       
      if(rs.next()) // just-in-case                  
      {
        text = rs.getString(1);
      }
                 
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
    }    
    
    return text;
  }  

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
}

