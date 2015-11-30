#!/bin/sh
sh xvfb.sh start
export DISPLAY=:99
java -jar ./../.tarpit/extractIng.jar
sh xvfb.sh stop
