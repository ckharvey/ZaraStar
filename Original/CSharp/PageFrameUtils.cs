// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Page frame
// Module: PageFrameUtils.cs
// Author: C.K.Harvey
// Copyright (c) 2006-18 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

using System;
using System.IO;
using System.Net;
using MySql.Data.MySqlClient;

namespace zarastar
{
    class PageFrameUtils
    {
        GeneralUtils        generalUtils        = new GeneralUtils();
        AuthenticationUtils authenticationUtils = new AuthenticationUtils();
        DirectoryUtils      directoryUtils      = new DirectoryUtils();
        DashboardUtils      dashboardUtils      = new DashboardUtils();

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void OutputPageFrame(MySqlConnection con, StreamWriter swOut, HttpWebRequest req, string service, string bodyStr, string callingServlet, string unm, string sid, string uty, string men, string mainDNM,
                                    string dnm, string bnm, string localDefnsDir, string defnsDir, int[] hmenuCount, int[] bytesOut)
        {
           OutputPageFrame(con, swOut, req, service, bodyStr, false, callingServlet, unm, sid, uty, men, mainDNM, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
        }
        public void OutputPageFrame(MySqlConnection con, StreamWriter swOut, HttpWebRequest req, string service, string bodyStr, bool plain, 
                                    string callingServlet, string unm, string sid, string uty, string men, string mainDNM, string dnm, string bnm, string localDefnsDir, string defnsDir, int[] hmenuCount, int[] bytesOut)
        {
            OutputPageFrame(con, swOut, req, service, bodyStr, plain, callingServlet, unm, sid, uty, men, mainDNM, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void OutputPageFrame(MySqlConnection con, StreamWriter swOut, HttpWebRequest req, string service, string bodyStr, bool plain, string callingServlet, string unm, string sid, string uty, string men, string den,
                                    string dnm, string bnm, string otherSetup, string localDefnsDir, string defnsDir, int[] hmenuCount, int[] bytesOut)

        {
            string subMenuText = dashboardUtils.BuildSubMenuText(con, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

            if (plain)
                Scoutln(swOut, bytesOut, authenticationUtils.BuildPlainScreen(con, unm, sid, uty, men, den, dnm, bnm, callingServlet, bodyStr, otherSetup));
            else Scoutln(swOut, bytesOut, authenticationUtils.BuildScreen(con, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DrawTitle(StreamWriter swOut, bool friendlyWanted, bool plain, string callingServlet, string account, string dateFrom, string dateTo, string p5, string p6, string title, string service, string unm,
                                string sid, string uty, string men, string den, string dnm, string bnm, int[] hmenuCount, int[] bytesOut)
        {
            string s = Scoutln(swOut, bytesOut, "<table id='title' width='100%'>");

            if (service.Length > 0)
                s += Scoutln(swOut, bytesOut, "<tr><td colspan='2' nowrap='nowrap'><p>" + title + directoryUtils.BuildHelp(service) + "</p></td></tr></table>");
            else s += Scoutln(swOut, bytesOut, "<tr><td colspan='2' nowrap='nowrap'><p>" + title + "</p></td></tr></table>");

            s += Scoutln(swOut, bytesOut, "<table id='submenu' width='100%'><tr><td>"
                    + BuildSubMenuText(friendlyWanted, plain, callingServlet, account, dateFrom, dateTo, p5, p6, unm, sid, uty, men, den, dnm, bnm, hmenuCount) + "</td></tr></table>");

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void DrawTitleW(StreamWriter swOut, bool friendlyWanted, bool plain, string callingServlet, string account, string dateFrom, string dateTo, string p5, string p6, string title, string service, string unm, string sid, string uty,
                               string men, string den, string dnm, string bnm, int[] bytesOut)
        {
            Scoutln(swOut, bytesOut, "<table id='title' width=100%>");
            Scoutln(swOut, bytesOut, "<tr><td colspan=2 nowrap><span id='x'>" + title);
            if (service.Length > 0)
                Scoutln(swOut, bytesOut, directoryUtils.BuildHelp(service));
            Scoutln(swOut, bytesOut, "</span></td></tr></table>");

            int[] hmenuCount = new int[1]; hmenuCount[0] = 1;

            Scoutln(swOut, bytesOut, "<table id='submenuX' width=100%><tr><td>" + BuildSubMenuText(friendlyWanted, plain, callingServlet, account, dateFrom, dateTo, p5, p6, unm, sid, uty, men, den, dnm, bnm, hmenuCount) + "</td></tr></table>");

        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string BuildSubMenuText(bool friendlyWanted, bool plain, string callingServlet, string account, string dateFrom, string dateTo, string p5, string p6, string unm, string sid, string uty,
                                        string men, string den, string dnm, string bnm, int[] hmenuCount)
        {
            string s = "<div id='submenu'>";

            hmenuCount[0] = 1;

            // does friendly printing for GL
            if (friendlyWanted && !plain)
            {
                s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
                s += "<a href=\"/central/servlet/" + callingServlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                   + "&bnm=" + bnm + "&p1=" + account + "&p2=" + generalUtils.ConvertFromYYYYMMDD(dateFrom) + "&p3="
                  + generalUtils.ConvertFromYYYYMMDD(dateTo) + "&p5=" + p5 + "&p6=" + p6 + "&p4=P\">Friendly</a></dt></dl>";
            }

            s += "</div>";

            --hmenuCount[0];

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void OutputPageFrame(MySqlConnection con, byte[] b, HttpWebRequest req, string service, string bodyStr, string callingServlet, string unm, string sid, string uty, string men, string den, string dnm,
                                    string bnm, string localDefnsDir, string defnsDir, int[] bytesOut)
        {
            int[] hmenuCount = new int[1];

            string subMenuText = dashboardUtils.BuildSubMenuText(con, req, service, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

            Scoutln(b, bytesOut, authenticationUtils.BuildScreen(con, true, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount[0], bodyStr, "", localDefnsDir, defnsDir));

            Scoutln(b, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
            Scoutln(b, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
        }

        public string OutputPageFrame(bool friendlyWanted, bool plain, string account, string dateFrom, string dateTo, string p5, string p6,
                                      string callingServlet, string unm, string sid, string uty, string men, string den, string dnm, string bnm)
        {
            int[] hmenuCount = new int[1];

            string subMenuText = BuildSubMenuText2(friendlyWanted, plain, callingServlet, account, dateFrom, dateTo, p5, p6, unm, sid, uty, men, den, dnm, bnm, hmenuCount);

            return "";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string BuildSubMenuText2(bool friendlyWanted, bool plain, string callingServlet, string account, string dateFrom, string dateTo, string p5, string p6, string unm, string sid, string uty, string men, 
                                         string den, string dnm, string bnm, int[] hmenuCount)
        {
            string s = "<div id='submenu'>";

            hmenuCount[0] = 1;

            // does friendly printing for GL
            if (friendlyWanted && !plain)
            {
                s += "<dl><dt onmouseover=\"setup('hmenu" + hmenuCount[0]++ + "');\">";
                s += "<a href=\"/central/servlet/" + callingServlet + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm
                  + "&bnm=" + bnm + "&p1=" + account + "&p2=" + dateFrom + "&p3=" + dateTo + "&p5=" + p5 + "&p6=" + p6 + "&p4=P\">Friendly</a></dt></dl>";
            }

            s += "</div>";

            --hmenuCount[0];

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DrawTitle(string title, string service)
        {
            string s = "<table id='title' width=100%>";

            if (service.Length > 0)
                s += "<tr><td colspan=2 nowrap><p>" + title + directoryUtils.BuildHelp(service) + "</td></tr></table>";
            else s += "<tr><td colspan=2 nowrap><p>" + title + "</td></tr></table>";

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void Scoutln(byte[] b, int[] bytesOut, string str)
        {
            str += Convert.ToString(b);
            bytesOut[0] += (str.Length + 2);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string Scoutln(StreamWriter swOut, int[] bytesOut, string str)
        {
            if (swOut != null) swOut.WriteLine(str);
            bytesOut[0] += (str.Length + 2);
            return str + "\n";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void PostToIW(string iwRtnInfo, string str)
        {
        }
    }

}