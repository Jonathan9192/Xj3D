#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.vrml.renderer.common.nodes.particle
# directory
#
# Author: Justin Couch
# Version: $Revision: 2.2 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.renderer.common.nodes.particle

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
  BaseEmitter.java \
  BaseExplosionEmitter.java \
  BasePointEmitter.java \
  BasePolylineEmitter.java \
  BasePhysicsModel.java \
  BaseGravityPhysicsModel.java \
  BaseWindPhysicsModel.java \
  BaseParticleSystem.java 

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = j3d-org-all_2.0.0.jar


include ${PROJECT_ROOT}/make/Makefile.jav
