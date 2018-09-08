package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.IndexedQuadArray;
import org.j3d.aviatrix3d.IndexedVertexGeometry;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.QuadArray;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.VertexGeometry;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseFace;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.xj3d.impl.core.eventmodel.DefaultBrepManager;

public class OGLFace extends BaseFace implements OGLGeometryNodeType,
		NodeUpdateListener {
	/** The geometry implementation */
	private Geometry impl;

	/**
	 * Construct a default WireBREP instance
	 */
	public OGLFace() {
	}

	/**
	 * Construct a new instance of this node based on the details from the given
	 * node.
	 * 
	 * @param node
	 *            The node to copy
	 * @throws IllegalArgumentException
	 *             The node is not a Group node
	 */
	public OGLFace(VRMLNodeType node) {
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
	 * @param geom
	 */
	public void setGeometry(Geometry geom) {
		impl = geom;
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

		if (recompute) {
			impl = (Geometry) renderer.getEmptyImpl();
			for (VRMLNodeType an : vfInnerWire) {
				an.setupFinished();
			}

			vfSurface.setupFinished();

//			 renderer.initialise();

			if (!inSetup) {
				return;
			}

			super.setupFinished();

			boolean threaded=false;
			
			if(threaded)
			{
				this.render();
			}
			else
			{
				try {
					renderer.renderFaceBREP(this);
					this.impl=(Geometry)renderer.getFaceImpl(this);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			//get an empty aviatrix object, so that we can add the face to the sceneGraph before computing its tesselation
			

			recompute = false;
		}

	}

	/**
	 * Aviatrix3D callback used by the Brep Manager in order to asynchronously
	 * update a face geometry
	 * 
	 * @see org.j3d.aviatrix3d.NodeUpdateListener#updateNodeBoundsChanges(java.lang.Object)
	 */
	public void updateNodeBoundsChanges(Object src) {
		// TODO Auto-generated method stub
		try {

			Geometry newImplGeom = (Geometry) renderer.getFaceImpl(this);

			if (newImplGeom instanceof IndexedVertexGeometry) {
				IndexedVertexGeometry newImpl = (IndexedVertexGeometry) newImplGeom;

				float[] vertices = new float[newImpl.getValidVertexCount() * 3];
				newImpl.getVertices(vertices);
				((IndexedVertexGeometry) impl).setValidVertexCount(newImpl
						.getValidVertexCount());
				((IndexedVertexGeometry) impl).setVertices(
						VertexGeometry.COORDINATE_3, vertices);
				int[] indices = new int[newImpl.getValidIndexCount()];
				newImpl.getIndices(indices);
				((IndexedVertexGeometry) impl).setIndices(indices, indices.length);
			}
			else if (newImplGeom instanceof VertexGeometry ) {
				VertexGeometry newImpl = (VertexGeometry) newImplGeom;
				float[] vertices = new float[newImpl.getValidVertexCount() * 3];
				newImpl.getVertices(vertices);
				((VertexGeometry) impl).setValidVertexCount(newImpl
						.getValidVertexCount());
				((VertexGeometry) impl).setVertices(VertexGeometry.COORDINATE_3,
						vertices);
				// int[] indices=new int[newImpl.getValidIndexCount()];
				// newImpl.getIndices(indices);
				// ((IndexedQuadArray)impl).setIndices(indices, indices.length);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateNodeDataChanges(Object src) {

	}

}
