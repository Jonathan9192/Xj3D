#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
# 
# Package makefile for the org.web3d.vrml.renderer.common.input.movie
# directory
#
# Author: Justin Couch
# Version: $Revision: 1.6 $
#
#*********************************************************************
#           JMF is required for this package
# You must have the environment variable JMFHOME
# defined to point to the installation path like this:
#JMFHOME=D:/Program Files/JMF2.1.1
# If JMFHOME is not installed, this package will not be built.
#*********************************************************************
PACKAGE = org.web3d.vrml.renderer.common.input.movie

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = \
    VideoStreamHandler.java \
    Renderer.java \
    VideoRenderer.java \
    AudioRenderer.java \
    MovieDecoder.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA = 

# If the package requires extra classes outside the JDK, the JAR files that 
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY=j3d-org-all_1.1.0.jar jmf.jar

include ${PROJECT_ROOT}/make/Makefile.jav
