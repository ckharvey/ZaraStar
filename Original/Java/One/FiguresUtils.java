// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Convert figures to words
// Module: FiguresUtils.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

public class FiguresUtils
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // sets word strs for each line separated by '\000'
  // returns # of lines created
  public int wordsFromFigures(double value, int maximumCharactersPerLine, boolean includeTrailingAsterisks, byte[] words) throws Exception
  {
    char trailingChar;

    words[0] = '\000';

    if(value < 0) // quick fix. A -ve value causes an arrayboundsexeception somewhere. FIXME (maybe)
      return 0;

    doIt(value, words);

    if(includeTrailingAsterisks)
      trailingChar = '*';
    else trailingChar = ' ';

    return breakLine(words, maximumCharactersPerLine, trailingChar);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int breakLine(byte[] words, int maximumCharactersPerLine, char trailingChar) throws Exception
  {
    int  x, y, i;
    byte[] b = new byte[500];

    generalUtils.bytesToBytes(b, 0, words, 0);
    x = y = 0; i = 1;
    while(b[x] != '\000')
    {
      if(y == maximumCharactersPerLine)
      {
        while(words[x] != ' ')
          --x;
        words[x++] = '\000';
        y=0;
        ++i;
      }
      else ++y;

      words[x] = b[x];
      ++x;
    }

    while(y < maximumCharactersPerLine)
    {
      words[x++] = (byte)trailingChar;
      ++y;
    }
    words[x] = '\000';

    return i;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(double value, byte[] words) throws Exception
  {
    byte[] cents = new byte[10];
    byte[] b     = new byte[100];
    boolean dollars;

    generalUtils.doubleToBytesCharFormat(value, b, 0);

    int x=0;
    while(b[x] == '0')
      ++x;

    if(b[x] == '.') // no dollars
      dollars = false;
    else dollars = true;

    // Copies b2 to b1 (b2 must be null'd)
    x = generalUtils.lengthBytes(b, 0) - 3;
    generalUtils.bytesToBytes(cents, 0, b, x);
    b[x] = '\000';

    if(! dollars)
    {
      value = generalUtils.doubleFromBytes(cents, 1L);
      generalUtils.catAsBytes("Nil and ", 0, words, false);

      process(value, cents, 1, words);
      if(value == 1)
        generalUtils.catAsBytes(" Cent", 0, words, false);
      else generalUtils.catAsBytes(" Cents", 0, words, false);
    }
    else
    {
      process(value, b, 0, words);
      addCents(cents, words);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void process(double value, byte[] b, int start, byte[] words) throws Exception
  {
    int x=start;

    while(b[x] == '0')
      ++x;
    if(value < 20) processUnits(b, x, words, false);
    else
    {
      if(value < 100)
      {
        byte[] b1 = new byte[2];
        b1[0] = '-'; b1[1] = '\000';
        processTens(b, x, words, b1, false);
      }
      else
      if(value < 1000) processHundreds(b, x, words);
      else
      if(value < 1000000.00) processThousands(b, x, words);
      else processMillions(b, x, words);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean processUnits(byte[] value, int start, byte[] words, boolean actualUnits) throws Exception
  {
    boolean rtn = true;
    switch(generalUtils.intFromBytesCharFormat(value, (short)start))
    {
      case  0 : if(actualUnits)generalUtils.catAsBytes("Nil", 0, words, false); else rtn = false; break;
      case  1 : generalUtils.catAsBytes("One",       0, words, false); break;
      case  2 : generalUtils.catAsBytes("Two",       0, words, false); break;
      case  3 : generalUtils.catAsBytes("Three",     0, words, false); break;
      case  4 : generalUtils.catAsBytes("Four",      0, words, false); break;
      case  5 : generalUtils.catAsBytes("Five",      0, words, false); break;
      case  6 : generalUtils.catAsBytes("Six",       0, words, false); break;
      case  7 : generalUtils.catAsBytes("Seven",     0, words, false); break;
      case  8 : generalUtils.catAsBytes("Eight",     0, words, false); break;
      case  9 : generalUtils.catAsBytes("Nine",      0, words, false); break;
      case 10 : generalUtils.catAsBytes("Ten",       0, words, false); break;
      case 11 : generalUtils.catAsBytes("Eleven",    0, words, false); break;
      case 12 : generalUtils.catAsBytes("Twelve",    0, words, false); break;
      case 13 : generalUtils.catAsBytes("Thirteen",  0, words, false); break;
      case 14 : generalUtils.catAsBytes("Fourteen",  0, words, false); break;
      case 15 : generalUtils.catAsBytes("Fifteen",   0, words, false); break;
      case 16 : generalUtils.catAsBytes("Sixteen",   0, words, false); break;
      case 17 : generalUtils.catAsBytes("Seventeen", 0, words, false); break;
      case 18 : generalUtils.catAsBytes("Eighteen",  0, words, false); break;
      case 19 : generalUtils.catAsBytes("Nineteen",  0, words, false); break;
      default : rtn = false;
    }
    return rtn;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processTens(byte[] value, int start, byte[] words, byte[] character, boolean actualUnits) throws Exception
  {
    byte[] b1 = new byte[20];

    if(generalUtils.lengthBytes(value, start) <= 2)
    {
      generalUtils.bytesToBytes(b1, 0, value, start);
      b1[1] = '\000';

      boolean none = false;
      switch(generalUtils.intFromBytesCharFormat(b1, (short)0))
      {
        case 2 : generalUtils.catAsBytes("Twenty",  0, words, false);  break;
        case 3 : generalUtils.catAsBytes("Thirty",  0, words, false);  break;
        case 4 : generalUtils.catAsBytes("Forty",   0, words, false);  break;
        case 5 : generalUtils.catAsBytes("Fifty",   0, words, false);  break;
        case 6 : generalUtils.catAsBytes("Sixty",   0, words, false);  break;
        case 7 : generalUtils.catAsBytes("Seventy", 0, words, false);  break;
        case 8 : generalUtils.catAsBytes("Eighty",  0, words, false);  break;
        case 9 : generalUtils.catAsBytes("Ninety",  0, words, false);  break;
        default : none = true; break;
      }

      if(! none)
        generalUtils.bytesToBytes(words, generalUtils.lengthBytes(words, 0), character, 0);
      if(value[start] == '0' || value[start] == '1')
        processUnits(value, start, words, actualUnits);
      else
        if(value[start + 1] != '0')
        processUnits(value, start + 1, words, actualUnits);
    }
    else processUnits(value, start, words, actualUnits);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processHundreds(byte[] value, int start, byte[] words) throws Exception
  {
    byte[] b1 = new byte[20];

    if(value[0] != '0')
    {
      generalUtils.bytesToBytes(b1, 0, value, start);
      b1[1] = '\000';
      if(processUnits(b1, 0, words, false))
      {
        generalUtils.catAsBytes(" Hundred", 0, words, false);

        if(generalUtils.intFromBytesCharFormat(value, (short)(start + 1)) != 0)
          generalUtils.catAsBytes(" and ", 0, words, false);
      }
    }
    if(generalUtils.intFromBytesCharFormat(value, (short)(start + 1)) < 20)
      b1[0] = '\000';
    else
    {
      b1[0] = '-'; b1[1] = '\000';
    }

    processTens(value, start + 1, words, b1, false);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processThousands(byte[] value, int start, byte[] words) throws Exception
  {
    int x;

    byte[] b1 = new byte[20];
    byte[] b2 = new byte[10];

    generalUtils.bytesToBytes(b1, 0, value, start);

    int len = generalUtils.lengthBytes(b1, 0);

    if(len == 4)
    {
      x = 1;
      b1[1] = '\000';
      processUnits(b1, 0, words, false);
    }
    else
    if(len == 5)
    {
      x = 2;
      b1[2] = '\000';
      if(generalUtils.intFromBytesCharFormat(b1, (short)0) <= 20 || b1[1] == '0')
        b2[0] = '\000';
      else
      {
        b2[0] = '-'; b2[1] = '\000';
      }

      processTens(b1, 0, words, b2, false);
    }
    else
    {
      x = 3;
      b1[3] = '\000';
      processHundreds(b1, 0, words);
    }

    generalUtils.catAsBytes(" Thousand", 0, words, false);
    int y = generalUtils.intFromBytesCharFormat(value, (short)(start + x));
    if(y > 0 && y <= 99)
      generalUtils.catAsBytes(" and ", 0, words, false);
    else generalUtils.catAsBytes(" ", 0, words, false);

    processHundreds(value, (start + x), words);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processMillions(byte[] value, int start, byte[] words) throws Exception
  {
    byte[] b1 = new byte[20];
    byte[] b2 = new byte[2];

    generalUtils.bytesToBytes(b1, 0, value, start);

    int len = generalUtils.lengthBytes(b1, 0);
    if(len == 7)
    {
      b1[1] = '\000';
      processUnits(b1, 0, words, false);
    }
    else
    {
      if(len == 8)
      {
        b1[2] = '\000';
        if(b1[1] == '0' || b1[0] == '1')
        {
          b2[0] = '\000';
          processTens(b1, 0, words, b2, false);
        }
        else
        {
          b2[0] = '-'; b2[1] = '\000';
          processTens(b1, 0, words, b2, false);
        }
      }
      else
      {
        b1[3] = '\000';
        processHundreds(b1, 0, words);
      }
    }

    generalUtils.catAsBytes(" Million ", 0, words, false);

    long l = generalUtils.longFromBytesCharFormat(value, (short)(len/*start */- 6));
    if(l > 0 && l <= 99)
      generalUtils.catAsBytes(" and ", 0, words, false);

    process((double)l, value, (start + len - 6), words);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void addCents(byte[] cents, byte[] words) throws Exception
  {
    int len = generalUtils.lengthBytes(words, 0);
    if(words[len - 1] != ' ')
    {
      generalUtils.catAsBytes(" ", 0, words, false);
      ++len;
    }

    generalUtils.catAsBytes("and Cents ", 0, words, false);
    len += 9;

    byte[] b1 = new byte[2];
    b1[0] = '-'; b1[1] = '\000';
    processTens(cents, 1, words, b1, true);

    len = generalUtils.lengthBytes(words, 0) - 1;
    if(words[len] == '-') // fix trailing '-' bug
      words[len] = '\000';

    generalUtils.catAsBytes(" Only", 0, words, false);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int maxCharsPerLine(String defnsDir, String zaraDefnsDir)
  {
    try
    {
      String s = generalUtils.getFromDefnFile("MAXCHARSPERLINE", "document.dfn", defnsDir, zaraDefnsDir);

      if(s.length() == 0)
        return 80;

      return generalUtils.intFromStr(s);
    }
    catch(Exception e) { return 80; }
  }

}
