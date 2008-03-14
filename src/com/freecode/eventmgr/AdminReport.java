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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handler that creates visitor reports
 */

class AdminReport implements AdminHandler
{
    Connection dbconn = null;

    /**
     * Entrypoint from (admin) servlet. Does everything, and needs
     * some splitting up
     * @param req request passed on from invoking servlet
     * @param resp response passed on from invoking servlet
     * @throws ServletException for an invalid request
     * @throws IOException (passed on)
     */

    public void entry( HttpServletRequest req,  HttpServletResponse resp )
	throws IOException, ServletException
    {
	PrintWriter writer = resp.getWriter();
	Statement statement = null;

	String eventIDString = req.getParameter( "event_id" );
	if( eventIDString == null || eventIDString.equals("") )
	    return;

	int eventID = Integer.parseInt( eventIDString );

	try {
	    statement = dbconn.createStatement();
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}

	AdminEventInfo eventInfo = new AdminEventInfo();
	boolean success = false;
	try {
	    success = eventInfo.dbRestore( dbconn, eventID );
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}

	if( !success )
	    return;

	// Write header
	writer.print( "Deltagernummer,Registreringstidspunkt," );
	try {
	    ResultSet results = statement.executeQuery( "SELECT title FROM inputfield WHERE event_id=" + eventID );
	    if( results != null ) {
		while( results.next() )
		    writer.print( results.getString("title") + "," );
	    }
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}
	writer.println( "Bes\u00F8k" );

	// Get from db
	try {
	    ResultSet results = statement.executeQuery( "SELECT id,date FROM participant WHERE event_id=" + eventID + " ORDER BY id" );
	    if( results != null ) {
		while( results.next() ) {
		    int pid = results.getInt( "id" );
		    writer.print( pid );
		    writer.print( "," );
		    writer.print( results.getString("date") );

		    Enumeration e = eventInfo.getUserFields();
		    while( e.hasMoreElements() ) {
			AdminEventField f = 
			    (AdminEventField)e.nextElement();

			Statement choiceStatement = dbconn.createStatement();
			ResultSet choiceResult = choiceStatement.executeQuery( "SELECT data,menuelement_id FROM participantchoice WHERE inputfield_id=" + f.id + "AND participant_id=" + pid );

			if( choiceResult != null ) {
			    choiceResult.first();
			    writer.print( "," );
			    if( f instanceof AdminEventTextField ||
				f instanceof AdminEventCheckboxField ) {
				f.setStringValue( choiceResult.getString("data") );
				writer.print( f.getStringValue() );
			    }
			    else if( f instanceof AdminEventMenuContainerField ) {
				int meid = choiceResult.getInt( "menuelement_id" );
				choiceResult = choiceStatement.executeQuery( "SELECT title FROM menuelement WHERE id=" + meid );
				if( choiceResult != null ) {
				    choiceResult.first();
				    writer.print( choiceResult.getString("title") );
				}
			    }
			    
			    choiceResult.close();
			}

		    }

		    // visits
		    Statement visitStatement = dbconn.createStatement();
		    ResultSet visitResult = visitStatement.executeQuery( "SELECT date FROM visit WHERE participant_id=" + pid );
		    
		    if( visitResult != null ) {
			while( visitResult.next() )
			    writer.print( "," + 
					  visitResult.getString("date") );
			visitResult.close();
		    }

		    writer.println( "" );
		}
	    }
	    results.close();
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}

	writer.println( "" );
    }

    AdminReport( Connection c )
    {
	dbconn = c;
    }
}
