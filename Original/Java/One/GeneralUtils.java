// =======================================================================================================================================================================================================
// System: ZaraStar Utils: General Utilities
// File:   GeneralUtils.java
// Author: C.K.Harvey
// Copyright (c) 1998-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================
 
package org.zarastar.zarastar;

import java.io.*;
import java.util.*;               
import java.math.BigInteger;
import java.math.BigDecimal;

// -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
public class GeneralUtils
{
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void dfs(byte[] ipBuf, short fld, byte[] opBuf)
  {
    try
    {
      short x=0;
      short y=0;
      while(x < fld)
      {
        while(ipBuf[y] != '\000')
          ++y;
        ++y;
        ++x;
      }
      x=0;
      while(ipBuf[y] != '\000')
        opBuf[x++] = ipBuf[y++];
      opBuf[x] = '\000';
      return;
    }
    catch(Exception e) { opBuf[0] = '\000'; }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String dfsGivenSeparator(boolean onlyValue, char separator, String ipStr, short fld)
  {
    byte[] opBuf = new byte[300]; // plenty

    dfsGivenSeparator(onlyValue, separator, ipStr, fld, opBuf);

    return stringFromBytes(opBuf, 0L);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String dfsGivenSeparator(boolean onlyValue, char separator, byte[] ipBuf, short fld)
  {
    byte[] opBuf = new byte[300]; // plenty

    dfsGivenSeparator(onlyValue, separator, ipBuf, fld, opBuf);
    
    return stringFromBytes(opBuf, 0L);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void dfsGivenSeparator(boolean onlyValue, char separator, String ipStr, short fld, byte[] opBuf)
  {
    int len = ipStr.length();
    byte[] ipBuf = new byte[len + 1];

    strToBytes(ipBuf, ipStr, 0, len);

    dfsGivenSeparator(onlyValue, separator, ipBuf, fld, opBuf);
  }
  public void dfsGivenSeparator(boolean onlyValue, char separator, byte[] ipBuf, short fld, byte[] opBuf)
  {
    try
    {
      if(separator == '\000')
      {
        dfs(ipBuf, fld, opBuf);
      }
      else
      if(separator == '\001')
      {
        short x=0;
        short y=0;
        while(ipBuf[y] != '\000' && x < fld)
        {
          while(ipBuf[y] != '\001')
            ++y;
          ++y;
          ++x;
        }

        if(ipBuf[y] == '\000')
        {
          opBuf[0] = '\000';
          return;
        }

        if(! onlyValue)
        {
          x=0;
          while(ipBuf[y] != '\001')
            opBuf[x++] = ipBuf[y++];
          opBuf[x] = '\000';
        }
        else
        {
          x=0;
          while(ipBuf[y] != '=' && ipBuf[y] != '\001') // just-in-case
            ++y;
          if(ipBuf[y] != '\001')
          {
            ++y;
            while(ipBuf[y] != '\001')
              opBuf[x++] = ipBuf[y++];
          }
          opBuf[x] = '\000';
        }

        return;
      }
      else 
      if(separator == ' ')
      {
        short x=0;
        short y=0;
        while(true)
        {
          if(x == fld)
          {
            if(! onlyValue)
            {
              x=0;
              while(ipBuf[y] != '\000' && ipBuf[y] != '"')
                opBuf[x++] = ipBuf[y++];
              if(ipBuf[y] != '\000')
              {
                opBuf[x++] = '"';
                ++y;
                while(ipBuf[y] != '\000' && ipBuf[y] != '"')
                  opBuf[x++] = ipBuf[y++];
                opBuf[x++] = '"';
              }
              opBuf[x] = '\000';
            }
            else
            {
              while(ipBuf[y] != '"') // opening quote
                ++y;
              ++y;

              x=0;
              while(ipBuf[y] != '"') // closing quote
                opBuf[x++] = ipBuf[y++];
              opBuf[x] = '\000';
            }
            return;
          }
          
          while(ipBuf[y] != '\000' && ipBuf[y] != '"') // opening quote
            ++y;
          if(ipBuf[y] != '\000')
          {
            ++y;
            while(ipBuf[y] != '"') // closing quote
              ++y;
            ++y;
          }
          ++y;
          ++x;
        }
      }
    }
    catch(Exception e) { opBuf[0] = '\000'; }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void repDoubleGivenSeparator(char dps, char separator, byte[] ipBuf, int bufLen, short fld, double newValue)
  {
    String newStr = doubleToStr(dps, newValue);
    repAlphaGivenSeparator(separator, ipBuf, bufLen, fld, newStr);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // works with separator of '\000' (whereupon a 'normal' repAlpha is done)
  // and a separator of ' ' (that is, ipBuf[] = 'abc="123" def="123456789" ghi="789", and
  // the "123456789" could be replaced by newStr = "123"
  public void repAlphaGivenSeparator(char separator, byte[] ipBuf, int bufLen, short fld, String newStr)
  {
    try
    {
      if(separator == '\000')
      {
        repAlpha(ipBuf, bufLen, fld, newStr);
        return;
      }
      else
      if(separator == '\001')
      {
        int x=0;
        int y=0;
        while(true)
        {
          while(ipBuf[y] != '=' && ipBuf[y] != '\001')
            ++y;
          if(ipBuf[y] != '\001')
            ++y;

          if(x == fld)
          {
            int lenNewStr = newStr.length();
            int lenOldStr=0;
            int z=y;
            while(ipBuf[z] != '\001')
            {
              ++lenOldStr;
              ++z;
            }
            if(lenOldStr == lenNewStr)
            {
              for(z=0;z<lenNewStr;++z)
              {
                ipBuf[y++] = (byte)newStr.charAt(z);
              }
              return;
            }
            else
            if(lenOldStr < lenNewStr) // newStr needs more room, so, shunt-up by the difference
            {
              int diff = lenNewStr - lenOldStr;
              for(z=bufLen-1;z>diff&&z>y;--z)
                ipBuf[z] = ipBuf[z-diff];
              for(z=0;z<lenNewStr;++z)
                ipBuf[y++] = (byte)newStr.charAt(z);
              return;
            }
            else // lenOldStr > lenNewStr
            {
              // newStr needs less room, so, shunt-down by the difference
              int diff = lenOldStr - lenNewStr;
              for(z=y;z<bufLen-diff;++z)
                ipBuf[z] = ipBuf[z+diff];
              for(z=0;z<lenNewStr;++z)
                ipBuf[y++] = (byte)newStr.charAt(z);
              return;
            }
          }
          ++x;

          while(ipBuf[y] != '\001')
            ++y;
          ++y;
        }
      }
      else
      if(separator == ' ')
      {
        int x=0;
        int y=0;
        while(true)
        {
          while(ipBuf[y] != '"') // opening quote
            ++y;
          ++y;

          if(x == fld)
          {
            int lenNewStr = newStr.length();
            int lenOldStr=0;
            int z=y;
            while(ipBuf[z] != '"') // closing quote
            {
              ++lenOldStr;
              ++z;
            }
            if(lenOldStr == lenNewStr)
            {
              for(z=0;z<lenNewStr;++z)
              {
                ipBuf[y++] = (byte)newStr.charAt(z);
              }
              return;
            }
            else
            if(lenOldStr < lenNewStr) // newStr needs more room, so, shunt-up by the difference
            {
              int diff = lenNewStr - lenOldStr;
              for(z=bufLen-1;z>diff&&z>y;--z)
                ipBuf[z] = ipBuf[z-diff];
              for(z=0;z<lenNewStr;++z)
                ipBuf[y++] = (byte)newStr.charAt(z);
              return;
            }
            else // lenOldStr > lenNewStr
            {
              // newStr needs less room, so, shunt-down by the difference
              int diff = lenOldStr - lenNewStr;
              for(z=y;z<bufLen-diff;++z)
                ipBuf[z] = ipBuf[z+diff];
              for(z=0;z<lenNewStr;++z)
                ipBuf[y++] = (byte)newStr.charAt(z);
              return;
            }
          }
          ++x;

          while(ipBuf[y] != '"') // closing quote
            ++y;
          ++y;
        }
      }
    }
    catch(Exception e) { }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // works with separator of '\000' (whereupon a 'normal' repAlpha is done)
  // and a separator of ' ' (that is, ipBuf[] = 'abc="123" def="123456789" ghi="789", and
  // the "123456789" could be replaced by newB = "123"
  public void repAlphaGivenSeparator(char separator, byte[] ipBuf, int bufLen, short fld, byte[] newB)
  {
    try
    {
      if(separator == '\000')
      {
        repAlpha(ipBuf, bufLen, fld, newB);
        return;
      }
      else
      if(separator == '\001')
      {
        int x=0;
        int y=0;
        while(true)
        {
          while(ipBuf[y] != '=' && ipBuf[y] != '\001')
            ++y;
          if(ipBuf[y] == '=')
            ++y;

          if(x == fld)
          {
            int lenNewB = lengthBytes(newB, 0);
            int lenOldStr=0;
            int z=y;
            while(ipBuf[z] != '\001')
            {
              ++lenOldStr;
              ++z;
            }

            if(lenOldStr == lenNewB)
            {
              for(z=0;z<lenNewB;++z)
                ipBuf[y++] = newB[z];
              return;
            }
            else
            if(lenOldStr < lenNewB) // newB needs more room, so, shunt-up by the difference
            {
              int diff = lenNewB - lenOldStr;
              for(z=bufLen-1;z>diff&&z>y;--z)
                ipBuf[z] = ipBuf[z-diff];
              for(z=0;z<lenNewB;++z)
                ipBuf[y++] = newB[z];
              return;
            }
            else // lenOldStr > lenNewB
            {
              // newB needs less room, so, shunt-down by the difference
              int diff = lenOldStr - lenNewB;
              for(z=y;z<bufLen-diff;++z)
                ipBuf[z] = ipBuf[z+diff];
              for(z=0;z<lenNewB;++z)
                ipBuf[y++] = newB[z];
              return;
            }
          }
          ++x;

          while(ipBuf[y] != '\001')
            ++y;
          ++y;
        }
      }
      else
      if(separator == ' ')
      {
        int x=0;
        int y=0;
        while(true)
        {
          while(ipBuf[y] != '"') // opening quote
             ++y;
          ++y;

          if(x == fld)
          {
            int lenNewB = lengthBytes(newB, 0);
            int lenOldStr=0; // does not include the two quotes
            int z=y;
            while(ipBuf[z] != '"') // closing quote
            {
              ++lenOldStr;
              ++z;
            }

            if(lenOldStr == lenNewB)
            {
              for(z=0;z<lenNewB;++z)
                ipBuf[y++] = newB[z];
              return;
            }
            else
            if(lenOldStr < lenNewB) // newB needs more room, so, shunt-up by the difference
            {
              int diff = lenNewB - lenOldStr;
              for(z=bufLen-1;z>diff&&z>y;--z)
                ipBuf[z] = ipBuf[z-diff];
              for(z=0;z<lenNewB;++z)
                ipBuf[y++] = newB[z];
              return;
            }
            else // lenOldStr > lenNewB
            {
              // newB needs less room, so, shunt-down by the difference
              int diff = lenOldStr - lenNewB;
              for(z=y;z<bufLen-diff;++z)
                ipBuf[z] = ipBuf[z+diff];
              for(z=0;z<lenNewB;++z)
                ipBuf[y++] = newB[z];
              return;
            }
          }
          ++x;

          while(ipBuf[y] != '"') // opening quote
            ++y;
          ++y;
        }
      }
    }
    catch(Exception e) { }

  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // works with separator of '\001' (that is, ipBuf[] = 'abc.xxx=123\001def.yyy=123456789\001ghi.zzz=789\001', and where the
  // "123456789" could be replaced by contents of newB, "123". (Where fldName is 'yyy').
  public void repAlphaUsingOnes(byte[] ipBuf, int bufLen, String fldName, byte[] newB)
  {
    repAlphaUsingOnes(ipBuf, bufLen, fldName, stringFromBytes(newB, 0L));
  }
  public void repAlphaUsingOnes(byte[] ipBuf, int bufLen, String fldName, String newB)
  {
    byte[] thisFldName = new byte[100];
    try
    {
      int a, x=0, y=0;
      while(true)
      {
        while(ipBuf[y] != '.')
          ++y;
        a=0;
        ++y;
        while(ipBuf[y] != '=') // start
          thisFldName[a++] = ipBuf[y++];
        thisFldName[a] = '\000';
        ++y;

        if(matchIgnoreCase(thisFldName, 0, fldName))
        {
          int lenNewB = newB.length();
          int lenOldStr=0; // does not include the two quotes
          int z=y;
          while(ipBuf[z] != '\001' && ipBuf[z] != '\000') // end
          {
            ++lenOldStr;
            ++z;
          }

          if(lenOldStr == lenNewB)
          {
            for(z=0;z<lenNewB;++z)
              ipBuf[y++] = (byte)newB.charAt(z);
            return;
          }
          else
          if(lenOldStr < lenNewB) // newB needs more room, so, shunt-up by the difference
          {
            int diff = lenNewB - lenOldStr;
            for(z=bufLen-1;z>diff&&z>y;--z)
              ipBuf[z] = ipBuf[z-diff];
            for(z=0;z<lenNewB;++z)
             ipBuf[y++] = (byte)newB.charAt(z);
           return;
          }
          else // lenOldStr > lenNewB
          {
            // newB needs less room, so, shunt-down by the difference
            int diff = lenOldStr - lenNewB;
            for(z=y;z<bufLen-diff;++z)
              ipBuf[z] = ipBuf[z+diff];
            for(z=0;z<lenNewB;++z)
              ipBuf[y++] = (byte)newB.charAt(z);
            return;
          }
        }
        ++x;

        while(ipBuf[y] != '\001') // start
          ++y;
        ++y;
      }
    }
    catch(Exception e) { System.out.println("generalUtils: ouch! " + e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // works with separator of '\001' (that is, ipBuf[] = 'abc.xxx=123\001def.yyy=123456789\001ghi.zzz=789\001'
  public void dfsAlphaUsingOnes(byte[] ipBuf, String fldName, byte[] value)
  {
    byte[] thisFldName = new byte[100];
    try
    {
      int a, x=0, y=0;
      while(true)
      {
        while(ipBuf[y] != '.')
          ++y;
        a=0;
        ++y;
        while(ipBuf[y] != '=') // start
          thisFldName[a++] = ipBuf[y++];
        thisFldName[a] = '\000';
        ++y;
        if(matchIgnoreCase(thisFldName, 0, fldName))
        {
          int z=0;
          while(ipBuf[y] != '\001' && ipBuf[y] != '\000') // end
            value[z++] = ipBuf[y++];
          value[z++] = (byte)'\000';
          return;
        }
        ++x;

        while(ipBuf[y] != '\001') // start
          ++y;
        ++y;
      }
    }
    catch(Exception e) { value[0] = '\000'; return; }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Assumes separator of ' '.
  // E.g., ipBuf[] = 'fred.abc="123" fred.def="12345" fred.ghi="789"
  // newB is 'jim' thence:
  // ipBuf[] = 'jim.abc="123" jim.def="12345" jim.ghi="789"
  public void repFileNameGivenSeparator(byte[] ipBuf, int bufLen, byte[] newB)
  {
    byte[] tmpBuf = new byte[bufLen];
    int x=0, y=0, i;

    while(ipBuf[x] != '\000')
    {
      i=0;
      while(newB[i] != '\000')
        tmpBuf[y++] = newB[i++];

      while(ipBuf[x] != '.')
        ++x;

      while(ipBuf[x] != '"')
        tmpBuf[y++] = ipBuf[x++];
      tmpBuf[y++] = '"';
      ++x;
      while(ipBuf[x] != '"')
        tmpBuf[y++] = ipBuf[x++];
      tmpBuf[y++] = '"';

      x += 2;
      tmpBuf[y++] = ' ';
    }
    tmpBuf[y] = '\000';

    x=0;
    while(tmpBuf[x] != '\000')
    {
      ipBuf[x] = tmpBuf[x];
      ++x;
    }
    ipBuf[x] = '\000';
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Assumes separator of ' '.
  // E.g., ipBuf[] = 'abc="123" def="12345" ghi="789"
  // fld is 1. newB[] is "6789". sep is "<br>" thence:
  // ipBuf[] = 'abc="123" def="12345<br>6789" ghi="789"
  public void appendAlpha(byte[] ipBuf, int bufLen, int fld, byte[] newB, String sep)
  {
    int x=0;
    int y=0;
    while(true)
    {
      while(ipBuf[y] != '"') // opening quote
         ++y;
      ++y;

      if(x == fld)
      {
        while(ipBuf[y] != '"') // closing quote
          ++y;

        // shunt-up by the length of newb + sep
        int lenNewB = lengthBytes(newB, 0);
        int sepLen  = sep.length();
        int more    = lenNewB + sepLen;
        int z;
        for(z=bufLen-1;(z-more)>y;--z)
          ipBuf[z] = ipBuf[z-more];
        for(z=0;z<sepLen;++z)
          ipBuf[y++] = (byte)sep.charAt(z);
        for(z=0;z<lenNewB;++z)
          ipBuf[y++] = newB[z];
        ipBuf[y] = '"';
        return;
      }
      ++x;

      while(ipBuf[y] != '"') // opening quote
        ++y;
      ++y;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // Assumes separator of '\001'.
  // E.g., ipBuf[] = 'abc=123\001def=12345\001ghi=789'
  // fld is 1. newB[] is '6789'. sep is '<br>' thence:
  // ipBuf[] = 'abc=123\001def=12345<br>6789\001ghi="89'
  public void appendAlphaGivenBinary1(byte[] ipBuf, int bufLen, int fld, byte[] newB, String sep)
  {
    int x=0;
    int y=0;

    while(true)
    {
      while(ipBuf[y] != '=' && ipBuf[y] != '\001')
         ++y;
      ++y;

      if(x == fld)
      {
        while(ipBuf[y] != '\001')
          ++y;

        // shunt-up by the length of newb + sep
        int lenNewB = lengthBytes(newB, 0);
        int sepLen  = sep.length();
        int more    = lenNewB + sepLen;
        
        int z;
        for(z=bufLen-1;(z-more)>y;--z) // make room
          ipBuf[z] = ipBuf[z-more];
        
        for(z=0;z<sepLen;++z)          // insert separator string
          ipBuf[y++] = (byte)sep.charAt(z);
        
        for(z=0;z<lenNewB;++z)         // insert new stuff 
          ipBuf[y++] = newB[z];
        ipBuf[y] = '\001';
        
        return;
      }
      ++x;

      if(ipBuf[y-1] == '=')
      {
        while(ipBuf[y] != '\001')
          ++y;
        ++y;
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public void putAlpha(byte[] buf, int bufLen, short posn, byte[] str)
  {
    int count, len;
    int   x, y;

    x=0;
    count=0;
    while(count < posn)
    {
      while(buf[x] != '\000')
        ++x;
      ++x;
      ++count;
    }

    len = lengthBytes(str, 0);
    for(y=(bufLen-1);y>=(x+len);--y)
      buf[y] = buf[y - len];

    for(y=0;y<len;++y)
      buf[x + y] = str[y];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public void repAlpha(byte[] buf, int bufLen, short posn, byte[] b)
  {
    delAlpha(buf, bufLen, posn);
    putAlpha(buf, bufLen, posn, b);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public void repAlpha(byte[] buf, int bufLen, short posn, String str)
  {
    delAlpha(buf, bufLen, posn);
    putAlpha(buf, bufLen, posn, str);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public void repAlpha(byte[] buf, int bufLen, short posn, int i)
  {
    delAlpha(buf, bufLen, posn);
    putAlpha(buf, bufLen, posn, intToStr(i));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public void repAlpha(byte[] buf, int bufLen, short posn, double d)
  {
    delAlpha(buf, bufLen, posn);
    putAlpha(buf, bufLen, posn, doubleToStr(d));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public void delAlpha(byte[] buf, int bufLen, short posn)
  {
    int count, len;
    int x, y;

    x=0;
    count=0;
    while(count < posn)
    {
      while(buf[x] != '\000')
        ++x;
      ++x;
      ++count;
    }

    len = lengthBytes(buf, x); // determine len of bit to del
    for(y=(x+len);y<bufLen;++y)
      buf[x++] = buf[y];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public void putAlpha(byte[] buf, int bufLen, short posn, String str)
  {
    int count, len;
    int   x, y;

    x=0;
    count=0;
    while(count < posn)
    {
      while(buf[x] != '\000')
        ++x;
      ++x;
      ++count;
    }

    len = str.length();
    for(y=(bufLen-1);y>=(x+len);--y)
      buf[y] = buf[y - len];

    for(y=0;y<len;++y)
      buf[x + y] = (byte)str.charAt(y);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String dfsAsStr(byte[] ipBuf, short fld)
  {
    try
    {
      if(ipBuf == null) return "";

      String str = "";
      short x=0;
      short y=0;
      while(x < fld)
      {
        while(ipBuf[y] != '\000')
          ++y;
        ++y;
        ++x;
      }

      while(ipBuf[y] != '\000')
        str += (char)ipBuf[y++];

      return str;
    }
    catch(Exception e)
    {
      return("");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String dfsAsStrGivenBinary2(byte[] ipBuf, int fld)
  {
    try
    {
      if(ipBuf == null) return "";

      String str = "";
      short x=0;
      short y=0;
      while(x < fld)
      {
        while(ipBuf[y] != '\002')
          ++y;
        ++y;
        ++x;
      }

      while(ipBuf[y] != '\002')
        str += (char)ipBuf[y++];

      return str;
    }
    catch(Exception e)
    {
      return "";
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String dfsAsStrGivenBinary1(byte[] ipBuf, short fld)
  {
    try
    {
      if(ipBuf == null) return "";

      String str = "";
      short x=0;
      short y=0;
      while(x < fld)
      {
        while(ipBuf[y] != '\001')
          ++y;
        ++y;
        ++x;
      }

      while(ipBuf[y] != '\001')
        str += (char)ipBuf[y++];

      return str;
    }
    catch(Exception e)
    {
      return "";
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void dfsGivenBinary1(boolean onlyValue, byte[] ipBuf, short fld, byte[] b)
  {
    strToBytes(b, dfsAsStrGivenBinary1(onlyValue, ipBuf, fld));
  }
  public String dfsAsStrGivenBinary1(boolean onlyValue, byte[] ipBuf, short fld)
  {
    try
    {
      String str = "";
      short x=0;
      short y=0;

      while(x < fld)
      {
        while(ipBuf[y] != '\001')
          ++y;
        ++y;
        ++x;
      }

      if(onlyValue)
      {
        while(ipBuf[y] != '=')
          ++y;
        ++y;

        while(ipBuf[y] != '\001')
          str += (char)ipBuf[y++];
      }
      else
      {
        while(ipBuf[y] != '=' && ipBuf[y] != '\001')
          str += (char)ipBuf[y++];
        if(ipBuf[y] != '\001')
        {
          while(ipBuf[y] != '\001')
            str += (char)ipBuf[y++];
        }
      }

      return str;
    }
    catch(Exception e)
    {
      return "";
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String dfsAsStrGivenSeparator(char separator, byte[] ipBuf, short fld)
  {
    return dfsAsStrGivenSeparator(false, separator, ipBuf, fld);
  }
  public String dfsAsStrGivenSeparator(boolean onlyValue, char separator, byte[] ipBuf, short fld)
  {
    try
    {
      String str = "";
      short x=0;
      short y=0;
      boolean quit, open;
      while(x < fld)
      {
        open = quit = false;
        while(! quit)
        {
          if(ipBuf[y] == '"')
          {
            if(open)
              open = false;
            else open = true;
          }

          if(ipBuf[y] == separator)
          {
            if(! open)
              quit = true;
            else ++y;
          }
          else ++y;
        }
        ++y;
        ++x;
      }

      if(onlyValue)
      {
        while(ipBuf[y] != '"')
          ++y;
        ++y;

        while(ipBuf[y] != '"')
          str += (char)ipBuf[y++];
      }
      else
      {
        while(ipBuf[y] != '"' && ipBuf[y] != separator)
          str += (char)ipBuf[y++];
        if(ipBuf[y] != separator)
        {
          ++y;
          while(ipBuf[y] != '"')
            str += (char)ipBuf[y++];
          str += '\"';
        }
      }

      return str;
    }
    catch(Exception e)
    {
      return("");
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String dfsAsStr(String ipStr, short fld)
  {
    String str = "";
    short x=0;
    short y=0;
    while(x < fld)
    {
      while(ipStr.charAt(y) != '\001')
        ++y;
      ++y;
      ++x;
    }

    while(ipStr.charAt(y) != '\001')
      str += ipStr.charAt(y++);

    return str;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int dfsAsInt(byte[] ipBuf, short fld)
  {
    return strToInt(dfsAsStr(ipBuf, fld));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int dfsAsIntGivenSeparator(char separator, byte[] ipBuf, short fld)
  {
    return strToInt(dfsAsStrGivenSeparator(false, separator, ipBuf, fld));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double dfsAsDouble(byte[] ipBuf, short fld)
  {
    return doubleFromStr(dfsAsStr(ipBuf, fld));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double dfsAsDoubleGivenSeparator(boolean onlyValue, char separator, byte[] ipBuf, short fld)
  {
    byte[] b = new byte[50];
    dfsGivenSeparator(onlyValue, separator, ipBuf, fld, b);
    return doubleFromBytesCharFormat(b, 0);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Assumes null-term'd bytes; in a char format (e.g., '1','2','3')
  public int intFromBytesCharFormat(byte[] b, short fromByte)
  {
    short x=fromByte;
    int y=0;

    while(b[x] != 0)
    {
      y *= 10;
      y += (b[x] - 48);
      ++x;
    }

    return y;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Appends a null
  public void intToBytesCharFormat(int value, byte[] b, short fromByte)
  {
    byte[] c;
    Integer in = new Integer(value);
    String s = in.toString();
    c = s.getBytes();
    int len=lengthBytes(c, 0);

    int x=0;
    while(x < len && c[x] != 0)
    {
      b[fromByte + x] = c[x];
      ++x;
    }
    b[fromByte + x] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String intToStr(int value)
  {
    Integer in = new Integer(value);
    return in.toString();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String longToStr(long value)
  {
    Long in = new Long(value);
    return in.toString();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int intFromBytes(byte[] b, long start)
  {
    try
    {
      byte[] bb = new byte[4];

      int from = (int)start;
      int y=3;
      for(int x=from;x<from+4;++x)
        bb[y--] = b[x];

      ByteArrayInputStream bais = new ByteArrayInputStream(bb);
      DataInputStream is = new DataInputStream(bais);
      int d = is.readInt();
      bais.close();
      is.close();
      return d;
    }
    catch(Exception ioErr)
    {
      return 0;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void intToBytes(int theInt, byte[] b, long start)
  {
    try
    {
      byte[] bb;

      ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
      DataOutputStream os = new DataOutputStream(baos);
      os.writeInt(theInt);
      bb = baos.toByteArray();

      int from = (int)start;
      int y=3;
      for(int x=from;x<from+4;++x)
        b[x] = bb[y--];

      baos.close();
      os.close();
    }
    catch(Exception ioErr) { }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public long longFromBytes(byte[] b, int start)
  {
    try
    {
      byte[] bb = new byte[8];

      int from = start;
      int y=7;
      for(int x=from;x<from+8;++x)
        bb[y--] = b[x];

      ByteArrayInputStream bais = new ByteArrayInputStream(bb);
      DataInputStream is = new DataInputStream(bais);
      long l = is.readLong();
      bais.close();
      is.close();
      return l;
    }
    catch(Exception ioErr)
    {
      return 0L;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Assumes null-term'd bytes; in a char format (e.g., '1','2','3')
  public long longFromBytesCharFormat(byte[] b, short fromByte)
  {
    short x=fromByte;
    long y=0;

    while(b[x] != 0)
    {
      y *= 10;
      y += (b[x] - 48);
      ++x;
    }

    return y;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public short shortFromBytes(byte[] b, long start)
  {
    try
    {
      byte[] bb = new byte[2];

      int from = (int)start;
      int y=1;
      for(int x=from;x<from+2;++x)
        bb[y--] = b[x];

      ByteArrayInputStream bais = new ByteArrayInputStream(bb);
      DataInputStream is = new DataInputStream(bais);
      short s = is.readShort();
      bais.close();
      is.close();
      return s;
    }
    catch(Exception ioErr)
    {
      return (short)0;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void shortToBytes(short theShort, byte[] b, long start)
  {
    try
    {
      byte[] bb;

      ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
      DataOutputStream os = new DataOutputStream(baos);
      os.writeShort(theShort);
      bb = baos.toByteArray();

      int from = (int)start;
      int y=1;
      for(int x=from;x<from+2;++x)
      {
        b[x] = bb[y--];
      }

      baos.close();
      os.close();
    }
    catch(Exception ioErr) { }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String stringFromBytes(byte[] b, long start)
  {
    String s = "";
    int x = (int)start;
    while(b[x] != '\000')
    {
      s += (char)b[x];
      ++x;
    }
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void stringToBytes(String str, int start, byte[] b)
  {
    int y=0;
    int x = start;
    int len = str.length();
    while(x < len)
    {
      b[y] = (byte)str.charAt(x);
      ++y;
      ++x;
    }
    b[y] = (byte)0;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int stringIntoBytes(String str, int start, byte[] b, int startBytes) throws Exception
  {
    int y = startBytes;
    int x = start;
    int len = str.length();
    while(x < len)
      b[y++] = (byte)str.charAt(x++);
    return y;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void catAsBytes(String str, char terminator, byte[] b) throws Exception
  {
    int x=0;
    while(str.charAt(x) != terminator)
    {
      b[x] = (byte)str.charAt(x);
      ++x;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void catAsBytes(String str, int start, byte[] b, boolean isFirst) throws Exception
  {
    int y;
    if(isFirst)
      y = 0;
    else y = lengthBytes(b, 0);

    int x = start;
    int len = str.length();
    while(x < len)
      b[y++] = (byte)str.charAt(x++);
    b[y] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Copies b2 to b1 (b2 must be null'd)
  public void bytesToBytes(byte[] b1, int start1, byte[] b2, int start2)
  {
    int x = start1;
    int y = start2;

    while(b2[y] != 0)
       b1[x++] = b2[y++];
    b1[x] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Copies b2 to b1 (given length of b2)
  public void bytesToBytes(byte[] b1, int start1, byte[] b2, int start2, int len)
  {
    for(int x=0;x<len;++x)
      b1[start1 + x] = b2[start2 + x];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Copies str to b1 (b1 assumed empty)
  public int strToBytes(byte[] b1, String str)
  {
    return strToBytes(b1, str, 0, str.length());
  }
  public int strToBytes(byte[] b1, String str, int strStart, int strLen)
  {
    if(strLen == -1)
      strLen = str.length();

    int x = strStart;
    int y = 0;

    while(x < strLen)
       b1[y++] = (byte)str.charAt(x++);
    b1[y] = '\000';
    return y;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Cats b2 to b1 (b2 must be null'd)
  public void bytesToBytes(byte[] b1, byte[] b2, int start2)
  {
    int x = lengthBytes(b1, 0);
    int y = start2;

    while(b2[y] != 0)
       b1[x++] = b2[y++];
    b1[x] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Assumes null-term'd bytes; in a char format (e.g., '1','2','3')
  public double doubleFromBytesCharFormat(byte[] bb, int fromByte)
  {
    int x=fromByte;
    double y=0;

    byte[] b = new byte[100]; // plenty
    boolean isNeg;
    if(bb[0] == '-') // -ve
    {
      isNeg = true;
      while(bb[x] != '\000')
      {
        b[x] = bb[x+1];
        ++x;
      }
      b[x] = '\000';
    }
    else
    {
      isNeg = false;
      while(bb[x] != '\000')
      {
        b[x] = bb[x];
        ++x;
      }
      b[x] = '\000';
    }
    x=0;

    while(b[x] != '.' && b[x] != '\000')
    {
      y *= 10;
      y += (b[x] - 48);
      ++x;
    }

    if(b[x] == '.')
    {
      double j, z=0;
      int i, w=1;
      ++x;
      while(b[x] != '\000')
      {
        j = b[x] - 48;
        for(i=0;i<w;++i)
          j /= 10.000000000;
        z += j;

        ++w;
        ++x;
      }
      y += z;
    }

    if(isNeg)
      y *= -1;
    
    return y;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Appends a null
  // appends a zero as the second char after the decPoint (for other calls to this fn, to work)
  public void doubleToBytesCharFormat(double value, byte[] b, int fromByte) throws Exception
  {
    doubleToBytesCharFormat(value, b, fromByte, '2');
  }
  public void doubleToBytesCharFormat(double value, byte[] b, int fromByte, char numDecPlaces) throws Exception
  {
    doubleToChars(numDecPlaces, value, b, fromByte);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double doubleFromBytes(byte[] b, long start)
  {
    try
    {
      byte[] bb = new byte[8];

      int from = (int)start;
      int y=7;
      for(int x=from;x<from+8;++x)
      {
        bb[y--] = b[x];
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(bb);
      DataInputStream is = new DataInputStream(bais);
      double d = is.readDouble();
      bais.close();
      is.close();
      return d;
    }
    catch(Exception ioErr)
    {
      return 0L;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void doubleToBytes(double theDouble, byte[] b, long start)
  {
    try
    {
      byte[] bb;

      ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
      DataOutputStream os = new DataOutputStream(baos);
      os.writeDouble(theDouble);
      bb = baos.toByteArray();

      int from = (int)start;
      int y=7;
      for(int x=from;x<from+8;++x)
      {
        b[x] = bb[y--];
      }

      baos.close();
      os.close();
    }
    catch(Exception ioErr) { }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int lengthBytes(byte[] b, int start)
  {
    int len=0;
    int x=start;
    try
    {
      while(b[x] != '\000')
      {
        ++len;
        ++x;
      }
    }
    catch(Exception e) { }
    return len;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean match(byte[] b1, String str2)
  {
    byte[] b2;
    b2 = str2.getBytes();
    int len = str2.length();
    int len1 = lengthBytes(b1, 0);
    if(len1 > len)
      len = len1;
    ++len;
    return match('=', b1, 0, b2, 0, len);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean match(byte[] b1, String str2, int len)
  {
    byte[] b2;
    b2 = str2.getBytes();

    if(len == 0) return false;

    int len1 = lengthBytes(b1, 0);

    if(len > len1)
      return false;

    return match('=', b1, 0, b2, 0, len);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean match(char test, byte[] b1, int start1, byte[] b2, int start2, int numChars)
  {
    int len1 = lengthBytes(b1, 0);
    int len2 = lengthBytes(b2, 0);
    int x;

    switch(test)
    {
      case '>' : x=0;
                 while(x < numChars && x < len1 && x < len2)
                 {
                   if(b1[start1 + x] > b2[start2 + x])
                     return true;
                   else
                     if(b1[start1 + x] < b2[start2 + x])
                       return false;
                   ++x;
                 }
                 if(x == numChars)
                   return false;
                 if(x == len2)
                 {
                   if(x == len1)
                     return false;
                   return true;
                 }
                 return false;
      case '<' : x=0;
                 while(x < numChars && x < len1 && x < len2)
                 {
                   if(b1[start1 + x] < b2[start2 + x])
                     return true;
                   else
                     if(b1[start1 + x] > b2[start2 + x])
                       return false;
                   ++x;
                 }
                 if(x == numChars)
                   return false;
                 if(x == len1)
                 {
                   if(x == len2)
                     return false;
                   return true;
                 }
                 return false;
      case '=' : x=0;
                 while(x < numChars && x < len1 && x < len2)
                 {
                   if(b1[start1 + x] != b2[start2 + x])
                     return false;
                   ++x;
                 }
                 if(x == numChars)
                   return true;
                 if(x == len1 || x == len2)
                 {
                   if(len1 == len2)
                     return true;
                   return false;
                 }
                 return true;
    }

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean matchFixed(char test, byte[] b1, int start1, byte[] b2, int start2, int numChars)
  {
    int x=0;

    switch(test)
    {
      case '>' : x=0;
                 while(x < numChars)
                 {
                   if(b1[start1 + x] > b2[start2 + x])
                     return true;
                   else
                     if(b1[start1 + x] < b2[start2 + x])
                       return false;
                   ++x;
                 }
                 return false;
      case '<' : x=0;
                 while(x < numChars)
                 {
                   if(b1[start1 + x] < b2[start2 + x])
                     return true;
                   else
                     if(b1[start1 + x] > b2[start2 + x])
                       return false;
                   ++x;
                 }
                 return false;
      case '=' : x=0;
                 while(x < numChars)
                 {
                   if(b1[start1 + x] != b2[start2 + x])
                     return false;
                   ++x;
                 }
                 return true;
    }

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean matchIgnoreCase(byte[] b1, int start1, byte[] b2, int start2)
  {
    try
    {
      if(lengthBytes(b1, 0) != lengthBytes(b2, 0)) // cannot be the same
        return false;

      if((b1[0] == '\000' && b2[0] != '\000') || (b1[0] != '\000' && b2[0] == '\000'))
        return false;

      int x=0;
      byte byte1, byte2;
      while(b1[start1 + x] != '\000')
      {
        byte1 = b1[start1 + x];
        byte2 = b2[start2 + x];
        if(byte1 >= (byte)'a' && byte1 <= (byte)'z')
          byte1 -= 32;
        if(byte2 >= (byte)'a' && byte2 <= (byte)'z')
          byte2 -= 32;
        if(byte1 != byte2)
          return false;
        ++x;
      }
      return true;
    }
    catch(Exception e)
    {
      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean matchIgnoreCase(byte[] b1, int start1, String s)
  {
    try
    {
      if(lengthBytes(b1, 0) != s.length()) // cannot be the same
        return false;
      int x=0;
      byte byte1, byte2;
      while(b1[start1 + x] != '\000')
      {
        byte1 = b1[start1 + x];
        byte2 = (byte)s.charAt(x);
        if(byte1 >= (byte)'a' && byte1 <= (byte)'z')
          byte1 -= 32;
        if(byte2 >= (byte)'a' && byte2 <= (byte)'z')
          byte2 -= 32;
        if(byte1 != byte2)
          return false;
        ++x;
      }
      return true;
    }
    catch(Exception e)
    {
      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean contains(byte[] ip, String reqd)
  {
    return contains(ip, 0, reqd);
  }
  public boolean contains(byte[] ip, int startFrom, String reqd)
  {
    byte[] b = new byte[100]; // plenty
    int lenReqd = strToBytes(b, reqd);
    int x=startFrom;
    int len = lengthBytes(ip, 0);
    while(x < len)
    {
      if(matchFixed('=', ip, x, b, 0, lenReqd))
        return true;
      ++x;
    }

    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int containsPosn(byte[] ip, int startFrom, String reqd)
  {
    byte[] b = new byte[reqd.length() + 1];
    int lenReqd = strToBytes(b, reqd);
    int x=startFrom;
    int len = lengthBytes(ip, 0);
    while(x < len)
    {
      if(matchFixed('=', ip, x, b, 0, lenReqd))
        return x;
      ++x;
    }

    return -1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int indexOf(byte[] buf, char ch)
  {

    int x=0;
    while(buf[x] != '\000')
    {
      if(buf[x] == (byte)ch)
        return x;

      ++x;
    }

    return -1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean startsWithIgnoreCase(byte[] b1, int start1, String s)
  {
    try
    {
      int x=0;
      byte byte1, byte2;
      int len = s.length();
      while(x < len)
      {
        byte1 = b1[start1 + x];
        byte2 = (byte)s.charAt(x);
        if(byte1 >= (byte)'a' && byte1 <= (byte)'z')
          byte1 -= 32;
        if(byte2 >= (byte)'a' && byte2 <= (byte)'z')
          byte2 -= 32;
        if(byte1 != byte2)
          return false;
        ++x;
      }
      return true;
    }
    catch(Exception e)
    {
      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean match(char test, byte[] b1, int start1, byte[] b2, int start2)
  {
    try
    {
    int x=0;
    switch(test)
    {
      case '>' : while(b1[start1 + x] != '\000' && b1[start1 + x] != '\001' && b1[start1 + x] != '\002')
                 {
                   if(b1[start1 + x] > b2[start2 + x])
                     return true;
                   else
                     if(b1[start1 + x] < b2[start2 + x])
                       return false;
                   ++x;
                 }
                 return false;
      case '<' : while(b1[start1 + x] != '\000' && b1[start1 + x] != '\001' && b1[start1 + x] != '\002')
                 {
                   if(b1[start1 + x] < b2[start2 + x])
                     return true;
                   else
                     if(b1[start1 + x] > b2[start2 + x])
                       return false;
                   ++x;
                 }
                 return false;
      case '=' : if((b1[0] == '\000' && b2[0] != '\000') || (b1[0] != '\000' && b2[0] == '\000'))
                   return false;

                 if(lengthBytes(b1, 0) != lengthBytes(b2, 0)) // cannot be the same
                   return false;

                 while(b1[start1 + x] != '\000' && b1[start1 + x] != '\001' && b1[start1 + x] != '\002')
                 {
                   if(b1[start1 + x] != b2[start2 + x])
                     return false;
                   ++x;
                 }
                 break;
    }
    return true;
    }
    catch(Exception e)
    {
      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean match(char test, String s1, int start1, String s2, int start2, int numChars)
  {
    int len1 = s1.length();
    int len2 = s2.length();
    int x;

    switch(test)
    {
      case '>' : x=0;
                 while(x < numChars && x < len1 && x < len2)
                 {
                   if(s1.charAt(start1 + x) > s2.charAt(start2 + x))
                     return true;
                   else
                     if(s1.charAt(start1 + x) < s2.charAt(start2 + x))
                       return false;
                   ++x;
                 }
                 if(x == numChars)
                   return false;
                 if(x == len2)
                 {
                   if(x == len1)
                     return false;
                   return true;
                 }
                 return false;
      case '<' : x=0;
                 while(x < numChars && x < len1 && x < len2)
                 {
                   if(s1.charAt(start1 + x) < s2.charAt(start2 + x))
                     return true;
                   else
                     if(s1.charAt(start1 + x) > s2.charAt(start2 + x))
                       return false;
                   ++x;
                 }
                 if(x == numChars)
                   return false;
                 if(x == len1)
                 {
                   if(x == len2)
                     return false;
                   return true;
                 }
                 return false;
      case '=' : x=0;
                 while(x < numChars && x < len1 && x < len2)
                 {
                   if(s1.charAt(start1 + x) != s2.charAt(start2 + x))
                     return false;
                   ++x;
                 }
                 if(x == numChars)
                   return true;
                 if(x == len1 || x == len2)
                 {
                   if(len1 == len2)
                     return true;
                   return false;
                 }
                 return true;
    }

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Converts bytes to upper case; term'd by null (or not - arraybounds... caught)
  public void toUpper(byte[] b, int start)
  {
    try
    {
      int x = start;
      while(b[x] != '\000')
      {
        if(b[x] >= 'a' && b[x] <= 'z')
          b[x] -= 32;
        ++x;
      }
    }
    catch(Exception e) { }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Converts bytes to lower case; term'd by null (or not - arraybounds... caught)
  public void toLower(byte[] b, int start)
  {
    try
    {
      int x = start;
      while(b[x] != '\000')
      {
        if(b[x] >= 'A' && b[x] <= 'Z')
          b[x] += 32;
        ++x;
      }
    }
    catch(Exception e) { }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int strToInt(String s)
  {
    if(s.length() == 0)
      return 0;
    
    BigInteger bi = new BigInteger(s);
    return bi.intValue();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public long strToLong(String s)
  {
    BigInteger bi = new BigInteger(s);
    return bi.longValue();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double doubleFromChars(byte[] b)
  {
    String s = stringFromBytes(b, 0L);

    if(s.length() == 0)
      return 0.0;

    Double d = new Double(s);

    return d;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double doubleFromChars(byte[] b, int start)
  {
    String s = stringFromBytes(b, start);
    if(s.length() == 0)
      return 0.0;

    Double d = new Double(s);

    return d;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double doubleFromStr(String s)
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
      return(d * -1);
    }
    
    d = new Double(t);

    return d;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int intFromStr(String s)
  {
    if(s.length() == 0) return 0;
    if(s.charAt(0) == ' ') return 0;

    Integer i = new Integer(s);

    return i;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String doubleToStr(double d)
  {
    return doubleToStr('2', d);
  }
  public String doubleToStr(char dps, double d)
  {
    byte[] buf = new byte[50];
    doubleToChars(dps, d, buf, 0);
    return stringFromBytes(buf, 0L);
  }
  public String doubleToStr(boolean stripZeroes, char dps, double d)
  {
    byte[] buf = new byte[50];
    doubleToChars(dps, d, buf, 0);
    if(stripZeroes)
    {
      int x = lengthBytes(buf, 0);
      --x;
      while(x > 0 && buf[x] == '0')
        --x;
      buf[x + 1] = '\000';
      if(buf[x] == '.')
      {
        buf[x + 1] = '0';
        buf[x + 2] = '0';
        buf[x + 3] = '\000';
      }
      if(buf[x-1] == '.')
      {
        buf[x + 1] = '0';
        buf[x + 2] = '\000';
      }

    }

    return stringFromBytes(buf, 0L);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public short doubleToChars(char dps, double d, byte[] buf, int start)
  {
      try
      {
    int x;
    int numChars = dps - 48;
    String s = "";

    if(d == 0.0)
    {
      buf[start]   = (byte)'0';
      buf[start+1] = (byte)'.';
      buf[start+2] = (byte)'0';
      buf[start+3] = (byte)'0';
      buf[start+4] = (byte)'\000';
      return 4;
    }

    // truncate after dps number of chars
    BigDecimal dd3 = new BigDecimal(d).setScale(8, BigDecimal.ROUND_HALF_UP);
    String t = dd3.toString();

    int len = t.length();
    x=0;
    while(x < len && t.charAt(x) != '.')
      s += t.charAt(x++);

    if(x == len) // no decPt
      ;
    else
    {
      int y=0;
      while(x < len && y <= (numChars+1))
      {
        s += t.charAt(x++);
        ++y;
      }
    }

    d = doubleFromStr(s);

    BigDecimal dd2 = new BigDecimal(d).setScale(numChars + 1, BigDecimal.ROUND_HALF_UP);
    BigDecimal dd = dd2.setScale(numChars, BigDecimal.ROUND_HALF_UP);

    s = dd.toString();

    x=0;
    while(x < s.length() && s.charAt(x) != '.')
    {
      buf[start + x] = (byte)s.charAt(x);
      ++x;
    }

    len=x;
    if(numChars > 0)
    {
      buf[start + x++] = '.';
      ++len;

      int y=0;
      while(y < numChars)
      {
        if(len < s.length())
          buf[start + x] = (byte)s.charAt(x);
        else buf[start + x] = (byte)'0';
        ++x;
        ++y;
        ++len;
      }
    }

    buf[start+x] = (byte)'\000';

    if(match(buf, "-0.00"))
    {
      buf[start]   = (byte)'0';
      buf[start+1] = (byte)'.';
      buf[start+2] = (byte)'0';
      buf[start+3] = (byte)'0';
      buf[start+4] = (byte)'\000';
    }
    
    return (short)++len;
    }
    catch(Exception e)
    {
      buf[start]   = (byte)'0';
      buf[start+1] = (byte)'\000';
      return 1;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public double doubleDPs(double d, char dps) throws Exception
  {
    byte[] thisVal = new byte[100];
    doubleToChars(dps, d, thisVal, 0);
    return doubleFromChars(thisVal, 0);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void doubleDPs(byte[] thisVal, byte[] newVal, char dps) throws Exception
  {
    double d = doubleFromChars(thisVal, 0);
    doubleToChars(dps, d, newVal, 0);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String doubleDPs(String thisVal, char dps) throws Exception
  {
    double d = doubleFromStr(thisVal);
    byte[] newVal = new byte[50];
    doubleToChars(dps, d, newVal, 0);
    return stringFromBytes(newVal, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String doubleDPs(char dps, double d) throws Exception
  {
    byte[] newVal = new byte[50];
    doubleToChars(dps, d, newVal, 0);
    return stringFromBytes(newVal, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Simply truncates an entry after a given num of DPs
  public void bytesDPsGivenSeparatorRegardless(boolean stripTrailingZeroes, char separator, char dps, byte[] b, int bufLen, int entry) throws Exception
  {
    bytesDPsGivenSeparator(stripTrailingZeroes, separator, dps, b, bufLen, entry);
    byte[] b2 = new byte[100];
    dfsGivenSeparator(true, separator, b, (short)entry, b2);
    stripTrailingZeroes(b2, 0);
    repAlphaGivenSeparator(separator, b, bufLen, (short)entry, b2);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void bytesDPsGivenSeparator(char separator, char dps, byte[] b, int bufLen, int entry) throws Exception
  {
    bytesDPsGivenSeparator(false, separator, dps, b, bufLen, entry);
  }
  public void bytesDPsGivenSeparator(boolean stripTrailingZeroes, char separator, char dps, byte[] b, int bufLen, int entry) throws Exception
  {
    byte[] opBuf = new byte[1000];

    dfsGivenSeparator(false, separator, b, (short)entry, opBuf);

    int numDPs = (dps - (char)48);

    int x=0, y=0;

    switch(separator)
    {
      case '\001' : while(opBuf[x] != '\000' && opBuf[x] != '=')
                    ++x;
                  if(opBuf[x] != '\000')
                  {
                    byte[] thisVal = new byte[100];
                    ++x;
                    int z=0;
                    while(opBuf[x] != '\000' && opBuf[x] != '\001')
                      thisVal[z++] = opBuf[x++];
                    thisVal[z] = '\000';

                    double d = doubleFromChars(thisVal, 0);
                    doubleToChars('8', d, thisVal, 0);
                    if(stripTrailingZeroes)
                      stripTrailingZeroes(thisVal, numDPs);
                    repAlphaGivenSeparator(separator, b, bufLen, (short)entry, thisVal);
                  }
                  break;
      case ' ' :  while(opBuf[x] != '\000' && opBuf[x] != '"')
                    ++x;
                  if(opBuf[x] != '\000')
                  {
                    byte[] thisVal = new byte[100];
                    ++x;
                    int z=0;
                    while(opBuf[x] != '\000' && opBuf[x] != '"' && opBuf[x] != '.')
                      thisVal[z++] = opBuf[x++];

                    if(/*numDPs != 0 &&*/ opBuf[x] != '\000' && opBuf[x] != '"' && opBuf[x] == '.')
                    {
                      thisVal[z++] = '.';
                      ++x;
                    }

                    while(opBuf[x] != '\000' && opBuf[x] != '"')// && y < numDPs)
                    {
                      thisVal[z++] = opBuf[x++];
                      ++y;
                    }

                    thisVal[z] = '\000';

                    double d = doubleFromChars(thisVal, 0);
                    doubleToChars('8', d, thisVal, 0);
                    if(stripTrailingZeroes)
                      stripTrailingZeroes(thisVal, numDPs);

                    repAlphaGivenSeparator(separator, b, bufLen, (short)entry, thisVal);
                  }
                  break;
      case '\000' : while(opBuf[x] != '\000' && opBuf[x] != '.')
                    ++x;

                    if(opBuf[x] != '\000' && opBuf[x] == '.')
                      ++x;

                    while(opBuf[x] != '\000')
                    {
                      ++x;
                      ++y;
                    }

                    opBuf[x] = '\000';

                    double d = doubleFromChars(opBuf, 0);
                    doubleToChars('8', d, opBuf, 0);
                    if(stripTrailingZeroes)
                      stripTrailingZeroes(opBuf, numDPs);

                    repAlphaGivenSeparator(separator, b, bufLen, (short)entry, opBuf);
                    break;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Sets dPs in a \001-separated value-only buf
  public void bytesDPsGivenBinary1(boolean stripTrailingZeroes, char dps, byte[] b, int bufLen, int entry) throws Exception
  {
    byte[] opBuf = new byte[1000];

    dfsGivenSeparator(false, '\001', b, (short)entry, opBuf);

    int numDPs = (dps - (char)48);

    int x=0, y=0;

    while(opBuf[x] != '\000' && opBuf[x] != '.')
      ++x;

    if(opBuf[x] != '\000' && opBuf[x] == '.')
      ++x;
    while(opBuf[x] != '\000')
    {
      ++x;
       ++y;
    }

    opBuf[x] = '\000';

    double d = doubleFromChars(opBuf, 0);
    doubleToChars('8', d, opBuf, 0);

    if(stripTrailingZeroes)
      stripTrailingZeroes(opBuf, numDPs);

    repAlphaGivenSeparator('\001', b, bufLen, (short)entry, opBuf);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String strDPs(boolean stripTrailingZeroes, char dps, String s) throws Exception
  {
    byte[] b = new byte[50];
    strToBytes(b, s);
    bytesDPsGivenSeparator(stripTrailingZeroes, '\000', dps, b, 50, 0);
    return stringFromBytes(b, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Simply truncates a str after a given num of DPs
  public String strDPs(char dps, String s)
  {
    int numDPs = dps - 48;

    String opStr = "";
    int x=0;
    while(x < s.length() && s.charAt(x) != '.')
      opStr += s.charAt(x++);

    if(x < s.length() && s.charAt(x) == '.')
      opStr += '.';
    ++x;

    int y=0;
    while(x < s.length() && y < numDPs)
    {
      opStr += s.charAt(x++);
      ++y;
    }
    
    if(opStr.endsWith("."))
      return opStr.substring(0, (opStr.length() - 1));
   
    return opStr;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // removeBlanks will completely remove any entries that are all spaces
  public void stripLeadingAndTrailingSpaces(boolean removeBlanks, char intermediateTerminator, byte[] strs, int start, short numFlds, byte[] oBuf)
  {
    short count=0;
    int x = start;
    short y=0;
    while(count < numFlds)
    {
      while(strs[x] == ' ')
        ++x;
      while(strs[x] != '\000')
        oBuf[y++] = strs[x++];
      --y;
      while(oBuf[y] == ' ')
        --y;
      ++y;

      if(removeBlanks)
      {
        if(oBuf[y-1] == intermediateTerminator)
          --y;
        else ++count;
      }

      oBuf[y++] = (byte)intermediateTerminator;
      ++x;
    }
    oBuf[y] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripQuotes(String str)
  {
    int x = 0, len = str.length();
    String s="";

    while(x < len)
    {
      if(str.charAt(x) != '"')
        s += str.charAt(x);
      
      ++x;
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripQuote(String str)
  {
    int x = 0, len = str.length();
    String s="";

    while(x < len)
    {
      if(str.charAt(x) != '\'')
        s += str.charAt(x);
      
      ++x;
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripLeadingAndTrailingSpaces(String str)
  {
    if(str.length() == 0)
      return "";

    int from = 0, len = str.length();

    while(from < len && str.charAt(from) == ' ')
      ++from;

    if(from == len)
      return "";

    int x = len - 1;
    while(x > 0 && str.charAt(x) == ' ')
      --x;

    return str.substring(from, x+1);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripLeadingZeroes(String str)
  {
    if(str.length() == 0)
      return "";

    int from = 0, len = str.length();

    while(from < len && str.charAt(from) == '0')
      ++from;

    if(from == len)
      return "";

    return str.substring(from);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripDuplicateSpaces(String str)
  {
    int x = 0, len = str.length();
    boolean first = true;
    String s = "";
    
    while(x < len)
    {
      if(str.charAt(x) != ' ')
      {
        first = true;
        s += str.charAt(x);
      }
      else // space
      {
        if(first)
        {
          s += " ";
          first = false;
        }
      }
      
      ++x;
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void stripLeadingAndTrailingSpaces(byte[] b)
  {
    int x, from=0, len = lengthBytes(b, 0);

    while(from < len && b[from] == ' ')
      ++from;

    x = len - 1;
    while(x > 0 && b[x] == ' ')
      --x;

    int z=0;
    for(int y=from;y<=x;++y)
      b[z++] = b[y];
    b[z] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void stripTrailingSpaces(byte[] b)
  {
    stripTrailingSpaces(b, lengthBytes(b, 0));
  }
  public void stripTrailingSpaces(byte[] b, int len)
  {
    --len;
    while(len >= 0 && b[len] == ' ') // = changed to >= 30dec03
      --len;
    b[++len] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripTrailingSpacesStr(String str)
  {
    if(str == null) return "";

    int x = str.length() - 1;
    while(x >= 0 && str.charAt(x) == ' ')
      --x;

    String s="";
    for(int y=0;y<=x;++y)
      s += str.charAt(y);
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void stripTrailingZeroes(byte[] b, int minNumDPs)
  {
    int x = lengthBytes(b, 0);
    if(x == 0) return;

    boolean hasDecPt = false;
    for(int i=0;i<x;++i)
      if(b[i] == '.') hasDecPt = true;
    if(! hasDecPt)
      return;

    --x;
    while(x >= 0 && b[x] == '0' && b[x] != '.')
      --x;

    ++x;
    b[x] = '\000';

    x=0;
    while(b[x] != '\000' && b[x] != '.')
      ++x;
    int y;
    if(b[x] == '\000')
    {
      for(y=0;y<minNumDPs;++y)
        b[x++] = (byte)'0';
      b[x] = '\000';
    }
    else // == '.'
    {
      boolean no = true;
      if(minNumDPs == 0)
      {
        y=x+1;
        while(b[y] != '\000')
        {
          if(b[y] != '0')
            no = false;
          ++y;
        }
        if(no)
          b[x] = '\000';
      }
      else no = false;

      if(! no)
      {
        ++x;
        y=0;
        while(b[x] != '\000')
        {
          ++x;
          ++y;
        }
        while(y < minNumDPs)
        {
          b[x++] = (byte)'0';
          ++y;
        }
        b[x] = '\000';
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripTrailingZeroesStr(String str)
  {
    return stripTrailingZeroesStr(true, str);
  }
  public String stripTrailingZeroesStr(boolean treatAsNumericStr, String str)
  {
    try
    {
      if(treatAsNumericStr)
      {
        if(str.indexOf(".") == -1)
          return str;
      }

      int x = str.length() - 1;
      while(x >= 0 && str.charAt(x) == '0')
        --x;

      if(str.charAt(x) == '.')
        --x;

      String s="";
      for(int y=0;y<=x;++y)
        s += str.charAt(y);

      return s;
   }
   catch(Exception e) { return "0"; }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripAllNonNumeric(String str) throws Exception
  {
    String str2="";
    int len = str.length();
    for(int i=0;i<len;++i)
    {
      if(str.charAt(i) >= '0' && str.charAt(i) <= '9')
        str2 += str.charAt(i);
    }
    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void pb(String msg, byte[] b, int start, int numBytes)
  {
    pb(msg, b, start, numBytes, false);
  }
  public void pb(String msg, byte[] b, int start, int numBytes, boolean inDec)
  {
    int x=start;
    System.out.println("");
    try
    {
      System.out.print(msg + ": ");
      if(numBytes==0)
      {
        while(b[x] != '\000')
        {
          System.out.print((char)b[x]);
          ++x;
        }
      }
      else
      {
        for(int i=0;i<numBytes;++i)
        {
          if(b[start+i] == -1)
            System.out.print("*FF*,");
          else
          if(b[start+i] == 0)
            System.out.print("#,");
          else
          if(b[start+i] == '\001')
            System.out.print("*1*,");
          else
          if(b[start+i] == '\002')
            System.out.print("*2*,");
          else
          if(b[start+i] == '\r')
            System.out.print("*R*,");
          else
          if(b[start+i] == '\n')
            System.out.print("*N*,");
          else
          {
            if(inDec)
              System.out.print(b[start+i] + ",");
            else System.out.print((char)b[start+i] + ",");
          }
        }
      }
    }
    catch(Exception e) { }
//    System.out.println("");
  }

  // -------------------------------------------------------------------------------------------
  // decPlaces of -1 means any number of places
  public boolean validNumeric(String num, int decPlaces) throws Exception
  {
    int len = num.length();
    for(int i=0;i<len;++i)
    {
      if( ( num.charAt(i) >= '0' && num.charAt(i) <= '9' ) || num.charAt(i) == '-')
        ; // ok
      else
      {
        if(num.charAt(i) == '.')
        {
          if(decPlaces == 0)
            return false;
        }
        else return false;
      }
    }
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // decPlaces of -1 means any number of places
  public boolean validNumeric(byte[] num, int decPlaces) throws Exception
  {
    int i=0, dotCount=0;
    while(num[i] != '\000')
    {
      if( ( num[i] >= '0' && num[i] <= '9' ) || num[i] == '-')
        ; // ok
      else
      {
        if(num[i] == '.')
        {
          if(decPlaces == 0)
            return false;
          ++dotCount;
        }
        else return false;
      }
      ++i; // valid
    }
    if(dotCount > 1)
      return false;
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String capitalize(String s)
  {
    if(s.length() == 0)
      return "";

    String rtnStr;

    String str = s.toLowerCase();
    
    s = s.toUpperCase();
    int len = str.length();
    rtnStr = "" + s.charAt(0);
    for(int x=1;x<len;++x)
      rtnStr += str.charAt(x);

    return rtnStr;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // list items are separated by \001
  // subfields within an item are separated by \002
  // list is terminated by \0
  // code will work whether there are subfields or not (i.e., whether there are any \002).
  // only the first subfield is used to match (or the whole item if there are no subfields).
  public byte[] appendToList(boolean dupsAllowed, byte[] newItem, byte[] list, int[] listLen) throws Exception
  {
    int x=0, y=0, len;
    byte[] newItemKey = new byte[500];
    byte[] entry = new byte[500];
    //int start=0;

    while(newItem[y] != '\001' && newItem[y] != '\002')
      newItemKey[x++] = newItem[y++];
    newItemKey[x] = '\000';
    len = x;

    x=y=0;
    int upto = lengthBytes(list, 0);

    boolean append = false;
    while(! append && list[y] != '\000')
    {
      x=0;

      while(list[y] != '\001' && list[y] != '\002')
        entry[x++] = list[y++];
      entry[x] = '\000';

      if(! dupsAllowed)
      {
        if(len == x && matchFixed('=', entry, 0, newItemKey, 0, x)) // same length and same chars
          return list;
      }
      else append = true;
    }
    // insert at end
    len = lengthBytes(newItem, 0);
    if((upto + len + 1) >= listLen[0])
    {
      byte[] tmp = new byte[listLen[0]];
      System.arraycopy(list, 0, tmp, 0, listLen[0]);
      listLen[0] += (len + 1000);
      list = new byte[listLen[0]];
      System.arraycopy(tmp, 0, list, 0, listLen[0] - (len + 1000));
    }

    for(x=0;x<len;++x)
      list[upto + x] = newItem[x];
    list[upto + len] = '\000';

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // list items are separated by \001
  // subfields within an item are separated by \002
  // list is terminated by \0
  // code will work whether there are subfields or not (i.e., whether there are any \002).
  // on entry, newItem is \001 term'd (as well as \0)
  // only the first subfield is used to match (or the whole item if there are no subfields).
  public byte[] addToList(byte[] newItem, byte[] list, int[] listLen) throws Exception
  {
    return addToList(false, newItem, list, listLen);
  }
  public byte[] addToList(boolean dupsAllowed, byte[] newItem, byte[] list, int[] listLen) throws Exception
  {
    return addToList(dupsAllowed, newItem, list, listLen, false);
  }
  public byte[] addToList(boolean dupsAllowed, byte[] newItem, byte[] list, int[] listLen, boolean firstEntryIsNumeric) throws Exception
  {
    int x=0, y=0, len, start;
    byte[] newItemKey = new byte[500];
    byte[] entry = new byte[500];
    boolean isLessThan;

    start=0;

    while(newItem[y] != '\001' && newItem[y] != '\002')
      newItemKey[x++] = newItem[y++];
    newItemKey[x] = '\000';

    len = x;

    x=y=0;
    int upto = lengthBytes(list, 0);

    while(list[y] != '\000')
    {
      x=0;
      start = y;
      while(list[y] != '\001' && list[y] != '\002')
        entry[x++] = list[y++];
      entry[x] = '\000';

      if(! dupsAllowed)
      {
        if(len == x)
        {
          if(firstEntryIsNumeric)
          {
            int entryI      = intFromBytes(entry, 0);
            int newItemKeyI = intFromBytes(newItemKey, 0);

            if(entryI == newItemKeyI)
              return list;
          }
          else
          {
            if(matchFixed('=', entry, 0, newItemKey, 0, x)) // same length and same chars
              return list;
          }
        }
      }

      isLessThan = false;
      if(firstEntryIsNumeric)
      {
        int entryI      = intFromBytes(entry, 0);
        int newItemKeyI = intFromBytes(newItemKey, 0);

        if(entryI < newItemKeyI) // this entry < newitemkey, so read next...
          isLessThan = true;
      }
      else
      {
        if(matchFixed('<', entry, 0, newItemKey, 0, len)) // this entry < newitemkey, so read next...
          isLessThan = true;
      }

      if(isLessThan)
      {
        while(list[y] != '\001')
          ++y;
        ++y;
      }
      else // this entry >= newitem, so insert now
      {
        len = lengthBytes(newItem, 0);
        if((upto + len + 1) >= listLen[0])
        {
          byte[] tmp = new byte[listLen[0]];
          System.arraycopy(list, 0, tmp, 0, listLen[0]);
          listLen[0] += 1000;
          list = new byte[listLen[0]];
          System.arraycopy(tmp, 0, list, 0, listLen[0] - 1000);
        }

        while(list[y] != '\001')
          ++y;
        ++y;
        for(x=(listLen[0]-1);x>(start+len-1);--x) // shunt-up
          list[x] = list[x - len];

        for(x=0;x<len;++x)
          list[start + x] = newItem[x];
        return list;
      }
    }

    // insert at end
    len = lengthBytes(newItem, 0);
    if((upto + len + 1) >= listLen[0])
    {
      byte[] tmp = new byte[listLen[0]];
      System.arraycopy(list, 0, tmp, 0, listLen[0]);
      listLen[0] += 1000;
      list = new byte[listLen[0]];
      System.arraycopy(tmp, 0, list, 0, listLen[0] - 1000);
    }

    for(x=0;x<len;++x)
      list[y + x] = newItem[x];
    list[y + len] = '\000';

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean chkList(byte[] reqdItemKey, byte[] list) throws Exception
  {
    int x, y=0;
    byte[] entry = new byte[500];

    while(list[y] != '\000')
    {
      x=0;
      while(list[y] != '\001' && list[y] != '\002')
        entry[x++] = list[y++];
      entry[x] = '\000';
      if(matchFixed('=', entry, 0, reqdItemKey, 0, x))
        return true;
      while(list[y] != '\001')
        ++y;
      ++y;
    }

    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean getListEntry(byte[] reqdItemKey, byte[] list, byte[] entry) throws Exception
  {
    int x, y=0, lenNewItem = lengthBytes(reqdItemKey, 0);

    while(list[y] != '\000')
    {
      x=0;
      while(list[y] != '\001' && list[y] != '\002')
        entry[x++] = list[y++];
      entry[x] = '\000';

      if(matchFixed('=', entry, 0, reqdItemKey, 0, lenNewItem))
      {
        while(list[y] != '\001')
          entry[x++] = list[y++];
        entry[x] = '\000';

        return true;
      }
      while(list[y] != '\001')
        ++y;
      ++y;
    }

    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // origin-0
  public boolean getListEntryByNum(int entryNum, byte[] list, byte[] entry) throws Exception
  {
    try
    {
      int x, y=0, count=0;

      while(list[y] != '\000')
      {
        x=0;
        while(list[y] != '\000' && list[y] != '\001') // just-in-case
          entry[x++] = list[y++];
        entry[x] = '\000';

        if(count == entryNum)
        {
          return true;
        }

        ++count;
        ++y;
      }
    }
    catch(Exception e)
    {
      //System.out.print("generalUtils: getListEntryByNum: " + e);
    }

    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // checks each list entry and pulls-out the record where the third fld matches thirdFld
  // starts search at entry num rec
  // rtns: entry num
  public int getListEntryByNumAndValue(int entryNum, int maxEntries, byte[] thirdFld, byte[] list, byte[] entry) throws Exception
  {
    byte[] third = new byte[500]; // plenty
    int x, y=0, z=0, count=0, len = lengthBytes(list, 0);

    if(entryNum >= maxEntries)
      return -1;
  
    while(count <= entryNum && count < maxEntries)
    {
      x=0;
      while(y < len && list[y] != '\001')
        entry[x++] = list[y++];
      entry[x] = '\000';
      ++y;
      ++count;
    }

    while(true)
    {
      x=0;
      while(entry[x] != '\002' && entry[x] != '\000') // step past fld 1
        ++x;
      ++x;
      while(entry[x] != '\002' && entry[x] != '\000') // step past fld 2
        ++x;
    
      if(entry[x] == '\002') // just-in-case
      {
        ++x;
        z=0;
        while(entry[x] != '\002' && entry[x] != '\000')
          third[z++] = entry[x++];
        third[z] = '\000';

        if(matchIgnoreCase(third, 0, thirdFld, 0))
        {
          return count;
        }
      }
      else return -1;

      x=0;
      while(y < len && list[y] != '\001')
        entry[x++] = list[y++];
      entry[x] = '\000';
      ++y;

      if(count == maxEntries)
        return -1;

      ++count;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] repListEntry(byte[] reqdItemKey, byte[] newEntry, byte[] list, int[] listLen) throws Exception
  {
    int x, y=0, z;
    byte[] entry = new byte[500];

    while(list[y] != '\000')
    {
      z = y;
      x=0;
      while(list[y] != '\001' && list[y] != '\002')
        entry[x++] = list[y++];

      entry[x] = '\000';
      if(matchFixed('=', entry, 0, reqdItemKey, 0, x))
      {
        while(list[y] != '\001')
        {
          ++x;
          ++y;
        }
        ++x;
        ++y;

        int len = lengthBytes(newEntry, 0);
        if(x < len) // need to consider extra storage
        {
          if(lengthBytes(list, 0) + len - x > listLen[0]) // need extra storage
          {
            byte[] tmp = new byte[listLen[0]];
            System.arraycopy(list, 0, tmp, 0, listLen[0]);
            listLen[0] += 1000;
            list = new byte[listLen[0]];
            System.arraycopy(tmp, 0, list, 0, listLen[0] - 1000);
          }
        }

        if(len > x) // new needs more room, so, shunt-up by the difference
        {
          int diff = len - x;
          for(x=listLen[0]-1;x>y;--x)
            list[x] = list[x-diff];
        }
        else
        if(x > len) // new needs less room, so, shunt-down by the difference
        {
          int diff = x - len;
          for(x=z+len;x<listLen[0]-diff;++x)
            list[x] = list[x+diff];
        }

        x=0;
        while(newEntry[x] != '\001') // insert new
          list[z++] = newEntry[x++];
        list[z] = '\001';

        return list;
      }

      while(list[y] != '\001')
        ++y;
      ++y;
    }

    return list;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public byte[] repListEntryByNum(int entryNum, byte[] newEntry, byte[] list, int[] listLen) throws Exception
  {
    int x, y=0, z, count=0;
    byte[] entry = new byte[1000]; // plenty

    while(list[y] != '\000')
    {
      z = y;
      x=0;
      while(list[y] != '\001')
        entry[x++] = list[y++];
      entry[x++] = '\001';
      entry[x] = '\000';
      if(count == entryNum)
      {
        int len = lengthBytes(newEntry, 0);
        if(x < len) // need to consider extra storage
        {
          if(lengthBytes(list, 0) + len - x > listLen[0]) // need extra storage
          {
            byte[] tmp = new byte[listLen[0]];
            System.arraycopy(list, 0, tmp, 0, listLen[0]);
            listLen[0] += 2000;
            list = new byte[listLen[0]];
            System.arraycopy(tmp, 0, list, 0, listLen[0] - 2000);
          }
        }

        if(len > x) // new needs more room, so, shunt-up by the difference
        {
          int diff = len - x;
          for(x=listLen[0]-1;x>y;--x)
            list[x] = list[x-diff];
        }
        else
        if(x > len) // new needs less room, so, shunt-down by the difference
        {
          int diff = x - len;
          for(x=z+len;x<listLen[0]-diff;++x)
            list[x] = list[x+diff];
        }

        x=0;
        while(newEntry[x] != '\001') // insert new
          list[z++] = newEntry[x++];
        list[z] = '\001';

        return list;
      }

      ++count;
      ++y;
    }

    return list; // just-in-case
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public int countListEntries(byte[] list) throws Exception
  {
    int y=0, count=0;

    while(list[y] != '\000')
    {
      while(list[y] != '\001')
        ++y;
      ++y;

      ++count;
    }

    return count;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // currently does not correctly replace dots and commas if the French-style is req'd
  public String formatNumeric(double d, char numDecPlaces) throws Exception
  {
    byte[] b = new byte[100];
    doubleToBytesCharFormat(d, b, 0, numDecPlaces);
    formatNumeric(b, numDecPlaces);
    return stringFromBytes(b, 0L);
  }
  public String formatNumeric(int i) throws Exception
  {
    byte[] b = new byte[100];
    intToBytesCharFormat(i, b, (short)0);
    formatNumeric(b, '0');
    return stringFromBytes(b, 0L);
  }
  public String formatNumeric(String s, char numDecPlaces) throws Exception
  {
    byte[] b = new byte[100];

    strToBytes(b, s);

    formatNumeric(b, numDecPlaces);

    return stringFromBytes(b, 0L);
  }
  public void formatNumeric(byte[] b, char numDecPlaces) throws Exception
  {
    double d = doubleFromBytesCharFormat(b, 0);
    
    bytesDPsGivenSeparator(true, '\000', numDecPlaces, b, 20, 0); // does dp 'truncation'

    if(d < 1000 && d > -1000) // no need for thousand commas
      return;

    int x=0;
    while(b[x] != '.' && b[x] != '\000')
      ++x;
    int p=x;

    int y=0;
    byte[] trailingStuff = new byte[50];
    while(b[x] != '\000')
      trailingStuff[y++] = b[x++];
    trailingStuff[y] = '\000';

    b[p] = '\000'; // 'remove' trailing stuff from b

    byte[] s = new byte[20];
    byte[] z = new byte[20];
    int len = lengthBytes(b, 0);

    int i=0;
    p=0;
    int stop;

    if(d < 0) stop = 1; else stop = 0;

    while(len > stop)
    {
      ++i;
      if(i == 4)
      {
        s[p++] = (byte)',';
        i = 1;
      }
      s[p++] = b[--len];
    }
    if(d < 0)
      s[p++] = '-';
    s[p] = '\000';

    // s is in reverse order
    i=0;
    len = lengthBytes(s, 0);
    while(len > 0)
      z[i++] = s[--len];
    z[i] = '\000';

    if(d < 0)
    {
      String negSign = "-";
      if(negSign.length() == 0) // just-in-case
        negSign = "-";
      if(negSign.charAt(0) != '-') // parens
      {
        b[0] = '(';
        bytesToBytes(b, 1, z, 0);
        i = lengthBytes(z, 1);
        b[i++] = ')';
        b[i] = '\000';
      }
      else // neg sign
        bytesToBytes(b, 0, z, 0);
    }
    else bytesToBytes(b, 0, z, 0);

    bytesToBytes(b, lengthBytes(b, 0), trailingStuff, 0);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String formatJustEnough(double d) throws Exception
  {
    if(d == 0) return "0";

    boolean negative = false;
    if(d < 0)
    {
      d *= -1;
      negative = true;
    }

    if(doubleDPs(d, '2') > 0.00)
    {
      if(negative) d *= -1;
      return formatNumeric(d, '2');
    }

    if(doubleDPs(d, '3') > 0.00)
    {
      if(negative) d *= -1;
      return formatNumeric(d, '3');
    }

    if(doubleDPs(d, '4') > 0.00)
    {
      if(negative) d *= -1;
      return formatNumeric(d, '4');
    }

    if(doubleDPs(d, '5') > 0.00)
    {
      if(negative) d *= -1;
      return formatNumeric(d, '5');
    }

    if(doubleDPs(d, '6') > 0.00)
    {
      if(negative) d *= -1;
      return formatNumeric(d, '6');
    }

    if(doubleDPs(d, '7') > 0.00)
    {
      if(negative) d *= -1;
      return formatNumeric(d, '7');
    }

    if(negative) d *= -1;
    return formatNumeric(d, '8');
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripAllSpaces(String str)
  {
    int len = str.length();
    String s="";

    for(int x=0;x<len;++x)
    {
      if(str.charAt(x) != ' ')
        s += str.charAt(x);
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------
  public void stripAllSpaces(byte[] b)
  {
    int x=0, y=0;

    while(b[x] != '\000')
    {
      if(b[x] != ' ')
        b[y++] = b[x];
      ++x;
    }
    b[y] = '\000';
  }

  // -------------------------------------------------------------------------------------------
  public void zeroize(byte[] b, int len)
  {
    for(int x=0;x<len;++x)
      b[x] = '\000';
  }

  // -------------------------------------------------------------------------------------------
  public String replaceSpacesWith20(String str)
  {
    String s="";
    int len = str.length();
    for(int x=0;x<len;++x)
    {
      if(str.charAt(x) == ' ')
        s += "%20";
      else s += str.charAt(x);
    }
    return s;
  }

  // -------------------------------------------------------------------------------------------
  public String replaceDoubleQuotesWithTwoSingleQuotes(String str)
  {
    String s="";
    int len = str.length();
    for(int x=0;x<len;++x)
    {
      if(str.charAt(x) == '"')
        s += "''";
      else s += str.charAt(x);
    }
    return s;
  }

  // -------------------------------------------------------------------------------------------
  public String newSessionID()
  {
    long l = new Date().getTime();
    int ii = new Random(l).nextInt();
    if(ii < 0)
      ii *= -1;

    return longToStr(l).substring(3) + intToStr(ii).substring(0,5);
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public String sanitise(String str)
  {
    return sanitise(false, str);
  }
  public String sanitise(boolean stripNewlines, String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
           if(str.charAt(x) == '#')  str2 += "%23";
      else if(str.charAt(x) == '\"') str2 += "%22";
      else if(str.charAt(x) == '\'') str2 += "%27";
      else if(str.charAt(x) == '&')  str2 += "%26";
      else if(str.charAt(x) == '%')  str2 += "%25";
      else if(str.charAt(x) == ' ')  str2 += "%20";
      else if(str.charAt(x) == '?')  str2 += "%3f";
      else if(str.charAt(x) == '+')  str2 += "%2b";
      else if(str.charAt(x) == '\n') { if(! stripNewlines) str2 += '\n'; }
      else if(str.charAt(x) == '\r') { if(! stripNewlines) str2 += '\r'; }
      else str2 += str.charAt(x);
    }
    return str2;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String deSanitise(String str)
  {
    String s, str2 = "";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '+')
        str2 += " ";
      else
      if(str.charAt(x) == '%')
      {
        s = str.substring(x+1, x+3);

        if(s.equals("23"))
        {
          str2 += "#";
          x += 2;
        }
        else
        if(s.equals("22"))
        {
          str2 += "\"";
          x += 2;
        }
        else
        if(s.equals("27"))
        {
          str2 += "'";
          x += 2;
        }
        else
        if(s.equals("26"))
        {
          str2 += "&";
          x += 2;
        }
        else
        if(s.equals("25"))
        {
          str2 += "%";
          x += 2;
        }
        else
        if(s.equals("20"))
        {
          str2 += " ";
          x += 2;
        }
        else
        if(s.equalsIgnoreCase("2f"))
        {
          str2 += "/";
          x += 2;
        }
        else
        if(s.equalsIgnoreCase("3f"))
        {
          str2 += "?";
          x += 2;
        }
        else
        if(s.equals("40"))
        {
          str2 += "@";
          x += 2;
        }
        else
        if(s.equals("2b"))
        {
          str2 += "+";
          x += 2;
        }
        else str2 += "%";
      }
      else str2 += str.charAt(x);
    }
    
    return str2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String deSanitiseReplaceNewLines(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\003')
        str2 += "\n";
      else str2 += str.charAt(x);
    }

    return str2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String replaceNewlinesByThrees(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\n')
        str2 += "\003";
      else str2 += str.charAt(x);
    }

    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripNewLines(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == 10 || str.charAt(x) == 13)
        ;
      else str2 += str.charAt(x);
    }

    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String replaceNewLinesByBR(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == 10)
        str2 += "<br>";
      else
      if(str.charAt(x) == 13)
        ;
      else str2 += str.charAt(x);
    }

    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String replaceNewLinesByN(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == 10)
        str2 += "*N*";
      else
      if(str.charAt(x) == 13)
        ;
      else str2 += str.charAt(x);
    }

    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String sanitiseReplacingNewlines(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
           if(str.charAt(x) == '#')  str2 += "%23";
      else if(str.charAt(x) == '\"') str2 += "%22";
      else if(str.charAt(x) == '\'') str2 += "%27";
      else if(str.charAt(x) == '&')  str2 += "%26";
      else if(str.charAt(x) == '%')  str2 += "%25";
      else if(str.charAt(x) == ' ')  str2 += "%20";
      else if(str.charAt(x) == '?')  str2 += "%3f";
      else if(str.charAt(x) == '+')  str2 += "%2b";
      else if(str.charAt(x) == '\n') str2 += '\003';
      else if(str.charAt(x) == '\r') ;
      else str2 += str.charAt(x);
    }
    return str2;
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String sanitise(byte[] b)
  {
    String s = sanitise(stringFromBytes(b, 0L));
    stringToBytes(s, 0, b);
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String sanitiseReplacingNulls(byte[] b, int bufLen)
  {
    int highest = bufLen - 1;
    while(b[highest] == '\000')
      --highest;

    String str2="";
    for(int x=0;x<=highest;++x)
    {
           if(b[x] == '#') str2 += "%23";
      else if(b[x] == '\"') str2 += "%22";
      else if(b[x] == '\'') str2 += "%27";
      else if(b[x] == '&') str2 += "%26";
      else if(b[x] == '%') str2 += "%25";
      else if(b[x] == ' ') str2 += "%20";
      else if(b[x] == '?') str2 += "%3F";
      else if(b[x] == '+') str2 += "%2B";
      else if(b[x] == '\000') str2 += '\001';
      else str2 += (char)b[x];
    }

    return str2;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public String sanitise2(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
           if(str.charAt(x) == '\"') str2 += "\\'\\'";
      else if(str.charAt(x) == '\'') str2 += "\\'";
      else if(str.charAt(x) == '\\')  str2 += "\\\\";
      else str2 += str.charAt(x);
    }
    return str2;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public String sanitise3(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\"') str2 += "\"\"";
      else str2 += str.charAt(x);
    }
    return str2;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public String sanitise4(String str)
  {
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
           if(str.charAt(x) == '\"') str2 += "\\\"";
      else if(str.charAt(x) == '\'') str2 += "\\\'";
      else if(str.charAt(x) == '?') str2 += "\\?";
      else if(str.charAt(x) == '\\') str2 += "\\\\";
      else str2 += str.charAt(x);
    }
    return str2;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String sanitiseForSQL(byte[] b)
  {
    return sanitiseForSQL(stringFromBytes(b, 0L));
  }
  public String sanitiseForSQL(String str)
  {
    if(str == null)
        return "";
    
    String str2="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\'') str2 += "''";
      else str2 += str.charAt(x);
    }

    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String sanitiseForXML(String s)
  {    
    byte[] b = new byte[s.length() + 1];
    strToBytes(b, s);
    return sanitiseForXML(b);    
  }
  public String sanitiseForXML(byte[] b)
  {
    String str2="";
    int x=0;
    while(b[x] != '\000')
    {
           if(b[x] == '&')  str2 += "&amp;";
      else if(b[x] == '"')  str2 += "&quot;";
      else if(b[x] == '\'') str2 += "&apos;";
      else if(b[x] == '<')  str2 += "&lt;";
      else if(b[x] == '>')  str2 += "&gt;";
      else str2 += (char)b[x];
      ++x;
    }

    return str2;
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String replaceNulls(byte[] b, int bufLen)
  {
    int highest = bufLen - 1;
    while(b[highest] == '\000')
      --highest;

    String str2="";
    for(int x=0;x<=highest;++x)
    {
      if(b[x] == '\000') str2 += '\001';
      else str2 += (char)b[x];
    }
    str2 += '\001';
    return str2;
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int replaceOnes(String str, byte[] buf)
  {
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\001') buf[x] = '\000';
      else buf[x] = (byte)str.charAt(x);
    }
    buf[len] = (byte)'\000';
    return len;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String replaceNewlinesWithSpaces(String str)
  {
    String s="";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\n')
        s += " ";
      else s += str.charAt(x);
    }
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String replaceBRWithSpaces(String str)
  {
    String s="";
    int x=0, len=str.length();
    while(x < len)
    {
      if(str.charAt(x) == '<')
      {
        if(x < len && str.substring(x+1).equalsIgnoreCase("br>"))
        {
          s += " ";
          x += 3;
        }
        else s += str.charAt(x++);
      }
      else s += str.charAt(x++);
    }
    
    return s;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripHTMLAndsanitise(String str)
  {
    String s="";
    int x=0, len=str.length();
    while(x < len)
    {
      if(str.charAt(x) == '<')
      {
        ++x;
        while(x < len && str.charAt(x) != '>')
          ++x;
        ++x;
      }
      else
      if(str.charAt(x) == '[' && (x < (len + 1) && str.charAt(x + 1) == '['))
      {
        x += 2;
        while(x < (len + 1) && str.charAt(x) != ']' && str.charAt(x+1) != ']')
          ++x;
        x += 2;
      }
      else s += str.charAt(x++);
    }
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripHTML(String str)
  {
    String s="";
    int x=0, len=str.length();
    while(x < len)
    {
      if(str.charAt(x) == '<')
      {
        ++x;
        while(x < len && str.charAt(x) != '>')
          ++x;
        ++x;
      }
      else s += str.charAt(x++);
    }
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int replaceOnesWithTwos(String str, byte[] buf)
  {
    int x, y = 0, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\001')
      {
        buf[x] = '\002';
        ++y;
      }
      else
      {
        buf[x] = (byte)str.charAt(x);
        ++y;
      }
    }
    
    buf[y] = (byte)'\000';
    
    return y;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public int replaceThreesWithNewlines(String str, byte[] buf)
  {
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\003') buf[x] = '\n';
      else buf[x] = (byte)str.charAt(x);
    }
    buf[len] = (byte)'\000';
    return len;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String replaceThreesWithSpaces(String str)
  {
    String s = "";
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '\003') s += " ";
      else s += str.charAt(x);
    }
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripAllOnes(String str)
  {
    int len = str.length();
    String s="";

    for(int x=0;x<len;++x)
    {
      if(str.charAt(x) != '\001')
        s += str.charAt(x);
    }

    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int replaceTwosWithOnes(byte[] buf)
  {
    int x=0;
    while(buf[x] != '\000')
    {
      if(buf[x] == '\002')
        buf[x] = '\001';
      ++x;
    }
    return x;
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int replaceTwosWithNulls(byte[] buf)
  {
    int x=0;
    while(buf[x] != '\000')
    {
      if(buf[x] == '\002') buf[x] = '\000';
      ++x;
    }
    return x;
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String sanitiseNumeric(String str)
  {
    String str2="";
    boolean oneDone = false;
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) == '.')
      {
        if(! oneDone)
        {
          str2 += ".";
          oneDone = true;
        }
      }
      else if(str.charAt(x) >= '0' && str.charAt(x) <= '9') str2 += str.charAt(x);
    }
    return str2;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public String handleSuperScripts(String str)
  {
    String s = handleSuperScripts(str, "(TM)");
    return     handleSuperScripts(s, "(R)");
  }    
  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  private String handleSuperScripts(String str, String what)
  {
    String s = "", t;
    int x, len, offset = 0;
    while(offset != -1)
    {
      offset = str.indexOf(what, offset);
      if(offset != -1)
      {
        s = "";
        x=0;
        while(x < offset)
          s += str.charAt(x++);
        t = "<sup><font size='1'>" +  what + "</font></sup>";
        s += t;
        len = str.length();
        x += what.length();
        while(x < len)
          s += str.charAt(x++);
        offset += t.length();
        str = s;
      }
    }
    if(s.length() == 0)
      s = str;

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isASCII(String str)
  {
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) >= 32 && str.charAt(x) <= 126)
        ;
      else return false;
    }
    
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isASCII(char ch)
  {
    if(ch >= 32 && ch <= 126)
      return true;
    
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isASCII(byte ch)
  {
    if(ch >= 32 && ch <= 126)
      return true;
    
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isNumericIP(String str)
  {
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      switch(str.charAt(x))
      {
        case '.' :
        case 'h' :
        case 't' :
        case 'p' :
        case ':' :
        case '/' :
        case '1' :
        case '2' :
        case '3' :
        case '4' :
        case '5' :
        case '6' :
        case '7' :
        case '8' :
        case '9' :
        case '0' : break;
        default  : return false;
      }
    }
    return true;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isNumeric(byte[] b)
  {
    return isNumeric(stringFromBytes(b, 0L));
  }
  public boolean isNumeric(String str)
  {
    int dotCount=0;  
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      switch(str.charAt(x))
      {
        case '.' : ++dotCount;
                    if(dotCount > 1) return false;
                    break;
        case '-' :
        case '1' :
        case '2' :
        case '3' :
        case '4' :
        case '5' :
        case '6' :
        case '7' :
        case '8' :
        case '9' :
        case '0' : break;
        default  : return false;
      }
    }
    return true;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isInteger(byte[] b)
  {
    return isInteger(stringFromBytes(b, 0L));
  }
  public boolean isInteger(String str)
  {
    int x, len=str.length();
    for(x=0;x<len;++x)
    {
      switch(str.charAt(x))
      {
        case '1' :
        case '2' :
        case '3' :
        case '4' :
        case '5' :
        case '6' :
        case '7' :
        case '8' :
        case '9' :
        case '0' : break;
        default  : return false;
      }
    }
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public RandomAccessFile fileOpen(String fullName)
  {
    RandomAccessFile fh;

    try
    {
      if(fileExists(fullName))
      {
        fh = new RandomAccessFile(fullName, "rw");
        return fh;
      }
      return null;
    }
    catch(Exception ioErr)
    {
      return null;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public RandomAccessFile fileOpenD(String fileName, String dirName)
  {
    RandomAccessFile fh;

    String fullName = dirName + fileName;

    try
    {
      if(fileExists(fullName))
      {
        fh = new RandomAccessFile(fullName, "rw");
        return fh;
      }
      return null;
    }
    catch(Exception ioErr)
    {
      return null;
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean fileClose(RandomAccessFile fh)
  {
    try
    {
      fh.close();
    }
    catch(Exception ioErr)
    {
      return false;
    }

    return true;
  }


  // -------------------------------------------------------------------------------------------------------------------------------
  // Prepends filename and '.' to each fldname
  public int buildFieldNamesInBuf(String fieldNames, String fileName, byte[] buf)
  {    
    int count=0;
    
    try
    {
      int x=0, y=0, z, len = fieldNames.length();
      int lenFileName = fileName.length();
    
      while(x < len)
      {
        for(z=0;z<lenFileName;++z)
          buf[y++] = (byte)fileName.charAt(z);
        buf[y++] = '.';
    
        while(x < len && fieldNames.charAt(x) != ',')
          buf[y++] = (byte)fieldNames.charAt(x++);
      
        ++x;
        while(x < len && fieldNames.charAt(x) == ' ')
          ++x;

        buf[y++] = '\000';
        ++count;
      }
    }
    catch(Exception e)
    {
      buf[0] = '\000';
      count = 0;
    }
    
    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean createDir(String dirName)
  {
    return createDir(dirName, false);
  }
  public boolean createDir(String dirName, boolean createMissing)
  {
    File newDir = new File(dirName);
    if(createMissing)
      return newDir.mkdirs();
    return newDir.mkdir();
  }

  public boolean createDir(String dirName, String permissions) throws Exception
  {
    File newDir = new File(dirName);
    if(newDir.mkdir())
    {
      Runtime runtime = Runtime.getRuntime();
      runtime.exec("chmod " + permissions + " " + dirName);
    }
    return true;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public RandomAccessFile create(String fName)
  {
    fileDelete(fName);

    FileOutputStream fos;

    try
    {
      fos = new FileOutputStream(fName);
      FileDescriptor fd = fos.getFD();
      fd.sync();
      fos.close();

      return new RandomAccessFile(fName, "rw");
    }
    catch(Exception ioErr)
    {
      System.out.println("GeneralUtils:create: " + ioErr);
      return null;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean fileDelete(String fullName)
  {
    try
    {
      File f = new File(fullName);
      if(f.delete())
        return true;
      return false;
    }
    catch(Exception ioErr)
    {
      System.out.println("generalUtils fileDelete: " + ioErr);
      return false;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean directoryDelete(String dirName)
  {
    try
    {
      File f = new File(dirName);
      if(f.delete())
        return true;
      return false;
    }
    catch(Exception ioErr)
    {
      System.out.println("generalUtils: dir delete: " + ioErr);
      return false;
    }
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void directoryHierarchyDelete(String directory)
  {
    directoryHierarchyDelete2(directory);
    directoryDelete(directory);     
  }
  public void directoryHierarchyDelete2(String directory)
  {
    try
    {
      File path = new File(directory);
      String fs[] = new String[0];
      fs = path.list();

      for(int x=0;x<fs.length;++x)
      {
        if(! isDirectory(directory + "/" + fs[x]))
          fileDelete(directory + "/" + fs[x]);
        else
        {
          directoryHierarchyDelete2(directory + "/" + fs[x] + "/");
          directoryDelete(directory + fs[x]);
        }
      }
    }
    catch(Exception e) { System.out.println(e); } 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void filesDeleteGivenPrefix(String directory, String prefix)
  {
    try
    {
      File path = new File(directory);
      String fs[] = new String[0];
      fs = path.list();

      for(int x=0;x<fs.length;++x)
      {
        if(! isDirectory(directory + fs[x]))
        {
          if(fs[x].startsWith(prefix))
            fileDelete(directory + fs[x]);
        }
      }
    }
    catch(Exception e) { System.out.println(e); } 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean fileDeleteD(String fName, String dirName)
  {
    File f = new File(dirName+fName);

    return f.delete();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String newRandom() throws Exception
  {
    long l = new Date().getTime();
    
    int iii = new Random(l).nextInt();
    
    int ii = new Random(iii).nextInt();
    if(ii < 0)
      ii *= -1;

    return intToStr(ii);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean fileDeleteAndRenameD(String oldName, String newName, String dirName)
  {
    File oldF = new File(dirName+oldName);
    File newF = new File(dirName+newName);

    boolean res = oldF.renameTo(newF);

    return res;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean isDirectory(String name) //throws Exception
  {
    try
    {
      File f = new File(name);
      return f.isDirectory();
    }
    catch(Exception e)
    {
      return false;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // Deletes the inx with the number in extns
  // Note: If the number 0 is specified then the file .CFS is deleted
  //       extns is terminated by -1
  public void deleteFiles(String fName, String dirName, short[] extns)
  {
    String extn;

    short x=0;
    while(extns[x] >= 0)
    {
      if(extns[x] == 0)
        extn = ".cfs";
      else
      {
        extn = ".";
        if(extns[x] < 10)
	        extn += "0";
        if(extns[x] < 100)
          extn += "0";
        extn += extns[x];
      }

      fileDeleteD((fName + extn), dirName);
      ++x;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean fileExists(String pathName)
  {
    File f = new File(pathName);
    return f.exists();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void copyBetweenDirectories(String fromDirectory, String toDirectory)
  {
    try
    {
      File path = new File(fromDirectory);
      String fs[];
      fs = path.list();

      for(int x=0;x<fs.length;++x)
      {
        if(! isDirectory(fromDirectory + fs[x]))
          copyFileToFile(fromDirectory + fs[x], toDirectory + fs[x]);
      }
    }
    catch(Exception e) { System.out.println(e); } 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void copyFileToFile(String fromFullFileName, String toFullFileName) throws Exception
  {
    RandomAccessFile fh = create(toFullFileName);
    fh.seek(0);

    FileInputStream fis;

    try
    {
      fis = new FileInputStream(fromFullFileName);
    }
    catch(Exception e)
    {
      fileClose(fh);
      return;
    }

    BufferedInputStream in = new BufferedInputStream(fis);

    int ch = in.read();
    while(ch != (byte)-1)
    {
      fh.writeByte((char)ch);
      ch = in.read();
    }

    in.close();
    fis.close();
    fileClose(fh);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // format is 'S' for short: "95"
  //           'L' for long: "1995"
  public String decode(int date, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] dateBuf = new byte[20]; 
    decode(date, dateBuf, localDefnsDir, defnsDir);
    return stringFromBytes(dateBuf, 0L);
  }
  public short decode(int date, byte[] dateBuf, String localDefnsDir, String defnsDir) throws Exception
  {
    short first, second;
    short[] dd = new short[1];
    short[] mm = new short[1];
    short[] yyyy = new short[1];

    char  format        = getDateLength(localDefnsDir, defnsDir);
    char  dateSeparator = getDateSeparator(localDefnsDir, defnsDir);
    short layout        = getDateFormat(localDefnsDir, defnsDir);
    short century       = getDateCentury(localDefnsDir, defnsDir);

    if(date == 0 || date == 1)
    {
      dateBuf[0] = '\000';
      return((short)0);
    }

    analyzeNumOfDays(date, dd, mm, yyyy);

    switch(layout)
    {
      case  1 : // mm dd yy
	        first = mm[0];  second = dd[0];  break;
      default : // dd mm yy
	        first = dd[0];  second = mm[0];  break;
    }

    int p=0;
    if(first < 10)
      dateBuf[p++] = '0';

    String s = String.valueOf(first);
    for(int i=0;i<s.length();++i)
      dateBuf[p++] = (byte)s.charAt(i);
    dateBuf[p++] = (byte)dateSeparator;

    if(second < 10)
      dateBuf[p++] = '0';

    s = String.valueOf(second);
    for(int i=0;i<s.length();++i)
      dateBuf[p++] = (byte)s.charAt(i);
    dateBuf[p++] = (byte)dateSeparator;

    // determine century of this date
    int x = yyyy[0] / 100;

    if(format == 'L' || x != century)
    {
      if(yyyy[0] <= 9)
      {
        dateBuf[p++] = '0';
        dateBuf[p++] = '0';
        dateBuf[p++] = '0';
      }
      else
        if(yyyy[0] <= 99)
        {
          dateBuf[p++] = '0';
          dateBuf[p++] = '0';
        }
        else
          if(yyyy[0] <= 999)
            dateBuf[p++] = '0';

      s = String.valueOf(yyyy[0]);
      for(int i=0;i<s.length();++i)
        dateBuf[p++] = (byte)s.charAt(i);
      dateBuf[p] = '\000';
    }
    else // format == 'S'
    {
      x = yyyy[0] / 100;
      yyyy[0] -= (short)(x * 100);
      if(yyyy[0] <= 9)
      {
        dateBuf[p++] = '0';
        s = String.valueOf(yyyy[0]);
        for(int i=0;i<s.length();++i)
          dateBuf[p++] = (byte)s.charAt(i);
        dateBuf[p] = '\000';
      }
      else
        if(yyyy[0] <= 99)
        {
          s = String.valueOf(yyyy[0]);
          for(int i=0;i<s.length();++i)
            dateBuf[p++] = (byte)s.charAt(i);
          dateBuf[p] = '\000';
        }
        else
          if(yyyy[0] <= 999)
          {
            s = String.valueOf(yyyy[0] / 10);
            for(int i=0;i<s.length();++i)
              dateBuf[p++] = (byte)s.charAt(i);
            dateBuf[p] = '\000';
          }
          else
          {
            s = String.valueOf(yyyy[0] / 100);
            for(int i=0;i<s.length();++i)
              dateBuf[p++] = (byte)s.charAt(i);
            dateBuf[p] = '\000';
          }
    }
    return((short)p);
  }

  // -------------------------------------------------------------------------------------------
  // if yy is 2 chars iCentury is used IF cFormat is 'S'
  public int encode(String dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] str = new byte[20];
    stringToBytes(dateStr, 0, str);
    return encode(str, localDefnsDir, defnsDir);
  }
  public int encode(byte[] dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    short dd, mm, yyyy;
    byte[] str = new byte[20];

    int len = lengthBytes(dateStr, 0);

    if(len < 6)
      return 1; // illegal date

    if(dateStr[0] == '\000')
      return(1);  // no date supplied (illegal date)

    char  format        = getDateLength(localDefnsDir, defnsDir);
    char  dateSeparator = getDateSeparator(localDefnsDir, defnsDir);
    short layout        = getDateFormat(localDefnsDir, defnsDir);
    short century       = getDateCentury(localDefnsDir, defnsDir);

    int x=0;
    while(x < len && dateStr[x] != '\000')
    {
      if(dateStr[x] != dateSeparator && (dateStr[x] < '0' || dateStr[x] > '9'))
        return(1);  // illegal date
      ++x;
    }

    x=0;   int y=0;
    while(x < len && dateStr[x] != dateSeparator && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';
    short first = (short)intFromBytesCharFormat(str, (short)0);

    ++x;   y=0;
    while(x < len && dateStr[x] != dateSeparator && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';
    short second = (short)intFromBytesCharFormat(str, (short)0);

    ++x;   y=0;
    while(x < len && dateStr[x] != '\000' && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';
    yyyy = (short)intFromBytesCharFormat(str, (short)0);

    if(yyyy <= 99)
      if(format == 'S')
        yyyy += (century * 100);

    switch(layout)
    {
      case 1  : // mm dd yy
                mm = first;  dd = second;  break;
      default : // dd mm yy
                dd = first;  mm = second;  break;
    }

    return numOfDaysTotal(dd, mm, yyyy);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public int encodeFromYYYYMMDD(String date) throws Exception
  {
    if(date.equals("1970-01-01") || date.equals("19700101"))
      return 0;
    
    int i = date.indexOf("-");
    
    if(i == -1)
    {
      date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
      i = 4;
    }
    
    int yyyy = strToInt(date.substring(0, i));
    
    ++i;
    int j = date.substring(i).indexOf("-");
    
    int mm   = strToInt(date.substring(i, (i + j)));

    ++j;
    int dd   = strToInt(date.substring(i+j));
   
    return numOfDaysTotal((short)dd, (short)mm, (short)yyyy);
  }
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String decodeToYYYYMMDD(int date) throws Exception
  {
    short[] dd = new short[1];
    short[] mm = new short[1];
    short[] yyyy = new short[1];

    analyzeNumOfDays(date, dd, mm, yyyy);
    
    String s = yyyy[0] + "-";
    
    if(mm[0] < 10)
      s += "0";
    s += (mm[0] + "-");
    
    if(dd[0] < 10)
      s += "0";
    s += dd[0];
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public String subtractNumDaysFromDate(String date, int numDays) throws Exception  
  {
    int x = encodeFromYYYYMMDD(date);
    return decodeToYYYYMMDD(x + numDays);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private int numOfDaysTotal(short dd, short mm, short yyyy) throws Exception
  {
    int total = 0;

    if(yyyy > 0)
    {
      for(short x=0;x<yyyy;++x)
        total += numOfDaysInYear(x);
    }

    for(short x=1;x<mm;++x)
      total += numOfDaysInMonth(x, yyyy);

    total += dd;

    return total;
  }

  // -------------------------------------------------------------------------------------------
  public short numOfDaysInMonth(short mm, short yyyy) throws Exception
  {
    if(mm == 4 || mm == 6 || mm == 9 || mm == 11)
      return 30;

    if(mm == 2)
    {
      if(isLeapYear(yyyy))
        return 29;
      return 28;
    }

    return 31;
  }

  // -------------------------------------------------------------------------------------------
  private void analyzeNumOfDays(int date, short[] dd, short[] mm, short[] yyyy) throws Exception
  {
    yyyy[0] = 0;
    while(date > 0)
    {
      date -= numOfDaysInYear(yyyy[0]);
      ++ yyyy[0];
    }
    --yyyy[0];
    date += numOfDaysInYear(yyyy[0]);

    short days = (short)date;

    if(days <= 31) { mm[0] = 1; dd[0] = days; return; }
    days -= 31;

    if(isLeapYear(yyyy[0]))
    {
      if(days <= 29) { mm[0] = 2; dd[0] = days; return; }
      days -= 29;
    }
    else
    {
      if(days <= 28) { mm[0] = 2; dd[0] = days; return; }
      days -= 28;
    }

    if(days <= 31) { mm[0] = 3; dd[0] = days; return; }
    days -= 31;

    if(days <= 30) { mm[0] = 4; dd[0] = days; return; }
    days -= 30;

    if(days <= 31) { mm[0] = 5; dd[0] = days; return; }
    days -= 31;

    if(days <= 30) { mm[0] = 6; dd[0] = days; return; }
    days -= 30;

    if(days <= 31) { mm[0] = 7; dd[0] = days; return; }
    days -= 31;

    if(days <= 31) { mm[0] = 8; dd[0] = days; return; }
    days -= 31;

    if(days <= 30) { mm[0] = 9; dd[0] = days; return; }
    days -= 30;

    if(days <= 31) { mm[0] = 10; dd[0] = days; return; }
    days -= 31;

    if(days <= 30) { mm[0] = 11; dd[0] = days; return; }
    days -= 30;

    mm[0] = 12; dd[0] = days;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public short numOfDaysInYear(short yyyy) throws Exception
  {
    if(isLeapYear(yyyy))
      return 366;
    return 365;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isLeapYear(short yyyy) throws Exception
  {
    if(yyyy < (short)50)
      yyyy += (short)2000;
    else
    if(yyyy >= (short)50 && yyyy < (short)100)
      yyyy += (short)1900;
     
    if(yyyy == 2000) return true;

    short x = (short)(yyyy / 4);
    if((x * 4) != yyyy)
      return false;
    x = (short)(yyyy / 1000);
    if((x * 1000) == yyyy)
      return false;
    return true;
  }

  // -------------------------------------------------------------------------------------------
  public int todayEncoded(String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] b;

    String t = today(localDefnsDir, defnsDir);

    b = t.getBytes();
    return encode(b, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------
  public String today(String localDefnsDir, String defnsDir) throws Exception
  {
    int   first=0, second=0, dd, mm, yy;
    char  format, dateSeparator;
    short layout;

    format        = getDateLength(localDefnsDir, defnsDir);
    dateSeparator = getDateSeparator(localDefnsDir, defnsDir);
    layout        = getDateFormat(localDefnsDir, defnsDir);

    GregorianCalendar g = new GregorianCalendar();
    Integer i1 = new Integer(g.get(GregorianCalendar.DAY_OF_MONTH));
    dd = i1.intValue();

    Integer i2 = new Integer(g.get(GregorianCalendar.MONTH));
    mm = i2.intValue() + 1;

    Integer i3 = new Integer(g.get(GregorianCalendar.YEAR));
    yy = i3.intValue();

    switch(layout)
    {
      case 0 : // dd mm yy
               first = dd;  second = mm;  break;
      case 1 : // mm dd yy
               first = mm;  second = dd;  break;
    }

    String str = "";
    if(first < 10)
      str = "0";

    str += first;
    str += dateSeparator;

    if(second < 10)
      str += "0";

    str += second;
    str += dateSeparator;

    String year = "" + yy;
    if(format == 'S')
      year = year.substring(2);

    str += year;

    return str;
  }

  // -------------------------------------------------------------------------------------------
  public void today(String localDefnsDir, String defnsDir, byte[] date) throws Exception
  {
    stringToBytes(today(localDefnsDir, defnsDir), 0, date);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void todaySQLFormat(String localDefnsDir, String defnsDir, byte[] date) throws Exception
  {
    today(localDefnsDir, defnsDir, date);
    convertToYYYYMMDD(date);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String todaySQLFormat(String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] date = new byte[20];
    today(localDefnsDir, defnsDir, date);
    convertToYYYYMMDD(date);
    return stringFromBytes(date, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public short getMonth(int date) throws Exception
  {
    short[] dd = new short[1];
    short[] mm = new short[1];
    short[] yy = new short[1];

    analyzeNumOfDays(date, dd, mm, yy);
    return mm[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public short getYear(int date) throws Exception
  {
    short[] dd = new short[1];
    short[] mm = new short[1];
    short[] yy = new short[1];

    analyzeNumOfDays(date, dd, mm, yy);
    return yy[0];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public short getDay(int date) throws Exception
  {
    short[] dd = new short[1];
    short[] mm = new short[1];
    short[] yy = new short[1];

    analyzeNumOfDays(date, dd, mm, yy);
    return dd[0];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getDayOfWeek(String date, int[] dayOfWeek, String localDefnsDir, String defnsDir) throws Exception
  {
    int dd=0, mm=0, yy;

    char separator = getDateSeparator(localDefnsDir, defnsDir);
    int  century   = getDateCentury(localDefnsDir, defnsDir);
    int  layout    = getDateFormat(localDefnsDir, defnsDir);

    short x=0;

    String str = "";
    while(x < date.length() && date.charAt(x) != separator && date.charAt(x) != '\000') // just-in-case
      str += date.charAt(x++);

    if(layout == 0)
      dd = strToInt(str);
    else mm = strToInt(str);
    ++x; // separator

    str = "";
    while(x < date.length() && date.charAt(x) != separator && date.charAt(x) != '\000') // just-in-case
      str += date.charAt(x++);

    if(layout == 0)
      mm = strToInt(str);
    else dd = strToInt(str);
    ++x; // separator

    --mm;

    str = "";
    while(x < date.length() && date.charAt(x) != '\000')
      str += date.charAt(x++);

    yy = strToInt(str);
    if(yy > 100)
      yy -= (century * 100);

    GregorianCalendar g = new GregorianCalendar(yy, mm, dd);
    Integer i = new Integer(g.get(GregorianCalendar.DAY_OF_WEEK));
    dayOfWeek[0] = i.intValue();

    switch(dayOfWeek[0])
    {
      case 1 : str = "Sunday";      break;
      case 2 : str = "Monday";      break;
      case 3 : str = "Tuesday";     break;
      case 4 : str = "Wednesday";   break;
      case 5 : str = "Thursday";    break;
      case 6 : str = "Friday";      break;
      case 7 : str = "Saturday";    break;
    }

    return(str);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Format of monthYear is "July 1996"
  // Rtns: month as 1 to 12 (0 if no match - jic)
  //       year as long format (e.g., 1996)
  public void monthYearStrToYearAndMonth(String monthYear, int[] month, int[] year) throws Exception
  {
    if(monthYear.startsWith("January"))   month[0] =  1; else
    if(monthYear.startsWith("February"))  month[0] =  2; else
    if(monthYear.startsWith("March"))     month[0] =  3; else
    if(monthYear.startsWith("April"))     month[0] =  4; else
    if(monthYear.startsWith("May"))       month[0] =  5; else
    if(monthYear.startsWith("June"))      month[0] =  6; else
    if(monthYear.startsWith("July"))      month[0] =  7; else
    if(monthYear.startsWith("August"))    month[0] =  8; else
    if(monthYear.startsWith("September")) month[0] =  9; else
    if(monthYear.startsWith("October"))   month[0] = 10; else
    if(monthYear.startsWith("November"))  month[0] = 11; else
    if(monthYear.startsWith("December"))  month[0] = 12; else month[0] = 0;

    year[0] = strToInt(monthYear.substring(monthYear.indexOf(" ")+1));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Format of monthYear is "7-1996"
  // Rtns: month as 1 to 12 (0 if no match - jic)
  //       year as long format (e.g., 1996)
  public void monthYearStrToYearAndMonth2(String monthYear, int[] month, int[] year) throws Exception
  {
    int len = monthYear.length();
    int x=0;
    String m="";
    while(x < len && monthYear.charAt(x) != '-')
      m += monthYear.charAt(x++);

    month[0] = strToInt(m);

    year[0] = strToInt(monthYear.substring(monthYear.indexOf("-")+1));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Month is 1 to 12, Year is long format (e.g., 1996)
  // Rtns: string as "July 1996"
  public String yearAndMonthToMonthYearStr(int month, int year) throws Exception
  {
    String s = "";

    switch(month)
    {
      case  1 : s = "January";   break;
      case  2 : s = "February";  break;
      case  3 : s = "March";     break;
      case  4 : s = "April";     break;
      case  5 : s = "May";       break;
      case  6 : s = "June";      break;
      case  7 : s = "July";      break;
      case  8 : s = "August";    break;
      case  9 : s = "September"; break;
      case 10 : s = "October";   break;
      case 11 : s = "November";  break;
      case 12 : s = "December";  break;
    }

    if(year < 10)
      return(s + " 0" + year);
    return(s + " " + year);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // monthYear is "5-2007"
  // Rtns: string as "July 1996"
  public String monthYearToMonthYearStr(String monthYear) throws Exception
  {
    if(monthYear.equalsIgnoreCase("ALL"))
      return "Start to Finish";

    String month = monthYear.substring(0, monthYear.indexOf("-"));
    String year  = monthYear.substring(monthYear.indexOf("-") + 1);

    String s = "";

    if(month.equals("1"))  s = "January";
    else if(month.equals("2"))  s = "February";
    else if(month.equals("3"))  s = "March";   
    else if(month.equals("4"))  s = "April";
    else if(month.equals("5"))  s = "May";
    else if(month.equals("6"))  s = "June";
    else if(month.equals("7"))  s = "July";
    else if(month.equals("8"))  s = "August";
    else if(month.equals("9"))  s = "September";
    else if(month.equals("10")) s = "October";
    else if(month.equals("11")) s = "November";
    else if(month.equals("12")) s = "December";

    return(s + " " + year);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Tue Jul 5 10:48:11 2005
  public String timeStamp(String localDefnsDir, String defnsDir) throws Exception
  {
    String today = today(localDefnsDir, defnsDir);

    String year="";
    year += today.charAt(6);
    year += today.charAt(7);
    int yy = strToInt(year);

    String str = "";
    str += today.charAt(3);
    str += today.charAt(4);
    int mm = strToInt(str);

    str = "";
    str += today.charAt(0);
    str += today.charAt(1);
    int dd = strToInt(str);

    String month="";
    switch(mm)
    {
      case  1 : month = "Jan"; break;
      case  2 : month = "Feb"; break;
      case  3 : month = "Mar"; break;
      case  4 : month = "Apr"; break;
      case  5 : month = "May"; break;
      case  6 : month = "Jun"; break;
      case  7 : month = "Jul"; break;
      case  8 : month = "Aug"; break;
      case  9 : month = "Sep"; break;
      case 10 : month = "Oct"; break;
      case 11 : month = "Nov"; break;
      case 12 : month = "Dec"; break;
    }

    if(yy >= 00 && yy <= 50)
      yy += 2000;
    GregorianCalendar g = new GregorianCalendar(yy, (mm - 1), dd);
    Integer i = new Integer(g.get(GregorianCalendar.DAY_OF_WEEK));

    String day="";
    switch(i.intValue())
    {
      case 1 : day = "Sun"; break;
      case 2 : day = "Mon"; break;
      case 3 : day = "Tue"; break;
      case 4 : day = "Wed"; break;
      case 5 : day = "Thu"; break;
      case 6 : day = "Fri"; break;
      case 7 : day = "Sat"; break;
    }

    String time = timeNow(4, "");

    // Tue Jul 5 10:48:11 2005
    return day + " " + month + " " + dd + " " + time.charAt(0) + time.charAt(1) + ":" + time.charAt(2) + time.charAt(3) + ":" + time.charAt(4) + time.charAt(5) + " " + yy;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String yymmddExpandGivenSQLFormat(boolean wantDay, String yyyymmdd) throws Exception
  {
    String s = "" + yyyymmdd.charAt(2) + yyyymmdd.charAt(3) + yyyymmdd.charAt(5) + yyyymmdd.charAt(6) + yyyymmdd.charAt(8) + yyyymmdd.charAt(9);
    return yymmddExpand(wantDay, s);
  } 
 
  // Format of yymmdd is "970131"
  // Rtns: Wednesday, 31 January 97
  //   or, 31 January 97
  public String yymmddExpand(boolean wantDay, String yymmdd) throws Exception
  {
    String str = "";
    str += yymmdd.charAt(0);
    str += yymmdd.charAt(1);
    int yy = strToInt(str);

    str = "";
    str += yymmdd.charAt(2);
    str += yymmdd.charAt(3);
    int mm = strToInt(str);

    str = "";
    str += yymmdd.charAt(4);
    str += yymmdd.charAt(5);

    String str2 = str + " " + yearAndMonthToMonthYearStr(mm, yy);

    if(wantDay)
    {
      int dd = strToInt(str);

      if(yy >= 00 && yy <= 50)
        yy += 2000;
      GregorianCalendar g = new GregorianCalendar(yy, (mm - 1), dd);
      Integer i = new Integer(g.get(GregorianCalendar.DAY_OF_WEEK));

      switch(i)
      {
        case 1 : str2 = "Sunday, "    + str2;  break;
        case 2 : str2 = "Monday, "    + str2;  break;
        case 3 : str2 = "Tuesday, "   + str2;  break;
        case 4 : str2 = "Wednesday, " + str2;  break;
        case 5 : str2 = "Thursday, "  + str2;  break;
        case 6 : str2 = "Friday, "    + str2;  break;
        case 7 : str2 = "Saturday, "  + str2;  break;
      }
    }

    return str2;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // format == 1 for "3:06 PM"
  //           2 for "1506"
  //           3 for "15:06"
  //           4 for "150659"
  //           5 for "15:06:59"
  public void timeNow(int format, String separatorChar, byte[] time) throws Exception
  {
    strToBytes(time, timeNow(format, separatorChar));
  }
  public String timeNow(int format, String separatorChar) throws Exception
  {
    GregorianCalendar g = new GregorianCalendar();

    int amPm = g.get(java.util.GregorianCalendar.AM_PM);

    String s="";
    Integer i = new Integer(g.get(GregorianCalendar.HOUR));
    int j = i.intValue();

    if(format == 1)
    {
      if(j == 0)
        s = "12";
      else s += i.toString();

      s += separatorChar;
    }
    else
    if(format == 2 || format == 3 || format == 5)
    {
      if(amPm != 0)
        j += 12;
      s = intToStr(j);
      if(format == 3 || format == 5)
        s += separatorChar;
    }
    else
    if(format == 4)
    {
      if(amPm == 1) // pm
      {
        j += 12;
        s += intToStr(j);
      }
      else // am
      {
        if(j < 10)
          s += "0";

        s += i.toString();
      }

      s += separatorChar;
    }

    Integer i2 = new Integer(g.get(GregorianCalendar.MINUTE));
    if(i2.intValue() < 10)
      s += "0";

    s += i2.toString();

    if(format == 1)
    {
      if(amPm == 0)
        s += " AM";
      else s += " PM";
    }

    if(format == 5)
      s += separatorChar;

    if(format == 4 || format == 5)
    {
      Integer i3 = new Integer(g.get(GregorianCalendar.SECOND));
      if(i3.intValue() < 10)
       s += "0";
     s += i3.toString();
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void timeNowSQLFormat(byte[] time) throws Exception
  {
    timeNow(4, "", time);
    convertToHHMMSS(time, lengthBytes(time, 0));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String timeNowSQLFormat() throws Exception
  {
    byte[] time = new byte[10];
    timeNow(4, "", time);
    convertToHHMMSS(time, lengthBytes(time, 0));
    return stringFromBytes(time, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int timeNowInSecs() throws Exception
  {
    // Get time now (as HH MM SS) and convert to a second-count
    GregorianCalendar g = new GregorianCalendar();

    Integer i = new Integer(g.get(GregorianCalendar.HOUR_OF_DAY));
    int j = i.intValue();
    int secs = j * 3600;

    int amPm = g.get(java.util.GregorianCalendar.AM_PM);

    if(amPm == 1)
      j += 43200; // 12 hours of seconds

    Integer i2 = new Integer(g.get(GregorianCalendar.MINUTE));
    j = i2.intValue();
    secs += (j * 60);

    Integer i3 = new Integer(g.get(GregorianCalendar.SECOND));
    j = i3.intValue();
    secs += j;

    return secs;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Convert time (as "HH:MM:SS") to a second-count
  public long convertTimeToSecs(String timeStr) throws Exception
  {
    String s="";
    int x=0;
    int len = timeStr.length();
    while(x < len && timeStr.charAt(x) != ':')
      s += timeStr.charAt(x++);
    long secs = strToInt(s) * 3600;

    ++x;
    s="";
    while(x < len && timeStr.charAt(x) != ':')
      s += timeStr.charAt(x++);
    secs += (strToInt(s) * 60);
    
    ++x;
    s="";
    while(x < len)
      s += timeStr.charAt(x++);
    secs += strToInt(s);

    return secs;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String convertSecsToHoursMinsAndSecs(int secs) throws Exception
  {
    int hours = 0, i = secs;
    
    while(i >= 3600)
    {
      ++hours;
      i -= 3600;
    }
    
    i = secs - (hours * 3600);
    int mins = 0;
    
    while(i >= 60)
    {
      ++mins;
      i -= 60;
    }
    
    return hours + "h " + mins + "m " + (secs - (hours * 3600) - (mins * 60)) + "s";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // format == 1 for "3:06 PM"
  //           3 for "15:06"
  public void decodeTime(int format, String timeStr, String separatorChar, byte[] time) throws Exception
  {
    strToBytes(time, decodeTime(format, timeStr, separatorChar));
  }
  public String decodeTime(int format, String timeStr, String separatorChar) throws Exception
  {
    String hours, mins;

    if(timeStr.length() == 0) // just-in-case
      return "00:00";

    if(timeStr.length() == 1)
    {
      if(format == 1)
        hours = "0";
      else hours = "00";

      mins = "0" + timeStr.charAt(0);
    }
    else
    if(timeStr.length() == 2)
    {
      if(format == 1)
        hours = "0";
      else hours = "00";

      mins = "" + timeStr.charAt(1);
    }
    else
    if(timeStr.length() == 3)
    {
      hours = "0" + timeStr.charAt(0);
      mins  = timeStr.substring(1);
    }
    else // 4
    {
      hours = timeStr.substring(0, 2);
      mins  = timeStr.substring(2);
    }

    String s;
    if(format == 1)
      s = hours + separatorChar + mins;
    else s = hours + mins;

    if(format == 1)
    {
      if(intFromStr(timeStr) < 1200)
        s += " AM";
      else s += " PM";
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns the date of the first day of the month-before that of the given date
  public void previousMonthStart(byte[] theDate, byte[] previous, String localDefnsDir, String defnsDir) throws Exception
  {
    int date   = encode(theDate, localDefnsDir, defnsDir);
    short dd   = getDay(date);
    short mm   = getMonth(date);
    short yyyy = getYear(date);

    if(mm == 1)
    {
      mm = 12;
      --yyyy;
    }
    else --mm;

    short numPrevMM = numOfDaysInMonth(mm, yyyy);
    date -= (dd - 1);
    date -= numPrevMM;
    decode(date, previous, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns the date of the first day of the month-before that of the given date
  public String previousMonthStartYYYYMMDD(String theDate) throws Exception
  {
    int date   = encodeFromYYYYMMDD(theDate);
    short dd   = getDay(date);
    short mm   = getMonth(date);
    short yyyy = getYear(date);

    if(mm == 1)
    {
      mm = 12;
      --yyyy;
    }
    else --mm;

    short numPrevMM = numOfDaysInMonth(mm, yyyy);
    date -= (dd - 1);
    date -= numPrevMM;
    
    return decodeToYYYYMMDD(date);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns the date of the last day of the month-before that of the given date
  public void previousMonthEnd(byte[] date, byte[] previous, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] start = new byte[20];

    previousMonthStart(date, start, localDefnsDir, defnsDir);
    String lastDayOfPreviousMonth = lastDayOfMonth(stringFromBytes(start, 0L), localDefnsDir, defnsDir);
    strToBytes(previous, lastDayOfPreviousMonth);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // rtns the date of the first day of the current month for the given date
  public String thisMonthStart(String theDate, String localDefnsDir, String defnsDir) throws Exception
  {
    int x=0, len = theDate.length();
    while(x < len && theDate.charAt(x) != '.')
      ++x;

    String s = "01";
    s += theDate.substring(x);
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String lastDayOfMonth(String dateStart, String localDefnsDir, String defnsDir) throws Exception
  {
    int x=0, len = dateStart.length();
    while(x < len && dateStart.charAt(x) != '.')
      ++x;
    
    int y = x + 1;
    String month="";
    while(y < len && dateStart.charAt(y) != '.')
      month += dateStart.charAt(y++);
    
    ++y;
    String year="";
    while(y < len && dateStart.charAt(y) != '.')
      year += dateStart.charAt(y++);
    
    short lastDay = numOfDaysInMonth((short)intFromStr(month), (short)intFromStr(year));
    
    return lastDay + dateStart.substring(x);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String firstDayOfMonthYYYYMMDD(String dateEnd) throws Exception
  {
    int x=0, len = dateEnd.length();
    String year = "";
    while(x < len && dateEnd.charAt(x) != '-')
      year += dateEnd.charAt(x++);

    ++x;
    String month = "";
    while(x < len && dateEnd.charAt(x) != '-')
      month += dateEnd.charAt(x++);
    
    return year + "-" + month + "-01";
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String lastDayOfMonthYYYYMMDD(String dateStart) throws Exception
  {
    int x=0, len = dateStart.length();
    String year = "";
    while(x < len && dateStart.charAt(x) != '-')
      year += dateStart.charAt(x++);

    ++x;
    String month = "";
    while(x < len && dateStart.charAt(x) != '-')
      month += dateStart.charAt(x++);
    
    short lastDay = numOfDaysInMonth((short)intFromStr(month), (short)intFromStr(year));
    
    return year + "-" + month + "-" + lastDay;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public short detMonthNumFromMonthName(String month) throws Exception
  {
    byte[] b = new byte[20];
    strToBytes(b, month);
    
    return detMonthNumFromMonthName(b);
  }
  public short detMonthNumFromMonthName(byte[] month) throws Exception
  {
    if(match(month, "January"))   return 1;
    if(match(month, "February"))  return 2;
    if(match(month, "March"))     return 3;
    if(match(month, "April"))     return 4;
    if(match(month, "May"))       return 5;
    if(match(month, "June"))      return 6;
    if(match(month, "July"))      return 7;
    if(match(month, "August"))    return 8;
    if(match(month, "September")) return 9;
    if(match(month, "October"))   return 10;
    if(match(month, "November"))  return 11;
    return 12;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void detMonthNameFromMonthNum(short monthNum, byte[] month) throws Exception
  {
    switch(monthNum)
    {
      case  1 : catAsBytes("January",   0, month, true);   break;
      case  2 : catAsBytes("February",  0, month, true);   break;
      case  3 : catAsBytes("March",     0, month, true);   break;
      case  4 : catAsBytes("April",     0, month, true);   break;
      case  5 : catAsBytes("May",       0, month, true);   break;
      case  6 : catAsBytes("June",      0, month, true);   break;
      case  7 : catAsBytes("July",      0, month, true);   break;
      case  8 : catAsBytes("August",    0, month, true);   break;
      case  9 : catAsBytes("September", 0, month, true);   break;
      case 10 : catAsBytes("October",   0, month, true);   break;
      case 11 : catAsBytes("November",  0, month, true);   break;
      default : catAsBytes("December",  0, month, true);   break;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // calcs the num of months between two dates; inclusive of start and end months
  public short numMonths(byte[] dateFrom, byte[] dateTo, String localDefnsDir, String defnsDir) throws Exception
  {
    int   i1 = encode(dateTo, localDefnsDir, defnsDir);
    short m1 = getMonth(i1);
    short y1 = getYear(i1);

    int   i2 = encode(dateFrom, localDefnsDir, defnsDir);
    short m2 = getMonth(i2);
    short y2 = getYear(i2);

    if(i1 == i2)
      return 1;

    if(i1 < i2)
      return 0;

    short m;
    if(m1 <= m2)
    {
      m = (short)(12 - (m2 - m1));
      m += (short)((--y1 - y2) * 12);
    }
    else m = (short)((m1 - m2) + ((y1 - y2) * 12));

    return ++m;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // calcs the num of months between two monthstrs ('7-1994' style); inclusive of start and end months
  public int numMonths(String monthFrom, String monthTo, String localDefnsDir, String defnsDir) throws Exception
  {
    int[] month1 = new int[1];
    int[] month2 = new int[1];
    int[] year1  = new int[1];
    int[] year2  = new int[1];

    monthYearStrToYearAndMonth2(monthFrom, month1, year1);
    monthYearStrToYearAndMonth2(monthTo,   month2, year2);

    int numMonths = ((year2[0] - year1[0]) - 1) * 12;

    numMonths += ((12 - month1[0]) + 1);

    numMonths += month2[0];

    return numMonths;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // adds numofmonths months onto start date
  public int addMonths(int date, short numMonths, String localDefnsDir, String defnsDir) throws Exception
  {
    short yy, mm, dd;
    String s;

    for(short x=0;x<numMonths;++x)
    {
      yy = getYear(date);
      mm = getMonth(date);
      dd = getDay(date);
      ++mm;
      if(mm == 13)
      {
        mm = 1;
        ++yy;
      }

      if(dd == 31)
      {
        if(mm == 2)
        {
          if(isLeapYear(yy))
            dd = 28;
          else dd = 27;
        }
        else dd = 30;
      }
      else
      if(dd == 30)
      {
        if(mm == 2)
        {
          if(isLeapYear(yy))
            dd = 28;
          else dd = 27;
        }
        else dd = 29;
      }
      else --dd;

      s = dd + "." + mm + "." + yy;
      date = encode(s, localDefnsDir, defnsDir);
    }

    return date;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void addOneMonth(byte[] dateNow, byte[] datePlusOneMonth, String localDefnsDir, String defnsDir) throws Exception
  {
    int date = encode(dateNow, localDefnsDir, defnsDir);
    short dd = getDay(date);
    short mm = getMonth(date);
    short yyyy = getYear(date);
    short lastDay = numOfDaysInMonth(mm, yyyy);
    date += (lastDay - dd);
    decode(date, datePlusOneMonth, localDefnsDir, defnsDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private short getDateFormat(String localDefnsDir, String defnsDir) throws Exception
  {
    String s = getFromDefnFile("FORMAT", "dates.dfn", localDefnsDir, defnsDir);

    if(s.length() == 0) // just-in-case
      return 0; // ddmmyy

    if(s.equalsIgnoreCase("MMDDYY"))
      return 1;
    else
    if(s.equalsIgnoreCase("YYYYMMDD"))
      return 2;
    return 0; // ddmmyy
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private char getDateLength(String localDefnsDir, String defnsDir) throws Exception
  {
    String s;
    s = getFromDefnFile("LENGTH", "dates.dfn", localDefnsDir, defnsDir);

    if(s.length() == 0) // just-in-case
      return 'S'; // short

    if(s.equalsIgnoreCase("Long"))
      return 'L';
    return 'S'; // short
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private char getDateSeparator(String localDefnsDir, String defnsDir) throws Exception
  {
    String s = getFromDefnFile("SEPARATOR", "dates.dfn", localDefnsDir, defnsDir);

    if(s.length() == 0) // just-in-case
      return '.';

    return s.charAt(0);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private short getDateCentury(String localDefnsDir, String defnsDir) throws Exception
  {
    String s;
    s = getFromDefnFile("CENTURY", "dates.dfn", localDefnsDir, defnsDir);

    if(s.length() == 0) // just-in-case
      return 20;

    return (short)strToInt(s);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean validateDate(boolean noBlankDate, String dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] b = new byte[dateStr.length() + 1];
    strToBytes(b, dateStr);
    return validateDate(noBlankDate, b, localDefnsDir, defnsDir);
  }
  public boolean validateDate(byte[] dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    return validateDate(true, dateStr, localDefnsDir, defnsDir);
  }
  public boolean validateDate(boolean noBlankDate, byte[] dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    if(dateStr[0] == '\000')
    {
      if(noBlankDate)
        return false;  // no date supplied (illegal date)
      return true;
    }

    char separator = getDateSeparator(localDefnsDir, defnsDir);

    int x=0;
    while(dateStr[x] != '\000')
    {
      if( ( dateStr[x] < '0' || dateStr[x] > '9' ) && dateStr[x] != separator)
        return false;
      ++x;
    }

    short dd, mm, yyyy;
    byte[] str = new byte[20];

    int len = lengthBytes(dateStr, 0);

    char  format  = getDateLength(localDefnsDir, defnsDir);
    short layout  = getDateFormat(localDefnsDir, defnsDir);
    short century = getDateCentury(localDefnsDir, defnsDir);

    x=0;   int y=0;
    while(x < len && dateStr[x] != separator && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';
    short first = (short)intFromBytesCharFormat(str, (short)0);

    ++x;   y=0;
    while(x < len && dateStr[x] != separator && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';
    short second = (short)intFromBytesCharFormat(str, (short)0);

    ++x;   y=0;
    while(x < len && dateStr[x] != '\000' && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';
    yyyy = (short)intFromBytesCharFormat(str, (short)0);

    if(yyyy <= 99)
      if(format == 'S')
        yyyy += (century * 100);

    switch(layout)
    {
      case 1  : // mm dd yy
                mm = first;  dd = second;  break;
      default : // dd mm yy
                dd = first;  mm = second;  break;
    }

    if(dd == 0 || mm == 0)
      return false;

    switch(mm)
    {
      case  1 :
      case  3 :
      case  5 :
      case  7 :
      case  8 :
      case 10 :
      case 12 : if(dd > 31) return false;
                break;
      case  4 :
      case  6 :
      case  9 :
      case 11 : if(dd > 30) return false;
                break;
      case  2 : if(! isLeapYear(yyyy))
                {
                  if(dd > 28)
                    return false;
                }
                else // leap year
                if(dd > 29)
                  return false;
                break;
      default : return false;
    }

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean validateDateSQL(boolean noBlankDate, String dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    byte[] b = new byte[dateStr.length() + 1];
    strToBytes(b, dateStr);
    return validateDateSQL(noBlankDate, b, localDefnsDir, defnsDir);
  }
  public boolean validateDateSQL(byte[] dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    return validateDate(true, dateStr, localDefnsDir, defnsDir);
  }
  public boolean validateDateSQL(boolean noBlankDate, byte[] dateStr, String localDefnsDir, String defnsDir) throws Exception
  {
    if(dateStr[0] == '\000')
    {
      if(noBlankDate)
        return false;  // no date supplied (illegal date)
      return true;
    }

    byte[] str = new byte[20];
    int len = lengthBytes(dateStr, 0);

    int x=0;   
    int y=0;
    while(x < len && dateStr[x] != '-' && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';

    if(y != 4)
      return false;
        
    short yyyy = (short)intFromBytesCharFormat(str, (short)0);
    
    if(yyyy < 1970)
      return false;

    ++x;   y=0;
    while(x < len && dateStr[x] != '-' && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';

    if(y != 1 && y != 2)
      return false;
        
    short mm = (short)intFromBytesCharFormat(str, (short)0);
    
    if(mm < 1 || mm > 12)
      return false;

    ++x;   y=0;
    while(x < len && dateStr[x] != '\000' && y < 20)  // just-in-case
      str[y++] = dateStr[x++];
    str[y] = '\000';

    if(y != 1 && y != 2)
      return false;
        
    short dd = (short)intFromBytesCharFormat(str, (short)0);

    switch(mm)
    {
      case  1 :
      case  3 :
      case  5 :
      case  7 :
      case  8 :
      case 10 :
      case 12 : if(dd > 31) return false;
                break;
      case  4 :
      case  6 :
      case  9 :
      case 11 : if(dd > 30) return false;
                break;
      case  2 : if(! isLeapYear(yyyy))
                {
                  if(dd > 28)
                    return false;
                }
                else // leap year
                if(dd > 29)
                  return false;
                break;
      default : return false;
    }

    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // monthYear == "August 2001"
  // rtns true if date in in the specified month/year
  public boolean dateMatches(int date, String monthYear) throws Exception
  {
    if(monthYear.length() == 0)
      return true;
      
    short month = getMonth(date);
    short year  = getYear(date);

    String monthStr="", yearStr="";
    int x=0;
    while(monthYear.charAt(x) != '-')
      monthStr += monthYear.charAt(x++);
    ++x;
    while(x < monthYear.length())
      yearStr += monthYear.charAt(x++);

    if(month == strToInt(monthStr) && year == strToInt(yearStr))
      return true;
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Format of monthYear is "July 1996"
  // Rtns: first of month encoded, and last of month encoded
  public void monthYearStrToEncoded(String monthYear, int[] firstDateEncoded, int[] lastDateEncoded, String localDefnsDir, String defnsDir)
                                    throws Exception
  {
    String monthStr="", yearStr="";
    int x=0;
    while(monthYear.charAt(x) != '-')
      monthStr += monthYear.charAt(x++);
    ++x;
    while(x < monthYear.length())
      yearStr += monthYear.charAt(x++);

    firstDateEncoded[0] = encode("1." + monthStr + "." + yearStr, localDefnsDir, defnsDir);

    int numDays = numOfDaysInMonth((short)intFromStr(monthStr), (short)intFromStr(yearStr));
    lastDateEncoded[0] = encode(intToStr(numDays) + "." + monthStr + "." + yearStr, localDefnsDir, defnsDir);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Format of monthYear is "July 1996"
  // Rtns: month as 1 to 12 (0 if no match - jic)
  //       year as long format (e.g., 1996)
  public void monthYearStrToYYYYMMDDDates2(String monthYear, String[] dateFrom, String dateTo[]) throws Exception
  {
    if(monthYear.equalsIgnoreCase("ALL"))
    {
      dateFrom[0] = "1970-01-01"; 
      dateTo[0]   = "2099-12-31";
      return;
    }
      
    String year = monthYear.substring(monthYear.indexOf(" ") + 1);

    String month, numDays;
    
         if(monthYear.startsWith("January"))   { month = "01"; numDays = "31"; } 
    else if(monthYear.startsWith("March"))     { month = "03"; numDays = "31"; }
    else if(monthYear.startsWith("April"))     { month = "04"; numDays = "30"; }
    else if(monthYear.startsWith("May"))       { month = "05"; numDays = "31"; }
    else if(monthYear.startsWith("June"))      { month = "06"; numDays = "30"; }
    else if(monthYear.startsWith("July"))      { month = "07"; numDays = "31"; }
    else if(monthYear.startsWith("August"))    { month = "08"; numDays = "31"; }
    else if(monthYear.startsWith("September")) { month = "09"; numDays = "30"; }
    else if(monthYear.startsWith("October"))   { month = "10"; numDays = "31"; }
    else if(monthYear.startsWith("November"))  { month = "11"; numDays = "30"; }
    else if(monthYear.startsWith("December"))  { month = "12"; numDays = "31"; }
    else // if(monthYear.startsWith("February"))  
    { 
      month = "02";
      
      if(isLeapYear((short)strToInt(year)))
        numDays = "29"; 
      else numDays = "28"; 
    } 
    
    dateFrom[0] = year + "-" + month + "-01"; 
    dateTo[0]   = year + "-" + month + "-" + numDays; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // Format of monthYear is "1-1996"
  // Rtns: month as 1 to 12 (0 if no match - jic)
  //       year as long format (e.g., 1996)
  public void monthYearStrToYYYYMMDDDates(String monthYear, String[] dateFrom, String dateTo[]) throws Exception
  {
    if(monthYear.equalsIgnoreCase("ALL"))
    {
      dateFrom[0] = "1970-01-01"; 
      dateTo[0]   = "2099-12-31";
      return;
    }
      
    String year  = monthYear.substring(monthYear.indexOf("-") + 1);
    String month = monthYear.substring(0, monthYear.indexOf("-"));

    String numDays;
    
         if(month.equals("1"))  numDays = "31"; 
    else if(month.equals("3"))  numDays = "31";
    else if(month.equals("4"))  numDays = "30";
    else if(month.equals("5"))  numDays = "31";
    else if(month.equals("6"))  numDays = "30";
    else if(month.equals("7"))  numDays = "31";
    else if(month.equals("8"))  numDays = "31";
    else if(month.equals("9"))  numDays = "30";
    else if(month.equals("10")) numDays = "31";
    else if(month.equals("11")) numDays = "30";
    else if(month.equals("12")) numDays = "31";
    else // if(monthYear.equals("2"))  
    { 
      if(isLeapYear((short)strToInt(year)))
        numDays = "29"; 
      else numDays = "28"; 
    } 
    
    dateFrom[0] = year + "-" + month + "-01"; 
    dateTo[0]   = year + "-" + month + "-" + numDays; 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean createDefnFile(String fileName, String dirName)
  {
    try
    {
      RandomAccessFile fh = create(dirName + fileName);
      fh.close();
    }
    catch(Exception e)
    {
      return false;
    }
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean addToDefnFile(String name, String value, String fileName, String localDefnsDir, String defnsDir)
  {
    try
    {
      RandomAccessFile fh;
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return false;
        }
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return false;
      }

      fh.seek(fh.length());

      String s = name + " " + value;

      fh.writeBytes(s + "\n");

      fh.close();
    }
    catch(Exception e)
    {
      return false;
    }
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String getFromDefnFile(String name, String fileName, String localDefnsDir, String defnsDir)
  {
    short x;
    String str, buf, value;

    try
    {
      RandomAccessFile fh;

      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return "";
        }
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return "";
      }

      fh.seek(0);

      value = "";
      try
      {
        boolean quit = false;
        while(! quit)
        {
          x = 0;
          buf = "";
          str = fh.readLine();
          while(x < str.length() && str.charAt(x) != ' ')
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               buf += str.charAt(x);
            ++x;
          }

          if(buf.equalsIgnoreCase(name))
          {
            ++x;

            while(x < str.length() && str.charAt(x) == ' ')
              ++x;

            while(x < str.length() && str.charAt(x) != (char)13 && str.charAt(x) != (char)10
                  && str.charAt(x) != (char)26)
            {
              value += str.charAt(x++);
            }
            quit = true;
          }
        }
        fh.close();
      }
      catch(Exception ioErr)
      {
        fh.close();
      }
    }
    catch(Exception e)
    {
      return "";
    }

    return value;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // prependName is true if the name-value pair is required; else just the value.
  // if name is "" then entries are treated sequentially in file; else just those with name are considered.
  public String getFromDefnFileByEntry(boolean prependName, String name, int entry, String fileName, String localDefnsDir, String defnsDir)
  {
    short x;
    int count;
    String str, buf, value;

    try
    {
      RandomAccessFile fh=null;

      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return "";
        }
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return "";
      }

      fh.seek(0);

      count=1;
      value = "";
      try
      {
        boolean quit = false;
        while(! quit)
        {
          x = 0;
          buf = "";
          str = fh.readLine();
          while(x < str.length() && str.charAt(x) != ' ')
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               buf += str.charAt(x);
            ++x;
          }

          if(name.length() == 0 || buf.equalsIgnoreCase(name))
          {
            if(count == entry)
            {
              if(prependName)
              {
                x=0;
                while(x < str.length())
                {
                  if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
                    value += str.charAt(x);
                  ++x;
                }
              }
              else // only want value
              {
                ++x;
                while(str.charAt(x) == ' ')
                  ++x;
                while(x < str.length())
                {
                  if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
                  {
                    if(str.charAt(x) == ' ')
                      value += ' ';
                    else value += str.charAt(x);
                  }
                  ++x;
                }
              }
              quit = true;
            }
            ++count;
          }
        }
        
        fileClose(fh);
      }
      catch(Exception ioErr)
      {
        fileClose(fh);
      }
    }
    catch(Exception e)
    {
      return "";
    }
    return value;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public RandomAccessFile openFile(String fileName, String localDefnsDir, String defnsDir)
  {
    try
    {
      RandomAccessFile fh;
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return null;
        }
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return null;
      }

      fh.seek(0);
      
      return fh;
    }
    catch(Exception e)
    {
      return null;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void closeFile(RandomAccessFile fh)
  {
    try
    {
      fh.close();  
    }
    catch(Exception e) { System.out.println("Z31: Cannot close file"); }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // prependName is true if the name-value pair is required; else just the value.
  // if name is "" then entries are treated sequentially in file; else just those with name are
  //   considered.
  // This second version, to save next-search time:
  //   1. Assumes the file is opened, and closed separately
  //   2. keeps a running file pointer
  public String getFromDefnFileByEntry(RandomAccessFile fh, boolean prependName, String name, long[] upto) throws Exception
  {
    short x;
    String str, buf, value;

    try
    {
      fh.seek(upto[0]);

      value = "";
      boolean quit = false;
      while(! quit)
      {
        x = 0;
        buf = "";
        str = fh.readLine();
        while(x < str.length() && str.charAt(x) != ' ')
        {
          if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
             buf += str.charAt(x);
          ++x;
        }

        if(name.length() == 0 || buf.equalsIgnoreCase(name))
        {
          if(prependName)
          {
            x=0;
            while(x < str.length())
            {
              if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
                value += str.charAt(x);
              ++x;
            }
          }
          else // only want value
          {
            ++x;
            while(str.charAt(x) == ' ')
              ++x;
            while(x < str.length())
            {
              if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
              {
                if(str.charAt(x) == ' ')
                  value += ' ';
                else value += str.charAt(x);
              }
              ++x;
            }
          }
          quit = true;
        }
      }
    }
    catch(Exception e) { value = ""; }

    upto[0] = fh.getFilePointer();
    return value;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean repInDefnFile(String name, String newValue, String fileName, String localDefnsDir, String defnsDir)
  {
    RandomAccessFile fh, fhTmp;
    boolean written;
    String theDefnsDir;

    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return false;
          theDefnsDir = defnsDir;
        }
        else theDefnsDir = localDefnsDir;
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return false;
        theDefnsDir = defnsDir;
      }

      fh.seek(0);
    }
    catch(Exception ioEx) //  jic
    {
      return false;
    }

    try
    {
      // create tmp file
      createDefnFile("tmp", theDefnsDir);
      fhTmp = fileOpenD("tmp", theDefnsDir);
      fhTmp.seek(0);
    }
    catch(Exception e)
    {
      try
      {
        fh.close();
      }
      catch(Exception e2) { }
      return false;
    }

    written = false;
    try
    {
      short x;
      String str, buf;

      while(true)
      {
        x = 0;
        buf = "";

        str = fh.readLine();
        while(x < str.length() && str.charAt(x) != ' ')
        {
          if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
             buf += str.charAt(x);
          ++x;
        }

        if(buf.equalsIgnoreCase(name))
        {
          fhTmp.writeBytes(name + " " + newValue + "\n");
          written = true;
        }
        else // not the line wanted, so write to tmp file
        {
          x=0;
          while(x < str.length())
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               fhTmp.writeByte(str.charAt(x));
            ++x;
          }
          
          fhTmp.writeBytes("\n");
        }
      }
    }
    catch(Exception iEx) // eof
    {
      try
      {
        if(! written)
          fhTmp.writeBytes(name + " " + newValue + "\n");
        fh.close();
        fhTmp.close();

        fileDeleteD(fileName, theDefnsDir);
      }
      catch(Exception ioEx) //  jic
      {
        return false;
      }

      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // replaces an entry starting with a string of words (not just the first word)
  public boolean repInDefnFileStringMatch(String nameStr, String newEntry, String fileName, String localDefnsDir, String defnsDir)
  {
    RandomAccessFile fh, fhTmp;
    boolean written;
    String theDefnsDir;

    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return false;
          theDefnsDir = defnsDir;
        }
        else theDefnsDir = localDefnsDir;
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return false;
        theDefnsDir = defnsDir;
      }

      fh.seek(0);
    }
    catch(Exception ioEx) //  jic
    {
      return false;
    }

    try
    {
      // create tmp file
      createDefnFile("tmp", theDefnsDir);
      fhTmp = fileOpenD("tmp", theDefnsDir);
      fhTmp.seek(0);
    }
    catch(Exception e)
    {
      try
      {
        fh.close();
      }
      catch(Exception e2) { }
      return false;
    }

    written = false;
    try
    {
      short x;
      String str;

      while(true)
      {
        str = fh.readLine();
        if(str.startsWith(nameStr))
        {
          fhTmp.writeBytes(newEntry + "\n");
          written = true;
        }
        else // not the line wanted, so write to tmp file
        {
          x=0;
          while(x < str.length())
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               fhTmp.writeByte(str.charAt(x));
            ++x;
          }
          fhTmp.writeBytes("\n");
        }
      }
    }
    catch(Exception iEx) // eof
    {
      try
      {
        if(! written)
          fhTmp.writeBytes(newEntry + "\n");
        fh.close();
        fhTmp.close();

        fileDeleteD(fileName, theDefnsDir);
      }
      catch(Exception ioEx) //  jic
      {
        return false;
      }

      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // inserts keyed based on nameStr WHERE the first word is a numeric ONLY
  public boolean insertInDefnFile(String nameStr, String newEntry, String fileName, String localDefnsDir, String defnsDir)
  {
    RandomAccessFile fh, fhTmp;
    boolean written;
    String theDefnsDir;

    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return false;
          theDefnsDir = defnsDir;
        }
        else theDefnsDir = localDefnsDir;
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return false;
        theDefnsDir = defnsDir;
      }

      fh.seek(0);
    }
    catch(Exception ioEx) //  jic
    {
      return false;
    }

    try
    {
      // create tmp file
      createDefnFile("tmp", theDefnsDir);
      fhTmp = fileOpenD("tmp", theDefnsDir);
      fhTmp.seek(0);
    }
    catch(Exception e)
    {
      try
      {
        fh.close();
      }
      catch(Exception e2) { }
      return false;
    }

    written = false;
    try
    {
      int x, len;
      String str, firstWord;

      double firstWordD, nameStrD = doubleFromStr(nameStr);

      while(true)
      {
        str = fh.readLine();
        len = str.length();
        firstWord = "";
        x=0;
        while(x < len && str.charAt(x) != ' ')
          firstWord += str.charAt(x++);
        firstWordD = doubleFromStr(firstWord);
        if(! written && firstWordD > nameStrD)
        {
          fhTmp.writeBytes(newEntry + "\n");
          written = true;
        }

        x=0;
        while(x < str.length())
        {
          if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
             fhTmp.writeByte(str.charAt(x));
          ++x;
        }
        fhTmp.writeBytes("\n");
      }
    }
    catch(Exception iEx) // eof
    {
      try
      {
        if(! written)
          fhTmp.writeBytes(newEntry + "\n");
        fh.close();
        fhTmp.close();

        fileDeleteD(fileName, theDefnsDir);
      }
      catch(Exception ioEx) //  jic
      {
        return false;
      }

      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // STOCK ROD RO/????? 1
  public boolean repInDefnFile(String name, String secondName, String newValue, String fileName, String localDefnsDir, String defnsDir)
  {
    RandomAccessFile fh, fhTmp;
    String theDefnsDir;

    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return false;
          theDefnsDir = defnsDir;
        }
        else theDefnsDir = localDefnsDir;
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return false;
        theDefnsDir = defnsDir;
      }

      fh.seek(0);
    }
    catch(Exception ioEx) //  jic
    {
      return false;
    }

    try
    {
      // create tmp file
      createDefnFile("tmp", theDefnsDir);
      fhTmp = fileOpenD("tmp", theDefnsDir);
      fhTmp.seek(0);
    }
    catch(Exception e)
    {
      try
      {
        fh.close();
      }
      catch(Exception e2) { }
      return false;
    }

    boolean written = false;
    boolean replaced;
    try
    {
      short x;
      String str, buf;

      while(true)
      {
        x = 0;
        buf = "";

        str = fh.readLine();
        while(x < str.length() && str.charAt(x) != ' ')
        {
          if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
             buf += str.charAt(x);
          ++x;
        }

        replaced = false;
        if(buf.equalsIgnoreCase(name))
        {
          ++x;
          buf = "";
          while(x < str.length() && str.charAt(x) != ' ')
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
              buf += str.charAt(x);
            ++x;
          }

          if(buf.equalsIgnoreCase(secondName))
          {
            ++x;
            buf = "";
            while(x < str.length() && str.charAt(x) != ' ')
            {
              if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
                buf += str.charAt(x);
              ++x;
            }

            fhTmp.writeBytes(name + " " + secondName + " " + buf + " " + newValue + "\n");
            replaced = true;
            written = true;
          }
        }

        if(! replaced) // not the line wanted, so write to tmp file
        {
          x=0;
          while(x < str.length())
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               fhTmp.writeByte(str.charAt(x));
            ++x;
          }
          fhTmp.writeBytes("\n");
        }
      }
    }
    catch(Exception iEx) // eof
    {
      try
      {
        if(! written)
          fhTmp.writeBytes(name + " " + newValue + "\n");
        fh.close();
        fhTmp.close();

        fileDeleteD(fileName, theDefnsDir);
      }
      catch(Exception ioEx) //  jic
      {
        return false;
      }

      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // if name is "" then entries are treated sequentially in file; else just those with name are considered
  public boolean repInDefnFileByEntry(String name, int entry, String newValue, String fileName, String localDefnsDir, String defnsDir)
  {
    RandomAccessFile fh, fhTmp;
    String theDefnsDir;

    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return false;
          theDefnsDir = defnsDir;
        }
        else theDefnsDir = localDefnsDir;
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return false;
        theDefnsDir = defnsDir;
      }

      fh.seek(0);
    }
    catch(Exception ioEx) //  jic
    {
      return false;
    }

    try
    {
      // create tmp file
      createDefnFile("tmp", theDefnsDir);
      fhTmp = fileOpenD("tmp", theDefnsDir);
      fhTmp.seek(0);
    }
    catch(Exception e)
    {
      try
      {
        fh.close();
      }
      catch(Exception e2) { }
      return false;
    }

    try
    {
      int count=0;
      short x;
      String str, buf;
      boolean wanted;

      while(true)
      {
        x = 0;
        buf = "";

        str = fh.readLine();
        while(x < str.length() && str.charAt(x) != ' ')
        {
          if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
             buf += str.charAt(x);
          ++x;
        }

        if(name.length() == 0 || buf.equalsIgnoreCase(name))
        {
          ++count;
          if(count == entry)
            wanted = true;
          else wanted = false;
        }
        else wanted = false;

        if(wanted)
          fhTmp.writeBytes(buf + " " + newValue + "\n");
        else // not the line wanted, so write to tmp file
        {
          x=0;
          while(x < str.length())
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               fhTmp.writeByte(str.charAt(x));
            ++x;
          }
          fhTmp.writeBytes("\n");
        }
      }
    }
    catch(Exception iEx) // eof
    {
      try
      {
        fh.close();
        fhTmp.close();
      }
      catch(Exception ioEx) //  jic
      {
        return false;
      }

      if(fileDeleteD(fileName, theDefnsDir))
      return true;
    }
    
    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // if theValue == "", then dels first match of theName
  public boolean delFromDefnFile(String theName, String theValue, String fileName, String localDefnsDir, String defnsDir)
  {
    RandomAccessFile fh, fhTmp;
    String theDefnsDir, thisValue;

    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return false;
          theDefnsDir = defnsDir;
        }
        else theDefnsDir = localDefnsDir;
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return false;
        theDefnsDir = defnsDir;
      }

      fh.seek(0);
    }
    catch(Exception ioEx) //  jic
    {
      return false;
    }

    try
    {
      // create tmp file
      createDefnFile("tmp", theDefnsDir);
      fhTmp = fileOpenD("tmp", theDefnsDir);
      fhTmp.seek(0);
    }
    catch(Exception e)
    {
      try
      {
        fh.close();
      }
      catch(Exception e2) { }
      return false;
    }

    try
    {
      short x;
      String str, buf;
      boolean ignored, notCR;

      while(true)
      {
        x = 0;
        buf = "";

        str = fh.readLine();
        while(x < str.length() && str.charAt(x) != ' ')
        {
          if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
             buf += str.charAt(x);
          ++x;
        }
        ignored=false;

        if(buf.equalsIgnoreCase(theName))
        {
          ++x;

          while(x < str.length() && str.charAt(x) == ' ')
            ++x;

          thisValue="";
          while(x < str.length() && str.charAt(x) != ' ' && str.charAt(x) != (char)13
                && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
          {
            thisValue += str.charAt(x++);
          }

          if(theValue.length() == 0 || thisValue.equalsIgnoreCase(theValue))
            ignored = true; // match, so ignore it
        }

        if(! ignored) // not the line wanted, so write to tmp file
        {
          x=0;
          notCR = false;
          while(x < str.length())
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
              notCR = true;
            ++x;
          }

          if(notCR)
          {
            x=0;
            while(x < str.length())
            {
              if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
                fhTmp.writeByte(str.charAt(x));
              ++x;
            }
            fhTmp.writeBytes("\n");
          }
        }
      }
    }
    catch(Exception iEx) // eof
    {
      try
      {
        fh.close();
        fhTmp.close();

        fileDeleteD(fileName, theDefnsDir);
      }
      catch(Exception ioEx) //  jic
      {
        return false;
      }

      return true;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int countFileEntries(String fileName, String localDefnsDir, String defnsDir) throws Exception
  {
    int x=1, count=0;
    String s = " ";

    while(s.length() != 0)
    {
      s = getFromDefnFileByEntry(true, "", x, fileName, localDefnsDir, defnsDir);
      if(s.length() != 0)
        ++count;
      ++x;
    }
    return count;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // which = P - PO
  //       = Q - quote
  public String getRemark(char which, String remarkType, int reqdLine, String localDefnsDir, String defnsDir) throws Exception
  {
    short x;
    String str, buf;
    boolean possWanted;
    RandomAccessFile fh;

    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD("Remarks.dfn", localDefnsDir)) == null)
        {
          if((fh = fileOpenD("Remarks.dfn", defnsDir)) == null)
            return "";
        }
      }
      else
      {
        if((fh = fileOpenD("Remarks.dfn", defnsDir)) == null)
          return "";
      }

      fh.seek(0);

      try
      {
        while(true)
        {
          x = 0;
          buf = "";
          str = fh.readLine();
          while(x < str.length() && str.charAt(x) != ' ')
          {
            if(str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
               buf += str.charAt(x);
            ++x;
          }

          if(buf.equalsIgnoreCase("REMARK"))
          {
            ++x;

            while(x < str.length() && str.charAt(x) == ' ')
              ++x;

            buf = "";
            while(x < str.length() && str.charAt(x) != ' ' && str.charAt(x) != (char)13
                  && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
            {
              buf += str.charAt(x++);
            }

            possWanted = false;
            switch(which)
            {
              case 'P' : if(buf.equalsIgnoreCase("PO")) possWanted = true; break;
              case 'Q' : if(buf.equalsIgnoreCase("quote")) possWanted = true; break;
            }

            if(possWanted)
            {
              while(x < str.length() && str.charAt(x) == ' ')
                ++x;

              buf = "";
              while(x < str.length() && str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
                buf += str.charAt(x++);

              if(buf.equalsIgnoreCase(remarkType)) // this is the reqd entry
              {
                // pickup the remark line
                try
                {
                  for(int i=0;i<reqdLine;++i)
                    fh.readLine();
                  str = fh.readLine();
                  x=0;
                  buf = "";
                  while(x < str.length() && str.charAt(x) != (char)13 && str.charAt(x) != (char)10 && str.charAt(x) != (char)26)
                    buf += str.charAt(x++);
                  if(buf.startsWith("REMARK")) // stepped onto next
                    buf = " ";
                }
                catch(Exception e2)
                {
                  fh.close();
                  return " ";
                }
                fh.close();
                return buf;
              }
            }
          }
        }
      }
      catch(Exception ioErr) { fh.close(); }
    }
    catch(Exception e) { }

    return " ";
  }

  // -------------------------------------------------------------------------------------------
  // assumes first fld is int
  public int getNextHighest(String fileName, String localDefnsDir, String defnsDir)
  {
    int soFar=0;
    RandomAccessFile fh;
    try
    {
      if(localDefnsDir.length() > 0)
      {
        if((fh = fileOpenD(fileName, localDefnsDir)) == null)
        {
          if((fh = fileOpenD(fileName, defnsDir)) == null)
            return 0;
        }
      }
      else
      {
        if((fh = fileOpenD(fileName, defnsDir)) == null)
          return 0;
      }

      fh.seek(0);

      long[] upto = new long[1];  upto[0] = 0;
      int x, thisOneInt;
      String thisOne;
      String s = getFromDefnFileByEntry(fh, true, "", upto);
      int len = s.length();
      while(len > 0)
      {
        x=0;
        thisOne = "";
        while(x < len && s.charAt(x) != ' ')
          thisOne += s.charAt(x++);
        thisOneInt = intFromStr(thisOne);
        if(thisOneInt > soFar)
          soFar = thisOneInt;

        s = getFromDefnFileByEntry(fh, true, "", upto);
        len = s.length();
      }

      fh.close();
    }
    catch(Exception ioErr)
    {
      return soFar;
    }

    return soFar;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void insertionSort(int[] array, int numElements) throws Exception
  {
    int j;
    for(int i=1;i<numElements;i++)
    {
      for(j=0;j<i;j++)
      {
        if(array[i-j-1] > array[i-j])
          swap(array, i-j-1, i-j);
        else j=i;
      }
    }
  }

  private void swap(int[] array, int x, int y) throws Exception
  {
    int i = array[x];
    array[x] = array[y];
    array[y] = i;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void insertionSort(String[] array, int numElements) throws Exception
  {
    int j;
    for(int i=1;i<numElements;i++)
    {
      for(j=0;j<i;j++)
      {
        if(array[i-j-1].compareToIgnoreCase(array[i-j]) > 0)
          swap(array, i-j-1, i-j);
        else j=i;
      }
    }
  }

  private void swap(String[] array, int x, int y) throws Exception
  {
    String s = array[x];
    array[x] = array[y];
    array[y] = s;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean appendToFile(String text, String fileName) throws Exception
  {
    RandomAccessFile fh = fileOpen(fileName);
    if(fh == null)
      fh = create(fileName);

    if(fh == null) // just-in-case
      return false;

    fh.seek(fh.length());

    fh.writeBytes(text);

    fileClose(fh);

    return true;
  }

  // ------------------------------------------------------------------------------------------------
  // in 01.01.1970 00:00:00, out 1970-01-01 00:00:00
  public String convertToTimestamp(String ts) throws Exception
  {
    String date = "";
    int x = 0, len = ts.length();
    while(x < len && ts.charAt(x) != ' ') // just-in-case
      date += ts.charAt(x++);
    date = convertDateToSQLFormat(date);
    if(date.length() == 0) // 1970-01-01
      return "";
    return date + ts.substring(x);
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // converts "31.01.05" to "2005-01-31"
  public String convertDateToSQLFormat(String s) throws Exception
  {
    byte[] buf = new byte[20];
    strToBytes(buf, s);
    convertToYYYYMMDD(buf);
    return stringFromBytes(buf, 0L);
  } 

  // -------------------------------------------------------------------------------------------------------------------------------
  // converts "31.01.05" to "2005-01-31"
  public int convertToYYYYMMDD(byte[] buf) throws Exception
  {
    byte[] dd = new byte[10];
    byte[] mm = new byte[10];
    byte[] yy = new byte[10];

    if(buf[0] == '\000')
    {
      strToBytes(buf, "1970-01-01");
      return 10;
    }

    int x=0,y=0;
    while(buf[x] != '\000' && buf[x] != '.')
      dd[y++] = buf[x++];
    dd[y] = '\000';

    if(y == 1)
    {
      dd[2] = '\000';
      dd[1] = dd[0];
      dd[0] = '0';
    }

    ++x;
    y=0;
    while(buf[x] != '\000' && buf[x] != '.')
      mm[y++] = buf[x++];
    mm[y] = '\000';
    
    if(y == 1)
    {
      mm[2] = '\000';
      mm[1] = mm[0];
      mm[0] = '0';
    }

    ++x;
    y=0;
    while(buf[x] != '\000' && buf[x] != '.')
      yy[y++] = buf[x++];
    yy[y] = '\000';

    y=0;
    
    // if year is only 2-chars, prepend century
    if(yy[2] == '\000')
    {
      if(intFromBytesCharFormat(yy, (short)0) < 50)
      {
        buf[y++] = '2';
        buf[y++] = '0';
      }
      else
      {  
        buf[y++] = '1';
        buf[y++] = '9';
      }      
    }  
    
    x=0;
    
    while(yy[x] != '\000')
      buf[y++] = yy[x++];
    buf[y++] = '-';

    x=0;
    while(mm[x] != '\000')
      buf[y++] = mm[x++];
    buf[y++] = '-';

    x=0;
    while(dd[x] != '\000')
      buf[y++] = dd[x++];
    buf[y] = '\000';
     
    return y;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // converts "2005-01-31" to "31.01.05"; leaving result in buf
  public String convertFromYYYYMMDD(String s) throws Exception
  {
    byte[] b = new byte[20];
    strToBytes(b, s);
    convertFromYYYYMMDD(b);
    
    return stringFromBytes(b, 0L);
  }
  public String convertFromYYYYMMDD(byte[] buf) throws Exception
  {
    if(buf[0] == '\000')
      return "";
    
    if(match(buf, "1970-01-01"))
    {
      buf[0] = '\000';
      return "";
    }    
    
    byte[] b  = new byte[11];
    byte[] mm = new byte[2];
    byte[] dd = new byte[2];

    bytesToBytes(b, buf, 0);
    
    int len = lengthBytes(b, 0);

    if(b[6] == '-')
    {
      mm[0] = '0';
      mm[1] = b[5];
    } 
    else
    {
      mm[0] = b[5];
      mm[1] = b[6];
    }
    
    if(len == 9) // b[9] == '\000')
    {
      if(b[7] == '-')
      {
        dd[0] = '0';
        dd[1] = b[8];
      }
      else
      if(len == 8) // b[8] == '\000')
      {
        dd[0] = '0';
        dd[1] = b[7];
      }
      else
      {
        dd[0] = b[7];
        dd[1] = b[8];
      }      
    }
    else
    {
      dd[0] = b[8];
      dd[1] = b[9];
    }
    
    buf[0] = dd[0];
    buf[1] = dd[1];
    buf[2] = '.';
    buf[3] = mm[0];
    buf[4] = mm[1];
    buf[5] = '.';
    buf[6] = b[0];
    buf[7] = b[1];
    buf[8] = b[2];
    buf[9] = b[3];
    buf[10] = '\000';

    return stringFromBytes(buf, 0L);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // converts "2005-01-31" to "31.01.05"
  public String convertFromYYYYMMDD2(byte[] buf) throws Exception
  {
    if(buf[0] == '\000')
      return "";
    
    if(match(buf, "1970-01-01"))
    {
      buf[0] = '\000';
      return "";
    }    
    
    byte[] a = new byte[11];
    byte[] b = new byte[11];

    bytesToBytes(b, buf, 0);
    
    a[0] = b[8];
    a[1] = b[9];
    a[2] = '.';
    a[3] = b[5];
    a[4] = b[6];
    a[5] = '.';
    a[6] = b[0];
    a[7] = b[1];
    a[8] = b[2];
    a[9] = b[3];
    a[10] = '\000';
    
    return stringFromBytes(a, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // converts "2006-10-09 15:08:03.0" to "09.10.2006 15:08:03"
  public String convertFromTimestamp(byte[] b)
  {
    return convertFromTimestamp(stringFromBytes(b, 0));
  }
  public String convertFromTimestamp(String str)
  {
    try
    {
      int x=0, len = str.length();
      String s="";
      while(x < len && str.charAt(x) != ' ')
        s += str.charAt(x++);
      ++x;

      String t="";
      while(x < len && str.charAt(x) != '.')
        t += str.charAt(x++);

      String d = convertFromYYYYMMDD(s);
      if(d.length() == 0)
        return "";

      return d + " " + t;
    }
    catch(Exception e) { return ""; }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // converts "2006-10-09 15:08:03.0" to "09.10.2006 15:08:03"
  public String convertFromTimestampDoW(String str)
  {
    try
    {
      int x=0, len = str.length();
      String s = "";
      while(x < len && str.charAt(x) != ' ')
        s += str.charAt(x++);
      ++x;

      String t="";
      while(x < len && str.charAt(x) != '.')
        t += str.charAt(x++);

      String d = convertFromYYYYMMDD(s);
      if(d.length() == 0)
        return "";

      String yymmdd = "" + s.charAt(2) + s.charAt(3) + s.charAt(5) + s.charAt(6) + s.charAt(8) + s.charAt(9);

      d = yymmddExpand(true, yymmdd);

      return d + " " + t;
    }
    catch(Exception e) { return ""; }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // converts "2006-10-09 15:08:03.0" to "15:08:03"
  public String timeFromTimestamp(String str)
  {
    try
    {
      int x=0, len = str.length();
      while(x < len && str.charAt(x) != ' ')
        ++x;
      ++x;
    
      String t="";
      while(x < len && str.charAt(x) != '.')
        t += str.charAt(x++);
    
      return t;
    }
    catch(Exception e) { return ""; }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // converts "1" to    "00:01:00"
  // converts "12" to   "00:12:00"
  // converts "859" to  "08:59:00"
  // converts "1259" to "12:59:00"
  public int convertToHHMMSS(byte[] buf, int len) throws Exception
  {
    if(buf[0] == '\000')
    {    
      strToBytes(buf, "12:15:00");
      return 8;
    }
    
    byte[] b = new byte[10];

    if(len == 1)
    {
      b[0] = buf[0];
    
      buf[0] = '0';
      buf[1] = '0';
      buf[2] = ':';
      buf[3] = '0';
      buf[4] = b[0];

      buf[5] = ':';
      buf[6] = '0';
      buf[7] = '0';
      buf[8] = '\000';
    }
    else
    if(len == 2)
    {
      b[0] = buf[0];
      b[1] = buf[1];
    
      buf[0] = '0';
      buf[1] = '0';
      buf[2] = ':';
      buf[3] = b[0];
      buf[4] = b[1];

      buf[5] = ':';
      buf[6] = '0';
      buf[7] = '0';
      buf[8] = '\000';
    }
    else
    if(len == 3)
    {
      b[0] = buf[0];
      b[1] = buf[1];
      b[2] = buf[2];
    
      buf[0] = '0';
      buf[1] = b[0];
      buf[2] = ':';
      buf[3] = b[1];
      buf[4] = b[2];

      buf[5] = ':';
      buf[6] = '0';
      buf[7] = '0';
      buf[8] = '\000';
    }
    else 
    if(len == 4)
    {
      b[0] = buf[0];
      b[1] = buf[1];
      b[2] = buf[2];
      b[3] = buf[3];
    
      buf[0] = b[0];
      buf[1] = b[1];
      buf[2] = ':';
      buf[3] = b[2];
      buf[4] = b[3];
        
      buf[5] = ':';
      buf[6] = '0';
      buf[7] = '0';
      buf[8] = '\000';
    }
    else // if(len == 6)
    {
      b[0] = buf[0];
      b[1] = buf[1];
      b[2] = buf[2];
      b[3] = buf[3];
      b[4] = buf[4];
      b[5] = buf[5];
    
      buf[0] = b[0];
      buf[1] = b[1];
      buf[2] = ':';
      buf[3] = b[2];
      buf[4] = b[3];
      buf[5] = ':';
      buf[6] = b[4];
      buf[7] = b[5];
      buf[8] = '\000';
    }

    return 8;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public int encodeSQLFormat(String dateStr) throws Exception
  {
    short yyyy = (short)strToInt(dateStr.substring(0, 4));
    short mm   = (short)strToInt(dateStr.substring(5, 7));
    short dd   = (short)strToInt(dateStr.substring(8));
        
    return numOfDaysTotal(dd, mm, yyyy);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String yearNow(String localDefnsDir, String defnsDir) throws Exception
  {
    String today = today(localDefnsDir, defnsDir);

    return "20" + today.charAt(6) + today.charAt(7); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String generatePassWord() throws Exception
  {
    byte[] pwd = new byte[100];

    long l = new java.util.Date().getTime();
    int xx = new Random(l).nextInt();
    if(xx < 0)
      xx *= -1;

    intToBytesCharFormat(xx, pwd, (short)0);

    pwd[8] = '\000';
    switch(pwd[0])
    {
      case '0' : pwd[0] = 'a'; break;
      case '1' : pwd[0] = 'c'; break;
      case '2' : pwd[0] = 'g'; break;
      case '3' : pwd[0] = 'e'; break;
      case '4' : pwd[0] = 'k'; break;
      case '5' : pwd[0] = 't'; break;
      case '6' : pwd[0] = 'i'; break;
      case '7' : pwd[0] = 'b'; break;
      case '8' : pwd[0] = 'j'; break;
      case '9' : pwd[0] = 'w'; break;
    }
    switch(pwd[2])
    {
      case '0' : pwd[2] = 'Q'; break;
      case '1' : pwd[2] = 'R'; break;
      case '2' : pwd[2] = 'V'; break;
      case '3' : pwd[2] = 'S'; break;
      case '4' : pwd[2] = 'D'; break;
      case '5' : pwd[2] = 'B'; break;
      case '6' : pwd[2] = 'N'; break;
      case '7' : pwd[2] = 'M'; break;
      case '8' : pwd[2] = 'K'; break;
      case '9' : pwd[2] = 'P'; break;
    }

    switch(pwd[5])
    {
      case '0' : pwd[5] = 'Q'; break;
      case '1' : pwd[5] = 'W'; break;
      case '2' : pwd[5] = 'E'; break;
      case '3' : pwd[5] = 'R'; break;
      case '4' : pwd[5] = 'T'; break;
      case '5' : pwd[5] = 'Y'; break;
      case '6' : pwd[5] = 'U'; break;
      case '7' : pwd[5] = 'Z'; break;
      case '8' : pwd[5] = 'S'; break;
      case '9' : pwd[5] = 'N'; break;
    }
    switch(pwd[7])
    {
      case '0' : pwd[7] = 'm'; break;
      case '1' : pwd[7] = 'n'; break;
      case '2' : pwd[7] = 'b'; break;
      case '3' : pwd[7] = 'v'; break;
      case '4' : pwd[7] = 'c'; break;
      case '5' : pwd[7] = 'x'; break;
      case '6' : pwd[7] = 'z'; break;
      case '7' : pwd[7] = 'k'; break;
      case '8' : pwd[7] = 'j'; break;
      case '9' : pwd[7] = 'g'; break;
    }

    for(int x=0;x<8;++x)
    {
      if(pwd[x] == '0')
        pwd[x] = '8';
      else
      if(pwd[x] == '1')
        pwd[x] = '2';
    }

    return stringFromBytes(pwd, 0L);
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripPunctuation(String p) throws Exception
  {
    byte[] phrase = new byte[p.length() + 1];
    strToBytes(phrase, p);
    
    int len = lengthBytes(phrase, 0);
    int x=0, y;
    while(x < len)
    {
      if(   phrase[x] == ','
         || phrase[x] == '.'
         || phrase[x] == ';'
         || phrase[x] == ':'
         || phrase[x] == '/'
         || phrase[x] == '\\'
         || phrase[x] == '('
         || phrase[x] == ')'
         || phrase[x] == '['
         || phrase[x] == ']'
         || phrase[x] == '{'
         || phrase[x] == '}'
         || phrase[x] == '*'
         || phrase[x] == '+'
         || phrase[x] == '~'
         || phrase[x] == '\''
         || phrase[x] == '`'
         || phrase[x] == '_'
         || phrase[x] == '%'
         || phrase[x] == '&'
         || phrase[x] == '#'
         || phrase[x] == '$'
         || phrase[x] == '^'
         || phrase[x] == '"'
         || phrase[x] == '='
         || phrase[x] == '!'
         || phrase[x] == '@'
         || phrase[x] == '|'
        )
      {
        phrase[x] = ' ';
      }
      else
      if(phrase[x] == '-')
      {
        if(x > 0 && x < len && phrase[x-1] != ' ' && phrase[x+1] != ' ')
        {
          // shuntdown
          for(y=x;y<=len;++y)
            phrase[y] = phrase[y+1];
          --len;
        }
        else phrase[x] = ' ';
      }
      ++x;
    }
    
    return stringFromBytes(phrase, 0L);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripNonDisplayable(String str)
  {
    int x, len=str.length();
    String t = "";
    for(x=0;x<len;++x)
    {
      if(str.charAt(x) < 32 || str.charAt(x) > 126)
        ; // ignore - skip it
      else t += str.charAt(x);
    }
    
    return t;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String stripNoise(String p) throws Exception
  {
    if(p.equals("-"))
      return "";

    int len = p.length();
    byte[] phrase = new byte[len + 1];
    strToBytes(phrase, p);

    byte[] word = new byte[102];
    int x=0, y, z;
    while(x < len)
    {
      y=0;  z=x;
      while(x < len && y < 100 && phrase[x] != ' ')
        word[y++] = phrase[x++];
      word[y] = '\000';
      ++x;

      while(x < len && phrase[x] != ' ')
        ++x;;
      ++x;

      if(   match(word, "a")
         || match(word, "an")
         || match(word, "the")
         || match(word, "by")
         || match(word, "x")
         || match(word, "in")
         || match(word, "for")
         || match(word, "from")
         || match(word, "at")
         || match(word, "and")
         || match(word, "or")
         || match(word, "to")
         || match(word, "tel:")
         || match(word, "tel")
         || match(word, "fax:")
         || match(word, "fax")
        )
      {
        // shuntdown
        x = z;
        z = lengthBytes(word, 0) + 1;
        for(y=x;y<=len;++y)
          phrase[y] = phrase[y+z];
        len -= z;
      }
    }

    return stringFromBytes(phrase, 0L);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public int serviceToInt(String s)
  {
    String t = "";
    int len = s.length();
    for(int x=0;x<len;++x)
    {
      if(s.charAt(x) >= '0' && s.charAt(x) <= '9')
        t += s.charAt(x);
    }
    
    BigInteger bi = new BigInteger(t);
    return bi.intValue();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public String orderBySize(byte[] entries, byte[] itemCodes, int numEntries, int maxEntrySize)
  {
    String notSorted = "", sortedMetric = "", sortedImperial = "";

    try
    {
      int x, numSortedMetric = 0, numSortedImperial = 0;
      byte[] entry                  = new byte[maxEntrySize];
      byte[] itemCode               = new byte[21];
      char[] metricOrImperial       = new char[1];
      double[] sizeValue            = new double[1];
      String[] sortedMetricItems    = new String[numEntries];
      String[] sortedImperialItems  = new String[numEntries];
      double[] sortedMetricValues   = new double[numEntries];
      double[] sortedImperialValues = new double[numEntries];
      
      for(x=0;x<numEntries;++x)
      {
        if(getListEntryByNum(x, entries, entry)) // just-in-case
        {
          if(isASizeEntry(stringFromBytes(entry, 0L), metricOrImperial, sizeValue))
          {
            getListEntryByNum(x, itemCodes, itemCode);
            if(metricOrImperial[0] == 'm')
            {
              sortedMetricItems[numSortedMetric]  = stringFromBytes(itemCode, 0L);
              sortedMetricValues[numSortedMetric] = sizeValue[0];
              ++numSortedMetric;
            }
            else
            {
              sortedImperialItems[numSortedImperial]  = stringFromBytes(itemCode, 0L);
              sortedImperialValues[numSortedImperial] = sizeValue[0];
              ++numSortedImperial;
            }
          }
          else // no size found
          {
            getListEntryByNum(x, itemCodes, itemCode);
            notSorted += (stringFromBytes(itemCode, 0L) + "\001");
          }
        }
      }

      insertionSort(sortedMetricValues, numSortedMetric, sortedMetricItems);

      for(x=0;x<numSortedMetric;++x)
        sortedMetric += (sortedMetricItems[x] + "\001");

      insertionSort(sortedImperialValues, numSortedImperial, sortedImperialItems);
      for(x=0;x<numSortedImperial;++x)
        sortedImperial += (sortedImperialItems[x] + "\001");   
    }
    catch(Exception e) { System.out.println(e); }

    return sortedMetric + sortedImperial + notSorted;
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean isASizeEntry(String entry, char[] metricOrImperial, double[] sizeValue) throws Exception
  {
    sizeValue[0] = 0;
    int i;

    if((i = entry.indexOf("mm")) != -1)
    {
      if((i + 2) == entry.length())
        sizeValue[0] = extractValue(entry.substring(0, i), false);
      else
      if(entry.substring(i + 2).startsWith("."))
        sizeValue[0] = extractValue(entry.substring(0, i), false);
      else
      if(entry.substring(i + 2).startsWith(" "))
        sizeValue[0] = extractValue(entry.substring(0, i), false);
      
      if(sizeValue[0] == -88888)
        return false;

      metricOrImperial[0] = 'm';
      if(sizeValue[0] != 0.0)
        return true;
    }
        
    if((i = entry.indexOf("inches")) != -1)
    {
      if((i + 6) == entry.length())
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      else
      if(entry.substring(i + 6).startsWith("."))
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      else
      if(entry.substring(i + 6).startsWith(" "))
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      
      if(sizeValue[0] == -88888)
        return false;

      metricOrImperial[0] = 'i';
      if(sizeValue[0] != 0.0)
        return true;
    }
    
    // entry: 1 Lufkin plumb bob 20 oz, brass, inch graduation 100422

    if((i = entry.indexOf("inch")) != -1)
    {
      if((i + 4) == entry.length())
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      else
      if(entry.substring(i + 4).startsWith("."))
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      else
      if(entry.substring(i + 4).startsWith(" "))
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      
      if(sizeValue[0] == -88888)
        return false;

      metricOrImperial[0] = 'i';
      if(sizeValue[0] != 0.0)
        return true;
    }

    if((i = entry.indexOf("''")) != -1)
    {
      if((i + 2) == entry.length())
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      else
      if(entry.substring(i + 2).startsWith(" "))
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      
      if(sizeValue[0] == -88888)
        return false;

      metricOrImperial[0] = 'i';
      if(sizeValue[0] != 0.0)
        return true;
    }

    if((i = entry.indexOf("in.")) != -1)
    {
      if((i + 3) == entry.length())
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      else
      if(entry.substring(i + 3).startsWith("."))
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      else
      if(entry.substring(i + 3).startsWith(" "))
        sizeValue[0] = extractValue(entry.substring(0, i), true);
      
      if(sizeValue[0] == -88888)
        return false;

      metricOrImperial[0] = 'i';
      if(sizeValue[0] != 0.0)
        return true;
    }

    return false;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double extractValue(String entryBit, boolean imperial)
  {
    try
    {
      int x = (entryBit.length() - 1);
      while(x >= 0 && entryBit.charAt(x) == ' ')
        --x;

      int z = x + 1;
      if(imperial)
      {
        while(x >= 0 && ( ( entryBit.charAt(x) >= '0' && entryBit.charAt(x) <= '9') || entryBit.charAt(x) == '.' || entryBit.charAt(x) == ','
                         || entryBit.charAt(x) == '/' || entryBit.charAt(x) == '-'))
        {
          --x;
        }
      }
      else // metric
      {
        while(x >= 0 && ( ( entryBit.charAt(x) >= '0' && entryBit.charAt(x) <= '9') || entryBit.charAt(x) == '.' || entryBit.charAt(x) == ','))
        {
          --x;
        }
      }

      while(x < entryBit.length() && (entryBit.charAt(x) < '0' || entryBit.charAt(x) > '9'))
        ++x;
      if(x < 0 && (entryBit.charAt(x) >= '0' && entryBit.charAt(x) <= '9'))
        --x;    

      double num=0.0;

      if(imperial)
        num = convertImperial(entryBit.substring(x, z));
      else num = convertMetric(entryBit.substring(x, z));

      return num;
    }
    catch(Exception e)
    {
      return -88888;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double convertImperial(String bit) throws Exception
  {
    int x = 0, len = bit.length();
    String integerBit = "", beforeBit = "", afterBit = "";
    while(x < len && bit.charAt(x) >= '0' && bit.charAt(x) <= '9')
      integerBit += bit.charAt(x++);
      
    if(x < len && (bit.charAt(x) == ' ' || bit.charAt(x) == '-' || bit.charAt(x) == '.'))
    {
      ++x;
      beforeBit = "";
      while(x < len && bit.charAt(x) != '/')
        beforeBit += bit.charAt(x++);
      ++x;
          
      afterBit = "";
      while(x < len && bit.charAt(x) >= '0' && bit.charAt(x) <= '9')
        afterBit += bit.charAt(x++);
    }
    else
    {
      beforeBit = integerBit;
      integerBit = "0";
          
      ++x;
      afterBit = "";
      while(x < len && bit.charAt(x) >= '0' && bit.charAt(x) <= '9')
        afterBit += bit.charAt(x++);
    }
      
    if(doubleFromStr(afterBit) == 0.0)
      afterBit = "1";
    
    return doubleFromStr(integerBit) + (doubleFromStr(beforeBit) / doubleFromStr(afterBit));
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private double convertMetric(String bit) throws Exception
  {
    return doubleFromStr(bit);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void insertionSort(double[] array, int numElements, String[] itemCodes) throws Exception
  {
    int j;
    for(int i=1;i<numElements;i++)
    {
      for(j=0;j<i;j++)
      {
        if(array[i-j-1] > array[i-j])
          swap(array, i-j-1, i-j, itemCodes);
        else j=i;
      }
    }
  }

  private void swap(double[] array, int x, int y, String[] itemCodes) throws Exception
  {
    double d = array[x];
    array[x] = array[y];
    array[y] = d;
    
    String itemCode = itemCodes[x];
    itemCodes[x] = itemCodes[y];
    itemCodes[y] = itemCode;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String deNull(String s) throws Exception
  {
     if(s == null)
       return "";
     
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // takes list (binary-1 separated) and returns (binary-1 separated) list of ordered positions within the original list
  // 000 000 000 000 000 000 000 000
  public String sortDecimalDot(String entries, int numEntries) throws Exception
  {
    int x, y = 0, z, len, partLen;
    String entry, part, formattedPart, result = "";
    String[] array = new String[numEntries];

    for(x=0;x<numEntries;++x)
    {
      len = 0;
      entry = "";
      while(entries.charAt(y) != '\001')
      {
        entry += entries.charAt(y++);
        ++len;
      }
      ++y;

      result = "";
      z = 0;
      while(z < len)
      {
        part = "";
        partLen = 0;
        while(z < len && entry.charAt(z) != '.')
        {
          part += entry.charAt(z++);
          ++partLen;
        }
        ++z;

        if(partLen == 1)
          formattedPart = "00" + part;
        else
        if(partLen == 2)
          formattedPart = "0" + part;
        else
        if(partLen == 3)
          formattedPart = part;
        else formattedPart = "000"; // just-in-case

        result += formattedPart;
      }

      for(z=result.length();z<24;++z)
        result += "0";

      array[x] = result;
    }

    String[] array2 = new String[numEntries];
    for(x=0;x<numEntries;++x)
      array2[x] = array[x];
    
    insertionSort(array, numEntries);

    String ret = "";

    for(x=0;x<numEntries;++x)
    {
      entry = array[x];

      for(z=0;z<numEntries;++z)
      {
        if(array2[z].equals(entry))
        {
          ret += (z + "\001");

          z = numEntries;
        }
      }
    }

    return ret;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean entryInsertionSort(String[] entries, int numLines, int[] lineNums) throws Exception
  {
    boolean atleastOneSwapped = false;

    String[][] part = new String[numLines][20]; // plenty

    int maxParts = entryInsertionBuildParts(entries, numLines, part);

    int j;
    for(int i=1;i<numLines;i++)
    {
      for(j=0;j<i;j++)
      {
        if(entryInsertionSortTest(part[i - j - 1], part[i - j], maxParts) > 0)
        {
          entryInsertionSortSwap(entries, i-j-1, i-j, lineNums);
          atleastOneSwapped = true;
          maxParts = entryInsertionBuildParts(entries, numLines, part);
        }
        else j=i;
      }
    }

    return atleastOneSwapped;
  }

  private int entryInsertionSortTest(String[] entry1, String[] entry2, int maxParts) throws Exception
  {
    int res, i1, i2;
    boolean tryInt;

    for(int x=0;x<=maxParts;++x)
    {
      tryInt = false;

      if(isInteger(entry1[x]) && isInteger(entry2[x]))
        tryInt = true;

      if(tryInt)
      {
        i1 = strToInt(entry1[x]);
        i2 = strToInt(entry2[x]);

        if(i1 < i2)
          res = -1;
        else
        if(i1 > i2)
          res = 1;
        else res = 0;
      }
      else res = entry1[x].compareToIgnoreCase(entry2[x]);

      if(res != 0)
        return res;
    }

    return 0;
  }
  
  private int entryInsertionBuildParts(String[] entries, int numLines, String[][] part) throws Exception
  {
    int x, y, len, partNum, maxParts = 0;

    for(x=0;x<numLines;++x)
      for(y=0;y<20;++y)
        part[x][y] = "";

    for(int count=0;count<numLines;++count)
    {
      x = 0;
      partNum = 0;
      len = entries[count].length();

      while(x < len)
      {
        if(entries[count].charAt(x) >= '0' && entries[count].charAt(x) <= '9')
        {
          while(x < len && entries[count].charAt(x) >= '0' && entries[count].charAt(x) <= '9')
            part[count][partNum] += entries[count].charAt(x++);
        }
        else
        {
          while(x < len && (entries[count].charAt(x) < '0' || entries[count].charAt(x) > '9'))
            part[count][partNum] += entries[count].charAt(x++);
        }

        ++partNum;
        if(partNum > maxParts)
          maxParts = partNum;
      }
    }

    return maxParts;
  }

  private void entryInsertionSortSwap(String[] entries, int x, int y, int[] lineNums) throws Exception
  {
    String s = entries[x];
    entries[x] = entries[y];
    entries[y] = s;

    int lineNum = lineNums[x];
    lineNums[x] = lineNums[y];
    lineNums[y] = lineNum;
  }

}
