#!/bin/sh
if [ -z "$INST_HOME" ]; then
  INST_HOME=$JAVA_HOME
fi
if [ -z "$JAVA_HOME" ]; then
  echo "Error: Please set \$JAVA_HOME"
else
  echo "Ensuring instrumented JREs exist for tests... to refresh, do mvn clean\n"
  #	if [ ! -d "target/jre-inst-obj" ]; then
  #			echo "Creating obj tag instrumented JRE\n";
  #		java -Xmx6g -Dphosphor.verify=true -jar target/Phosphor-0.0.5-SNAPSHOT.jar -serialization -forceUnboxAcmpEq -withEnumsByValue $INST_HOME target/jre-inst-obj;
  #	else
  #		echo "Not regenerating obj tag instrumented JRE\n";
  #	fi
  #	#if [ ! -d "target/jre-inst-obj-alen" ]; then
  #	#		echo "Creating obj-alen tag instrumented JRE\n";
  #	#	java -Xmx6g -jar target/Phosphor-0.0.3-SNAPSHOT.jar -serialization -forceUnboxAcmpEq -withEnumsByValue -withArrayLengthTags -withArrayIndexTags $INST_HOME target/jre-inst-obj-alen;
  #	#	chmod +x target/jre-inst-obj-alen/bin/*;
  #	#	chmod +x target/jre-inst-obj-alen/lib/*;
  #	#	chmod +x target/jre-inst-obj-alen/jre/bin/*;
  #	#	chmod +x target/jre-inst-obj-alen/jre/lib/*;
  #	#else
  #	#	echo "Not regenerating obj-alen tag instrumented JRE\n";
  #	#fi
  #	if [ ! -d "target/jre-inst-implicit" ]; then
  #		echo "Creating obj tag + implicit flow instrumented JRE\n";
  #		java -Xmx6g -Dphosphor.verify=true -jar target/Phosphor-0.0.5-SNAPSHOT.jar -controlTrack -forceUnboxAcmpEq -withEnumsByValue $INST_HOME target/jre-inst-implicit;
  #	else
  #		echo "Not regenerating implicit flow instrumented JRE\n";
  #	fi
  #	if [ ! -d "target/jre-inst-binding-control" ]; then
  #		echo "Creating obj tag + binding control flow instrumented JRE\n";
  #		java -Xmx6g -Dphosphor.verify=true -jar target/Phosphor-0.0.5-SNAPSHOT.jar -bindingControlTracking -forceUnboxAcmpEq -withEnumsByValue $INST_HOME target/jre-inst-binding-control;
  #	else
  #		echo "Not regenerating binding control flow instrumented JRE\n";
  #	fi
  if [ ! -d "target/jre-inst-implicit-optimized" ]; then
    echo "Creating obj tag + optimized instrumented JRE\n"
    java -Xms16G -Xmx16G -Dphosphor.verify=true -jar target/Phosphor-0.0.5-SNAPSHOT.jar -implicitHeadersNoTracking -withoutBranchNotTaken -serialization -forceUnboxAcmpEq -withEnumsByValue $INST_HOME target/jre-inst-implicit-optimized
  else
    echo "Not regenerating optimized implicit flow instrumented JRE\n"
  fi
fi
