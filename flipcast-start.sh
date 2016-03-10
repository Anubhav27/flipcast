#! /bin/bash
# Start script for flipcast service
sleep 10

#Export all config variables
export MONGO_CONNECTION_STRING=$1
export DATABASE_NAME=$2
export RMQ_CONNECTION_STRING=$3
export RMQ_USER=$4
export RMQ_PASSWORD=$5

#Application Name
PACKAGE=flipcast

#Application Jar
APP_JAR="flipcast.jar"

#Memory settings
JAVA_OPTS="${JAVA_OPTS} -Xms512m"
JAVA_OPTS="${JAVA_OPTS} -Xmx512m"
JAVA_OPTS="${JAVA_OPTS} -XX:NewSize=256m"
JAVA_OPTS="${JAVA_OPTS} -XX:MaxNewSize=256m"

# Set Server JVM
JAVA_OPTS="${JAVA_OPTS} -server"

#Enable NUMA (For modern processors) - Reduces thread contention
JAVA_OPTS="${JAVA_OPTS} -XX:+UseNUMA"

# Set to headless, just in case
JAVA_OPTS="${JAVA_OPTS} -Djava.awt.headless=true"

#Set encoding to UTF-8
JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=UTF-8"

# Reduce the per-thread stack size
JAVA_OPTS="${JAVA_OPTS} -Xss228k"

# Force the JVM to use IPv4 stack
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"

# GC Options
JAVA_OPTS="${JAVA_OPTS} -XX:+UseCompressedOops"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseG1GC"
JAVA_OPTS="${JAVA_OPTS} -XX:+DisableExplicitGC"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseTLAB"

#Memory Setting
JAVA_OPTS="${JAVA_OPTS} -XX:MetaspaceSize=512m"
JAVA_OPTS="${JAVA_OPTS} -XX:MaxMetaspaceSize=512m"

#Enable super options (For HotSpot optimizations)
JAVA_OPTS="${JAVA_OPTS} -XX:+AggressiveOpts"

# General Tuning Settings
JAVA_OPTS="${JAVA_OPTS} -XX:CompileThreshold=50000"

# Causes the JVM to dump its heap on OutOfMemory.
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError"

#Setup remote JMX monitoring
JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote"
JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.port=29005"
JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.ssl=false"

#Provide application configuration & log configuration file path
JAVA_OPTS="${JAVA_OPTS} -Dapp.config=application.conf"
JAVA_OPTS="${JAVA_OPTS} -Dlogback.configurationFile=logback.xml"

#Java Executable
JAVA=`which java`

#Execute main application JAR
EXEC_CMD="exec -a ${PACKAGE} ${JAVA} -jar ${JAVA_OPTS} ${APP_JAR}"
eval ${EXEC_CMD}