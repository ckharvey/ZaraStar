// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Print to network printer
// Module: AdminPrintControlNetwork.java
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

public class AdminPrintControlNetworkWave extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="";

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
      p1  = req.getParameter("p1"); // fileName
      p2  = req.getParameter("p2"); // numPages
      p3  = req.getParameter("p3"); // ptr
      p4  = req.getParameter("p4"); // returnServletAndParams
      p5  = req.getParameter("p5"); // returnMsg

      if(p4 == null) p4 = "";
      if(p5 == null) p5 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AdminPrintControlNetwork", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7400, bytesOut[0], 0, "ERR:" + p1 + " " + p3);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String returnServletAndParams, String returnMsg, int[] bytesOut)
                    throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreenW(out, req, 12, unm, sid, uty, men, den, dnm, bnm, "7400a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 7400, bytesOut[0], 0, "SID:" + p1 + " " + p3);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    pprToPS(unm, dnm, p1, p2, "");

    sendJob(unm, dnm, p1, p3);

    messagePage.msgScreenW(out, req, 21, unm, sid, uty, men, den, dnm, bnm, "7400", returnServletAndParams, returnMsg, "", imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 7400, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + " " + p3);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void sendJob(String unm, String dnm, String fileName, String ptr) throws Exception
  {
    Process p;
    BufferedReader reader;
    String currentLine;

    Runtime r = Runtime.getRuntime();

    String commandArray =  "/Zara/Support/Scripts/Print/print.sh /Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/" + fileName + ".ps " + ptr;
    p = r.exec(commandArray);

    p.waitFor();

    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    reader = new BufferedReader(isr);

    String res="";
    while((currentLine = reader.readLine()) != null)
      res += currentLine;

    reader.close();
    isr.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // only converts those pages req'd to be printed
  public void pprToPS(String unm, String dnm, String fileName, String numPages, String pagesToOutput) throws Exception
  {
    RandomAccessFile fh = generalUtils.fileOpen("/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/" + fileName);
    if(fh == null) // just-in-case
    {
      System.out.println("Failed to open: /Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/" + fileName);
      return;
    }

    RandomAccessFile fho = generalUtils.create("/Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/" + fileName + ".ps");
    if(fho == null) // just-in-case
    {
      System.out.println("Failed to create: /Zara/" + dnm + "/Users/" + unm + "/" + dnm + "/Reports/" + fileName + ".ps");
      return;
    }

    int x, len;
    String down1, across1, down2, across2, font, style, size, just, italic, text, str, width, height;
    char orientation='P';

    byte[] tmp = new byte[10000];
    while(getNextLine(fh, tmp, 10000))
    {
      len = generalUtils.lengthBytes(tmp, 0);
      switch(tmp[0])
      {
        case 'S' : x = 2;
                   width = "";
                   while(x < len && tmp[x] != ',')
                     width += (char)tmp[x++];
                   ++x;

                   height = "";
                   while(x < len && tmp[x] != ',')
                     height += (char)tmp[x++];
                   ++x;

                   orientation = 'P'; // default portrait
                   if(x < len)
                     orientation = (char)tmp[x];

                   outputHeader(fho, orientation, fileName, numPages);
                   break;
        case 'E' :
                   fho.writeBytes("showpage\n");
                   outputHeader(fho, orientation, fileName, numPages);
                   break;
        case 'D' : x=2;
                   down1 = "";
                   while(x < len && tmp[x] != ',')
                     down1 += (char)tmp[x++];
                   ++x;

                   across1 = "";
                   while(x < len && tmp[x] != ',')
                     across1 += (char)tmp[x++];
                   ++x;

                   font = "";
                   while(x < len && tmp[x] != ',')
                     font += (char)tmp[x++];
                   ++x;

                   style = "";
                   while(x < len && tmp[x] != ',')
                     style += (char)tmp[x++];
                   ++x;

                   italic = "";
                   while(x < len && tmp[x] != ',')
                     italic += (char)tmp[x++];
                   ++x;

                   size = "";
                   while(x < len && tmp[x] != ',')
                     size += (char)tmp[x++];
                   ++x;

                   just = "";
                   while(x < len && tmp[x] != ',')
                     just += (char)tmp[x++];
                   x += 2; // ,"

                   text = "";
                   while(x < len && tmp[x] != '"')
                     text += (char)tmp[x++];

                   if(style.equals("N")) style = ""; // not bold

                   if(font.equals("R")) font = "T"; // times
                   else
                   if(font.equals("S")) font = "H"; // helvetica
                   else
                   if(font.equals("H")) font = "C"; // courier
                   else
                   if(font.equals("Arial")) font = "H"; // helvetica
                   else
                   if(font.equals("TimesRoman")) font = "T"; // times
                   else font = "H";

                   size = psSizes(generalUtils.strToInt(size));

                   // esc any parentheses in text
                   str="";
                   x=0;
                   len = text.length();
                   for(x=0;x<len;++x)
                   {
                     if(text.charAt(x) == '(' || text.charAt(x) == ')')
                       str += "\\";
                     str += text.charAt(x);
                   }

                   fho.writeBytes("(" + str + ") " + font + style + italic + " " + size + " SS " + across1 + " " + flip(down1, orientation));

                   if(just.equals("R"))
                     fho.writeBytes(" MSR\n");
                   else fho.writeBytes(" MS\n");

                   break;
        case 'L' :  x=2;
                    down1 = "";
                    while(x < len && tmp[x] != ',')
                      down1 += (char)tmp[x++];
                    ++x;

                    across1 = "";
                    while(x < len && tmp[x] != ',')
                      across1 += (char)tmp[x++];
                    ++x;

                    down2 = "";
                    while(x < len && tmp[x] != ',')
                      down2 += (char)tmp[x++];
                    ++x;

                    across2 = "";
                    while(x < len && tmp[x] != ',')
                      across2 += (char)tmp[x++];

                    fho.writeBytes("0.1 setlinewidth\n");
                    fho.writeBytes(across1 + " " + flip(down1, orientation) + " moveto\n");
                    fho.writeBytes(across2 + " " + flip(down2, orientation) + " lineto\n");
                    fho.writeBytes("0 setgray\n");
                    fho.writeBytes("stroke\n");
                    break;
        case 'B' :  x=2;
                    down1 = "";
                    while(x < len && tmp[x] != ',')
                      down1 += (char)tmp[x++];
                    ++x;

                    across1 = "";
                    while(x < len && tmp[x] != ',')
                      across1 += (char)tmp[x++];
                    ++x;

                    down2 = "";
                    while(x < len && tmp[x] != ',')
                      down2 += (char)tmp[x++];
                    ++x;

                    across2 = "";
                    while(x < len && tmp[x] != ',')
                      across2 += (char)tmp[x++];

                    fho.writeBytes("0.1 setlinewidth\n");
                    fho.writeBytes(across1 + " " + flip(down1, orientation) + " moveto\n");
                    fho.writeBytes(across2 + " " + flip(down1, orientation) + " lineto\n");
                    fho.writeBytes(across2 + " " + flip(down2, orientation) + " lineto\n");
                    fho.writeBytes(across1 + " " + flip(down2, orientation) + " lineto\n");
                    fho.writeBytes(across1 + " " + flip(down1, orientation) + " lineto\n");
                    fho.writeBytes("0 setgray\n");
                    fho.writeBytes("stroke\n");
                    break;
        case 'I' :
                    break;
        default : 
                    break;
      }
    }

    fho.writeBytes("showpage\n");

    fho.close();
    fh.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputHeader(RandomAccessFile fho, char orientation, String fileName, String numPages) throws Exception
  {
//    fho.writeBytes("%!PS-Adobe-2.0\n");
//    fho.writeBytes("%%Creator: ZaraStar Copyright 1997-2009 Christopher Harvey\n");
//    fho.writeBytes("%%Title: " + fileName + "\n");
//    fho.writeBytes("%%Pages: " + numPages + "\n");
//    fho.writeBytes("%%PageOrder: Ascend\n");
//    fho.writeBytes("%%BoundingBox: 0 0 612 792\n");
//    fho.writeBytes("%%DocumentPaperSizes: Letter\n");
//    fho.writeBytes("%%EndComments\n");

    fho.writeBytes("%!PS\n");
    fho.writeBytes("matrix currentmatrix /originmat exch def\n");
    fho.writeBytes("/umatrix {originmat matrix concatmatrix setmatrix} def\n");
    fho.writeBytes("[2.83465 0 0 2.83465 1.05 1.00] umatrix\n");

    fho.writeBytes("/RSH {dup stringwidth pop neg 0 rmoveto show} bind def\n");

    fho.writeBytes("/SS {scalefont setfont} bind def\n");
    fho.writeBytes("/MS {moveto show} bind def\n");
    fho.writeBytes("/MSR {moveto RSH} bind def\n");
    fho.writeBytes("/T {/Times-Roman findfont} bind def\n");
    fho.writeBytes("/TB {/Times-Bold findfont} bind def\n");
    fho.writeBytes("/TI {/Times-Italic findfont} bind def\n");
    fho.writeBytes("/TBI {/Times-BoldItalic findfont} bind def\n");
    fho.writeBytes("/C {/Courier findfont} bind def\n");
    fho.writeBytes("/CB {/Courier-Bold findfont} bind def\n");
    fho.writeBytes("/CI {/Courier-Oblique findfont} bind def\n");
    fho.writeBytes("/CBI {/Courier-BoldOblique findfont} bind def\n");
    fho.writeBytes("/H {/Helvetica findfont} bind def\n");
    fho.writeBytes("/HB {/Helvetica-Bold findfont} bind def\n");
    fho.writeBytes("/HI {/Helvetica-Oblique findfont} bind def\n");
    fho.writeBytes("/HBI {/Helvetica-BoldOblique findfont} bind def\n");

    if(orientation == 'L')
      fho.writeBytes("90 rotate\n");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String psSizes(int oldSize) throws Exception
  {
    switch(oldSize)
    {
      case  9 : return "2.5";
      case 10 : return "3.6";
      case 11 : return "4";
      case 14 : return "6";
      case 15 : return "6.2";
      case 20 : return "7.5";
      case 25 : return "8";
      case 35 : return "10";
    }

    return "2.7";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String flip(String down, char orientation) throws Exception
  {
    double x = generalUtils.doubleFromStr(down);
    int y = 277 - (int)x;

    if(orientation == 'L')
      y -= 280;

    return generalUtils.intToStr(y);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getNextLine(RandomAccessFile fh, byte[] buf, int bufSize) throws Exception
  {
    int x=0, red=0;
    boolean inQuote = false;

    long curr = fh.getFilePointer();
    long high = fh.length();

    fh.seek(curr);

    if(curr == high)
      return false;

    fh.read(buf, 0, 1);
    while(curr < high && x < (bufSize - 1))
    {
      if(buf[x] == '"')
      {
        if(inQuote)
          inQuote = false;
        else inQuote = true;
      }

      if((buf[x] == 10 || buf[x] == 13 || buf[x] == 26) && ! inQuote)
      {
        while(buf[x] == 10 || buf[x] == 13 || buf[x] == 26)
        {
          red = fh.read(buf, x, 1);
          if(red < 0)
            break;
        }

        if(buf[x] == 26)
          ;
        else
          if(red > 0)
            fh.seek(fh.getFilePointer() -1);

        buf[x] = '\000';

        return true;
      }

      ++x;
      fh.read(buf, x, 1);
      ++curr;
    }

    // remove trailing spaces
    x = bufSize - 1;
    while(buf[x] == 32)
      --x;
    buf[++x] = '\000';

    return true;
  }

}
