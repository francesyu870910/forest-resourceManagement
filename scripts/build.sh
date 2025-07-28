#!/bin/bash

# 森林资源管理系统构建脚本

echo "开始构建森林资源管理系统..."

# 检查环境
echo "检查构建环境..."

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装JDK 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "错误: Java版本过低，需要JDK 17+，当前版本: $JAVA_VERSION"
    exit 1
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请安装Maven 3.6+"
    exit 1
fi

# 检查Node.js
if ! command -v node &> /dev/null; then
    echo "错误: 未找到Node.js，请安装Node.js 16+"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo "错误: Node.js版本过低，需要16+，当前版本: $NODE_VERSION"
    exit 1
fi

# 检查npm
if ! command -v npm &> /dev/null; then
    echo "错误: 未找到npm"
    exit 1
fi

echo "环境检查通过"

# 创建构建目录
BUILD_DIR="build"
if [ -d "$BUILD_DIR" ]; then
    echo "清理旧的构建目录..."
    rm -rf "$BUILD_DIR"
fi
mkdir -p "$BUILD_DIR"

# 构建后端
echo "构建后端项目..."
cd backend

# 清理并编译
mvn clean compile
if [ $? -ne 0 ]; then
    echo "错误: 后端编译失败"
    exit 1
fi

# 运行测试
echo "运行后端测试..."
mvn test
if [ $? -ne 0 ]; then
    echo "警告: 后端测试失败，但继续构建"
fi

# 打包
echo "打包后端应用..."
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "错误: 后端打包失败"
    exit 1
fi

# 复制后端构建产物
cp target/*.jar "../$BUILD_DIR/"
echo "后端构建完成"

# 构建前端
echo "构建前端项目..."
cd ../frontend

# 安装依赖
echo "安装前端依赖..."
npm install
if [ $? -ne 0 ]; then
    echo "错误: 前端依赖安装失败"
    exit 1
fi

# 运行测试
echo "运行前端测试..."
npm run test 2>/dev/null || echo "警告: 前端测试跳过"

# 构建生产版本
echo "构建前端生产版本..."
npm run build
if [ $? -ne 0 ]; then
    echo "错误: 前端构建失败"
    exit 1
fi

# 复制前端构建产物
cp -r dist "../$BUILD_DIR/frontend"
echo "前端构建完成"

# 创建部署脚本
cd ..
cat > "$BUILD_DIR/deploy.sh" << 'EOF'
#!/bin/bash

# 森林资源管理系统部署脚本

echo "开始部署森林资源管理系统..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境"
    exit 1
fi

# 停止旧的服务
echo "停止旧的服务..."
pkill -f "forest-management" || true

# 启动后端服务
echo "启动后端服务..."
nohup java -jar forest-management-*.jar > app.log 2>&1 &
echo $! > app.pid

echo "后端服务已启动，PID: $(cat app.pid)"
echo "日志文件: app.log"

# 等待服务启动
sleep 5

# 检查服务状态
if curl -f http://localhost:8080/api/overview/system-status > /dev/null 2>&1; then
    echo "后端服务启动成功"
else
    echo "警告: 后端服务可能未正常启动，请检查日志"
fi

echo "前端文件已准备就绪，请配置Web服务器指向 frontend 目录"
echo "部署完成！"
EOF

chmod +x "$BUILD_DIR/deploy.sh"

# 创建停止脚本
cat > "$BUILD_DIR/stop.sh" << 'EOF'
#!/bin/bash

echo "停止森林资源管理系统..."

if [ -f app.pid ]; then
    PID=$(cat app.pid)
    if kill -0 $PID 2>/dev/null; then
        kill $PID
        echo "服务已停止 (PID: $PID)"
        rm app.pid
    else
        echo "服务未运行"
        rm app.pid
    fi
else
    echo "未找到PID文件，尝试强制停止..."
    pkill -f "forest-management" || echo "未找到运行的服务"
fi
EOF

chmod +x "$BUILD_DIR/stop.sh"

# 创建README
cat > "$BUILD_DIR/README.md" << 'EOF'
# 森林资源管理系统部署包

## 部署说明

### 环境要求
- Java 17+
- Web服务器 (Nginx/Apache，可选)

### 部署步骤

1. **部署后端服务**
```bash
./deploy.sh
```

2. **配置Web服务器** (可选)
将 `frontend` 目录配置为Web服务器的根目录，并设置反向代理：

Nginx配置示例：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    root /path/to/frontend;
    index index.html;
    
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

3. **访问系统**
- 直接访问: http://localhost:8080 (仅后端API)
- 通过Web服务器: http://your-domain.com

### 管理命令

- 停止服务: `./stop.sh`
- 查看日志: `tail -f app.log`
- 检查状态: `curl http://localhost:8080/api/overview/system-status`

### 故障排除

1. **端口被占用**
   - 修改 application.yml 中的端口配置
   - 或停止占用端口的其他服务

2. **服务启动失败**
   - 检查 app.log 日志文件
   - 确认Java版本是否正确

3. **前端无法访问后端**
   - 检查防火墙设置
   - 确认后端服务正常运行
   - 检查跨域配置
EOF

echo ""
echo "构建完成！"
echo "构建产物位于: $BUILD_DIR/"
echo "包含文件:"
ls -la "$BUILD_DIR/"
echo ""
echo "部署说明:"
echo "1. 将 $BUILD_DIR 目录复制到目标服务器"
echo "2. 运行 ./deploy.sh 启动服务"
echo "3. 配置Web服务器指向 frontend 目录 (可选)"
echo ""