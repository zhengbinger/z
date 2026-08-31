---
name: "java-design-doc"
description: "为 Spring Boot 项目（含 RBAC/JWT/Redis/状态机/任务调度等模块）生成/维护统一风格的设计文档，含 7 章骨架、ER/时序/分层/流程图模板。用户要求写某模块设计文档或补全某领域文档时调用。"
---

# Spring Boot 项目设计文档生成器

适用于本工作区下任何 Spring Boot 项目的设计文档编写与维护。可覆盖 RBAC 权限、双 Token 认证、Redis 缓存、观察者模式状态机、用户管理、任务调度、消息通知等各类领域模块。

## 一、触发条件

当出现以下任一请求时**立即调用**本 Skill：

1. 用户说"写 XX 设计文档"、"XX 模块出一个设计文档"、"整理 XX 成文档"、"把 XX 方案记成文档"。
2. 实现完一套功能后用户要求"总结成文档"、"生成对应的文档"。
3. 用户要求"补全某文档缺失章节"、"把某章节的图改成 mermaid / ASCII / SVG"。
4. 代码侧完成了 RBAC / 认证 / 缓存 / 状态机 / 调度 / 消息通知 等新领域能力，需要沉淀文档。

## 二、文档统一结构（必须遵循的骨架）

```
# {文档主题} · 设计文档

> 模块路径：`{com.yourcompany}.{module}[.{subpackage}]`
> 作者：{author} · 版本：{version} · 日期：YYYY-MM-DD

## 1. 设计目标
### 1.1 背景（推荐：说明项目现状痛点、为什么要做这个设计、现状清单）
### 1.2 设计目标（3~7 条，列出要解决的问题 + 非目标（可选））

## 2. 核心概念与对比表
（按主题选择：技术栈表 / ER 关系 / 方案对比 / 分工边界表 / 角色映射）

## 3. {详细设计章节 A}
（例如：表结构、JWT 设计、序列化策略、核心服务、接口契约...）

## 4. {详细设计章节 B}

## ... 按需追加章节（扩展章节触发规则见 §三之二）...

### 可选扩展章节（按触发条件追加，非全部必写）：
- 背景子章节（§1.1，推荐）
- 分工边界表 + 决策流程图（多方案并存时必加 / 推荐）
- 各场景使用方式（设计方案类文档强烈推荐）
- 缓存设计（涉及 Redis/Spring Cache 时必加）
- 同步一致性机制（跨数据源双份存储时必加）
- 核心流程时序图（每个对外接口必加）
- 取舍建议表（推荐）

## N. 扩展约定 / 最佳实践 / 踩坑与最佳实践
（5~10 条，列出 Do/Don't、性能红线、已知取舍 + 取舍建议表）

## N+1. 相关文件
（一张两列表：类别 | 文件路径。路径相对项目根，如 src/main/java/{package}/service/XxxService.java）
```

**头部四行元数据不可省略**：标题（`# XXX · 设计文档`）、`>` 引用块内的模块路径 + 作者 + 版本 + 日期。日期格式 `YYYY-MM-DD`。

**章节编号**：按 `## N.` 顺序递增，层级标题不跨级。

**文件名**：kebab-case，中文主题用英文转译，放在 `docs/` 目录下，例如：
- 项目架构 → `docs/project-architecture-design.md`
- RBAC 权限体系 → `docs/rbac-permission-system-design.md`
- 双 Token 认证 → `docs/dual-token-auth-design.md`
- Redis 缓存 → `docs/redis-cache-design.md`
- 状态变更观察者 → `docs/{topic}-observer-design.md`

## 三、统一写作规范

1. **真实代码示例**：所有 ```java / yaml / sql / xml``` 代码块必须来自项目源码，不得编造型字段、方法名、返回码；若源文件已存在，直接抄录片段。
2. **表格优先**：对比（两方案）、字段（表结构）、清单（接口/方法/文件）一律用 Markdown 表格。
3. **技术名词保留英文**：中文正文 + 英文专有名词（如 Token、TTL、CacheEvict、DTO/VO、RBAC、JWT、CAS、Lua）。
4. **文件引用**：文档中引用项目代码/资源路径一律**相对路径**，用 `src/main/java/{package}/xxx/Xxx.java` 格式（`{package}` 从项目根扫描得到，例如 `com/example/demo`）。
5. **Java 项目通用规则贯穿**（项目无明文反对时默认套用，遇到冲突以项目实际约定为准）：
   - 命名：`{com.yourcompany}.{module}.{layer}`、大驼峰类、小驼峰方法、常量全大写下划线
   - 字符串/集合判空工具：Hutool 的 `StrUtil` / `CollUtil`（或项目已选定的工具类，如 `StringUtils`）
   - Service 写方法必须加 `@Transactional(rollbackFor = Exception.class)`
   - 依赖注入优先 `@Resource`（项目可改用 `@Autowired` 或构造器注入）
   - 日志用 Slf4j + 占位符 `{}`，异常必须打印堆栈 `log.error("...", e)`
   - 性能红线：禁止循环查 DB，禁止魔法值
   - Git 备注风格：遵循项目已有 commit history 的 type + scope 约定（如 `feat(scope): ...`）

## 三之二、设计方案文档扩展规范（可选章节触发规则）

本节规范沉淀自 `dict-system-design.md` / `i18n-design.md` 等设计方案文档的写作实践。设计方案类文档（含表结构 + Service + Controller + 多场景使用）在 §二 基础骨架之上，**按以下触发条件追加扩展章节**，后续同类文档参照执行。

### 1. 背景子章节（§1.1，推荐）

设计方案类文档**强烈推荐**在"设计目标"前加 §1.1 背景，说明：
- 当前项目存在的问题/痛点（如魔法值、硬编码中文、N+1 等）
- 为什么要做这个设计（业务/技术驱动）
- 现状清单（如有，列出改造前的具体问题点 + 涉及的类/字段/SQL 注释）

**格式参考**：
```
### 1.1 背景

当前项目存在 XXX 问题，集中在 N 个层面：
1. **问题点 A**：具体描述 + 代码/字段示例（如 `UserStatus` 枚举文案写死）
2. **问题点 B**：具体描述（如 `sys_permission.type` 注释为 `1-菜单,2-按钮`，前端拿不到）
...
引入本设计后，XXX 统一走 YYY，运营后台可维护，前端按 ZZZ 拉取，后端按 WWW 校验。
```

### 2. 分工边界表（多方案并存时必加）

当设计涉及多个工具/方案并存（如 Java 枚举 + 数据字典 + 常量类、JWT + Session、@Cacheable + 手动 Redis）时，**必须**用对比表说明职责分工边界。

**格式**：

| 维度 | 方案 A | 方案 B | 方案 C |
|------|--------|--------|--------|
| 职责 | ... | ... | ... |
| 变更方式 | ... | ... | ... |
| 多语言 | 是/否 | 是/否 | 是/否 |
| 前端可见 | 是/否 | 是/否 | 是/否 |
| 典型示例 | ... | ... | ... |

**结论用一句话点明"谁管什么"**：如"枚举管逻辑判断，字典管展示，常量管程序级固定值"。

### 3. 决策流程图（多方案并存时推荐）

配合分工边界表，用 mermaid `flowchart TD` 决策树帮助判断"某个值/字段该用哪种方案"。比纯表格更直观，回答"我该选哪个"。

**格式参考**：

```mermaid
flowchart TD
    A[新增一个值-含义对应关系] --> B{是否在 Java 里做<br/>if/switch 逻辑判断?}
    B -->|是| C{是否需要展示/多语言?}
    B -->|否| D{是否与业务相关?}
    C -->|否, 稳定不变| E[✅ 只用枚举]
    C -->|是| F[✅ 枚举 + 字典双份]
    D -->|是, 仅展示| G[✅ 只用字典]
    D -->|否, 程序级固定值| H[✅ 只用常量类]
```

### 4. 各场景使用方式章节（设计方案类文档强烈推荐）

设计方案类文档**强烈推荐**单独设一章"各场景使用方式"，列举 **5-6 个具体业务场景**，每个场景统一格式：

```
### X.1 场景一：场景名称

**场景**：业务痛点描述（如"用户列表页筛选条件里有'用户状态'下拉框"）

**调用方式**：
​```http
GET /xxx/yyy
Accept-Language: zh_CN
​```

**响应示例**：
​```json
{ "code": 200, "data": [...] }
​```

**代码片段**（来自项目源码或改造后片段，注明文件路径）：
​```java
// 来源：src/main/java/.../Xxx.java
xxx
​```

**取舍说明**（如有多方案）：
- 方案 A：优点 → 适用场景
- 方案 B：优点 → 适用场景
默认推荐 X，理由：...
```

**典型场景类型**（按需挑选凑够 5-6 个）：
- 前端下拉框数据源 / 列表数据回显
- 后端入参自动校验（自定义注解 + ConstraintValidator）
- 数据导出 Excel（Converter 双向转换）
- 替代 Java 枚举魔法值（重构前后对比）
- 多语言展示联动（Accept-Language 切换）
- 缓存命中/失效场景
- 跨数据源同步校验

### 5. 缓存设计章节（涉及 Redis/Spring Cache 时必加）

涉及 Redis / Spring Cache 的模块，**必须**单独设一章"缓存设计"，含两张表：

**缓存策略表**：

| 维度 | 配置 |
|------|------|
| 缓存空间 | `xxx` |
| 缓存键 | `xxx:{key}:{locale}`（多语言分维度） |
| TTL | 30 分钟 |
| 读策略 | `@Cacheable`，未命中查 DB 后回填 |
| 写策略 | `saveOrUpdate` 触发 `@CacheEvict(allEntries = true)` |
| 启动预热 | `ApplicationRunner` 加载高频 key |

**缓存失效场景表**：

| 操作 | 失效范围 | 原因 |
|------|---------|------|
| 新增/修改 X | 全部 `xxx:*` | ... |
| 状态切换 | 全部 `xxx:*` | ... |
| 多语言 locale 字段更新 | 全部 `xxx:*` | 各 locale 缓存独立，统一清空简化逻辑 |

### 6. 同步一致性机制（跨数据源双份存储时必加）

当同一份"值-含义"对应关系存在多份存储（如 Java 枚举 + DB 字典双份、缓存 + DB 双份、配置文件 + DB 双份）时，**必须**设计启动校验机制，防止两边不一致导致线上事故。

**实现方式**：`ApplicationRunner` 启动时遍历枚举，校验每条枚举值在字典里是否存在，不一致直接抛 `IllegalStateException` 阻止启动。

**代码模板**：

```java
// src/main/java/{package}/component/xxx/XxxEnumSyncValidator.java

/**
 * 启动时校验枚举与字典一致性
 * 防止枚举新增值后忘记同步字典
 *
 * @author {author}
 * @since YYYY-MM-DD
 **/
@Slf4j
@Component
public class XxxEnumSyncValidator implements ApplicationRunner {

    @Resource
    private DictService dictService;

    @Override
    public void run(ApplicationArguments args) {
        for (XxxEnum e : XxxEnum.values()) {
            if (!dictService.isValidValue(XxxEnum.DICT_CODE, String.valueOf(e.getCode()))) {
                throw new IllegalStateException(
                        "枚举与字典不一致: " + e.name()
                                + ", code=" + e.getCode()
                                + ", dictCode=" + XxxEnum.DICT_CODE);
            }
        }
        log.info("枚举与字典一致性校验通过");
    }
}
```

**触发条件**：枚举保留 `DICT_CODE` 常量 + `getCode()` 方法，文案完全交给字典（枚举不再硬编码 description 中文字段）。

### 7. 核心流程时序图（每个对外接口必加）

设计方案文档的每个对外接口/关键业务流程，**必须**配 mermaid `sequenceDiagram`，覆盖正常 + 异常分支（`alt/else`）。

- 正常分支：缓存命中 / 校验通过 / 业务成功
- 异常分支：缓存未命中回源 / 校验失败抛异常 / 业务失败返回错误码

详见 §四 图形式选型规则的 `sequenceDiagram` 固定风格。**一个文档至少 2 张时序图**（如：前端拉取 + 入参校验，或：登录 + 刷新）。

### 8. 取舍建议表（推荐）

设计方案文档**推荐**在"扩展约定"章节内或独立章节，用三列表格总结关键设计取舍，便于后续维护者快速理解决策依据：

**格式**：

| 取舍点 | 推荐 | 理由 |
|--------|------|------|
| xxx 值类型 | 字符串 | 兼容非数字值（如 password/oauth） |
| A vs B | 并存 | 各司其职，A 管逻辑 B 管展示 |
| `@CacheEvict(allEntries=true)` | 是 | 更新频率低，整空间清空影响可忽略 |
| 前端转换 vs 后端转换 | 默认前端 | 网络包小、响应快；特殊接口后端转换 |

### 9. 扩展章节选用速查表

| 章节 | 触发条件 | 必加/推荐 |
|------|---------|-----------|
| 背景（§1.1） | 设计方案类文档 | 推荐 |
| 分工边界表 | 多方案并存（≥2 种） | 必加 |
| 决策流程图 | 多方案并存 | 推荐 |
| 各场景使用方式 | 设计方案类文档 | 强烈推荐 |
| 缓存设计 | 涉及 Redis/Spring Cache | 必加 |
| 同步一致性机制 | 跨数据源双份存储 | 必加 |
| 核心流程时序图 | 每个对外接口 | 必加 |
| 取舍建议表 | 设计方案类文档 | 推荐 |

**反模式（禁止）**：纯架构描述类文档（如 `project-architecture-design.md`）不要硬塞"各场景使用方式"章节，按主题自然组织即可。扩展章节只针对"设计方案类"文档生效。

## 四、图形式选型规则（重要，按用户最近偏好）

### 默认首选图（本项目当前偏好）

| 图的语义 | 推荐语法 | 示例用途 |
|----------|----------|----------|
| 实体关系（ER） | **mermaid `erDiagram`** | 业务表关联 / RBAC 多表关系 |
| 调用/泳道时序 | **mermaid `sequenceDiagram`** | 登录/刷新/登出流程 / 缓存命中 4 阶段链路 / 事件分发 |
| 模块 / 数据流概览 | **mermaid `graph LR`** | 双链路概览 / 组件依赖 |
| **分层架构（自上而下堆叠）** | **mermaid `graph TB`**（首选） / `flowchart TD` | Controller→拦截器→Service→Mapper→MySQL/Redis 分层 |
| 拦截/处理链路（带判断节点） | **mermaid `flowchart TD`**（首选） / ASCII 兜底 | AOP 拦截链路 / 状态流转 / 鉴权判断分支 |
| 横向对比 / 双向依赖 | **mermaid `graph LR`** | 双链路概览 / 组件横向依赖 |

### mermaid `sequenceDiagram` 固定风格（对齐本项目预览渲染效果）

```mermaid
sequenceDiagram
    participant Client as 前端
    participant XxxController
    participant XxxServiceImpl
    participant XxxMapper or XxxService as XxxService(Redis)
    participant DB

    Note over Client,DB: 阶段1：阶段中文语义说明
    Client->>XxxController: HTTP Method /path（入参）
    XxxController->>XxxServiceImpl: 方法名(dto)
    XxxServiceImpl->>XxxMapper: 数据库操作
    XxxMapper-->>XxxServiceImpl: 返回值
    XxxServiceImpl->>DB: Redis SET/DEL/GET key
    Note right of DB: 旁注（TTL / Lua / 分支说明可用 <br/> 换行）
    XxxServiceImpl-->>XxxController: VO
    XxxController-->>Client: Result<VO>

    Note over Client,DB: 阶段2：...
    alt 条件成立（成功分支）
        A->>B: ...
        B-->>A: ...
    else 条件不成立（失败分支）
        A-->>B: ...
    end
```

核心元素：
- `participant A as 别名` 来用中文显示列头。
- `Note over A,B: 阶段说明` 作为横跨多列的**阶段灰条**。
- `->>` 实线请求，`-->>` 虚线返回。
- `Note right of X` 侧栏小注（可用 `<br/>` 做多行，适合放 Lua 脚本、TTL 说明）。
- `alt / else` 做条件分支（如 refresh_token 轮转成功 / 失败）。

### mermaid `erDiagram` 固定风格

```mermaid
erDiagram
    t_user ||--o{ t_user_role : "1:N"
    t_role ||--o{ t_user_role : "1:N"

    t_user {
        bigint id PK
        varchar username
        tinyint status
    }
    t_user_role {
        bigint id PK
        bigint user_id FK
        bigint role_id FK
    }
```

- 关联语法：`||--o{` 代表 1 对多（另一端 0 或 N）；PK/UK 在字段后标注。
- ER 图在文档中必须保留 mermaid，**不要**转 SVG/ASCII。
- 表前缀（如 `sys_` / `t_`）从项目实际表名扫描得到，不要臆造。

### mermaid `graph LR` 固定风格

```mermaid
graph LR
    subgraph A链路:xxx
        A1[Svc1] -->|@Cacheable| K1[(cache:roles::userId)]
        A2[Svc2] -->|@CacheEvict allEntries| K1
    end
    subgraph B链路:yyy
        B1[Login] -->|save| K2[(auth:refresh:userId)]
        B2[Refresh] -->|rotate Lua CAS| K2
    end
```

- `subgraph X链路:语义` 为每条独立链路打分组框。
- `[( )]` 圆柱语法表示 Redis/缓存等 KV 存储。
- 缓存 key 命名从项目 `Grep` 扫描真实前缀，不要硬编码示例值。

### mermaid `graph TB` 分层架构固定风格（**首选**，对齐 preview 深色 participantBox）

用于自上而下堆叠的「分层架构图」。用 `subgraph` 把每一层独立分组（分组标题 + 节点内标题 + 节点侧描述注解），关键路径（拦截器鉴权 / AOP 切面）用 `linkStyle` 或 `classDef focus` 高亮品牌色。底部分叉（Mapper→MySQL、Component→Redis）用 `A --> B & C` 一次画出两条分支。

```mermaid
graph TB
    %% 样式 classDef：neutral 普通层 / focus 焦点层（品牌色） / storage 存储层（圆角圆柱）
    classDef neutral fill:#F7F7F8,stroke:#71717A,stroke-width:1px,color:#171717,rx:8,ry:8
    classDef focus fill:#F2F7FF,stroke:#4B3FE3,stroke-width:1.6px,color:#1A1759,rx:8,ry:8
    classDef storage fill:#EAFBF8,stroke:#27D2BF,stroke-width:1.2px,color:#0F766E,rx:8,ry:8
    classDef pathFocus stroke:#4B3FE3,stroke-width:2px,fill:none

    subgraph CLIENT[客户端层]
        C1[客户端 / 前端]
    end

    subgraph INTERCEPTOR[拦截器层 — WebMvcConfig 注册 TokenAuthInterceptor]
        I1[Token 解析 & 安全上下文注入]
    end

    subgraph CONTROLLER[Controller 层]
        C2[参数校验 @Valid & 声明式鉴权 @RequiresPermission]
    end

    subgraph AOP[AOP 切面 — 权限注解切面 @Before]
        A1[注解权限校验 401/403]:::focus
    end

    subgraph SERVICE[Service 层]
        S1[业务逻辑 · @Transactional · @Cacheable 缓存]
    end

    subgraph PERSIST[持久化与组件层（底部分叉）]
        direction LR
        P1[Mapper 层 · CRUD + 自定义 SQL]
        P2[Component 组件层 · security / status]
    end

    subgraph STORE[存储中间件（底部分叉）]
        direction LR
        DB1[(MySQL · 业务表)]:::storage
        DB2[(Redis · 缓存/Token)]:::storage
    end

    CLIENT -->|HTTP Authorization Bearer| INTERCEPTOR
    INTERCEPTOR --> C2
    C2 -->|AOP 拦截| A1:::pathFocus
    A1 -->|权限通过| S1:::pathFocus
    S1 --> P1 & P2
    P1 --> DB1
    P2 --> DB2

    class C1,I1,C2,S1 neutral
    class INTERCEPTOR focus
    class AOP focus
```

**核心元素**：
- `classDef` 一次性定义三档视觉（neutral 灰、focus 蓝紫品牌色、storage 青绿存储圆柱 [( )] 语法）。
- `subgraph Name[标题]` 每个独立层一个分组，组内节点再写"本层做什么 + 关键注解"。
- 自上而下用 `-->` 连接，**焦点路径**（拦截器→AOP→Service）`stroke:#4B3FE3,stroke-width:2px` 单独加粗紫色。
- 底部分叉用 `S1 --> P1 & P2` 一行双箭头，对齐 ASCII 视觉但更稳定。

### mermaid `flowchart TD` 拦截链路/流程图固定风格（**首选**）

用于「带判断节点」的拦截链路 / 状态流转 / 鉴权判断分支。比 ASCII 稳定，且支持 `{判断菱形}`、`:::ok / :::fail` 语义配色。

```mermaid
flowchart TD
    A[HTTP 请求] --> B[拦截器注入 SecurityContext]
    B --> C{是否标注 @RequiresLogin?}
    C -->|是| D{userId 存在?}
    C -->|否| E[通过，匿名接口放行]
    D -->|否| F[401 未授权]:::fail
    D -->|是| G{是否标注 @RequiresPermission?}
    G -->|否| H[通过]
    G -->|是| I[权限服务查权限（@Cacheable）]
    I --> J{具备权限?}
    J -->|是| H[通过]:::ok
    J -->|否| K[403 无权限]:::fail

    classDef ok fill:#EAFBF8,stroke:#27D2BF,color:#0F766E
    classDef fail fill:#FEE2E2,stroke:#EF4444,color:#991B1B
```

### ASCII 分层图 / ASCII 流程图（**兜底**，仅当明确要求纯文本时使用）

```
┌──────────────────────────────────┐
│          Controller 层           │  @RequiresPermission / @Valid
└───────────────┬──────────────────┘
                ▼  AOP @Before
┌──────────────────────────────────┐
│          Service 层              │  @Transactional / @Cacheable
└───────┬───────────────┬──────────┘
        ▼               ▼
┌──────────────┐  ┌──────────────────┐
│ Mapper 层    │  │ Component 组件层  │
└──────┬───────┘  └──────────────────┘
       ▼
┌──────────────┐  ┌──────────────────┐
│   MySQL      │  │      Redis       │
└──────────────┘  └──────────────────┘
```

### 何时回退 SVG 独立文件

**只有**当用户**明确要求**"用 SVG"、"ASCII/mermaid 效果不好看换 SVG"时，才走 SVG 方案：
1. SVG 文件放 `docs/images/` 目录，命名 `{文档主题前缀}-{图类型}-diagram.svg`（如 `auth-intercept-flow.svg`）。
2. 图中**严格沿用**项目内已有 SVG 风格（参考 docs/images/ 下任一现有 SVG 文件）：
   - 背景色 `.surface-muted = #EFEFF2`，主色 `.brand = #4B3FE3`，辅色 `.accent = #27D2BF`
   - 字体：`SF Pro Text, PingFang SC, system-ui, sans-serif`
   - `moduleRect` / `participantBox` / `labelBg` / `phaseChip` / `activation` / `connectorPath` / `messagePath` 等 class 沿用原命名与颜色。
3. md 里用相对路径引用：`![图标题](./images/xxx.svg)`。

**默认（用户不特别说明）不走 SVG，优先 mermaid / ASCII。**

## 五、代码定位流程（写文档第一步必做）

写任何文档之前**必须**先定位真实代码，再按"代码→文档"方向推进，避免编造型字段和路径：

1. **定位项目基础包名**：`Glob **/Application.java` 或 `**/*Application.java` → 从启动类的 `package` 行得到 `{package}`（如 `com/example/demo`）。
2. **定位实体/表**：`Glob **/entity/Xxx.java` → 拿表字段 / `@TableName`。
3. **定位业务服务**：`Grep "interface XxxService"` → `Grep "class XxxServiceImpl"` → 找方法签名、注解（@Transactional / @Cacheable / @CacheEvict）。
4. **定位 Controller/接口**：`Glob **/controller/XxxController.java` → 找 HTTP 方法 + 权限注解 + 入参 DTO/VO。
5. **定位 Mapper/XML SQL**：`Glob **/mapper/XxxMapper.xml` → 真实字段与 JOIN 关系。
6. **定位配置/常量**：`Grep "cache:" / "auth:" / TTL 常量` → 用真实 key / TTL，不要硬编码示例值。
7. **定位已有文档**：`LS docs/` → 参考同风格段落组织，沿用已有的表前缀、缓存 key 前缀、命名约定。

定位失败时优先 `Glob` + `Grep` 建立真实调用链，再开始写文档，严禁"猜测模块路径"或"编造缓存 key 前缀"。

## 六、文档文件放置与命名清单（按主题映射）

| 主题 | 推荐文件路径 | 图建议 |
|------|-------------|--------|
| 项目基础架构 / 脚手架 / 技术栈总览 | `docs/project-architecture-design.md` | mermaid graph TB 分层图 + 文本目录树 |
| RBAC 角色权限 / 鉴权 / 访问控制 | `docs/rbac-permission-system-design.md` | mermaid **erDiagram** + mermaid flowchart TD 拦截链路 |
| 双 Token 认证 / JWT / 登录刷新登出 | `docs/dual-token-auth-design.md` | 3 张 mermaid **sequenceDiagram**（登录 / 刷新 alt 分支 / 登出） |
| Redis 缓存 / Spring Cache / refresh_token 轮转 | `docs/redis-cache-design.md` | mermaid **graph LR**（双链路） + mermaid **sequenceDiagram**（4 阶段） |
| 状态变更 / 观察者模式 / 状态机 | `docs/{topic}-observer-design.md` | 按需 mermaid 或 SVG |
| 任务调度 / 定时任务 / Quartz / XXL-Job | `docs/scheduled-job-design.md` | mermaid sequenceDiagram + graph LR（任务流） |
| 消息通知 / MQ / 邮件 / 钉钉 / 飞书 | `docs/message-notification-design.md` | mermaid sequenceDiagram（发送+失败重试链路） |
| 数据字典 / 字典类型 / 下拉框数据源 | `docs/dict-system-design.md` | mermaid **erDiagram** + sequenceDiagram（拉取/校验） + flowchart TD（决策树） |
| 多语言 i18n / Locale / 消息源 | `docs/i18n-design.md` | mermaid sequenceDiagram（Locale 解析链路） + graph LR（动态/静态双链路） |
| 单元测试 / 覆盖率 / Mock | `docs/unit-testing-design.md` | 文本目录树 + 表格（覆盖率矩阵） |
| 模块级单体功能（如标签打印/订单流转） | `docs/{module-english}-design.md` | 按主题从以上 5 种图中组合 |

## 七、交付检查清单（写完文档必过）

- [ ] 头部 4 行元数据齐全（`# 标题 · 设计文档` + `>` 引用块的模块路径 / 作者 / 版本 / 日期）
- [ ] 章节编号 1..N+1 递增，中间无跳号
- [ ] 代码块均来自项目真实文件，字段名、方法名、返回码无编造
- [ ] 表结构有 PK/UK 标记；接口表有 HTTP 方法与路径；方法矩阵有注解 value/key/allEntries 三列
- [ ] 图按"四. 图形式选型规则"选择（ER=erDiagram，时序=sequenceDiagram，分层=graph TB），**用户未明确要求 SVG 时不要生成 SVG**
- [ ] **设计方案类文档**按"三之二. 扩展规范"补齐扩展章节：背景（§1.1）/ 分工边界表 / 各场景使用方式（5-6 个）/ 缓存设计（涉及缓存时）/ 同步一致性机制（跨数据源双份时）/ 核心流程时序图（每个对外接口）/ 取舍建议表
- [ ] 各场景使用方式章节每个场景含：场景描述 + 调用示例（HTTP/JSON） + 代码片段（注明文件路径） + 取舍说明（如有多方案）
- [ ] 跨数据源双份存储（枚举+字典、缓存+DB）时文档内含 `ApplicationRunner` 启动校验代码
- [ ] 涉及缓存的模块含两张表：缓存策略表 + 缓存失效场景表
- [ ] 末尾有"相关文件"表格，列出核心源码/脚本/配置文件的相对路径
- [ ] 文档中无真实密码、内网 IP、Token 等敏感信息
- [ ] 若改/补了已有文档：用 `Grep` 确认替换干净，无旧 SVG `.svg` 残留引用（除非用户明确保留）

## 八、典型输出示例（时序图，供对齐视觉风格）

```mermaid
sequenceDiagram
    participant Client as 前端
    participant XxxController
    participant XxxServiceImpl
    participant XxxMapper
    participant XxxUtil
    participant XxxCacheService

    Note over Client,XxxCacheService: 阶段：核心业务流程示例
    Client->>XxxController: POST /xxx (param1, param2)
    XxxController->>XxxServiceImpl: doBusiness(dto)
    XxxServiceImpl->>XxxMapper: selectByXxx(key)
    XxxMapper-->>XxxServiceImpl: Entity（含校验字段）
    XxxServiceImpl->>XxxServiceImpl: 业务校验
    XxxServiceImpl->>XxxUtil: generate(payload)
    XxxUtil-->>XxxServiceImpl: token/value
    XxxServiceImpl->>XxxCacheService: save(key, value)
    Note right of XxxCacheService: Redis SET cache:{key} TTL=7d
    XxxServiceImpl-->>XxxController: VO
    XxxController-->>Client: Result<VO>
```

## 九、测试用例

本节用可复现的"输入 → 期望输出"形式，对 Skill 的关键行为做回归验证。**每条用例都给出可执行命令**，调用 Skill 后跑一遍就能判断是否通过。

### 用例 T1 · 触发识别（Skill 应被调用）

| 项 | 内容 |
|----|------|
| 输入 | "为 RBAC 权限体系写一份设计文档" |
| 期望 | Skill 被触发；最终产出 `docs/rbac-permission-system-design.md` |
| 验证命令 | `Test-Path docs/rbac-permission-system-design.md` |
| 通过条件 | 返回 `True` |

反例（Skill 不应触发）："给我讲讲 RBAC 是什么" → 纯概念问答，不产出文档。

### 用例 T2 · 头部元数据齐全

| 项 | 内容 |
|----|------|
| 输入 | 用例 T1 产出的文档 |
| 期望 | 前 5 行含：`# XXX · 设计文档` + `> 模块路径：` + `> 作者：` + `> 版本：` + `> 日期：YYYY-MM-DD` |
| 验证命令 | `Get-Content docs/rbac-permission-system-design.md -TotalCount 5` |
| 通过条件 | 5 行同时出现 `· 设计文档`、`模块路径`、`作者`、`版本`、`日期`，且日期匹配 `\d{4}-\d{2}-\d{2}` |

### 用例 T3 · 章节骨架合规

| 项 | 内容 |
|----|------|
| 输入 | 用例 T1 产出的文档 |
| 期望 | 至少含 `## 1. 设计目标` / `## N. 扩展约定` / `## N+1. 相关文件`，且编号连续无跳号 |
| 验证命令 | `Select-String -Path docs/rbac-permission-system-design.md -Pattern '^## \d+\.'` |
| 通过条件 | 命中行数 ≥ 5；行号从 1 递增；最后一条为"相关文件" |

### 用例 T4 · 图选型：分层架构用 mermaid graph TB，不用 ASCII

| 项 | 内容 |
|----|------|
| 输入 | "写一份项目架构设计文档" |
| 期望 | 文档中出现 ``` ```mermaid\ngraph TB\n``` 代码块；**不出现** ASCII 框线 `┌──` / `│  ` / `└──` 作为分层图主体 |
| 验证命令 A（正面） | `Select-String -Path docs/project-architecture-design.md -Pattern 'graph TB'` |
| 验证命令 B（反面） | `Select-String -Path docs/project-architecture-design.md -Pattern '┌──┐│└──'` |
| 通过条件 | A 命中 ≥ 1；B 命中 = 0 |

### 用例 T5 · 图选型：ER 关系用 mermaid erDiagram

| 项 | 内容 |
|----|------|
| 输入 | 用例 T1 产出的 RBAC 文档 |
| 期望 | 含 ``` ```mermaid\nerDiagram\n``` 代码块；字段标注 `PK` / `FK` |
| 验证命令 | `Select-String -Path docs/rbac-permission-system-design.md -Pattern 'erDiagram'` |
| 通过条件 | 命中 ≥ 1，且同文档内出现 ` PK` 与 ` FK` 标记 |

### 用例 T6 · 代码定位先行（不得编造型字段）

| 项 | 内容 |
|----|------|
| 输入 | 用例 T1 产出的 RBAC 文档 |
| 期望 | 文档内出现的 Java 类名 / 表名 / 缓存 key 前缀全部来自项目源码，无 `XxxService` / `t_user` 这类占位符残留 |
| 验证命令 A | `Select-String -Path docs/rbac-permission-system-design.md -Pattern 'XxxService|XxxController|t_user|t_role|com\.yourcompany'` |
| 通过条件 | A 命中 = 0（这些是 Skill 模板里的占位符，真实文档里不应残留） |

### 用例 T7 · 末尾必含"相关文件"表

| 项 | 内容 |
|----|------|
| 输入 | 任意主题的设计文档 |
| 期望 | 最后一章为"相关文件"，含两列表格 `类别 | 文件路径`，路径形如 `src/main/java/...` |
| 验证命令 | `Get-Content docs/rbac-permission-system-design.md -Tail 30` |
| 通过条件 | 末尾 30 行内出现 `相关文件` 标题 + 表头分隔线 `|---|---|` + 至少 1 行 `src/main/java/` 路径 |

### 用例 T8 · 修订已有文档时清理旧 SVG 引用

| 项 | 内容 |
|----|------|
| 输入 | "把 docs/dual-token-auth-design.md 第 5 章的图改成 mermaid"（原为 SVG） |
| 期望 | 修订后文档内 `.svg` 引用 = 0 |
| 验证命令 | `Select-String -Path docs/dual-token-auth-design.md -Pattern '\.svg'` |
| 通过条件 | 命中 = 0 |

### 用例 T9 · Git 提交备注沿用项目约定

| 项 | 内容 |
|----|------|
| 输入 | "把刚才的文档提交一下" |
| 期望 | commit message 遵循项目已有 history 的 type + scope 约定（不要硬编码 `z  xxx`） |
| 验证命令 | `git log -1 --pretty=%s` |
| 通过条件 | 与 `git log -5 --pretty=%s` 的 type 前缀（`feat`/`fix`/`docs`/`refactor`/...）一致 |

### 用例 T10 · 敏感信息脱敏

| 项 | 内容 |
|----|------|
| 输入 | 任意主题的设计文档 |
| 期望 | 文档中不出现真实密码、内网 IP、真实 Token 值 |
| 验证命令 | `Select-String -Path docs/*.md -Pattern 'password\s*[:=]\s*\S{4,}|10\.0\.|192\.168\.|Bearer\s+eyJ'` |
| 通过条件 | 命中 = 0 |

### 端到端回归脚本

把上述用例串成一个脚本，放在 `scripts/test-design-doc-skill.ps1`（如该路径不存在则按需创建），一次性跑完输出 PASS/FAIL 报告：

```powershell
# scripts/test-design-doc-skill.ps1
# 回归 java-design-doc Skill 的 10 条用例
$cases = @(
    @{ Id='T1'; Script={ Test-Path docs/rbac-permission-system-design.md } ; Expect=$true }
    @{ Id='T2'; Script={ (Get-Content docs/rbac-permission-system-design.md -TotalCount 5) -match '设计文档|模块路径|作者|版本|日期|\d{4}-\d{2}-\d{2}' } ; Expect=$true }
    @{ Id='T4A'; Script={ (Select-String -Path docs/project-architecture-design.md -Pattern 'graph TB').Count -ge 1 }; Expect=$true }
    @{ Id='T4B'; Script={ (Select-String -Path docs/project-architecture-design.md -Pattern '┌──┐│└──').Count }; Expect=0 }
    @{ Id='T6';  Script={ (Select-String -Path docs/rbac-permission-system-design.md -Pattern 'XxxService|XxxController|t_user|t_role|com\.yourcompany').Count }; Expect=0 }
    @{ Id='T8';  Script={ (Select-String -Path docs/dual-token-auth-design.md -Pattern '\.svg').Count }; Expect=0 }
    @{ Id='T10'; Script={ (Select-String -Path docs/*.md -Pattern 'password\s*[:=]\s*\S{4,}|10\.0\.|192\.168\.|Bearer\s+eyJ').Count }; Expect=0 }
)
foreach ($c in $cases) {
    $actual = & $c.Script
    $pass = if ($actual -eq $c.Expect) { 'PASS' } else { 'FAIL' }
    Write-Host ("{0}  {1}  (got={2}, expect={3})" -f $pass, $c.Id, $actual, $c.Expect)
}
```

**使用方式**：先按用例 T1 / "写 RBAC 设计文档" 触发一次 Skill 产出文档，然后在项目根执行：

```powershell
./scripts/test-design-doc-skill.ps1
```

期望输出：

```
PASS  T1   (got=True, expect=True)
PASS  T2   (got=True, expect=True)
PASS  T4A  (got=1,  expect=True)
PASS  T4B  (got=0,  expect=0)
PASS  T6   (got=0,  expect=0)
PASS  T8   (got=0,  expect=0)
PASS  T10  (got=0,  expect=0)
```

任何一行 FAIL 都说明 Skill 产出偏离规范，应回到对应章节修正。
