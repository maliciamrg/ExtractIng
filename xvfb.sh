#!/bin/bash
 
XVFB=/usr/bin/Xvfb
XVFBARGS=":99 -screen 0 1024x768x24 -fbdir /tmp -ac"
PIDFILE=/tmp/xvfb.pid
case "$1" in
  start)
    echo -n "Starting virtual X frame buffer: xvfb"
    start-stop-daemon --start --pidfile $PIDFILE --make-pidfile --background --exec $XVFB -- $XVFBARGS
    echo "."
    ;;
  stop)
    echo -n "Stopping virtual X frame buffer: xvfb"
    start-stop-daemon --stop --pidfile $PIDFILE
    echo "."
    ;;
  restart)
    $0 stop
    $0 start
    ;;
  *)
        echo "Usage: xvfb {start|stop|restart}"
        exit 1
esac
 
exit 0