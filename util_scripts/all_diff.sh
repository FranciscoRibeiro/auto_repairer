#!/bin/bash

DIR="$1"
FILES=($(ls "$DIR"))

for f in ${!FILES[@]}
do
	next_idx=$((f+1))
	for ((f1=$next_idx;f1<${#FILES[@]};f1++))
	do
		diff $DIR/${FILES[$f]} $DIR/${FILES[$f1]} &> /dev/null && echo "$DIR/${FILES[$f]} and $DIR/${FILES[$f1]} are identical"
	done
done

