// =======================================================================================================================================================================================================
// System: ZaraStar Utils: Create PDF BlogGuide
// Module: BlogsBlogGuideCreatePDFProcess.java
// Author: C.K.Harvey
// Copyright (c) 1999-2009 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class BlogsBlogGuideCreatePDFUpdate extends PdfPageEventHelper
{
  protected PdfPTable header;
  protected BaseFont helv;
  protected PdfTemplate total;
  protected PdfPCell cell;

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @Override
  public void onOpenDocument(PdfWriter writer, Document document)
  {
    try
    {
      total = writer.getDirectContent().createTemplate(100, 100);

      total.setBoundingBox(new Rectangle(-20, -20, 100, 100));

      helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }
    catch(Exception e) { throw new ExceptionConverter(e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
  public void onEndPage(PdfWriter writer, Document document)
  {
    try
    {
      PdfContentByte cb = writer.getDirectContent();

      cb.saveState();
      String text = "Page " + writer.getPageNumber() + " of ";
      float textBase = document.bottom() - 20;
      float textSize = helv.getWidthPoint(text, 12);
      cb.beginText();
      cb.setFontAndSize(helv, 12);
      float adjust = helv.getWidthPoint("0", 12);
      cb.setTextMatrix(document.right() - textSize - adjust, textBase);
      cb.showText(text);
      cb.endText();
      cb.addTemplate(total, document.right() - adjust, textBase);
      cb.restoreState();

      if(document.getPageNumber() > 1)
      {
          //    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, header, document.right(), document.top() + 10, 0);
if(header != null)

    header.writeSelectedRows(0, -1, document.leftMargin(), document.top() + 20, cb);
      
      }
    }
    catch(Exception e) { throw new ExceptionConverter(e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void onCloseDocument(PdfWriter writer, Document document)
  {
    try
    {
      total.beginText();
      total.setFontAndSize(helv, 12);
      total.setTextMatrix(0, 0);
      total.showText(String.valueOf(writer.getPageNumber() - 1));
      total.endText();
    }
    catch(Exception e) { throw new ExceptionConverter(e); }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void headerFooter(String headerText, Document document)
  {
    try
    {
      header = new PdfPTable(1);
      header.setWidthPercentage(100);
      float width = document.getPageSize().getWidth();
      header.setTotalWidth(width);

      cell = new PdfPCell(new Paragraph(headerText));
      cell.setVerticalAlignment(Element.ALIGN_CENTER);
      cell.setHorizontalAlignment(Element.ALIGN_LEFT);
      cell.setGrayFill(0.8f);
      cell.setColspan(1);
      
      header.addCell(cell);
    }
    catch(Exception e) { }
  }

}
