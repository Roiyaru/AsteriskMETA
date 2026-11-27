# PROGRESS.md

最后更新：2026-11-28 03:0x（本地时间）

## 当前状态：fork 已重置为纯上游 + 单条测试构建提交；parity 内核构建中

### 仓库与分支
- 本地：`C:/Users/Roiyaru/WorkBuddy/2026-11-28-00-31-19/asteriskmeta`
- 远端：`git@github.com:Roiyaru/AsteriskMETA.git`（`origin`）
- 上游：`https://github.com/Asterisk4Magisk/AsteriskMETA.git`（`upstream`）
- `main` = `upstream/main`（`66b1ecf`）+ 一条提交，HEAD `632e08d`（旧为 `0f84519`）
- `meow-kernel-test`：与 `main` 同步，专用于触发内核测试构建（工作流只在 push 到该分支或手动时启动）
- `archive/pre-upstream-reset`（`9bdf4a8`）：旧 fork 历史（含 DNS 监视模块 40 提交），仅存档，不参与构建

### fork 相对上游的全部差异（仅 3 项）
1. `settings.gradle.kts`：支持 `-PkernelRepoDir` 覆盖 `com.github.Asterisk4Magisk:libclash-rs` 的 ivy 仓库位置。
2. `.github/workflows/build-meow-kernel-test.yml`：在 CI 内用 AndroidLibClashLite 脚本现场编译 meow 内核 AAR → 本地 ivy → 打 arm64 release APK → 发布 `meow-test-N` 预发布。
3. `signing/`：测试签名材料（勿另存副本、勿外传）。

### 内核基线约束（关键，改动前必读）
- `AndroidLibClashLite` 的 11 个 Android 补丁（tproxy / 系统 DNS / DNS 事件，约 3.4k 行）只适用于 meow **0.30.0**（commit `8e4e9484`）。
- meow main 上的 tproxy IPv6 提交（`8d1c36cf`）使这些补丁失效 → main 基线无法直接产 Android AAR。
- 因此内核代码分两条线：
  - `fix/provider-filter-lookaround`（基于 main，`7bb067c5`）：可直接上游化。
  - `test/provider-parity-0.30`（基于 0.30.0，`3c343c67`）：三个修复 cherry-pick 到 AAR 可用基线，供 APK 构建。

### 本轮修复内容（对应 meow-rs 分支）
| 分支 | 内容 |
| --- | --- |
| `fix/provider-filter-lookaround` | `filter`/`exclude-filter` 改用 fancy-regex，支持 mihomo 的负向前瞻；评估失败记日志按不匹配处理；含两个负向前瞻测试与文档 |
| `feat/provider-share-link-payload` | provider 载荷支持 base64 任意字母表 + 分享链接（ss/vmess/vless/trojan/hysteria2/anytls）转节点 map；YAML 解析失败后回退 |
| `feat/provider-override` | provider `override` 字段集（udp/tfo/mptcp/skip-cert-verify/up/down/name-cert-verify/interface-name/ip-version/routing-mark）+ `additional-prefix`/`additional-suffix`；顺序为过滤 → 字段 → 前后缀 |
| `test/provider-parity-0.30` | 上述三项 cherry-pick 到 `8e4e9484`，供 AAR 构建（`MEOW_REVISION` 指向它） |

### 本次构建
- `MEOW_REVISION` = `37d32c7919ad85527b9e8c87923fd9ce4d23d75e`，`KERNEL_VERSION` = `meow-parity-0.30-1`
- 触发：`git push origin main:meow-kernel-test`
- 结果：run `46056760595` **success**（约 12 分钟）→ release `meow-test-9`
- 产物：`app-arm64-v8a-release-unsigned-signed.apk`，28,148,709 B，
  sha256 `63b7eb8187401106984b4f48c3a0cb36bda29c26da7f4998a94ded0955ca19d2`
- 校验：包名 `org.asterisk.zcc.ameta.dev`，versionCode 628，versionName `1.3.1-merge`，
  签名 `CN=AsteriskMETA HevFix Test`，`lib/arm64-v8a/librsjni.so` 27.4 MB；
  二进制内含 `fancy-regex-0.16.2` 与 `no share link could be parsed` 字符串，
  确认三项修复都进了内核。

#### 失败记录（已修，勿重复踩）
1. run `46053584358` / `46054734547` / `46055649424` 连续失败：真实原因是 `provider_payload.rs:395`
   在 ws 分支把 `host` move 进 headers，TLS 块又 `host.clone()` → E0382。
   修复提交 `f9ab085c`（`feat/provider-share-link-payload`）与 `37d32c79`（parity，cherry-pick）。
   同时修正 hysteria2 测试断言（链接给的是 `cfn`，断言写了 `xyz`）。
2. 日志一度完全不可见：`softprops/action-gh-release@v2` 上传固定 tag 的 release 资产，
   且 `build.log` 不存在时报 `size must be >= 1` 直接失败。现已改为
   `actions/upload-artifact@v4` + 失败时把日志尾部镜像到 `ci-logs` 分支（匿名可 fetch）。

### 踩坑备忘
- 本地无 cmake/clang，Gradle 发行版下载被墙 → **只能云端构建**。
- CI 失败日志：工作流的 `Upload build logs on failure` 会写 `$GITHUB_STEP_SUMMARY` 并尝试传 release `ci-logs`（固定 tag，资产不覆盖，可能是旧日志）；优先读 job summary。
- GitHub API 匿名 60 次/小时；轮询间隔 ≥45s。外部请求串行、间隔 2–3s。
- shell 遍历提交列表用 `git rev-list`，不要用 `git log --pretty=format:'%H'`（末行无换行）。
- 工作流触发条件仅 `workflow_dispatch` + push `meow-kernel-test`；改完 `main` 必须再推一次该分支才会构建。

### 待办
1. 等 run `46053584358` 出包 → 校验 arm64、签名、`libclash-rs.aar` 版本。
2. 真机验证：配置加载成功、代理页分组齐全、Car 订阅节点数 > 0、信息类节点被 filter 掉。
3. 分别向 meow-rs 上游提交 PR（`fix/provider-filter-lookaround` 与 `feat/provider-*` 需先 rebase 到最新 main）。
