// =======================================================================================================================================================================================================
// System: ZaraStar Admin: Edit styling
// Module: WikiStylingEdit.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
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

public class WikiStylingEditWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Wiki wiki = new Wiki();
  DashboardUtils dashboardUtils = new DashboardUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // style
      p2  = req.getParameter("p2"); // source

      doIt(out, req, p1, p2, unm, sid, uty, men, den, dnm, bnm, bytesOut);
    }
    catch(Exception e)
    {
      StringBuffer url = req.getRequestURL();
      int x=0;
      if(url.charAt(x) == 'h' || url.charAt(x) == 'H')
        x += 7;
      String urlBit="";
      while(url.charAt(x) != '\000' && url.charAt(x) != ',' && url.charAt(x) != '/')
        urlBit += url.charAt(x++);

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "WikiStylingEdit", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7081, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String p1, String p2, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir    = directoryUtils.getLocalOverrideDir(dnm);
    String imagesLibraryDir = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if((! serverUtils.isSysAdmin(con, stmt, rs, unm, dnm, sid, uty, localDefnsDir, defnsDir)) && (! serverUtils.isDBAdmin(con, stmt, rs, unm)))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "WikiStylingEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7081, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "WikiStylingEdit", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7081, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    imagesLibraryDir = men + imagesLibraryDir;
    if(! imagesLibraryDir.startsWith("http://"))
      imagesLibraryDir = "http://" + imagesLibraryDir;

    int imagesLibraryDirLen = imagesLibraryDir.length();

    scoutln(out, bytesOut, "7081\001Styling\001Styling\001javascript:getHTML('WikiStylingEditWave','')\001\001\001\001\003");

    set(con, stmt, rs, out, p1, p2, unm, sid, uty, men, den, dnm, bnm, imagesLibraryDirLen, imagesDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7081, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String styleName, String source, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                   int imagesLibraryDirLen, String imagesDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function update(){postForm('WikiStylingProcessWave','7081bw');}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    scoutln(out, bytesOut, "<style type='text/css'>'");
    scoutln(out, bytesOut, "option.transparent{background-color:white; color:black}");
    scoutln(out, bytesOut, "option.aliceblue{background-color:aliceblue; color:black}");
    scoutln(out, bytesOut, "option.antiquewhite{background-color:antiquewhite; color:black}");
    scoutln(out, bytesOut, "option.aqua{background-color:aqua; color:black}");
    scoutln(out, bytesOut, "option.aquamarine{background-color:aquamarine; color:black}");
    scoutln(out, bytesOut, "option.azure{background-color:azure; color:black}");
    scoutln(out, bytesOut, "option.beige{background-color:beige; color:black}");
    scoutln(out, bytesOut, "option.bisque{background-color:bisque; color:black}");
    scoutln(out, bytesOut, "option.black{background-color:black; color:white}");
    scoutln(out, bytesOut, "option.blanchedalmond{background-color:blanchedalmond; color:black}");
    scoutln(out, bytesOut, "option.blue{background-color:blue; color:black}");
    scoutln(out, bytesOut, "option.blueviolet{background-color:blueviolet; color:black}");
    scoutln(out, bytesOut, "option.brown{background-color:brown; color:black}");
    scoutln(out, bytesOut, "option.burlywood{background-color:burlywood; color:black}");
    scoutln(out, bytesOut, "option.cadetblue{background-color:cadetblue; color:black}");
    scoutln(out, bytesOut, "option.chartreuse{background-color:chartreuse; color:black}");
    scoutln(out, bytesOut, "option.chocolate{background-color:chocolate; color:black}");
    scoutln(out, bytesOut, "option.coral{background-color:coral; color:black}");
    scoutln(out, bytesOut, "option.cornflowerblue{background-color:cornflowerblue; color:black}");
    scoutln(out, bytesOut, "option.cornsilk{background-color:cornsilk; color:black}");
    scoutln(out, bytesOut, "option.crimson{background-color:crimson; color:black}");
    scoutln(out, bytesOut, "option.cyan{background-color:cyan; color:black}");
    scoutln(out, bytesOut, "option.darkblue{background-color:darkblue; color:black}");
    scoutln(out, bytesOut, "option.darkcyan{background-color:darkcyan; color:black}");
    scoutln(out, bytesOut, "option.darkgoldenrod{background-color:darkgoldenrod; color:black}");
    scoutln(out, bytesOut, "option.darkgray{background-color:darkgray; color:black}");
    scoutln(out, bytesOut, "option.darkgreen{background-color:darkgreen; color:black}");
    scoutln(out, bytesOut, "option.darkkhaki{background-color:darkkhaki; color:black}");
    scoutln(out, bytesOut, "option.darkmagenta{background-color:darkmagenta; color:black}");
    scoutln(out, bytesOut, "option.darkolivegreen{background-color:darkolivegreen; color:black}");
    scoutln(out, bytesOut, "option.darkorange{background-color:darkorange; color:black}");
    scoutln(out, bytesOut, "option.darkorchid{background-color:darkorchid; color:black}");
    scoutln(out, bytesOut, "option.darkred{background-color:darkred; color:black}");
    scoutln(out, bytesOut, "option.darksalmon{background-color:darksalmon; color:black}");
    scoutln(out, bytesOut, "option.darkseagreen{background-color:darkseagreen; color:black}");
    scoutln(out, bytesOut, "option.darkslateblue{background-color:darkslateblue; color:black}");
    scoutln(out, bytesOut, "option.darkslategray{background-color:darkslategray; color:black}");
    scoutln(out, bytesOut, "option.darkturquoise{background-color:darkturquoise; color:black}");
    scoutln(out, bytesOut, "option.darkviolet{background-color:darkviolet; color:black}");
    scoutln(out, bytesOut, "option.deeppink{background-color:deeppink; color:black}");
    scoutln(out, bytesOut, "option.deepskyblue{background-color:deepskyblue; color:black}");
    scoutln(out, bytesOut, "option.dimgray{background-color:dimgray; color:black}");
    scoutln(out, bytesOut, "option.dodgerblue{background-color:dodgerblue; color:black}");
    scoutln(out, bytesOut, "option.firebrick{background-color:firebrick; color:black}");
    scoutln(out, bytesOut, "option.floralwhite{background-color:floralwhite; color:black}");
    scoutln(out, bytesOut, "option.forestgreen{background-color:forestgreen; color:black}");
    scoutln(out, bytesOut, "option.fuchsia{background-color:fuchsia; color:black}");
    scoutln(out, bytesOut, "option.gainsboro{background-color:gainsboro; color:black}");
    scoutln(out, bytesOut, "option.ghostwhite{background-color:ghostwhite; color:black}");
    scoutln(out, bytesOut, "option.gold{background-color:gold; color:black}");
    scoutln(out, bytesOut, "option.goldenrod{background-color:goldenrod; color:black}");
    scoutln(out, bytesOut, "option.gray{background-color:gray; color:black}");
    scoutln(out, bytesOut, "option.green{background-color:green; color:black}");
    scoutln(out, bytesOut, "option.greenyellow{background-color:greenyellow; color:black}");
    scoutln(out, bytesOut, "option.honeydew{background-color:honeydew; color:black}");
    scoutln(out, bytesOut, "option.hotpink{background-color:hotpink; color:black}");
    scoutln(out, bytesOut, "option.indianred{background-color:indianred; color:black}");
    scoutln(out, bytesOut, "option.indigo{background-color:indigo; color:black}");
    scoutln(out, bytesOut, "option.ivory{background-color:ivory; color:black}");
    scoutln(out, bytesOut, "option.khaki{background-color:khaki; color:black}");
    scoutln(out, bytesOut, "option.lavender{background-color:lavender; color:black}");
    scoutln(out, bytesOut, "option.lavenderblush{background-color:lavenderblush; color:black}");
    scoutln(out, bytesOut, "option.lawngreen{background-color:lawngreen; color:black}");
    scoutln(out, bytesOut, "option.lemonchiffon{background-color:lemonchiffon; color:black}");
    scoutln(out, bytesOut, "option.lightblue{background-color:lightblue; color:black}");
    scoutln(out, bytesOut, "option.lightcoral{background-color:lightcoral; color:black}");
    scoutln(out, bytesOut, "option.lightcyan{background-color:lightcyan; color:black}");
    scoutln(out, bytesOut, "option.lightgoldenrodyellow{background-color:lightgoldenrodyellow; color:black}");
    scoutln(out, bytesOut, "option.lightgreen{background-color:lightgreen; color:black}");
    scoutln(out, bytesOut, "option.lightgray{background-color:lightgray; color:black}");
    scoutln(out, bytesOut, "option.lightpink{background-color:lightpink; color:black}");
    scoutln(out, bytesOut, "option.lightsalmon{background-color:lightsalmon; color:black}");
    scoutln(out, bytesOut, "option.lightseagreen{background-color:lightseagreen; color:black}");
    scoutln(out, bytesOut, "option.lightskyblue{background-color:lightskyblue; color:black}");
    scoutln(out, bytesOut, "option.lightslategray{background-color:lightslategray; color:black}");
    scoutln(out, bytesOut, "option.lightsteelblue{background-color:lightsteelblue; color:black}");
    scoutln(out, bytesOut, "option.lightyellow{background-color:lightyellow; color:black}");
    scoutln(out, bytesOut, "option.lime{background-color:lime; color:black}");
    scoutln(out, bytesOut, "option.limegreen{background-color:limegreen; color:black}");
    scoutln(out, bytesOut, "option.linen{background-color:linen; color:black}");
    scoutln(out, bytesOut, "option.magenta{background-color:magenta; color:black}");
    scoutln(out, bytesOut, "option.maroon{background-color:maroon; color:black}");
    scoutln(out, bytesOut, "option.mediumaquamarine{background-color:mediumaquamarine; color:black}");
    scoutln(out, bytesOut, "option.mediumblue{background-color:mediumblue; color:black}");
    scoutln(out, bytesOut, "option.mediumorchid{background-color:mediumorchid; color:black}");
    scoutln(out, bytesOut, "option.mediumpurple{background-color:mediumpurple; color:black}");
    scoutln(out, bytesOut, "option.mediumseagreen{background-color:mediumseagreen; color:black}");
    scoutln(out, bytesOut, "option.mediumslateblue{background-color:mediumslateblue; color:black}");
    scoutln(out, bytesOut, "option.mediumspringgreen{background-color:mediumspringgreen; color:black}");
    scoutln(out, bytesOut, "option.mediumturquoise{background-color:mediumturquoise; color:black}");
    scoutln(out, bytesOut, "option.mediumvioletred{background-color:mediumvioletred; color:black}");
    scoutln(out, bytesOut, "option.midnightblue{background-color:midnightblue; color:black}");
    scoutln(out, bytesOut, "option.mintcream{background-color:mintcream; color:black}");
    scoutln(out, bytesOut, "option.mistyrose{background-color:mistyrose; color:black}");
    scoutln(out, bytesOut, "option.moccasin{background-color:moccasin; color:black}");
    scoutln(out, bytesOut, "option.navajowhite{background-color:navajowhite; color:black}");
    scoutln(out, bytesOut, "option.navy{background-color:navy; color:black}");
    scoutln(out, bytesOut, "option.oldlace{background-color:oldlace; color:black}");
    scoutln(out, bytesOut, "option.olive{background-color:olive; color:black}");
    scoutln(out, bytesOut, "option.olivedrab{background-color:olivedrab; color:black}");
    scoutln(out, bytesOut, "option.orange{background-color:orange; color:black}");
    scoutln(out, bytesOut, "option.orangered{background-color:orangered; color:black}");
    scoutln(out, bytesOut, "option.orchid{background-color:orchid; color:black}");
    scoutln(out, bytesOut, "option.palegoldenrod{background-color:palegoldenrod; color:black}");
    scoutln(out, bytesOut, "option.palegreen{background-color:palegreen; color:black}");
    scoutln(out, bytesOut, "option.paleturquoise{background-color:paleturquoise; color:black}");
    scoutln(out, bytesOut, "option.palevioletred{background-color:palevioletred; color:black}");
    scoutln(out, bytesOut, "option.papayawhip{background-color:papayawhip; color:black}");
    scoutln(out, bytesOut, "option.peachpuff{background-color:peachpuff; color:black}");
    scoutln(out, bytesOut, "option.peru{background-color:peru; color:black}");
    scoutln(out, bytesOut, "option.pink{background-color:pink; color:black}");
    scoutln(out, bytesOut, "option.plum{background-color:plum; color:black}");
    scoutln(out, bytesOut, "option.powderblue{background-color:powderblue; color:black}");
    scoutln(out, bytesOut, "option.purple{background-color:purple; color:black}");
    scoutln(out, bytesOut, "option.red{background-color:red; color:black}");
    scoutln(out, bytesOut, "option.rosybrown{background-color:rosybrown; color:black}");
    scoutln(out, bytesOut, "option.royalblue{background-color:royalblue; color:black}");
    scoutln(out, bytesOut, "option.saddlebrown{background-color:saddlebrown; color:black}");
    scoutln(out, bytesOut, "option.salmon{background-color:salmon; color:black}");
    scoutln(out, bytesOut, "option.sandybrown{background-color:sandybrown; color:black}");
    scoutln(out, bytesOut, "option.seagreen{background-color:seagreen; color:black}");
    scoutln(out, bytesOut, "option.seashell{background-color:seashell; color:black}");
    scoutln(out, bytesOut, "option.sienna{background-color:sienna; color:black}");
    scoutln(out, bytesOut, "option.silver{background-color:silver; color:black}");
    scoutln(out, bytesOut, "option.skyblue{background-color:skyblue; color:black}");
    scoutln(out, bytesOut, "option.slateblue{background-color:slateblue; color:black}");
    scoutln(out, bytesOut, "option.slategray{background-color:slategray; color:black}");
    scoutln(out, bytesOut, "option.snow{background-color:snow; color:black}");
    scoutln(out, bytesOut, "option.springgreen{background-color:springgreen; color:black}");
    scoutln(out, bytesOut, "option.steelblue{background-color:steelblue; color:black}");
    scoutln(out, bytesOut, "option.tan{background-color:tan; color:black}");
    scoutln(out, bytesOut, "option.teal{background-color:teal; color:black}");
    scoutln(out, bytesOut, "option.thistle{background-color:thistle; color:black}");
    scoutln(out, bytesOut, "option.tomato{background-color:tomato; color:black}");
    scoutln(out, bytesOut, "option.turquoise{background-color:turquoise; color:black}");
    scoutln(out, bytesOut, "option.violet{background-color:violet; color:black}");
    scoutln(out, bytesOut, "option.wheat{background-color:wheat; color:black}");
    scoutln(out, bytesOut, "option.white{background-color:white; color:black}");
    scoutln(out, bytesOut, "option.whitesmoke{background-color:whitesmoke; color:black}");
    scoutln(out, bytesOut, "option.yellow{background-color:yellow; color:black}");
    scoutln(out, bytesOut, "option.yellowgreen{background-color:yellowgreen; color:black}");
    scoutln(out, bytesOut, "</style>");

    dashboardUtils.drawTitleW(out, "Site Styling for " + styleName, "7081", unm, sid, uty, men, den, dnm, bnm, bytesOut);

    scoutln(out, bytesOut, "<form id=\"7081bw\" enctype=\"application/x-www-form-urlencoded\" method=POST>");
    scoutln(out, bytesOut, "<input type=hidden name=unm value="+unm+">");
    scoutln(out, bytesOut, "<input type=hidden name=sid value="+sid+">");
    scoutln(out, bytesOut, "<input type=hidden name=uty value="+uty+">");
    scoutln(out, bytesOut, "<input type=hidden name=men value=\""+men+"\">");
    scoutln(out, bytesOut, "<input type=hidden name=den value="+den+">");
    scoutln(out, bytesOut, "<input type=hidden name=dnm value="+dnm+">");
    scoutln(out, bytesOut, "<input type=hidden name=bnm value="+bnm+">");
    scoutln(out, bytesOut, "<input type=hidden name=styleName value=" + generalUtils.sanitise(styleName) + ">");
    scoutln(out, bytesOut, "<input type=hidden name=source value=" + source + ">");

    scoutln(out, bytesOut, "<table id='page' border=0 cellspacing=0 cellpadding=2 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    String[] headerLogo       = new String[1];
    String[] footerText       = new String[1];
    String[] usesFlash        = new String[1];
    String[] pageHeaderImage1 = new String[1];
    String[] pageHeaderImage2 = new String[1];
    String[] pageHeaderImage3 = new String[1];
    String[] pageHeaderImage4 = new String[1];
    String[] pageHeaderImage5 = new String[1];
    String[] plainLogo        = new String[1];

    wiki.getStyling(dnm, headerLogo, plainLogo, usesFlash, footerText, pageHeaderImage1, pageHeaderImage2, pageHeaderImage3, pageHeaderImage4, pageHeaderImage5, null);

    String fileName;
    if(source.equals("Z"))
      fileName = "/Zara/Support/Css/" + styleName + "/general.css";
    else fileName = "/Zara/" + dnm + "/Css/" + styleName + "/general.css";

    RandomAccessFile fh = generalUtils.fileOpen(fileName);

    if(fh == null)
      System.out.println("error fh " + fileName);

    int layoutType = directoryUtils.getLayoutType(fh);

    // Layout Type

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Layout</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Type</td><td colspan=5>" + layoutDDL("layoutType", generalUtils.intToStr(layoutType)) + "</td></tr>");

    String bigouterLine = getLine(fh, "div#bigouter");
    String bigouterBackgroundColor  = getEntry(bigouterLine, "background-color:",  false);
    String bigouterBackgroundImage  = getEntry(bigouterLine, "background-image:",  false);
    String bigouterBackgroundRepeat = getEntry(bigouterLine, "background-repeat:", false);

    if(bigouterBackgroundImage.length() > imagesLibraryDirLen)
      bigouterBackgroundImage = bigouterBackgroundImage.substring(imagesLibraryDirLen);

    String outerLine = getLine(fh, "div#outer");
    String outerBackgroundImage    = getEntry(outerLine, "background-image:",    false);
    String outerWidth              = getEntry(outerLine, "width:",               true);
    String outerBackgroundRepeat   = getEntry(outerLine, "background-repeat:",   false);
    String outerBackgroundPosition = getEntry(outerLine, "background-position:", false);
    String outerBackgroundColor    = getEntry(outerLine, "background-color:",    false);
    String outerPaddingLeft        = getEntry(outerLine, "padding-left:",        true);
    String outerPaddingRight       = getEntry(outerLine, "padding-right:",       true);
    String outerPaddingTop         = getEntry(outerLine, "padding-top:",         true);
    String outerPaddingBottom      = getEntry(outerLine, "padding-bottom:",      true);

    if(outerBackgroundImage.length() > imagesLibraryDirLen)
      outerBackgroundImage = outerBackgroundImage.substring(imagesLibraryDirLen);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Fixed Width</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Width</td><td colspan=5><p><input type='text' name='outerWidth' size='4' maxlength='4' value='" + outerWidth + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Side Colour</td><td>" + colourDDL("bigouterBackgroundColor", bigouterBackgroundColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>or, Side Image</td><td colspan=3><input type='text' name='bigouterBackgroundImage' size='50' maxlength='100' value='" + bigouterBackgroundImage + "'></td>");
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Side Image Repeat</td><td colspan=5>" + repeatDDL("bigouterBackgroundRepeat", bigouterBackgroundRepeat) + "</td></tr>");

    String bodyLine = getLine(fh, "body");
    String bodyBorderWidth = getEntry(bodyLine, "border-width:",  true);
    String bodyBorderStyle = getEntry(bodyLine, "border-style:",  false);
    String bodyBorderColor = getEntry(bodyLine, "border-color:",  false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Screen Border</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Width</td><td><p><input type='text' name='bodyBorderWidth' size='2' maxlength='2' value='" + bodyBorderWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Style</td><td>" + styleDDL("bodyBorderStyle", bodyBorderStyle) + "</td>");
    scoutln(out, bytesOut, "<td><p>Colour</td><td>" + colourDDL("bodyBorderColor", bodyBorderColor) + "</td></tr>");

    // Header

    String headerrepeatLine = getLine(fh, "div#headerrepeat");
    String headerrepeatHeight          = getEntry(headerrepeatLine, "height:",           true);
    String headerrepeatWeight          = getEntry(headerrepeatLine, "font-weight:",      false);
    String headerrepeatSize            = getEntry(headerrepeatLine, "font-size:",        true);
    String headerrepeatStyle           = getEntry(headerrepeatLine, "font-style:",       false);
    String headerrepeatFamily          = getEntry(headerrepeatLine, "font-family:",      false);
    String headerrepeatBackgroundImage = getEntry(headerrepeatLine, "background-image:", false);
    String headerrepeatColor           = getEntry(headerrepeatLine, "color:",            false);
    String headerrepeatTextAlign       = getEntry(headerrepeatLine, "text-align:",       false);

    if(headerrepeatBackgroundImage.length() > imagesLibraryDirLen)
      headerrepeatBackgroundImage = headerrepeatBackgroundImage.substring(imagesLibraryDirLen);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Header</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Logo</td><td colspan=5><input type='text' name='headerLogo' size='50' maxlength='100' value='" + headerLogo[0] + "'></td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Repeating Logo</td><td colspan=5><input type='text' name='headerrepeatBackgroundImage' size='50' " + "maxlength='100' value='" + headerrepeatBackgroundImage + "'></td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Plain Logo</td><td colspan=5><input type='text' name='plainLogo' size='50' maxlength='100' value='" + plainLogo[0] + "'></td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Watermark</td><td colspan=5><input type='text' name='outerBackgroundImage' size='50' maxlength='100' value='" + outerBackgroundImage + "'></td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Watermark Position</td><td>" + positionDDL("outerBackgroundPosition", outerBackgroundPosition)
                         + "</td>");
    scoutln(out, bytesOut, "<td><p>Watermark Repeat</td><td colspan=3>" + repeatDDL("outerBackgroundRepeat", outerBackgroundRepeat) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Main Background Colour</td><td>" + colourDDL("outerBackgroundColor", outerBackgroundColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Top of Main Area Padding</td><td><p><input type='text' name='headerrepeatHeight' size='3' maxlength='3' value='" + headerrepeatHeight + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Padding Left</td><td><p><input type='text' name='outerPaddingLeft' size='3' maxlength='3' value='" + outerPaddingLeft + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Padding Right</td><td><p><input type='text' name='outerPaddingRight' size='3' maxlength='3' value='" + outerPaddingRight + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Padding top</td><td><p><input type='text' name='outerPaddingTop' size='3' maxlength='3' value='" + outerPaddingTop
                         + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Padding Bottom</td><td><p><input type='text' name='outerPaddingBottom' size='3' maxlength='3' value='" + outerPaddingBottom
                         + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>User SignOn</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("headerrepeatFamily", headerrepeatFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("headerrepeatWeight", headerrepeatWeight) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td>" + fontstyleDDL("headerrepeatStyle", headerrepeatStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Font Size</td><td><p><input type='text' name='headerrepeatSize' size='2' maxlength='2' value='" + headerrepeatSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Colour</td><td>" + colourDDL("headerrepeatColor", headerrepeatColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Alignment</td><td>" + alignDDL("headerrepeatTextAlign", headerrepeatTextAlign) + "</td></tr>");

    // Footer

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Footer</td></tr>");

    String footerLine = getLine(fh, "div#footer");
    String footerBorderTopColor    = getEntry(footerLine, "border-top-color:",    false);
    String footerBorderTopWidth    = getEntry(footerLine, "border-top-width:",    true);
    String footerBorderTopStyle    = getEntry(footerLine, "border-top-style:",    false);
    String footerBorderBottomColor = getEntry(footerLine, "border-bottom-color:", false);
    String footerBorderBottomWidth = getEntry(footerLine, "border-bottom-width:", true);
    String footerBorderBottomStyle = getEntry(footerLine, "border-bottom-style:", false);
    String footerBorderLeftColor   = getEntry(footerLine, "border-left-color:",   false);
    String footerBorderLeftWidth   = getEntry(footerLine, "border-left-width:",   true);
    String footerBorderLeftStyle   = getEntry(footerLine, "border-left-style:",   false);
    String footerBorderRightColor  = getEntry(footerLine, "border-right-color:",  false);
    String footerBorderRightWidth  = getEntry(footerLine, "border-right-width:",  true);
    String footerBorderRightStyle  = getEntry(footerLine, "border-right-style:",  false);
    String footerPaddingRight      = getEntry(footerLine, "padding-right:",       true);
    String footerPaddingLeft       = getEntry(footerLine, "padding-left:",        true);
    String footerPaddingTop        = getEntry(footerLine, "padding-top:",         true);
    String footerPaddingBottom     = getEntry(footerLine, "padding-bottom:",      true);
    String footerSize              = getEntry(footerLine, "font-size:",           true);
    String footerStyle             = getEntry(footerLine, "font-style:",          false);
    String footerFamily            = getEntry(footerLine, "font-family:",         false);
    String footerWeight            = getEntry(footerLine, "font-weight:",         false);
    String footerBackgroundColor   = getEntry(footerLine, "background-color:",    false);
    String footerColor             = getEntry(footerLine, "color:",               false);
    String footerTextAlign         = getEntry(footerLine, "text-align:",          false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td nowrap valign=top><p>Footer Text</td><td colspan=5><textarea name='footerText' rows='4' cols='80'>"
                         + footerText[0] + "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Colour</td><td>" + colourDDL("footerColor", footerColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background Colour</td><td>" + colourDDL("footerBackgroundColor", footerBackgroundColor) + "</td>");

    scoutln(out, bytesOut, "<td nowrap><p>Alignment</td><td>" + alignDDL("footerTextAlign", footerTextAlign) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("footerFamily", footerFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("footerWeight", footerWeight) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td>" + fontstyleDDL("footerStyle", footerStyle) + "</td>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Font Size</td><td><p><input type='text' name='footerSize' size='2' maxlength='2' value='" + footerSize + "'> point</td>");

    scoutln(out, bytesOut, "<td nowrap><p>Padding Top</td><td><input type='text' name='footerPaddingTop' size='2' maxlength='2' value='" + footerPaddingTop + "'></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Padding Bottom</td><td><input type='text' name='footerPaddingBottom' size='2' maxlength='2' value='" + footerPaddingBottom + "'></td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td nowrap><p>Padding Left</td><td><input type='text' name='footerPaddingLeft' size='2' maxlength='2' value='" + footerPaddingLeft + "'></td>");
    scoutln(out, bytesOut, "<td nowrap><p>Padding Right</td><td colspan=3><input type='text' name='footerPaddingRight' size='2' maxlength='2' value='" + footerPaddingRight + "'></td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Top Border</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Colour</td><td>" + colourDDL("footerBorderTopColor", footerBorderTopColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Width</td><td><p><input type='text' name='footerBorderTopWidth' size='2' maxlength='2' value='" + footerBorderTopWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td><p>" + styleDDL("footerBorderTopStyle", footerBorderTopStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Bottom Border</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Colour</td><td>" + colourDDL("footerBorderBottomColor", footerBorderBottomColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Width</td><td><p><input type='text' name='footerBorderBottomWidth' size='2' maxlength='2' value='" + footerBorderBottomWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td><p>" + styleDDL("footerBorderBottomStyle", footerBorderBottomStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Left Border</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Colour</td><td>" + colourDDL("footerBorderLeftColor", footerBorderLeftColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Width</td><td><p><input type='text' name='footerBorderLeftWidth' size='2' maxlength='2' value='" + footerBorderLeftWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td><p>" + styleDDL("footerBorderLeftStyle", footerBorderLeftStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Right Border</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Colour</td><td>" + colourDDL("footerBorderRightColor", footerBorderRightColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Width</td><td><p><input type='text' name='footerBorderRightWidth' size='2' maxlength='2' value='" + footerBorderRightWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td><p>" + styleDDL("footerBorderRightStyle", footerBorderRightStyle) + "</td></tr>");

    // Main Menu

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Main Menu</td></tr>");

    /* div#mainmenu { width:180px;  / *  1,2 - same as the dt width below * /
                      width:100%;    / * 3,4 * /
                      border-width: 1px;  / * item border * /
                      border-style: inset; border-color: black;
                      background-color: blanchedalmond;  / *  menu bg * /
                      font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 10pt; font-style: normal; font-weight: normal; } */
    String mainmenuLine = getLine(fh, "div#mainmenu {");
    String mainmenuWidth           = getEntry(mainmenuLine, "width:",            true);
    String mainmenuBorderColor     = getEntry(mainmenuLine, "border-color:",     false);
    String mainmenuBorderWidth     = getEntry(mainmenuLine, "border-width:",     true);
    String mainmenuBorderStyle     = getEntry(mainmenuLine, "border-style:",     false);
    String mainmenuBackgroundColor = getEntry(mainmenuLine, "background-color:", false);
    String mainmenuSize            = getEntry(mainmenuLine, "font-size:",        true);
    String mainmenuStyle           = getEntry(mainmenuLine, "font-style:",       false);
    String mainmenuFamily          = getEntry(mainmenuLine, "font-family:",      false);
    String mainmenuWeight          = getEntry(mainmenuLine, "font-weight:",      false);
    String mainmenuPaddingRight    = getEntry(mainmenuLine, "padding-right:",    true);
    String mainmenuPaddingLeft     = getEntry(mainmenuLine, "padding-left:",     true);
    String mainmenuPaddingTop      = getEntry(mainmenuLine, "padding-top:",      true);
    String mainmenuPaddingBottom   = getEntry(mainmenuLine, "padding-top:",      true);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Menu Width</td><td><p><input type='text' name='mainmenuWidth' size='3' maxlength='3' value='" + mainmenuWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Menu Background Colour</td><td>" + colourDDL("mainmenuBackgroundColor", mainmenuBackgroundColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Menu Border Colour</td><td>" + colourDDL("mainmenuBorderColor", mainmenuBorderColor) + "</td></tr>");
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Menu Border Width</td><td><p><input type='text' name='mainmenuBorderWidth' size='2' maxlength='2' value='" + mainmenuBorderWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Menu Border Style</td><td><p>" + styleDDL("mainmenuBorderStyle", mainmenuBorderStyle) + "</td>");

    scoutln(out, bytesOut, "<td><p>Padding Left</td><td><p><input type='text' name='mainmenuPaddingLeft' size='2' maxlength='2' value='" + mainmenuPaddingLeft + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Padding Right</td><td><p><input type='text' name='mainmenuPaddingRight' size='2' maxlength='2' value='" + mainmenuPaddingRight + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Padding Top</td><td><p><input type='text' name='mainmenuPaddingTop' size='2' maxlength='2' value='" + mainmenuPaddingTop + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Padding Bottom</td><td><p><input type='text' name='mainmenuPaddingBottom' size='2' maxlength='2' value='" + mainmenuPaddingBottom + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Item</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Face</td><td>" + familyDDL("mainmenuFamily", mainmenuFamily) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Weight</td><td>" + weightDDL("mainmenuWeight", mainmenuWeight) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Style</td><td>" + fontstyleDDL("mainmenuStyle", mainmenuStyle) + "</td></tr>");
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Font Size</td><td><p><input type='text' name='mainmenuSize' size='2' maxlength='2' value='" + mainmenuSize + "'> point</td>");

    /* div#mainmenu dt { width:180px;  / * 1- same as above; 2 - whatever width * /
                         background-color: yellow; / * menu item bg * /
                         color: green; / * same as dt a   below * /
                         text-align: center; / * alignment * /
                         border-width: 1px;  / * item border * /
                         border-style: inset;
                         border-color: black;
                         height:16px; / * menu item height * / } */
    String mainmenudtLine = getLine(fh, "div#mainmenu dt {");
    String mainmenudtWidth           = getEntry(mainmenudtLine, "width:",            true);
    String mainmenudtColor           = getEntry(mainmenudtLine, "color:",            false);
    String mainmenudtBackgroundColor = getEntry(mainmenudtLine, "background-color:", false);
    String mainmenudtTextAlign       = getEntry(mainmenudtLine, "text-align:",       false);
    String mainmenudtBorderColor     = getEntry(mainmenudtLine, "border-color:",     false);
    String mainmenudtBorderWidth     = getEntry(mainmenudtLine, "border-width:",     true);
    String mainmenudtBorderStyle     = getEntry(mainmenudtLine, "border-style:",     false);
    String mainmenudtHeight          = getEntry(mainmenudtLine, "height:",           true);

    scoutln(out, bytesOut, "<td><p>Item Width</td><td><p><input type='text' name='mainmenudtWidth' size='3' maxlength='3' value='" + mainmenudtWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Item Height</td><td><p><input type='text' name='mainmenudtHeight' size='2' maxlength='2' value='" + mainmenudtHeight + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Font Colour</td><td>" + colourDDL("mainmenudtColor", mainmenudtColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Background Colour</td><td>" + colourDDL("mainmenudtBackgroundColor", mainmenudtBackgroundColor) + "</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Item Text Alignment</td><td>" + alignDDL("mainmenudtTextAlign", mainmenudtTextAlign) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Border Colour</td><td>" + colourDDL("mainmenudtBorderColor", mainmenudtBorderColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Border Width</td><td><p><input type='text' name='mainmenudtBorderWidth' size='2' maxlength='2' value='" + mainmenudtBorderWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Item Border Style</td><td><p>" + styleDDL("mainmenudtBorderStyle", mainmenudtBorderStyle) + "</td></tr>");

    /* div#mainmenu dl { padding-right: 0px; / * margin around the actual menu item * /
                         padding-left: 0; } */

    String mainmenudlLine = getLine(fh, "div#mainmenu dl {");
    String mainmenudlPaddingRight    = getEntry(mainmenudlLine, "padding-right:", true);
    String mainmenudlPaddingLeft     = getEntry(mainmenudlLine, "padding-left:",  true);
    String mainmenudlMarginTop       = getEntry(mainmenudlLine, "margin-top:",    true);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Padding Left</td><td><p><input type='text' name='mainmenudlPaddingLeft' size='2' maxlength='2' value='" + mainmenudlPaddingLeft + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Item Padding Right</td><td><p><input type='text' name='mainmenudlPaddingRight' size='2' maxlength='2' value='" + mainmenudlPaddingRight + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Item Padding Top</td><td><p><input type='text' name='mainmenudlMarginTop' size='2' maxlength='2' value='" + mainmenudlMarginTop + "'> pixels</td></tr>");

     /* div#mainmenu li a:hover, div#mainmenu dt a:hover { color: red; / * text hover colour * /
                                                          background-color: cyan;  / * hover colour * / } */
    String mainmenuliahoverLine = getLine(fh, "div#mainmenu li a:hover");
    String mainmenuliahoverColor           = getEntry(mainmenuliahoverLine, "color:",            false);
    String mainmenuliahoverBackgroundColor = getEntry(mainmenuliahoverLine, "background-color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Hover Font Colour</td><td>" + colourDDL("mainmenuliahoverColor", mainmenuliahoverColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Hover Background Colour</td><td colspan=5>" + colourDDL("mainmenuliahoverBackgroundColor", mainmenuliahoverBackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Dropdown</td></tr>");

    /* div#mainmenu li a { color: blue; / * DD text colour * / } */
    String mainmenuliaLine = getLine(fh, "div#mainmenu li a {");
    String mainmenuliaColor = getEntry(mainmenuliaLine, "color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Font Colour</td><td>" + colourDDL("mainmenuliaColor", mainmenuliaColor) + "</td>");

    /* div#mainmenu dd { background-color: lightyellow; / * DD bg * /
                         border-width: 1px;  / * DD border * /
                         border-style: solid;
                         border-color: red;
                         margin-left: 30; / * DD offset from parent item * / } */

    String mainmenuddLine = getLine(fh, "div#mainmenu dd {");
    String mainmenuddBackgroundColor = getEntry(mainmenuddLine, "background-color:", false);
    String mainmenuddBorderColor     = getEntry(mainmenuddLine, "border-color:",     false);
    String mainmenuddBorderWidth     = getEntry(mainmenuddLine, "border-width:",     true);
    String mainmenuddBorderStyle     = getEntry(mainmenuddLine, "border-style:",     false);
    String mainmenuddMarginLeft      = getEntry(mainmenuddLine, "margin-left:",      true);
    String mainmenuddHeight          = getEntry(mainmenuddLine, "line-height:",      true);

    scoutln(out, bytesOut, "<td><p>Item Background Colour</td><td>" + colourDDL("mainmenuddBackgroundColor", mainmenuddBackgroundColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Border Colour</td><td>" + colourDDL("mainmenuddBorderColor", mainmenuddBorderColor) + "</td></tr>");
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Border Width</td><td><p><input type='text' name='mainmenuddBorderWidth' size='2' maxlength='2' value='" + mainmenuddBorderWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Item Border Style</td><td><p>" + styleDDL("mainmenuddBorderStyle", mainmenuddBorderStyle) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Offset</td><td><p><input type='text' name='mainmenuddMarginLeft' size='2' maxlength='2' value='" + mainmenuddMarginLeft + "'> pixels</td></tr></tr>");

    /* div#mainmenu li { width:180px; / * DD width * / } */

    String mainmenuliLine = getLine(fh, "div#mainmenu li {");
    String mainmenuliWidth = getEntry(mainmenuliLine, "width:", true);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Width</td><td><p><input type='text' name='mainmenuliWidth' size='3' maxlength='3' value='" + mainmenuliWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Item Height</td><td colspan=3><p><input type='text' name='mainmenuddHeight' size='2' maxlength='2' value='" + mainmenuddHeight + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Separators</td></tr>");

    /* div#mainmenu h1 { text-align: right; width: 178px; font-size: 16pt; color: orange; background-color: blue;
                         margin-left: 0; margin-right: 0; margin-bottom: 0; margin-top: 0;
                         border-left-width: 1px; border-left-style: solid; border-left-color: lightgreen;
                         border-right-width: 1px; border-right-style: solid; border-right-color: lightgreen;
                         border-top-width: 1px; border-top-style: solid; border-top-color: lightgreen;
                         border-bottom-width: 1px; border-bottom-style: solid; border-bottom-color: lightgreen; } */

    String mainmenuh1Line = getLine(fh, "div#mainmenu h1");
    String mainmenuh1TextAlign         = getEntry(mainmenuh1Line, "text-align:",          false);
    String mainmenuh1MarginLeft        = getEntry(mainmenuh1Line, "margin-left:",         true);
    String mainmenuh1MarginRight       = getEntry(mainmenuh1Line, "margin-right:",        true);
    String mainmenuh1MarginTop         = getEntry(mainmenuh1Line, "margin-top:",          true);
    String mainmenuh1MarginBottom      = getEntry(mainmenuh1Line, "margin-bottom:",       true);
    String mainmenuh1Width             = getEntry(mainmenuh1Line, "width:",               true);
    String mainmenuh1BorderTopColor    = getEntry(mainmenuh1Line, "border-top-color:",    false);
    String mainmenuh1BorderTopWidth    = getEntry(mainmenuh1Line, "border-top-width:",    true);
    String mainmenuh1BorderTopStyle    = getEntry(mainmenuh1Line, "border-top-style:",    false);
    String mainmenuh1BorderBottomColor = getEntry(mainmenuh1Line, "border-bottom-color:", false);
    String mainmenuh1BorderBottomWidth = getEntry(mainmenuh1Line, "border-bottom-width:", true);
    String mainmenuh1BorderBottomStyle = getEntry(mainmenuh1Line, "border-bottom-style:", false);
    String mainmenuh1BorderLeftColor   = getEntry(mainmenuh1Line, "border-left-color:",   false);
    String mainmenuh1BorderLeftWidth   = getEntry(mainmenuh1Line, "border-left-width:",   true);
    String mainmenuh1BorderLeftStyle   = getEntry(mainmenuh1Line, "border-left-style:",   false);
    String mainmenuh1BorderRightColor  = getEntry(mainmenuh1Line, "border-right-color:",  false);
    String mainmenuh1BorderRightWidth  = getEntry(mainmenuh1Line, "border-right-width:",  true);
    String mainmenuh1BorderRightStyle  = getEntry(mainmenuh1Line, "border-right-style:",  false);
    String mainmenuh1BackgroundColor   = getEntry(mainmenuh1Line, "background-color:",    false);
    String mainmenuh1FontSize          = getEntry(mainmenuh1Line, "font-size:",           true);
    String mainmenuh1Color             = getEntry(mainmenuh1Line, "color:",               false);
    String mainmenuh1Style             = getEntry(mainmenuh1Line, "font-style:",          false);
    String mainmenuh1Family            = getEntry(mainmenuh1Line, "font-family:",         false);
    String mainmenuh1Weight            = getEntry(mainmenuh1Line, "font-weight:",         false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Colour</td><td>" + colourDDL("mainmenuh1Color", mainmenuh1Color) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Background Colour</td><td>" + colourDDL("mainmenuh1BackgroundColor", mainmenuh1BackgroundColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Font Size</td><td><p><input type='text' name='mainmenuh1FontSize' size='2' maxlength='2' value='" + mainmenuh1FontSize + "'> point</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("mainmenuh1Family", mainmenuh1Family) + "</td>");
    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("mainmenuh1Weight", mainmenuh1Weight) + "</td>");
    scoutln(out, bytesOut, "<td><p>Style</td><td>" + fontstyleDDL("mainmenuh1Style", mainmenuh1Style) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td nowrap><p>Text Alignment</td><td>" + alignDDL("mainmenuh1TextAlign", mainmenuh1TextAlign) + "</td>");
    scoutln(out, bytesOut, "<td><p>Width</td><td><p><input type='text' name='mainmenuh1Width' size='3' maxlength='3' value='" + mainmenuh1Width + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Margin Left</td><td><p><input type='text' name='mainmenuh1MarginLeft' size='2' maxlength='2' value='" + mainmenuh1MarginLeft + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Margin Right</td><td><p><input type='text' name='mainmenuh1MarginRight' size='2' maxlength='2' value='" + mainmenuh1MarginRight + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Margin Top</td><td><p><input type='text' name='mainmenuh1MarginTop' size='2' maxlength='2' value='" + mainmenuh1MarginTop + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Margin Bottom</td><td><p><input type='text' name='mainmenuh1MarginBottom' size='2' maxlength='2' value='" + mainmenuh1MarginBottom + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Left Border Colour</td><td>" + colourDDL("mainmenuh1BorderLeftColor", mainmenuh1BorderLeftColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Left Border Width</td><td><p><input type='text' name='mainmenuh1BorderLeftWidth' size='2' maxlength='2' value='" + mainmenuh1BorderLeftWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Left Border Style</td><td><p>" + styleDDL("mainmenuh1BorderLeftStyle", mainmenuh1BorderLeftStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Right Border Colour</td><td>" + colourDDL("mainmenuh1BorderRightColor", mainmenuh1BorderRightColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Right Border Width</td><td><p><input type='text' name='mainmenuh1BorderRightWidth' size='2' maxlength='2' value='" + mainmenuh1BorderRightWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Right Border Style</td><td><p>" + styleDDL("mainmenuh1BorderRightStyle", mainmenuh1BorderRightStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Top Border Colour</td><td>" + colourDDL("mainmenuh1BorderTopColor", mainmenuh1BorderTopColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Top Border Width</td><td><p><input type='text' name='mainmenuh1BorderTopWidth' size='2' maxlength='2' value='" + mainmenuh1BorderTopWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Top Border Style</td><td><p>" + styleDDL("mainmenuh1BorderTopStyle", mainmenuh1BorderTopStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Bottom Border Colour</td><td>" + colourDDL("mainmenuh1BorderBottomColor", mainmenuh1BorderBottomColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Bottom Border Width</td><td><p><input type='text' name='mainmenuh1BorderBottomWidth' size='2' maxlength='2' value='" + mainmenuh1BorderBottomWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Bottom Border Style</td><td><p>" + styleDDL("mainmenuh1BorderBottomStyle", mainmenuh1BorderBottomStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Alerts</td></tr>");

    // #highlightmenuitem { color: yellow; background-color: blue; }

    String highlightmenuitemLine = getLine(fh, "#highlightmenuitem");
    String highlightmenuitemColor           = getEntry(highlightmenuitemLine, "color:",            false);
    String highlightmenuitemBackgroundColor = getEntry(highlightmenuitemLine, "background-color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Item Colour</td><td>" + colourDDL("highlightmenuitemColor", highlightmenuitemColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Background Colour</td><td colspan=3>" + colourDDL("highlightmenuitemBackgroundColor", highlightmenuitemBackgroundColor) + "</td></tr>");

    // Sub Menu

//    scoutln(out, bytesOut, "<tr><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Sub Menu</td></tr>");

    /* div#submenu dl { padding-right: 10px; / * margin around the menu item box * /
                        padding-top: 0; padding-left: 0; padding-bottom: 0; } */

    String submenudlLine = getLine(fh, "div#submenu dl {");
    String submenudlPaddingRight  = getEntry(submenudlLine, "padding-right:",  true);
    String submenudlPaddingTop    = getEntry(submenudlLine, "padding-top:",    true);
    String submenudlPaddingLeft   = getEntry(submenudlLine, "padding-left:",   true);
    String submenudlPaddingBottom = getEntry(submenudlLine, "padding-bottom:", true);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Margin around Item Box Left</td><td><p><input type='text' name='submenudlPaddingLeft' size='2' maxlength='2' value='" + submenudlPaddingLeft + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Margin around Item Box Right</td><td colspan=3><p><input type='text' name='submenudlPaddingRight' size='2' maxlength='2' value='" + submenudlPaddingRight + "'> pixels</td></tr>");
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Margin around Item Box Top</td><td><p><input type='text' name='submenudlPaddingTop' size='2' maxlength='2' value='" + submenudlPaddingTop + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Margin around Item Box Bottom</td><td colspan=3><p><input type='text' name='submenudlPaddingBottom' size='2' maxlength='2' value='" + submenudlPaddingBottom + "'> pixels</td></tr>");

    /* div#submenu li a:hover, div#submenu dt a:hover { color: brown; / * hover text colour - parent and dropdown * /
                                                        background-color: blanchedalmond; } */

    String submenuliahoverLine = getLine(fh, "div#submenu li a:hover");
    String submenuliahoverColor           = getEntry(submenuliahoverLine, "color:",            false);
    String submenuliahoverBackgroundColor = getEntry(submenuliahoverLine, "background-color:", false);

    /* div#submenu dt { color: blue; text-align: center; / * parent * /
                        background-color: pink;
                        font-family: courier,Verdana,Arial,Helvetica,sans-serif; font-style: normal; font-weight: normal; font-size: 12pt;
                        border-color: white; border-width: 2px; border-style: outset;  / * parent * /
                        width:150px; / * if not here then variable width, else width of menu item * /
                        padding-left: 2px; padding-right: 2px; } */

    String submenudtLine = getLine(fh, "div#submenu dt {");
    String submenudtColor           = getEntry(submenudtLine, "color:",            false);
    String submenudtBackgroundColor = getEntry(submenudtLine, "background-color:", false);
    String submenudtSize            = getEntry(submenudtLine, "font-size:",        true);
    String submenudtStyle           = getEntry(submenudtLine, "font-style:",       false);
    String submenudtFamily          = getEntry(submenudtLine, "font-family:",      false);
    String submenudtWeight          = getEntry(submenudtLine, "font-weight:",      false);
    String submenudtBorderColor     = getEntry(submenudtLine, "border-color:",     false);
    String submenudtBorderWidth     = getEntry(submenudtLine, "border-width:",     true);
    String submenudtBorderStyle     = getEntry(submenudtLine, "border-style:",     false);
    String submenudtWidth           = getEntry(submenudtLine, "width:",            true);
    String submenudtTextAlign       = getEntry(submenudtLine, "text-align:",       false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Text Colour</td><td>" + colourDDL("submenudtColor", submenudtColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Background Colour</td><td>" + colourDDL("submenudtBackgroundColor", submenudtBackgroundColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Face</td><td>" + familyDDL("submenudtFamily", submenudtFamily) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Text Hover Colour</td><td>" + colourDDL("submenuliahoverColor", submenuliahoverColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Hover Background Colour</td><td>" + colourDDL("submenuliahoverBackgroundColor", submenuliahoverBackgroundColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Weight</td><td>" + weightDDL("submenudtWeight", submenudtWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Style</td><td>" + fontstyleDDL("submenudtStyle", submenudtStyle) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Font Size</td><td><p><input type='text' name='submenudtSize' size='2' maxlength='2' value='" + submenudtSize + "'> point</td>");
    scoutln(out, bytesOut, "<td><p>Item Border Colour</td><td>" + colourDDL("submenudtBorderColor", submenudtBorderColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Border Width</td><td><p><input type='text' name='submenudtBorderWidth' size='2' maxlength='2' value='" + submenudtBorderWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Item Border Style</td><td><p>" + styleDDL("submenudtBorderStyle", submenudtBorderStyle) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Width</td><td><p><input type='text' name='submenudtWidth' size='3' maxlength='3' value='" + submenudtWidth + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td nowrap><p>Text Alignment</td><td colspan=5s>" + alignDDL("submenudtTextAlign", submenudtTextAlign) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Dropdown</td></tr>");

     /* div#submenu li a { color: orange; / * DD menu text * / } */
    String submenuliaLine = getLine(fh, "div#submenu li a {");
    String submenuliaColor = getEntry(submenuliaLine, "color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Text Colour</td><td>" + colourDDL("submenuliaColor", submenuliaColor) + "</td>");

    /* div#submenu li { height:16px; / * DD item height * /
                        text-align: center;  / * DD text alignment * /
                        font-size: 12px;  / * DD text size * /
                        font-weight: bold; font-style: italic; font-family: courier,Verdana,Arial,Helvetica,sans-serif; } */

    String submenuliLine = getLine(fh, "div#submenu li {");
    String submenuliHeight    = getEntry(submenuliLine, "height:",      true);
    String submenuliTextAlign = getEntry(submenuliLine, "text-align:",  false);
    String submenuliSize      = getEntry(submenuliLine, "font-size:",   true);
    String submenuliStyle     = getEntry(submenuliLine, "font-style:",  false);
    String submenuliFamily    = getEntry(submenuliLine, "font-family:", false);
    String submenuliWeight    = getEntry(submenuliLine, "font-weight:", false);

    scoutln(out, bytesOut, "<td><p>Item Height</td><td><p><input type='text' name='submenuliHeight' size='2' maxlength='2' value='" + submenuliHeight + "'> pixels</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Text Alignment</td><td>" + alignDDL("submenuliTextAlign", submenuliTextAlign) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Face</td><td>" + familyDDL("submenuliFamily", submenuliFamily) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Weight</td><td>" + weightDDL("submenuliWeight", submenuliWeight) + "</td>");
    scoutln(out, bytesOut, "<td><p>Item Style</td><td>" + fontstyleDDL("submenuliStyle", submenuliStyle) + "</td></tr>");
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Font Size</td><td><p><input type='text' name='submenuliSize' size='2' maxlength='2' value='" + submenuliSize + "'> point</td>");

     /* div#submenu dd { background-color: lightgray; / * DD bg * /
                         border-width: 1px; border-style: dashed; border-color: blue; } */
     
    String submenuddLine = getLine(fh, "div#submenu dd {");
    String submenuddBackgroundColor = getEntry(submenuddLine, "background-color:", false);
    String submenuddBorderColor     = getEntry(submenuddLine, "border-color:",     false);
    String submenuddBorderWidth     = getEntry(submenuddLine, "border-width:",     true);
    String submenuddBorderStyle     = getEntry(submenuddLine, "border-style:",     false);

    scoutln(out, bytesOut, "<td><p>Item Background Colour</td><td>" + colourDDL("submenuddBackgroundColor", submenuddBackgroundColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Item Border Colour</td><td>" + colourDDL("submenuddBorderColor", submenuddBorderColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Item Border Width</td><td><p><input type='text' name='submenuddBorderWidth' size='2' maxlength='2' value='" + submenuddBorderWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Item Border Style</td><td colspan=3><p>" + styleDDL("submenuddBorderStyle", submenuddBorderStyle) + "</td></tr>");

    // Main

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Main</td></tr>");

    /* div#main { border-left-width: 1px; border-left-style: solid; border-left-color: lightgreen;
                  border-right-width: 1px; border-right-style: solid; border-right-color: lightgreen;
                  border-top-width: 1px; border-top-style: solid; border-top-color: lightgreen;
                  border-bottom-width: 1px; border-bottom-style: solid; border-bottom-color: lightgreen; } */

    String mainLine = getLine(fh, "div#main {");
    String mainBorderTopColor    = getEntry(mainLine, "border-top-color:",    false);
    String mainBorderTopWidth    = getEntry(mainLine, "border-top-width:",    true);
    String mainBorderTopStyle    = getEntry(mainLine, "border-top-style:",    false);
    String mainBorderBottomColor = getEntry(mainLine, "border-bottom-color:", false);
    String mainBorderBottomWidth = getEntry(mainLine, "border-bottom-width:", true);
    String mainBorderBottomStyle = getEntry(mainLine, "border-bottom-style:", false);
    String mainBorderLeftColor   = getEntry(mainLine, "border-left-color:",   false);
    String mainBorderLeftWidth   = getEntry(mainLine, "border-left-width:",   true);
    String mainBorderLeftStyle   = getEntry(mainLine, "border-left-style:",   false);
    String mainBorderRightColor  = getEntry(mainLine, "border-right-color:",  false);
    String mainBorderRightWidth  = getEntry(mainLine, "border-right-width:",  true);
    String mainBorderRightStyle  = getEntry(mainLine, "border-right-style:",  false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Top Border Colour</td><td>" + colourDDL("mainBorderTopColor", mainBorderTopColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Top Border Width</td><td><p><input type='text' name='mainBorderTopWidth' size='2' maxlength='2' value='" + mainBorderTopWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Top Border Style</td><td><p>" + styleDDL("mainBorderTopStyle", mainBorderTopStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Bottom Border Colour</td><td>" + colourDDL("mainBorderBottomColor", mainBorderBottomColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Bottom Border Width</td><td><p><input type='text' name='mainBorderBottomWidth' size='2' maxlength='2' value='" + mainBorderBottomWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Bottom Border Style</td><td><p>" + styleDDL("mainBorderBottomStyle", mainBorderBottomStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Left Border Colour</td><td>" + colourDDL("mainBorderLeftColor", mainBorderLeftColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Left Border Width</td><td><p><input type='text' name='mainBorderLeftWidth' size='2' maxlength='2' value='" + mainBorderLeftWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Left Border Style</td><td><p>" + styleDDL("mainBorderLeftStyle", mainBorderLeftStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Right Border Colour</td><td>" + colourDDL("mainBorderRightColor", mainBorderRightColor) + "</td>");
    scoutln(out, bytesOut, "<td><p>Right Border Width</td><td><p><input type='text' name='mainBorderRightWidth' size='2' maxlength='2' value='" + mainBorderRightWidth + "'> pixels</td>");
    scoutln(out, bytesOut, "<td><p>Right Border Style</td><td><p>" + styleDDL("mainBorderRightStyle", mainBorderRightStyle) + "</td></tr>");

    // Option Text

    // #optional { color: #880088; background-color: transparent; font-weight: normal; font-size: 10px; font-family: Verdana,Arial,Helvetica,sans-serif; } // mainBackgroundColor
    String optionalLine = getLine(fh, "#optional");
    String optionalColor      = getEntry(optionalLine, "color:",       false);
    String optionalFontWeight = getEntry(optionalLine, "font-weight:", false);
    String optionalFontSize   = getEntry(optionalLine, "font-size:",   true);
    String optionalFontFamily = getEntry(optionalLine, "font-family:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Option Text</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("optionalFontFamily", optionalFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='optionalFontSize' size='2' maxlength='2' value='" + optionalFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("optionalFontWeight", optionalFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Colour</td><td colspan=5>" + colourDDL("optionalColor", optionalColor) + "</td></tr>");

    // Title Bar

    // #title { background-color: #9ac6e3; color: #574838; border-top-width: 2px; border-top-color: #574838; border-top-style: solid;
    //                      border-bottom-width: 2px: border-bottom-color: #574838; border-bottom-style: solid; font-family: courier; font-size: 14pt;
    //                      font-weight: bold; }
    String titleBarLine = getLine(fh, "#title {");
    String titleBarBackgroundColor   = getEntry(titleBarLine, "background-color:",    false);
    String titleBarColor             = getEntry(titleBarLine, "color:",               false);
    String titleBarBorderTopWidth    = getEntry(titleBarLine, "border-top-width:",    true);
    String titleBarBorderTopColor    = getEntry(titleBarLine, "border-top-color:",    false);
    String titleBarBorderTopStyle    = getEntry(titleBarLine, "border-top-style:",    false);
    String titleBarBorderBottomWidth = getEntry(titleBarLine, "border-bottom-width:", true);
    String titleBarBorderBottomColor = getEntry(titleBarLine, "border-bottom-color:", false);
    String titleBarBorderBottomStyle = getEntry(titleBarLine, "border-bottom-style:", false);
    String titleBarFontWeight        = getEntry(titleBarLine, "font-weight:",         false);
    String titleBarFontSize          = getEntry(titleBarLine, "font-size:",           true);
    String titleBarFontFamily        = getEntry(titleBarLine, "font-family:",         false);

    // #title a:link { color: cyan; text-decoration: none; background-color: transparent; solid; font-family: courier; font-size: 14pt; font-weight: bold; }
    String titleBarALinkLine = getLine(fh, "#title a:link");
    String titleBarALinkColor          = getEntry(titleBarALinkLine, "color:",           false);
    String titleBarALinkTextDecoration = getEntry(titleBarALinkLine, "text-decoration:", false);
    String titleBarLinkFontWeight      = getEntry(titleBarALinkLine, "font-weight:",     false);
    String titleBarLinkFontSize        = getEntry(titleBarALinkLine, "font-size:",       true);
    String titleBarLinkFontFamily      = getEntry(titleBarALinkLine, "font-family:",     false);

    // #title a:visited { color: cyan; text-decoration: none; background-color: transparent; solid; font-family: courier; font-size: 14pt; font-weight: bold; }
    // #title a:active { color: cyan; text-decoration: none; background-color: transparent; solid; font-family: courier; font-size: 14pt; font-weight: bold; }
    // #title a:hover { color: white; text-decoration: none; background-color: transparent; solid; font-family: courier; font-size: 14pt; font-weight: bold; }
    String titleBarAHoverLine = getLine(fh, "#title a:hover");
    String titleBarAHoverColor          = getEntry(titleBarAHoverLine, "color:",           false);
    String titleBarAHoverTextDecoration = getEntry(titleBarAHoverLine, "text-decoration:", false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Title Bar</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Colour</td><td>" + colourDDL("titleBarColor", titleBarColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("titleBarBackgroundColor", titleBarBackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Bar Border</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Top Colour</td><td>" + colourDDL("titleBarBorderTopColor", titleBarBorderTopColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Top Style</td><td><p>" + styleDDL("titleBarBorderTopStyle", titleBarBorderTopStyle) + "</td>");

    scoutln(out, bytesOut, "<td><p>Top Width</td><td><p><input type='text' name='titleBarBorderTopWidth' size='2' maxlength='2' value='" + titleBarBorderTopWidth + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Bottom Colour</td><td>" + colourDDL("titleBarBorderBottomColor", titleBarBorderBottomColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Bottom Style</td><td><p>" + styleDDL("titleBarBorderBottomStyle", titleBarBorderBottomStyle) + "</td>");

    scoutln(out, bytesOut, "<td><p>Bottom Width</td><td><p><input type='text' name='titleBarBorderBottomWidth' size='2' maxlength='2' value='" + titleBarBorderBottomWidth + "'> pixels</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Bar Font</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("titleBarFontFamily", titleBarFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='titleBarFontSize' size='2' maxlength='2' value='" + titleBarFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("titleBarFontWeight", titleBarFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Bar Links</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Colour</td><td>" + colourDDL("titleBarALinkColor", titleBarALinkColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Underline</td><td><p>" + decorationDDL("titleBarALinkTextDecoration", titleBarALinkTextDecoration) + "</td>");

    scoutln(out, bytesOut, "<td><p>Hover Colour</td><td>" + colourDDL("titleBarAHoverColor", titleBarAHoverColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Hover Style</td><td><p>" + decorationDDL("titleBarAHoverTextDecoration", titleBarAHoverTextDecoration) + "</td>");

    scoutln(out, bytesOut, "<td><p>Face</td><td>" + familyDDL("titleBarLinkFontFamily", titleBarLinkFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='titleBarLikFontSize' size='2' maxlength='2' value='" + titleBarLinkFontSize + "'> point</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Weight</td><td colspan=5>" + weightDDL("titleBarLinkFontWeight", titleBarLinkFontWeight) + "</td></tr>");

    // Service Help Link on Main Page

    // #service p { color: #880088; text-decoration: none; background-color: transparent; font-family: courier; font-size: 14pt;
    //                         font-weight: bold; } // mainBackgroundColor + fonts
    String serviceMainLine = getLine(fh, "#service p");
    String serviceMainColor      = getEntry(serviceMainLine, "color:",           false);
    String serviceMainFontWeight = getEntry(serviceMainLine, "font-weight:",     false);
    String serviceMainFontSize   = getEntry(serviceMainLine, "font-size:",       true);
    String serviceMainFontFamily = getEntry(serviceMainLine, "font-family:",     false);

    // #service a:link { color: #880088; text-decoration: none; background-color: transparent; } // mainBackgroundColor + fonts
    String serviceMainALinkLine = getLine(fh, "#service a:link");

    // String serviceMainALinkColor          = getEntry(serviceMainALinkLine, "color:",           false);
    String serviceMainALinkTextDecoration = getEntry(serviceMainALinkLine, "text-decoration:", false);

    // #service a:visited { color: #880088; text-decoration: none; background-color: transparent;} // mainBackgroundColor + fonts
    // #service a:active { color: #880088; text-decoration: none; background-color: transparent;} // mainBackgroundColor + fonts

    // #service a:hover { color: red; text-decoration: none; background-color: transparent;} // mainBackgroundColor + fonts
    String serviceMainAHoverLine = getLine(fh, "#service a:hover");
    String serviceMainAHoverColor          = getEntry(serviceMainAHoverLine, "color:",           false);
    String serviceMainAHoverTextDecoration = getEntry(serviceMainAHoverLine, "text-decoration:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Service Help Link on Main Page</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("serviceMainFontFamily", serviceMainFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='serviceMainFontSize' size='2' maxlength='2' value='" + serviceMainFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("serviceMainFontWeight", serviceMainFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Colour</td><td>" + colourDDL("serviceMainColor", serviceMainColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td><p>" + decorationDDL("serviceMainALinkTextDecoration", serviceMainALinkTextDecoration) + "</td>");

    scoutln(out, bytesOut, "<td><p>Hover Colour</td><td>" + colourDDL("serviceMainAHoverColor", serviceMainAHoverColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Hover Style</td><td colspan=4><p>" + decorationDDL("serviceMainAHoverTextDecoration", serviceMainAHoverTextDecoration) + "</td>");

    scoutln(out, bytesOut, "<td><input type='hidden' name='serviceMainALinkColor' value='" + serviceMainColor + "'></td></tr>");

    // Page

    // #pageColumn { font-family: courier; font-size: 14pt; font-weight: bold; background-color: #FFDDDD; }
    String pageColumnLine = getLine(fh, "#pageColumn");
    String pageColumnBackgroundColor = getEntry(pageColumnLine, "background-color:", false);
    String pageColumnFontWeight      = getEntry(pageColumnLine, "font-weight:",      false);
    String pageColumnFontSize        = getEntry(pageColumnLine, "font-size:",        true);
    String pageColumnFontFamily      = getEntry(pageColumnLine, "font-family:",      false);

    // #page p { color: black; font-family: courier; font-size: 14pt; font-weight: bold; background-color: white;}
    String pagePLine = getLine(fh, "#page p");
    String pagePColor           = getEntry(pagePLine, "color:",            false);
    String pagePBackgroundColor = getEntry(pagePLine, "background-color:", false);
    String pagePFontWeight      = getEntry(pagePLine, "font-weight:",      false);
    String pagePFontSize        = getEntry(pagePLine, "font-size:",        true);
    String pagePFontFamily      = getEntry(pagePLine, "font-family:",      false);

    // #page h1 { font-family: courier; font-size: 14pt; font-weight: bold; color: black; margin-top: 12px; border-top-width: 2px;
    //                      border-top-color: red; border-top-style: outset; border-bottom-size: 1px; border-bottom-color: red;
    //                      border-bottom-style: dashed; background-color: #F0F0F0; }
    String pageH1Line = getLine(fh, "#page h1");
    String pageH1BackgroundColor   = getEntry(pageH1Line, "background-color:",    false);
    String pageH1Color             = getEntry(pageH1Line, "color:",               false);
    String pageH1FontWeight        = getEntry(pageH1Line, "font-weight:",         false);
    String pageH1FontSize          = getEntry(pageH1Line, "font-size:",           true);
    String pageH1FontFamily        = getEntry(pageH1Line, "font-family:",         false);
    String pageH1BorderTopWidth    = getEntry(pageH1Line, "border-top-width:",    true);
    String pageH1BorderTopColor    = getEntry(pageH1Line, "border-top-color:",    false);
    String pageH1BorderTopStyle    = getEntry(pageH1Line, "border-top-style:",    false);
    String pageH1BorderBottomWidth = getEntry(pageH1Line, "border-bottom-width:", true);
    String pageH1BorderBottomColor = getEntry(pageH1Line, "border-bottom-color:", false);
    String pageH1BorderBottomStyle = getEntry(pageH1Line, "border-bottom-style:", false);
    // #page a:link { font-family: courier; font-size: 14pt; font-weight: bold; color: blue; text-decoration: none;
    //                       background-color: transparent;} // mainBackgroundColor
    String pageALinkLine = getLine(fh, "#page a:link");
    String pageALinkColor          = getEntry(pageALinkLine, "color:",           false);
    String pageALinkFontWeight     = getEntry(pageALinkLine, "font-weight:",     false);
    String pageALinkFontSize       = getEntry(pageALinkLine, "font-size:",       true);
    String pageALinkFontFamily     = getEntry(pageALinkLine, "font-family:",     false);
    String pageALinkTextDecoration = getEntry(pageALinkLine, "text-decoration:", false);
    // #page a:visited { color: blue; text-decoration: none; background-color: transparent; }
    // #page a:active { blue; text-decoration: none; background-color: transparent; }
    // #page a:hover { color: red; text-decoration: none; background-color: transparent;}
    String pageAHoverLine = getLine(fh, "#page a:hover");
    String pageAHoverColor          = getEntry(pageAHoverLine, "color:",           false);
    String pageAHoverTextDecoration = getEntry(pageAHoverLine, "text-decoration:", false);

    // #line1 { color: #000000; font-family: courier; font-size: 14pt; font-weight: bold; background-color: #DDF0DD; }
    //          pagePFontWeight, pagePFontSize, pagePFontFamily
    String line1Line = getLine(fh, "#line1");
    String line1Color           = getEntry(line1Line, "color:",            false);
    String line1BackgroundColor = getEntry(line1Line, "background-color:", false);
    // #line2 { color: #000000; font-family: courier; font-size: 14pt; font-weight: bold; background-color: white; }
    //          pagePFontWeight, pagePFontSize, pagePFontFamily
    String line2Line = getLine(fh, "#line2");
    String line2Color           = getEntry(line2Line, "color:",            false);
    String line2BackgroundColor = getEntry(line2Line, "background-color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Column Headings</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("pageColumnFontFamily", pageColumnFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='pageColumnFontSize' size='2' maxlength='2' value='" + pageColumnFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("pageColumnFontWeight", pageColumnFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Background</td><td colspan=5>" + colourDDL("pageColumnBackgroundColor", pageColumnBackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Lines</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("pagePFontFamily", pagePFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='pagePFontSize' size='2' maxlength='2' value='" + pagePFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("pagePFontWeight", pagePFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Text</td><td>" + colourDDL("pagePColor", pagePColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("pagePBackgroundColor", pagePBackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Section Headings</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("pageH1FontFamily", pageH1FontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='pageH1FontSize' size='2' maxlength='2' value='" + pageH1FontSize + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("pageH1FontWeight", pageH1FontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Text</td><td>" + colourDDL("pageH1Color", pageH1Color) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("pageH1BackgroundColor", pageH1BackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Section Borders</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Top</td><td>" + colourDDL("pageH1BorderTopColor", pageH1BorderTopColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Top Width</td><td><p><input type='text' name='pageH1BorderTopWidth' size='2' maxlength='2' value='" + pageH1BorderTopWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Top Style</td><td><p>" + styleDDL("pageH1BorderTopStyle", pageH1BorderTopStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Bottom</td><td>" + colourDDL("pageH1BorderBottomColor", pageH1BorderBottomColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Bottom Width</td><td><p><input type='text' name='pageH1BorderBottomWidth' size='2' maxlength='2' value='" + pageH1BorderBottomWidth + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Bottom Style</td><td><p>" + styleDDL("pageH1BorderBottomStyle", pageH1BorderBottomStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Links</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("pageALinkFontFamily", pageALinkFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='pageALinkFontSize' size='2' maxlength='2' value='"
                         + pageALinkFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("pageALinkFontWeight", pageALinkFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Text</td><td>" + colourDDL("pageALinkColor", pageALinkColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td colspan=3><p>" + decorationDDL("pageALinkTextDecoration", pageALinkTextDecoration)
                         + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Links Hover</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Text</td><td>" + colourDDL("pageAHoverColor", pageAHoverColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td colspan=3><p>" + decorationDDL("pageAHoverTextDecoration", pageAHoverTextDecoration)
                         + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Listing Lines</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Line 1 Text</td><td>" + colourDDL("line1Color", line1Color) + "</td>");

    scoutln(out, bytesOut, "<td><p>Line 1 Background</td><td colspan=3>" + colourDDL("line1BackgroundColor", line1BackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Line 2 Text</td><td>" + colourDDL("line2Color", line2Color) + "</td>");

    scoutln(out, bytesOut, "<td><p>Line 2 Background</td><td colspan=3>" + colourDDL("line2BackgroundColor", line2BackgroundColor) + "</td></tr>");

    // #page input { color: black;  background-color: lightblue;  font-family: ; font-size: 10pt; font-weight: ; }

    String pageInputLine = getLine(fh, "#page input");
    String pageInputColor           = getEntry(pageInputLine, "color:",            false);
    String pageInputBackgroundColor = getEntry(pageInputLine, "background-color:", false);
    String pageInputFontWeight      = getEntry(pageInputLine, "font-weight:",      false);
    String pageInputFontSize        = getEntry(pageInputLine, "font-size:",        true);
    String pageInputFontFamily      = getEntry(pageInputLine, "font-family:",      false);

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Page - Input</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("pageInputFontFamily", pageInputFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='pageInputFontSize' size='2' maxlength='2' value='" + pageInputFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("pageInputFontWeight", pageInputFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Colour</td><td>" + colourDDL("pageInputColor", pageInputColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("pageInputBackgroundColor", pageInputBackgroundColor) + "</td></tr>");

    // misc

    // #textErrorLarge { color: red; background-color: transparent; font-family: courier; font-size: 14pt; font-weight: bold; } // mainBackground
    String textErrorLargeLine = getLine(fh, "#textErrorLarge");
    String textErrorLargeColor          = getEntry(textErrorLargeLine, "color:",           false);
    String textErrorLargeFontWeight     = getEntry(textErrorLargeLine, "font-weight:",     false);
    String textErrorLargeFontSize       = getEntry(textErrorLargeLine, "font-size:",       true);
    String textErrorLargeFontFamily     = getEntry(textErrorLargeLine, "font-family:",     false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Large Error Messages</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("textErrorLargeFontFamily", textErrorLargeFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='textErrorLargeFontSize' size='2' maxlength='2' value='"
                         + textErrorLargeFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("textErrorLargeFontWeight", textErrorLargeFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td colspan=5>" + colourDDL("textErrorLargeColor", textErrorLargeColor) + "</td></tr>");

    // #textNumericValue { color: #880000; background-color: transparent; font: bold 1em/1em Verdana,Arial,Helvetica,sans-serif; } // mainBackground
    String textNumericLine = getLine(fh, "#textNumericValue");
    String textNumericColor      = getEntry(textNumericLine, "color:",           false);
    String textNumericFontWeight = getEntry(textNumericLine, "font-weight:",     false);
    String textNumericFontSize   = getEntry(textNumericLine, "font-size:",       true);
    String textNumericFontFamily = getEntry(textNumericLine, "font-family:",     false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Numeric Messages</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("textNumericFontFamily", textNumericFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='textNumericFontSize' size='2' maxlength='2' value='" + textNumericFontSize
                         + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("textNumericFontWeight", textNumericFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td colspan=5>" + colourDDL("textNumericColor", textNumericColor) + "</td></tr>");

    // #textSalesCreditNote { color: #FF00FF; background-color: transparent; font-family: courier; font-size: 14pt; font-weight: bold; } // mainBackground
    String textSalesCreditNote = getLine(fh, "#textSalesCreditNote");
    String textSalesCreditNoteColor      = getEntry(textSalesCreditNote, "color:",           false);
    String textSalesCreditNoteFontWeight = getEntry(textSalesCreditNote, "font-weight:",     false);
    String textSalesCreditNoteFontSize   = getEntry(textSalesCreditNote, "font-size:",       true);
    String textSalesCreditNoteFontFamily = getEntry(textSalesCreditNote, "font-family:",     false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Credit Note Messages</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("textSalesCreditNoteFontFamily", textSalesCreditNoteFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='textSalesCreditNoteFontSize' size='2' maxlength='2' value='"
                         + textSalesCreditNoteFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("textSalesCreditNoteFontWeight", textSalesCreditNoteFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td colspan=5>" + colourDDL("textSalesCreditNoteColor", textSalesCreditNoteColor) + "</td></tr>");

    // #textReceipt { color: green; background-color: transparent; font-family: courier; font-size: 14pt; font-weight: bold; } // mainBackground
    String textReceipt = getLine(fh, "#textReceipt");
    String textReceiptColor      = getEntry(textReceipt, "color:",       false);
    String textReceiptFontWeight = getEntry(textReceipt, "font-weight:", false);
    String textReceiptFontSize   = getEntry(textReceipt, "font-size:",   true);
    String textReceiptFontFamily = getEntry(textReceipt, "font-family:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Receipt Messages</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("textReceiptFontFamily", textReceiptFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='textReceiptFontSize' size='2' maxlength='2' value='" + textReceiptFontSize
                         + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("textReceiptFontWeight", textReceiptFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td colspan=5>" + colourDDL("textReceiptColor", textReceiptColor) + "</td></tr>");

     // #textInvoice { color: blue; background-color: transparent; font-family: courier; font-size: 14pt; font-weight: bold; } // mainBackground
    String textInvoice = getLine(fh, "#textInvoice");
    String textInvoiceColor      = getEntry(textInvoice, "color:",       false);
    String textInvoiceFontWeight = getEntry(textInvoice, "font-weight:", false);
    String textInvoiceFontSize   = getEntry(textInvoice, "font-size:",   true);
    String textInvoiceFontFamily = getEntry(textInvoice, "font-family:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Sales Invoice Messages</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("textInvoiceFontFamily", textInvoiceFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='textInvoiceFontSize' size='2' maxlength='2' value='" + textInvoiceFontSize
                         + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("textInvoiceFontWeight", textInvoiceFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td colspan=5>" + colourDDL("textInvoiceColor", textInvoiceColor) + "</td></tr>");

    // #textRedHighlighting { color: red; background-color: transparent; font-family: courier; font-size: 14pt; font-weight: bold; }
    String textRedHighlighting = getLine(fh, "#textRedHighlighting");
    String textRedHighlightingColor      = getEntry(textRedHighlighting, "color:",       false);
    String textRedHighlightingFontWeight = getEntry(textRedHighlighting, "font-weight:", false);
    String textRedHighlightingFontSize   = getEntry(textRedHighlighting, "font-size:",   true);
    String textRedHighlightingFontFamily = getEntry(textRedHighlighting, "font-family:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Red Highlighting Messages</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("textRedHighlightingFontFamily", textRedHighlightingFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='textRedHighlightingFontSize' size='2' maxlength='2' value='"
                         + textRedHighlightingFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("textRedHighlightingFontWeight", textRedHighlightingFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td colspan=5>" + colourDDL("textRedHighlightingColor", textRedHighlightingColor) + "</td></tr>");

    // libraries

    // #directoryCell { background-color: #006688; border-color: red; border-width: 1px; border-style: outset; }
    String libraryLine = getLine(fh, "#directoryCell {");
    String libraryBackgroundColor = getEntry(libraryLine, "background-color:", false);
    String libraryBorderColor     = getEntry(libraryLine, "border-color:",     false);
    String libraryBorderWidth     = getEntry(libraryLine, "border-width:",     true);
    String libraryBorderStyle     = getEntry(libraryLine, "border-style:",     false);
    ////// // #directoryCell p { font-weight: normal; font-size 10px; font-family: sans-serif; color: yellow; }
    // #directory p { font-weight: normal; font-size 10px; font-family: sans-serif; color: yellow; background-color: transparent; liiiine-height: 16px; }
    String libraryPLine = getLine(fh, "#directory p");
    String libraryPFontWeight = getEntry(libraryPLine, "font-weight:", false);
    String libraryPFontSize   = getEntry(libraryPLine, "font-size:",   true);
    String libraryPFontFamily = getEntry(libraryPLine, "font-family:", false);
    String libraryPColor      = getEntry(libraryPLine, "color:",       false);
    String libraryPBackgroundColor = getEntry(libraryPLine, "background-color:",       false);
    // #directory { background-color: white; }
    String libraryTableLine = getLine(fh, "#directory {");
    String libraryTableBackgroundColor = getEntry(libraryTableLine, "background-color:",    false);
    // #directory a:link { font-weight: normal; font-size 10px; font-family: sans-serif; color: cyan; text-decoration: none; background-color: transparent;}
    String libraryALinkLine = getLine(fh, "#directory a:link");
    String libraryALinkFontWeight     = getEntry(libraryALinkLine, "font-weight:",     false);
    String libraryALinkFontSize       = getEntry(libraryALinkLine, "font-size:",       true);
    String libraryALinkFontFamily     = getEntry(libraryALinkLine, "font-family:",     false);
    String libraryALinkColor          = getEntry(libraryALinkLine, "color:",           false);
    String libraryALinkTextDecoration = getEntry(libraryALinkLine, "text-decoration:", false);
    // #directory a:hover { font-weight: normal; font-size 10px; font-family: sans-serif; color: white; text-decoration: none; background-color: transparent; }
    String libraryAHoverLine = getLine(fh, "#directory a:hover");
    String libraryAHoverFontWeight     = getEntry(libraryAHoverLine, "font-weight:",     false);
    String libraryAHoverFontSize       = getEntry(libraryAHoverLine, "font-size:",       true);
    String libraryAHoverFontFamily     = getEntry(libraryAHoverLine, "font-family:",     false);
    String libraryAHoverColor          = getEntry(libraryAHoverLine, "color:",           false);
    String libraryAHoverTextDecoration = getEntry(libraryAHoverLine, "text-decoration:", false);

    // #directory a:visited { font-weight: normal; font-size 10px; font-family: sans-serif; color: cyan; text-decoration: none; background-color: transparent; }
    // #directory a:active { font-weight: normal; font-size 10px; font-family: sans-serif; color: cyan; text-decoration: none; background-color: transparent; }

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Libraries - Directory Box</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("libraryPFontFamily", libraryPFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='libraryPFontSize' size='2' maxlength='2' value='" + libraryPFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("libraryPFontWeight", libraryPFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Text</td><td>" + colourDDL("libraryPColor", libraryPColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Text Background</td><td>" + colourDDL("libraryPBackgroundColor", libraryPBackgroundColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Box Background</td><td>" + colourDDL("libraryBackgroundColor", libraryBackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Border</td><td>" + colourDDL("libraryBorderColor", libraryBorderColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Width</td><td><input type='text' name='libraryBorderWidth' size='2' maxlength='2' value='" + libraryBorderWidth + "'></td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td>" + styleDDL("libraryBorderStyle", libraryBorderStyle) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Libraries - Links</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("libraryALinkFontFamily", libraryALinkFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='libraryALinkFontSize' size='2' maxlength='2' value='" + libraryALinkFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("libraryALinkFontWeight", libraryALinkFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Text</td><td>" + colourDDL("libraryALinkColor", libraryALinkColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td colspan=3><p>" + decorationDDL("libraryALinkTextDecoration", libraryALinkTextDecoration) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Libraries - Hover</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Face</td><td>" + familyDDL("libraryAHoverFontFamily", libraryAHoverFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='libraryAHoverFontSize' size='2' maxlength='2' value='" + libraryAHoverFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("libraryAHoverFontWeight", libraryAHoverFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Text</td><td>" + colourDDL("libraryAHoverColor", libraryAHoverColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td colspan=3><p>" + decorationDDL("libraryAHoverTextDecoration", libraryAHoverTextDecoration) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td colspan=6><p><b>Libraries - General</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='lightgrey'><td><p>Screen Background</td><td colspan=5>" + colourDDL("libraryTableBackgroundColor", libraryTableBackgroundColor) + "</td></tr>");

    // Channels

    // #channelHeader { color: red; border-width: 13px; border-style: dashed; border-color: red; background-color: white; font-weight: normal; font-size: 22pt; font-family: Verdana,Arial,Helvetica,sans-serif; }
    String channelHeader = getLine(fh, "#channelHeader");
    String channelHeaderFontFamily      = getEntry(channelHeader, "font-family:",      false);
    String channelHeaderFontSize        = getEntry(channelHeader, "font-size:",        true);
    String channelHeaderFontWeight      = getEntry(channelHeader, "font-weight:",      false);
    String channelHeaderFontColor       = getEntry(channelHeader, "color:",            false);
    String channelHeaderBackgroundColor = getEntry(channelHeader, "background-color:", false);
    String channelHeaderBorderWidth     = getEntry(channelHeader, "border-width:",     true);
    String channelHeaderBorderColor     = getEntry(channelHeader, "border-color:",     false);
    String channelHeaderBorderStyle     = getEntry(channelHeader, "border-style:",     false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Channel Header</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("channelHeaderFontFamily", channelHeaderFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='channelHeaderFontSize' size='2' maxlength='2' value='" + channelHeaderFontSize + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("channelHeaderFontWeight", channelHeaderFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td>" + colourDDL("channelHeaderFontColor", channelHeaderFontColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("channelHeaderBackgroundColor", channelHeaderBackgroundColor) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Border</td><td>" + colourDDL("channelHeaderBorderColor", channelHeaderBorderColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Width</td><td><input type='text' name='channelHeaderBorderWidth' size='2' maxlength='2' value='" + channelHeaderBorderWidth + "'></td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td>" + styleDDL("channelHeaderBorderStyle", channelHeaderBorderStyle) + "</td></tr>");

    // #channel {  color: red; background-color: blue; font-weight: normal; font-size: 12pt; font-family: serif; }

    String channel = getLine(fh, "#channel {");
    String channelFontFamily      = getEntry(channel, "font-family:",      false);
    String channelFontSize        = getEntry(channel, "font-size:",        true);
    String channelFontWeight      = getEntry(channel, "font-weight:",      false);
    String channelFontColor       = getEntry(channel, "color:",            false);
    String channelBackgroundColor = getEntry(channel, "background-color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Channel</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("channelFontFamily", channelFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='channelFontSize' size='2' maxlength='2' value='" + channelFontSize + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("channelFontWeight", channelFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td>" + colourDDL("channelFontColor", channelFontColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("channelBackgroundColor", channelBackgroundColor) + "</td></tr>");

    // #channelMessage textarea { color: red; background-color: yellow; font-weight: bold; font-size: 13pt; font-family: Verdana,Arial,Helvetica,sans-serif; }
    String channelMsg = getLine(fh, "#channelMessage");
    String channelMsgFontFamily      = getEntry(channelMsg, "font-family:",      false);
    String channelMsgFontSize        = getEntry(channelMsg, "font-size:",        true);
    String channelMsgFontWeight      = getEntry(channelMsg, "font-weight:",      false);
    String channelMsgFontColor       = getEntry(channelMsg, "color:",            false);
    String channelMsgBackgroundColor = getEntry(channelMsg, "background-color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Channel Message</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("channelMsgFontFamily", channelMsgFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='channelMsgFontSize' size='2' maxlength='2' value='" + channelMsgFontSize + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("channelMsgFontWeight", channelMsgFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td>" + colourDDL("channelMsgFontColor", channelMsgFontColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("channelMsgBackgroundColor", channelMsgBackgroundColor) + "</td></tr>");

    // #channelButtons { color: red; background-color: cyan; font-weight: bold; font-size: 13pt; font-family: Verdana,Arial,Helvetica,sans-serif; }
    String channelButtons = getLine(fh, "#channelButtons { ");
    String channelButtonsFontFamily      = getEntry(channelButtons, "font-family:",      false);
    String channelButtonsFontSize        = getEntry(channelButtons, "font-size:",        true);
    String channelButtonsFontWeight      = getEntry(channelButtons, "font-weight:",      false);
    String channelButtonsFontColor       = getEntry(channelButtons, "color:",            false);
    String channelButtonsBackgroundColor = getEntry(channelButtons, "background-color:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Channel Buttons</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("channelButtonsFontFamily", channelButtonsFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='channelButtonsFontSize' size='2' maxlength='2' value='" + channelButtonsFontSize + "'> pixels</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("channelButtonsFontWeight", channelButtonsFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td>" + colourDDL("channelButtonsFontColor", channelButtonsFontColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Background</td><td colspan=3>" + colourDDL("channelButtonsBackgroundColor", channelButtonsBackgroundColor) + "</td></tr>");

    // #channelButtons a:link { font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 10pt; font-weight: normal; color: darkmagenta; text-decoration: none; background-color: transparent; }
    String channelButtonsALink = getLine(fh, "#channelButtons a:link");
    String channelButtonsALinkFontWeight     = getEntry(channelButtonsALink, "font-weight:",     false);
    String channelButtonsALinkFontSize       = getEntry(channelButtonsALink, "font-size:",       true);
    String channelButtonsALinkFontFamily     = getEntry(channelButtonsALink, "font-family:",     false);
    String channelButtonsALinkColor          = getEntry(channelButtonsALink, "color:",           false);
    String channelButtonsALinkTextDecoration = getEntry(channelButtonsALink, "text-decoration:", false);
    // #channelButtons a:hover { color: navy; text-decoration: none; background-color: transparent;font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 10pt; font-weight: normal; }
    String channelButtonsAHover = getLine(fh, "#channelButtons a:hover");
    String channelButtonsAHoverFontWeight     = getEntry(channelButtonsAHover, "font-weight:",     false);
    String channelButtonsAHoverFontSize       = getEntry(channelButtonsAHover, "font-size:",       true);
    String channelButtonsAHoverFontFamily     = getEntry(channelButtonsAHover, "font-family:",     false);
    String channelButtonsAHoverColor          = getEntry(channelButtonsAHover, "color:",           false);
    String channelButtonsAHoverTextDecoration = getEntry(channelButtonsAHover, "text-decoration:", false);

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Channel - Links</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("channelButtonsALinkFontFamily", channelButtonsALinkFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='channelButtonsALinkFontSize' size='2' maxlength='2' value='" + channelButtonsALinkFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("channelButtonsALinkFontWeight", channelButtonsALinkFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td>" + colourDDL("channelButtonsALinkColor", channelButtonsALinkColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td colspan=3><p>" + decorationDDL("channelButtonsALinkTextDecoration", channelButtonsALinkTextDecoration) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><hr></td></tr>"); // ------------------------------------------------------------------
    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td colspan=6><p><b>Channel - Hover</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Face</td><td>" + familyDDL("channelButtonsAHoverFontFamily", channelButtonsAHoverFontFamily) + "</td>");

    scoutln(out, bytesOut, "<td><p>Size</td><td><p><input type='text' name='channelButtonsAHoverFontSize' size='2' maxlength='2' value='" + channelButtonsAHoverFontSize + "'> point</td>");

    scoutln(out, bytesOut, "<td><p>Weight</td><td>" + weightDDL("channelButtonsAHoverFontWeight", channelButtonsAHoverFontWeight) + "</td></tr>");

    scoutln(out, bytesOut, "<tr bgcolor='darkgray'><td><p>Text</td><td>" + colourDDL("channelButtonsAHoverColor", channelButtonsAHoverColor) + "</td>");

    scoutln(out, bytesOut, "<td><p>Style</td><td colspan=3><p>" + decorationDDL("channelButtonsAHoverTextDecoration", channelButtonsAHoverTextDecoration) + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td><p><a href=\"javascript:update()\">Update</a></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");

    generalUtils.fileClose(fh);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String familyDDL(String ddlName, String currentValue) throws Exception
  {
    if(! currentValue.equals("sans-serif") && ! currentValue.equals("serif") && ! currentValue.equals("courier"))
      currentValue = "sans-serif";

    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='sans-serif'";
    if(currentValue.contains("sans-serif"))
      s += " selected";
    s += ">sans-serif";

    s += "<option class='transparent' value='serif'";
    if(currentValue.contains("serif") && ! currentValue.contains("sans-serif"))
      s += " selected";
    s += ">serif";

    s += "<option class='transparent' value='courier'";
    if(currentValue.contains("courier"))
      s += " selected";
    s += ">courier";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String decorationDDL(String ddlName, String currentValue) throws Exception
  {
    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='none'";
    if(currentValue.equals("none"))
      s += " selected";
    s += ">none";

    s += "<option class='transparent' value='underline'";
    if(currentValue.equals("underline"))
      s += " selected";
    s += ">underline";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String alignDDL(String ddlName, String currentValue) throws Exception
  {
    if(! currentValue.equals("left") && ! currentValue.equals("center") && ! currentValue.equals("right"))
      currentValue = "left";

    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='left'";
    if(currentValue.equals("left"))
      s += " selected";
    s += ">left";

    s += "<option class='transparent' value='center'";
    if(currentValue.equals("center"))
      s += " selected";
    s += ">center";

    s += "<option class='transparent' value='right'";
    if(currentValue.equals("right"))
      s += " selected";
    s += ">right";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String weightDDL(String ddlName, String currentValue) throws Exception
  {
    if(! currentValue.equals("normal") && ! currentValue.equals("bold"))
      currentValue = "normal";

    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='normal'";
    if(currentValue.equals("normal"))
      s += " selected";
    s += ">normal";

    s += "<option class='transparent' value='bold'";
    if(currentValue.equals("bold"))
      s += " selected";
    s += ">bold";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String repeatDDL(String ddlName, String currentValue) throws Exception
  {
    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='no-repeat'";
    if(currentValue.equals("no-repeat"))
      s += " selected";
    s += ">no-repeat";

    s += "<option class='transparent' value='repeat'";
    if(currentValue.equals("repeat"))
      s += " selected";
    s += ">repeat";

    s += "<option class='transparent' value='repeat-x'";
    if(currentValue.equals("repeat-x"))
      s += " selected";
    s += ">repeat horizontally";

    s += "<option class='transparent' value='repeat-y'";
    if(currentValue.equals("repeat-y"))
      s += " selected";
    s += ">repeat vertically";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String positionDDL(String ddlName, String currentValue) throws Exception
  {
    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='top center'";
    if(currentValue.equals("top center"))
      s += " selected";
    s += ">top center";

    s += "<option class='transparent' value='top right'";
    if(currentValue.equals("top right"))
      s += " selected";
    s += ">top right";

    s += "<option class='transparent' value='center left'";
    if(currentValue.equals("center left"))
      s += " selected";
    s += ">center left";

    s += "<option class='transparent' value='center center'";
    if(currentValue.equals("center center"))
      s += " selected";
    s += ">center center";

    s += "<option class='transparent' value='center right'";
    if(currentValue.equals("center right"))
      s += " selected";
    s += ">center right";

    s += "<option class='transparent' value='bottom left'";
    if(currentValue.equals("bottom left"))
      s += " selected";
    s += ">bottom left";

    s += "<option class='transparent' value='bottom center'";
    if(currentValue.equals("bottom center"))
      s += " selected";
    s += ">bottom center";

    s += "<option class='transparent' value='bottom right'";
    if(currentValue.equals("bottom right"))
      s += " selected";
    s += ">bottom right";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String layoutDDL(String ddlName, String currentValue) throws Exception
  {
    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='1'";
    if(currentValue.equals("1"))
      s += " selected";
    s += ">1: Full-Width, Logo Left";

    s += "<option class='transparent' value='2'";
    if(currentValue.equals("2"))
      s += " selected";
    s += ">2: Full-Width, Logo Right";

    s += "<option class='transparent' value='3'";
    if(currentValue.equals("3"))
      s += " selected";
    s += ">3: Fixed-Width, Logo Left";

    s += "<option class='transparent' value='4'";
    if(currentValue.equals("4"))
      s += " selected";
    s += ">4: Fixed-Width, Logo Right";

    s += "<option class='transparent' value='5'";
    if(currentValue.equals("5"))
      s += " selected";
    s += ">5: Fixed-Width, Full-Width Logo";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String styleDDL(String ddlName, String currentValue) throws Exception
  {
    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='none'";
    if(currentValue.equals("none"))
      s += " selected";
    s += ">none";

    s += "<option class='transparent' value='hidden'";
    if(currentValue.equals("hidden"))
      s += " selected";
    s += ">hidden";

    s += "<option class='transparent' value='dotted'";
    if(currentValue.equals("dotted"))
      s += " selected";
    s += ">dotted";

    s += "<option class='transparent' value='dashed'";
    if(currentValue.equals("dashed"))
      s += " selected";
    s += ">dashed";

    s += "<option class='transparent' value='solid'";
    if(currentValue.equals("solid"))
      s += " selected";
    s += ">solid";

    s += "<option class='transparent' value='double'";
    if(currentValue.equals("double"))
      s += " selected";
    s += ">double";

    s += "<option class='transparent' value='groove'";
    if(currentValue.equals("groove"))
      s += " selected";
    s += ">groove";

    s += "<option class='transparent' value='ridge'";
    if(currentValue.equals("ridge"))
      s += " selected";
    s += ">ridge";

    s += "<option class='transparent' value='inset'";
    if(currentValue.equals("inset"))
      s += " selected";
    s += ">inset";

    s += "<option class='transparent' value='outset'";
    if(currentValue.equals("outset"))
      s += " selected";
    s += ">outset";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String fontstyleDDL(String ddlName, String currentValue) throws Exception
  {
    if(! currentValue.equals("normal") && ! currentValue.equals("italic"))
      currentValue = "normal";

    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='normal'";
    if(currentValue.equals("normal"))
      s += " selected";
    s += ">normal";

    s += "<option class='transparent' value='italic'";
    if(currentValue.equals("italic"))
      s += " selected";
    s += ">italic";

    s += "</select>";

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // #copyright { margin-top: 20px; font-size: 80%; color: darkblue; }
  private String getLine(RandomAccessFile fh, String entryToFind) throws Exception
  {
    fh.seek(0);

    int x, len, entryToFindLength = entryToFind.length();
    String s, t;

    try
    {
      while(true)
      {
        s = fh.readLine();

        len = s.length();

        x=0;
        while(x < len && s.charAt(x) == ' ') // just-in-case
          ++x;

        t = "";
        while(x < len && x < entryToFindLength) // just-in-case
          t += s.charAt(x++);

        if(t.equals(entryToFind))
          return s;
      }
    }
    catch(Exception e) { }

    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // #copyright { margin-top: 20px; font-size: 8pt; color: darkblue; }
  private String getEntry(String entry, String elementToFind, boolean stripTrailing) throws Exception
  {
    int len = entry.length();
    if(len== 0)
      return "";

    int i;

    if((i = entry.indexOf(elementToFind)) == -1)
      return "";

    int x = i + elementToFind.length();
    while(x < len && entry.charAt(x) == ' ') // just-in-case
      ++x;

    String t = "";
    while(x < len && entry.charAt(x) != ';' && entry.charAt(x) != '}') // just-in-case
      t += entry.charAt(x++);

    if(stripTrailing)
    {
      x = t.length() - 1;
      while(x >= 0 && t.charAt(x) != ' ' && (t.charAt(x) < '0' || t.charAt(x) > '9')) // just-in-case
        --x;

      while(x >= 0 && entry.charAt(x) == ' ') // just-in-case
        --x;

      t = t.substring(0, x + 1);
    }

    if(t.startsWith("url"))
    {
      x = t.length() - 1;
      while(x >= 0 && (t.charAt(x) == ' ' || t.charAt(x) == ';' || t.charAt(x) == ')' || t.charAt(x) == '"')) // just-in-case
        --x;

      t = t.substring(0, x + 1);

      x = 4;
      if(t.charAt(x) == '"')
        ++x;
      t = t.substring(x);
    }

    return t;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {
    out.println(str);
    bytesOut[0] += str.length() + 2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String colourDDL(String ddlName, String currentValue) throws Exception
  {
    if(currentValue.length() == 0)
      currentValue = "transparent";

    String s = "<select name='" + ddlName + "'>";

    s += "<option class='transparent' value='transparent'";          if(currentValue.equals("transparent"))          s += " selected"; s += ">transparent";
    s += "<option class='aliceblue'   value='aliceblue'";            if(currentValue.equals("aliceblue"))            s += " selected"; s += ">aliceblue";
    s += "<option class='antiquewhite' value='antiquewhite'";         if(currentValue.equals("antiquewhite"))         s += " selected"; s += ">antiquewhite";
    s += "<option class='aqua' value='aqua'";                 if(currentValue.equals("aqua"))                 s += " selected"; s += ">aqua";
    s += "<option class='aquamarine' value='aquamarine'";           if(currentValue.equals("aquamarine"))           s += " selected"; s += ">aquamarine";
    s += "<option class='azure' value='azure'";                if(currentValue.equals("azure"))                s += " selected"; s += ">azure";
    s += "<option class='beige' value='beige'";                if(currentValue.equals("beige"))                s += " selected"; s += ">beige";
    s += "<option class='bisque' value='bisque'";               if(currentValue.equals("bisque"))               s += " selected"; s += ">bisque";
    s += "<option class='black' value='black'";                if(currentValue.equals("black"))                s += " selected"; s += ">black";
    s += "<option class='blanchedalmond' value='blanchedalmond'";       if(currentValue.equals("blanchedalmond"))       s += " selected"; s += ">blanchedalmond";
    s += "<option class='blue' value='blue'";                 if(currentValue.equals("blue"))                 s += " selected"; s += ">blue";
    s += "<option class='blueviolet' value='blueviolet'";           if(currentValue.equals("blueviolet"))           s += " selected"; s += ">blueviolet";
    s += "<option class='brown' value='brown'";                if(currentValue.equals("brown"))                s += " selected"; s += ">brown";
    s += "<option class='burlywood' value='burlywood'";            if(currentValue.equals("burlywood"))            s += " selected"; s += ">burlywood";
    s += "<option class='cadetblue' value='cadetblue'";            if(currentValue.equals("cadetblue"))            s += " selected"; s += ">cadetblue";
    s += "<option class='chartreuse' value='chartreuse'";           if(currentValue.equals("chartreuse"))           s += " selected"; s += ">chartreuse";
    s += "<option class='chocolate' value='chocolate'";            if(currentValue.equals("chocolate"))            s += " selected"; s += ">chocolate";
    s += "<option class='coral' value='coral'";                if(currentValue.equals("coral"))                s += " selected"; s += ">coral";
    s += "<option class='cornflowerblue' value='cornflowerblue'";       if(currentValue.equals("cornflowerblue"))       s += " selected"; s += ">cornflowerblue";
    s += "<option class='cornsilk' value='cornsilk'";             if(currentValue.equals("cornsilk"))             s += " selected"; s += ">cornsilk";
    s += "<option class='crimson' value='crimson'";              if(currentValue.equals("crimson"))              s += " selected"; s += ">crimson";
    s += "<option class='cyan' value='cyan'";                 if(currentValue.equals("cyan"))                 s += " selected"; s += ">cyan";
    s += "<option class='darkblue' value='darkblue'";             if(currentValue.equals("darkblue"))             s += " selected"; s += ">darkblue";
    s += "<option class='darkcyan' value='darkcyan'";             if(currentValue.equals("darkcyan"))             s += " selected"; s += ">darkcyan";
    s += "<option class='darkgoldenrod' value='darkgoldenrod'";        if(currentValue.equals("darkgoldenrod"))        s += " selected"; s += ">darkgoldenrod";
    s += "<option class='darkgray' value='darkgray'";             if(currentValue.equals("darkgray"))             s += " selected"; s += ">darkgray";
    s += "<option class='darkgreen' value='darkgreen'";            if(currentValue.equals("darkgreen"))            s += " selected"; s += ">darkgreen";
    s += "<option class='darkkhaki' value='darkkhaki'";            if(currentValue.equals("darkkhaki"))            s += " selected"; s += ">darkkhaki";
    s += "<option class='darkmagenta' value='darkmagenta'";          if(currentValue.equals("darkmagenta"))          s += " selected"; s += ">darkmagenta";
    s += "<option class='darkolivegreen' value='darkolivegreen'";       if(currentValue.equals("darkolivegreen"))       s += " selected"; s += ">darkolivegreen";
    s += "<option class='darkorange' value='darkorange'";           if(currentValue.equals("darkorange"))           s += " selected"; s += ">darkorange";
    s += "<option class='darkorchid' value='darkorchid'";           if(currentValue.equals("darkorchid"))           s += " selected"; s += ">darkorchid";
    s += "<option class='darkred' value='darkred'";              if(currentValue.equals("darkred"))              s += " selected"; s += ">darkred";
    s += "<option class='darksalmon' value='darksalmon'";           if(currentValue.equals("darksalmon"))           s += " selected"; s += ">darksalmon";
    s += "<option class='darkseagreen' value='darkseagreen'";         if(currentValue.equals("darkseagreen"))         s += " selected"; s += ">darkseagreen";
    s += "<option class='darkslateblue' value='darkslateblue'";        if(currentValue.equals("darkslateblue"))        s += " selected"; s += ">darkslateblue";
    s += "<option class='darkslategray' value='darkslategray'";        if(currentValue.equals("darkslategray"))        s += " selected"; s += ">darkslategray";
    s += "<option class='darkturquoise' value='darkturquoise'";        if(currentValue.equals("darkturquoise"))        s += " selected"; s += ">darkturquoise";
    s += "<option class='darkviolet' value='darkviolet'";           if(currentValue.equals("darkviolet"))           s += " selected"; s += ">darkviolet";
    s += "<option class='deeppink' value='deeppink'";             if(currentValue.equals("deeppink"))             s += " selected"; s += ">deeppink";
    s += "<option class='deepskyblue' value='deepskyblue'";          if(currentValue.equals("deepskyblue"))          s += " selected"; s += ">deepskyblue";
    s += "<option class='dimgray' value='dimgray'";              if(currentValue.equals("dimgray"))              s += " selected"; s += ">dimgray";
    s += "<option class='dodgerblue' value='dodgerblue'";           if(currentValue.equals("dodgerblue"))           s += " selected"; s += ">dodgerblue";
    s += "<option class='firebrick' value='firebrick'";            if(currentValue.equals("firebrick"))            s += " selected"; s += ">firebrick";
    s += "<option class='floralwhite' value='floralwhite'";          if(currentValue.equals("floralwhite"))          s += " selected"; s += ">floralwhite";
    s += "<option class='forestgreen' value='forestgreen'";          if(currentValue.equals("forestgreen"))          s += " selected"; s += ">forestgreen";
    s += "<option class='fuchsia' value='fuchsia'";              if(currentValue.equals("fuchsia"))              s += " selected"; s += ">fuchsia";
    s += "<option class='gainsboro' value='gainsboro'";            if(currentValue.equals("gainsboro"))            s += " selected"; s += ">gainsboro";
    s += "<option class='ghostwhite' value='ghostwhite'";           if(currentValue.equals("ghostwhite"))           s += " selected"; s += ">ghostwhite";
    s += "<option class='gold' value='gold'";                 if(currentValue.equals("gold"))                 s += " selected"; s += ">gold";
    s += "<option class='goldenrod' value='goldenrod'";            if(currentValue.equals("goldenrod"))            s += " selected"; s += ">goldenrod";
    s += "<option class='gray' value='gray'";                 if(currentValue.equals("gray"))                 s += " selected"; s += ">gray";
    s += "<option class='green' value='green'";                if(currentValue.equals("green"))                s += " selected"; s += ">green";
    s += "<option class='greenyellow' value='greenyellow'";          if(currentValue.equals("greenyellow"))          s += " selected"; s += ">greenyellow";
    s += "<option class='honeydew' value='honeydew'";             if(currentValue.equals("honeydew"))             s += " selected"; s += ">honeydew";
    s += "<option class='hotpink' value='hotpink'";              if(currentValue.equals("hotpink"))              s += " selected"; s += ">hotpink";
    s += "<option class='indianred' value='indianred'";            if(currentValue.equals("indianred"))            s += " selected"; s += ">indianred";
    s += "<option class='indigo' value='indigo'";               if(currentValue.equals("indigo"))               s += " selected"; s += ">indigo";
    s += "<option class='ivory' value='ivory'";                if(currentValue.equals("ivory"))                s += " selected"; s += ">ivory";
    s += "<option class='khaki' value='khaki'";                if(currentValue.equals("khaki"))                s += " selected"; s += ">khaki";
    s += "<option class='lavender' value='lavender'";             if(currentValue.equals("lavender"))             s += " selected"; s += ">lavender";
    s += "<option class='lavenderblush' value='lavenderblush'";        if(currentValue.equals("lavenderblush"))        s += " selected"; s += ">lavenderblush";
    s += "<option class='lawngreen' value='lawngreen'";            if(currentValue.equals("lawngreen"))            s += " selected"; s += ">lawngreen";
    s += "<option class='lemonchiffon' value='lemonchiffon'";         if(currentValue.equals("lemonchiffon"))         s += " selected"; s += ">lemonchiffon";
    s += "<option class='lightblue' value='lightblue'";            if(currentValue.equals("lightblue"))            s += " selected"; s += ">lightblue";
    s += "<option class='lightcoral' value='lightcoral'";           if(currentValue.equals("lightcoral"))           s += " selected"; s += ">lightcoral";
    s += "<option class='lightcyan' value='lightcyan'";            if(currentValue.equals("lightcyan"))            s += " selected"; s += ">lightcyan";
    s += "<option class='lightgoldenrodyellow' value='lightgoldenrodyellow'"; if(currentValue.equals("lightgoldenrodyellow")) s += " selected"; s += ">lightgoldenrodyellow";
    s += "<option class='lightgreen' value='lightgreen'";           if(currentValue.equals("lightgreen"))           s += " selected"; s += ">lightgreen";
    s += "<option class='lightgray' value='lightgray'";            if(currentValue.equals("lightgray"))            s += " selected"; s += ">lightgray";
    s += "<option class='lightpink' value='lightpink'";            if(currentValue.equals("lightpink"))            s += " selected"; s += ">lightpink";
    s += "<option class='lightsalmon' value='lightsalmon'";          if(currentValue.equals("lightsalmon"))          s += " selected"; s += ">lightsalmon";
    s += "<option class='lightseagreen' value='lightseagreen'";        if(currentValue.equals("lightseagreen"))        s += " selected"; s += ">lightseagreen";
    s += "<option class='lightskyblue' value='lightskyblue'";         if(currentValue.equals("lightskyblue"))         s += " selected"; s += ">lightskyblue";
    s += "<option class='lightslategray' value='lightslategray'";       if(currentValue.equals("lightslategray"))       s += " selected"; s += ">lightslategray";
    s += "<option class='lightsteelblue' value='lightsteelblue'";       if(currentValue.equals("lightsteelblue"))       s += " selected"; s += ">lightsteelblue";
    s += "<option class='lightyellow' value='lightyellow'";          if(currentValue.equals("lightyellow"))          s += " selected"; s += ">lightyellow";
    s += "<option class='lime' value='lime'";                 if(currentValue.equals("lime"))                 s += " selected"; s += ">lime";
    s += "<option class='limegreen' value='limegreen'";            if(currentValue.equals("limegreen"))            s += " selected"; s += ">limegreen";
    s += "<option class='linen' value='linen'";                if(currentValue.equals("linen"))                s += " selected"; s += ">linen";
    s += "<option class='magenta' value='magenta'";              if(currentValue.equals("magenta"))              s += " selected"; s += ">magenta";
    s += "<option class='maroon' value='maroon'";               if(currentValue.equals("maroon"))               s += " selected"; s += ">maroon";
    s += "<option class='mediumaquamarine' value='mediumaquamarine'";     if(currentValue.equals("mediumaquamarine"))     s += " selected"; s += ">mediumaquamarine";
    s += "<option class='mediumblue' value='mediumblue'";           if(currentValue.equals("mediumblue"))           s += " selected"; s += ">mediumblue";
    s += "<option class='mediumorchid' value='mediumorchid'";         if(currentValue.equals("mediumorchid"))         s += " selected"; s += ">mediumorchid";
    s += "<option class='mediumpurple' value='mediumpurple'";         if(currentValue.equals("mediumpurple"))         s += " selected"; s += ">mediumpurple";
    s += "<option class='mediumseagreen' value='mediumseagreen'";       if(currentValue.equals("mediumseagreen"))       s += " selected"; s += ">mediumseagreen";
    s += "<option class='mediumslateblue' value='mediumslateblue'";      if(currentValue.equals("mediumslateblue"))      s += " selected"; s += ">mediumslateblue";
    s += "<option class='mediumspringgreen' value='mediumspringgreen'";    if(currentValue.equals("mediumspringgreen"))    s += " selected"; s += ">mediumspringgreen";
    s += "<option class='mediumturquoise' value='mediumturquoise'";      if(currentValue.equals("mediumturquoise"))      s += " selected"; s += ">mediumturquoise";
    s += "<option class='mediumvioletred' value='mediumvioletred'";      if(currentValue.equals("mediumvioletred"))      s += " selected"; s += ">mediumvioletred";
    s += "<option class='midnightblue' value='midnightblue'";         if(currentValue.equals("midnightblue"))         s += " selected"; s += ">midnightblue";
    s += "<option class='mintcream' value='mintcream'";            if(currentValue.equals("mintcream"))            s += " selected"; s += ">mintcream";
    s += "<option class='mistyrose' value='mistyrose'";            if(currentValue.equals("mistyrose"))            s += " selected"; s += ">mistyrose";
    s += "<option class='moccasin' value='moccasin'";             if(currentValue.equals("moccasin"))             s += " selected"; s += ">moccasin";
    s += "<option class='navajowhite' value='navajowhite'";          if(currentValue.equals("navajowhite"))          s += " selected"; s += ">navajowhite";
    s += "<option class='navy' value='navy'";                 if(currentValue.equals("navy"))                 s += " selected"; s += ">navy";
    s += "<option class='oldlace' value='oldlace'";              if(currentValue.equals("oldlace"))              s += " selected"; s += ">oldlace";
    s += "<option class='olive' value='olive'";                if(currentValue.equals("olive"))                s += " selected"; s += ">olive";
    s += "<option class='olivedrab' value='olivedrab'";            if(currentValue.equals("olivedrab"))            s += " selected"; s += ">olivedrab";
    s += "<option class='orange' value='orange'";               if(currentValue.equals("orange"))               s += " selected"; s += ">orange";
    s += "<option class='orangered' value='orangered'";            if(currentValue.equals("orangered"))            s += " selected"; s += ">orangered";
    s += "<option class='orchid' value='orchid'";               if(currentValue.equals("orchid"))               s += " selected"; s += ">orchid";
    s += "<option class='palegoldenrod' value='palegoldenrod'";        if(currentValue.equals("palegoldenrod"))        s += " selected"; s += ">palegoldenrod";
    s += "<option class='palegreen' value='palegreen'";            if(currentValue.equals("palegreen"))            s += " selected"; s += ">palegreen";
    s += "<option class='paleturquoise' value='paleturquoise'";        if(currentValue.equals("paleturquoise"))        s += " selected"; s += ">paleturquoise";
    s += "<option class='palevioletred' value='palevioletred'";        if(currentValue.equals("palevioletred"))        s += " selected"; s += ">palevioletred";
    s += "<option class='papayawhip' value='papayawhip'";           if(currentValue.equals("papayawhip"))           s += " selected"; s += ">papayawhip";
    s += "<option class='peachpuff' value='peachpuff'";            if(currentValue.equals("peachpuff"))            s += " selected"; s += ">peachpuff";
    s += "<option class='peru' value='peru'";                 if(currentValue.equals("peru"))                 s += " selected"; s += ">peru";
    s += "<option class='pink' value='pink'";                 if(currentValue.equals("pink"))                 s += " selected"; s += ">pink";
    s += "<option class='plum' value='plum'";                 if(currentValue.equals("plum"))                 s += " selected"; s += ">plum";
    s += "<option class='powderblue' value='powderblue'";           if(currentValue.equals("powderblue"))           s += " selected"; s += ">powderblue";
    s += "<option class='purple' value='purple'";               if(currentValue.equals("purple"))               s += " selected"; s += ">purple";
    s += "<option class='red' value='red'";                  if(currentValue.equals("red"))                  s += " selected"; s += ">red";
    s += "<option class='rosybrown' value='rosybrown'";            if(currentValue.equals("rosybrown"))            s += " selected"; s += ">rosybrown";
    s += "<option class='royalblue' value='royalblue'";            if(currentValue.equals("royalblue"))            s += " selected"; s += ">royalblue";
    s += "<option class='saddlebrown' value='saddlebrown'";          if(currentValue.equals("saddlebrown"))          s += " selected"; s += ">saddlebrown";
    s += "<option class='salmon' value='salmon'";               if(currentValue.equals("salmon"))               s += " selected"; s += ">salmon";
    s += "<option class='sandybrown' value='sandybrown'";           if(currentValue.equals("sandybrown"))           s += " selected"; s += ">sandybrown";
    s += "<option class='seagreen' value='seagreen'";             if(currentValue.equals("seagreen"))             s += " selected"; s += ">seagreen";
    s += "<option class='seashell' value='seashell'";             if(currentValue.equals("seashell"))             s += " selected"; s += ">seashell";
    s += "<option class='sienna' value='sienna'";               if(currentValue.equals("sienna"))               s += " selected"; s += ">sienna";
    s += "<option class='silver' value='silver'";               if(currentValue.equals("silver"))               s += " selected"; s += ">silver";
    s += "<option class='skyblue' value='skyblue'";              if(currentValue.equals("skyblue"))              s += " selected"; s += ">skyblue";
    s += "<option class='slateblue' value='slateblue'";            if(currentValue.equals("slateblue"))            s += " selected"; s += ">slateblue";
    s += "<option class='slategray' value='slategray'";            if(currentValue.equals("slategray"))            s += " selected"; s += ">slategray";
    s += "<option class='snow' value='snow'";                 if(currentValue.equals("snow"))                 s += " selected"; s += ">snow";
    s += "<option class='springgreen' value='springgreen'";          if(currentValue.equals("springgreen"))          s += " selected"; s += ">springgreen";
    s += "<option class='steelblue' value='steelblue'";            if(currentValue.equals("steelblue"))            s += " selected"; s += ">steelblue";
    s += "<option class='tan' value='tan'";                  if(currentValue.equals("tan"))                  s += " selected"; s += ">tan";
    s += "<option class='teal' value='teal'";                 if(currentValue.equals("teal"))                 s += " selected"; s += ">teal";
    s += "<option class='thistle' value='thistle'";              if(currentValue.equals("thistle"))              s += " selected"; s += ">thistle";
    s += "<option class='tomato' value='tomato'";               if(currentValue.equals("tomato"))               s += " selected"; s += ">tomato";
    s += "<option class='turquoise' value='turquoise'";            if(currentValue.equals("turquoise"))            s += " selected"; s += ">turquoise";
    s += "<option class='violet' value='violet'";               if(currentValue.equals("violet"))               s += " selected"; s += ">violet";
    s += "<option class='wheat' value='wheat'";                if(currentValue.equals("wheat"))                s += " selected"; s += ">wheat";
    s += "<option class='white' value='white'";                if(currentValue.equals("white"))                s += " selected"; s += ">white";
    s += "<option class='whitesmoke' value='whitesmoke'";           if(currentValue.equals("whitesmoke"))           s += " selected"; s += ">whitesmoke";
    s += "<option class='yellow' value='yellow'";               if(currentValue.equals("yellow"))               s += " selected"; s += ">yellow";
    s += "<option class='yellowgreen' value='yellowgreen'";          if(currentValue.equals("yellowgreen"))          s += " selected"; s += ">yellowgreen";

    s += "</select>";

    return s;
  }

}
