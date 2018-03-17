#!/usr/bin/env bash

# This script is ONLY for Ubuntu 16.04 LTS WITHOUT docker!

check_result () {
    if [ "$?" -ne "0" ]; then
        echo -e "\033[31m$1fail, exit.\033[0m"
        exit 1
    else
        echo -e "\033[32m$1done.\033[0m"
    fi
}

echo -e "\033[36mAutomated Examination System components install script\033[0m"
echo -e "\033[41;37mThis script is ONLY for Ubuntu 16.04 LTS WITHOUT docker! \033[0m"
echo -e -n "\033[36mPress any key to start install or exit with CTRL+C... \033[0m"
read -n1 -s
echo
echo "Update package repository... "
sudo apt update
check_result "Update package repository... "
echo "Install essential dependency of script... "
sudo apt install -y gnupg curl apt-transport-https ca-certificates software-properties-common
check_result "Install essential dependency of script... "
echo "Install Gitlab repository GPG Key... "
curl https://packages.gitlab.com/gpg.key 2> /dev/null | sudo apt-key add -
check_result "Install Gitlab repository GPG Key... "
echo "Install Gitlab repository source... "
sudo echo "deb https://mirrors.tuna.tsinghua.edu.cn/gitlab-ce/ubuntu xenial main" > /etc/apt/sources.list.d/gitlab.list
check_result "Install Gitlab repository source... "
echo "Install Jenkins repository GPG Key... "
curl https://pkg.jenkins.io/debian/jenkins.io.key 2> /dev/null | sudo apt-key add -
check_result "Install Jenkins repository GPG Key... "
echo "Install Jenkins repository source... "
sudo echo "deb https://pkg.jenkins.io/debian binary/" > /etc/apt/sources.list.d/jenkins.list
check_result "Install Jenkins repository source... "
echo "Install essential dependency of components... "
sudo apt install -y git default-jdk default-jre maven python-nose
check_result "Install essential dependency of components... "
echo "Update package repository... "
sudo apt update
check_result "Update package repository... "
echo "Install Jenkins... "
sudo apt install -y jenkins
check_result "Install Jenkins... "
echo "Change Jenkins default setting... "
sudo sed -i "s/HTTP_PORT=8080/HTTP_PORT=8081/g" /etc/default/jenkins
check_result "Change Jenkins default setting... "
echo "Restart Jenkins... "
sudo service jenkins restart
check_result "Restart Jenkins... "
echo "Install Gitlab... "
sudo apt install -y gitlab-ce
check_result "Install Gitlab... "
echo "Reconfigure Gitlab... "
sudo gitlab-ctl reconfigure
check_result "Reconfigure Gitlab... "
echo -e "\033[36mInstall all components successfully.\033[0m"
echo -e "\033[36mGitlab is running on port 80.\033[0m"
echo -e "\033[36mJenkins is running on port 8081.\033[0m"
