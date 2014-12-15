#!/bin/sh

#Runs several variations on AIs for hearthsim.  yay.
#this .sh script just runs various java tasks in the background to get'er done

java -jar cmdRunJar.jar ./examples/zoo/zooAggroAggroParams.hsparam &
java -jar cmdRunJar.jar ./examples/zoo/zooAggroControlParams.hsparam &
java -jar cmdRunJar.jar ./examples/zoo/zooAggroTempoParams.hsparam &
java -jar cmdRunJar.jar ./examples/zoo/zooControlControlParams.hsparam &
java -jar cmdRunJar.jar ./examples/zoo/zooTempoControlParams.hsparam &
java -jar cmdRunJar.jar ./examples/zoo/zooTempoTempoParams.hsparam &

