// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Directories etc
// Module: DirectoryUtils.cs
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
using System.Text;
using MySql.Data.MySqlClient;

namespace zarastar
{
    class DirectoryUtils
    {
        readonly GeneralUtils generalUtils = new GeneralUtils();
        DefinitionTables definitionTables = new DefinitionTables();
        AuthenticationUtils authenticationUtils = new AuthenticationUtils();

        public bool CreateConnection(MySqlConnection connection)
        {
            try
            {
                connection.Open();
                return true;
            }
            catch (MySqlException ex)
            {
                switch (ex.Number)
                {
                    case 0:
                        Console.WriteLine("Cannot connect to server");
                        break;

                    case 1045:
                        Console.WriteLine("Invalid username/password");
                        break;
                }
                return false;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // check in this app's current css directory first
        // if not present, looks in the Support Default css directory
        public string GetCssDocumentDirectory(MySqlConnection con, string dnm)
        {
            try
            {
                string[] companyName = new string[1];
                string[] applicationStartDate = new string[1];
                string[] financialYearStartMonth = new string[1];
                string[] financialYearEndMonth = new string[1];
                string[] effectiveStartDate = new string[1];
                string[] companyPhone = new string[1];
                string[] companyFax = new string[1];
                string[] dateStyle = new string[1];
                string[] dateSeparator = new string[1];
                string[] description = new string[1];
                string[] companyEMail = new string[1];
                string[] terseName = new string[1];
                string[] latitude = new string[1];
                string[] longitude = new string[1];
                string[] googleMapsKey = new string[1];
                string[] currentStyle = new string[1];

                definitionTables.GetAppConfig(con, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, dateSeparator,
                                   description, companyEMail, terseName, latitude, longitude, googleMapsKey, currentStyle);

                return currentStyle[0].StartsWith("Zara", StringComparison.CurrentCultureIgnoreCase)
                    ? "/Zara/Support/Css/" + currentStyle[0] + "/"
                    : "/Zara/" + dnm + "/Css/" + currentStyle[0] + "/";
            }
            catch (Exception e)
            {
                Console.WriteLine("authenticationUtils: GetCssDocumentDirectory: " + e);
                return "/Zara/Support/Css/Zara Default/";
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetCurrentStyleForAUser(MySqlConnection con, string unm, string dnm)
        {
            string styleName = "";
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT StyleName FROM cssstyles WHERE UserCode = '" + unm + "'";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    styleName = Convert.ToString(dataReader["StyleName"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("authenticationUtils: style: " + e);

                if (! TableExists(con, "ofsa", "cssstyles", dnm))
                {
                    AdminDataBaseFixedFilesCreate adminDataBaseFixedFilesCreate = new AdminDataBaseFixedFilesCreate();
                    adminDataBaseFixedFilesCreate.ProcessATable(con, false, 'C', "cssstyles", dnm, null, null, null);
                }
            }

            return styleName;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool TableExists(MySqlConnection con, string dbName, string tableName, string dnm)
        {
            bool res = false;
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT COUNT(*) As count FROM INFORMATION_SCHEMA.TABLES where table_schema='" + dnm + "_" + dbName + "' AND table_name = '" + tableName + "'";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                int count;

                if (dataReader.Read())
                {
                    count = Convert.ToInt16(dataReader["count"]);
                    res |= count > 0;
                }
                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("TableExists: " + e); }

            return res;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // checks in user's directory for any local style defn
        // if not present, checks in app config table for style defn
        // if not present, uses Support Default sytle (which must be present, otherwise no styling can (will) be applied)
        public string GetCssGeneralDirectory(MySqlConnection con, string unm, string dnm)
        {
            string s = GetCurrentStyleForAUser(con, unm, dnm);

            if (s.Length > 0)
            {
                if (s.StartsWith("Zara", StringComparison.CurrentCultureIgnoreCase))
                    return "/Zara/Support/Css/" + s + "/";

                if (StyleExists(s, dnm)) // in case style has been deleted after user selected it
                    return "/Zara/" + dnm + "/Css/" + generalUtils.Capitalize(s) + "/";
            }

            try
            {
                string[] companyName = new string[1];
                string[] applicationStartDate = new string[1];
                string[] financialYearStartMonth = new string[1];
                string[] financialYearEndMonth = new string[1];
                string[] effectiveStartDate = new string[1];
                string[] companyPhone = new string[1];
                string[] companyFax = new string[1];
                string[] dateStyle = new string[1];
                string[] dateSeparator = new string[1];
                string[] description = new string[1];
                string[] companyEMail = new string[1];
                string[] terseName = new string[1];
                string[] latitude = new string[1];
                string[] longitude = new string[1];
                string[] googleMapsKey = new string[1];
                string[] currentStyle = new string[1];

                definitionTables.GetAppConfig(con, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle, 
                                              dateSeparator, description, companyEMail, terseName, latitude, longitude, googleMapsKey, currentStyle);

                return currentStyle[0].StartsWith("Zara", StringComparison.CurrentCultureIgnoreCase)
                    ? "/Zara/Support/Css/" + currentStyle[0] + "/"
                    : "/Zara/" + dnm + "/Css/" + currentStyle[0] + "/";
            }
            catch (Exception e)
            {
                Console.WriteLine("GetCssGeneralDirectory: " + e);

            return "/Zara/Support/Css/Default/";
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetCatalogCssDirectory(string mfr)
        {
            return "http://www.xxx.com/Zara/Catalogs/Css/Default/" + mfr + ".css";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool StyleExists(string reqdStyle, string dnm)
        {
            string dir = "/Zara/" + dnm + "/Css/";

            string[] files = System.IO.Directory.GetFiles(dir);
            string fileName;

            foreach (string s in files)
            {
                fileName = System.IO.Path.GetFileName(s);

                if (generalUtils.IsDirectory(dir + fileName)) // just-in-case
                {
                    if (reqdStyle.Equals(fileName))
                        return true;
                }
            }

            return false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetEditorDirectory()
        {
            return "/Zara/Support/Scripts/Editor/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetGoogleScriptsDirectory()
        {
            return "/Zara/Support/Scripts/Google/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetScriptsDirectory()
        {
            return "/Zara/Support/Scripts/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetFlashDirectory(string dnm)
        {
            return "/Zara/" + dnm + "/Flash/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetUserDir(string dnm)
        {
            return "/Zara/" + dnm + "/Users"; // no trailing slash
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetSessionsDir(string dnm)
        {
            return "/Zara/" + dnm + "/Sessions/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetFaxTmpDir()
        {
            return "/Zara/Fax/Tmp/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetReferenceLibraryDir(string dnm)
        {
            return "/Zara/" + dnm + "/Reference/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetCatalogImagesDir(string dnm, string mfr)
        {
            return "/Zara/" + dnm + "/Catalogs/" + mfr + "/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetImagesDir(string dnm)
        {
            return "/Zara/" + dnm + "/Images/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetTextsDir(string dnm)
        {
            return "/Zara/" + dnm + "/Texts/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetSignaturesDir(string dnm)
        {
            return "/Zara/" + dnm + "/Signatures/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetUserDir(char param, string dnm, string unm)
        {
            string dir = "";

            switch (param)
            {
                case 'U': dir = "/Zara/" + dnm + "/Users/" + unm + "/"; break;
                case 'R': dir = "/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/"; break;
                case 'E': dir = "/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Export/"; break;
                case 'W': dir = "/Zara/" + dnm + "/Users/" + unm + "/Working/"; break;
                case 'L': dir = "/Zara/" + dnm + "/Users/" + unm + "/Library/"; break;
                case 'M': dir = "/Zara/" + dnm + "/Users/" + unm + "/Mail/"; break;
            }

            if (! generalUtils.FileExists(dir))
                generalUtils.CreateDir(dir, true);

            return dir;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetSupportDirs(char param)
        {
            switch (param)
            {
                case 'H': return "/Zara/Support/Html/";
                case 'I': return "/Zara/Support/Images/";
                case 'S': return "/Zara/Support/Scripts/";
                case 'D': return "/Zara/Support/Defns/";
            }
            return "";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetExportDir(string dnm)
        {
            return "/Zara/" + dnm + "/Export/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetLocalOverrideDir(string dnm)
        {
            return "/Zara/" + dnm + "/Defns/";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void SetContentHeaders(HttpWebResponse res)
        {
            /* Http handling TODO
            res.setHeader("Header", "HTTP/1.0 200 OK");
            res.setStatus(200);
            res.setContentType("text/html");
            res.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");              // Set to expire far in the past
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // Set standard HTTP/1.1 no-cache headers
            res.addHeader("Cache-Control", "post-check=0, pre-check=0");           // Set IE extended HTTP/1.1 no-cache (use addHeader)
            res.setHeader("Pragma", "no-cache");                                   // Set standard HTTP/1.0 no-cache header
            */
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void SetContentHeaders2(HttpWebResponse res)
        {
            /* Http handling TODO
            res.setHeader("Header", "HTTP/1.0 200 OK");
            res.setStatus(200);
            res.setContentType("text/html");
            res.setHeader("Expires", "Sat, 6 May 2995 12:00:00 GMT");              // Set to expire far in the future
            */
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool CreateTmpTable(bool removeExisting, MySqlConnection con, string createstring, string indexstring, string tableName)
        {
            try
            {
                if (removeExisting)
                {
                    string query = "DROP TABLE " + tableName;

                    MySqlCommand cmd = new MySqlCommand(query, con);

                    cmd.ExecuteNonQuery();
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: createTmpTable (1): " + e);
            }

            try
            {
                string query = "CREATE TABLE " + tableName + " (" + createstring + ")";

                MySqlCommand cmd = new MySqlCommand(query, con);
            
                cmd.ExecuteNonQuery();
            }
            catch (Exception e) // throws error if already created
            {
                Console.WriteLine("directoryUtils: createTmpTable (2): " + e);
                return true;
            }

            if (indexstring.Length > 0)
            {
                try
                {
                    string query = "CREATE INDEX " + indexstring;
                    MySqlCommand cmd = new MySqlCommand(query, con);

                    cmd.ExecuteNonQuery();
                }
                catch (Exception e)
                {
                    Console.WriteLine("directoryUtils: createTmpTable (3): " + e);
                    return true;
                }
            }

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool RemoveTmpTable(MySqlConnection con, string tableName)
        {
            try
            {
                string query = "DROP TABLE " + tableName;
                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: RemoveTmpTable(): " + e);
            }

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool ClearTmpTable(MySqlConnection con, string tableName)
        {
            try
            {
                string query = "TRUNCATE TABLE " + tableName;
                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: ClearTmpTable(): " + e);
            }

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool UsesFlash(MySqlConnection con)
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

            authenticationUtils.GetStyling(con, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

            return usesFlash[0].Equals("Y") ? true : false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public int GetLayoutType(FileStream fh)
        {
            fh.Seek(0L, SeekOrigin.Begin);
            StreamReader sr = new StreamReader(fh);

            string s = sr.ReadLine(); // e.g., /* 1 */

            int x = 2; // /*
            while (x < s.Length && s[x] == ' ') // just-in-case
                ++x;

            string t = "";
            while (x < s.Length && s[x] != ' ') // just-in-case
                t += s[x++];

            int i = 1;
            try
            {
                i = generalUtils.IntFromStr(t);
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: Failure to get layout type: " + e);
            }
            sr.Close();

            return i;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetMySQLUserName()
        {
            string s;

            try
            {
                s = generalUtils.GetFromDefnFile("SQLUSERNAME", "mysql.dfn", "/Zara/Support/Defns/", "");
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: Failure to get SQL UserName: " + e);
                return "";
            }

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetMySQLPassWord()
        {
            string s;

            try
            {
                s = generalUtils.GetFromDefnFile("SQLPASSWORD", "mysql.dfn", "/Zara/Support/Defns/", "");
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: Failure to get SQL PassWord: " + e);
                return "";
            }

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetAppConfigInfo(MySqlConnection con, char which, string dnm)
        {
            string[] companyName = new string[1];
            string[] applicationStartDate = new string[1];
            string[] financialYearStartMonth = new string[1];
            string[] financialYearEndMonth = new string[1];
            string[] effectiveStartDate = new string[1];
            string[] companyPhone = new string[1];
            string[] companyFax = new string[1];
            string[] dateStyle = new string[1];
            string[] dateSeparator = new string[1];
            string[] description = new string[1];
            string[] companyEMail = new string[1];
            string[] terseName = new string[1];
            string[] latitude = new string[1];
            string[] longitude = new string[1];
            string[] googleMapsKey = new string[1];
            string[] currentStyle = new string[1];

            definitionTables.GetAppConfig(con, dnm, companyName, applicationStartDate, financialYearStartMonth, financialYearEndMonth, effectiveStartDate, companyPhone, companyFax, dateStyle,
                                          dateSeparator, description, companyEMail, terseName, latitude, longitude, googleMapsKey, currentStyle);


            switch (which)
            {
                case 'N': return companyName[0];
                case 'L': return ""; ;
            }

            return "";
        }


        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string BuildHelp(int service)
        {
            return BuildHelp("" + service);
        }
        public string BuildHelp(string service)
        {
            return "<table cellspacing='2' cellpadding='2' border='0'><tr><td nowrap='nowrap'><a href=\"javascript:help('" + service + "')\">" + service + "</a></td></tr></table>";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void UpdateState(string fileName, string str)
        {
            try
            {
                FileStream fs = generalUtils.FileOpen(fileName);
                str += "\n";
                fs.Write(Encoding.ASCII.GetBytes(str), 0, str.Length);
                fs.Close();
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: Failure to get UpdateState: " + e);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool UsePostgres()
        {
            try
            {
                string s = generalUtils.GetFromDefnFile("USEPOSTGRES", "mysql.dfn", "/Zara/Support/Defns/", "");

                return s.Equals("yes") ? true : false;
            }
            catch (Exception e)
            {
                Console.WriteLine("directoryUtils: Failure to get UsePostgres: " + e);
            }

            return false;
        }
    }
}