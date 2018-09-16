// =======================================================================================================================================================================================================
// System: ZaraStar WikiEngine: Utilities
// Module: Wiki.java
// Author: C.K.Harvey
// Copyright (c) 2006-08 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class Wiki
{
  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getStyling(String dnm, String[] headerLogo, String[] plainLogo, String[] usesFlash, String[] footerText, String[] pageHeaderImage1, String[] pageHeaderImage2, String[] pageHeaderImage3, String[] pageHeaderImage4,
                         String[] pageHeaderImage5, String[] watermark)
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_sitewiki?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT * FROM styling");   
      if(rs.next())
      {
        headerLogo[0]       = rs.getString(1); 
        plainLogo[0]        = rs.getString(2);
        usesFlash[0]        = rs.getString(3);
        footerText[0]       = rs.getString(4);
        pageHeaderImage1[0] = rs.getString(5);
        pageHeaderImage2[0] = rs.getString(6);
        pageHeaderImage3[0] = rs.getString(7);
        pageHeaderImage4[0] = rs.getString(8);
        pageHeaderImage5[0] = rs.getString(9);
        //watermark[0]        = rs.getString(10);
      }
      else
      {
        headerLogo[0]       = "";
        plainLogo[0]        = "";
        usesFlash[0]        = "N";
        footerText[0]       = "Copyright (c)";
        pageHeaderImage1[0] = "";
        pageHeaderImage2[0] = "";
        pageHeaderImage3[0] = "";
        pageHeaderImage4[0] = "";
        pageHeaderImage5[0] = "";
        //watermark[0]        = "";
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      headerLogo[0]       = "";
      plainLogo[0]        = "";
      usesFlash[0]        = "N";  
      footerText[0]       = "Copyright (c)";
      pageHeaderImage1[0] = "";
      pageHeaderImage2[0] = "";
      pageHeaderImage3[0] = "";
      pageHeaderImage4[0] = "";
      pageHeaderImage5[0] = "";

      System.out.println("Wiki: " + e);
      try
      {
        if(rs   != null) rs.close();     
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String convertLinks(String text, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir) throws Exception
  {
    String s="", link, linkTo, linkText;
    boolean inQuotes;

    int len = text.length();
    int x=0, y, linkLen, i, len2;
    while(x < len)
    {
      if((x + 2) < len && text.charAt(x) == '_' && text.charAt(x+1) == '_' && text.charAt(x+2) == '_')
      {
        x += 3;
        while((x + 2) < len && ! (text.charAt(x) == '_' && text.charAt(x+1) == '_' && text.charAt(x+2) == '_'))
          s += text.charAt(x++);
        x += 3;
      }
      else
      if(text.charAt(x) == '[')
      {
        if(x < len && text.charAt(x + 1) == '[')
        {
          ++x;
          link = "[";
          while(x < len && text.charAt(x) != ']') // just-in-case
            link += text.charAt(x++);

          if(x == len) // unterminated potential link structure at EOF
            s += link; // append whatever it is and leave as-is
          else
          {
            if(text.charAt(x) != ']') // unterminated potential link structure
              s += link;              // append whatever it is and leave as-is
            else
            {
              x += 2; // ]]

              if(   ! link.startsWith("[[ilink ") && ! link.startsWith("[[elink ") && ! link.startsWith("[[dlink ") && ! link.startsWith("[[plink ")
                 && ! link.startsWith("[[mlink ") && ! link.startsWith("[[slink ") && ! link.startsWith("[[flink ") && ! link.startsWith("[[xlink ")
                 && ! link.startsWith("[[alink ") && ! link.startsWith("[[clink ") && ! link.startsWith("[[cilink ") && ! link.startsWith("[[iilink ")
                 && ! link.startsWith("[[silink ") && ! link.startsWith("[[blink ") && ! link.startsWith("[[bzlink ") && ! link.startsWith("[[vlink ")
                 && ! link.startsWith("[[eilink ") && ! link.startsWith("[[tlink ") && ! link.startsWith("[[tilink ") && ! link.startsWith("[[rlink "))
              { // not a link structure
                s += link;                         // append whatever it is and leave as-is
              }
              else // is a link structure
              {
                linkLen = link.length();

                //y=8;
                y = 0;
                while(y < linkLen && link.charAt(y) != ' ') // just-in-case
                  ++y;
                ++y;

                while(y < linkLen && link.charAt(y) == ' ' || link.charAt(y) == '"')
                  ++y;
                if(y < linkLen && link.charAt(y - 1) == '"')
                  inQuotes = true;
                else inQuotes = false;

                linkTo = "";
                if(inQuotes)
                {
                  while(y < linkLen && link.charAt(y) != '"')
                    linkTo += link.charAt(y++);
                  ++y;
                }
                else
                {
                  while(y < linkLen && link.charAt(y) != ' ')
                    linkTo += link.charAt(y++);
                }

                while(y < linkLen && link.charAt(y) == ' ')
                  ++y;

                linkText = "";
                while(y < linkLen && link.charAt(y) != ']')
                  linkText += link.charAt(y++);

                switch(link.charAt(2))
                {
                  case 'a' : s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + linkTo + "\"><img src=\"http://" + men
                               + imageLibraryDir + linkText + "\" border=0></a>";
                             break;
                  case 'i' :
                             if(link.charAt(3) == 'i')
                             {
                               if(linkText.startsWith("\"") || linkText.startsWith("'"))
                                 linkText = linkText.substring(1, (linkText.length() - 1));
                               linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";
                             }

                             s += "<a href=\"http://" + men + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + linkTo + "\">" + linkText + "</a>";
                             break;
                  case 'e' :
                             if(link.charAt(3) == 'i')
                             {
                               if(linkText.startsWith("\"") || linkText.startsWith("'"))
                                 linkText = linkText.substring(1, (linkText.length() - 1));
                               linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";
                             }

                             if(! linkTo.startsWith("http://"))
                               linkTo = "http://" + linkTo;

                             s += "<a href=\"" + linkTo + "\">" + linkText + "</a>";
                             break;
                  case 't' :
                             if(link.charAt(3) == 'i')
                             {
                               if(linkText.startsWith("\"") || linkText.startsWith("'"))
                                 linkText = linkText.substring(1, (linkText.length() - 1));
                               linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";
                             }

                             if(! linkTo.startsWith("http://"))
                               linkTo = "http://" + linkTo;

                             s += "<a href=\"" + linkTo + "\" target=_new>" + linkText + "</a>";

                             break;
                  case 'b' :
                             if(link.charAt(3) == 'z') // link to blogguide on zarastar.org
                             {
                               s += "<a href=\"http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/BlogsDisplayBlogGuide?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=Zarastarorg&bnm=" + bnm + "&p1="
                                 + linkTo + "\" target=_blank>" + linkText + "</a>";
                             }
                             else s += "<a href=\"http://" + men + "/central/servlet/BlogsDisplayBlogGuide?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + linkTo + "\">" + linkText + "</a>";

                             break;
                  case 'v' : s += "<a href=\"http://" + men + "/central/servlet/_" + linkTo + "?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\">" + linkText + "</a>";
                             break;
                  case 'd' : s += "<a href=\"http://" + men + "/central/servlet/LibraryDownloaCasual?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + linkTo + "\">" + linkText + "</a>";
                             break;
                  case 'r' : s += "<a href=\"javascript:getHTML('RATDisplayIssueWave','&p1=" + linkTo + "')\">" + linkText + "</a>";
                             break;
                  case 'p' : s += "<img src=\"http://" + men  + imageLibraryDir + linkTo + "\" border=0>";
                             break;
                  case 'm' : s += "<a href=\"mailto:" + linkTo + "\">" + linkText + "</a>";
                             break;
                  case 's' :
                              if(link.charAt(3) == 'i')
                                linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";

                              s += "<a href=\"http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/SiteDisplayPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=Zarastarorg&bnm=" + bnm + "&p1=-"
                                + linkTo + "\" target=_blank>" + linkText + "</a>";

                             break;
                  case 'c' : i = 0;
                             len2 = linkText.length();
                             String catLocation = "";
                             while(i < len2 && linkText.charAt(i) != ' ')
                               catLocation += linkText.charAt(i++);
                             while(i < len2 && linkText.charAt(i) == ' ')
                               ++i;
                             String catType = "";
                             while(i < len2 && linkText.charAt(i) != ' ')
                               catType += linkText.charAt(i++);
                             while(i < len2 && linkText.charAt(i) == ' ')
                               ++i;

                             String rem = "";
                             while(i < len2)
                               rem += linkText.charAt(i++);

                             if(catType.equalsIgnoreCase("Catalog"))
                              catType = "C";
                             else catType = "L";

                             if(link.charAt(3) == 'i')
                               rem = "<img src=\"http://" + men + imageLibraryDir + rem + "\" border=0>";

                             if(catLocation.equalsIgnoreCase("Local"))
                             {
                               if(catType.equals("L"))
                               {
                                 s += "<a href=\"http://" + men + "/central/servlet/ProductManufacturerItems?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=F&p1=" + linkTo + "\">" + rem + "</a>";
                               }
                               else
                               {
                                 s += "<a href=\"http://" + men + "/central/servlet/CatalogUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + linkTo + "\">" + rem + "</a>";
                               }
                             }
                             else
                             {
                               s += "<a href=\"http://" + men + "/central/servlet/CatalogFetchLinked?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + catType + "&p1=" + linkTo + "\">" + rem
                                 + "</a>";
                             }
                             break;
                  case 'f' : i = 0;
                             len2 = linkText.length();
                             String width = "";
                             while(i < len2 && linkText.charAt(i) != ' ')
                               width += linkText.charAt(i++);
                             while(i < len2 && linkText.charAt(i) == ' ')
                               ++i;
                             String height = "";
                             while(i < len2)
                               height += linkText.charAt(i++);

                             s += "<div id='flashcontent'><p>The video content presented here requires the latest version of the Macromedia Flash Player. Please update your version of the free Flash Player by downloading "
                               + "<a href=\"http://www.macromedia.com/go/getflashplayer\">here</a>.</p><br><br><br><br></div><script type='text/javascript'>var fo=new FlashObject(\"http://" + men + flashDir + linkTo + "_controller.swf\",\"http://"
                               + men + flashDir + linkTo + "_controller.swf\",\"" + width + "\",\"" + height + "\", \"7\", \"#FFFFFF\", false, \"best\");fo.addVariable(\"csConfigFile\",\"http://" + men + flashDir + linkTo + "_config.xml\");"
                               + "fo.addVariable('csColor','#FFFFFF');fo.addVariable('csPreloader', \"http://" + men + flashDir + linkTo + "_preload.swf\");fo.write('flashcontent');</script>";
                               break;
                  case 'x' : s += "<form name='scSearch'><script language='JavaScript'>function steelclawsSearch(){window.location.href=\"http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/ExternalUserServices1a?&p1=\"+"
                               + "escape(document.forms.scSearch.searchPhrase.value)+\"&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "&\";}</script><input type='text' maxlength='100' size='60' name='searchPhrase'><a href=\"javascript:steelclawsSearch()\"> &nbsp; " + linkTo + "</a></form>";
                               break;
                }
              }
            }
          }
        }
        else s += text.charAt(x++);
      }
      else s += text.charAt(x++);
    }

   return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String convertLinksW(String text, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imageLibraryDir, String flashDir, String localDefnsDir) throws Exception
  {
    String s="", link, linkTo, linkText;
    boolean inQuotes;

    int len = text.length();
    int x=0, y, linkLen, i, len2;
    while(x < len)
    {
      if((x + 2) < len && text.charAt(x) == '_' && text.charAt(x+1) == '_' && text.charAt(x+2) == '_')
      {
        x += 3;
        while((x + 2) < len && ! (text.charAt(x) == '_' && text.charAt(x+1) == '_' && text.charAt(x+2) == '_'))
          s += text.charAt(x++);
        x += 3;
      }
      else
      if(text.charAt(x) == '[')
      {
        if(x < len && text.charAt(x + 1) == '[')
        {
          ++x;
          link = "[";
          while(x < len && text.charAt(x) != ']') // just-in-case
            link += text.charAt(x++);

          if(x == len) // unterminated potential link structure at EOF
            s += link; // append whatever it is and leave as-is
          else
          {
            if(text.charAt(x) != ']') // unterminated potential link structure
              s += link;              // append whatever it is and leave as-is
            else
            {
              x += 2; // ]]
              if(   ! link.startsWith("[[ilink ") && ! link.startsWith("[[elink ") && ! link.startsWith("[[dlink ") && ! link.startsWith("[[plink ")
                 && ! link.startsWith("[[mlink ") && ! link.startsWith("[[slink ") && ! link.startsWith("[[flink ") && ! link.startsWith("[[xlink ")
                 && ! link.startsWith("[[alink ") && ! link.startsWith("[[clink ") && ! link.startsWith("[[cilink ") && ! link.startsWith("[[iilink ")
                 && ! link.startsWith("[[silink ") && ! link.startsWith("[[blink ") && ! link.startsWith("[[bzlink ") && ! link.startsWith("[[vlink ")
                 && ! link.startsWith("[[eilink ") && ! link.startsWith("[[tlink ") && ! link.startsWith("[[tilink ") && ! link.startsWith("[[rlink "))
              { // not a link structure
                s += link;                         // append whatever it is and leave as-is
              }
              else // is a link structure
              {
                linkLen = link.length();

                //y=8;
                y = 0;
                while(y < linkLen && link.charAt(y) != ' ') // just-in-case
                  ++y;
                ++y;

                while(y < linkLen && link.charAt(y) == ' ' || link.charAt(y) == '"')
                  ++y;
                if(y < linkLen && link.charAt(y - 1) == '"')
                  inQuotes = true;
                else inQuotes = false;

                linkTo = "";
                if(inQuotes)
                {
                  while(y < linkLen && link.charAt(y) != '"')
                    linkTo += link.charAt(y++);
                  ++y;
                }
                else
                {
                  while(y < linkLen && link.charAt(y) != ' ')
                    linkTo += link.charAt(y++);
                }

                while(y < linkLen && link.charAt(y) == ' ')
                  ++y;

                linkText = "";
                while(y < linkLen && link.charAt(y) != ']')
                  linkText += link.charAt(y++);

                switch(link.charAt(2))
                {
                  case 'a' : s += "<a href=\"javascript:getHTML('SiteDisplayPageWave','&p1=" + linkTo + "')\"><img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0></a>";
                             break;
                  case 'i' :
                             if(link.charAt(3) == 'i')
                             {
                               if(linkText.startsWith("\"") || linkText.startsWith("'"))
                                 linkText = linkText.substring(1, (linkText.length() - 1));
                               linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";
                             }

                             s += "<a href=\"javascript:getHTML('SiteDisplayPageWave','&p1=" + linkTo + "')\">" + linkText + "</a>";
                             break;
                  case 'e' :
                             if(link.charAt(3) == 'i')
                             {
                               if(linkText.startsWith("\"") || linkText.startsWith("'"))
                                 linkText = linkText.substring(1, (linkText.length() - 1));
                               linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";
                             }

                             if(! linkTo.startsWith("http://"))
                               linkTo = "http://" + linkTo;

                             s += "<a href=\"" + linkTo + "\" target=_new>" + linkText + "</a>";
                             break;
                  case 't' :
                             if(link.charAt(3) == 'i')
                             {
                               if(linkText.startsWith("\"") || linkText.startsWith("'"))
                                 linkText = linkText.substring(1, (linkText.length() - 1));
                               linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";
                             }

                             if(! linkTo.startsWith("http://"))
                               linkTo = "http://" + linkTo;

                             s += "<a href=\"" + linkTo + "\" target=_new>" + linkText + "</a>";
                             break;
                  case 'b' :
                             if(link.charAt(3) == 'z') // link to blogguide on zarastar.org
                             {
                               s += "<a href=\"http://www.zarastar.org/central/servlet/BlogsDisplayBlogGuideWave?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=Zarastarorg&bnm=" + bnm + "&p1="
                                 + linkTo + "\" target=_blank>" + linkText + "</a>";///////////////////////////////
                             }
                             else s += "<a href=\"javascript:getHTML('BlogsDisplayBlogGuideWave','&p1=" + linkTo + "')\">" + linkText + "</a>";

                             break;
                  case 'v' : s += "<a href=\"javascript:getHTML('_" + linkTo + "','')\">" + linkText + "</a>";
                             break;
                  case 'd' : s += "<a href=\"javascript:getHTML('LibraryDownloaCasualw','&p1=" + linkTo + "')\">" + linkText + "</a>";
                             break;
                  case 'r' : s += "<a href=\"javascript:getHTML('RATDisplayIssueWave','&p1=" + linkTo + "')\">" + linkText + "</a>";
                             break;
                  case 'p' : s += "<img src=\"http://" + men  + imageLibraryDir + linkTo + "\" border=0>";
                             break;
                  case 'm' : s += "<a href=\"mailto:" + linkTo + "\">" + linkText + "</a>";
                             break;
                  case 's' :
                              if(link.charAt(3) == 'i')
                                linkText = "<img src=\"http://" + men + imageLibraryDir + linkText + "\" border=0>";

                              s += "<a href=\"javascript:getHTML('SiteDisplayPageWave','&p1=" + linkTo + "')\">" + linkText + "</a>";

                             break;
                  case 'c' : i = 0;
                             len2 = linkText.length();
                             String catLocation = "";
                             while(i < len2 && linkText.charAt(i) != ' ')
                               catLocation += linkText.charAt(i++);
                             while(i < len2 && linkText.charAt(i) == ' ')
                               ++i;
                             String catType = "";
                             while(i < len2 && linkText.charAt(i) != ' ')
                               catType += linkText.charAt(i++);
                             while(i < len2 && linkText.charAt(i) == ' ')
                               ++i;

                             String rem = "";
                             while(i < len2)
                               rem += linkText.charAt(i++);

                             if(catType.equalsIgnoreCase("Catalog"))
                              catType = "C";
                             else catType = "L";

                             if(link.charAt(3) == 'i')
                               rem = "<img src=\"http://" + men + imageLibraryDir + rem + "\" border=0>";

                             if(catLocation.equalsIgnoreCase("Local"))
                             {
                               if(catType.equals("L"))
                               {
                                 s += "<a href=\"http://" + men + "/central/servlet/ProductManufacturerItems?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=F&p1=" + linkTo + "\">" + rem + "</a>";
                               }
                               else
                               {
                                 s += "<a href=\"http://" + men + "/central/servlet/CatalogUtils?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + linkTo + "\">" + rem + "</a>";
                               }
                             }
                             else
                             {
                               s += "<a href=\"http://" + men + "/central/servlet/CatalogFetchLinked?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=" + catType + "&p1=" + linkTo + "\">" + rem
                                 + "</a>";
                             }
                             break;
                  case 'f' : i = 0;
                             len2 = linkText.length();
                             String width = "";
                             while(i < len2 && linkText.charAt(i) != ' ')
                               width += linkText.charAt(i++);
                             while(i < len2 && linkText.charAt(i) == ' ')
                               ++i;
                             String height = "";
                             while(i < len2)
                               height += linkText.charAt(i++);

                             s += "<div id='flashcontent'><p>The video content presented here requires the latest version of the Macromedia Flash Player. Please update your version of the free Flash Player by downloading "
                               + "<a href=\"http://www.macromedia.com/go/getflashplayer\">here</a>.</p><br><br><br><br></div><script type='text/javascript'>var fo=new FlashObject(\"http://" + men + flashDir + linkTo + "_controller.swf\",\"http://"
                               + men + flashDir + linkTo + "_controller.swf\",\"" + width + "\",\"" + height + "\", \"7\", \"#FFFFFF\", false, \"best\");fo.addVariable(\"csConfigFile\",\"http://" + men + flashDir + linkTo + "_config.xml\");"
                               + "fo.addVariable('csColor','#FFFFFF');fo.addVariable('csPreloader', \"http://" + men + flashDir + linkTo + "_preload.swf\");fo.write('flashcontent');</script>";
                               break;
                  case 'x' : s += "<form name='scSearch'><script language='JavaScript'>function steelclawsSearch(){window.location.href=\"http://" + serverUtils.serverToCall("ZC", localDefnsDir) + "/central/servlet/ExternalUserServices1a?&p1=\"+"
                               + "escape(document.forms.scSearch.searchPhrase.value)+\"&unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm
                               + "&\";}</script><input type='text' maxlength='100' size='60' name='searchPhrase'><a href=\"javascript:steelclawsSearch()\"> &nbsp; " + linkTo + "</a></form>";
                               break;
                }
              }
            }
          }
        }
        else s += text.charAt(x++);
      }
      else s += text.charAt(x++);
    }

   return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getLogoFromStyling(String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    String headerLogo = "";
    
    try
    {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_sitewiki?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();
      rs = stmt.executeQuery("SELECT HeaderLogo FROM styling");   
      if(rs.next())
        headerLogo = rs.getString(1); 
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return headerLogo;
  }

}
