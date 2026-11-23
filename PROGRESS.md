# PROGRESS.md

## 当前状态：DNS 模块已完成并构建通过（2026-11-23）

### 仓库
- 本地：`C:/Users/Roiyaru/WorkBuddy/2026-11-23-23-21-45/repo`
- 远端：`git@github.com:Roiyaru/AsteriskMETA.git`（fork）
- 上游：`git@github.com:Asterisk4Magisk/AsteriskMETA.git`（remote `upstream`）
- 分支：`main`、`feat/dns-monitor-tab`（均已推送，HEAD `1e24205`）

### 已完成
1. **合并上游 main**（13 个提交，8 个冲突文件全部以上游为准；保留 fork 的签名材料、versionCode 修复、build-test CI、source patch）。
2. **DNS 模块**：在「规则」标签后新增「DNS」标签，记录 DNS 请求（域名、类型、解析结果、上游、耗时、缓存标记、代理/直连来源），带类型/缓存/NXDOMAIN/来源筛选与搜索。
3. **GitHub 构建验证通过**：run `45231891438`，release `test-62`。
4. **测试包只构建 arm64-v8a**：`buildAbis` / `buildUniversalApk` gradle 属性控制架构拆分；`build-test.yml` 只跑 `:app:assembleRelease -PbuildAbis=arm64-v8a -PbuildUniversalApk=false`，已移除 debug 包步骤；签名步骤校验只允许 arm64 且产物恰好 1 个。`release.yml` 不传这些属性，正式发布仍出全架构。
5. **发布说明按本次改动自动生成**：新增 `Generate release notes` 步骤，基准为上一次成功构建的提交；按每个提交触及的路径分流为「应用改动」与「构建与文档」两组；附全量 `--stat` 文件统计；不再沿用旧版硬编码说明。

### 关键实现要点（后续修改前必读）
- **versionCode 规则**：跟随上游——`git rev-list --count HEAD`。我方 fork 已合并上游完整历史，因此计数天然高于上游（上游 608 → 我方 625+）。`build-test.yml` 用 `fetch-depth: 0` 取得完整历史；**不要再用 `500 + GITHUB_RUN_NUMBER`**，那会让版本号远落后上游。
- **架构**：源码保留全架构（`ProjectConfig.SUPPORTED_ANDROID_ABIS`），构建通过 gradle 属性 `buildAbis` / `buildUniversalApk` 收窄。test 与 release 流程都只构建 arm64-v8a。
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
- GitHub REST API 匿名调用有速率限制；查询构建状态可改用 WebFetch 打开 Actions 页面。
- shell 脚本里遍历提交列表要用 `git rev-list`（每行有换行），**不要用 `git log --pretty=format:'%H'`**（末行无换行，`while read` 读不到，导致列表为空）。
- `List<T>` 没有 `entries` 属性（那是枚举类的），遍历要用 `forEach`；跨文件引用顶层 `val` 需补 import。
- CI 失败日志若上传到固定 tag 的 release，资产不会覆盖，拿到的是旧日志；改用 `$GITHUB_STEP_SUMMARY` 写 job summary（随 run 走，不外发）。
- `release.yml`（Build Release APKs）历史上一直失败，原因是 `Validate signing secrets` 缺密钥，与代码无关；验证请用 `build-test.yml`。

### 下一步（若需继续）
- 真机验证 DNS 标签页：需要 core 的 debug 日志流有输出，若用户日志级别非 debug 也不影响（订阅独立）。
- 可能的增强：把 DNS 记录的「代理/直连」判定与 core 实际路由进一步对齐（当前按 upstream 是否属于 `proxy-server-nameserver` / `nameserver`+`fallback` 判定）。
