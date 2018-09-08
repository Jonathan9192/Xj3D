/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// External imports
// none

// Local imports
// none

/**
 * Creates a marker for code that generates hash code values as longs rather
 * than the system standard int.
 * <p>
 *
 * This interface is used with items that need to be stored in the
 * {@link LongHashSet}.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface LongHash {
    
    /**
     * Generate the hashCode for this object as a long. The same contract for
     * the standard {@link Object#hashCode()} method but with the values being
     * a <code>long</code> rather than an <code>int</code>.
     *
     *
     * @see Object#hashCode()
     */
    long longHashCode();
}
