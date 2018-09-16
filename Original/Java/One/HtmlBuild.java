// =======================================================================================================================================================================================================
// System: ZaraStar: Utils: Build record html save and caller strings
// Module: HtmlBuild.java
// Author: C.K.Harvey
// Copyright (c) 1998-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.io.*;

public class HtmlBuild
{
  GeneralUtils generalUtils = new GeneralUtils();

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // html input to temp buf1
  public byte[] layoutToBuffer(byte[] sourceBuf, int iSize1, String defnsDir, String zaraDefnsDir, String sourceHtml) throws Exception
  {
    sourceBuf[0] = -1;
    int ch=0;
    int x=0;

    FileInputStream fis;
    try
    {
      fis = new FileInputStream(defnsDir + sourceHtml);
    }
    catch(FileNotFoundException e)
    {
      fis = new FileInputStream(zaraDefnsDir + sourceHtml);
    }

    BufferedInputStream htmlInput = new BufferedInputStream(fis);

    while(ch != -1)
    {
      ch = htmlInput.read();
      try
      {
        sourceBuf[x] = (byte)ch;
        ++x;
      }
      catch(ArrayIndexOutOfBoundsException e)
      {
        byte[] buf3 = new byte[iSize1];
        System.arraycopy(sourceBuf, 0, buf3, 0, iSize1);
        iSize1 += 5000;
        sourceBuf = new byte[iSize1];
        System.arraycopy(buf3, 0, sourceBuf, 0, iSize1 - 5000);
        sourceBuf[x++] = (byte)ch;
      }
    }
    sourceBuf[x] = -1;
    
    htmlInput.close();
    fis.close();
    
    return sourceBuf;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildSaveString(byte[] sourceBuf, String htmlPage) throws Exception
  {
    return buildSaveString(sourceBuf, htmlPage, 'E');
  }
  public String buildSaveString(byte[] sourceBuf, String htmlPage, char dispOrEdit) throws Exception
  {
    String[] zaraTableAndFieldName = new String[1];
    String[] zaraFieldName         = new String[1];
    byte[] zaraType      = new byte[20];
    byte[] zaraOption    = new byte[40];
    byte[] zaraDefault   = new byte[200];
    byte[] zaraMaxChars  = new byte[10];
    byte[] zaraValues    = new byte[200];
    byte[] zaraTagEntry = new byte[200]; // plenty
    byte[] token = new byte[200];
    byte[] value = new byte[200];
    int[] zaraTagEntryPtr = new int[1];
    byte[] tagName = new byte[10];
    int[] iPtr = new int[1];
    
    byte ch;
    String saveStr="";

    iPtr[0]=0;
    int i;
    while(sourceBuf[iPtr[0]] != -1)
    {
      ch = sourceBuf[iPtr[0]];
      if(ch != '<')
        ++iPtr[0];
      else // is an '<'
      {
        for(i=0;i<8;++i)
          tagName[i] = sourceBuf[i + iPtr[0]];
        tagName[i] = '\000';

        if(generalUtils.matchIgnoreCase(tagName, 0, "<ZaraTag"))
        {
          processZaraTag(sourceBuf, zaraTableAndFieldName, zaraFieldName, zaraType, zaraOption, zaraMaxChars, zaraValues, zaraDefault, token, value,
                         zaraTagEntry, zaraTagEntryPtr, iPtr);

          if(generalUtils.matchIgnoreCase(zaraType, 0, "prepend"))
           ; // nowt
          else
          if(generalUtils.matchIgnoreCase(zaraType, 0, "image"))
           ; // nowt
          else
          if(generalUtils.matchIgnoreCase(zaraType, 0, "linex"))
            ; // nowt
          else                                     
          if(generalUtils.matchIgnoreCase(zaraType, 0, "hidden"))
            ;
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "image"))
            ; // ignore
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "check") || generalUtils.startsWithIgnoreCase(zaraOption, 0, "check") && dispOrEdit == 'E')
          {
            saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\\\"\";if(" + htmlPage + "." + zaraFieldName[0] + ".checked)saveStr+=\""
                    + (char)zaraValues[1] + "\\\" \";else saveStr+=\"" + (char)zaraValues[0] + "\\\" \";\n";
          }
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "dropdownlist"))
          {
            saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\\\"\";saveStr+=" + htmlPage + "." + zaraFieldName[0] + ".options[" + htmlPage + "."
                    + zaraFieldName[0] + ".selectedIndex].value;saveStr+=\"\\\" \";\n";
          }
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "combo"))
          {
            saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\\\"\";saveStr+=" + htmlPage + "." + zaraFieldName[0] + ".value;saveStr+=\"\\\" \";\n";
          }
          else
          {
            if(generalUtils.startsWithIgnoreCase(zaraOption, 0, "radio"))
            {
              saveStr += "if(" + htmlPage + "." + zaraFieldName[0] + "[" + (char)zaraOption[5] + "].checked)saveStr+=\"" + zaraTableAndFieldName[0] + "=\\\""
                      + (char)zaraValues[0] + "\\\" \";\n";
            }
            else
            {
              saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\\\"\";saveStr+=" + htmlPage + "." + zaraFieldName[0] + ".value;saveStr+=\"\\\" \";\n";
            }
          }

          ++iPtr[0];
        }
        else ++iPtr[0];
      }
    }

    return saveStr;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildSaveStringDelimited(byte[] sourceBuf, String htmlPage) throws Exception
  {
    return buildSaveStringDelimited(sourceBuf, htmlPage, 'E');
  }
  public String buildSaveStringDelimited(byte[] sourceBuf, String htmlPage, char dispOrEdit) throws Exception
  {
    String[] zaraTableAndFieldName = new String[1];
    String[] zaraFieldName         = new String[1];
    byte[] zaraType      = new byte[20];
    byte[] zaraOption    = new byte[40];
    byte[] zaraDefault   = new byte[200];
    byte[] zaraMaxChars  = new byte[10];
    byte[] zaraValues    = new byte[200];
    byte[] zaraTagEntry = new byte[200]; // plenty
    byte[] token = new byte[200];
    byte[] value = new byte[200];
    int[] zaraTagEntryPtr = new int[1];
    byte[] tagName = new byte[10];
    int[] iPtr = new int[1];

    byte ch;
    String saveStr="";

    iPtr[0]=0;
    int i;
    while(sourceBuf[iPtr[0]] != -1)
    {
      ch = sourceBuf[iPtr[0]];
      if(ch != '<')
        ++iPtr[0];
      else // is an '<'
      {
        for(i=0;i<8;++i)
          tagName[i] = sourceBuf[i + iPtr[0]];
        tagName[i] = '\000';

        if(generalUtils.matchIgnoreCase(tagName, 0, "<ZaraTag"))
        {
          processZaraTag(sourceBuf, zaraTableAndFieldName, zaraFieldName, zaraType, zaraOption, zaraMaxChars, zaraValues, zaraDefault, token, value, zaraTagEntry, zaraTagEntryPtr, iPtr);

          if(generalUtils.matchIgnoreCase(zaraType, 0, "prepend"))
           ; // nowt
          else
          if(generalUtils.matchIgnoreCase(zaraType, 0, "image"))
           ; // nowt
          else
          if(generalUtils.matchIgnoreCase(zaraType, 0, "linex"))
            ; // nowt
          else
          if(generalUtils.matchIgnoreCase(zaraType, 0, "hidden"))
            ;
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "image"))
            ; // ignore
          else
          if(   generalUtils.matchIgnoreCase(zaraOption, 0, "check")
             || generalUtils.startsWithIgnoreCase(zaraOption, 0, "check") && dispOrEdit == 'E')
          {
            saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\";" + "if(" + htmlPage + "." + zaraFieldName[0] + ".checked)saveStr+=\"" + (char)zaraValues[1] + "\001\";else saveStr+=\"" + (char)zaraValues[0] + "\001\";\n";
          }
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "dropdownlist"))
          {
            saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\"+" + htmlPage + "." + zaraFieldName[0] + ".options[" + htmlPage + "." + zaraFieldName[0] + ".selectedIndex].value+\"\001\";\n";
          }
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "combo"))
          {
            saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\"+" + htmlPage + "." + zaraFieldName[0] + ".value+\"\001\";\n";
          }
          else
          {
            if(generalUtils.startsWithIgnoreCase(zaraOption, 0, "radio"))
            {
              saveStr += "if(" + htmlPage + "." + zaraFieldName[0] + "[" + (char)zaraOption[5] + "].checked)saveStr+=\"" + zaraTableAndFieldName[0] + "=" + (char)zaraValues[0] + "\001\";\n";
            }
            else
            {
              saveStr += "saveStr+=\"" + zaraTableAndFieldName[0] + "=\"+" + htmlPage + "." + zaraFieldName[0] + ".value+\"\001\";\n";
            }
          }
          
          ++iPtr[0];
        }
        else ++iPtr[0];
      }
    }

    return saveStr;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void processZaraTag(byte[] sourceBuf, String[] zaraTableAndFieldName, String[] zaraFieldName, byte[] zaraType, byte[] zaraOption, byte[] zaraMaxChars,
                              byte[] zaraValues, byte[] zaraDefault, byte[] token, byte[] value, byte[] zaraTagEntry, int[] zaraTagEntryPtr, int[] iPtr)
                              throws Exception
  {
    zaraType[0] = zaraOption[0] = zaraMaxChars[0] = zaraValues[0] = zaraDefault[0] = '\000';

    iPtr[0] += 8;

    int i, len, tokenPtr;

    generalUtils.catAsBytes("<ZaraTag", 0, zaraTagEntry, true);
    zaraTagEntryPtr[0] = 8;
    while(sourceBuf[iPtr[0]] != '>' && sourceBuf[iPtr[0]] != -1)
    {
      while(sourceBuf[iPtr[0]] == ' ' && sourceBuf[iPtr[0]] != -1)
      {
        zaraTagEntry[zaraTagEntryPtr[0]++] = ' ';
        ++iPtr[0];
      }

      tokenPtr=0;
      while(sourceBuf[iPtr[0]] != '=' && sourceBuf[iPtr[0]] != '>' && sourceBuf[iPtr[0]] != -1)
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

      int valuePtr = 0;

      if(sourceBuf[iPtr[0]] == '"') // is in quotes
      {
        value[valuePtr++] = '"';
        ++iPtr[0];
        while(sourceBuf[iPtr[0]] != '"' && sourceBuf[iPtr[0]] != -1)
        {
          value[valuePtr++] = sourceBuf[iPtr[0]++];
        }
        value[valuePtr++] = '"';
      }
      else
      {
        boolean quit=false;
        while(! quit)
        {
          if(sourceBuf[iPtr[0]] == ' ')
          {
            if(sourceBuf[iPtr[0]-1] == '=')
              value[valuePtr++] = sourceBuf[iPtr[0]++];
            else quit = true;
          }
          else
          if(sourceBuf[iPtr[0]] == '>' || sourceBuf[iPtr[0]] == -1)
            quit = true;
          else value[valuePtr++] = sourceBuf[iPtr[0]++];
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
        i=0;
        zaraTableAndFieldName[0] = "";
        while(value[i] != '.') // first part upto '.'
        {
          zaraTableAndFieldName[0] += (char)value[i];
          ++i;
        }
        zaraTableAndFieldName[0] += '.';
        ++i;

        zaraFieldName[0] = "";
        while(i < valuePtr)
        {
          zaraFieldName [0]        += (char)value[i];
          zaraTableAndFieldName[0] += (char)value[i];
          ++i;
        }
      }
      else
      if(generalUtils.matchIgnoreCase(token, 0, "Default"))
      {
        len = generalUtils.lengthBytes(value, 0);
        generalUtils.bytesToBytes(zaraTagEntry, 0, value, 0);
        zaraTagEntryPtr[0] += len;
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public String buildClearString(byte[] sourceBuf, String htmlPage) throws Exception
  {
    String[] zaraTableAndFieldName = new String[1];
    String[] zaraFieldName         = new String[1];
    byte[] zaraType      = new byte[20];
    byte[] zaraOption    = new byte[40];
    byte[] zaraDefault   = new byte[200];
    byte[] zaraMaxChars  = new byte[10];
    byte[] zaraValues    = new byte[200];
    byte[] zaraTagEntry = new byte[200]; // plenty
    byte[] token = new byte[200];
    byte[] value = new byte[200];
    int[] zaraTagEntryPtr = new int[1];
    byte[] tagName = new byte[10];
    int[] iPtr = new int[1];

    byte ch;
    String clearStr="";

    iPtr[0] = 0;
    int i;
    while(sourceBuf[iPtr[0]] != -1)
    {
      ch = sourceBuf[iPtr[0]];
      if(ch != '<')
        ++iPtr[0];
      else // is an '<'
      {
        for(i=0;i<8;++i)
          tagName[i] = sourceBuf[i + iPtr[0]];
        tagName[i] = '\000';

        if(generalUtils.matchIgnoreCase(tagName, 0, "<ZaraTag"))
        {
          processZaraTag(sourceBuf, zaraTableAndFieldName, zaraFieldName, zaraType, zaraOption, zaraMaxChars, zaraValues, zaraDefault, token, value,
                         zaraTagEntry, zaraTagEntryPtr, iPtr);
          if(generalUtils.matchIgnoreCase(zaraType, 0, "hidden"))
            ; // nowt
          else
          if(generalUtils.matchIgnoreCase(zaraType, 0, "linex"))
            ; // nowt
          else                                     
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "check"))
          {
            clearStr += htmlPage + "." + zaraFieldName[0] + ".checked=false;\n";
          }
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "image"))
            ; // nowt
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "dropdownlist"))
            ;
          else
          if(generalUtils.matchIgnoreCase(zaraOption, 0, "combo"))
          {
            clearStr += htmlPage + "." + zaraFieldName[0] + ".value=\"\";\n";
          }
          else
          {
            if(generalUtils.startsWithIgnoreCase(zaraOption, 0, "radio"))
            {
              if(zaraOption[5] == '0')
              {
                clearStr += htmlPage + "." + zaraFieldName[0] + "[0].checked=true;\n";
              }
              else
              {
                clearStr += htmlPage + "." + zaraFieldName[0] + "[" + (char)zaraOption[5]
                         + "].checked=false;\n";
              }
            }
            else
            {
              clearStr += htmlPage + "." + zaraFieldName[0] + ".value=\"\";\n";
            }
          }
          ++iPtr[0];
        }
        else ++iPtr[0];
      }
    }

    return clearStr;
  }

}
