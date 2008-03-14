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


class AdminEventTextField extends AdminEventField
{
    String value;
    int size;

    void setStringValue( String v )
    {
	String newValue = v.trim();
	value = newValue.replaceAll( "['\"<>]", "_" );
    }

    String getStringValue()
    {
	return value;
    }

    boolean isEmpty()
    {
	return value == null || value.equals("");
    }

    String getAsHtml()
    {
	String htmlStr = "<td align=\"right\">" + name;

	if( isEmpty() && !optional )
	    htmlStr += "(*)";
	

	htmlStr += "</td><td><input type=\"text\" name=\"" + 
	    getInternalName()  + "\" value=\"" + 
	    getStringValue() + "\" size=\"" + 
	    size + "\"/></td>";

	return htmlStr;
    }

    void dbStoreParticipant( Connection dbconn, int participantID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();

	statement.executeUpdate( "INSERT INTO participantchoice (data,inputfield_id,participant_id) VALUES ('" + getSqlSafeStringValue() + "'," + id + "," + participantID + ")" );
    }

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

	    statement.executeUpdate( "INSERT INTO inputfield (id,type,event_id) VALUES (" + id + ",'T'," + parentID  + ")" );
	}
	
	statement.executeUpdate( "UPDATE inputfield SET title='" + name + "', size=" + size + ", optional='" + optional + "', badge=" + badgeFieldIndex + " WHERE id=" + id );
    }

    void dbRestore( Connection dbconn, int thisID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();
	ResultSet result;

	result = statement.executeQuery( "SELECT title,size,optional,badge FROM inputfield WHERE id=" + thisID );
	
	if( result != null ) {
	    result.first();

	    id = thisID;
	    name = result.getString( "title" );
	    size = result.getInt( "size" );
	    optional = result.getBoolean( "optional" );
	    badgeFieldIndex = result.getInt( "badge" );

	    result.close();
	}
    }

    void dbRemove( Connection dbconn )
	throws SQLException
    {
	if( id == 0 )
	    return;

	Statement statement = dbconn.createStatement();
	statement.executeUpdate( "DELETE FROM inputfield WHERE id=" + id );
    }

    AdminEventTextField()
    {
	name = "";
	value = "";
	size = 0;
    }

    AdminEventTextField( String n, String v, int s, boolean o )
    {
	setName( n );
	value = new String( v );
	size = s;
	optional = o;
    }
}
