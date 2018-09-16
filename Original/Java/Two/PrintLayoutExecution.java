// =======================================================================================================================================================================================================
// System: Zara: General: Do print layout itself
// Module: PrintLayoutExecution.java
// Author: C.K.Harvey
// Copyright (c) 1997-2004 Christopher Harvey. All Rights Reserved.
// Where:  Client
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.applet.*;
import java.math.BigInteger;
import java.awt.print.*;
import java.awt.Graphics2D;
import java.awt.geom.*;

public class PrintLayoutExecution extends Applet implements Runnable, ActionListener
{
  Paper pap = new Paper();

  Image image;

  // drawStuffOntoPage vars
  double ppm = 72 / 25.4;
  Font font;
  double numOfPointsAcross;
  int ff, fw, italic;
  String faceName = "Serif";;
  String textStuff;
  String thisLine;
  char orientation;
  int[] p         = new int[1];
  int[] lineWidth = new int[1];
  int[] fontSize  = new int[1];
  byte[] fontFace   = new byte[50]; // plenty
  char[] fontStyle  = new char[1];
  char[] fontItalic = new char[1];
  char[] type       = new char[1];
  char[] just       = new char[1];
  double[] down1Coord   = new double[1];
  double[] across1Coord = new double[1];
  double[] down2Coord   = new double[1];
  double[] across2Coord = new double[1];
  double[] width        = new double[1];
  double[] height       = new double[1];

  int i;
  boolean found;
  BigInteger bi;

  PrinterJob job;

  short status = 0;
  String DataFromDB = null;
  int pageNo;
  PrintLayoutExecutionA canvas;
  Frame f;
  int numPages = 1;
  double mmWidth=210, mmHeight=297;
  List pagesList, printList;

  String fileName   = "";
  String pageNum    = "";
  String dnm = "";
  String unm = "";
  String sid = "";
  String uty = "";
  String bnm = "";
  String men = "";
  String den = "";

  int pagesToPrint;
  String[] entries;

  public void start() { }

  public void stop() { }

  public void run() { }

  public void resetStatus()
  {
    status = 0;
  }

  public void init()
  {
    fileName = getParameter("p1"); // fileName
    pageNum  = getParameter("p2"); // pageNum
    dnm      = getParameter("dnm");
    unm      = getParameter("unm");
    sid      = getParameter("sid");
    uty      = getParameter("uty");
    bnm      = getParameter("bnm");
    men      = getParameter("men");
    den      = getParameter("den");

    try
    {
      setLayout(new FlowLayout(FlowLayout.CENTER));

      setBackground(new Color(0, 0, 136));//Color.blue);

      Button b1 = new Button("Previous");
      b1.setActionCommand("prev");
      b1.addActionListener(this);
      add(b1);

      Button b2 = new Button("Next");
      b2.setActionCommand("next");
      b2.addActionListener(this);
      add("West", b2); //pack();

      Button b3 = new Button("GoTo");
      b3.setActionCommand("goto");
      b3.addActionListener(this);
      add("West", b3); //pack();

      getReportDetails(unm, sid, uty, dnm, men, den, bnm);

      pagesList = new List(3, false);

      for(int x=1;x<=numPages;++x)
        pagesList.add(String.valueOf(x));
      add("West", pagesList);

      Button b4 = new Button("Print");
      b4.setActionCommand("print");
      b4.addActionListener(this);
      add("West", b4);

      printList = new List(3, true);

      printList.add("All Pages");
      printList.select(0);

      for(int x=1;x<=numPages;++x)
        printList.add(String.valueOf(x));
      add("West", printList);

      canvas = new PrintLayoutExecutionA();
      add("West", canvas);

      pageNo = 1;
      getPageFromServer(unm, sid, uty, dnm, men, den, bnm);

      setVisible(true);
    }
    catch (Exception e) { status = -1; }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void actionPerformed(ActionEvent e)
  {
    String cmd = e.getActionCommand();

    try
    {
      if(cmd.equals("print"))
      {
        entries = printList.getSelectedItems();

        if(entries[0].equals("All Pages"))
          pagesToPrint = numPages;
        else pagesToPrint = entries.length;

        job = PrinterJob.getPrinterJob();

        PrintLayoutExecutionB printLayoutExecutionb = new PrintLayoutExecutionB();

        PageFormat pf = new PageFormat();

        if(orientation == 'L')
        {
          pf = job.defaultPage();

          pf.setOrientation(PageFormat.LANDSCAPE);
          pap.setSize((double)(mmHeight * (72.0 / 25.4)),(double)(mmWidth * (72.0 / 26.5)));
          pap.setImageableArea(0, 0, (double)(mmHeight * (72.0 / 25.4)),(double)(mmWidth * (72.0 / 26.5)));
        }
        else
        {
          pf = job.defaultPage();
          pf.setOrientation(PageFormat.PORTRAIT);
          pap.setSize((double)(mmWidth * (72 / 25.4)), (double)(mmHeight * (72 / 25.4)));
          pap.setImageableArea(0, 0, (double)(mmWidth * (72 / 25.4)), (double)(mmHeight * (72 / 25.4)));
        }

        pf.setPaper(pap);

        job.setPrintable(printLayoutExecutionb, job.validatePage(pf));

        if(job.printDialog())
        {                  
          int pageNoSave = pageNo;
          pageNo = 1;

          if(entries[0].equals("All Pages"))
            job.print();
          else
          {
            for(int i=1;i<=pagesToPrint;++i)
            {
              BigInteger bi = new BigInteger(entries[i-1]);
              pageNo = bi.intValue();
              job.print();
            }
            pageNo = pageNoSave;
          }
        }
      }
      else
      if(cmd.equals("next"))
      {
        if(pageNo < numPages)
        {
          ++pageNo;
          getPageFromServer(unm, sid, uty, dnm, men, den, bnm);
          pagesList.select(pageNo - 1);
        }
      }
      else
      if(cmd.equals("prev"))
      {
        if(pageNo > 1)
        {
          --pageNo;
          getPageFromServer(unm, sid, uty, dnm, men, den, bnm);
          pagesList.select(pageNo - 1);
        }
      }
      else
      if(cmd.equals("goto"))
      {
        String[] entries = pagesList.getSelectedItems();
        bi = new BigInteger(entries[0]);
        pageNo = bi.intValue();

        getPageFromServer(unm, sid, uty, dnm, men, den, bnm);
      }
    }
    catch(Exception ex)
    {
      canvas.str = ex.toString();
      canvas.repaint();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  protected class PrintLayoutExecutionA extends Canvas
  {
    String str;

    public Dimension getPreferredSize()
    {
      if(orientation == 'P')
      {
        return new Dimension((int)(475.0 * (mmWidth / mmHeight)), (int)475.0);
      }
      else // landscape
      {
        return new Dimension((int)700.0, (int)(700.0 * (mmWidth / mmHeight)));
      }
    }

    public void paint(Graphics g)
    {
      setBackground(Color.white);

      Graphics2D g2 = (Graphics2D)g;
      g2 = (Graphics2D)g;

      if(orientation == 'P')
      {
        g2.scale(mmHeight / 475.0, mmHeight / 475.0);
      }
      else
      {
        g2.scale(mmWidth / 350.0, mmWidth / 350.0);
      }

      try
      {
        drawStuffOntoPage2(DataFromDB, g2);
      }
      catch(Exception e) { }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  protected class PrintLayoutExecutionB extends PageFormat implements Printable
  {
    String str;

    public void paint(Graphics g) throws Exception
    {
    }

    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException
    {
      try
      {
        if(pi >= pagesToPrint)
          return Printable.NO_SUCH_PAGE;

        if(entries[0].equals("All Pages") && entries.length == 1)
        {
          pageNo = pi + 1;
          found = true;
        }
        else
          if(pi >= 1)
            return Printable.NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D)g;
        getPageFromServer(unm, sid, uty, dnm, men, den, bnm);
        drawStuffOntoPage2(DataFromDB, g2);

        if(orientation == 'L')
        {
          pf = job.defaultPage();
          pf.setOrientation(PageFormat.LANDSCAPE);
        }
        else
        {
          pf = job.defaultPage();
          pf.setOrientation(PageFormat.PORTRAIT);
        }

        if(orientation == 'L')
        {
          pap.setSize((double)(mmHeight * (72.0 / 25.4)),(double)(mmWidth * (72.0 / 26.5)));
          pap.setImageableArea(0, 0, (double)(mmHeight * (72.0 / 25.4)),(double)(mmWidth * (72.0 / 26.5)));
        }
        else
        {
          pap.setSize((double)(mmWidth * (72 / 25.4)), (double)(mmHeight * (72 / 25.4)));
          pap.setImageableArea(0, 0, (double)(mmWidth * (72 / 25.4)), (double)(mmHeight * (72 / 25.4)));
        }

        pf.setPaper(pap);

        job.setPrintable(this, job.validatePage(pf));

        g2.translate(pf.getImageableX(), pf.getImageableY());

        paint(g);

        return Printable.PAGE_EXISTS;
      }
      catch(Exception e) { return Printable.NO_SUCH_PAGE; }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getPageFromServer(String unm, String sid, String uty, String dnm, String men, String den, String bnm) throws Exception
  {
    URL url = new URL("http://" + men + "/central/servlet/FetchPrintPageData?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + fileName + "&p2=" + pageNo + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    DataFromDB = "";
    String s = di.readLine();
    while(s != null)
    {
      DataFromDB += s;
      s = di.readLine();
    }

    di.close();

    canvas.repaint();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getReportDetails(String unm, String sid, String uty, String dnm, String men, String den, String bnm) throws Exception
  {
    URL url = new URL("http://"+ men + "/central/servlet/ReportDetails?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + fileName + "&bnm=" + bnm);

    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setUseCaches(false);
    uc.setDefaultUseCaches(false);

    uc.setRequestProperty("Content-Type", "application/octet-stream");

    BufferedReader di = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String s = di.readLine();

    int x=0;
    while(s.charAt(x) != ':')
      ++x;

    bi = new BigInteger(s.substring(0, x));
    numPages = bi.intValue();

    int y = ++x;
    while(s.charAt(x) != ':')
      ++x;

    bi = new BigInteger(s.substring(y, x));
    mmWidth = bi.intValue();

    y = ++x;
    while(s.charAt(x) != ':')
      ++x;

    bi = new BigInteger(s.substring(y, x));
    mmHeight = bi.intValue();

    ++x;
    orientation = s.charAt(x);

    di.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void drawStuffOntoPage2(String line, Graphics2D g) throws Exception
  {
    try
    {
      p[0]=0;
      int len = line.length();
      thisLine = getNextLine(line, p, len);

      while(p[0] <= len)
      {
        textStuff = breakDownLine(thisLine, down1Coord, across1Coord, fontFace, fontStyle, fontItalic, fontSize, down2Coord, across2Coord, lineWidth, just, type);
        
        switch(type[0])
        {
          case 'D' : // textual data
                     try
                     {
                       if(fontFace[1] == '\000')
                       {
                         switch((char)fontFace[0])
                         {
                           case 'R' : faceName = "Serif";      break; // TimesRoman
                           case 'S' : faceName = "SansSerif";  break; // Helvetica
                           default  : faceName = "Monospaced"; break; // Couriernew
                         }
                       }
                       else // user-specified font
                         faceName = stringFromBytes(fontFace, 0);

                       switch(fontStyle[0])
                       {
                         case 'B' : fw = Font.BOLD;  break;
                         default  : fw = Font.PLAIN; break;
                       }

                       switch(fontItalic[0])
                       {
                         case 'I' : italic = Font.ITALIC;  break;
                         default  : italic = 0;            break;
                       }

                       font = new Font(faceName, fw | italic, fontSize[0]);
                       g.setPaint(java.awt.Color.black);
                       g.setFont(font);

                       numOfPointsAcross = across1Coord[0]*ppm;

                       if(just[0] == 'R')
                       {
                         Rectangle2D rect = font.getStringBounds(textStuff, g.getFontRenderContext());

                         numOfPointsAcross -= rect.getWidth();
                       }

                       g.drawString(textStuff, (int)numOfPointsAcross, (int)((down1Coord[0])*ppm));
                     }
                     catch(Exception ee) { }
                     break;
          case 'L' : // line
                     g.drawLine((int)(across1Coord[0]*ppm), (int)(down1Coord[0]*ppm), (int)(across2Coord[0]*ppm), (int)(down2Coord[0]*ppm));
                     break;
          case 'B' : // box
                     g.draw(new Rectangle2D.Double(across1Coord[0]*ppm, down1Coord[0]*ppm, (across2Coord[0]-across1Coord[0])*ppm, (down2Coord[0]-down1Coord[0])*ppm));
                     break;
          case 'R' : // rounded box
                     g.draw(new RoundRectangle2D.Double(across1Coord[0]*ppm, down1Coord[0]*ppm, (across2Coord[0]-across1Coord[0])*ppm, (down2Coord[0]-down1Coord[0])*ppm, 10, 10));
                     break;
          case 'S' : // page size
                     break;
          case 'I' : // image
                     image = getImage(getCodeBase(), textStuff);
                     g.drawImage(image, (int)(across1Coord[0]*ppm), (int)(down1Coord[0]*ppm), this);
                     break;
          default  : // ignore
                     break;
        }

        thisLine = getNextLine(line, p, len);
      }
    }
    catch(Exception e) { }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // lines of string separated by '\001'
  private String getNextLine(String line, int[] p, int len) throws Exception
  {
    String thisLine="";

    while(p[0] < len && line.charAt(p[0]) != '\001')
    {
      thisLine += line.charAt(p[0]);
      ++p[0];
    }
    ++p[0];
    return thisLine;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // D:26.00,15.00,R,B,I,12,L,"CONTRACT WORKERS WAGES CLAIM (Monday to Sunday)"
  // D:26.00,15.00,Courier New,B,I,12,L,"CONTRACT WORKERS WAGES CLAIM (Monday to Sunday)"
  private String breakDownLine(String line, double[] down1Coord, double[] across1Coord, byte[] fontFace, char[] fontStyle, char[] fontItalic, int[] fontSize, double[] down2Coord, double[] across2Coord, int[] lineWidth, char[] just, char[] type)
                               throws Exception
  {
    try
    {
    int  x;
    String textStuff="";
    String buf;

    int lenLine = line.length();
    across1Coord[0] = down1Coord[0] = across2Coord[0] = down2Coord[0] = 0.0;
    just[0] = ' ';

    if(lenLine > 0)
      type[0] = line.charAt(0);
    else type[0] = ' ';

    switch(type[0])
    {
      case 'D' : // textual data
	             // down coord
	             x = 2;
                 buf="";
	             while(x < lenLine && line.charAt(x) != ',')
                   buf += line.charAt(x++);
                 down1Coord[0] = doubleFromStr(buf);

   	              // across coord
	              if(line.charAt(x) != ',') // something is illegal
	              {
		            type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','
	         buf="";
	         while(x < lenLine && line.charAt(x) != ',')
                   buf += line.charAt(x++);
                 across1Coord[0] = doubleFromStr(buf);

	         ++x; // step over ','
                 // fontface
                 int y=0;
                 while(x < lenLine && line.charAt(x) != ',')
                   fontFace[y++] = (byte)line.charAt(x++);
                 fontFace[y] = (byte)'\000';

	         // fontstyle
                 if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','
	         fontStyle[0] = line.charAt(x++);

	         // fontitalic
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','
	         fontItalic[0] = line.charAt(x++);

	         // fontsize
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','
	         buf="";
	         while(x < lenLine && line.charAt(x) != ',')
		   buf += line.charAt(x++);

	         fontSize[0] = intFromStr(buf);

                 // just
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','
	         just[0] = line.charAt(x++);

	         // text
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','

	         ++x; // step over opening quote
	         textStuff = "";
	         while(x < lenLine && line.charAt(x) != '\"')
                   textStuff += line.charAt(x++);

	         down2Coord[0] = across2Coord[0] = 0.0;
	         break;
      case 'L' : // line
      case 'B' : // box
      case 'R' : // rounded box
	         // down1 coord
	         buf="";
                 x = 2;
	         while(x < lenLine && line.charAt(x) != ',')
                   buf += line.charAt(x++);

	         down1Coord[0] = doubleFromStr(buf);

	         // across1 coord
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','

                 buf="";
	         while(x < lenLine && line.charAt(x) != ',')
		   buf += line.charAt(x++);

	         across1Coord[0] = doubleFromStr(buf);

	         // down2 coord
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
                 ++x; // step over ','

	         buf="";
	         while(x < lenLine && line.charAt(x) != ',')
                   buf += line.charAt(x++);

                 down2Coord[0] = doubleFromStr(buf);

	         // across2 coord
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
                 }
	         ++x; // step over ','

	         buf="";
	         while(x < lenLine && line.charAt(x) != ',')
                   buf += line.charAt(x++);

	         across2Coord[0] = doubleFromStr(buf);

	         // linewidth
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }

	         ++x; // step over ','
	         buf="";
	         while(x < lenLine && line.charAt(x) != ',')
                 buf += line.charAt(x++);

	         lineWidth[0] = intFromStr(buf);
	         break;
      case 'I' : // image
	         // down coord
	         x = 2;
                 buf="";
	         while(x < lenLine && line.charAt(x) != ',')
                   buf += line.charAt(x++);
                 down1Coord[0] = doubleFromStr(buf);

	         // across coord
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','
	         buf="";
	         while(x < lenLine && line.charAt(x) != ',')
                   buf += line.charAt(x++);
                 across1Coord[0] = doubleFromStr(buf);

	         // text
	         if(line.charAt(x) != ',') // something is illegal
	         {
		   type[0] = ' ';
		   break;
	         }
	         ++x; // step over ','

	         ++x; // step over opening quote
	         textStuff = "";
	         while(x < lenLine && line.charAt(x) != '\"')
                   textStuff += line.charAt(x++);

	         down2Coord[0] = across2Coord[0] = 0.0;
	         break;
      case 'S' : break;
      default  : // unknown
                 type[0] = ' ';
                 break;
    }

    return textStuff;

    }
    catch(Exception e)
    {
      type[0] = ' ';
      return "";
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public short getStatus()
  {
    return status;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String stringFromBytes(byte[] b, long start)
  {
    String s="";
    int x=(int)start;
    while(b[x] != '\000')
    {
      s += (char)b[x];
      ++x;
    }
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private double doubleFromStr(String s)
  {
    if(s.length() == 0) return 0.0;
    if(s.charAt(0) == ' ') return 0.0;

    String t = "";
    for(int x=0;x<s.length();++x)
    {
      if(s.charAt(x) != ',')
        t += s.charAt(x);
    }

    Double d;
    if(t.charAt(0) == '-')
    {
      d = new Double(t.substring(1));
      return (d.doubleValue() * -1);
    }
    else
    {
      d = new Double(t);

      return d.doubleValue();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int intFromStr(String s)
  {
    if(s.length() == 0) return 0;
    if(s.charAt(0) == ' ') return 0;

    Integer i = new Integer(s);

    return i.intValue();
  }

}

