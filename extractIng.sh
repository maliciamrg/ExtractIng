#!/bin/sh
logger_cmd="logger -p local6.debug -t $0[$$]"
$logger_cmd "$(whoami)"
$logger_cmd "$(stat -c '%n : %A : %U : %z : %s' /mnt/diablo/kitchen/source_code/.tarpit/extractIng.jar)"
sh xvfb.sh start
export DISPLAY=:99
if [ -f ./tweet_sysout.txt ]; then
	rm ./tweet_sysout.txt
fi
$logger_cmd "start java"
java -jar /mnt/diablo/kitchen/source_code/.tarpit/extractIng.jar
$logger_cmd "stop java"
sleep 5
if [ -f ./tweet_sysout.txt ]; then
	line=$(head -n 1 ./tweet_sysout.txt)
	tweet $line
	$logger_cmd "$line"
	rm ./tweet_sysout.txt
fi
sh xvfb.sh stop
