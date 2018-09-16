// =======================================================================================================================================================================================================
// System: ZaraStar Admin: process styling
// Module: WikiStylingProcess.java
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

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class WikiStylingProcess extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", layoutType="", headerLogo="", plainLogo="", outerPaddingTop="", mainmenudlMarginTop="", outerPaddingBottom="", outerPaddingLeft="", footerBorderTopColor="",
           footerBorderTopWidth="", footerBorderTopStyle="", outerPaddingRight="", outerWidth="", outerBackgroundImage="", outerBackgroundPosition="", outerBackgroundRepeat="", mainmenuBackgroundColor="", mainmenuSize="", mainmenuFamily="",
           mainmenuStyle="", mainmenuWeight="", mainmenuBorderWidth="", mainmenuBorderStyle="", mainmenuBorderColor="", mainmenudtHeight="", mainmenuWidth="", mainmenudtBackgroundColor="", mainmenudtColor="", mainmenudtTextAlign="",
           mainmenudtBorderWidth="", mainmenudtBorderStyle="", mainmenudtBorderColor="", mainmenuliaColor="", mainmenuliahoverBackgroundColor="", mainmenuliahoverColor="", mainmenuddBackgroundColor="", mainmenuddBorderWidth="",
           mainmenuddBorderStyle="", mainmenuddBorderColor="", mainmenuddMarginLeft="", submenudlPaddingRight="", submenudlPaddingLeft="", submenudlPaddingTop="", submenudlPaddingBottom="", submenuliaColor="", submenuliStyle="",
           submenuliFamily="", submenuliWeight="", submenuliSize="", submenuliTextAlign="", submenuliHeight="", submenuliahoverBackgroundColor="", submenuliahoverColor="", mainBorderLeftWidth="", mainBorderLeftStyle="", mainBorderLeftColor="",
           mainBorderRightWidth="", mainBorderRightStyle="", mainBorderRightColor="", outerBackgroundColor="", mainBorderTopWidth="", mainBorderTopStyle="", mainBorderTopColor="", footerBackgroundColor="", mainmenudlPaddingLeft="",
           optionalColor="", optionalFontWeight="", optionalFontSize="", optionalFontFamily="", titleBarColor="", titleBarBackgroundColor="", titleBarBorderTopWidth="", titleBarBorderTopColor="", titleBarBorderTopStyle="",
           titleBarBorderBottomWidth="", titleBarBorderBottomColor="", titleBarBorderBottomStyle="", titleBarFontWeight="", titleBarFontSize="", titleBarFontFamily="", titleBarALinkColor="", titleBarALinkTextDecoration="", titleBarAHoverColor="",
           titleBarAHoverTextDecoration="", serviceMainFontWeight="", serviceMainFontSize="", serviceMainFontFamily="", serviceMainColor="", serviceMainALinkColor="", serviceMainALinkTextDecoration="", serviceMainAHoverColor="",
           serviceMainAHoverTextDecoration="", headerrepeatSize="", headerrepeatFamily="", headerrepeatStyle="", headerrepeatTextAlign="", headerrepeatHeight="", headerrepeatBackgroundImage="", mainmenudtWidth="", mainBorderBottomWidth="",
           mainBorderBottomStyle="", mainBorderBottomColor="", mainmenudlPaddingRight="", mainmenudlPaddingTop="", pageInputBackgroundColor="", pageInputColor="", pageInputFontWeight="", pageInputFontFamily="", pageInputFontSize="",
           mainmenudlPaddingBottom="", mainmenuh1BackgroundColor="", mainmenuliWidth="", mainmenuh1Color="", mainmenuh1BorderLeftStyle="", mainmenuh1BorderLeftWidth="", mainmenuh1BorderRightWidth="", mainmenuh1BorderRightStyle="",
           mainmenuh1BorderTopColor="", mainmenuh1BorderTopWidth="", mainmenuh1BorderBottomWidth="", pageColumnBackgroundColor="", pageColumnFontWeight="", pageColumnFontFamily="", pageColumnFontSize="", pagePColor="", pagePBackgroundColor="",
           pagePFontWeight="", pagePFontFamily="", pagePFontSize="", pageH1BackgroundColor="", pageH1Color="", pageH1FontWeight="", pageH1FontFamily="", pageH1FontSize="", pageH1BorderTopWidth="", pageH1BorderTopColor="", pageH1BorderTopStyle="",
           pageH1BorderBottomWidth="", pageH1BorderBottomColor="", pageH1BorderBottomStyle="", pageALinkColor="", pageALinkFontWeight="", pageALinkFontFamily="", pageALinkFontSize="", pageALinkTextDecoration="", pageAHoverColor="",
           pageAHoverTextDecoration="", line1Color="", line1BackgroundColor="", line2Color="", line2BackgroundColor="", textErrorLargeColor="", textErrorLargeFontWeight="", textErrorLargeFontFamily="", textErrorLargeFontSize="",
           textNumericColor="", textNumericFontWeight="", textNumericFontFamily="", textNumericFontSize="", textSalesCreditNoteColor="", textSalesCreditNoteFontWeight="", textSalesCreditNoteFontFamily="", textSalesCreditNoteFontSize="", textReceiptColor="",
           textReceiptFontWeight="", textReceiptFontFamily="", textReceiptFontSize="", textInvoiceColor="", textInvoiceFontWeight="", textInvoiceFontFamily="", textInvoiceFontSize="", textRedHighlightingColor="", textRedHighlightingFontWeight="",
           textRedHighlightingFontFamily="", textRedHighlightingFontSize="", footerText="", pageHeaderImage1="", pageHeaderImage2="", pageHeaderImage3="", pageHeaderImage4="", pageHeaderImage5="", styleName="", mainmenuh1BorderLeftColor="",
           libraryBackgroundColor="", libraryBorderColor="", libraryBorderWidth="", libraryBorderStyle="", libraryPColor="", libraryPBackgroundColor="", libraryPFontWeight="", libraryPFontFamily="", libraryPFontSize="",
           libraryTableBackgroundColor="", libraryALinkColor="", libraryALinkTextDecoration="", libraryALinkFontWeight="", libraryALinkFontFamily="", libraryALinkFontSize="", libraryAHoverColor="", libraryAHoverTextDecoration="",
           libraryAHoverFontWeight="", libraryAHoverFontFamily="", libraryAHoverFontSize="", bodyBorderColor="", bodyBorderStyle="", bodyBorderWidth="", bigouterBackgroundColor="", bigouterBackgroundImage="", bigouterBackgroundRepeat="",
           mainmenuh1BorderTopStyle="", mainmenuh1BorderBottomColor="", mainmenuh1MarginTop="", mainmenuh1BorderRightColor="", mainmenuh1TextAlign="", mainmenuh1Width="", mainmenuh1FontSize="", mainmenuh1MarginBottom="",
           mainmenuh1BorderBottomStyle="", mainmenuh1MarginRight="", mainmenuh1MarginLeft="", submenudtBackgroundColor="", submenudtBorderColor="", submenudtBorderWidth="", submenudtBorderStyle="", submenudtTextAlign="", submenudtFamily="",
           submenudtWeight="", submenudtStyle="", submenudtSize="", submenudtWidth="", submenuddBackgroundColor="", submenuddBorderWidth="", submenuddBorderStyle="", submenuddBorderColor="", footerColor="", footerBorderLeftWidth="",
           footerBorderLeftStyle="", footerBorderLeftColor="", footerBorderRightWidth="", footerBorderRightStyle="", footerBorderRightColor="", footerBorderBottomWidth="", footerBorderBottomStyle="", footerBorderBottomColor="",
           footerTextAlign="", footerFamily="", footerStyle="", footerWeight="", footerSize="", footerPaddingRight="", footerPaddingBottom="", footerPaddingTop="", footerPaddingLeft="", submenudtColor = "", headerrepeatColor="",
           titleBarLinkFontFamily="", titleBarLinkFontWeight="", titleBarLinkFontSize="", mainmenuh1Style="", mainmenuh1Weight="", mainmenuh1Family="", source="", highlightmenuitemColor="", highlightmenuitemBackgroundColor="",
           channelHeaderFontFamily = "", channelHeaderFontSize = "", channelHeaderFontWeight = "", channelHeaderFontColor = "", channelHeaderBackgroundColor = "", channelHeaderBorderWidth = "", channelHeaderBorderColor = "",
           channelHeaderBorderStyle = "", channelFontFamily = "", channelFontSize = "", channelFontWeight = "", channelFontColor = "", channelBackgroundColor = "", channelMsgFontFamily = "", channelMsgFontSize = "", channelMsgFontWeight = "",
           channelMsgFontColor =  "", channelMsgBackgroundColor = "", channelButtonsFontFamily = "", channelButtonsFontSize = "", channelButtonsFontWeight = "", channelButtonsFontColor = "", channelButtonsBackgroundColor = "",
           channelButtonsALinkFontWeight = "", channelButtonsALinkFontSize = "", channelButtonsALinkFontFamily = "", channelButtonsALinkColor = "", channelButtonsALinkTextDecoration = "", channelButtonsAHoverFontWeight = "",
           channelButtonsAHoverFontSize = "", channelButtonsAHoverFontFamily = "", channelButtonsAHoverColor = "", channelButtonsAHoverTextDecoration = "", mainmenuddHeight = "", mainmenuPaddingLeft = "", mainmenuPaddingRight = "",
           mainmenuPaddingTop = "", mainmenuPaddingBottom = "";

    try
    {
      directoryUtils.setContentHeaders(res);
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
        if(name.equals("source"))
          source = value[0];
        else
        if(name.equals("layoutType"))
          layoutType = value[0];
        else
        if(name.equals("headerLogo"))
          headerLogo = value[0];
        else
        if(name.equals("plainLogo"))
          plainLogo = value[0];
        else
        if(name.equals("outerPaddingTop"))
          outerPaddingTop = value[0];
        else
        if(name.equals("outerPaddingBottom"))
          outerPaddingBottom = value[0];
        else
        if(name.equals("outerPaddingLeft"))
          outerPaddingLeft = value[0];
        else
        if(name.equals("footerBorderTopColor"))
          footerBorderTopColor = value[0];
        else
        if(name.equals("footerBorderTopWidth"))
          footerBorderTopWidth = value[0];
        else
        if(name.equals("footerBorderTopStyle"))
          footerBorderTopStyle = value[0];
        else
        if(name.equals("footerBorderTopStyle"))
          footerBorderTopStyle = value[0];
        else
        if(name.equals("outerPaddingRight"))
          outerPaddingRight = value[0];
        else
        if(name.equals("outerBackgroundColor"))
          outerBackgroundColor = value[0];
        else
        if(name.equals("outerWidth"))
          outerWidth = value[0];
        else
        if(name.equals("outerBackgroundImage"))
          outerBackgroundImage = value[0];
        else
       if(name.equals("bigouterBackgroundColor"))
          bigouterBackgroundColor = value[0];
        else
        if(name.equals("bigouterBackgroundImage"))
          bigouterBackgroundImage = value[0];
        else
        if(name.equals("bigouterBackgroundRepeat"))
          bigouterBackgroundRepeat = value[0];
        else
        if(name.equals("outerBackgroundRepeat"))
          outerBackgroundRepeat = value[0];
        else
        if(name.equals("outerBackgroundPosition"))
          outerBackgroundPosition = value[0];
        else
        if(name.equals("mainmenuStyle"))
          mainmenuStyle = value[0];
        else
        if(name.equals("mainmenuWeight"))
          mainmenuWeight = value[0];
        else
        if(name.equals("mainmenuBorderWidth"))
          mainmenuBorderWidth = value[0];
        else
        if(name.equals("mainmenuBorderStyle"))
          mainmenuBorderStyle = value[0];
        else
        if(name.equals("mainmenuFamily"))
          mainmenuFamily = value[0];
        else
        if(name.equals("mainmenuBorderColor"))
          mainmenuBorderColor = value[0];
        else
        if(name.equals("mainmenudtHeight"))
          mainmenudtHeight = value[0];
        else
        if(name.equals("mainmenuWidth"))
          mainmenuWidth = value[0];
        else
        if(name.equals("mainmenuPaddingLeft"))
          mainmenuPaddingLeft = value[0];
        else
        if(name.equals("mainmenuPaddingRight"))
          mainmenuPaddingRight = value[0];
        else
        if(name.equals("mainmenuPaddingTop"))
          mainmenuPaddingTop = value[0];
        else
        if(name.equals("mainmenuPaddingBottom"))
          mainmenuPaddingBottom = value[0];
        else
        if(name.equals("mainmenudtBackgroundColor"))
          mainmenudtBackgroundColor = value[0];
        else
        if(name.equals("mainmenudtColor"))
          mainmenudtColor = value[0];
        else
        if(name.equals("mainmenudtTextAlign"))
          mainmenudtTextAlign = value[0];
        else
        if(name.equals("mainmenudtBorderStyle"))
          mainmenudtBorderStyle = value[0];
        else
        if(name.equals("mainmenudtBorderColor"))
          mainmenudtBorderColor = value[0];
        else
        if(name.equals("mainmenuliaColor"))
          mainmenuliaColor = value[0];
        else
        if(name.equals("mainmenuliahoverBackgroundColor"))
          mainmenuliahoverBackgroundColor = value[0];
        else
        if(name.equals("mainmenuliahoverColor"))
          mainmenuliahoverColor = value[0];
        else
        if(name.equals("mainmenuddBackgroundColor"))
          mainmenuddBackgroundColor = value[0];
        else
        if(name.equals("mainmenuddBorderWidth"))
          mainmenuddBorderWidth = value[0];
        else
        if(name.equals("mainmenuddBorderStyle"))
          mainmenuddBorderStyle = value[0];
        else
        if(name.equals("mainmenuddBorderColor"))
          mainmenuddBorderColor = value[0];
        else
        if(name.equals("mainmenuddMarginLeft"))
          mainmenuddMarginLeft = value[0];
        else
        if(name.equals("mainmenuddBackgroundColor"))
          mainmenuddBackgroundColor = value[0];
        else
        if(name.equals("submenudlPaddingRight"))
          submenudlPaddingRight = value[0];
        else
        if(name.equals("submenudlPaddingLeft"))
          submenudlPaddingLeft = value[0];
        else
        if(name.equals("submenudlPaddingTop"))
          submenudlPaddingTop = value[0];
        else
        if(name.equals("submenudlPaddingBottom"))
          submenudlPaddingBottom = value[0];
        else
        if(name.equals("submenuliStyle"))
          submenuliStyle = value[0];
        else
        if(name.equals("submenuliFamily"))
          submenuliFamily = value[0];
        else
        if(name.equals("submenuliWeight"))
          submenuliWeight = value[0];
        else
        if(name.equals("submenuliSize"))
          submenuliSize = value[0];
        else
        if(name.equals("submenuliTextAlign"))
          submenuliTextAlign = value[0];
        else
        if(name.equals("submenuliHeight"))
          submenuliHeight = value[0];
        else
        if(name.equals("submenuliahoverBackgroundColor"))
          submenuliahoverBackgroundColor = value[0];
        else
        if(name.equals("submenuliahoverColor"))
          submenuliahoverColor = value[0];
        else
        if(name.equals("mainBorderLeftWidth"))
          mainBorderLeftWidth = value[0];
        else
        if(name.equals("mainBorderLeftStyle"))
          mainBorderLeftStyle = value[0];
        else
        if(name.equals("mainBorderLeftColor"))
          mainBorderLeftColor = value[0];
        else
        if(name.equals("mainBorderRightWidth"))
          mainBorderRightWidth = value[0];
        else
        if(name.equals("mainBorderRightStyle"))
          mainBorderRightStyle = value[0];
        else
        if(name.equals("mainmenudtBorderWidth"))
          mainmenudtBorderWidth = value[0];
        else
        if(name.equals("outerBackgroundColor"))
          outerBackgroundColor = value[0];
        else
        if(name.equals("mainBorderTopWidth"))
          mainBorderTopWidth = value[0];
        else
        if(name.equals("mainBorderTopStyle"))
          mainBorderTopStyle = value[0];
        else
        if(name.equals("bodyBorderWidth"))
          bodyBorderWidth = value[0];
        else
        if(name.equals("bodyBorderColor"))
          bodyBorderColor = value[0];
        else
        if(name.equals("bodyBorderStyle"))
          bodyBorderStyle = value[0];
        else
        if(name.equals("optionalColor"))
          optionalColor = value[0];
        else
        if(name.equals("optionalFontWeight"))
          optionalFontWeight = value[0];
        else
        if(name.equals("optionalFontSize"))
          optionalFontSize = value[0];
        else
        if(name.equals("optionalFontFamily"))
          optionalFontFamily = value[0];
        else
        if(name.equals("titleBarColor"))
          titleBarColor = value[0];
        else
        if(name.equals("titleBarBackgroundColor"))
          titleBarBackgroundColor = value[0];
        else
        if(name.equals("titleBarBorderTopWidth"))
          titleBarBorderTopWidth = value[0];
        else
        if(name.equals("titleBarBorderTopColor"))
          titleBarBorderTopColor = value[0];
        else
        if(name.equals("titleBarBorderTopStyle"))
          titleBarBorderTopStyle = value[0];
        else
        if(name.equals("titleBarBorderBottomWidth"))
          titleBarBorderBottomWidth = value[0];
        else
        if(name.equals("titleBarBorderBottomColor"))
          titleBarBorderBottomColor = value[0];
        else
        if(name.equals("titleBarBorderBottomStyle"))
          titleBarBorderBottomStyle = value[0];
        else
        if(name.equals("titleBarFontWeight"))
          titleBarFontWeight = value[0];
        else
        if(name.equals("titleBarFontSize"))
          titleBarFontSize = value[0];
        else
        if(name.equals("titleBarFontFamily"))
          titleBarFontFamily = value[0];
        else
        if(name.equals("titleBarLinkFontWeight"))
          titleBarLinkFontWeight = value[0];
        else
        if(name.equals("titleBarLinkFontSize"))
          titleBarLinkFontSize = value[0];
        else
        if(name.equals("titleBarLinkFontFamily"))
          titleBarLinkFontFamily = value[0];
        else
        if(name.equals("titleBarALinkColor"))
          titleBarALinkColor = value[0];
        else
        if(name.equals("titleBarALinkTextDecoration"))
          titleBarALinkTextDecoration = value[0];
        else
        if(name.equals("titleBarAHoverColor"))
          titleBarAHoverColor = value[0];
        else
        if(name.equals("titleBarAHoverTextDecoration"))
          titleBarAHoverTextDecoration = value[0];
        else
        if(name.equals("serviceMainFontWeight"))
          serviceMainFontWeight = value[0];
        else
        if(name.equals("serviceMainFontSize"))
          serviceMainFontSize = value[0];
        else
        if(name.equals("serviceMainFontFamily"))
          serviceMainFontFamily = value[0];
        else
        if(name.equals("serviceMainColor"))
          serviceMainColor = value[0];
        else
        if(name.equals("serviceMainALinkColor"))
          serviceMainALinkColor = value[0];
        else
        if(name.equals("serviceMainALinkTextDecoration"))
          serviceMainALinkTextDecoration = value[0];
        else
        if(name.equals("serviceMainAHoverColor"))
          serviceMainAHoverColor = value[0];
        else
        if(name.equals("serviceMainAHoverTextDecoration"))
          serviceMainAHoverTextDecoration = value[0];
        else
         if(name.equals("headerrepeatSize"))
          headerrepeatSize = value[0];
        else
        if(name.equals("headerrepeatStyle"))
          headerrepeatStyle = value[0];
        else
        if(name.equals("headerrepeatTextAlign"))
          headerrepeatTextAlign = value[0];
        else
        if(name.equals("headerrepeatHeight"))
          headerrepeatHeight = value[0];
        else
        if(name.equals("headerrepeatBackgroundImage"))
          headerrepeatBackgroundImage = value[0];
        else
        if(name.equals("mainmenuBackgroundColor"))
          mainmenuBackgroundColor = value[0];
        else
        if(name.equals("mainmenudtWidth"))
          mainmenudtWidth = value[0];
        else
        if(name.equals("submenuliaColor"))
          submenuliaColor = value[0];
        else
        if(name.equals("mainBorderRightColor"))
          mainBorderRightColor = value[0];
        else
        if(name.equals("mainBorderTopColor"))
          mainBorderTopColor = value[0];
        else
        if(name.equals("mainmenudlMarginTop"))
          mainmenudlMarginTop = value[0];
        else
         if(name.equals("mainmenudlPaddingRight"))
          mainmenudlPaddingRight = value[0];
        else
         if(name.equals("mainmenudlPaddingLeft"))
          mainmenudlPaddingLeft = value[0];
        else
        if(name.equals("footerBackgroundColor"))
          footerBackgroundColor = value[0];
        else
        if(name.equals("mainmenudlPaddingBottom"))
          mainmenudlPaddingBottom = value[0];
        else
        if(name.equals("mainmenuh1Color"))
          mainmenuh1Color = value[0];
        else
        if(name.equals("mainmenuh1Style"))
          mainmenuh1Style = value[0];
        else
        if(name.equals("mainmenuh1Weight"))
          mainmenuh1Weight = value[0];
        else
        if(name.equals("mainmenuh1Family"))
          mainmenuh1Family = value[0];
        else
        if(name.equals("mainmenuh1BorderLeftWidth"))
          mainmenuh1BorderLeftWidth = value[0];
        else
        if(name.equals("mainmenuh1BorderLeftStyle"))
          mainmenuh1BorderLeftStyle = value[0];
        else
        if(name.equals("mainmenuh1BorderTopWidth"))
          mainmenuh1BorderTopWidth = value[0];
        else
        if(name.equals("mainmenudlPaddingTop"))
          mainmenudlPaddingTop = value[0];
        else
        if(name.equals("mainmenuh1BackgroundColor"))
          mainmenuh1BackgroundColor = value[0];
        else
        if(name.equals("mainmenuliWidth"))
          mainmenuliWidth = value[0];
        else
        if(name.equals("mainBorderBottomWidth"))
          mainBorderBottomWidth = value[0];
        else
        if(name.equals("mainBorderBottomColor"))
          mainBorderBottomColor = value[0];
        else
        if(name.equals("mainBorderBottomStyle"))
          mainBorderBottomStyle = value[0];
        else
        if(name.equals("mainmenuh1BorderRightWidth"))
          mainmenuh1BorderRightWidth = value[0];
        else
        if(name.equals("mainmenuh1BorderRightStyle"))
          mainmenuh1BorderRightStyle = value[0];
        else
        if(name.equals("mainmenuh1BorderTopColor"))
          mainmenuh1BorderTopColor = value[0];
        else
        if(name.equals("mainmenuSize"))
          mainmenuSize = value[0];
        else
        if(name.equals("mainmenuh1BorderBottomWidth"))
          mainmenuh1BorderBottomWidth = value[0];
        else
        if(name.equals("pageColumnBackgroundColor"))
          pageColumnBackgroundColor = value[0];
        else
        if(name.equals("pageColumnFontWeight"))
          pageColumnFontWeight = value[0];
        else
        if(name.equals("pageColumnFontFamily"))
          pageColumnFontFamily = value[0];
        else
        if(name.equals("pageColumnFontSize"))
          pageColumnFontSize = value[0];
        else
        if(name.equals("pagePColor"))
          pagePColor = value[0];
        else
        if(name.equals("pagePBackgroundColor"))
          pagePBackgroundColor = value[0];
        else
        if(name.equals("pagePFontWeight"))
          pagePFontWeight = value[0];
        else
        if(name.equals("pagePFontFamily"))
          pagePFontFamily = value[0];
        else
        if(name.equals("pagePFontSize"))
          pagePFontSize = value[0];
        else
        if(name.equals("pageH1BackgroundColor"))
          pageH1BackgroundColor = value[0];
        else
        if(name.equals("pageH1Color"))
          pageH1Color = value[0];
        else
        if(name.equals("pageH1FontWeight"))
          pageH1FontWeight = value[0];
        else
        if(name.equals("pageH1FontFamily"))
          pageH1FontFamily = value[0];
        else
        if(name.equals("pageH1FontSize"))
          pageH1FontSize = value[0];
        else
        if(name.equals("pageH1BorderTopWidth"))
          pageH1BorderTopWidth = value[0];
        else
        if(name.equals("pageH1BorderTopColor"))
          pageH1BorderTopColor = value[0];
        else
        if(name.equals("pageH1BorderTopStyle"))
          pageH1BorderTopStyle = value[0];
        else
        if(name.equals("pageH1BorderBottomWidth"))
          pageH1BorderBottomWidth = value[0];
        else
        if(name.equals("pageH1BorderBottomColor"))
          pageH1BorderBottomColor = value[0];
        else
        if(name.equals("pageH1BorderBottomStyle"))
          pageH1BorderBottomStyle = value[0];
        else
        if(name.equals("pageALinkColor"))
          pageALinkColor = value[0];
        else
        if(name.equals("pageALinkFontWeight"))
          pageALinkFontWeight = value[0];
        else
        if(name.equals("pageALinkFontFamily"))
          pageALinkFontFamily = value[0];
        else
        if(name.equals("pageALinkFontSize"))
          pageALinkFontSize = value[0];
        else
        if(name.equals("pageALinkTextDecoration"))
          pageALinkTextDecoration = value[0];
        else
        if(name.equals("pageAHoverColor"))
          pageAHoverColor = value[0];
        else
        if(name.equals("pageAHoverTextDecoration"))
          pageAHoverTextDecoration = value[0];
        else
        if(name.equals("line1Color"))
          line1Color = value[0];
        else
        if(name.equals("line1BackgroundColor"))
          line1BackgroundColor = value[0];
        else
        if(name.equals("line2Color"))
          line2Color = value[0];
        else
        if(name.equals("line2BackgroundColor"))
          line2BackgroundColor = value[0];
        else
        if(name.equals("textErrorLargeColor"))
          textErrorLargeColor = value[0];
        else
        if(name.equals("textErrorLargeFontWeight"))
          textErrorLargeFontWeight = value[0];
        else
        if(name.equals("textErrorLargeFontFamily"))
          textErrorLargeFontFamily = value[0];
        else
        if(name.equals("textErrorLargeFontSize"))
          textErrorLargeFontSize = value[0];
        else
        if(name.equals("textNumericColor"))
          textNumericColor = value[0];
        else
        if(name.equals("textNumericFontWeight"))
          textNumericFontWeight = value[0];
        else
        if(name.equals("textNumericFontFamily"))
          textNumericFontFamily = value[0];
        else
        if(name.equals("textNumericFontSize"))
          textNumericFontSize = value[0];
        else
        if(name.equals("textSalesCreditNoteColor"))
          textSalesCreditNoteColor = value[0];
        else
        if(name.equals("textSalesCreditNoteFontWeight"))
          textSalesCreditNoteFontWeight = value[0];
        else
        if(name.equals("textSalesCreditNoteFontFamily"))
          textSalesCreditNoteFontFamily = value[0];
        else
        if(name.equals("textSalesCreditNoteFontSize"))
          textSalesCreditNoteFontSize = value[0];
        else
        if(name.equals("textReceiptColor"))
          textReceiptColor = value[0];
        else
        if(name.equals("textReceiptFontWeight"))
          textReceiptFontWeight = value[0];
        else
        if(name.equals("textReceiptFontFamily"))
          textReceiptFontFamily = value[0];
        else
        if(name.equals("textReceiptFontSize"))
          textReceiptFontSize = value[0];
        else
        if(name.equals("textInvoiceColor"))
          textInvoiceColor = value[0];
        else
        if(name.equals("textInvoiceFontWeight"))
          textInvoiceFontWeight = value[0];
        else
        if(name.equals("textInvoiceFontFamily"))
          textInvoiceFontFamily = value[0];
        else
        if(name.equals("textInvoiceFontSize"))
          textInvoiceFontSize = value[0];
        else
        if(name.equals("textRedHighlightingColor"))
          textRedHighlightingColor = value[0];
        else
        if(name.equals("textRedHighlightingFontWeight"))
          textRedHighlightingFontWeight = value[0];
        else
        if(name.equals("textRedHighlightingFontFamily"))
          textRedHighlightingFontFamily = value[0];
        else
        if(name.equals("textRedHighlightingFontSize"))
          textRedHighlightingFontSize = value[0];
        else
        if(name.equals("footerText"))
          footerText = value[0];
        else
        if(name.equals("pageHeaderImage1"))
          pageHeaderImage1 = value[0];
        else
        if(name.equals("pageHeaderImage2"))
          pageHeaderImage2 = value[0];
        else
        if(name.equals("pageHeaderImage3"))
          pageHeaderImage3 = value[0];
        else
        if(name.equals("pageHeaderImage4"))
          pageHeaderImage4 = value[0];
        else
        if(name.equals("pageHeaderImage5"))
          pageHeaderImage5 = value[0];
        else
        if(name.equals("styleName"))
          styleName = value[0];
        else
        if(name.equals("mainmenuh1BorderLeftColor"))
          mainmenuh1BorderLeftColor = value[0];
        else
        if(name.equals("libraryBackgroundColor"))
          libraryBackgroundColor = value[0];
        else
        if(name.equals("libraryBorderColor"))
          libraryBorderColor = value[0];
        else
        if(name.equals("libraryBorderWidth"))
          libraryBorderWidth = value[0];
        else
        if(name.equals("libraryBorderStyle"))
          libraryBorderStyle = value[0];
        else
        if(name.equals("libraryPColor"))
          libraryPColor = value[0];
        else
        if(name.equals("libraryPBackgroundColor"))
          libraryPBackgroundColor = value[0];
        else
        if(name.equals("libraryPFontWeight"))
          libraryPFontWeight = value[0];
        else
        if(name.equals("libraryPFontFamily"))
          libraryPFontFamily = value[0];
        else
        if(name.equals("libraryPFontSize"))
          libraryPFontSize = value[0];
        else
        if(name.equals("libraryTableBackgroundColor"))
          libraryTableBackgroundColor = value[0];
        else
        if(name.equals("libraryALinkColor"))
          libraryALinkColor = value[0];
        else
        if(name.equals("libraryALinkTextDecoration"))
          libraryALinkTextDecoration = value[0];
        else
        if(name.equals("libraryALinkFontWeight"))
          libraryALinkFontWeight = value[0];
        else
        if(name.equals("libraryALinkFontFamily"))
          libraryALinkFontFamily = value[0];
        else
        if(name.equals("libraryALinkFontSize"))
          libraryALinkFontSize = value[0];
        else
        if(name.equals("libraryAHoverColor"))
          libraryAHoverColor = value[0];
        else
        if(name.equals("libraryAHoverTextDecoration"))
          libraryAHoverTextDecoration = value[0];
        else
        if(name.equals("libraryAHoverFontWeight"))
          libraryAHoverFontWeight = value[0];
        else
        if(name.equals("libraryAHoverFontFamily"))
          libraryAHoverFontFamily = value[0];
        else
        if(name.equals("libraryAHoverFontSize"))
          libraryAHoverFontSize = value[0];
        else
        if(name.equals("channelBackgroundColor"))
          channelBackgroundColor = value[0];
        else
        if(name.equals("channelHeaderFontFamily"))
          channelHeaderFontFamily = value[0];
        else
        if(name.equals("channelHeaderFontSize"))
          channelHeaderFontSize = value[0];
        else
        if(name.equals("channelHeaderFontWeight"))
          channelHeaderFontWeight = value[0];
        else
        if(name.equals("channelHeaderFontColor"))
          channelHeaderFontColor = value[0];
        else
        if(name.equals("channelHeaderBackgroundColor"))
          channelHeaderBackgroundColor = value[0];
        else
        if(name.equals("channelHeaderBorderWidth"))
          channelHeaderBorderWidth = value[0];
        else
        if(name.equals("channelHeaderBorderColor"))
          channelHeaderBorderColor = value[0];
        else
        if(name.equals("channelHeaderBorderStyle"))
          channelHeaderBorderStyle = value[0];
        else
        if(name.equals("channelFontFamily"))
          channelFontFamily = value[0];
        else
        if(name.equals("channelFontSize"))
          channelFontSize = value[0];
        else
        if(name.equals("channelFontWeight"))
          channelFontWeight = value[0];
        else
        if(name.equals("channelFontColor"))
          channelFontColor = value[0];
        else
        if(name.equals("channelMsgFontFamily"))
          channelMsgFontFamily = value[0];
        else
        if(name.equals("channelMsgFontSize"))
          channelMsgFontSize = value[0];
        else
        if(name.equals("channelMsgFontWeight"))
          channelMsgFontWeight = value[0];
        else
        if(name.equals("channelMsgFontColor"))
          channelMsgFontColor = value[0];
        else
        if(name.equals("channelMsgBackgroundColor"))
          channelMsgBackgroundColor = value[0];
        else
        if(name.equals("channelButtonsFontFamily"))
          channelButtonsFontFamily = value[0];
        else
        if(name.equals("channelButtonsFontSize"))
          channelButtonsFontSize = value[0];
        else
        if(name.equals("channelButtonsFontWeight"))
          channelButtonsFontWeight = value[0];
        else
        if(name.equals("channelButtonsFontColor"))
          channelButtonsFontColor = value[0];
        else
        if(name.equals("channelButtonsBackgroundColor"))
          channelButtonsBackgroundColor = value[0];
        else
        if(name.equals("channelButtonsALinkFontWeight"))
          channelButtonsALinkFontWeight = value[0];
        else
        if(name.equals("channelButtonsALinkFontSize"))
          channelButtonsALinkFontSize = value[0];
        else
        if(name.equals("channelButtonsALinkFontFamily"))
          channelButtonsALinkFontFamily = value[0];
        else
        if(name.equals("channelButtonsALinkColor"))
          channelButtonsALinkColor = value[0];
        else
        if(name.equals("channelButtonsALinkTextDecoration"))
          channelButtonsALinkTextDecoration = value[0];
        else
        if(name.equals("channelButtonsAHoverFontWeight"))
          channelButtonsAHoverFontWeight = value[0];
        else
        if(name.equals("channelButtonsAHoverFontSize"))
          channelButtonsAHoverFontSize = value[0];
        else
        if(name.equals("channelButtonsAHoverFontFamily"))
          channelButtonsAHoverFontFamily = value[0];
        else
        if(name.equals("channelButtonsAHoverColor"))
          channelButtonsAHoverColor = value[0];
        else
        if(name.equals("channelButtonsAHoverTextDecoration"))
          channelButtonsAHoverTextDecoration = value[0];
        else
        if(name.equals("submenuddBorderStyle"))
          submenuddBorderStyle = value[0];
        else
        if(name.equals("submenuddBorderColor"))
          submenuddBorderColor = value[0];
        else
        if(name.equals("footerColor"))
          footerColor = value[0];
        else
        if(name.equals("footerBorderLeftWidth"))
          footerBorderLeftWidth = value[0];
        else
        if(name.equals("footerBorderLeftStyle"))
          footerBorderLeftStyle = value[0];
        else
        if(name.equals("footerBorderLeftColor"))
          footerBorderLeftColor = value[0];
        else
        if(name.equals("footerBorderRightWidth"))
          footerBorderRightWidth = value[0];
        else
        if(name.equals("footerBorderRightStyle"))
          footerBorderRightStyle = value[0];
        else
        if(name.equals("footerBorderRightColor"))
          footerBorderRightColor = value[0];
        else
        if(name.equals("footerBorderBottomWidth"))
          footerBorderBottomWidth = value[0];
        else
        if(name.equals("footerBorderBottomStyle"))
          footerBorderBottomStyle = value[0];
        else
        if(name.equals("footerBorderBottomColor"))
          footerBorderBottomColor = value[0];
        else
        if(name.equals("footerTextAlign"))
          footerTextAlign = value[0];
        else
        if(name.equals("footerFamily"))
          footerFamily = value[0];
        else
        if(name.equals("footerStyle"))
          footerStyle = value[0];
        else
        if(name.equals("footerWeight"))
          footerWeight = value[0];
        else
        if(name.equals("footerSize"))
          footerSize = value[0];
        else
        if(name.equals("footerPaddingRight"))
          footerPaddingRight = value[0];
        else
        if(name.equals("footerPaddingBottom"))
          footerPaddingBottom = value[0];
        else
        if(name.equals("footerPaddingTop"))
          footerPaddingTop = value[0];
        else
        if(name.equals("footerPaddingLeft"))
          footerPaddingLeft = value[0];
        else
        if(name.equals("submenudtWidth"))
          submenudtWidth = value[0];
        else
        if(name.equals("submenudtSize"))
          submenudtSize = value[0];
        else
        if(name.equals("submenudtStyle"))
          submenudtStyle = value[0];
        else
        if(name.equals("submenudtWeight"))
          submenudtWeight = value[0];
        else
        if(name.equals("submenudtFamily"))
          submenudtFamily = value[0];
        else
        if(name.equals("submenudtTextAlign"))
          submenudtTextAlign = value[0];
        else
        if(name.equals("submenudtBorderStyle"))
          submenudtBorderStyle = value[0];
        else
        if(name.equals("submenudtBorderWidth"))
          submenudtBorderWidth = value[0];
        else
        if(name.equals("submenudtBorderColor"))
          submenudtBorderColor = value[0];
        else
        if(name.equals("submenudtBackgroundColor"))
          submenudtBackgroundColor = value[0];
        else
        if(name.equals("mainmenuh1MarginRight"))
          mainmenuh1MarginRight = value[0];
        else
        if(name.equals("mainmenuh1MarginLeft"))
          mainmenuh1MarginLeft = value[0];
        else
        if(name.equals("mainmenuh1BorderBottomStyle"))
          mainmenuh1BorderBottomStyle = value[0];
        else
       if(name.equals("submenuddBackgroundColor"))
          submenuddBackgroundColor = value[0];
        else
        if(name.equals("submenuddBorderWidth"))
          submenuddBorderWidth = value[0];
        else
        if(name.equals("mainmenuh1MarginBottom"))
          mainmenuh1MarginBottom = value[0];
        else
        if(name.equals("mainmenuh1FontSize"))
          mainmenuh1FontSize = value[0];
        else
        if(name.equals("mainmenuh1Width"))
          mainmenuh1Width = value[0];
        else
        if(name.equals("mainmenuh1TextAlign"))
          mainmenuh1TextAlign = value[0];
        else
        if(name.equals("mainmenuh1BorderRightColor"))
          mainmenuh1BorderRightColor = value[0];
        else
        if(name.equals("mainmenuh1MarginTop"))
          mainmenuh1MarginTop = value[0];
        else
        if(name.equals("mainmenuh1BorderBottomColor"))
          mainmenuh1BorderBottomColor = value[0];
        else
        if(name.equals("mainmenuh1BorderTopStyle"))
          mainmenuh1BorderTopStyle = value[0];
        else
        if(name.equals("headerrepeatFamily"))
          headerrepeatFamily = value[0];
        else
        if(name.equals("headerrepeatColor"))
          headerrepeatColor = value[0];
        else
        if(name.equals("submenudtColor"))
          submenudtColor = value[0];
        else
        if(name.equals("pageInputColor"))
          pageInputColor = value[0];
        else
        if(name.equals("pageInputBackgroundColor"))
          pageInputBackgroundColor = value[0];
        else
        if(name.equals("pageInputFontFamily"))
          pageInputFontFamily = value[0];
        else
        if(name.equals("pageInputFontSize"))
          pageInputFontSize = value[0];
        else
        if(name.equals("pageInputFontWeight"))
          pageInputFontWeight = value[0];
        else
        if(name.equals("highlightmenuitemColor"))
          highlightmenuitemColor = value[0];
        else
        if(name.equals("highlightmenuitemBackgroundColor"))
          highlightmenuitemBackgroundColor = value[0];
        else
        if(name.equals("mainmenuddHeight"))
          mainmenuddHeight = value[0];
      }

      styleName = generalUtils.deSanitise((styleName));
      
      long startTime = new java.util.Date().getTime();

      String imagesDir     = directoryUtils.getSupportDirs('I');
      String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
      String defnsDir      = directoryUtils.getSupportDirs('D');
      String imagesLibraryDir = directoryUtils.getImagesDir(dnm);

      if(! imagesLibraryDir.startsWith("http://"))
        imagesLibraryDir = "http://" + men + imagesLibraryDir;              
              
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
      Statement stmt = null;
      ResultSet rs   = null;

      if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
      {
        messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "WikiStyling", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 7081, bytesOut[0], 0, "ACC:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }

      if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
      {
        messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "WikiStyling", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 7081, bytesOut[0], 0, "SID:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }
  
      String fileName;
      if(source.equals("Z"))
        fileName = "/Zara/Support/Css/" + styleName + "/general.css";
      else fileName = "/Zara/" + dnm + "/Css/" + styleName + "/general.css";

      RandomAccessFile fh = generalUtils.create(fileName + ".new");

      if(fh == null) // just-in-case
      {
        messagePage.msgScreen(false, out, req, 23, unm, sid, uty, men, den, dnm, bnm, "WikiStyling", imagesDir, localDefnsDir, defnsDir, bytesOut);
        serverUtils.etotalBytes(req, unm, dnm, 7081, bytesOut[0], 0, "ERR:");
        if(con != null) con.close();
        if(out != null) out.flush();
        return;
      }

      updateStyling(headerLogo, plainLogo, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, dnm);
      
      output(fh, "/* " + layoutType + " */\n");

      output(fh, "#second { margin-left:160px; background-color: transparent; position:relative; }\n");
      output(fh, "#third { margin-left:160px; background-color: transparent; position:relative; }\n");
      
      // body
    
      output(fh, "body { margin: 0 0 0 0; padding: 0 0 0 0; border-color: " + bodyBorderColor + "; border-width: " + bodyBorderWidth + "; border-style: "
               + bodyBorderStyle + "; }\n");

      // bigouter
      
      output(fh, "div#bigouter { margin: 0 0 0 0; padding: 0 0 0 0; width: 100%; ");
      
      if(layoutType.equals("3") || layoutType.equals("4") || layoutType.equals("5")) // fixed width
      {      
        if(bigouterBackgroundImage.length() > 0)
          output(fh, "background-image: url(\"" + imagesLibraryDir + bigouterBackgroundImage + "\"); background-repeat: " + bigouterBackgroundRepeat + ";");
        output(fh, "background-color: " + bigouterBackgroundColor + ";");
      }
      
      output(fh, " }\n");

      // outer
      
      output(fh, "div#outer { margin-top: 0; margin-bottom: 0; margin-left: auto; margin-right: auto; padding-top: " + px(outerPaddingTop)
               + "; padding-bottom: " + px(outerPaddingBottom) + "; padding-left: " + px(outerPaddingLeft) + "; padding-right: " + px(outerPaddingRight)
               + "; background-color: " + outerBackgroundColor + "; ");
      
      if(layoutType.equals("3") || layoutType.equals("4") || layoutType.equals("5")) // fixed width
        output(fh, "width: " + px(outerWidth) + ";");
      else output(fh, "width: 100%;");
      
      if(outerBackgroundImage.length() > 0)
      {
        output(fh, "background-image: url(\"" + imagesLibraryDir + outerBackgroundImage + "\"); background-repeat: " + outerBackgroundRepeat + ";"
                 + "background-position: " + outerBackgroundPosition + ";");
      }

      output(fh, " }\n");
      
      // header
      
      output(fh, "div#header { margin: 0 0 0 0; }\n");
      
      // headerrepeat
      
      output(fh, "div#headerrepeat { margin: 0 0 0 0; padding: 0 0 0 0; font-size: " + fs(headerrepeatSize)  + "; font-family: " + fa(headerrepeatFamily) + "; font-style: " + headerrepeatStyle + "; text-align: " + headerrepeatTextAlign
                + "; height: " + px(headerrepeatHeight) + "; color: " + headerrepeatColor + ";");

      if(headerrepeatBackgroundImage.length() > 0)
        output(fh, "background-image: url(\"" + imagesLibraryDir + headerrepeatBackgroundImage + "\"); background-repeat: repeat;");
      
      output(fh, " }\n");

      // mainmenu
      
      output(fh, "div#mainmenu { margin: 0 0 0 0; float: left; ");

      if(layoutType.equals("3") || layoutType.equals("4") || layoutType.equals("5")) // fixed width
        output(fh, "width: 100%; ");
      else output(fh, "width: " + px(mainmenudtWidth) + ";");

      output(fh, "padding-right: " + px(mainmenuPaddingRight) + "; padding-bottom: " + px(mainmenuPaddingBottom) + "; padding-top: " + px(mainmenuPaddingTop) + "; padding-left: " + px(mainmenuPaddingLeft) + "; ");

      output(fh, "background-color: " + mainmenuBackgroundColor + "; font-size: " + fs(mainmenuSize)  + "; font-family: " + fa(mainmenuFamily) + "; font-style: "
               + mainmenuStyle + "; font-weight: " + mainmenuWeight + "; border-width: " + px(mainmenuBorderWidth) + "; border-style: " + mainmenuBorderStyle
               + "; border-color: " + mainmenuBorderColor + "; }\n");

      // mainmenu dt
      
      output(fh, "div#mainmenu dt { cursor: pointer; height: " + px(mainmenudtHeight) + "; line-height: " + px(mainmenudtHeight) + "; ");

      if(layoutType.equals("3") || layoutType.equals("4") || layoutType.equals("5")) // fixed width
        output(fh, "width: " + px(mainmenuWidth) + "; ");
      else output(fh, "width: " + px(mainmenudtWidth) + "; ");
      
      output(fh, "color: " + mainmenudtColor + "; background-color: " + mainmenudtBackgroundColor + "; text-align: " + mainmenudtTextAlign + "; border-width: "
               + px(mainmenudtBorderWidth) + "; border-style: " + mainmenudtBorderStyle + "; border-color: " + mainmenudtBorderColor + "; }\n");

      // mainmenu dt a
      
      output(fh, "div#mainmenu dt a { color: " + mainmenudtColor + "; background-color: " + mainmenudtBackgroundColor + "; }\n");

      // mainmenu li a
      
      output(fh, "div#mainmenu li a { color: " + mainmenuliaColor + "; }\n");

      // mainmenu li a,
      
      output(fh, "div#mainmenu li a, div#mainmenu dt a { text-decoration: none; display: block; }\n");

      // mainmenu li a:hover
     
      output(fh, "div#mainmenu li a:hover, div#mainmenu dt a:hover { color: " + mainmenuliahoverColor + "; background-color: "
               + mainmenuliahoverBackgroundColor + "; }\n");
     
      // mainmenu dd
     
      output(fh, "div#mainmenu dd { line-height: " + mainmenuddHeight + "px; background-color: " + mainmenuddBackgroundColor + "; border-width: " + px(mainmenuddBorderWidth) + "; border-style: "
             + mainmenuddBorderStyle + "; border-color: " + mainmenuddBorderColor + "; position: absolute; z-index: 2; padding: 0 0 0 0; margin-right: 0;"
             + " margin-bottom: 0; margin-top: 0; margin-left: " + px(mainmenuddMarginLeft) + "; }\n");
 
      // mainmenu dl
     
      output(fh, "div#mainmenu dl { background-color: " + mainmenudtBackgroundColor + "; padding-right: " + px(mainmenudlPaddingRight) + "; padding-top: "
             + px(mainmenudlPaddingTop) + "; padding-left: " + px(mainmenudlPaddingLeft) + "; padding-bottom: " + px(mainmenudlPaddingBottom)
             + "; margin-top: " + px(mainmenudlMarginTop) + "; margin-bottom: 0px; margin-left: 0px; margin-right: 0px; float: left; }\n");

      // mainmenu li
     
      output(fh, "div#mainmenu li { width: " + px(mainmenuliWidth) + "; list-style-type: none; }\n");
     
      // mainmenu h1
     
      output(fh, "div#mainmenu h1 { color: " + mainmenuh1Color + "; background-color: " + mainmenuh1BackgroundColor + "; width: " + px(mainmenuh1Width)
             + "; border-left-width: " + px(mainmenuh1BorderLeftWidth) + "; border-left-style: " + mainmenuh1BorderLeftStyle + "; border-left-color: "
             + mainmenuh1BorderLeftColor + "; border-right-width: " + px(mainmenuh1BorderRightWidth) + "; border-right-style: " + mainmenuh1BorderRightStyle
             + "; border-right-color: " + mainmenuh1BorderRightColor + "; border-top-width: " + px(mainmenuh1BorderTopWidth) + "; border-top-style: "
             + mainmenuh1BorderTopStyle + "; border-top-color: " + mainmenuh1BorderTopColor + "; border-bottom-width: " + px(mainmenuh1BorderBottomWidth)
             + "; border-bottom-style: " + mainmenuh1BorderBottomStyle + "; border-bottom-color: " + mainmenuh1BorderBottomColor
             + "; padding: 2px 2px 2px 2px; float: left; margin-right: " + px(mainmenuh1MarginRight) + "; margin-bottom: " + px(mainmenuh1MarginBottom)
             + "; margin-top: " + px(mainmenuh1MarginTop) + "; margin-left: " + px(mainmenuh1MarginLeft) + "; text-align: " + mainmenuh1TextAlign
             + "; font-size: " + fs(mainmenuh1FontSize) + "; font-family: " + fa(mainmenuh1Family) + "; font-style: " + mainmenuh1Style + "; font-weight: "
             + mainmenuh1Weight + "; }\n");
   
      // submenu
     
      output(fh, "div#submenu { float: left; }\n");
     
      // submenu dl
     
      output(fh, "div#submenu dl { float: left; padding-right: " + px(submenudlPaddingRight) + "; padding-bottom: " + px(submenudlPaddingBottom)
               + "; padding-top: " + px(submenudlPaddingTop) + "; padding-left: " + px(submenudlPaddingLeft) + "; position: relative; margin: 0 0 0 0; }\n");
 
      // submenu dd
     
      output(fh, "div#submenu dd { z-index: 2; width: 100%; position: absolute; background-color: " + submenuddBackgroundColor + "; border-width: "
               + px(submenuddBorderWidth) + "; border-style: " + submenuddBorderStyle + "; border-color: " + submenuddBorderColor + "; }\n");
 
 
      // submenu ul
     
      output(fh, "div#submenu ul { padding: 0 0 0 0; margin: 0 0 0 0; }\n");
 
      // submenu li
     
      output(fh, "div#submenu li { padding: 0 0 0 0; margin: 0 0 0 0; height: " + px(submenuliHeight) + "; line-height: " + px(submenuliHeight)
               + "; list-style-type: none; text-align: " + submenuliTextAlign + "; font-size: " + fs(submenuliSize) + "; font-weight: "+ submenuliWeight
               +  "; font-style: " + submenuliStyle + "; font-family: " + fa(submenuliFamily) + "; }\n");
 
      // submenu li a,
     
      output(fh, "div#submenu li a, div#submenu dt a { text-decoration: none; display: block; border: 0 none; }\n");
 
      // submenu li a
     
      output(fh, "div#submenu li a { color: " + submenuliaColor + "; }\n");
 
      // submenu dt a
     
      output(fh, "div#submenu dt a { color: " + submenudtColor + "; }\n");
 
      // submenu la a:hover, div#submenu dt a:hover
     
      output(fh, "div#submenu li a:hover, div#submenu dt a:hover { color: " + submenuliahoverColor + "; background-color: " + submenuliahoverBackgroundColor
               + "; }\n");
 
      // submenu dt
     
      output(fh, "div#submenu dt { color: " + submenudtColor + "; background-color: " + submenudtBackgroundColor + "; width: " + px(submenudtWidth)
              + "; border-width: " + px(submenudtBorderWidth) + "; border-style: " + submenudtBorderStyle + "; border-color: " + submenudtBorderColor
              + "; text-align: " + submenudtTextAlign + "; font-family: " + fa(submenudtFamily) + "; font-style: " + submenudtStyle + "; font-weight: "
              + submenudtWeight + "; font-size: " + fs(submenudtSize) + "; padding-left: 2px; padding-right: 2px;  }\n");
     
      // main
     
      output(fh, "div#main { background-color: transparent; margin: 0 0 0 0; padding: 0 0 0 0; border-left-width: " + px(mainBorderLeftWidth)
               + "; border-left-style: " + mainBorderLeftStyle + "; border-left-color: " + mainBorderLeftColor + "; border-right-width: "
               + px(mainBorderRightWidth) + "; border-right-style: " + mainBorderRightStyle + "; border-right-color: " + mainBorderRightColor
               + "; border-top-width: " + px(mainBorderTopWidth) + "; border-top-style: " + mainBorderTopStyle + "; border-top-color: " + mainBorderTopColor
               + "; border-bottom-width: " + px(mainBorderBottomWidth) + "; border-bottom-style: " + mainBorderBottomStyle + "; border-bottom-color: "
               + mainBorderBottomColor + "; }\n");

      // footer
     
      output(fh, "div#footer { color: " + footerColor + "; background-color: " + footerBackgroundColor + "; margin: 0 0 0 0; border-left-width: "
               + px(footerBorderLeftWidth) + "; border-left-style: " + footerBorderLeftStyle + "; border-left-color: " + footerBorderLeftColor
               + "; border-right-width: " + px(footerBorderRightWidth) + "; border-right-style: " + footerBorderRightStyle + "; border-right-color: "
               + footerBorderRightColor + "; border-top-width: " + px(footerBorderTopWidth) + "; border-top-style: " + footerBorderTopStyle
               + "; border-top-color: " + footerBorderTopColor + "; border-bottom-width: " + px(footerBorderBottomWidth) + "; border-bottom-style: "
               + footerBorderBottomStyle + "; border-bottom-color: " + footerBorderBottomColor + "; text-align: " + footerTextAlign + "; font-family: "
               + fa(footerFamily) + "; font-style: " + footerStyle + "; font-weight: " + footerWeight + "; font-size: " + fs(footerSize) + "; padding-right: "
               + px(footerPaddingRight) + "; padding-bottom: " + px(footerPaddingBottom) + "; padding-top: " + px(footerPaddingTop) + "; padding-left: "
               + px(footerPaddingLeft) + "; }\n");
      
      // mainmenu item highlight alert

      output(fh, "#highlightmenuitem { color: " + highlightmenuitemColor + "; background-color: " + highlightmenuitemBackgroundColor + "; }\n");
      
      // Option Text
    
      output(fh, "#optional { color: " + optionalColor + "; background-color: " + outerBackgroundColor + "; font-weight: " + optionalFontWeight + "; font-size: "
              + fs(optionalFontSize) + "; font-family: " + fa(optionalFontFamily) + "; }\n");
            
      // Title Bar
    
      output(fh, "#title { color: " + titleBarColor + "; background-color: " + titleBarBackgroundColor + "; border-top-width: " + px(titleBarBorderTopWidth)
               + "; border-top-color: " + titleBarBorderTopColor + "; border-top-style: " + titleBarBorderTopStyle + "; border-bottom-width: "
               + px(titleBarBorderBottomWidth) + "; border-bottom-color: " + titleBarBorderBottomColor + "; border-bottom-style: "
               + titleBarBorderBottomStyle + "; font-family: " + fa(titleBarFontFamily) + "; font-size: " + fs(titleBarFontSize) + "; font-weight: "
               + titleBarFontWeight + "; }\n");

      output(fh, "#title a:link { color: " + titleBarALinkColor + "; text-decoration: " + titleBarALinkTextDecoration + "; background-color: "
               + titleBarBackgroundColor + "; font-family: " + fa(titleBarLinkFontFamily) + "; font-size: " + fs(titleBarLinkFontSize) + "; font-weight: "
               + titleBarLinkFontWeight + "; }\n");
     
      output(fh, "#title a:visited { color: " + titleBarALinkColor + "; text-decoration: " + titleBarALinkTextDecoration + "; background-color: "
               + titleBarBackgroundColor + "; font-family: " + fa(titleBarLinkFontFamily) + "; font-size: " + fs(titleBarLinkFontSize) + "; font-weight: "
               + titleBarLinkFontWeight + "; }\n");
      
      output(fh, "#title a:active { color: " + titleBarALinkColor + "; text-decoration: " + titleBarALinkTextDecoration + "; background-color: "
               + titleBarBackgroundColor + "; font-family: " + fa(titleBarLinkFontFamily) + "; font-size: " + fs(titleBarLinkFontSize) + "; font-weight: "
               + titleBarLinkFontWeight + "; }\n");
    
      output(fh, "#title a:hover { color: " + titleBarAHoverColor + "; text-decoration: " + titleBarAHoverTextDecoration + "; background-color: "
               + titleBarBackgroundColor + ";  font-family: " + fa(titleBarLinkFontFamily) + "; font-size: " + fs(titleBarLinkFontSize) + "; font-weight: "
               + titleBarLinkFontWeight + "; }\n");
     
      // Page
    
      output(fh, "#pageColumn { font-family: " + fa(pageColumnFontFamily) + "; font-size: " + fs(pageColumnFontSize) + "; font-weight: " + pageColumnFontWeight
               + "; background-color: " + pageColumnBackgroundColor + "; }\n");
      
      output(fh, "#page p { margin: 0; color: " + pagePColor + "; font-family: " + fa(pagePFontFamily) + "; font-size: " + fs(pagePFontSize) + "; font-weight: "
               + pagePFontWeight + "; background-color: " + pagePBackgroundColor + "; }\n");
    
      output(fh, "#page input { color: " + pageInputColor + "; background-color: " + pageInputBackgroundColor + "; font-family: " + fa(pageInputFontFamily)
                + "; font-size: " + fs(pageInputFontSize) + "; font-weight: " + pageInputFontWeight + "; }\n");
      
      output(fh, "#page select { color: " + pageInputColor + "; background-color: " + pageInputBackgroundColor + "; font-family: " + fa(pageInputFontFamily)
               + "; font-size: " + fs(pageInputFontSize) + "; font-weight: " + pageInputFontWeight + "; }\n");

      output(fh, "#page textarea { color: " + pageInputColor + "; background-color: " + pageInputBackgroundColor + "; font-family: " + fa(pageInputFontFamily)
               + "; font-size: " + fs(pageInputFontSize) + "; font-weight: " + pageInputFontWeight + "; }\n");

      output(fh, "#page h1 { font-family: " + fa(pageH1FontFamily) + "; font-size: " + fs(pageH1FontSize) + "; font-weight: " + pageH1FontWeight + "; color: "
               + pageH1Color + "; margin-top: 12px; border-top-width: " + px(pageH1BorderTopWidth) + "; border-top-color: " + pageH1BorderTopColor
               + "; border-top-style: " + pageH1BorderTopStyle + "; border-bottom-width: " + px(pageH1BorderBottomWidth) + "; border-bottom-color: "
               + pageH1BorderBottomColor + "; border-bottom-style: " + pageH1BorderBottomStyle + "; background-color: " + pageH1BackgroundColor + "; }\n");
    
      output(fh, "#page a:link { font-family: " + fa(pageALinkFontFamily) + "; font-size: " + fs(pageALinkFontSize) + "; font-weight: " + pageALinkFontWeight
               + "; color: " + pageALinkColor + "; text-decoration: " + pageALinkTextDecoration + "; background-color: transparent; }\n");

      output(fh, "#page a:visited { color: " + pageALinkColor + "; text-decoration: " + pageALinkTextDecoration + "; background-color: transparent"// + outerBackgroundColor
               + "; font-family: " + fa(pageALinkFontFamily) + "; font-size: " + fs(pageALinkFontSize) + "; font-weight: " + pageALinkFontWeight + "; }\n");
      
      output(fh, "#page a:active { color: " + pageALinkColor + "; text-decoration: " + pageALinkTextDecoration + "; background-color: transparent"// + outerBackgroundColor
               + "; font-family: " + fa(pageALinkFontFamily) + "; font-size: " + fs(pageALinkFontSize) + "; font-weight: " + pageALinkFontWeight + "; }\n");

      output(fh, "#page a:hover { color: " + pageAHoverColor + "; text-decoration: " + pageAHoverTextDecoration + "; background-color: transparent"// + outerBackgroundColor
               + ";font-family: " + fa(pageALinkFontFamily) + "; font-size: " + fs(pageALinkFontSize) + "; font-weight: " + pageALinkFontWeight + "; }\n");
 
      output(fh, "#line1 { color: " + line1Color + "; font-family: " + fa(pagePFontFamily) + "; font-size: " + fs(pagePFontSize) + "; font-weight: "
               + pagePFontWeight + "; background-color: " + line1BackgroundColor + "; }\n");
    
      output(fh, "#line2 { color: " + line2Color + "; font-family: " + fa(pagePFontFamily) + "; font-size: " + fs(pagePFontSize) + "; font-weight: "
               + pagePFontWeight + "; background-color: " + line2BackgroundColor + "; }\n");
      
      // Service Help Link on Main Page
    
      output(fh, "#service p { color: " + serviceMainColor + "; background-color: " + outerBackgroundColor
                + "; font-family: " + fa(serviceMainFontFamily) + "; font-size: " + fs(serviceMainFontSize) + "; font-weight: " + serviceMainFontWeight + "; }\n");
      
      output(fh, "#service a:link { color: " + serviceMainALinkColor + "; text-decoration: " + serviceMainALinkTextDecoration + "; background-color: "
                + outerBackgroundColor + "; font-family: " + fa(serviceMainFontFamily) + "; font-size: " + fs(serviceMainFontSize) + "; font-weight: "
                + serviceMainFontWeight + "; }\n");
      
      output(fh, "#service a:visited { color: " + serviceMainALinkColor + "; text-decoration: " + serviceMainALinkTextDecoration + "; background-color: "
               + outerBackgroundColor + "; font-family: " + fa(serviceMainFontFamily) + "; font-size: " + fs(serviceMainFontSize) + "; font-weight: "
               + serviceMainFontWeight + "; }\n");
      
      output(fh, "#service a:active { color: " + serviceMainALinkColor + "; text-decoration: " + serviceMainALinkTextDecoration + "; background-color: "
               + outerBackgroundColor + "; font-family: " + fa(serviceMainFontFamily) + "; font-size: " + fs(serviceMainFontSize) + "; font-weight: "
               + serviceMainFontWeight + "; }\n");

      output(fh, "#service a:hover { color: " + serviceMainAHoverColor + "; text-decoration: " + serviceMainAHoverTextDecoration + "; background-color: "
               + outerBackgroundColor + "; font-family: " + fa(serviceMainFontFamily) + "; font-size: " + fs(serviceMainFontSize) + "; font-weight: "
               + serviceMainFontWeight + "; }\n");
      
      // misc
    
      output(fh, "#textErrorLarge { color: " + textErrorLargeColor + "; background-color: " + outerBackgroundColor + "; font-family: "
               + fa(textErrorLargeFontFamily) + "; font-size: " + fs(textErrorLargeFontSize) + "; font-weight: " + textErrorLargeFontWeight+ "; }\n");

      output(fh, "#textNumericValue { color: " + textNumericColor + "; background-color: " + outerBackgroundColor + "; font-family: "
               + fa(textNumericFontFamily) + "; font-size: " + fs(textNumericFontSize) + "; font-weight: " + textNumericFontWeight + "; }\n");
 
      output(fh, "#textSalesCreditNote { color: " + textSalesCreditNoteColor + "; background-color: " + outerBackgroundColor + "; font-family: "
               + fa(textSalesCreditNoteFontFamily) + "; font-size: " + fs(textSalesCreditNoteFontSize) + "; font-weight: " + textSalesCreditNoteFontWeight + "; }\n");
    
      output(fh, "#textReceipt { color: " + textReceiptColor + "; background-color: " + outerBackgroundColor + "; font-family: "
               + fa(textReceiptFontFamily) + "; font-size: " + fs(textReceiptFontSize) + "; font-weight: " + textReceiptFontWeight + "; }\n");
     
      output(fh, "#textInvoice { color: " + textInvoiceColor + "; background-color: " + outerBackgroundColor + "; font-family: "
                 + fa(textInvoiceFontFamily) + "; font-size: " + fs(textInvoiceFontSize) + "; font-weight: " + textInvoiceFontWeight + "; }\n");
    
      output(fh, "#textRedHighlighting { color: " + textRedHighlightingColor + "; background-color: " + outerBackgroundColor + "; font-family: "
                + fa(textRedHighlightingFontFamily) + "; font-size: " + fs(textRedHighlightingFontSize) + "; font-weight: " + textRedHighlightingFontWeight
                + "; }\n");
    
      // libraries
    
      output(fh, "#directoryCell { background-color: " + libraryBackgroundColor + "; border-color: " + libraryBorderColor + "; border-width: "
               + libraryBorderWidth + "; border-style: " + libraryBorderStyle + "; }\n");
                                                                                                
      output(fh, "#directory p { font-weight: " + libraryPFontWeight + "; font-size: " + fs(libraryPFontSize) + "; font-family: " + fa(libraryPFontFamily)
               + "; color: " + libraryPColor + "; background-color: " + libraryPBackgroundColor + "; }\n");
    
      output(fh, "#directory { background-color: " + libraryTableBackgroundColor + "; }\n");
    
      output(fh, "#directory a:link { font-weight: " + libraryALinkFontWeight + "; font-size: " + fs(libraryALinkFontSize) + "; font-family: " + fa(libraryALinkFontFamily) + "; color: " + libraryALinkColor + "; text-decoration: "
               + libraryALinkTextDecoration + "; background-color: transparent; }\n");

      output(fh, "#directory a:visited { font-weight: " + libraryALinkFontWeight + "; font-size: " + fs(libraryALinkFontSize) + "; font-family: " + fa(libraryALinkFontFamily) + "; color: " + libraryALinkColor + "; text-decoration: "
               + libraryALinkTextDecoration + "; background-color: transparent; }\n");
      
      output(fh, "#directory a:active { font-weight: " + libraryALinkFontWeight + "; font-size: " + fs(libraryALinkFontSize) + "; font-family: " + fa(libraryALinkFontFamily) + "; color: " + libraryALinkColor + "; text-decoration: "
               + libraryALinkTextDecoration + "; background-color: transparent; }\n");
                   
      output(fh, "#directory a:hover { font-weight: " + libraryAHoverFontWeight + "; font-size: " + fs(libraryAHoverFontSize) + "; font-family: " + fa(libraryAHoverFontFamily) + "; color: " + libraryAHoverColor + "; text-decoration: "
               + libraryAHoverTextDecoration + "; background-color: transparent; }\n");

      // Channels

      // #channel {  color: red; background-color: blue; font-weight: normal; font-size: 12pt; font-family: serif; }
      output(fh, "#channel { color: " + channelFontColor + "; background-color: " + channelBackgroundColor + "; font-weight: " + channelFontWeight + "; font-size: " + fs(channelFontSize) + "; font-family: " + fa(channelFontFamily) + "; }\n");

      // #channelHeader { color: red; border-width: 13px; border-style: dashed; border-color: red; background-color: white; font-weight: normal; font-size: 22pt; font-family: Verdana,Arial,Helvetica,sans-serif; }
      output(fh, "#channelHeader { color: " + channelHeaderFontColor + "; background-color: " + channelHeaderBackgroundColor + "; border-width: " + px(channelHeaderBorderWidth) + "; border-style: " + channelHeaderBorderStyle + "; border-color: "
                + channelHeaderBorderColor + "; font-weight: " + channelHeaderFontWeight + "; font-size: " + fs(channelHeaderFontSize) + "; font-family: " + fa(channelHeaderFontFamily) + "; }\n");

      // #channelMessage textarea { color: red; background-color: yellow; font-weight: bold; font-size: 13pt; font-family: Verdana,Arial,Helvetica,sans-serif; }
      output(fh, "#channelMessage textarea {  color: " + channelMsgFontColor + "; background-color: " + channelMsgBackgroundColor + "; font-weight: " + channelMsgFontWeight + "; font-size: " + fs(channelMsgFontSize) + "; font-family: "
               + fa(channelMsgFontFamily) + "; }\n");

      // #channelButtons { color: red; background-color: cyan; font-weight: bold; font-size: 13pt; font-family: Verdana,Arial,Helvetica,sans-serif; }
      output(fh, "#channelButtons { color: " + channelButtonsFontColor + "; background-color: " + channelButtonsBackgroundColor + "; font-weight: " + channelButtonsFontWeight + "; font-size: " + fs(channelButtonsFontSize) + "; font-family: "
                + fa(channelButtonsFontFamily) + "; }\n");

      // #channelButtons a:link { font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 10pt; font-weight: normal; color: darkmagenta; text-decoration: none; background-color: transparent; }
      output(fh, "#channelButtons a:link { font-weight: " + channelButtonsALinkFontWeight + "; font-size: " + fs(channelButtonsALinkFontSize) + "; font-family: " + fa(channelButtonsALinkFontFamily) + "; color: " + channelButtonsALinkColor
               + "; text-decoration: " + channelButtonsALinkTextDecoration + "; background-color: transparent; }\n");

      output(fh, "#channelButtons a:visited { font-weight: " + channelButtonsALinkFontWeight + "; font-size: " + fs(channelButtonsALinkFontSize) + "; font-family: " + fa(channelButtonsALinkFontFamily) + "; color: " + channelButtonsALinkColor
               + "; text-decoration: " + channelButtonsALinkTextDecoration + "; background-color: transparent; }\n");

      output(fh, "#channelButtons a:active { font-weight: " + channelButtonsALinkFontWeight + "; font-size: " + fs(channelButtonsALinkFontSize) + "; font-family: " + fa(channelButtonsALinkFontFamily) + "; color: " + channelButtonsALinkColor
               + "; text-decoration: " + channelButtonsALinkTextDecoration + "; background-color: transparent; }\n");

      // #channelButtons a:hover { color: navy; text-decoration: none; background-color: transparent;font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 10pt; font-weight: normal; }

      output(fh, "#channelButtons a:hover { font-weight: " + channelButtonsAHoverFontWeight + "; font-size: " + fs(channelButtonsAHoverFontSize) + "; font-family: " + fa(channelButtonsAHoverFontFamily) + "; color: " + channelButtonsAHoverColor
               + "; text-decoration: " + channelButtonsAHoverTextDecoration + "; background-color: transparent; }\n");

      generalUtils.fileClose(fh);

      generalUtils.copyFileToFile(fileName + ".new", fileName);
      
      generalUtils.fileDelete(fileName + ".new");
      
      refetch(out, unm, sid, uty, dnm, men, den, bnm, localDefnsDir, styleName, source);

      serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7081, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
      if(con != null) con.close();
      if(out != null) out.flush();      
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "WikiStylingProcess", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7081, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean updateStyling(String headerLogo, String plainLogo, String footerText, String pageHeaderImage1, String pageHeaderImage2,
                                String pageHeaderImage3, String pageHeaderImage4, String pageHeaderImage5, String dnm) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
 
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_sitewiki?user=" + uName + "&password=" + pWord);
      stmt = con.createStatement();
           
      String q = "UPDATE styling SET HeaderLogo = '" + generalUtils.sanitiseForSQL(headerLogo) + "', HeaderLogoRepeat = '" + generalUtils.sanitiseForSQL(plainLogo)
               + "', FooterText = '" + generalUtils.sanitiseForSQL(footerText) + "', PageHeaderImage1 = '" + generalUtils.sanitiseForSQL(pageHeaderImage1)
               + "', PageHeaderImage2 = '" + generalUtils.sanitise(pageHeaderImage2) + "', PageHeaderImage3 = '" + generalUtils.sanitiseForSQL(pageHeaderImage3)
               + "', PageHeaderImage4 = '" + generalUtils.sanitiseForSQL(pageHeaderImage4) + "', PageHeaderImage5 = '" + generalUtils.sanitiseForSQL(pageHeaderImage5) + "'";
      
      stmt.executeUpdate(q);
        
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
      
      return true;
    }
    catch(Exception e)
    {
      System.out.println("7081b: " + e);
      if(stmt != null) stmt.close();
      if(con  != null) con.close();
    }
    
    return false;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void refetch(PrintWriter out, String unm, String sid, String uty, String dnm, String men, String den, String bnm, String localDefnsDir,
                       String styleName, String source) throws Exception
  {
    URL url = new URL("http://" + serverUtils.serverToCall("OFSA", localDefnsDir) + "/central/servlet/WikiStylingEdit?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(styleName) + "&p2="
                    + source + "&bnm=" + bnm);
    
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();
    while(s != null)
    {
      out.println(s);
      s = di.readLine();
    }

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void output(RandomAccessFile fh, String s) throws Exception
  {
    fh.writeBytes(s);
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String px(String s) throws Exception
  {
    s = generalUtils.stripAllNonNumeric(s);
    if(s == null || s.length() == 0)
      return "0px";
    return s + "px";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String fs(String s) throws Exception
  {
    s = generalUtils.stripAllNonNumeric(s);
    if(s == null || s.length() == 0)
      return "10pt";
    return s + "pt";
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String fa(String s) throws Exception
  {
    if(s.equals("sans-serif"))
      return "Verdana,Arial,Helvetica,sans-serif";
    return s;
  }

}
