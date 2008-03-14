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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Main servlet for user registration pages. Write user fields and
 * store choices if filled out. Then redirect to badge writer.
 */

public final class UserServlet extends EventServlet {

    protected void doRequest( HttpServletRequest request,
			      HttpServletResponse response )
	throws IOException, ServletException {

	HttpSession session = request.getSession(); 
	AdminEventInfo eventInfo = null;
	boolean isInternal = false;

	if( request.getParameter("reset") != null ) { // reset users session
	    session.removeAttribute( "internal" );
	    session.removeAttribute( "userevent" );
	}
	else
	    eventInfo = (AdminEventInfo)session.getAttribute( "userevent" );

	isInternal = request.getParameter("internal") != null || session.getAttribute("internal") != null;

	if( isInternal ) {
	    session.setMaxInactiveInterval( 60*60*24 ); // 24 hours
	    session.setAttribute( "internal", "true" );
	}

	response.setContentType("text/html");
	PrintWriter writer = response.getWriter();

	// Write header

	writer.println( "<html>" );
	writer.println( "<head>" );
	writer.println( "<title>user</title>" );
	writer.println( "<link rel=\"stylesheet\" href=\"css\"/>" );
	writer.println( "</head>" );
	writer.println( "<body>" );
	writer.println( "<img src=\"img?kind=logo\"/>" );
	writer.println( "<h1>Messeregistrering</h1>" );
	writer.println( "<p>" );

	if( eventInfo == null ) {
	    String eventCodeString = request.getParameter( "event_code" );
	    if( eventCodeString != null && 
		!eventCodeString.equals("") ) {
		boolean success = true;
		eventInfo = new AdminEventInfo();
		int eventId = -1;
		try {
		    eventId = EventCode.getId( eventCodeString.trim() );
		}
		catch( NumberFormatException e ) {
		    success = false;
		}
		
		if( success )
		    try {
			success = eventInfo.dbRestore( dbconn, eventId );
		    }
		    catch( SQLException e ) {
			success = false;
		    }

		if( !success ) {
		    writer.println( "Ugyldig messekode" ); 
		    eventInfo = null;
		}
		else {
		    session.setAttribute( "event_code", eventCodeString );
		    session.setAttribute( "userevent", eventInfo );
		    session.setAttribute( "newuser", new Boolean(true) );
		}
	    }
	}

	if( eventInfo == null ) {
	    writer.println( "<form method=\"post\" action=\"\">" );
	    writer.println( "Messekode: " );
	    writer.println( "<input type=\"text\" name=\"event_code\"/>" );
	    writer.println( "<input type=\"submit\" value=\"OK\"/>" );
	    writer.println( "</form>" ); 
	}
	else if( request.getParameter("save") != null ) {
	    // Check that we have all answers
	    boolean missing = false;
	    Enumeration e = eventInfo.getUserFields();

	    while( e.hasMoreElements() ) {
		AdminEventField f = (AdminEventField)e.nextElement();

		if( f.isEmpty() && !f.optional )
		    missing = true;
	    }

	    if( !missing ) {
		try {
		    eventInfo.dbStoreParticipant( dbconn );
		}
		catch( SQLException ex ) {
		    throw new ServletException( ex );
		}

		if( eventInfo.getParticipantId() > 0 ) {
		    session.setAttribute( "pdfuserevent", eventInfo );
		    session.removeAttribute( "userevent" );
		    writer.println( "</p>" );

		    writer.println( "<p>" );
		    writer.println( "Takk for at du registrerte deg! Ditt registreringsnummer er " + eventInfo.getParticipantId() + "<br/><br/>" );

		    writer.println( "Registreringen din skal skrives ut automatisk til din skriver.<br/><br/>" );

		    writer.println ( "Dersom dette ikke skjer, m&aring; du h&oslash;yre-klikke" );
		    writer.println ( "<a href=\"badge.pdf\" target=\"_blank\">her</a> og trykke p&aring; \"lagre som\"." );
		    writer.println ( "&Aring;pne s&aring; adgangskortet med hjelp av Acrobat Reader. Hvis du ikke har Acrobat Reader, kan du laste det ned" );
		    writer.println ( "<a href=\"http://www.adobe.com/products/acrobat/readstep.html\" target=\"_blank\">her</a>.<br/><br/>" );

		    writer.println ( "Husk &aring; ta med adgangskortet ditt n&aring;r du kommer til messen.<br/><br/>" );

		    writer.println ( "Velkommen!" );

		    writer.println( "</p>" );
		    writer.println( "<p>" );
		    writer.println( "<form method=\"post\" action=\"user\">" );

		    if( isInternal ) {
			writer.println( "<input type=\"hidden\" name=\"event_code\" value=\"" + session.getAttribute("event_code") + "\"/>" );
			writer.println( "<input type=\"hidden\" name=\"internal\" value=\"true\"/>" );
			writer.println( "<input type=\"submit\" value=\"OK\"/>" );
		    }
		    else {
			writer.println( "<input type=\"hidden\" name=\"reset\" value=\"true\"/>" );
			writer.println( "<input type=\"submit\" value=\"Avslutt\"/>" );			
		    }
		    
		    writer.println( "</form>" );
		    writer.println( "</p>" );
		    writer.println( "<p>" );
		    writer.println( "<iframe src=\"badge.pdf\" width=\"100%\" height=\"75%\"/>" );
		}
	    }
	    else {
		writer.println( "Registrering feilet." );
	    }

	    //session.removeAttribute( "userevent" );
	}
	else {
	    writer.println( "</p>" );
	    writer.println( "<h2>" );
	    writer.println( eventInfo.getEventName() );
	    writer.println( "</h2>" );
	    writer.println( "<p>" );
	    writer.println( "<table>" );
	    writer.println( "<form method=\"post\" action=\"\">" );

	    boolean newUser = false;
	    if( session.getAttribute("newuser") != null ) {
		newUser = true;
		session.removeAttribute( "newuser" );
	    }

	    boolean missing = false;
	    Enumeration e = eventInfo.getUserFields();

	    while( e.hasMoreElements() ) {
		AdminEventField f = (AdminEventField)e.nextElement();

		String inputValue = 
		    request.getParameter( f.getInternalName() );

		if( inputValue != null && !inputValue.equals("") )
		    f.setStringValue( inputValue );

		writer.println( "<tr>" );
		writer.println( f.getAsHtml() );
		writer.println( "</tr>" );

		if( f.isEmpty() && !f.optional )
		    missing = true;
	    }

	    writer.println( "</tr>" );
	    writer.println( "<tr>" );
	    writer.println( "<td/>" );
	    writer.println( "<td>" );
	    writer.println( "<input type=\"submit\" value=\"Oppdater\"/>" );
	    writer.println( "</td>" );
	    writer.println( "</tr>" );
	    writer.println( "</form>" );

	    writer.println( "<form method=\"post\" action=\"\">" );
	    writer.println( "<tr>" );
	    writer.println( "<td/>" );
	    writer.println( "<td>" );
	    if( isInternal ) {
		writer.println( "<input type=\"hidden\" name=\"event_code\" value=\"" + session.getAttribute("event_code") + "\"/>" );
		writer.println( "<input type=\"hidden\" name=\"internal\" value=\"true\"/>" );
	    }
	    writer.println( "<input type=\"hidden\" name=\"reset\"/>" );
	    writer.println( "<input type=\"submit\" value=\"Avbryt\"/>" );
	    writer.println( "</td>" );
	    writer.println( "</tr>" );
	    writer.println( "</form>" );

	    if( missing && !newUser ) {
		writer.println( "<tr>" );
		writer.println( "<td/>" );
		writer.println( "<td>" );
		writer.println( "Du m&aring; fylle ut alle feltene som er merket med (*)" );
		writer.println( "</td>" );
		writer.println( "</tr>" );
	    }
	    else if( !missing ) {
		// Lagre
		writer.println( "<form method=\"post\" action=\"\">" );
		writer.println( "<tr>" );
		writer.println( "<td/>" );
		writer.println( "<td>" );
		writer.println( "<input type=\"hidden\" name=\"save\"/>" );
		writer.println( "<input type=\"submit\" value=\"Send registrering\"/>" );
		writer.println( "</td>" );
		writer.println( "</tr>" );
		writer.println( "</form>" );
	    }

	    writer.println( "</table>" );
	}
	
	writer.println( "</p>" );

	// Write debug

	if( debug ) {

	    writer.println( "<p>" );
	    writer.println( "<hr/>" );
	    Enumeration e = request.getParameterNames();
	    while( e.hasMoreElements() ) {
		String name = (String)e.nextElement();
		writer.println( "<emph>" + name + "</emph>: " +
				request.getParameter(name) );
		writer.println( "<br/>" );
	    }
	    writer.println( "</p>" );
	}

	// Write footer

	writer.println( "</body>" );
	writer.println( "</html>" );
    }
}
