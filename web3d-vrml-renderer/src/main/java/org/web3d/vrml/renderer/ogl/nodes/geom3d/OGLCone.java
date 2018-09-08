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

package org.web3d.vrml.renderer.ogl.nodes.geom3d;

// External imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.TriangleArray;
import org.j3d.aviatrix3d.VertexGeometry;
import org.j3d.aviatrix3d.NodeUpdateListener;

import org.j3d.geom.GeometryData;
import org.j3d.geom.ConeGenerator;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseCone;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

/**
 * OGL implementation of a Cone.
 * <p>
 *
 * A box is a fixed size object in VRML. Once set at startup, the size cannot
 * be changed. All dynamic requests to modify the size of this implementation
 * will generate an exception.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.12 $
 */
public class OGLCone extends BaseCone
    implements OGLGeometryNodeType, NodeUpdateListener {

    /** The geometry implementation */
    private TriangleArray impl;

    /** The number of texture coordinate sets to use */
    private int numTexCoordSets;

    /**
     * Construct new default box object.
     */
    public OGLCone() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Cone node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLCone(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    /*
     * Returns a OGL Geometry node.
     *
     * @return A Geometry node
     */
    public Geometry getGeometry() {
        return impl;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType 
    //-------------------------------------------------------------

    /**
     * Set the number of textures that were found on the accompanying Appearance
     * node. Used to set the number of texture coordinates that need to be
     * passed in to the renderer when no explicit texture coordinates were
     * given.
     *
     * @param count The number of texture coordinate sets to add
     */
    public void setTextureCount(int count) {
        numTexCoordSets = count;

        if(inSetup)
            return;

        if(impl.isLive())
            impl.dataChanged(this);
         else
            updateNodeDataChanges(impl);
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return impl;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //-------------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        ConeGenerator generator = new ConeGenerator(vfHeight,
                                                    vfBottomRadius,
                                                    24,
                                                    vfSide,
                                                    vfBottom);
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        impl = new TriangleArray(false, VertexGeometry.VBO_HINT_STATIC);
        impl.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        impl.setNormals(data.normals);

        // Make an array of objects for the texture setting
        float[][] textures = { data.textureCoordinates };
        int[] tex_type = { TriangleArray.TEXTURE_COORDINATE_2 };
        impl.setTextureCoordinates(tex_type, textures, 1);

        // Setup texture units
        int[] tex_maps = new int[numTexCoordSets];

        for(int i=0; i < numTexCoordSets; i++)
            tex_maps[i] = 0;

        impl.setTextureSetMap(tex_maps, numTexCoordSets);

        OGLUserData u_data = new OGLUserData();
        u_data.geometryData = data;

        impl.setUserData(u_data);

        // Release generator
        generator = null;
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------
     
    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *  
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        int[] tex_maps = new int[numTexCoordSets];

        for(int i=0; i < numTexCoordSets; i++)
            tex_maps[i] = 0;

        impl.setTextureSetMap(tex_maps, numTexCoordSets);
    }
}
