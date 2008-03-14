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
import java.util.Random;

/**
 * Create or decode a unique and somewhat random code for identifying 
 * an event.
 */

public final class EventCode
{
    private static final int radix = 36;
    private static final long dither = 0x038c9f2647745216L;
    private static final int pos = 20;
    private static final long decodeMask = 0xFFFFFFFFL << pos;
    private static final long encodeMask = ~decodeMask;

    /**
     * Make a somewhat random identifier for an event
     * @param seed a random seed
     * @param id plain numeric id for the event
     * @return string with identifier
     */

    static String makeCode( long seed, int id )
    {
	Random r = new Random( seed );
	long code = Math.abs( r.nextLong() );
	code &= encodeMask;
	code |= ((long)id) << pos;
	code ^= dither;
	return Long.toString( code, radix );
    }

    /**
     * Decode the identifier (reversing makeCode)
     * @param codeString identifier
     * @return numeric id
     */

    static int getId( String codeString )
    {
	long decode = Long.parseLong( codeString, radix );
	decode ^= dither;
	decode &= decodeMask;
	decode >>= pos;
	return (int)decode;
    }
}
