/*
 * Copyright 2005 FreeCode AS
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * Or get it at http://www.gnu.org/licenses/gpl.html
 *
 */

package com.freecode.eventmgr;

import java.sql.*;
import java.util.Enumeration;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.Barcode39;

/**
 * Servlet for creating participant badges
 */

public final class PdfServlet extends EventServlet {

    final float badgeSizeY = 1.77f * 72;//2.25f * 72;
    final float badgeSizeX = 3 * 72; //4 * 72;

    PreparedStatement logoStatement = null;

    /**
     * Make the actual badge
     * @param document pdf document we write to
     * @param badgeTemplate information about the badge we are writing
     * @param cb pdf content
     * @param x x position
     * @param y y position
     * @param participantID primary key identifying the participant
     * @param logo badge logo primary key
     */

    private void makeBadge( Document document, Badge badgeTemplate, PdfContentByte cb, int x, int y, int participantId, int logo )
	throws DocumentException, IOException, SQLException
    {
	BaseFont bbf = BaseFont.createFont( BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED );
	BaseFont nbf = BaseFont.createFont( BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED );
	cb.beginText();
	cb.setFontAndSize( bbf, 20 );
	cb.showTextAligned( PdfContentByte.ALIGN_LEFT, badgeTemplate.getFieldStringValue(0), x + 2, y + 100, 0 );
	cb.setFontAndSize( nbf, 10 );
	cb.showTextAligned( PdfContentByte.ALIGN_LEFT, badgeTemplate.getFieldStringValue(1) + " " + badgeTemplate.getFieldStringValue(2), x + 2, y + 80, 0 );
	cb.setFontAndSize( bbf, 12 );
	cb.showTextAligned( PdfContentByte.ALIGN_LEFT, badgeTemplate.getFieldStringValue(3), x + 2, y + 62, 0 );
	cb.setFontAndSize( nbf, 10 );
	cb.showTextAligned( PdfContentByte.ALIGN_LEFT, badgeTemplate.getFieldStringValue(4), x + 2, y + 45, 0 );
	cb.endText();

	// Add the barcode
	Barcode39 barcode = new Barcode39();
	barcode.setCode( "" + participantId );
	PdfTemplate barcodeTemplate = 
	    barcode.createTemplateWithBarcode( cb, null, null );
	cb.addTemplate( barcodeTemplate, x + badgeSizeX/2 - barcodeTemplate.getWidth()/2, y + 2 );

	// Add logo
	byte[] logoBytes = null;
	if( logoStatement == null )
	    logoStatement = dbconn.prepareStatement( "SELECT contents FROM fileobject WHERE id=?" );

	logoStatement.setInt( 1, logo );
	ResultSet result = logoStatement.executeQuery();
	if( result != null ) {
	    result.first();
	    logoBytes = result.getBytes( 1 );
	}
	result.close();

	Image logoImage = Image.getInstance( logoBytes );
	logoImage.setAbsolutePosition( x+badgeSizeX-logoImage.scaledWidth(), y+badgeSizeY-logoImage.scaledHeight() );
	document.add( logoImage );
    }

    /**
     * Makes the pdf document
     */

    protected void doRequest( HttpServletRequest request,
			      HttpServletResponse response )
	throws IOException, ServletException {

	HttpSession session = request.getSession(); 
	AdminEventInfo eventInfo = 
	    (AdminEventInfo)session.getAttribute( "pdfuserevent" );

	if( eventInfo == null /* || eventInfo.getParticipantId() <= 0 */ )
	    return;

	//session.removeAttribute( "pdfuserevent" );

	boolean isInternal = session.getAttribute("internal") != null;
	
	Badge badgeTemplate = eventInfo.getBadge();

	response.setContentType( "application/pdf" );

	// Make pdf
	Document document;
	if( isInternal ) {
	    // if internal we have a label printer
	    Rectangle size = new Rectangle( badgeSizeX + 72, badgeSizeY );
	    document = new Document( size, 0, 0, 0, 0 );
	}
	else
	    // else we assume standard A4 page size
	    document = new Document( PageSize.A4, 72, 72, 72, 72 );
	try {
	    // step 2:
	    // we create a writer that listens to the document
	    // and directs a PDF-stream to a file
	    PdfWriter writer = PdfWriter.getInstance( document, response.getOutputStream() );
	    document.open();

	    // set "this.print(false)" to auto-print
	    if( isInternal )
		writer.addJavaScript( "this.print(false);", false );
	    else
		writer.addJavaScript( "this.print(true);", false );
	    
	    PdfContentByte cb = writer.getDirectContent();

	    // Make badge
	    int xpos = 72;
	    int ypos = 0;
	    if( !isInternal ) {
		xpos = 100;
		ypos = 100;
		cb.rectangle( xpos, ypos, badgeSizeX, badgeSizeY );
		cb.stroke();
	    }

	    try {
		makeBadge( document, badgeTemplate, cb, xpos, ypos, eventInfo.getParticipantId(), eventInfo.getBadgeLogoId() );
	    }
	    catch( SQLException e ) {
		throw new ServletException( e );
	    }

	    if( !isInternal ) {

		// Write some info to participant
		document.add( new Paragraph( "Takk for at du registrerte deg til " + eventInfo.getEventName() ));

		Rectangle border = new Rectangle( 0, 0 );
		//border.setBorderColor( Color.WHITE );
		border.setBorderWidthLeft( 0 );
		border.setBorderWidthBottom( 0 );
		border.setBorderWidthRight( 0 );
		border.setBorderWidthTop( 0 );

		float[] tableWidths = { 0.3f, 0.7f };
		PdfPTable table = new PdfPTable( tableWidths );
		Paragraph cellText = new Paragraph( "Deltagernummer:" );
		PdfPCell cell = new PdfPCell( cellText );
		cell.setHorizontalAlignment( Element.ALIGN_RIGHT );
		cell.cloneNonPositionParameters( border );
		table.addCell( cell );

		cellText = new Paragraph( "" + eventInfo.getParticipantId() );
		cell = new PdfPCell( cellText );
		cell.setHorizontalAlignment( Element.ALIGN_LEFT );
		cell.cloneNonPositionParameters( border );
		table.addCell( cell );

		Enumeration e = eventInfo.getUserFields();
		while( e.hasMoreElements() ) {
		    AdminEventField f = (AdminEventField)e.nextElement();

		    cellText = new Paragraph( f.getName() + ":" );
		    cell = new PdfPCell( cellText );
		    cell.setHorizontalAlignment( Element.ALIGN_RIGHT );
		    cell.cloneNonPositionParameters( border );
		    table.addCell( cell );

		    cellText = new Paragraph( f.getStringValue() );
		    cell = new PdfPCell( cellText );
		    cell.setHorizontalAlignment( Element.ALIGN_LEFT );
		    cell.cloneNonPositionParameters( border );
		    table.addCell( cell );
		}

		table.setHorizontalAlignment( Element.ALIGN_LEFT );
		table.setSpacingBefore( 15 );
		table.setSpacingAfter( 15 );
		document.add( table );	    
	    }
	} 
	catch( DocumentException e ) {
	    throw new ServletException( e );
	}

	document.close();
    }
}
