/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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
import javax.vecmath.*;

import org.j3d.aviatrix3d.*;

import org.j3d.geom.GeometryData;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseExtrusion;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;
import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

/**
 * OpenGL implGeomementation of an Extrusion
 * <p> The 1.16 version of OGLExtrusion generates
 * end caps correctly for both convex and concave
 * crossSections.  More thorough testing is needed
 * to see if textures are handled properly, however.
 *
 * @author Justin Couch, Andrzej Kapolka, Rick Goldberg
 * @version $Revision: 1.20 $
 */
public class OGLExtrusion extends BaseExtrusion
    implements OGLGeometryNodeType, NodeUpdateListener{

    /** Message when we detect a solid of revolution */
    private static final String SOR_ERR =
        "Invalid Extrusion data; looks like a solid of revolution";

    /** When the normalisation of the Y axis fails because it is zero length */
    private static final String Y_NORM_MSG =
        "Error normalizing Y in Extrusion";

    /** The OpenGL geometry implGeommentation
     * Previous implementation of OGLExtrusion used an IndexedTriangleStripArray;
     * more recently implGeom is a TriangleArray.        */
    private Geometry implGeom;

    /** The coord array defines the 3D vertices referenced by the coordIndex field. */
    private float[] coords;

    /** The coordIndex array specifies polygonal faces by indexing into coordinates
     * in the 'coords' array.  An index of -1 indicates that the current face has
     * ended and the next one begins.     */
    private int[] coordIndex;

    /** The Point3f[] version of vfSpine.
     * One point for every spine.      */
    private Point3f[] spines;

    /** The Vector3f[] version of vfScale. */
    private Vector3f[] scales;

    /** The AxisAngle4f[] version of vfOrientation. */
    private AxisAngle4f[] orientations;

    /** rotations will contain the per spine transform composed with
     * orientation after the call to calculateSCP(); */
    private Matrix3f[] rotations;
    private Matrix4f[] transforms;

    private boolean collinear;

    /** Flag to say normals have changed when updating the geometry */
    private boolean normalsChanged;

    /** Flag to say texture coords have changed when updating the geometry */
    private boolean texCoordsChanged;

    /** Flag to say colors have changed when updating the geometry */
    private boolean colorsChanged;

    /** Did the vbo state change */
    private boolean vboChanged;

    /** Flag indicating if the spine is closed as a surface of revolution */
    private boolean spineClosed;

    /** Flag to say if the crossSection is closed or not.
     * Fundamental enough that it may belong in the base class */
    boolean crossSectionClosed;

    /** Userdata kept in the triangle geometry */
    protected GeometryData geomData;

    /** Set to "numCrossSection-1" if(crossSectionClosed)
     * else equal to numCrossSection.  */
    int uniqueCrossSectionPoints;

    /**
     * Construct a default sphere instance
     */
    public OGLExtrusion() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLExtrusion(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the UpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        ((TriangleArray)implGeom).setVertices(  TriangleArray.COORDINATE_3,
                                                geomData.coordinates,
                                                geomData.vertexCount);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {

/*        if(colorsChanged) {
            int num_comp =
                (vfColor == null) ? 0 : vfColor.getNumColorComponents();
            boolean has_alpha = num_comp == 4;

            implGeom.setColors(has_alpha, geomData.colors);
            colorsChanged = false;

            if (vfColor == null) {
                localColors = false;
            }
        }

        if(normalsChanged) {*/
            ((TriangleArray)implGeom).setNormals(geomData.normals);
            /*normalsChanged = false;
        }

        if(texCoordsChanged) {
            implGeom.setTextureCoordinates(texTypes,
                                           texCoords,
                                           numUniqueTexSets);
            implGeom.setTextureSetMap(texSetMap, numTexSets);

            texCoordsChanged = false;
        }

        if (vboChanged) {
            // Only ever change to false after activity
            implGeom.setVBOEnabled(false);

            vboChanged = false;
        }*/
    }

    //-------------------------------------------------------------
    // Methods used to set implGeom as TriangleArray
    //-------------------------------------------------------------

    /**
     * Build the implGeom, set the normals, etc.:
     * Basically let GeometryUtils do all the work.
     *
     * @author Eric Fickenscher
     */
    private void buildImplTriangleArray(){

        // convert vrml data to intermediate form
        initSetup();
        // calculate per spine SCP transforms
        // results in transforms[] being filled withs SCP info
        // complete with scale, translation and orientation from
        // fields
        if(!calculateSCP()) return;

        // transform the crossSections to coordinates
        createExtrusionCoordinates();

        // set coordIndex to create an IndexedFaceSet representing this extrusion
        createIndicesTriangleArray();
        //printIndices();

        GeometryUtils gutils = new GeometryUtils();
        GeometryHolder gholder = new GeometryHolder();

        gutils.generateTriangleArrays(  coords,
                                        null,   // float[] color
                                        null,   // float[] normal
                                        null,   // float[] texture
                                        1,
                                        true,   // genTexCoords,
                                        true,   // genNormals,
                                        coordIndex,
                                        coordIndex.length,
                                        null,   // int[] vfColorIndex,
                                        null,   // int[] vfNormalIndex,
                                        null,   // int[] vfTexCoordIndex,
                                                // are we obeying the ccw right-hand-
                                        true,   // rule or not?  this is far different
                                                // than the meaning of vfCCW!
                                        vfConvex,
                                        false,  // colorPerVertex,
                                        true,   // normalPerVertex,
                                        0,
                                        vfCreaseAngle,
                                        true,   // initialBuild,
                                        gholder);

        geomData = new GeometryData();
        geomData.geometryType = GeometryData.TRIANGLES;
        gutils.copyData(gholder, geomData);

        if (implGeom.isLive())
            implGeom.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGeom);

        if (implGeom.isLive())
            implGeom.dataChanged(this);
        else
            updateNodeDataChanges(implGeom);

    }

    /**
     * Result: Completed "coords" array: An array of all the float information
     * describing each vertex in the extrusion, created by applying the
     * transforms to the vfCrossSection points
     *
     * @author Eric Fickenscher
     */
    private void createExtrusionCoordinates(){

        // calculate the number of coordinates needed for the sides
        // of the extrusion: 3 coordinates per vertex, one vertex per
        // crossSectionPoint, and one set of crossSectionPoints per spinePoint
        coords = new float[ numSpine * uniqueCrossSectionPoints * 3 ];

        for(int i = 0; i < numSpine; i++) {

            Matrix4f tx = transforms[i];

            for(int j = 0; j < uniqueCrossSectionPoints; j++) {

                int ind = (i * uniqueCrossSectionPoints + j) * 3;

                // basically a transform, in place
                float c_x = vfCrossSection[j*2   ];
                float c_z = vfCrossSection[j*2 +1];

                float x = c_x * tx.m00 + c_z * tx.m02 + tx.m03;
                float y = c_x * tx.m10 + c_z * tx.m12 + tx.m13;
                float z = c_x * tx.m20 + c_z * tx.m22 + tx.m23;

                coords[ind] = x;
                coords[ind + 1] = y;
                coords[ind + 2] = z;
            }
        }
    }

    /**
     * Result: Completed "coordIndex" array: an int array representing an
     * IndexedFaceSet representation of the extrusion.
     *
     * @author Eric Fickenscher
     */
    private void createIndicesTriangleArray(){

        int sizeOfCoordIndex = 5*(numCrossSection-1) * (numSpine-1);
        if( vfBeginCap) sizeOfCoordIndex += uniqueCrossSectionPoints+1;
        if( vfEndCap)   sizeOfCoordIndex += uniqueCrossSectionPoints+1;

        coordIndex = new int[sizeOfCoordIndex];

        int indx = 0;
        int curIndex;

        // for each separate segment between two spine points
        for(int i = 0; i<numSpine-1; i++){

            curIndex = i*uniqueCrossSectionPoints;

            // build a quadrilateral for every crossSection-to-crossSection side around that segment
            // note that Xj3D wireframe mode shows triangulation even though quads are being built here
            for(int j = 0; j < numCrossSection-1; j++){

                if(vfCCW){
                    coordIndex[ indx++ ] = j + curIndex;
                    coordIndex[ indx++ ] = j + curIndex +1;
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints +1;
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints;
                } else {
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints;
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints +1;
                    coordIndex[ indx++ ] = j + curIndex +1;
                    coordIndex[ indx++ ] = j + curIndex;
                }

                coordIndex[ indx++ ] = -1;
            }

            if(crossSectionClosed){
                coordIndex[indx -4] -= uniqueCrossSectionPoints;
                coordIndex[indx -3] -= uniqueCrossSectionPoints;
            }
        }

        // note that Xj3D wireframe mode shows triangulation even though N-sided polygons are being built here
        if( vfBeginCap) {

            for(int i = 0; i < uniqueCrossSectionPoints; i++){
                if(vfCCW) coordIndex[ indx++ ] = uniqueCrossSectionPoints -i -1;
                else coordIndex[ indx++ ] = i;
            }
            coordIndex[ indx++ ] = -1;
        }
        if( vfEndCap) {

            for(int i = 0; i < uniqueCrossSectionPoints; i++){
                if(vfCCW) coordIndex[ indx++ ] = (numSpine-1)*uniqueCrossSectionPoints + i;
                else coordIndex[ indx++ ] = numSpine*uniqueCrossSectionPoints -i -1;
            }
            coordIndex[ indx ] = -1;
        }
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Returns a OGL Geometry node
     *
     * @return A Geometry node
     */
    public Geometry getGeometry() {
        return implGeom;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    @Override
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    @Override
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, value, numValid);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the j3d representation
     * only once per frame.
     */
    @Override
    public void allEventsComplete() {
        buildImplTriangleArray();
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        // if using a TriangleArray instead of an IndexedTriangleStripArray {
        implGeom = new TriangleArray(true, VertexGeometry.VBO_HINT_STATIC);
        //OGLUserData u_data = new OGLUserData();
        //u_data.geometryData = geomData;
        //implGeom.setUserData(u_data);

        buildImplTriangleArray();

        //} else {
        //    implGeom = new IndexedTriangleStripArray(true, VertexGeometry.VBO_HINT_STATIC);
        //    buildImpl();
        //}
    }

    //-------------------------------------------------------------
    // Local methods
    //-------------------------------------------------------------

    /**
     * Instantiate the various variables needed for processing.
     */
    private void initSetup() {

        normalsChanged = false;
        texCoordsChanged = false;
        colorsChanged = false;
        vboChanged = false;

        collinear = false;
        spineClosed =  (vfSpine[0] == vfSpine[vfSpine.length-3] &&
                        vfSpine[1] == vfSpine[vfSpine.length-2] &&
                        vfSpine[2] == vfSpine[vfSpine.length-1]);

        uniqueCrossSectionPoints = numCrossSection;
        crossSectionClosed =
            (vfCrossSection[0] == vfCrossSection[vfCrossSection.length-2] &&
             vfCrossSection[1] == vfCrossSection[vfCrossSection.length-1]);
        if(crossSectionClosed) uniqueCrossSectionPoints--;

        // Convert the spine array into a Point3f array for
        // easier manipulation later
        spines = new Point3f[numSpine];
        for(int i = 0; i < spines.length; i++) {
            spines[i] = new Point3f(vfSpine[i * 3],
                                    vfSpine[i * 3 + 1],
                                    vfSpine[i * 3 + 2]);
        }

        // Convert the orientation points so they match specification
        //
        // Note: if the number of scale or orientation points is greater
        // than the number of spine points, the excess values are ignored.
        // If they contain one value, it is applied at all spine points.
        // (results are 'undefined' if the number of sets of scale or orientation
        // values is greater than one but less than the number of spine
        // points... in such a case, we repeat the final set of values for
        // the remainder of spine points)
        orientations = new AxisAngle4f[numSpine];
        for(int i = 0; i < orientations.length; i++) {
            if(i * 4 + 3 < vfOrientation.length)
                orientations[i] = new AxisAngle4f(
                    vfOrientation[i * 4],
                    vfOrientation[i * 4 + 1],
                    vfOrientation[i * 4 + 2],
                    vfOrientation[i * 4 + 3]
               );
            else
                orientations[i] = new AxisAngle4f(orientations[i-1]);
        }

        // Convert the scale points so they match specification
        //
        // Note that scales are really just 2D scalars,
        // but this version uses a 3f for some reason,
        // leaving the "y" value as 1.
        scales = new Vector3f[numSpine];
        for(int i = 0; i < scales.length; i++) {
            if(i * 2 + 1 < vfScale.length)
                scales[i] = new Vector3f(vfScale[i * 2],
                                         1,
                                         vfScale[i * 2 + 1]);
            else
                scales[i] = new Vector3f(scales[i-1]);
        }

        rotations = new Matrix3f[vfSpine.length / 3];

        // if entirely collinear
        Vector3d v2 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v0 = new Vector3d();
        double d = 0;
        for(int i = 1; i < spines.length - 1; i++) {
            v2.set(spines[i+1]);
            v1.set(spines[i]);
            v0.set(spines[i - 1]);
            v2.sub(v1);
            v1.sub(v0);
            v0.cross(v2,v1);
            d += v0.dot(v0);
        }

        collinear = (d == 0);
    }

    /**
     * Create the spine information and verify that this is a valid object.
     *
     * @return true if everything is valid, false otherwise
     */
    private boolean calculateSCP() {
        // find an orthonormal basis and construct rotation matrix
        // for each spine. handle special cases in second pass
        Vector3f u,v;

        int last = numSpine - 1;
        Vector3f[] x,y,z;

        x = new Vector3f[numSpine];
        y = new Vector3f[numSpine];
        z = new Vector3f[numSpine];

        if(collinear) {
            if(spineClosed) {
                errorReporter.warningReport(SOR_ERR, null);
                StringBuilder buf = new StringBuilder("Spine data:");

                for(int i = 0; i < spines.length; i++) {
                   buf.append(spines[i]);
                   buf.append(' ');
                }

                errorReporter.messageReport(buf.toString());

                return false;
            }

            // Direction is the first spine point that does not equal to
            // spines[0]
            Vector3f direction = null;
            for(int i = 0; i < spines.length; i++) {
                if(!spines[0].equals(spines[i])) {
                    direction = new Vector3f(spines[i]);
                }
            }

            y[0] = new Vector3f();
            y[0].sub(direction, spines[0]);


            if (!norm(y[0])) {
                errorReporter.warningReport(Y_NORM_MSG, null);
            }

            // Create an initial x[0]
            if(y[0].x == 1)
                x[0] = new Vector3f(0,-1,0);
            else if(y[0].x == -1)
                x[0] = new Vector3f(0,1,0);
            else
                x[0] = new Vector3f(1,0,0);
            // Create z[0]
            z[0] = new Vector3f();
            z[0].cross(x[0],y[0]);

            // Create final x[0]
            x[0].cross(y[0],z[0]);
            for(int i = 1; i < spines.length; i++) {
                // redo, this should take the direction of y
                // redone by Pasi Paasiala < < check this >  >
                x[i] = new Vector3f(x[0]);
                y[i] = new Vector3f(y[0]);
                z[i] = new Vector3f(z[0]);
            }
        } else { // "collinear" is false


            // find y[i] for all but first and last
            // most times the exception cases are bad data and hopefully
            // wont happen. It is free to try catch you later, so hopes
            // 99% cases will be one if faster by not checking the if
            for(int i = 1; i < last; i++) {
                y[i] = new Vector3f();
                y[i].sub(spines[i+1], spines[i - 1]);
                if(!norm(y[i])) {
                    // spines[i+1] equals spines[i - 1]
                    y[i].sub(spines[i+1], spines[i]);
                    if(!norm(y[i])) {
                        // spines[i+1] equaled spines[i]
                        y[i].sub(spines[i], spines[i - 1]);
                        if(!norm(y[i])) {

                            // spines[i] equaled spines[i - 1]
                            // real bad case, do something
                            int w=i+2;
                            while ((w < last+1) && (spines[i - 1].equals(spines[w]))) {
                                w++;
                            }
                            if(w < last+1) {
                                y[i].sub(spines[w],spines[i - 1]);
                                norm(y[i]); // should never divide by zero here
                            } else { // worst worst case
                                y[i] = new Vector3f(0,1,0);
                            }
                        }
                    }
                }
            }

            // y for ends
            if(spineClosed) {
                // spineClosed and not collinear - >  not all one point
                y[0] = new Vector3f();
                y[0].sub(spines[1],spines[last-1]);
                if(!norm(y[0])) {
                    // bad case that the spine[n-2] == spine[1]
                    int w = last-2;
                    while((w > 1) && (spines[1].equals(spines[w]))) {
                        w--;
                    }
                    if(w > 1) {
                        y[0].sub(spines[1],spines[w]);
                        norm(y[0]); // should never divide by zero here
                    } else
                        // how did this happen?
                        y[0].set(0,0,1);
                }
                y[last] = new Vector3f(y[0]);
            } else {
                y[0] = new Vector3f();
                y[last] = new Vector3f();
                y[0].sub(spines[1],spines[0]);
                if(!norm(y[0])) {
                    int w=2;
                    while ((w < last) && (spines[0].equals(spines[w]))) {
                        w++;
                    }
                    if(w < last) {
                        y[0].sub(spines[w],spines[0]);
                        norm(y[0]); // should not divide by zero here
                    } else
                        y[0].set(0,0,1);
                }
                y[last] = new Vector3f();
                y[last].sub(spines[last],spines[last-1]);

                if(!norm(y[last])) {
                    int w=last-2;
                    while ((w > -1) && (spines[last].equals(spines[w]))) {
                        w--;
                    }
                    if(w > -1)  {
                        y[last].sub(spines[last],spines[w]);
                        norm(y[last]);
                    } else
                        y[last].set(0,0,1);
                }
            }

            // now z axis for each spine
            // first all except first and last
            boolean recheck = false;
            for(int i = 1; i < last; i++) {
                u = new Vector3f();
                v = new Vector3f();
                z[i] = new Vector3f();
                u.sub(spines[i - 1],spines[i]);
                v.sub(spines[i+1],spines[i]);
                // spec seems backwards on u and v
                // shouldn't it be z[i].cross(u,v)???
                //z[i].cross(v,u);
                //-- >  z[i].cross(u,v); is correct < < check this >  >
                // Modified by Pasi Paasiala (Pasi.Paasiala@solibri.com)
                z[i].cross(u,v);
                if(!norm(z[i]))
                    recheck=true;
            }
            if(spineClosed) {
                z[0] = z[last] = new Vector3f();
                u = new Vector3f();
                v = new Vector3f();
                u.sub(spines[last-1],spines[0]);
                v.sub(spines[1],spines[0]);
                try {
                    z[0].cross(u,v);
                } catch (ArithmeticException ae) {
                    recheck=true;
                }
            } else { // not spineClosed
                z[0] = new Vector3f(z[1]);
                z[last] = new Vector3f(z[last-1]);
            }

            if(recheck) { // found adjacent collinear spines
                // first z has no length ?
                if(z[0].dot(z[0]) == 0) {
                    for(int i = 1; i < spines.length; i++) {
                    if(z[i].dot(z[i]) > 0)
                        z[0] = new Vector3f(z[i]);
                }
                // test again could be most degenerate of cases
                if(z[0].dot(z[0]) == 0)
                    z[0] = new Vector3f(0,0,1);
            }

            // check rest of z's
            for(int i = 1; i < last+1; i++) {
                if(z[i].dot(z[i]) == 0)
                    z[i] = new Vector3f(z[i - 1]);
                }
            }

            // finally, do a neighbor comparison
            // and evaluate the x's
            for(int i = 0; i < spines.length; i++) {
                if((i > 0) && (z[i].dot(z[i - 1]) < 0))
                    z[i].negate();

                // at this point, y and z should be nice
                x[i] = new Vector3f();

                //Original was: x[i].cross(z[i],y[i]); < < check this >  >
                //but it doesn't result in right handed coordinates
                // Modified by Pasi Paasiala
                x[i].cross(y[i],z[i]);
                norm(x[i]);
            }
        }

        // should now have orthonormal vectors for each
        // spine. create the rotation matrix with scale for
        // each spine. spec is unclear whether a twist imparted
        // at one of the spines is inherited by its "children"
        // so assume not.
        // also, the order looks like SxTxRscpxRo , ie ,
        // the spec doc looks suspect, double check
        Matrix3f m = new Matrix3f();
        transforms = new Matrix4f[spines.length];
        for(int i = 0; i < spines.length; i++) {
            rotations[i] = new Matrix3f();
            // Original had setRow. This is correct < < check this >  >
            // Modified by Pasi Paasiala
            rotations[i].setColumn(0,x[i]);
            rotations[i].setColumn(1,y[i]);
            rotations[i].setColumn(2,z[i]);
        }

        Matrix3f[] correctionRotations = createCorrectionRotations(z);
        Vector3f tmp = new Vector3f();
        // Create the transforms
        for(int i = 0; i < spines.length; i++) {
            rotations[i].mul(correctionRotations[i]);
            m.set(orientations[i]);
            rotations[i].mul(m);
            transforms[i] = new Matrix4f();
            transforms[i].setIdentity();
            m.m00 = scales[i].x;
            m.m11 = scales[i].y; // should always be '1'
            m.m22 = scales[i].z;
            m.mul(rotations[i]);
            transforms[i].setRotationScale(m);

            tmp.set(spines[i]);
            transforms[i].setTranslation(tmp);
        }

        return true;
    }

    /**
     * Creates a rotation for each spine point to avoid twisting of the profile
     * when the orientation of SCP changes.
     * @author Pasi Paasiala
     * @param z the vector containing the z unit vectors for each spine point
     */
    private Matrix3f[] createCorrectionRotations(Vector3f[] z) {

        Matrix3f[] correctionRotations = new Matrix3f[spines.length];
        correctionRotations[0] = new Matrix3f();
        correctionRotations[0].setIdentity();
        AxisAngle4f checkAngle = new AxisAngle4f();

        // testPoint is used to find the angle that gives the smallest distance
        // between the previous and current rotation. Find a point that is not
        // in the origin.
        Point3f testPoint = new Point3f(vfCrossSection[0], 0, vfCrossSection[1]);

        for(int i = 0; i < numCrossSection; i++) {
            if(vfCrossSection[i*2] != 0 || vfCrossSection[i*2 +1] != 0) {
                testPoint = new Point3f(vfCrossSection[i*2], 0, vfCrossSection[i*2 +1]);
                break;
            }
        }

        // Fix the orientations by using the angle between previous z and current z
        for(int i = 1; i < spines.length; i++) {
            float angle = z[i].angle(z[i - 1]);
            correctionRotations[i] = correctionRotations[i - 1];
            if(angle != 0) {
                correctionRotations[i] = new Matrix3f(correctionRotations[i - 1]);
                Point3f previous = new Point3f();
                //Point3f previous = testPoint;
                // Test with negative angle:
                Matrix3f previousRotation = new Matrix3f(rotations[i - 1]);
                previousRotation.mul(correctionRotations[i - 1]);
                previousRotation.transform(testPoint, previous);
                Matrix3f delta = new Matrix3f();
                delta.setIdentity();
                delta.rotY(-angle);
                correctionRotations[i].mul(delta);

                Matrix3f negativeRotation = new Matrix3f(rotations[i]);
                negativeRotation.mul(correctionRotations[i]);

                Point3f pointNegative = new Point3f();
                negativeRotation.transform(testPoint,pointNegative);

                float distNegative = pointNegative.distance(previous);

                // Test with positive angle
                delta.rotY(angle*2);
                correctionRotations[i].mul(delta);
                Matrix3f positiveRotation = new Matrix3f(rotations[i]);
                positiveRotation.mul(correctionRotations[i]);
                Point3f pointPositive = new Point3f();
                positiveRotation.transform(pointPositive);
                float distPositive = pointPositive.distance(previous);

                if(distPositive > distNegative) {
                    // Reset correctionRotations to negative angle
                    delta.rotY(-angle*2);
                    correctionRotations[i].mul(delta);
                }

                // Check that the angle is not more than PI.
                // If it is subtract PI from angle
                checkAngle.set(correctionRotations[i]);
                if(((float)Math.PI - checkAngle.angle) < 0.001) {
                    correctionRotations[i].rotY((float)(checkAngle.angle - Math.PI));
                }
            }
        }

        return correctionRotations;
    }

    /**
     * Variant on the traditional normalisation process. If the length is zero
     * we've had something go wrong so should let the user know, courtesy of
     * the return value.
     *
     * @param n The vector to normalise
     * @return false when the vector is zero length
     */
    private boolean norm(Vector3f n) {

        float norml = n.x*n.x + n.y*n.y + n.z*n.z;

        if(norml == 0)
            return false;

        norml = 1 / (float)Math.sqrt(norml);

        n.x *= norml;
        n.y *= norml;
        n.z *= norml;

        return true;
    }

    /**
     * Just a helper method that may prove useful for debugging purposes.
     */
    private void printCrossSectionPoints(){
        for(int i = 0; i < vfCrossSection.length; i++){
            if(vfCrossSection[i] >= 0) System.out.print(" ");
            System.out.print(vfCrossSection[i++]);
            System.out.print(", " + vfCrossSection[i]);
            if((i/2)%numCrossSection == numCrossSection-1) System.out.println();
            else System.out.print("\t");
        }
        System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }
    /**
     * Just a helper method that may prove useful for debugging purposes.
     */
    private void printCoords(){
        for(int i = 0; i < coords.length; i++){
            if(coords[i] >= 0) System.out.print(" ");
            System.out.print(coords[i]);
            if(i%(uniqueCrossSectionPoints*3) == (uniqueCrossSectionPoints*3-1)) System.out.println();
            else if(i%3 == 2) System.out.print("\t");
            else System.out.print(", ");
            if(i%3 == 0) i++;
        }
        System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }
    /**
     * Just a helper method that may prove useful for debugging purposes.
     */
    private void printIndices(){
        for(int i = 0; i < coordIndex.length; i++){
            if(coordIndex[i] == -1) System.out.println(coordIndex[i]);
            else System.out.print(coordIndex[i] + ",\t");
        }
        System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }


    //----------------------------------------------------------
    // Old Methods that used an IndexedTriangleStripArray instead of TriangleArray
    //----------------------------------------------------------

    /*
     * The previous implementation of OGLExtrusion used an
     * IndexedTriangleStripArray for the underlying geometry.
     * Also note: no need for the additional overhead
     * of crossSectionPts, particularly
     * since the "y" value is always zero - Eric Fickenscher

    private void buildImpl() {

        // convert vrml data to intermediate form
        initSetup();
        // calculate per spine SCP transforms
        // results in transforms[] being filled withs SCP info
        // complete with scale, translation and orientation from
        // fields
        if(!calculateSCP())
            return;

        // transform the crossSections to coordinates
        createExtrusion();
        // indexify, including endcaps if needed
        // leaves coordIndex with index and stripCounts with counts
        // per facet.
        createIndices();

        ((IndexedTriangleStripArray)implGeom).setVertices(IndexedTriangleStripArray.COORDINATE_3, coords);
        ((IndexedTriangleStripArray)implGeom).setIndices(coordIndex, coordIndex.length);
        ((IndexedTriangleStripArray)implGeom).setStripCount(stripCounts, stripCounts.length);

        float ca = vfCreaseAngle;
        if(ca < 0)
            ca = 0;
        else if(ca > (float)Math.PI)
            ca -= (float)Math.PI;

        generateNormals();
        generateTexCoords();
    }

    /*
     * create a list of unique coords (of Point3f)
     * by applying the transforms to the crossSectionPts
     *
    private void createExtrusion() {

        // TODO: spines.length can be replaced with numSpine.
        // crossSectionPts.length can be replaced by numCrossSection
        int req_size = spines.length * crossSectionPts.length * 3;

        if((coords == null) || (coords.length < req_size))
            coords = new float[req_size];

        // TODO: We don't need to use spines.length when we have numSpine already
        for(int i = 0; i < spines.length; i++) {

            Matrix4f tx = transforms[i];

            // TODO: We don't need to use crossSectionPts.length when we have
            // numCrossPoints already
            for(int j = 0; j < crossSectionPts.length; j++) {

                int ind = (i * crossSectionPts.length + j) * 3;

                // basically a transform, in place
                float c_x = crossSectionPts[j].x;
                float c_y = crossSectionPts[j].y;
                float c_z = crossSectionPts[j].z;

                float x = c_x * tx.m00 + c_y * tx.m01 + c_z * tx.m02 + tx.m03;
                float y = c_x * tx.m10 + c_y * tx.m11 + c_z * tx.m12 + tx.m13;
                float z = c_x * tx.m20 + c_y * tx.m21 + c_z * tx.m22 + tx.m23;

                coords[ind] = x;
                coords[ind + 1] = y;
                coords[ind + 2] = z;
            }
        }
    }

    // wind the coords with indexed connectivity and create
    // stripCounts see page 47 of small bluebook
    private void createIndices() {

        // We use 'm' to determine the number of triangleStrips needed (the
        // size of coordIndex).  Why not rename it sizeOfCoordIndex or something?
        int m = 0;
        int k = numCrossSection; // crossSectionPts.length;
        int s = 0;

        if(vfEndCap) {
            // TODO: We don't need to use crossSectionPts.length (or k,
            // for that matter) when we have numCrossSection already
            m += k - 1;
            s++;
        }

        if(vfBeginCap) {
            m += k - 1;
            s++;
        }

        // if both caps exist, m is equal to "2*numCrossPoints -2"
        // if one cap exists, m is equal to "numCrossPoints - 1"
        // if no caps exist, m is equal to zero

        // TODO: We don't need to use spines.length when we have numSpine already
        m += (spines.length - 1) * (4 * (k - 1));
        // m += (numSpine -1) * ( 4 * (numCrossPoints - 1))

        coordIndex = new int[m];

        //////////////////////////////////////////////////////////////////
        // @author Eric Fickenscher
        //
        // This code handles the triangle strips a little differently,
        // such as one strip per spine point (instead of one strip per face per
        // spine point).  It also properly determines the number of triangleStrips
        // needed for the endcaps of a convex crossSection by using the triangulator.
        //
        // Removed since the new Extrusion uses TriangleArray geometry instead of
        // IndexedTriangleStripArray geometry.
        //////////////////////////////////////////////////////////////////
//
//        // one triangle strip for each individual spine section
//        int numberOfStripCounts = numSpine-1;
//        int numTrisForConcave = 0;
//
//        if(vfConvex){
//            if(vfBeginCap) numberOfStripCounts ++;
//            if(vfEndCap) numberOfStripCounts ++;
//        } else {
//            // the object is concave and so we must call the triangulator
//            TriangulationUtils triangulator = new TriangulationUtils();
//
//            float[] triangulationCoords = new float[numCrossSection*3];
//
//            // this 'for' loop translates the 2d vfCrossSection coordinates
//            // into 3 dimensions, with all the 'y' values equaling zero.
//            for(int i = 0; i < numCrossSection; i++){
//                triangulationCoords[(i*3)   ] = vfCrossSection[(i*2)    ];
//                triangulationCoords[(i*3 +2)] = vfCrossSection[(i*2 + 1)];
//            }
//
//            // since the crossSection-triangulation points are in the x-z plane,
//            // they have a normal parallel to the y-axis
//            float[] normal = {0, 0, 0};
//            if(vfCCW) normal[1] = 1;
//            else normal[1] = -1;
//
//            // output must have enough room for all the triangles of the end cap
//            int[] output = new int[3*(numCrossSection-2)];
//
//            // how many triangles are needed?
//            // TODO: consider using costlier code that minimizes the number of triangles
//            numTrisForConcave = triangulator.triangulateConcavePolygon(
//                                                    triangulationCoords,
//                                                    output,
//                                                    normal);
//
//            // add another strip for each triangle needed
//            if(vfBeginCap) numberOfStripCounts += numTrisForConcave;
//            if(vfEndCap) numberOfStripCounts += numTrisForConcave;
//        }
//
//        stripCounts = new int[numberOfStripCounts];
//
//        // the sides of the extrusion can be described with a triStrip with length
//        // equal to (number of sides * 2)+2.  This assumes two triangles per side.
//        // We need one such tri strip for each individual spine section.
//        for(int i = 0; i< numSpine-1; i++){
//            stripCounts[i] = 2* numCrossSection;
//        }
//
//        if(vfConvex){
//            // the cap of a convex extrusion can be described by a triStrip
//            // with length equal to numCrossSection
//            int capIndex = numSpine-1;
//            if(vfBeginCap) stripCounts[capIndex++] = numCrossSection;
//            if(vfEndCap) stripCounts[capIndex] = numCrossSection;
//        } else {
//            // TODO: Clearly, if we switch to code that minimizes the number of
//            // triangles, we will need to be smarter about determining the length
//            // of the different triStrips composing each cap
//            for(int i = numSpine-1; i< stripCounts.length; i++){
//                stripCounts[i] = 3;
//            }
//        }

        // TODO: We don't need to use spines.length when we have numSpine already
        // It seems better to use data from the base class whenever possible
        stripCounts = new int[s + (spines.length - 1) * (k - 1)];

        // stripCounts is an array of integers with length equal to
        // #of caps + (numSpine-1)*(numCrossSection-1)

        s = 0;

        // start with extrusion body from bottom.
        int n = 0; // coordIndex count
        if(vfCCW) {
            // for (int i = 0; i< (numSpine); i++){
            for(int i = 0; i < spines.length - 1; i++) {

                // for (int j = 0; j< (numCrossSection-1); j++){
                for(int j = 0; j < k - 1; j++) {

                    coordIndex[n] = (i * k) + j + 1;
                    coordIndex[n + 1] = ((i + 1) * k) + j + 1;
                    coordIndex[n + 2] = (i * k) + j;
                    coordIndex[n + 3] = ((i + 1) * k) + j;

                    stripCounts[s++] = 4;
                    n += 4;
                }
            }
        } else {
            for(int i = 0; i < spines.length - 1; i++) {
                for(int j = 0; j < k - 1; j++) {
                    coordIndex[n] = ((i + 1) * k) + j;
                    coordIndex[n + 1] = ((i + 1) * k) + j + 1;
                    coordIndex[n + 2] = (i * k) + j;
                    coordIndex[n + 3] = (i * k) + j + 1;

                    stripCounts[s++] = 4;
                    n += 4;
                }
            }
        }

        // add top and bottom
        // note: when switching cw from ccw notice that
        // the index is off by one, this is ok since there
        // is one extra point in the cross-section, each
        // cap has 2 ways to be drawn
        // also note top and bottom caps are reverse oriented to
        // each other
// TODO:
// These are going to be badly borked. The index list is not taking into
// account the circular cross section doesn't work as a tri-strip. Not only
// that but for any concave cross section it is going to look wrong. Really
// need to generate a separate geometry for the two end caps and make use of
// the triangulator to generate the correct geometry.

/* commenting these end caps out...
 * I'm not a fan of their behavior.
        if(vfBeginCap && vfEndCap) {

            // remember, m is equal to coordIndex.length
            // k is equal to numCrossSection
            int indB = m - (2 * (k - 1));
            int indE = m - (k - 1);

            int l = coords.length / 3;

            if(!vfCCW) {
                for(int i = 0; i < k - 1; i++)
                    coordIndex[indB++] = i;

                for(int i = l - 1; i > l - k; i--)
                    coordIndex[indE++] = i;

            } else {

                for(int i = k - 1; i > 0; i--)
                    coordIndex[indB++] = i;

                for(int i = 0; i < k - 1; i++)
                    coordIndex[indE++] = l - (k - 1) + i;

            }

            stripCounts[s++] = k - 1;
            stripCounts[s++] = k - 1;
        } else if(vfBeginCap) {
            int l = coords.length / 3;
            int ind = m - (k - 1);
            if(!vfCCW) {
                for(int i = 0; i < k - 1; i++)
                    coordIndex[ind++] = i;
            } else {
                // this is ok since extra x-sectpt give off by one
                for(int i = k - 1; i > 0; i--)
                    coordIndex[ind++] = i;
            }
            stripCounts[s++] = k - 1;
        } else if(vfEndCap) {
            int l = coords.length / 3;
            int ind = m - (k - 1);
            if(vfCCW) {
                for(int i = l - (k - 1); i < l; i++)
                    coordIndex[ind++] = i;
            } else {
                for(int i = l - 1; i > l-k; i--)
                    coordIndex[ind++] = i;
            }

            stripCounts[s++] = k - 1;
        }
   */
/*
        // @author Eric Fickenscher
        // new code to calculate endcaps.
        // Note: This will NOT work for concave endcaps...
        // only for convex crossSections!
        // TODO: split concave crossSection into smaller
        // convex bits and proceed normally from that point.
        int beginIndex = m-(2 * (numCrossSection-1));
        int endIndex = beginIndex + numCrossSection -1;

        if(vfBeginCap){

            coordIndex[beginIndex++] = 1;
            int geomIndex = 1;
            boolean flip = true;

            for(int i = beginIndex; i<endIndex; i++){
                if(flip) {
                    coordIndex[i] = 1+geomIndex;
                } else {
                    coordIndex[i] = numCrossSection - geomIndex;
                    geomIndex ++;
                }
                flip = !flip;
            }
            stripCounts[s++] = numCrossSection -1;
        }
        if(vfEndCap){

            beginIndex = endIndex;
            endIndex += numCrossSection -1;

            coordIndex[beginIndex++] = numCrossSection;
            int geomIndex = 1;
            boolean flip = true;

            for(int i = beginIndex; i<endIndex; i++){
                if(flip) {
                    coordIndex[i] = geomIndex + numCrossSection;
                } else {
                    coordIndex[i] = (numCrossSection*2) - geomIndex -1;
                    geomIndex ++;
                }
                flip = !flip;
            }
            stripCounts[s++] = numCrossSection -1;
        }

        // If the crossSection is closed
        // then we want to have the first and last vertex share the same index.
        // This makes it far easier to calculate normals that are smoothed
        // across each face. Go back and check each index and see if it is one
        // of those effected. If it is, modify it to be the start vertex.
        /* @author Eric Fickenscher: I think this should be handled
         * differently (ie: earlier in the method, prior to the endCap indexing)
        if(crossSectionClosed) {
            for(int i = 0; i < coordIndex.length; i++) {
                if(((coordIndex[i] + 1) % k == 0) && coordIndex[i] != 0) {
                    coordIndex[i] -= k - 1;
                }
            }
        }
    }

    // Once the basic geometry is complete, come here to generate the normals.
    // For now it ignores creaseAngle and smooths everything.
    private void generateNormals() {
        int num_faces = 0;

        // TODO: we can get rid of a lot of subtractions in favor
        // of one multiplication:
        // for (int i = 0; i < stripCounts.length; i++){
        //  num_faces += stripCounts[i];
        //}
        // num_faces -= 2*stripCounts.length;
        for(int i = 0; i < stripCounts.length; i++)
            num_faces += stripCounts[i] - 2;



        // First create normals for each face.
        float[] face_normals = new float[num_faces * 3];
        int p, p0, p1;
        int n_cnt = 0;
        int s_cnt = 0;
        boolean even = false;

        // Every other face should be opposite normal for tristrips
        for(int i = 0; i < stripCounts.length; i++)  {
            p0 = coordIndex[s_cnt] * 3;
            p = coordIndex[s_cnt + 1] * 3;

            s_cnt += 2;

            for(int j = 2; j < stripCounts[i]; j++) {
                p1 = coordIndex[s_cnt++] * 3;
                if (!even) {
                    createFaceNormal(p, p1, p0, face_normals, n_cnt);
                    even = true;
                } else {
                    createFaceNormal(p, p0, p1, face_normals, n_cnt);
                    even = false;
                }

                n_cnt += 3;
                p0 = p;
                p = p1;
            }
        }

        // TODO:
        // Because of the broken nature of the end cap triangulation, they
        // will be ignored and will not contribute to the final normal
        // calculation. However, note that the normal for the end caps will
        // be the normalised direction vector of the spine segment for the
        // respective end

        // This code is buggy and will need to be fixed for general trip strip detection
/*
        // Work out which faces share a given coordinate index.
        int[] face_counts = new int[coords.length / 3];
        s_cnt = 0;

        for(int i = 0; i < stripCounts.length - 2; i++)  {
            face_counts[coordIndex[s_cnt++]]++;

            for(int j = 1; j < stripCounts[i] - 1; j++)
                face_counts[coordIndex[s_cnt++]] += 2;

            face_counts[coordIndex[s_cnt++]]++;
        }

        int[][] vtx_to_face = new int[coords.length / 3][];
        for(int i = 0; i < face_counts.length; i++) {
            vtx_to_face[i] = new int[face_counts[i]];
        }
        // Finally, build the vertexToFace list. Use a temporary list to
        // keep track of where we are in the list as we're setting the face
        // numbers. First clear the temp array.
        for(int i = 0; i < face_counts.length; i++)
            face_counts[i] = 0;

        int current_face = 0;
        s_cnt = 0;


        for(int i = 0; i < stripCounts.length - 2; i++)  {
            int idx = coordIndex[s_cnt++];
            int pos = face_counts[idx];

            vtx_to_face[idx][pos] = current_face;
            face_counts[idx]++;

            for(int j = 1; j < stripCounts[i] - 1; j++) {
                idx = coordIndex[s_cnt++];
                pos = face_counts[idx];

                vtx_to_face[idx][pos] = current_face;

                vtx_to_face[idx][pos + 1] = current_face + 1;
                face_counts[idx] += 2;

                //current_face++;
            }

            idx = coordIndex[s_cnt++];
            pos = face_counts[idx];

            vtx_to_face[idx][pos] = current_face;
            face_counts[idx]++;
            current_face++;
        }
*/
/*
        // This face to normal code assumes 2 triangles per face, its not general
        int side_verts = 0;
        for(int i = 0; i < stripCounts.length - 2; i++)  {
            side_verts += stripCounts[i];
        }

        int[] face_counts = new int[coords.length / 3];
        for(int i=0; i < side_verts; i++) {
            face_counts[coordIndex[i]]++;
        }

        int[][] vtx_to_face = new int[coords.length / 3][];
        for(int i = 0; i < face_counts.length; i++) {
            vtx_to_face[i] = new int[face_counts[i]];
            face_counts[i] = 0;
        }

        int idx;
        int face = 0;
        for(int i=0; i < side_verts; i++) {
            idx = coordIndex[i];
            vtx_to_face[idx][face_counts[idx]] = face;
            face_counts[idx]++;
            if ((i+1) % 2 == 0) {
                face++;
            }
        }

        // Finally generate the normal at each vertex by averaging up the
        // list of normals from the faces that contribute to this vertex. Note
        // that we are ignoring the effects of a creaseAngle at this point.
        float[] final_normals = new float[coords.length];
        float face_x, face_y, face_z;
        float norm_x, norm_y, norm_z;

        for(int i = 0; i < vtx_to_face.length; i++) {
            if(vtx_to_face[i].length == 0)
                continue;

            face_x = face_normals[vtx_to_face[i][0] * 3];
            face_y = face_normals[vtx_to_face[i][0] * 3 + 1];
            face_z = face_normals[vtx_to_face[i][0] * 3 + 2];

            for(int j = 1; j < vtx_to_face[i].length; j++) {
                face_x += face_normals[vtx_to_face[i][j] * 3];
                face_y += face_normals[vtx_to_face[i][j] * 3 + 1];
                face_z += face_normals[vtx_to_face[i][j] * 3 + 2];
            }
            final_normals[i * 3] = face_x / vtx_to_face[i].length;
            final_normals[i * 3 + 1] = face_y / vtx_to_face[i].length;
            final_normals[i * 3 + 2] = face_z / vtx_to_face[i].length;
        }
        // this method should only be called by the buildImpl() method,
        // which declares implGeom as an IndexedTriangleStripArray.
        // Perhaps better type-safety-check is in needed
        ((IndexedTriangleStripArray)implGeom).setNormals(final_normals);
   }

    // Generate texture coordinates for the geometry.
    private void generateTexCoords() {
    }
    /**
     * Convenience method to create a normal for the given vertex coordinates
     * and normal array. This performs a cross product of the two vectors
     * described by the middle and two end points.
     *
     * @param p The index of the middle point
     * @param p1 The index of the first point
     * @param p2 The index of the second point
     * @param normals The array to leave the computed result in
     * @param offset The offset into the normal array to place the normal values
     *
    private void createFaceNormal(int p,
                                  int p1,
                                  int p2,
                                  float[] normals,
                                  int offset)
    {
        float x1 = coords[p1]     - coords[p];
        float y1 = coords[p1 + 1] - coords[p + 1];
        float z1 = coords[p1 + 2] - coords[p + 2];

        float x2 = coords[p]     - coords[p2];
        float y2 = coords[p + 1] - coords[p2 + 1];
        float z2 = coords[p + 2] - coords[p2 + 2];

        // TODO: cross backwards for setup, perhaps change calling routines?
        float cross_x = y2 * z1 - z2 * y1;
        float cross_y = z2 * x1 - x2 * z1;
        float cross_z = x2 * y1 - y2 * x1;

        float mag = cross_x * cross_x + cross_y * cross_y + cross_z * cross_z;

        if(mag != 0) {
            mag = 1 / (float)Math.sqrt(mag);
            normals[offset] = cross_x * mag;
            normals[offset + 1] = cross_y * mag;
            normals[offset + 2] = cross_z * mag;
        } else {
            normals[offset] = 0;
            normals[offset + 1] = 1;
            normals[offset + 2] = 0;
        }
    }*/
}
