#!/bin/python

#Python script to parse simple result files from hearthsim
#Mostly reports on wins/losses

import json
import sys

#typical argument checking
if len(sys.argv) != 2:
	print "Incorrect number of arguments.  use: parseResults [resultFilePath]"
	exit -1

path = sys.argv[1]

data = {} #set data to the null object

#read in and parse the file
#we can do fancy stuff later, right now, lets just get a win report
#explictly coding for two players
player0Wins = 0
player1Wins = 0
with open(path, 'r') as f:
	for line in f:
		data = json.loads(line)
		#print str(data)
		if data[u'winner'] == 0:
			player0Wins = player0Wins + 1
		elif data[u'winner'] == 1:
			player1Wins = player1Wins + 1
		else:
			print "Couldn't get a winner for this game"

print "Player 0 Wins: " + str(player0Wins) 
print "Player 1 Wins: " + str(player1Wins)

