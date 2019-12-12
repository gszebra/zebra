#!/bin/bash
PROC_NAME=zebra-monitor-console
PROCESS=`ps -ef|grep $PROC_NAME|grep -v grep|grep -v PPID|awk '{ print $2}'`
for i in $PROCESS
do
  echo "Kill the $PROC_NAME process [ $i ]"
  kill -9 $i
done
