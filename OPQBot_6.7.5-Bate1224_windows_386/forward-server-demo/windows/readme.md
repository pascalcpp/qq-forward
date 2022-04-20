## 基于OPQ开发forward server



1.

配置CoreConf.conf，
Token的获取 https://developer.gitter.im/docs/welcome

配置好后在cmd中运行firststart.bat只有初次使用时运行。

http://host:port/v1/Login/GetQRcode，进行登录得到cookie，后续一段时间都不需要重复登录



2.

配置config.properties

如下格式可以转发多个group

group.qq = xxxxx xxxxx xxxxx 

目前只支持这个功能



3.

以后登录都可以在cmd运行startup，完成启动

