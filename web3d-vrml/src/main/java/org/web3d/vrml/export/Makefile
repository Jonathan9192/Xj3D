#*********************************************************************
#
#  (C) 2001 Web3d Consortium
#    http://www.web3d.org/
#
# Package makefile for the org.web3d.vrml.sav directory
#
# Author: Alan Hudson
# Version: $Revision: 1.16 $
#
#*********************************************************************

PACKAGE = org.web3d.vrml.export

# The source files to compile. These are declared in order that they must be
# compiled in
SOURCE = Exporter.java \
    ExporterFactory.java \
    StatisticsCollector.java \
    SAXPrinter.java \
    PlainTextErrorReporter.java \
    BaseRetainedExporter.java \
    NullExporter.java \
    X3DClassicRetainedExporter.java \
    X3DRetainedSAXExporter.java \
    X3DXMLRetainedExporter.java \
    X3DClassicExporter.java \
    X3DXMLExporter.java \
    X3DBinarySerializer.java \
    VrmlExporter.java \
    X3DBinaryRetainedDirectExporter.java

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

# If the package requires extra classes outside the JDK, the JAR files that
# are required are listed here. These should be found in ${PROJECT_ROOT}/lib
JARS_3RDPARTY = fastinfoset_1.2.9.jar uri.jar j3d-org-all_2.0.0.jar


include ${PROJECT_ROOT}/make/Makefile.jav
