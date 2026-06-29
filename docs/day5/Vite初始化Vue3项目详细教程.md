# Vite 初始化 Vue3 项目详细教程

### 一、 环境准备

安装 **Node.js 18+** 版本（官网下载：[https://nodejs.org/]），或者使用工具中的node-v24.17.0-win-x64.zip

#### 1 解压压缩包

1. 找到node-v24.17.0-win-x64.zip 文件
2. 解压到你希望存放的目录，**强烈建议路径不包含中文、空格或特殊字符**，例如 `D:\DevTools\nodejs`。
3. 解压完成后，目录内可直接看到 `node.exe`、`npm.cmd`、`npx.cmd` 等文件，该目录即为 Node.js 根目录。

#### 2 配置系统环境变量（核心步骤）

配置后才能在电脑任意位置的命令行中调用 `node` 和 `npm` 命令。

1. **打开环境变量设置**（任选一种方式）：
   - 右键「此电脑」→「属性」→ 点击「高级系统设置」→ 右下角「环境变量」
   - 按下 `Win + R`，输入 `sysdm.cpl` 回车 → 切换到「高级」选项卡 → 「环境变量」
2. **编辑 Path 变量**
   - 在下方「系统变量」列表中，找到名为 `Path` 的变量，选中后点击「编辑」
   - 点击「新建」，填入你刚才解压的 Node.js 根目录完整路径（例如 `D:\DevTools\nodejs`）
   - 依次点击所有窗口的「确定」，保存配置并关闭。

#### 3 验证安装是否成功

1. 按下 `Win + R`，输入 `cmd` 打开命令提示符（或 PowerShell）。

   > ⚠️ 重要：如果配置环境变量前已经打开了命令行，**必须关闭后重新打开**，否则读取不到新的环境变量。

2. 分别执行以下两条命令：

   ```bash
   node -v
   npm -v
   ```

3. 若两条命令都正常输出版本号（例如 `v24.17.0`、`11.13.0`），说明基础安装配置完成。



### 二、创建 Vite+Vue3 项目

~~~
  ├─在这一级目录打开命令行
  │
  └─├─ JavaProject/                  # 现有后端Java项目
    │  ├─ .idea
    │  ├─ src
    │  └─ pom.xml
    └─ work-order-front/    		 # 生成的前端项目
       ├─ src
       ├─ package.json
       └─ vite.config.js
~~~

1. 打开命令行，进入项目存放目录，执行创建命令：

```bash
npm create vite@latest work-order-front -- --template vue
```

1. 执行过程中提示安装`create-vite`，输入`y`回车确认。
2. 创建完成后，进入项目目录（work-order-front）并安装基础依赖：

```bash
npm install
```

### 三、安装项目依赖（Element Plus、Axios、Vue-Router、Pinia）

在项目目录（work-order-front）下执行以下命令，一次性安装所有依赖：

```bash
# Element Plus UI组件库
npm install element-plus
# Axios 网络请求库
npm install axios
# Vue-Router 路由
npm install vue-router@4
# Pinia 状态管理
npm install pinia
# qs
npm install qs
```

### 四、项目基础配置

#### 4.1 全局引入 Element Plus

修改`src/main.js`：

```javascript
import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import { createPinia } from 'pinia'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(createPinia())
app.mount('#app')
```

#### 4.2 配置路由

1. 新建`src/router/index.js`：

```javascript
import { createRouter, createWebHashHistory } from 'vue-router'
import Main from '../views/Main.vue'

const routes = [
    {
        path: '/',
        redirect: '/main'
    },
    {
        path: '/main',
        name: 'Main',
        component: Main
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router
```

#### 4.3 封装 Axios

新建`src/utils/request.js`：

```javascript
import axios from 'axios'

const service = axios.create({
    baseURL: 'http://localhost:8080', // 后端接口地址
    timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(config => {
    return config
}, error => {
    return Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(response => {
    return response.data
}, error => {
    return Promise.reject(error)
})

export default service
```

#### 4.4 封装工单 API（分布式项目不是必须封装）

新建`src/api/workOrder.js`：

```javascript
import request from '../utils/request'

// 分页查询工单列表
export const getOrderPage = (params) => {
    return request({
        url: '/api/work-order/page',
        method: 'get',
        params
    })
}

// 获取工单详情
export const getOrderDetail = (orderId) => {
    return request({
        url: `/api/work-order/detail/${orderId}`,
        method: 'get'
    })
}
```

### 五、项目启动

1. 前端项目目录`work-order-front`中打开命令行

2. 执行启动命令：

   ~~~bash
   npm run dev
   ~~~

3. 浏览器自动打开`http://localhost:5173`

4. 如果没有自动打开浏览器，需要进行配置修改：

   修改项目根目录的 `vite.config.js`，在配置中添加 `server.open: true`：

   ~~~javascript
   import { defineConfig } from 'vite'
   import vue from '@vitejs/plugin-vue'
   
   export default defineConfig({
     plugins: [vue()],
     server: {
       open: true, // 启动时自动打开浏览器
       // 如果想指定打开特定浏览器，比如 Chrome，可以写成：
       // open: 'chrome'
     }
   })
   ~~~

   

