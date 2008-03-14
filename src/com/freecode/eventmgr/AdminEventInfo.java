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

import java.util.Vector;
import java.util.Enumeration;
import java.sql.*;

/**
 * Information container for an event
 */

class AdminEventInfo
{
    int id = 0;
    int pid = 0;
    int themeId = 1;
    int logoId = 0;
    int blogoId = 0;

    Vector adminFields = new Vector();
    Vector userFields = new Vector();


    /**
     * Create a badge for use with this event
     * @return the newly created badge with field mappings set
     */
    
    Badge getBadge()
    {
	Badge b = new Badge();

	Enumeration e = getUserFields();
	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement();
	    int bf = f.getBadgeField();
	    b.setField( bf, f );
	}

	return b;
    }

    /**
     * Identifier for the participant using this information container
     * @return a number identifying the participant
     */

    int getParticipantId()
    {
	return pid;
    }

    /**
     * Get database reference for this events (web) logo
     * @return a numbe identifying the logo
     */

    int getLogoId()
    {
	return logoId;
    }

    /**
     * Get database reference for this events badge (small) logo
     * @return a numbe identifying the logo
     */

    int getBadgeLogoId()
    {
	return blogoId;
    }

    /**
     * Set the database reference identifying the badge logo
     * @param lid database primary key
     */

    void setBadgeLogoId( int lid )
    {
	blogoId = lid;
    }

    /**
     * Set the database reference identifying the (web) logo
     * @param lid database primary key
     */

    void setLogoId( int lid )
    {
	logoId = lid;
    }

    /**
     * Set the database reference identifying the stylesheet for this event
     * @param tid database primary key
     */

    void setThemeId( int tid )
    {
	themeId = tid;
    }

    /**
     * Get the database reference identifying the stylesheet for this event
     * @return database primary key for the stylesheet
     */

    int getThemeId()
    {
	return themeId;
    }

    /**
     * Get the name/title for this event
     * @return string with events name
     */

    String getEventName()
    {
	return getAdminStringValueByName( "name" );
    }

    /**
     * Query an event information field by name for its value
     * @param n name of field
     * @return string value of named field
     */

    String getAdminStringValueByName( String n )
    {
	AdminEventField f = getAdminFieldByName( n );
	if( f != null )
	    return f.getStringValue();
	else
	    return null;
    }

    /**
     * Query for a reference to an information field by name
     * @return a reference to the field, or null if not found
     */

    AdminEventField getAdminFieldByName( String n )
    {
	Enumeration e = getAdminFields();
	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement();
	    if( n.equals(f.getInternalName()) )
		return f;
	}

	return null;
    }

    /**
     * Get collection of all information fields (fixed)
     * @return all admin fields
     */

    Enumeration getAdminFields()
    {
	return adminFields.elements();
    }

    /**
     * Get the fields the participants fill out when registering (variable)
     * @return all user fields
     */

    Enumeration getUserFields()
    {
	return userFields.elements();
    }

    /**
     * Add a user field to this event
     * @param f field to be added
     */

    void addUserField( AdminEventField f )
    {
	userFields.add( f );
    }

    /**
     * Remove a user field from this event (by reference)
     * Also remove from database if already written
     * @param f field to be removed
     * @param dbconn database connection (not always used)
     */

    void removeUserField( AdminEventField f, Connection dbconn )
	throws SQLException
    {
	userFields.remove( f );
	f.dbRemove( dbconn );
    }

    /**
     * Remove a user field from this event (by index)
     * Also remove from database if already written
     * @param f field to be removed
     * @param dbconn database connection (not always used)
     */

    void removeUserField( int index, Connection dbconn )
	throws SQLException
    {
	AdminEventField f = (AdminEventField)userFields.get( index );
	userFields.remove( index );
	f.dbRemove( dbconn );
    }

    /**
     * Write participant's choice set to database
     * @param dbconn database connection
     */

    void dbStoreParticipant( Connection dbconn )
	throws SQLException
    {
	Enumeration e = getUserFields();
	ResultSet result;
	Statement statement = dbconn.createStatement();


	result = statement.executeQuery( "SELECT nextval('participant_id_seq')" );
	result.first();
	pid = result.getInt( "nextval" );
	result.close();

	statement.executeUpdate( "INSERT INTO participant (id,date,event_id) VALUES (" + pid + ",now()," + id + ")" );

	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement();
	    if( f instanceof AdminEventTextField )
		((AdminEventTextField)f).dbStoreParticipant( dbconn, pid );
	    else if( f instanceof AdminEventMenuContainerField )
		((AdminEventMenuContainerField)f).dbStoreParticipant( dbconn, pid );
	    else if( f instanceof AdminEventCheckboxField )
		((AdminEventCheckboxField)f).dbStoreParticipant( dbconn, pid );
	}
    }

    /**
     * Write information about event (meta and user fields) to database
     * @param dbconn database connection
     */

    void dbCommit( Connection dbconn )
	throws SQLException
    {
	Enumeration e;
	ResultSet result;
 	Statement statement = dbconn.createStatement();

	// if id is 0, we must create (insert) a new event, else it's
	// already in database and we update.

	if( id == 0 ) {
	    result = statement.executeQuery( "SELECT nextval('event_id_seq')" );
	    result.first();
	    id = result.getInt( "nextval" );
	    result.close();

	    statement.executeUpdate( "INSERT INTO event (id,state) VALUES (" + id + ",'I')" );
	}

	e = getAdminFields();
	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement();
	    statement.executeUpdate( "UPDATE event SET " + f.getInternalName() + "='" + f.getSqlSafeStringValue() + "' WHERE id=" + id );
	}

	e = getUserFields();
	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement();
	    f.dbCreateOrUpdate( dbconn, id );
	}

	// set associated files
	statement.executeUpdate( "UPDATE event SET logo=" + getLogoId() + ", badgelogo=" + getBadgeLogoId() + ", theme=" + getThemeId() + " WHERE id=" + id );
    }

    /**
     * Load information about event (meta and user fields) from database
     * @param dbconn database connection 
     * @param eventID primary key identifying the event
     */

    boolean dbRestore( Connection dbconn, int eventID )
	throws SQLException
    {
	id = eventID;

	Statement statement = dbconn.createStatement();
	ResultSet results = statement.executeQuery( "SELECT name,startdate,enddate,administrator,theme,logo,badgelogo FROM event WHERE id=" + id );
	//+ " AND state='I'" );

	if( results != null ) {
	    results.first();
	    Enumeration e = getAdminFields();
	    while( e.hasMoreElements() ) {
		AdminEventField f = (AdminEventField)e.nextElement();
		f.setStringValue( results.getString(f.getInternalName()) );
	    }

	    setThemeId( results.getInt("theme") );
	    setLogoId( results.getInt("logo") );
	    setBadgeLogoId( results.getInt("badgelogo") );
	    results.close();

	    results = statement.executeQuery( "SELECT id,type FROM inputfield WHERE event_id=" + id + " ORDER BY id" );
	    if( results != null ) {
		while( results.next() ) {
		    int fieldID = results.getInt( "id" );
		    String type = results.getString( "type" );
		    AdminEventField f; 
		    if( type.equals("T") )
			f = new AdminEventTextField();
		    else if( type.equals("M") )
			f = new AdminEventMenuContainerField();
		    else if( type.equals("C") )
			f = new AdminEventCheckboxField();
		    else
			continue;

		    f.dbRestore( dbconn, fieldID );
		    addUserField( f );
		}
	    }

	    return true;
	}

	return false;
    }

    /**
     * Add the fixed admin information fields (meta information for event)
     */
    
    private void makeAdminFields()
    {
	AdminEventField f;

	f = new AdminEventTextField( "Tittel", "", 60, false );
	f.setInternalName( "name" );
	adminFields.add( f );

	f = new AdminEventDateField( "Fra dato", false );
	f.setInternalName( "startdate" );
	adminFields.add( f );

	f = new AdminEventDateField( "Til dato", false );
	f.setInternalName( "enddate" );
	adminFields.add( f );

	f = new AdminEventTextField( "Administrator", "", 60, false );
	f.setInternalName( "administrator" );
	adminFields.add( f );

	/*
	f = new AdminEventFileField( "Tema" );
	f.setInternalName( "theme" );
	adminFields.add( f );

	f = new AdminEventFileField( "Logo" );
	f.setInternalName( "logo" );
	adminFields.add( f );
	*/
    }

    /**
     * Create empty event
     */

    AdminEventInfo()
    {
	makeAdminFields();
    }

    /**
     * Create event from template (template describes user fields)
     */

    AdminEventInfo( String[] template )
    {
	makeAdminFields();

	for( int i = 0; i < template.length; i++ ) {
	    String[] token = template[i].split( "," );

	    String title = token[0];
	    String type = token[1];

	    if( type.equals("text") ) {
		AdminEventTextField tf = new AdminEventTextField( title, "", Integer.parseInt(token[2]), token[3].equals("true") );
		addUserField( tf );
	    }
	    else if( type.equals("menu") ) {
		AdminEventMenuContainerField c = new AdminEventMenuContainerField( title, token[2].equals("true") );
		for( int j = 3; j < token.length; j++ ) {
		    String[] elementToken = token[j].split( ":" );
		    if( elementToken.length > 1 )
			c.addMenuItem( new AdminEventMenuElementField(elementToken[0],elementToken[1].charAt(0)) );
		    else
			c.addMenuItem( new AdminEventMenuElementField(elementToken[0]) );
		}
		addUserField( c );
	    }
	    else if( type.equals("checkbox") ) {
		AdminEventCheckboxField cf = new AdminEventCheckboxField( title, false );
	    }
	}
    }
}
