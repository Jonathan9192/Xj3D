/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.eai;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import vrml.eai.field.*;

/**
 *  EventInMFColorWrapper provides the functional implementation of
 *  EventInMFColor for the SimpleBrowser class.  The functionality of this class
 *  is specified in the EAI 2.0 specification.
 */
class EventInMFColorWrapper extends EventInMFColor
    implements ExternalEvent, EventWrapper {

    /** Flag indicating if a value has been stored for later transmission. */
    boolean hasStoredValue;

    /** Flag indicating that this event is a Set1Value or accumulation of
     ** Set1Value calls.  This flag is checked during the processing of
     ** set1Value calls in order to correctly either generate a new event,
     ** or merge with a previous set1Value call.*/
    boolean isSet1Value;

    /** The value stored in this event (if hasStoredValue is true) */
    float storedValue[];

    /** The Node implementation uses integer identifiers for the fields */
    int theFieldID;

    /** Reference to the underlying Node implementation */
    VRMLNodeType theNode;

    /** The queue to post events to.*/
    ExternalEventQueue theQueue;

    /** Basic constructor for general use.
      * @param aNode The underlying target node
      * @param fieldID The underlying target field
      * @param aQueue The queue to send events to
      */
    EventInMFColorWrapper(
        VRMLNodeType aNode, int fieldID, ExternalEventQueue aQueue
    ) {
        fieldType=MFColor;
        theNode=aNode;
        theFieldID=fieldID;
        theQueue=aQueue;
    }

    /** Constructor for use in event queueing system.
      * @param aNode The underlying target node
      * @param fieldID The underlying target field
      * @param aQueue The queue to send events to
      * @param newValue The value for this event
      */
    EventInMFColorWrapper(
        VRMLNodeType aNode, int fieldID, ExternalEventQueue aQueue,
        float newValue[][]
    ) {
        this(aNode,fieldID,aQueue);
        storeValue(newValue);
    }

    /** Constructor for use in set1Value event queueing.
      * Loads the existing field value, and then applies the change.
      * @param aNode The underlying target node
      * @param fieldID The underlying target field
      * @param aQueue The queue to send events to
      * @param index oldValue[index]=newValue
      * @param newValue The value for this event
      */
    EventInMFColorWrapper(
        VRMLNodeType aNode, int fieldID, ExternalEventQueue aQueue, int index,
        float newValue[]
    ) {
        this(aNode,fieldID,aQueue);
        loadValue();
        store1Value(index,newValue);
    }

    /** The EventIn*Wrapper classes implement doEvent by posting their
        stored values to the underlying implementation.
        @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent
     **/
    public void doEvent() {
        try {
            try {
                theNode.setValue(theFieldID,storedValue,storedValue.length);
            } finally {
                isSet1Value=false;
                hasStoredValue=false;
            }
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            ife.printStackTrace(System.err);
            throw new RuntimeException(
                "InvalidFieldException setting EventIn value.",ife
            );
        } catch (org.web3d.vrml.lang.InvalidFieldValueException ifve) {
            ifve.printStackTrace(System.err);
            throw new RuntimeException(
                "InvalidFieldValueException setting EventIn value.",ifve
            );
        }
    }

    /** Two eventIn's are equal if they refer to the same actual node and field
      */
    public boolean equals(Object other) {
        if (other == null)
            return false;
        else if (other instanceof EventWrapper) {
            EventWrapper otherWrapper=(EventWrapper)other;
            return (
                otherWrapper.getFieldNode()==theNode
                && otherWrapper.getFieldID()==theFieldID
                && otherWrapper.getType()==getType()
            );
        } else
            return super.equals(other);
    }

    /** The underlying field ID
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldID
      */
    public int getFieldID() {
        return theFieldID;
    }

    /** The underlying implementation node.
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldNode
      */
    public VRMLNodeType getFieldNode() {
        return theNode;
    }

   /** @see vrml.eai.field.EventIn#getUserData */
    public Object getUserData() {
        try {
            return theNode.getUserData(theFieldID);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error getting user data");
        }
    }

    /** In order to make the event queueing system easier, and since
      * an equals method is required by the specification, compute the hashcode
      * based on the field number and underlying node hashcode.
      * @see java.lang.Object#hashCode
     **/
    public int hashCode() {
        return theNode.hashCode()+theFieldID;
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
	public boolean isConglomerating() {
		return isSet1Value;
	}
    
    /** Load the current event value from the underlying implmementation.
     ** This method is used to implement the semantics of the set1Value
     ** methods.  After this method returns, hasStoredValue will be
     ** true, and storedValue represents the value of the field at the
     ** time called. */
    private void loadValue() {
        try {
            VRMLFieldData fieldValue=theNode.getFieldValue(theFieldID);
            if (
                storedValue==null
                || storedValue.length!=(fieldValue.numElements*3)
            )
                storedValue=new float[fieldValue.numElements*3];
            System.arraycopy(
                fieldValue.floatArrayValue,0,storedValue,0,
                fieldValue.numElements*3
            );
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException(
                "InvalidFieldException Error setting EventIn value."
            );
        } catch (org.web3d.vrml.lang.InvalidFieldValueException ifve) {
            throw new RuntimeException(
                "InvalidFieldValueException Error setting EventIn value."
            );
        }
        hasStoredValue=true;
    }

    /** @see vrml.eai.field.EventIn#setUserData */
    public void setUserData(Object data) {
        try {
            theNode.setUserData(theFieldID,data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

    /** @see vrml.eai.field.EventInMFColor#setValue */
    public void setValue(float[][] newValue) {
        EventInMFColorWrapper queuedElement;
        if (!hasStoredValue) {
            queuedElement=this;
            storeValue(newValue);
        } else
            queuedElement=new EventInMFColorWrapper(
                theNode,theFieldID,theQueue,newValue
            );
        theQueue.processEvent(queuedElement);
    }

    /** @see vrml.eai.field.EventInMFColor#set1Value */
    public void set1Value(int index, float[] newValue) {
        synchronized(theQueue.eventLock) {
            EventInMFColorWrapper queuedElement=(EventInMFColorWrapper)
                theQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSet1Value) {
                if (!hasStoredValue) {
                    queuedElement=this;
                    loadValue();
                    store1Value(index,newValue);
                } else
                    queuedElement=new EventInMFColorWrapper(
                        theNode,theFieldID,theQueue,index,newValue
                    );
                theQueue.processEvent(queuedElement);
            } else
                queuedElement.store1Value(index,newValue);
        }
    }

    /** Store a value in this EventIn as a place holder for the buffering
        system.  */
    private void store1Value(int index, float[] newValue) {
        isSet1Value=true;
        System.arraycopy(newValue,0,storedValue,index*3,3);
    }

    /** Store a value in this EventIn as a place holder for the buffering
      * system.  */
    private void storeValue(float[][] newValue) {
        if (newValue!=null) {
            if (storedValue==null || storedValue.length!=(newValue.length*3))
                storedValue=new float[newValue.length*3];
            ArrayUtils.flatten3(newValue,newValue.length,storedValue);
        } else
            storedValue=null;
        hasStoredValue=true;
    }

}
