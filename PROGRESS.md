# PROGRESS.md

## 当前状态：DNS 模块已完成并构建通过（2026-11-23）

### 仓库
- 本地：`C:/Users/Roiyaru/WorkBuddy/2026-11-23-23-21-45/repo`
- 远端：`git@github.com:Roiyaru/AsteriskMETA.git`（fork）
- 上游：`git@github.com:Asterisk4Magisk/AsteriskMETA.git`（remote `upstream`）
- 分支：`main`、`feat/dns-monitor-tab`（均已推送，HEAD `1e24205`）

### 已完成
1. **合并上游 main**（13 个提交，8 个冲突文件全部以上游为准；保留 fork 的签名材料、versionCode 修复、build-test CI、source patch）。
2. **DNS 模块**：在「规则」标签后新增「DNS」标签，记录 DNS 请求（域名、类型、解析结果、上游、耗时、缓存标记），带类型/缓存/NXDOMAIN 筛选与搜索。
3. **GitHub 构建验证通过**：run `45220476615`，release `test-56`。

### 关键实现要点（后续修改前必读）
- **DNS 数据来源**：mihomo 无 DNS 历史接口，改为订阅 `GET /logs?level=debug` 解析 `[DNS]` 行。mihomo 的 `log.Debugln` 无条件进订阅通道，与全局 log-level 无关，因此订阅 debug 即可拿到。
- **日志行格式**：
  - `[DNS] resolve <domain> <TYPE> from <upstream>`
  - `[DNS] <domain> --> [<ips>] <TYPE> from <upstream>`
  - `[DNS] cache hit <domain> --> [<ips>] <TYPE>, expire at <time>`（无 from 段）
- **fork 已废弃的旧规则实现**（上游已重写为 `engine/proxy/RuntimeRouteRule.kt` + `connections/RulesMonitorContent.kt`）：`MihomoRuleRuntimeItem`、`MihomoRulesState`、`parseMihomoRulesJson`、旧 `resolveRuleOutboundChain(policyName)`、`MihomoRuleOutbound`。
- **不要重新引入**旧规则类型，否则会再次触发 `resolveRuleOutboundChain` 重载冲突。

### 踩坑备忘
- 本地 Gradle 发行版下载被墙，**无法本地编译**；必须用 GitHub Actions 构建。
- CI 失败日志读取方式：工作流 `Upload build logs on failure` 会把 `build.log` 发到 release `ci-logs` 标签，地址 `https://github.com/Roiyaru/AsteriskMETA/releases/download/ci-logs/build.log`。直接 curl 会被墙，**用 WebFetch 读取**。
- `release.yml`（Build Release APKs）历史上一直失败，原因是 `Validate signing secrets` 缺密钥，与代码无关；验证请用 `build-test.yml`。

### 下一步（若需继续）
- 真机验证 DNS 标签页：需要 core 的 debug 日志流有输出，若用户日志级别非 debug 也不影响（订阅独立）。
- 可能的增强：DNS 记录来源标注「代理/直连」（需读 core 配置里的 nameserver 与 proxy-server-nameserver 对照 upstream 字段）。
