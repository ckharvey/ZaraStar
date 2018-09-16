// =======================================================================================================================================================================================================
// System: ZaraStar Analytic: Document trace - do it
// Module: DocumentTraceExecute.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class DocumentTraceExecute extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Customer customer = new Customer();

  private boolean maxedOut = false;
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", bta = "";

    try
    {
      directoryUtils.setContentHeaders2(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // docCode
      p2  = req.getParameter("p2"); // type
      bta = req.getParameter("bta");

      if(bta == null) bta = "";

      maxedOut = false;

      boolean backTrackingAllowed = false;
      if(bta.equals("Y"))
        backTrackingAllowed = true;

      doIt(out, req, backTrackingAllowed, false, p1, p2.charAt(0), unm, sid, uty, men, den, dnm, bnm, bytesOut);
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
 
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentTrace", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1025, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", cpo="", ce="", q="", so="", oc="", pl="", dorder="", i="", pro="", grn="", po="", lp="", dn="", cn="", oa="", traceCode="", bta = "";

    try
    {
      directoryUtils.setContentHeaders2(res);
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
        if(name.equals("cpo"))
          cpo = value[0];
        else
        if(name.equals("ce"))   
          ce = value[0];
        else
        if(name.equals("oa"))
          oa = value[0];
        else
        if(name.equals("oc"))
          oc = value[0];
        else
        if(name.equals("q"))
          q = value[0];
        else
        if(name.equals("so"))
          so = value[0];
        else
        if(name.equals("do"))
          dorder = value[0];
        else
        if(name.equals("i"))
          i = value[0];
        else
        if(name.equals("pl"))
          pl = value[0];
        else
        if(name.equals("pro"))
          pro = value[0];
        else
        if(name.equals("grn"))
          grn = value[0];
        else
        if(name.equals("po"))
          po = value[0];
        else
        if(name.equals("lp"))
          lp = value[0];
        else
        if(name.equals("dn"))
          dn = value[0];
        else
        if(name.equals("cn"))
          cn = value[0];
        else
        if(name.equals("bta"))
          bta = value[0];
      }
      
      if(cpo    == null) cpo    = "";
      if(ce     == null) ce     = "";
      if(q      == null) q      = "";
      if(so     == null) so     = "";
      if(oa     == null) oa     = "";
      if(oc     == null) oc     = "";
      if(pl     == null) pl     = "";
      if(dorder == null) dorder = "";
      if(i      == null) i      = "";
      if(pro    == null) pro    = "";
      if(grn    == null) grn    = "";
      if(po     == null) po     = "";
      if(lp     == null) lp     = "";
      if(dn     == null) dn     = "";
      if(cn     == null) cn     = "";
      
      boolean moreThanOne = false;
      char traceType = ' ';
      
      if(cpo.length() != 0)
      {
        traceType = 'X';
        traceCode = cpo;
      }
      
      if(ce.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'E';
          traceCode = ce;
        }
      }
      
      if(q.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'Q';
          traceCode = q;
        }
      }

      if(so.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'S';
          traceCode = so;
        }
      }
    
      if(oa.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'B';
          traceCode = oa;
        }
      }
    
      if(oc.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'O';
          traceCode = oc;
        }
      }
    
      if(pl.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'P';
          traceCode = pl;
        }
      }

      if(dorder.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'D';
          traceCode = dorder;
        }
      }
      
      if(i.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'I';
          traceCode = i;
        }
      }
      
      if(pro.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'R';
          traceCode = pro;
        }
      }
      
      if(grn.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'G';
          traceCode = grn;
        }
      }
      
      if(po.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'Y';
          traceCode = po;
        }
      }
      
      if(lp.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'Z';
          traceCode = lp;
        }
      }

      if(dn.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'N';
          traceCode = dn;
        }
      }
      
      if(cn.length() != 0)
      {
        if(traceType != ' ')
          moreThanOne = true;
        else
        {
          traceType = 'C';
          traceCode = cn;
        }
      }

      maxedOut = false;

      if(bta == null) bta = "";

      boolean backTrackingAllowed = false;
      if(bta.equals("Y"))
        backTrackingAllowed = true;

      doIt(out, req, backTrackingAllowed, moreThanOne, traceCode, traceType, unm, sid, uty, men, den, dnm, bnm, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "DocumentTraceExecute", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1025, bytesOut[0], 0, "ERR:" + traceCode);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, boolean backTrackingAllowed, boolean moreThanOne, String traceCode, char traceType, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String localDefnsDir  = directoryUtils.getLocalOverrideDir(dnm);
    String defnsDir       = directoryUtils.getSupportDirs('D');
    String imagesDir      = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;
    
    int serviceCode = 0;
    if(traceType == 'S') serviceCode = 4121; else
    if(traceType == 'Q') serviceCode = 4122; else
    if(traceType == 'D') serviceCode = 4123; else
    if(traceType == 'I') serviceCode = 4124; else
    if(traceType == 'R') serviceCode = 4125; else
    if(traceType == 'O') serviceCode = 4126; else
    if(traceType == 'B') serviceCode = 4127;

    if(! (((uty.equals("R") && authenticationUtils.verifyAccessForDocument(con, stmt, rs, req, serviceCode, traceCode, unm, sid, uty, dnm, localDefnsDir, defnsDir))
      || (uty.equals("I") && authenticationUtils.verifyAccess(con, stmt, rs, req, 1025, unm, uty, dnm, localDefnsDir, defnsDir)))))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "DocumentTrace", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1025, bytesOut[0], 0, "ACC:" + traceCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "DocumentTrace", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 1025, bytesOut[0], 0, "SID:" + traceCode);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    traceCode = generalUtils.stripLeadingAndTrailingSpaces(traceCode);
    
    process(con, stmt, rs, out, req, backTrackingAllowed, moreThanOne, traceCode, traceType, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1025, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), traceCode);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, boolean backTrackingAllowed, boolean moreThanOne, String traceCode, char traceType, String unm, String sid, String uty, String men,
                       String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Document Trace</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 139, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewPO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/PurchaseOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 152, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewLP(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/LocalPurchasePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewProforma(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoiceRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 150, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "function viewProforma(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ProformaInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }
    
    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoiceRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 149, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "function viewInvoice(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesInvoicePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }

    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 142, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "function viewQuote(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/QuotationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }
    
    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 140, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "function viewSO(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 155, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesCreditNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrdeRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 148, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "function viewDO(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/DeliveryOrderPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 156, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewDN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SalesDebitNotePage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewOC(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 146, unm, uty, dnm, localDefnsDir, defnsDir))
     {
        scoutln(out, bytesOut, "function viewOC(code){var p1=sanitise(code);");
         scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderConfirmationPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }    
    if(uty.equals("R"))
    {
      scoutln(out, bytesOut, "function viewOA(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderAcknowledgementRegisteredUser?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+p1;}");
    }
    else
    {
      if(authenticationUtils.verifyAccess(con, stmt, rs, req, 170, unm, uty, dnm, localDefnsDir, defnsDir))
      {
        scoutln(out, bytesOut, "function viewOA(code){var p1=sanitise(code);");
        scoutln(out, bytesOut, "this.location.href=\"/central/servlet/OrderAcknowledgementPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
      }
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 153, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewGRN(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 147, unm, uty, dnm, localDefnsDir, defnsDir))
    {
       scoutln(out, bytesOut, "function viewPL(code){var p1=sanitise(code);");
       scoutln(out, bytesOut, "this.location.href=\"/central/servlet/GoodsReceivedPickingList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p3=&p4=&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function sanitise(code){");
    scoutln(out, bytesOut, "var code2='';var x;var len=code.length;");
    scoutln(out, bytesOut, "for(x=0;x<len;++x)");
    scoutln(out, bytesOut, "if(code.charAt(x)=='#')code2+='%23';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='\"')code2+='%22';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='&')code2+='%26';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='%')code2+='%25';");
    scoutln(out, bytesOut, "else if(code.charAt(x)==' ')code2+='%20';");
    scoutln(out, bytesOut, "else if(code.charAt(x)=='?')code+='%3F';");
    scoutln(out, bytesOut, "else code2+=code.charAt(x);");
    scoutln(out, bytesOut, "return code2};");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");
    
    scoutln(out, bytesOut, "<script language=\"JavaScript\">"); 

    // text/HTML of the hints
    scoutln(out, bytesOut, "var HINTS_ITEMS = [ 'details' ];");
    scoutln(out, bytesOut, "var myHint;");
    
    scoutln(out, bytesOut, "var req2;");
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function details(docType,docCode){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/DocumentTraceHints?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + docType + \"&p2=\" + docCode + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status == 200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length > 0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var hint = req2.responseXML.getElementsByTagName(\"hint\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('hints');");
    scoutln(out, bytesOut, "messageElement.innerHTML=layout(hint);");
    scoutln(out, bytesOut, "document.getElementById('hints').style.height = (15 * numRows(hint));");
    scoutln(out, bytesOut, "document.getElementById('hints').style.width = (7 * numCols(hint));");
    scoutln(out, bytesOut, "}}}}}");
    
    scoutln(out, bytesOut, "var req3;");
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function hint(code){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/SupportHelpHints?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req3.send(null);}");

    scoutln(out, bytesOut, "function processRequest3(){");
    scoutln(out, bytesOut, "if(req3.readyState==4){");
    scoutln(out, bytesOut, "if(req3.status == 200){");
    scoutln(out, bytesOut, "var res=req3.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length > 0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var hint = req3.responseXML.getElementsByTagName(\"hint\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var messageElement = document.getElementById('hints');");
    scoutln(out, bytesOut, "messageElement.innerHTML=layout(hint);");
    scoutln(out, bytesOut, "document.getElementById('hints').style.height = (15 * numRows(hint));");
    scoutln(out, bytesOut, "document.getElementById('hints').style.width = (7 * numCols(hint));");
    scoutln(out, bytesOut, "}}}}}");

    scoutln(out, bytesOut, "function layout(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,t='';");
    scoutln(out, bytesOut, "while(x < len){");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')t += '<br />';");
    scoutln(out, bytesOut, "else t += s.charAt(x);++x;}");
    scoutln(out, bytesOut, "return t;}");
    
    scoutln(out, bytesOut, "function numRows(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,n=1;");
    scoutln(out, bytesOut, "while(x<len){");
    scoutln(out, bytesOut, "if(s.charAt(x) == '`')++n;++x;}");
    scoutln(out, bytesOut, "return n;}");
    
    scoutln(out, bytesOut, "function numCols(s){");
    scoutln(out, bytesOut, "var x=0,len=s.length,n=0,y=0;");
    scoutln(out, bytesOut, "while(x < len){");
    scoutln(out, bytesOut, "if(s.charAt(x)=='`')");
    scoutln(out, bytesOut, "{if(y>n)n=y;y=0;}else ++y;++x;}");
    scoutln(out, bytesOut, "if(y>n)n=y;return n;}");

    scoutln(out, bytesOut, "</script>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\" src=\"" + directoryUtils.getSupportDirs('S') + "tigra_hints.js\"></script></head>");
    
    int[] hmenuCount = new int[1];
    
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "1025", "", "DocumentTrace", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
             
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    // configuration variable for the hint object, these setting will be shared among all hints created by this object
    scoutln(out, bytesOut, "var HINTS_CFG = { 'wise'       : true," +
            "	'margin'     : 10," +
            "	'gap'        : 0," +
            "	'align'      : 'tcbc'," +
            "	'css'        : 'hintsClass'," +
            "	'show_delay' : 100," +
            "	'hide_delay' : 100," +
            "   'follow'     : false," +
            "	'z-index'    : 100," +
            "	'IEfix'      : false," +
            "	'IEtrans'    : ['blendTrans(DURATION=.3)', 'blendTrans(DURATION=.3)']," +
            "	'opacity'    : 100 " +
            "};");

    scoutln(out, bytesOut, "</script><script language=\"JavaScript\">myHint = new THints (HINTS_ITEMS, HINTS_CFG);</script>");

    scoutln(out, bytesOut, "<div id=\"hints\" style=\"position:absolute;border:2px dotted red;top:100px;left:100px;z-index:1000;"
                         + "visibility:hidden;width:10px;height:10px; font-family: tahoma, verdana, arial; font-size: 12px; background-color: #f0f0f0; color: #000000;padding: 5px;\"></div>");

    scoutln(out, bytesOut, "<form>");
  
    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Document Trace", "1025",  unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<p>Documents related to ");

    switch(traceType)
    {
      case 'X' : scout(out, bytesOut, "Customer PO");            break;
      case 'E' : scout(out, bytesOut, "Customer Enquiry");       break;
      case 'Q' : scout(out, bytesOut, "Quotation");              break;
      case 'S' : scout(out, bytesOut, "Sales Order");            break;
      case 'B' : scout(out, bytesOut, "Order Acknowledgement");  break;
      case 'O' : scout(out, bytesOut, "Order Confirmation");     break;
      case 'P' : scout(out, bytesOut, "Picking List");           break;
      case 'D' : scout(out, bytesOut, "Delivery Order");         break;
      case 'I' : scout(out, bytesOut, "Sales Invoice");          break;
      case 'R' : scout(out, bytesOut, "Proforma Invoice");       break;
      case 'G' : scout(out, bytesOut, "Goods Received Note");    break;
      case 'Y' : scout(out, bytesOut, "Purchase Order");         break;
      case 'Z' : scout(out, bytesOut, "Local Purchase");         break;
      case 'N' : scout(out, bytesOut, "Debit Note");             break;
      case 'C' : scout(out, bytesOut, "Credit Note");            break;
    }
    scoutln(out, bytesOut, ": &nbsp; &nbsp; " + traceCode);
    
    scoutln(out, bytesOut, "<br><table id=\"page\" width=100%>");
    
    if(moreThanOne)
    {
      scoutln(out, bytesOut, "<br><br><p>Only specify ONE document code<br><br>");
      scoutln(out, bytesOut, "</table></form>");
      scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
      return;
    }
    
    String[][]  codes    = new String [1][100];
    char[][]    types    = new char[1][100];
    boolean[][] searched = new boolean[1][100];
    
    int[] codesLen = new int[1];  codesLen[0] = 100;

    int x, numEntries = 0;

    numEntries = addToList(backTrackingAllowed, traceCode, traceType, numEntries, codes, types, searched, codesLen);
      
    boolean atLeastOneRemaining = true;
    while(atLeastOneRemaining && ! maxedOut)
    {
      for(x=0;x<numEntries;++x)
      {
        if(! searched[0][x])
        {
          numEntries = allSearches(con, stmt, rs, backTrackingAllowed, codes[0][x], types[0][x], codes, types, searched, codesLen, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
          searched[0][x] = true;
        }
      }

      atLeastOneRemaining = false;
      for(x=0;x<numEntries;++x)
      {
        if(! searched[0][x])
          atLeastOneRemaining = true;
      }
    }

    displayResults(con, stmt, rs, out, numEntries, backTrackingAllowed, codes, types, imagesDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }   
        
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int allSearches(Connection con, Statement stmt, ResultSet rs, boolean backTrackingAllowed, String thisCode, char thisType, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, int numEntries, HttpServletRequest req,
                          String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
      try{


    if(thisType == 'O')
      numEntries = searchOCHeadersGivenOCCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);

    if(thisType == 'B')
    {
      numEntries = searchOAHeadersGivenOACode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }

    if(thisType == 'Y')
    {
      numEntries = searchPOLinesGivenPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchGRNHeadersGivenPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchGRNLinesGivenPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }

    if(thisType == 'G')
    {
      numEntries = searchGRNHeadersGivenGRCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchGRNLinesGivenGRCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }
    
    if(thisType == 'Z')
    {
      numEntries = searchLPLinesGivenLPCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchGRNHeadersGivenLPCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchGRNLinesGivenLPCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }
    
    if(thisType == 'S')
    {
      numEntries = searchSOHeadersGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchPLLinesGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchDOLinesGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchPOLinesGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchLPLinesGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchOCHeadersGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchOAHeadersGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchProformaLinesGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchInvoiceLinesGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
   }
  
    if(thisType == 'X')
    {
      numEntries = searchSOHeadersGivenCPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchPLHeadersGivenCustPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchProformaHeadersGivenCustPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchInvoiceHeadersGivenCustPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchDNHeadersGivenCustPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchDOHeadersGivenCustPOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }
    
    if(thisType == 'E')
      numEntries = searchQuoteHeadersGivenEnquiryCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
  
    if(thisType == 'R')
    {
      numEntries = searchProformaHeadersGivenProformaCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchProformaLinesGivenProformaCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }

    if(thisType == 'I')
    {
      numEntries = searchInvoiceHeadersGivenInvoiceCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchInvoiceLinesGivenInvoiceCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchDOHeadersGivenInvoiceCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchPLHeadersGivenInvoiceCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchCNLinesGivenInvoiceCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchDNHeadersGivenInvoiceCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
    }
    
    if(thisType == 'Q')
    {
      numEntries = searchSOHeadersGivenQuoteCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
    }
    
    if(thisType == 'P')
    {
      numEntries = searchPLHeadersGivenPLCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchPLLinesGivenPLCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchDOHeadersGivenPLCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchInvoiceHeadersGivenPLCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm,
                                              localDefnsDir, defnsDir);
      numEntries = searchInvoiceLinesGivenSOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }
    
    if(thisType == 'D')
    {
      numEntries = searchDOHeadersGivenDOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchDOLinesGivenDOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
      numEntries = searchInvoiceHeadersGivenDOCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }

    if(thisType == 'C')
    {
      numEntries = searchCNLinesGivenCNCode(con, stmt, rs, thisCode, codes, types, searched, codesLen, backTrackingAllowed, numEntries, req, unm, sid, uty, dnm, localDefnsDir, defnsDir);
    }
    }
      catch(Exception e)
      {
        System.out.println(e);
      }
  
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNHeadersGivenGRCode(Connection con, Statement stmt, ResultSet rs, String grCode, String[][] codes, char[][] types,
                                          boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                          String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(grCode, con, stmt, rs, grCode, 'G', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT POCode FROM gr WHERE GRCode = '" + grCode  + "'");
    
    String code;
    
    while(rs.next())
    {    
      code = rs.getString(1);
      if(checkCodeExists(grCode, con, stmt, rs, code, 'Y', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, code, 'Y', numEntries, codes, types, searched, codesLen);

      if(checkCodeExists(grCode, con, stmt, rs, code, 'Z', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, code, 'Z', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPOLinesGivenPOCode(Connection con, Statement stmt, ResultSet rs, String poCode, String[][] codes, char[][] types,
                                       boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(poCode, con, stmt, rs, poCode, 'Y', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM pol WHERE POCode = '" + poCode  + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(poCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceLinesGivenInvoiceCode(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String[][] codes, char[][] types,
                                       boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(invoiceCode, con, stmt, rs, invoiceCode, 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM invoicel WHERE InvoiceCode = '" + invoiceCode  + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(invoiceCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOLinesGivenDOCode(Connection con, Statement stmt, ResultSet rs, String doCode, String[][] codes, char[][] types,
                                       boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(doCode, con, stmt, rs, doCode, 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM dol WHERE DOCode = '" + doCode  + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(doCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchLPLinesGivenLPCode(Connection con, Statement stmt, ResultSet rs, String lpCode, String[][] codes, char[][] types,
                                       boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(lpCode, con, stmt, rs, lpCode, 'Z', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM lpl WHERE LPCode = '" + lpCode  + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(lpCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNHeadersGivenPOCode(Connection con, Statement stmt, ResultSet rs, String poCode, String[][] codes, char[][] types,
                                          boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                          String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(poCode, con, stmt, rs, poCode, 'Y', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT GRCode FROM gr WHERE POCode = '" + poCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(poCode, con, stmt, rs, rs.getString(1), 'G', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'G', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNLinesGivenPOCode(Connection con, Statement stmt, ResultSet rs, String poCode, String[][] codes, char[][] types,
                                        boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                        String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(poCode, con, stmt, rs, poCode, 'Y', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT GRCode FROM grl WHERE POCode = '" + poCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(poCode, con, stmt, rs, rs.getString(1), 'G', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'G', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNLinesGivenGRCode(Connection con, Statement stmt, ResultSet rs, String grCode, String[][] codes, char[][] types,
                                        boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                        String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(grCode, con, stmt, rs, grCode, 'G', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT POCode FROM grl WHERE GRCode = '" + grCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(grCode, con, stmt, rs, rs.getString(1), 'Y', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'Y', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNHeadersGivenLPCode(Connection con, Statement stmt, ResultSet rs, String lpCode, String[][] codes, char[][] types,
                                          boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                          String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(lpCode, con, stmt, rs, lpCode, 'Z', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT GRCode FROM gr WHERE POCode = '" + lpCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(lpCode, con, stmt, rs, rs.getString(1), 'G', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'G', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchGRNLinesGivenLPCode(Connection con, Statement stmt, ResultSet rs, String lpCode, String[][] codes, char[][] types,
                                        boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                        String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(lpCode, con, stmt, rs, lpCode, 'Z', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT GRCode FROM grl WHERE POCode = '" + lpCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(lpCode, con, stmt, rs, rs.getString(1), 'G', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'G', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchSOHeadersGivenCPOCode(Connection con, Statement stmt, ResultSet rs, String cpoCode, String[][] codes, char[][] types,
                                          boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                          String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
if(cpoCode.equals("-")) return numEntries;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM so WHERE CustomerPOCode = '" + cpoCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(cpoCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchSOHeadersGivenQuoteCode(Connection con, Statement stmt, ResultSet rs, String quoteCode, String[][] codes, char[][] types,
                                            boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                            String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(quoteCode, con, stmt, rs, quoteCode, 'Q', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM so WHERE QuoteCode = '" + quoteCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(quoteCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchSOHeadersGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types,
                                         boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT QuoteCode, CustomerPOCode FROM so WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Q', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'Q', numEntries, codes, types, searched, codesLen);
      
      if(rs.getString(2).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'X', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchOCHeadersGivenOCCode(Connection con, Statement stmt, ResultSet rs, String ocCode, String[][] codes, char[][] types,
                                         boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(ocCode, con, stmt, rs, ocCode, 'O', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode, CustomerPOCode FROM oc WHERE OCCode = '" + ocCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(ocCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
      if(rs.getString(2).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'X', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchOAHeadersGivenOACode(Connection con, Statement stmt, ResultSet rs, String oaCode, String[][] codes, char[][] types,
                                         boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(oaCode, con, stmt, rs, oaCode, 'B', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode, CustomerPOCode FROM oa WHERE OACode = '" + oaCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(oaCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
      if(rs.getString(2).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'X', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchQuoteHeadersGivenEnquiryCode(Connection con, Statement stmt, ResultSet rs, String enquiryCode, String[][] codes, char[][] types,
                                                 boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                                 String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT QuoteCode FROM quote WHERE EnquiryCode = '" + enquiryCode + "'");

    while(rs.next())
    {    
      if(rs.getString(1).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'Q', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPLHeadersGivenCustPOCode(Connection con, Statement stmt, ResultSet rs, String customerPOCode, String[][] codes, char[][] types,
                                             boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                             String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(customerPOCode.equals("-")) return numEntries;

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT PLCode FROM pl WHERE CustomerPOCode = '" + customerPOCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(customerPOCode, con, stmt, rs, rs.getString(1), 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'P', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPLHeadersGivenInvoiceCode(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String[][] codes, char[][] types,
                                              boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                              String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(invoiceCode, con, stmt, rs, invoiceCode, 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT PLCode FROM pl WHERE InvoiceCode = '" + invoiceCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(invoiceCode, con, stmt, rs, rs.getString(1), 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'P', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPLLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT PLCode FROM pll WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'P', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT DOCode FROM dol WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'D', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req,
                                            String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT InvoiceCode FROM invoicel WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'I', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPLLinesGivenPLCode(Connection con, Statement stmt, ResultSet rs, String plCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(plCode, con, stmt, rs, plCode, 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM pll WHERE PLCode = '" + plCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(plCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOHeadersGivenPLCode(Connection con, Statement stmt, ResultSet rs, String plCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(plCode, con, stmt, rs, plCode, 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DOCode FROM do WHERE PLCode = '" + plCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(plCode, con, stmt, rs, rs.getString(1), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'D', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOHeadersGivenInvoiceCode(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm,
                                              String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(invoiceCode, con, stmt, rs, invoiceCode, 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DOCode FROM do WHERE InvoiceCode = '" + invoiceCode + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(invoiceCode, con, stmt, rs, rs.getString(1), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'D', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchProformaHeadersGivenCustPOCode(Connection con, Statement stmt, ResultSet rs, String customerPOCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req,
                                                   String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT ProformaCode FROM proforma WHERE PORefNum = '" + generalUtils.sanitiseForSQL(customerPOCode) + "'");

    while(rs.next())
    {    
      if(checkCodeExists(customerPOCode, con, stmt, rs, rs.getString(1), 'R', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'R', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchProformaHeadersGivenProformaCode(Connection con, Statement stmt, ResultSet rs, String proformaCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req,
                                                     String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(proformaCode, con, stmt, rs, proformaCode, 'R', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT PORefNum FROM proforma WHERE ProformaCode = '" + proformaCode + "'");

    while(rs.next())
    {    
      numEntries = addToList(backTrackingAllowed, rs.getString(1), 'X', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DORefNum FROM proforma WHERE ProformaCode = '" + proformaCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(proformaCode, con, stmt, rs, rs.getString(1), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'D', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchProformaLinesGivenProformaCode(Connection con, Statement stmt, ResultSet rs, String proformaCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req,
                                                   String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(proformaCode, con, stmt, rs, proformaCode, 'R', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT SOCode FROM proformal WHERE ProformaCode = '" + proformaCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(proformaCode, con, stmt, rs, rs.getString(1), 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'S', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();

    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchProformaLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                             String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ProformaCode FROM proformal WHERE SOCode = '" + soCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'R', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'R', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceHeadersGivenPLCode(Connection con, Statement stmt, ResultSet rs, String plCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                              String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(plCode, con, stmt, rs, plCode, 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT InvoiceCode, DORefNum, PORefNum FROM invoice WHERE PLCode = '" + plCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(plCode, con, stmt, rs, rs.getString(1), 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'I', numEntries, codes, types, searched, codesLen);
      if(checkCodeExists(plCode, con, stmt, rs, rs.getString(2), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))   
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'D', numEntries, codes, types, searched, codesLen);
      if(rs.getString(3).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(3), 'X', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceHeadersGivenDOCode(Connection con, Statement stmt, ResultSet rs, String doCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req,
                                              String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(doCode, con, stmt, rs, doCode, 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT InvoiceCode, PORefNum, PLCode FROM invoice WHERE DORefNum = '" + doCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(doCode, con, stmt, rs, rs.getString(1), 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'I', numEntries, codes, types, searched, codesLen);

      if(rs.getString(2).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'X', numEntries, codes, types, searched, codesLen);
      
      if(checkCodeExists(doCode, con, stmt, rs, rs.getString(3), 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))   
        numEntries = addToList(backTrackingAllowed, rs.getString(3), 'P', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOHeadersGivenDOCode(Connection con, Statement stmt, ResultSet rs, String doCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(doCode, con, stmt, rs, doCode, 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT PLCode, PORefNum, InvoiceCode FROM do WHERE DOCode = '" + doCode  + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(doCode, con, stmt, rs, rs.getString(1), 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'P', numEntries, codes, types, searched, codesLen);

      if(rs.getString(2).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'X', numEntries, codes, types, searched, codesLen);
      
      if(checkCodeExists(doCode, con, stmt, rs, rs.getString(3), 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))   
        numEntries = addToList(backTrackingAllowed, rs.getString(3), 'I', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPLHeadersGivenPLCode(Connection con, Statement stmt, ResultSet rs, String plCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(plCode, con, stmt, rs, plCode, 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT CustomerPOCode, InvoiceCode FROM pl WHERE PLCode = '" + plCode  + "'");
    
    while(rs.next())
    {    
      if(rs.getString(1).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'X', numEntries, codes, types, searched, codesLen);
      if(checkCodeExists(plCode, con, stmt, rs, rs.getString(2), 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))   
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'I', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDOHeadersGivenCustPOCode(Connection con, Statement stmt, ResultSet rs, String custPOCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                             String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DOCode FROM do WHERE PORefNum = '" + generalUtils.sanitiseForSQL(custPOCode)  + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(custPOCode, con, stmt, rs, rs.getString(1), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'D', numEntries, codes, types, searched, codesLen);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceHeadersGivenCustPOCode(Connection con, Statement stmt, ResultSet rs, String customerPOCode, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req,
                                                  String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT InvoiceCode, DORefNum, PLCode FROM invoice WHERE PORefNum = '" + generalUtils.sanitiseForSQL(customerPOCode) + "'");

    while(rs.next())
    {    
      if(checkCodeExists(customerPOCode, con, stmt, rs, rs.getString(1), 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'I', numEntries, codes, types, searched, codesLen);
      if(checkCodeExists(customerPOCode, con, stmt, rs, rs.getString(2), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))   
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'D', numEntries, codes, types, searched, codesLen);
      if(checkCodeExists(customerPOCode, con, stmt, rs, rs.getString(3), 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))   
        numEntries = addToList(backTrackingAllowed, rs.getString(3), 'P', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchInvoiceHeadersGivenInvoiceCode(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String[][] codes, char[][] types,
                                                   boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm,
                                                   String sid, String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(invoiceCode, con, stmt, rs, invoiceCode, 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DORefNum, PORefNum, PLCode FROM invoice WHERE InvoiceCode = '" + invoiceCode + "'");

    while(rs.next())
    {    
      if(checkCodeExists(invoiceCode, con, stmt, rs, rs.getString(1), 'D', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'D', numEntries, codes, types, searched, codesLen);

      if(rs.getString(2).length() > 0)
        numEntries = addToList(backTrackingAllowed, rs.getString(2), 'X', numEntries, codes, types, searched, codesLen);
      
      if(checkCodeExists(invoiceCode, con, stmt, rs, rs.getString(3), 'P', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(3), 'P', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDNHeadersGivenInvoiceCode(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String[][] codes, char[][] types,
                                              boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                              String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(invoiceCode, con, stmt, rs, invoiceCode, 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DNCode FROM debit WHERE InvoiceCode = '" + invoiceCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(invoiceCode, con, stmt, rs, rs.getString(1), 'N', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'N', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchDNHeadersGivenCustPOCode(Connection con, Statement stmt, ResultSet rs, String customerPOCode, String[][] codes, char[][] types,
                                             boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                             String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT DNCode FROM debit WHERE PORefNum = '" + generalUtils.sanitiseForSQL(customerPOCode) + "'");

    while(rs.next())
    {    
      if(checkCodeExists(customerPOCode, con, stmt, rs, rs.getString(1), 'N', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'N', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchCNLinesGivenInvoiceCode(Connection con, Statement stmt, ResultSet rs, String invoiceCode, String[][] codes, char[][] types,
                                            boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                            String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(invoiceCode, con, stmt, rs, invoiceCode, 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT CNCode FROM creditl WHERE InvoiceCode = '" + invoiceCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(invoiceCode, con, stmt, rs, rs.getString(1), 'C', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'C', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
 
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchCNLinesGivenCNCode(Connection con, Statement stmt, ResultSet rs, String cnCode, String[][] codes, char[][] types,
                                       boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(cnCode, con, stmt, rs, cnCode, 'C', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT InvoiceCode FROM creditl WHERE CNCode = '" + cnCode  + "'");
    
    while(rs.next())
    {    
      if(checkCodeExists(cnCode, con, stmt, rs, rs.getString(1), 'I', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'I', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchPOLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types,
                                       boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   
 
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT POCode FROM pol WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Y', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'Y', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchLPLinesGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types,
                                       boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                       String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT LPCode FROM lpl WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'Z', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'Z', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchOCHeadersGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types,
                                         boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT OCCode FROM oc WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'O', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'O', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int searchOAHeadersGivenSOCode(Connection con, Statement stmt, ResultSet rs, String soCode, String[][] codes, char[][] types,
                                         boolean[][] searched, int[] codesLen, boolean backTrackingAllowed, int numEntries, HttpServletRequest req, String unm, String sid,
                                         String uty, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(! checkCodeExists(soCode, con, stmt, rs, soCode, 'S', req, unm, sid, uty, dnm, localDefnsDir, defnsDir)) return numEntries;   

    stmt = con.createStatement();
    
    rs = stmt.executeQuery("SELECT OACode FROM oa WHERE SOCode = '" + soCode  + "'");

    while(rs.next())
    {    
      if(checkCodeExists(soCode, con, stmt, rs, rs.getString(1), 'B', req, unm, sid, uty, dnm, localDefnsDir, defnsDir))
        numEntries = addToList(backTrackingAllowed, rs.getString(1), 'B', numEntries, codes, types, searched, codesLen);
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    
    return numEntries;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int addToList(boolean backTrackingAllowed, String newCode, char newType, int numEntries, String[][] codes, char[][] types, boolean[][] searched, int[] codesLen)
                        throws Exception
  {
    if(numEntries == 20 && ! backTrackingAllowed) { maxedOut = true;   return numEntries; } ////////// max

    if(newCode == null || newCode.length() == 0)
      return numEntries;

    if(newCode.indexOf("'") != -1)
      return numEntries;

    for(int x=0;x<numEntries;++x)
    {
      if(codes[0][x].equalsIgnoreCase(newCode))
      {
        if(types[0][x] == newType)
        {
          return numEntries; // already on list
        }     
      }
    }
    
    if(numEntries >= codesLen[0])
    {
      int y, len = codesLen[0];
      
      String[] buf = new String[len];
      for(y=0;y<len;++y)
        buf[y] = codes[0][y];
      codesLen[0] += 100;
      codes[0] = new String[codesLen[0]];
      for(y=0;y<len;++y)
        codes[0][y] = buf[y];

      char[] cbuf = new char[len];
      for(y=0;y<len;++y)
        cbuf[y] = types[0][y];
      types[0] = new char[codesLen[0]];
      for(y=0;y<len;++y)
        types[0][y] = cbuf[y];
      
      boolean[] bbuf = new boolean[len];
      for(y=0;y<len;++y)
        bbuf[y] = searched[0][y];
      searched[0] = new boolean[codesLen[0]];
      for(y=0;y<len;++y)
        searched[0][y] = bbuf[y];
    }
    
    codes[0][numEntries] = newCode;
    types[0][numEntries] = newType;
    searched[0][numEntries] = false;

    return (numEntries + 1);
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayResults(Connection con, Statement stmt, ResultSet rs, PrintWriter out, int numEntries, boolean backTrackingAllowed, String[][] codes, char[][] types, String imagesDir, int[] bytesOut) throws Exception
  {
    if(! backTrackingAllowed && maxedOut)
    {
      scoutln(out, bytesOut, "<tr><td colspan=100><p><font color=red>Maximum number of documents reached.</td></tr>");
    }

    displayDocs(con, stmt, rs, out, 'Q', "viewQuote",    "1079.gif", "1079c.gif", "quote",    "QuoteCode",    "DocumentStatus", numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'S', "viewSO",       "1183.gif", "1183c.gif", "so",       "SOCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'B', "viewOA",       "1295.png", "1295c.png", "oa",       "OACode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'O', "viewOC",       "1296.gif", "1296c.gif", "oc",       "OCCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'P', "viewPL",       "1294.gif", "1294c.gif", "pl",       "PLCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'D', "viewDO",       "1100.gif", "1100c.gif", "do",       "DOCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'I', "viewInvoice",  "1033.gif", "1033c.gif", "invoice",  "InvoiceCode",  "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'R', "viewProforma", "1011.gif", "1011c.gif", "proforma", "ProformaCode", "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'N', "viewDN",       "1208.gif", "1208c.gif", "debit",    "DNCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'C', "viewCN",       "1042.gif", "1042c.gif", "credit",   "CNCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'Y', "viewPO",       "1102.gif", "1102c.gif", "po",       "POCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'Z', "viewLP",       "1192.gif", "1192c.gif", "lp",       "LPCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
    displayDocs(con, stmt, rs, out, 'G', "viewGRN",      "1298.gif", "1298c.gif", "gr",       "GRCode",       "Status",         numEntries, codes[0],
                types[0], imagesDir, bytesOut);
  }   

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void displayDocs(Connection con, Statement stmt, ResultSet rs, PrintWriter out, char type, String viewJS, String imageFileLive,
                           String imageFileCancelled, String tableName, String docCodeName, String docStatusName, int numEntries, String[] codes,
                           char[] types, String imagesDir, int[] bytesOut) throws Exception
  {
    String imageFile;
    boolean first = true;
    for(int x=0;x<numEntries;++x)
    {
      if(types[x] == type)
      {
        if(first)
        {
          scout(out, bytesOut, "<tr>");
          first = false;
        }

        if(getDetails(con, stmt, rs, codes[x], tableName, docCodeName, docStatusName))
          imageFile = imageFileLive;          
        else imageFile = imageFileCancelled;        
     
        scoutln(out, bytesOut, "<td><br><a href=\"javascript:" + viewJS + "('" + codes[x] + "')\"><img src=\"" + imagesDir
                             + imageFile + "\" border=0  onMouseOver=\"details('" + tableName + "','" + codes[x] + "');myHint.show('hints', this)\" onMouseOut=\"myHint.hide()\"       ><br>" + codes[x] + "</a></td>");
      }
    }
    
    if(! first)
      scout(out, bytesOut, "</tr>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getDetails(Connection con, Statement stmt, ResultSet rs, String docCode, String tableName, String docCodeName, String docStatusName) throws Exception
  {    
     stmt = con.createStatement();

     rs = stmt.executeQuery("SELECT " + docStatusName + " FROM " + tableName + " WHERE " + docCodeName + " = '" + docCode + "'");
    
     String status = "";
     
     if(rs.next()) // just-in-case
       status = rs.getString(1);
         
     if(rs   != null) rs.close();
     if(stmt != null) stmt.close();
    
     if(status.equals("C"))
       return false;
     
     return true;     
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean checkCodeExists(String callerCode, Connection con, Statement stmt, ResultSet rs, String code, char type, HttpServletRequest req, String unm, String sid, String uty, String dnm, String localDefnsDir, String defnsDir)
                                  throws Exception
  {   
    if(code.length() == 0)
      return false;
    
    boolean res = false;
    int serviceCode = 0;
    
    switch(type)
    {
      case 'Q' : res = existsGivenCode(con, stmt, rs, code, "quote",    "QuoteCode");    serviceCode = 4122; break;
      case 'S' : res = existsGivenCode(con, stmt, rs, code, "so",       "SOCode");       serviceCode = 4121; break;
      case 'B' : res = existsGivenCode(con, stmt, rs, code, "oa",       "OACode");       serviceCode = 4129; break;
      case 'O' : res = existsGivenCode(con, stmt, rs, code, "oc",       "OCCode");       serviceCode = 4126; break;
      case 'P' : res = existsGivenCode(con, stmt, rs, code, "pl",       "PLCode");       break;
      case 'D' : res = existsGivenCode(con, stmt, rs, code, "do",       "DOCode");       serviceCode = 4123; break;
      case 'I' : res = existsGivenCode(con, stmt, rs, code, "invoice",  "InvoiceCode");  serviceCode = 4124; break;
      case 'R' : res = existsGivenCode(con, stmt, rs, code, "proforma", "ProformaCode"); serviceCode = 4125; break;
      case 'C' : res = existsGivenCode(con, stmt, rs, code, "credit",   "CNCode");       break;
      case 'Y' : res = existsGivenCode(con, stmt, rs, code, "po",       "POCode");       break;
      case 'Z' : res = existsGivenCode(con, stmt, rs, code, "lp",       "LPCode");       break;
      case 'G' : res = existsGivenCode(con, stmt, rs, code, "gr",       "GRCode");       break;
      case 'N' : res = existsGivenCode(con, stmt, rs, code, "debit",    "DNCode");       break;
    } 
     
    if(res) // so far ok
    {
      if(uty.equals("R"))
      {
        if(serviceCode != 0)
          res = authenticationUtils.verifyAccessForDocument(con, stmt, rs, req, serviceCode, code, unm, sid, uty, dnm, localDefnsDir, defnsDir);
        else res = false;
      }
    }
   
    return res;
  }      
      
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean existsGivenCode(Connection con, Statement stmt, ResultSet rs, String code, String tableName, String codeName) throws Exception
  {
    if(code.length() == 0) // just-in-case
      return false;
    
    if(code.indexOf("'") != -1)
      return false;

    code = code.toUpperCase();

    stmt = con.createStatement();
    
    int numRecs = 0;
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + tableName + " WHERE " + codeName + " = '" + code + "'");
    if(rs.next()) // just-in-case      
      numRecs = rs.getInt("rowcount") ;
    if(rs != null) rs.close();
   
    if(stmt != null) stmt.close();

    if(numRecs == 1)
      return true;
    
    return false;
  }
    
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();    
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
