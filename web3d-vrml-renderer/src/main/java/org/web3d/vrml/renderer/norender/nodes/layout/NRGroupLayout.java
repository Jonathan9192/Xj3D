/*****************************************************************************
 *                     Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.layout;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.layout.BaseGroupLayout;

/**
 * Null-renderer implementation of a GroupLayout node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRGroupLayout extends BaseGroupLayout
    implements NRVRMLNode {

    /**
     * Construct a new default Overlay object
     */
    public NRGroupLayout() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRGroupLayout(VRMLNodeType node) {
        super(node);
    }
}
