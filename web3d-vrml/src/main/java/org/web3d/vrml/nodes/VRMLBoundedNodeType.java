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
package org.web3d.vrml.nodes;

// Standard imports

// Application specific imports
// none

/**
 * Indicates that a node contains a bounding box field.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public interface VRMLBoundedNodeType extends VRMLChildNodeType {

    /**
     * Get the current value of field the bboxCenter field.
     * The default value is <code>0 0 0</code>.
     *
     * @return The value of bboxCenter(SFVec3f)
     */
    float[] getBboxCenter();

    /**
     * Get the current value of the bboxSize field.
     * The default value is <code>-1 -1 -1</code>.
     *
     * @return The size of the bounding box(SFVec3f)
     */
    float[] getBboxSize();

    /**
     * Check to see if this node has been used more than once. If it has then
     * return true.
     *
     * @return true if this node is shared
     */
    boolean isShared();

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    void setShared(boolean used);
}
