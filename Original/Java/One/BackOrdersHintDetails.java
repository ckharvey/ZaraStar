// =======================================================================================================================================================================================================
// System: ZaraStar Analytics: Back Orders - fetch hint details
// Module: BackOrdersHintDetails.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
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
import java.util.Enumeration;

public class BackOrdersHintDetails extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  OrderConfirmation orderConfirmation = new OrderConfirmation();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", dnm="", soCode="";

    try
    {
      directoryUtils.setContentHeaders(res);
      res.setHeader("Header", "HTTP/1.0 200 OK");
      res.setStatus(200);

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String elementName = (String)en.nextElement();
        String[] value = req.getParameterValues(elementName);
        if(elementName.equals("unm"))
          unm = value[0];
        else
        if(elementName.equals("sid"))
          sid = value[0];
        else
        if(elementName.equals("uty"))
          uty = value[0];
        else
        if(elementName.equals("dnm"))
          dnm = value[0];
        else
        if(elementName.equals("p1"))
          soCode = value[0];
      }

      doIt(req, res, unm, sid, uty, dnm, soCode, bytesOut);
    }    
    catch(Exception e)
    {
      System.out.println(("Unexpected System Error: 1201a: " + e));
      res.getWriter().write("Unexpected System Error: 1201a");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String dnm, String soCode, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null, stmt3 = null;
    ResultSet rs   = null, rs2 = null, rs3 = null;
    
    String hint="";
    
    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      ;
    }
    else
    {
      hint = fetch(con, stmt, stmt2, stmt3, rs, rs2, rs3, soCode, dnm, localDefnsDir, defnsDir);
    }
    
    res.setContentType("text/xml");
    res.setHeader("Cache-Control", "no-cache");

    if(hint.length() == 0)
      hint = ".";
    else hint = generalUtils.sanitiseForXML(hint);
    
    String s = "<msg><res>.</res><hint>" + hint + "</hint></msg>";
    res.getWriter().write(s);
    
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 1201, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), soCode);
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String fetch(Connection con, Statement stmt, Statement stmt2, Statement stmt3, ResultSet rs, ResultSet rs2, ResultSet rs3, String soCode,
                       String dnm, String localDefnsDir, String defnsDir) throws Exception 
  {    
    try
    {
      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT Date, SOType, ToEngineering, EngineeringApproved, ToManager, ManagerApproved, ToProcurement, "
                           + "ProcurementConfirmed, ToScheduling, SchedulingConfirmed, EngineeringApprovedDate, EngineeringApprovedSignOn, "
                           + "ProcurementConfirmedDate, ProcurementConfirmedSignOn, SchedulingConfirmedDate, SchedulingConfirmedSignOn, "
                           + "ToEngineeringDate, ToEngineeringSignOn, ToProcurementDate, ToProcurementSignOn, ToSchedulingDate, ToSchedulingSignOn, "
                           + "ManagerApprovedDate, ManagerApprovedSignOn, ToManagerDate, ToManagerSignOn, SalesPerson FROM so WHERE SOCode = '"
                           + soCode + "'");
      
      String s, soDate, soType, toEngineering, engineeringApproved, toManager, managerApproved, toScheduling, schedulingConfirmed, toProcurement,
             procurementConfirmed, engineeringApprovedDate, engineeringApprovedSignOn, procurementConfirmedDate, procurementConfirmedSignOn,
             schedulingConfirmedDate, schedulingConfirmedSignOn, toEngineeringDate, toEngineeringSignOn, toProcurementDate, toProcurementSignOn,
             toSchedulingDate, toSchedulingSignOn, managerApprovedDate, managerApprovedSignOn, toManagerDate, toManagerSignOn, salesPerson;
      String[] ocDate   = new String[1];
      String[] ocSignOn = new String[1];
      int diff;
    
      if(rs.next())
      {    
        soDate                     = rs.getString(1);
        soType                     = rs.getString(2);
        toEngineering              = rs.getString(3);
        engineeringApproved        = rs.getString(4);
        toManager                  = rs.getString(5);
        managerApproved            = rs.getString(6);
        toProcurement              = rs.getString(7);
        procurementConfirmed       = rs.getString(8);
        toScheduling               = rs.getString(9);
        schedulingConfirmed        = rs.getString(10);
        engineeringApprovedDate    = rs.getString(11);
        engineeringApprovedSignOn  = rs.getString(12);
        procurementConfirmedDate   = rs.getString(13);
        procurementConfirmedSignOn = rs.getString(14);
        schedulingConfirmedDate    = rs.getString(15);
        schedulingConfirmedSignOn  = rs.getString(16);
        toEngineeringDate          = rs.getString(17);
        toEngineeringSignOn        = rs.getString(18);
        toProcurementDate          = rs.getString(19);
        toProcurementSignOn        = rs.getString(20);
        toSchedulingDate           = rs.getString(21);
        toSchedulingSignOn         = rs.getString(22);
        managerApprovedDate        = rs.getString(23);
        managerApprovedSignOn      = rs.getString(24);
        toManagerDate              = rs.getString(25);
        toManagerSignOn            = rs.getString(26);
        salesPerson                = rs.getString(27);
              
        s = soCode;
        
        if(soType.equals("T"))
          s += " - TRADE - salesperson: " + salesPerson + "`";
        else s += " - PROJECT - salesperson: " + salesPerson + "`";

        if(soType.equals("T"))
          ;
        else
        if(! toEngineering.equals("Y"))
          s += "Not sent to engineering`";
        else
        {
          if(! engineeringApproved.equals("Y"))
            s += "Sent to engineering on " + generalUtils.convertFromYYYYMMDD(toEngineeringDate)  + " by " + toEngineeringSignOn  + "`";
          else s += "Approved by engineering on " + generalUtils.convertFromYYYYMMDD(engineeringApprovedDate)  + " by " + engineeringApprovedSignOn  + "`";
        }
      
        if(! toProcurement.equals("Y"))
          s += "Not sent to procurement`";
        else
        {
          if(! procurementConfirmed.equals("Y"))
            s += "Sent to procurement on " + generalUtils.convertFromYYYYMMDD(toProcurementDate)  + " by " + toProcurementSignOn  + "`";
          else s += "Confirmed by procurement on " + generalUtils.convertFromYYYYMMDD(procurementConfirmedDate)  + " by " + procurementConfirmedSignOn  + "`";
        }
      
        if(soType.equals("T"))
          ;
        else        
        if(! toScheduling.equals("Y"))
          s += "Not sent to scheduling`";
        else
        {
          if(! schedulingConfirmed.equals("Y"))
            s += "Sent to scheduling on " + generalUtils.convertFromYYYYMMDD(toSchedulingDate)  + " by " + toSchedulingSignOn  + "`";
          else s += "Confirmed by scheduling on " + generalUtils.convertFromYYYYMMDD(schedulingConfirmedDate)  + " by " + schedulingConfirmedSignOn  + "`";
        }
      
        if(! toManager.equals("Y"))
          s += "Not sent to sales manager`";
        else
        {
          if(! managerApproved.equals("Y"))
            s += "Sent to sales manager on " + generalUtils.convertFromYYYYMMDD(toManagerDate)  + " by " + toManagerSignOn  + "`";
          else s += "Approved by sales manager on " + generalUtils.convertFromYYYYMMDD(managerApprovedDate)  + " by " + managerApprovedSignOn  + "`";
        }
            
        if(orderConfirmation.getOCDetailsGivenSOCode(con, stmt2, rs2, soCode, ocDate, ocSignOn))
        {
          s += "Order Confirmation sent on " + generalUtils.convertFromYYYYMMDD(ocDate[0])  + " by " + ocSignOn[0];
          diff = generalUtils.encodeFromYYYYMMDD(ocDate[0]) - generalUtils.encodeFromYYYYMMDD(soDate);
          if(diff > 1)
            s += ("; " + --diff + " days late");
//        s += "`";
        }
        else s += "Order Confirmation not sent";
       
        if(rs   != null) rs.close();        
        if(stmt != null) stmt.close();        

        return s;
      }
    }
    catch(Exception e)
    {
      System.out.println("1201a: " + e);
      if(rs   != null) rs.close();        
      if(stmt != null) stmt.close();
    }
    
    return "Not Found";
  }

}

