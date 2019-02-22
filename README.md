# Dtc双屏值班终端(开发代号:Dtc_S)

##  整体思路：
  * sysinfo接口获取数据
  * 缓存videoSources和SipSources
  * 注册Sip到服务器
  * 启动各种服务
## 常用接口

  * /webapi/sysinfo 		输出服务资源定义
  * /webapi/videos[?groupid=xx] 	输出视频资源
  * /webapi/videogroups		输出视频资源分组
  * /webapi/sips[?groupid=xx]	输出 SIP 资源
  * /webapi/sipgroups		输出 SIP 资源分组
  * /webapi/sipstatus		输出 SIP 注册，振铃，通话的状态 (不在此列表中的SIP客户端视为离线)
  * /webapi/onlinedevices		输出心跳保活的设备，带有弹箱等状态
  * /webapi/siprelease?number=xxxx	执行强拆指定号码的 SIP 会话
  * /webapi/fingerprint?feature=xx	执行指纹比对，返回用户信息, feature=指纹特征码
  * /webapi/locations[?type={camera|terminal}&guid=xxx] 返回指定的监控点或者哨位终端的坐标信息，如未指定type或guid则返回所有监控点和哨位终端的坐标信息
  * /webapi/alertdefines		输出所有报警类型关联的警灯颜色，red/yellow/green/blue/orange/pink/none -> 红黄绿蓝橙粉无，无定义一般按红色处理
/webapi/videotypes		
  
  

### 关于作者
  >>> `每只菜鸟都有鹰的梦想！`
