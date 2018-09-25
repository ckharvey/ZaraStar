// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Authentication, logging, misc
// Module: AuthenticationUtils.cs
// Author: C.K.Harvey
// Copyright (c) 1998-2018 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

using System;
using System.IO;
using MySql.Data.MySqlClient;

namespace zarastar
{
    class AuthenticationUtils
    {
        GeneralUtils generalUtils = new GeneralUtils();
        ServerUtils serverUtils = new ServerUtils();
        readonly DirectoryUtils directoryUtils = new DirectoryUtils();
         DefinitionTables definitionTables = new DefinitionTables();
        Profile profile = new Profile();
        AdminControlUtils adminControlUtils = new AdminControlUtils();

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // userType[0] set to I (internal), R (registered), or A (anonymous)
        public bool ValidateSignOn(MySqlConnection con, string unm, string pwd, string dnm, char[] userType, string[] sid, string[] userName, string[] actualUNM)
        {
            string userDir = directoryUtils.GetSessionsDir(dnm) + "/";
            string sessionsDir = directoryUtils.GetSessionsDir(dnm);
            string defnsDir = directoryUtils.GetSupportDirs('D');
            string localDefnsDir = directoryUtils.GetLocalOverrideDir(dnm);

            unm = generalUtils.StripLeadingAndTrailingSpaces(unm);

            unm = generalUtils.Capitalize(unm);
            serverUtils.RemoveSID(unm, sessionsDir);

            // not anon
            unm = generalUtils.Capitalize(unm);

            bool res;

            res = NewValidateSignOn(con, true, unm, pwd, userName);

            if (!res) // not validated as an internal user
            {
                if (!ValidateSignOnExtUser(unm, pwd, dnm, defnsDir)) // not validated against an extuser rec
                {
                    return false;
                }
                // else // validated as an external user
                // check registered user dir to see if all registered users are allowed access
                if (NewValidateSignOn(con, false, "Registered", "", userName)) // validated as an internal user
                    return false;

                userType[0] = 'R';
                unm = unm + "_";
                actualUNM[0] = unm;
            }
            else
            {
                userType[0] = 'I';
                actualUNM[0] = unm;
            }

            if (generalUtils.CreateDir(userDir + unm)) // does not (did not) exist!
            {
                if (!generalUtils.CreateDir(userDir + unm + "/Working"))
                    return false;
            }

            sid[0] = serverUtils.NewSessionID(unm, "I", dnm, sessionsDir, localDefnsDir, defnsDir);

            serverUtils.RemoveExtinctSIDs(sessionsDir, localDefnsDir, defnsDir);

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool NewValidateSignOn(MySqlConnection con, bool needToCheckPassWord, string unm, string pwd, string[] userName)
        {
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT UserName, PassWord, Status FROM profiles WHERE UserCode = '" + generalUtils.SanitiseForSQL(unm) + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                string passWord = "", status = "";

                if (dataReader.Read())
                {
                    userName[0] = Convert.ToString(dataReader["UserName"]);
                    passWord = Convert.ToString(dataReader["PassWord"]);
                    status = Convert.ToString(dataReader["Status"]);
                }

                dataReader.Close();

                return status.Equals("L") && !needToCheckPassWord || pwd.Equals(passWord) ? true : false;
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("NewValidateSignOn: " + e);
            }

            return false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool ValidateSignOnExtUser(string unm, string pwd, string dnm, string defnsDir)
        {
            int i;
            if ((i = unm.IndexOf("_", StringComparison.CurrentCultureIgnoreCase)) != -1) // registered
                unm = unm.Substring(0, i);

            return profile.validateExternalAccess(unm, pwd, dnm, "", defnsDir);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetUserNameGivenUserCode(MySqlConnection con, string unm)
        {
            string userName = "";

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT UserName FROM profiles WHERE UserCode = '" + unm + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    userName = Convert.ToString(dataReader["UserName"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: " + e);
            }

            return userName;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void Scoutln(StreamWriter sw, int[] bytesOut, string str)
        {
            sw.WriteLine(str);
            bytesOut[0] += (str.Length + 2);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void Scoutln(byte[] b, int[] bytesOut, string str)
        {
            str += Convert.ToString(b);
            bytesOut[0] += (str.Length + 2);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool UnprocessedInboxTransactions(MySqlConnection con)
        {
            int rowCount = 0;
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT COUNT(*) AS rowcount FROM inbox WHERE Processed != 'Y' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    rowCount = Convert.ToInt16(dataReader["rowcount"]);
                }
                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: " + e);
            }

            return rowCount != 0 ? true : false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetRelease(MySqlConnection con)
        {
            string release = "8"; // just-in-case

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT ServerRelease FROM ServerAdmin LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();
                if (dataReader.Read())
                {
                    release = Convert.ToString(dataReader["ServerRelease"]);
                }
                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: " + e);
            }

            return release;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void GetStyling(MySqlConnection con, string[] headerLogo, string[] headerLogoRepeat, string[] usesFlash, string[] footerText, string[] pageHeaderImage1, string[] pageHeaderImage2,
                               string[] pageHeaderImage3, string[] pageHeaderImage4, string[] pageHeaderImage5, string[] watermark)
        {
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT * FROM styling LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    headerLogo[0] = Convert.ToString(dataReader["headerLogo"]);
                    headerLogoRepeat[0] = Convert.ToString(dataReader["headerLogoRepeat"]);
                    usesFlash[0] = Convert.ToString(dataReader["usesFlash"]);
                    footerText[0] = Convert.ToString(dataReader["footerText"]);
                    pageHeaderImage1[0] = Convert.ToString(dataReader["pageHeaderImage1"]);
                    pageHeaderImage2[0] = Convert.ToString(dataReader["pageHeaderImage2"]);
                    pageHeaderImage3[0] = Convert.ToString(dataReader["pageHeaderImage3"]);
                    pageHeaderImage4[0] = Convert.ToString(dataReader["pageHeaderImage4"]);
                    pageHeaderImage5[0] = Convert.ToString(dataReader["pageHeaderImage5"]);
                    watermark[0] = Convert.ToString(dataReader["watermark"]);
                }
                else
                {
                    headerLogo[0] = "";
                    headerLogoRepeat[0] = "";
                    usesFlash[0] = "N";
                    footerText[0] = "Copyright (c)";
                    pageHeaderImage1[0] = "";
                    pageHeaderImage2[0] = "";
                    pageHeaderImage3[0] = "";
                    pageHeaderImage4[0] = "";
                    pageHeaderImage5[0] = "";
                    watermark[0] = "";
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                headerLogo[0] = "";
                headerLogoRepeat[0] = "";
                usesFlash[0] = "N";
                footerText[0] = "Copyright (c)";
                pageHeaderImage1[0] = "";
                pageHeaderImage2[0] = "";
                pageHeaderImage3[0] = "";
                pageHeaderImage4[0] = "";
                pageHeaderImage5[0] = "";
                watermark[0] = "";

                Console.WriteLine("AuthenticationUtils:GetStyling: " + e);
            }
        }


        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string BuildMainMenu(MySqlConnection con, int layoutType, bool writeSetupNow, string unm, string sid, string uty, string men, string den, string dnm, string bnm, string callingServlet,
                                     int hmenuCount, string otherSetup, string localDefnsDir, string defnsDir)
        {
            string s = "<div id='mainmenu'>\n";

            if (callingServlet.Equals("MainPageUtilsc")) // on signon screen
            {
                s += "</div>";
                return s;
            }

            int vmenuCount = 1;

            if (uty.Equals("A")) // Casual
            {
                if (layoutType < 3) s += "<h1>Cloud</h1><br/>";

                s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&bnm=" + bnm + "\">Home</a></dt></dl>\n";

                if (adminControlUtils.NotDisabled(con, 806))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserProfile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Register</a></dt></dl>\n";
                }

                if (adminControlUtils.NotDisabled(con, 809))
                {
                    if (HavePersonnel(con))
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">People</a></dt></dl>\n";
                    }
                }

                if (CountBloggers(con) > 0)
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/BlogsBlogRollList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Blogs</a></dt></dl>\n";
                }

                if (adminControlUtils.NotDisabled(con, 824))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/RATIssuesMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Forum</a></dt></dl>\n";
                }

                if (adminControlUtils.NotDisabled(con, 802))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/MailZaraAnonymousUserProducts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Products</a></dt></dl>\n";
                }

                if (adminControlUtils.NotDisabled(con, 808))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Cart</a></dt></dl>\n";
                }

                if (generalUtils.GetFromDefnFile("LATITUDE", "map.dfn", localDefnsDir, defnsDir).Length > 0)
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/GoogleMapServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Location</a></dt></dl>\n";
                }

                if (VerifyAccess(con, 188, unm, uty, dnm))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/AboutZara?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">About</a></dt></dl>\n";
                }
            }

            if (uty.Equals("R")) // Registered Users
            {
                if (layoutType < 3) s += "<h1>Cloud</h1><br/>";

                s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&bnm=" + bnm + "\">Home</a></dt></dl>\n";

                string[] name = new string[1]; name[0] = "";
                string[] companyName = new string[1]; companyName[0] = "";
                string[] accessRights = new string[1]; accessRights[0] = "";
                int i = unm.IndexOf("_", StringComparison.CurrentCultureIgnoreCase);

                profile.getExternalAccessNameCompanyAndRights(con, unm.Substring(0, i), name, companyName, accessRights);
                string[] customerCode = new string[1];
                if (!profile.getExternalAccessNameCustomerCode(con, unm.Substring(0, i), dnm, localDefnsDir, defnsDir, customerCode))
                    customerCode[0] = "";

                if (adminControlUtils.NotDisabled(con, 909))
                {
                    if (HavePersonnel(con))
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">People</a></dt></dl>\n";
                    }
                }

                if (CountBloggers(con) > 0)
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/BlogsBlogRollList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Blogs</a></dt></dl>\n";
                }

                if (adminControlUtils.NotDisabled(con, 924))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/RATIssuesMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Forum</a></dt></dl>\n";
                }

                if (adminControlUtils.NotDisabled(con, 908))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Cart</a></dt></dl>\n";
                }

                if (customerCode[0].Length > 0 && HaveProjects(con, customerCode[0]))
                {
                    if (adminControlUtils.NotDisabled(con, 907))
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/ExternalUserProjects?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + customerCode[0] + "&bnm=" + bnm + "\">Projects</a></dt></dl>\n";
                    }
                }

                if (adminControlUtils.NotDisabled(con, 902))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserProducts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Products</a></dt></dl>\n";
                }

                if (customerCode[0].Length > 0)
                {
                    if (adminControlUtils.NotDisabled(con, 903))
                    {
                        if (accessRights[0].Equals("sales") || accessRights[0].Equals("accounts"))
                        {
                            if (layoutType < 3) s += "<br/>";

                            s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                            s += "<a href=\"http://" + men + "/central/servlet/ExternalUserTransactionServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Transactions</a></dt></dl>\n";
                        }
                    }

                    if (adminControlUtils.NotDisabled(con, 904))
                    {
                        if (accessRights[0].Equals("accounts"))
                        {
                            if (layoutType < 3) s += "<br/>";

                            s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                            s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserAccounts?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Accounts</a></dt></dl>\n";
                        }
                    }
                }

                if (adminControlUtils.NotDisabled(con, 906))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/RegisteredUserProfile?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + unm + "&bnm=" + bnm + "\">My Profile</a></dt></dl>\n";
                }

                if (VerifyAccess(con, 188, unm, uty, dnm))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/AboutZara?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">About</a></dt></dl>\n";
                }
            }

            if (uty.Equals("I")) // Internal Users
            {
                if (layoutType < 3) s += "<h1>Cloud</h1><br/>";

                s += "<dl><dt onmouseover=\"setup('vmenu" + vmenuCount++ + "');\">";
                s += "<a id='oc' href=\"http://" + men + "/central/servlet/ContactsDashboardView?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Dashboard</a></dt></dl>";

                if (VerifyAccess(con, 168, unm, uty, dnm))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/InboxServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">B2B Inbox</a></dt></dl>\n";
                }

                if (VerifyAccess(con, 6500, unm, uty, dnm))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/ImagesMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Images</a></dt></dl>\n";
                }

                if (VerifyAccess(con, 6100, unm, uty, dnm))
                {
                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/TextMaintainDirectories?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Text</a></dt></dl>\n";
                }

                bool AccountsServices = VerifyAccess(con, 109, unm, uty, dnm);
                bool AdminServices = VerifyAccess(con, 111, unm, uty, dnm);
                bool ProductCart = VerifyAccess(con, 121, unm, uty, dnm);
                bool ImagesMaintainDirectories = VerifyAccess(con, 6500, unm, uty, dnm);
                bool LibraryListDirectory = VerifyAccess(con, 12000, unm, uty, dnm);
                bool ProductServices = VerifyAccess(con, 102, unm, uty, dnm);
                bool AdminDataBaseFixedFilesCreate = VerifyAccess(con, 104, unm, uty, dnm);
                bool TextMaintainDirectories = VerifyAccess(con, 6100, unm, uty, dnm);
                bool ProductStockRecord = VerifyAccess(con, 3001, unm, uty, dnm);
                bool CustomerPage = VerifyAccess(con, 4001, unm, uty, dnm);
                bool SupplierPage = VerifyAccess(con, 5001, unm, uty, dnm);

                if (AccountsServices || AdminServices || ProductCart || ImagesMaintainDirectories || LibraryListDirectory || ProductServices || ProductServices || TextMaintainDirectories || ProductStockRecord || CustomerPage || SupplierPage)
                {
                    if (layoutType < 3) s += "<br/><h1>Applications</h1>";

                    if (AccountsServices)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/AccountsYearSelection?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Accounts</a></dt></dl>\n";
                    }

                    if (AdminServices)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/AdminServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Admin</a></dt></dl>\n";
                    }

                    if (ProductCart)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/ProductCart?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Cart</a></dt></dl>\n";
                    }

                    if (CustomerPage)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/CustomerServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Customer</a></dt></dl>\n";
                    }

                    if (ProductStockRecord) // stock records
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/ProductServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Product</a></dt></dl>\n";
                    }

                    if (SupplierPage)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/ProductServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Supplier</a></dt></dl>\n";
                    }
                }

                bool FaxServices = VerifyAccess(con, 11000, unm, uty, dnm);
                bool OnlineUsers = VerifyAccess(con, 12601, unm, uty, dnm);
                bool AccountsgeceivableServices = VerifyAccess(con, 158, unm, uty, dnm);
                bool AccountsPayableServices = VerifyAccess(con, 165, unm, uty, dnm);
                bool InboxServices = VerifyAccess(con, 168, unm, uty, dnm);
                bool StockControlServices = VerifyAccess(con, 180, unm, uty, dnm);
                bool WorksControlServices = VerifyAccess(con, 181, unm, uty, dnm);
                bool DataAnalytics = VerifyAccess(con, 182, unm, uty, dnm);
                bool SalesControlServices = VerifyAccess(con, 183, unm, uty, dnm);
                bool _8600 = VerifyAccess(con, 8600, unm, uty, dnm);
                bool ProjectList = VerifyAccess(con, 6800, unm, uty, dnm);
                bool MailZaraSignOn = VerifyAccess(con, 8014, unm, uty, dnm);
                bool _6300 = VerifyAccess(con, 6300, unm, uty, dnm);
                bool ContactsAddressBook = VerifyAccess(con, 8801, unm, uty, dnm);

                if (FaxServices || OnlineUsers || AccountsgeceivableServices || AccountsPayableServices || InboxServices || StockControlServices || WorksControlServices || DataAnalytics || SalesControlServices || _8600 || ProjectList || MailZaraSignOn || _6300 || ContactsAddressBook)
                {
                    if (FaxServices)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/FaxServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Faxes</a></dt></dl>\n";
                    }

                    if (OnlineUsers)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/OnlineUsers?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Online Users</a></dt></dl>\n";
                    }

                    if (ProjectList)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/ProjectList?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Project</a></dt></dl>\n";
                    }

                    if (SalesControlServices)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/SalesControlServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Sales Control</a></dt></dl>\n";
                    }

                    if (StockControlServices)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/StockControlServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Stock Control</a></dt></dl>\n";
                    }

                    if (WorksControlServices)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/WorksControlServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Works Control</a></dt></dl>\n";
                    }
                }

                bool accountsAnalyticServices = VerifyAccess(con, 110, unm, uty, dnm);
                bool salesAnalytics = VerifyAccess(con, 120, unm, uty, dnm);
                bool purchasesAnalytics = VerifyAccess(con, 122, unm, uty, dnm);
                bool companyPersonnelDirectory = VerifyAccess(con, 128, unm, uty, dnm);
                bool aboutZara = VerifyAccess(con, 188, unm, uty, dnm);
                bool _7530 = VerifyAccess(con, 7530, unm, uty, dnm);

                if (accountsAnalyticServices || salesAnalytics || purchasesAnalytics || companyPersonnelDirectory || aboutZara || _7530 || unm.Equals("Tempstaff1") || unm.Equals("Tempstaff2"))
                {
                    if (accountsAnalyticServices)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/AccountsAnalyticServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Accounts BI</a></dt></dl>\n";
                    }

                    if (adminControlUtils.anyOfACategory(con, 7513, unm, uty, dnm) && !(unm.Equals("Tempstaff1") || unm.Equals("Tempstaff2")))
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/PurchasesAnalytics?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Purchases BI</a></dt></dl>\n";
                    }

                    if (adminControlUtils.anyOfACategory(con, 7513, unm, uty, dnm) && !(unm.Equals("Tempstaff1") || unm.Equals("Tempstaff2")))
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/SalesAnalytics?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Sales BI</a></dt></dl>\n";
                    }

                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/DataAnalytics?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Data Analytics</a></dt></dl>\n";

                    if (layoutType < 3) s += "<br/><h1>Site</h1>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=&bnm=" + bnm + "\">Home</a></dt></dl>\n";

                    if (companyPersonnelDirectory)
                    {
                        if (HavePersonnel(con))
                        {
                            if (layoutType < 3) s += "<br/>";

                            s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                            s += "<a href=\"http://" + men + "/central/servlet/CompanyPersonnelDirectory?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">People</a></dt></dl>\n";
                        }
                    }

                    if (aboutZara)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/AboutZara?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">About</a></dt></dl>\n";
                    }

                    if (serverUtils.IsSysAdmin(con, unm, dnm, sid, uty, localDefnsDir, defnsDir) || serverUtils.IsDBAdmin(con, unm) || (_7530 && adminControlUtils.NotDisabled(con, 5900)))
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/RATIssuesMain?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Forum</a></dt></dl>\n";
                    }

                    if (serverUtils.IsSysAdmin(con, unm, dnm, sid, uty, localDefnsDir, defnsDir))
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/_950?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Event</a></dt></dl>\n";
                    }

                    if (layoutType < 3) s += "<br/>";

                    s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                    s += "<a target=\"zarahelp\" href=\"http://" + men + "/central/servlet/HelpguideJump?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\">HelpGuide</a></dt></dl>\n";

                    if (generalUtils.GetFromDefnFile("LATITUDE", "map.dfn", localDefnsDir, defnsDir).Length > 0)
                    {
                        if (layoutType < 3) s += "<br/>";

                        s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                        s += "<a href=\"http://" + men + "/central/servlet/GoogleMapServices?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">Location</a></dt></dl>\n";
                    }
                }
            }

            // display any blogpages
            if (layoutType < 3)
            {
                s += "<br/><h1>News</h1>";

                MySqlDataReader dataReader = null;

                try
                {
                    string query = "SELECT Code, ShortTitle, IsTheHomePage FROM blogs WHERE IsAMenuItem='Y' AND Published = 'Y' ORDER BY ShortTitle";

                    MySqlCommand cmd = new MySqlCommand(query, con);

                    dataReader = cmd.ExecuteReader();

                    string code, shortTitle, isTheHomePage;
                    bool wanted;

                    while (dataReader.Read())
                    {
                        code = Convert.ToString(dataReader["IsDBAdmin"]);
                        shortTitle = Convert.ToString(dataReader["ShortTitle"]);
                        isTheHomePage = Convert.ToString(dataReader["IsTheHomePage"]);

                        wanted = false;
                        if (isTheHomePage.Equals("Y"))
                        {
                            // home page is automatically listed
                        }
                        else wanted = true;

                        if (wanted)
                        {
                            s += "<br/><dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount++ + "');\">\n";
                            s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&bnm=" + bnm + "\">" + shortTitle + "</a></dt></dl>\n";
                        }
                    }

                    dataReader.Close();
                }
                catch (Exception e)
                {
                    if (dataReader != null) dataReader.Close();
                    Console.WriteLine("AuthenticationUtils: " + e);
                }
            }
            else // layout >= 3
            {
                MySqlDataReader dataReader = null;
                MySqlDataReader dataReader2 = null;

                try
                {
                    string query = "SELECT DISTINCT TopicName FROM blogs WHERE IsAMenuItem='Y' AND Published = 'Y' ORDER BY TopicName";

                    MySqlCommand cmd = new MySqlCommand(query, con);

                    dataReader = cmd.ExecuteReader();

                    string topicName, code, shortTitle;

                    // each topic of a blog that has a menu item set

                    bool first;

                    while (dataReader.Read())
                    {
                        topicName = Convert.ToString(dataReader["TopicName"]);

                        // output DD menu

                        dataReader2 = null;

                        string query2 = "SELECT Code, ShortTitle FROM blogs WHERE TopicName = '" + topicName + "' AND IsAMenuItem='Y' AND Published = 'Y' AND IsTheHomePage != 'Y' ORDER BY Date";

                        MySqlCommand cmd2 = new MySqlCommand(query2, con);

                        dataReader2 = cmd2.ExecuteReader();

                        first = true;

                        while (dataReader2.Read())
                        {
                            code = Convert.ToString(dataReader2["Code"]);
                            shortTitle = Convert.ToString(dataReader2["ShortTitle"]);

                            if (first)
                            {
                                s += "<dl><dt onmouseover=\"javascript:setup('vmenu" + vmenuCount + "');\">" + topicName + "</dt><dd id='vmenu" + vmenuCount++ + "'><ul>\n";
                                first = false;
                            }

                            s += "<li><a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + code + "&bnm=" + bnm + "\">" + shortTitle + "</a></li>\n";
                        }

                        if (!first) // at least one written
                            s += "</ul></dd></dl>\n";

                        dataReader2.Close();
                    }

                    dataReader.Close();
                }
                catch (Exception e)
                {
                    if (dataReader != null) dataReader.Close();
                    if (dataReader2 != null) dataReader2.Close();
                    Console.WriteLine("AuthenticationUtils: " + e);
                }
            }
            s += "</div>"; // mainmenu

            s += CreateSetupstring(writeSetupNow, hmenuCount, vmenuCount, otherSetup);

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string CreateSetupstring(bool writeSetupNow, int hmenuCount, int vmenuCount, string otherSetup)
        {
            string s = "<script type=\"text/javascript\">var done=false;\n";
            s += "function setup(id){vsetup(id);hsetup(id);if(!done){done=true;" + otherSetup + "}}";
            s += "function vsetup(id){";
            s += "var i,d=document.getElementById(id);\n";
            s += "for(i=1;i<=" + --vmenuCount + ";++i){if(document.getElementById('vmenu'+i)){document.getElementById('vmenu'+i).style.display='none';}}\n";
            s += "if(d){d.style.display='block';}}</script>\n";

            if (writeSetupNow)
                s += CreateHorizSetupstring(hmenuCount);

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string CreateHorizSetupstring(int hmenuCount)
        {
            string s = "<script type=\"text/javascript\">var done2=false;\n";
            s += "function hsetup(id){if(!done2){done2=true;setFooter();};";
            s += "var i,d=document.getElementById(id);for(i=1;i<=" + hmenuCount + ";++i){if(document.getElementById('hmenu'+i)){document.getElementById('hmenu'+i).style.display='none';}}\n";
            s += "if(d){d.style.display='block';}}</script>\n";

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string CreateHorizSetupstringW()
        {
            string s = "<script type=\"text/javascript\">\n";
            s += "function setup(count,init){";
            s += "var d;for(var i=1;i<=count;++i){d=document.getElementById('hmenu'+i);if(d){d.style.visibility='hidden';}};";
            s += "d=document.getElementById('hmenu'+count);if(d){d.style.display='block';if(init=='Y')d.style.visibility='hidden';";
            s += "else d.style.visibility='visible';}}</script>\n";

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void BuildScreen(MySqlConnection con, bool writeSetupNow, string unm, string sid, string uty, string men, string den, string dnm, string bnm, string callingServlet, string subMenuText,
                                int hmenuCount, string bodyStr, string otherSetup, string localDefnsDir, string defnsDir, byte[] b, int[] bytesOut)
        {
            Scoutln(b, bytesOut, BuildScreen(con, writeSetupNow, unm, sid, uty, men, den, dnm, bnm, callingServlet, subMenuText, hmenuCount, bodyStr, otherSetup, localDefnsDir, defnsDir));
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string BuildScreen(MySqlConnection con, bool writeSetupNow, string unm, string sid, string uty, string men, string den, string dnm, string bnm, string callingServlet,
                                  string subMenuText, int hmenuCount, string bodyStr, string otherSetup, string localDefnsDir, string defnsDir)
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

            GetStyling(con, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

            string imageLibraryDir = directoryUtils.GetImagesDir(dnm);

            string scriptsDir = directoryUtils.GetScriptsDirectory();

            string s = "";

            s += "<script type=\"text/javascript\" src=\"http://" + men + "/Zara/Support/Scripts/contact.js\"></script>\n";
            s += "<script type=\"text/javascript\">";

            s += "function help(which){var newWindow=window.open('','zarahelp');";
            s += "newWindow.location.href=\"http://" + men + "/central/servlet/MainPageUtilsk?dnm=Help&bnm=" + bnm + "&p1=\"+which;}";

            s += "function updateCheckConnect(p1){";
            s += "var url = \"http://" + men + "/central/servlet/MediaUpdateCheck?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&p2=\" + escape('" + callingServlet
              + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + p1 + \"&dnm=\" + escape('" + dnm + "');\n";
            s += "contactInitRequest(url);contactReq.onreadystatechange=contactProcessRequest;contactReq.open(\"GET\",url,true);contactReq.send(null);}";
            s += "</script>\n";

            if (directoryUtils.UsesFlash(con))
                s += "<script type=\"text/javascript\" src=\"http://" + men + scriptsDir + "flashobject.js\"></script>\n";

            int layoutType = GetLayoutType(con, unm, dnm);

            s += "<style type='text/css'>a:link#indexLink,a:visited#indexLink,a:hover#indexLink,a:active#indexLink{text-decoration:none;}</style>"; // for link to index.zaracloud.com

            s += "</head><body " + bodyStr + " onLoad=\"" + otherSetup + "setup();contactInitializeTimer(100);setFooter();\" onResize=\"setFooter();\">\n";

            s += "<div id='bigouter'>";

            if (layoutType > 2)
            {
                if (bnm.Equals("M")) // fix for left-margin when fixed-width
                    s += "<div style='text-align: center;'><div style='text-align: left; margin: 1em auto; width: 50%;'>";
            }

            s += "<div id='outer'>";
            s += "<table border='0' cellpadding='0' cellspacing='0'      width=100%><tr>";

            if (layoutType < 3)
                s += "<td colspan='2'>";
            else s += "<td>";

            s += "<table border='0' width='100%' cellpadding='0' cellspacing='0'>\n";

            if (layoutType == 5)
            {
                if (uty.Equals("A"))
                {
                    s += "<tr id='topBar'><td align='right' valign='top'><div id='headerrepeat'>You are not signed-on. <a href=\"http://" + men + "/central/servlet/MainPageUtilsc?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm="
                      + dnm + "&p1=" + callingServlet + "&p2=" + men + "&p3=" + dnm + "&bnm=" + bnm + "\">Sign-On Here</a>";

                    s += "\n</div></td></tr>\n";
                }
                else
                {
                    s += "<tr><td align='right' valign='top'><div id='headerrepeat'>";

                    s += "User: " + unm;

                    s += "\n</div></td></tr>\n";
                }

                if (headerLogo[0].Length > 0)
                    s += "<tr><td align='left' valign='top'><div id='header'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></div></td>\n";
                else s += "<tr><td align='left' valign='top'><div id='header'></div></td>\n";

                s += "</tr>";
            }
            else
            if (layoutType == 1 || layoutType == 3)
            {
                if (headerLogo[0].Length > 0)
                    s += "<tr><td align='left' valign='top'><div id='header'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></div></td>\n";
                else s += "<tr><td align='left' valign='top'><div id='header'></div></td>\n";

                s += "<td width='99%' valign='top' align='right'><div id='headerrepeat'>\n";

                if (uty.Equals("A"))
                {
                    s += "You are not signed-on. <a href=\"http://" + men + "/central/servlet/MainPageUtilsc?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + callingServlet + "&p2=" + men + "&p3=" + dnm + "&bnm="
                      + bnm + "\">Sign-On Here</a>\n";
                }
                else
                {
                    s += "User: " + unm;
                }

                s += "</div></td></tr>";
            }
            else
            {
                s += "<tr><td width='99%' valign='top' align='right'><div id='headerrepeat'>\n";

                if (uty.Equals("A"))
                {
                    s += "You are not signed-on. <a href=\"http://" + men + "/central/servlet/MainPageUtilsc?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + callingServlet + "&p2=" + men + "&p3=" + dnm + "&bnm="
                      + bnm + "\">Sign-On Here</a>\n";
                }
                else
                {
                    s += "User: " + unm;
                }

                s += "</div></td>";
                s += "<td align='left' valign='top'><div id='header'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></div></td></tr>\n";
            }

            s += "</table></td></tr>\n";

            s += "<tr><td valign='top'>";

            s += BuildMainMenu(con, layoutType, writeSetupNow, unm, sid, uty, men, den, dnm, bnm, callingServlet, hmenuCount, otherSetup, localDefnsDir, defnsDir);

            s += "</td>";

            if (layoutType > 2)
                s += "</tr><tr>";
            else s += "<td valign=top width=99%><table border='0' cellpadding='0' cellspacing='0'        width=100%   ><tr>";

            s += "<td width='99%' valign='top' height='1%'>";
            s += subMenuText;
            s += "</td>";

            s += "</tr><tr>";

            if (layoutType < 3)
                s += "<td valign='top' width='99%' xrowspan='2'>";
            else s += "<td valign='top' width='99%'>";

            s += "<div id='main'>";

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string BuildPlainScreen(MySqlConnection con, string unm, string sid, string uty, string men, string den, string dnm, string bnm, string callingServlet,
                                         string bodyStr, string otherSetup)
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

            GetStyling(con, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

            string imageLibraryDir = directoryUtils.GetImagesDir(dnm);

            string s = "";

            s += "<script type=\"text/javascript\" src=\"http://" + men + "/Zara/Support/Scripts/contact.js\"></script>\n";
            s += "<script type=\"text/javascript\">";
            s += "function updateCheckConnect(p1){";
            s += "var url = \"http://" + men + "/central/servlet/MediaUpdateCheck?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
              + "') + \"&men=\" + escape('" + men + "') + \"&p2=\" + escape('" + callingServlet + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
              + "') + \"&p1=\" + p1 + \"&dnm=\" + escape('" + dnm + "');\n";
            s += "contactInitRequest(url);";
            s += "contactReq.onreadystatechange=contactProcessRequest;\n";
            s += "contactReq.open(\"GET\",url,true);";
            s += "contactReq.send(null);}";
            s += "</script>\n";

            s += "</head><body " + bodyStr + " onLoad=\"" + otherSetup + " contactInitializeTimer(100);\">\n";

            s += "<table border='0' width='100%' cellpadding='0' cellspacing='0'>\n";
            s += "<tr><td align='left' valign='top'><img src=\"http://" + men + imageLibraryDir + headerLogo[0] + "\" border='0'></td></tr>\n";

            s += "<tr><td valign='top'>";

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string BuildFooter(MySqlConnection con, string unm, string dnm, string bnm)
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

            GetStyling(con, headerLogo, headerLogoRepeat, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, watermark);

            int layoutType = GetLayoutType(con, unm, dnm);

            string s = "</div></td>";

            if (layoutType < 3)
                s += "</tr></table></td>";

            s += "</tr><tr><td colspan='2' valign='bottom'>";

            s += "</td></tr></table></div></div>";

            if (layoutType > 2)
            {
                if (bnm.Equals("M")) // fix for left-margin when fixed-width
                    s += "</div></div>";
            }

            return s + "</body></html>";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string Markup(string text)
        {
            string t = "";
            try
            {
                int x = 0, len = text.Length;
                while (x < len)
                {
                    if (text[x] == '\n')
                        t += "<br>";
                    else t += text[x];

                    ++x;
                }
            }
            catch (Exception) { }

            return t;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        int GetLayoutType(MySqlConnection con, string unm, string dnm)
        {
            FileStream fh = generalUtils.FileOpen(directoryUtils.GetCssGeneralDirectory(con, unm, dnm) + "general.css");

            int layoutType = fh == null ? 1 : directoryUtils.GetLayoutType(fh);

            generalUtils.CloseFile(fh);

            return layoutType;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string JsForHeight(int layoutType)
        {
            string s = "<script type='text/javascript'>"
                     + "function getWindowHeight(){var wHt=0;if(typeof(window.innerHeight)=='number'){wHt=window.innerHeight;}else"
                     + "{if(document.documentElement&&document.documentElement.clientHeight){wHt=document.documentElement.clientHeight;}else"
                     + "{if(document.body&&document.body.clientHeight){wHt=document.body.clientHeight;}}}return wHt;}\n";

            s += "function setFooter(){if(document.getElementById){var wHt=getWindowHeight();if(wHt>0){var footerE=document.getElementById('footer');"
              + "var footerH;if(footerE==null)footerH=0;"
              + "else footerH=footerE.offsetHeight;var headerE=document.getElementById('header');var headerH=headerE.offsetHeight;"
              + "var submenuE=document.getElementById('submenu');var submenuH;if(submenuE==null) submenuH=0;else submenuH=submenuE.offsetHeight;"
              + "var topBarE=document.getElementById('topBar');var topBarH;if(topBarE==null)topBarH=0;else topBarH=topBarE.offsetHeight;\n";


            s += "var mainmenuE=document.getElementById('mainmenu');";

            if (layoutType > 2)
                s += "var mainmenuH=mainmenuE.offsetHeight;";
            else s += "var mainmenuH=0;";

            s += "var mainE=document.getElementById('main');\n";

            s += "var mainH=mainE.offsetHeight;\n";

            s += "if((mainH+headerH+footerH+mainmenuH+submenuH+topBarH)<wHt){mainE.style.height=(wHt-(headerH+footerH+mainmenuH+submenuH+topBarH))+'px';if(footerE!=null)footerE.style.position='static';}}}}</script>\n";

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public int CountBloggers(MySqlConnection con)
        {
            int rowCount = 0;

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT COUNT(DISTINCT Owner) AS rowcount FROM blogs LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    rowCount = Convert.ToInt16(dataReader["rowcount"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine(e);
            }

            return rowCount;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool HavePersonnel(MySqlConnection con)
        {
            int rowCount = 0;

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT COUNT(*) AS rowcount FROM profiles WHERE Status = 'L' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    rowCount = Convert.ToInt16(dataReader["rowcount"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine(e);
            }

            return rowCount > 0 ? true : false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool HaveProjects(MySqlConnection con, string companyCode)
        {
            int rowCount = 0;

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT COUNT(*) AS rowcount FROM projects WHERE Status != 'C' AND CompanyCode = '" + companyCode + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    rowCount = Convert.ToInt16(dataReader["rowcount"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine(e);
            }

            return rowCount > 0 ? true : false;
        }


        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool VerifyAccess(MySqlConnection con, int serviceCode, string unm, string uty, string dnm)
        {
            return VerifyAccess(con, serviceCode, true, unm, uty, dnm);
        }
        public bool VerifyAccess(MySqlConnection con, int serviceCode, bool checkForMaintenance, string unm, string uty, string dnm)
        {
            if (unm.Equals("Sysadmin"))
                return true;

            if (checkForMaintenance)
            {
                string[] systemStatus = new string[1];
                definitionTables.GetSystem(con, dnm, systemStatus);
                if (!systemStatus[0].Equals("L")) // ! live
                    return false;

                if (definitionTables.IsSuspended(con, serviceCode))
                    return false;
            }

            if (serviceCode < 0) // some calls may be < 0
                return false;

            if (uty[0] == 'R')
                unm = "___registered___";

            if (!adminControlUtils.NotDisabled(con, serviceCode))
            {
                return false;
            }

            bool res = profile.VerifyAccess(con, unm, uty, dnm, serviceCode);

            return res ? true : false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // checks unm and pwd, and whether specified operation or service may be accessed, and whether docCode belongs to the company
        public bool VerifyAccessForDocument(MySqlConnection con, int serviceCode, string docCode, string unm, string sid, string uty, string dnm, string localDefnsDir, string defnsDir)

        {
            if (!serverUtils.CheckSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
                return false;

            if (serviceCode < 0) // some calls may be < 0
                return false;

            // check if this user has access

            if (uty.Length == 0 || uty[0] == 'A')
                return false;

            if (uty[0] == 'R')
            {
                int i = unm.IndexOf("_", StringComparison.CurrentCultureIgnoreCase);
                unm = unm.Substring(0, i);

                string docCompanyCode;
                switch (serviceCode)
                {
                    case 4121:
                        SalesOrder salesOrder = new SalesOrder();
                        docCompanyCode = salesOrder.GetASOFieldGivenCode(con, "CompanyCode", docCode);
                        if (profile.ValidateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
                            return true;
                        break;
                    case 4122:
                        Quotation quotation = new Quotation();
                        docCompanyCode = quotation.GetAQuoteFieldGivenCode(con, "CompanyCode", docCode);
                        if (profile.ValidateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
                            return true;
                        break;
                    case 4124:
                        SalesInvoice salesInvoice = new SalesInvoice();
                        docCompanyCode = salesInvoice.GetAnInvoiceFieldGivenCode(con, "CompanyCode", docCode);
                        if (profile.ValidateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
                            return true;
                        break;
                    case 4125:
                        ProformaInvoice proformaInvoice = new ProformaInvoice();
                        docCompanyCode = proformaInvoice.GetAProformaFieldGivenCode(con, "CompanyCode", docCode);
                        if (profile.ValidateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
                            return true;
                        break;
                    case 4123:
                        DeliveryOrder deliveryOrder = new DeliveryOrder();
                        docCompanyCode = deliveryOrder.GetADOFieldGivenCode(con, "CompanyCode", docCode);
                        if (profile.ValidateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
                            return true;
                        break;
                    case 4126:
                        OrderConfirmation orderConfirmation = new OrderConfirmation();
                        docCompanyCode = orderConfirmation.GetAnOCFieldGivenCode(con, "CompanyCode", docCode);
                        if (profile.ValidateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
                            return true;
                        break;
                    case 4127:
                        OrderAcknowledgement orderAcknowledgement = new OrderAcknowledgement();
                        docCompanyCode = orderAcknowledgement.GetAnOAFieldGivenCode(con, "CompanyCode", docCode);
                        if (profile.ValidateExternalAccessCompany(unm, 'C', docCompanyCode, dnm, defnsDir, ""))
                            return true;
                        break;
                }

                return false;
            }

            return false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // - internal user can access any doc
        // - anon users only those docs marked as external
        // - registered users only those docs marked as external; and also if a project code exists for the doc, then the user's company must match the project company
        public bool VerifyAccessForLibrary(MySqlConnection con, string unm, string uty, string dnm)
        {
            string[] systemStatus = new string[1];
            definitionTables.GetSystem(con, dnm, systemStatus);
            if (!systemStatus[0].Equals("L")) // ! live
                return false;

            // check if this user has access

            if (uty.Length == 0)
                return false;

            string unmSave = "";

            if (uty[0] == 'R')
            {
                unmSave = unm;
                unm = "Registered"; // needs to be for rights chk for extuser rec check
            }
            else
            if (uty[0] == 'A')
            {
                unm = "Anonymous"; // needs to be for rights chk for anon rec check
            }
            // else unm is as given (an internal user, or demo user)

            if (uty[0] == 'R')
                unm = unmSave;

            if (uty[0] == 'I')
            {
                string[] userName = new string[61];
                return NewValidateSignOn(con, false, unm, "", userName) ? true : false;
            }

            return false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // checks whether the ofsa/system table exists; if not, attempts to create every table
        public void CheckTables(MySqlConnection con, string dnm, string localDefnsDir, string defnsDir)
        {
            try
            {
                if (!directoryUtils.TableExists(con, "ofsa", "system", dnm))
                {
                    AdminDataBaseFixedFiles adminDataBaseFixedFiles = new AdminDataBaseFixedFiles();
                    AdminDataBaseFixedFilesCreate adminDataBaseFixedFilesCreate = new AdminDataBaseFixedFilesCreate();

                    string table, tableNames = adminDataBaseFixedFiles.names();
                    int x = 0, len = tableNames.Length;
                    while (x < len)
                    {
                        table = "";
                        while (tableNames[x] != (char)1)
                            table += tableNames[x++];
                        ++x;

                        if (!directoryUtils.TableExists(con, "ofsa", table, dnm))
                            adminDataBaseFixedFilesCreate.ProcessATable(con, false, 'C', table, dnm, null, null, null);
                    }

                    MailUtils mailUtils = new MailUtils();
                    if (!directoryUtils.TableExists(con, "mail", "mail", dnm))
                        mailUtils.CreateTableMail(false, dnm);

                    if (!directoryUtils.TableExists(con, "mail", "mailaccounts", dnm))
                        mailUtils.CreateTableMailAccounts(false, dnm);

                    LibraryUtils libraryUtils = new LibraryUtils();
                    if (!directoryUtils.TableExists(con, "library", "documents", dnm))
                        libraryUtils.CreateTableDocuments(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "library", "permissions", dnm))
                        libraryUtils.CreateTablePermissions(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "library", "docmove", dnm))
                        libraryUtils.CreateTableDocmove(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "library", "properties", dnm))
                        libraryUtils.CreateTableProperties(false, dnm, localDefnsDir, defnsDir);

                    ProjectUtils projectUtils = new ProjectUtils();
                    if (!directoryUtils.TableExists(con, "project", "projects", dnm))
                        projectUtils.CreateTableProjects(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "project", "title", dnm))
                        projectUtils.CreateTableTitle(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "project", "officetasks", dnm))
                        projectUtils.CreateTableOfficeTasks(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "project", "workshoptasks", dnm))
                        projectUtils.CreateTableWorkshopTasks(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "project", "taskallocation", dnm))
                        projectUtils.CreateTableTaskAllocation(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "project", "tasktime", dnm))
                        projectUtils.CreateTableTaskTime(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "project", "stockrequirements", dnm))
                        projectUtils.CreateTableStockRequirements(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "project", "permissions", dnm))
                        projectUtils.CreateTablePermissions(false, dnm, localDefnsDir, defnsDir);

                    IssuesUtils issuesUtils = new IssuesUtils();
                    if (!directoryUtils.TableExists(con, "info", "rat", dnm))
                        issuesUtils.CreateTableRAT(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "info", "ratcat", dnm))
                        issuesUtils.CreateTableRATCat(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "info", "ratprob", dnm))
                        issuesUtils.CreateTableRATProblemTypes(false, dnm, localDefnsDir, defnsDir);

                    FaxUtils faxUtils = new FaxUtils();
                    if (!directoryUtils.TableExists(con, "fax", "faxed", dnm))
                        faxUtils.CreateTableFaxed(false, dnm, localDefnsDir, defnsDir);

                    BlogsUtils blogsUtils = new BlogsUtils();
                    if (!directoryUtils.TableExists(con, "info", "blogs", dnm))
                        blogsUtils.CreateTableBlogs(false, dnm, localDefnsDir, defnsDir);

                    if (!directoryUtils.TableExists(con, "info", "blogtopics", dnm))
                        blogsUtils.CreateTableBlogTopics(false, dnm);

                    if (!directoryUtils.TableExists(con, "info", "styles", dnm))
                        blogsUtils.CreateTableStyles(false, dnm, localDefnsDir, defnsDir);

                    BlogGuideUtils blogGuideUtils = new BlogGuideUtils();
                    if (!directoryUtils.TableExists(con, "info", "blogguide", dnm))
                        blogGuideUtils.CreateTableBlogGuide(false, dnm);
                }
            }
            catch (Exception e) { Console.WriteLine("CheckTables: " + e); }
        }
    }
}