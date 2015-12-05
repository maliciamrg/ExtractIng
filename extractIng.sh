#!/bin/sh
sh xvfb.sh start
export DISPLAY=:99
java -jar /mnt/diablo/kitchen/source_code/.tarpit/extractIng.jar
sleep 5
if [ -f ./tweet_sysout.txt ]; then
	line=$(head -n 1 ./tweet_sysout.txt)
	tweet $line
fi
sh xvfb.sh stop
