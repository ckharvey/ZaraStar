// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Report generation utilities
// Module: ReportGeneration.java
// Author: C.K.Harvey
// Copyright (c) 1998-2007 C.K.Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;

public class ReportGeneration
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  
  static final short FONTNONE              = -1;
  static final short SHAPENONE             = -1;
  static final short COORDSNONE            = -1;
  static final short COORDSDOWNABSOLUTE    = 1;
  static final short COORDSDOWNTOPPAGE     = 2;
  static final short COORDSDOWNBOTTOMPAGE  = 3;
  static final short COORDSDOWNRELATIVE    = 4;
  static final short COORDSACROSSLEFTPAGE  = 5;
  static final short COORDSACROSSRIGHTPAGE = 6;
  static final short COORDSACROSSCENTPAGE  = 7;
  static final short COORDSACROSSLEFTOF    = 8;
  static final short COORDSACROSSRIGHTOF   = 9;
  static final short COORDSACROSSCENTOF    = 10;
  static final short COORDSACROSSABSOLUTE  = 11;
  static final short COORDSACROSSRELATIVE  = 12;

  public RandomAccessFile fhPPR, fhO;

  char   negStyle, zeroStyle;
  public double tm, bm, lm, rm, pageSizeWidth, pageSizeLength, currDown, currAcross;
  public short dp, currFont, currPage, oPtr, oBufLen;
  public boolean lastOperationPF;
  String userFontName, userFontStyle, userFontItalic, userFontSize;

  char[] fontDefType   = new char[21];
  char[] fontDefStyle  = new char[21];
  char[] fontDefItalic = new char[21];
  
  int[]  fontDefSize   = new int[21];

  double[] down2Coord   = new double[1];
  double[] across2Coord = new double[1];
  
  public byte[] oBuf;
  byte[] ch = new byte[1];
  byte[] tmp = new byte[1000];
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public RandomAccessFile getFirstDefnLine(String defnFile, String defnsDir, String xxxxxxxxxx, byte[] style, byte[] accCodeOrFormula,
                                           byte[] percentageFormula, byte[] schedule, byte[] text, short[] numDefnLines) throws Exception
  {
    long curr, high;
    byte[] buf = new byte[1];
    byte[] lineNum = new byte[20];

    RandomAccessFile fh;
    if((fh = generalUtils.fileOpenD(defnFile, defnsDir)) == null)
    {
      if((fh = generalUtils.fileOpenD(defnFile, xxxxxxxxxx)) == null)
        return null;
    }

    curr = fh.getFilePointer();
    high = fh.length();

    fh.seek(0);//curr);

    if(curr == high)
    return((RandomAccessFile)null);

    numDefnLines[0] = 0;
    fh.read(buf);
    while(curr < high)
    {
      if(buf[0] == (byte)10)
        ++ numDefnLines[0];
      fh.read(buf);
      ++curr;
    }

    fh.seek(0);
    getNextDefnLine(fh, lineNum, style, accCodeOrFormula, percentageFormula, schedule, text);
    return fh;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getNextDefnLine(RandomAccessFile fh, byte[] lineNum, byte[] style, byte[] accCodeOrFormula, byte[] percentageFormula,
                                 byte[] schedule, byte[] text) throws Exception
  {
    short x, y;
    int red=0, quit;
    long  curr, high;

    for(x=0;x<1000;++x) tmp[x] = ' ';

    curr = fh.getFilePointer();
    high = fh.length();

    fh.seek(curr);

    if(curr == high)
      return false;

    quit = 0;  x = 0;
    fh.read(ch);
    tmp[0] = ch[0];
    while(curr < high && x < 999 && quit == 0)
    {
      if(tmp[x] == (byte)10 || tmp[x] == (byte)13 || tmp[x] == (byte)26)
      {
        while(tmp[x] == (byte)10 || tmp[x] == (byte)13 || tmp[x] == (byte)26)
        {
          red = fh.read(ch);
          tmp[x] = ch[0];
          if(red == -1)
            break;
        }

        if(tmp[x] == (byte)26)
          ;   //	--x;
        else
	       if(red != -1)
            fh.seek(fh.getFilePointer() - 1);

        tmp[x] = '\000';
        quit = 1;
      }

      if(quit == 0) // still
      {
        ++x;
        fh.read(ch);
        tmp[x] = ch[0];
        ++curr;
      }
    }

    // remove trailing spaces
    x = 999;
    while(tmp[x] == ' ')
      --x;

    tmp[++x] = '\000';

    // break down line
    x=0;  y=0;
    while(tmp[x] != ':' && tmp[x] != '\000' && y < 10) // just-in-case
      lineNum[y++] = tmp[x++];
    --y;
    while(lineNum[y] == ' ')
      --y;
    lineNum[++y] = '\000';
    if(y == 10 || tmp[x] == '\000') // just-in-case
    {
      percentageFormula[0] = accCodeOrFormula[0] = schedule[0] = text[0] = '\000';
      return true;
    }

    ++x; // step over ':'
    while(tmp[x] == ' ')
      ++x;
    y=0;
    accCodeOrFormula[0] = ' ';
    while(tmp[x] != ':' && tmp[x] != '\000' && y < 800) // just-in-case
      accCodeOrFormula[y++] = tmp[x++];
    if(y > 0)
      --y;

    while(y >= 0 && accCodeOrFormula[y] == ' ')
      --y;
    accCodeOrFormula[++y] = '\000';

    if(y == 800 || tmp[x] == '\000') // just-in-case
    {
      percentageFormula[0] = text[0] = schedule[0] = '\000';
      return true;
    }

    ++x; // step over ':'
    while(tmp[x] == ' ')
      ++x;
    y=0;
    percentageFormula[0] = ' ';
    while(tmp[x] != ':' && tmp[x] != '\000' && y < 100) // just-in-case
      percentageFormula[y++] = tmp[x++];
    if(y > 0)   --y;
    while(y >= 0 && percentageFormula[y] == ' ')
      --y;
    percentageFormula[++y] = '\000';

    if(y == 100 || tmp[x] == '\000') // just-in-case
    {
      schedule[0] = text[0] = '\000';
      return true;
    }

    ++x; // step over ':'
    while(tmp[x] == ' ')
      ++x;
    y=0;
    schedule[0] = ' ';
    while(tmp[x] != ':' && tmp[x] != '\000' && y < 100) // just-in-case
      schedule[y++] = tmp[x++];
    if(y > 0)   --y;
    while(y >= 0 && schedule[y] == ' ')
      --y;
    schedule[++y] = '\000';

    if(y == 100 || tmp[x] == '\000') // just-in-case
    {
      text[0] = '\000';
      return true;
    }

    ++x; // step over ':'
    while(tmp[x] == ' ')
      ++x;
    y=0;
    while(tmp[x] != ':' && tmp[x] != '\000' && y < 8) // just-in-case
      style[y++] = tmp[x++];
    if(y > 0)    --y;
    while(y >= 0 && style[y] == ' ')
      --y;
    style[++y] = '\000';

    ++x; // step over ':'
    while(tmp[x] == ' ')
      ++x;
    ++x; // step over "
    y=0;
    while(tmp[x] != '\"' && tmp[x] != '\000' && y < 800) // just-in-case
      text[y++] = tmp[x++];
    if(y > 0)    --y;
    while(y >= 0 && text[y] == ' ')
      --y;
    text[++y] = '\000';

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getDefnLine(RandomAccessFile fh, short reqdLineNum, byte[] style, byte[] accCodeOrFormula, byte[] percentageFormula, byte[] schedule,
                             byte[] text) throws Exception
  {
    short quit;
    long  save;
    byte[] lineNum = new byte[20];

    save = fh.getFilePointer();

    fh.seek(0);
    if(! getNextDefnLine(fh, lineNum, style, accCodeOrFormula, percentageFormula, schedule, text))
      quit = -1; // not exists
    else quit=0;

    while(quit == 0)
    {
      if(generalUtils.intFromBytes(lineNum, 0) == reqdLineNum)
        quit = 1;
      else
      if(! getNextDefnLine(fh, lineNum, style, accCodeOrFormula, percentageFormula, schedule, text))
        quit = -1; // not exists
    }

    fh.seek(save);

    if(quit == 1)
      return true;
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void processControl(String dbName, String uName, String localDefnsDir, String defnsDir) throws Exception
  {
    short quit, fontNum, x, y;
    byte[] line = new byte[500];
    byte[] item = new byte[500];

    // set defaults
    dp = 2;
    negStyle  = 'P'; // negparen
    zeroStyle = 'Z'; // print zeroes
    fontDefType[1]    = 'R'; fontDefStyle[1]  = 'N'; fontDefItalic[1] = ' ';
    fontDefSize[1]    =  11;
    fontDefType[2]    = 'R'; fontDefStyle[2]  = 'B'; fontDefItalic[2] = ' ';
    fontDefSize[2]    =  11;
    fontDefType[3]    = 'R'; fontDefStyle[3]  = 'N'; fontDefItalic[3] = 'I';
    fontDefSize[3]    =  11;
    fontDefType[4]    = 'R'; fontDefStyle[4]  = 'B'; fontDefItalic[4] = ' ';
    fontDefSize[4]    =  15;
    fontDefType[5]    = 'R'; fontDefStyle[5]  = 'N'; fontDefItalic[5] = 'I';
    fontDefSize[5]    =  25;
    fontDefType[6]    = 'S'; fontDefStyle[6]  = 'N'; fontDefItalic[6] = ' ';
    fontDefSize[6]    =  9;
    fontDefType[7]    = 'S'; fontDefStyle[7]  = 'N'; fontDefItalic[7] = ' ';
    fontDefSize[7]    =  10;
    fontDefType[8]    = 'S'; fontDefStyle[8]  = 'N'; fontDefItalic[8] = ' ';
    fontDefSize[8]    =  11;
    fontDefType[9]    = 'S'; fontDefStyle[9]  = 'B'; fontDefItalic[9] = ' ';
    fontDefSize[9]    =  15;
    fontDefType[10]   = 'M'; fontDefStyle[10] = 'N'; fontDefItalic[10] = ' ';
    fontDefSize[10]   =  11;
    fontDefType[11]   = 'R'; fontDefStyle[11] = 'N'; fontDefItalic[11] = ' ';
    fontDefSize[11]   =  20;
    fontDefType[12]   = 'R'; fontDefStyle[12] = 'N'; fontDefItalic[12] = ' ';
    fontDefSize[12]   =  25;
    fontDefType[13]   = 'M'; fontDefStyle[13] = 'N'; fontDefItalic[13] = ' ';
    fontDefSize[13]   =  10;
    fontDefType[14]   = 'M'; fontDefStyle[14] = 'B'; fontDefItalic[14] = ' ';
    fontDefSize[14]   =  11;
    fontDefType[15]   = 'R'; fontDefStyle[15] = 'B'; fontDefItalic[15] = 'I';
    fontDefSize[15]   =  15;
    fontDefType[16]   = 'S'; fontDefStyle[16] = 'B'; fontDefItalic[16] = ' ';
    fontDefSize[16]   =  25;
    fontDefType[17]   = 'S'; fontDefStyle[17] = 'N'; fontDefItalic[17] = 'I';
    fontDefSize[17]   =  11;
    fontDefType[18]   = 'R'; fontDefStyle[18] = 'N'; fontDefItalic[18] = ' ';
    fontDefSize[18]   =  35;
    fontDefType[19]   = 'S'; fontDefStyle[19] = 'B'; fontDefItalic[19] = ' ';
    fontDefSize[19]   =  9;
    fontDefType[20]   = 'R'; fontDefStyle[20] = 'N'; fontDefItalic[20] = ' ';
    fontDefSize[20]   =  14;
    tm = bm = lm = rm = 0.0;
    pageSizeWidth = 210.0;
    pageSizeLength = 297.0;

    if(gotoSection("CONTROL") == 0)
    {
      quit = 0;
      while(quit == 0)
      {
        if(getNextLine(line, (short)500) == -1)
          quit = 1;
        else
        if(isASection(line))
          quit = 1;
        else
        {
	      getItem(line, (short)0, item);
          if(generalUtils.match(item, "DP", 2))
          {
	         getItem(line, (short)1, item);
	         if(item[0] == '=')
     	      getItem(line, (short)2, item);
            dp= (short)generalUtils.intFromBytesCharFormat(item, (short)0);
          }
          else
          if(generalUtils.match(item, "NEGPAREN", 8))
            negStyle = 'P';
          else
          if(generalUtils.match(item, "NEGSIGN", 7))
            negStyle = 'S';
          else
          if(generalUtils.match(item, "NOZERO", 6))
            zeroStyle = 'N';
          else
          if(generalUtils.match(item, "TOPMARGIN", 9))
          {
     	    getItem(line, (short)1, item);
            if(item[0] == '=')
              getItem(line, (short)2, item);
            tm = generalUtils.doubleFromChars(item);
          }
          else
          if(generalUtils.match(item, "BOTTOMMARGIN", 12))
          {
     	    getItem(line, (short)1, item);
            if(item[0] == '=')
              getItem(line, (short)2, item);
            bm = generalUtils.doubleFromChars(item);
          }
          else
          if(generalUtils.match(item, "LEFTMARGIN", 10))
          {
     	    getItem(line, (short)1, item);
            if(item[0] == '=')
       	    getItem(line, (short)2, item);
            lm = generalUtils.doubleFromChars(item);
          }
          else
          if(generalUtils.match(item, "RIGHTMARGIN", 11))
          {
     	    getItem(line, (short)1, item);
            if(item[0] == '=')
              getItem(line, (short)2, item);
            rm = generalUtils.doubleFromChars(item);
          }
          else
          if(generalUtils.match(item, "FONT", 4))
          {
     	    getItem(line, (short)1, item);
            fontNum = (short)generalUtils.intFromBytesCharFormat(item, (short)0);
            if(fontNum >= 1 && fontNum <= 20)  // font # is legal
            {
              y = numItemsInStr(line);
              for(x=2;x<y;++x)
              {
                getItem(line, x, item);
                if(generalUtils.match(item, "ROMAN", 5))
                  fontDefType[fontNum] = 'R';
                else
                if(generalUtils.match(item, "MODERN", 6))
                  fontDefType[fontNum] = 'M';
                else
                if(generalUtils.match(item, "SWISS", 5))
                  fontDefType[fontNum] = 'S';
                else
                if(generalUtils.match(item, "NORMAL", 6))
                  fontDefStyle[fontNum] = 'N';
                else
                if(generalUtils.match(item, "BOLD", 4))
                  fontDefStyle[fontNum] = 'B';
                else
                if(generalUtils.match(item, "ITALIC", 6))
                  fontDefItalic[fontNum] = 'I';
                else // must be pt size
                  fontDefSize[fontNum] = generalUtils.intFromBytesCharFormat(item, (short)0);
              }
            }
          }
          else
          if(generalUtils.match(item, "PAGESIZE", 8)) // PAGESIZE = 210 277 LANDSCAPE
          {
            getItem(line, (short)1, item);
            if(item[0] == '=')
              getItem(line, (short)2, item);
            pageSizeWidth = generalUtils.doubleFromChars(item);

            int i=3;
       	    getItem(line, (short)i, item);
            if(item[0] == ',')
              getItem(line, (short)++i, item);
            pageSizeLength = generalUtils.doubleFromChars(item);

     	    getItem(line, (short)++i, item);
            if(item[0] == ',')
              getItem(line, (short)++i, item);
            char orientation = (char)item[0];

            generalUtils.doubleToChars('0', pageSizeWidth, item, 0);
            generalUtils.catAsBytes(",", 0, item, false);
            generalUtils.doubleToChars('0', pageSizeLength, item, generalUtils.lengthBytes(item, 0));

            if(orientation == 'L')
              generalUtils.catAsBytes(",L", 0, item, false);
            else generalUtils.catAsBytes(",P", 0, item, false);

            outputLine('S', item, null, null, (short)0, ' ', dbName, uName, localDefnsDir, defnsDir);
          }
        }
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private int gotoSection(String sectName) throws Exception
  {
    byte[] item = new byte[500];
    byte[] line = new byte[500];

    fhPPR.seek(0);

    while(getNextLine(line, (short)500) != -1)
    {
      if(isASection((byte[])line))
      {
        if(numItemsInStr((byte[])line) > 1)
        {
          getItem((byte[])line, (short)1, (byte[])item);
          if(generalUtils.match(item, sectName))
            return 0;
        }
      }
    }

    return -1;  // not found
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isASection(byte[] line) throws Exception
  {
    byte[] item = new byte[500];

    getItem(line, (short)0, item);
    if(generalUtils.match(item, "SECTION", 7))
      return true;
    return false;  // not a section
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToOBuf(RandomAccessFile fh, byte[] b, int len) throws Exception
  {
    if((oPtr + len) >= oBufLen)
    {
      fh.write(oBuf, 0, oPtr);
      oPtr = 0;
    }

    generalUtils.bytesToBytes(oBuf, oPtr, b, 0);
    oPtr += len;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void addToOBuf(RandomAccessFile fh, String s) throws Exception
  {
    int len = s.length();
    if((oPtr + len) >= oBufLen)
    {
      fh.write(oBuf, 0, oPtr);
      oPtr = 0;
    }

    generalUtils.stringIntoBytes(s, 0, oBuf, oPtr);
    oPtr += len;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // if qryNum == 0 then file is to be "0.000"
  public RandomAccessFile createNewFile(short qryNum, String workingDir, String localDefnsDir, String defnsDir, String reportsDir) throws Exception
  {
    String[] newName = new String[1];
    return createNewFile(qryNum, workingDir, localDefnsDir, defnsDir, reportsDir, newName);
  }
  public RandomAccessFile createNewFile(short qryNum, String workingDir, String localDefnsDir, String defnsDir, String reportsDir,
                                        String[] newName) throws Exception
  {
    if(qryNum == 0)
    {
      newName[0] = "0.000";
      return generalUtils.create(reportsDir + "0.000");
    }

    String dateToday = generalUtils.today(localDefnsDir, defnsDir);

    String fName = "" + dateToday.charAt(6) + dateToday.charAt(7) + dateToday.charAt(3) + dateToday.charAt(4) + dateToday.charAt(0) + dateToday.charAt(1);

    String qryNumStr = generalUtils.intToStr(qryNum);

    String extn;
    if(qryNum < 10)
      extn = "00" + qryNumStr;
    else
    if(qryNum < 100)
      extn = "0" + qryNumStr;
    else extn = qryNumStr;

    // chk for next available number
    File path = new File(reportsDir);
    String names[] = new String[0];
    names = path.list();
    String dateStr;
    int highestSofar=0;
    int thisNum;
    boolean first=true;

    for(int i=0;i<names.length;++i)
    {
      if(names[i].endsWith(extn))
      {
        dateStr = names[i].substring(0, 6);
        if(dateStr.equals(fName))
        {
          thisNum = generalUtils.strToInt(names[i].substring(6, 8));
          if(thisNum > highestSofar)
            highestSofar = thisNum;
        }
      }
    }

    ++highestSofar;

    String num;
    if(highestSofar < 10)
      num = "0" + highestSofar;
    else num = "" + highestSofar;

    newName[0] = fName + num + "." + extn;
    return (RandomAccessFile)generalUtils.create(reportsDir + fName + num + "." + extn);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Looks for either a FONT # >= 1 (and < 20), or an entry of the form:
  // FONT 0 "Times Roman" 12 I B
  private short getFontFromLine(byte[] line) throws Exception
  {
    byte[] item = new byte[500];

    short fontNum;
    short numItems = numItemsInStr(line);
    for(short x=0;x<numItems;++x)
    {
      getItem(line, x, item);
      if(generalUtils.match(item, "FONT", 4))
      {
        getItem(line, (short)(x + 1), item);
        fontNum = (short)generalUtils.intFromBytesCharFormat(item, (short)0);

        if(fontNum > 0)
          return fontNum;

        // else is a user-specified font
        getItem(line, (short)(x + 2), item);
        userFontSize = generalUtils.stringFromBytes(item, 0L);

        getItem(line, (short)(x + 3), item);
        userFontItalic = generalUtils.stringFromBytes(item, 0L);

        getItem(line, (short)(x + 4), item);
        userFontStyle = generalUtils.stringFromBytes(item, 0L);

        getItem(line, (short)(x + 5), item);
        userFontName = "";
        int y = 0;
        while(item[0] != '\000')
        {
          if(y > 0)
            userFontName += " ";
          userFontName += generalUtils.stringFromBytes(item, 0L);
          ++y;
          getItem(line, (short)(x + y + 5), item);
        }

        return (short)0;
      }
    }
    return FONTNONE;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double getCoordsFromLine(byte[] line, double[] down1Coord, double[] across1Coord, short[] down1Status, short[] across1Status,
                                   double[] down2Coord, double[] across2Coord, short[] down2Status, short[] across2Status) throws Exception
  {
    short itemNum, x, y;
    byte[] item    = new byte[500];
    byte[] down1   = new byte[50];
    byte[] across1 = new byte[50];
    byte[] down2   = new byte[50];
    byte[] across2 = new byte[50];
    byte[] across1CoordBytes = new byte[50];
    byte[] across2CoordBytes = new byte[50];

    down1Status[0] = COORDSNONE;
    down2Status[0] = COORDSNONE;
    int numItems = numItemsInStr((byte[])line); 
    if(numItems != 0) // just-in-case
    {
      itemNum=0;
      getItem(line, itemNum, item);
      if(item[0] == '(')
      {
        if(item[1] == '\000')
        {
          ++itemNum;
          getItem(line, itemNum, item);
          x = 0;
        }
        else x = 1;
        y=0;
        while(item[x] != '\000' && y < 50) // just-in-case
          down1[y++] = item[x++];
        down1[y] = '\000';
      }

      ++itemNum;
      getItem(line, itemNum, item);
      y=0; x=0;
      while(item[x] != '\000' && item[x] != ')' && y < 50) // just-in-case
        across1[y++] = item[x++];
      across1[y] = '\000';

      if(   generalUtils.match(across1, "RIGHTOF", 7) || generalUtils.match(across1, "LEFTOF", 6)
         || generalUtils.match(across1, "CENTOF", 6))
      {
        ++itemNum;
        getItem(line, itemNum, item);   
      }
      y=0; x=0;
      while(item[x] != '\000' && item[x] != ')' && y < 50) // just-in-case
        across1CoordBytes[y++] = item[x++];
      across1CoordBytes[y] = '\000';
     //  generalUtils.pb("",across1Coord,0,0);
      if(item[x] != ')')
      {
        ++itemNum;
        getItem(line, itemNum, item);
        y=0; x=0;
        while(item[x] != '\000' && y < 50) // just-in-case
          down2[y++] = item[x++];
        down2[y] = '\000';
        down2Status[0] = COORDSDOWNABSOLUTE; // provisionally

        ++itemNum;
        getItem(line, itemNum, item);
        y=0; x=0;
        while(item[x] != '\000' && item[x] != ')' && y < 50) // just-in-case
          across2[y++] = item[x++];
        across2[y] = '\000';

        if(   generalUtils.match(across2, "RIGHTOF", 7) || generalUtils.match(across2, "LEFTOF", 6)
           || generalUtils.match(across2, "CENTOF", 6))
        {
	       ++itemNum;
          getItem(line, itemNum, item);
        }
        y=0; x=0;
        while(item[x] != '\000' && item[x] != ')' && y < 50) // just-in-case
          across2CoordBytes[y++] = item[x++];
        across2CoordBytes[y] = '\000';
        across2Status[0] = COORDSACROSSABSOLUTE; // provisionally
      }
      else down2Status[0] = across2Status[0] = COORDSNONE;
    }
        
    //.
    // process Down1
    if(generalUtils.match(down1, "TOPPAGE", 7))
      down1Status[0] = COORDSDOWNTOPPAGE;
    else
    if(generalUtils.match(down1, "BOTTOMPAGE", 10))
      down1Status[0] = COORDSDOWNBOTTOMPAGE;
    else
    {
      if(down1[0] == '+')
      {
        x = 1;
	     down1Status[0]= COORDSDOWNRELATIVE;
      }
      else
      {
        x = 0;
	     down1Status[0] = COORDSDOWNABSOLUTE;
      }
      down1Coord[0] = generalUtils.doubleFromChars((byte[])down1, x);
    }
    //.
    // process Across1
    if(generalUtils.match(across1, "LEFTPAGE", 8))
      across1Status[0] = COORDSACROSSLEFTPAGE;
    else
    if(generalUtils.match(across1, "RIGHTPAGE", 9))
      across1Status[0] = COORDSACROSSRIGHTPAGE;
    else
    if(generalUtils.match(across1, "CENTPAGE", 8))
	   across1Status[0] = COORDSACROSSCENTPAGE;
    else
    if(generalUtils.match(across1, "LEFTOF", 6))
    {
      across1Coord[0] = generalUtils.doubleFromChars((byte[])across1CoordBytes);
      across1Status[0] = COORDSACROSSLEFTOF;
    }
    else
    if(generalUtils.match(across1, "RIGHTOF", 7))
    {
      across1Coord[0] = generalUtils.doubleFromChars((byte[])across1CoordBytes);
      across1Status[0] = COORDSACROSSRIGHTOF;
    }
    else
    if(generalUtils.match(across1, "CENTOF", 6))
    {
      across1Coord[0] = generalUtils.doubleFromChars((byte[])across1CoordBytes);
      across1Status[0] = COORDSACROSSCENTOF;
    }
    else
    {
      if(across1[0] == '+')
      {
	x = 1;
	across1Status[0] = COORDSACROSSRELATIVE;
      }
      else
      {
	x = 0;
	across1Status[0] = COORDSACROSSABSOLUTE;
      }
      
      across1Coord[0] = generalUtils.doubleFromChars((byte[])across1CoordBytes, x);
    }
      
    // process Down2
    if(down2Status[0] != COORDSNONE)
    {
      if(generalUtils.match(down2, "TOPPAGE", 7))
        down2Status[0] = COORDSDOWNTOPPAGE;
      else
      if(generalUtils.match(down2, "BOTTOMPAGE", 10))
	down2Status[0] = COORDSDOWNBOTTOMPAGE;
      else
      {
	if(down2[0] == '+')
	{
	  x = 1;
	  down2Status[0] = COORDSDOWNRELATIVE;
        }
        else
        {
	  x = 0;
	  down2Status[0] = COORDSDOWNABSOLUTE;
        }
	  
        down2Coord[0] = generalUtils.doubleFromChars((byte[])down2, x);
      }
    }
    else down2Coord[0] = 0.0;

    // process Across2
    if(across2Status[0] != COORDSNONE)
    {
      if(generalUtils.match(across2, "LEFTPAGE", 8))
        across2Status[0] = COORDSACROSSLEFTPAGE;
      else
      if(generalUtils.match(across2, "RIGHTPAGE", 9))
        across1Status[0] = COORDSACROSSRIGHTPAGE;
      else
      if(generalUtils.match(across2, "CENTPAGE", 8))
        across2Status[0] = COORDSACROSSCENTPAGE;
      else
      if(generalUtils.match(across2, "LEFTOF", 6))
      {
	across2Coord[0] = generalUtils.doubleFromChars((byte[])across2CoordBytes);
        across2Status[0] = COORDSACROSSLEFTOF;
      }
      else
      if(generalUtils.match(across2, "RIGHTOF", 7))
      {
	across2Coord[0] = generalUtils.doubleFromChars((byte[])across2CoordBytes);
	across2Status[0] = COORDSACROSSRIGHTOF;
      }
      else
      if(generalUtils.match(across2, "CENTOF", 6))
      {
	across2Coord[0] = generalUtils.doubleFromChars((byte[])across2CoordBytes);
  	across2Status[0] = COORDSACROSSCENTOF;
      }
      else
      {
	across2Coord[0] = generalUtils.doubleFromChars((byte[])across2CoordBytes);
        across2Status[0] = COORDSACROSSABSOLUTE;
      }
    }
    else across2Coord[0] = 0.0;

    return(down1Status[0]);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // itemNum origin-0
  private void getItem(byte[] b, short itemNum, byte[] item) throws Exception
  {
    short c;
    int   x, y;    
   
    x=0;  c=0;
    while(true)
    {
      while(b[x] == ' ') ++x; // leading spaces
      if(c == itemNum)
      {
        y=0;
        if(b[x] == '"')  // quoted string
        {
          item[y++] = '\"';
          ++x;
          while(b[x] != '"' && y < 150) // closing quote + jic
            item[y++] = b[x++];
          item[y++] = '\"';
          item[y] = '\000';
          return;
        }

        while(b[x] != ' ' && b[x] != '\000' && b[x] != ',')
	  item[y++] = b[x++];
        item[y] = '\000';
        return;
      }

      if(b[x] == '"')
      {
        ++x;
        while(b[x] != '"')  ++x;
        ++x;
      }
      else
      {
        while(b[x] != ' ' && b[x] != ',' && b[x] != '\000')  ++x;
        if(b[x] == ',')  ++x;
      }
      ++c;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private short numItemsInStr(byte[] b) throws Exception
  {
    short x, c;

    x=0;  c=0;
    while(b[x] != '\000')
    {
      while(b[x] == ' ')  ++x;
      if(b[x] != '\000')
      {
        ++c;
        if(b[x] == '"')
        {
          ++x;
          while(b[x] != '"')
            ++x;  
          ++x;
        }
        else
        {
	  while(b[x] != ' ' && b[x] != ',' && b[x] != '\000')
            ++x;
          if(b[x] == ',')
            ++x;
        }
      }
    }
    return c;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public short getNextLine(byte[] buf, short bufSize) throws Exception
  {
    int x=0, red=0;
    byte[] ch = new byte[1];

    for(x=0;x<bufSize;++x) buf[x] = ' ';
    
    x=0;

    long curr = fhPPR.getFilePointer();
    long high = fhPPR.length();

    fhPPR.seek(curr);

    if(curr == high)
      return -1;

    fhPPR.read(ch);
    buf[0]=ch[0];
    boolean atLeastOneChar = false;
    while(curr < high && x < (bufSize - 1))
    {
      if(buf[x] == (byte)10 || buf[x] == (byte)13 || buf[x] == (byte)26)
      {
        while(buf[x] == (byte)10 || buf[x] == (byte)13 || buf[x] == (byte)26)
        {
	  red = fhPPR.read(ch);
          ++curr;
          if(red == -1)
            break;
          else buf[x] = ch[0];
        }

        if(buf[x] == (byte)26)
          ;//	--x;
        else
	if(red != -1)
	  fhPPR.seek(fhPPR.getFilePointer() - 1);

        if(atLeastOneChar)
        {
          buf[x] = '\000';         
          return 0;
        }
      }
      else atLeastOneChar = true;

      ++x;
      fhPPR.read(ch);
      buf[x] = ch[0];
      ++curr;
    }

    // remove trailing spaces
    x = bufSize - 1;
    while(buf[x] == ' ')
      --x;

    buf[++x] = '\000';

    return 0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void processSection(String sectionName, byte[] rec, byte[]fldNames, short numFlds, String dbName, String uName, String localDefnsDir,
                             String defnsDir) throws Exception
  {
    processSection(sectionName, "PH", "PF", rec, fldNames, numFlds, dbName, uName, localDefnsDir, defnsDir);
  }
  public void processSection(String sectionName, String pageHeaderSection, String pageFooterSection, byte[] rec, byte[]fldNames, short numFlds,
                             String dbName, String uName, String localDefnsDir, String defnsDir) throws Exception
  {
    short x, quit;
    short[] down1Status   = new short[1];
    short[] across1Status = new short[1];
    short[] down2Status   = new short[1];
    short[] across2Status = new short[1];
    double[] down1Coord   = new double[1];
    double[] across1Coord = new double[1];

    byte[] opStr = new byte[800];
    byte[] line  = new byte[800];
    byte[] b     = new byte[20];
    char[] ch    = new char[1];
    char just;

    if(sectionName.equalsIgnoreCase("SUMMARY") || sectionName.equalsIgnoreCase("FULL"))
    {
      if(gotoSection(sectionName) == 0)
      {
        outputLine('X', pageHeaderSection, pageFooterSection, null, null, null, (short)0, ' ', dbName, uName, localDefnsDir, defnsDir);
        quit = 0;
        while(quit == 0)
        {
          if(getNextLine(line, (short)500) == -1)
            quit = 1;
          else
          if(isASection(line))
            quit = 1;
          else
          {
            if(generalUtils.match(line, "ENDRECORD", 9))
              outputLine('X', pageHeaderSection, pageFooterSection, null, null, null, (short)0, ' ', dbName, uName, localDefnsDir, defnsDir);
            else
	    {
              if(getTextFromLine(line, opStr, rec, fldNames, numFlds, dbName, uName, localDefnsDir, defnsDir))
                outputLine('Y', pageHeaderSection, pageFooterSection, opStr, rec, fldNames, numFlds, ' ', dbName, uName, localDefnsDir, defnsDir);
            }
          }
        }
      }
    }
    else // not an Export defn
    {
      if(gotoSection(sectionName) == 0)
      {
        //fCurrDown = fCurrAcross = 0.0;
        quit = 0;
        while(quit == 0)
        {
          if(getNextLine(line, (short)500) == -1)
            quit = 1;
          else
            if(isASection(line))
              quit = 1;
            else
            {
              if(generalUtils.match(line, "NEWPAGE", 7))
              {
                outputLine('E', pageHeaderSection, pageFooterSection, null, rec, fldNames, (short)100, /* plenty */ ' ', dbName, uName,
                           localDefnsDir, defnsDir);
              }
              else
              if((x = chkLineForShape(line, ch)) != SHAPENONE)
              {
                if(getCoordsFromLine(line, down1Coord, across1Coord, down1Status, across1Status, down2Coord, across2Coord, down2Status,
                                     (short[])across2Status) != COORDSNONE)
                  {
                    translateCoordsToPosn(down1Coord, across1Coord, (short)down1Status[0],  (short)across1Status[0], (short)down2Status[0],
                                          (short)across2Status[0]);
                  }
                  generalUtils.intToBytesCharFormat(x, (byte[])b, (short)0);
                  outputLine(ch[0], pageHeaderSection, pageFooterSection, b, (byte[])null, (byte[])null, (short)0, ' ', dbName, uName,
                             localDefnsDir, defnsDir);
	        }
                else
                if(chkLineForImage(line))
                {
                  if(getTextFromLine(line, opStr, rec, fldNames, numFlds, dbName, uName, localDefnsDir, defnsDir))
                  {
                    if(getCoordsFromLine(line, down1Coord, across1Coord, down1Status, across1Status, down2Coord, across2Coord, down2Status, across2Status) != COORDSNONE)
                    {
                      translateCoordsToPosn(down1Coord, across1Coord, down1Status[0], across1Status[0], down2Status[0], across2Status[0]);
                    }
                    outputLine('I', pageHeaderSection, pageFooterSection, opStr, rec, fldNames, numFlds, ' ', dbName, uName, localDefnsDir, defnsDir);
                  }
	        }
                else // not shape
                {
                  if((x = getFontFromLine(line)) != FONTNONE)
                    currFont = x;
                  
                  if(getTextFromLine(line, opStr, rec, fldNames, (short)numFlds, dbName, uName, localDefnsDir, defnsDir))
                  {
                    if(getCoordsFromLine(line, down1Coord, across1Coord, down1Status, across1Status, down2Coord, across2Coord, down2Status,
                                         across2Status) != COORDSNONE)
                    {
                      translateCoordsToPosn(down1Coord, across1Coord, (short)down1Status[0], (short)across1Status[0], (short)down2Status[0], (short)across2Status[0]);
                    }

                    if(currDown > (pageSizeLength - bm))
                    {
                      outputLine('E', pageHeaderSection, pageFooterSection, null, rec, fldNames, (short)100, /* plenty */ ' ', 
                                 dbName, uName, localDefnsDir, defnsDir);
                      
                      if(getCoordsFromLine(line, down1Coord, across1Coord, down1Status, across1Status, down2Coord, across2Coord, down2Status, across2Status) != COORDSNONE)
                      {
                        translateCoordsToPosn(down1Coord, across1Coord, down1Status[0], across1Status[0], down2Status[0], across2Status[0]);
                      }
       	            }

                    if(across1Status[0] == COORDSACROSSLEFTOF)
                      just = 'R';
                    else just = ' ';

                    outputLine('D', pageHeaderSection, pageFooterSection, opStr, rec, fldNames, (short)numFlds, just, dbName, uName, localDefnsDir, defnsDir);
                  }
     	        }
             }
         }
      }
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void translateCoordsToPosn(double[] down1Coord, double[] across1Coord, short down1Status, short across1Status, short down2Status, short across2Status) throws Exception
  {
    switch(down1Status)
    {
      case COORDSNONE           : break;
      case COORDSDOWNABSOLUTE   : currDown = down1Coord[0] + tm;          break;
      case COORDSDOWNTOPPAGE    : currDown = tm;                          break;
      case COORDSDOWNBOTTOMPAGE : currDown = bm - heightOfFont(currFont); break;
      case COORDSDOWNRELATIVE   : currDown += down1Coord[0];              break;
    }

    switch(across1Status)
    {
      case COORDSNONE            : break;
      case COORDSACROSSABSOLUTE  : currAcross = across1Coord[0] + lm; break;
      case COORDSACROSSLEFTPAGE  : currAcross = lm;                   break;
      case COORDSACROSSRIGHTPAGE : currAcross = rm;                   break;
      case COORDSACROSSCENTPAGE  : //???fPageCentre = fLM + (double)((PageSize.Width - (fRM + fLM)) / 2.0);
			                          //	 fTextWidth =
			                          // (double)WidthText((int)iCurrFont, (LPSTR)szOpStr);
			                          //	 fCurrAcross = (double)(fPageCentre
			                          //			               - (fTextWidth / 2.0))
                                                  break;
      case COORDSACROSSLEFTOF    : //fTextWidth = (double)WidthText((int)iCurrFont,
			                          currAcross = across1Coord[0];
			                          //				   (LPSTR)szOpStr);
			                          //fCurrAcross = (double)(*fAcross1Coord - fTextWidth);
                                   //{char s[30];sprintf(s,"%.2f",*fAcross1Coord);MessageBox(0,s,"",16);}
			                          break;
      case COORDSACROSSRIGHTOF   : currAcross = across1Coord[0];
			                          break;
      case COORDSACROSSCENTOF    : //fTextWidth = (double)WidthText((int)iCurrFont, (LPSTR)szOpStr);
			                          //fCurrAcross = (double)(*fAcross1Coord - (fTextWidth / 2.0));
			                          break;
      case COORDSACROSSRELATIVE  : currAcross += across1Coord[0];     break;
    }

    switch(down2Status)
    {
      case COORDSNONE           : break;
      case COORDSDOWNABSOLUTE   : down2Coord[0] += tm;                         break;
      case COORDSDOWNTOPPAGE    : down2Coord[0] = tm;                          break;
      case COORDSDOWNBOTTOMPAGE : down2Coord[0] = bm - heightOfFont(currFont); break;
      case COORDSDOWNRELATIVE   : down2Coord[0] = currDown;                    break;
    }

    switch(across2Status)
    {
      case COORDSNONE            : break;
      case COORDSACROSSABSOLUTE  : across2Coord[0] += lm; break;
      case COORDSACROSSLEFTPAGE  : across2Coord[0] = lm;  break;
      case COORDSACROSSRIGHTPAGE : across2Coord[0] = rm;  break;
      case COORDSACROSSCENTPAGE  :
      case COORDSACROSSLEFTOF    : // just-in-case
      case COORDSACROSSRIGHTOF   :
      case COORDSACROSSCENTOF    : across2Coord[0] = lm + ((pageSizeWidth - (rm + lm)) / 2.0); break;
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private short heightOfFont(short font) throws Exception
  {
    return(20); 
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Rtns: Width of text str for specified font in millimetres
  private double widthText(short font, String text) throws Exception
  {
    double textWidth;
    textWidth = 100; 
    return textWidth;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean getTextFromLine(byte[] line, byte[] opStr, byte[] rec, byte[] fldNames, short numFlds, String dbName, String uName, String localDefnsDir, String defnsDir) throws Exception
  {
    short fldNum;
    byte[] item = new byte[800];

    opStr[0] = '\"';
    opStr[1] = '\000';

    getItem(line, (short)0, item);
    if(item[0] == ';') return false; // comment

    short numItems = numItemsInStr(line);

    for(short x=0;x<numItems;++x)
    {
      getItem(line, x, item);
      if(item[0] == '\"')
      {
        generalUtils.bytesToBytes(opStr, item, 1);
        opStr[generalUtils.lengthBytes(opStr, 0) - 1] = '\000'; // zap ending "
      }
      else
      if((fldNum = isValidFldName(item, fldNames, numFlds)) != -1)
      {
        String s = generalUtils.dfsAsStr(rec, fldNum);
        generalUtils.catAsBytes(s, 0, opStr, false);
      }
      else
      if(generalUtils.match(item, "COMPANYNAME", 11))
      {
      }
      else
      if(generalUtils.match(item, "DATABASENAME", 12))
      {
        // fetch db desc given db name
        String companyName = directoryUtils.getAppConfigInfo('N', dbName); 
        generalUtils.catAsBytes(companyName, 0, opStr, false);
      }
      else
      if(generalUtils.match(item, "USER", 4))
      {
      }
      else
      if(generalUtils.match(item, "DATE", 4))
      {
        String s = generalUtils.today(localDefnsDir, defnsDir);
        generalUtils.catAsBytes(s, 0, opStr, false);
      }
      else
      if(generalUtils.match(item, "TIME", 4))
      {
        String s = generalUtils.timeNow(1, ":");
        generalUtils.catAsBytes(s, 0, opStr, false);
      }
      else
      if(generalUtils.match(item, "PAGE", 4))
      {
        String s = generalUtils.intToStr(currPage);
        generalUtils.catAsBytes(s, 0, opStr, false);
      }
      else
      if(generalUtils.match(item, "NUMPAGES", 8))
      {
        generalUtils.catAsBytes("NUMPAGES", 0, opStr, false);
      }
    }
    generalUtils.catAsBytes("\"", 0, opStr, false);

    return true;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private short chkLineForShape(byte[] line, char[] shape) throws Exception
  {
    byte[] item = new byte[800];

    short numItems = numItemsInStr(line);
    for(short x=0;x<numItems;++x)
    {
      getItem(line, x, item);
      if(generalUtils.match(item, "BOX", 3))
        shape[0] = 'B';
      else
      if(generalUtils.match(item, "BOXR", 4))
        shape[0] = 'R';
      else
      if(generalUtils.match(item, "LINE", 4))
        shape[0] = 'L';
      else shape[0] = ' ';

      if(shape[0] != ' ')
      {
        getItem(line, (short)(x + 1), item);
        x = (short)generalUtils.intFromBytesCharFormat(item, (short)0);
        if(x == 0) // no thickness, or illegal
          x = 1;
        return x;
      }
    }
    return SHAPENONE;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean chkLineForImage(byte[] line) throws Exception
  {
    byte[] item = new byte[800];

    short numItems = numItemsInStr(line);
    for(short x=0;x<numItems;++x)
    {
      getItem(line, x, item);
      if(generalUtils.match(item, "IMAGE", 5))
        return true;
    }
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void outputLine(char type, byte[] opStr, byte[] rec, byte[] fldNames, short numFlds, char just, String dbName, String uName, String localDefnsDir, String defnsDir) throws Exception
  {
    outputLine(type, "PH", "PF", opStr, rec, fldNames, numFlds, just, dbName, uName, localDefnsDir, defnsDir);
  }
  public void outputLine(char type, String pageHeaderSection, String pageFooterSection, byte[] opStr, byte[] rec, byte[] fldNames, short numFlds,
                         char just, String dbName, String uName, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] crlf = new byte[3];
    crlf[0] = 13;
    crlf[1] = 10;
    crlf[2] = 0;
    String s;
    long save;
    short currFontSave;

    switch(type)
    {
      case 'E' : // eop marker
                 ++currPage;
                 save = fhPPR.getFilePointer();
                 currFontSave = currFont;
                 processSection(pageFooterSection, rec, fldNames, (short)numFlds, dbName, uName, localDefnsDir, defnsDir);
                 addToOBuf(fhO, "E:");
                 addToOBuf(fhO, crlf, 2);
                 currDown = currAcross = 0.0;
                 lastOperationPF = true;
                 processSection(pageHeaderSection, rec, fldNames, (short)numFlds, dbName, uName, localDefnsDir, defnsDir);
                 fhPPR.seek(save);
                 currFont = currFontSave;
 	               break;
      case 'G' : // eop marker but doing PH and PF before page throw
                 ++currPage;
                 save = fhPPR.getFilePointer();
                 currFontSave = currFont;
                 processSection(pageFooterSection, rec, fldNames, (short)numFlds, dbName, uName, localDefnsDir, defnsDir);
                 processSection(pageHeaderSection, rec, fldNames, (short)numFlds, dbName, uName, localDefnsDir, defnsDir);
                 addToOBuf(fhO, "E:");
                 addToOBuf(fhO, crlf, 2);
                 currDown = currAcross = 0.0;
                 lastOperationPF = true;
                 fhPPR.seek(save);
                 currFont = currFontSave;
 	               break;
      case 'F' : // eop marker but no new page header
                 ++currPage;
                 save = fhPPR.getFilePointer();
                 currFontSave = currFont;
                 processSection(pageFooterSection, rec, fldNames, (short)numFlds, dbName, uName, localDefnsDir, defnsDir);
                 addToOBuf(fhO, "E:");
                 addToOBuf(fhO, crlf, 2);
                 currDown = currAcross = 0.0;
                 lastOperationPF = true;
                 fhPPR.seek(save);
                 currFont = currFontSave;
   	             break;
      case 'D' : // data
                 s = "D:" + currDown + "," + currAcross;
                 addToOBuf(fhO, (String)s);

                 // D:8.0,0.0,M,B, ,12, "Date : 27.12.98"
                 if(currFont != 0) // not a user-specifed font
                 {
                   s = "," + fontDefType[currFont] + "," + fontDefStyle[currFont] + "," + fontDefItalic[currFont] + "," + fontDefSize[currFont] + ","
                     + just + ",";
                 }
                 else // user-specified
                 {
                   s = "," + userFontName + "," + userFontStyle + "," + userFontItalic + "," + userFontSize + "," + just + ",";
                 }

                 addToOBuf(fhO, (String)s);
                 addToOBuf(fhO, (byte[])opStr, (short)generalUtils.lengthBytes(opStr,0));
	               addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'L' : // line
                 // L:100.0,115.0,100.0,133.0, ,49
                 s = "L:" + currDown + "," + currAcross + "," + down2Coord[0] + "," + across2Coord[0] + "," + just + ",";
                 addToOBuf(fhO, (String)s);
                 addToOBuf(fhO, (byte[])opStr, (short)generalUtils.lengthBytes(opStr,0));
	         addToOBuf(fhO, (byte[])crlf, 2);
	         break;
      case 'B' : // box
	         s = "B:" + currDown + "," + currAcross + "," + down2Coord[0] + "," + across2Coord[0] + "," + just + ",";
                 addToOBuf(fhO, (String)s);
                 addToOBuf(fhO, (byte[])opStr, (short)generalUtils.lengthBytes(opStr,0));
	         addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'R' : // rounded box
	         s = "B:" + currDown + "," + currAcross + "," + down2Coord[0] + "," + across2Coord[0] + "," + just + ",";
                 addToOBuf(fhO, (String)s);
                 addToOBuf(fhO, (byte[])opStr, (short)generalUtils.lengthBytes(opStr,0));
	         addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'S' : // PageSize
                 addToOBuf(fhO, (String)"S:");
                 addToOBuf(fhO, (byte[])opStr, generalUtils.lengthBytes(opStr, 0));
                 addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'I' : // image
	         s = "I:" + currDown + "," + currAcross + ",";
                 addToOBuf(fhO, (String)s);
                 addToOBuf(fhO, (byte[])opStr, (short)generalUtils.lengthBytes(opStr,0));
       	         addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'M' : // message
                 addToOBuf(fhO, (byte[])opStr, (int)generalUtils.lengthBytes(opStr, 0));
                 addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'T' : // type
                 addToOBuf(fhO, (byte[])opStr, (int)generalUtils.lengthBytes(opStr, 0));
                 addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'X' : // Export
                 addToOBuf(fhO, (String)"X:");
                 addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'Y' : // export data
                 addToOBuf(fhO, (byte[])opStr, (int)generalUtils.lengthBytes(opStr, 0));
                 addToOBuf(fhO, (byte[])crlf, 2);
                 break;
      case 'Z' : // EOReport
                 addToOBuf(fhO, (String)"Z:");
                 addToOBuf(fhO, (byte[])crlf, 2);
                 break;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private short isValidFldName(byte[] item, byte[] fldNames, short numFlds)
  {
    try
    {
      byte[] thisOne = new byte[100];

      short x=0;
      while(x < numFlds)
      {
        generalUtils.dfs((byte[])fldNames, (short)x, (byte[])thisOne);
        if(generalUtils.match((char)'=', (byte[])item, (int)0, (byte[])thisOne, (int)0))
          return(x);
        ++x;
      }

      return -1;
    }
    catch(Exception e)
    {
      return -1;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void createTempFile(String fileName, String workingDir)
  {
    RandomAccessFile fh = (RandomAccessFile)generalUtils.create(workingDir + fileName);
    generalUtils.fileClose((RandomAccessFile)fh);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Rtns the number of GR items in arg; sets set to number which are set.
  // String arg = "041 G 042 R 043 G"
  // Use where the GR item has no spaces
  public short countGRs(String arg, short[] set) throws Exception
  {
    int len = arg.length();
    short count=0;
    set[0]=0;
    int x=0;
    while(x < len && arg.charAt(x) != '\001')
    {
      while(x < len && arg.charAt(x) != ' ')
        ++x;
      ++x;
      ++count;
      if(arg.charAt(x) == 'G')
        ++set[0];

      ++x;
      if(x < len)
        ++x;
    }
    return count;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Rtns true if 'which' is set.
  // String arg = "041 G 042 R 043 G"
  // Use where the GR item has no spaces
  public boolean isGRSet(String arg, short which) throws Exception
  {
    int len = arg.length();
    short count=0;
    int x=0;
    while(x < len)
    {
      while(arg.charAt(x) != ' ')
        ++x;
      ++x;

      if(count == which)
      {
        if(arg.charAt(x) == 'G')
          return true;
        return false;
      }
      ++count;

      ++x;
      if(x < len)
        ++x;
    }
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // String arg = "041 G 042 R 043 G"
  // Use where the GR item has no spaces
  public boolean isGRSet(String arg, String item) throws Exception
  {
    int len = arg.length();
    String thisItem;
    int x=0;
    while(x < len)
    {
      thisItem="";
      while(arg.charAt(x) != ' ')
        thisItem += arg.charAt(x++);
      ++x;

      if(item.equalsIgnoreCase(thisItem))
      {
        if(arg.charAt(x) == 'G')
          return true;
        return false;
      }

      ++x;
      if(x < len)
        ++x;
    }
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // String arg = "041 G 042 R 043 G"
  // Use where the GR item has no spaces
  public String getGRItem(String arg, short which) throws Exception
  {
    int len = arg.length();
    short count=0;
    String item="";
    int x=0;
    while(x < len)
    {
      if(count == which)
      {
        while(arg.charAt(x) != ' ')
          item += arg.charAt(x++);
        return item;
      }

      while(arg.charAt(x) != ' ')
        ++x;
      ++x;
      ++count;
      ++x;
      if(x < len)
        ++x;
    }
    return "";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Rtns the number of GR items in arg; sets set to number which are set.
  // String arg   = "Clasen Tan G Stephen Lim R "
  // originalList = "Clasen Tan\002Stephen Lim\002"
  // Use where the GR item may have spaces; and so is matched one-for-one against a \002-separated 'original' list
  public short countGRs(String arg, String originalList, short[] set) throws Exception
  {
    int len = arg.length();
    short count=0;
    set[0]=0;
    int x=0;
    int y=0;
    while(x < len)
    {
      while(originalList.charAt(y) != '\002')
      {
        ++y;
        ++x;
      }
      ++y;
      ++x;
      ++count;

      if(arg.charAt(x) == 'G')
        ++set[0];

      if(x < len)
        x += 2;
    }
    return count;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Rtns true if 'which' is set.
  // String arg   = "Clasen Tan G Stephen Lim R "
  // originalList = "Clasen Tan\002Stephen Lim\002"
  // Use where the GR item may have spaces; and so is matched one-for-one against a
  // \002-separated 'original' list
  public boolean isGRSet(String arg, String originalList, short which) throws Exception
  {
    int len = arg.length();
    short count=0;
    int x=0;
    int y=0;
    while(x < len)
    {
      while(originalList.charAt(y) != '\002')
      {
        ++y;
        ++x;
      }
      ++y;
      ++x;

      if(count == which)
      {
        if(arg.charAt(x) == 'G')
          return true;
        return false;
      }
      ++count;

      if(x < len)
        x += 2;
    }
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Rtns true if 'which' is set.
  // String arg   = "Clasen Tan G Stephen Lim R "
  // originalList = "Clasen Tan\002Stephen Lim\002"
  // Use where the GR item may have spaces; and so is matched one-for-one against a \002-separated 'original' list
  public boolean isGRSet(byte[] arg, String originalList, String item) throws Exception
  {
    return isGRSet(generalUtils.stringFromBytes(arg, (long)0), originalList, item);
  }
  public boolean isGRSet(String arg, String originalList, byte[] item) throws Exception
  {
    return isGRSet(arg, originalList, generalUtils.stringFromBytes(item, (long)0));
  }
  public boolean isGRSet(String arg, String originalList, String item) throws Exception
  {
    int len = arg.length();
    String thisItem;
    int x=0;
    int y=0;
    while(x < len)
    {
      thisItem="";
      while(originalList.charAt(y) != '\002')
      {
        thisItem += originalList.charAt(y++);
        ++x;
      }
      ++x;
      ++y;

      if(item.equalsIgnoreCase(thisItem))
      {
        if(arg.charAt(x) == 'G')
          return true;
        return false;
      }

      if(x < len)
        x += 2;
    }
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // String arg   = "Clasen Tan G Stephen Lim R "
  // originalList = "Clasen Tan\002Stephen Lim\002"
  // Use where the GR item may have spaces; and so is matched one-for-one against a \002-separated 'original' list
  public String getGRItem(String arg, String originalList, short which) throws Exception
  {
    int len = arg.length();
    short count=0;
    String item="";
    int x=0;
    int y=0;
    while(x < len)
    {
      if(count == which)
      {
        while(originalList.charAt(y) != '\002')
          item += originalList.charAt(y++);
        return item;
      }

      while(originalList.charAt(y) != '\002')
      {
        ++x;
        ++y;
      }
      ++y;
      ++x;

      ++count;
      if(x < len)
        x += 2;
    }
    return "";
  }

}
