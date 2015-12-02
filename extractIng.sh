#!/bin/sh
sh xvfb.sh start
export DISPLAY=:99
java -jar ~/.tarpit/extractIng.jar
line=$(head -n 1 ./tweet_sysout.txt)
tweet line
sh xvfb.sh stop
