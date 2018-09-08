/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript;

// External imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.scripting.ecmascript.builtin.NodeImplSource;
import org.web3d.vrml.scripting.ecmascript.builtin.FieldExtras;
import org.web3d.vrml.scripting.ecmascript.builtin.FieldFactory;
import org.web3d.vrml.scripting.ecmascript.builtin.FieldScriptableObject;
import org.web3d.vrml.scripting.ecmascript.x3d.Browser;
import org.web3d.vrml.scripting.ecmascript.x3d.X3DConstants;

/**
 * ECMAScript representation of a script as a top level scriptable object.
 * <P>
 *
 * In order for the script adapters to work nicely, they need to implement
 * a scriptable object so that we can tell when a field has been changed.
 * It combines the roles of ExecutionContext and the container for the
 * field wrappers. Each context has the field names registered with it so
 * that they can be accessed dynamically. The parent scope of this object will
 * normally be the output from Context.initSharedObjects() with a shared global
 * scope for all scripts.
 * <P>
 *
 * The implementation never tracks the values of the fields for passing back
 * to the parent script. There's no need for them back there, so we just track
 * the eventOuts.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
class X3DScriptContext implements Scriptable, FieldExtras, NodeImplSource {

    /** Name of the Browser object */
    private static final String BROWSER = "Browser";

    /** Name of the Browser object */
    private static final String X3DCONSTANTS = "X3DConstants";

    /** The VRML TRUE Javascript object as a string */
    private static final String TRUE_STRING = "TRUE";

    /** The VRML FALSE Javascript object as a string */
    private static final String FALSE_STRING = "FALSE";

    private static final Object[] EMPTY_IDS = new Object[0];

    /** The parent scope of this object */
    private Scriptable parentScope;

    /** Mapping of field names to the value */
    private Map<String, Object> fieldValueMap;

    /** Mapping of the eventOut index to the name string */
    private IntHashMap eventOutIndexMap;

    /** Mapping of eventOut names to the value */
    private Map<String, Object> eventOutValueMap;

    /**
     * Set of eventOut names that have changed since the last time it was
     * asked for. When the name is checked, it gets removed from the set
     * so that if it is not here, it hasn't been changed.
     */
    private HashSet<String> changedEventOuts;

    /** Holder of the Rhino function object representations */
    private Map<String, Object> functionObjects;

    /** Map of the standard objects: TRUE, FALSE and Browser */
    private Map<String, Object> stdObjects;

    /** The factory to use for fields from this context */
    private FieldFactory fieldFactory;

    /** The browser for doing string parsing */
    private Browser browser;

    /** The script node instance that we're wrapping */
    private VRMLScriptNodeType scriptNodeImpl;

    /**
     * Create a new, empty script context. Fields and their values will be
     * set separately.
     *
     * @param b The browser instance to use in this context
     * @param globalScope Needed to do object wrapping.
     */
    X3DScriptContext(Browser b, Scriptable globalScope, FieldFactory fac) {

        fieldValueMap = new HashMap<String, Object>();
        eventOutValueMap = new HashMap<String, Object>();
        changedEventOuts = new HashSet<String>();
        eventOutIndexMap = new IntHashMap();

        functionObjects = new HashMap<String, Object>();
        stdObjects = new HashMap<String, Object>();

        browser = b;
        fieldFactory = fac;

        // register the browser with this local scope as a standard object.
        Scriptable js_browser = Context.toObject(b, globalScope);

        stdObjects.put(TRUE_STRING, Boolean.TRUE);
        stdObjects.put(FALSE_STRING, Boolean.FALSE);
        stdObjects.put(BROWSER, js_browser);
        stdObjects.put(X3DCONSTANTS, new X3DConstants());
    }

    //----------------------------------------------------------
    // Methods defined by Scriptable
    //----------------------------------------------------------

    /**
     * Get the name of the class as Javascript would see it. Really should
     * return null here because the script should never use the script instance
     * but I don't think we can use "this" either.
     *
     * @return A class name string
     */
    public String getClassName() {
        return "Script";
    }

    /**
     * Return a default value for this. Return null until we have a better
     * idea.
     */
    public Object getDefaultValue(Class hint) {
        return null;
    }

    /**
     * Get the variable at the given index. Since we don't support integer
     * index values for fields of the script, this always returns NOT_FOUND.
     */
    public Object get(int index, Scriptable start) {
        return NOT_FOUND;
    }

    /**
     * Get the value of the named variable, which is either a field or eventOut
     * In Javascript, you can read the values of eventOuts.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     */
    public Object get(String name, Scriptable start) {
        Object ret_val = fieldValueMap.get(name);

        if(ret_val == null)
            ret_val = eventOutValueMap.get(name);

        if(ret_val == null) {
            // is this the browser object then?
            if(stdObjects.containsKey(name))
                ret_val = stdObjects.get(name);
            else
                ret_val = functionObjects.get(name);
        }

        // Are we still not defined?
        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    /**
     * Check for the indexed property presence. Always returns NOT_FOUND as
     * scripts don't support indexed objects.
     */
    public boolean has(int index, Scriptable start) {
        return false;
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    public boolean has(String name, Scriptable start) {
        // Return true for all properties to support global variables
        return true;
    }

    /**
     * Sets a property based on the index. Since we don't support indexes,
     * this is ignored.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(int index, Scriptable start, Object value) {
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFVec3f(0, 1, 0);</code> then a whole new object is
     * passed to us.
     */
    public void put(String name, Scriptable start, Object value) {
        Object obj = fieldValueMap.get(name);
        boolean is_field = true;

        if(obj == null) {
            obj = eventOutValueMap.get(name);
            is_field = false;
        }

        if(value instanceof FieldScriptableObject) {
            Scriptable node = (Scriptable)value;

            // NOTE:
            // Always force the parent node's scope to be this node. Probably
            // not the best thing to do, but at least everything should be
            // correctly set then. May need to check on what happens with a
            // node that is in an array returned from createVrmlFromString()
            // that doesn't really have a parent. We need to check on sanity
            // there.
            node.setParentScope(this);
        }

        if(obj != null) {
            if(is_field)
                fieldValueMap.put(name, value);
            else {
                eventOutValueMap.put(name, value);
                changedEventOuts.add(name);
            }
        } else {
            // this must be a function being thrown at us by the runtime
            functionObjects.put(name, value);
        }

        // ignore anything else
    }

    /**
     * Delete a property. There are no dynamic fields, so this does nothing.
     *
     * @param index The index of the property to delete
     */
    public void delete(int index) {
        // Do nothing
    }

    /**
     * Delete a property. There are no dynamic fields, so this does nothing.
     *
     * @param name The name of the property to delete
     */
    public void delete(String name) {
        // Do nothing
    }

    /**
     * Get properties.
     *
     * We return an empty array since we define all properties to be DONTENUM.
     */
    public Object[] getIds() {
        return EMPTY_IDS;
    }

    /**
     * instanceof operator.
     *
     * We mimic the normal JavaScript instanceof semantics, returning
     * true if <code>this</code> appears in <code>value</code>'s prototype
     * chain.
     */
    public boolean hasInstance(Scriptable value) {
        Scriptable proto = value.getPrototype();
        while (proto != null) {
            if (proto.equals(this))
                return true;
            proto = proto.getPrototype();
        }

        return false;
    }

    /**
     * Fetch the parent scope of this context.
     */
    public Scriptable getParentScope() {
        return parentScope;
    }

    /**
     * Set the parent scope of this object. Should be the global shared
     * instance.
     */
    public void setParentScope(Scriptable parent) {
        parentScope = parent;
    }

    /**
     * Get the prototype used by this context. There is no prototype, so it
     * always returns null.
     */
    public Scriptable getPrototype() {
        return null;
    }

    /**
     * Ignored. Set the prototype for this context. The context does not have
     * a prototype that we want the user to play with.
     */
    public void setPrototype(Scriptable prototype) {
    }

    //----------------------------------------------------------
    // Methods defined by FieldExtras
    //----------------------------------------------------------

    /**
     * Create a collection of VRML Objects from a string. Not used by this
     * context.
     *
     * @param vrmlString The string containing VRML statements
     * @return A scene containing all the information
     */
    public VRMLNodeType[] parseVrmlString(String vrmlString)
        throws VRMLException, VRMLParseException {


        return null;
    }

    /**
     * Locate the field factory appropriate to this node and context
     * information. Used so that the field factory can generate the nodes
     * within the correct execution space etc.
     *
     * @return The local field factory instance in use
     */
    public FieldFactory getFieldFactory() {
        return fieldFactory;
    }

    //----------------------------------------------------------
    // Methods defined by NodeImplSource
    //----------------------------------------------------------

    /**
     * Get the underlying node that this object represents.
     *
     * @return The node reference
     */
    public VRMLNodeType getImplNode() {
        return scriptNodeImpl;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the node implementation that we are wrapping. Used to return the
     * node reference to any addRoute calls. Should never be null passed in
     * here.
     *
     * @param node The node impl to use
     */
    void setNodeImpl(VRMLScriptNodeType node) {
        scriptNodeImpl = node;
    }

    /**
     * Add a field to the script. Only add ordinary fields here. EventIns
     * should never be registered and eventOuts have a separate method.
     *
     * @param name The name of the field
     * @param value The field representation as a Scriptable object or raw
     *    value class (eg String, Number etc)
     */
    void addField(String name, Object value) {
        fieldValueMap.put(name, value);
        if(value instanceof Scriptable)
            ((Scriptable)value).setParentScope(this);
    }

    /**
     * Add an eventOut to the context wrapper.
     *
     * @param name The name of the field
     * @param index The index from the source VRMLNodeType
     * @param value The field representation as a Scriptable object or raw
     *    value class (eg String, Number etc)
     */
    void addEventOut(String name, int index, Object value) {
        eventOutValueMap.put(name, value);
        eventOutIndexMap.put(index, name);

        if(value instanceof Scriptable)
            ((Scriptable)value).setParentScope(this);
    }

    /**
     * Check to see if any event out has changed.
     *
     * @return true if any eventOut has changed
     */
    boolean hasAnyEventOutChanged() {
        return !changedEventOuts.isEmpty();
    }

    /**
     * Check to see if any of the scene variables have been changed, such as
     * the root nodes, proto definitions etc.
     *
     * @return true if any of the scene structures have changed
     */
    boolean hasSceneChanged() {
        return browser.hasSceneChanged();
    }

    /**
     * Get the list of fields that have changed. The return value may be
     * either a single {@link NodeFieldData} instance or an
     * {@link java.util.ArrayList} of field data instances if more than one
     * has changed. When called, this is recursive so that all fields and
     * nodes referenced by this node field will be included. If no fields have
     * changed, this will return null. However, that should never happen as the
     * user should always check {@link FieldScriptableObject#hasChanged()} which
     * would return false before calling this method.
     *
     * @return A single {@link NodeFieldData}, {@link java.util.ArrayList}
     *   or null
     */
    public Object getChangedData() {
        return browser.getChangedData();
    }

    /**
     * Check to see if the given field index has changed.
     *
     * @param index The index of the field to check
     * @return true if the field has changed since last check
     */
    boolean hasEventOutChanged(int index) {
        Object name = eventOutIndexMap.get(index);

        boolean ret_val = changedEventOuts.remove((String)name);

        // Also check to see if the object is a derived object and
        if(!ret_val) {
            Object eo = eventOutValueMap.get((String)name);
            if(eo instanceof FieldScriptableObject)
                ret_val = ((FieldScriptableObject)eo).hasChanged();
        } else {
            // Force anything below this to clean out the change flag of the
            // nodes below us.
            Object eo = eventOutValueMap.get((String)name);
            if(eo instanceof FieldScriptableObject)
                ((FieldScriptableObject)eo).hasChanged();
        }

        return ret_val;
    }

    /**
     * Check to see if the given field name, has changed.
     *
     * @param name The name of the field to check
     * @return true if the field has changed since last check
     */
    boolean hasEventOutChanged(String name) {
        boolean ret_val = changedEventOuts.remove(name);

        // Also check to see if the object is a derived object and
        if(!ret_val) {
            Object eo = eventOutValueMap.get(name);
            if(eo instanceof FieldScriptableObject)
                ret_val = ((FieldScriptableObject)eo).hasChanged();
        } else {
            // Force anything below this to clean out the change flag of the
            // nodes below us.
            Object eo = eventOutValueMap.get(name);
            if(eo instanceof FieldScriptableObject)
                ((FieldScriptableObject)eo).hasChanged();
        }

        return ret_val;
    }
}