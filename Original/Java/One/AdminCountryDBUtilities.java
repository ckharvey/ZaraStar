// =================================================================================================================================
// System: ZaraStar AdminEngine: Country DB Utilities
// Module: AdminCountryDBUtilities.java
// Author: C.K.Harvey
// Copyright (c) 1998-2006 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
//
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import java.sql.*;

public class AdminCountryDBUtilities
{
  GeneralUtils generalUtils = new GeneralUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();

  int count;
  
  // -------------------------------------------------------------------------------------------------------------------------------
  public void primeCountries(String dnm, String localDefnsDir, String defnsDir)
  {
    Connection con = null;
    Statement stmt = null;
    
    try
    {
      String uName    = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_ofsa?user=" + uName + "&password=" + passWord);

      count = 10;
      primeACountry(con, stmt, "Afghanistan");
      primeACountry(con, stmt, "Aland Islands");
      primeACountry(con, stmt, "Albania");
      primeACountry(con, stmt, "Algeria");
      primeACountry(con, stmt, "American Samoa");
      primeACountry(con, stmt, "Andorra");
      primeACountry(con, stmt, "Angola");
      primeACountry(con, stmt, "Anguilla");
      primeACountry(con, stmt, "Antarctica");
      primeACountry(con, stmt, "Antigua and Barbuda");
      primeACountry(con, stmt, "Argentina");
      primeACountry(con, stmt, "Armenia");
      primeACountry(con, stmt, "Aruba");
      primeACountry(con, stmt, "Australia");
      primeACountry(con, stmt, "Austria");
      primeACountry(con, stmt, "Azerbaijan");
      primeACountry(con, stmt, "Bahamas");
      primeACountry(con, stmt, "Bahrain");
      primeACountry(con, stmt, "Bangladesh");
      primeACountry(con, stmt, "Barbados");
      primeACountry(con, stmt, "Belarus");
      primeACountry(con, stmt, "Belgium");
      primeACountry(con, stmt, "Belize");
      primeACountry(con, stmt, "Benin");
      primeACountry(con, stmt, "Bermuda");
      primeACountry(con, stmt, "Bhutan");
      primeACountry(con, stmt, "Bolivia");
      primeACountry(con, stmt, "Bosnia and Herzegovina");
      primeACountry(con, stmt, "Botswana");
      primeACountry(con, stmt, "Bouvet Island");
      primeACountry(con, stmt, "Brazil");
      primeACountry(con, stmt, "British Indian Ocean Territory");
      primeACountry(con, stmt, "Brunei Darussalam");
      primeACountry(con, stmt, "Bulgaria");
      primeACountry(con, stmt, "Burkina Faso");
      primeACountry(con, stmt, "Burundi");
      primeACountry(con, stmt, "Cambodia");
      primeACountry(con, stmt, "Cameroon");
      primeACountry(con, stmt, "Canada");
      primeACountry(con, stmt, "Cape Verde");
      primeACountry(con, stmt, "Cayman Islands");
      primeACountry(con, stmt, "Central African Republic");
      primeACountry(con, stmt, "Chad");
      primeACountry(con, stmt, "Chile");
      primeACountry(con, stmt, "China");
      primeACountry(con, stmt, "Christmas Island");
      primeACountry(con, stmt, "Cocos (Keeling) Islands");
      primeACountry(con, stmt, "Colombia");
      primeACountry(con, stmt, "Comoros");
      primeACountry(con, stmt, "Congo");
      primeACountry(con, stmt, "Congo, The Democratic Republic of the");
      primeACountry(con, stmt, "Cook Islands");
      primeACountry(con, stmt, "Costa Rica");
      primeACountry(con, stmt, "Cote d\\\'Ivoire");
      primeACountry(con, stmt, "Croatia");
      primeACountry(con, stmt, "Cuba");
      primeACountry(con, stmt, "Cyprus");
      primeACountry(con, stmt, "Czech Republic");
      primeACountry(con, stmt, "Denmark");
      primeACountry(con, stmt, "Djibouti");
      primeACountry(con, stmt, "Dominica");
      primeACountry(con, stmt, "Dominican Republic");
      primeACountry(con, stmt, "Ecuador");
      primeACountry(con, stmt, "Egypt");
      primeACountry(con, stmt, "El Salvador");
      primeACountry(con, stmt, "Equatorial Guinea");
      primeACountry(con, stmt, "Eritrea");
      primeACountry(con, stmt, "Estonia");
      primeACountry(con, stmt, "Ethiopia");
      primeACountry(con, stmt, "Falkland Islands (Malvinas)");
      primeACountry(con, stmt, "Faroe Islands");
      primeACountry(con, stmt, "Fiji");
      primeACountry(con, stmt, "Finland");
      primeACountry(con, stmt, "France");
      primeACountry(con, stmt, "French Guiana");
      primeACountry(con, stmt, "French Polynesia");
      primeACountry(con, stmt, "French Southern Territories");
      primeACountry(con, stmt, "Gabon");
      primeACountry(con, stmt, "Gambia");
      primeACountry(con, stmt, "Georgia");
      primeACountry(con, stmt, "Germany");
      primeACountry(con, stmt, "Ghana");
      primeACountry(con, stmt, "Gibraltar");
      primeACountry(con, stmt, "Greece");
      primeACountry(con, stmt, "Greenland");
      primeACountry(con, stmt, "Grenada");
      primeACountry(con, stmt, "Guadeloupe");
      primeACountry(con, stmt, "Guam");
      primeACountry(con, stmt, "Guatemala");
      primeACountry(con, stmt, "Guernsey");
      primeACountry(con, stmt, "Guinea");
      primeACountry(con, stmt, "Guinea-Bissau");
      primeACountry(con, stmt, "Guyana");
      primeACountry(con, stmt, "Haiti");
      primeACountry(con, stmt, "Heard Island and McDonald Islands");
      primeACountry(con, stmt, "Honduras");
      primeACountry(con, stmt, "Hong Kong");
      primeACountry(con, stmt, "Hungary");
      primeACountry(con, stmt, "Iceland");
      primeACountry(con, stmt, "India");
      primeACountry(con, stmt, "Indonesia");
      primeACountry(con, stmt, "Iran (Islamic Republic of)");
      primeACountry(con, stmt, "Iraq");
      primeACountry(con, stmt, "Ireland");
      primeACountry(con, stmt, "Isle of Man");
      primeACountry(con, stmt, "Israel");
      primeACountry(con, stmt, "Italy");
      primeACountry(con, stmt, "Jamaica");
      primeACountry(con, stmt, "Japan");
      primeACountry(con, stmt, "Jersey");
      primeACountry(con, stmt, "Jordan");
      primeACountry(con, stmt, "Kazakhstan");
      primeACountry(con, stmt, "Kenya");
      primeACountry(con, stmt, "Kiribati");
      primeACountry(con, stmt, "Korea, Democratic People\\\'s Republic of");
      primeACountry(con, stmt, "Korea, Republic of");
      primeACountry(con, stmt, "Kuwait");
      primeACountry(con, stmt, "Kyrgyzstan");
      primeACountry(con, stmt, "Lao People\\\'s Democratic Republic");
      primeACountry(con, stmt, "Latvia");
      primeACountry(con, stmt, "Lebanon");
      primeACountry(con, stmt, "Lesotho");
      primeACountry(con, stmt, "Liberia");
      primeACountry(con, stmt, "Libyan Arab Jamahiriya");
      primeACountry(con, stmt, "Liechtenstein");
      primeACountry(con, stmt, "Lithuania");
      primeACountry(con, stmt, "Luxembourg");
      primeACountry(con, stmt, "Macao");
      primeACountry(con, stmt, "Macedonia, The Former Yugoslav Republic of");
      primeACountry(con, stmt, "Madagascar");
      primeACountry(con, stmt, "Malawi");
      primeACountry(con, stmt, "Malaysia");
      primeACountry(con, stmt, "Maldives");
      primeACountry(con, stmt, "Mali");
      primeACountry(con, stmt, "Malta");
      primeACountry(con, stmt, "Marshall Islands");
      primeACountry(con, stmt, "Martinique");
      primeACountry(con, stmt, "Mauritania");
      primeACountry(con, stmt, "Mauritius");
      primeACountry(con, stmt, "Mayotte");
      primeACountry(con, stmt, "Mexico");
      primeACountry(con, stmt, "Micronesia, Federated States of");
      primeACountry(con, stmt, "Moldova, Republic of");
      primeACountry(con, stmt, "Monaco");
      primeACountry(con, stmt, "Mongolia");
      primeACountry(con, stmt, "Montenegro");
      primeACountry(con, stmt, "Montserrat");
      primeACountry(con, stmt, "Morocco");
      primeACountry(con, stmt, "Mozambique");
      primeACountry(con, stmt, "Myanmar");
      primeACountry(con, stmt, "Namibia");
      primeACountry(con, stmt, "Nauru");
      primeACountry(con, stmt, "Nepal");
      primeACountry(con, stmt, "Netherlands");
      primeACountry(con, stmt, "Netherlands Antilles");
      primeACountry(con, stmt, "New Caledonia");
      primeACountry(con, stmt, "New Zealand");
      primeACountry(con, stmt, "Nicaragua");
      primeACountry(con, stmt, "Niger");
      primeACountry(con, stmt, "Nigeria");
      primeACountry(con, stmt, "Niue");
      primeACountry(con, stmt, "Norfolk Island");
      primeACountry(con, stmt, "Northern Mariana Islands");
      primeACountry(con, stmt, "Norway");
      primeACountry(con, stmt, "Oman");
      primeACountry(con, stmt, "Pakistan");
      primeACountry(con, stmt, "Palau");
      primeACountry(con, stmt, "Palestiian Territory, Occupied");
      primeACountry(con, stmt, "Panama");
      primeACountry(con, stmt, "Papua New Guinea");
      primeACountry(con, stmt, "Paraguay");
      primeACountry(con, stmt, "Peru");
      primeACountry(con, stmt, "Philippines");
      primeACountry(con, stmt, "Pitcairn");
      primeACountry(con, stmt, "Poland");
      primeACountry(con, stmt, "Portugal");
      primeACountry(con, stmt, "Puerto Rico");
      primeACountry(con, stmt, "Qatar");
      primeACountry(con, stmt, "Reunion");
      primeACountry(con, stmt, "Romania");
      primeACountry(con, stmt, "Russian Federation");
      primeACountry(con, stmt, "Rwanda");
      primeACountry(con, stmt, "Saint Helena");
      primeACountry(con, stmt, "Saint Kitts and Nevis");
      primeACountry(con, stmt, "Saint Lucia");
      primeACountry(con, stmt, "Saint Pierre and Miquelon");
      primeACountry(con, stmt, "Saint Vincent and the Grenadines");
      primeACountry(con, stmt, "Samoa");
      primeACountry(con, stmt, "San Marino");
      primeACountry(con, stmt, "Sao Tome and Principe");
      primeACountry(con, stmt, "Saudi Arabia");
      primeACountry(con, stmt, "Senegal");
      primeACountry(con, stmt, "Serbia");
      primeACountry(con, stmt, "Seychelles");
      primeACountry(con, stmt, "Sierra Leone");
      primeACountry(con, stmt, "Singapore");
      primeACountry(con, stmt, "Slovakia");
      primeACountry(con, stmt, "Slovenia");
      primeACountry(con, stmt, "Solomon Islands");
      primeACountry(con, stmt, "Somalia");
      primeACountry(con, stmt, "South Africa");
      primeACountry(con, stmt, "South Georgia and the South Sandwich Islands");
      primeACountry(con, stmt, "Spain");
      primeACountry(con, stmt, "Sri Lanka");
      primeACountry(con, stmt, "Sudan");
      primeACountry(con, stmt, "Suriname");
      primeACountry(con, stmt, "Svalbard and Jan Mayen");
      primeACountry(con, stmt, "Swaziland");
      primeACountry(con, stmt, "Sweden");
      primeACountry(con, stmt, "Switzerland");
      primeACountry(con, stmt, "Syrian Arab Republic");
      primeACountry(con, stmt, "Taiwan, Province of China");
      primeACountry(con, stmt, "Tajikistan");
      primeACountry(con, stmt, "Tanzania, United Republic of");
      primeACountry(con, stmt, "Thailand");
      primeACountry(con, stmt, "Timor-Leste");
      primeACountry(con, stmt, "Togo");
      primeACountry(con, stmt, "Tokelau");
      primeACountry(con, stmt, "Tonga");
      primeACountry(con, stmt, "Trinidad and Tobago");
      primeACountry(con, stmt, "Tunisia");
      primeACountry(con, stmt, "Turkey");
      primeACountry(con, stmt, "Turkmenistan");
      primeACountry(con, stmt, "Turks and Caicos Islands");
      primeACountry(con, stmt, "Tuvalu");
      primeACountry(con, stmt, "Uganda");
      primeACountry(con, stmt, "Ukraine");
      primeACountry(con, stmt, "United Arab Emirates");
      primeACountry(con, stmt, "United Kingdom");
      primeACountry(con, stmt, "United States");
      primeACountry(con, stmt, "United States Minor Outlying Islands");
      primeACountry(con, stmt, "Uruguay");
      primeACountry(con, stmt, "Uzbekistan");
      primeACountry(con, stmt, "Vatican City State");
      primeACountry(con, stmt, "Vanuatu");
      primeACountry(con, stmt, "Venezuela");
      primeACountry(con, stmt, "Viet Nam");
      primeACountry(con, stmt, "Virgin Islands, British");
      primeACountry(con, stmt, "Virgin Islands, U.S.");
      primeACountry(con, stmt, "Wallis and Futuna");
      primeACountry(con, stmt, "Western Sahara");
      primeACountry(con, stmt, "Yemen");
      primeACountry(con, stmt, "Zambia");
      primeACountry(con, stmt, "Zimbabwe");

      if(con  != null) con.close();
    }
    catch(Exception e)
    {
      System.out.println("AdminCountryDBUtilities: " + e);
      try
      {
        if(stmt != null) stmt.close();
        if(con  != null) con.close();
      }
      catch(Exception e2) { }
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------
  private void primeACountry(Connection con, Statement stmt, String name) throws Exception
  {
    stmt = con.createStatement();
    stmt.executeUpdate("INSERT INTO country ( Name, Position ) VALUES ('" + name + "','" + count + "')");
    if(stmt != null) stmt.close();
    
    count += 10;
  }
  
}
