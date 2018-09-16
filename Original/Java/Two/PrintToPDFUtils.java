// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Print to PDF
// Module: PrintToPDFUtils.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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
import java.net.*;

public class PrintToPDFUtils extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminPrintControlNetwork adminPrintControlNetwork = new AdminPrintControlNetwork();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";

    try
    {
      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // fileName
      p2  = req.getParameter("p2"); // servlet  - set if coming from a document rather a report
      p3  = req.getParameter("p3"); // code     - set if coming from a document rather a report
      p4  = req.getParameter("p4"); // option   - set if coming from a document rather a report
      p5  = req.getParameter("p5"); // docName

      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";
  
      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "PrintToPDFUtils", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11200, bytesOut[0], 0, "ERR:" + p1 + " " + p3);
      if(out != null) out.flush(); 
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String fileName, String p2, String p3, String p4, String docName,
                    int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String reportsDir    = directoryUtils.getUserDir('R', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "11200", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 11200, bytesOut[0], 0, "SID:" + fileName);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    generate(unm, sid, uty, men, den, dnm, bnm, docName, p2, p3, p4);

    adminPrintControlNetwork.pprToPS(unm, dnm, fileName, "", "");
    
    docName = createDocName(docName, p3);

    psToPDF(unm, dnm, fileName, docName);

    download(res, reportsDir, docName + ".pdf", bytesOut);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 11200, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), fileName);
    if(con != null) con.close();
  }
   
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String createDocName(String docName, String docCode) throws Exception
  {
    String s = docName + "_";

    int len = docCode.length();
    for(int x=0;x<len;++x)
    {
      if(docCode.charAt(x) >= '0' && docCode.charAt(x) <= '9')
        s += docCode.charAt(x);
    }
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String generate(String unm, String sid, String uty, String men, String den, String dnm, String bnm, String docName, String servlet,
                          String code, String option) throws Exception
  {
    URL url;
    
    if(docName.equals("Invoice") || docName.equals("CreditNote") || docName.equals("DebitNote"))
      url = new URL("http://" + men + "/central/servlet/" + servlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&p2=" + option + "&p3=F&dnm=" + dnm + "&bnm=" + bnm);
    else url = new URL("http://" + men + "/central/servlet/" + servlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&p2=" + option + "&dnm=" + dnm + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    String numPages = s;
    while(s != null)
      s = di.readLine();

    di.close();

    return numPages;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void psToPDF(String unm, String dnm, String fileName, String docName) throws Exception
  {
    Process p;
    BufferedReader reader;
    String currentLine;

    Runtime r = Runtime.getRuntime();

    String commandArray = "/Zara/Support/Scripts/Pdf/psToPDF.sh /Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/" + fileName + ".ps " + "/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/" + docName + ".pdf";
    p = r.exec(commandArray);

    p.waitFor();
   // System.out.println("Process exit code is: " + p.exitValue());

    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    reader = new BufferedReader(isr);

    String res="";
    while((currentLine = reader.readLine()) != null)
      res += currentLine;

    reader.close();
    isr.close();
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
    catch(Exception e) // finally
    {
      if(in != null)
        in.close();
    }
      
    File file = new File(dirName + fileName);
    long fileSize = file.length(); 

    bytesOut[0] += (int)fileSize;
  }

}
