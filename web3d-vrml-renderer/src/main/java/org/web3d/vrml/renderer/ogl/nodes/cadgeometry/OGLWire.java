package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

import org.j3d.aviatrix3d.SceneGraphObject;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseWire;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

public class OGLWire extends BaseWire implements OGLVRMLNode{
	public OGLWire(){
		super();
	}
	
	

	public void render() {
		renderer.render();
		
	}
	@Override
	public void setupFinished() {
		for(VRMLNodeType vn:vfEdges)
			vn.setupFinished();
		
		super.setupFinished();
	}



	public SceneGraphObject getSceneGraphObject() {
		// TODO Auto-generated method stub
		return null;
	}

}
