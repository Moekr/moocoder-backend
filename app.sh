#!/usr/bin/env bash

# Application root directory.
path=/srv/aes
# Application username.
name=automated-examination-system
# Application version.
version=0.2.4

# Application config file. Default config.yml
config_file=${path}/config.yml
# File which store application process PID. Default proc.pid
pid_file=${path}/proc.pid
# Directory which store application log. Default ${path}/log
log_dir=${path}/log
# Name template of log files (Parsed with shell command "date"). Default %Y-%m-%d_%H-%M-%S.log
log_name=%Y-%m-%d_%H-%M-%S.log
# Time in second how long "stop" and "force-stop" wait for application totally stop. Default 60
wait_count=60

# JVM default time zone. Default GMT+8
time_zone="GMT+8"

function start {
    if [ -f ${pid_file} ]
    then
        local pid=$(cat ${pid_file})
        if ps -p ${pid} > /dev/null
        then
            echo "Application ${name} is already running! (Version:${version} PID:${pid})"
        else
            rm -rf ${pid_file}
        fi
    fi
    if [ ! -f ${pid_file} ]
    then
        if [ ! -d ${log_dir} ]
        then
            mkdir ${log_dir}
        fi
        java -server -jar ${path}/${name}-${version}.jar --spring.config.additional-location=${config_file} -Duser.timezone=${time_zone} 2>&1 >${log_dir}/$(date +${log_name}) &
        local pid=$(echo -e "$!\c")
        echo -e "${pid}\c" > ${pid_file}
        echo "Start application ${name} successfully! (Version:${version} PID:${pid})"
    fi
}

function stop {
    if [ -f ${pid_file} ]
    then
        if ! ps -p $(cat ${pid_file}) > /dev/null
        then
            rm -rf ${pid_file}
        fi
    fi
    if [ -f ${pid_file} ]
    then
        local pid=$(cat ${pid_file})
        kill ${pid}
        local count=${wait_count}
        while((${count} > 0))
        do
            sleep 1
            if ! ps -p ${pid} > /dev/null
            then
                rm -rf ${pid_file}
                echo "Stop application ${name} successfully! (Version:${version})"
                break
            fi
            count=`expr ${count} - 1`
        done
        if [ ${count} -eq 0 ]
        then
            echo "Failed to stop application ${name}! (Version:${version} PID:${pid})"
            exit 1
        fi
    else
        echo "Application ${name} is not running! (Version:${version})"
    fi
}

function restart {
    stop
    start
}

function status {
    if [ -f ${pid_file} ]
    then
        if ! ps -p $(cat ${pid_file}) > /dev/null
        then
            rm -rf ${pid_file}
        fi
    fi
    if [ -f ${pid_file} ]
    then
        local pid=$(cat ${pid_file})
        echo "Application ${name} is running! (Version:${version} PID:${pid})"
    else
        echo "Application ${name} is not running! (Version:${version})"
    fi
}

function usage {
    echo "Usage: $0 {start|stop|restart|status}"
}

pushd ${path} > /dev/null

case $1 in
start)
    start
;;
stop)
    stop
;;
restart)
    restart
;;
status)
    status
;;
*)
    usage
;;
esac

popd > /dev/null

exit 0