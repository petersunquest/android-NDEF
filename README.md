# android-NDEF — Beamio Android POS / NFC Terminal

面向商户的 **Beamio Android 终端应用**：在 **Base** 等链上与 **Beamio User Card（ERC-1155 商户卡）** 协同，通过 **NFC（NTAG / SUN）**、**二维码** 与 **Beamio API** 完成充值、收款与卡务相关操作。本仓库为 **Kotlin + Jetpack Compose** 客户端，与链上合约、后端集群配合构成 Beamio 支付与会员体系的一部分。

---

## Beamio 系统在做什么（开源视角）

Beamio 是一套将 **链上商户卡、账户抽象（AA）、USDC 结算、NFC/二维码交互** 串起来的协议与产品组合，典型能力包括：

| 层级 | 说明 |
|------|------|
| **链上** | 商户卡合约（如 `BeamioUserCard`）、工厂与模块（治理、兑换、统计等）、基础设施卡与权限模型。 |
| **后端** | Beamio API（Cluster / Master 等）：预检、签名验证、与链上写操作编排；metadata 等走约定好的 HTTPS 端点。 |
| **客户端** | 本应用属于 **商户侧 POS**：设备内生成/保管用于 `ExecuteForAdmin` 等场景的 **EIP-712 签名密钥**，与面板展示的 **Panel ID（钱包地址）** 一致；通过 NFC 读卡、扫码与 HTTP API 完成业务流程。 |

本仓库 **不包含** 完整服务端与全部合约源码；若你在更大的 monorepo 中，通常还会在其它目录看到 `x402sdk`、`BeamioUserCard` 合约、Web 前端等。单独克隆本仓库时，需要自行配置可用的 **Beamio API 基地址** 与链上环境（见应用内常量或构建配置）。

---

## 本应用能力概览

- **NFC**：读写 NTAG、支持符合约定的 **SUN** 参数；用于识别卡与用户资产相关流程。  
- **充值（Top-up）**：调用 Beamio API（如 `nfcTopupPrepare` / `nfcTopup`），使用设备内商户密钥完成 **admin topup** 路径所需的 EIP-712 签名。  
- **二维码**：扫码触发基于 `beamioTag` / `wallet` 等参数的充值或其它深度链接流程。  
- **链交互**：依赖 **Web3j** 等与 **Base** 上合约/地址配置交互（具体常量以源码为准）。  
- **本地密钥**：通过 `WalletStorageManager` 等组件在设备上安全存储 POS 私钥（请勿将生产密钥提交到 git）。

---

## 环境要求

- **Android Studio**（推荐最新稳定版）或兼容的 Gradle/Android Gradle Plugin 环境  
- **JDK 17**（与 `app/build.gradle.kts` 中 `jvmTarget` 一致）  
- 真机调试 **NFC** 功能（模拟器通常无法满足 NTAG 需求）  
- **Camera** 为可选硬件（扫码相关）

---

## 构建说明

### Debug

```bash
./gradlew assembleDebug
```

输出 APK 位于：`app/build/outputs/apk/debug/`。

### Release（正式签名）

Release 构建配置为：若存在同级目录 **`../Android-init-NDEF/keystore.properties`**，则使用其中的 **`storeFile`**（相对该目录的 keystore 路径）、别名与密码进行 **v1/v2 签名**。

1. 在 **`Android-init-NDEF`** 目录放置 `keystore.properties` 与对应 `.keystore`（勿提交仓库；可参考该目录下的 `keystore.properties.example`）。  
2. 执行：

```bash
./gradlew assembleRelease
```

输出：`app/build/outputs/apk/release/app-release.apk`。

若仅有本仓库、没有 `Android-init-NDEF`，请在 `app/build.gradle.kts` 中改为本地 `keystore.properties` 路径，或使用 Android Studio **Generate Signed Bundle / APK** 自行指定密钥库。

---

## 安全与合规提示

- **切勿**将 `keystore`、`keystore.properties` 或生产 API 密钥推送到公开仓库。  
- POS 私钥仅应存在于受控设备；丢失密钥将导致无法以原商户身份完成链上授权类操作。  
- 生产环境请使用固定版本的依赖并通过官方渠道分发 APK/AAB。

---

## 应用标识

- **Application ID**：`com.beamio.android_ntag`  
- **命名空间**：`com.beamio.android_ntag`  
- 资源中的 **`app_name`** 当前为 `Android-NTAG`（显示名可在 `res/values/strings.xml` 中调整）。

---

## 开源许可

Beamio 相关代码在父仓库中通常以 **MIT License** 授权（以仓库根目录 **`LICENSE`** 文件为准）。使用或分发时请保留许可证与版权声明。

---

## 相关链接（随部署更新）

- 产品 / 文档入口：以你们对外公布的官网或文档站为准。  
- 链上验证与合约地址：以当前部署的 Factory / 模块地址及浏览器展示为准。

如有问题或希望贡献代码，请通过仓库 **Issues / Pull Requests** 联系维护者。
