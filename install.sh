#!/usr/bin/env bash

# This script is ONLY for Ubuntu 16.04 LTS!

JENKINS_PORT=8081
GITLAB_PORT=8082

GITLAB_IMAGE="gitlab/gitlab-ce:10.6.1-ce.0"
JAVA_ENV_IMAGE="moekr/java8-maven3:1.0.0"
PYTHON_ENV_IMAGE="moekr/python3-nose:1.0.0"

GITLAB_CONTAINER="gitlab-aes"
GITLAB_VOLUME="/srv/aes/gitlab"

check_privilege () {
    sudo -A ls >/dev/null 2>&1
    if [ "$?" -ne "0" ]; then
        echo "Please run this script as root or use sudo!"
        exit 1
    fi
}

exec_command () {
    echo "$1"
    bash -c "$2"
    if [ "$?" -ne "0" ]; then
        echo -e "\033[31m$1fail, exit.\033[0m"
        exit 1
    else
        echo -e "\033[32m$1done.\033[0m"
    fi
}

check_privilege
echo -e "\033[36mAutomated Examination System components install script\033[0m"
echo -e "\033[41;37mThis script is ONLY for Ubuntu 16.04 LTS! \033[0m"
echo -e -n "\033[36mPress any key to start install or exit with CTRL+C... \033[0m"
read -n1 -s
echo

# Prepare
exec_command "Update package repository... " \
    "apt update"
exec_command "Install essential dependencies... " \
    "apt install -y gnupg curl apt-transport-https ca-certificates software-properties-common unzip git default-jdk default-jre maven python3-nose"

# Install Docker
exec_command "Uninstall old version Docker... " \
    "apt remove docker docker-engine docker.io"
exec_command "Install Docker repository GPG key... " \
    "curl https://download.docker.com/linux/ubuntu/gpg 2>/dev/null | apt-key add -"
exec_command "Install Docker repository source... " \
    "echo \"deb [arch=amd64] https://mirrors.tuna.tsinghua.edu.cn/docker-ce/linux/ubuntu xenial stable\" >/etc/apt/sources.list.d/docker.list"
exec_command "Update package repository... " \
    "apt update"
exec_command "Install Docker... " \
    "apt install -y docker-ce"

# Install Jenkins
exec_command "Install Jenkins repository GPG key... " \
    "curl https://pkg.jenkins.io/debian/jenkins.io.key 2>/dev/null | apt-key add -"
exec_command "Install Jenkins repository source... " \
    "echo \"deb https://pkg.jenkins.io/debian binary/\" >/etc/apt/sources.list.d/jenkins.list"
exec_command "Update package repository... " \
    "apt update"
exec_command "Install Jenkins... " \
    "apt install -y jenkins"
exec_command "Change Jenkins default setting... " \
    "sed -i \"s/HTTP_PORT=8080/HTTP_PORT=${JENKINS_PORT}/g\" /etc/default/jenkins"
exec_command "Add jenkins user to docker group... " \
    "usermod -a -G docker jenkins"
exec_command "Restart Jenkins... " \
    "service jenkins restart"

# Pull image
exec_command "Pull GitLab-CE... " \
    "docker pull ${GITLAB_IMAGE}"
exec_command "Pull Java environment... " \
    "docker pull ${JAVA_ENV_IMAGE}"
exec_command "Pull Python environment... " \
    "docker pull ${PYTHON_ENV_IMAGE}"

# Run GitLab
exec_command "Run GitLab-CE... " \
    "docker run -d --name ${GITLAB_CONTAINER} -p ${GITLAB_PORT}:80 --restart always \
    -v ${GITLAB_VOLUME}/config:/etc/gitlab:Z -v ${GITLAB_VOLUME}/logs:/var/log/gitlab:Z -v ${GITLAB_VOLUME}/data:/var/opt/gitlab:Z \
    ${GITLAB_IMAGE}"

echo -e "\033[36mInstall all components successfully.\033[0m"
echo -e "\033[36mJenkins is running on port ${JENKINS_PORT}.\033[0m"
echo -e "\033[36mGitlab is running on port ${GITLAB_PORT}.\033[0m"
