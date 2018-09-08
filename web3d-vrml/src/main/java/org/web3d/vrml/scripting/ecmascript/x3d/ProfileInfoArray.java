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

package org.web3d.vrml.scripting.ecmascript.x3d;

// Standard imports
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.web3d.util.HashSet;

import org.web3d.vrml.scripting.ecmascript.builtin.AbstractScriptableObject;

/**
 * ProfileInfo miscellaneous object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ProfileInfoArray extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static final HashSet<String> propertyNames;

    /** List of profiles in the array */
    private final ProfileInfo[] profiles;

    /** Length of the array as a property */
    private Integer length;

    static {
        propertyNames = new HashSet<String>();
        propertyNames.add("length");
    }

    /**
     * Construct a profile descriptor for the given information.
     */
    public ProfileInfoArray(ProfileInfo[] profiles) {
        super("ProfileInfoArray");
        this.profiles = profiles;
        length = new Integer(profiles.length);
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    @Override
    public boolean has(String name, Scriptable start) {
        return propertyNames.contains(name);
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(propertyNames.contains(name)) {
            ret_val = length;
        }

        return ret_val;
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param index The index into the array
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(index >= 0 && index < profiles.length)
            ret_val = profiles[index];

        return ret_val;
    }
}
