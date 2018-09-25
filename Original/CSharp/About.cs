// ==================================================================================================================================================================================================================================================
// System: ZaraStar: About Zara 6
// Module: AboutZara.cs
// Author: C.K.Harvey
// Copyright (c) 2002-18 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License aint64with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

using System;
using System.IO;
using System.Net;
using MySql.Data.MySqlClient;

namespace zarastar
{
    class AboutZara
    {
        MessagePage         messagePage         = new MessagePage();
        ServerUtils         serverUtils         = new ServerUtils();
        PageFrameUtils      pageFrameUtils      = new PageFrameUtils();
        AuthenticationUtils authenticationUtils = new AuthenticationUtils(); 
        DirectoryUtils      directoryUtils      = new DirectoryUtils();
        private readonly Wiki wiki = new Wiki();

        private MySqlConnection connection;

        public void DoPost(HttpWebRequest req, HttpWebResponse res)
        {
            StreamWriter twOut = null;
            int[] bytesOut = new int[1]; bytesOut[0] = 0;

            string unm = "", sid = "", uty = "", men = "", den = "", dnm = "", bnm = "";

            try
            {
                directoryUtils.SetContentHeaders(res);

                twOut = new StreamWriter(res.GetResponseStream());

                // Http handling TODO
                //unm = req.getParameter("unm");
                //sid = req.getParameter("sid");
                //uty = req.getParameter("uty");
                //men = req.getParameter("men");
                //den = req.getParameter("den");
                //dnm = req.getParameter("dnm");
                //bnm = req.getParameter("bnm");

                DoIt(twOut, req, unm, sid, uty, men, den, dnm, bnm, bytesOut);
            }
            catch (Exception e)
            {
                string url = req.RequestUri.ToString();
             
                int x = 0;
                if (url[x] == 'h' || url[x] == 'H')
                    x += 7;
                string urlBit = "";
                while (url[x] != '\0' && url[x] != ',' && url[x] != '/')
                    urlBit += url[x++];

                messagePage.errorPage(twOut, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AboutZara", bytesOut);
                serverUtils.ETotalBytes(req, unm, dnm, 188, bytesOut[0], 0, 0, "ERR:");
                if (twOut != null) twOut.Flush();
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void DoIt(StreamWriter twOut, HttpWebRequest req, string unm, string sid, string uty, string men, string den, string dnm, string bnm, int[] bytesOut)
        {
            long startTime = Convert.ToInt64(DateTime.Now);

            string imagesDir     = directoryUtils.GetSupportDirs('I');
            string defnsDir      = directoryUtils.GetSupportDirs('D');
            string localDefnsDir = directoryUtils.GetLocalOverrideDir(dnm);

            this.connection = new MySqlConnection("Server = myServerAddress; Port = 3306; Database = myDataBase; Uid = myUsername; Pwd = myPassword;");

            directoryUtils.CreateConnection(connection);

            if (!serverUtils.CheckSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
            {
                messagePage.msgScreen(false, twOut, req, 12, unm, sid, uty, men, den, dnm, bnm, "AboutZara", imagesDir, localDefnsDir, defnsDir, bytesOut);
            
                try
                {
                    connection.Close();
                }
                catch (MySqlException) { }

                if (twOut != null) twOut.Flush();
                return;
            }

            Set(twOut, req, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

            serverUtils.TotalBytes(this.connection, req, unm, dnm, 188, bytesOut[0], 0, (Convert.ToInt64(DateTime.Now) - startTime), "");

            try
            {
                connection.Close();
            }
            catch (MySqlException) { }

            if (twOut != null) twOut.Flush();
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void Set(StreamWriter twOut, HttpWebRequest req, string unm, string sid, string uty, string men, string den, string dnm, string bnm, string imagesDir,
                         string localDefnsDir, string defnsDir, int[] bytesOut)
        {
            Scoutln(twOut, bytesOut, "<html><head><title>About ZaraStar 6</title>");

            Scoutln(twOut, bytesOut, "<script language=\"JavaScript\">");

            Scoutln(twOut, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.GetCssGeneralDirectory(this.connection, unm, dnm) + "general.css\"></head>");

            int[] hmenuCount = new int[1];

            pageFrameUtils.OutputPageFrame(this.connection, twOut, req, "188", "", "AboutZara", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

            Scoutln(twOut, bytesOut, "<form>");

            pageFrameUtils.DrawTitle(twOut, false, false, "", "", "", "", "", "", "About", "188", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);

            Scoutln(twOut, bytesOut, "<table id=\"page\" width=100% border=0>");

            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td><p><b>Relating to this site content:</b></td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");

            Scoutln(twOut, bytesOut, "<tr><td><p>" + GetFooterText(dnm) + "</td></tr>");

            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td><hr></td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td><p><b>Relating to the underlying software application:</b></td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");

            Scoutln(twOut, bytesOut, "<tr><td><p>This software contains application services from the ZaraStar<sup>6</sup> software suite.</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td valign=top nowrap><p><a href=\"http://zarastar.xyz\"><img src=\"" + imagesDir + "zaralogosmall.png\" border=0/></a></td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td><p>Zara software is copyright &copy; 1995-2018 <a href=\"http://chrisharvey.tech\">Christopher Harvey</a>.</td></tr>");

            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");
            Scoutln(twOut, bytesOut, "<tr><td>&nbsp;</td></tr>");

            Scoutln(twOut, bytesOut, "</table></form>");
            Scoutln(twOut, bytesOut, authenticationUtils.BuildFooter(this.connection, unm, bnm, localDefnsDir));
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void Scoutln(StreamWriter sw, int[] bytesOut, string str)
        {
            sw.WriteLine(str);
            bytesOut[0] += (str.Length + 2);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string GetFooterText(string dnm)
        {
            string[] headerLogo = new string[1];
            string[] headerLogoRepeat = new string[1];
            string[] usesFlash = new string[1];
            string[] footerText = new string[1];
            string[] pageHeaderImage1 = new string[1];
            string[] pageHeaderImage2 = new string[1];
            string[] pageHeaderImage3 = new string[1];
            string[] pageHeaderImage4 = new string[1];
            string[] pageHeaderImage5 = new string[1];
            string[] watermark = new string[1];

            wiki.GetStyling(dnm, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

            return footerText[0];
        }

    }
}
