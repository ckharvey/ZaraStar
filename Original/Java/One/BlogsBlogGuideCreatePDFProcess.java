// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Create PDF BlogGuide
// Module: BlogsBlogGuideCreatePDFProcess.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class BlogsBlogGuideCreatePDFProcess extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  BlogsBlogGuideCreatePDFUpdate blogsBlogGuideCreatePDFUpdate = new BlogsBlogGuideCreatePDFUpdate();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

    try
    {
      unm = req.getParameter("unm");
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // name
      p2  = req.getParameter("p2"); // layout type
      p3  = req.getParameter("p3"); // version

      if(p2 == null) p2 = "4"; // A4
      if(p3 == null) p3 = "F"; // full

      doIt(out, req, res, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "BlogsBlogGuideCreatePDFProcess", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8113, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, HttpServletResponse res, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String name, String layout, String version, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);
    String imageLibDir = directoryUtils.getImagesDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs=null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "8113a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8113, bytesOut[0], 0, "SID:" + name);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    create(workingDir + name + ".pdf", name, layout, version, dnm, imageLibDir, localDefnsDir, defnsDir);

    download(res, workingDir, name + ".pdf", bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8113, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), name);
    if(con != null) con.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void download(HttpServletResponse res, String dirName, String fileName, int[] bytesOut) throws Exception
  {
    res.setContentType("application/x-download");
    res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    OutputStream out = res.getOutputStream();

    InputStream in = null;
    try
    {
      in = new BufferedInputStream(new FileInputStream(dirName + fileName));
      byte[] buf = new byte[4096];
      int bytesRead;
      while((bytesRead = in.read(buf)) != -1)
        out.write(buf, 0, bytesRead);
    }
    catch(Exception e) //finally
    {
      if(in != null)
        in.close();
    }

    File file = new File(dirName + fileName);
    long fileSize = file.length();

    bytesOut[0] += (int)fileSize;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(String fileName, String name, String layout, String version, String dnm, String imageLibDir, String localDefnsDir, String defnsDir) throws Exception
  {
    Statement infoStmt = null, infoStmt2 = null, infoStmt3 = null;
    ResultSet infoRs   = null, infoRs2   = null, infoRs3   = null;

    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();

    Connection conInfo = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_info?user=" + uName + "&password=" + pWord);

    Document document;
    if(layout.equals("L"))
      document = new Document(PageSize.LETTER, 50, 50, 70, 70);
    else document = new Document(PageSize.A4, 50, 50, 70, 70);

    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));

    blogsBlogGuideCreatePDFUpdate.headerFooter(name, document);

    writer.setPageEvent(blogsBlogGuideCreatePDFUpdate);
  
    document.open();

    Font[] fonts = new Font[4];
    fonts[0] = new Font(Font.TIMES_ROMAN, 30, Font.NORMAL);
    fonts[1] = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
    fonts[2] = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
    fonts[3] = new Font(Font.TIMES_ROMAN, 14, Font.NORMAL);

    Paragraph p = new Paragraph("\n\n\n\n" + name, fonts[0]);
    document.add(p);

    if(version.equals("A"))
    {
      p = new Paragraph("\n\nAbridged Version");
      document.add(p);
    }

    p = new Paragraph("\n\n\nAs at " + generalUtils.yymmddExpandGivenSQLFormat(true, generalUtils.todaySQLFormat(localDefnsDir, defnsDir)), fonts[1]);
    document.add(p);

    document.newPage();

    createContents(conInfo, infoStmt, infoRs, document, name, fonts[3]);

    createPages(conInfo, infoStmt, infoStmt2, infoStmt3, infoRs, infoRs2, infoRs3, document, name, version, imageLibDir, fonts);
    
    if(conInfo != null) conInfo.close();

    document.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createContents(Connection infoCon, Statement infoStmt, ResultSet infoRs, Document document, String name, Font font) throws Exception
  {
    try
    {
      infoStmt = infoCon.createStatement();

      infoRs = infoStmt.executeQuery("SELECT Section FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "'");

      String section, orderList, entriesList = "";
      int numEntries = 0;

      while(infoRs.next())
      {
        entriesList += (infoRs.getString(1) + "\001");
        ++numEntries;
      }

      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();

      orderList = generalUtils.sortDecimalDot(entriesList, numEntries);

      String title, s;
      int len, z;
      Paragraph p = null;
      Chunk c;

      for(int x=0;x<numEntries;++x)
      {
        infoStmt = infoCon.createStatement();

        section = getSectionByPosition(orderList, x, entriesList);

        infoRs = infoStmt.executeQuery("SELECT Title FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "' AND Section = '" + section + "'");

        if(infoRs.next()) // just-in-case
        {
          title = infoRs.getString(1);

          if(title == null) title = "";

          s = "";
          len = section.length();
          for(z=0;z<len;++z)
          {
            if(section.charAt(z) == '.')
              s += "          ";
          }

          c = new Chunk(s + section + "      " + title).setLocalGoto(section);
          p = new Paragraph();
          p.setFont(font);
          p.add(c);

          document.add(p);
        }

        if(infoRs   != null) infoRs.close();
        if(infoStmt != null) infoStmt.close();
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createPages(Connection infoCon, Statement infoStmt, Statement infoStmt2, Statement infoStmt3, ResultSet infoRs, ResultSet infoRs2, ResultSet infoRs3, Document document, String name, String version, String imageLibDir, Font[] fonts)
                           throws Exception
  {
    try
    {
      String where = "";
      if(version.equals("A"))
        where = " AND Abridged = 'Y'";

      infoStmt = infoCon.createStatement();

      infoRs = infoStmt.executeQuery("SELECT Section FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "'" + where);

      String section, orderList, entriesList = "";
      int numEntries = 0;
      Paragraph p = null;

      while(infoRs.next())
      {
        entriesList += (infoRs.getString(1) + "\001");
        ++numEntries;
      }

      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();

      orderList = generalUtils.sortDecimalDot(entriesList, numEntries);

      String title, services, service;
      int y, len;
      boolean first;

      for(int x=0;x<numEntries;++x)
      {
        infoStmt = infoCon.createStatement();

        section = getSectionByPosition(orderList, x, entriesList);

        if(section.indexOf(".") == -1)
          document.newPage();

        infoRs = infoStmt.executeQuery("SELECT Title, Services FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "' AND Section = '" + section + "'");

        if(infoRs.next()) // just-in-case
        {
          title    = infoRs.getString(1);
          services = infoRs.getString(2);

          if(title    == null) title = "";
          if(services == null) services = "";

          if(! anyBlogsForAService(infoCon, infoStmt2, infoRs2, services))
          {
            p = new Paragraph();
            p.setFont(fonts[2]);
            p.add(new Chunk("\n\n" + section + "    " + title + "\n\n").setLocalDestination(section));
            document.add(p);
            addPara(document, p, "\n\n<blank>\n\n", fonts[1]);
          }
          else
          {
            first = true;
            len = services.length();
            y = 0;
            while(y < len)
            {
              service = "";
              while(y < len && services.charAt(y) != ',')
                service += services.charAt(y++);

              if(first)
              {
                p = new Paragraph();
                p.setFont(fonts[2]);
                p.add(new Chunk("\n\n" + section + "    " + title + "\n\n").setLocalDestination(section));
                document.add(p);
                first = false;
              }

              createSection(infoCon, infoStmt2, infoStmt3, infoRs2, infoRs3, document, name, service, imageLibDir, fonts);

              while(y < len && services.charAt(y) == ',')
                ++y;
            }
          }
        }

        if(infoRs   != null) infoRs.close();
        if(infoStmt != null) infoStmt.close();
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void createSection(Connection infoCon, Statement stmtInfo, Statement infoStmt2, ResultSet rsInfo, ResultSet infoRs2, Document document, String name, String service, String imageLibDir, Font[] fonts) throws Exception
  {
    try
    {
      stmtInfo = infoCon.createStatement();

      rsInfo = stmtInfo.executeQuery("SELECT Title, Image, Text, Type from blogs WHERE ServiceCode = '" + service + "' ORDER BY Date");

      String title, image, text, type;
      Paragraph p = null;

      while(rsInfo.next())
      {
        title = rsInfo.getString(1);
        image = rsInfo.getString(2);
        text  = rsInfo.getString(3);
        type  = rsInfo.getString(4);

        if(title == null) title = "";
        if(image == null) image = "";
        if(text  == null) text = "";
        if(type  == null) type = "";

        addPara(document, p, title + "\n\n", fonts[1]);

        if(image.length() > 0 && generalUtils.fileExists(imageLibDir + image))
          addImage(document, p, imageLibDir + image, type);

        outputText(infoCon, infoStmt2, infoRs2, document, name, text, imageLibDir);
      }

      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSectionByPosition(String orderList, int entryWithinOrderList, String entriesList) throws Exception
  {
    int x, y = 0;

    for(x=0;x<entryWithinOrderList;++x)
    {
      while(orderList.charAt(y) != '\001')
        ++y;
      ++y;
    }

    String entryWithinEntriesList = "";
    while(orderList.charAt(y) != '\001')
      entryWithinEntriesList += orderList.charAt(y++);

    int offset = generalUtils.strToInt(entryWithinEntriesList);

    y = 0;
    for(x=0;x<offset;++x)
    {
      while(entriesList.charAt(y) != '\001')
        ++y;
      ++y;
    }

    String thisEntry = "";
    while(entriesList.charAt(y) != '\001')
      thisEntry += entriesList.charAt(y++);

    return thisEntry;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean anyBlogsForAService(Connection conInfo, Statement stmtInfo, ResultSet rsInfo, String services) throws Exception
  {
    int rowCount = 0;

    try
    {
      int len = services.length();
      int y = 0;
      String service;
      while(y < len)
      {
        service = "";
        while(y < len && services.charAt(y) != ',')
          service += services.charAt(y++);
        ++y;

        stmtInfo = conInfo.createStatement();

        rsInfo = stmtInfo.executeQuery("SELECT COUNT(*) AS rowcount from blogs WHERE ServiceCode = '" + service + "' AND Published='Y'");

        if(rsInfo.next())
          rowCount += rsInfo.getInt("rowcount");

        if(rsInfo   != null) rsInfo.close();
        if(stmtInfo != null) stmtInfo.close();
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rsInfo   != null) rsInfo.close();
      if(stmtInfo != null) stmtInfo.close();
    }

    if(rowCount == 0)
      return false;
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void convertLinks(Connection infoCon, Statement infoStmt, ResultSet infoRs, Paragraph p, String name, String text, String imageLibraryDir, Font font) throws Exception
  {
    String s="", link, linkTo, linkText;
    boolean inQuotes;
    Chunk c;

    int len = text.length();
    int x=0, y, linkLen;
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
                 && ! link.startsWith("[[silink ") && ! link.startsWith("[[blink ") && ! link.startsWith("[[bzlink ")
                 && ! link.startsWith("[[eilink ") && ! link.startsWith("[[zclink ") && ! link.startsWith("[[z1link ") && ! link.startsWith("[[z2link "))
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
                  case 's' : // service
                             if(s.length() > 0)
                             {
                               c = new Chunk(s);
                               c.setFont(font);
                               p.add(c);
                             }

                             if(linkTo.length() > 0)
                             {
                               String section = getSectionGivenService(infoCon, infoStmt, infoRs, linkTo, name);
                               if(section.length() > 0)
                               {
                                 c = new Chunk(linkText).setLocalGoto(section);
                                 c.setFont(font);
                                 c.setBackground(new java.awt.Color(255,255,0));
                                 p.add(c);
                               }
                             }

                             s = "";

                             break;
                  case 'p' : // picture
                             if(s.length() > 0)
                             {
                               c = new Chunk(s);
                               c.setFont(font);
                               p.add(c);
                             }

                             if(linkTo.length() > 0 && generalUtils.fileExists(imageLibraryDir + linkTo))
                             {
                               Image i = Image.getInstance(imageLibraryDir + linkTo);
                               if(i.getWidth() > 500)
                                 i.scaleToFit(500,1000);

                               p.add(i);
                             }

                             s = "";

                             break;
                  default  : // catch all the others
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

    if(s.length() > 0)
    {
      c = new Chunk(s);
      c.setFont(font);
      p.add(c);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addPara(Document document, Paragraph p, String text, Font font)
  {
    text = generalUtils.stripDuplicateSpaces(text);

    String s="";
    int len=text.length();
    for(int x=0;x<len;++x)
    {
      if(text.charAt(x) == 13)
        ;
      else
      if(text.charAt(x) == 10)
        s += "\n";
      else
      if(text.charAt(x) == 32)
        s += " ";
      else s += text.charAt(x);
    }

    try
    {
      p = new Paragraph();
      p.setSpacingBefore(0);
      p.setSpacingAfter(0);
      p.setFont(font);
      p.add(s);
      p.setAlignment(Element.ALIGN_LEFT);
      document.add(p);
    }
    catch(Exception e) { }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addImage(Document document, Paragraph p, String image, String location)
  {
    try
    {
      Image i = Image.getInstance(image);
      if(i.getWidth() > 500)
        i.scaleToFit(500,1000);

      p = new Paragraph();
      p.add(i);
      if(location.equals("R"))
        p.setAlignment(Element.ALIGN_RIGHT);
      else p.setAlignment(Element.ALIGN_LEFT);
      document.add(p);
    }
    catch(Exception e) { }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputText(Connection infoCon, Statement infoStmt, ResultSet infoRs, Document document, String name, String text, String imageLibraryDir) throws Exception
  {
    try
    {
      parseBlogEntry(infoCon, infoStmt, infoRs, document, text, name, imageLibraryDir);
    }
    catch(Exception e) { System.out.println("optext " + e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getSectionGivenService(Connection infoCon, Statement infoStmt, ResultSet infoRs, String service, String name) throws Exception
  {
    String section = "", thisService, services;
    int x, len;
    boolean found = false;

    try
    {
      infoStmt = infoCon.createStatement();

      infoRs = infoStmt.executeQuery("SELECT Section, Services FROM blogguide WHERE Name = '" + generalUtils.sanitiseForSQL(name) + "'");

      while(! found && infoRs.next())
      {
        section  = infoRs.getString(1);
        services = infoRs.getString(2);

        len = services.length();
        x = 0;
        while(! found && x < len)
        {
          thisService = "";
          while(x < len && services.charAt(x) != ' ' && services.charAt(x) != ',')
            thisService += services.charAt(x++);
          if(thisService.equals(service))
            found = true;
          else
          {
            while(x < len && (services.charAt(x) == ' ' || services.charAt(x) == ','))
              ++x;
          }
        }
      }

      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(infoRs   != null) infoRs.close();
      if(infoStmt != null) infoStmt.close();
    }

    if(! found)
      section = "";

    return section;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // parse a blog entry and construct one Paragraph
  private void parseBlogEntry(Connection infoCon, Statement infoStmt, ResultSet infoRs, Document document, String text, String name, String imageLibraryDir) throws Exception
  {
    char ch;
    int x = 0, y, len = text.length();
    String text2 = "", tag;
    int style = Font.NORMAL;
    int[] family  = new int[1];
    int[] size    = new int[1];
    int[] numCols = new int[1];
    int[] border  = new int[1];
    String[] colour     = new String[1];
    String[] fontStyle  = new String[1];
    String[] fontWeight = new String[1];
    Paragraph p = new Paragraph();
    boolean inTable = false, firstRowInTable = false, lastCharWasSpace = false;
    float[] widths;
    PdfPTable table = null;

    Font[] fonts = new Font[10];
    int[] fontSize = new int[10];  fontSize[0] = 10;
    int[] fontPtr  = new int[10];  fontPtr[0]  = 0;
    Font font, f = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
    fonts[fontPtr[0]++] = f;
    
    while(x < len)
    {
      ch = text.charAt(x);

      if(ch == '<') // opening tag
      {
        if(text2.length() > 0) // text so far
        {
          if(fontPtr[0] > 0) // just-in-case
            font = fonts[fontPtr[0] - 1];
          else font = fonts[0];
          convertLinks(infoCon, infoStmt, infoRs, p, name, text2, imageLibraryDir, font);
          text2 = "";
        }

        tag = "";
        while(x < len && text.charAt(x) != '>')
          tag += text.charAt(x++);
        tag += ">";
        ++x;

        // process the tag

        if(tag.equalsIgnoreCase("<br>") || tag.equalsIgnoreCase("<br/>") || tag.equalsIgnoreCase("<br />"))
        {
          text2 += '\n';
        }
        else
        if((tag.toLowerCase()).startsWith("<hr"))
        {
          text2 += "_____________________________________________________________________\n";
        }
        else
        if(tag.equalsIgnoreCase("<i>"))
        {
          if(style == Font.BOLD)
            style = Font.BOLDITALIC;
          else style = Font.ITALIC;

          modifyFont(family[0], size[0], style, fonts, fontPtr[0]);
        }
        else
        if(tag.equalsIgnoreCase("</i>"))
        {
          if(style == Font.BOLDITALIC)
            style = Font.BOLD;
          else style = Font.NORMAL;

          modifyFont(family[0], size[0], style, fonts, fontPtr[0]);
        }
        else
        if(tag.equalsIgnoreCase("<b>") || tag.equalsIgnoreCase("<strong>"))
        {
          if(style == Font.ITALIC)
            style = Font.BOLDITALIC;
          else style = Font.BOLD;

          modifyFont(family[0], size[0], style, fonts, fontPtr[0]);
        }
        else
        if(tag.equalsIgnoreCase("</b>") || tag.equalsIgnoreCase("</strong>"))
        {
          if(style == Font.BOLDITALIC)
            style = Font.ITALIC;
          else style = Font.NORMAL;

          modifyFont(family[0], size[0], style, fonts, fontPtr[0]);
        }
        else
        if(tag.equals("<p>"))
        {
          if(! inTable)
            text2 += "\n";
        }
        else
        if(   tag.equalsIgnoreCase("<ul>") || tag.equalsIgnoreCase("</p>") || tag.equalsIgnoreCase("</li>") || tag.equalsIgnoreCase("</ol>") || tag.equalsIgnoreCase("<tbody>") || tag.equalsIgnoreCase("</tbody>") || tag.equalsIgnoreCase("</tr>")
           || tag.equalsIgnoreCase("</td>")|| tag.equalsIgnoreCase("<url>")|| tag.equalsIgnoreCase("</url>") || (tag.toLowerCase()).startsWith("<a ")  || (tag.toLowerCase()).startsWith("<col ") )
        {
        }
        else
        if(tag.equalsIgnoreCase("<li>"))
        {
          text2 += "\n* ";
        }
        else
        if(tag.equalsIgnoreCase("</ul>"))
        {
          text2 += "\n";
        }
        else
        if((tag.toLowerCase()).startsWith("<table"))
        {
          inTable = firstRowInTable = true;
          parseTable(tag, border);
        }
        else
        if(tag.equalsIgnoreCase("</table>"))
        {
          inTable = false;
          p.add(table);
        }
        else
        if((tag.toLowerCase()).startsWith("<tr"))
        {
          if(firstRowInTable)
          {
            determineTableStructure(text, x, numCols);

            widths = new float[numCols[0]];
            for(int i=0;i<numCols[0];++i)
              widths[i] = 1f;
            table = new PdfPTable(widths);
            table.setWidthPercentage(100);
            table.setHeaderRows(0);
            
            firstRowInTable = false;
          }

          if(table != null)
            x = processTableRow(text, x, table, border[0], f);
        }
        else
        if((tag.toLowerCase()).startsWith("<br"))
        {
          text2 += '\n';
        }
        else
        if((   tag.toLowerCase()).startsWith("<font") || (tag.toLowerCase()).startsWith("<br") || (tag.toLowerCase()).startsWith("<ul ") || (tag.toLowerCase()).startsWith("<p ") || (tag.toLowerCase()).startsWith("<ol ")
            || (tag.toLowerCase()).startsWith("<page ") || (tag.toLowerCase()).startsWith("<span ")) // <pre
        {
          parseFontTag(tag, family, size, colour);

          fonts = addFont(family[0], size[0], style, fonts, fontPtr, fontSize);

          parseSpanTag(tag, fontStyle, fontWeight);

          if(fontStyle[0].equals("italic"))
          {
            if(style == Font.BOLD)
              style = Font.BOLDITALIC;
            else style = Font.ITALIC;
          }

          if(fontWeight[0].equals("bold"))
          {
            if(style == Font.ITALIC)
              style = Font.BOLDITALIC;
            else style = Font.BOLD;
          }

          modifyFont(family[0], size[0], style, fonts, fontPtr[0]);
        }
        else
        if(tag.equalsIgnoreCase("</font>") || tag.equalsIgnoreCase("</page>") || tag.equalsIgnoreCase("</span>"))
        {
          style = Font.NORMAL;
          if(fontPtr[0] > 0) // just-in-case
            --fontPtr[0];
        }
        else text2 += tag;
      }
      else // not an HMTL opening tag
      if(ch == '&')
      {
        y = x;
        tag = "";
        while(y < len && text.charAt(y) != ';')
          tag += text.charAt(y++);

        if(tag.equals("&nbsp"))
        {
          text2 += ' ';
          x = y;
        }
        else
        if(tag.equals("&amp"))
        {
          text2 += '&';
          x = y;
        }
        else text2 += '&';

        ++x;
      }
      else // nor a '&', plain text outside of HTML
      {
        if(text.charAt(x) != '\n' && text.charAt(x) != '\r')
        {
          if(text.charAt(x) == ' ')
          {
            if(lastCharWasSpace)
              ;
            else
            {
              text2 += " ";
              lastCharWasSpace = true;
            }
          }
          else
          {
            text2 += text.charAt(x);
            lastCharWasSpace = false;
          }
        }

        ++x;
      }
    }

    if(text2.length() > 0)
    {
      if(fontPtr[0] > 0) // just-in-case
        font = fonts[fontPtr[0] - 1];
      else font = fonts[0];

      convertLinks(infoCon, infoStmt, infoRs, p, name, text2, imageLibraryDir, font);
    }

    document.add(p);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private Font[] addFont(int family, int size, int style, Font[] fonts, int[] fontPtr, int[] fontSize)
  {
    if(fontPtr[0] == fontSize[0])
    {
      int x;
      Font[] tmp = new Font[fontPtr[0]];
      for(x=0;x<fontPtr[0];++x)
        tmp[x] = fonts[x];
      fonts = new Font[fontPtr[0] + 10];
      for(x=0;x<fontPtr[0];++x)
        fonts[x] = tmp[x];
      fontSize[0] += 10;
    }

    fonts[fontPtr[0]++] = new Font(family, size, style);

    return fonts;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void modifyFont(int family, int size, int style, Font[] fonts, int fontPtr)
  {
    if(fontPtr > 0)
      --fontPtr;

    fonts[fontPtr] = new Font(family, size, style);
  }


  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void determineTableStructure(String text, int x, int[] numCols) throws Exception
  {
    String tag;
    char ch;
    int len = text.length();

    numCols[0] = 0;

    while(x < len)
    {
      ch = text.charAt(x);

      if(ch == '<') // opening tag
      {
        tag = "";
        while(x < len && text.charAt(x) != '>')
          tag += text.charAt(x++);
        tag += ">";
        ++x;

        // process the tag

        if(tag.equalsIgnoreCase("</tr>") || tag.equalsIgnoreCase("</table>"))
          return;

        if((tag.toLowerCase()).startsWith("<td"))
          ++numCols[0];
      }

      ++x;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void parseTable(String tag, int[] border) throws Exception
  {
    String s = "0";
    int i, len = tag.length();

    if((i = tag.indexOf("border=")) != -1)
    {
      i += 7;
      if(tag.charAt(i) == '"' || tag.charAt(i) == '\'')
        ++i;
      s = "";
      while(i < len && (tag.charAt(i) >= '0' && tag.charAt(i) <= '9'))  // just-in-case
        s += tag.charAt(i++);
      if(! generalUtils.isInteger(s)) // just-in-case
        s = "0";
    }
    
    border[0] = generalUtils.strToInt(s);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int processTableRow(String text, int x, PdfPTable table, int border, Font font) throws Exception
  {
    String tag, thisTDText = "";
    char ch;
    int len = text.length();
    boolean inTD = false;
    int[] valign  = new int[1];
    int[] align   = new int[1];
    int[] colSpan = new int[1];
    String[] colour = new String[1]; colour[0] = "black";
    Paragraph p;

    ++x;

    while(x < len)
    {
      ch = text.charAt(x);

      if(ch == '<') // opening tag
      {
        tag = "";
        while(x < len && text.charAt(x) != '>')
          tag += text.charAt(x++);
        tag += ">";
        ++x;

        if(tag.equalsIgnoreCase("</tr>") || tag.equalsIgnoreCase("</table>"))
        {
          if(inTD) // just-in-case
          {
            p = new Paragraph();
            p.setFont(font);
            p.add(new Chunk(thisTDText));
            PdfPCell cell = new PdfPCell(p);
            cell.setColspan(colSpan[0]);
            cell.setVerticalAlignment(valign[0]);
            cell.setHorizontalAlignment(align[0]);
            if(border != 0)
              cell.setBorderColor(new java.awt.Color(0,0,0));
            else cell.setBorderColor(new java.awt.Color(255,255,255));

            cell.setBackgroundColor(java.awt.Color.decode("0x" + convertColourToHex(colour[0])));

            table.addCell(cell);
          }

          return x;
        }

        if((tag.toLowerCase()).startsWith("<td"))
        {
          inTD = true;
          thisTDText = "";
          parseTDTag(tag, valign, align, colSpan, colour);
        }
        else
        if((tag.toLowerCase()).startsWith("</td"))
        {
          inTD = false;

          p = new Paragraph();
          p.setFont(font);
          p.add(new Chunk(thisTDText));
          PdfPCell cell = new PdfPCell(p);
          cell.setColspan(colSpan[0]);
          cell.setVerticalAlignment(valign[0]);
          cell.setHorizontalAlignment(align[0]);
          if(border != 0)
            cell.setBorderColor(new java.awt.Color(0,0,0));
          else cell.setBorderColor(new java.awt.Color(255,255,255));

          
          cell.setBackgroundColor(java.awt.Color.decode("0x" + convertColourToHex(colour[0])));

          table.addCell(cell);
        }
        // else ignore the tag
      }
      else // outside html
      {
        if(inTD)
        {
          thisTDText += ch;
        }

        ++x;
      }
    }

    return x;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // tag: <font style="font-family: Verdana,Arial,Helvetica,sans-serif;" size="2", color=red>
  private void parseSpanTag(String tag, String[] fontStyle, String[] fontWeight)
  {
    tag = tag.toLowerCase();

    if(tag.indexOf("italic") != -1)
      fontStyle[0] = "italic";
    else fontStyle[0] = "";

    if(tag.indexOf("bold") != -1)
      fontWeight[0] = "bold";
    else fontWeight[0] = "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // tag: <font style="font-family: Verdana,Arial,Helvetica,sans-serif;" size="2", color=red>
  private void parseFontTag(String tag, int[] family, int[] size, String[] colour)
  {
    int len = tag.length();
    tag = tag.toLowerCase();

    if(tag.indexOf("courier") != -1)
      family[0] = Font.COURIER;
    else family[0] = Font.TIMES_ROMAN; // force all to Times

    int i;
    String s = "12";

    if((i = tag.indexOf("size=")) != -1)
    {
      i += 5;
      if(tag.charAt(i) == '"' || tag.charAt(i) == '\'')
       ++i;
      s = "";
      while(i < len && (tag.charAt(i) >= '0' && tag.charAt(i) <= '9'))  // just-in-case
        s += tag.charAt(i++);
      if(! generalUtils.isInteger(s)) // just-in-case
        s = "12";
      else
      {
        if(s.equals("1"))
          s = "8";
        else
        if(s.equals("2"))
          s = "12";
        else
        if(s.equals("3"))
          s = "14";
        else
        if(s.equals("4"))
          s = "17";
        else
        if(s.equals("5"))
          s = "20";
        else
        if(s.equals("6"))
          s = "25";
        else
        if(s.equals("7"))
          s = "30";
      }
    }

    int sizeI = generalUtils.strToInt(s);
    if(sizeI == 0)
      size[0] = 12;
    else size[0] = sizeI;

    colour[0] = "black";

    if((i = tag.indexOf("color=")) != -1)
    {
      i += 6;
      if(tag.charAt(i) == '"' || tag.charAt(i) == '\'')
        ++i;
      colour[0] = "";
      while(i < len && ((tag.charAt(i) >= '0' && tag.charAt(i) <= '9') || (tag.charAt(i) >= 'a' && tag.charAt(i) <= 'z') || (tag.charAt(i) >= 'A' && tag.charAt(i) <= 'Z') || tag.charAt(i) == '#'))  // just-in-case
        colour[0] += tag.charAt(i++);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void parseTDTag(String tag, int[] valign, int[] align, int[] colSpan, String[] colour)
  {
    int len = tag.length();
    tag = tag.toLowerCase();

    int i;

    valign[0] = Element.ALIGN_BOTTOM;

    if((i = tag.indexOf("valign=")) != -1)
    {
      i += 7;
      if(tag.charAt(i) == '"' || tag.charAt(i) == '\'')
        ++i;
      if(i < len && tag.charAt(i) == 't')  // just-in-case
        valign[0] = Element.ALIGN_TOP;
      else
      if(i < len && tag.charAt(i) == 'b')  // just-in-case
        valign[0] = Element.ALIGN_BOTTOM;
      else
      if(i < len && tag.charAt(i) == 'c')  // just-in-case
        valign[0] = Element.ALIGN_MIDDLE;
    }

    align[0] = Element.ALIGN_LEFT;

    if((i = tag.indexOf("align=")) != -1 && tag.charAt(i-1) != 'v')
    {
      i += 6;
      if(tag.charAt(i) == '"' || tag.charAt(i) == '\'')
        ++i;
      if(i < len && tag.charAt(i) == 'r')  // just-in-case
        align[0] = Element.ALIGN_RIGHT;
    }

    colour[0] = "FFFFFF";

    if((i = tag.indexOf("color=")) != -1)
    {
      i += 6;
      if(tag.charAt(i) == '"' || tag.charAt(i) == '\'')
        ++i;

      colour[0] = "";
      while(i < len && ((tag.charAt(i) >= '0' && tag.charAt(i) <= '9') || (tag.charAt(i) >= 'a' && tag.charAt(i) <= 'z') || (tag.charAt(i) >= 'A' && tag.charAt(i) <= 'Z') || tag.charAt(i) == '#'))  // just-in-case
        colour[0] += tag.charAt(i++);
     colour[0] = colour[0].toLowerCase();
    }
    else
    if((i = tag.indexOf("background:")) != -1)
    {
      i += 11;
      if(tag.charAt(i) == ' ')
        ++i;

      colour[0] = "";
      while(i < len && ((tag.charAt(i) >= '0' && tag.charAt(i) <= '9') || (tag.charAt(i) >= 'a' && tag.charAt(i) <= 'z') || (tag.charAt(i) >= 'A' && tag.charAt(i) <= 'Z') || tag.charAt(i) == '#'))  // just-in-case
        colour[0] += tag.charAt(i++);
     colour[0] = colour[0].toLowerCase();
    }

    String s = "1";

    if((i = tag.indexOf("colspan=")) != -1)
    {
      i += 8;
      if(tag.charAt(i) == '"' || tag.charAt(i) == '\'')
        ++i;

      s = "";
      while(i < len && tag.charAt(i) >= '0' && tag.charAt(i) <= '9')  // just-in-case
        s += tag.charAt(i++);
    }

    if(! generalUtils.isInteger(s)) // just-in-case
      s = "1";
    
    int colsI = generalUtils.strToInt(s);
    if(colsI == 0)
      colSpan[0] = 1;
    else colSpan[0] = colsI;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String convertColourToHex(String colour)
  {
    if(colour.equalsIgnoreCase("AliceBlue")) return "F0F8FF";
    if(colour.equalsIgnoreCase("AntiqueWhite")) return "FAEBD7";
    if(colour.equalsIgnoreCase("Aqua")) return "00FFFF";
    if(colour.equalsIgnoreCase("Aquamarine")) return "7FFFD4";
    if(colour.equalsIgnoreCase("Azure")) return "F0FFFF";
    if(colour.equalsIgnoreCase("Beige")) return "F5F5DC";
    if(colour.equalsIgnoreCase("Bisque")) return "FFE4C4";
    if(colour.equalsIgnoreCase("Black")) return "000000";
    if(colour.equalsIgnoreCase("BlanchedAlmond")) return "FFEBCD";
    if(colour.equalsIgnoreCase("Blue")) return "0000FF";
    if(colour.equalsIgnoreCase("BlueViolet")) return "8A2BE2";
    if(colour.equalsIgnoreCase("Brown")) return "A52A2A";
    if(colour.equalsIgnoreCase("BurlyWood")) return "DEB887";
    if(colour.equalsIgnoreCase("CadetBlue")) return "5F9EA0";
    if(colour.equalsIgnoreCase("Chartreuse")) return "7FFF00";
    if(colour.equalsIgnoreCase("Chocolate")) return "D2691E";
    if(colour.equalsIgnoreCase("Coral")) return "FF7F50";
    if(colour.equalsIgnoreCase("CornflowerBlue")) return "6495ED";
    if(colour.equalsIgnoreCase("Cornsilk")) return "FFF8DC";
    if(colour.equalsIgnoreCase("Crimson")) return "DC143C";
    if(colour.equalsIgnoreCase("Cyan")) return "00FFFF";
    if(colour.equalsIgnoreCase("DarkBlue")) return "00008B";
    if(colour.equalsIgnoreCase("DarkCyan")) return "008B8B";
    if(colour.equalsIgnoreCase("DarkGoldenRod")) return "B8860B";
    if(colour.equalsIgnoreCase("DarkGray")) return "A9A9A9";
    if(colour.equalsIgnoreCase("DarkGreen")) return "006400";
    if(colour.equalsIgnoreCase("DarkKhaki")) return "BDB76B";
    if(colour.equalsIgnoreCase("DarkMagenta")) return "8B008B";
    if(colour.equalsIgnoreCase("DarkOliveGreen")) return "556B2F";
    if(colour.equalsIgnoreCase("Darkorange")) return "FF8C00";
    if(colour.equalsIgnoreCase("DarkOrchid")) return "9932CC";
    if(colour.equalsIgnoreCase("DarkRed")) return "8B0000";
    if(colour.equalsIgnoreCase("DarkSalmon")) return "E9967A";
    if(colour.equalsIgnoreCase("DarkSeaGreen")) return "8FBC8F";
    if(colour.equalsIgnoreCase("DarkSlateBlue")) return "483D8B";
    if(colour.equalsIgnoreCase("DarkSlateGray")) return "2F4F4F";
    if(colour.equalsIgnoreCase("DarkTurquoise")) return "00CED1";
    if(colour.equalsIgnoreCase("DarkViolet")) return "9400D3";
    if(colour.equalsIgnoreCase("DeepPink")) return "FF1493";
    if(colour.equalsIgnoreCase("DeepSkyBlue")) return "00BFFF";
    if(colour.equalsIgnoreCase("DimGray")) return "696969";
    if(colour.equalsIgnoreCase("DodgerBlue")) return "1E90FF";
    if(colour.equalsIgnoreCase("FireBrick")) return "B22222";
    if(colour.equalsIgnoreCase("FloralWhite")) return "FFFAF0";
    if(colour.equalsIgnoreCase("ForestGreen")) return "228B22";
    if(colour.equalsIgnoreCase("Fuchsia")) return "FF00FF";
    if(colour.equalsIgnoreCase("Gainsboro")) return "DCDCDC";
    if(colour.equalsIgnoreCase("GhostWhite")) return "F8F8FF";
    if(colour.equalsIgnoreCase("Gold")) return "FFD700";
    if(colour.equalsIgnoreCase("GoldenRod")) return "DAA520";
    if(colour.equalsIgnoreCase("Gray")) return "808080";
    if(colour.equalsIgnoreCase("Green")) return "008000";
    if(colour.equalsIgnoreCase("GreenYellow")) return "ADFF2F";
    if(colour.equalsIgnoreCase("HoneyDew")) return "F0FFF0";
    if(colour.equalsIgnoreCase("HotPink")) return "FF69B4";
    if(colour.equalsIgnoreCase("IndianRed")) return "CD5C5C";
    if(colour.equalsIgnoreCase("Indigo")) return "4B0082";
    if(colour.equalsIgnoreCase("Ivory")) return "FFFFF0";
    if(colour.equalsIgnoreCase("Khaki")) return "F0E68C";
    if(colour.equalsIgnoreCase("Lavender")) return "E6E6FA";
    if(colour.equalsIgnoreCase("LavenderBlush")) return "FFF0F5";
    if(colour.equalsIgnoreCase("LawnGreen")) return "7CFC00";
    if(colour.equalsIgnoreCase("LemonChiffon")) return "FFFACD";
    if(colour.equalsIgnoreCase("LightBlue")) return "ADD8E6";
    if(colour.equalsIgnoreCase("LightCoral")) return "F08080";
    if(colour.equalsIgnoreCase("LightCyan")) return "E0FFFF";
    if(colour.equalsIgnoreCase("LightGoldenRodYellow")) return "FAFAD2";
    if(colour.equalsIgnoreCase("LightGrey")) return "D3D3D3";
    if(colour.equalsIgnoreCase("LightGreen")) return "90EE90";
    if(colour.equalsIgnoreCase("LightPink")) return "FFB6C1";
    if(colour.equalsIgnoreCase("LightSalmon")) return "FFA07A";
    if(colour.equalsIgnoreCase("LightSeaGreen")) return "20B2AA";
    if(colour.equalsIgnoreCase("LightSkyBlue")) return "87CEFA";
    if(colour.equalsIgnoreCase("LightSlateGray")) return "778899";
    if(colour.equalsIgnoreCase("LightSteelBlue")) return "B0C4DE";
    if(colour.equalsIgnoreCase("LightYellow")) return "FFFFE0";
    if(colour.equalsIgnoreCase("Lime")) return "00FF00";
    if(colour.equalsIgnoreCase("LimeGreen")) return "32CD32";
    if(colour.equalsIgnoreCase("Linen")) return "FAF0E6";
    if(colour.equalsIgnoreCase("Magenta")) return "FF00FF";
    if(colour.equalsIgnoreCase("Maroon")) return "800000";
    if(colour.equalsIgnoreCase("MediumAquaMarine")) return "66CDAA";
    if(colour.equalsIgnoreCase("MediumBlue")) return "0000CD";
    if(colour.equalsIgnoreCase("MediumOrchid")) return "BA55D3";
    if(colour.equalsIgnoreCase("MediumPurple")) return "9370D8";
    if(colour.equalsIgnoreCase("MediumSeaGreen")) return "3CB371";
    if(colour.equalsIgnoreCase("MediumSlateBlue")) return "7B68EE";
    if(colour.equalsIgnoreCase("MediumSpringGreen")) return "00FA9A";
    if(colour.equalsIgnoreCase("MediumTurquoise")) return "48D1CC";
    if(colour.equalsIgnoreCase("MediumVioletRed")) return "C71585";
    if(colour.equalsIgnoreCase("MidnightBlue")) return "191970";
    if(colour.equalsIgnoreCase("MintCream")) return "F5FFFA";
    if(colour.equalsIgnoreCase("MistyRose")) return "FFE4E1";
    if(colour.equalsIgnoreCase("Moccasin")) return "FFE4B5";
    if(colour.equalsIgnoreCase("NavajoWhite")) return "FFDEAD";
    if(colour.equalsIgnoreCase("Navy")) return "000080";
    if(colour.equalsIgnoreCase("OldLace")) return "FDF5E6";
    if(colour.equalsIgnoreCase("Olive")) return "808000";
    if(colour.equalsIgnoreCase("OliveDrab")) return "6B8E23";
    if(colour.equalsIgnoreCase("Orange")) return "FFA500";
    if(colour.equalsIgnoreCase("OrangeRed")) return "FF4500";
    if(colour.equalsIgnoreCase("Orchid")) return "DA70D6";
    if(colour.equalsIgnoreCase("PaleGoldenRod")) return "EEE8AA";
    if(colour.equalsIgnoreCase("PaleGreen")) return "98FB98";
    if(colour.equalsIgnoreCase("PaleTurquoise")) return "AFEEEE";
    if(colour.equalsIgnoreCase("PaleVioletRed")) return "D87093";
    if(colour.equalsIgnoreCase("PapayaWhip")) return "FFEFD5";
    if(colour.equalsIgnoreCase("PeachPuff")) return "FFDAB9";
    if(colour.equalsIgnoreCase("Peru")) return "CD853F";
    if(colour.equalsIgnoreCase("Pink")) return "FFC0CB";
    if(colour.equalsIgnoreCase("Plum")) return "DDA0DD";
    if(colour.equalsIgnoreCase("PowderBlue")) return "B0E0E6";
    if(colour.equalsIgnoreCase("Purple")) return "800080";
    if(colour.equalsIgnoreCase("Red")) return "FF0000";
    if(colour.equalsIgnoreCase("RosyBrown")) return "BC8F8F";
    if(colour.equalsIgnoreCase("RoyalBlue")) return "4169E1";
    if(colour.equalsIgnoreCase("SaddleBrown")) return "8B4513";
    if(colour.equalsIgnoreCase("Salmon")) return "FA8072";
    if(colour.equalsIgnoreCase("SandyBrown")) return "F4A460";
    if(colour.equalsIgnoreCase("SeaGreen")) return "2E8B57";
    if(colour.equalsIgnoreCase("SeaShell")) return "FFF5EE";
    if(colour.equalsIgnoreCase("Sienna")) return "A0522D";
    if(colour.equalsIgnoreCase("Silver")) return "C0C0C0";
    if(colour.equalsIgnoreCase("SkyBlue")) return "87CEEB";
    if(colour.equalsIgnoreCase("SlateBlue")) return "6A5ACD";
    if(colour.equalsIgnoreCase("SlateGray")) return "708090";
    if(colour.equalsIgnoreCase("Snow")) return "FFFAFA";
    if(colour.equalsIgnoreCase("SpringGreen")) return "00FF7F";
    if(colour.equalsIgnoreCase("SteelBlue")) return "4682B4";
    if(colour.equalsIgnoreCase("Tan")) return "D2B48C";
    if(colour.equalsIgnoreCase("Teal")) return "008080";
    if(colour.equalsIgnoreCase("Thistle")) return "D8BFD8";
    if(colour.equalsIgnoreCase("Tomato")) return "FF6347";
    if(colour.equalsIgnoreCase("Turquoise")) return "40E0D0";
    if(colour.equalsIgnoreCase("Violet")) return "EE82EE";
    if(colour.equalsIgnoreCase("Wheat")) return "F5DEB3";
    if(colour.equalsIgnoreCase("White")) return "FFFFFF";
    if(colour.equalsIgnoreCase("WhiteSmoke")) return "F5F5F5";
    if(colour.equalsIgnoreCase("Yellow")) return "FFFF00";
    if(colour.equalsIgnoreCase("YellowGreen")) return "9ACD32";

    return colour;
  }
}
