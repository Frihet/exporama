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

/**
 * Our abstract view of a simple checkbox
 */

class AdminEventCheckboxField extends AdminEventField
{
    boolean value;

    /**
     * Setting the checkbox value, as a string
     * @param v checkbox string value (true or on, if it's selected)
     */

    void setStringValue( String v )
    {
	value = v.equalsIgnoreCase("true") || v.equalsIgnoreCase("on");
    }

    /**
     * The current state of the checkbox as a string
     * @return "true" if it's selected, "false" otherwise
     */

    String getStringValue()
    {
	return value ? "true" : "false";
    }

    /**
     * Does not apply for this field, but is required by the interface
     */

    boolean isEmpty()
    {
	return false;
    }

    /**
     * Html markup for this checkbox
     * @return Html for the checkbox
     */

    String getAsHtml()
    {
	String htmlStr = "<td align=\"right\">" + name;

	if( isEmpty() && !optional )
	    htmlStr += "(*)";

	htmlStr += "</td><td><input type=\"checkbox\" name=\"" + 
	    getInternalName() + "\"";

	if( value )
	    htmlStr += " checked";

	htmlStr += "/></td>";

	return htmlStr;
    }

    /**
     * Write the users selection to the database
     * @param dbconn database connection
     * @param participantID a number identifying the participant whos selection we are storing
     * @throws SQLException if database error occurs
     */

    void dbStoreParticipant( Connection dbconn, int participantID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();

	statement.executeUpdate( "INSERT INTO participantchoice (data,inputfield_id,participant_id) VALUES ('" + getStringValue() + "'," + id + "," + participantID + ")" );
    }

    /**
     * Create or update this field in the database, connected to an event
     * @param dbconn database connection
     * @param parentID the event this field belongs to
     * @throws SQLException if database error occurs
     */

    void dbCreateOrUpdate( Connection dbconn, int parentID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();
	ResultSet result;

	if( id == 0 ) {
	    result = statement.executeQuery( "SELECT nextval('inputfield_id_seq')" );
	    result.first();
	    id = result.getInt( "nextval" );
	    result.close();

	    statement.executeUpdate( "INSERT INTO inputfield (id,type,event_id) VALUES (" + id + ",'C'," + parentID  + ")" );
	}
	
	statement.executeUpdate( "UPDATE inputfield SET title='" + name + "', optional='" + optional + "', badge=" + badgeFieldIndex + " WHERE id=" + id );
    }

    /**
     * We load an instance of a field from the database
     * @param dbconn database connection
     * @param thisID the field id we are loading
     * @throws SQLException if database error occurs
     */

    void dbRestore( Connection dbconn, int thisID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();
	ResultSet result;

	result = statement.executeQuery( "SELECT title,badge FROM inputfield WHERE id=" + thisID );
	
	if( result != null ) {
	    result.first();

	    id = thisID;
	    name = result.getString( "title" );
	    badgeFieldIndex = result.getInt( "badge" );

	    result.close();
	}
    }

    /**
     * Remove this field from the database (if user edits and removes)
     * @param dbconn database connection
     * @throws SQLException if database error occurs
     */

    void dbRemove( Connection dbconn )
	throws SQLException
    {
	if( id == 0 )
	    return;

	Statement statement = dbconn.createStatement();
	statement.executeUpdate( "DELETE FROM inputfield WHERE id=" + id );
    }

    /**
     * Create an "empty" nameless checkbox
     */

    AdminEventCheckboxField()
    {
	name = "";
	value = false;
	optional = true;
    }

    /**
     * Create a checkbox with name and value
     */

    AdminEventCheckboxField( String n, boolean v )
    {
	setName( n );
	value = v;
	optional = true;
    }
}
