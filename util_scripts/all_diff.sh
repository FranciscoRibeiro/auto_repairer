#!/bin/bash

DIR="$1"
FILES=($(find "$DIR" -name "*java"))

for f in ${!FILES[@]}
do
	next_idx=$((f+1))
	for ((f1=$next_idx;f1<${#FILES[@]};f1++))
	do
		diff ${FILES[$f]} ${FILES[$f1]} &> /dev/null && echo "${FILES[$f]} and ${FILES[$f1]} are identical"
	done
done

