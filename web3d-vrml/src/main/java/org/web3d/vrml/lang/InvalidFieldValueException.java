/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.lang;

// Standard imports
// none

// Application specific imports
// none

/**
 * Exception indicating that a field value is out of range.
 * <P>
 *
 * This is generated when a node verifies the correctness of a setValue
 * May be generated during the parsing process or by the field during
 * runtime as the user is trying to modify it.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class InvalidFieldValueException extends FieldException {

    /**
     * Create a new exception with no message.
     */
    public InvalidFieldValueException() {
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public InvalidFieldValueException(String msg) {
        super(msg);
    }
    /**
     * Create a new exception with a message and the name of the field
     * that had the error.
     *
     * @param msg The message associated with this exception
     * @param field The name of the field with the error
     */
    public InvalidFieldValueException(String msg, String field) {
        super(msg);
    }

}
