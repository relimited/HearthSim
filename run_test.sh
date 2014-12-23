#!/bin/sh

#Runs several variations on AIs for hearthsim.  yay.
#this .sh script just runs various java tasks in the background to get'er done

java -jar build/libs/HearthSim-all.jar ./examples/zoo/zooAggroAggroParams.hsparam &
java -jar build/libs/HearthSim-all.jar ./examples/zoo/zooAggroControlParams.hsparam &
java -jar build/libs/HearthSim-all.jar ./examples/zoo/zooAggroTempoParams.hsparam &
java -jar build/libs/HearthSim-all.jar ./examples/zoo/zooControlControlParams.hsparam &
java -jar build/libs/HearthSim-all.jar ./examples/zoo/zooTempoControlParams.hsparam &
java -jar build/libs/HearthSim-all.jar ./examples/zoo/zooTempoTempoParams.hsparam &

