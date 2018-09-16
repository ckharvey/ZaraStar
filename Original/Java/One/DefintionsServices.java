// =======================================================================================================================================================================================================
// System: ZaraStar AdminEngine - Definition Services access page
// Module: DefintionsServices.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;
import java.sql.*;

public class DefintionsServices
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    dashboardUtils.drawTitle(out, "Customisation", "115", unm, sid, uty, men, den, dnm, bnm, bytesOut);
  
    scoutln(out, bytesOut, "<table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>Application-Related</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminAppconfigEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Configuration</td><td nowrap width=90% nowrap>"
                            + directoryUtils.buildHelp(7062) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminPeoplePositioning?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Person Positioning</td><td nowrap width=90% nowrap>"
                            + directoryUtils.buildHelp(7056) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/WikiStyling?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Styling</a>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>Documents-Related</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminCompanyTypeDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Company Types</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7058) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminCountryDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Countries</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7057) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminDocumentCodesDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Document Codes</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7031) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminIndustryTypeDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Industry Types</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7059) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminLikelihoodRatingDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Likelihood Ratings</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7063) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminDocumentSettings?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Document Settings</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7019) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminDocumentOptionsEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Document Options</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7070) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminQuoteStatusDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Quotation Status</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7064) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminQuotationReasonsEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Quotation Reasons</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7065) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminSalesPeople?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Sales People</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7001) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminDeliveryDriverDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Delivery Drivers</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7012) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminStorePersonDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Storemen</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7013) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminStoreDefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                         + "\">Change</a> Stores</td><td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7060) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>Mail-Related</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/SignaturesListDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Signatures</td><td nowrap width=90% nowrap>"
                            + directoryUtils.buildHelp(6301) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>Forum-Related</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/RATCategories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Forum Categories</td><td nowrap width=90% nowrap>"
                            + directoryUtils.buildHelp(5907) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/RATProblemTypes?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Change</a> Issue Problem Types</td><td nowrap width=90% nowrap>"
                            + directoryUtils.buildHelp(5917) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td nowrap><h1>System Services</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminSetSystemStatus?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Set</a> System Status</td><td nowrap width=90% nowrap>"
                          + directoryUtils.buildHelp(7029) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p>");
    scoutln(out, bytesOut, "<a href=\"/central/servlet/AdminTimeoutsDefinitionEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Set</a> TimeOuts</td><td nowrap width=90% nowrap>"
                          + directoryUtils.buildHelp(7025) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminCustomerPOCodesAnalyseInput?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Analyze</a> Customer PO Codes</td>");
    scoutln(out, bytesOut, "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7077) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/AdminCustomerPOCodesClean?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Remove</a> Customer PO Codes</td>");
    scoutln(out, bytesOut, "<td nowrap width=90% nowrap>" + directoryUtils.buildHelp(7076) + "</td></tr>");

    if(unm.equals("Sysadmin"))
    {
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/_19001?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Replace</a> Industry Types</td>");
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/_19105?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Update</a> WrittenOff</td>");
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/_19106?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Update</a> Purchase Prices</td>");
      scoutln(out, bytesOut, "<tr><td></td><td nowrap><p><a href=\"/central/servlet/_19107?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Generate</a> RR SpreadSheet</td>");
      scoutln(out, bytesOut, "</tr>");
    }

    scoutln(out, bytesOut, "</table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
