#!/bin/bash
PROC_NAME=zebra-conf
PROCESS=`ps -ef|grep $PROC_NAME|grep -v grep|grep -v PPID|awk '{ print $2}'`
for i in $PROCESS
do
  echo "Check the $PROC_NAME process [ $i ] success"
  exit 0
done
exit 1
