// =======================================================================================================================================================================================================
// System: ZaraStar Utils: General Utilities
// File:   GeneralUtils.cs
// Author: C.K.Harvey
// Copyright (c) 1998-2018 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

using System;
using System.IO;
using System.Text;

namespace zarastar
{
    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    class GeneralUtils
    {
        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void Dfs(char[] ipBuf, short fld, char[] opBuf)
        {
            try
            {
                short x = 0;
                short y = 0;
                while (x < fld)
                {
                    while (ipBuf[y] != '\0')
                        ++y;
                    ++y;
                    ++x;
                }
                x = 0;
                while (ipBuf[y] != '\0')
                    opBuf[x++] = ipBuf[y++];
                opBuf[x] = '\0';
                return;
            }
            catch (Exception) { opBuf[0] = '\0'; }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DfsGivenSeparator(bool onlyValue, char separator, string ipStr, short fld)
        {
            char[] opBuf = new char[300]; // plenty

            DfsGivenSeparator(onlyValue, separator, ipStr, fld, opBuf);

            return StringFromChars(opBuf);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DfsGivenSeparator(bool onlyValue, char separator, char[] ipBuf, short fld)
        {
            char[] opBuf = new char[300]; // plenty

            DfsGivenSeparator(onlyValue, separator, ipBuf, fld, opBuf);

            return StringFromChars(opBuf);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void DfsGivenSeparator(bool onlyValue, char separator, string ipStr, short fld, char[] opBuf)
        {
            int len = ipStr.Length;
            char[] ipBuf = new char[len + 1];

            StrToChars(ipBuf, ipStr, 0, len);

            DfsGivenSeparator(onlyValue, separator, ipBuf, fld, opBuf);
        }
        public void DfsGivenSeparator(bool onlyValue, char separator, char[] ipBuf, short fld, char[] opBuf)
        {
            try
            {
                if (separator == '\0')
                {
                    Dfs(ipBuf, fld, opBuf);
                }
                else
                if (separator.Equals(@"\1"))
                {
                    short x = 0;
                    short y = 0;
                    while (ipBuf[y] != '\0' && x < fld)
                    {
                        while (!ipBuf[y].Equals(@"\1"))
                            ++y;
                        ++y;
                        ++x;
                    }

                    if (ipBuf[y] == '\0')
                    {
                        opBuf[0] = '\0';
                        return;
                    }

                    if (!onlyValue)
                    {
                        x = 0;
                        while (!ipBuf[y].Equals(@"\1"))
                            opBuf[x++] = ipBuf[y++];
                        opBuf[x] = '\0';
                    }
                    else
                    {
                        x = 0;
                        while (ipBuf[y] != '=' && !ipBuf[y].Equals(@"\1")) // just-in-case
                            ++y;
                        if (!ipBuf[y].Equals(@"\1"))
                        {
                            ++y;
                            while (!ipBuf[y].Equals(@"\1"))
                                opBuf[x++] = ipBuf[y++];
                        }
                        opBuf[x] = '\0';
                    }

                    return;
                }
                else
                if (separator == ' ')
                {
                    short x = 0;
                    short y = 0;
                    while (true)
                    {
                        if (x == fld)
                        {
                            if (!onlyValue)
                            {
                                x = 0;
                                while (ipBuf[y] != '\0' && ipBuf[y] != '"')
                                    opBuf[x++] = ipBuf[y++];
                                if (ipBuf[y] != '\0')
                                {
                                    opBuf[x++] = '"';
                                    ++y;
                                    while (ipBuf[y] != '\0' && ipBuf[y] != '"')
                                        opBuf[x++] = ipBuf[y++];
                                    opBuf[x++] = '"';
                                }
                                opBuf[x] = '\0';
                            }
                            else
                            {
                                while (ipBuf[y] != '"') // opening quote
                                    ++y;
                                ++y;

                                x = 0;
                                while (ipBuf[y] != '"') // closing quote
                                    opBuf[x++] = ipBuf[y++];
                                opBuf[x] = '\0';
                            }
                            return;
                        }

                        while (ipBuf[y] != '\0' && ipBuf[y] != '"') // opening quote
                            ++y;
                        if (ipBuf[y] != '\0')
                        {
                            ++y;
                            while (ipBuf[y] != '"') // closing quote
                                ++y;
                            ++y;
                        }
                        ++y;
                        ++x;
                    }
                }
            }
            catch (Exception) { opBuf[0] = '\0'; }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void RepDoubleGivenSeparator(char dps, char separator, char[] ipBuf, int bufLen, short fld, double newValue)
        {
            string newStr = DoubleToStr(dps, newValue);
            RepAlphaGivenSeparator(separator, ipBuf, bufLen, fld, newStr);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // works with separator of '\0' (whereupon a 'normal' RepAlpha is done)
        // and a separator of ' ' (that is, ipBuf[] = 'abc="123" def="123456789" ghi="789", and
        // the "123456789" could be replaced by newStr = "123"
        public void RepAlphaGivenSeparator(char separator, char[] ipBuf, int bufLen, short fld, string newStr)
        {
            try
            {
                if (separator == '\0')
                {
                    RepAlpha(ipBuf, bufLen, fld, newStr);
                    return;
                }
                else
                if (separator.Equals(@"\1"))
                {
                    int x = 0;
                    int y = 0;
                    while (true)
                    {
                        while (ipBuf[y] != '=' && !ipBuf[y].Equals(@"\1"))
                            ++y;
                        if (!ipBuf[y].Equals(@"\1"))
                            ++y;

                        if (x == fld)
                        {
                            int lenNewStr = newStr.Length;
                            int lenOldStr = 0;
                            int z = y;
                            while (!ipBuf[z].Equals(@"\1"))
                            {
                                ++lenOldStr;
                                ++z;
                            }
                            if (lenOldStr == lenNewStr)
                            {
                                for (z = 0; z < lenNewStr; ++z)
                                {
                                    ipBuf[y++] = (char)newStr[z];
                                }
                                return;
                            }
                            else
                            if (lenOldStr < lenNewStr) // newStr needs more room, so, shunt-up by the difference
                            {
                                int diff = lenNewStr - lenOldStr;
                                for (z = bufLen - 1; z > diff && z > y; --z)
                                    ipBuf[z] = ipBuf[z - diff];
                                for (z = 0; z < lenNewStr; ++z)
                                    ipBuf[y++] = (char)newStr[z];
                                return;
                            }
                            else // lenOldStr > lenNewStr
                            {
                                // newStr needs less room, so, shunt-down by the difference
                                int diff = lenOldStr - lenNewStr;
                                for (z = y; z < bufLen - diff; ++z)
                                    ipBuf[z] = ipBuf[z + diff];
                                for (z = 0; z < lenNewStr; ++z)
                                    ipBuf[y++] = (char)newStr[z];
                                return;
                            }
                        }
                        ++x;

                        while (!ipBuf[y].Equals(@"\1"))
                            ++y;
                        ++y;
                    }
                }
                else
                if (separator == ' ')
                {
                    int x = 0;
                    int y = 0;
                    while (true)
                    {
                        while (ipBuf[y] != '"') // opening quote
                            ++y;
                        ++y;

                        if (x == fld)
                        {
                            int lenNewStr = newStr.Length;
                            int lenOldStr = 0;
                            int z = y;
                            while (ipBuf[z] != '"') // closing quote
                            {
                                ++lenOldStr;
                                ++z;
                            }
                            if (lenOldStr == lenNewStr)
                            {
                                for (z = 0; z < lenNewStr; ++z)
                                {
                                    ipBuf[y++] = (char)newStr[z];
                                }
                                return;
                            }
                            else
                            if (lenOldStr < lenNewStr) // newStr needs more room, so, shunt-up by the difference
                            {
                                int diff = lenNewStr - lenOldStr;
                                for (z = bufLen - 1; z > diff && z > y; --z)
                                    ipBuf[z] = ipBuf[z - diff];
                                for (z = 0; z < lenNewStr; ++z)
                                    ipBuf[y++] = (char)newStr[z];
                                return;
                            }
                            else // lenOldStr > lenNewStr
                            {
                                // newStr needs less room, so, shunt-down by the difference
                                int diff = lenOldStr - lenNewStr;
                                for (z = y; z < bufLen - diff; ++z)
                                    ipBuf[z] = ipBuf[z + diff];
                                for (z = 0; z < lenNewStr; ++z)
                                    ipBuf[y++] = (char)newStr[z];
                                return;
                            }
                        }
                        ++x;

                        while (ipBuf[y] != '"') // closing quote
                            ++y;
                        ++y;
                    }
                }
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // works with separator of '\0' (whereupon a 'normal' RepAlpha is done)
        // and a separator of ' ' (that is, ipBuf[] = 'abc="123" def="123456789" ghi="789", and
        // the "123456789" could be replaced by newB = "123"
        public void RepAlphaGivenSeparator(char separator, char[] ipBuf, int bufLen, short fld, char[] newB)
        {
            try
            {
                if (separator == '\0')
                {
                    RepAlpha(ipBuf, bufLen, fld, newB);
                    return;
                }
                else
                if (separator.Equals(@"\1"))
                {
                    int x = 0;
                    int y = 0;
                    while (true)
                    {
                        while (ipBuf[y] != '=' && !ipBuf[y].Equals(@"\1"))
                            ++y;
                        if (ipBuf[y] == '=')
                            ++y;

                        if (x == fld)
                        {
                            int lenNewB = Lengthchars(newB, 0);
                            int lenOldStr = 0;
                            int z = y;
                            while (!ipBuf[z].Equals(@"\1"))
                            {
                                ++lenOldStr;
                                ++z;
                            }

                            if (lenOldStr == lenNewB)
                            {
                                for (z = 0; z < lenNewB; ++z)
                                    ipBuf[y++] = newB[z];
                                return;
                            }
                            else
                            if (lenOldStr < lenNewB) // newB needs more room, so, shunt-up by the difference
                            {
                                int diff = lenNewB - lenOldStr;
                                for (z = bufLen - 1; z > diff && z > y; --z)
                                    ipBuf[z] = ipBuf[z - diff];
                                for (z = 0; z < lenNewB; ++z)
                                    ipBuf[y++] = newB[z];
                                return;
                            }
                            else // lenOldStr > lenNewB
                            {
                                // newB needs less room, so, shunt-down by the difference
                                int diff = lenOldStr - lenNewB;
                                for (z = y; z < bufLen - diff; ++z)
                                    ipBuf[z] = ipBuf[z + diff];
                                for (z = 0; z < lenNewB; ++z)
                                    ipBuf[y++] = newB[z];
                                return;
                            }
                        }
                        ++x;

                        while (!ipBuf[y].Equals(@"\1"))
                            ++y;
                        ++y;
                    }
                }
                else
                if (separator == ' ')
                {
                    int x = 0;
                    int y = 0;
                    while (true)
                    {
                        while (ipBuf[y] != '"') // opening quote
                            ++y;
                        ++y;

                        if (x == fld)
                        {
                            int lenNewB = Lengthchars(newB, 0);
                            int lenOldStr = 0; // does not include the two quotes
                            int z = y;
                            while (ipBuf[z] != '"') // closing quote
                            {
                                ++lenOldStr;
                                ++z;
                            }

                            if (lenOldStr == lenNewB)
                            {
                                for (z = 0; z < lenNewB; ++z)
                                    ipBuf[y++] = newB[z];
                                return;
                            }
                            else
                            if (lenOldStr < lenNewB) // newB needs more room, so, shunt-up by the difference
                            {
                                int diff = lenNewB - lenOldStr;
                                for (z = bufLen - 1; z > diff && z > y; --z)
                                    ipBuf[z] = ipBuf[z - diff];
                                for (z = 0; z < lenNewB; ++z)
                                    ipBuf[y++] = newB[z];
                                return;
                            }
                            else // lenOldStr > lenNewB
                            {
                                // newB needs less room, so, shunt-down by the difference
                                int diff = lenOldStr - lenNewB;
                                for (z = y; z < bufLen - diff; ++z)
                                    ipBuf[z] = ipBuf[z + diff];
                                for (z = 0; z < lenNewB; ++z)
                                    ipBuf[y++] = newB[z];
                                return;
                            }
                        }
                        ++x;

                        while (ipBuf[y] != '"') // opening quote
                            ++y;
                        ++y;
                    }
                }
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // works with separator of(char)1 (that is, ipBuf[] = 'abc.xxx=123\001def.yyy=123456789\001ghi.zzz=789\001', and where the
        // "123456789" could be replaced by contents of newB, "123". (Where fldName is 'yyy').
        public void RepAlphaUsingOnes(char[] ipBuf, int bufLen, string fldName, char[] newB)
        {
            RepAlphaUsingOnes(ipBuf, bufLen, fldName, StringFromChars(newB));
        }
        public void RepAlphaUsingOnes(char[] ipBuf, int bufLen, string fldName, string newB)
        {
            char[] thisFldName = new char[100];
            try
            {
                int a, x = 0, y = 0;
                while (true)
                {
                    while (ipBuf[y] != '.')
                        ++y;
                    a = 0;
                    ++y;
                    while (ipBuf[y] != '=') // start
                        thisFldName[a++] = ipBuf[y++];
                    thisFldName[a] = '\0';
                    ++y;

                    if (MatchIgnoreCase(thisFldName, 0, fldName))
                    {
                        int lenNewB = newB.Length;
                        int lenOldStr = 0; // does not include the two quotes
                        int z = y;
                        while (!ipBuf[z].Equals(@"\1") && ipBuf[z] != '\0') // end
                        {
                            ++lenOldStr;
                            ++z;
                        }

                        if (lenOldStr == lenNewB)
                        {
                            for (z = 0; z < lenNewB; ++z)
                                ipBuf[y++] = (char)newB[z];
                            return;
                        }
                        else
                        if (lenOldStr < lenNewB) // newB needs more room, so, shunt-up by the difference
                        {
                            int diff = lenNewB - lenOldStr;
                            for (z = bufLen - 1; z > diff && z > y; --z)
                                ipBuf[z] = ipBuf[z - diff];
                            for (z = 0; z < lenNewB; ++z)
                                ipBuf[y++] = (char)newB[z];
                            return;
                        }
                        else // lenOldStr > lenNewB
                        {
                            // newB needs less room, so, shunt-down by the difference
                            int diff = lenOldStr - lenNewB;
                            for (z = y; z < bufLen - diff; ++z)
                                ipBuf[z] = ipBuf[z + diff];
                            for (z = 0; z < lenNewB; ++z)
                                ipBuf[y++] = (char)newB[z];
                            return;
                        }
                    }
                    ++x;

                    while (!ipBuf[y].Equals(@"\1")) // start
                        ++y;
                    ++y;
                }
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // works with separator of(char)1 (that is, ipBuf[] = 'abc.xxx=123\001def.yyy=123456789\001ghi.zzz=789\001'
        public void DfsAlphaUsingOnes(char[] ipBuf, string fldName, char[] value)
        {
            char[] thisFldName = new char[100];
            try
            {
                int a, x = 0, y = 0;
                while (true)
                {
                    while (ipBuf[y] != '.')
                        ++y;
                    a = 0;
                    ++y;
                    while (ipBuf[y] != '=') // start
                        thisFldName[a++] = ipBuf[y++];
                    thisFldName[a] = '\0';
                    ++y;
                    if (MatchIgnoreCase(thisFldName, 0, fldName))
                    {
                        int z = 0;
                        while (!ipBuf[y].Equals(@"\1") && ipBuf[y] != '\0') // end
                            value[z++] = ipBuf[y++];
                        value[z++] = (char)'\0';
                        return;
                    }
                    ++x;

                    while (!ipBuf[y].Equals(@"\1")) // start
                        ++y;
                    ++y;
                }
            }
            catch (Exception) { value[0] = '\0'; return; }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Assumes separator of ' '.
        // E.g., ipBuf[] = 'fred.abc="123" fred.def="12345" fred.ghi="789"
        // newB is 'jim' thence:
        // ipBuf[] = 'jim.abc="123" jim.def="12345" jim.ghi="789"
        public void RepFileNameGivenSeparator(char[] ipBuf, int bufLen, char[] newB)
        {
            char[] tmpBuf = new char[bufLen];
            int x = 0, y = 0, i;

            while (ipBuf[x] != '\0')
            {
                i = 0;
                while (newB[i] != '\0')
                    tmpBuf[y++] = newB[i++];

                while (ipBuf[x] != '.')
                    ++x;

                while (ipBuf[x] != '"')
                    tmpBuf[y++] = ipBuf[x++];
                tmpBuf[y++] = '"';
                ++x;
                while (ipBuf[x] != '"')
                    tmpBuf[y++] = ipBuf[x++];
                tmpBuf[y++] = '"';

                x += 2;
                tmpBuf[y++] = ' ';
            }
            tmpBuf[y] = '\0';

            x = 0;
            while (tmpBuf[x] != '\0')
            {
                ipBuf[x] = tmpBuf[x];
                ++x;
            }
            ipBuf[x] = '\0';
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Assumes separator of ' '.
        // E.g., ipBuf[] = 'abc="123" def="12345" ghi="789"
        // fld is 1. newB[] is "6789". sep is "<br>" thence:
        // ipBuf[] = 'abc="123" def="12345<br>6789" ghi="789"
        public void AppendAlpha(char[] ipBuf, int bufLen, int fld, char[] newB, string sep)
        {
            int x = 0;
            int y = 0;
            while (true)
            {
                while (ipBuf[y] != '"') // opening quote
                    ++y;
                ++y;

                if (x == fld)
                {
                    while (ipBuf[y] != '"') // closing quote
                        ++y;

                    // shunt-up by the length of newb + sep
                    int lenNewB = Lengthchars(newB, 0);
                    int sepLen = sep.Length;
                    int more = lenNewB + sepLen;
                    int z;
                    for (z = bufLen - 1; (z - more) > y; --z)
                        ipBuf[z] = ipBuf[z - more];
                    for (z = 0; z < sepLen; ++z)
                        ipBuf[y++] = (char)sep[z];
                    for (z = 0; z < lenNewB; ++z)
                        ipBuf[y++] = newB[z];
                    ipBuf[y] = '"';
                    return;
                }
                ++x;

                while (ipBuf[y] != '"') // opening quote
                    ++y;
                ++y;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Assumes separator of(char)1.
        // E.g., ipBuf[] = 'abc=123\001def=12345\001ghi=789'
        // fld is 1. newB[] is '6789'. sep is '<br>' thence:
        // ipBuf[] = 'abc=123\001def=12345<br>6789\001ghi="89'
        public void AppendAlphaGivenBinary1(char[] ipBuf, int bufLen, int fld, char[] newB, string sep)
        {
            int x = 0;
            int y = 0;

            while (true)
            {
                while (ipBuf[y] != '=' && !ipBuf[y].Equals(@"\1"))
                    ++y;
                ++y;

                if (x == fld)
                {
                    while (!ipBuf[y].Equals(@"\1"))
                        ++y;

                    // shunt-up by the length of newb + sep
                    int lenNewB = Lengthchars(newB, 0);
                    int sepLen = sep.Length;
                    int more = lenNewB + sepLen;

                    int z;
                    for (z = bufLen - 1; (z - more) > y; --z) // make room
                        ipBuf[z] = ipBuf[z - more];

                    for (z = 0; z < sepLen; ++z)          // insert separator string
                        ipBuf[y++] = (char)sep[z];

                    for (z = 0; z < lenNewB; ++z)         // insert new stuff 
                        ipBuf[y++] = newB[z];
                    ipBuf[y] = (@"\1")[0];

                    return;
                }
                ++x;

                if (ipBuf[y - 1] == '=')
                {
                    while (!ipBuf[y].Equals(@"\1"))
                        ++y;
                    ++y;
                }
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public void PutAlpha(char[] buf, int bufLen, short posn, char[] str)
        {
            int count, len;
            int x, y;

            x = 0;
            count = 0;
            while (count < posn)
            {
                while (buf[x] != '\0')
                    ++x;
                ++x;
                ++count;
            }

            len = Lengthchars(str, 0);
            for (y = (bufLen - 1); y >= (x + len); --y)
                buf[y] = buf[y - len];

            for (y = 0; y < len; ++y)
                buf[x + y] = str[y];
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public void RepAlpha(char[] buf, int bufLen, short posn, char[] b)
        {
            DelAlpha(buf, bufLen, posn);
            PutAlpha(buf, bufLen, posn, b);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public void RepAlpha(char[] buf, int bufLen, short posn, string str)
        {
            DelAlpha(buf, bufLen, posn);
            PutAlpha(buf, bufLen, posn, str);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public void RepAlpha(char[] buf, int bufLen, short posn, int i)
        {
            DelAlpha(buf, bufLen, posn);
            PutAlpha(buf, bufLen, posn, IntToStr(i));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public void RepAlpha(char[] buf, int bufLen, short posn, double d)
        {
            DelAlpha(buf, bufLen, posn);
            PutAlpha(buf, bufLen, posn, DoubleToStr(d));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public void DelAlpha(char[] buf, int bufLen, short posn)
        {
            int count, len;
            int x, y;

            x = 0;
            count = 0;
            while (count < posn)
            {
                while (buf[x] != '\0')
                    ++x;
                ++x;
                ++count;
            }

            len = Lengthchars(buf, x); // determine len of bit to del
            for (y = (x + len); y < bufLen; ++y)
                buf[x++] = buf[y];
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public void PutAlpha(char[] buf, int bufLen, short posn, string str)
        {
            int count, len;
            int x, y;

            x = 0;
            count = 0;
            while (count < posn)
            {
                while (buf[x] != '\0')
                    ++x;
                ++x;
                ++count;
            }

            len = str.Length;
            for (y = (bufLen - 1); y >= (x + len); --y)
                buf[y] = buf[y - len];

            for (y = 0; y < len; ++y)
                buf[x + y] = (char)str[y];
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DfsAsStr(char[] ipBuf, short fld)
        {
            try
            {
                if (ipBuf == null) return "";

                string str = "";
                short x = 0;
                short y = 0;
                while (x < fld)
                {
                    while (ipBuf[y] != '\0')
                        ++y;
                    ++y;
                    ++x;
                }

                while (ipBuf[y] != '\0')
                    str += (char)ipBuf[y++];

                return str;
            }
            catch (Exception)
            {
                return "";
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DfsAsStrGivenBinary2(char[] ipBuf, int fld)
        {
            try
            {
                if (ipBuf == null) return "";

                string str = "";
                short x = 0;
                short y = 0;
                while (x < fld)
                {
                    while (!ipBuf[y].Equals(@"\002"))
                        ++y;
                    ++y;
                    ++x;
                }

                while (!ipBuf[y].Equals(@"\002"))
                    str += (char)ipBuf[y++];

                return str;
            }
            catch (Exception)
            {
                return "";
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DfsAsStrGivenBinary1(char[] ipBuf, short fld)
        {
            try
            {
                if (ipBuf == null) return "";

                string str = "";
                short x = 0;
                short y = 0;
                while (x < fld)
                {
                    while (!ipBuf[y].Equals(@"\1"))
                        ++y;
                    ++y;
                    ++x;
                }

                while (!ipBuf[y].Equals(@"\1"))
                    str += (char)ipBuf[y++];

                return str;
            }
            catch (Exception)
            {
                return "";
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void DfsGivenBinary1(bool onlyValue, char[] ipBuf, short fld, char[] b)
        {
            StrToChars(b, DfsAsStrGivenBinary1(onlyValue, ipBuf, fld));
        }
        public string DfsAsStrGivenBinary1(bool onlyValue, char[] ipBuf, short fld)
        {
            try
            {
                string str = "";
                short x = 0;
                short y = 0;

                while (x < fld)
                {
                    while (!ipBuf[y].Equals(@"\1"))
                        ++y;
                    ++y;
                    ++x;
                }

                if (onlyValue)
                {
                    while (ipBuf[y] != '=')
                        ++y;
                    ++y;

                    while (!ipBuf[y].Equals(@"\1"))
                        str += (char)ipBuf[y++];
                }
                else
                {
                    while (ipBuf[y] != '=' && !ipBuf[y].Equals(@"\1"))
                        str += (char)ipBuf[y++];
                    if (!ipBuf[y].Equals(@"\1"))
                    {
                        while (!ipBuf[y].Equals(@"\1"))
                            str += (char)ipBuf[y++];
                    }
                }

                return str;
            }
            catch (Exception)
            {
                return "";
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DfsAsStrGivenSeparator(char separator, char[] ipBuf, short fld)
        {
            return DfsAsStrGivenSeparator(false, separator, ipBuf, fld);
        }
        public string DfsAsStrGivenSeparator(bool onlyValue, char separator, char[] ipBuf, short fld)
        {
            try
            {
                string str = "";
                short x = 0;
                short y = 0;
                bool quit, open;
                while (x < fld)
                {
                    open = quit = false;
                    while (!quit)
                    {
                        if (ipBuf[y] == '"')
                        {
                            open = !open;
                        }

                        if (ipBuf[y] == separator)
                        {
                            if (!open)
                                quit = true;
                            else ++y;
                        }
                        else ++y;
                    }
                    ++y;
                    ++x;
                }

                if (onlyValue)
                {
                    while (ipBuf[y] != '"')
                        ++y;
                    ++y;

                    while (ipBuf[y] != '"')
                        str += (char)ipBuf[y++];
                }
                else
                {
                    while (ipBuf[y] != '"' && ipBuf[y] != separator)
                        str += (char)ipBuf[y++];
                    if (ipBuf[y] != separator)
                    {
                        ++y;
                        while (ipBuf[y] != '"')
                            str += (char)ipBuf[y++];
                        str += '\"';
                    }
                }

                return str;
            }
            catch (Exception)
            {
                return ("");
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DfsAsStr(string ipStr, short fld)
        {
            string str = "";
            short x = 0;
            short y = 0;
            while (x < fld)
            {
                while (!ipStr[y].Equals(@"\1"))
                    ++y;
                ++y;
                ++x;
            }

            while (!ipStr[y].Equals(@"\1"))
                str += ipStr[y++];

            return str;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int DfsAsInt(char[] ipBuf, short fld)
        {
            return StrToInt(DfsAsStr(ipBuf, fld));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int DfsAsIntGivenSeparator(char separator, char[] ipBuf, short fld)
        {
            return StrToInt(DfsAsStrGivenSeparator(false, separator, ipBuf, fld));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public double DfsAsDouble(char[] ipBuf, short fld)
        {
            return DoubleFromStr(DfsAsStr(ipBuf, fld));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public double DfsAsDoubleGivenSeparator(bool onlyValue, char separator, char[] ipBuf, short fld)
        {
            char[] b = new char[50];
            DfsGivenSeparator(onlyValue, separator, ipBuf, fld, b);
            return DoubleFromCharsCharFormat(b, 0);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Assumes null-term'd chars; in a char format (e.g., '1','2','3')
        public int IntFromCharsCharFormat(char[] b, short fromchar)
        {
            short x = fromchar;
            int y = 0;

            while (b[x] != 0)
            {
                y *= 10;
                y += (b[x] - 48);
                ++x;
            }

            return y;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Appends a null
        public void IntToCharsCharFormat(int value, char[] b, short fromchar)
        {
            string s = Convert.ToString(value);

            int len = s.Length;

            int x = 0;
            while (x < len)
            {
                b[fromchar + x] = s[x];
                ++x;
            }
            b[fromchar + x] = '\0';
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string IntToStr(int value)
        {
            return Convert.ToString(value);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string LongToStr(long value)
        {
            return Convert.ToString(value);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int IntFromChars(char[] b)
        {
            try
            {
                return Convert.ToInt32(b);
            }
            catch (Exception)
            {
                return 0;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public long LongFromChars(char[] b)
        {
            try
            {
                return Convert.ToInt64(b);
            }
            catch (Exception)
            {
                return 0L;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Assumes null-term'd chars; in a char format (e.g., '1','2','3')
        public long LongFromCharsCharFormat(char[] b, short fromchar)
        {
            short x = fromchar;
            long y = 0;

            while (b[x] != 0)
            {
                y *= 10;
                y += (b[x] - 48);
                ++x;
            }

            return y;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public short ShortFromChars(char[] b)
        {
            try
            {
                return Convert.ToInt16(b);
            }
            catch (Exception)
            {
                return (short)0;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void ShortToChars(short theShort, char[] b)
        {
            try
            {
                string s = Convert.ToString(theShort);

                b = GetChars(s);
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public string StringFromChars(char[] b)
        {
            return Convert.ToString(b);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void StringToChars(string str, int start, char[] b)
        {
            int y = 0;
            int x = start;
            int len = str.Length;
            while (x < len)
            {
                b[y] = (char)str[x];
                ++y;
                ++x;
            }
            b[y] = (char)0;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int StringIntoChars(string str, int start, char[] b, int startchars)
        {
            int y = startchars;
            int x = start;
            int len = str.Length;
            while (x < len)
                b[y++] = (char)str[x++];
            return y;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void CatAsChars(string str, char terminator, char[] b)
        {
            int x = 0;
            while (str[x] != terminator)
            {
                b[x] = (char)str[x];
                ++x;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void CatAsChars(string str, int start, char[] b, bool isFirst)
        {
            int y = isFirst ? 0 : Lengthchars(b, 0);
            int x = start;
            int len = str.Length;
            while (x < len)
                b[y++] = (char)str[x++];
            b[y] = '\0';
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Copies b2 to b1 (b2 must be null'd)
        public void CharsToChars(char[] b1, int start1, char[] b2, int start2)
        {
            int x = start1;
            int y = start2;

            while (b2[y] != 0)
                b1[x++] = b2[y++];
            b1[x] = '\0';
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Copies b2 to b1 (given length of b2)
        public void CharsToChars(char[] b1, int start1, char[] b2, int start2, int len)
        {
            for (int x = 0; x < len; ++x)
                b1[start1 + x] = b2[start2 + x];
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Copies str to b1 (b1 assumed empty)
        public int StrToChars(char[] b1, string str)
        {
            return StrToChars(b1, str, 0, str.Length);
        }
        public int StrToChars(char[] b1, string str, int strStart, int strLen)
        {
            if (strLen == -1)
                strLen = str.Length;

            int x = strStart;
            int y = 0;

            while (x < strLen)
                b1[y++] = (char)str[x++];
            b1[y] = '\0';
            return y;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Cats b2 to b1 (b2 must be null'd)
        public void CharsToChars(char[] b1, char[] b2, int start2)
        {
            int x = Lengthchars(b1, 0);
            int y = start2;

            while (b2[y] != 0)
                b1[x++] = b2[y++];
            b1[x] = '\0';
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Assumes null-term'd chars; in a char format (e.g., '1','2','3')
        public double DoubleFromCharsCharFormat(char[] bb, int fromchar)
        {
            int x = fromchar;
            double y = 0;

            char[] b = new char[100]; // plenty
            bool isNeg;
            if (bb[0] == '-') // -ve
            {
                isNeg = true;
                while (bb[x] != '\0')
                {
                    b[x] = bb[x + 1];
                    ++x;
                }
                b[x] = '\0';
            }
            else
            {
                isNeg = false;
                while (bb[x] != '\0')
                {
                    b[x] = bb[x];
                    ++x;
                }
                b[x] = '\0';
            }
            x = 0;

            while (b[x] != '.' && b[x] != '\0')
            {
                y *= 10;
                y += (b[x] - 48);
                ++x;
            }

            if (b[x] == '.')
            {
                double j, z = 0;
                int i, w = 1;
                ++x;
                while (b[x] != '\0')
                {
                    j = b[x] - 48;
                    for (i = 0; i < w; ++i)
                        j /= 10.000000000;
                    z += j;

                    ++w;
                    ++x;
                }
                y += z;
            }

            if (isNeg)
                y *= -1;

            return y;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public double DoubleFromChars(char[] b)
        {
            try
            {
                return Convert.ToDouble(b);
            }
            catch (Exception)
            {
                return 0L;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int Lengthchars(char[] b, int start)
        {
            int len = 0;
            int x = start;
            try
            {
                while (b[x] != '\0')
                {
                    ++len;
                    ++x;
                }
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
            return len;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public char[] GetChars(string str)
        {

            char[] b = new char[str.Length];

            StringReader sr = new StringReader(str);

            sr.Read(b, 0, str.Length);

            return b;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool Match(char[] b1, string str2)
        {
            char[] b2 = GetChars(str2);
            int len = str2.Length;
            int len1 = Lengthchars(b1, 0);
            if (len1 > len)
                len = len1;
            ++len;
            return Match('=', b1, 0, b2, 0, len);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool Match(char[] b1, string str2, int len)
        {
            char[] b2 = GetChars(str2);

            if (len == 0) return false;

            int len1 = Lengthchars(b1, 0);

            return len <= len1 && Match('=', b1, 0, b2, 0, len);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool Match(char test, char[] b1, int start1, char[] b2, int start2, int numChars)
        {
            int len1 = Lengthchars(b1, 0);
            int len2 = Lengthchars(b2, 0);
            int x;

            switch (test)
            {
                case '>':
                    x = 0;
                    while (x < numChars && x < len1 && x < len2)
                    {
                        if (b1[start1 + x] > b2[start2 + x])
                            return true;
                        else
                          if (b1[start1 + x] < b2[start2 + x])
                            return false;
                        ++x;
                    }
                    if (x == numChars)
                        return false;
                    if (x == len2)
                    {
                        return x != len1;
                    }
                    return false;
                case '<':
                    x = 0;
                    while (x < numChars && x < len1 && x < len2)
                    {
                        if (b1[start1 + x] < b2[start2 + x])
                            return true;
                        else
                          if (b1[start1 + x] > b2[start2 + x])
                            return false;
                        ++x;
                    }
                    if (x == numChars)
                        return false;
                    if (x == len1)
                    {
                        return x != len2;
                    }
                    return false;
                case '=':
                    x = 0;
                    while (x < numChars && x < len1 && x < len2)
                    {
                        if (b1[start1 + x] != b2[start2 + x])
                            return false;
                        ++x;
                    }
                    if (x == numChars)
                        return true;
                    if (x == len1 || x == len2)
                    {
                        return len1 == len2 ? true : false;
                    }
                    return true;
            }

            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool MatchFixed(char test, char[] b1, int start1, char[] b2, int start2, int numChars)
        {
            int x = 0;

            switch (test)
            {
                case '>':
                    x = 0;
                    while (x < numChars)
                    {
                        if (b1[start1 + x] > b2[start2 + x])
                            return true;
                        else
                          if (b1[start1 + x] < b2[start2 + x])
                            return false;
                        ++x;
                    }
                    return false;
                case '<':
                    x = 0;
                    while (x < numChars)
                    {
                        if (b1[start1 + x] < b2[start2 + x])
                            return true;
                        else
                          if (b1[start1 + x] > b2[start2 + x])
                            return false;
                        ++x;
                    }
                    return false;
                case '=':
                    x = 0;
                    while (x < numChars)
                    {
                        if (b1[start1 + x] != b2[start2 + x])
                            return false;
                        ++x;
                    }
                    return true;
            }

            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool MatchIgnoreCase(char[] b1, int start1, char[] b2, int start2)
        {
            try
            {
                if (Lengthchars(b1, 0) != Lengthchars(b2, 0)) // cannot be the same
                    return false;

                if ((b1[0] == '\0' && b2[0] != '\0') || (b1[0] != '\0' && b2[0] == '\0'))
                    return false;

                int x = 0;
                char char1, char2;
                while (b1[start1 + x] != '\0')
                {
                    char1 = b1[start1 + x];
                    char2 = b2[start2 + x];
                    if (char1 >= (char)'a' && char1 <= (char)'z')
                        char1 -= (char)32;
                    if (char2 >= (char)'a' && char2 <= (char)'z')
                        char2 -= (char)32;
                    if (char1 != char2)
                        return false;
                    ++x;
                }
                return true;
            }
            catch (Exception)
            {
                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool MatchIgnoreCase(char[] b1, int start1, string s)
        {
            try
            {
                if (Lengthchars(b1, 0) != s.Length) // cannot be the same
                    return false;
                int x = 0;
                char char1, char2;
                while (b1[start1 + x] != '\0')
                {
                    char1 = b1[start1 + x];
                    char2 = s[x];
                    if (char1 >= (char)'a' && char1 <= (char)'z')
                        char1 -= (char)32;
                    if (char2 >= (char)'a' && char2 <= (char)'z')
                        char2 -= (char)32;
                    if (char1 != char2)
                        return false;
                    ++x;
                }
                return true;
            }
            catch (Exception)
            {
                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool Contains(char[] ip, string reqd)
        {
            return Contains(ip, 0, reqd);
        }
        public bool Contains(char[] ip, int startFrom, string reqd)
        {
            char[] b = new char[100]; // plenty
            int lenReqd = StrToChars(b, reqd);
            int x = startFrom;
            int len = Lengthchars(ip, 0);
            while (x < len)
            {
                if (MatchFixed('=', ip, x, b, 0, lenReqd))
                    return true;
                ++x;
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int ContainsPosn(char[] ip, int startFrom, string reqd)
        {
            char[] b = new char[reqd.Length + 1];
            int lenReqd = StrToChars(b, reqd);
            int x = startFrom;
            int len = Lengthchars(ip, 0);
            while (x < len)
            {
                if (MatchFixed('=', ip, x, b, 0, lenReqd))
                    return x;
                ++x;
            }

            return -1;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int IndexOf(char[] buf, char ch)
        {

            int x = 0;
            while (buf[x] != '\0')
            {
                if (buf[x] == (char)ch)
                    return x;

                ++x;
            }

            return -1;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool StartsWithIgnoreCase(char[] b1, int start1, string s)
        {
            try
            {
                int x = 0;
                char char1, char2;
                int len = s.Length;
                while (x < len)
                {
                    char1 = b1[start1 + x];
                    char2 = (char)s[x];
                    if (char1 >= (char)'a' && char1 <= (char)'z')
                        char1 -= (char)32;
                    if (char2 >= (char)'a' && char2 <= (char)'z')
                        char2 -= (char)32;
                    if (char1 != char2)
                        return false;
                    ++x;
                }
                return true;
            }
            catch (Exception)
            {
                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool Match(char test, char[] b1, int start1, char[] b2, int start2)
        {
            try
            {
                int x = 0;
                switch (test)
                {
                    case '>':
                        while (b1[start1 + x] != '\0' && !b1[start1 + x].Equals(@"\1") && b1[start1 + x] != (char)2)
                        {
                            if (b1[start1 + x] > b2[start2 + x])
                                return true;
                            else
                              if (b1[start1 + x] < b2[start2 + x])
                                return false;
                            ++x;
                        }
                        return false;
                    case '<':
                        while (b1[start1 + x] != '\0' && !b1[start1 + x].Equals(@"\1") && b1[start1 + x] != (char)2)
                        {
                            if (b1[start1 + x] < b2[start2 + x])
                                return true;
                            else
                              if (b1[start1 + x] > b2[start2 + x])
                                return false;
                            ++x;
                        }
                        return false;
                    case '=':
                        if ((b1[0] == '\0' && b2[0] != '\0') || (b1[0] != '\0' && b2[0] == '\0'))
                            return false;

                        if (Lengthchars(b1, 0) != Lengthchars(b2, 0)) // cannot be the same
                            return false;

                        while (b1[start1 + x] != '\0' && !b1[start1 + x].Equals(@"\1") && b1[start1 + x] != (char)2)
                        {
                            if (b1[start1 + x] != b2[start2 + x])
                                return false;
                            ++x;
                        }
                        break;
                }
                return true;
            }
            catch (Exception)
            {
                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool Match(char test, string s1, int start1, string s2, int start2, int numChars)
        {
            int len1 = s1.Length;
            int len2 = s2.Length;
            int x;

            switch (test)
            {
                case '>':
                    x = 0;
                    while (x < numChars && x < len1 && x < len2)
                    {
                        if (s1[start1 + x] > s2[start2 + x])
                            return true;
                        else
                        if (s1[start1 + x] < s2[start2 + x])
                            return false;
                        ++x;
                    }
                    if (x == numChars)
                        return false;
                    if (x == len2)
                    {
                        return x != len1;
                    }
                    return false;
                case '<':
                    x = 0;
                    while (x < numChars && x < len1 && x < len2)
                    {
                        if (s1[start1 + x] < s2[start2 + x])
                            return true;
                        else
                        if (s1[start1 + x] > s2[start2 + x])
                            return false;
                        ++x;
                    }
                    if (x == numChars)
                        return false;
                    if (x == len1)
                    {
                        return x != len2;
                    }
                    return false;
                case '=':
                    x = 0;
                    while (x < numChars && x < len1 && x < len2)
                    {
                        if (s1[start1 + x] != s2[start2 + x])
                            return false;
                        ++x;
                    }
                    if (x == numChars)
                        return true;
                    if (x == len1 || x == len2)
                    {
                        return len1 == len2 ? true : false;
                    }
                    return true;
            }

            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Converts chars to upper case; term'd by null (or not - arraybounds... caught)
        public void ToUpper(char[] b, int start)
        {
            try
            {
                int x = start;
                while (b[x] != '\0')
                {
                    if (b[x] >= 'a' && b[x] <= 'z')
                        b[x] -= (char)32;
                    ++x;
                }
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Converts chars to lower case; term'd by null (or not - arraybounds... caught)
        public void ToLower(char[] b, int start)
        {
            try
            {
                int x = start;
                while (b[x] != '\0')
                {
                    if (b[x] >= 'A' && b[x] <= 'Z')
                        b[x] += (char)32;
                    ++x;
                }
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int StrToInt(string s)
        {
            return s.Length == 0 ? 0 : Convert.ToInt32(s);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public long StrToLong(string s)
        {
            return Convert.ToInt64(s);
        }

        //// -------------------------------------------------------------------------------------------------------------------------------------------------
        //public double DoubleFromChars(char[] b)
        //{
        //    string s = StringFromChars(b);
        //    return s.Length == 0 ? 0.0 : Convert.ToDouble(s);
        //}

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public double DoubleFromStr(string s)
        {
            if (s.Length == 0) return 0.0;
            if (s[0] == ' ') return 0.0;

            string t = "";
            for (int x = 0; x < s.Length; ++x)
            {
                if (s[x] != ',')
                    t += s[x];
            }
            return Convert.ToDouble(t);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public int IntFromStr(string s)
        {
            return s.Length == 0 ? 0 : s[0] == ' ' ? 0 : Convert.ToInt32(s);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DoubleToStr(double d)
        {
            return DoubleToStr('2', d);
        }
        public string DoubleToStr(char dps, double d)
        {
            char[] buf = new char[50];
            DoubleToChars(dps, d, buf, 0);
            return StringFromChars(buf);
        }
        public string DoubleToStr(bool stripZeroes, char dps, double d)
        {
            char[] buf = new char[50];
            DoubleToChars(dps, d, buf, 0);
            if (stripZeroes)
            {
                int x = Lengthchars(buf, 0);
                --x;
                while (x > 0 && buf[x] == '0')
                    --x;
                buf[x + 1] = '\0';
                if (buf[x] == '.')
                {
                    buf[x + 1] = '0';
                    buf[x + 2] = '0';
                    buf[x + 3] = '\0';
                }
                if (buf[x - 1] == '.')
                {
                    buf[x + 1] = '0';
                    buf[x + 2] = '\0';
                }

            }

            return StringFromChars(buf);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public short DoubleToChars(char dps, double d, char[] buf, int start)
        {
            try
            {
                int x;
                int numChars = dps - 48;
                string s = "";

                if (d.Equals(0.0))
                {
                    buf[start] = (char)'0';
                    buf[start + 1] = (char)'.';
                    buf[start + 2] = (char)'0';
                    buf[start + 3] = (char)'0';
                    buf[start + 4] = (char)'\0';
                    return 4;
                }

                // truncate after dps number of chars
                double dd3 = Math.Round(d, dps);

                string t = dd3.ToString();

                int len = t.Length;
                x = 0;
                while (x < len && t[x] != '.')
                    s += t[x++];

                if (x != len) // has decPt
                {
                    int y = 0;
                    while (x < len && y <= (numChars + 1))
                    {
                        s += t[x++];
                        ++y;
                    }
                }

                d = DoubleFromStr(s);

                double dd2 = Math.Round(d, dps);

                s = dd2.ToString();

                x = 0;
                while (x < s.Length && s[x] != '.')
                {
                    buf[start + x] = (char)s[x];
                    ++x;
                }

                len = x;
                if (numChars > 0)
                {
                    buf[start + x++] = '.';
                    ++len;

                    int y = 0;
                    while (y < numChars)
                    {
                        buf[start + x] = len < s.Length ? (char)s[x] : (char)'0';
                        ++x;
                        ++y;
                        ++len;
                    }
                }

                buf[start + x] = (char)'\0';

                if (Match(buf, "-0.00"))
                {
                    buf[start] = (char)'0';
                    buf[start + 1] = (char)'.';
                    buf[start + 2] = (char)'0';
                    buf[start + 3] = (char)'0';
                    buf[start + 4] = (char)'\0';
                }

                return (short)++len;
            }
            catch (Exception)
            {
                buf[start] = (char)'0';
                buf[start + 1] = (char)'\0';
                return 1;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public double DoubleDPs(double d, char dps)
        {
            char[]
            thisVal = new char[100];
            DoubleToChars(dps, d, thisVal, 0);
            return DoubleFromChars(thisVal);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void DoubleDPs(char[] thisVal, char[] newVal, char dps)
        {
            double d = DoubleFromChars(thisVal);
            DoubleToChars(dps, d, newVal, 0);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DoubleDPs(string thisVal, char dps)
        {
            double d = DoubleFromStr(thisVal);
            char[] newVal = new char[50];
            DoubleToChars(dps, d, newVal, 0);
            return StringFromChars(newVal);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DoubleDPs(char dps, double d)
        {
            char[] newVal = new char[50];
            DoubleToChars(dps, d, newVal, 0);
            return StringFromChars(newVal);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Simply truncates an entry after a given num of DPs
        public void CharsDPsGivenSeparatorRegardless(bool stripTrailingZeroes, char separator, char dps, char[] b, int bufLen, int entry)
        {
            CharsDPsGivenSeparator(stripTrailingZeroes, separator, dps, b, bufLen, entry);
            char[] b2 = new char[100];
            DfsGivenSeparator(true, separator, b, (short)entry, b2);
            StripTrailingZeroes(b2, 0);
            RepAlphaGivenSeparator(separator, b, bufLen, (short)entry, b2);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void CharsDPsGivenSeparator(char separator, char dps, char[] b, int bufLen, int entry)
        {
            CharsDPsGivenSeparator(false, separator, dps, b, bufLen, entry);
        }
        public void CharsDPsGivenSeparator(bool stripTrailingZeroes, char separator, char dps, char[] b, int bufLen, int entry)
        {
            char[] opBuf = new char[1000];

            DfsGivenSeparator(false, separator, b, (short)entry, opBuf);

            int numDPs = (dps - (char)48);

            int x = 0, y = 0;

            switch (separator)
            {
                case (char)1:
                    while (opBuf[x] != '\0' && opBuf[x] != '=')
                        ++x;
                    if (opBuf[x] != '\0')
                    {
                        char[] thisVal = new char[100];
                        ++x;
                        int z = 0;
                        while (opBuf[x] != '\0' && !opBuf[x].Equals(@"\1"))
                            thisVal[z++] = opBuf[x++];
                        thisVal[z] = '\0';

                        double dd = DoubleFromChars(thisVal);
                        DoubleToChars('8', dd, thisVal, 0);
                        if (stripTrailingZeroes)
                            StripTrailingZeroes(thisVal, numDPs);
                        RepAlphaGivenSeparator(separator, b, bufLen, (short)entry, thisVal);
                    }
                    break;
                case ' ':
                    while (opBuf[x] != '\0' && opBuf[x] != '"')
                        ++x;
                    if (opBuf[x] != '\0')
                    {
                        char[] thisVal = new char[100];
                        ++x;
                        int z = 0;
                        while (opBuf[x] != '\0' && opBuf[x] != '"' && opBuf[x] != '.')
                            thisVal[z++] = opBuf[x++];

                        if (opBuf[x] != '\0' && opBuf[x] != '"' && opBuf[x] == '.')
                        {
                            thisVal[z++] = '.';
                            ++x;
                        }

                        while (opBuf[x] != '\0' && opBuf[x] != '"')
                        {
                            thisVal[z++] = opBuf[x++];
                            ++y;
                        }

                        thisVal[z] = '\0';

                        double d2 = DoubleFromChars(thisVal);
                        DoubleToChars('8', d2, thisVal, 0);
                        if (stripTrailingZeroes)
                            StripTrailingZeroes(thisVal, numDPs);

                        RepAlphaGivenSeparator(separator, b, bufLen, (short)entry, thisVal);
                    }
                    break;
                case '\0':
                    while (opBuf[x] != '\0' && opBuf[x] != '.')
                        ++x;

                    if (opBuf[x] != '\0' && opBuf[x] == '.')
                        ++x;

                    while (opBuf[x] != '\0')
                    {
                        ++x;
                        ++y;
                    }

                    opBuf[x] = '\0';

                    double d = DoubleFromChars(opBuf);
                    DoubleToChars('8', d, opBuf, 0);
                    if (stripTrailingZeroes)
                        StripTrailingZeroes(opBuf, numDPs);

                    RepAlphaGivenSeparator(separator, b, bufLen, (short)entry, opBuf);
                    break;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Sets dPs in a \001-separated value-only buf
        public void CharsDPsGivenBinary1(bool stripTrailingZeroes, char dps, char[] b, int bufLen, int entry)
        {
            char[] opBuf = new char[1000];
            DfsGivenSeparator(false, (char)1, b, (short)entry, opBuf);

            int numDPs = (dps - (char)48);

            int x = 0, y = 0;

            while (opBuf[x] != '\0' && opBuf[x] != '.')
                ++x;

            if (opBuf[x] != '\0' && opBuf[x] == '.')
                ++x;
            while (opBuf[x] != '\0')
            {
                ++x;
                ++y;
            }

            opBuf[x] = '\0';

            double d = DoubleFromChars(opBuf);
            DoubleToChars('8', d, opBuf, 0);

            if (stripTrailingZeroes)
                StripTrailingZeroes(opBuf, numDPs);

            RepAlphaGivenSeparator((char)1, b, bufLen, (short)entry, opBuf);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StrDPs(bool StripTrailingZeroes, char dps, string s)
        {
            char[] b = new char[50];
            StrToChars(b, s);
            CharsDPsGivenSeparator(StripTrailingZeroes, '\0', dps, b, 50, 0);
            return StringFromChars(b);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Simply truncates a str after a given num of DPs
        public string StrDPs(char dps, string s)
        {
            int numDPs = dps - 48;

            string opStr = "";
            int x = 0;
            while (x < s.Length && s[x] != '.')
                opStr += s[x++];

            if (x < s.Length && s[x] == '.')
                opStr += '.';
            ++x;

            int y = 0;
            while (x < s.Length && y < numDPs)
            {
                opStr += s[x++];
                ++y;
            }

            return opStr.EndsWith(".", StringComparison.CurrentCulture) ? opStr.Substring(0, (opStr.Length - 1)) : opStr;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // removeBlanks will completely remove any entries that are all spaces
        public void StripLeadingAndTrailingSpaces(bool removeBlanks, char intermediateTerminator, char[] strs, int start, short numFlds, char[] oBuf)
        {
            short count = 0;
            int x = start;
            short y = 0;
            while (count < numFlds)
            {
                while (strs[x] == ' ')
                    ++x;
                while (strs[x] != '\0')
                    oBuf[y++] = strs[x++];
                --y;
                while (oBuf[y] == ' ')
                    --y;
                ++y;

                if (removeBlanks)
                {
                    if (oBuf[y - 1] == intermediateTerminator)
                        --y;
                    else ++count;
                }

                oBuf[y++] = (char)intermediateTerminator;
                ++x;
            }
            oBuf[y] = '\0';
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripQuotes(string str)
        {
            int x = 0, len = str.Length;
            string s = "";

            while (x < len)
            {
                if (str[x] != '"')
                    s += str[x];

                ++x;
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripQuote(string str)
        {
            int x = 0, len = str.Length;
            string s = "";

            while (x < len)
            {
                if (str[x] != '\'')
                    s += str[x];

                ++x;
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripLeadingAndTrailingSpaces(string str)
        {
            if (str.Length == 0)
                return "";

            int from = 0, len = str.Length;

            while (from < len && str[from] == ' ')
                ++from;

            if (from == len)
                return "";

            int x = len - 1;
            while (x > 0 && str[x] == ' ')
                --x;

            return str.Substring(from, x + 1);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripLeadingZeroes(string str)
        {
            if (str.Length == 0)
                return "";

            int from = 0, len = str.Length;

            while (from < len && str[from] == '0')
                ++from;

            return from == len ? "" : str.Substring(from);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripDuplicateSpaces(string str)
        {
            int x = 0, len = str.Length;
            bool first = true;
            string s = "";

            while (x < len)
            {
                if (str[x] != ' ')
                {
                    first = true;
                    s += str[x];
                }
                else // space
                {
                    if (first)
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
        public void StripLeadingAndTrailingSpaces(char[] b)
        {
            int x, from = 0, len = Lengthchars(b, 0);

            while (from < len && b[from] == ' ')
                ++from;

            x = len - 1;
            while (x > 0 && b[x] == ' ')
                --x;

            int z = 0;
            for (int y = from; y <= x; ++y)
                b[z++] = b[y];
            b[z] = '\0';
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void StripTrailingSpaces(char[] b)
        {
            StripTrailingSpaces(b, Lengthchars(b, 0));
        }
        public void StripTrailingSpaces(char[] b, int len)
        {
            --len;
            while (len >= 0 && b[len] == ' ')
                --len;
            b[++len] = '\0';
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripTrailingSpacesStr(string str)
        {
            if (str == null) return "";

            int x = str.Length - 1;
            while (x >= 0 && str[x] == ' ')
                --x;

            string s = "";
            for (int y = 0; y <= x; ++y)
                s += str[y];
            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void StripTrailingZeroes(char[] b, int minNumDPs)
        {
            int x = Lengthchars(b, 0);
            if (x == 0) return;

            bool hasDecPt = false;
            for (int i = 0; i < x; ++i)
                hasDecPt |= b[i] == '.';

            if (!hasDecPt)
                return;

            --x;
            while (x >= 0 && b[x] == '0' && b[x] != '.')
                --x;

            ++x;
            b[x] = '\0';

            x = 0;
            while (b[x] != '\0' && b[x] != '.')
                ++x;
            int y;
            if (b[x] == '\0')
            {
                for (y = 0; y < minNumDPs; ++y)
                    b[x++] = (char)'0';
                b[x] = '\0';
            }
            else // == '.'
            {
                bool no = true;
                if (minNumDPs == 0)
                {
                    y = x + 1;
                    while (b[y] != '\0')
                    {
                        if (b[y] != '0')
                        {
                            no = false;
                        }

                        ++y;
                    }
                    if (no)
                        b[x] = '\0';
                }
                else no = false;

                if (!no)
                {
                    ++x;
                    y = 0;
                    while (b[x] != '\0')
                    {
                        ++x;
                        ++y;
                    }
                    while (y < minNumDPs)
                    {
                        b[x++] = (char)'0';
                        ++y;
                    }
                    b[x] = '\0';
                }
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripTrailingZeroesStr(string str)
        {
            return StripTrailingZeroesStr(true, str);
        }
        public string StripTrailingZeroesStr(bool treatAsNumericStr, string str)
        {
            try
            {
                if (treatAsNumericStr)
                {
                    if (str.IndexOf(".", StringComparison.CurrentCultureIgnoreCase) == -1)
                        return str;
                }

                int x = str.Length - 1;
                while (x >= 0 && str[x] == '0')
                    --x;

                if (str[x] == '.')
                    --x;

                string s = "";
                for (int y = 0; y <= x; ++y)
                    s += str[y];

                return s;
            }
            catch (Exception) { return "0"; }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripAllNonNumeric(string str)
        {
            string str2 = "";
            int len = str.Length;
            for (int i = 0; i < len; ++i)
            {
                if (str[i] >= '0' && str[i] <= '9')
                    str2 += str[i];
            }
            return str2;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void Pb(string msg, char[] b, int start, int numchars)
        {
            Pb(msg, b, start, numchars, false);
        }
        public void Pb(string msg, char[] b, int start, int numchars, bool inDec)
        {
            int x = start;
            Console.WriteLine("");
            try
            {
                Console.Write(msg + ": ");
                if (numchars == 0)
                {
                    while (b[x] != '\0')
                    {
                        Console.Write((char)b[x]);
                        ++x;
                    }
                }
                else
                {
                    for (int i = 0; i < numchars; ++i)
                    {
                        switch (b[start + i])
                        {
                            case (char)0xFF:
                                Console.Write("*FF*,");
                                break;
                            case (char)0:
                                Console.Write("#,");
                                break;
                            case (char)1:
                                Console.Write("*1*,");
                                break;
                            case (char)2:
                                Console.Write("*2*,");
                                break;
                            case '\r':
                                Console.Write("*R*,");
                                break;
                            case '\n':
                                Console.Write("*N*,");
                                break;
                            default:
                                if (inDec)
                                    Console.Write(b[start + i] + ",");
                                else Console.Write((char)b[start + i] + ",");
                                break;
                        }
                    }
                }
            }
            catch (Exception e) { Console.WriteLine("generalUtils: " + e); }
        }

        // -------------------------------------------------------------------------------------------
        // decPlaces of -1 means any number of places
        public bool ValidNumeric(string num, int decPlaces)
        {
            int len = num.Length;
            for (int i = 0; i < len; ++i)
            {
                if ((num[i] >= '0' && num[i] <= '9') || num[i] == '-')
                { } // ok
                else
                {
                    if (num[i] == '.')
                    {
                        if (decPlaces == 0)
                            return false;
                    }
                    else return false;
                }
            }
            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // decPlaces of -1 means any number of places
        public bool ValidNumeric(char[] num, int decPlaces)
        {
            int i = 0, dotCount = 0;
            while (num[i] != '\0')
            {
                if ((num[i] >= '0' && num[i] <= '9') || num[i] == '-')
                { } // ok
                else
                {
                    if (num[i] == '.')
                    {
                        if (decPlaces == 0)
                            return false;
                        ++dotCount;
                    }
                    else return false;
                }
                ++i; // valid
            }
            return dotCount <= 1;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string Capitalize(string s)
        {
            if (s.Length == 0)
                return "";

            string rtnStr;

            string str = s.ToLower();

            s = s.ToUpper();
            int len = str.Length;
            rtnStr = "" + s[0];
            for (int x = 1; x < len; ++x)
                rtnStr += str[x];

            return rtnStr;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // list items are separated by \001
        // subfields within an item are separated by \002
        // list is terminated by \0
        // code will work whether there are subfields or not (i.e., whether there are any \002).
        // only the first subfield is used to match (or the whole item if there are no subfields).
        public char[] AppendToList(bool dupsAllowed, char[] newItem, char[] list, int[] listLen)
        {
            int x = 0, y = 0, len;
            char[]
            newItemKey = new char[500];
            char[] entry = new char[500];


            while (newItem[y] != (char)1 && newItem[y] != (char)2)
                newItemKey[x++] = newItem[y++];
            newItemKey[x] = '\0';
            len = x;

            x = y = 0;
            int upto = Lengthchars(list, 0);

            bool append = false;
            while (!append && list[y] != '\0')
            {
                x = 0;

                while (!list[y].Equals(@"\1") && list[y] != (char)2)
                    entry[x++] = list[y++];
                entry[x] = '\0';

                if (!dupsAllowed)
                {
                    if (len == x && MatchFixed('=', entry, 0, newItemKey, 0, x)) // same length and same chars
                        return list;
                }
                else append = true;
            }
            // insert at end
            len = Lengthchars(newItem, 0);
            if ((upto + len + 1) >= listLen[0])
            {
                char[] tmp = new char[listLen[0]];
                Array.Copy(list, 0, tmp, 0, listLen[0]);
                listLen[0] += (len + 1000);
                list = new char[listLen[0]];
                Array.Copy(tmp, 0, list, 0, listLen[0] - (len + 1000));
            }

            for (x = 0; x < len; ++x)
                list[upto + x] = newItem[x];
            list[upto + len] = '\0';

            return list;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // list items are separated by \001
        // subfields within an item are separated by \002
        // list is terminated by \0
        // code will work whether there are subfields or not (i.e., whether there are any \002).
        // on entry, newItem is \001 term'd (as well as \0)
        // only the first subfield is used to match (or the whole item if there are no subfields).
        public char[] AddToList(char[] newItem, char[] list, int[] listLen)
        {
            return AddToList(false, newItem, list, listLen);
        }
        public char[] AddToList(bool dupsAllowed, char[] newItem, char[] list, int[] listLen)
        {
            return AddToList(dupsAllowed, newItem, list, listLen, false);
        }
        public char[] AddToList(bool dupsAllowed, char[] newItem, char[] list, int[] listLen, bool firstEntryIsNumeric)
        {
            int x = 0, y = 0, len, start;
            char[]
            newItemKey = new char[500];
            char[] entry = new char[500];
            bool isLessThan;

            start = 0;

            while (newItem[y] != (char)1 && newItem[y] != (char)2)
                newItemKey[x++] = newItem[y++];
            newItemKey[x] = '\0';

            len = x;

            x = y = 0;
            int upto = Lengthchars(list, 0);

            while (list[y] != '\0')
            {
                x = 0;
                start = y;
                while (!list[y].Equals(@"\1") && list[y] != (char)2)
                    entry[x++] = list[y++];
                entry[x] = '\0';

                if (!dupsAllowed)
                {
                    if (len == x)
                    {
                        if (firstEntryIsNumeric)
                        {
                            int entryI = IntFromChars(entry);
                            int newItemKeyI = IntFromChars(newItemKey);

                            if (entryI == newItemKeyI)
                                return list;
                        }
                        else
                        {
                            if (MatchFixed('=', entry, 0, newItemKey, 0, x)) // same length and same chars
                                return list;
                        }
                    }
                }

                isLessThan = false;
                if (firstEntryIsNumeric)
                {
                    int entryI = IntFromChars(entry);
                    int newItemKeyI = IntFromChars(newItemKey);

                    if (entryI < newItemKeyI) // this entry < newitemkey, so read next...
                        isLessThan = true;
                }
                else
                {
                    isLessThan |= MatchFixed('<', entry, 0, newItemKey, 0, len);
                }

                if (isLessThan)
                {
                    while (!list[y].Equals(@"\1"))
                        ++y;
                    ++y;
                }
                else // this entry >= newitem, so insert now
                {
                    len = Lengthchars(newItem, 0);
                    if ((upto + len + 1) >= listLen[0])
                    {
                        char[] tmp = new char[listLen[0]];
                        Array.Copy(list, 0, tmp, 0, listLen[0]);
                        listLen[0] += 1000;
                        list = new char[listLen[0]];
                        Array.Copy(tmp, 0, list, 0, listLen[0] - 1000);
                    }

                    while (!list[y].Equals(@"\1"))
                        ++y;
                    ++y;
                    for (x = (listLen[0] - 1); x > (start + len - 1); --x) // shunt-up
                        list[x] = list[x - len];

                    for (x = 0; x < len; ++x)
                        list[start + x] = newItem[x];
                    return list;
                }
            }

            // insert at end
            len = Lengthchars(newItem, 0);
            if ((upto + len + 1) >= listLen[0])
            {
                char[] tmp = new char[listLen[0]];
                Array.Copy(list, 0, tmp, 0, listLen[0]);
                listLen[0] += 1000;
                list = new char[listLen[0]];
                Array.Copy(tmp, 0, list, 0, listLen[0] - 1000);
            }

            for (x = 0; x < len; ++x)
                list[y + x] = newItem[x];
            list[y + len] = '\0';

            return list;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool ChkList(char[] reqdItemKey, char[] list)
        {
            int x, y = 0;
            char[]
            entry = new char[500];

            while (list[y] != '\0')
            {
                x = 0;
                while (!list[y].Equals(@"\1") && list[y] != (char)2)
                    entry[x++] = list[y++];
                entry[x] = '\0';
                if (MatchFixed('=', entry, 0, reqdItemKey, 0, x))
                    return true;
                while (!list[y].Equals(@"\1"))
                    ++y;
                ++y;
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool GetListEntry(char[] reqdItemKey, char[] list, char[] entry)
        {
            int x, y = 0, lenNewItem = Lengthchars(reqdItemKey, 0);

            while (list[y] != '\0')
            {
                x = 0;
                while (!list[y].Equals(@"\1") && list[y].Equals(@"\002"))
                    entry[x++] = list[y++];
                entry[x] = '\0';

                if (MatchFixed('=', entry, 0, reqdItemKey, 0, lenNewItem))
                {
                    while (!list[y].Equals(@"\1"))
                        entry[x++] = list[y++];
                    entry[x] = '\0';

                    return true;
                }
                while (!list[y].Equals(@"\1"))
                    ++y;
                ++y;
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // origin-0
        public bool GetListEntryByNum(int entryNum, char[] list, char[] entry)
        {
            try
            {
                int x, y = 0, count = 0;

                while (list[y] != '\0')
                {
                    x = 0;
                    while (list[y] != '\0' && !list[y].Equals(@"\1")) // just-in-case
                        entry[x++] = list[y++];
                    entry[x] = '\0';

                    if (count == entryNum)
                    {
                        return true;
                    }

                    ++count;
                    ++y;
                }
            }
            catch (Exception e)
            {
                Console.Write("generalUtils: GetListEntryByNum: " + e);
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        // checks each list entry and pulls-out the record where the third fld matches thirdFld
        // starts search at entry num rec
        // rtns: entry num
        public int GetListEntryByNumAndValue(int entryNum, int maxEntries, char[] thirdFld, char[] list, char[] entry)
        {
            char[]
            third = new char[500]; // plenty
            int x, y = 0, z = 0, count = 0, len = Lengthchars(list, 0);

            if (entryNum >= maxEntries)
                return -1;

            while (count <= entryNum && count < maxEntries)
            {
                x = 0;
                while (y < len && !list[y].Equals(@"\1"))
                    entry[x++] = list[y++];
                entry[x] = '\0';
                ++y;
                ++count;
            }

            while (true)
            {
                x = 0;
                while (!entry[x].Equals("\002") && entry[x] != '\0') // step past fld 1
                    ++x;
                ++x;
                while (!entry[x].Equals("\002") && entry[x] != '\0') // step past fld 2
                    ++x;

                if (entry[x] == (char)2) // just-in-case
                {
                    ++x;
                    z = 0;
                    while (!entry[x].Equals("\002") && entry[x] != '\0')
                        third[z++] = entry[x++];
                    third[z] = '\0';

                    if (MatchIgnoreCase(third, 0, thirdFld, 0))
                    {
                        return count;
                    }
                }
                else return -1;

                x = 0;
                while (y < len && !list[y].Equals(@"\1"))
                    entry[x++] = list[y++];
                entry[x] = '\0';
                ++y;

                if (count == maxEntries)
                    return -1;

                ++count;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public char[] RepListEntry(char[] reqdItemKey, char[] newEntry, char[] list, int[] listLen)
        {
            int x, y = 0, z;
            char[]
            entry = new char[500];

            while (list[y] != '\0')
            {
                z = y;
                x = 0;
                while (!list[y].Equals(@"\1") && list[y] != (char)2)
                    entry[x++] = list[y++];

                entry[x] = '\0';
                if (MatchFixed('=', entry, 0, reqdItemKey, 0, x))
                {
                    while (!list[y].Equals(@"\1"))
                    {
                        ++x;
                        ++y;
                    }
                    ++x;
                    ++y;

                    int len = Lengthchars(newEntry, 0);
                    if (x < len) // need to consider extra storage
                    {
                        if (Lengthchars(list, 0) + len - x > listLen[0]) // need extra storage
                        {
                            char[] tmp = new char[listLen[0]];
                            Array.Copy(list, 0, tmp, 0, listLen[0]);
                            listLen[0] += 1000;
                            list = new char[listLen[0]];
                            Array.Copy(tmp, 0, list, 0, listLen[0] - 1000);
                        }
                    }

                    if (len > x) // new needs more room, so, shunt-up by the difference
                    {
                        int diff = len - x;
                        for (x = listLen[0] - 1; x > y; --x)
                            list[x] = list[x - diff];
                    }
                    else
                    if (x > len) // new needs less room, so, shunt-down by the difference
                    {
                        int diff = x - len;
                        for (x = z + len; x < listLen[0] - diff; ++x)
                            list[x] = list[x + diff];
                    }

                    x = 0;
                    while (!newEntry[x].Equals(@"\1")) // insert new
                        list[z++] = newEntry[x++];
                    list[z] = (char)1;

                    return list;
                }

                while (!list[y].Equals(@"\1"))
                    ++y;
                ++y;
            }

            return list;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public char[] RepListEntryByNum(int entryNum, char[] newEntry, char[] list, int[] listLen)
        {
            int x, y = 0, z, count = 0;
            char[]
            entry = new char[1000]; // plenty

            while (list[y] != '\0')
            {
                z = y;
                x = 0;
                while (!list[y].Equals(@"\1"))
                    entry[x++] = list[y++];
                entry[x++] = (char)1;
                entry[x] = '\0';
                if (count == entryNum)
                {
                    int len = Lengthchars(newEntry, 0);
                    if (x < len) // need to consider extra storage
                    {
                        if (Lengthchars(list, 0) + len - x > listLen[0]) // need extra storage
                        {
                            char[] tmp = new char[listLen[0]];
                            Array.Copy(list, 0, tmp, 0, listLen[0]);
                            listLen[0] += 2000;
                            list = new char[listLen[0]];
                            Array.Copy(tmp, 0, list, 0, listLen[0] - 2000);
                        }
                    }

                    if (len > x) // new needs more room, so, shunt-up by the difference
                    {
                        int diff = len - x;
                        for (x = listLen[0] - 1; x > y; --x)
                            list[x] = list[x - diff];
                    }
                    else
                    if (x > len) // new needs less room, so, shunt-down by the difference
                    {
                        int diff = x - len;
                        for (x = z + len; x < listLen[0] - diff; ++x)
                            list[x] = list[x + diff];
                    }

                    x = 0;
                    while (newEntry[x] != (char)1) // insert new
                        list[z++] = newEntry[x++];
                    list[z] = (char)1;

                    return list;
                }

                ++count;
                ++y;
            }

            return list; // just-in-case
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public int CountListEntries(char[] list)
        {
            int y = 0, count = 0;

            while (list[y] != '\0')
            {
                while (!list[y].Equals(@"\1"))
                    ++y;
                ++y;

                ++count;
            }

            return count;
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        // currently does not correctly replace dots and commas if the French-style is req'd
        public string FormatNumeric(double d, char numDecPlaces)
        {
            char[] b = new char[100];
            //DoubleToCharsCharFormat(d, b, 0, numDecPlaces);
            FormatNumeric(b, numDecPlaces);
            return StringFromChars(b);
        }
        public string FormatNumeric(int i)
        {
            char[] b = new char[100];
            IntToCharsCharFormat(i, b, (short)0);
            FormatNumeric(b, '0');
            return StringFromChars(b);
        }
        public string FormatNumeric(string s, char numDecPlaces)
        {
            char[] b = new char[100];

            StrToChars(b, s);

            FormatNumeric(b, numDecPlaces);

            return StringFromChars(b);
        }
        public void FormatNumeric(char[] b, char numDecPlaces)
        {
            double d = DoubleFromCharsCharFormat(b, 0);

            CharsDPsGivenSeparator(true, '\0', numDecPlaces, b, 20, 0); // does dp 'truncation'

            if (d < 1000 && d > -1000) // no need for thousand commas
                return;

            int x = 0;
            while (b[x] != '.' && b[x] != '\0')
                ++x;
            int p = x;

            int y = 0;
            char[] trailingStuff = new char[50];
            while (b[x] != '\0')
                trailingStuff[y++] = b[x++];
            trailingStuff[y] = '\0';

            b[p] = '\0'; // 'remove' trailing stuff from b

            char[] s = new char[20];
            char[] z = new char[20];
            int len = Lengthchars(b, 0);

            int i = 0;
            p = 0;
            int stop = d < 0 ? 1 : 0;
            while (len > stop)
            {
                ++i;
                if (i == 4)
                {
                    s[p++] = (char)',';
                    i = 1;
                }
                s[p++] = b[--len];
            }
            if (d < 0)
                s[p++] = '-';
            s[p] = '\0';

            // s is in reverse order
            i = 0;
            len = Lengthchars(s, 0);
            while (len > 0)
                z[i++] = s[--len];
            z[i] = '\0';

            if (d < 0)
            {
                string negSign = "-";
                if (negSign.Length == 0) // just-in-case
                    negSign = "-";
                if (negSign[0] != '-') // parens
                {
                    b[0] = '(';
                    CharsToChars(b, 1, z, 0);
                    i = Lengthchars(z, 1);
                    b[i++] = ')';
                    b[i] = '\0';
                }
                else // neg sign
                    CharsToChars(b, 0, z, 0);
            }
            else CharsToChars(b, 0, z, 0);

            CharsToChars(b, Lengthchars(b, 0), trailingStuff, 0);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string FormatJustEnough(double d)
        {
            if (d.Equals(0.0)) return "0";

            bool negative = false;
            if (d < 0)
            {
                d *= -1;
                negative = true;
            }

            if (DoubleDPs(d, '2') > 0.00)
            {
                if (negative) d *= -1;
                return FormatNumeric(d, '2');
            }

            if (DoubleDPs(d, '3') > 0.00)
            {
                if (negative) d *= -1;
                return FormatNumeric(d, '3');
            }

            if (DoubleDPs(d, '4') > 0.00)
            {
                if (negative) d *= -1;
                return FormatNumeric(d, '4');
            }

            if (DoubleDPs(d, '5') > 0.00)
            {
                if (negative) d *= -1;
                return FormatNumeric(d, '5');
            }

            if (DoubleDPs(d, '6') > 0.00)
            {
                if (negative) d *= -1;
                return FormatNumeric(d, '6');
            }

            if (DoubleDPs(d, '7') > 0.00)
            {
                if (negative) d *= -1;
                return FormatNumeric(d, '7');
            }

            if (negative) d *= -1;
            return FormatNumeric(d, '8');
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripAllSpaces(string str)
        {
            int len = str.Length;
            string s = "";

            for (int x = 0; x < len; ++x)
            {
                if (str[x] != ' ')
                    s += str[x];
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------
        public void StripAllSpaces(char[] b)
        {
            int x = 0, y = 0;

            while (b[x] != '\0')
            {
                if (b[x] != ' ')
                    b[y++] = b[x];
                ++x;
            }
            b[y] = '\0';
        }

        // -------------------------------------------------------------------------------------------
        public void Zeroize(char[] b, int len)
        {
            for (int x = 0; x < len; ++x)
                b[x] = '\0';
        }

        // -------------------------------------------------------------------------------------------
        public string ReplaceSpacesWith20(string str)
        {
            string s = "";
            int len = str.Length;
            for (int x = 0; x < len; ++x)
            {
                if (str[x] == ' ')
                    s += "%20";
                else s += str[x];
            }
            return s;
        }

        // -------------------------------------------------------------------------------------------
        public string ReplaceDoubleQuotesWithTwoSingleQuotes(string str)
        {
            string s = "";
            int len = str.Length;
            for (int x = 0; x < len; ++x)
            {
                if (str[x] == '"')
                    s += "''";
                else s += str[x];
            }
            return s;
        }

        // -------------------------------------------------------------------------------------------
        public string NewSessionID()
        {
            int ii = new Random().Next(10000000);
            if (ii < 0)
                ii *= -1;

            return Convert.ToString(ii).Substring(3) + IntToStr(ii).Substring(0, 5);
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public string Sanitise(string str)
        {
            return Sanitise(false, str);
        }
        public string Sanitise(bool stripNewlines, string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                switch (str[x])
                {
                    case '#':
                        str2 += "%23";
                        break;
                    case '\"':
                        str2 += "%22";
                        break;
                    case '\'':
                        str2 += "%27";
                        break;
                    case '&':
                        str2 += "%26";
                        break;
                    case '%':
                        str2 += "%25";
                        break;
                    case ' ':
                        str2 += "%20";
                        break;
                    case '?':
                        str2 += "%3f";
                        break;
                    case '+':
                        str2 += "%2b";
                        break;
                    case '\n':
                        if (!stripNewlines) str2 += '\n';
                        break;
                    case '\r':
                        if (!stripNewlines) str2 += '\r';
                        break;
                    default:
                        str2 += str[x];
                        break;
                }
            }
            return str2;
        }

        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DeSanitise(string str)
        {
            string s, str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == '+')
                    str2 += " ";
                else
                if (str[x] == '%')
                {
                    s = str.Substring(x + 1, x + 3);

                    if (s.Equals("23"))
                    {
                        str2 += "#";
                        x += 2;
                    }
                    else
                  if (s.Equals("22"))
                    {
                        str2 += "\"";
                        x += 2;
                    }
                    else
                  if (s.Equals("27"))
                    {
                        str2 += "'";
                        x += 2;
                    }
                    else
                  if (s.Equals("26"))
                    {
                        str2 += "&";
                        x += 2;
                    }
                    else
                  if (s.Equals("25"))
                    {
                        str2 += "%";
                        x += 2;
                    }
                    else
                  if (s.Equals("20"))
                    {
                        str2 += " ";
                        x += 2;
                    }
                    else
                  if (s.Equals("2f", StringComparison.CurrentCultureIgnoreCase))
                    {
                        str2 += "/";
                        x += 2;
                    }
                    else
                  if (s.Equals("3f", StringComparison.CurrentCultureIgnoreCase))
                    {
                        str2 += "?";
                        x += 2;
                    }
                    else
                  if (s.Equals("40"))
                    {
                        str2 += "@";
                        x += 2;
                    }
                    else
                  if (s.Equals("2b"))
                    {
                        str2 += "+";
                        x += 2;
                    }
                    else str2 += "%";
                }
                else str2 += str[x];
            }

            return str2;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DeSanitiseReplaceNewLines(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == (char)3)
                    str2 += "\n";
                else str2 += str[x];
            }

            return str2;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string ReplaceNewlinesByThrees(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == '\n')
                    str2 += @"\3";
                else str2 += str[x];
            }

            return str2;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripNewLines(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == 10 || str[x] == 13)
                {

                }
                else str2 += str[x];
            }

            return str2;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string ReplaceNewLinesByBR(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == 10)
                    str2 += "<br>";
                else
                if (str[x] == 13)
                {
                }
                else str2 += str[x];
            }

            return str2;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string ReplaceNewLinesByN(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == 10)
                    str2 += "*N*";
                else
                if (str[x] == 13)
                {

                }
                else str2 += str[x];
            }

            return str2;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string SanitiseReplacingNewlines(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                switch (str[x])
                {
                    case '#':
                        str2 += "%23";
                        break;
                    case '\"':
                        str2 += "%22";
                        break;
                    case '\'':
                        str2 += "%27";
                        break;
                    case '&':
                        str2 += "%26";
                        break;
                    case '%':
                        str2 += "%25";
                        break;
                    case ' ':
                        str2 += "%20";
                        break;
                    case '?':
                        str2 += "%3f";
                        break;
                    case '+':
                        str2 += "%2b";
                        break;
                    case '\n':
                        str2 += @"\3";
                        break;
                    case '\r':
                        ;
                        break;
                    default:
                        str2 += str[x];
                        break;
                }
            }
            return str2;
        }

        // -----------------------------------------------------------------------------------------------------------------------
        public string Sanitise(char[] b)
        {
            string s = Sanitise(StringFromChars(b));
            StringToChars(s, 0, b);
            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------
        public string SanitiseReplacingNulls(char[] b, int bufLen)
        {
            int highest = bufLen - 1;
            while (b[highest] == '\0')
                --highest;

            string str2 = "";
            for (int x = 0; x <= highest; ++x)
            {
                switch (b[x])
                {
                    case '#':
                        str2 += "%23";
                        break;
                    case '\"':
                        str2 += "%22";
                        break;
                    case '\'':
                        str2 += "%27";
                        break;
                    case '&':
                        str2 += "%26";
                        break;
                    case '%':
                        str2 += "%25";
                        break;
                    case ' ':
                        str2 += "%20";
                        break;
                    case '?':
                        str2 += "%3F";
                        break;
                    case '+':
                        str2 += "%2B";
                        break;
                    case '\0':
                        str2 += (char)1;
                        break;
                    default:
                        str2 += (char)b[x];
                        break;
                }
            }

            return str2;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public string Sanitise2(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                switch (str[x])
                {
                    case '\"':
                        str2 += "\\'\\'";
                        break;
                    case '\'':
                        str2 += "\\'";
                        break;
                    case '\\':
                        str2 += "\\\\";
                        break;
                    default:
                        str2 += str[x];
                        break;
                }
            }
            return str2;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public string Sanitise3(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == '\"') str2 += "\"\"";
                else str2 += str[x];
            }
            return str2;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public string Sanitise4(string str)
        {
            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                switch (str[x])
                {
                    case '\"':
                        str2 += "\\\"";
                        break;
                    case '\'':
                        str2 += "\\\'";
                        break;
                    case '?':
                        str2 += "\\?";
                        break;
                    case '\\':
                        str2 += "\\\\";
                        break;
                    default:
                        str2 += str[x];
                        break;
                }
            }
            return str2;
        }

        // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string SanitiseForSQL(char[] b)
        {
            return SanitiseForSQL(StringFromChars(b));
        }
        public string SanitiseForSQL(string str)
        {
            if (str == null)
                return "";

            string str2 = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == '\'') str2 += "''";
                else str2 += str[x];
            }

            return str2;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string SanitiseForXML(string s)
        {
            char[] b = new char[s.Length + 1];
            StrToChars(b, s);
            return SanitiseForXML(b);
        }
        public string SanitiseForXML(char[] b)
        {
            string str2 = "";
            int x = 0;
            while (b[x] != '\0')
            {
                switch (b[x])
                {
                    case '&':
                        str2 += "&amp;";
                        break;
                    case '"':
                        str2 += "&quot;";
                        break;
                    case '\'':
                        str2 += "&apos;";
                        break;
                    case '<':
                        str2 += "&lt;";
                        break;
                    case '>':
                        str2 += "&gt;";
                        break;
                    default:
                        str2 += (char)b[x];
                        break;
                }

                ++x;
            }

            return str2;
        }

        // -----------------------------------------------------------------------------------------------------------------------
        public string ReplaceNulls(char[] b, int bufLen)
        {
            int highest = bufLen - 1;
            while (b[highest] == '\0')
                --highest;

            string str2 = "";
            for (int x = 0; x <= highest; ++x)
            {
                if (b[x] == '\0') str2 += (char)1;
                else str2 += (char)b[x];
            }
            str2 += (char)1;
            return str2;
        }

        // -----------------------------------------------------------------------------------------------------------------------
        public int ReplaceOnes(string str, char[] buf)
        {
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                buf[x] = str[x] == (char)1 ? '\0' : (char)str[x];
            }
            buf[len] = (char)'\0';
            return len;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string ReplaceNewlinesWithSpaces(string str)
        {
            string s = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == '\n')
                    s += " ";
                else s += str[x];
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string ReplaceBRWithSpaces(string str)
        {
            string s = "";
            int x = 0, len = str.Length;
            while (x < len)
            {
                if (str[x] == '<')
                {
                    if (x < len && str.Substring(x + 1).Equals("br>", StringComparison.CurrentCultureIgnoreCase))
                    {
                        s += " ";
                        x += 3;
                    }
                    else s += str[x++];
                }
                else s += str[x++];
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripHTMLAndsanitise(string str)
        {
            string s = "";
            int x = 0, len = str.Length;
            while (x < len)
            {
                if (str[x] == '<')
                {
                    ++x;
                    while (x < len && str[x] != '>')
                        ++x;
                    ++x;
                }
                else
                if (str[x] == '[' && (x < (len + 1) && str[x + 1] == '['))
                {
                    x += 2;
                    while (x < (len + 1) && str[x] != ']' && str[x + 1] != ']')
                        ++x;
                    x += 2;
                }
                else s += str[x++];
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripHTML(string str)
        {
            string s = "";
            int x = 0, len = str.Length;
            while (x < len)
            {
                if (str[x] == '<')
                {
                    ++x;
                    while (x < len && str[x] != '>')
                        ++x;
                    ++x;
                }
                else s += str[x++];
            }

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public int ReplaceOnesWithTwos(string str, char[] buf)
        {
            int x, y = 0, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x].Equals(@"\1"))
                {
                    buf[x] = (@"\\2")[0];
                    ++y;
                }
                else
                {
                    buf[x] = (char)str[x];
                    ++y;
                }
            }

            buf[y] = (char)'\0';

            return y;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public int ReplaceThreesWithNewlines(string str, char[] buf)
        {
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                buf[x] = str[x] == (char)3 ? '\n' : (char)str[x];
            }
            buf[len] = (char)'\0';
            return len;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public string ReplaceThreesWithSpaces(string str)
        {
            string s = "";
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == (char)3) s += " ";
                else s += str[x];
            }
            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripAllOnes(string str)
        {
            int len = str.Length;
            string s = "";

            for (int x = 0; x < len; ++x)
            {
                if (str[x] != (char)1)
                    s += str[x];
            }

            return s;
        }

        // -----------------------------------------------------------------------------------------------------------------------
        public int ReplaceTwosWithOnes(char[] buf)
        {
            int x = 0;
            while (buf[x] != '\0')
            {
                if (buf[x] == (char)2)
                    buf[x] = (char)1;
                ++x;
            }
            return x;
        }

        // -----------------------------------------------------------------------------------------------------------------------
        public int ReplaceTwosWithNulls(char[] buf)
        {
            int x = 0;
            while (buf[x] != '\0')
            {
                if (buf[x] == (char)2) buf[x] = '\0';
                ++x;
            }
            return x;
        }

        // -----------------------------------------------------------------------------------------------------------------------
        public string SanitiseNumeric(string str)
        {
            string str2 = "";
            bool oneDone = false;
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] == '.')
                {
                    if (!oneDone)
                    {
                        str2 += ".";
                        oneDone = true;
                    }
                }
                else if (str[x] >= '0' && str[x] <= '9') str2 += str[x];
            }
            return str2;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public string HandleSuperScripts(string str)
        {
            string s = HandleSuperScripts(str, "(TM)");
            return HandleSuperScripts(s, "(R)");
        }
        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        string HandleSuperScripts(string str, string what)
        {
            string s = "", t;
            int x, len, offset = 0;
            while (offset != -1)
            {
                offset = str.IndexOf(what, offset, StringComparison.CurrentCultureIgnoreCase);
                if (offset != -1)
                {
                    s = "";
                    x = 0;
                    while (x < offset)
                        s += str[x++];
                    t = "<sup><font size='1'>" + what + "</font></sup>";
                    s += t;
                    len = str.Length;
                    x += what.Length;
                    while (x < len)
                        s += str[x++];
                    offset += t.Length;
                    str = s;
                }
            }
            if (s.Length == 0)
                s = str;

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsASCII(string str)
        {
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                if (str[x] >= 32 && str[x] <= 126)
                {

                }
                else return false;
            }

            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsASCII(char ch)
        {
            return ch >= 32 && ch <= 126 ? true : false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsNumericIP(string str)
        {
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                switch (str[x])
                {
                    case '.':
                    case 'h':
                    case 't':
                    case 'p':
                    case ':':
                    case '/':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '0': break;
                    default: return false;
                }
            }
            return true;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsNumeric(char[] b)
        {
            return IsNumeric(StringFromChars(b));
        }
        public bool IsNumeric(string str)
        {
            int dotCount = 0;
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                switch (str[x])
                {
                    case '.':
                        ++dotCount;
                        if (dotCount > 1) return false;
                        break;
                    case '-':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '0': break;
                    default: return false;
                }
            }
            return true;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsInteger(char[] b)
        {
            return IsInteger(StringFromChars(b));
        }
        public bool IsInteger(string str)
        {
            int x, len = str.Length;
            for (x = 0; x < len; ++x)
            {
                switch (str[x])
                {
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '0': break;
                    default: return false;
                }
            }
            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public FileStream FileOpen(string fullName)
        {
            FileStream fs = new FileStream(fullName, FileMode.Open);
            return fs;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public FileStream FileOpenD(string fileName, string dirName)
        {
            FileStream fs = new FileStream(dirName + fileName, FileMode.Open);
            return fs;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public void FileClose()
        {
        }


        // -------------------------------------------------------------------------------------------------------------------------------
        // Prepends filename and '.' to each fldname
        public int BuildFieldNamesInBuf(string fieldNames, string fileName, char[] buf)
        {
            int count = 0;

            try
            {
                int x = 0, y = 0, z, len = fieldNames.Length;
                int lenFileName = fileName.Length;

                while (x < len)
                {
                    for (z = 0; z < lenFileName; ++z)
                        buf[y++] = (char)fileName[z];
                    buf[y++] = '.';

                    while (x < len && fieldNames[x] != ',')
                        buf[y++] = (char)fieldNames[x++];

                    ++x;
                    while (x < len && fieldNames[x] == ' ')
                        ++x;

                    buf[y++] = '\0';
                    ++count;
                }
            }
            catch (Exception)
            {
                buf[0] = '\0';
                count = 0;
            }

            return count;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool CreateDir(string dirName)
        {
            return CreateDir(dirName, false);
        }
        public bool CreateDir(string dirName, bool createMissing)
        {
            System.IO.Directory.CreateDirectory(dirName);
            return true;
        }

        public bool CreateDir(string dirName, string permissions)
        {
            System.IO.Directory.CreateDirectory(dirName);
            DirectoryInfo dInfo = new DirectoryInfo(dirName);

            // TODO set permissions

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public FileStream Create(string fName)
        {
            try
            {
                File.Delete(fName);

                FileStream fs = File.Create(fName);

                return fs;
            }
            catch (Exception ioErr)
            {
                Console.WriteLine("GeneralUtils:create: " + ioErr);
                return null;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool FileDelete(string fullName)
        {
            try
            {
                File.Delete(fullName);
                return true;
            }
            catch (Exception ioErr)
            {
                Console.WriteLine("generalUtils fileDelete: " + ioErr);
                return false;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool DirectoryDelete(string dirName)
        {
            try
            {
                Directory.Delete(dirName);
                return true;
            }
            catch (Exception ioErr)
            {
                Console.WriteLine("generalUtils: dir delete: " + ioErr);
                return false;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void DirectoryHierarchyDelete(string directory)
        {
            DirectoryHierarchyDelete2(directory);
            DirectoryDelete(directory);
        }
        public void DirectoryHierarchyDelete2(string directory)
        {
            try
            {
                System.IO.DriveInfo di = new System.IO.DriveInfo(directory);
                System.IO.DirectoryInfo dirInfo = di.RootDirectory;

                System.IO.FileInfo[] fileNames = dirInfo.GetFiles(directory);


                foreach (System.IO.FileInfo fi in fileNames)
                {
                    if (!IsDirectory(directory + "/" + fi.Name))
                        FileDelete(directory + "/" + fi.Name);
                    else
                    {
                        DirectoryHierarchyDelete2(directory + "/" + fi.Name + "/");
                        DirectoryDelete(directory + fi.Name);
                    }
                }
            }
            catch (Exception e) { Console.WriteLine(e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void FilesDeleteGivenPrefix(string directory, string prefix)
        {
            try
            {
                System.IO.DriveInfo di = new System.IO.DriveInfo(directory);
                System.IO.DirectoryInfo dirInfo = di.RootDirectory;

                System.IO.FileInfo[] fileNames = dirInfo.GetFiles(directory);


                foreach (System.IO.FileInfo fi in fileNames)
                {
                    if (!IsDirectory(directory + "/" + fi.Name))
                        if (fi.Name.StartsWith(prefix, StringComparison.CurrentCultureIgnoreCase))
                            FileDelete(directory + "/" + fi.Name);

                }
            }
            catch (Exception e) { Console.WriteLine(e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool FileDeleteD(string fName, string dirName)
        {
            File.Delete(dirName + fName);

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string NewRandom()
        {
            double iii = new Random().NextDouble();

            return Convert.ToString(iii * 100000);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool FileDeleteAndRenameD(string oldName, string newName, string dirName)
        {
            File.Replace(dirName + oldName, dirName + newName, dirName + newName + ".bak");

            return true;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool IsDirectory(string name)
        {
            try
            {
                if (Directory.Exists(name))
                    return true;
            }
            catch (Exception)
            {
                return false;
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        // Deletes the inx with the number in extns
        // Note: If the number 0 is specified then the file .CFS is deleted
        //       extns is terminated by -1
        public void DeleteFiles(string fName, string dirName, short[] extns)
        {
            string extn;

            short x = 0;
            while (extns[x] >= 0)
            {
                if (extns[x] == 0)
                    extn = ".cfs";
                else
                {
                    extn = ".";
                    if (extns[x] < 10)
                        extn += "0";
                    if (extns[x] < 100)
                        extn += "0";
                    extn += extns[x];
                }

                FileDeleteD((fName + extn), dirName);
                ++x;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool FileExists(string pathName)
        {
            return File.Exists(pathName);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void CopyBetweenDirectories(string fromDirectory, string toDirectory)
        {
            try
            {
                string fileName, destFile;
                if (System.IO.Directory.Exists(fromDirectory))
                {
                    string[] files = System.IO.Directory.GetFiles(fromDirectory);

                    foreach (string s in files)
                    {
                        fileName = System.IO.Path.GetFileName(s);
                        destFile = System.IO.Path.Combine(toDirectory, fileName);
                        System.IO.File.Copy(s, destFile, true);
                    }
                }
                else
                {
                    Console.WriteLine("CopyBetweenDirectories: Path does not exist");
                }
            }
            catch (Exception e) { Console.WriteLine(e); }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void CopyFileToFile(string fromFullFileName, string toFullFileName)
        {
            System.IO.File.Copy(fromFullFileName, toFullFileName, true);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // format is 'S' for short: "95"
        //           'L' for long: "1995"
        public string Decode(int date, string localDefnsDir, string defnsDir)
        {
            char[] dateBuf = new char[20];
            Decode(date, dateBuf, localDefnsDir, defnsDir);
            return StringFromChars(dateBuf);
        }
        public short Decode(int date, char[] dateBuf, string localDefnsDir, string defnsDir)
        {
            short first, second;
            short[]
            dd = new short[1];
            short[] mm = new short[1];
            short[] yyyy = new short[1];

            char format = GetDateLength(localDefnsDir, defnsDir);
            char dateSeparator = GetDateSeparator(localDefnsDir, defnsDir);
            short layout = GetDateFormat(localDefnsDir, defnsDir);
            short century = GetDateCentury(localDefnsDir, defnsDir);

            if (date == 0 || date == 1)
            {
                dateBuf[0] = '\0';
                return ((short)0);
            }

            AnalyzeNumOfDays(date, dd, mm, yyyy);

            switch (layout)
            {
                case 1: // mm dd yy
                    first = mm[0]; second = dd[0]; break;
                default: // dd mm yy
                    first = dd[0]; second = mm[0]; break;
            }

            int p = 0;
            if (first < 10)
                dateBuf[p++] = '0';

            string s = Convert.ToString(first);
            for (int i = 0; i < s.Length; ++i)
                dateBuf[p++] = (char)s[i];
            dateBuf[p++] = (char)dateSeparator;

            if (second < 10)
                dateBuf[p++] = '0';

            s = Convert.ToString(second);
            for (int i = 0; i < s.Length; ++i)
                dateBuf[p++] = (char)s[i];
            dateBuf[p++] = (char)dateSeparator;

            // determine century of this date
            int x = yyyy[0] / 100;

            if (format == 'L' || x != century)
            {
                if (yyyy[0] <= 9)
                {
                    dateBuf[p++] = '0';
                    dateBuf[p++] = '0';
                    dateBuf[p++] = '0';
                }
                else
                if (yyyy[0] <= 99)
                {
                    dateBuf[p++] = '0';
                    dateBuf[p++] = '0';
                }
                else
                if (yyyy[0] <= 999)
                    dateBuf[p++] = '0';

                s = Convert.ToString(yyyy[0]);
                for (int i = 0; i < s.Length; ++i)
                    dateBuf[p++] = (char)s[i];
                dateBuf[p] = '\0';
            }
            else // format == 'S'
            {
                x = yyyy[0] / 100;
                yyyy[0] -= (short)(x * 100);
                if (yyyy[0] <= 9)
                {
                    dateBuf[p++] = '0';
                    s = Convert.ToString(yyyy[0]);
                    for (int i = 0; i < s.Length; ++i)
                        dateBuf[p++] = (char)s[i];
                    dateBuf[p] = '\0';
                }
                else
                  if (yyyy[0] <= 99)
                {
                    s = Convert.ToString(yyyy[0]);
                    for (int i = 0; i < s.Length; ++i)
                        dateBuf[p++] = (char)s[i];
                    dateBuf[p] = '\0';
                }
                else
                    if (yyyy[0] <= 999)
                {
                    s = Convert.ToString(yyyy[0] / 10);
                    for (int i = 0; i < s.Length; ++i)
                        dateBuf[p++] = (char)s[i];
                    dateBuf[p] = '\0';
                }
                else
                {
                    s = Convert.ToString(yyyy[0] / 100);
                    for (int i = 0; i < s.Length; ++i)
                        dateBuf[p++] = (char)s[i];
                    dateBuf[p] = '\0';
                }
            }
            return ((short)p);
        }

        // -------------------------------------------------------------------------------------------
        // if yy is 2 chars iCentury is used IF cFormat is 'S'
        public int Encode(string dateStr, string localDefnsDir, string defnsDir)
        {
            char[]
            str = new char[20];
            StringToChars(dateStr, 0, str);
            return Encode(str, localDefnsDir, defnsDir);
        }
        public int Encode(char[] dateStr, string localDefnsDir, string defnsDir)
        {
            short dd, mm, yyyy;
            char[]
            str = new char[20];

            int len = Lengthchars(dateStr, 0);

            if (len < 6)
                return 1; // illegal date

            if (dateStr[0] == '\0')
                return (1);  // no date supplied (illegal date)

            char format = GetDateLength(localDefnsDir, defnsDir);
            char dateSeparator = GetDateSeparator(localDefnsDir, defnsDir);
            short layout = GetDateFormat(localDefnsDir, defnsDir);
            short century = GetDateCentury(localDefnsDir, defnsDir);

            int x = 0;
            while (x < len && dateStr[x] != '\0')
            {
                if (dateStr[x] != dateSeparator && (dateStr[x] < '0' || dateStr[x] > '9'))
                    return (1);  // illegal date
                ++x;
            }

            x = 0; int y = 0;
            while (x < len && dateStr[x] != dateSeparator && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';
            short first = (short)IntFromCharsCharFormat(str, (short)0);

            ++x; y = 0;
            while (x < len && dateStr[x] != dateSeparator && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';
            short second = (short)IntFromCharsCharFormat(str, (short)0);

            ++x; y = 0;
            while (x < len && dateStr[x] != '\0' && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';
            yyyy = (short)IntFromCharsCharFormat(str, (short)0);

            if (yyyy <= 99)
                if (format == 'S')
                    yyyy += (short)(century * 100);

            switch (layout)
            {
                case 1: // mm dd yy
                    mm = first; dd = second; break;
                default: // dd mm yy
                    dd = first; mm = second; break;
            }

            return NumOfDaysTotal(dd, mm, yyyy);
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        public int EncodeFromYYYYMMDD(string date)
        {
            if (date.Equals("1970-01-01") || date.Equals("19700101"))
                return 0;

            int i = date.IndexOf("-", StringComparison.CurrentCulture);

            if (i == -1)
            {
                date = date.Substring(0, 4) + "-" + date.Substring(4, 6) + "-" + date.Substring(6);
                i = 4;
            }

            int yyyy = StrToInt(date.Substring(0, i));

            ++i;
            int j = date.Substring(i).IndexOf("-", StringComparison.CurrentCulture);

            int mm = StrToInt(date.Substring(i, (i + j)));

            ++j;
            int dd = StrToInt(date.Substring(i + j));

            return NumOfDaysTotal((short)dd, (short)mm, (short)yyyy);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string DecodeToYYYYMMDD(int date)
        {
            short[]
            dd = new short[1];
            short[] mm = new short[1];
            short[] yyyy = new short[1];

            AnalyzeNumOfDays(date, dd, mm, yyyy);

            string s = yyyy[0] + "-";

            if (mm[0] < 10)
                s += "0";
            s += (mm[0] + "-");

            if (dd[0] < 10)
                s += "0";
            s += dd[0];

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        public string SubtractNumDaysFromDate(string date, int numDays)
        {
            int x = EncodeFromYYYYMMDD(date);
            return DecodeToYYYYMMDD(x + numDays);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        int NumOfDaysTotal(short dd, short mm, short yyyy)
        {
            int total = 0;

            if (yyyy > 0)
            {
                for (short x = 0; x < yyyy; ++x)
                    total += NumOfDaysInYear(x);
            }

            for (short x = 1; x < mm; ++x)
                total += NumOfDaysInMonth(x, yyyy);

            total += dd;

            return total;
        }

        // -------------------------------------------------------------------------------------------
        public short NumOfDaysInMonth(short mm, short yyyy)
        {
            return mm == 4 || mm == 6 || mm == 9 || mm == 11 ? (short)30 : mm == 2 ? IsLeapYear(yyyy) ? (short)29 : (short)28 : (short)31;
        }

        // -------------------------------------------------------------------------------------------
        void AnalyzeNumOfDays(int date, short[] dd, short[] mm, short[] yyyy)
        {
            yyyy[0] = 0;
            while (date > 0)
            {
                date -= NumOfDaysInYear(yyyy[0]);
                ++yyyy[0];
            }
            --yyyy[0];
            date += NumOfDaysInYear(yyyy[0]);

            short days = (short)date;

            if (days <= 31) { mm[0] = 1; dd[0] = days; return; }
            days -= 31;

            if (IsLeapYear(yyyy[0]))
            {
                if (days <= 29) { mm[0] = 2; dd[0] = days; return; }
                days -= 29;
            }
            else
            {
                if (days <= 28) { mm[0] = 2; dd[0] = days; return; }
                days -= 28;
            }

            if (days <= 31) { mm[0] = 3; dd[0] = days; return; }
            days -= 31;

            if (days <= 30) { mm[0] = 4; dd[0] = days; return; }
            days -= 30;

            if (days <= 31) { mm[0] = 5; dd[0] = days; return; }
            days -= 31;

            if (days <= 30) { mm[0] = 6; dd[0] = days; return; }
            days -= 30;

            if (days <= 31) { mm[0] = 7; dd[0] = days; return; }
            days -= 31;

            if (days <= 31) { mm[0] = 8; dd[0] = days; return; }
            days -= 31;

            if (days <= 30) { mm[0] = 9; dd[0] = days; return; }
            days -= 30;

            if (days <= 31) { mm[0] = 10; dd[0] = days; return; }
            days -= 31;

            if (days <= 30) { mm[0] = 11; dd[0] = days; return; }
            days -= 30;

            mm[0] = 12; dd[0] = days;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public short NumOfDaysInYear(short yyyy)
        {
            return IsLeapYear(yyyy) ? (short)366 : (short)365;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        bool IsLeapYear(short yyyy)
        {
            if (yyyy < (short)50)
                yyyy += (short)2000;
            else
            if (yyyy >= (short)50 && yyyy < (short)100)
                yyyy += (short)1900;

            if (yyyy == 2000) return true;

            short x = (short)(yyyy / 4);
            if ((x * 4) != yyyy)
                return false;
            x = (short)(yyyy / 1000);
            return (x * 1000) != yyyy;
        }

        // -------------------------------------------------------------------------------------------
        public int TodayEncoded(string localDefnsDir, string defnsDir)
        {
            char[] b;

            string t = Today(localDefnsDir, defnsDir);

            b = GetChars(t);
            return Encode(b, localDefnsDir, defnsDir);
        }

        // -------------------------------------------------------------------------------------------
        public string Today(string localDefnsDir, string defnsDir)
        {
            int first = 0, second = 0, dd, mm, yy;
            char format, dateSeparator;
            short layout;

            format = GetDateLength(localDefnsDir, defnsDir);
            dateSeparator = GetDateSeparator(localDefnsDir, defnsDir);
            layout = GetDateFormat(localDefnsDir, defnsDir);


            DateTime dt = new DateTime();
            dd = dt.Day;
            mm = dt.Month;
            yy = dt.Year;

            switch (layout)
            {
                case 0: // dd mm yy
                    first = dd; second = mm; break;
                case 1: // mm dd yy
                    first = mm; second = dd; break;
            }

            string str = "";
            if (first < 10)
                str = "0";

            str += first;
            str += dateSeparator;

            if (second < 10)
                str += "0";

            str += second;
            str += dateSeparator;

            string year = "" + yy;
            if (format == 'S')
                year = year.Substring(2);

            str += year;

            return str;
        }

        // -------------------------------------------------------------------------------------------
        public void Today(string localDefnsDir, string defnsDir, char[] date)
        {
            StringToChars(Today(localDefnsDir, defnsDir), 0, date);
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        public void TodaySQLFormat(string localDefnsDir, string defnsDir, char[] date)
        {
            Today(localDefnsDir, defnsDir, date);
            ConvertToYYYYMMDD(date);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string TodaySQLFormat(string localDefnsDir, string defnsDir)
        {
            char[] date = new char[20];
            Today(localDefnsDir, defnsDir, date);
            ConvertToYYYYMMDD(date);
            return StringFromChars(date);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public short GetMonth(int date)
        {
            short[]
            dd = new short[1];
            short[] mm = new short[1];
            short[] yy = new short[1];

            AnalyzeNumOfDays(date, dd, mm, yy);
            return mm[0];
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public short GetYear(int date)
        {
            short[]
            dd = new short[1];
            short[] mm = new short[1];
            short[] yy = new short[1];

            AnalyzeNumOfDays(date, dd, mm, yy);
            return yy[0];
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public short GetDay(int date)
        {
            short[]
            dd = new short[1];
            short[] mm = new short[1];
            short[] yy = new short[1];

            AnalyzeNumOfDays(date, dd, mm, yy);
            return dd[0];
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetDayOfWeek(string date, int[] dayOfWeek, string localDefnsDir, string defnsDir)
        {
            int dd = 0, mm = 0, yy;

            char separator = GetDateSeparator(localDefnsDir, defnsDir);
            int century = GetDateCentury(localDefnsDir, defnsDir);
            int layout = GetDateFormat(localDefnsDir, defnsDir);

            short x = 0;

            string str = "";
            while (x < date.Length && date[x] != separator && date[x] != '\0') // just-in-case
                str += date[x++];

            if (layout == 0)
                dd = StrToInt(str);
            else mm = StrToInt(str);
            ++x; // separator

            str = "";
            while (x < date.Length && date[x] != separator && date[x] != '\0') // just-in-case
                str += date[x++];

            if (layout == 0)
                mm = StrToInt(str);
            else dd = StrToInt(str);
            ++x; // separator

            --mm;

            str = "";
            while (x < date.Length && date[x] != '\0')
                str += date[x++];

            yy = StrToInt(str);
            if (yy > 100)
                yy -= (century * 100);

            dayOfWeek[0] = (int)new DateTime(yy, mm, dd).DayOfWeek;

            switch (dayOfWeek[0])
            {
                case 1: str = "Sunday"; break;
                case 2: str = "Monday"; break;
                case 3: str = "Tuesday"; break;
                case 4: str = "Wednesday"; break;
                case 5: str = "Thursday"; break;
                case 6: str = "Friday"; break;
                case 7: str = "Saturday"; break;
            }

            return (str);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Format of monthYear is "July 1996"
        // Rtns: month as 1 to 12 (0 if no match - jic)
        //       year as long format (e.g., 1996)
        public void MonthYearStrToYearAndMonth(string monthYear, int[] month, int[] year)
        {
            if (monthYear.StartsWith("January", StringComparison.CurrentCultureIgnoreCase)) month[0] = 1;
            else
            if (monthYear.StartsWith("February", StringComparison.CurrentCultureIgnoreCase)) month[0] = 2;
            else
            if (monthYear.StartsWith("March", StringComparison.CurrentCultureIgnoreCase)) month[0] = 3;
            else
            if (monthYear.StartsWith("April", StringComparison.CurrentCultureIgnoreCase)) month[0] = 4;
            else
            if (monthYear.StartsWith("May", StringComparison.CurrentCultureIgnoreCase)) month[0] = 5;
            else
            if (monthYear.StartsWith("June", StringComparison.CurrentCultureIgnoreCase)) month[0] = 6;
            else
            if (monthYear.StartsWith("July", StringComparison.CurrentCultureIgnoreCase)) month[0] = 7;
            else
            if (monthYear.StartsWith("August", StringComparison.CurrentCultureIgnoreCase)) month[0] = 8;
            else
            if (monthYear.StartsWith("September", StringComparison.CurrentCultureIgnoreCase)) month[0] = 9;
            else
                month[0] = monthYear.StartsWith("October", StringComparison.CurrentCultureIgnoreCase)
                ? 10
                : monthYear.StartsWith("November", StringComparison.CurrentCultureIgnoreCase)
        ? 11
        : monthYear.StartsWith("December", StringComparison.CurrentCultureIgnoreCase) ? 12 : 0;

            year[0] = StrToInt(monthYear.Substring(monthYear.IndexOf(" ", StringComparison.CurrentCulture) + 1));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Format of monthYear is "7-1996"
        // Rtns: month as 1 to 12 (0 if no match - jic)
        //       year as long format (e.g., 1996)
        public void MonthYearStrToYearAndMonth2(string monthYear, int[] month, int[] year)
        {
            int len = monthYear.Length;
            int x = 0;
            string m = "";
            while (x < len && monthYear[x] != '-')
                m += monthYear[x++];

            month[0] = StrToInt(m);

            year[0] = StrToInt(monthYear.Substring(monthYear.IndexOf("-", StringComparison.CurrentCulture) + 1));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Month is 1 to 12, Year is long format (e.g., 1996)
        // Rtns: string as "July 1996"
        public string YearAndMonthToMonthYearStr(int month, int year)
        {
            string s = "";

            switch (month)
            {
                case 1: s = "January"; break;
                case 2: s = "February"; break;
                case 3: s = "March"; break;
                case 4: s = "April"; break;
                case 5: s = "May"; break;
                case 6: s = "June"; break;
                case 7: s = "July"; break;
                case 8: s = "August"; break;
                case 9: s = "September"; break;
                case 10: s = "October"; break;
                case 11: s = "November"; break;
                case 12: s = "December"; break;
            }

            return year < 10 ? s + " 0" + year : s + " " + year;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // monthYear is "5-2007"
        // Rtns: string as "July 1996"
        public string MonthYearToMonthYearStr(string monthYear)
        {
            if (monthYear.Equals("ALL", StringComparison.CurrentCultureIgnoreCase))
                return "Start to Finish";

            string month = monthYear.Substring(0, monthYear.IndexOf("-", StringComparison.CurrentCulture));
            string year = monthYear.Substring(monthYear.IndexOf("-", StringComparison.CurrentCulture) + 1);

            string s = "";

            if (month.Equals("1")) s = "January";
            else if (month.Equals("2")) s = "February";
            else if (month.Equals("3")) s = "March";
            else if (month.Equals("4")) s = "April";
            else if (month.Equals("5")) s = "May";
            else if (month.Equals("6")) s = "June";
            else if (month.Equals("7")) s = "July";
            else if (month.Equals("8")) s = "August";
            else if (month.Equals("9")) s = "September";
            else if (month.Equals("10")) s = "October";
            else if (month.Equals("11")) s = "November";
            else if (month.Equals("12")) s = "December";

            return (s + " " + year);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Tue Jul 5 10:48:11 2005
        public string TimeStamp(string localDefnsDir, string defnsDir)
        {
            string today = Today(localDefnsDir, defnsDir);

            string year = "";
            year += today[6];
            year += today[7];
            int yy = StrToInt(year);

            string str = "";
            str += today[3];
            str += today[4];
            int mm = StrToInt(str);

            str = "";
            str += today[0];
            str += today[1];
            int dd = StrToInt(str);

            string month = "";
            switch (mm)
            {
                case 1: month = "Jan"; break;
                case 2: month = "Feb"; break;
                case 3: month = "Mar"; break;
                case 4: month = "Apr"; break;
                case 5: month = "May"; break;
                case 6: month = "Jun"; break;
                case 7: month = "Jul"; break;
                case 8: month = "Aug"; break;
                case 9: month = "Sep"; break;
                case 10: month = "Oct"; break;
                case 11: month = "Nov"; break;
                case 12: month = "Dec"; break;
            }

            if (yy >= 00 && yy <= 50)
                yy += 2000;

            int i = (int)new DateTime(yy, mm, dd).DayOfWeek;

            string day = "";
            switch (i)
            {
                case 1: day = "Sun"; break;
                case 2: day = "Mon"; break;
                case 3: day = "Tue"; break;
                case 4: day = "Wed"; break;
                case 5: day = "Thu"; break;
                case 6: day = "Fri"; break;
                case 7: day = "Sat"; break;
            }

            string time = TimeNow(4, "");

            // Tue Jul 5 10:48:11 2005
            return day + " " + month + " " + dd + " " + time[0] + time[1] + ":" + time[2] + time[3] + ":" + time[4] + time[5] + " " + yy;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public string YymmddExpandGivenSQLFormat(bool wantDay, string yyyymmdd)
        {
            string s = "" + yyyymmdd[2] + yyyymmdd[3] + yyyymmdd[5] + yyyymmdd[6] + yyyymmdd[8] + yyyymmdd[9];
            return YymmddExpand(wantDay, s);
        }

        // Format of yymmdd is "970131"
        // Rtns: Wednesday, 31 January 97
        //   or, 31 January 97
        public string YymmddExpand(bool wantDay, string yymmdd)
        {
            string str = "";
            str += yymmdd[0];
            str += yymmdd[1];
            int yy = StrToInt(str);

            str = "";
            str += yymmdd[2];
            str += yymmdd[3];
            int mm = StrToInt(str);

            str = "";
            str += yymmdd[4];
            str += yymmdd[5];

            string str2 = str + " " + YearAndMonthToMonthYearStr(mm, yy);

            if (wantDay)
            {
                int dd = StrToInt(str);

                if (yy >= 00 && yy <= 50)
                    yy += 2000;

                int i = (int)new DateTime(yy, mm, dd).DayOfWeek;
                switch (i)
                {
                    case 1: str2 = "Sunday, " + str2; break;
                    case 2: str2 = "Monday, " + str2; break;
                    case 3: str2 = "Tuesday, " + str2; break;
                    case 4: str2 = "Wednesday, " + str2; break;
                    case 5: str2 = "Thursday, " + str2; break;
                    case 6: str2 = "Friday, " + str2; break;
                    case 7: str2 = "Saturday, " + str2; break;
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
        public void TimeNow(int format, string separatorChar, char[] time)
        {
            StrToChars(time, TimeNow(format, separatorChar));
        }
        public string TimeNow(int format, string separatorChar)
        {
            DateTime dt = DateTime.Now;
            int hour = dt.Hour;

            string s = "";
            switch (format)
            {
                case 1:
                    if (hour == 0)
                        s = "12";
                    else s += hour.ToString();

                    s += separatorChar;
                    break;
                case 2:
                case 3:
                case 5:
                    s = IntToStr(hour);
                    if (format == 3 || format == 5)
                        s += separatorChar;
                    break;
                case 4:
                    s += hour.ToString();
                    break;
                default:
                    if (hour < 10)
                        s += "0";

                    s += hour.ToString();
                    break;
            }

            s += separatorChar;


            int i2 = dt.Minute;
            if (i2 < 10)
                s += "0";

            s += i2.ToString();

            if (format == 1)
            {
                if (hour <= 12)
                    s += " AM";
                else s += " PM";
            }

            if (format == 5)
                s += separatorChar;

            if (format == 4 || format == 5)
            {
                int i3 = dt.Second;
                if (i3 < 10)
                    s += "0";
                s += i3.ToString();
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void TimeNowSQLFormat(char[] time)
        {
            TimeNow(4, "", time);
            ConvertToHHMMSS(time, Lengthchars(time, 0));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string TimeNowSQLFormat()
        {
            char[] time = new char[10];
            TimeNow(4, "", time);
            ConvertToHHMMSS(time, Lengthchars(time, 0));
            return StringFromChars(time);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int TimeNowInSecs()
        {
            // Get time now (as HH MM SS) and convert to a second-count

            DateTime dt = DateTime.Now;
            int hour = dt.Hour;

            int secs = hour * 3600;

            if (hour > 12)
                hour += 43200; // 12 hours of seconds

            int i2 = dt.Minute;
            secs += (i2 * 60);

            secs += dt.Second;

            return secs;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Convert time (as "HH:MM:SS") to a second-count
        public long ConvertTimeToSecs(string timeStr)
        {
            string s = "";
            int x = 0;
            int len = timeStr.Length;
            while (x < len && timeStr[x] != ':')
                s += timeStr[x++];
            long secs = StrToInt(s) * 3600;

            ++x;
            s = "";
            while (x < len && timeStr[x] != ':')
                s += timeStr[x++];
            secs += (StrToInt(s) * 60);

            ++x;
            s = "";
            while (x < len)
                s += timeStr[x++];
            secs += StrToInt(s);

            return secs;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string ConvertSecsToHoursMinsAndSecs(int secs)
        {
            int hours = 0, i = secs;

            while (i >= 3600)
            {
                ++hours;
                i -= 3600;
            }

            i = secs - (hours * 3600);
            int mins = 0;

            while (i >= 60)
            {
                ++mins;
                i -= 60;
            }

            return hours + "h " + mins + "m " + (secs - (hours * 3600) - (mins * 60)) + "s";
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // format == 1 for "3:06 PM"
        //           3 for "15:06"
        public void DecodeTime(int format, string timeStr, string separatorChar, char[] time)
        {
            StrToChars(time, DecodeTime(format, timeStr, separatorChar));
        }
        public string DecodeTime(int format, string timeStr, string separatorChar)
        {
            string hours, mins;

            if (timeStr.Length == 0) // just-in-case
                return "00:00";
            switch (timeStr.Length)
            {
                case 1:
                    hours = format == 1 ? "0" : "00";

                    mins = "0" + timeStr[0];
                    break;
                case 2:
                    hours = format == 1 ? "0" : "00";

                    mins = "" + timeStr[1];
                    break;
                case 3:
                    hours = "0" + timeStr[0];
                    mins = timeStr.Substring(1);
                    break;
                default:
                    hours = timeStr.Substring(0, 2);
                    mins = timeStr.Substring(2);
                    break;
            }

            string s = format == 1 ? hours + separatorChar + mins : hours + mins;
            if (format == 1)
            {
                if (IntFromStr(timeStr) < 1200)
                    s += " AM";
                else s += " PM";
            }

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // rtns the date of the first day of the month-before that of the given date
        public void PreviousMonthStart(char[] theDate, char[] previous, string localDefnsDir, string defnsDir)
        {
            int date = Encode(theDate, localDefnsDir, defnsDir);
            short dd = GetDay(date);
            short mm = GetMonth(date);
            short yyyy = GetYear(date);

            if (mm == 1)
            {
                mm = 12;
                --yyyy;
            }
            else --mm;

            short numPrevMM = NumOfDaysInMonth(mm, yyyy);
            date -= (dd - 1);
            date -= numPrevMM;
            Decode(date, previous, localDefnsDir, defnsDir);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // rtns the date of the first day of the month-before that of the given date
        public string PreviousMonthStartYYYYMMDD(string theDate)
        {
            int date = EncodeFromYYYYMMDD(theDate);
            short dd = GetDay(date);
            short mm = GetMonth(date);
            short yyyy = GetYear(date);

            if (mm == 1)
            {
                mm = 12;
                --yyyy;
            }
            else --mm;

            short numPrevMM = NumOfDaysInMonth(mm, yyyy);
            date -= (dd - 1);
            date -= numPrevMM;

            return DecodeToYYYYMMDD(date);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // rtns the date of the last day of the month-before that of the given date
        public void PreviousMonthEnd(char[] date, char[] previous, string localDefnsDir, string defnsDir)
        {
            char[] start = new char[20];

            PreviousMonthStart(date, start, localDefnsDir, defnsDir);
            string lastDayOfPreviousMonth = LastDayOfMonth(StringFromChars(start));
            StrToChars(previous, lastDayOfPreviousMonth);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // rtns the date of the first day of the current month for the given date
        public string ThisMonthStart(string theDate)
        {
            int x = 0, len = theDate.Length;
            while (x < len && theDate[x] != '.')
                ++x;

            string s = "01";
            s += theDate.Substring(x);

            return s;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string LastDayOfMonth(string dateStart)
        {
            int x = 0, len = dateStart.Length;
            while (x < len && dateStart[x] != '.')
                ++x;

            int y = x + 1;
            string month = "";
            while (y < len && dateStart[y] != '.')
                month += dateStart[y++];

            ++y;
            string year = "";
            while (y < len && dateStart[y] != '.')
                year += dateStart[y++];

            short lastDay = NumOfDaysInMonth((short)IntFromStr(month), (short)IntFromStr(year));

            return lastDay + dateStart.Substring(x);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string FirstDayOfMonthYYYYMMDD(string dateEnd)
        {
            int x = 0, len = dateEnd.Length;
            string year = "";
            while (x < len && dateEnd[x] != '-')
                year += dateEnd[x++];

            ++x;
            string month = "";
            while (x < len && dateEnd[x] != '-')
                month += dateEnd[x++];

            return year + "-" + month + "-01";
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string LastDayOfMonthYYYYMMDD(string dateStart)
        {
            int x = 0, len = dateStart.Length;
            string year = "";
            while (x < len && dateStart[x] != '-')
                year += dateStart[x++];

            ++x;
            string month = "";
            while (x < len && dateStart[x] != '-')
                month += dateStart[x++];

            short lastDay = NumOfDaysInMonth((short)IntFromStr(month), (short)IntFromStr(year));

            return year + "-" + month + "-" + lastDay;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public short DetMonthNumFromMonthName(string month)
        {
            char[] b = new char[20];
            StrToChars(b, month);

            return DetMonthNumFromMonthName(b);
        }
        public short DetMonthNumFromMonthName(char[] month)
        {
            if (Match(month, "January")) return 1;
            if (Match(month, "February")) return 2;
            if (Match(month, "March")) return 3;
            if (Match(month, "April")) return 4;
            if (Match(month, "May")) return 5;
            if (Match(month, "June")) return 6;
            if (Match(month, "July")) return 7;
            if (Match(month, "August")) return 8;
            if (Match(month, "September")) return 9;
            if (Match(month, "October")) return 10;
            return Match(month, "November") ? (short)11 : (short)12;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void DetMonthNameFromMonthNum(short monthNum, char[] month)
        {
            switch (monthNum)
            {
                case 1: CatAsChars("January", 0, month, true); break;
                case 2: CatAsChars("February", 0, month, true); break;
                case 3: CatAsChars("March", 0, month, true); break;
                case 4: CatAsChars("April", 0, month, true); break;
                case 5: CatAsChars("May", 0, month, true); break;
                case 6: CatAsChars("June", 0, month, true); break;
                case 7: CatAsChars("July", 0, month, true); break;
                case 8: CatAsChars("August", 0, month, true); break;
                case 9: CatAsChars("September", 0, month, true); break;
                case 10: CatAsChars("October", 0, month, true); break;
                case 11: CatAsChars("November", 0, month, true); break;
                default: CatAsChars("December", 0, month, true); break;
            }
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // calcs the num of months between two dates; inclusive of start and end months
        public short NumMonths(char[] dateFrom, char[] dateTo, string localDefnsDir, string defnsDir)
        {
            int i1 = Encode(dateTo, localDefnsDir, defnsDir);
            short m1 = GetMonth(i1);
            short y1 = GetYear(i1);

            int i2 = Encode(dateFrom, localDefnsDir, defnsDir);
            short m2 = GetMonth(i2);
            short y2 = GetYear(i2);

            if (i1 == i2)
                return 1;

            if (i1 < i2)
                return 0;

            short m;
            if (m1 <= m2)
            {
                m = (short)(12 - (m2 - m1));
                m += (short)((--y1 - y2) * 12);
            }
            else m = (short)((m1 - m2) + ((y1 - y2) * 12));

            return ++m;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // calcs the num of months between two monthstrs ('7-1994' style); inclusive of start and end months
        public int NumMonths(string monthFrom, string monthTo)
        {
            int[] month1 = new int[1];
            int[] month2 = new int[1];
            int[] year1 = new int[1];
            int[] year2 = new int[1];

            MonthYearStrToYearAndMonth2(monthFrom, month1, year1);
            MonthYearStrToYearAndMonth2(monthTo, month2, year2);

            int numMths = ((year2[0] - year1[0]) - 1) * 12;

            numMths += ((12 - month1[0]) + 1);

            numMths += month2[0];

            return numMths;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // adds numofmonths months onto start date
        public int AddMonths(int date, short numMonths, string localDefnsDir, string defnsDir)
        {
            short yy, mm, dd;
            string s;

            for (short x = 0; x < numMonths; ++x)
            {
                yy = GetYear(date);
                mm = GetMonth(date);
                dd = GetDay(date);
                ++mm;
                if (mm == 13)
                {
                    mm = 1;
                    ++yy;
                }

                if (dd == 31)
                {
                    dd = mm == 2 ? IsLeapYear(yy) ? (short)28 : (short)27 : (short)30;
                }
                else
                if (dd == 30)
                {
                    dd = mm == 2 ? IsLeapYear(yy) ? (short)28 : (short)27 : (short)29;
                }
                else --dd;

                s = dd + "." + mm + "." + yy;
                date = Encode(s, localDefnsDir, defnsDir);
            }

            return date;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void AddOneMonth(char[] dateNow, char[] datePlusOneMonth, string localDefnsDir, string defnsDir)
        {
            int date = Encode(dateNow, localDefnsDir, defnsDir);
            short dd = GetDay(date);
            short mm = GetMonth(date);
            short yyyy = GetYear(date);
            short lastDay = NumOfDaysInMonth(mm, yyyy);
            date += (lastDay - dd);
            Decode(date, datePlusOneMonth, localDefnsDir, defnsDir);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        short GetDateFormat(string localDefnsDir, string defnsDir)
        {
            string s = GetFromDefnFile("FORMAT", "dates.dfn", localDefnsDir, defnsDir);

            if (s.Length == 0) // just-in-case
                return 0; // ddmmyy

            if (s.Equals("MMDDYY", StringComparison.CurrentCultureIgnoreCase))
                return 1;
     
            if (s.Equals("YYYYMMDD", StringComparison.CurrentCultureIgnoreCase))
                return 2;
            return 0; // ddmmyy
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        char GetDateLength(string localDefnsDir, string defnsDir)
        {
            string s;
            s = GetFromDefnFile("LENGTH", "dates.dfn", localDefnsDir, defnsDir);

            if (s.Length == 0) // just-in-case
                return 'S'; // short

            if (s.Equals("Long", StringComparison.CurrentCultureIgnoreCase))
                return 'L';
            return 'S'; // short
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        char GetDateSeparator(string localDefnsDir, string defnsDir)
        {
            string s = GetFromDefnFile("SEPARATOR", "dates.dfn", localDefnsDir, defnsDir);

            return s.Length == 0 ? '.' : s[0];
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        short GetDateCentury(string localDefnsDir, string defnsDir)
        {
            string s;
            s = GetFromDefnFile("CENTURY", "dates.dfn", localDefnsDir, defnsDir);

            return s.Length == 0 ? (short)20 : (short)StrToInt(s);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool ValidateDate(bool noBlankDate, string dateStr, string localDefnsDir, string defnsDir)
        {
            char[]
            b = new char[dateStr.Length + 1];
            StrToChars(b, dateStr);
            return ValidateDate(noBlankDate, b, localDefnsDir, defnsDir);
        }
        public bool ValidateDate(char[] dateStr, string localDefnsDir, string defnsDir)
        {
            return ValidateDate(true, dateStr, localDefnsDir, defnsDir);
        }
        public bool ValidateDate(bool noBlankDate, char[] dateStr, string localDefnsDir, string defnsDir)
        {
            if (dateStr[0] == '\0')
            {
                if (noBlankDate)
                    return false;  // no date supplied (illegal date)
                return true;
            }

            char separator = GetDateSeparator(localDefnsDir, defnsDir);

            int x = 0;
            while (dateStr[x] != '\0')
            {
                if ((dateStr[x] < '0' || dateStr[x] > '9') && dateStr[x] != separator)
                    return false;
                ++x;
            }

            short dd, mm, yyyy;
            char[]
            str = new char[20];

            int len = Lengthchars(dateStr, 0);

            char format = GetDateLength(localDefnsDir, defnsDir);
            short layout = GetDateFormat(localDefnsDir, defnsDir);
            short century = GetDateCentury(localDefnsDir, defnsDir);

            x = 0; int y = 0;
            while (x < len && dateStr[x] != separator && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';
            short first = (short)IntFromCharsCharFormat(str, (short)0);

            ++x; y = 0;
            while (x < len && dateStr[x] != separator && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';
            short second = (short)IntFromCharsCharFormat(str, (short)0);

            ++x; y = 0;
            while (x < len && dateStr[x] != '\0' && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';
            yyyy = (short)IntFromCharsCharFormat(str, (short)0);

            if (yyyy <= 99)
                if (format == 'S')
                    yyyy += (short)(century * 100);

            switch (layout)
            {
                case 1: // mm dd yy
                    mm = first; dd = second; break;
                default: // dd mm yy
                    dd = first; mm = second; break;
            }

            if (dd == 0 || mm == 0)
                return false;

            switch (mm)
            {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    if (dd > 31) return false;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    if (dd > 30) return false;
                    break;
                case 2:
                    if (!IsLeapYear(yyyy))
                    {
                        if (dd > 28)
                            return false;
                    }
                    else // leap year
                        if (dd > 29)
                        return false;
                    break;
                default: return false;
            }

            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool ValidateDateSQL(bool noBlankDate, string dateStr, string localDefnsDir, string defnsDir)
        {
            char[] b = new char[dateStr.Length + 1];
            StrToChars(b, dateStr);
            return ValidateDateSQL(noBlankDate, Convert.ToString(b), localDefnsDir, defnsDir);
        }
        public bool ValidateDateSQL(char[] dateStr, string localDefnsDir, string defnsDir)
        {
            return ValidateDate(true, dateStr, localDefnsDir, defnsDir);
        }
        public bool ValidateDateSQL(bool noBlankDate, char[] dateStr)
        {
            if (dateStr[0] == '\0')
            {
                if (noBlankDate)
                    return false;  // no date supplied (illegal date)
                return true;
            }

            char[] str = new char[20];
            int len = Lengthchars(dateStr, 0);

            int x = 0;
            int y = 0;
            while (x < len && dateStr[x] != '-' && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';

            if (y != 4)
                return false;

            short yyyy = (short)IntFromCharsCharFormat(str, (short)0);

            if (yyyy < 1970)
                return false;

            ++x; y = 0;
            while (x < len && dateStr[x] != '-' && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';

            if (y != 1 && y != 2)
                return false;

            short mm = (short)IntFromCharsCharFormat(str, (short)0);

            if (mm < 1 || mm > 12)
                return false;

            ++x; y = 0;
            while (x < len && dateStr[x] != '\0' && y < 20)  // just-in-case
                str[y++] = dateStr[x++];
            str[y] = '\0';

            if (y != 1 && y != 2)
                return false;

            short dd = (short)IntFromCharsCharFormat(str, (short)0);

            switch (mm)
            {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    if (dd > 31) return false;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    if (dd > 30) return false;
                    break;
                case 2:
                    if (!IsLeapYear(yyyy))
                    {
                        if (dd > 28)
                            return false;
                    }
                    else // leap year
                    if (dd > 29)
                        return false;
                    break;
                default: return false;
            }

            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // monthYear == "August 2001"
        // rtns true if date in in the specified month/year
        public bool DateMatches(int date, string monthYear)
        {
            if (monthYear.Length == 0)
                return true;

            short month = GetMonth(date);
            short year = GetYear(date);

            string monthStr = "", yearStr = "";
            int x = 0;
            while (monthYear[x] != '-')
                monthStr += monthYear[x++];
            ++x;
            while (x < monthYear.Length)
                yearStr += monthYear[x++];

            return month == StrToInt(monthStr) && year == StrToInt(yearStr) ? true : false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Format of monthYear is "July 1996"
        // Rtns: first of month encoded, and last of month encoded
        public void MonthYearStrToEncoded(string monthYear, int[] firstDateEncoded, int[] lastDateEncoded, string localDefnsDir, string defnsDir)

        {
            string monthStr = "", yearStr = "";
            int x = 0;
            while (monthYear[x] != '-')
                monthStr += monthYear[x++];
            ++x;
            while (x < monthYear.Length)
                yearStr += monthYear[x++];

            firstDateEncoded[0] = Encode("1." + monthStr + "." + yearStr, localDefnsDir, defnsDir);

            int numDays = NumOfDaysInMonth((short)IntFromStr(monthStr), (short)IntFromStr(yearStr));
            lastDateEncoded[0] = Encode(IntToStr(numDays) + "." + monthStr + "." + yearStr, localDefnsDir, defnsDir);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Format of monthYear is "July 1996"
        // Rtns: month as 1 to 12 (0 if no match - jic)
        //       year as long format (e.g., 1996)
        public void MonthYearStrToYYYYMMDDDates2(string monthYear, string[] dateFrom, string[] dateTo)
        {
            if (monthYear.Equals("ALL", StringComparison.CurrentCultureIgnoreCase))
            {
                dateFrom[0] = "1970-01-01";
                dateTo[0] = "2099-12-31";
                return;
            }

            string year = monthYear.Substring(monthYear.IndexOf(" ", StringComparison.CurrentCultureIgnoreCase) + 1);

            string month, numDays;

            if (monthYear.StartsWith("January", StringComparison.CurrentCultureIgnoreCase)) { month = "01"; numDays = "31"; }
            else if (monthYear.StartsWith("March", StringComparison.CurrentCultureIgnoreCase)) { month = "03"; numDays = "31"; }
            else if (monthYear.StartsWith("April", StringComparison.CurrentCultureIgnoreCase)) { month = "04"; numDays = "30"; }
            else if (monthYear.StartsWith("May", StringComparison.CurrentCultureIgnoreCase)) { month = "05"; numDays = "31"; }
            else if (monthYear.StartsWith("June", StringComparison.CurrentCultureIgnoreCase)) { month = "06"; numDays = "30"; }
            else if (monthYear.StartsWith("July", StringComparison.CurrentCultureIgnoreCase)) { month = "07"; numDays = "31"; }
            else if (monthYear.StartsWith("August", StringComparison.CurrentCultureIgnoreCase)) { month = "08"; numDays = "31"; }
            else if (monthYear.StartsWith("September", StringComparison.CurrentCultureIgnoreCase)) { month = "09"; numDays = "30"; }
            else if (monthYear.StartsWith("October", StringComparison.CurrentCultureIgnoreCase)) { month = "10"; numDays = "31"; }
            else if (monthYear.StartsWith("November", StringComparison.CurrentCultureIgnoreCase)) { month = "11"; numDays = "30"; }
            else if (monthYear.StartsWith("December", StringComparison.CurrentCultureIgnoreCase)) { month = "12"; numDays = "31"; }
            else // if(monthYear.StartsWith("February"))  
            {
                month = "02";

                numDays = IsLeapYear((short)StrToInt(year)) ? "29" : "28";
            }

            dateFrom[0] = year + "-" + month + "-01";
            dateTo[0] = year + "-" + month + "-" + numDays;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // Format of monthYear is "1-1996"
        // Rtns: month as 1 to 12 (0 if no match - jic)
        //       year as long format (e.g., 1996)
        public void MonthYearStrToYYYYMMDDDates(string monthYear, string[] dateFrom, string[] dateTo)
        {
            if (monthYear.Equals("ALL", StringComparison.CurrentCultureIgnoreCase))
            {
                dateFrom[0] = "1970-01-01";
                dateTo[0] = "2099-12-31";
                return;
            }

            string year = monthYear.Substring(monthYear.IndexOf("-", StringComparison.CurrentCultureIgnoreCase) + 1);
            string month = monthYear.Substring(0, monthYear.IndexOf("-", StringComparison.CurrentCultureIgnoreCase));

            string numDays;

            if (month.Equals("1")) numDays = "31";
            else if (month.Equals("3")) numDays = "31";
            else if (month.Equals("4")) numDays = "30";
            else if (month.Equals("5")) numDays = "31";
            else if (month.Equals("6")) numDays = "30";
            else if (month.Equals("7")) numDays = "31";
            else if (month.Equals("8")) numDays = "31";
            else if (month.Equals("9")) numDays = "30";
            else if (month.Equals("10")) numDays = "31";
            else if (month.Equals("11")) numDays = "30";
            else if (month.Equals("12")) numDays = "31";
            else // if(monthYear.Equals("2"))  
            {
                numDays = IsLeapYear((short)StrToInt(year)) ? "29" : "28";
            }

            dateFrom[0] = year + "-" + month + "-01";
            dateTo[0] = year + "-" + month + "-" + numDays;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool CreateDefnFile(string fileName, string dirName)
        {
            try
            {
                Create(dirName + fileName);
            }
            catch (Exception)
            {
                return false;
            }
            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool AddToDefnFile(string name, string value, string fileName, string localDefnsDir, string defnsDir)
        {
            try
            {
                FileStream fs;

                if (localDefnsDir.Length > 0)
                {
                    if ((fs = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fs = FileOpenD(fileName, defnsDir)) == null)
                            return false;
                    }
                }
                else
                {
                    if ((fs = FileOpenD(fileName, defnsDir)) == null)
                        return false;
                }

                fs.Seek(0, SeekOrigin.End);

                string s = name + " " + value;
                StreamWriter writer = new StreamWriter(fs);
                StringBuilder output = new StringBuilder();

                output.AppendLine(s);

                fs.Close();
            }
            catch (Exception)
            {
                return false;
            }
            return true;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string GetFromDefnFile(string name, string fileName, string localDefnsDir, string defnsDir)
        {
            short x;
            string str, buf, value;

            try
            {
                FileStream fs;

                if (localDefnsDir.Length > 0)
                {
                    if ((fs = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fs = FileOpenD(fileName, defnsDir)) == null)
                            return "";
                    }
                }
                else
                {
                    if ((fs = FileOpenD(fileName, defnsDir)) == null)
                        return "";
                }

                fs.Seek(0L, SeekOrigin.Begin);

                value = "";
                try
                {
                    StreamReader sr = new StreamReader(fs);

                    bool quit = false;
                    while (!quit)
                    {
                        x = 0;
                        buf = "";
                        str = sr.ReadLine();
                        while (x < str.Length && str[x] != ' ')
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                buf += str[x];
                            ++x;
                        }

                        if (buf.Equals(name, StringComparison.CurrentCultureIgnoreCase))
                        {
                            ++x;

                            while (x < str.Length && str[x] == ' ')
                                ++x;

                            while (x < str.Length && str[x] != (char)13 && str[x] != (char)10
                                  && str[x] != (char)26)
                            {
                                value += str[x++];
                            }
                            quit = true;
                        }
                    }
                    fs.Close();
                }
                catch (Exception)
                {
                    fs.Close();
                }
            }
            catch (Exception)
            {
                return "";
            }

            return value;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // prependName is true if the name-value pair is required; else just the value.
        // if name is "" then entries are treated sequentially in file; else just those with name are considered.
        public string GetFromDefnFileByEntry(bool prependName, string name, int entry, string fileName, string localDefnsDir, string defnsDir)
        {
            short x;
            int count;
            string str, buf, value;

            try
            {
                FileStream fh = null;

                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return "";
                    }
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return "";
                }

                fh.Seek(0L, SeekOrigin.Begin);

                count = 1;
                value = "";
                try
                {
                    StreamReader sr = new StreamReader(fh);
                    bool quit = false;
                    while (!quit)
                    {
                        x = 0;
                        buf = "";
                        str = sr.ReadLine();

                        while (x < str.Length && str[x] != ' ')
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                buf += str[x];
                            ++x;
                        }

                        if (name.Length == 0 || buf.Equals(name, StringComparison.CurrentCultureIgnoreCase))
                        {
                            if (count == entry)
                            {
                                if (prependName)
                                {
                                    x = 0;
                                    while (x < str.Length)
                                    {
                                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                            value += str[x];
                                        ++x;
                                    }
                                }
                                else // only want value
                                {
                                    ++x;
                                    while (str[x] == ' ')
                                        ++x;
                                    while (x < str.Length)
                                    {
                                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                        {
                                            if (str[x] == ' ')
                                                value += ' ';
                                            else value += str[x];
                                        }
                                        ++x;
                                    }
                                }
                                quit = true;
                            }
                            ++count;
                        }
                    }

                    fh.Close();
                }
                catch (Exception)
                {
                    fh.Close();
                }
            }
            catch (Exception)
            {
                return "";
            }
            return value;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public FileStream OpenFile(string fileName, string localDefnsDir, string defnsDir)
        {
            try
            {
                FileStream fh;
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return null;
                    }
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return null;
                }

                fh.Seek(0L, SeekOrigin.Begin);

                return fh;
            }
            catch (Exception)
            {
                return null;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public void CloseFile(FileStream fh)
        {
            try
            {
                fh.Close();
            }
            catch (Exception) { Console.WriteLine("GeneralUtils: Cannot close file"); }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // prependName is true if the name-value pair is required; else just the value.
        // if name is "" then entries are treated sequentially in file; else just those with name are
        //   considered.
        // This second version, to save next-search time:
        //   1. Assumes the file is opened, and closed separately
        //   2. keeps a running file pointer
        public string GetFromDefnFileByEntry(FileStream fh, bool prependName, string name, long[] upto)
        {
            short x;
            string str, buf, value;

            try
            {
                fh.Seek(upto[0], SeekOrigin.Begin);
                StreamReader sr = new StreamReader(fh);

                value = "";
                bool quit = false;
                while (!quit)
                {
                    x = 0;
                    buf = "";
                    str = sr.ReadLine();
                    while (x < str.Length && str[x] != ' ')
                    {
                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            buf += str[x];
                        ++x;
                    }

                    if (name.Length == 0 || buf.Equals(name, StringComparison.CurrentCultureIgnoreCase))
                    {
                        if (prependName)
                        {
                            x = 0;
                            while (x < str.Length)
                            {
                                if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                    value += str[x];
                                ++x;
                            }
                        }
                        else // only want value
                        {
                            ++x;
                            while (str[x] == ' ')
                                ++x;
                            while (x < str.Length)
                            {
                                if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                {
                                    if (str[x] == ' ')
                                        value += ' ';
                                    else value += str[x];
                                }
                                ++x;
                            }
                        }
                        quit = true;
                    }
                }
            }
            catch (Exception) { value = ""; }

            upto[0] = fh.Position;
            return value;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public bool RepInDefnFile(string name, string newValue, string fileName, string localDefnsDir, string defnsDir)
        {
            FileStream fh, fhTmp;
            bool written;
            string theDefnsDir;

            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return false;
                        theDefnsDir = defnsDir;
                    }
                    else theDefnsDir = localDefnsDir;
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return false;
                    theDefnsDir = defnsDir;
                }

                fh.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception) //  jic
            {
                return false;
            }

            try
            {
                // create tmp file
                CreateDefnFile("tmp", theDefnsDir);
                fhTmp = FileOpenD("tmp", theDefnsDir);
                fhTmp.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception)
            {
                try
                {
                    fh.Close();
                }
                catch (Exception) { }
                return false;
            }

            written = false;
            try
            {
                short x;
                string str, buf, s;
                StreamReader sr = new StreamReader(fh);
                StreamWriter writer = new StreamWriter(fhTmp);
                StringBuilder output = new StringBuilder();

                while (true)
                {
                    x = 0;
                    buf = "";

                    str = sr.ReadLine();
                    while (x < str.Length && str[x] != ' ')
                    {
                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            buf += str[x];
                        ++x;
                    }

                    if (buf.Equals(name, StringComparison.CurrentCultureIgnoreCase))
                    {
                        s = name + " " + newValue + "\n";
                        fh.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);
                        written = true;
                    }
                    else // not the line wanted, so write to tmp file
                    {
                        x = 0;
                        while (x < str.Length)
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            {
                                byte[] b = new byte[1]; b[0] = (byte)str[x];
                                fhTmp.Write(b, 0, 1);
                            }
                            ++x;
                        }

                        fhTmp.Write(Encoding.ASCII.GetBytes("\n"), 0, 1);
                    }
                }
            }
            catch (Exception) // eof
            {
                try
                {
                    if (!written)
                    {
                        string s = name + " " + newValue + "\n";
                        fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);
                    }
                    fh.Close();
                    fhTmp.Close();

                    FileDeleteD(fileName, theDefnsDir);
                }
                catch (Exception) //  jic
                {
                    return false;
                }

                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // replaces an entry starting with a string of words (not just the first word)
        public bool RepInDefnFilestringMatch(string nameStr, string newEntry, string fileName, string localDefnsDir, string defnsDir)
        {
            FileStream fh, fhTmp;
            bool written;
            string theDefnsDir;

            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return false;
                        theDefnsDir = defnsDir;
                    }
                    else theDefnsDir = localDefnsDir;
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return false;
                    theDefnsDir = defnsDir;
                }

                fh.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception) //  jic
            {
                return false;
            }

            try
            {
                // create tmp file
                CreateDefnFile("tmp", theDefnsDir);
                fhTmp = FileOpenD("tmp", theDefnsDir);
                fhTmp.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception)
            {
                try
                {
                    fh.Close();
                }
                catch (Exception) { }
                return false;
            }

            written = false;
            try
            {
                short x;
                string str, s;
                StreamReader sr = new StreamReader(fh);

                while (true)
                {
                    str = sr.ReadLine();
                    if (str.StartsWith(nameStr, StringComparison.CurrentCulture))
                    {
                        s = newEntry + "\n";
                        fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);
                        written = true;
                    }
                    else // not the line wanted, so write to tmp file
                    {
                        x = 0;
                        while (x < str.Length)
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            {
                                byte[] bb = new byte[1]; bb[0] = (byte)str[x];
                                fhTmp.Write(bb, 0, 1);
                            }
                            ++x;
                        }
                        byte[] b = new byte[1]; b[0] = (byte)10;
                        fhTmp.Write(b, 0, 1);
                    }
                }
            }
            catch (Exception) // eof
            {
                try
                {
                    if (!written)
                    {
                        string s = newEntry + "\n";
                        fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);
                    }
                    fh.Close();
                    fhTmp.Close();

                    FileDeleteD(fileName, theDefnsDir);
                }
                catch (Exception) //  jic
                {
                    return false;
                }

                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // inserts keyed based on nameStr WHERE the first word is a numeric ONLY
        public bool InsertInDefnFile(string nameStr, string newEntry, string fileName, string localDefnsDir, string defnsDir)
        {
            FileStream fh, fhTmp;
            bool written;
            string theDefnsDir;

            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return false;
                        theDefnsDir = defnsDir;
                    }
                    else theDefnsDir = localDefnsDir;
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return false;
                    theDefnsDir = defnsDir;
                }

                fh.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception) //  jic
            {
                return false;
            }

            try
            {
                // create tmp file
                CreateDefnFile("tmp", theDefnsDir);
                fhTmp = FileOpenD("tmp", theDefnsDir);
                fhTmp.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception)
            {
                try
                {
                    fh.Close();
                }
                catch (Exception) { }
                return false;
            }

            written = false;
            try
            {
                int x, len;
                string str, firstWord, s;

                double firstWordD, nameStrD = DoubleFromStr(nameStr);
                StreamReader sr = new StreamReader(fh);

                while (true)
                {
                    str = sr.ReadLine();
                    len = str.Length;
                    firstWord = "";
                    x = 0;
                    while (x < len && str[x] != ' ')
                        firstWord += str[x++];
                    firstWordD = DoubleFromStr(firstWord);
                    if (!written && firstWordD > nameStrD)
                    {
                        s = newEntry + "\n";
                        fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);
                        written = true;
                    }

                    x = 0;
                    while (x < str.Length)
                    {
                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                        {
                            byte[] bb = new byte[1]; bb[0] = (byte)str[x];
                            fhTmp.Write(bb, 0, 1);
                        }
                        ++x;
                    }
                    byte[] b = new byte[1]; b[0] = (byte)10;
                    fhTmp.Write(b, 0, 1);
                }
            }
            catch (Exception) // eof
            {
                try
                {
                    if (!written)
                    {
                        string s = newEntry + "\n";
                        fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);
                    }
                    fh.Close();
                    fhTmp.Close();

                    FileDeleteD(fileName, theDefnsDir);
                }
                catch (Exception) //  jic
                {
                    return false;
                }

                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // STOCK ROD RO/????? 1
        public bool RepInDefnFile(string name, string secondName, string newValue, string fileName, string localDefnsDir, string defnsDir)
        {
            FileStream fh, fhTmp;
            string theDefnsDir;

            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return false;
                        theDefnsDir = defnsDir;
                    }
                    else theDefnsDir = localDefnsDir;
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return false;
                    theDefnsDir = defnsDir;
                }

                fh.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception) //  jic
            {
                return false;
            }

            try
            {
                // create tmp file
                CreateDefnFile("tmp", theDefnsDir);
                fhTmp = FileOpenD("tmp", theDefnsDir);
                fhTmp.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception)
            {
                try
                {
                    fh.Close();
                }
                catch (Exception) { }
                return false;
            }

            bool written = false;
            bool replaced;
            try
            {
                short x;
                string str, buf, s;
                StreamReader sr = new StreamReader(fh);

                while (true)
                {
                    x = 0;
                    buf = "";

                    str = sr.ReadLine();
                    while (x < str.Length && str[x] != ' ')
                    {
                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            buf += str[x];
                        ++x;
                    }

                    replaced = false;
                    if (buf.Equals(name, StringComparison.CurrentCultureIgnoreCase))
                    {
                        ++x;
                        buf = "";
                        while (x < str.Length && str[x] != ' ')
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                buf += str[x];
                            ++x;
                        }

                        if (buf.Equals(secondName, StringComparison.CurrentCultureIgnoreCase))
                        {
                            ++x;
                            buf = "";
                            while (x < str.Length && str[x] != ' ')
                            {
                                if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                    buf += str[x];
                                ++x;
                            }

                            s = name + " " + secondName + " " + buf + " " + newValue + "\n";
                            fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);

                            replaced = true;
                            written = true;
                        }
                    }

                    if (!replaced) // not the line wanted, so write to tmp file
                    {
                        x = 0;
                        while (x < str.Length)
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            {
                                byte[] bb = new byte[1]; bb[0] = (byte)str[x];
                                fhTmp.Write(bb, 0, 1);
                            }
                            ++x;
                        }
                        byte[] b = new byte[1]; b[0] = (byte)10;
                        fhTmp.Write(b, 0, 1);
                    }
                }
            }
            catch (Exception) // eof
            {
                try
                {
                    if (!written)
                    {
                        string s = name + " " + newValue + "\n";
                        fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);
                    }
                    fh.Close();
                    fhTmp.Close();

                    FileDeleteD(fileName, theDefnsDir);
                }
                catch (Exception) //  jic
                {
                    return false;
                }

                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // if name is "" then entries are treated sequentially in file; else just those with name are considered
        public bool RepInDefnFileByEntry(string name, int entry, string newValue, string fileName, string localDefnsDir, string defnsDir)
        {
            FileStream fh, fhTmp;
            string theDefnsDir;

            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return false;
                        theDefnsDir = defnsDir;
                    }
                    else theDefnsDir = localDefnsDir;
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return false;
                    theDefnsDir = defnsDir;
                }

                fh.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception) //  jic
            {
                return false;
            }

            try
            {
                // create tmp file
                CreateDefnFile("tmp", theDefnsDir);
                fhTmp = FileOpenD("tmp", theDefnsDir);
                fhTmp.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception)
            {
                try
                {
                    fh.Close();
                }
                catch (Exception) { }
                return false;
            }

            try
            {
                int count = 0;
                short x;
                string str, buf, s;
                bool wanted;
                StreamReader sr = new StreamReader(fh);

                while (true)
                {
                    x = 0;
                    buf = "";

                    str = sr.ReadLine();
                    while (x < str.Length && str[x] != ' ')
                    {
                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            buf += str[x];
                        ++x;
                    }

                    if (name.Length == 0 || buf.Equals(name, StringComparison.CurrentCultureIgnoreCase))
                    {
                        ++count;
                        if (count == entry)
                            wanted = true;
                        else wanted = false;
                    }
                    else wanted = false;

                    if (wanted)
                    {
                        s = buf + " " + newValue + "\n";
                        fhTmp.Write(Encoding.ASCII.GetBytes(s), 0, s.Length);

                    }
                    else // not the line wanted, so write to tmp file
                    {
                        x = 0;
                        while (x < str.Length)
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            {
                                byte[] bb = new byte[1]; bb[0] = (byte)str[x];
                                fhTmp.Write(bb, 0, 1);

                            }
                            ++x;
                        }
                        byte[] b = new byte[1]; b[0] = (byte)10;
                        fhTmp.Write(b, 0, 1);
                    }
                }
            }
            catch (Exception) // eof
            {
                try
                {
                    fh.Close();
                    fhTmp.Close();
                }
                catch (Exception) //  jic
                {
                    return false;
                }

                if (FileDeleteD(fileName, theDefnsDir))
                    return true;
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // if theValue == "", then dels first match of theName
        public bool DelFromDefnFile(string theName, string theValue, string fileName, string localDefnsDir, string defnsDir)
        {
            FileStream fh, fhTmp;
            string theDefnsDir, thisValue;

            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return false;
                        theDefnsDir = defnsDir;
                    }
                    else theDefnsDir = localDefnsDir;
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return false;
                    theDefnsDir = defnsDir;
                }

                fh.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception) //  jic
            {
                return false;
            }

            try
            {
                // create tmp file
                CreateDefnFile("tmp", theDefnsDir);
                fhTmp = FileOpenD("tmp", theDefnsDir);
                fhTmp.Seek(0L, SeekOrigin.Begin);
            }
            catch (Exception)
            {
                try
                {
                    fh.Close();
                }
                catch (Exception) { }
                return false;
            }

            try
            {
                short x;
                string str, buf;
                bool ignored, notCR;
                StreamReader sr = new StreamReader(fh);

                while (true)
                {
                    x = 0;
                    buf = "";

                    str = sr.ReadLine();
                    while (x < str.Length && str[x] != ' ')
                    {
                        if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                            buf += str[x];
                        ++x;
                    }
                    ignored = false;

                    if (buf.Equals(theName, StringComparison.CurrentCultureIgnoreCase))
                    {
                        ++x;

                        while (x < str.Length && str[x] == ' ')
                            ++x;

                        thisValue = "";
                        while (x < str.Length && str[x] != ' ' && str[x] != (char)13
                              && str[x] != (char)10 && str[x] != (char)26)
                        {
                            thisValue += str[x++];
                        }

                        if (theValue.Length == 0 || thisValue.Equals(theValue, StringComparison.CurrentCultureIgnoreCase))
                        {
                            ignored = true; // match, so ignore it
                        }
                    }

                    if (!ignored) // not the line wanted, so write to tmp file
                    {
                        x = 0;
                        notCR = false;
                        while (x < str.Length)
                        {
                            notCR |= (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26);

                            ++x;
                        }

                        if (notCR)
                        {
                            x = 0;
                            while (x < str.Length)
                            {
                                if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                {
                                    byte[] bb = new byte[1]; bb[0] = (byte)str[x];
                                    fhTmp.Write(bb, 0, 1);
                                }
                                ++x;
                            }
                            byte[] b = new byte[1]; b[0] = (byte)10;
                            fhTmp.Write(b, 0, 1);
                        }
                    }
                }
            }
            catch (Exception) // eof
            {
                try
                {
                    fh.Close();
                    fhTmp.Close();

                    FileDeleteD(fileName, theDefnsDir);
                }
                catch (Exception) //  jic
                {
                    return false;
                }

                return true;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int CountFileEntries(string fileName, string localDefnsDir, string defnsDir)
        {
            int x = 1, count = 0;
            string s = " ";

            while (s.Length != 0)
            {
                s = GetFromDefnFileByEntry(true, "", x, fileName, localDefnsDir, defnsDir);
                if (s.Length != 0)
                    ++count;
                ++x;
            }
            return count;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // which = P - PO
        //       = Q - quote
        public string GetRemark(char which, string remarkType, int reqdLine, string localDefnsDir, string defnsDir)
        {
            short x;
            string str, buf;
            bool possWanted;
            FileStream fh;

            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD("Remarks.dfn", localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD("Remarks.dfn", defnsDir)) == null)
                            return "";
                    }
                }
                else
                {
                    if ((fh = FileOpenD("Remarks.dfn", defnsDir)) == null)
                        return "";
                }

                fh.Seek(0L, SeekOrigin.Begin);

                try
                {
                    StreamReader sr = new StreamReader(fh);

                    while (true)
                    {
                        x = 0;
                        buf = "";
                        str = sr.ReadLine();
                        while (x < str.Length && str[x] != ' ')
                        {
                            if (str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                buf += str[x];
                            ++x;
                        }

                        if (buf.Equals("REMARK", StringComparison.CurrentCultureIgnoreCase))
                        {
                            ++x;

                            while (x < str.Length && str[x] == ' ')
                                ++x;

                            buf = "";
                            while (x < str.Length && str[x] != ' ' && str[x] != (char)13
                                  && str[x] != (char)10 && str[x] != (char)26)
                            {
                                buf += str[x++];
                            }

                            possWanted = false;
                            switch (which)
                            {
                                case 'P': if (buf.Equals("PO", StringComparison.CurrentCultureIgnoreCase)) possWanted = true; break;
                                case 'Q': possWanted |= buf.Equals("quote", StringComparison.CurrentCultureIgnoreCase); break;
                            }

                            if (possWanted)
                            {
                                while (x < str.Length && str[x] == ' ')
                                    ++x;

                                buf = "";
                                while (x < str.Length && str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                    buf += str[x++];

                                if (buf.Equals(remarkType, StringComparison.CurrentCultureIgnoreCase)) // this is the reqd entry
                                {
                                    // pickup the remark line
                                    try
                                    {
                                        for (int i = 0; i < reqdLine; ++i)
                                            sr.ReadLine();
                                        str = sr.ReadLine();
                                        x = 0;
                                        buf = "";
                                        while (x < str.Length && str[x] != (char)13 && str[x] != (char)10 && str[x] != (char)26)
                                            buf += str[x++];
                                        if (buf.StartsWith("REMARK", StringComparison.CurrentCultureIgnoreCase)) // stepped onto next
                                            buf = " ";
                                    }
                                    catch (Exception)
                                    {
                                        fh.Close();
                                        return " ";
                                    }
                                    fh.Close();
                                    return buf;
                                }
                            }
                        }
                    }
                }
                catch (Exception) { fh.Close(); }
            }
            catch (Exception) { }

            return " ";
        }

        // -------------------------------------------------------------------------------------------
        // assumes first fld is int
        public int GetNextHighest(string fileName, string localDefnsDir, string defnsDir)
        {
            int soFar = 0;
            FileStream fh;
            try
            {
                if (localDefnsDir.Length > 0)
                {
                    if ((fh = FileOpenD(fileName, localDefnsDir)) == null)
                    {
                        if ((fh = FileOpenD(fileName, defnsDir)) == null)
                            return 0;
                    }
                }
                else
                {
                    if ((fh = FileOpenD(fileName, defnsDir)) == null)
                        return 0;
                }

                fh.Seek(0L, SeekOrigin.Begin);

                long[] upto = new long[1]; upto[0] = 0;
                int x, thisOneInt;
                string thisOne;
                string s = GetFromDefnFileByEntry(fh, true, "", upto);
                int len = s.Length;
                while (len > 0)
                {
                    x = 0;
                    thisOne = "";
                    while (x < len && s[x] != ' ')
                        thisOne += s[x++];
                    thisOneInt = IntFromStr(thisOne);
                    if (thisOneInt > soFar)
                        soFar = thisOneInt;

                    s = GetFromDefnFileByEntry(fh, true, "", upto);
                    len = s.Length;
                }

                fh.Close();
            }
            catch (Exception)
            {
                return soFar;
            }

            return soFar;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void InsertionSort(int[] array, int numElements)
        {
            int j;
            for (int i = 1; i < numElements; i++)
            {
                for (j = 0; j < i; j++)
                {
                    if (array[i - j - 1] > array[i - j])
                        Swap(array, i - j - 1, i - j);
                    else j = i;
                }
            }
        }

        void Swap(int[] array, int x, int y)
        {
            int i = array[x];
            array[x] = array[y];
            array[y] = i;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public void InsertionSort(String[] array, int numElements)
        {
            int j;
            for (int i = 1; i < numElements; i++)
            {
                for (j = 0; j < i; j++)
                {
                    if (String.Compare(array[i - j - 1], array[i - j]) > 0)
                        Swap(array, i - j - 1, i - j);
                    else j = i;
                }
            }
        }

        void Swap(string[] array, int x, int y)
        {
            string s = array[x];
            array[x] = array[y];
            array[y] = s;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool AppendToFile(string text, string fileName)
        {
            FileStream fh = FileOpen(fileName);
            if (fh == null)
                fh = Create(fileName);

            if (fh == null) // just-in-case
                return false;

            fh.Seek(0L, SeekOrigin.End);

            StreamWriter writer = new StreamWriter(fh);
            StringBuilder output = new StringBuilder();

            output.Append(text);

            CloseFile(fh);

            return true;
        }

        // ------------------------------------------------------------------------------------------------
        // in 01.01.1970 00:00:00, out 1970-01-01 00:00:00
        public string ConvertToTimestamp(string ts)
        {
            string date = "";
            int x = 0, len = ts.Length;
            while (x < len && ts[x] != ' ') // just-in-case
                date += ts[x++];
            date = ConvertDateToSQLFormat(date);
            return date.Length == 0 ? "" : date + ts.Substring(x);
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        // converts "31.01.05" to "2005-01-31"
        public string ConvertDateToSQLFormat(string s)
        {
            char[]
            buf = new char[20];
            StrToChars(buf, s);
            ConvertToYYYYMMDD(buf);
            return StringFromChars(buf);
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        // converts "31.01.05" to "2005-01-31"
        public int ConvertToYYYYMMDD(char[] buf)
        {
            char[]
            dd = new char[10];
            char[] mm = new char[10];
            char[] yy = new char[10];

            if (buf[0] == '\0')
            {
                StrToChars(buf, "1970-01-01");
                return 10;
            }

            int x = 0, y = 0;
            while (buf[x] != '\0' && buf[x] != '.')
                dd[y++] = buf[x++];
            dd[y] = '\0';

            if (y == 1)
            {
                dd[2] = '\0';
                dd[1] = dd[0];
                dd[0] = '0';
            }

            ++x;
            y = 0;
            while (buf[x] != '\0' && buf[x] != '.')
                mm[y++] = buf[x++];
            mm[y] = '\0';

            if (y == 1)
            {
                mm[2] = '\0';
                mm[1] = mm[0];
                mm[0] = '0';
            }

            ++x;
            y = 0;
            while (buf[x] != '\0' && buf[x] != '.')
                yy[y++] = buf[x++];
            yy[y] = '\0';

            y = 0;

            // if year is only 2-chars, prepend century
            if (yy[2] == '\0')
            {
                if (IntFromCharsCharFormat(yy, (short)0) < 50)
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

            x = 0;

            while (yy[x] != '\0')
                buf[y++] = yy[x++];
            buf[y++] = '-';

            x = 0;
            while (mm[x] != '\0')
                buf[y++] = mm[x++];
            buf[y++] = '-';

            x = 0;
            while (dd[x] != '\0')
                buf[y++] = dd[x++];
            buf[y] = '\0';

            return y;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // converts "2005-01-31" to "31.01.05"; leaving result in buf
        public string ConvertFromYYYYMMDD(string s)
        {
            char[]
            b = new char[20];
            StrToChars(b, s);
            ConvertFromYYYYMMDD(b);

            return StringFromChars(b);
        }
        public string ConvertFromYYYYMMDD(char[] buf)
        {
            if (buf[0] == '\0')
                return "";

            if (Match(buf, "1970-01-01"))
            {
                buf[0] = '\0';
                return "";
            }

            char[] b = new char[11];
            char[] mm = new char[2];
            char[] dd = new char[2];

            CharsToChars(b, buf, 0);

            int len = Lengthchars(b, 0);

            if (b[6] == '-')
            {
                mm[0] = '0';
                mm[1] = b[5];
            }
            else
            {
                mm[0] = b[5];
                mm[1] = b[6];
            }

            if (len == 9) // b[9] == '\0')
            {
                if (b[7] == '-')
                {
                    dd[0] = '0';
                    dd[1] = b[8];
                }
                else
                if (len == 8) // b[8] == '\0')
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
            buf[10] = '\0';

            return StringFromChars(buf);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // converts "2005-01-31" to "31.01.05"
        public string ConvertFromYYYYMMDD2(char[] buf)
        {
            if (buf[0] == '\0')
                return "";

            if (Match(buf, "1970-01-01"))
            {
                buf[0] = '\0';
                return "";
            }

            char[]
            a = new char[11];
            char[] b = new char[11];

            CharsToChars(b, buf, 0);

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
            a[10] = '\0';

            return StringFromChars(a);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // converts "2006-10-09 15:08:03.0" to "09.10.2006 15:08:03"
        public string ConvertFromTimestamp(char[] b)
        {
            return ConvertFromTimestamp(StringFromChars(b));
        }
        public string ConvertFromTimestamp(string str)
        {
            try
            {
                int x = 0, len = str.Length;
                string s = "";
                while (x < len && str[x] != ' ')
                    s += str[x++];
                ++x;

                string t = "";
                while (x < len && str[x] != '.')
                    t += str[x++];

                string d = ConvertFromYYYYMMDD(s);
                return d.Length == 0 ? "" : d + " " + t;
            }
            catch (Exception) { return ""; }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // converts "2006-10-09 15:08:03.0" to "09.10.2006 15:08:03"
        public string ConvertFromTimestampDoW(string str)
        {
            try
            {
                int x = 0, len = str.Length;
                string s = "";
                while (x < len && str[x] != ' ')
                    s += str[x++];
                ++x;

                string t = "";
                while (x < len && str[x] != '.')
                    t += str[x++];

                string d = ConvertFromYYYYMMDD(s);
                if (d.Length == 0)
                    return "";

                string yymmdd = "" + s[2] + s[3] + s[5] + s[6] + s[8] + s[9];

                d = YymmddExpand(true, yymmdd);

                return d + " " + t;
            }
            catch (Exception) { return ""; }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        // converts "2006-10-09 15:08:03.0" to "15:08:03"
        public string TimeFromTimestamp(string str)
        {
            try
            {
                int x = 0, len = str.Length;
                while (x < len && str[x] != ' ')
                    ++x;
                ++x;

                string t = "";
                while (x < len && str[x] != '.')
                    t += str[x++];

                return t;
            }
            catch (Exception) { return ""; }
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        // converts "1" to    "00:01:00"
        // converts "12" to   "00:12:00"
        // converts "859" to  "08:59:00"
        // converts "1259" to "12:59:00"
        public int ConvertToHHMMSS(char[] buf, int len)
        {
            if (buf[0] == '\0')
            {
                StrToChars(buf, "12:15:00");
                return 8;
            }

            char[] b = new char[10];
            switch (len)
            {
                case 1:
                    b[0] = buf[0];

                    buf[0] = '0';
                    buf[1] = '0';
                    buf[2] = ':';
                    buf[3] = '0';
                    buf[4] = b[0];

                    buf[5] = ':';
                    buf[6] = '0';
                    buf[7] = '0';
                    buf[8] = '\0';
                    break;
                case 2:
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
                    buf[8] = '\0';
                    break;
                case 3:
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
                    buf[8] = '\0';
                    break;
                case 4:
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
                    buf[8] = '\0';
                    break;
                default:
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
                    buf[8] = '\0';
                    break;
            }

            return 8;
        }

        // -------------------------------------------------------------------------------------------------------------------------------
        public int EncodeSQLFormat(string dateStr)
        {
            short yyyy = (short)StrToInt(dateStr.Substring(0, 4));
            short mm = (short)StrToInt(dateStr.Substring(5, 7));
            short dd = (short)StrToInt(dateStr.Substring(8));

            return NumOfDaysTotal(dd, mm, yyyy);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string YearNow(string localDefnsDir, string defnsDir)
        {
            string today = Today(localDefnsDir, defnsDir);

            return "20" + today[6] + today[7];
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string GeneratePassWord()
        {
            char[] pwd = new char[100];

            int xx = new Random().Next(1000000);

            IntToCharsCharFormat(xx, pwd, (short)0);

            pwd[8] = '\0';
            switch (pwd[0])
            {
                case '0': pwd[0] = 'a'; break;
                case '1': pwd[0] = 'c'; break;
                case '2': pwd[0] = 'g'; break;
                case '3': pwd[0] = 'e'; break;
                case '4': pwd[0] = 'k'; break;
                case '5': pwd[0] = 't'; break;
                case '6': pwd[0] = 'i'; break;
                case '7': pwd[0] = 'b'; break;
                case '8': pwd[0] = 'j'; break;
                case '9': pwd[0] = 'w'; break;
            }
            switch (pwd[2])
            {
                case '0': pwd[2] = 'Q'; break;
                case '1': pwd[2] = 'R'; break;
                case '2': pwd[2] = 'V'; break;
                case '3': pwd[2] = 'S'; break;
                case '4': pwd[2] = 'D'; break;
                case '5': pwd[2] = 'B'; break;
                case '6': pwd[2] = 'N'; break;
                case '7': pwd[2] = 'M'; break;
                case '8': pwd[2] = 'K'; break;
                case '9': pwd[2] = 'P'; break;
            }

            switch (pwd[5])
            {
                case '0': pwd[5] = 'Q'; break;
                case '1': pwd[5] = 'W'; break;
                case '2': pwd[5] = 'E'; break;
                case '3': pwd[5] = 'R'; break;
                case '4': pwd[5] = 'T'; break;
                case '5': pwd[5] = 'Y'; break;
                case '6': pwd[5] = 'U'; break;
                case '7': pwd[5] = 'Z'; break;
                case '8': pwd[5] = 'S'; break;
                case '9': pwd[5] = 'N'; break;
            }
            switch (pwd[7])
            {
                case '0': pwd[7] = 'm'; break;
                case '1': pwd[7] = 'n'; break;
                case '2': pwd[7] = 'b'; break;
                case '3': pwd[7] = 'v'; break;
                case '4': pwd[7] = 'c'; break;
                case '5': pwd[7] = 'x'; break;
                case '6': pwd[7] = 'z'; break;
                case '7': pwd[7] = 'k'; break;
                case '8': pwd[7] = 'j'; break;
                case '9': pwd[7] = 'g'; break;
            }

            for (int x = 0; x < 8; ++x)
            {
                if (pwd[x] == '0')
                    pwd[x] = '8';
                else
                if (pwd[x] == '1')
                    pwd[x] = '2';
            }

            return StringFromChars(pwd);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripPunctuation(string p)
        {
            char[] phrase = new char[p.Length + 1];
            StrToChars(phrase, p);

            int len = Lengthchars(phrase, 0);
            int x = 0, y;
            while (x < len)
            {
                if (phrase[x] == ','
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
                if (phrase[x] == '-')
                {
                    if (x > 0 && x < len && phrase[x - 1] != ' ' && phrase[x + 1] != ' ')
                    {
                        // shuntdown
                        for (y = x; y <= len; ++y)
                            phrase[y] = phrase[y + 1];
                        --len;
                    }
                    else phrase[x] = ' ';
                }
                ++x;
            }

            return StringFromChars(phrase);
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripNonDisplayable(string str)
        {
            int x, len = str.Length;
            string t = "";
            for (x = 0; x < len; ++x)
            {
                if (str[x] < 32 || str[x] > 126)
                { } // ignore - skip it
                else t += str[x];
            }

            return t;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string StripNoise(string p)
        {
            if (p.Equals("-"))
                return "";

            int len = p.Length;
            char[]
            phrase = new char[len + 1];
            StrToChars(phrase, p);

            char[] word = new char[102];
            int x = 0, y, z;
            while (x < len)
            {
                y = 0; z = x;
                while (x < len && y < 100 && phrase[x] != ' ')
                    word[y++] = phrase[x++];
                word[y] = '\0';
                ++x;

                while (x < len && phrase[x] != ' ')
                    ++x; ;
                ++x;

                if (Match(word, "a")
                   || Match(word, "an")
                   || Match(word, "the")
                   || Match(word, "by")
                   || Match(word, "x")
                   || Match(word, "in")
                   || Match(word, "for")
                   || Match(word, "from")
                   || Match(word, "at")
                   || Match(word, "and")
                   || Match(word, "or")
                   || Match(word, "to")
                   || Match(word, "tel:")
                   || Match(word, "tel")
                   || Match(word, "fax:")
                   || Match(word, "fax")
                  )
                {
                    // shuntdown
                    x = z;
                    z = Lengthchars(word, 0) + 1;
                    for (y = x; y <= len; ++y)
                        phrase[y] = phrase[y + z];
                    len -= z;
                }
            }

            return StringFromChars(phrase);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public int ServiceToInt(string s)
        {
            string t = "";
            int len = s.Length;
            for (int x = 0; x < len; ++x)
            {
                if (s[x] >= '0' && s[x] <= '9')
                    t += s[x];
            }

            return Convert.ToInt32(t);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        public string OrderBySize(char[] entries, char[] itemCodes, int numEntries, int maxEntrySize)
        {
            string notSorted = "", sortedMetric = "", sortedImperial = "";

            try
            {
                int x, numSortedMetric = 0, numSortedImperial = 0;
                char[] entry = new char[maxEntrySize];
                char[] itemCode = new char[21];
                char[] metricOrImperial = new char[1];
                double[] sizeValue = new double[1];
                string[] sortedMetricItems = new string[numEntries];
                string[] sortedImperialItems = new string[numEntries];
                double[] sortedMetricValues = new double[numEntries];
                double[] sortedImperialValues = new double[numEntries];

                for (x = 0; x < numEntries; ++x)
                {
                    if (GetListEntryByNum(x, entries, entry)) // just-in-case
                    {
                        if (IsASizeEntry(StringFromChars(entry), metricOrImperial, sizeValue))
                        {
                            GetListEntryByNum(x, itemCodes, itemCode);
                            if (metricOrImperial[0] == 'm')
                            {
                                sortedMetricItems[numSortedMetric] = StringFromChars(itemCode);
                                sortedMetricValues[numSortedMetric] = sizeValue[0];
                                ++numSortedMetric;
                            }
                            else
                            {
                                sortedImperialItems[numSortedImperial] = StringFromChars(itemCode);
                                sortedImperialItems[numSortedImperial] = Convert.ToString(sizeValue[0]);
                                ++numSortedImperial;
                            }
                        }
                        else // no size found
                        {
                            GetListEntryByNum(x, itemCodes, itemCode);
                            notSorted += (StringFromChars(itemCode) + "\001");
                        }
                    }
                }

                InsertionSort(sortedMetricValues, numSortedMetric, sortedMetricItems);

                for (x = 0; x < numSortedMetric; ++x)
                    sortedMetric += (sortedMetricItems[x] + "\001");

                InsertionSort(sortedImperialValues, numSortedImperial, sortedImperialItems);
                for (x = 0; x < numSortedImperial; ++x)
                    sortedImperial += (sortedImperialItems[x] + "\001");
            }
            catch (Exception e) { Console.WriteLine(e); }

            return sortedMetric + sortedImperial + notSorted;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        bool IsASizeEntry(string entry, char[] metricOrImperial, double[] sizeValue)
        {
            sizeValue[0] = 0;
            int i;

            if ((i = entry.IndexOf("mm", StringComparison.CurrentCulture)) != -1)
            {
                if ((i + 2) == entry.Length)
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), false);
                else
                if (entry.Substring(i + 2).StartsWith(".", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), false);
                else
                if (entry.Substring(i + 2).StartsWith(" ", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), false);

                if ((int)sizeValue[0] == -88888)
                    return false;

                metricOrImperial[0] = 'm';
                if ((int)sizeValue[0] != 0)
                    return true;
            }

            if ((i = entry.IndexOf("inches", StringComparison.CurrentCultureIgnoreCase)) != -1)
            {
                if ((i + 6) == entry.Length)
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);
                else
                if (entry.Substring(i + 6).StartsWith(".", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);
                else
                if (entry.Substring(i + 6).StartsWith(" ", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);

                if ((int)sizeValue[0] == -88888)
                    return false;

                metricOrImperial[0] = 'i';
                if ((int)sizeValue[0] != 0)
                    return true;
            }

            // entry: 1 Lufkin plumb bob 20 oz, brass, inch graduation 100422

            if ((i = entry.IndexOf("inch", StringComparison.CurrentCultureIgnoreCase)) != -1)
            {
                if ((i + 4) == entry.Length)
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);
                else
                if (entry.Substring(i + 4).StartsWith(".", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);
                else
                if (entry.Substring(i + 4).StartsWith(" ", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);

                if ((int)sizeValue[0] == -88888)
                    return false;

                metricOrImperial[0] = 'i';
                if ((int)sizeValue[0] != 0)
                    return true;
            }

            if ((i = entry.IndexOf("''", StringComparison.CurrentCultureIgnoreCase)) != -1)
            {
                if ((i + 2) == entry.Length)
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);
                else
                if (entry.Substring(i + 2).StartsWith(" ", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);

                if ((int)sizeValue[0] == -88888)
                    return false;

                metricOrImperial[0] = 'i';
                if ((int)sizeValue[0] != 0)
                    return true;
            }

            if ((i = entry.IndexOf("in.", StringComparison.CurrentCultureIgnoreCase)) != -1)
            {
                if ((i + 3) == entry.Length)
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);
                else
                if (entry.Substring(i + 3).StartsWith(".", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);
                else
                if (entry.Substring(i + 3).StartsWith(" ", StringComparison.CurrentCultureIgnoreCase))
                    sizeValue[0] = ExtractValue(entry.Substring(0, i), true);

                if ((int)sizeValue[0] == -88888)
                    return false;

                metricOrImperial[0] = 'i';
                if ((int)sizeValue[0] != 0)
                    return true;
            }

            return false;
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        double ExtractValue(string entryBit, bool imperial)
        {
            try
            {
                int x = (entryBit.Length - 1);
                while (x >= 0 && entryBit[x] == ' ')
                    --x;

                int z = x + 1;
                if (imperial)
                {
                    while (x >= 0 && ((entryBit[x] >= '0' && entryBit[x] <= '9') || entryBit[x] == '.' || entryBit[x] == ','
                                     || entryBit[x] == '/' || entryBit[x] == '-'))
                    {
                        --x;
                    }
                }
                else // metric
                {
                    while (x >= 0 && ((entryBit[x] >= '0' && entryBit[x] <= '9') || entryBit[x] == '.' || entryBit[x] == ','))
                    {
                        --x;
                    }
                }

                while (x < entryBit.Length && (entryBit[x] < '0' || entryBit[x] > '9'))
                    ++x;
                if (x < 0 && (entryBit[x] >= '0' && entryBit[x] <= '9'))
                    --x;

                double num = imperial ? ConvertImperial(entryBit.Substring(x, z)) : ConvertMetric(entryBit.Substring(x, z));
                return num;
            }
            catch (Exception)
            {
                return -88888;
            }
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        double ConvertImperial(string bit)
        {
            int x = 0, len = bit.Length;
            string integerBit = "", beforeBit = "", afterBit = "";
            while (x < len && bit[x] >= '0' && bit[x] <= '9')
                integerBit += bit[x++];

            if (x < len && (bit[x] == ' ' || bit[x] == '-' || bit[x] == '.'))
            {
                ++x;
                beforeBit = "";
                while (x < len && bit[x] != '/')
                    beforeBit += bit[x++];
                ++x;

                afterBit = "";
                while (x < len && bit[x] >= '0' && bit[x] <= '9')
                    afterBit += bit[x++];
            }
            else
            {
                beforeBit = integerBit;
                integerBit = "0";

                ++x;
                afterBit = "";
                while (x < len && bit[x] >= '0' && bit[x] <= '9')
                    afterBit += bit[x++];
            }

            if ((int)DoubleFromStr(afterBit) == 0)
                afterBit = "1";

            return DoubleFromStr(integerBit) + (DoubleFromStr(beforeBit) / DoubleFromStr(afterBit));
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        double ConvertMetric(string bit)
        {
            return DoubleFromStr(bit);
        }

        // -------------------------------------------------------------------------------------------------------------------------------------------------
        void InsertionSort(double[] array, int numElements, string[] itemCodes)
        {
            int j;
            for (int i = 1; i < numElements; i++)
            {
                for (j = 0; j < i; j++)
                {
                    if (array[i - j - 1] > array[i - j])
                        Swap(array, i - j - 1, i - j, itemCodes);
                    else j = i;
                }
            }
        }

        void Swap(double[] array, int x, int y, string[] itemCodes)
        {
            double d = array[x];
            array[x] = array[y];
            array[y] = d;

            string itemCode = itemCodes[x];
            itemCodes[x] = itemCodes[y];
            itemCodes[y] = itemCode;
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------
        public string DeNull(string s)
        {
            return s ?? "";
        }

        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // takes list (binary-1 separated) and returns (binary-1 separated) list of ordered positions within the original list
        // 000 000 000 000 000 000 000 000
        public string SortDecimalDot(string entries, int numEntries)
        {
            int x, y = 0, z, len, partLen;
            string entry, part, formattedPart, result = "";
            string[]
            array = new string[numEntries];

            for (x = 0; x < numEntries; ++x)
            {
                len = 0;
                entry = "";
                while (entries[y] != (char)1)
                {
                    entry += entries[y++];
                    ++len;
                }
                ++y;

                result = "";
                z = 0;
                while (z < len)
                {
                    part = "";
                    partLen = 0;
                    while (z < len && entry[z] != '.')
                    {
                        part += entry[z++];
                        ++partLen;
                    }
                    ++z;

                    formattedPart = partLen == 1 ? "00" + part : partLen == 2 ? "0" + part : partLen == 3 ? part : "000"; // just-in-case

                    result += formattedPart;
                }

                for (z = result.Length; z < 24; ++z)
                    result += "0";

                array[x] = result;
            }

            string[] array2 = new string[numEntries];
            for (x = 0; x < numEntries; ++x)
                array2[x] = array[x];

            InsertionSort(array, numEntries);

            string ret = "";

            for (x = 0; x < numEntries; ++x)
            {
                entry = array[x];

                for (z = 0; z < numEntries; ++z)
                {
                    if (array2[z].Equals(entry))
                    {
                        ret += (z + "\001");

                        z = numEntries;
                    }
                }
            }

            return ret;
        }

        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        public bool EntryInsertionSort(string[] entries, int numLines, int[] lineNums)
        {
            bool atleastOneSwapped = false;

            string[][] part = new string[numLines][]; // plenty
            for (int i = 0; i < 20; ++i)
                part[i] = new string[20];
            int maxParts = EntryInsertionBuildParts(entries, numLines, part);

            int j;
            for (int i = 1; i < numLines; i++)
            {
                for (j = 0; j < i; j++)
                {
                    if (EntryInsertionSortTest(part[i - j - 1], part[i - j], maxParts) > 0)
                    {
                        EntryInsertionSortSwap(entries, i - j - 1, i - j, lineNums);
                        atleastOneSwapped = true;
                        maxParts = EntryInsertionBuildParts(entries, numLines, part);
                    }
                    else j = i;
                }
            }

            return atleastOneSwapped;
        }

        int EntryInsertionSortTest(string[] entry1, string[] entry2, int maxParts)
        {
            int res, i1, i2;
            bool tryInt;

            for (int x = 0; x <= maxParts; ++x)
            {
                tryInt = false;

                tryInt |= (IsInteger(entry1[x]) && IsInteger(entry2[x]));

                if (tryInt)
                {
                    i1 = StrToInt(entry1[x]);
                    i2 = StrToInt(entry2[x]);

                    res = i1 < i2 ? -1 : i1 > i2 ? 1 : 0;
                }
                else res = String.Compare(entry1[x], entry2[x]);

                if (res != 0)
                    return res;
            }

            return 0;
        }

        int EntryInsertionBuildParts(string[] entries, int numLines, string[][] part)
        {
            int x, y, len, partNum, maxParts = 0;

            for (x = 0; x < numLines; ++x)
                for (y = 0; y < 20; ++y)
                    part[x][y] = "";

            for (int count = 0; count < numLines; ++count)
            {
                x = 0;
                partNum = 0;
                len = entries[count].Length;

                while (x < len)
                {
                    if (entries[count][x] >= '0' && entries[count][x] <= '9')
                    {
                        while (x < len && entries[count][x] >= '0' && entries[count][x] <= '9')
                            part[count][partNum] += entries[count][x++];
                    }
                    else
                    {
                        while (x < len && (entries[count][x] < '0' || entries[count][x] > '9'))
                            part[count][partNum] += entries[count][x++];
                    }

                    ++partNum;
                    if (partNum > maxParts)
                        maxParts = partNum;
                }
            }

            return maxParts;
        }

        void EntryInsertionSortSwap(string[] entries, int x, int y, int[] lineNums)
        {
            string s = entries[x];
            entries[x] = entries[y];
            entries[y] = s;

            int lineNum = lineNums[x];
            lineNums[x] = lineNums[y];
            lineNums[y] = lineNum;
        }
    }
}
