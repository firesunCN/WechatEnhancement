# 微信增强插件 WechatEnhancement
本项目需要Xposed，同时支持太极与[VirtualXposed](https://github.com/android-hacker/VirtualXposed) 

## 支持的微信版本
- 理论上支持微信全版本，自适应方式采用微信巫师的自动搜索方式，所以理论上即使微信升级也可继续支持
- 已测试通过的微信版本：6.6.0，6.6.3，6.6.5，6.6.6，6.6.7，6.7.3，7.0.0，7.0.3，7.0.4，7.0.6，7.0.7

## 支持功能
- 抢红包
- 自动接收转账 **（恢复聊天记录时务必关闭此功能）**
- 消息防撤回
- 朋友圈防删除
- 朋友圈去广告
- 电脑端微信自动登录
- 突破发送图片9张限制

## 效果预览
<img src="https://raw.githubusercontent.com/firesunCN/WechatEnhancement/master/image/screenshot1.jpg" width="45%" /> <img src="https://raw.githubusercontent.com/firesunCN/WechatEnhancement/master/image/screenshot2.jpg" width="45%" />
<img src="https://raw.githubusercontent.com/firesunCN/WechatEnhancement/master/image/screenshot3.jpg" width="45%" />

## 致谢
本项目为以下三个项目的融合，使用Java重（chao）写（xi）了微信巫师的自动搜索hook类的功能，并应用在抢红包和自动接收转账上，使得以上功能都能自动适配微信，在此十分感谢veryyoung，Gh0u1L5，wuxiaosu

[WechatLuckyMoney](https://github.com/veryyoung/WechatLuckyMoney) 

[WechatMagician](https://github.com/Gh0u1L5/WechatMagician) 

[XposedWechatHelper](https://github.com/wuxiaosu/XposedWechatHelper) 
