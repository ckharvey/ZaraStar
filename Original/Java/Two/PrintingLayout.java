// =======================================================================================================================================================================================================
// System: Zara: General: Printing Layout
// File:   PrintingLayout.java
// Author: C.K.Harvey
// Copyright (c) 1998-2005 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;

public class PrintingLayout
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void getPageSize(RandomAccessFile fh, double[] width, double[] height, char[] orientation)
  {
    width[0] = 210.0; height[0] = 297.0;

    try
    {
      fh.seek(0);

      boolean found = false;

      String s = getNextLine(fh);

      while(! found && s.length() != 0)
      {
        if(s.startsWith("S:"))
        {
          // format:      S:210.0,297.0,L
          String str="";
          int x = 2;
          while(x < s.length() && s.charAt(x) != ',') // just-in-case
            str += s.charAt(x++);

          if(s.charAt(x) != ',') // something is illegal
            return;

          width[0] = generalUtils.doubleFromStr(str);

          ++x; // step over ','
          str = "";
          while(x < s.length() && s.charAt(x) != ',') // just-in-case
            str += s.charAt(x++);

          height[0] = generalUtils.doubleFromStr(str);

          ++x; // step over ','
          if(x < s.length())
            orientation[0] = s.charAt(x);
          else orientation[0] = 'P'; // portrait

          found = true;
        }
        else s = getNextLine(fh);
      }
    }
    catch(Exception e) { }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public String getPageData(int pageNum, String fileName, String dirName)
  {
    String pageData="";
    int count = 1;

    try
    {
      RandomAccessFile fh = generalUtils.fileOpenD(fileName, dirName);

      fh.seek(0);

      String line="";

      boolean found=false;

      if(count == pageNum)
        found = true;
      else line = getNextLine(fh);

      while(! found && line.length() != 0)
      {
        if(line.startsWith("E:"))
        {
          ++count;
          if(count == pageNum)
            found = true;
          else line = getNextLine(fh);
        }
        else line = getNextLine(fh);
      }

      if(found)
      {
        boolean eop=false;
        line = getNextLine(fh);
        while(! eop && line.length() != 0)
        {
          if(line.startsWith("E:"))
            eop = true;
          else
          {
            pageData += line;
            pageData += "\001";
          }

          line = getNextLine(fh);
        }
      }

      fh.close();
    }
    catch(Exception e) { }

    return pageData;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getNextLine(RandomAccessFile fh)
  {
    byte b;
    String str="";

    try
    {
      long curr = fh.getFilePointer();
      long high = fh.length();

      if(curr == high)
        return "";

      fh.seek(curr);

      b = fh.readByte();

      while(b == (byte)';') // comment line
      {
        while(curr < high && b != (byte)10 && b != (byte)13 && b != (byte)26)
        {
          b = fh.readByte();
          ++curr;
        }

        while(b == (byte)10 || b == (byte)13 || b == (byte)26)
        {
          try
          {
            b = fh.readByte();
          }
          catch(Exception exEof)
          {
            return str;
          }
          ++curr;
        }
        if(b == (byte)26)
          return str;
      }

      while(curr < high)
      {
        if(b == (byte)10 || b == (byte)13 || b == (byte)26)
        {
          while(b == (byte)10 || b == (byte)13 || b == (byte)26)
          {
            try
            {
              b = fh.readByte();
            }
            catch(Exception exEof2)
            {
              return str;
            }
          }

          if(b == (byte)26)
           ; // --x;
          else fh.seek(fh.getFilePointer() - 1);

          return str;
        }

        str += (char)b;
        ++curr;

        try
        {
          b = fh.readByte();
        }
        catch(Exception exEof2)
        {
          return str;
        }
      }
    }
    catch(Exception e)
    {
      return str;
    }

    return str;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public int getNumPages(RandomAccessFile fh)
  {
    int count = 1;

    try
    {
      fh.seek(0);

      boolean startsWithE;
      String line = getNextLine(fh);
      while(line.length() != 0)
      {
        startsWithE = false;      // catches the case where the last line of a file is "E:"
        if(line.startsWith("E:"))
          startsWithE = true;
        line = getNextLine(fh);
        if(startsWithE && line.length() != 0)
          ++count;
      }
    }
    catch(Exception e) { }

    return count;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  // amendBy is to allow adjustment for those cases where an 'extra' "E:" has output (e.g., SoA)
  public void updateNumPages(String fileName, String reportsDir) throws Exception
  {
    updateNumPages(0, fileName, reportsDir);
  }
  public void updateNumPages(int amendBy, String fileName, String reportsDir) throws Exception
  {
    RandomAccessFile fh=null, fhTmp=null;

    try
    {
      fh = generalUtils.fileOpenD(fileName, reportsDir);
      if(fh==null) // just-in-case
        return;

      fh.seek(0);

      int numPages = getNumPages(fh);
      numPages += amendBy;

      // create tmp file
      fhTmp = generalUtils.create(reportsDir + "tmp");

      int offset;
      String newLine;

      fh.seek(0);
      String line = getNextLine(fh);
      while(line.length() != 0)
      {
        offset = line.indexOf("NUMPAGES");
        if(offset != -1)
        {
          newLine = line.substring(0, offset) + numPages + line.substring(offset + 8, line.length());
          fhTmp.writeBytes(newLine + "\n");
        }
        else fhTmp.writeBytes(line + "\n");

        line = getNextLine(fh);
      }

      fh.close();
      fhTmp.close();

      generalUtils.fileDeleteD(fileName, reportsDir);
    }
    catch(Exception e) { System.out.println(e); }
  }

}
