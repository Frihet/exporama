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
 * Menu item, belongs to a menu container
 */

class AdminEventMenuElementField extends AdminEventField
{
    char category = '0';

    /**
     * Does not apply for this field, but is required by the interface
     */

    boolean isEmpty()
    {
	return false;
    }

    /**
     * Does not apply for this field, but is required by the interface
     */

    void setStringValue( String val )
    {
    }

    /**
     * Does not apply for this field, but is required by the interface
     */

    String getStringValue()
    {
	return "FIXME: NOT IMPLEMENTED";
    }

    /**
     * Html markup for this item
     * @return Html for the item
     */

    String getAsHtml()
    {
	return name;
    }

    /**
     * A menu item can have a category associated
     * @return a single letter category
     */

    String getCategory()
    {
	String categoryString = "";

	if( category != '0' )
	    categoryString += category;

	return categoryString;
    }

    /**
     * Sets a category for this item
     * @param c a single letter category
     */

    void setCategory( char c )
    {
	if( Character.isLetter(c) )
	    category = c;
    }

    /**
     * Create (or update) this menu item in the database
     * @param dbconn database connection
     * @param parentID primary key for menu container
     */

    void dbCreateOrUpdate( Connection dbconn, int parentID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();
	ResultSet result;

	if( id == 0 ) {
	    result = statement.executeQuery( "SELECT nextval('menuelement_id_seq')" );
	    result.first();
	    id = result.getInt( "nextval" );
	    result.close();

	    statement.executeUpdate( "INSERT INTO menuelement (id,inputfield_id) VALUES (" + id + "," + parentID  + ")" );
	}

	statement.executeUpdate( "UPDATE menuelement SET title='" + name + "', category='" + category + "' WHERE id=" + id );
    }

    /**
     * Load menu item from database
     * @param dbconn database connection
     * @param thisID primary key for menu item
     */

    void dbRestore( Connection dbconn, int thisID )
	throws SQLException
    {
	Statement statement = dbconn.createStatement();
	ResultSet result;

	result = statement.executeQuery( "SELECT title,category FROM menuelement WHERE id=" + thisID );

	if( result != null ) {
	    result.first();

	    id = thisID; 
	    name = result.getString( "title" );
	    String categoryString = result.getString( "category" );
	    if( categoryString != null )
		category = categoryString.charAt( 0 );

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
	statement.executeUpdate( "DELETE FROM menuelement WHERE id=" + id );
    }

    /** 
     * Create emtpy nameless menu element
     */

    AdminEventMenuElementField()
    {
	name = "";
    }

    /**
     * Create named menu element
     */

    AdminEventMenuElementField( String title )
    {
	name = title.trim();
    }

    /**
     * Create named menu element with category
     */

    AdminEventMenuElementField( String title, char cat )
    {
	name = title.trim();
	category = cat;
    }
}
