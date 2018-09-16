// =======================================================================================================================================================================================================
// System: ZaraStar: ExtUser - save cart to inbox
// Module: ExternalUserCartInbox.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class ExternalUserCartInbox extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  DocumentUtils documentUtils = new DocumentUtils();
  Inbox inbox = new Inbox();
  Inventory inventory = new Inventory();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AdminControlUtils adminControlUtils = new AdminControlUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", companyCode="", companyName="", fao="", designation="", eMail="", phone="", fax="", shipAddr1="", shipAddr2="", shipAddr3="", shipAddr4="", shipAddr5="", shipCountry="",
           billAddr1="", billAddr2="", billAddr3="", billAddr4="", billAddr5="", billCountry="", notes="", reference="", orderOrEnquiry="", band="";

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
        if(name.equals("p1"))
          orderOrEnquiry = value[0];
        else
        if(name.equals("companyCode"))
          companyCode = value[0];
        else
        if(name.equals("companyName"))
          companyName = value[0];
        else
        if(name.equals("fao"))
          fao = value[0];
        else
        if(name.equals("designation"))
          designation = value[0];
        else
        if(name.equals("eMail"))
          eMail = value[0];
        else
        if(name.equals("phone"))
          phone = value[0];
        else
        if(name.equals("fax"))
          fax = value[0];
        else
        if(name.equals("shipAddr1"))
          shipAddr1 = value[0];
        else
        if(name.equals("shipAddr2"))
          shipAddr2 = value[0];
        else
        if(name.equals("shipAddr3"))
          shipAddr3 = value[0];
        else
        if(name.equals("shipAddr4"))
          shipAddr4 = value[0];
        else
        if(name.equals("shipAddr5"))
          shipAddr5 = value[0];
        else
        if(name.equals("shipCountry"))
          shipCountry = value[0];
        else
        if(name.equals("billAddr1"))
          billAddr1 = value[0];
        else
        if(name.equals("billAddr2"))
          billAddr2 = value[0];
        else
        if(name.equals("billAddr3"))
          billAddr3 = value[0];
        else
        if(name.equals("billAddr4"))
          billAddr4 = value[0];
        else
        if(name.equals("billAddr5"))
          billAddr5 = value[0];
        else
        if(name.equals("billCountry"))
          billCountry = value[0];
        else
        if(name.equals("notes"))
          notes = value[0];
        else
        if(name.equals("reference"))
          reference = value[0];
        else
        if(name.equals("band"))
          band = value[0];
      }
        
      if(companyCode == null) companyCode = "";
      if(companyName == null) companyName = "";
      if(fao == null)         fao = "";
      if(designation == null) designation = "";
      if(eMail == null)       eMail = "";
      if(phone == null)       phone = "";
      if(fax == null)         fax = "";
      if(shipAddr1 == null)   shipAddr1 = "";
      if(shipAddr2 == null)   shipAddr2 = "";
      if(shipAddr3 == null)   shipAddr3 = "";
      if(shipAddr4 == null)   shipAddr4 = "";
      if(shipAddr5 == null)   shipAddr5 = "";
      if(shipCountry == null) shipCountry = "";
      if(billAddr1 == null)   billAddr1 = "";
      if(billAddr2 == null)   billAddr2 = "";
      if(billAddr3 == null)   billAddr3 = "";
      if(billAddr4 == null)   billAddr4 = "";
      if(billAddr5 == null)   billAddr5 = "";
      if(billCountry == null) billCountry = "";
      if(notes == null)       notes = "";
      if(reference == null)   reference = "";
      if(band == null)        band = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, orderOrEnquiry, companyCode, companyName, fao, designation, eMail, phone, fax, shipAddr1, shipAddr2, shipAddr3, shipAddr4, shipAddr5, shipCountry, billAddr1, billAddr2, billAddr3, billAddr4,
           billAddr5, billCountry, notes, reference, band, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ExternalUserCartInbox", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 915, bytesOut[0], 0, "ERR:" + orderOrEnquiry + ":" + companyCode);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String orderOrEnquiry, String companyCode, String companyName, String fao, String designation,
                    String eMail, String phone, String fax, String shipAddr1, String shipAddr2, String shipAddr3, String shipAddr4, String shipAddr5, String shipCountry, String billAddr1, String billAddr2, String billAddr3, String billAddr4,
                    String billAddr5, String billCountry, String notes, String reference, String band, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    Statement stmt2 = null;
    ResultSet rs2 = null;
    
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + pWord);

    if(! adminControlUtils.notDisabled(con, stmt, rs, 915))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ExternalUserCartInbox", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 915, bytesOut[0], 0, "ACC:" + orderOrEnquiry + ":" + companyCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ExternalUserCartInbox", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 915, bytesOut[0], 0, "SID:" + orderOrEnquiry + ":" + companyCode);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    byte[] nextCode = new byte[21];
    boolean res;
    if(writeNewHeaderRec(con, stmt, rs, unm, dnm, orderOrEnquiry, companyCode, companyName, fao, designation, eMail, phone, fax, shipAddr1, shipAddr2, shipAddr3, shipAddr4, shipAddr5, shipCountry, billAddr1, billAddr2, billAddr3, billAddr4,
                         billAddr5, billCountry, notes, reference, localDefnsDir, defnsDir, nextCode) == ' ')
    {
      if(writeNewLineRecs(con, stmt, stmt2, rs, rs2, nextCode, band, unm, dnm, localDefnsDir, defnsDir) == ' ')
        res = true;
      else res = false;
    }
    else res = false;

    if(res)
      inbox.getRecToHTMLDisplayOnly(con, stmt, rs, out, req, nextCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
    else messagePage.msgScreen(false, out, req, 39, unm, sid, uty, men, den, dnm, bnm, "ExternalUserCartInbox", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 915, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), orderOrEnquiry + ":" + companyCode);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private char writeNewHeaderRec(Connection con, Statement stmt, ResultSet rs, String unm, String dnm, String orderOrEnquiry, String companyCode, String companyName, String fao, String designation, String eMail, String phone, String fax,
                                 String shipAddr1, String shipAddr2, String shipAddr3, String shipAddr4, String shipAddr5, String shipCountry, String billAddr1, String billAddr2, String billAddr3, String billAddr4, String billAddr5,
                                 String billCountry, String notes, String reference, String localDefnsDir, String defnsDir, byte[] nextCode) throws Exception
  {
    byte[] buf = new byte[1000];

    generalUtils.putAlpha(buf, 1000, (short)1, generalUtils.todaySQLFormat(localDefnsDir, defnsDir));
    generalUtils.putAlpha(buf, 1000, (short)2, companyName);
    generalUtils.putAlpha(buf, 1000, (short)3, fao);
    generalUtils.putAlpha(buf, 1000, (short)4, designation);
    generalUtils.putAlpha(buf, 1000, (short)5, eMail);
    generalUtils.putAlpha(buf, 1000, (short)6, phone);
    generalUtils.putAlpha(buf, 1000, (short)7, fax);

    if(orderOrEnquiry.equals("S")) // order
    {
      generalUtils.putAlpha(buf, 1000, (short)8,  shipAddr1);
      generalUtils.putAlpha(buf, 1000, (short)9,  shipAddr2);
      generalUtils.putAlpha(buf, 1000, (short)10, shipAddr3);
      generalUtils.putAlpha(buf, 1000, (short)11, shipAddr4);
      generalUtils.putAlpha(buf, 1000, (short)12, shipAddr5);
      generalUtils.putAlpha(buf, 1000, (short)13, shipCountry);
      generalUtils.putAlpha(buf, 1000, (short)14, billAddr1);
      generalUtils.putAlpha(buf, 1000, (short)15, billAddr2);
      generalUtils.putAlpha(buf, 1000, (short)16, billAddr3);
      generalUtils.putAlpha(buf, 1000, (short)17, billAddr4);
      generalUtils.putAlpha(buf, 1000, (short)18, billAddr5);
      generalUtils.putAlpha(buf, 1000, (short)19, billCountry);
    }

    if(orderOrEnquiry.equals("S")) // order
      generalUtils.putAlpha(buf, 1000, (short)20, "S");
    else generalUtils.putAlpha(buf, 1000, (short)20, "E");

    generalUtils.putAlpha(buf, 1000, (short)21, notes);
    generalUtils.putAlpha(buf, 1000, (short)22, reference);
    generalUtils.putAlpha(buf, 1000, (short)23, companyCode);

    generalUtils.putAlpha(buf, 1000, (short)24, "N"); // processed

    generalUtils.putAlpha(buf, 1000, (short)26, unm); // signOn
    generalUtils.putAlpha(buf, 1000, (short)27, "L"); // status
    generalUtils.putAlpha(buf, 1000, (short)28, "");  // userCodeFrom
    generalUtils.putAlpha(buf, 1000, (short)29, "");  // userCodeTo

    documentUtils.getNextCode(con, stmt, rs, "inbox", true, nextCode);

    generalUtils.putAlpha(buf, 1000, (short)0, nextCode);

    return inbox.putRecHeadGivenCode(con, stmt, rs, nextCode, 'N', buf, dnm, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private char writeNewLineRecs(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, byte[] nextCode, String band, String unm, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String cartTable;

    if(unm.startsWith("_")) // anon user
      cartTable = unm.substring(1) + "_cart_tmp";
    else
    {    
      int i = unm.indexOf("_");
      if(i != -1) // registered user
        cartTable = unm.substring(0, i) + "_cart_tmp";
      else cartTable = unm + "_cart_tmp";
    }

    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT Line, ItemCode, Mfr, MfrCode, Description, Quantity, UoM, Price, Currency FROM " + cartTable + " ORDER BY Line");

      String line, itemCode, mfr, mfrCode, desc, qty, uom, price, currency;
      byte[] buf = new byte[1000];
      
      while(rs.next())                  
      {
        line     = rs.getString(1);
        itemCode = rs.getString(2);
        mfr      = rs.getString(3);
        mfrCode  = rs.getString(4);
        desc     = rs.getString(5);
        qty      = rs.getString(6);
        uom      = rs.getString(7);
        price    = rs.getString(8);
        currency = rs.getString(9);
  
        if(itemCode == null) itemCode = "-";
        if(mfr      == null) mfr = "-";
        if(desc     == null) desc = "-";
        if(qty      == null) qty = "0";
        if(uom      == null) uom = "";
        if(price    == null) price = "0";
        if(currency == null) currency = "";
        
        generalUtils.repAlpha(buf, 1000, (short)0,  nextCode);
        generalUtils.repAlpha(buf, 1000, (short)1,  line);
        generalUtils.repAlpha(buf, 1000, (short)2,  itemCode);
        generalUtils.repAlpha(buf, 1000, (short)3,  mfr);
        generalUtils.repAlpha(buf, 1000, (short)4,  qty);

        if(generalUtils.doubleFromStr(price) == 0.0)
        {
          String[] sellPrice     = new String[1];
          String[] salesCurrency = new String[1];
          String[] uom2          = new String[1];
          String[] descs         = new String[1];
          inventory.getPriceDetailsGivenCode(con, stmt2, rs2, itemCode, band, dnm, localDefnsDir, defnsDir, descs, sellPrice, salesCurrency, uom2);
          price    = sellPrice[0];
          currency = salesCurrency[0];
          uom      = uom2[0];
        }
        
        generalUtils.repAlpha(buf, 1000, (short)5,  currency);
        generalUtils.repAlpha(buf, 1000, (short)6,  price);
        generalUtils.repAlpha(buf, 1000, (short)7,  uom);
        generalUtils.repAlpha(buf, 1000, (short)8,  desc);
        generalUtils.repAlpha(buf, 1000, (short)10, unm);
        generalUtils.repAlpha(buf, 1000, (short)11, mfrCode);

        inbox.putRecLine(con, stmt2, rs2, nextCode, buf, 'N', buf, dnm, localDefnsDir, defnsDir);
      }
                 
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    
    return ' ';
  }

}
