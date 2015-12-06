#!/bin/sh
whoami
stat -c '%n : %A : %U : %z : %s' /mnt/diablo/kitchen/source_code/.tarpit/extractIng.jar
sh xvfb.sh start
export DISPLAY=:99
if [ -f ./tweet_sysout.txt ]; then
	rm ./tweet_sysout.txt
fi
java -jar /mnt/diablo/kitchen/source_code/.tarpit/extractIng.jar
sleep 5
if [ -f ./tweet_sysout.txt ]; then
	line=$(head -n 1 ./tweet_sysout.txt)
	tweet $line
	rm ./tweet_sysout.txt
fi
sh xvfb.sh stop
