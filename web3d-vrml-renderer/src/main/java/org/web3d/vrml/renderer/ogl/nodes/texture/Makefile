#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.ogl.nodes.texture
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.20 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.ogl.nodes.texture

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = OGLTextureCoordinate.java \
  OGLTextureCoordinateGenerator.java \
  OGLTextureTransform.java \
  OGLImageTexture.java \
  OGLPixelTexture.java \
  OGLTextureProperties.java \
  OGLMultiTexture.java \
  OGLMultiTextureTransform.java \
  OGLMultiTextureCoordinate.java \
  OGLTextureCoordinate3D.java \
  OGLTextureCoordinate4D.java \
  OGLTextureTransform3D.java \
  OGLComposedTexture3D.java \
  OGLRenderedTexture.java \
  OGLMovieTexture.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_1.1.0.jar aviatrix3d-all_2.2.0.jar vecmath.jar jogl.jar

include ${PROJECT_ROOT}/make/Makefile.jav
