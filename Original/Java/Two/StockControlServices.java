// =======================================================================================================================================================================================================
// System: ZaraStar: Stock Control
// Module: StockControlServices.java
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
import java.io.*;
import java.sql.*;

public class StockControlServices extends HttpServlet
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
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockControlServices", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 180, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush(); 
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                    String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;
    
    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 180, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "StockControlServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 180, bytesOut[0], 0, "ACC:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "StockControlServices", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 180, bytesOut[0], 0, "SID:");
      
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    set(con, stmt, rs, out, req, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 180, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      
      if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Control</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
  
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                        + "general.css\"></head>");
  
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "180", "", "StockControlServices", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
                                
    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Stock Control", "180",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");

    boolean _7801 = authenticationUtils.verifyAccess(con, stmt, rs, req, 7801, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _7802 = authenticationUtils.verifyAccess(con, stmt, rs, req, 7802, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _7803 = authenticationUtils.verifyAccess(con, stmt, rs, req, 7803, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _7805 = authenticationUtils.verifyAccess(con, stmt, rs, req, 7805, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _7806 = authenticationUtils.verifyAccess(con, stmt, rs, req, 7806, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(_7801 || _7802 || _7803 || _7805 || _7806)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Materials Planning Services</td></tr>");
    
      if(_7801)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_7801?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Generate Materials Plan</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('7801')\">(Service 7801)</a></span></td></tr>");
      }
    
      if(_7802)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_7802?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Generate Stock Requirements Status</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('7802')\">(Service 7802)</a></span></td></tr>");
      }
    
      if(_7803)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_7803?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Generate Weekly Delivery Status</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('7803')\">(Service 7803)</a></span></td></tr>");
      }

      if(_7805)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_7805?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">List Sales Orders With Missing Delivery Date</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('7805')\">(Service 7805)</a></span></td></tr>");
      }

      if(_7806)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_7806?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">List Purchase Orders With Missing Delivery Date</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('7806')\">(Service 7806)</a></span></td></tr>");
      }

    }

    boolean _1014 = authenticationUtils.verifyAccess(con, stmt, rs, req, 1014, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean _1017 = authenticationUtils.verifyAccess(con, stmt, rs, req, 1017, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ChartsServices  = authenticationUtils.verifyAccess(con, stmt, rs, req, 118,  unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockReorderInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1021, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ManufacturerSalesInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1022, unm, uty, dnm, localDefnsDir, defnsDir);        
    boolean StockUsageSpread = authenticationUtils.verifyAccess(con, stmt, rs, req, 3014, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ProductStockLevelListing = authenticationUtils.verifyAccess(con, stmt, rs, req, 3010, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean StockStatusPage = authenticationUtils.verifyAccess(con, stmt, rs, req, 3062, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ManufacturerPurchases = authenticationUtils.verifyAccess(con, stmt, rs, req, 1035, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ManufacturerPurchasesInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1036, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean PurchaseOrderDeliveryInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1037, unm, uty, dnm, localDefnsDir, defnsDir);
    boolean ManufacturerSalesByInvoiceInput = authenticationUtils.verifyAccess(con, stmt, rs, req, 1038, unm, uty, dnm, localDefnsDir, defnsDir);
        
    if(_1014 || _1017 || ChartsServices || StockReorderInput || ProductStockLevelListing || ManufacturerSalesInput || StockUsageSpread || ManufacturerPurchases || ManufacturerPurchasesInput || PurchaseOrderDeliveryInput || ManufacturerSalesByInvoiceInput || StockStatusPage)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Stock Report Services</td></tr>");
    
      if(_1014)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_1014?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock Usage Enquiry for Customer</a></td><td width=70%>"
                               + "<span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1014')\">(Service 1014)</a></span></td></tr>");
      }
    
      if(_1017)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/_1017?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock Usage Enquiry for Supplier</a></td><td width=70%>"
                               + "<span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1017')\">(Service 1017)</a></span></td></tr>");
      } 
    
      if(StockReorderInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockReorderInput?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock ReOrder</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('1021')\">(Service 1021)</a></span></td></tr>");
      }
 
      if(ProductStockLevelListing)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ProductStockLevelListing?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Stock Listing</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3010')\">(Service 3010)</a></span></td></tr>");
      }
      
      if(StockStatusPage)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockStatusPageExecute?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock Status Report</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3062')\">(Service 3062)</a></span></td></tr>");
        
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockMovementGenerate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock Movement Report</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3055')\">(Service 3055)</a></span></td></tr>");
      }
      
      if(ManufacturerSalesInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ManufacturerSalesInput?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Manufacturer Sales by Customer</a></td><td width=70%>"
                               + "<span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1022')\">(Service 1022)</a></span></td></tr>");
      }

      if(ManufacturerPurchases)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ManufacturerPurchases?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Manufacturer Purchases from PO Lines</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1035')\">(Service 1035)</a></span></td></tr>");
      }

      if(ManufacturerSalesByInvoiceInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ManufacturerSalesByInvoiceInput?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Manufacturer Sales from Invoice Lines</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1038')\">(Service 1038)</a></span></td></tr>");
      }

      if(ManufacturerPurchasesInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ManufacturerPurchasesInput?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Supplier Purchases from PO Lines</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1036')\">(Service 1036)</a></span></td></tr>");
      }

      if(StockUsageSpread)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/StockUsageSpread?unm="
                               + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                               + "&bnm=" + bnm + "\">Stock Usage Spread for a Manufacturer</a></td><td width=70%><span id=\"service\">"
                               + "&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('3014')\">(Service 3014)</a></span></td></tr>");
      }      

      if(PurchaseOrderDeliveryInput)
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/PurchaseOrderDeliveryInput?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Purchase Order Delivery Performance</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                               + "<a href=\"javascript:help('1037')\">(Service 1037)</a></span></td></tr>");
      }

      if(StockStatusPage || unm.equals("Desmondpoh"))
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/ManufacturerSalesBySalesperson?unm=" + unm + "&sid="
                               + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "\">Manufacturer Sales By SalesPerson</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;<a href=\"javascript:help('13055')\">(Service 13055)</a>"
                               + "</span></td></tr>");
      }

    }

    boolean MainPageUtils3 = authenticationUtils.verifyAccess(con, stmt, rs, req, 1003, unm, uty, dnm, localDefnsDir, defnsDir);
    
    if(MainPageUtils3)
    {
      scoutln(out, bytesOut, "<tr><td nowrap colspan=3><h1>Charts</td></tr>");

      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 1003, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "<tr><td width=100>&nbsp;</td><td nowrap><p><a href=\"/central/servlet/MainPageUtils3?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                             + "\">Chart: Inventory, Sales, and Purchases</a></td><td width=70%><span id=\"service\">&nbsp;&nbsp;&nbsp;"
                             + "<a href=\"javascript:help('1003')\">(Service 1003)</a></span></td></tr>");
      }
    }
    
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
