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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handler for registering visitors (uploading scanned data)
 */

class AdminRegister implements AdminHandler
{
    Connection dbconn = null;
    PreparedStatement getFileStatement = null;

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

	writer.println( "<h2>Registrering av bes&oslash;kende</h2>" );
	writer.println( "<p>" );

	String inputIdString = req.getParameter( "register_input" );
	if( inputIdString == null || inputIdString.equals("") ) {
	    writer.println( "<form method=\"post\" action=\"upload\" enctype=\"multipart/form-data\">" );
	    writer.println( "<input type=\"file\" name=\"register_input\" size=\"48\"/>" );
	    writer.println( "<input type=\"submit\" value=\"Registrere\"/>" );
	    writer.println( "</form>" );
	    writer.println( "<br/>" );
	    writer.println( "<form method=\"post\" action=\"\">" );
	    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"reset\"/>" );
	    writer.println( "<input type=\"submit\" value=\"Avbryt\"/>" );
	    writer.println( "</form>" ); 
	}
	else {
	    try {
		if( getFileStatement == null )
		    getFileStatement = dbconn.prepareStatement( "SELECT contents FROM fileobject WHERE id=?" );

		writer.println( "<form method=\"post\" action=\"\">" );
		writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"reset\"/>" );
		writer.println( "<input type=\"submit\" value=\"Til administrasjonsside\"/>" );
		writer.println( "</form>" );

		int fileId = Integer.parseInt( inputIdString );
		
		getFileStatement.setInt( 1, fileId );
		getFileStatement.executeQuery();
		byte[] fileContents = null;
		ResultSet result = getFileStatement.executeQuery();
		if( result != null ) {
		    result.first();
		    fileContents = result.getBytes( "contents" );
		    result.close();
		}

		VisitorParser p = new VisitorParser( fileContents );
		writer.println( "<table>" );

		while( p.parseNextRow() ) {

		    writer.println( "<tr>" );
		    writer.println( "<td>" );
		    writer.print( "" + p.getColumnParticipant() );
		    writer.print( "," + p.getColumnDate() );
		    writer.print( "," + p.getColumnTime() );
		    writer.println( "," + p.getColumnScanner() );
		    writer.println( "</td>" );

		    // insert visit
		    try {
			Statement visitStatement = dbconn.createStatement();
			visitStatement.executeUpdate( "INSERT INTO visit (date,time,participant_id,scanner_id) VALUES ('" + p.getColumnDate() + "','" + p.getColumnTime() + "'," + p.getColumnParticipant() + "," + p.getColumnScanner() + ")" );
		    }
		    catch( SQLException e ) {
			writer.println( "<td>" );
			writer.println( "<font color=\"red\">" );
			writer.println( "FEIL" );
			writer.println( "</font>" );
			writer.println( "<!--" );
			writer.println( e.toString() );
			writer.println( "-->" );
			writer.println( "</td>" );
		    }

		    writer.println( "</tr>" );
		}

		writer.println( "</table>" );
	    }
	    catch( SQLException e ) {
		throw new ServletException( e );
	    }
	}

	writer.println( "</p>" );
    }

    AdminRegister( Connection c )
    {
	dbconn = c;
    }

}
