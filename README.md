# Employee-Management-System

> 一个基于 Spring Boot 4 + MyBatis + MySQL 的后台管理系统（部门 / 员工 / 报表 / 文件上传 / 操作日志），配套 Vue 3 前端。
> 项目源自黑马程序员《JavaWeb + AI》课程，在课程基础上**独立完成了 Spring AOP 操作日志模块**等功能的实现与调试。

---

## 一、项目简介

TLIAS（Tlias）是一套企业内部员工管理后台，后端提供 RESTful 接口，前端做数据展示与交互。系统主要解决三件事：

1. **部门与员工数据的增删改查** —— 员工支持分页、条件筛选、工作经历（一对多）级联保存
2. **登录鉴权** —— JWT 令牌 + 拦截器统一校验，未登录访问接口返回 401
3. **操作留痕** —— 通过 Spring AOP 环绕通知，自动记录所有增删改接口的操作人、耗时、入参、返回值

---

## 二、技术栈

| 分类 | 技术 | 说明 |
|---|---|---|
| 语言 / 运行环境 | Java 17 | 用到 `stream().toList()`、`record` 等特性 |
| 框架 | Spring Boot 4.0.6 | 注意 4.0 与 3.x 的起步依赖**坐标有变更**（见下文「踩坑记录」） |
| Web 层 | Spring MVC (`spring-boot-starter-webmvc`) | RESTful 接口设计 |
| 持久层 | MyBatis 4.0.1 + PageHelper 1.4.7 | 注解 + XML 混合；PageHelper 做物理分页 |
| 数据库 | MySQL 8 | 库名 `tlias` |
| 连接池 | HikariCP | Spring Boot 默认 |
| 鉴权 | JJWT 0.11.5 | 签发 / 解析 JWT 令牌 |
| AOP | Spring AOP (`spring-boot-starter-aspectj`) | 环绕通知实现操作日志 |
| 对象存储 | 阿里云 OSS SDK 3.18.4 | 上传员工头像等文件 |
| 工具 | Lombok | `@Data` / `@Slf4j` 简化实体与日志 |
| 日志 | Logback | 自定义 `logback.xml`，只放开 `com.itheima` 包的 debug |
| 前端 | Vue 3 + Element Plus + Axios | 独立前端工程（本仓库只含后端） |
| 部署 | Nginx | 前端静态资源 + 反向代理 |

---

## 三、功能模块

### 1. 部门管理 `/depts`

| 方法 | 路径 | 功能 | 记录操作日志 |
|---|---|---|---|
| GET | `/depts` | 查询全部部门 | |
| GET | `/depts/{id}` | 根据 ID 查询部门 | |
| POST | `/depts` | 新增部门 | ✅ |
| PUT | `/depts` | 修改部门 | ✅ |
| DELETE | `/depts?id=` | 删除部门 | ✅ |

### 2. 员工管理 `/emps`

| 方法 | 路径 | 功能 | 记录操作日志 |
|---|---|---|---|
| GET | `/emps` | 分页 + 条件查询（姓名 / 性别 / 入职时间范围） | |
| GET | `/emps/{id}` | 根据 ID 查询（含工作经历） | |
| POST | `/emps` | 新增员工（含工作经历） | ✅ |
| PUT | `/emps` | 修改员工 | ✅ |
| DELETE | `/emps?ids=` | 批量删除员工 | ✅ |

### 3. 登录鉴权 `/login`

- `POST /login` —— 校验用户名密码，成功返回 JWT 令牌和员工信息
- `TokenInterceptor` 拦截 `/**`，放行 `/login`
- 令牌放在请求头 `token` 中，校验失败 / 缺失统一返回 **401**

### 4. 操作日志（本项目重点）

**需求**：记录所有增、删、改接口的操作日志，字段包含：操作人、操作时间、目标类全类名、目标方法名、方法运行时参数、返回值、方法执行时长。

**实现思路**：

```
Controller 方法打上 @Log 注解
        ↓
LogAspect 环绕通知拦截 @annotation(com.itheima.anno.Log)
        ↓
joinPoint.proceed() 前后取时间 → 算耗时
        ↓
组装 OperateLog → OperateLogMapper.insert() 落库
```

**核心技术点**：

- **`@Around` 环绕通知**：只有环绕通知能同时拿到「方法参数」「返回值」「执行耗时」，`@Before` / `@AfterReturning` 做不到
- **`ProceedingJoinPoint`**：
  - `getTarget()` 拿的是**原始对象**而非代理对象，保证 `class_name` 记录的是真实全类名
  - `getSignature().getName()` 拿方法名
  - `getArgs()` 拿运行时参数
  - `proceed()` 手动放行，且**必须 return 其返回值**，否则接口拿不到数据
- **`@annotation` 切点表达式**：只对标注了 `@Log` 的方法生效，而不是无差别拦截所有 Controller，避免把查询接口也写进日志表
- **ThreadLocal 传递操作人**：切面拿不到 `HttpServletRequest`，因此由 `TokenInterceptor` 解析 JWT 后把员工 ID 存进 `BaseContext`（内部是 `ThreadLocal`），切面再取出来
- **必须 `remove()`**：Tomcat 线程池的线程是**复用**的，`afterCompletion` 里不清理 ThreadLocal，下一个请求可能读到上一个请求残留的员工 ID，导致日志里的「操作人」串号

**关键代码**：

```java
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperateLogMapper operateLogMapper;

    @Autowired
    private ObjectMapper objectMapper;   // 用容器里的 Jackson，自带 Java 时间类型支持

    @Around("@annotation(com.itheima.anno.Log)")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime operateTime = LocalDateTime.now();
        Integer operateEmpId = BaseContext.getCurrentId();
        String className  = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        String methodParams = Arrays.toString(joinPoint.getArgs());

        long begin = System.currentTimeMillis();
        Object result = joinPoint.proceed();          // 执行原始方法
        long end = System.currentTimeMillis();

        String returnValue = objectMapper.writeValueAsString(result);
        long costTime = end - begin;

        OperateLog operateLog = new OperateLog();
        operateLog.setOperateEmpId(operateEmpId);
        operateLog.setOperateTime(operateTime);
        operateLog.setClassName(className);
        operateLog.setMethodName(methodName);
        operateLog.setMethodParams(methodParams);
        operateLog.setReturnValue(returnValue);
        operateLog.setCostTime(costTime);
        operateLogMapper.insert(operateLog);

        log.info("AOP记录操作日志：{}", operateLog);
        return result;                                 // 必须返回，否则接口拿不到数据
    }
}
```

`operate_log` 建表语句：

```sql
CREATE TABLE operate_log (
    id              INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    operate_emp_id  INT UNSIGNED   COMMENT '操作人ID',
    operate_time    DATETIME       COMMENT '操作时间',
    class_name      VARCHAR(255)   COMMENT '操作的类名',
    method_name     VARCHAR(255)   COMMENT '操作的方法名',
    method_params   VARCHAR(2000)  COMMENT '方法参数',
    return_value    VARCHAR(2000)  COMMENT '返回值',
    cost_time       BIGINT         COMMENT '方法执行耗时, 单位:ms'
) COMMENT '操作日志表';
```

### 5. 报表统计 `/report`

- `GET /report/empJobData` —— 统计各职位人数（返回 `{pos: [...], num: [...]}` 供 ECharts 柱状图使用）
- `GET /report/empGenderData` —— 统计员工性别分布

### 6. 文件上传 `/upload`

- `POST /upload` —— 接收 `MultipartFile`，通过 `AliyunOSSOperator` 上传至阿里云 OSS
- 配置通过 `@ConfigurationProperties` 绑定到 `AliyunOSSProperties`
- `application.yml` 中已配置单文件 10MB / 单请求 100MB 上限

---

## 四、项目结构

```
webai-project02/
├── tlias-web-management/          # 主工程
│   ├── src/main/java/com/itheima/
│   │   ├── anno/Log.java          # 自定义注解，标记需要记录日志的接口
│   │   ├── aop/LogAspect.java     # 操作日志切面（环绕通知）
│   │   ├── config/WebConfig.java  # 注册拦截器
│   │   ├── controller/            # Dept / Emp / Login / Report / Upload / Session
│   │   ├── exception/             # 全局异常处理器
│   │   ├── filter/                # Filter 示例（TokenFilter / DemoFilter）
│   │   ├── interceptor/           # TokenInterceptor 令牌校验 + ThreadLocal 写入
│   │   ├── mapper/                # MyBatis Mapper 接口
│   │   ├── pojo/                  # 实体类 / VO / DTO
│   │   ├── service/               # 业务接口与实现
│   │   └── utils/                 # JwtUtils / BaseContext / AliyunOSSOperator
│   └── src/main/resources/
│       ├── application.yml        # 数据源 / MyBatis / OSS 配置
│       ├── logback.xml            # 日志配置
│       └── com/itheima/mapper/    # MyBatis XML（动态 SQL）
├── springboot-aop-quickstart/     # AOP 学习用独立 Demo 工程
└── README.md
```

---

## 五、本地运行

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 建库建表

```sql
CREATE DATABASE tlias DEFAULT CHARSET utf8mb4;
```

需要的数据表：`dept`、`emp`、`emp_expr`、`emp_log`、`operate_log`（DDL 见上文「操作日志」一节及各实体类字段）。

### 3. 配置数据库密码

仓库里**不含任何明文密码**。`application.yml` 中默认激活 `local` 环境，密码从这个文件读：

```
tlias-web-management/src/main/resources/application-local.yml    ← 已被 .gitignore 忽略
```

内容形如：

```yaml
spring:
  datasource:
    password: 你的数据库密码
```

`application.yml` 里的兜底写法是 `${DB_PASSWORD:your_password}`：先用环境变量 `DB_PASSWORD`，没有就用默认值。**优先级：`application-local.yml` > 环境变量 > 默认值。**

> clone 下来的仓库里没有 `application-local.yml`（这是故意的）。Spring Boot 找不到该文件会直接跳过、不报错，你只要自己建一个填上密码即可。

阿里云 OSS 部分（`aliyun.oss.*`）需要换成自己的 Bucket；**AccessKey 未写入配置文件**，代码里走的是 SDK 默认凭证链（环境变量 `ALIBABA_CLOUD_ACCESS_KEY_ID` / `ALIBABA_CLOUD_ACCESS_KEY_SECRET`）。

### 4. 启动

```bash
cd tlias-web-management
mvn spring-boot:run
```

服务默认监听 `8080`。

### 5. 接口自测

```bash
# 登录拿令牌
curl -X POST http://localhost:8080/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"123456"}'

# 带令牌访问
curl http://localhost:8080/depts -H "token: <上一步返回的令牌>"
```

---

## 六、踩坑记录

这些是开发过程中真实踩过的坑，记录下来备查：

**1. Spring Boot 4.0 起步依赖改名了**

4.0 有一批 starter 的 artifactId 发生了变更，直接沿用 3.x 的坐标会报 `Dependency 'xxx' not found`：

| 3.x | 4.0 |
|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| `spring-boot-starter-aop` | `spring-boot-starter-aspectj` |
| `spring-boot-starter-json` | `spring-boot-starter-jackson` |

**这类坐标由 BOM 统一管理，不要手写版本号** —— BOM 里根本没有 `spring-boot-starter-aop` 这个坐标，加版本号也解决不了。

**2. Jackson 升到了 3.x**

Spring Boot 4.0 默认使用 **Jackson 3**，包名从 `com.fasterxml.jackson.databind` 变成 `tools.jackson.databind`。引入 `ObjectMapper` 时注意 import 的包名。

**3. `@Slf4j` 与局部变量命名冲突**

`@Slf4j` 会生成一个名为 `log` 的成员变量。如果在方法里再写 `OperateLog log = new OperateLog();`，局部变量会**遮蔽（shadowing）** 掉它，导致后面的 `log.info(...)` 编译报错。把变量改名为 `operateLog` 即可。

**4. `@Data` 不生成全参构造**

Lombok 的 `@Data` 只包含 `@Getter @Setter @ToString @EqualsAndHashCode @RequiredArgsConstructor`。类中没有 `final` / `@NonNull` 字段时，`@RequiredArgsConstructor` 生成的是**无参构造**，不是全参构造。需要全参构造得显式加 `@AllArgsConstructor`。

**5. DELETE 请求 405 而不是 404**

前端 `api/dept.js` 发的是 `request.delete('/depts?id=' + id)`（**问号传参**），而后端写成了：

```java
@DeleteMapping("/depts/{id}")          // ❌ 路径传参
public Result delete(@PathVariable Integer id)
```

`/depts` 这个路径本身存在（GET/POST/PUT 都注册在上面），只是没有 DELETE 对应的映射，所以 Spring MVC 返回的是 **405 Method Not Allowed** 而不是 404。

- 405 = 路径存在，但 HTTP 方法不支持
- 404 = 路径本身不存在

改成 `@DeleteMapping("/depts")` + `@RequestParam Integer id` 即可。

**6. `varchar(2000)` 溢出**

MySQL 严格模式下，超长字符串插入会直接抛 `Data too long for column`，而不是截断。日志表的参数字段长度需要评估。

---

## 七、可优化方向

- [ ] 操作日志接口补上分页查询（目前只写不读）
- [ ] 日志表的 `method_params` / `return_value` 超长时截断或改 `TEXT` 类型
- [ ] 引入 Spring Security 替换手写拦截器
- [ ] 增加单元测试覆盖率
- [ ] 用 Docker Compose 一键起 MySQL + 应用

---

## 八、License

[MIT](LICENSE)
