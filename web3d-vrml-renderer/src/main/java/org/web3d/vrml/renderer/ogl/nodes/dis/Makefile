#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.renderer.j3d.nodes.render
# directory
#
# Author: Alan Hudson
# Version: $Revision: 1.10 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.ogl.nodes.dis

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  OGLEspduTransform.java \
  OGLDISEntityManager.java \
  OGLDISEntityTypeMapping.java \


# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY= jaxb-api.jar dis-enums.jar hibernate-jpa-2.0-api-1.0.0.Final.jar \
  open-dis_4.06.jar aviatrix3d-all_2.2.0.jar j3d-org-all_2.0.0.jar vecmath.jar

include ${PROJECT_ROOT}/make/Makefile.jav
