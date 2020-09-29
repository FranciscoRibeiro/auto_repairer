#!/bin/bash

DIR1="$1"
DIR2="$2"
FILES1=($(ls "$DIR1"))
FILES2=($(ls "$DIR2"))

all_exist=true
for f in ${FILES1[@]}
do
	exists=false
	for f1 in ${FILES2[@]}
	do
		((counter+=1))
		diff $DIR1/$f $DIR2/$f1 &> /dev/null
		if [[ "$?" -eq 0 ]]
		then
			exists=true
			break
		fi
	done
	if [[ "$exists" == "false" ]]
	then
		all_exist=false
		echo "$f does not exist in $DIR2"
	fi
done

[[ "$all_exist" == true ]] && echo "all files in $DIR1 exist in $DIR2"

