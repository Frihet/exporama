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
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handler for viewing events and ui for creating new events
 */

class AdminView implements AdminHandler
{
    Connection dbconn = null;
    Random seedGenerator = new Random( System.currentTimeMillis() );

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

	try {
	    statement = dbconn.createStatement();
	}
	catch( SQLException e ) {
	    throw new ServletException( e );
	}

	writer.println( "<h2>Opprett</h2>" );
	writer.println( "<p>" );
	writer.println( "<form method=\"post\" action=\"\">" );
	writer.println( "<select name=\"template\">" );

	// Get available templates from db
	try {
	    ResultSet results = statement.executeQuery( "SELECT id,name FROM inputtemplate ORDER BY id" );
	    if( results != null ) {
		while( results.next() ) {
		    writer.print( "<option value=\"" );
		    writer.print( results.getInt("id") );
		    writer.print( "\">" );
		    writer.print( results.getString("name") );
		    writer.println( "</option>" );
		} 
	    }
	    results.close();
	}
	catch( SQLException e ) {
	    throw new ServletException( "dberror: " + e.toString() );
	}

	writer.println( "</select>" );
	writer.println( "<input type=\"submit\" value=\"Ny\"/>" );
	writer.println( "<input type=\"hidden\" name=\"create\"/>" );
	writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"edit\"/>" );
	writer.println( "</form>" );
	writer.println( "</p>" );

	if( req.getParameter("open") != null )
	    try {
		String idString = req.getParameter( "event_id" );
		if( idString != null && !idString.equals("") ) {
		    statement.executeUpdate( "UPDATE event SET state='R' WHERE id=" + Integer.parseInt(idString) + " AND state='I'" );
		}
	    }
	    catch( SQLException e ) {
		throw new ServletException( "dberror: " + e.toString() );
	    }

	if( req.getParameter("remove") != null )
	    try {
		String idString = req.getParameter( "event_id" );
		if( idString != null && !idString.equals("") ) {
		    statement.executeUpdate( "UPDATE event SET state='D' WHERE id=" + Integer.parseInt(idString) );
		}
	    }
	    catch( SQLException e ) {
		throw new ServletException( "dberror: " + e.toString() );
	    }


	writer.println( "<h2>Aktuelle</h2>" );
	writer.println( "<p>" );

	writer.println( "<table>" );

	//
	//  N y e
	//

        writer.println( "<tr>" );
	writer.println( "<th class=\"heading\" colspan=\"4\">Nye</th>" );
	writer.println( "</tr>" );
        writer.println( "<tr>" );
	writer.println( "<th>Kode</th><th>Navn</th><th>Tidsrom</th><th>Administrator</th>" );
	writer.println( "</tr>" );

	try {
	    ResultSet results = statement.executeQuery( "SELECT id,name,startdate,enddate,administrator FROM event WHERE state='I'" );
	    if( results != null )
		while( results.next() ) {
		    int id = results.getInt( "id" );

		    writer.println( "<tr>" );
		    writer.println( getResultAsHtml(results) );
		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Endre\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"edit\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );
		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Klar\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"open\" value=\"true\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"remove\" value=\"true\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );
		    writer.println( "</tr>" );
		}

	    results.close();
	}
	catch( SQLException e ) {
	    throw new ServletException( "dberror: " + e.toString() );
	}

	//
	// K l a r e
	//

        writer.println( "<tr>" );
	writer.println( "<th class=\"heading\" colspan=\"4\">Klare</th>" );
	writer.println( "</tr>" );
        writer.println( "<tr>" );
	writer.println( "<th>Kode</th><th>Navn</th><th>Tidsrom</th><th>Administrator</th>" );
	writer.println( "</tr>" );

	try {
	    ResultSet results = statement.executeQuery( "SELECT id,name,startdate,enddate,administrator FROM event WHERE state='R' AND startdate > date_trunc('day',now())" );
	    if( results != null )
		while( results.next() ) {
		    int id = results.getInt( "id" );

		    writer.println( "<tr>" );
		    writer.println( getResultAsHtml(results) );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Rapport\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"report\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"remove\" value=\"true\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "</tr>" );
		}

	    results.close();
	}
	catch( SQLException e ) {
	    throw new ServletException( "dberror: " + e.toString() );
	}


	//
	// A k t i v e
	//

        writer.println( "<tr>" );
	writer.println( "<th class=\"heading\" colspan=\"4\">Aktive</th>" );
	writer.println( "</tr>" );
        writer.println( "<tr>" );
	writer.println( "<th>Kode</th><th>Navn</th><th>Tidsrom</th><th>Administrator</th>" );
	writer.println( "</tr>" );

	try {
	    ResultSet results = statement.executeQuery( "SELECT id,name,startdate,enddate,administrator FROM event WHERE state='R' AND enddate >= date_trunc('day',now()) AND startdate <= date_trunc('day',now())" );
	    if( results != null )
		while( results.next() ) {
		    int id = results.getInt( "id" );

		    writer.println( "<tr>" );
		    writer.println( getResultAsHtml(results) );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Rapport\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"report\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Registrere bes&oslash;kende\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"register\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"remove\" value=\"true\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "</tr>" );
		}

	    results.close();
	}
	catch( SQLException e ) {
	    throw new ServletException( "dberror: " + e.toString() );
	}


	//
	// A v s l u t t e t
	//

        writer.println( "<tr>" );
	writer.println( "<th class=\"heading\" colspan=\"4\">Nylig avsluttet</th>" );
	writer.println( "</tr>" );
        writer.println( "<tr>" );
	writer.println( "<th>Kode</th><th>Navn</th><th>Tidsrom</th><th>Administrator</th>" );
	writer.println( "</tr>" );

	try {
	    ResultSet results = statement.executeQuery( "SELECT id,name,startdate,enddate,administrator FROM event WHERE state='R' AND enddate < date_trunc('day',now())" );
	    if( results != null )
		while( results.next() ) {
		    int id = results.getInt( "id" );

		    writer.println( "<tr>" );
		    writer.println( getResultAsHtml(results) );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Rapport\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"report\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Registrere bes&oslash;kende\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"cmd\" value=\"register\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );

		    writer.println( "<td>" );
		    writer.println( "<form method=\"post\" action=\"\">" );
		    writer.println( "<input type=\"submit\" value=\"Fjerne\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"event_id\" value=\"" + id + "\"/>" );
		    writer.println( "<input type=\"hidden\" name=\"remove\" value=\"true\"/>" );
		    writer.println( "</form>" );
		    writer.println( "</td>" );
		    writer.println( "</tr>" );
		}

	    results.close();
	}
	catch( SQLException e ) {
	    throw new ServletException( "dberror: " + e.toString() );
	}


	writer.println( "</table>" );
	writer.println( "</p>" );
    }

    String getResultAsHtml( ResultSet result )
	throws SQLException
    {
	String htmlString = "";
	String dateString = "";

	htmlString += "<td align=\"right\">";
	int id = result.getInt( "id" );
	htmlString += "<code>";
	htmlString += EventCode.makeCode( seedGenerator.nextLong(), id );
	htmlString += "</code>";
	htmlString += "</td>";

	htmlString += "<td>";
	htmlString += result.getString( "name" );
	htmlString += "</td>";
	htmlString += "<td>";

	dateString = result.getString( "startdate" );
	htmlString += (dateString.split( "\\s" ))[0];
	htmlString += " - ";
	dateString = result.getString( "enddate" );
	htmlString += (dateString.split( "\\s" ))[0];

	htmlString += "</td>";
	htmlString += "<td>";
	htmlString += result.getString( "administrator" );
	htmlString += "</td>";

	return htmlString;
    }

    /*
    ResultSet queryEvents( char state )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();
	ResultSet results = statement.executeQuery( "SELECT id,name,startdate,enddate,administrator FROM event WHERE state='" + state + "'" );

	return results;
    }
    */
    AdminView( Connection c )
    {
	dbconn = c;
    }
}
