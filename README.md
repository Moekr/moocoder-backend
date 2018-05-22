MOOCODER Backend
===========================
基于GitLab、Jenkins与Docker的自动化编程考试系统后端部分

### 主要架构

![主要架构](https://pic.moekr.com/2018/05/997939A6D068F036A0E8850D2BF25D1E.png)

### 特性

- [x] 使用通用的Git协议
- [x] 自动化测试，实时展示成绩
- [x] 完全隐藏GitLab和Jenkins
- [x] Docker中运行测试，隔离网络
- [x] CPU、内存资源限制
- [ ] LDAP统一认证
- [ ] 分布式与GitLab集群
- [ ] WebIDE支持

### 支持题型

- [x] Python
- [x] Python覆盖率测试
- [x] Java
- [x] Java覆盖率测试
- [x] Java变异覆盖测试

### 编译

1. 编译[前端项目](https://github.com/Moekr/moocoder-frontend)
2. 对前端项目进行[WebJars封装](https://github.com/Moekr/moocoder-frontend-webjars)
3. 编译本项目

### 依赖

> 推荐使用Linux操作系统并在宿主机安装docker，所有依赖实例除NGINX外都使用docker运行，其中Jenkins实例推荐将宿主机的docker二进制文件和docker unix socket映射至容器中

* Docker支持
* 邮件发送支持
* 一个GitLab实例
    ```
    docker run -d --name moocoder-gitlab -p 8081:80 --restart always -v /srv/moocoder/gitlab/config:/etc/gitlab:Z -v /srv/moocoder/gitlab/log:/var/log/gitlab:Z -v /srv/moocoder/gitlab/data:/var/opt/gitlab:Z gitlab/gitlab-ce:10.8.0-ce.0
    ```
* 一个Jenkins实例
    ```
    docker run -d --name moocoder-jenkins -p 8082:8080 --restart always -v /srv/moocoder/jenkins:/var/jenkins_home:Z -v /usr/bin/docker:/usr/bin/docker:Z -v /var/run/docker.sock:/var/run/docker.sock:Z -u root jenkins/jenkins:2.124
    ```
    
    注意：最新版本的Jenkins官方Docker镜像缺少docker二进制文件所依赖的运行库libltdl，需要使用`apt install libltdl-dev`进行安装
    其他：由于Jenkins Workspace中的文件均为临时性文件，当磁盘负担较重时可以使用`-v /dev/shm/jenkins:/var/jenkins_home/workspace:Z`来使用内存进行加速
    
* 一个Docker Registry实例
    ```
    docker run -d --name moocoder-registry -p 5000:5000 --restart always -v /srv/moocoder/registry:/var/lib/registry:Z library/registry:2.6.2
    ```
* 一个NGINX实例

### 配置

1. 选择一个随机字符串作为内部的Secret
2. 配置GitLab
    * 为管理员用户生成一个至少包含api与sudo权限的Token
    * 为GitLab自带的NGINX增加一个反向代理服务，用于为WebHook请求增加`X-Moocoder-Secret`头
        * 修改GitLab的配置文件，示例：
            ```diff
              # nginx['custom_gitlab_server_config'] = "location ^~ /foo-namespace/bar-project/raw/ {\n deny all;\n}\n"
            - # nginx['custom_nginx_config'] = "include /etc/nginx/conf.d/example.conf;"
            + nginx['custom_nginx_config'] = "include /etc/gitlab/webhook-proxy.conf;"
              # nginx['proxy_read_timeout'] = 3600
            ```
        * 增加新的NGINX配置文件，示例：
            ```
            server {
            	listen 3000 default_server;
            	listen [::]:3000 default_server;
            	server_name _;
            	
            	location / {
            		proxy_pass http://backend_host:8090;
            		
            		proxy_set_header X-Moocoder-Secret backend_secret;
            	}
            }
            ```
        * 勾选系统设置中的`Allow requests to the local network from hooks and services`
3. 配置Jenkins
    * 增加全局变量`MOOCODER_HOST`和`MOOCODER_SECRET`
    * 增加一个ID为`MOOCODER`的Credential，内容为GitLab管理员用户的账号密码
    * 设置合适的Executor数量
    * 安装Cobertura插件，用于测试覆盖率报告的获取
4. 配置Docker
    * 设置Docker Daemon信任启动的Registry实例为insecure的
    * 如果Docker Daemon与后端主程序不在同一台设备上，需要设置Docker暴露TCP通信端口
5. 配置NGINX，主要用于拦截部分外部请求和分流Git请求，示例：
    ```
    server {
        listen 80 default_server;
        listen [::]:80 default_server;
        server_name _;
    
        location ~ .*\.(js|css|ico|woff2)$ {
            include /etc/nginx/proxy_params;
            
            proxy_pass http://localhost:8090;
            
            proxy_cache moocoder;
            expires 7d;
    
            add_header Cache "$upstream_cache_status";
            proxy_ignore_headers X-Accel-Expires Expires Cache-Control Set-Cookie;
            proxy_hide_header Pragma;
    
            proxy_cache_valid 200 304 301 302 8h;
            proxy_cache_valid 404 1m;
            proxy_cache_valid any 2d;
        }
    
        location ~ /internal/ {
            return 403;
        }
    
        location ~ / {
            include /etc/nginx/proxy_params;
    
            if ($request_uri ~* (info/refs|git-upload-pack|git-receive-pack)) {
                proxy_pass http://localhost:8081;
            }
    
            proxy_pass http://localhost:8090;
        }
    }
    ```
6. 配置好后端项目config.yml中的各项参数
7. (可选)配置防火墙，拦截对部分端口的访问

### 协议

GPLv3
