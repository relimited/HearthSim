
#JAVA + init configuration setup here

#Basic script procedure
#	1) make new run directory for tests
#	2) move required files to run directory
#	3) edit AI file
#	4) run test


fileSourceDir="examples/example1"
baseTestDir="tests"
jarDir="build/libs/HearthSim.jar"
cwd=$(pwd)

#AI weights are a 10 dimensional vector:
aiWeights=(
w_a=0.25
w_h=0.25
wt_a=0.25
wt_h=0.25

w_taunt=0.25
w_health=0.25
wt_health=0.25

w_mana=0.25
w_num_minions=0.25
wt_num_minions=0.25
)

endPattern=(
w_a=0.75
w_h=0.75
wt_a=0.75
wt_h=0.75

w_taunt=0.75
w_health=0.75
wt_health=0.75

w_mana=0.75
w_num_minions=0.75
wt_num_minions=0.75
)

#amount to change each weight vector for each iteration
updateAmt=0.25

######################## START SUBPROCEDURES ############################
#add the increment value to the AI weight vector
function add () {
	added="false"
	#find the rollover value
	rolloverAmt=0$(bc <<< "scale=2;1.0-$updateAmt")
	for i in `seq 1 ${#aiWeights[*]}`;
	do
		##bash index fixing
		i=$i-1

		if [ "$added" == "false" ]; then
		
			##So, this isn't _great_ but, I think it can work maybe?
			## we need to do a direct string equality match because bash is DUMB
			##USE PYTHON FOR SYSTEM SCRIPTS.  JUST DO IT.  GODDAMN
			entry=${aiWeights[$i]}
			##break down into name / value
			name=`expr "$entry" : '\([a-zA-Z_]*\)'`
			val=`expr "$entry" : '.*\([0-9]\.[0-9]\)'`
		
			## do a rollover op, don't add (someone down the future will handle the addition
			if [ "$val" == "0.75" ]; then
				val="0.25"
				aiWeights[$i]=$name"="$val
			else
				##trust me, it'll be less than one.  TRUST ME, BRO
				##if we haven't added it in yet, add it in
				eqn=$val"+"$updateAmt
				
				#we need two seperate add ops, because sometimes fuck you, bash (not right now)
				if [ "$val" == $rolloverAmt ]; then
					val=0$(bc <<< "scale=2;$eqn")
				else
					val=0$(bc <<< "scale=2;$eqn")
				fi
				aiWeights[$i]=$name"="$val
				added="true"
			fi
		fi 
	done
}

#echo the AI weight vector
function print(){	
	ref="aiWeights[*]"
	echo ${!ref}	
}

#write some new AI file
function runTest(){
	ref="aiWeights[*]"
	local ${!ref}
	dirPath=$cwd"/"$baseTestDir"/test-"$w_a$w_h$wt_a$wt_h$w_taunt$w_health$wt_health$w_mana$w_num_minions$wt_num_minions
	mkdir -p $dirPath
	cp -r $cwd"/"$fileSourceDir"/." $dirPath
	rm $dirPath"/ai1.hsai"

	aiFileStr="w_a = "$w_a"\nw_h = "$w_h"\nwt_a = "$wt_a"\nwt_h = "$wt_h"\n\nw_taunt = "$w_taunt"\nw_health = "$w_health"\nwt_health = "$wt_health"\n\nw_mana = "$w_mana"\nw_num_minions = "$w_num_minions"\nwt_num_minions = "$wt_num_minions"\n\nnumMCTSIterations = 5\nnumChildrenPerGeneration = 20\nnumSimulateTurns = 1"

	echo -e "$aiFileStr" >> "$dirPath/ai1.hsai"
	#cat ./gradle.properties > "$dirPath/gradle.properties.bak"
	#rm ./gradle.properties
	#echo -e "hsparam=$dirPath/masterParams.hsparam\norg.gradle.daemon=true\norg.gradle.parallel=true" > ./gradle.properties
	#./gradlew runSim
	java -jar $jarDir "$dirPath/masterParams.hsparam" &

}

################################# END SUBPROCEDURES ###############################

### KINDA BROKEN WAY TO DO THIS ####
##deref the weight vector and final pattern vector
ref="aiWeights[*]"
end="endPattern[*]"

mkdir -p "./$baseTestDir"

#CHECK TO MAKE SURE THIS INVARIANT WORKS
while [ "${!ref}" != "${!end}" ]; do
	runSim
	add
	numGames=$(($numGames+1))
	if [ $numGames -gt 1024 ]; then
		exit -1
	fi
done

#some quick testing before we fire this off on a server
#The loop to end all loops
# yeah, this is gonna get gross

#testDir="tests/test-"$testNumber 
#echo "Test Dir: "
#echo "./"$testDir

#echo "source file regex: "
#echo "./"$fileSourceDir"/*"

#mkdir -p "./"$testDir

#

#### work in progress below this line ####


#edit the masterParams file
#cat ./gradle.properties | sed -e '1s#.*#hsparam=$testDir/masterParams.hsparam#' > ./gradle.properties

#edit the AI file
#some sed call here

#run the tests
#./gradlew runSim

#LOOP


