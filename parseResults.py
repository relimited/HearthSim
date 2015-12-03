#!/bin/python

#Python script to parse simple result files from hearthsim
#Mostly reports on wins/losses

import json
import sys
import os
import re

#typical argument checking
if len(sys.argv) != 2:
	print "Incorrect number of arguments.  use: parseResults [resultFilePath]"
	exit -1

basePath = sys.argv[1]

readData = {} #set data to the null object
storeData = []


#read in and parse the file
#we can do fancy stuff later, right now, lets just get a win report
#explictly coding for two players

#convert a dir header into a comma seperated weight string
def getWeightString(dirStr):
	dirName = os.path.basename(dirStr)[5:] #take off the test bit
	valList = ["0."+weight for weight in re.split('[01]\.', dirName)] #split and recover
	return ", ".join(valList[:])
	


#Subprocedure to get a record from a test directory
def getRecord(path):
	player0Wins = 0
	player1Wins = 0
	fullPath = path + "/example1.hsres"

	with open(fullPath, 'r') as f:
		for line in f:
			readData = json.loads(line)
			#print str(data)
			if readData[u'winner'] == 0:
				player0Wins = player0Wins + 1
			elif readData[u'winner'] == 1:
				player1Wins = player1Wins + 1
			else:
				print "Couldn't get a winner for this game"
	headerString = getWeightString(path)
	return [headerString, player0Wins, player1Wins]


#MAIN PROGRAM
rootList = os.listdir(basePath)
#for strPath in rootList:
	#storeData.append(getRecord(basePath + strPath))
	#print getWeightString(basePath + strPath)
storeData.append(getRecord(basePath))
print max(storeData, key=lambda x: x[2])

