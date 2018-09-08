package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

import java.util.ArrayList;

import javax.media.opengl.GL;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.PointArray;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.VertexGeometry;
import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseCADKernelRenderer;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BasePointBREP;
import org.web3d.vrml.renderer.common.nodes.render.BaseCoordinate;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;


public class OGLPointBREP extends BasePointBREP implements  OGLGeometryNodeType {
    /** The geometry implementation */
    private PointArray impl;

	
    /**
     * Construct a default PointBREP instance
     */
    public OGLPointBREP() {
    	cadKernelRenderer = new CADKernelRenderer();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLPointBREP(VRMLNodeType node) {
        super(node);
    }

    /**
     * Returns a OGL Geometry node
     *
     * @return A Geometry node
     */
    public Geometry getGeometry() {
        return impl;
    }

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
    
    @Override
    public void setupFinished() {
    	
    	cadKernelRenderer.initialise();
    	
    	errorReporter.messageReport("rendering PointBREP");
    	if(!inSetup)
            return;
    	
    	 super.setupFinished();
    	
    	impl=(PointArray)cadKernelRenderer.getPointBREPImpl();
    }





}
