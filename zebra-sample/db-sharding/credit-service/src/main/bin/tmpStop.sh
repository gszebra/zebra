#!/bin/bash
PROC_NAME=${app}
PROCESS=`ps -ef|grep $PROC_NAME|grep -v grep|grep -v PPID|awk '{ print $2}'`
for i in $PROCESS
do
  kill -15 $i
  sleep 1
  ps -ef|grep $PROC_NAME |grep -v grep
  if [ $? -ne 0 ]
  then
	echo "Kill the $PROC_NAME process [ $i ] success"
  else
  	kill -9 $i
	echo "Kill the $PROC_NAME process [ $i ] success"
  fi
done
