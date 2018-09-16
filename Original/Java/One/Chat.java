// =======================================================================================================================================================================================================
// System: ZaraStar Chat: Record Access
// Module: inbox.java
// Author: C.K.Harvey
// Copyright (c) 2001-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class Chat
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChat() throws Exception
  {
    return "chat ( MsgID integer not null AUTO_INCREMENT, DateTime timestamp, ToGroup char(40), FromUser char(40), ToUser char(40), "
                + "Msg mediumtext, Status char(1), unique(MsgID))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChat(String[] s) throws Exception
  {
    s[0] = "chatToUserInx on chat (ToUser)";
    s[1] = "chatStatusInx on chat (Status)";

    return 2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChat() throws Exception
  {
    return "MsgID, DateTime, ToGroup, FromUser, ToUser, Msg, Status";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChat() throws Exception
  {
    return "CDCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChat(short[] sizes) throws Exception
  {
    sizes[0] = 0;  sizes[1] = 0;  sizes[2] = 40;  sizes[3] = 40;  sizes[4] = 40;  sizes[5] = 0;  sizes[2] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChat() throws Exception
  {
    return "MOOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChatUnRead() throws Exception
  {
    return "chatunread ( MsgID integer not null, ToUser char(40), ToGroup char(40), ReadOnce char(1), unique(MsgID, ToUser))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChatUnRead(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChatUnRead() throws Exception
  {
    return "MsgID, ToUser, ToGroup, ReadOnce";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChatUnRead() throws Exception
  {
    return "CCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChatUnRead(short[] sizes) throws Exception
  {
    sizes[0] = 0;  sizes[1] = 40;  sizes[2] = 40;  sizes[3] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChatUnRead() throws Exception
  {
    return "MMMO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChannels() throws Exception
  {
    return "channels ( Name char(60), Purpose char(250), RegisterOnSC char(1), Type char(1), Status char(1), Deleteable char(1), unique(Name))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChannels(String[] s) throws Exception
  {
    s[0] = "\000";
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChannels() throws Exception
  {
    return "Name, Purpose, RegisterOnSC, Type, Status, Deleteable";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChannels() throws Exception
  {
    return "CCCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChannels(short[] sizes) throws Exception
  {
    sizes[0] = 60;  sizes[1] = 250;  sizes[2] = 1;  sizes[3] = 1;  sizes[4] = 1;  sizes[5] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChannels() throws Exception
  {
    return "MOOOOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChannelsOpen() throws Exception
  {
    return "channelsopen ( UserCode char(20), Name char(60), Type char(1), Host char(100), unique(UserCode, Name, Type, Host))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChannelsOpen(String[] s) throws Exception
  {
    s[0] = "\000";
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChannelsOpen() throws Exception
  {
    return "UserCode, Name, Type, Host";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChannelsOpen() throws Exception
  {
    return "CCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChannelsOpen(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 40;  sizes[2] = 1;  sizes[3] = 100;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChannelsOpen() throws Exception
  {
    return "MMMM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChannelsMine() throws Exception
  {
    return "channelsmine ( UserCode char(20), Name char(60), Type char(1), Host char(100), unique(UserCode, Name, Type, Host))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChannelsMine(String[] s) throws Exception
  {
    s[0] = "\000";
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChannelsMine() throws Exception
  {
    return "UserCode, Name, Type, Host";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChannelsMine() throws Exception
  {
    return "CCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChannelsMine(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 40;  sizes[2] = 1;  sizes[3] = 100;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChannelsMine() throws Exception
  {
    return "MMMM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChannelusers() throws Exception
  {
    return "channelusers ( Name char(60), User char(20), Domain char(80), PassWord char(20), Status char(1), unique(Name,User,Domain))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChannelusers(String[] s) throws Exception
  {
    s[0] = "channelusersUserInx on channelusers (User)";
    return 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChannelusers() throws Exception
  {
    return "Name, User, Domain, PassWord, Status";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChannelusers() throws Exception
  {
    return "CCCCC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChannelusers(short[] sizes) throws Exception
  {
    sizes[0] = 60;  sizes[1] = 20;  sizes[2] = 80;  sizes[3] = 20;  sizes[4] = 1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChannelusers() throws Exception
  {
    return "MMMOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChannelAlerts() throws Exception
  {
    return "channelalerts ( ToUser char(20), ToGroup char(60), unique(ToUser,ToGroup))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChannelAlerts(String[] s) throws Exception
  {
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChannelAlerts() throws Exception
  {
    return "ToUser, ToGroup";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChannelAlerts() throws Exception
  {
    return "CC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChannelAlerts(short[] sizes) throws Exception
  {
    sizes[0] = 20;  sizes[1] = 60;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChannelAlerts() throws Exception
  {
    return "MM";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getTableCreateStringChannelLinks() throws Exception
  {
    return "channellinks ( MsgID integer, Description char(100), Info char(250))";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int getIndexCreateStringsChannelLinks(String[] s) throws Exception
  {
    s[0] = "\000";
    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldNamesChannelLinks() throws Exception
  {
    return "msgID, Description, Info";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldTypesChannelLinks() throws Exception
  {
    return "ICC";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void getFieldSizesChannelLinks(short[] sizes) throws Exception
  {
    sizes[0] = 0;  sizes[1] = 100;  sizes[2] = 250;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFieldStylesChannelLinks() throws Exception
  {
    return "MOO";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void primeChannels(Connection con, Statement stmt) throws Exception
  {
    stmt = con.createStatement();

    stmt.executeUpdate("INSERT INTO channels ( Name, Purpose, RegisterOnSC, Type, Status, Deleteable ) "
                     + "VALUES ('___Company___','Broadcast Channel for Internal Users','N','I','U','N')");
    stmt.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateOpenChannels(Connection con, Statement stmt, boolean clear, String unm, String openChannels, String openChannelsTypes,
                                 String openChannelsHosts) throws Exception
  {
    try
    {
      if(clear)
      {
        stmt = con.createStatement();

        stmt.executeUpdate("DELETE FROM channelsopen WHERE UserCode = '" + unm + "'");

        stmt.close();
      }

      String entry, host;
      char type;
      int j, count = 0;

      int i = openChannels.indexOf("&#1;");
      while(i != -1)
      {
        entry = openChannels.substring(0, i);

        type = openChannelsTypes.charAt(count++);

        j = openChannelsHosts.indexOf("&#1;");
        if(j != -1)
        {
          host = openChannelsHosts.substring(0, j);
          openChannelsHosts = openChannelsHosts.substring(j+4);
          j = openChannelsHosts.indexOf("&#1;");
        }
        else host = ""; // just-in-case

        stmt = con.createStatement();

        stmt.executeUpdate("INSERT INTO channelsopen ( UserCode, Name, Type, Host ) VALUES ('" + unm + "','" + generalUtils.sanitiseForSQL(entry) + "','"
                         + type + "','" + generalUtils.sanitiseForSQL(host) + "')");

        stmt.close();

        openChannels = openChannels.substring(i+4);
        i = openChannels.indexOf("&#1;");
      }
    }
    catch(Exception e) // already
    {
      if(stmt != null) stmt.close();
    }
  }

}
