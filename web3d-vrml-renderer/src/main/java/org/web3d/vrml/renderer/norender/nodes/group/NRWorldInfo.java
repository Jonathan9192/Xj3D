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

package org.web3d.vrml.renderer.norender.nodes.group;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.group.BaseWorldInfo;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of a WorldInfo node.
 * <p>
 *
 * This node is purely informational within the scenegraph. It does not have
 * a renderable representation.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class NRWorldInfo extends BaseWorldInfo
    implements NRVRMLNode {

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public NRWorldInfo() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRWorldInfo(VRMLNodeType node) {
        super(node);
    }
}
