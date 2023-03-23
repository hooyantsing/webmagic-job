# webmagic-job

> 基于 springboot 底座、webmagic 爬虫内核、xxl-job 任务定时调度实现的分布式爬虫平台。

[![Java](https://img.shields.io/badge/java-8+-ae7118.svg?style=flat-square)](https://www.oracle.com/cn/java/technologies)
[![SpringBoot](https://img.shields.io/badge/springboot-2.7.9-6cb52d.svg?style=flat-square)](https://github.com/spring-projects/spring-boot)
[![Webmagic](https://img.shields.io/badge/webmagic-0.8.0-749c4c.svg?style=flat-square)](https://github.com/code4craft/webmagic)
[![Xxl-job](https://img.shields.io/badge/xxl%20job-2.3.1-69bc92.svg?style=flat-square)](https://github.com/xuxueli/xxl-job)
[![Mysql](https://img.shields.io/badge/mysql-8.0+-027792.svg?style=flat-square)](https://www.mysql.com)

### 架构设计

**调度中心**：任务发布、定时、调度、日志和统计

**爬虫执行器**：分布式部署、多线程执行任务，采集数据并自动入库

### 平台特性

* 优雅的 json 格式配置文件；
* 不受限制的字段定义以及多页面层层跳转；
* 通过代理、分布式和多线程方式执行任务，高效采集数据；
* 支持定时任务，采集到数据自动入库；

### 许可协议

[暂无]()
