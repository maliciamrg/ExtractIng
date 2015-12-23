#!/bin/bash
whoami
XVFB=/usr/bin/Xvfb
XVFBARGS=":$2 -screen 0 1024x768x24 -fbdir /tmp -ac"
PIDFILE="/tmp/xvfb$2.pid"
case "$1" in
  start)
    echo -n "Starting virtual X frame buffer: xvfb"
    /sbin/start-stop-daemon --start --pidfile $PIDFILE --make-pidfile --background --exec $XVFB -- $XVFBARGS
    echo "."
    ;;
  stop)
    echo -n "Stopping virtual X frame buffer: xvfb"
    /sbin/start-stop-daemon --stop --pidfile $PIDFILE
    echo "."
    ;;
  restart)
    $0 stop $2
    $0 start $2
    ;;
  *)
        echo "Usage: xvfb {start|stop|restart} {99}"
        exit 1
esac
 
exit 0