#!/bin/bash
LD_LIBRARY_PATH=
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$(dirname "$PWD")/libso
WORKDIR=$(dirname "$PWD")
JARFILE=$WORKDIR/zebra-monitor-console.jar
CONF=$WORKDIR/config

#if [ $# -lt 2  ]
#then
#  echo argument is missing
#  exit
#fi          

cd $WORKDIR

for file in `ls $WORKDIR/lib/*.jar` ;
do
	   CLASSPATH="$CLASSPATH":"$file"
done
     
CLASSPATH="$CLASSPATH":"$JARFILE"
CLASSPATH="$CLASSPATH":"$CONF"
getConfig(){
  if [ -s $WORKDIR/config/start.ini ];then
    start_cmd=""
    for line in `sed s/[[:space:]]//g $WORKDIR/config/start.ini`
    do
      start_cmd=${start_cmd}${line}"&"
    done
    start_cmd=${start_cmd%&}
  else
    start_cmd=""
  fi
}

getConfig
echo $start_cmd

java -Xmx4G -Xms4G -Xmn2G -Xss1m -XX:+UseParallelGC -XX:+UseParallelOldGC -Dapplication=zebra-monitor-console -classpath $CLASSPATH com.guosen.App $start_cmd

echo "done!!!"
