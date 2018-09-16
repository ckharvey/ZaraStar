// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Modify screen layout file
// Module: ScreenLayout.java
// Author: C.K.Harvey
// Copyright (c) 1997-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;

public class ScreenLayout
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // html input to temp buf1
  public short layoutToBuffer(byte[][] buf1, byte[][] buf2, int[] iSize1, byte[] fieldNames, byte[] fieldTypes, short[] fieldSizes, String localDefnsDir, String defnsDir, String sourceHtml, int numTables, String fieldNames1,  short[] fieldSizes1,
                              String fieldNames2, short[] fieldSizes2, String fieldNames3, short[] fieldSizes3) throws Exception
  {
    buf1[0][0] = (byte)-1;
    buf2[0][0] = (byte)-1;
    int x=0, ch=0;
    short numFields = 0;

    FileInputStream fis = null;

    if(localDefnsDir.length() > 0)
      if(generalUtils.fileExists(localDefnsDir + sourceHtml))
        fis = new FileInputStream(localDefnsDir + sourceHtml);

    if(fis == null) // still
      fis = new FileInputStream(defnsDir + sourceHtml);

    BufferedInputStream htmlInput = new BufferedInputStream(fis);

    while(ch != (byte)-1)
    {
      ch = htmlInput.read();
      try
      {
        buf1[0][x] = (byte)ch;
        ++x;
      }
      catch(ArrayIndexOutOfBoundsException e)
      {
        byte[] buf3 = new byte[iSize1[0]];
        System.arraycopy(buf1[0], 0, buf3, 0, iSize1[0]);
        iSize1[0] += 5000;
        buf1[0] = new byte[iSize1[0]];
        System.arraycopy(buf3, 0, buf1[0], 0, iSize1[0] - 5000);
        buf1[0][x++] = (byte)ch;
      }
    }
    buf1[0][x] = (byte)-1;
    htmlInput.close();
    fis.close();

    int i, y, len;

    // fieldNames are separated by '\001', term'd by '\000'
    // fieldSizes are a short array
    if(numTables >= 1)
    {
      i=x=y=0;
      len = fieldNames1.length();
      while(y < len)
      {  
        while(y < len && fieldNames1.charAt(y) != ',')
          fieldNames[x++] = (byte)fieldNames1.charAt(y++);
        fieldNames[x++] = (byte)'\001';

        fieldSizes[numFields++] = fieldSizes1[i++];
        
        ++y;
        while(y < len && fieldNames1.charAt(y) == ' ')
          ++y;
      }

      fieldNames[x] = (byte)'\000';
    }  

    if(numTables >= 2)
    {
      i=y=0;
      len = fieldNames2.length();

      while(y < len)
      {  
        while(y < len && fieldNames2.charAt(y) != ',')
          fieldNames[x++] = (byte)fieldNames2.charAt(y++);
       fieldNames[x++] = (byte)'\001';

        fieldSizes[numFields++] = fieldSizes2[i++];
       
        ++y;
        while(y < len && fieldNames2.charAt(y) == ' ')
          ++y;
      }

      fieldNames[x] = (byte)'\000';
    }  

    if(numTables == 3)
    {
      i=y=0;
      len = fieldNames3.length();
      while(y < len)
      {  
        while(y < len && fieldNames3.charAt(y) != ',')
          fieldNames[x++] = (byte)fieldNames3.charAt(y++);
        fieldNames[x++] = (byte)'\001';

        fieldSizes[numFields++] = fieldSizes3[i++];
        
        ++y;
        while(y < len && fieldNames3.charAt(y) == ' ')
          ++y;
      }

      fieldNames[x] = (byte)'\000';
    }
    
    return numFields;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // html input to temp buf1
  public void appendLayoutToBuffer(byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, byte[] fieldNames, byte[] fieldTypes,
                                   short[] fieldSizes, String localDefnsDir, String defnsDir, String sourceHtml, int numTables, String fieldNames1,
                                   short[] fieldSizes1, String fieldNames2, short[] fieldSizes2, String fieldNames3, short[] fieldSizes3)
                                   throws Exception
  {
    int x=0, ch=0;

    FileInputStream fis = null;

    if(localDefnsDir.length() > 0)
      if(generalUtils.fileExists(localDefnsDir + sourceHtml))
        fis = new FileInputStream(localDefnsDir + sourceHtml);

    if(fis == null) // still
      fis = new FileInputStream(defnsDir + sourceHtml);

    BufferedInputStream htmlInput = new BufferedInputStream(fis);

    if(source[0] == '2')
    {
      while(buf2[0][x] != (byte)-1)
        ++x;
    }
    else
    {
      while(buf1[0][x] != (byte)-1)
        ++x;
    }

    while(ch != (byte)-1)
    {
      ch = htmlInput.read();
      try
      {
        if(source[0] == '2')
          buf2[0][x] = (byte)ch;
        else buf1[0][x] = (byte)ch;
        ++x;
      }
      catch(ArrayIndexOutOfBoundsException e)
      {
        if(source[0] == '2')
        {
          byte[] buf3 = new byte[iSize2[0]];
          System.arraycopy(buf2[0], 0, buf3, 0, iSize2[0]);
          iSize2[0] += 5000;
          buf2[0] = new byte[iSize2[0]];
          System.arraycopy(buf3, 0, buf2[0], 0, iSize2[0] - 5000);
          buf2[0][x++] = (byte)ch;
        }
        else
        {
          byte[] buf3 = new byte[iSize1[0]];
          System.arraycopy(buf1[0], 0, buf3, 0, iSize1[0]);
          iSize1[0] += 5000;
          buf1[0] = new byte[iSize1[0]];
          System.arraycopy(buf3, 0, buf1[0], 0, iSize1[0] - 5000);
          buf1[0][x++] = (byte)ch;
        }
      }
    }

    if(source[0] == '2')
      buf2[0][x] = (byte)-1;
    else buf1[0][x] = (byte)-1;

    htmlInput.close();
    fis.close();

    int i, y, len;
    short numFields=0;

    for(int z=0;z<numTables;++z)
    {
      // fieldNames are separated by '\001', term'd by '\000'
      // fieldSizes are a short array
      if(numTables >= 1)
      {
        i=x=y=0;
        len = fieldNames1.length();
        while(y < len)
        {  
          while(y < len && fieldNames1.charAt(y) != ',')
            fieldNames[x++] = (byte)fieldNames1.charAt(y++);
          fieldNames[x++] = (byte)'\001';

          fieldSizes[numFields++] = fieldSizes1[i++];
        
          ++y;
          while(y < len && fieldNames1.charAt(y) == ' ')
            ++y;
        }

        fieldNames[x] = (byte)'\000';
      }  
      else
      if(numTables >= 2)
      {
        i=y=0;
        len = fieldNames2.length();
        while(y < len)
        {  
          while(y < len && fieldNames2.charAt(y) != ',')
            fieldNames[x++] = (byte)fieldNames2.charAt(y++);
          fieldNames[x++] = (byte)'\001';

          fieldSizes[numFields++] = fieldSizes2[i++];
       
          ++y;
          while(y < len && fieldNames2.charAt(y) == ' ')
            ++y;
        }

        fieldNames[x] = (byte)'\000';
      }  
      else
      if(numTables == 3)
      {
        i=y=0;
        len = fieldNames3.length();
        while(y < len)
        {  
          while(y < len && fieldNames3.charAt(y) != ',')
            fieldNames[x++] = (byte)fieldNames3.charAt(y++);
          fieldNames[x++] = (byte)'\001';

          fieldSizes[numFields++] = fieldSizes3[i++];
        
          ++y;
          while(y < len && fieldNames3.charAt(y) == ' ')
            ++y;
        }

        fieldNames[x] = (byte)'\000';
      }
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void appendBytesToBuffer(byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, byte[] inputBuf) throws Exception
  {
    int x=0;

    if(source[0] == '2')
    {
      while(buf2[0][x] != (byte)-1)
        ++x;
    }
    else
    {
      while(buf1[0][x] != (byte)-1)
        ++x;
    }

    int y=0;
    while(inputBuf[y] != '\000')
    {
      try
      {
        if(source[0] == '2')
          buf2[0][x] = inputBuf[y];
        else buf1[0][x] = inputBuf[y];
        ++x;
        ++y;
      }
      catch(ArrayIndexOutOfBoundsException e)
      {
        if(source[0] == '2')
        {
          byte[] buf3 = new byte[iSize2[0]];
          System.arraycopy(buf2[0], 0, buf3, 0, iSize2[0]);
          iSize2[0] += 5000;
          buf2[0] = new byte[iSize2[0]];
          System.arraycopy(buf3, 0, buf2[0], 0, iSize2[0] - 5000);
          buf2[0][x++] = inputBuf[y++];
        }
        else
        {
          byte[] buf3 = new byte[iSize1[0]];
          System.arraycopy(buf1[0], 0, buf3, 0, iSize1[0]);
          iSize1[0] += 5000;
          buf1[0] = new byte[iSize1[0]];
          System.arraycopy(buf3, 0, buf1[0], 0, iSize1[0] - 5000);
          buf1[0][x++] = inputBuf[y++];
        }
      }
    }

    try
    {
      if(source[0] == '2')
        buf2[0][x] = (byte)-1;
      else buf1[0][x] = (byte)-1;
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      if(source[0] == '2')
      {
        byte[] buf3 = new byte[iSize2[0]];
        System.arraycopy(buf2[0], 0, buf3, 0, iSize2[0]);
        iSize2[0] += 5000;
        buf2[0] = new byte[iSize2[0]];
        System.arraycopy(buf3, 0, buf2[0], 0, iSize2[0] - 5000);
      }
      else
      {
        byte[] buf3 = new byte[iSize1[0]];
        System.arraycopy(buf1[0], 0, buf3, 0, iSize1[0]);
        iSize1[0] += 5000;
        buf1[0] = new byte[iSize1[0]];
        System.arraycopy(buf3, 0, buf1[0], 0, iSize1[0] - 5000);
      }

      if(source[0] == '2')
        buf2[0][x] = (byte)-1;
      else buf1[0][x] = (byte)-1;
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void appendTmpFileToBuffer(byte[][] buf1, byte[][] buf2, char[] source, int[] iSize1, int[] iSize2, String fileName, String dirName)
                                    throws Exception
  {
    int ch=0;
    int x=0;

    FileInputStream fis = new FileInputStream(dirName + fileName);

    BufferedInputStream htmlInput = new BufferedInputStream(fis);

    if(source[0] == '2')
    {
      while(buf2[0][x] != (byte)-1)
        ++x;
    }
    else
    {
      while(buf1[0][x] != (byte)-1)
        ++x;
    }

    while(ch != (byte)-1)
    {
      ch = htmlInput.read();
      try
      {
        if(source[0] == '2')
          buf2[0][x] = (byte)ch;
        else buf1[0][x] = (byte)ch;
        ++x;
      }
      catch(ArrayIndexOutOfBoundsException e)
      {
        if(source[0] == '2')
        {
          byte[] buf3 = new byte[iSize2[0]];
          System.arraycopy(buf2[0], 0, buf3, 0, iSize2[0]);
          iSize2[0] += 5000;
          buf2[0] = new byte[iSize2[0]];
          System.arraycopy(buf3, 0, buf2[0], 0, iSize2[0] - 5000);
          buf2[0][x++] = (byte)ch;
        }
        else
        {
          byte[] buf3 = new byte[iSize1[0]];
          System.arraycopy(buf1[0], 0, buf3, 0, iSize1[0]);
          iSize1[0] += 5000;
          buf1[0] = new byte[iSize1[0]];
          System.arraycopy(buf3, 0, buf1[0], 0, iSize1[0] - 5000);
          buf1[0][x++] = (byte)ch;
        }
      }
    }

    if(source[0] == '2')
      buf2[0][x] = (byte)-1;
    else buf1[0][x] = (byte)-1;

    htmlInput.close();
    fis.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void appendToTmpFile(byte[] buf, String fileName, String dirName) throws Exception
  {
    RandomAccessFile fh = generalUtils.fileOpen(dirName+fileName);
    fh.seek(fh.length());
    fh.writeBytes(generalUtils.stringFromBytes(buf, 0L));
    generalUtils.fileClose(fh);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void tmpFileToOut(PrintWriter out, String fileName, String dirName) throws Exception
  {
    FileInputStream fis;

    try
    {
      fis = new FileInputStream(dirName + fileName);
    }
    catch(Exception e) { return; }

    BufferedInputStream htmlInput = new BufferedInputStream(fis);

    int ch=0;

    ch = htmlInput.read();
    while(ch != (byte)-1)
    {
      out.print((char)ch);
      ch = htmlInput.read();
    }

    htmlInput.close();
    fis.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // temp buf to html output
  public void bufferToLayout(byte[][] buf1, byte[][] buf2, char[] source, String targetHtml) throws Exception
  {
    int x=0;

    FileOutputStream fos = new FileOutputStream(targetHtml);
    BufferedOutputStream htmlOutput = new BufferedOutputStream(fos);

    if(source[0] == '2')
    {
      while(buf2[0][x] != (byte)-1)
        htmlOutput.write(buf2[0][x++]);
    }
    else
    {
      while(buf1[0][x] != (byte)-1)
        htmlOutput.write(buf1[0][x++]);
    }
    
    htmlOutput.close();
    fos.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void appendBufferToTmpFile(byte[][] buf1, byte[][] buf2, char[] source, String fileName, String dirName) throws Exception
  {
    RandomAccessFile fh = generalUtils.fileOpenD(fileName, dirName);
    fh.seek(fh.length());

    int x=0;

    if(source[0] == '2')
    {
      while(buf2[0][x] != (byte)-1)
        fh.writeByte(buf2[0][x++]);
    }
    else
    {
      while(buf1[0][x] != (byte)-1)
        fh.writeByte(buf1[0][x++]);
    }
    
    generalUtils.fileClose(fh);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // temp buf to out.println
  public String bufferToOut(byte[][] buf1, byte[][] buf2, char[] source, PrintWriter out) throws Exception
  {
    String s = "";

    int x=0;

    if(source[0] == '2')
    {
      while(buf2[0][x] != (byte)-1)
      {
        out.print((char)buf2[0][x]);
        ++x;
      }
    }
    else
    {
      while(buf1[0][x] != (byte)-1)
      {
        out.print((char)buf1[0][x]);
        ++x;
      }
    }

    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void replaceInBuffer(byte[][] buf1, byte[][] buf2, char[] source, String pattern, String str) throws Exception
  {
    int x=0;

    if(source[0] == '2')
    {
      int i = 0;
      while(i != -1)
      {
        i = generalUtils.containsPosn(buf2[0], 0, pattern);
        x = 0;
        int z = 0;
        if(i != -1)
        {
          byte[] b = new byte[generalUtils.lengthBytes(buf2[0],0) + 100];
          while(x < i)
            b[z++] = buf2[0][x++];

          x += pattern.length();

          int len = str.length(), y = 0;

          while(y < len)
            b[z++] = (byte)str.charAt(y++);

          while(buf2[0][x] != (byte)-1)
            b[z++] = buf2[0][x++];
          b[z] = -1;

          x = 0;
          while(b[x] != (byte)-1)
          {
            buf2[0][x] = b[x];
            ++x;
          }
          buf2[0][x] = -1;
        }
      }
    }
    else
    {
      int i = 0;
      while(i != -1)
      {
        i = generalUtils.containsPosn(buf1[0], 0, pattern);
        x = 0;
        int z = 0;
        if(i != -1)
        {
          byte[] b = new byte[generalUtils.lengthBytes(buf1[0],0) + 100];
          while(x < i)
            b[z++] = buf1[0][x++];

          x += pattern.length();

          int len = str.length(), y = 0;

          while(y < len)
            b[z++] = (byte)str.charAt(y++);

          while(buf1[0][x] != (byte)-1)
            b[z++] = buf1[0][x++];
          b[z] = -1;

          x = 0;
          while(b[x] != (byte)-1)
          {
            buf1[0][x] = b[x];
            ++x;
          }
          buf1[0][x] = -1;
        }
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public void displayBuffer(byte[][] buf1, byte[][] buf2, char[] source) throws Exception
  {
    int x=0;

    if(source[0] == '2')
    {
      while(buf2[0][x] != (byte)-1)
      {
        System.out.print((char)buf2[0][x]);
        ++x;
      }
    }
    else
    {
      while(buf1[0][x] != (byte)-1)
      {
        System.out.print((char)buf1[0][x]);
        ++x;
      }
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  public char resetBuffer(byte[][] buf1, byte[][] buf2) throws Exception
  {
    buf1[0][0] = (byte)-1;
    buf2[0][0] = (byte)-1;
    return '1'; // source
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  // temp buf to temp buf including data as we go
  public void bufferToBuffer(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, short numFields, byte[] fieldNames,
                             byte[] fieldTypes, short[] fieldSizes, char type, char lineType, byte[] data, int lenData, byte[] ddlData,
                             int lenDDLData, byte[] javaScriptCode, byte[] prependCode) throws Exception
  {
    bufferToBuffer(buf1, buf2, size1, size2, source, numFields, fieldNames, fieldTypes, fieldSizes, type, lineType, data, lenData, ddlData,
                   lenDDLData, javaScriptCode, prependCode, false, false, "", 0);
  }
  public void bufferToBuffer(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, short numFields, byte[] fieldNames,
                             byte[] fieldTypes, short[] fieldSizes, char type, char lineType, byte[] data, int lenData, byte[] ddlData,
                             int lenDDLData, byte[] javaScriptCode, byte[] prependCode, boolean addNoneToDDL, boolean displayOnlyFld, String fldName,
                             int presetLineEntry) throws Exception
  {
    byte ch;
    boolean doingBL = false;
    byte[] sourceBuf;
    byte[][] targetBuf   = new byte[1][];
    byte[] saveBL        = new byte[3000];
    byte[] tagName       = new byte[10];
    byte[] zaraFieldName = new byte[40];
    byte[] zaraType      = new byte[20];
    byte[] zaraOption    = new byte[40];
    byte[] zaraDefault   = new byte[200];
    byte[] zaraMaxChars  = new byte[10];
    byte[] zaraValues    = new byte[200];
    byte[] linexName     = new byte[62];
    byte[] zaraTagEntry  = new byte[200]; // plenty
    byte[] defaultValue  = new byte[200]; // plenty
    byte[] zaraTableAndFieldName = new byte[50];
    byte[] dataStrRes    = new byte[3000];
    byte[] javaScript    = new byte[2000];
    byte[] token         = new byte[200];
    byte[] value         = new byte[200];
    byte[] tmp           = new byte[50];
    byte[] save          = new byte[1000];
    int[] iPtr = new int[1];  iPtr[0] = 0;
    int[] oPtr = new int[1];  oPtr[0] = 0;
    int i, len, saveBLPtr = 0;
    int[] zaraTagEntryPtr = new int[1];

    if(source[0] == '2')
    {
      sourceBuf = buf2[0];
      targetBuf[0] = buf1[0];
      source[0] = '1';
    }
    else
    {
      sourceBuf = buf1[0];
      targetBuf[0] = buf2[0];
      source[0] = '2';
    }

    while(sourceBuf[iPtr[0]] != (byte)-1)
    {
      ch = sourceBuf[iPtr[0]];

      if(ch != '<')
      {
        if(doingBL)
          saveBL[saveBLPtr++] = ch;
        
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, ch);
        ++iPtr[0];
      }
      else // is an '<'
      {
        for(i=0;i<8;++i)
          tagName[i] = sourceBuf[i + iPtr[0]];
        tagName[i] = '\000';
        if(generalUtils.matchIgnoreCase(tagName, 0, "<ZaraTag"))
        {
          processZaraTag(zaraType, zaraOption, zaraMaxChars, zaraValues, zaraDefault, defaultValue, iPtr, zaraTagEntry, zaraTagEntryPtr, sourceBuf, token, value, zaraTableAndFieldName, zaraFieldName);

          if(generalUtils.matchIgnoreCase(zaraType, 0, "linex"))
          {
            processLineX(buf1, buf2, size1, size2, source, zaraTableAndFieldName, dataStrRes, linexName, targetBuf, oPtr, data, lenData);
          }
          else
          if(generalUtils.matchIgnoreCase(zaraType, 0, "image"))
          {
            processFreeStandingImageLine(buf1, buf2, size1, size2, source, zaraOption, targetBuf, oPtr);
          }  
          else
          if(( lineType == 'L' && generalUtils.matchIgnoreCase(zaraType, 0, "line") ) || ( lineType == 'H' && generalUtils.matchIgnoreCase(zaraType, 0, "head") ))
          {
            if(generalUtils.matchIgnoreCase(zaraType, 0, "line"))
            {
              if(generalUtils.matchIgnoreCase(zaraOption, 0, "begin"))
              {
                len = generalUtils.lengthBytes(zaraTagEntry, 0);
                System.arraycopy(zaraTagEntry, 0, saveBL, saveBLPtr, len);
                saveBLPtr += len;
                doingBL = true;
              }
              else // not a begin
              {
                if(generalUtils.matchIgnoreCase(zaraOption, 0, "end"))
                {
                  saveBL[saveBLPtr] = '\000';
                  writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, saveBL);
                  writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraTagEntry);
                  doingBL = false;
                }
                else
                {
                  if(generalUtils.startsWithIgnoreCase(zaraOption, 0, "javascript"))
                  {
                    if(javaScriptCode != null && javaScriptCode[0] != '\000')
                      generalUtils.bytesToBytes(javaScript, 0, javaScriptCode, 0);
                  }

                  if(generalUtils.matchIgnoreCase(zaraOption, 0, "image"))
                  {
                    processImageLine(buf1, buf2, size1, size2, source, zaraTableAndFieldName, dataStrRes, zaraFieldName, targetBuf, oPtr, numFields,
                                     fieldNames, tmp, data, lenData);
                  }  
                  else
                  {
                    processLine(buf1, buf2, size1, size2, source, zaraTableAndFieldName, zaraFieldName, zaraMaxChars, zaraOption, fieldSizes, dataStrRes,
                                defaultValue, tmp, javaScript, save, targetBuf, oPtr, numFields, fieldNames, type, data, lenData, displayOnlyFld, fldName,
                                presetLineEntry);
                  }

                  len = generalUtils.lengthBytes(zaraTagEntry, 0);
                  generalUtils.bytesToBytes(saveBL, saveBLPtr, zaraTagEntry, 0, len);
                  saveBLPtr += len;
                }
              }
            }
            else // not 'line'
            {
              if(generalUtils.matchIgnoreCase(zaraOption, 0, "image"))
              {
                processImageLine(buf1, buf2, size1, size2, source, zaraTableAndFieldName, dataStrRes, zaraFieldName, targetBuf, oPtr, numFields,
                                 fieldNames, tmp, data, lenData);
              }
              else
              if(generalUtils.matchIgnoreCase(zaraOption, 0, "html"))
              {
                processHtmlLine(buf1, buf2, size1, size2, source, zaraFieldName, zaraTableAndFieldName, dataStrRes, targetBuf, oPtr, numFields,
                                fieldNames, tmp, data, lenData);
              }
              else
              if(generalUtils.startsWithIgnoreCase(zaraOption, 0, "check"))
              {
                processCheckBoxLine(buf1, buf2, size1, size2, source, zaraFieldName, zaraValues, zaraOption, zaraDefault, zaraTableAndFieldName,
                                    dataStrRes, targetBuf, oPtr, numFields, fieldNames, tmp, data, lenData, type);
              }
              else
              if(generalUtils.matchIgnoreCase(zaraOption, 0, "dropdownlist"))
              {
                processDropDownListLine(buf1, buf2, size1, size2, source, zaraFieldName, zaraTableAndFieldName, dataStrRes, targetBuf, oPtr,
                                        numFields, fieldNames, tmp, data, lenData, type, ddlData, lenDDLData, addNoneToDDL);
              }
              else
              if(generalUtils.matchIgnoreCase(zaraOption, 0, "combo"))
              {
                processComboBoxLine(buf1, buf2, size1, size2, source, zaraFieldName, zaraTableAndFieldName, dataStrRes, targetBuf, oPtr, numFields, fieldNames,
                                    tmp, data, lenData, type, ddlData, lenDDLData, addNoneToDDL);
              }
              else
              if(generalUtils.matchIgnoreCase(zaraOption, 0, "textarea"))
              {
                processTextAreaLine(false, buf1, buf2, size1, size2, source, zaraFieldName, zaraValues, zaraTableAndFieldName, dataStrRes, targetBuf, oPtr,
                                    numFields, fieldNames, tmp, data, lenData, type);
              }
              else
              if(generalUtils.matchIgnoreCase(zaraOption, 0, "textareaC"))
              {
                processTextAreaLine(true, buf1, buf2, size1, size2, source, zaraFieldName, zaraValues, zaraTableAndFieldName, dataStrRes, targetBuf, oPtr,
                                    numFields, fieldNames, tmp, data, lenData, type);
              }
              else
              {
                if(generalUtils.startsWithIgnoreCase(zaraOption, 0, "radio"))
                {
                  processRadioButtonLine(buf1, buf2, size1, size2, source, zaraFieldName, zaraValues, zaraDefault, zaraTableAndFieldName, dataStrRes,
                                         targetBuf, oPtr, numFields, fieldNames, tmp, data, lenData, type);
                }  
                else 
                {
                  processLine(buf1, buf2, size1, size2, source, zaraTableAndFieldName, zaraFieldName, zaraMaxChars, zaraOption, fieldSizes,
                              dataStrRes, defaultValue, tmp, javaScript, save, targetBuf, oPtr, numFields, fieldNames, type, data, lenData,
                              displayOnlyFld, fldName, presetLineEntry);
                }
              }

              if(doingBL)
              {
                len = generalUtils.lengthBytes(zaraTagEntry, 0);
                System.arraycopy(zaraTagEntry, 0, saveBL, saveBLPtr, len);
                saveBLPtr += len;
                saveBL[saveBLPtr++] = '\n';
              }
            }
          }
          else // another type
          {
            if(generalUtils.startsWithIgnoreCase(zaraType, 0, "javascript"))
            {
              if(javaScriptCode != null && javaScriptCode[0] != '\000')
              {
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, javaScriptCode); // replaced 22feb03
                // insert in buf (rather than append) for cases where the javascript tag is not the last item shunt-up targetBuf
              }
            }
            else
            if(generalUtils.matchIgnoreCase(zaraType, 0, "prepend"))
            {
              if(prependCode != null && prependCode[0] != '\000')
              {                
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, prependCode);
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<ZaraTag type=prepend>");
              }
            }
            else
            if(generalUtils.matchIgnoreCase(zaraType, 0, "hidden"))
            {
              if(prependCode != null && prependCode[0] != '\000')
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, prependCode);
            }
            else
            if(generalUtils.matchIgnoreCase(zaraType, 0, "stepcount"))
            {
              processLine(buf1, buf2, size1, size2, source, zaraTableAndFieldName, zaraFieldName, zaraMaxChars, zaraOption, fieldSizes, dataStrRes,
                          defaultValue, tmp, javaScript, save, targetBuf, oPtr, numFields, fieldNames, type, data, lenData, displayOnlyFld, fldName,
                          presetLineEntry);
            }
            else
            {
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraTagEntry);
            }
          }
        }
        else // another tag
        {
          if(doingBL)
            saveBL[saveBLPtr++] = '<';
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<");
          ++iPtr[0];
        }
      }
    }

    writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, (byte)-1);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeToTargetBuf(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[][] targetBuf, int[] oPtr, int i)
  {
    Integer in = new Integer(i);
    writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, in.toString());
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeToTargetBuf(byte[][] buf1, byte[][] buf2, int[] iSize1, int[] iSize2, char[] source, byte[][] targetBuf, int[] oPtr, String s)
  {
    int len = s.length();
    
    if(source[0] == '1')
    {
      if((oPtr[0] + len) >= iSize1[0])
      {
        int newSize = iSize1[0] + len + 5000;
        byte[] buf3 = new byte[iSize1[0]];
        System.arraycopy(targetBuf[0], 0, buf3, 0, iSize1[0]);
        buf1[0] = new byte[newSize];
        targetBuf[0] = buf1[0];
        System.arraycopy(buf3, 0, targetBuf[0], 0, iSize1[0]);
        iSize1[0] = newSize;
      }
    }
    else // source set to 2
    {
      if((oPtr[0] + len) >= iSize2[0])
      {
        int newSize = iSize2[0] + len + 5000;
        byte[] buf3 = new byte[iSize2[0]];
        System.arraycopy(targetBuf[0], 0, buf3, 0, iSize2[0]);
        buf2[0] = new byte[newSize];
        targetBuf[0] = buf2[0];
        System.arraycopy(buf3, 0, targetBuf[0], 0, iSize2[0]);
        iSize2[0] = newSize;
      }
    }

    for(int i=0;i<len;++i)
      targetBuf[0][oPtr[0]++] = (byte)s.charAt(i);
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeToTargetBuf(byte[][] buf1, byte[][] buf2, int[] iSize1, int[] iSize2, char[] source, byte[][] targetBuf, int[] oPtr, byte[] b)
  {
    int len = generalUtils.lengthBytes(b, 0);

    if(source[0] == '1')
    {
      if((oPtr[0] + len) >= iSize1[0])
      {
        int newSize = iSize1[0] + len + 5000;
        byte[] buf3 = new byte[iSize1[0]];
        System.arraycopy(targetBuf[0], 0, buf3, 0, iSize1[0]);
        buf1[0] = new byte[newSize];
        targetBuf[0] = buf1[0];
        System.arraycopy(buf3, 0, targetBuf[0], 0, iSize1[0]);
        iSize1[0] = newSize;
      }
    }
    else // source set to 2
    {
      if((oPtr[0] + len) >= iSize2[0])
      {
        int newSize = iSize2[0] + len + 5000;
        byte[] buf3 = new byte[iSize2[0]];
        System.arraycopy(targetBuf[0], 0, buf3, 0, iSize2[0]);
        buf2[0] = new byte[newSize];
        targetBuf[0] = buf2[0];
        System.arraycopy(buf3, 0, targetBuf[0], 0, iSize2[0]);
        iSize2[0] = newSize;
      }
    }

    for(int i=0;i<len;++i)
      targetBuf[0][oPtr[0]++] = b[i];
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeToTargetBuf(byte[][] buf1, byte[][] buf2, int[] iSize1, int[] iSize2, char[] source, byte[][] targetBuf, int[] oPtr, byte b)
  {
    if(source[0] == '1')
    {
      if((oPtr[0] + 1) >= iSize1[0])
      {
        int newSize = iSize1[0] + 5000;
        byte[] buf3 = new byte[iSize1[0]];
        System.arraycopy(targetBuf[0], 0, buf3, 0, iSize1[0]);
        buf1[0] = new byte[newSize];
        targetBuf[0] = buf1[0];
        System.arraycopy(buf3, 0, targetBuf[0], 0, iSize1[0]);
        iSize1[0] = newSize;
      }
    }
    else // source set to 2
    {
      if((oPtr[0] + 1) >= iSize2[0])
      {
        int newSize = iSize2[0] + 5000;
        byte[] buf3 = new byte[iSize2[0]];
        System.arraycopy(targetBuf[0], 0, buf3, 0, iSize2[0]);
        buf2[0] = new byte[newSize];
        targetBuf[0] = buf2[0];
        System.arraycopy(buf3, 0, targetBuf[0], 0, iSize2[0]);
        iSize2[0] = newSize;
      }
    }

    targetBuf[0][oPtr[0]++] = b;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processZaraTag(byte[] zaraType,  byte[] zaraOption, byte[] zaraMaxChars, byte[] zaraValues, byte[] zaraDefault, byte[] defaultValue, int[] iPtr, byte[] zaraTagEntry, int[] zaraTagEntryPtr, byte[] sourceBuf, byte[] token,
                              byte[] value, byte[] zaraTableAndFieldName, byte[] zaraFieldName)
  {
    try
    {
      zaraType[0] = zaraOption[0] = zaraMaxChars[0] = zaraValues[0] = zaraDefault[0] = defaultValue[0] = '\000';

      iPtr[0] += 8;

      int tokenPtr;

      generalUtils.catAsBytes("<ZaraTag", 0, zaraTagEntry, true);
      zaraTagEntryPtr[0] = 8;
      while(sourceBuf[iPtr[0]] != '>' && sourceBuf[iPtr[0]] != (byte)-1)
      {
        while(sourceBuf[iPtr[0]] == ' ' && sourceBuf[iPtr[0]] != (byte)-1)
        {
          zaraTagEntry[zaraTagEntryPtr[0]++] = ' ';
          ++iPtr[0];
        }

        tokenPtr=0;
        while(sourceBuf[iPtr[0]] != '=' && sourceBuf[iPtr[0]] != '>' && sourceBuf[iPtr[0]] != (byte)-1)
        {
          zaraTagEntry[zaraTagEntryPtr[0]++] = sourceBuf[iPtr[0]];
          token[tokenPtr++] = sourceBuf[iPtr[0]++];
        }
        token[tokenPtr] = '\000';

        if(sourceBuf[iPtr[0]] == '=')
        {
          zaraTagEntry[zaraTagEntryPtr[0]++] = '=';
          ++iPtr[0];
        }

        int valueStart = iPtr[0];
        int valuePtr = 0;

        if(sourceBuf[iPtr[0]] == '"') // is in quotes
        {
          value[valuePtr++] = '"';
          ++iPtr[0];
          while(sourceBuf[iPtr[0]] != '"' && sourceBuf[iPtr[0]] != (byte)-1)
            value[valuePtr++] = sourceBuf[iPtr[0]++];
          value[valuePtr++] = '"';
          ++iPtr[0];
        }
        else
        {
          if(generalUtils.matchIgnoreCase(token, 0, "Values"))
          {
            for(int i=0;i<2;++i)
              value[valuePtr++] = sourceBuf[iPtr[0]++];
  
            if(sourceBuf[iPtr[0]-1] == ',')
            {
              while(sourceBuf[iPtr[0]] != ' ' && sourceBuf[iPtr[0]] != '>' && sourceBuf[iPtr[0]] != (byte)-1)
                value[valuePtr++] = sourceBuf[iPtr[0]++];
            }
          }
          else
          {
            while(sourceBuf[iPtr[0]] != ' ' && sourceBuf[iPtr[0]] != '>' && sourceBuf[iPtr[0]] != (byte)-1)
              value[valuePtr++] = sourceBuf[iPtr[0]++];
          }
        }
        value[valuePtr] = '\000';

        if(generalUtils.matchIgnoreCase(token, 0, "Type"))
          generalUtils.bytesToBytes(zaraType, 0, value, 0);
        else
        if(generalUtils.matchIgnoreCase(token, 0, "Option"))
          generalUtils.bytesToBytes(zaraOption, 0, value, 0);
        else
        if(generalUtils.matchIgnoreCase(token, 0, "MaxChars"))
          generalUtils.bytesToBytes(zaraMaxChars, 0, value, 0);
        else
        if(generalUtils.matchIgnoreCase(token, 0, "Values"))
          generalUtils.bytesToBytes(zaraValues, 0, value, 0);
        else
        if(generalUtils.matchIgnoreCase(token, 0, "Default"))
          generalUtils.bytesToBytes(zaraDefault, 0, value, 0);
        else
        if(generalUtils.matchIgnoreCase(token, 0, "Name"))
        {
          int i=0;
          while(value[i] != '.') // first part upto '.'
          {
            zaraTableAndFieldName[i] = value[i];
            ++i;
          }
          zaraTableAndFieldName[i++] = '.';

          int j=0;
          while(i < valuePtr)
          {
            zaraFieldName[j++]       = value[i];
            zaraTableAndFieldName[i] = value[i];
            ++i;
          }
          zaraFieldName[j] = '\000';
          zaraTableAndFieldName[i] = '\000';
        }
        else
        if(generalUtils.matchIgnoreCase(token, 0, "DefaultValue"))
        {
          if(value[0] == '"') // defaultValue is in quotes
          {
            int y=0;
            iPtr[0] = valueStart + 1;
            while(sourceBuf[iPtr[0]] != '"' && sourceBuf[iPtr[0]] != '>' && sourceBuf[iPtr[0]] != (byte)-1)
              defaultValue[y++] = sourceBuf[iPtr[0]++];
            defaultValue[y] = '\000';
          }
          else generalUtils.bytesToBytes(defaultValue, 0, value, 0);
        }

        System.arraycopy(value, 0, zaraTagEntry, zaraTagEntryPtr[0], valuePtr);
        zaraTagEntryPtr[0] += valuePtr;
      }

      if(sourceBuf[iPtr[0]] == '>')
      {
        ++iPtr[0];
        zaraTagEntry[zaraTagEntryPtr[0]++] = '>';
      }

      zaraTagEntry[zaraTagEntryPtr[0]] = '\000';
    }
    catch(Exception e)
    {
      System.out.println("screenLayout: " + e);
      generalUtils.pb("token", token, 0,0);
      generalUtils.pb("sourceBuf", sourceBuf, 0,0);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // format of data: "do.docode=12345/DO\001 ..."
  private void searchDataString(byte[] zaraTableAndFieldName, byte[] dataStrRes, byte[] data, int lenData)
  {
    int x, ptr=0, dataStrResPtr;
    byte[] tfName = new byte[50];
    
    while(ptr < lenData)
    {
      x=0;
      while(data[ptr] != '\000' && data[ptr] != '=')
        tfName[x++] = data[ptr++];
      tfName[x] = '\000';

      if(generalUtils.matchIgnoreCase(tfName, 0, zaraTableAndFieldName, 0))
      {
        ++ptr;
        dataStrResPtr = 0;
        while(data[ptr] != '\001' && data[ptr] != '\000')
          dataStrRes[dataStrResPtr++] = data[ptr++];
        dataStrRes[dataStrResPtr] = '\000';
        return;
      }
      
      // not the reqd table/field entry
      ++ptr;
      while(data[ptr] != '\001' && data[ptr] != '\000') // e o data entry
        ++ptr;
      ++ptr;
    }  

    dataStrRes[0] = '\000'; // data not found
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // format of data: "do.docode="12345/DO" ..."
  private void searchDataStringForLineX(byte[] linexName, byte[] dataStrRes, byte[] zaraTableAndFieldName, byte[] data, int lenData)
  {
    int x, y, ptr=0, dataStrResPtr;
    byte[] tfName = new byte[50];

    while(ptr < lenData)
    {
      x=0;
      while(data[ptr] != '\000' && data[ptr] != '=')
        tfName[x++] = data[ptr++];
      tfName[x] = '\000';

      x=y=0;
      while(zaraTableAndFieldName[x] != '\000')
        linexName[y++] = zaraTableAndFieldName[x++];
      linexName[y] = '\000';

      if(generalUtils.matchIgnoreCase(tfName, 0, linexName, 0))
      {
        ++ptr;
        dataStrResPtr = 0;
        while(data[ptr] != '\001' && data[ptr] != '\000')
          dataStrRes[dataStrResPtr++] = data[ptr++];
        dataStrRes[dataStrResPtr] = '\000';
        return;
      }
      
      // not the reqd table/field entry
      ++ptr;
      while(data[ptr] != '\001' && data[ptr] != '\000') // e o data entry
        ++ptr;
      ++ptr;
    }

    dataStrRes[0] = '\000'; // data not found
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraTableAndFieldName, byte[] zaraFieldName, byte[] zaraMaxChars, byte[] zaraOption, short[] fieldSizes, byte[] dataStrRes,
                           byte[] defaultValue, byte[] tmp, byte[] javaScript, byte[] save, byte[][] targetBuf, int[] oPtr, short numFields, byte[] fieldNames, char type, byte[] data, int lenData, boolean displayOnlyFld, String fldName,
                           int presetLineEntry)
  {
    try
    {
      boolean permitted;
      int fieldSize;
      int entry = findFieldDetails(zaraFieldName, numFields, fieldNames, tmp);

      if(entry != -1) // is a data item (otherwise assume a program-supplied item)
      {
        fieldSize = getFieldSize(fieldSizes, entry);
      }
      else fieldSize = 1000;

      int maxChars;
      if(zaraMaxChars[0] == '\000')
        maxChars = fieldSize;
      else maxChars = generalUtils.intFromBytesCharFormat(zaraMaxChars, (short)0);

      searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

      if(type == 'E') // edit
      {
        if(displayOnlyFld) // 'preprocess'
        {
          if(generalUtils.matchIgnoreCase(zaraFieldName, 0, fldName)) // this is the code field
            generalUtils.strToBytes(zaraOption, "displayonly");
        }

        boolean hideIt = false;
        if(generalUtils.matchIgnoreCase(zaraOption, 0, "displayonly"))
        {
          if(dataStrRes[0] != '\000')
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, dataStrRes);
          else
          if(generalUtils.matchIgnoreCase(zaraFieldName, 0, "line"))
          {
            if(presetLineEntry != 0)
            {
              generalUtils.intToBytesCharFormat(presetLineEntry, tmp, (short)0);
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, tmp);
            }
          }

          hideIt = true;
        }

        if(hideIt || zaraFieldName[0] == '_')
        {
          if(hideIt)
           writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT TYPE=HIDDEN NAME=\"");
          else
          {
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT class='input' TYPE=TEXT NAME=\"");
          }
        }
        else
        if(generalUtils.matchIgnoreCase(zaraOption, 0, "display"))
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT class='input' TYPE=\"TEXT\" NAME=\"");
        else writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT class='input' TYPE=\"TEXT\" NAME=\"");

        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);

        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\" ID=\"");
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);


        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\" SIZE=\"");
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, maxChars);
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\" MAXLENGTH=\"");
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, fieldSize);
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\" VALUE=\"");

        if(dataStrRes[0] != '\000')
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, sanitiseDoubleQuotes(dataStrRes));
        else
        {
          if(defaultValue[0] != '\000')
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, defaultValue);
          else // no default, so chk if its 'line'
          {
            if(generalUtils.matchIgnoreCase(zaraFieldName, 0, "line"))
            {
              if(presetLineEntry != 0)
              {
                generalUtils.intToBytesCharFormat(presetLineEntry, tmp, (short)0);
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, tmp);
              }
            }
            // else VALUE is blank
          }
        }
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\">\n");
      }
      else // display
      {
        if(generalUtils.matchIgnoreCase(zaraOption, 0, "javascriptUsingCode"))
        {
          generalUtils.bytesToBytes(save, 0, dataStrRes, 0);

          generalUtils.catAsBytes("_.permitted", 0, zaraTableAndFieldName, true);

          searchDataString(zaraTableAndFieldName, dataStrRes, javaScript, 1000);

          if(dataStrRes[0] != 'n') // permitted
            permitted = true;
          else permitted = false;

          String s="";
          if(permitted)
            s = "<a class='a' href=\"javascript:affect('";

          generalUtils.catAsBytes("_.lineFile", 0, zaraTableAndFieldName, true);
          searchDataString(zaraTableAndFieldName, dataStrRes, javaScript, 1000);

          generalUtils.catAsBytes(generalUtils.stringFromBytes(dataStrRes, 0L), 0, zaraTableAndFieldName, true);
          searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

          if(permitted)
            s += (generalUtils.stringFromBytes(dataStrRes, 0L) + "')\" id='"+generalUtils.stringFromBytes(dataStrRes, 0L)+"'>" + generalUtils.stringFromBytes(save, 0L) + "</a>");
          else s += generalUtils.stringFromBytes(dataStrRes, 0L);

          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, s);
        }
        else
        if(generalUtils.matchIgnoreCase(zaraOption, 0, "javascriptForStock"))
        {
          generalUtils.bytesToBytes(save, 0, dataStrRes, 0);

          generalUtils.catAsBytes("_.stockPermitted", 0, zaraTableAndFieldName, true);

          searchDataString(zaraTableAndFieldName, dataStrRes, javaScript, 1000);

          if(dataStrRes[0] != 'n') // permitted
            permitted = true;
          else permitted = false;

          String s="";
          if(permitted)
            s = "<a class='a' href=\"javascript:stockRec('";

          generalUtils.catAsBytes("_.stockRec", 0, zaraTableAndFieldName, true);
          searchDataString(zaraTableAndFieldName, dataStrRes, javaScript, 1000);

          generalUtils.catAsBytes(generalUtils.stringFromBytes(dataStrRes, 0L), 0, zaraTableAndFieldName, true);
          searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

          if(permitted)
            s += (generalUtils.stringFromBytes(dataStrRes, 0L) + "')\">" + generalUtils.stringFromBytes(save, 0L) + "</a>");
          else s += generalUtils.stringFromBytes(dataStrRes, 0L);

          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, s);
        }
        else
        if(generalUtils.matchIgnoreCase(zaraOption, 0, "javascriptForSO"))
        {
          generalUtils.bytesToBytes(save, 0, dataStrRes, 0);

          generalUtils.catAsBytes("_.soPermitted", 0, zaraTableAndFieldName, true);

          searchDataString(zaraTableAndFieldName, dataStrRes, javaScript, 1000);

          if(dataStrRes[0] != 'n') // permitted
            permitted = true;
          else permitted = false;

          String s="";
          if(permitted)
            s = "<a class='a' href=\"javascript:soRec('";

          generalUtils.catAsBytes("_.soRec", 0, zaraTableAndFieldName, true);
          searchDataString(zaraTableAndFieldName, dataStrRes, javaScript, 1000);

          generalUtils.catAsBytes(generalUtils.stringFromBytes(dataStrRes, 0L), 0, zaraTableAndFieldName, true);
          searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

          if(permitted)
            s += (generalUtils.stringFromBytes(dataStrRes, 0L) + "')\">" + generalUtils.stringFromBytes(save, 0L) + "</a>");
          else s += generalUtils.stringFromBytes(dataStrRes, 0L);

          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, s);
        }
        else
        {
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, dataStrRes);
        }
      }
    }
    catch(Exception e){ System.out.println("ScreenLayout: " + generalUtils.stringFromBytes(zaraFieldName, 0) + " " + e); }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private String sanitiseDoubleQuotes(byte[] buf) throws Exception
  {
    String s="";
    int x=0;
    while(buf[x] != '\000')
    {
      if(buf[x] == '"')
        s += "''";
      else s += (char)buf[x];
      ++x;
    }
    
    return s;    
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processImageLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraTableAndFieldName,
                                byte[] dataStrRes, byte[] zaraFieldName, byte[][] targetBuf, int[] oPtr, short numFields,
                                byte[] fieldNames, byte[] tmp, byte[] data, int lenData)
  {
    try
    {
      if(findFieldDetails(zaraFieldName, numFields, fieldNames, tmp) != -1) // is a data item
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

        if(dataStrRes[0] == '\000')
          ;
        else
        {
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<IMG BORDER=0 SRC=\"");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, dataStrRes);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\">");
        }
      }
      else // not a data item but perhaps added-in on the fly
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);
        if(dataStrRes[0] == '\000')
          ;
        else
        {
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<IMG BORDER=0 SRC=\"");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, dataStrRes);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\">");
        }
      }
    }
    catch(Exception e){}
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processFreeStandingImageLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraOption,
                                            byte[][] targetBuf, int[] oPtr)
  {
    try
    {
      writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<IMG SRC=\"");
      int x;
      if(zaraOption[0] == '"')
        x=1;
      else x=0;
      while(zaraOption[x] != '\000' && zaraOption[x] != '"')
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraOption[x++]);
      writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\">\n");
    }
    catch(Exception e){};
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processHtmlLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraFieldName,
                               byte[] zaraTableAndFieldName, byte[] dataStrRes, byte[][] targetBuf, int[] oPtr, short numFields,
                               byte[] fieldNames, byte[] tmp, byte[] data, int lenData)
  {
    try
    {
      if(findFieldDetails(zaraFieldName, numFields, fieldNames, tmp) != -1) // is a data item
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);
        if(dataStrRes[0] != '\000')
        {
          RandomAccessFile fh;
          if((fh = generalUtils.fileOpen(generalUtils.stringFromBytes(dataStrRes, 0L))) != null)
          {
            try
            {
              while(true)
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, fh.readByte());
            }
            catch(Exception e) { }
          }
          generalUtils.fileClose(fh);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\n");
        }
      }
    }
    catch(Exception e){}
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processCheckBoxLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraFieldName, 
                                   byte[] zaraValues, byte[] zaraOption, byte[] zaraDefault, byte[] zaraTableAndFieldName,
                                   byte[] dataStrRes, byte[][] targetBuf, int[] oPtr, short numFields, byte[] fieldNames, byte[] tmp,
                                   byte[] data, int lenData, char type)
  {
    try
    {
      if(findFieldDetails(zaraFieldName, numFields, fieldNames, tmp) != -1) // is a data item
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

        if(type == 'E')
        {
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "checkdisplay"))
          {
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraDefault);
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT class='p' TYPE=CHECKBOX NAME=\"");
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");

            if(dataStrRes[0] != '\000') // just-in-case
            {
              if(dataStrRes[0] == zaraValues[1])
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " CHECKED");
            }

            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " VALUE=\"");
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraValues[0]);
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, ">\n");
          }
          else
          {
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraDefault);
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT class='p' TYPE=CHECKBOX NAME=\"");
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");

            if(dataStrRes[0] != '\000') // just-in-case
            {
              if(dataStrRes[0] == zaraValues[1])
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " CHECKED");
            }
          
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " VALUE=\"");
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraValues[0]);
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");            
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " style={background-color:transparent;}>\n");
          }
        }
        else
        {
          if(dataStrRes[0] != '\000') // just-in-case
          {
            if(dataStrRes[0] == zaraValues[1])
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraDefault);
          }
        }
      }
    }
    catch(Exception e){}
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processRadioButtonLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraFieldName,
                                      byte[] zaraValues, byte[] zaraDefault, byte[] zaraTableAndFieldName, byte[] dataStrRes,
                                      byte[][] targetBuf, int[] oPtr, short numFields, byte[] fieldNames, byte[] tmp, byte[] data,
                                      int lenData, char type)
  {
    try
    {
      if(findFieldDetails(zaraFieldName, numFields, fieldNames, tmp) != -1) // is a data item
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

        if(type == 'E')
        {
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT class='p' TYPE=RADIO NAME=\"");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");
 
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " VALUE=\"");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraValues[0]);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");          
          
          if(dataStrRes[0] != '\000') // just-in-case
          {
            if(dataStrRes[0] == zaraValues[0])
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " CHECKED");
          }
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " style={background-color:transparent;}>");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraDefault);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\n");
        }
        else
        {
          if(dataStrRes[0] == zaraValues[0])
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraDefault);
        }
      }
    }
    catch(Exception e){}
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void processTextAreaLine(boolean isCourier, byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraFieldName,
                                   byte[] zaraValues, byte[] zaraTableAndFieldName, byte[] dataStrRes, byte[][] targetBuf, int[] oPtr,
                                   short numFields, byte[] fieldNames, byte[] tmp, byte[] data, int lenData, char type)
  {
    try
    {
      if(findFieldDetails(zaraFieldName, numFields, fieldNames, tmp) != -1) // is a data item
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);
        String s = "";
        int len;
        if(type == 'E')
        {
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<TEXTAREA class='textarea' NAME=\"");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr,  zaraFieldName);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");


      writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " ID=\"");
      writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);
      writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");

          String rows="";
          short x=0;
          while(x < generalUtils.lengthBytes(zaraValues, 0) && zaraValues[x] != ',')
            rows += (char)zaraValues[x++];
          if(x < generalUtils.lengthBytes(zaraValues, 0) && zaraValues[x] == ',')
            ++x;
          String cols="";
          while(x < generalUtils.lengthBytes(zaraValues, 0))
            cols += (char)zaraValues[x++];

          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " ROWS=");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, rows);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " COLS=");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, cols);

          if(isCourier)
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " STYLE=\"FONT-FAMILY:courier\"");

          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " WRAP=\"HARD\">");

          len = generalUtils.lengthBytes(dataStrRes, 0);
          for(int i=0;i<len;++i)
          {
            if(dataStrRes[i] == '\003' || dataStrRes[i] == '\n')
              s += "\012";
            else s += (char)dataStrRes[i];
          }
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, s);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "</TEXTAREA>");
        }
        else
        {
          len = generalUtils.lengthBytes(dataStrRes, 0);
          for(int i=0;i<len;++i)
          {
            if(dataStrRes[i] == '\003' || dataStrRes[i] == '\n')
              s += "<br>";
            else s += (char)dataStrRes[i];
          }
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, s);
        }
      }
    }
    catch(Exception e){}
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processDropDownListLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraFieldName,
                                       byte[] zaraTableAndFieldName, byte[] dataStrRes, byte[][] targetBuf, int[] oPtr, short numFields, byte[] fieldNames,
                                       byte[] tmp, byte[] data, int lenData, char type, byte[] ddlData, int lenDDLData, boolean addNoneToDDL)
  {
    try
    {
      if(findFieldDetails(zaraFieldName, numFields, fieldNames, tmp) != -1) // is a data item
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

        if(dataStrRes[0] == '\000')
          generalUtils.catAsBytes(" ", 0, dataStrRes, true);
        if(type == 'E')
        {
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<SELECT class='select' NAME=\"");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);

        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\" ID=\"");
        writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);

          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\">");
          int x, y;

          byte[] option = new byte[100];
          byte[] optionVisible = new byte[100];
          byte[] optionNotVisible = new byte[100];
          int[] upto = new int[1];
          upto[0]=0;
          boolean quit = false;
          boolean optionIsNone=false;
          while(! quit)
          {
            searchDDLData(zaraTableAndFieldName, ddlData, lenDDLData, upto, option);

            if(option[0] == '\000') // if no (more) options specified
            {
              if(addNoneToDDL)
              {
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<OPTION VALUE=\"<none>\"");
                if(optionIsNone)
                  writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " SELECTED");
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, ">none\n");
              }
              quit = true;
            }
            else
            {
              x=0;
              while(option[x] != '\001' && option[x] != '\000')
              {
                optionVisible[x]    = option[x];
                optionNotVisible[x] = option[x]; // just-in-case no nonvisible part specified
                ++x;
              }
              
              if(option[x] == '\001')
              {
                optionVisible[x] = '\000';
                ++x;
                y=0;
                while(option[x] != '\000')
                  optionNotVisible[y++] = option[x++];
                optionNotVisible[y] = '\000';
              }
              else
              {
                optionNotVisible[x] = '\000';
                optionVisible[x] = '\000';
              }

              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<OPTION VALUE=\"");
              generalUtils.stripTrailingSpaces(optionNotVisible);
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, optionNotVisible);
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");
              if(generalUtils.matchIgnoreCase(optionNotVisible, 0, dataStrRes, 0))
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " SELECTED");
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, ">");
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, optionVisible);
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\n");

              if(generalUtils.match(dataStrRes, "<none>"))
                optionIsNone = true;
            }
          }
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "</SELECT>\n");
        }
        else writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, dataStrRes);
      }
    }
    catch(Exception e){ System.out.println("ScreenLayout: "+e);}
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  // format of data: ...company.salesperson="Basil" company.salesperson="Fred"...
  private void searchDDLData(byte[] zaraTableAndFieldName, byte[] data, int lenData, int[] upto, byte[] value)
  {
    int x;
    byte[] tfName = new byte[50];
    
    while(upto[0] < lenData && data[upto[0]] != '\000')
    {
      x=0;
      while(data[upto[0]] != '\000' && data[upto[0]] != '=')
        tfName[x++] = data[upto[0]++];
      tfName[x] = '\000';

      if(generalUtils.matchIgnoreCase(tfName, 0, zaraTableAndFieldName, 0))
      {
        upto[0] += 2; // '="'
        int valuePtr=0;
        while(data[upto[0]] != '"')
          value[valuePtr++] = data[upto[0]++];
        value[valuePtr] = '\000';
        upto[0] += 2;
        return;
      }
      
      // not the reqd table/field entry
      upto[0] += 2; // '="'
      while(data[upto[0]] != '"') // e o data entry
        ++upto[0];
      upto[0] += 2;
    }

    value[0] = '\000';
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int findFieldDetails(byte[] fldName, short numFields, byte[] fieldNames, byte[] tmp) throws Exception
  {
    for(short i=0;i<numFields;++i)
    {
      generalUtils.dfsGivenSeparator(false, '\001', fieldNames, i, tmp);

      if(generalUtils.matchIgnoreCase(fldName, 0, tmp, 0))
        return i;
    }

    return -1;
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private int getFieldSize(short[] fieldSizes, int entry) throws Exception
  {
    if(fieldSizes[entry] == 0)
      return 15;
   
    return fieldSizes[entry];
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  public void buildNewDetails(byte[] fieldNames, short numFields, String fileName, byte[] buf) throws Exception
  {
    String fieldName = generalUtils.dfsAsStrGivenSeparator('\001', fieldNames, (short)0);
    generalUtils.catAsBytes(fileName + "." + fieldName, 0, buf, true);
    generalUtils.catAsBytes("=\001", 0, buf, false);
    for(short i=1;i<numFields;++i)
    {
      fieldName = generalUtils.dfsAsStrGivenSeparator('\001', fieldNames, i);
      generalUtils.catAsBytes(fileName + "." + fieldName, 0, buf, false);
      generalUtils.catAsBytes("=\001", 0, buf, false);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processLineX(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraTableAndFieldName, byte[] dataStrRes,
                            byte[] linexName, byte[][] targetBuf, int[] oPtr, byte[] data, int lenData)
  {
    try
    {
      searchDataStringForLineX(linexName, dataStrRes, zaraTableAndFieldName, data, lenData);
      writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, dataStrRes);
    }
    catch(Exception e){};
  }

  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  //  <DIV STYLE="position:relative">  
  //    <INPUT TYPE="text" NAME=textInput SIZE=18 AUTOCOMPLETE="OFF" ONKEYDOWN="ct_onkeydown(event,this,this.form.selectInput)">  
  //    <SELECT NAME=selectInput id=selectInput SIZE=8 STYLE="display:none;position:absolute;top:20px;left:0px" ONBLUR="this.style.display='none'"  
  //            ONCHANGE="cs_onchange(this,this.form.textInput)" ONKEYUP="cs_onkeyup(event.keyCode,this,this.form.textInput)">  
  //      <OPTION VALUE="Kansas City">Kansas City</OPTION>  
  //      <OPTION VALUE="Overland Park">Overland Park</OPTION>  
  //      <OPTION VALUE="St. Louis">St. Louis</OPTION>  
  //  </SELECT>  
  //  </DIV>
  // Note: Only one combobox on a form at any one time (due to use of the 'selectInput' name)
  private void processComboBoxLine(byte[][] buf1, byte[][] buf2, int[] size1, int[] size2, char[] source, byte[] zaraFieldName, byte[] zaraTableAndFieldName,
                                   byte[] dataStrRes, byte[][] targetBuf, int[] oPtr, short numFields, byte[] fieldNames, byte[] tmp, byte[] data, int lenData,
                                   char type, byte[] ddlData, int lenDDLData, boolean addNoneToDDL)
  {
    try
    {
      if(findFieldDetails(zaraFieldName, numFields, fieldNames, tmp) != -1) // is a data item
      {
        searchDataString(zaraTableAndFieldName, dataStrRes, data, lenData);

        if(dataStrRes[0] == '\000')
          generalUtils.catAsBytes(" ", 0, dataStrRes, true);
        if(type == 'E')
        {
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<DIV STYLE='position:relative'>");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<INPUT type='text' NAME=\"");
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, zaraFieldName);
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\" AUTOCOMPLETE='OFF' ONKEYDOWN='ct_onkeydown(event,this,this.form.selectInput)'");
        
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " VALUE=\"");
          if(dataStrRes[0] != '\000')
            writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, sanitiseDoubleQuotes(dataStrRes));
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\">\n");
          
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<SELECT NAME=selectInput id=selectInput SIZE=20"
                                                                            + " STYLE='display:none;position:absolute;top:20px;left:0px'"
                                                                            + " ONBLUR=\"this.style.display='none'\""
                                                                            + " ONCHANGE=\"cs_onchange(this,this.form."
                                                                            + generalUtils.stringFromBytes(zaraFieldName, 0L)
                                                                            + ")\" ONKEYUP=\"cs_onkeyup(event.keyCode,this,this.form."
                                                                            + generalUtils.stringFromBytes(zaraFieldName, 0L) + ")\">\n");  

          int x, y;

          byte[] option = new byte[100];
          byte[] optionVisible = new byte[100];
          byte[] optionNotVisible = new byte[100];
          int[] upto = new int[1];
          upto[0]=0;
          boolean quit = false;
          boolean optionIsNone=false;
          while(! quit)
          {
            searchDDLData(zaraTableAndFieldName, ddlData, lenDDLData, upto, option);

            if(option[0] == '\000') // if no (more) options specified
            {
              if(addNoneToDDL)
              {
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<OPTION VALUE=\"<none>\"");
                if(optionIsNone)
                  writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " SELECTED");
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, ">none\n");
              }
              quit = true;
            }
            else
            {
              x=0;
              while(option[x] != '\001' && option[x] != '\000')
              {
                optionVisible[x]    = option[x];
                optionNotVisible[x] = option[x]; // just-in-case no nonvisible part specified
                ++x;
              }
              
              if(option[x] == '\001')
              {
                optionVisible[x] = '\000';
                ++x;
                y=0;
                while(option[x] != '\000')
                  optionNotVisible[y++] = option[x++];
                optionNotVisible[y] = '\000';
              }
              else
              {
                optionNotVisible[x] = '\000';
                optionVisible[x] = '\000';
              }

              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "<OPTION VALUE=\"");
              generalUtils.stripTrailingSpaces(optionNotVisible);
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, optionNotVisible);
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\"");
              if(generalUtils.matchIgnoreCase(optionNotVisible, 0, dataStrRes, 0))
                writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, " SELECTED");
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, ">");
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, optionVisible);
              writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "\n");

              if(generalUtils.match(dataStrRes, "<none>"))
                optionIsNone = true;
            }
          }
          writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, "</SELECT></DIV>\n");
        }
        else writeToTargetBuf(buf1, buf2, size1, size2, source, targetBuf, oPtr, dataStrRes);
      }
    }
    catch(Exception e){ System.out.println("ScreenLayout: "+e);}
  }

}
