/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.cadgeometry;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseIndexedQuadSet;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null-renderer implementation of the IndexedQuadSet node.
 * <p>
 *
 * @author Vincent Marchetti
 * @version $Revision: 1.1 $
 * copied from the NRQuadSet class by Alan Hudson, just renamed class and constructors
 */
public class NRIndexedQuadSet extends BaseIndexedQuadSet
    implements NRVRMLNode {

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public NRIndexedQuadSet() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRIndexedQuadSet(VRMLNodeType node) {
        super(node);
    }
}
