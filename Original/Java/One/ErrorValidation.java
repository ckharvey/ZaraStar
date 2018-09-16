// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Validation Error Page
// Module: ErrorValidation.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class ErrorValidation
{
  byte[] tfName = new byte[50];
  byte[] tableAndFieldName = new byte[50];

  GeneralUtils generalUtils = new GeneralUtils();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
     
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean validatePageReturningStr(Connection con, Statement stmt, ResultSet rs, String dateFld, String[] retStr, String fieldNames, String fieldTypes, String fieldStyles, String fileName, String pageTitle, String dnm, String unm,
                                          byte[] recData, int recDataLen, String imagesDir, String localDefnsDir, String defnsDir)
  {
    try
    {
      byte[] value     = new byte[5000];
      byte[] fieldName = new byte[50];
    
      if(dateFld.length() > 0)
      {
        generalUtils.strToBytes(fieldName, dateFld);

        String date;
        if(searchDataString(recData, recDataLen, fileName, fieldName, value) != -1)
        {
          date = generalUtils.stringFromBytes(value, 0L);
        
          date = generalUtils.convertDateToSQLFormat(date);
   
          if(! serverUtils.passLockCheck(con, stmt, rs, fileName, date, unm))
          {
            retStr[0] = "";

            setBody(retStr, date);
 
            setFoot2(retStr, imagesDir);

            System.out.println("ErrorValidation: CreationNotify: " + pageTitle + ": " + dnm + ": " + unm + ": " + date);
      
            return false;
          }
        }
      }
    
      retStr[0] = "";

      setHead2(retStr);

      boolean atLeastOneError = false;
      boolean entryExists, noBlankDate;
  
      int numFields = fieldTypes.length();      
      for(int x=0;x<numFields;++x)
      {
        getFieldName(fieldNames, x, fieldName);

        if(searchDataString(recData, recDataLen, fileName, fieldName, value) != -1)
          entryExists = true;
        else entryExists = false;
        
        if(entryExists)
        {
          if(fieldStyles.charAt(x) == 'M')
          {
            if(value[0] == '\000')
            {
              setLine(retStr, 'M', generalUtils.stringFromBytes(fieldName, 0L), "");
              atLeastOneError = true;
            }
          }

          switch(fieldTypes.charAt(x)) // validate non-alpha flds
          {
            case 'D' : if(fieldStyles.charAt(x) == 'M')
                         noBlankDate = true;
                       else noBlankDate = false; 
 
                       if(! generalUtils.validateDate(noBlankDate, value, localDefnsDir, defnsDir))
                       {
                         setLine(retStr, 'D', generalUtils.stringFromBytes(fieldName, 0L), generalUtils.stringFromBytes(value, 0L));
                         atLeastOneError = true;
                       }
                       break;
            case 'F' : if(! generalUtils.validNumeric(value, -1))
                       {
                         setLine(retStr, 'F', generalUtils.stringFromBytes(fieldName, 0L), generalUtils.stringFromBytes(value, 0L));
                         atLeastOneError = true;
                       }
                       break;
            case 'N' : if(! generalUtils.validNumeric(value, 0))
                       {
                         setLine(retStr, 'N', generalUtils.stringFromBytes(fieldName, 0L), generalUtils.stringFromBytes(value, 0L));
                         atLeastOneError = true;
                       }
                       break;
            case 'I' : if(! generalUtils.validNumeric(value, 0))
                       {
                         setLine(retStr, 'I', generalUtils.stringFromBytes(fieldName, 0L), generalUtils.stringFromBytes(value, 0L));
                         atLeastOneError = true;
                       }
                       break;
          }
        }
      }

      setFoot2(retStr, imagesDir);

      if(atLeastOneError)
      {
        System.out.println("ErrorValidation: ValidationNotify: " + pageTitle + ": " + dnm + ": " + unm);
        return false;
      }

      return true;
    }
    catch(Exception ex)
    {
      System.out.println("ErrorValidation: Cannot Perform Validation: (" + ex + ") " + dnm + " " + unm + " " + pageTitle);
    }

    return true;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getFieldName(String fieldNames, int fieldReqd, byte[] fieldName) throws Exception
  {
    int x=0, y, fieldCount=0;
    int len = fieldNames.length();

    while(fieldCount < fieldReqd)
    {  
      while(x < len && fieldNames.charAt(x) != ',')
        ++x;
      ++x;
      while(x < len && fieldNames.charAt(x) == ' ')
        ++x;
      ++fieldCount;
    }
      
    if(x >= len) // just-in-case
      fieldName[0] = '\000';
    else
    {
      y=0;
      while(x < len && fieldNames.charAt(x) != ',')
        fieldName[y++] = (byte)fieldNames.charAt(x++);
      fieldName[y] = '\000';
    }
  }  

  // ------------------------------------------------------------------------------------------------------------------------------------------------------
  // format of data: "do.docode=12345/DO\001 ..."
  // returns fieldNum within data string (origin-0); -1 if not found
  public int searchDataString(byte[] data, int lenData, String tableName, byte[] fieldName, byte[] value)
  {
    int x=0;

    int len = tableName.length();
    while(x < len)
    {
      tableAndFieldName[x] = (byte)tableName.charAt(x);
      ++x;
    }
    tableAndFieldName[x++] = '.';

    int y=0;
    len = generalUtils.lengthBytes(fieldName, 0);
    while(y < len)
      tableAndFieldName[x++] = fieldName[y++];
    tableAndFieldName[x] = '\000';

    int ptr=0;
    int fieldNum=0;
    while(ptr < lenData)
    {
      x=0;
      while(data[ptr] != '\000' && data[ptr] != '=')
        tfName[x++] = data[ptr++];
      tfName[x] = '\000';

      if(generalUtils.matchIgnoreCase(tfName, 0, tableAndFieldName, 0))
      {
        ++ptr; // '='
        int valuePtr=0;
        while(data[ptr] != '\000' && data[ptr] != '\001')
          value[valuePtr++] = data[ptr++];
        value[valuePtr] = '\000';
        return fieldNum;
      }
      
      //else not the reqd table/field entry
      ++ptr; // '='
      while(data[ptr] != '\000' && data[ptr] != '\001') // e o data entry
        ++ptr;
      if(data[ptr] == '\001')
        ++ptr;
      
      ++fieldNum;
    }
    
    return -1; // data not found
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead2(String[] retStr) throws Exception
  {
    retStr[0] += "<table border=0 cellspacing=0 cellpadding=0 bgcolor=\"#FFFFFF\" width=100%>";
    retStr[0] += "<tr><td>&nbsp;</td></tr>";
    retStr[0] += "<tr><td><font face=\"Arial, Helvetica\" color=\"#FF0000\" size=3><b>&nbsp;Data Validation Error</td></tr>";
    retStr[0] += "<tr><td>&nbsp;</td></tr>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setBody(String[] retStr, String date)
  {
    retStr[0] += "<table border=0 cellspacing=0 cellpadding=0 bgcolor=\"#FFFFFF\" width=100%>";
    retStr[0] += "<tr><td>&nbsp;</td></tr>";
    retStr[0] += "<tr><td><font face=\"Arial, Helvetica\" color=\"#FF0000\" size=3><b>&nbsp;Data Update Error</td></tr>";
    retStr[0] += "<tr><td>&nbsp;</td></tr>";
    retStr[0] += "<tr><td><font face=\"Arial, Helvetica\" color=\"#FF0000\" size=2><b>&nbsp;Cannot save record into locked period: &nbsp;&nbsp;";
    retStr[0] += "<font color=\"#FF0000\"><b>" + date + "</td></tr>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setFoot2(String[] retStr, String imagesDir)
  {
    retStr[0] += "<tr><td>&nbsp;</td></tr>";
    retStr[0] += "<tr><td><img src=\"" + imagesDir + "blm2.gif\" width=100% height=3></td></tr></table>";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setLine(String[] retStr, char type, String fldName, String value) throws Exception
  {
    if(value.equalsIgnoreCase("null"))
      value = "none specified";

    switch(type)
    {
      case 'D' : // date
                 retStr[0] += "<tr><td><font face=\"Arial, Helvetica\" color=\"#FF0000\" size=2><b>&nbsp;Invalid date: &nbsp;&nbsp;";
                 retStr[0] += "<font color=\"#0000FF\"><b>" + fldName;
                 retStr[0] += "&nbsp;&nbsp;&nbsp;<font color=\"#000000\"><b>" + value + "</td></tr>";
                 break;
      case 'F' : // float
      case 'I' : // integer
      case 'N' : // numeric
                 retStr[0] += "<tr><td><font face=\"Arial, Helvetica\" color=\"#FF0000\" size=2><b>&nbsp;Invalid number: &nbsp;&nbsp;";
                 retStr[0] += "<font color=\"#0000FF\"><b>" + fldName;
                 retStr[0] += "&nbsp;&nbsp;&nbsp;<font color=\"#000000\"><b>" + value + "</td></tr>";
                 break;
      case 'M' : // mandatory
                 retStr[0] += "<tr><td><font face=\"Arial, Helvetica\" color=\"#FF0000\" size=2><b>&nbsp;Missing mandatory entry: &nbsp;&nbsp;";
                 retStr[0] += "<font color=\"#0000FF\"><b>" + fldName + "</td></tr>";
                 break;
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public void formatErrMsg(String msg, String[] retStr, String imagesDir) throws Exception
  {
    retStr[0] = "";
    setHead2(retStr);

    retStr[0] += "<tr><td><font face=\"Arial, Helvetica\" color=\"#FF0000\" size=2><b>&nbsp;" + msg + "</td></tr>";

    setFoot2(retStr, imagesDir);
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  public boolean existsGivenCode(String code, String tableName, String codeName, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    if(code.length() == 0) // just-in-case
      return false;
    
    if(code.indexOf("'") != -1)
      return false;

    code = code.toUpperCase();

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    String uName    = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

    stmt = con.createStatement();
    
    int numRecs = 0;
    
    rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM " + tableName + " WHERE " + codeName + " = '" + code + "'");
    if(rs.next()) // just-in-case      
      numRecs = rs.getInt("rowcount") ;

    if(rs != null) rs.close();
    if(stmt != null) stmt.close();
    if(con != null) con.close();
    
    if(numRecs == 1)
      return true;
    
    return false;
  }
    
}
