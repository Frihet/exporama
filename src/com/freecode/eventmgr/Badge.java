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

/**
 * Contains information about an entry badge. To be associated with an
 * event
 */

class Badge
{
    AdminEventField[] fields = new AdminEventField[5];

    /**
     * Place a user field at a given location on the badge
     * @param index location on badge
     * @param field the field to place
     */

    void setField( int index, AdminEventField field )
    {
	if( index >= 0 && index < getFieldCount() && fields[index] == null )
	    fields[index] = field;
    }

    /**
     * Remove a field (by reference) from the badge
     * @param field the field to remove
     */

    void removeField( AdminEventField field )
    {
	int index = getFieldIndex( field );
	if( index >= 0 )
	    fields[index] = null;
    }

    /**
     * Number of locations on a badge
     */ 

    int getFieldCount()
    {
	return fields.length;
    }

    /**
     * Query location for a given field
     * @param field field
     */

    int getFieldIndex( AdminEventField field )
    {
	for( int i = 0; i < fields.length; i++ )
	    if( fields[i] == field )
		return i;

	return -1;
    }

    /**
     * Synchronize where the field thinks it's placed
     */

    void syncField( AdminEventField field )
    {
	setField( field.getBadgeField(), field );
    }

    /**
     * Get reference to a field at a given location
     * @param index location on field
     */

    AdminEventField getField( int index )
    {
	return fields[index];
    }

    /**
     * Get string value of field at a given location
     * @param index location
     */

    String getFieldStringValue( int index )
    {
	AdminEventField f = getField( index );
	/* HACK BEGIN */
	if( index == 0 && f instanceof AdminEventMenuContainerField ) {
	    AdminEventMenuContainerField mf = (AdminEventMenuContainerField)f;
	    AdminEventMenuElementField me = mf.getSelectedField();
	    return me.getCategory();
	}
	/* HACK END */

	if( f == null )
	    return "";

	return f.getStringValue();
    }

    /**
     * Preview this badge as html
     * @return html markup for preview
     */

    String makeHtmlPreview()
    {
	String htmlPreviewString = "";
	String[] fieldString = new String[fields.length];

	for( int i = 0; i < fields.length; i++ ) {
	    if( fields[i] == null )
		fieldString[i] = "[Felt " + i + "]";
	    /* BEGIN HACK */
	    else if( i == 0 && 
		     fields[0] instanceof AdminEventMenuContainerField )
		fieldString[i] = "Kategori";
	    /* END HACK */
	    else
		fieldString[i] = fields[i].getName();
	    
	    //htmlPreviewString += fieldString[i] + "<br/>";
	}

	htmlPreviewString += "<font size=\"+2\">" + fieldString[0] + 
	    "</font><br/>";
	htmlPreviewString += "<font size=\"+1\">" + fieldString[1] + " " + fieldString[2] + "</font><br/>";
	htmlPreviewString += "<font size=\"+1\">" + fieldString[3] + "</font><br/>";
	htmlPreviewString += "<font size=\"+1\">" + fieldString[4] + "</font><br/>";	

	return htmlPreviewString;
    }
}
