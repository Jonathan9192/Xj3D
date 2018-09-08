package org.web3d.vrml.scripting.external.sai;

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

import org.web3d.x3d.sai.*;
import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

/**
 * Representation of a MFRotation field.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 *  Section 5.8 SFRotation and MFRotation.
 *
 */
class MFRotationWrapper extends BaseFieldWrapper
    implements MFRotation, ExternalEvent, ExternalOutputBuffer {

    /** Is this the result of set1Value calls? */
    private boolean isSetOneValue;

    /** The number of elements used in the input buffer */
    private int storedInputLength;

    /** The value to be sent to the rendering system iff storedInput */
    private float[] storedInputValue;

    /** The value to be sent to the rendering system iff storedOutput */
    private float[] storedOutputValue;

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    MFRotationWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory) {
        super(node,field,aQueue,factory);
    }

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
     */
    MFRotationWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory, boolean isInput) {
            this(node,field,aQueue,factory);
            if (isInput)
                loadInputValue();
            else
                loadOutputValue();
        }

    /**
     * @see org.web3d.x3d.sai.MFFloat#append(float[])
     */
    public void append(float[] value) {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFRotationWrapper queuedElement=
                (MFRotationWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFRotationWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+4);
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value[0];
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value[1];
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value[2];
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value[3];
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.x3d.sai.MFColorRGBA#clear()
     */
    public void clear() {
        setValue(0,new float[0]);
    }

    /** Post any queued field values to the target field */
    public void doEvent() {
        try {
            theNode.setValue(fieldIndex,storedInputValue,storedInputValue.length);
        } finally {
            isSetOneValue=false;
            storedInput=false;
        }
    }

    /** Ensures that there is atleast a certain number of elements
     *  in the storedInputValue array.
     * @param newSize The size to ensure.
     */
    protected void ensureArraySize(int newSize) {
        if (storedInputValue!=null && newSize<storedInputValue.length)
            return;
        float newArray[]=new float[newSize];
        if (storedInputValue!=null)
            System.arraycopy(storedInputValue,0,newArray,0,storedInputLength);
        storedInputValue=newArray;
    }

    /**
     * Get the value of a particular rotation value in the event out array.
     *
     * @param index The position to get the vectory value from.
     * @param vec The array to place the value in where.
     *    vec[0] = X component [0-1] <BR>
     *    vec[1] = Y component [0-1] <BR>
     *    vec[2] = Z component [0-1] <BR>
     *    vec[3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small or
     *     the index was outside the current data array bounds.
     */
    public void get1Value(int index, float[] vec) {
        if (storedOutput)
            System.arraycopy(storedOutputValue,index*4,vec,0,4);
        else {
            checkReadAccess();
            VRMLFieldData value=theNode.getFieldValue(fieldIndex);
            // floatArrayValue may be null if numElements == 0
            if (index<0 || index>=value.numElements)
                throw new ArrayIndexOutOfBoundsException();
            else
                System.arraycopy(value.floatArrayValue,index*4,vec,0,4);
        }
    }

    /**
     * Get the values of the event out flattened into a single 1D array. The
     * array must be at least 4 times the size of the array.
     *
     * @param vec The array to be filled in where the
     *    value[i + 0] = X component [0-1] <BR>
     *    value[i + 1] = Y component [0-1] <BR>
     *    value[i + 2] = Z component [0-1] <BR>
     *    value[i + 3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] vec) {
        if (storedOutput) {
            System.arraycopy(storedOutputValue,0,vec,0,storedOutputValue.length);
        } else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.numElements!=0)
                System.arraycopy(data.floatArrayValue,0,vec,0,data.numElements*4);
        }
    }

    /**
     * Write the value of the event out to the given array.
     *
     * @param vec The array to be filled in where<BR>
     *    value[i][0] = X component [0-1] <BR>
     *    value[i][1] = Y component [0-1] <BR>
     *    value[i][2] = Z component [0-1] <BR>
     *    value[i][3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[][] vec) {
        if (storedOutput) {
            ArrayUtils.raise4(storedOutputValue,storedOutputValue.length/4,vec);
        } else {
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.numElements!=0)
                ArrayUtils.raise4(data.floatArrayValue,data.numElements,vec);
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#initialize(org.web3d.vrml.nodes.VRMLNodeType, int)
     */
    public void initialize(VRMLNodeType srcNode, int fieldNumber) {
        theNode=srcNode;
        fieldIndex=fieldNumber;
    }

    /**
     * @see org.web3d.x3d.sai.MFRoation#insertValue(int, boolean)
     */
    public void insertValue(int index, float[] value) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFRotationWrapper queuedElement=
                (MFRotationWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFRotationWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(storedInputLength+4);
            System.arraycopy(queuedElement.storedInputValue,index*4,queuedElement.storedInputValue,index*4+4,queuedElement.storedInputLength-index*4);
            queuedElement.storedInputValue[index*4]=value[0];
            queuedElement.storedInputValue[index*4+1]=value[1];
            queuedElement.storedInputValue[index*4+2]=value[2];
            queuedElement.storedInputValue[index*4+3]=value[3];
            queuedElement.storedInputLength+=4;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
     */
    public boolean isConglomerating() {
        return isSetOneValue;
    }

    /** Load the current field value from the underlying node and store it as the input value.
     *
     */
    private void loadInputValue() {
        if(!isReadable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedInputValue == null || storedInputValue.length!=value.numElements)
            storedInputValue=new float[value.numElements*4];
        if (value.numElements!=0)
            System.arraycopy(value.floatArrayValue,0,storedInputValue,0,value.numElements*4);
        storedInput=true;
        storedInputLength=storedInputValue.length;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    public void loadOutputValue() {
        if(!isWritable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedOutputValue == null || storedOutputValue.length!=value.numElements*4)
            storedOutputValue=new float[value.numElements*4];
        if (value.numElements!=0)
            System.arraycopy(value.floatArrayValue,0,storedOutputValue,0,value.numElements*4);
        storedOutput=true;
    }

    /**
     * @see org.web3d.x3d.sai.MFRotation#removeValue(int)
     */
    public void removeValue(int index) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFRotationWrapper queuedElement=
                (MFRotationWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFRotationWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            if (queuedElement.storedInputLength>0) {
                if (index*4+4<queuedElement.storedInputLength)
                    System.arraycopy(queuedElement.storedInputValue,
                    		index*4+4,
                    		queuedElement.storedInputValue,
                    		index*4,
                    		queuedElement.storedInputLength-index*4-4);
                queuedElement.storedInputLength-=4;
                if (newEvent)
                    theEventQueue.processEvent(queuedElement);
            } else {
                // Free up the buffer before throwing the exception
                if (newEvent)
                    queuedElement.isSetOneValue=false;
                throw new ArrayIndexOutOfBoundsException();
            }
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#reset()
     */
    public void reset() {
        theNode=null;
        fieldIndex=-1;
        storedOutput=false;
    }

    /**
     * Set a particular rotation in the given eventIn array. To the VRML
     * world this will generate a full MFRotation event with the nominated index
     * value changed.
     * <P>
     * The value array must contain at least four elements. If the array
     * contains more than 4 values only the first four values will be used and
     * the rest ignored.
     * <P>
     * If the index is out of the bounds of the current array of data values or
     * the array of values does not contain at least 4 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the rotation value
     * @param value The array of rotation values where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
     *    values for the rotation
     */
    public void set1Value(int index, float[] value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFRotationWrapper queuedElement=
                (MFRotationWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFRotationWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            System.arraycopy(value,0,queuedElement.storedInputValue,index*4,4);
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * Set the value of the array of rotations. Input is an array of floats
     * values in order required to specify an SFRotation. If value[i] does not
     * contain at least four values an ArrayIndexOutOfBoundsException will be
     * generated. If value[i] contains more than four items only the first
     * four values will be used and the rest ignored.
     *
     * @param numRotations The number of rotations to copy from this array
     * @param value The array of rotation values where<BR>
     *    value[i] = X component [0-1] <BR>
     *    value[i+1] = Y component [0-1] <BR>
     *    value[i+2] = Z component [0-1] <BR>
     *    value[i+3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
     *    values for the rotation
     */
    public void setValue(int numRotations, float[] value) {
        checkWriteAccess();
        MFRotationWrapper queuedElement=this;
        // Input and output buffers do not mix and further don't overwrite
        // set1Value calls
        if (isSetOneValue || storedInput || storedOutput) {
            queuedElement=new MFRotationWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        }
        queuedElement.storedInput=true;
        if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=numRotations*4)
            queuedElement.storedInputValue=new float[numRotations*4];
        System.arraycopy(value,0,queuedElement.storedInputValue,0,numRotations*4);
        theEventQueue.processEvent(queuedElement);
    }

    /**
     * Set the value of the array of rotations. Input is an array of floats
     * values in order required to specify an SFRotation. If value[i] does not
     * contain at least four values an ArrayIndexOutOfBoundsException will be
     * generated. If value[i] contains more than four items only the first
     * four values will be used and the rest ignored.
     *
     * @param numRotations The number of rotations to copy from this array
     * @param value The array of rotation values where<BR>
     *    value[i][0] = X component [0-1] <BR>
     *    value[i][1] = Y component [0-1] <BR>
     *    value[i][2] = Z component [0-1] <BR>
     *    value[i][3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least four
     *    values for the rotation
     */
    public void setValue(int numRotations, float[][] value) {
        checkWriteAccess();
        MFRotationWrapper queuedElement=this;
        // Input and output buffers do not mix and further don't overwrite
        // set1Value calls
        if (isSetOneValue || storedInput || storedOutput) {
            queuedElement=new MFRotationWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        }
        queuedElement.storedInput=true;
        if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=numRotations*4)
            queuedElement.storedInputValue=new float[numRotations*4];
        ArrayUtils.flatten4(value,numRotations,queuedElement.storedInputValue);
        theEventQueue.processEvent(queuedElement);
    }

    /**
     * Get the size of the underlying data array. The size is the number of
     * elements for that data type. So for an MFFloat the size would be the
     * number of float values, but for an MFVec3f, it is the number of vectors
     * in the returned array (where a vector is 3 consecutive array indexes in
     * a flat array).
     *
     * @return The number of elements in this field
     */
    public int getSize() {
        if (storedOutput) {
            return storedOutputValue.length;
        } else {
            return theNode.getFieldValue(fieldIndex).numElements;
        }
    }
}
