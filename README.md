<p align="center">
    <img src="https://pro-oss.tduckcloud.com/6d4fa9c6ee554cd193161c4e658cb5e6.png" width="100" alt="TDuck Logo" />
</p>

<p align="center">
    <img src="https://gitee.com/TDuckApp/tduck-platform/badge/star.svg?theme=dark" alt="Gitee Star" />
    <img src="https://gitee.com/TDuckApp/tduck-platform/badge/fork.svg?theme=dark" alt="Gitee Fork" />
    <img src="https://img.shields.io/github/stars/tduckcloud/tduck-platform?style=social" alt="GitHub Stars" />
    <img src="https://img.shields.io/github/forks/tduckcloud/tduck-platform?style=social" alt="GitHub Forks" />
    <img src="https://img.shields.io/badge/Version-6.0-brightgreen" alt="Version" />
    <img src="https://img.shields.io/badge/License-MIT-orange" alt="License" />
</p>

<p align="center">
    <a href="https://www.tduckcloud.com">官方网站</a>
    ·
    <a href="https://doc.tduckcloud.com">部署文档</a>
    ·
    <a href="https://gitee.com/TDuckApp/tduck-platform/issues">用户社区</a>
    ·
    <a href="https://space.bilibili.com/409825300">Bilibili频道</a>
</p>

<p align="center">
    ⭐ 如果 TDuck 对您的项目有所帮助，欢迎 Star 支持项目持续发展
</p>

---



简体中文 |  [English](./README_en.md)


## 为什么选择 TDuck

TDuck 是一款企业级表单与数据采集平台，支持拖拽式表单设计、私有化部署和开放集成，帮助企业快速构建业务数据入口，实现数据的高效采集与流转。相比传统 SaaS 表单产品，TDuck 提供更高的数据控制权、更灵活的部署方式以及更强的系统集成能力。

### 功能特性

- 支持 **27+自定义组件** ，拖拽式快速生成表单问卷。
- 支持单行文本、多行文本、日期、下拉、单选、文件上传、排序、级联、轮播、一键定位、手机号验证、矩阵量表、子表单等组件。
- 支持通过文本批量导入表单组件，支持题目显隐逻辑设置。
- 表单数据，支持数据新增、编辑、导出、打印、预览和打包下载附件。
- 表单外观支持头图、背景图、背景颜色、按钮文字等配置。
- 报表支持对问题实时统计分析并以图形（柱状图、折线图、饼图）的形式展示输出和导出png图片。
- 提交后自定义文案、提交后自动跳转网址。
-  **每个微信、账号、ip、设备、答题次数限制、支持设置允许填写时间、记录微信个人信息** 。
- 支持发邮件、微信公众号模板推送提醒。
- 支持 **数据同步Api（全量数据）、数据WebHook推送（可订阅事件：新增、修改、删除）** 。
- 支持保存至模板中心，支持从模板中心选用模板创建表单问卷。
- 用户管理，新增用户、修改用户、删除用户。
- 文件存储自定义配置： **支持阿里云、七牛云、又拍云、本地、通用S3协议上传** 。
- 支持 **邮件、短信（阿里云、腾讯云、中昱维信）、微信公众号参数配置** 。
- 支持回收中心，快速恢复问卷。
- 支持配置[TReport可视化大屏](https://gitee.com/TDuckApp/tduck-report-platform)，高效展示数据


### 版本说明


表单系统有3个版本（社区、Pro、TDuckX） [填鸭表单产品能力对比](https://www.tduckcloud.com/doc/x/nSJMvQh6)

所有版本的填鸭表单数据都可以通过WebHook集成至大屏端，实现数据同步：[表单与TReport数据同步教程](https://www.bilibili.com/video/BV1MH4y1K7Xa/)；



| 核心决策维度           | 传统 SaaS 表单 | TDuck 社区版         | [TDuckX / Pro 企业源码版](https://x.tduckcloud.com)          |
| ---------------- | ---------- | ----------------- | -------------------------------- |
| **部署方式**         | SaaS 云端    | 支持私有部署         | ✔️ 私有化部署 / 信创部署                  |
| **数据存储**         | 第三方平台      | 企业自有服务器        | ✔️ 企业自有服务器                       |
| **数据控制权**        | 受平台约束      | ✔️ 自主控制           | ✔️ 完全自主控制                        |
| **源码开放程度**       | ❌ 不开放      | ✔️ 后端开源             | ✔️ 完整商业源码交付                      |
| **二次开发**         | 受平台能力限制    | ✔️ 需自行扩展          | ✔️ 支持深度定制与源码级开发                  |
| **系统集成**         | API 能力有限   | ✔️  WebHook  | ✔️ API / WebHook / 用户体系 / 业务系统集成 |
| **组织与权限**        | 平台标准能力     | 用户管理              | ✔️ 企业级组织、角色与权限体系                 |
| **SSO / 统一身份认证** | 依赖平台能力     | 需自行开发             | ✔️ 支持按企业需求进行集成                   |
| **项目交付**         | ❌ 不适合复杂定制  | 适合基础场景            | ✔️ 支持项目化交付与行业定制                  |
| **部署环境适应性**      | 受平台限制      | ✔️ 自主部署           | ✔️ 支持企业服务器、内网等部署场景               |
| **国产化 / 信创适配**   | 依赖 SaaS 厂商 | 需自行适配             | ✔️ 支持企业级适配与项目交付                  |
| **运维责任**         | 厂商负责       | 企业自行负责            | 企业自主运维 + 厂商技术支持                  |
| **升级方式**         | 平台统一升级     | 社区版本迭代            | ✔️ 企业版本升级与技术支持                   |
| **厂商依赖**         | **高**      | 低                 | **可控**                           |
| **长期成本**         | 持续订阅       | 软件成本低，开发运维成本由企业承担 | 软件授权 + 可控的技术服务保障                 |
| **适合场景**         | 快速在线使用     | 技术团队自主搭建          | **企业信息化、私有化项目、系统集成与商业交付**        |


> 如果您正在评估将表单系统用于商业项目或私有化交付，建议重点关注源码授权与长期成本结构。




### 预览-社区版 - Preview

如需在线体验系统👉：[官网体验地址](https://www.tduckcloud.com) 

![输入图片说明](readmeImages/iShot_2026-05-27_12.01.39.png)

![登录页](readmeImages/iShot_2026-05-27_11.53.30.png)

![个人中心](readmeImages/iShot_2026-05-27_11.54.41.png)
![用户管理](readmeImages/iShot_2026-05-27_11.54.52.png)
![AI配置](readmeImages/iShot_2026-05-27_11.55.01.png)
![AI建表](readmeImages/iShot_2026-05-27_11.55.35.png)
![表单设计](readmeImages/iShot_2026-05-27_11.55.56.png)
![逻辑设置](readmeImages/iShot_2026-05-27_11.56.11.png)
![表单发布](readmeImages/iShot_2026-05-27_11.56.28.png)
![趋势统计](readmeImages/iShot_2026-05-27_11.56.38.png)
![数据分析](readmeImages/iShot_2026-05-27_11.56.49.png)
![表单填写](readmeImages/iShot_2026-05-27_11.57.18.png)



### 部署安装
> - 部署管理员账号：admin@tduckcloud.com
> - 部署默认密码：123456

- 方式一：使用宝塔面板一键安装 🔥推荐 https://doc.tduckcloud.com/openSource/deploy/deployforbt.html

- 方式二：使用Docker快速启动 https://doc.tduckcloud.com/openSource/deploy/dockerDeploy.html

- 方式三：使用宝塔部署项目 https://doc.tduckcloud.com/openSource/deploy/openSourceDeploy.html

- 方式四：前后端分离部署 https://doc.tduckcloud.com/openSource/deploy/fenli.html


### 相关文档
- [填鸭表单系列产品能力对比](https://www.tduckcloud.com/doc/x/nSJMvQh6)
- <a href="https://doc.tduckcloud.com/openSource/deploy/deployforbt.html" target="_blank">宝塔一键安装（小白篇）</a>
- [前端项目地址](https://gitee.com/TDuckApp/tduck-front)
- [小程序插件](https://doc.tduckcloud.com/functionDesc/uniappDesc.html)
- 如果您在使用社区版过程中遇到了问题，可在社区查看常见问题或留言进行求助 - [点击进入填鸭问答社区](https://gitee.com/TDuckApp/tduck-platform/issues)


### License

MIT License


