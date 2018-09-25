// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Authentication, logging, misc
// Module: ServerUtils.cs
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
using System.Net;
using System.Text;
using MySql.Data.MySqlClient;

namespace zarastar
{
    class ServerUtils
    {
        readonly GeneralUtils     generalUtils     = new GeneralUtils();
                 DirectoryUtils   directoryUtils   = new DirectoryUtils();
                 Trail            trail            = new Trail();
        readonly DefinitionTables definitionTables = new DefinitionTables();
                 AuthenticationUtils authenticationUtils = new AuthenticationUtils();

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsSysAdmin(MySqlConnection con, string unm, string dnm, string sid, string uty, string localDefnsDir, string defnsDir)
        {
            if (unm.Equals("Sysadmin"))
            {
                if (!CheckSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
                    return false;

                string[] userName = new string[61];

                return authenticationUtils.NewValidateSignOn(con, false, unm, "", userName) ? true : false;
            }

            return false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsDBAdmin(MySqlConnection con, string unm)
        {
            string isDBAdmin = "N";
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT IsDBAdmin FROM profilesd WHERE UserCode = '" + generalUtils.SanitiseForSQL(unm) + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    isDBAdmin = Convert.ToString(dataReader["IsDBAdmin"]);
                }

                dataReader.Close();

                if (isDBAdmin == null) isDBAdmin = "N";
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("IsDBAdmin: " + e);
            }

            return isDBAdmin.Equals("Y") ? true : false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string TargetDNM(string serverName, string dnm, string defnsDir)
        {

            string s = generalUtils.GetFromDefnFile(serverName + "DNM", "local.dfn", "", defnsDir);

            return s.Length == 0 ? dnm : s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // the IP address of the local machine
        public string ServerToCall(string serverName, string defnsDir)
        {
            string s = generalUtils.GetFromDefnFile(serverName, "local.dfn", "", defnsDir);
            int len = s.Length;
            if (len == 0) // just-in-case
                return "127.0.0.1";

            string localServer = "";
            int x = 0;
            while (x < len && s[x] != ' ') // just-in-case
                localServer += s[x++];

            return localServer;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string ServerToCall(HttpWebRequest req, string serverName, string defnsDir)
        {
            string localLAN = generalUtils.GetFromDefnFile("LOCALLAN", "site.dfn", "", defnsDir);

            string s = generalUtils.GetFromDefnFile(serverName, "local.dfn", "", defnsDir);
            int len = s.Length;
            if (len == 0) // just-in-case
                return "";

            string localServer = "", remoteServer = "";
            int x = 0;
            while (x < len && s[x] != ' ') // just-in-case
                localServer += s[x++];
            while (x < len && s[x] == ' ')
                ++x;
            while (x < len) // just-in-case
                remoteServer += s[x++];

            string reqURL = "";
            /* Http handling TODO
            reqURL = req.getRequestURL().toString();
            */

            return reqURL.StartsWith("http://", StringComparison.CurrentCultureIgnoreCase)
                ? reqURL.Substring(7).StartsWith(localLAN, StringComparison.CurrentCultureIgnoreCase) ? localServer : remoteServer
                : reqURL.StartsWith(localLAN, StringComparison.CurrentCultureIgnoreCase) ? localServer : remoteServer;
        }

        public void ETotalBytes(HttpWebRequest req, string unm, string dnm, int service, int bytesOut, int bytesIn, long duration, string msg)
        {
            MySqlConnection connection = new MySqlConnection("Server = myServerAddress; Port = 3306; Database = myDataBase; Uid = myUsername; Pwd = myPassword;");

            directoryUtils.CreateConnection(connection);

            TotalBytes(connection, req, unm, dnm, service, bytesOut, bytesIn, duration, msg);

            connection.Close();
        }

        public void TotalBytes(MySqlConnection con, HttpWebRequest req, string unm, string dnm, int service, int bytesOut,
                               int bytesIn, long duration, string msg)
        {
            string host = "";

            /* Http handling TODO

            bytesIn += req.GetContentLength;

            if (req.getRequestURL() != null) bytesIn += req.getRequestURL().Length;

            if (req.getQueryString() != null) bytesIn += req.getQueryString().Length;

            if (req.getServletPath() != null) bytesIn += req.getServletPath().Length;

            host = req, GetRemoteHost();

            if (req.getPathInfo() != null) bytesIn += req.getPathInfo().Length;
            */
            if (string.IsNullOrEmpty(unm)) unm = "Casual";

            if (string.IsNullOrEmpty(msg)) msg = ".";

            string today = "", time = "";
            try
            {
                today = generalUtils.Today("", "");
                time = generalUtils.TimeNow(4, "");
            }
            catch (Exception) { }

            string serviceStr = generalUtils.IntToStr(service);
            string service2 = serviceStr;
            short i;

            for(i = 0; i < 9; ++i)
                serviceStr = " " + serviceStr;

            string bytesInStr = generalUtils.IntToStr(bytesIn);
            string bytesIn2 = bytesInStr;
            int len = bytesInStr.Length;
            for(i = 0; i < 5; ++i)
                bytesInStr = " " + bytesInStr;


            string bytesOutStr = generalUtils.IntToStr(bytesOut);
            string bytesOut2 = bytesOutStr;
   
            len = bytesOutStr.Length;
           
             for(i = 0; i < 5; ++i)
                 bytesOutStr = " " + bytesOutStr;

            string userCode = unm;
        
            len = unm.Length;

            for(i = 0; i < 19-len; ++i)
                unm = " " + unm;

            string host2 = host;
      
            len = host.Length;
            for(i = 0; i < 15; ++i) {
                host = " " + host;
            }

            string durationStr = generalUtils.LongToStr(duration);
            len = durationStr.Length;
            for(i = 0; i < 4; ++i)
                durationStr = " " + durationStr;

            if (serviceStr.Equals("8612") || serviceStr.Equals("11200") || serviceStr.StartsWith("1270", StringComparison.CurrentCultureIgnoreCase) || serviceStr.Equals("12601"))
            {}
            else
            if (serviceStr.Equals("12600") && ! msg.StartsWith("OUT:", StringComparison.CurrentCultureIgnoreCase))
            {}
            else
            {
                if (msg.Length > 80)
                    msg = msg.Substring(0, 80);

                Console.WriteLine(unm + " " + dnm + " " + serviceStr + " o:" + bytesOutStr + " i:" + bytesInStr + " " + today + " " + time[0] + time[1] + ":" + time[2] + time[3] + ":" + time[4] + time[5] + " "
                         + host + " " + durationStr + " " + msg);

                trail.AddToTrail(con, dnm, userCode, service2, bytesOut2, bytesIn2, host2, msg);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool CheckSID(string unm, string sid, string uty, string dnm, string localDefnsDir, string defnsDir)
        {
            if (uty == null)
                return false;

            if (!uty.Equals("A") && generalUtils.IsInteger(sid))
                return false;

            string sessionsDir = directoryUtils.GetSessionsDir(dnm);

            try
            {
                string[] files = System.IO.Directory.GetFiles(sessionsDir);
                string fileName;

                foreach (string s in files)
                {
                    fileName = System.IO.Path.GetFileName(s);
                    if (fileName.StartsWith(unm + ".", StringComparison.CurrentCultureIgnoreCase) && fileName.EndsWith("sid", StringComparison.CurrentCultureIgnoreCase))
                    {
                        // chk if is valid SID

                        String newTimeStamp = generalUtils.LongToStr(((generalUtils.TodayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.TimeNowInSecs());

                        FileStream fs = generalUtils.FileOpen(sessionsDir + unm + ".chk");
                        fs.Write(Encoding.ASCII.GetBytes(newTimeStamp));
                        generalUtils.CloseFile(fs);

                        fs = generalUtils.FileOpen(sessionsDir + unm + ".hb");
                        fs.Write(Encoding.ASCII.GetBytes(newTimeStamp));
                        generalUtils.CloseFile(fs);

                        return true;
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("serverUtils CheckSID: (" + sid + " " + unm + " " + dnm + "): " + e);
            }

            return false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void RemoveSID(string unm, string sessionsDir)
        {
            try
            {
                string[] files = System.IO.Directory.GetFiles(sessionsDir);
                string fileName;

                foreach (string s in files)
                {
                    fileName = System.IO.Path.GetFileName(s);
                    if (fileName.StartsWith(unm + ".", StringComparison.CurrentCultureIgnoreCase) && fileName.EndsWith("sid", StringComparison.CurrentCultureIgnoreCase))
                        generalUtils.FileDelete(sessionsDir + fileName);
                }
            }
            catch (Exception e) { Console.WriteLine("ServerUtils: " + e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void RemoveAllSIDRelated(string unm, string sessionsDir)
        {
            try
            {
                string[] files = System.IO.Directory.GetFiles(sessionsDir);
                string fileName;

                foreach (string s in files)
                {
                    fileName = System.IO.Path.GetFileName(s);
                    if (fileName.StartsWith(unm + ".", StringComparison.CurrentCultureIgnoreCase) && fileName.EndsWith("sid", StringComparison.CurrentCultureIgnoreCase))
                        generalUtils.FileDelete(sessionsDir + fileName);
                    else
                    if (fileName.StartsWith(unm + ".", StringComparison.CurrentCultureIgnoreCase) && fileName.EndsWith("hb", StringComparison.CurrentCultureIgnoreCase))
                        generalUtils.FileDelete(sessionsDir + fileName);
                    else
                    if (fileName.StartsWith(unm + ".", StringComparison.CurrentCultureIgnoreCase) && fileName.EndsWith("chk", StringComparison.CurrentCultureIgnoreCase))
                        generalUtils.FileDelete(sessionsDir + fileName);
                }
            }
            catch (Exception e) { Console.WriteLine("ServerUtils: " + e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string NewSessionID(string unm, string uty, string dnm, string sessionsDir, string localDefnsDir, string defnsDir)
        {
            int ii = new Random().Next(1000000);

            String sid = Convert.ToString(ii);

            try
            {
                if (!uty.Equals("A"))
                {
                    generalUtils.Create(sessionsDir + unm + "." + sid + ".sid");

                    if (!generalUtils.FileExists(sessionsDir + unm + ".chk"))
                        generalUtils.Create(sessionsDir + unm + ".chk");

                    if (!generalUtils.FileExists(sessionsDir + unm + ".hb"))
                        generalUtils.Create(sessionsDir + unm + ".hb");

                    CheckSID(unm, sid, uty, dnm, localDefnsDir, defnsDir); // init
                }
                else // anon
                {
                    generalUtils.Create(sessionsDir + "_" + sid + ".sid");

                    if (!generalUtils.FileExists(sessionsDir + "_" + sid + ".chk"))
                        generalUtils.Create(sessionsDir + "_" + sid + ".chk");

                    if (!generalUtils.FileExists(sessionsDir + "_" + sid + ".hb"))
                        generalUtils.Create(sessionsDir + "_" + sid + ".hb");

                    CheckSID("_" + sid, sid, uty, dnm, localDefnsDir, defnsDir); // init
                }
            }
            catch (Exception e) { Console.WriteLine("serverUtils: NewSessionID() : " + e); }

            return sid;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void RemoveExtinctSIDs(string sessionsDir, string localDefnsDir, string defnsDir)
        {
            long timeNow = ((generalUtils.TodayEncoded(localDefnsDir, defnsDir) - 1) * 86400L) + generalUtils.TimeNowInSecs();

            int z;
            string prefix;
            FileStream fs;
            byte[]
            lastHeartBeatTimeStamp = new byte[50];

            try
            {
                string[] files = System.IO.Directory.GetFiles(sessionsDir);
                string fileName;

                foreach (string s in files)
                {
                    fileName = System.IO.Path.GetFileName(s);

                    if (fileName.EndsWith(".hb", StringComparison.CurrentCultureIgnoreCase))
                    {
                        if ((fs = generalUtils.FileOpen(sessionsDir + fileName)) == null)
                            Console.WriteLine("serverUtils: >>> failed to open: >" + sessionsDir + fileName + "<");
                        else
                        {
                            z = 0;
                            try
                            {
                                while (true)
                                    lastHeartBeatTimeStamp[z++] = (byte)fs.ReadByte();
                            }
                            catch (Exception) { }

                            lastHeartBeatTimeStamp[z] = (byte)0;
                            generalUtils.CloseFile(fs);

                            if (timeNow > (Convert.ToInt64(lastHeartBeatTimeStamp) + 3600))
                            {
                                prefix = fileName.Substring(0, fileName.Length - 3);

                                generalUtils.FileDelete(sessionsDir + fileName);
                                generalUtils.FileDelete(sessionsDir + prefix + ".sid");
                                generalUtils.FileDelete(sessionsDir + prefix + ".chk");
                                if (prefix.Length > 0) // avoid deleting dirs if processing the ".hb" (erroneous) entry
                                    generalUtils.DirectoryHierarchyDelete(sessionsDir + prefix);
                            }
                        }
                    }
                }
            }
            catch (Exception e) { Console.WriteLine("ServerUtils: RemoveExtinctSIDs(): " + e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool PassLockCheck(MySqlConnection con, string docType, string docDate, string unm)
        {
            if (unm.Equals("Sysadmin")) return true;

            bool res = false;

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT LockedUpto, OpenTo FROM locks WHERE DocumentAbbrev = '" + docType + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                String lockedUpto = "", openTo = "";

                if (dataReader.Read())
                {
                    lockedUpto = Convert.ToString(dataReader["LockedUpto"]);
                    openTo = Convert.ToString(dataReader["OpenTo"]);
                }

                dataReader.Close();

                if (lockedUpto.Equals("1970-01-01"))
                    res = true;
                else
                if (generalUtils.EncodeFromYYYYMMDD(docDate) > generalUtils.EncodeFromYYYYMMDD(lockedUpto))
                    res = true;
                else
                {
                    string thisUNM;
                    bool found = false;
                    int x = 0, len = openTo.Length;
                    while (x < len && !found)
                    {
                        thisUNM = "";
                        while (x < len && openTo[x] != ' ' && openTo[x] != ',' && openTo[x] != ';' && openTo[x] != ':')
                            thisUNM += openTo[x++];
                        if (thisUNM.Equals(unm, StringComparison.CurrentCultureIgnoreCase))
                        {
                            res = true;
                            found = true;
                        }
                        else
                        {
                            while (x < len && (openTo[x] == ' ' || openTo[x] == ',' || openTo[x] == ';' || openTo[x] == ':'))
                                ++x;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("serverUtils: PassLockCheck() " + e);
            }

            return res;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void SyncToIW(MySqlConnection con, string docCode, string quoteCode, string soCode, char docType, string docDate, string userCode, string customerCode)
        {
            if (!DoIWSyncing()) return;

            try
            {
                string waveID;
                switch (docType)
                {
                    case 'Q':
                        CreateNew(con, userCode, "Q", docCode, customerCode, docDate);
                        break;
                    case 'S':
                        if (quoteCode.Length > 0)
                        {
                            waveID = GetWaveIDForDoc(con, quoteCode, 'Q');
                            AddToWave(con, waveID, userCode, "S", docCode);
                        }
                        else CreateNew(con, userCode, "S", docCode, customerCode, docDate);
                        break;
                    case 'D':
                        if (soCode.Length > 0) // soCode var used to pass-in plCode
                        {
                            soCode = GetSOCodeGivenPLCode(con, soCode);
                            if (soCode.Length > 0)
                            {
                                waveID = GetWaveIDForDoc(con, soCode, 'S');
                                AddToWave(con, waveID, userCode, "D", docCode);
                            }
                        }

                        ForEachSOOnDO(con, docCode, userCode);
                        break;
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("ServerUtils: SyncToIW(): " + e);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string GetWaveIDForDoc(MySqlConnection con, string docCode, char docType)
        {
            string waveID = "";
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT WaveID FROM waveletc WHERE DocCode = '" + docCode + "' AND DocType = '" + docType + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    waveID = Convert.ToString(dataReader["WaveID"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("GetWaveIDForDoc: " + e);
                return "";
            }

            return waveID;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool AddToWave(MySqlConnection con, string waveID, string userCode, string docType, string docCode)
        {
            if (waveID.Length == 0) // just-in-case
                return false;

            String posn = GetNextPositionInWavelet(con, waveID);

            String waveletID = CreateNewWavelet(con, waveID, userCode, docCode, docType, posn);

            return waveletID.Length != 0;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool CreateNew(MySqlConnection con, string userCode, string docType, string docCode, string custCode, string waveDated)
        {
            string waveID = CreateNewWave(con, userCode, waveDated);
            if (waveID.Length == 0)
                return false;

            string waveletID = CreateNewWavelet(con, waveID, userCode, docCode, docType, "10");

            if (waveletID.Length == 0)
            {
                DeleteWave(con, waveID);
                return false;
            }

            CreateNewWavechannelsRec(con, waveID, custCode, waveDated, "xxx");

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string GetNextPositionInWavelet(MySqlConnection con, string waveID)
        {
            string posn = "0";

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT Position FROM waveletc WHERE WaveID = '" + waveID + "' ORDER BY Position DESC LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    posn = Convert.ToString(dataReader["Position"]);
                }
           
                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: " + e);
                return "";
            }

            return "" + (generalUtils.IntFromStr(posn) + 10);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string CreateNewWave(MySqlConnection con, string userCode, string dated)
        {
            int waveID = 0;
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT WaveID FROM wavec ORDER BY WaveID DESC LIMIT 1'";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    waveID = Convert.ToInt32(dataReader["WaveID"]);
                    ++waveID;
                }
                else waveID = 1;

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: CreateNewWave(): " +e);
                return "";
            }

            try
            {
                string query = "INSERT INTO wavec (WaveID, Owner, Closed, Status, IsLocked, IsAboutWave, dated, IsInfo, IsAdvert, DNMOwner, IsEditorial, PaperID, IsOffer) VALUES ('" + waveID + "','" + userCode
                             + "','F','L','F','F','" + dated + "','F','F','xxx','F','','F')";

                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("CreateNewWave(): " + e);
              
                return "";
            }

            return Convert.ToString(waveID);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void DeleteWaveletRecord(MySqlConnection con, string waveletID)
        {
            try
            {
                string query = "DELETE FROM waveletc WHERE WaveletID = '" + waveletID + "'";
                
                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("DeleteWaveletRecord(): " + e);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string CreateNewWavelet(MySqlConnection con, string waveID, string userCode, string docCode, string docType, string posn)
        {
            int waveletID = 0;
       
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT WaveletID FROM waveletc ORDER BY WaveletID DESC LIMIT 1'";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    waveletID = Convert.ToInt32(dataReader["WaveletID"]);
                    ++waveletID;
                }
                else waveletID = 1;

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: " + e);
                return "";
            }

            try
            {
                string text = "";
                if (docType.Equals("Q"))
                    text = "Quotation: " + docCode;
                else
                if (docType.Equals("S"))
                    text = "Sales Order: " + docCode;
                else
                if (docType.Equals("D"))
                    text = "Delivery Order: " + docCode;

                string path = "";
                string youTubeID = "";
                string closureAchieved = "";
                string type = "B";

                string internalOnly = "N";
                if (docType.Equals("P"))
                    internalOnly = "Y";

                string query = "INSERT INTO waveletc (WaveletID, Position, WaveID, Owner, Type, Status, Text, Path, YouTubeID, DocCode, DocType, ClosureAchieved, InternalOnly) VALUES ('" + waveletID + "','" + posn + "','" + waveID + "','" + userCode
                             + "','" + type + "','L','" + text + "','" + path + "','" + youTubeID + "','" + docCode + "','" + docType + "','" + closureAchieved + "','" + internalOnly + "')";

                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("ServerUtils: " + e);
            }

            return Convert.ToString(waveletID);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void DeleteWave(MySqlConnection con, String waveID)
        {
            try
            {
                string query = "DELETE FROM wavec WHERE WaveID = '" + waveID + "'";

                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("DeleteWave: " + e);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool CreateNewBizDoc(MySqlConnection con, String waveletID, String docCode, char docType, String date)
        {
            try
            {
                string query = "INSERT INTO bizdoc (WaveletID, DocCode, DocType, CreationTimeStamp, ClosureAchieved) VALUES ('" + waveletID + "','" + generalUtils.SanitiseForSQL(docCode) + "','" + docType + "','" 
                             + date + "','N')";

                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("ServerUtils: " + e);
                return false;
            }

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool DoIWSyncing()
        {
            String sync = generalUtils.GetFromDefnFile("SYNC", "sync.dfn", "/iotaWave/", "");
            return sync.Equals("Y") || sync.Equals("y") ? true : false;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void SyncToIWCreateBizDocOp(MySqlConnection con, String docCode, char docType, String userCode, String content, String host, String bytesIn, String bytesOut, String service)
        {
            if (!DoIWSyncing()) return;

            try
            {
                String[] docType2 = new String[1];
                String[] operation = new String[1];

                GetDocTypeForService(service, docType2, operation);

                CreateNewBizDocOpRecord(con, docCode, docType, userCode, "", operation[0], content, host, bytesIn, bytesOut, service);
            }
            catch (Exception e)
            {
                Console.WriteLine("ServerUtils: " + e);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool CreateNewBizDocOpRecord(MySqlConnection con, String docCode, char docType, String userCode, String fieldName, String operation, String content, String host, String bytesIn, String bytesOut, String service)

        {
            try
            {
                string query = "INSERT INTO bizdocops (DocumentCode, DocumentType, UserCode, FieldName, Operation, Content, Host, BytesIn, BytesOut, Service) VALUES ('" + docCode + "','" + docType + "','" + userCode + "','" + fieldName + "','"
                             + operation + "','" + content + "','" + host + "','" + bytesIn + "','" + bytesOut + "','" + service + "')";

                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception) // duplicates are possible
            {
            }
            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void GetDocTypeForService(String service, String[] docType, String[] op)
        {
            docType[0] = "";

            // Records - Customer
            if (service.Equals("103")) { docType[0] = "1"; op[0] = "PGE"; }
            else // Screen: Customer Services
            if (service.Equals("141")) { docType[0] = "1"; op[0] = "PGE"; }
            else // Screen: Customer Focus

            if (service.Equals("4001")) { docType[0] = "1"; op[0] = "FTC"; }
            else // Record: Fetch Customer Record
            if (service.Equals("4003")) { docType[0] = "1"; op[0] = "CRT"; }
            else // Record: Create Customer Record
            if (service.Equals("4226")) { docType[0] = "1"; op[0] = "CHN"; }
            else // Record: Change Customer Record
            if (service.Equals("4227")) { docType[0] = "1"; op[0] = "UPD"; }
            else // Record: Update Customer Record

            // Query - Customer
            if (service.Equals("4004")) { docType[0] = "1"; op[0] = "LST"; }
            else // Record: List Customer Records
            if (service.Equals("4005")) { docType[0] = "1"; op[0] = "RPT"; }
            else // Record: Print Customer Listing
            if (service.Equals("4225")) { docType[0] = "1"; op[0] = "PGE"; }
            else // Record: Replace Customer Code
            if (service.Equals("4230")) { docType[0] = "1"; op[0] = "PGE"; }
            else // Record: Select Customer Code
            if (service.Equals("1004")) { docType[0] = "1"; op[0] = "RPT"; }
            else // Report: Debtors Ageing
            if (service.Equals("1029")) { docType[0] = "1"; op[0] = "ENQ"; }
            else // Query: Consolidated Debtors
            if (service.Equals("1012")) { docType[0] = "1"; op[0] = "RPT"; }
            else // Report: Statements of Account

            // Supplier
            if (service.Equals("104")) { docType[0] = "2"; op[0] = "PGE"; }
            else // Screen: Supplier Services
            if (service.Equals("138")) { docType[0] = "2"; op[0] = "PGE"; }
            else // Screen: Supplier Focus
            if (service.Equals("5001")) { docType[0] = "2"; op[0] = "FTC"; }
            else // Record: Fetch Supplier Record
            if (service.Equals("5003")) { docType[0] = "2"; op[0] = "CRT"; }
            else // Record: Create Supplier Record
            if (service.Equals("5070")) { docType[0] = "2"; op[0] = "CHN"; }
            else // Record: Change Supplier Record
            if (service.Equals("5071")) { docType[0] = "2"; op[0] = "UPD"; }
            else // Record: Update Supplier Record

            // Query - Supplier
            if (service.Equals("5004")) { docType[0] = "2"; op[0] = "LST"; }
            else // Record: List Supplier Records
            if (service.Equals("5068")) { docType[0] = "2"; op[0] = "PGE"; }
            else // Record: Replace Supplier Code
            if (service.Equals("5069")) { docType[0] = "2"; op[0] = "PGE"; }
            else // Record: Select Supplier Code
            if (service.Equals("1013")) { docType[0] = "2"; op[0] = "RPT"; }
            else // Report: Creditors Ageing
            if (service.Equals("1031")) { docType[0] = "2"; op[0] = "ENQ"; }
            else // Query: Consolidated Creditors

            // Document - Quote
            if (service.Equals("142")) { docType[0] = "Q"; op[0] = "PGE"; }
            else // Screen: Quotation Focus
            if (service.Equals("4019")) { docType[0] = "Q"; op[0] = "FTC"; }
            else // Document: Fetch Quotation
            if (service.Equals("4020")) { docType[0] = "Q"; op[0] = "CRT"; }
            else // Document: Create New Quotation
            if (service.Equals("4021")) { docType[0] = "Q"; op[0] = "CNV"; }
            else // Document: Create Quotation from Delivery Order
            if (service.Equals("4022")) { docType[0] = "Q"; op[0] = "CNV"; }
            else // Document: Create Quotation from Quotation
            if (service.Equals("3140")) { docType[0] = "Q"; op[0] = "CNV"; }
            else // Document: Create Quotation from Inbox Record
            if (service.Equals("4065")) { docType[0] = "Q"; op[0] = "CNV"; }
            else // Document: Create Quotation from Sales Order
            if (service.Equals("4023")) { docType[0] = "Q"; op[0] = "CHN"; }
            else // Document: Change Quotation Header
            if (service.Equals("4024")) { docType[0] = "Q"; op[0] = "UPD"; }
            else // Document: Update Quotation Header
            if (service.Equals("4025")) { docType[0] = "Q"; op[0] = "AMN"; }
            else // Document: Change Quotation Line
            if (service.Equals("4026")) { docType[0] = "Q"; op[0] = "UPT"; }
            else // Document: Update Quotation Line
            if (service.Equals("4027")) { docType[0] = "Q"; op[0] = "DLL"; }
            else // Document: Delete Quotation Line
            if (service.Equals("4028")) { docType[0] = "Q"; op[0] = "PRN"; }
            else // Document: Print Quotation
            if (service.Equals("4180")) { docType[0] = "Q"; op[0] = "FAX"; }
            else // Document: Fax Quotation
            if (service.Equals("2200")) { docType[0] = "Q"; op[0] = "CNV"; }
            else // Document: Save Cart to Quotation

            // Query - Quote
            if (service.Equals("4030")) { docType[0] = "Q"; op[0] = "LST"; }
            else // Document: List Quotations
            if (service.Equals("2024")) { docType[0] = "Q"; op[0] = "ENQ"; }
            else // Query: Quotation Track and Trace
            if (service.Equals("2029")) { docType[0] = "Q"; op[0] = "PGE"; }
            else // Control: Quotation Processing Sales

            // Document - SO
            if (service.Equals("140")) { docType[0] = "S"; op[0] = "PGE"; }
            else // Screen: Sales Order Focus
            if (service.Equals("4031")) { docType[0] = "S"; op[0] = "FTC"; }
            else // Document: Fetch Sales Order
            if (service.Equals("4032")) { docType[0] = "S"; op[0] = "CRT"; }
            else // Document: Create New Sales Order
            if (service.Equals("4033")) { docType[0] = "S"; op[0] = "CNV"; }
            else // Document: Create Sales Order from Quotation
            if (service.Equals("4034")) { docType[0] = "S"; op[0] = "CNV"; }
            else // Document: Create Sales Order from Sales Order
            if (service.Equals("3141")) { docType[0] = "S"; op[0] = "CNV"; }
            else // Document: Create Sales Order from Inbox Record
            if (service.Equals("4035")) { docType[0] = "S"; op[0] = "CHN"; }
            else // Document: Change Sales Order Header
            if (service.Equals("4036")) { docType[0] = "S"; op[0] = "UPD"; }
            else // Document: Update Sales Order Header
            if (service.Equals("4037")) { docType[0] = "S"; op[0] = "AMN"; }
            else // Document: Change Sales Order Line
            if (service.Equals("4038")) { docType[0] = "S"; op[0] = "UPT"; }
            else // Document: Update Sales Order Line
            if (service.Equals("4039")) { docType[0] = "S"; op[0] = "DLL"; }
            else // Document: Delete Sales Order Line
            if (service.Equals("4040")) { docType[0] = "S"; op[0] = "PRN"; }
            else // Document: Print Sales Order
            if (service.Equals("4181")) { docType[0] = "S"; op[0] = "FAX"; }
            else // Document: Fax Sales Order

            // Query - SO
            if (service.Equals("4042")) { docType[0] = "S"; op[0] = "LST"; }
            else // Document: List Sales Orders
            if (service.Equals("2021")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Track and Trace
            if (service.Equals("7805")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Query: Sales Order Lines Without Delivery Date
            if (service.Equals("1100")) { docType[0] = "S"; op[0] = "CHR"; }
            else // Chart: Sales Orders Past Due

            // Document - OC
            if (service.Equals("146")) { docType[0] = "O"; op[0] = "PGE"; }
            else // Screen: Order Confirmation Focus
            if (service.Equals("4043")) { docType[0] = "O"; op[0] = "FTC"; }
            else // Document: Fetch Order Confirmation
            if (service.Equals("4044")) { docType[0] = "O"; op[0] = "CRT"; }
            else // Document: Create New Order Confirmation
            if (service.Equals("4045")) { docType[0] = "O"; op[0] = "CNV"; }
            else // Document: Create Order Confirmation from Sales Order
            if (service.Equals("4046")) { docType[0] = "O"; op[0] = "CHN"; }
            else // Document: Change Order Confirmation Header
            if (service.Equals("4047")) { docType[0] = "O"; op[0] = "UPD"; }
            else // Document: Update Order Confirmation Header
            if (service.Equals("4048")) { docType[0] = "O"; op[0] = "AMN"; }
            else // Document: Change Order Confirmation Line
            if (service.Equals("4049")) { docType[0] = "O"; op[0] = "UPT"; }
            else // Document: Update Order Confirmation Line
            if (service.Equals("4050")) { docType[0] = "O"; op[0] = "DLL"; }
            else // Document: Delete Order Confirmation Line
            if (service.Equals("4051")) { docType[0] = "O"; op[0] = "PRN"; }
            else // Document: Print Order Confirmation
            if (service.Equals("4182")) { docType[0] = "O"; op[0] = "FAX"; }
            else // Document: Fax Order Confirmation

            // Query - OC
            if (service.Equals("4053")) { docType[0] = "O"; op[0] = "LST"; }
            else // Document: List Order Confirmations

            // Document - OA
            if (service.Equals("170")) { docType[0] = "B"; op[0] = "PGE"; }
            else // Screen: Order Acknowledgement Focus
            if (service.Equals("4130")) { docType[0] = "B"; op[0] = "FTC"; }
            else // Document: Fetch Order Acknowledgement
            if (service.Equals("4131")) { docType[0] = "B"; op[0] = "CRT"; }
            else // Document: Create New Order Acknowledgement
            if (service.Equals("4132")) { docType[0] = "B"; op[0] = "CNV"; }
            else // Document: Create Order Acknowledgement from Sales Order
            if (service.Equals("4133")) { docType[0] = "B"; op[0] = "CHN"; }
            else // Document: Change Order Acknowledgement Header
            if (service.Equals("4134")) { docType[0] = "B"; op[0] = "UPD"; }
            else // Document: Update Order Acknowledgement Header
            if (service.Equals("4135")) { docType[0] = "B"; op[0] = "AMN"; }
            else // Document: Change Order Acknowledgement Line
            if (service.Equals("4136")) { docType[0] = "B"; op[0] = "UPT"; }
            else // Document: Update Order Acknowledgement Line
            if (service.Equals("4137")) { docType[0] = "B"; op[0] = "DLL"; }
            else // Document: Delete Order Acknowledgement Line
            if (service.Equals("4138")) { docType[0] = "B"; op[0] = "PRN"; }
            else // Document: Print Order Acknowledgement
            if (service.Equals("4140")) { docType[0] = "B"; op[0] = "FAX"; }
            else // Document: Fax Order Acknowledgement
            if (service.Equals("4141")) { docType[0] = "B"; op[0] = "PGE"; }
            else // Document: Mail Order Acknowledgement

            // Query - OA
            if (service.Equals("4139")) { docType[0] = "B"; op[0] = "LST"; }
            else // Document: List Order Acknowledgements

            // Document - PL
            if (service.Equals("147")) { docType[0] = "P"; op[0] = "PGE"; }
            else // Screen: Picking List Focus
            if (service.Equals("3038")) { docType[0] = "P"; op[0] = "FTC"; }
            else // Document: Fetch Picking List
            if (service.Equals("3039")) { docType[0] = "P"; op[0] = "CRT"; }
            else // Document: Create New Picking List
            if (service.Equals("3040")) { docType[0] = "P"; op[0] = "CNV"; }
            else // Document: Create Picking List from DO
            if (service.Equals("3041")) { docType[0] = "P"; op[0] = "CNV"; }
            else // Document: Create Picking List from Invoice
            if (service.Equals("3042")) { docType[0] = "P"; op[0] = "CNV"; }
            else // Document: Create Picking List from SO
            if (service.Equals("3043")) { docType[0] = "P"; op[0] = "CHN"; }
            else // Document: Change Picking List Header
            if (service.Equals("3044")) { docType[0] = "P"; op[0] = "UPD"; }
            else // Document: Update Picking List Header
            if (service.Equals("3045")) { docType[0] = "P"; op[0] = "AMN"; }
            else // Document: Change Picking List Line
            if (service.Equals("3046")) { docType[0] = "P"; op[0] = "UPT"; }
            else // Document: Update Picking List Line
            if (service.Equals("3047")) { docType[0] = "P"; op[0] = "DLL"; }
            else // Document: Delete Picking List Line
            if (service.Equals("3048")) { docType[0] = "P"; op[0] = "PRN"; }
            else // Document: Print Picking List
            if (service.Equals("4183")) { docType[0] = "P"; op[0] = "FAX"; }
            else // Document: Fax Picking List
            if (service.Equals("3054")) { docType[0] = "P"; op[0] = "UPT"; }
            else // Document: Update Quantity Packed
            if (service.Equals("3056")) { docType[0] = "P"; op[0] = "UPD"; }
            else // Document: Update Set Completed

            // Query - PL
            if (service.Equals("3050")) { docType[0] = "P"; op[0] = "LST"; }
            else // Document: List Picking Lists
            if (service.Equals("2025")) { docType[0] = "P"; op[0] = "ENQ"; }
            else // Query: Picking List Track and Trace

            // Definitions - PL
            if (service.Equals("3060")) { docType[0] = "P"; op[0] = "PGE"; }
            else // Document: Edit Picking List Once Completed

            // Document - DO
            if (service.Equals("148")) { docType[0] = "D"; op[0] = "PGE"; }
            else // Screen: Delivery Order Focus
            if (service.Equals("4054")) { docType[0] = "D"; op[0] = "FTC"; }
            else // Document: Fetch Delivery Order
            if (service.Equals("4055")) { docType[0] = "D"; op[0] = "CRT"; }
            else // Document: Create New Delivery Order
            if (service.Equals("4056")) { docType[0] = "D"; op[0] = "CNV"; }
            else // Document: Create Delivery Order from Quotation
            if (service.Equals("4057")) { docType[0] = "D"; op[0] = "CNV"; }
            else // Document: Create Delivery Order from Picking List
            if (service.Equals("4058")) { docType[0] = "D"; op[0] = "CNV"; }
            else // Document: Create Delivery Order from Sales Order
            if (service.Equals("4059")) { docType[0] = "D"; op[0] = "CHN"; }
            else // Document: Change Delivery Order Header
            if (service.Equals("4060")) { docType[0] = "D"; op[0] = "UPD"; }
            else // Document: Update Delivery Order Header
            if (service.Equals("4061")) { docType[0] = "D"; op[0] = "AMN"; }
            else // Document: Change Delivery Order Line
            if (service.Equals("4062")) { docType[0] = "D"; op[0] = "UPT"; }
            else // Document: Update Delivery Order Line
            if (service.Equals("4063")) { docType[0] = "D"; op[0] = "DLL"; }
            else // Document: Delete Delivery Order Line
            if (service.Equals("4064")) { docType[0] = "D"; op[0] = "PRN"; }
            else // Document: Print Delivery Order
            if (service.Equals("4184")) { docType[0] = "D"; op[0] = "FAX"; }
            else // Document: Fax Delivery Order

            // Query - DO
            if (service.Equals("4066")) { docType[0] = "D"; op[0] = "LST"; }
            else // Document: List Delivery Orders
            if (service.Equals("2026")) { docType[0] = "D"; op[0] = "ENQ"; }
            else // Query: Delivery Order Track and Trace

            // Document - SI
            if (service.Equals("149")) { docType[0] = "I"; op[0] = "PGE"; }
            else // Screen: Sales Invoice Focus
            if (service.Equals("4067")) { docType[0] = "I"; op[0] = "FTC"; }
            else // Document: Fetch Sales Invoice
            if (service.Equals("4224")) { docType[0] = "I"; op[0] = "CRT"; }
            else // Document: Create New Sales Invoice
            if (service.Equals("4068")) { docType[0] = "I"; op[0] = "CNV"; }
            else // Document: Create Cash Sales Invoice from Picking List
            if (service.Equals("4069")) { docType[0] = "I"; op[0] = "CNV"; }
            else // Document: Create Sales Invoice from Picking List
            if (service.Equals("4070")) { docType[0] = "I"; op[0] = "CNV"; }
            else // Document: Create Sales Invoice from Delivery Order
            if (service.Equals("4071")) { docType[0] = "I"; op[0] = "CNV"; }
            else // Document: Create Sales Invoice from Quotation
            if (service.Equals("4072")) { docType[0] = "I"; op[0] = "CHN"; }
            else // Document: Change Sales Invoice Header
            if (service.Equals("4073")) { docType[0] = "I"; op[0] = "UPD"; }
            else // Document: Update Sales Invoice Header
            if (service.Equals("4074")) { docType[0] = "I"; op[0] = "AMN"; }
            else // Document: Change Sales Invoice Line
            if (service.Equals("4075")) { docType[0] = "I"; op[0] = "UPT"; }
            else // Document: Update Sales Invoice Line
            if (service.Equals("4076")) { docType[0] = "I"; op[0] = "DLL"; }
            else // Document: Delete Sales Invoice Line
            if (service.Equals("4077")) { docType[0] = "I"; op[0] = "PRN"; }
            else // Document: Print Sales Invoice
            if (service.Equals("4185")) { docType[0] = "I"; op[0] = "FAX"; }
            else //  Document: Fax Sales Invoice

            // Query - SI
            if (service.Equals("4079")) { docType[0] = "I"; op[0] = "LST"; }
            else // Document: List Sales Invoices
            if (service.Equals("4313")) { docType[0] = "I"; op[0] = "ENQ"; }
            else // Report: Sales Invoices Monthly Listing
            if (service.Equals("2028")) { docType[0] = "I"; op[0] = "ENQ"; }
            else // Query: Sales Invoice Track and Trace
            if (service.Equals("1023")) { docType[0] = "I"; op[0] = "ENQ"; }
            else // Query: Sales Invoice Listing Enquiry by SalesPerson
            if (service.Equals("1033")) { docType[0] = "I"; op[0] = "ENQ"; }
            else // Query: Sales Invoice Listing Enquiry by Date
            if (service.Equals("1038")) { docType[0] = "I"; op[0] = "ENQ"; }
            else // Query: Sales Invoice Lines Enquiry for all Manufacturers
            if (service.Equals("1027")) { docType[0] = "I"; op[0] = "ENQ"; }
            else // Query: Sales Invoice Enquiry for GST

            // Document - Proforma
            if (service.Equals("150")) { docType[0] = "R"; op[0] = "PGE"; }
            else // Screen: Proforma Invoice Focus
            if (service.Equals("4080")) { docType[0] = "R"; op[0] = "FTC"; }
            else // Document: Fetch Proforma Invoice
            if (service.Equals("4081")) { docType[0] = "R"; op[0] = "CRT"; }
            else // Document: Create New Proforma Invoice
            if (service.Equals("4082")) { docType[0] = "R"; op[0] = "CNV"; }
            else // Document: Create Proforma Invoice from Sales Order
            if (service.Equals("4083")) { docType[0] = "R"; op[0] = "CNV"; }
            else // Document: Create Proforma Invoice from Sales Invoice
            if (service.Equals("4084")) { docType[0] = "R"; op[0] = "CHN"; }
            else // Document: Change Proforma Invoice Header
            if (service.Equals("4085")) { docType[0] = "R"; op[0] = "UPD"; }
            else // Document: Update Proforma Invoice Header
            if (service.Equals("4086")) { docType[0] = "R"; op[0] = "AMN"; }
            else // Document: Change Proforma Invoice Line
            if (service.Equals("4087")) { docType[0] = "R"; op[0] = "UPT"; }
            else // Document: Update Proforma Invoice Line
            if (service.Equals("4088")) { docType[0] = "R"; op[0] = "DLL"; }
            else // Document: Delete Proforma Invoice Line
            if (service.Equals("4089")) { docType[0] = "R"; op[0] = "PRN"; }
            else // Document: Print Proforma Invoice
            if (service.Equals("4186")) { docType[0] = "R"; op[0] = "FAX"; }
            else // Document: Fax Proforma Invoice

            // Query - Proforma
            if (service.Equals("4091")) { docType[0] = "R"; op[0] = "LST"; }
            else // Document: List Proforma Invoices

            // Document - SCN
            if (service.Equals("155")) { docType[0] = "C"; op[0] = "PGE"; }
            else // Screen: Sales Credit Note Focus
            if (service.Equals("4101")) { docType[0] = "C"; op[0] = "FTC"; }
            else // Document: Fetch Sales Credit Note
            if (service.Equals("4102")) { docType[0] = "C"; op[0] = "CRT"; }
            else // Document: Create New Sales Credit Note
            if (service.Equals("4103")) { docType[0] = "C"; op[0] = "CHN"; }
            else // Document: Change Sales Credit Note Header
            if (service.Equals("4104")) { docType[0] = "C"; op[0] = "UPD"; }
            else // Document: Update Sales Credit Note Header
            if (service.Equals("4105")) { docType[0] = "C"; op[0] = "AMN"; }
            else // Document: Change Sales Credit Note Line
            if (service.Equals("4106")) { docType[0] = "C"; op[0] = "UPT"; }
            else // Document: Update Sales Credit Note Line
            if (service.Equals("4107")) { docType[0] = "C"; op[0] = "DLL"; }
            else // Document: Delete Sales Credit Note Line
            if (service.Equals("4108")) { docType[0] = "C"; op[0] = "PRN"; }
            else // Document: Print Sales Credit Note
            if (service.Equals("4187")) { docType[0] = "C"; op[0] = "FAX"; }
            else // Document: Fax Sales Credit Note

            // Query - SCN
            if (service.Equals("4110")) { docType[0] = "C"; op[0] = "LST"; }
            else // Document: List Sales Credit Notes
            if (service.Equals("4311")) { docType[0] = "C"; op[0] = "ENQ"; }
            else // Report: Sales Credit Notes Monthly Listing

            // Document - SDN
            if (service.Equals("156")) { docType[0] = "N"; op[0] = "PGE"; }
            else // Screen: Sales Debit Note Focus
            if (service.Equals("4111")) { docType[0] = "N"; op[0] = "FTC"; }
            else // Document: Fetch Sales Debit Note
            if (service.Equals("4112")) { docType[0] = "N"; op[0] = "CRT"; }
            else // Document: Create New Sales Debit Note
            if (service.Equals("4113")) { docType[0] = "N"; op[0] = "CHN"; }
            else // Document: Change Sales Debit Note Header
            if (service.Equals("4114")) { docType[0] = "N"; op[0] = "UPD"; }
            else // Document: Update Sales Debit Note Header
            if (service.Equals("4115")) { docType[0] = "N"; op[0] = "AMN"; }
            else // Document: Change Sales Debit Note Line                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               

            if (service.Equals("4116")) { docType[0] = "N"; op[0] = "UPT"; }
            else // Document: Update Sales Debit Note Line
            if (service.Equals("4117")) { docType[0] = "N"; op[0] = "DLL"; }
            else // Document: Delete Sales Debit Note Line
            if (service.Equals("4118")) { docType[0] = "N"; op[0] = "PRN"; }
            else // Document: Print Sales Debit Note
            if (service.Equals("4188")) { docType[0] = "N"; op[0] = "FAX"; }
            else // Document: Fax Sales Debit Note

            // Query - SDN
            if (service.Equals("4120")) { docType[0] = "N"; op[0] = "LST"; }
            else // Document: List Sales Debit Notes
            if (service.Equals("4316")) { docType[0] = "N"; op[0] = "ENQ"; }
            else // Report: Sales Debit Notes Monthly Listing

            // Sales Transactions
            if (service.Equals("183")) { docType[0] = "S"; op[0] = "PGE"; }
            else // Screen: Sales Control
            if (service.Equals("2027")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Processing Sales
            if (service.Equals("2039")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Processing Contract Review
            if (service.Equals("2030")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Processing Sales Manager
            if (service.Equals("2031")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Processing Engineer
            if (service.Equals("2032")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Processing Purchaser
            if (service.Equals("2033")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Processing Scheduler
            if (service.Equals("2034")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Sales Order Processing Coordinator

            if (service.Equals("1007")) { docType[0] = "S"; op[0] = "ENQ"; }
            else // Delivery Status by Sales
            if (service.Equals("107")) { docType[0] = "S"; op[0] = "PGE"; }
            else // Sales Order Tracking Menu

            if (service.Equals("1200")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Sales: Detail
            if (service.Equals("1201")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Sales: Book Orders
            if (service.Equals("1202")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Sales: Closure Analysis
            if (service.Equals("4002")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Customer Settlement History
            if (service.Equals("120")) { docType[0] = ""; op[0] = "PGE"; }
            else // Sales Analytics

            // Document - PO
            if (service.Equals("139")) { docType[0] = "Y"; op[0] = "PGE"; }
            else // Screen: Purchase Order Focus
            if (service.Equals("1035")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Lines Enquiry for a Manufacturer
            if (service.Equals("1036")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Lines Enquiry for a Supplier
            if (service.Equals("1037")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Delivery Performance
            if (service.Equals("5006")) { docType[0] = "Y"; op[0] = "FTC"; }
            else // Document: Fetch Purchase Order
            if (service.Equals("5007")) { docType[0] = "Y"; op[0] = "CRT"; }
            else // Document: Create New Purchase Order
            if (service.Equals("5008")) { docType[0] = "Y"; op[0] = "CHN"; }
            else // Document: Change Purchase Order Header
            if (service.Equals("5009")) { docType[0] = "Y"; op[0] = "UPD"; }
            else // Document: Update Purchase Order Header
            if (service.Equals("5010")) { docType[0] = "Y"; op[0] = "AMN"; }
            else // Document: Change Purchase Order Line
            if (service.Equals("5011")) { docType[0] = "Y"; op[0] = "UPT"; }
            else // Document: Update Purchase Order Line
            if (service.Equals("5012")) { docType[0] = "Y"; op[0] = "DLL"; }
            else // Document: Delete Purchase Order Line
            if (service.Equals("5013")) { docType[0] = "Y"; op[0] = "PRN"; }
            else // Document: Print Purchase Order
            if (service.Equals("5014")) { docType[0] = "Y"; op[0] = "UPT"; }
            else // Document: Set Confirmed Dates for all Lines on a Purchase Order
            if (service.Equals("4190")) { docType[0] = "Y"; op[0] = "FAX"; }
            else // Document: Fax Purchase Order

            // Query - PO
            if (service.Equals("5015")) { docType[0] = "Y"; op[0] = "LST"; }
            else // Document: List Purchase Orders
            if (service.Equals("5072")) { docType[0] = "Y"; op[0] = "PGE"; }
            else // Document: Create Purchase Order from Sales Order
            if (service.Equals("7806")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: PO Lines Without Required-by Date
            if (service.Equals("2022")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Track and Trace

            // Document - LP
            if (service.Equals("152")) { docType[0] = "Z"; op[0] = "PGE"; }
            else // Screen: Local Requisition Focus
            if (service.Equals("5016")) { docType[0] = "Z"; op[0] = "FTC"; }
            else // Document: Fetch Local Requisition
            if (service.Equals("5017")) { docType[0] = "Z"; op[0] = "CRT"; }
            else // Document: Create New Local Requisition
            if (service.Equals("5018")) { docType[0] = "Z"; op[0] = "CHN"; }
            else // Document: Change Local Requisition Header
            if (service.Equals("5019")) { docType[0] = "Z"; op[0] = "UPD"; }
            else // Document: Update Local Requisition Header
            if (service.Equals("5020")) { docType[0] = "Z"; op[0] = "AMN"; }
            else // Document: Change Local Requisition Line
            if (service.Equals("5021")) { docType[0] = "Z"; op[0] = "UPT"; }
            else // Document: Update Local Requisition Line
            if (service.Equals("5022")) { docType[0] = "Z"; op[0] = "DLL"; }
            else // Document: Delete Local Requisition Line
            if (service.Equals("5023")) { docType[0] = "Z"; op[0] = "PRN"; }
            else // Document: Print Local Requisition
            if (service.Equals("4191")) { docType[0] = "Z"; op[0] = "FAX"; }
            else // Document: Fax Local Requisition

            // Query - LP
            if (service.Equals("5025")) { docType[0] = "Z"; op[0] = "LST"; }
            else // Document: List Local Requisitions

            // Document - GRN
            if (service.Equals("153")) { docType[0] = "G"; op[0] = "PGE"; }
            else // Screen: Goods Received Note Focus
            if (service.Equals("3025")) { docType[0] = "G"; op[0] = "FTC"; }
            else // Document: Fetch Goods Received Note
            // Document - PO
            if (service.Equals("139")) { docType[0] = "Y"; op[0] = "PGE"; }
            else // Screen: Purchase Order Focus
            if (service.Equals("1035")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Lines Enquiry for a Manufacturer
            if (service.Equals("1036")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Lines Enquiry for a Supplier
            if (service.Equals("1037")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Delivery Performance
            if (service.Equals("5006")) { docType[0] = "Y"; op[0] = "FTC"; }
            else // Document: Fetch Purchase Order
            if (service.Equals("5007")) { docType[0] = "Y"; op[0] = "CRT"; }
            else // Document: Create New Purchase Order
            if (service.Equals("5008")) { docType[0] = "Y"; op[0] = "CHN"; }
            else // Document: Change Purchase Order Header
            if (service.Equals("5009")) { docType[0] = "Y"; op[0] = "UPD"; }
            else // Document: Update Purchase Order Header
            if (service.Equals("5010")) { docType[0] = "Y"; op[0] = "AMN"; }
            else // Document: Change Purchase Order Line
            if (service.Equals("5011")) { docType[0] = "Y"; op[0] = "UPT"; }
            else // Document: Update Purchase Order Line
            if (service.Equals("5012")) { docType[0] = "Y"; op[0] = "DLL"; }
            else // Document: Delete Purchase Order Line
            if (service.Equals("5013")) { docType[0] = "Y"; op[0] = "PRN"; }
            else // Document: Print Purchase Order
            if (service.Equals("5014")) { docType[0] = "Y"; op[0] = "UPT"; }
            else // Document: Set Confirmed Dates for all Lines on a Purchase Order
            if (service.Equals("4190")) { docType[0] = "Y"; op[0] = "FAX"; }
            else // Document: Fax Purchase Order

            // Query - PO
            if (service.Equals("5015")) { docType[0] = "Y"; op[0] = "LST"; }
            else // Document: List Purchase Orders
            if (service.Equals("5072")) { docType[0] = "Y"; op[0] = "PGE"; }
            else // Document: Create Purchase Order from Sales Order
            if (service.Equals("7806")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: PO Lines Without Required-by Date
            if (service.Equals("2022")) { docType[0] = "Y"; op[0] = "ENQ"; }
            else // Query: Purchase Order Track and Trace

            // Document - LP
            if (service.Equals("152")) { docType[0] = "Z"; op[0] = "PGE"; }
            else // Screen: Local Requisition Focus
            if (service.Equals("5016")) { docType[0] = "Z"; op[0] = "FTC"; }
            else // Document: Fetch Local Requisition
            if (service.Equals("5017")) { docType[0] = "Z"; op[0] = "CRT"; }
            else // Document: Create New Local Requisition
            if (service.Equals("5018")) { docType[0] = "Z"; op[0] = "CHN"; }
            else // Document: Change Local Requisition Header
            if (service.Equals("5019")) { docType[0] = "Z"; op[0] = "UPD"; }
            else // Document: Update Local Requisition Header
            if (service.Equals("5020")) { docType[0] = "Z"; op[0] = "AMN"; }
            else // Document: Change Local Requisition Line
            if (service.Equals("5021")) { docType[0] = "Z"; op[0] = "UPT"; }
            else // Document: Update Local Requisition Line
            if (service.Equals("5022")) { docType[0] = "Z"; op[0] = "DLL"; }
            else // Document: Delete Local Requisition Line
            if (service.Equals("5023")) { docType[0] = "Z"; op[0] = "PRN"; }
            else // Document: Print Local Requisition
            if (service.Equals("4191")) { docType[0] = "Z"; op[0] = "FAX"; }
            else // Document: Fax Local Requisition

            // Query - LP
            if (service.Equals("5025")) { docType[0] = "Z"; op[0] = "LST"; }
            else // Document: List Local Requisitions

            // Document - GRN
            if (service.Equals("153")) { docType[0] = "G"; op[0] = "PGE"; }
            else // Screen: Goods Received Note Focus
            if (service.Equals("3025")) { docType[0] = "G"; op[0] = "FTC"; }
            else // Document: Fetch Goods Received Note
            if (service.Equals("3026")) { docType[0] = "G"; op[0] = "CRT"; }
            else // Document: Create New Goods Received Note
            if (service.Equals("3027")) { docType[0] = "G"; op[0] = "CHN"; }
            else // Document: Change Goods Received Note header
            if (service.Equals("3028")) { docType[0] = "G"; op[0] = "UPD"; }
            else // Document: Update Goods Received Note Header
            if (service.Equals("3029")) { docType[0] = "G"; op[0] = "AMN"; }
            else // Document: Change Goods Received Note Line
            if (service.Equals("3030")) { docType[0] = "G"; op[0] = "UPT"; }
            else // Document: Update Goods Received Note Line
            if (service.Equals("3031")) { docType[0] = "G"; op[0] = "DLL"; }
            else // Document: Delete Goods Received Note Line
            if (service.Equals("3033")) { docType[0] = "G"; op[0] = "PGE"; }
            else // Document: Build Goods Received Note from Purchase Orders
            if (service.Equals("3034")) { docType[0] = "G"; op[0] = "PGE"; }
            else // Document: Build Goods Received Note from Local Requisitions
            if (service.Equals("3035")) { docType[0] = "G"; op[0] = "CRT"; }
            else // Document: Create Purchase Invoice from Goods Received Note
            if (service.Equals("3037")) { docType[0] = "G"; op[0] = "UPD"; }
            else // Document: Update GRN Stock Position

            // Query - GRN
            if (service.Equals("3036")) { docType[0] = "G"; op[0] = "LST"; }
            else // Document: List Goods Received Notes
            if (service.Equals("3024")) { docType[0] = "G"; op[0] = "ENQ"; }
            else // Query: Check Waiting Orders For An Item

            // Document - PI
            if (service.Equals("154")) { docType[0] = "J"; op[0] = "PGE"; }
            else // Screen: Purchase Invoice Focus
            if (service.Equals("5080")) { docType[0] = "J"; op[0] = "FTC"; }
            else // Document: Fetch Purchase Invoice
            if (service.Equals("5081")) { docType[0] = "J"; op[0] = "CRT"; }
            else // Document: Create New Purchase Invoice
            if (service.Equals("5082")) { docType[0] = "J"; op[0] = "CHN"; }
            else // Document: Change Purchase Invoice Header
            if (service.Equals("5083")) { docType[0] = "J"; op[0] = "UPD"; }
            else // Document: Update Purchase Invoice Header
            if (service.Equals("5084")) { docType[0] = "J"; op[0] = "AMN"; }
            else // Document: Change Purchase Invoice Line
            if (service.Equals("5085")) { docType[0] = "J"; op[0] = "UPT"; }
            else // Document: Update Purchase Invoice Line
            if (service.Equals("5086")) { docType[0] = "J"; op[0] = "DLL"; }
            else // Document: Delete Purchase Invoice Line

            // Query - PI
            if (service.Equals("5088")) { docType[0] = "J"; op[0] = "LST"; }
            else // Document: List Purchase Invoices
            if (service.Equals("4314")) { docType[0] = "J"; op[0] = "ENQ"; }
            else // Report: Purchase Invoices Monthly Listing
            if (service.Equals("2040")) { docType[0] = "J"; op[0] = "ENQ"; }
            else // Query: Purchase Invoice Track and Trace

            // Document - PCN
            if (service.Equals("162")) { docType[0] = "F"; op[0] = "PGE"; }
            else // Screen: Purchase Credit Note Focus
            if (service.Equals("5026")) { docType[0] = "F"; op[0] = "FTC"; }
            else // Document: Fetch Purchase Credit Note
            if (service.Equals("5027")) { docType[0] = "F"; op[0] = "CRT"; }
            else // Document: Create New Purchase Credit Note
            if (service.Equals("5028")) { docType[0] = "F"; op[0] = "CHN"; }
            else // Document: Change Purchase Credit Note Header
            if (service.Equals("5029")) { docType[0] = "F"; op[0] = "UPD"; }
            else // Document: Update Purchase Credit Note Header
            if (service.Equals("5030")) { docType[0] = "F"; op[0] = "AMN"; }
            else // Document: Change Purchase Credit Note Line
            if (service.Equals("5031")) { docType[0] = "F"; op[0] = "UPT"; }
            else // Document: Update Purchase Credit Note Line
            if (service.Equals("5032")) { docType[0] = "F"; op[0] = "DLL"; }
            else // Document: Delete Purchase Credit Note Line

            // Query - PCN
            if (service.Equals("5035")) { docType[0] = "F"; op[0] = "LST"; }
            else // Document: List Purchase Credit Notes
            if (service.Equals("4315")) { docType[0] = "F"; op[0] = "ENQ"; }
            else // Report: Purchase Credit Notes Monthly Listing

            // Document - PDN
            if (service.Equals("163")) { docType[0] = "H"; op[0] = "PGE"; }
            else // Screen: Purchase Debit Note Focus
            if (service.Equals("5036")) { docType[0] = "H"; op[0] = "FTC"; }
            else // Document: Fetch Purchase Debit Note
            if (service.Equals("5037")) { docType[0] = "H"; op[0] = "CRT"; }
            else // Document: Create New Purchase Debit Note
            if (service.Equals("5038")) { docType[0] = "H"; op[0] = "CHN"; }
            else // Document: Change Purchase Debit Note Header
            if (service.Equals("5039")) { docType[0] = "H"; op[0] = "UPD"; }
            else // Document: Update Purchase Debit Note Header
            if (service.Equals("5040")) { docType[0] = "H"; op[0] = "AMN"; }
            else // Document: Change Purchase Debit Note Line
            if (service.Equals("5041")) { docType[0] = "H"; op[0] = "UPT"; }
            else // Document: Update Purchase Debit Note Line
            if (service.Equals("5042")) { docType[0] = "H"; op[0] = "DLL"; }
            else // Document: Delete Purchase Debit Note Line

            // Query - PDN
            if (service.Equals("5045")) { docType[0] = "H"; op[0] = "LST"; }
            else // Document: List Purchase Debit Notes
            if (service.Equals("4317")) { docType[0] = "H"; op[0] = "ENQ"; }
            else // Report: Purchase Debit Notes Monthly Listing

            // Purchases Transactions
            if (service.Equals("1028")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Word Search Purchasing Document Lines
            if (service.Equals("1034")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Query: Purchase Invoice Listing Enquiry by Date
            if (service.Equals("1032")) { docType[0] = ""; op[0] = "PGE"; }
            else // Purchasing Document Lines Enquiry
            if (service.Equals("122")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Purchasing Analytics Menu
            if (service.Equals("5002")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Creditor Settlement History
            if (service.Equals("182")) { docType[0] = ""; op[0] = "PGE"; }
            else // Purchasing Control

            // Accounts Transactions
            if (service.Equals("105")) { docType[0] = ""; op[0] = "PGE"; }
            else // Accounts Year Selection
            if (service.Equals("109")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Accounts Services
            if (service.Equals("106")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Debtor Settlement Services
            if (service.Equals("133")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Creditor Settlement Services
            if (service.Equals("6032")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Verification Services
            if (service.Equals("110")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Accounts Analytics Charts
            if (service.Equals("6002")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Bank Reconciliation
            if (service.Equals("6003")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: GST Reconciliation
            if (service.Equals("6006")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Detail WAC Derivation
            if (service.Equals("6007")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Account
            if (service.Equals("6008")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Account
            if (service.Equals("6015")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Listing: Chart of Accounts
            if (service.Equals("6016")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Modify Chart of Accounts
            if (service.Equals("6027")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Listing: Journal Batch
            if (service.Equals("6022")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Modify Journal Batches
            if (service.Equals("6023")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Modify Journal Batches
            if (service.Equals("6045")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Verify Journal Batches
            if (service.Equals("6028")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View the General Ledger
            if (service.Equals("6029")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View the Debtors Ledger
            if (service.Equals("6030")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View the Creditors Ledger
            if (service.Equals("6031")) { docType[0] = ""; op[0] = "PGE"; }
            else // Listing: General Ledger
            if (service.Equals("6053")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Report: Trial Balance
            if (service.Equals("6054")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Report: Balance Sheet
            if (service.Equals("6055")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Report: Profit and Loss
            if (service.Equals("1101")) { docType[0] = ""; op[0] = "CHR"; }
            else // Chart: Profit and Loss
            if (service.Equals("1102")) { docType[0] = ""; op[0] = "CHR"; }
            else // Chart: Balance Sheet
            if (service.Equals("2037")) { docType[0] = ""; op[0] = "PGE"; }
            else // Control: Sales Order Processing Invoicer
            if (service.Equals("6020")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Year Closing and Starting
            if (service.Equals("6033")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Stock Reconciliation Analysis
            if (service.Equals("6034")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Debtors Opening Balances Verification
            if (service.Equals("6035")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Creditors Opening Balances Verification
            if (service.Equals("6036")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Work-in-Progress Analysis
            if (service.Equals("6037")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Stock-in-Transit Analysis
            if (service.Equals("108")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Self-Audit Validation

            // Accounts - Definitions
            if (service.Equals("7067")) { docType[0] = ""; op[0] = "PGE"; }
            else // Definitions: Bank Accounts
            if (service.Equals("7054")) { docType[0] = ""; op[0] = "PGE"; }
            else // Definitions: Currencies
            if (service.Equals("7055")) { docType[0] = ""; op[0] = "PGE"; }
            else // Definitions: Currency Rates
            if (service.Equals("7061")) { docType[0] = ""; op[0] = "PGE"; }
            else // Definitions: GST Rates

            // Accounts - Payables
            if (service.Equals("165")) { docType[0] = "A"; op[0] = "PGE"; }
            else // Payables Selection
            if (service.Equals("5047")) { docType[0] = "A"; op[0] = "PGE"; }
            else // Process: Payables Builder
            if (service.Equals("164")) { docType[0] = "A"; op[0] = "PGE"; }
            else // Screen: Payment Record Focus
            if (service.Equals("5049")) { docType[0] = "A"; op[0] = "FTC"; }
            else // fetch payment record
            if (service.Equals("5050")) { docType[0] = "A"; op[0] = "CRT"; }
            else // Record: Create New Payment Record
            if (service.Equals("5051")) { docType[0] = "A"; op[0] = "CHN"; }
            else // Record: Change Payment Record Header
            if (service.Equals("5052")) { docType[0] = "A"; op[0] = "UPD"; }
            else // Record: Update Payment Record Header
            if (service.Equals("5053")) { docType[0] = "A"; op[0] = "AMN"; }
            else // Record: Change Payment Record Line
            if (service.Equals("5054")) { docType[0] = "A"; op[0] = "UPT"; }
            else // Record: Update Payment Record Line
            if (service.Equals("5055")) { docType[0] = "A"; op[0] = "DLL"; }
            else // Record: Delete Payment Record Line
            if (service.Equals("5056")) { docType[0] = "A"; op[0] = "PRN"; }
            else // Record: Print Payment Advice
            if (service.Equals("5057")) { docType[0] = "A"; op[0] = "LST"; }
            else // Record: List Payment Records
            if (service.Equals("4192")) { docType[0] = "A"; op[0] = "FAX"; }
            else // Record: Fax Payment Advice
            if (service.Equals("4312")) { docType[0] = "A"; op[0] = "ENQ"; }
            else // Listing: Payments Monthly

            // Accounts - Receivables
            if (service.Equals("158")) { docType[0] = "R"; op[0] = "PGE"; }
            else // Receivables Selection
            if (service.Equals("4203")) { docType[0] = "R"; op[0] = "PGE"; }
            else // Process: Receivables Builder
            if (service.Equals("157")) { docType[0] = "R"; op[0] = "PGE"; }
            else // Screen: Receipt Record Focus
            if (service.Equals("4205")) { docType[0] = "R"; op[0] = "FTC"; }
            else // Record: Fetch Receipt Record
            if (service.Equals("4206")) { docType[0] = "R"; op[0] = "CRT"; }
            else // Record: Create New Receipt Record
            if (service.Equals("4207")) { docType[0] = "R"; op[0] = "CHN"; }
            else // Record: Change Receipt Record Header
            if (service.Equals("4208")) { docType[0] = "R"; op[0] = "UPD"; }
            else // Record: Update Receipt Record Header
            if (service.Equals("4209")) { docType[0] = "R"; op[0] = "AMN"; }
            else // Record: Change Receipt Record Line
            if (service.Equals("4210")) { docType[0] = "R"; op[0] = "UPT"; }
            else // Record: Update Receipt Record Line
            if (service.Equals("4211")) { docType[0] = "R"; op[0] = "DLL"; }
            else // Record: Delete Receipt Record Line
            if (service.Equals("4212")) { docType[0] = "R"; op[0] = "PRN"; }
            else // Record: Print Receipt
            if (service.Equals("4213")) { docType[0] = "R"; op[0] = "LST"; }
            else // Record: List Receipt Records
            if (service.Equals("4189")) { docType[0] = "R"; op[0] = "FAX"; }
            else // Record: Fax Receipt
            if (service.Equals("4310")) { docType[0] = "R"; op[0] = "ENQ"; }
            else // Listing: Receipts Monthly Listing

            // Accounts - IAT
            if (service.Equals("161")) { docType[0] = "T"; op[0] = "PGE"; }
            else // Screen: Inter-Account Transfer Focus
            if (service.Equals("6077")) { docType[0] = "T"; op[0] = "FTC"; }
            else // Record: Fetch InterAccount Transfer
            if (service.Equals("6078")) { docType[0] = "T"; op[0] = "CRT"; }
            else // Record: Create New InterAccount Transfer
            if (service.Equals("6079")) { docType[0] = "T"; op[0] = "CHN"; }
            else // Record: Change InterAccount Transfer Header
            if (service.Equals("6080")) { docType[0] = "T"; op[0] = "UPD"; }
            else // Record: Update InterAccount Transfer Header

            // Query - IAT
            if (service.Equals("6085")) { docType[0] = "T"; op[0] = "LST"; }
            else // Record: List InterAccount Transfers

            // Accounts - RV
            if (service.Equals("159")) { docType[0] = "U"; op[0] = "PGE"; }
            else // Screen: Receipt Voucher Focus
            if (service.Equals("6056")) { docType[0] = "U"; op[0] = "FTC"; }
            else // Record: Fetch Receipt Voucher
            if (service.Equals("6057")) { docType[0] = "U"; op[0] = "CRT"; }
            else // Record: Create New Receipt Voucher
            if (service.Equals("6058")) { docType[0] = "U"; op[0] = "CHN"; }
            else // Record: Change Receipt Voucher Header
            if (service.Equals("6059")) { docType[0] = "U"; op[0] = "UPD"; }
            else // Record: Update Receipt Voucher Header
            if (service.Equals("6060")) { docType[0] = "U"; op[0] = "AMN"; }
            else // Record: Change Receipt Voucher Line
            if (service.Equals("6061")) { docType[0] = "U"; op[0] = "UPT"; }
            else // Record: Update Receipt Voucher Line
            if (service.Equals("6062")) { docType[0] = "U"; op[0] = "DLL"; }
            else // Record: Delete Receipt Voucher Line

            // Query - RV
            if (service.Equals("6064")) { docType[0] = "U"; op[0] = "LST"; }
            else // Record: List Receipt Vouchers
            if (service.Equals("4318")) { docType[0] = "U"; op[0] = "ENQ"; }
            else // Listing: Receipt Vouchers Monthly

            // Accounts - PV
            if (service.Equals("160")) { docType[0] = "V"; op[0] = "PGE"; }
            else // Screen: Payment Voucher Focus
            if (service.Equals("6066")) { docType[0] = "V"; op[0] = "FTC"; }
            else // Record: Fetch Payment Voucher
            if (service.Equals("6067")) { docType[0] = "V"; op[0] = "CRT"; }
            else // Record: Create New Payment Voucher
            if (service.Equals("6068")) { docType[0] = "V"; op[0] = "CHN"; }
            else // Record: Change Payment Voucher Header
            if (service.Equals("6069")) { docType[0] = "V"; op[0] = "UPD"; }
            else // Record: Update Payment Voucher Header
            if (service.Equals("6070")) { docType[0] = "V"; op[0] = "AMN"; }
            else // Record: Change Payment Voucher Line
            if (service.Equals("6071")) { docType[0] = "V"; op[0] = "UPT"; }
            else // Record: Update Payment Voucher Line
            if (service.Equals("6072")) { docType[0] = "V"; op[0] = "DLL"; }
            else // Record: Delete Payment Voucher Line
            if (service.Equals("6073")) { docType[0] = "V"; op[0] = "PRN"; }
            else // Record: Print Payment Voucher

            // Query - PV
            if (service.Equals("6074")) { docType[0] = "V"; op[0] = "LST"; }
            else // Record: List Payment Vouchers
            if (service.Equals("4319")) { docType[0] = "V"; op[0] = "ENQ"; }
            else // Listing: Payment Vouchers Monthly

            // Definitions
            if (service.Equals("115")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Definition Services

            if (service.Equals("7001")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: SalesPeople
            if (service.Equals("7062")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Configuration
            if (service.Equals("7058")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Company Types
            if (service.Equals("7057")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Countries
            if (service.Equals("7031")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Document Codes
            if (service.Equals("7059")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Industry Types
            if (service.Equals("7063")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Likelihood Ratings
            if (service.Equals("7064")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Quotation Status
            if (service.Equals("7065")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Quotation Reasons
            if (service.Equals("7060")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Stores
            if (service.Equals("7070")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Document Print Options

            // Admin
            if (service.Equals("117")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Administration System Services

            if (service.Equals("7029")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Set System Status
            if (service.Equals("7049")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Modify Site Style
            if (service.Equals("7081")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Edit Site Styling

            if (service.Equals("111")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Administration Services
            if (service.Equals("188")) { docType[0] = ""; op[0] = "ENQ"; }
            else // About
            if (service.Equals("129")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Location Services

            if (service.Equals("112")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Administration User Services

            if (service.Equals("7008")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: User Modules
            if (service.Equals("7009")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Casual, Registered, and Demo User Services
            if (service.Equals("7003")) { docType[0] = ""; op[0] = "OPU"; }
            else // Modify Casual & Registered User Access
            if (service.Equals("7004")) { docType[0] = ""; op[0] = "OPU"; }
            else // List Detailed Access Rights
            if (service.Equals("7074")) { docType[0] = ""; op[0] = "OPU"; }
            else // Definitions: Change User Style
            if (service.Equals("7051")) { docType[0] = ""; op[0] = "OPU"; }
            else // User Group Manager
            if (service.Equals("7030")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Access Log Manager
            if (service.Equals("7034")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Access Services Manager

            // Contacts
            if (service.Equals("8801")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Contacts: Address Book
            if (service.Equals("8802")) { docType[0] = ""; op[0] = "ENQ"; }
            else // View Others' Contacts
            if (service.Equals("8803")) { docType[0] = ""; op[0] = "OPU"; }
            else // Sharing
            if (service.Equals("8804")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Show Contacts for a Company
            if (service.Equals("8805")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Scan Contacts File for a Company
            if (service.Equals("8806")) { docType[0] = ""; op[0] = "ENQ"; }
            else // List All Companies' Contacts
            if (service.Equals("8811")) { docType[0] = ""; op[0] = "OPU"; }
            else // Contacts: Add New Contact
            if (service.Equals("8812")) { docType[0] = ""; op[0] = "UPL"; }
            else // Contacts: Upload Address Book
            if (service.Equals("8816")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Create External Access
            if (service.Equals("8817")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: List/Select External Access
            if (service.Equals("8818")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Delete External Access
            if (service.Equals("8819")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Approval Rights External Access
            if (service.Equals("8821")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Contact Details
            if (service.Equals("8830")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Profile: List all Profiles
            if (service.Equals("8831")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Profile: View My Profile
            if (service.Equals("8841")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Contacts: Services
            if (service.Equals("8860")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Organization Definition

            // Fax
            if (service.Equals("11000")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Outgoing Faxes
            if (service.Equals("11002")) { docType[0] = ""; op[0] = "PGE"; }
            else // Send a Fax
            if (service.Equals("11003")) { docType[0] = ""; op[0] = "ENQ"; }
            else // View Historical Faxes
            if (service.Equals("11004")) { docType[0] = ""; op[0] = "OPU"; }
            else // Abort an Outgoing Fax
            if (service.Equals("11005")) { docType[0] = ""; op[0] = "OPU"; }
            else // ReSend an Historical Fax
            if (service.Equals("11006")) { docType[0] = ""; op[0] = "PGE"; }
            else // Compose a Fax
            if (service.Equals("11011")) { docType[0] = ""; op[0] = "OPU"; }
            else // Access Fax Options

            // Document Library
            if (service.Equals("12000")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: View Directory Contents
            if (service.Equals("12001")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Edit Document Details
            if (service.Equals("12002")) { docType[0] = ""; op[0] = "UPL"; }
            else // Process: Upload a File
            if (service.Equals("12003")) { docType[0] = ""; op[0] = "DNL"; }
            else // Process: Download a File
            if (service.Equals("12004")) { docType[0] = ""; op[0] = "DNL"; }
            else // Process: Check-out a File
            if (service.Equals("12005")) { docType[0] = ""; op[0] = "UPL"; }
            else // Process: Check-in a File
            if (service.Equals("12006")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Archive
            if (service.Equals("12008")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Directory Insert
            if (service.Equals("12009")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Directory Delete
            if (service.Equals("12010")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Directory List
            if (service.Equals("12011")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Options Access
            if (service.Equals("12013")) { docType[0] = ""; op[0] = "DNL"; }
            else // Process: External User Download
            if (service.Equals("12014")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View All Directories
            if (service.Equals("12015")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Properties
            if (service.Equals("12016")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Manage a Document

            // Casual
            if (service.Equals("801")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Main Page
            if (service.Equals("802")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Products Page
            if (service.Equals("803")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Transactions Page
            if (service.Equals("804")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Registration Page
            if (service.Equals("808")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: Catalogs Page
            if (service.Equals("811")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Cart

            // Registered
            if (service.Equals("901")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Main Page
            if (service.Equals("902")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Products Page
            if (service.Equals("903")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Transactions Page
            if (service.Equals("904")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Accounts Page
            if (service.Equals("905")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Reports Page
            if (service.Equals("906")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Registration Page
            if (service.Equals("907")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Projects Page
            if (service.Equals("908")) { docType[0] = ""; op[0] = "PGE"; }
            else // Process: Catalogs Page
            if (service.Equals("909")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Sales Document Word Search
            if (service.Equals("911")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Cart
            if (service.Equals("914")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Edit Checkout Information
            if (service.Equals("915")) { docType[0] = ""; op[0] = "UPD"; }
            else // Process: Save to Inbox
            if (service.Equals("4121")) { docType[0] = ""; op[0] = "FTC"; }
            else // Document: View a Sales Order
            if (service.Equals("4122")) { docType[0] = ""; op[0] = "FTC"; }
            else // Document: View a Quotation
            if (service.Equals("4126")) { docType[0] = ""; op[0] = "FTC"; }
            else // Document: View an Order Confirmation
            if (service.Equals("4127")) { docType[0] = ""; op[0] = "FTC"; }
            else // Document: View an Order Acknowledgement
            if (service.Equals("4123")) { docType[0] = ""; op[0] = "FTC"; }
            else // Document: View a Delivery Order
            if (service.Equals("4124")) { docType[0] = ""; op[0] = "FTC"; }
            else // Document: View a Sales Invoice
            if (service.Equals("4125")) { docType[0] = ""; op[0] = "FTC"; }
            else // Document: View a Proforma Invoice
            if (service.Equals("1016")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Generate a Statement of Account
            if (service.Equals("2012")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Query: ExtUsers Transaction Track & Trace

            // Transactions - Stock
            if (service.Equals("118")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Screen: Stock Charts Services Menu
            if (service.Equals("180")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Screen: Stock Control Menu
            if (service.Equals("102")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Screen: Product Services Menu Screen

            if (service.Equals("3002")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // View Stock Item Purchase Prices
            if (service.Equals("3059")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Process: Get Stock Pendings
            if (service.Equals("1003")) { docType[0] = "3"; op[0] = "CHR"; }
            else // Chart: Stock Purchasing vs. Sales
            if (service.Equals("1014")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Report: Stock Usage Enquiry for Customer
            if (service.Equals("1017")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Report: Stock Usage Enquiry for Supplier
            if (service.Equals("1021")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Stock ReOrder Generation
            if (service.Equals("1022")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Report: Manufacturer Sales by Customer
            if (service.Equals("3014")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Report: Stock Usage Spread for a Manufacturer
            if (service.Equals("7802")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Report: Stock Requirements Status
            if (service.Equals("3010")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Listing: Print Stock Records List
            if (service.Equals("3062")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Report: Stock Status
            if (service.Equals("3063")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Report: Stock Status Valuation Differences
            if (service.Equals("2020")) { docType[0] = "3"; op[0] = "LST"; }
            else // Paged Stock Listing
            if (service.Equals("3057")) { docType[0] = "3"; op[0] = "DNL"; }
            else // Process: Stock File Download
            if (service.Equals("3058")) { docType[0] = "3"; op[0] = "UPL"; }
            else // Process: Stock File Upload Update
            if (service.Equals("3053")) { docType[0] = "3"; op[0] = "OPU"; }
            else // Process: Update Stock Prices
            if (service.Equals("3061")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Query: Stock Prices Out-of-Date

            // Queries - Stock
            if (service.Equals("1001")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Query: Stock Enquiry
            if (service.Equals("1002")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Query: Stock History Enquiry
            if (service.Equals("3052")) { docType[0] = "3"; op[0] = "ENQ"; }
            else // Query: Stock Trace
            if (service.Equals("2001")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Query: Fast Fetch by Our Stock Code
            if (service.Equals("2002")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Query: Word Search of Stock File
            if (service.Equals("2006")) { docType[0] = "3"; op[0] = "LST"; }
            else // Select by Our Stock Listings
            if (service.Equals("2008")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Search by Manufacturer (List)

            // Records - Stock
            if (service.Equals("144")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Screen: Stock Records Focus Screen
            if (service.Equals("3001")) { docType[0] = "3"; op[0] = "FTC"; }
            else // Record: Fetch Stock Record
            if (service.Equals("3003")) { docType[0] = "3"; op[0] = "CRT"; }
            else // Record: Create new Stock Record
            if (service.Equals("3004")) { docType[0] = "3"; op[0] = "CHN"; }
            else // Record: Change Stock Record
            if (service.Equals("3005")) { docType[0] = "3"; op[0] = "UPD"; }
            else // Record: Update Stock Record
            if (service.Equals("3006")) { docType[0] = "3"; op[0] = "AMN"; }
            else // Record: Change Stock Record Store Line
            if (service.Equals("3007")) { docType[0] = "3"; op[0] = "UPT"; }
            else // Record: Update Stock Record Store Line
            if (service.Equals("3008")) { docType[0] = "3"; op[0] = "DLL"; }
            else // Record: Delete Stock Record Store Line

            // DataBase Updating - Stock
            if (service.Equals("125")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Screen: Stock Updating Services

            if (service.Equals("3009")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Record: Replace Stock Records
            if (service.Equals("3015")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Record: Replace Stock Records by Upload
            if (service.Equals("3016")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Record: Remove a Stock Record
            if (service.Equals("3017")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Record: Remove Stock Records by Upload
            if (service.Equals("3022")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Record: Add Stock Records by Upload
            if (service.Equals("3023")) { docType[0] = "3"; op[0] = "PGE"; }
            else // Record: Renumber a Stock Record

            // Stock Transactions

            // Catalogs
            if (service.Equals("114")) { docType[0] = "4"; op[0] = "PGE"; }
            else // Screen: Catalog Services Menu

            if (service.Equals("2000")) { docType[0] = "4"; op[0] = "ENQ"; }
            else // Process: Stock Availability
            if (service.Equals("2003")) { docType[0] = "4"; op[0] = "ENQ"; }
            else // Process: Browse Catalogs
            if (service.Equals("2014")) { docType[0] = "4"; op[0] = "ENQ"; }
            else // Process: Browse Buyers Catalog
            if (service.Equals("2004")) { docType[0] = "4"; op[0] = "ENQ"; }
            else // Process: Browse WCF-Linked Catalog
            if (service.Equals("2005")) { docType[0] = "4"; op[0] = "ENQ"; }
            else // Process: Browse Non-WCF-Linked Catalog
            if (service.Equals("2010")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Import WCF  Catalog
            if (service.Equals("2015")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Export WCF Catalog
            if (service.Equals("2011")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Manage Linked Catalogs
            if (service.Equals("7024")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Maintain Buyers Catalog
            if (service.Equals("7078")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Scan Documents to Create Buyers  Catalog
            if (service.Equals("2038")) { docType[0] = "4"; op[0] = "CNV"; }
            else // Process: Print Catalog as PDF
            if (service.Equals("7071")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Definitions: Stock Category
            if (service.Equals("7072")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Categorize Stock for Catalogs
            if (service.Equals("7079")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Set Show-to-Web for a Manufacturer
            if (service.Equals("7032")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Process: Modify  Catalogs List Page
            if (service.Equals("7006")) { docType[0] = "4"; op[0] = "ENQ"; }
            else // Process: Scan for Published  Catalogs
            if (service.Equals("2007")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Operation: Let Internal users see the Cost Price
            if (service.Equals("2009")) { docType[0] = "4"; op[0] = "OPU"; }
            else // Operation: Let Internal Users see the RRP (List) Price

            if (service.Equals("6100")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: View Directory Contents
            if (service.Equals("6101")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: List Directories
            if (service.Equals("6102")) { docType[0] = ""; op[0] = "UPL"; }
            else // Process: File Upload
            if (service.Equals("6103")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Edit Text
            if (service.Equals("6107")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Delete Text
            if (service.Equals("6108")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Insert Directory
            if (service.Equals("6109")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Delete Directory

            // Stock - Adjustment
            if (service.Equals("167")) { docType[0] = "5"; op[0] = "PGE"; }
            else // Screen: Stock Adjustment Focus

            if (service.Equals("3011")) { docType[0] = "5"; op[0] = "CHN"; }
            else // Record: Stock Adjustment Edit for an Item
            if (service.Equals("3012")) { docType[0] = "5"; op[0] = "CRT"; }
            else // Record: Stock Adjustment Record Entry
            if (service.Equals("3013")) { docType[0] = "5"; op[0] = "LST"; }
            else // Record: List Adjustment Records

            // Stock - Check
            if (service.Equals("166")) { docType[0] = "6"; op[0] = "PGE"; }
            else // Screen: Stock Check Focus

            if (service.Equals("3018")) { docType[0] = "6"; op[0] = "LST"; }
            else // Record: List Stock Check Records for an Item
            if (service.Equals("3019")) { docType[0] = "6"; op[0] = "CRT"; }
            else // Record: Stock Check Record Entry
            if (service.Equals("3020")) { docType[0] = "6"; op[0] = "LST"; }
            else // Record: List Stock Check Records
            if (service.Equals("3021")) { docType[0] = "6"; op[0] = "PGE"; }
            else // Record: Stock Check Import
            if (service.Equals("3064")) { docType[0] = "6"; op[0] = "ENQ"; }
            else // Record: List Incomplete Picking Lists and GRNs
            if (service.Equals("3065")) { docType[0] = "6"; op[0] = "ENQ"; }
            else // Process: Stock Check Reconciliation

            // Users
            if (service.Equals("128")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Company Personnel Directory

            //
            if (service.Equals("1025")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Document Trace (Cyclical)
            if (service.Equals("1026")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Document Trace (Forward)
            if (service.Equals("6094")) { docType[0] = ""; op[0] = "UPD"; }
            else // Process: Modify Document Attachments
            if (service.Equals("11200")) { docType[0] = ""; op[0] = "CNV"; }
            else // Document: Convert a Document to PDF
            if (service.Equals("11300")) { docType[0] = ""; op[0] = "CNV"; }
            else // Document: Convert a Document to CSV
            if (service.Equals("11800")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Document: Display a Document Access Trail
            if (service.Equals("11801")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Document: Display an Audit Trail
            if (service.Equals("1008")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Word Search Sales Document Lines
            if (service.Equals("1030")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Sales Document Lines Enquiry
            if (service.Equals("6092")) { docType[0] = ""; op[0] = "UPT"; }
            else // Document: Change GST Rates for a Document
            if (service.Equals("6093")) { docType[0] = ""; op[0] = "UPT"; }
            else // Document: Change Prices for a Document
            if (service.Equals("6096")) { docType[0] = ""; op[0] = "UPT"; }
            else // Document: Change Stores for a Document
            if (service.Equals("6098")) { docType[0] = ""; op[0] = "UPT"; }
            else // Document: Change Discounts for a Document
            if (service.Equals("121")) { docType[0] = ""; op[0] = "UPT"; }
            else // Process: Process: Add to Cart
            if (service.Equals("1018")) { docType[0] = ""; op[0] = "OPU"; }       // Process: Remove Report

            // DataBase Admin
            if (service.Equals("7052")) { docType[0] = ""; op[0] = "PGE"; }
            else // DataBase Tables Manager

            if (service.Equals("7048")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: DataBase Statistics
            if (service.Equals("7076")) { docType[0] = ""; op[0] = "OPU"; }
            else // Process: Clean Customer PO Codes
            if (service.Equals("7077")) { docType[0] = ""; op[0] = "ENQ"; }
            else // Process: Analyze Customer PO Codes
            if (service.Equals("116")) { docType[0] = ""; op[0] = "PGE"; }
            else // Screen: DataBase Services
            if (service.Equals("7033")) { docType[0] = ""; op[0] = "PGE"; }       // Screen: DataBase Locking
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void ForEachSOOnDO(MySqlConnection con, String doCode, String userCode)
        {
            MySqlDataReader dataReader = null;
            try
            {
                string query = "SELECT SOCode FROM dol WHERE DOCode = '" + doCode + "' AND SOCode != ''";

                String soCode, waveID;

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                while (dataReader.Read())
                {
                    soCode = Convert.ToString(dataReader["SOCode"]);
                    waveID = GetWaveIDForDoc(con, soCode, 'S');

                    if (! AlreadyOnWave(con, doCode, "D", waveID))
                        AddToWave(con, waveID, userCode, "D", doCode);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ForEachSOOnDO: " + e);
            }
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool AlreadyOnWave(MySqlConnection con, string docCode, string docType, string waveID)
        {
            bool res = false;

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT WaveID FROM waveletc WHERE DocCode = '" + generalUtils.SanitiseForSQL(docCode) + "' AND DocType = '" + docType + "' AND WaveID = '" + waveID + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                res |= dataReader.Read();

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: AlreadyOnWave(): " + e);
            }

            return res;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string GetSOCodeGivenPLCode(MySqlConnection con, string plCode)
        {
            string soCode = "";

            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT SOCode FROM pll WHERE PLCode = '" + plCode + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    soCode = Convert.ToString(dataReader["SOCode"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("GetSOCodeGivenPLCode: " + e);
            }

            return soCode;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        void CreateNewWavechannelsRec(MySqlConnection con, string waveID, string custCode, string waveDated, string DNM)
        {
            try
            {
                string channelID = custCode;

                if (!generalUtils.IsInteger(channelID))
                    channelID = "0";

                string query = "INSERT INTO wavechannels (WaveID, ChannelID, DNM, WaveDated, WaveType, ChannelType) VALUES ('" + waveID + "','" + channelID + "','" + DNM + "','" + waveDated + "','c','C')";
 
                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("CreateNewWavechannelsRec: " + e);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void UpdateNewWavechannelsRec(MySqlConnection con, string docCode, string docType, string custCode)
        {
            try
            {
                string waveID = GetWaveID(con, docCode, docType);

                string channelID = GetChannelID(con, waveID);

                string newChannelID = custCode;

                if (!generalUtils.IsInteger(newChannelID))
                    newChannelID = "0";

                if (channelID.Equals(newChannelID))
                    return;

                string query = "UPDATE wavechannels SET ChannelID = '" + newChannelID + "' WHERE WaveID = '" + waveID + "' AND ChannelID = '" + channelID + "'";

                MySqlCommand cmd = new MySqlCommand(query, con);

                cmd.ExecuteNonQuery();
            }
            catch (Exception e)
            {
                Console.WriteLine("UpdateNewWavechannelsRec: " + e);
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string GetChannelID(MySqlConnection con, string waveID)
        {
            string channelID = "";
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT ChannelID FROM wavechannels WHERE WaveID = '" + waveID + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    channelID = Convert.ToString(dataReader["ChannelID"]);
                }
  
                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: GetChannelID(): " + e);
                return "";
            }

            return channelID;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        string GetWaveID(MySqlConnection con, string docCode, string docType)
        {
            string waveID = "";
            MySqlDataReader dataReader = null;

            try
            {
                string query = "SELECT WaveID FROM waveletc WHERE DocCode = '" + generalUtils.SanitiseForSQL(docCode) + "' AND DocType = '" + docType + "' LIMIT 1";

                MySqlCommand cmd = new MySqlCommand(query, con);

                dataReader = cmd.ExecuteReader();

                if (dataReader.Read())
                {
                    waveID = Convert.ToString(dataReader["WaveID"]);
                }

                dataReader.Close();
            }
            catch (Exception e)
            {
                if (dataReader != null) dataReader.Close();
                Console.WriteLine("ServerUtils: GetWaveID(): " + e);
                return "";
            }

            return waveID;
        }
    }
}