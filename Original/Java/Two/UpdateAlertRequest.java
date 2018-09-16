// =======================================================================================================================================================================================================
// System: ZaraStar: Util: send request for update alert
// Module: UpdateAlertRequest.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class UpdateAlertRequest extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DefinitionTables definitionTables = new DefinitionTables();
  Profile profile = new Profile();
  Customer customer = new Customer();
  UpdateAlertUtils updateAlertUtils = new UpdateAlertUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p1  = req.getParameter("p1"); // docCode
      p2  = req.getParameter("p2"); // docType
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      System.out.println("13001: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "UpdateAlertRequest", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13001, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "UpdateAlertRequest", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 13001, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    generateAlert(con, stmt, rs, out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 13001, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void generateAlert(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String docCode, String docType, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                            String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Send Update Alert</title>");

    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "13001", "", "UpdateAlertRequest", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Update Alert", "13001", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id='page' width='100%' border='0'>");

    scoutln(out, bytesOut, "<tr><td><p>For Document Code: &nbsp; &nbsp; " + docCode + "</td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    String eMail = "", senderName = "", senderEMail = "", custCode = "", custPOCode = "", shipper = "", waybill = "", dateShipped = "", enquiryCode = "", title = "";

    String senderCompany = "xxx Ltd";
    
    if(docType.equals("Q"))
    {
      title = "Quotation Created";
      
      Quotation quotation = new Quotation();
      
      eMail = quotation.getAQuoteFieldGivenCode(con, stmt, rs, "BuyerEMail", docCode);
      
      custCode = quotation.getAQuoteFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);
      
      if(! customer.existsCompanyRecGivenCode(con, stmt, rs, custCode, dnm, localDefnsDir, defnsDir))
        custCode = "";

      enquiryCode = quotation.getAQuoteFieldGivenCode(con, stmt, rs, "EnquiryCode", docCode);

      senderName = profile.getNameFromProfile(con, stmt, rs, unm);

      senderEMail = profile.getEMailFromProfiled(con, stmt, rs, unm);
      if(unm.equals("Sysadmin")) senderEMail="xxx@xxx.com";
    }
    else
    if(docType.equals("D"))
    {
      title = "Delivery Order Goods Delivered";
      
      DeliveryOrder deliveryOrder = new DeliveryOrder();
      
      eMail = deliveryOrder.getADOFieldGivenCode(con, stmt, rs, "BuyerEMail", docCode);
      
      custCode = deliveryOrder.getADOFieldGivenCode(con, stmt, rs, "CompanyCode", docCode);

      if(! customer.existsCompanyRecGivenCode(con, stmt, rs, custCode, dnm, localDefnsDir, defnsDir))
        custCode = "";

      custPOCode = deliveryOrder.getADOFieldGivenCode(con, stmt, rs, "PORefNum", docCode);

      shipper = deliveryOrder.getADOFieldGivenCode(con, stmt, rs, "Shipper", docCode);
      waybill = deliveryOrder.getADOFieldGivenCode(con, stmt, rs, "Waybill", docCode);
      dateShipped = deliveryOrder.getADOFieldGivenCode(con, stmt, rs, "DateShipped", docCode);

      senderName = profile.getNameFromProfile(con, stmt, rs, unm);

      senderEMail = profile.getEMailFromProfiled(con, stmt, rs, unm);
      if(unm.equals("Sysadmin")) senderEMail="xxx@xxx.com";
    }

    if(eMail.length() > 0 && senderEMail.length() > 0 && custCode.length() > 0)
    {
      String waveID = getWaveIDGivenDocCode(con, stmt, rs, docCode, docType);
        
      // check whether to create new access profile for user
      
      String[] newAccessCode = new String[1];  newAccessCode[0] = "";
      String[] newPassWord   = new String[1];  newPassWord[0]   = "";
      
      if(checkProfiles(con, stmt, rs, eMail, custCode, "", dnm, localDefnsDir, defnsDir, newAccessCode, newPassWord))
      {
        String data = "x="              + URLEncoder.encode(newAccessCode[0], "UTF-8") + "\n"
                    + "y="              + URLEncoder.encode(newPassWord[0],   "UTF-8") + "\n"
                    + "docType="        + URLEncoder.encode(docType,          "UTF-8") + "\n"
                    + "docCode="        + URLEncoder.encode(docCode,          "UTF-8") + "\n"
                    + "eMail="          + URLEncoder.encode(eMail,            "UTF-8") + "\n"
                    + "senderName="     + URLEncoder.encode(senderName,       "UTF-8") + "\n"
                    + "senderEMail="    + URLEncoder.encode(senderEMail,      "UTF-8") + "\n"
                    + "senderCompany="  + URLEncoder.encode(senderCompany,    "UTF-8") + "\n"
                    + "senderDNM="      + URLEncoder.encode(dnm,              "UTF-8") + "\n"
                    + "senderURL="      + URLEncoder.encode("cfcgo.com",      "UTF-8") + "\n"
                    + "customerCode="   + URLEncoder.encode(custCode,         "UTF-8") + "\n"
                    + "customerPOCode=" + URLEncoder.encode(custPOCode,       "UTF-8") + "\n"
                    + "waveID="         + URLEncoder.encode(waveID,           "UTF-8") + "\n"
                    + "shipper="        + URLEncoder.encode(shipper,          "UTF-8") + "\n"
                    + "waybill="        + URLEncoder.encode(waybill,          "UTF-8") + "\n"
                    + "dateShipped="    + URLEncoder.encode(dateShipped,      "UTF-8") + "\n"
                    + "enquiryCode="    + URLEncoder.encode(enquiryCode,      "UTF-8") + "\n";

        String res = getResFromRemote("http://oreddy.com", data);

        if(res.startsWith("ERR:"))
        {
          scoutln(out, bytesOut, "<tr><td><p><font size=3 color='red'>Sending of eMail to " + eMail + " failed</td></tr>");
        }
        else
        {
          scoutln(out, bytesOut, "<tr><td><p><font size=3 color='green'>Sending of eMail to " + eMail + " succeeded</td></tr>");
          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
          if(res.length() > 3)
            scoutln(out, bytesOut, "<tr><td><p><font size=3>" + res.substring(3) + "</td></tr>");

          String[] newCode = new String[1];
          
          updateAlertUtils.add(con, stmt, rs, dnm, "", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), title, "E", docCode, docType, eMail, custCode, "C", newCode);
          
          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
          scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
          scoutln(out, bytesOut, "<tr><td><p>Update Alert Code: " + newCode[0] + "</p></td></tr>");
        }
      }
      else
      {
        scoutln(out, bytesOut, "<tr><td><p><font size=3 color='red'>Sending of eMail to " + eMail + " failed (Profile failure)</td></tr>");
      }
    }
    else
    {
      if(eMail.length() == 0)
        scoutln(out, bytesOut, "<tr><td><p><font size=3 color='red'>There is no eMail Address on the Document</td></tr>");
      if(senderEMail.length() == 0)
        scoutln(out, bytesOut, "<tr><td><p><font size=3 color='red'>You do not have an eMail Address on your Profile</td></tr>");
      if(custCode.length() == 0)
        scoutln(out, bytesOut, "<tr><td><p><font size=3 color='red'>The Customer Code is Invalid</td></tr>");
    }
  
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    if(out != null)
    {
      out.println(str);
      bytesOut[0] += (str.length() + 2);    
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void callRemote(String remoteURL, String servlet, String data, String ses, StringBuilder res) throws Exception
  {
    try
    {
      byte[] b = Base64.encodeBase64(data.getBytes());

      String pb = new String(b);

      if(! remoteURL.startsWith("http://"))
        remoteURL = "http://" + remoteURL;
      
      URL url = new URL(remoteURL + "/" + servlet + "/" + ses + ".rdf");

      HttpURLConnection uc = (HttpURLConnection)url.openConnection();

      uc.setRequestProperty("Content-Length", "" + pb.length());
      
      uc.setDoInput(true);
      uc.setDoOutput(true);
      uc.setUseCaches(false);
      uc.setDefaultUseCaches(false);
      uc.setDoOutput(true);

      uc.setRequestProperty("Content-Type", "application/octet-stream");

      uc.setRequestMethod("POST");

      PrintWriter p = new PrintWriter(uc.getOutputStream());
      p.print(pb);
      p.flush();
      p.close();

      BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

      String s = di.readLine();

      b = Base64.decodeBase64(s);

      parseResult(new String(b), res);

      di.close();
    }
    catch(Exception e)
    {
      System.out.println("13001: callRemote(): " + e);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void parseResult(String xml, StringBuilder res)
  {
    res.delete(0, res.length());
            
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    try
    {
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document dom = db.parse(new InputSource(new StringReader(xml)));

      Element docEle = dom.getDocumentElement();

      NodeList nl = docEle.getElementsByTagName("iota:data");

      if(nl != null && nl.getLength() > 0)
      {
        Element el = (Element)nl.item(0);

        res.append(el.getFirstChild().getNodeValue());
      }
    }
    catch(Exception e)
    {
      System.out.println("88: parseResult(): " + e);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getResFromRemote(String remoteURL, String data)
  {
    String s = "";
    
    try
    {
      StringBuilder res = new StringBuilder(256);
      
      callRemote(remoteURL, "UpdateAlertRequestr", data, "x", res);
        
      s = res.toString();

      if(s.equals("ACC") || s.equals("STO"))
        return "";
    }
    catch(Exception e)
    {
      System.out.println("13001: getResFromRemote(): " + e);
    }
    
    return s;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // only returns first match
    public String getWaveIDGivenDocCode(Connection con, Statement stmt, ResultSet rs, String docCode, String docType) throws Exception
  {
    String waveID = "";

    try
    {
      stmt = con.createStatement();

      stmt.setMaxRows(1);

System.out.println("SELECT WaveID FROM waveletc WHERE DocCode = '" + docCode + "' AND DocType = '" + docType + "'");
      rs = stmt.executeQuery("SELECT WaveID FROM waveletc WHERE DocCode = '" + docCode + "' AND DocType = '" + docType + "'");

      if(rs.next())
        waveID = rs.getString(1);

      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println("13001: getWaveIDGivenDocCode(): " + e);
      if(stmt != null) stmt.close();
      return "";
    }

    return waveID;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean checkProfiles(Connection con, Statement stmt, ResultSet rs, String eMail, String customerCode, String supplierCode, String dnm, String localDefnsDir, String defnsDir, String[] newAccessCode, String[] newPassWord)
  {
    boolean res = false;
    
    try
    {
System.out.println("... checking profiles for " + customerCode);
      if(profile.getProfileGivenCustomerCode(con, stmt, rs, customerCode).length() == 0)
      {
        newAccessCode[0] = profile.getNewNumericUserCode(con, stmt, rs);
System.out.println("... newAccessCode: " + newAccessCode[0]);
        
        if(newAccessCode[0].length() > 0) // just-in-case
        {
          newPassWord[0] = generalUtils.generatePassWord();
System.out.println("... newPassWord: " + newPassWord[0]);
          
System.out.println("... creating profiles");    
          if(profile.updateProfile(newAccessCode[0], "", newPassWord[0], "L", "", "", "1970-01-01", "", dnm))
          {
System.out.println("... creating profilesd");    
            if(profile.updateProfiled(newAccessCode[0], "", generalUtils.todaySQLFormat(localDefnsDir, defnsDir), "", "N", "", "", "", "C", eMail, "External Access for " + customerCode, "N", "N", "N", "N", "N", customerCode, supplierCode, dnm))
              res = true;
          }
        }
      }
      else res = true;
    }
    catch(Exception e)
    {
      System.out.println("13001: checkProfiles(): " + e);
    }
System.out.println("... checkProfiles() res: " + res);    
    return res;
  }              
              
}
