#!/bin/sh
logger_cmd="logger -p local6.debug -t $0[$$]"
$logger_cmd "$(whoami)"
$logger_cmd "$(stat -c '%n : %A : %U : %z : %s' /mnt/diablo/kitchen/source_code/.tarpit/extractIng.jar)"
$logger_cmd "$(sh xvfb.sh start 97)"
sleep 5
export DISPLAY=:97
if [ -f ./tweet_sysout.txt ]; then
	rm ./tweet_sysout.txt
fi
$logger_cmd "start java"
$logger_cmd "$((java -jar /mnt/diablo/kitchen/source_code/.tarpit/extractIng.jar) 2>&1)"
$logger_cmd "stop java"
sleep 5
if [ -f ./tweet_sysout.txt ]; then
	line=$(head -n 1 ./tweet_sysout.txt)
	tweet $0 $line
	$logger_cmd "$line"
	rm ./tweet_sysout.txt
fi
$logger_cmd "$(sh xvfb.sh stop 97)"
