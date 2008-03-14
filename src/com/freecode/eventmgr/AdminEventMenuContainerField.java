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
import java.util.Vector;
import java.util.Enumeration;

/**
 * Our abstract view of a menu. This is the container which holds menu
 * elements
 */

class AdminEventMenuContainerField extends AdminEventField
{
    int selected = -1;
    Vector menuElements = new Vector();

    /**
     * Emtpy if no element is selected
     * @return true if empty, false otherwise
     */

    boolean isEmpty()
    {
	return selected < 0;
    }

    /**
     * Select a menu element
     * @param val is the index of a menu element
     */

    void setStringValue( String val )
    {
	int value = Integer.parseInt( val );
	if( value >= 0 && value < menuElements.size() )
	    selected = value;
    }

    /**
     * The selected menu element
     * @return reference to selected menu element, or null if empty
     */

    AdminEventMenuElementField getSelectedField()
    {
	if( selected < 0 )
	    return null;

	return (AdminEventMenuElementField)menuElements.get( selected );
    }

    /**
     * Get name of the selected menu element
     * @return name of selected item, or null if empty
     */

    String getStringValue()
    {
	AdminEventMenuElementField me = getSelectedField();
	if( me == null )
	    return null;

	return me.getName();
    }

    /**
     * Html markup for this menu
     * @return Html for the menu
     */

    String getAsHtml()
    {
	String htmlStr = "<td align=\"right\">" + name;

	if( isEmpty() && !optional )
	    htmlStr += "(*)";
	

	htmlStr += "</td><td><select name=\"" + getInternalName() + "\">";
	int i;
	if( selected < 0 )
	    i = -1;
	else
	    i = 0;
	for( ; i < menuElements.size(); i++ ) {
	    htmlStr += "<option value=\"" + i + "\"";
	    if( i == selected )
		htmlStr += " selected";
	    htmlStr += ">";
	    if( i < 0 )
		htmlStr += "(ingen)";
	    else {
		AdminEventMenuElementField me = 
		    (AdminEventMenuElementField)menuElements.get( i );
		htmlStr += me.getAsHtml();
	    }
	    htmlStr += "</option>";
	}
	htmlStr += "</select></td>";

	return htmlStr;
    }

    /**
     * Adds an item to this menu
     * @param elm a reference to the menu element to be added
     */

    void addMenuItem( AdminEventMenuElementField elm )
    {
	menuElements.add( elm );
    }

    /**
     * Removes an item from this menu
     * @param index index of the element to be removed
     * @param dbconn useful if we need to remove it from database aswell
     */

    void removeMenuItem( int index, Connection dbconn )
	throws SQLException
    {
	AdminEventMenuElementField me = 
	    (AdminEventMenuElementField)menuElements.get( index );
	menuElements.remove( index );
	me.dbRemove( dbconn );
    }

    /**
     * Query all menu elements
     * @return all menu elements
     */

    Enumeration getMenuItems()
    {
	return menuElements.elements();
    }

    /**
     * Query a menu item by index
     * @param index index of menu item
     * @return reference to menu item, or null if not found
     */

    AdminEventMenuElementField getMenuItem( int index )
    {
	if( index < 0 || index >= menuElements.size() )
	    return null;

	AdminEventMenuElementField me = 
	    (AdminEventMenuElementField)menuElements.get( index );

	return me;
    }

    /**
     * Write users menu item choice to database
     * @param dbconn database connection
     * @param participantID primary key reference participant
     */

    void dbStoreParticipant( Connection dbconn, int participantID )
	throws SQLException
    {
	AdminEventMenuElementField me = getMenuItem( selected );
	if( me != null ) {

	    Statement statement = dbconn.createStatement();

	    statement.executeUpdate( "INSERT INTO participantchoice (menuelement_id,inputfield_id,participant_id) VALUES (" + me.id + "," + id + "," + participantID + ")" );
	}
    }

    /**
     * Write this menu to database
     * @param dbconn database connection
     * @param parentID primary key for event
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

	    statement.executeUpdate( "INSERT INTO inputfield (id,type,event_id) VALUES (" + id + ",'M'," + parentID  + ")" );
	}

	statement.executeUpdate( "UPDATE inputfield SET title='" + name + "', optional='" + optional + "', badge=" + badgeFieldIndex + " WHERE id=" + id );

	Enumeration e = getMenuItems();
	while( e.hasMoreElements() ) {
	    AdminEventField f = (AdminEventField)e.nextElement();
	    f.dbCreateOrUpdate( dbconn, id );
	}
    }

    /**
     * Load a menu from database
     * @param dbconn database connection
     * @param thisID primary key for menu
     */

    void dbRestore( Connection dbconn, int thisID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();
	ResultSet result;

	result = statement.executeQuery( "SELECT title,optional,badge FROM inputfield WHERE id=" + thisID );
	
	if( result != null ) {
	    result.first();

	    id = thisID;
	    name = result.getString( "title" );
	    optional = result.getBoolean( "optional" );
	    badgeFieldIndex = result.getInt( "badge" );

	    result.close();

	    result = statement.executeQuery( "SELECT id FROM menuelement WHERE inputfield_id=" + thisID );
	    if( result != null ) {
		while( result.next() ) {
		    AdminEventMenuElementField me = 
			new AdminEventMenuElementField();

		    me.dbRestore( dbconn, result.getInt("id") );
		    addMenuItem( me );
		}
	    }
	}
    }

    /**
     * remove (delete) menu from database
     * @param dbconn database connection
     */

    void dbRemove( Connection dbconn )
	throws SQLException
    {
	Enumeration e = getMenuItems();
	while( e.hasMoreElements() ) {
	    AdminEventMenuElementField me = 
		(AdminEventMenuElementField)e.nextElement();

	    me.dbRemove( dbconn );
	}

	if( id == 0 )
	    return;

	Statement statement = dbconn.createStatement();
	statement.executeUpdate( "DELETE FROM inputfield WHERE id=" + id );
    }

    /**
     * Create empty nameless menu container
     */

    AdminEventMenuContainerField()
    {
	name = "";
    }

    /**
     * Create named menu container
     */

    AdminEventMenuContainerField( String title, boolean o )
    {
	name = title.trim();
	optional = o;
    }
}
