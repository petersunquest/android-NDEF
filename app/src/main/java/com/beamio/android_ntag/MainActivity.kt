package com.beamio.android_ntag

import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.beamio.android_ntag.ui.theme.AndroidNTAGTheme

class MainActivity : ComponentActivity() {
    private companion object {
        const val HARD_CODED_KEY0 = "894FE8DAD6E206F142D107D11805579A"
        const val HARD_CODED_MASTER_KEY = "3DA90A7D29A5797A16ED53D91A49B803"
        const val SUN_BASE_URL = "https://api.beamio.app/api/sun"
    }

    private var uidText by mutableStateOf("未读取")
    private var readCheckText by mutableStateOf("未检测初始化状态")
    private var readUrlText by mutableStateOf("")
    private var readParamText by mutableStateOf("")
    private var lastCounter by mutableStateOf(0)
    private var lastCounterInput by mutableStateOf("0")
    private var initText by mutableStateOf("未初始化")
    private var topupAmount by mutableStateOf("")
    private var paymentAmount by mutableStateOf("")
    private var readArmed by mutableStateOf(false)
    private var initArmed by mutableStateOf(false)
    private var initTemplateArmed by mutableStateOf(false)
    private var showInitFlowScreen by mutableStateOf(false)
    private var showInitReconfirm by mutableStateOf(false)
    private var initReconfirmAllowContinue by mutableStateOf(false)
    private var initReconfirmText by mutableStateOf("")
    private var pendingInitTag: Tag? = null
    private var pendingInitRequiresKey0Reset: Boolean = false
    private var pendingInitUseDefaultKey0: Boolean = false
    private var nfcAdapter: NfcAdapter? = null

    private fun appendInitStep(step: String) {
        initText = if (initText.isBlank()) step else "$initText\n$step"
    }

    private fun initFail(step: String, reason: String) {
        appendInitStep("❌ $step 失败: $reason")
        initArmed = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        setContent {
            AndroidNTAGTheme {
                BeamioWebView(modifier = Modifier.fillMaxSize())
            }
        }
    }

    private fun startReadUid() {
        if (nfcAdapter == null) {
            uidText = "设备不支持 NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            uidText = "请先开启 NFC"
            return
        }
        readArmed = true
        initArmed = false
        initTemplateArmed = false
        uidText = "请贴卡读取 UID..."
    }

    private fun startInitCard() {
        if (nfcAdapter == null) {
            initText = "设备不支持 NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            initText = "请先开启 NFC"
            return
        }
        initArmed = true
        initTemplateArmed = false
        readArmed = false
        showInitFlowScreen = true
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        pendingInitTag = null
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = false
        initText = "⏳ [0/11] Init 已启动，请贴卡..."
    }

    private fun startInitTemplateOnly() {
        if (nfcAdapter == null) {
            initText = "设备不支持 NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            initText = "请先开启 NFC"
            return
        }
        initArmed = false
        initTemplateArmed = true
        readArmed = false
        showInitFlowScreen = true
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        pendingInitTag = null
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = false
        initText = "⏳ [T 0/2] 仅写模板模式已启动，请贴卡..."
    }

    private fun continueReInit() {
        val tag = pendingInitTag
        if (tag == null) {
            initText = "❌ 未找到待确认的卡，请重新点击 init"
            showInitReconfirm = false
            initReconfirmAllowContinue = false
            return
        }
        if (!initReconfirmAllowContinue) {
            initText = "❌ 当前卡密钥不匹配，无法继续 init"
            return
        }
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        initText = "⏳ 已确认继续，开始重新 init..."
        handleInitTag(
            tag,
            allowKey2Fallback = !pendingInitRequiresKey0Reset && !pendingInitUseDefaultKey0,
            useDefaultKey0 = pendingInitUseDefaultKey0
        )
    }

    private fun cancelInitFlow() {
        initArmed = false
        initTemplateArmed = false
        readArmed = false
        showInitFlowScreen = false
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        pendingInitTag = null
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = false
    }

    override fun onResume() {
        super.onResume()
        val adapter = nfcAdapter ?: return
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        // 抢占前台 NFC 分发，避免系统默认读取页面接管。
        adapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        nfcAdapter?.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action ?: return
        if (action != NfcAdapter.ACTION_TAG_DISCOVERED &&
            action != NfcAdapter.ACTION_TECH_DISCOVERED &&
            action != NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            return
        }

        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: run {
            uidText = "读取失败：未获取到 Tag"
            return
        }

        if (!readArmed && !initArmed && !initTemplateArmed) {
            uidText = "已拦截系统读取，请先点击 read/init"
            return
        }

        if (initTemplateArmed) {
            handleTemplateOnlyTag(tag)
            return
        }

        if (initArmed) {
            handleInitFlowTag(tag)
            return
        }

        if (readArmed) {
            val uid = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: "读取失败"
            uidText = uid
            val check = checkInitStatusByNdef(tag)
            val statusWithCounter = when (val counter = check.counter) {
                null -> check.status
                else -> {
                    val prev = lastCounter
                    if (counter > prev) {
                        lastCounter = counter
                        lastCounterInput = counter.toString()
                        "${check.status}\n✅ counter 前进: $prev -> $counter"
                    } else {
                        "${check.status}\n⚠️ counter 未前进: 当前=$counter, lastCounter=$prev"
                    }
                }
            }
            readCheckText = statusWithCounter
            readUrlText = check.url ?: ""
            readParamText = check.paramText ?: ""
            readArmed = false
        }
    }

    private data class InitInspectResult(val isInitCard: Boolean, val url: String?)
    private data class InitKeyProbeResult(
        val key0AppMatch: Boolean,
        val key0DefaultMatch: Boolean,
        val masterMatch: Boolean
    )

    private fun handleInitFlowTag(tag: Tag) {
        appendInitStep("⏳ 检测到卡，正在检查是否已初始化...")
        val inspect = inspectCardInitState(tag)
        if (inspect.isInitCard) {
            pendingInitTag = tag
            pendingInitUseDefaultKey0 = false
            pendingInitRequiresKey0Reset = false
            showInitReconfirm = true
            appendInitStep("⏳ 该卡已初始化，正在探测 key0/masterKey 匹配...")
            val probe = probeInitKeyMatch(tag)
            val allowContinue: Boolean
            val modeText: String
            if (probe.key0DefaultMatch) {
                // 出厂空白卡路径：仅凭默认 key0 可继续
                allowContinue = true
                pendingInitUseDefaultKey0 = true
                pendingInitRequiresKey0Reset = false
                modeText = "检测到 key0=0x00（出厂空白卡路径）"
            } else if (probe.key0AppMatch && probe.masterMatch) {
                // 已配置路径：app key0 + app masterKey
                allowContinue = true
                pendingInitUseDefaultKey0 = false
                pendingInitRequiresKey0Reset = true
                modeText = "检测到 key0=AppKey0 且 masterKey 匹配（重置路径）"
            } else {
                allowContinue = false
                pendingInitUseDefaultKey0 = false
                pendingInitRequiresKey0Reset = false
                modeText = "密钥不匹配，无法继续"
            }
            initReconfirmAllowContinue = allowContinue
            initReconfirmText = buildString {
                append("该卡已经 init。\n")
                append("key0(app)=${if (probe.key0AppMatch) "匹配" else "不匹配"}，")
                append("key0(0x00)=${if (probe.key0DefaultMatch) "匹配" else "不匹配"}，")
                append("masterKey(app)=${if (probe.masterMatch) "匹配" else "不匹配"}\n")
                append("$modeText\n")
                if (allowContinue) {
                    append("是否继续？\n")
                } else {
                    append("该卡无法完成 init（key0/masterKey 不匹配），仅可取消。\n")
                }
                if (!inspect.url.isNullOrBlank()) append(inspect.url)
            }
            appendInitStep(
                if (allowContinue) "⚠️ 已初始化卡：可继续或取消"
                else "❌ 已初始化卡：密钥不匹配，仅可取消"
            )
            return
        }
        // 未初始化/空卡：直接按出厂 key0=0x00 路径执行，避免前置探测误判拦截。
        appendInitStep("✅ 未检测到已初始化数据，按出厂 key0=0x00 路径执行 init")
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = true
        handleInitTag(tag, allowKey2Fallback = false, useDefaultKey0 = true)
    }

    private fun probeInitKeyMatch(tag: Tag): InitKeyProbeResult {
        val uid = tag.id ?: return InitKeyProbeResult(false, false, false)
        val key0App = hexToBytes(HARD_CODED_KEY0) ?: return InitKeyProbeResult(false, false, false)
        val master = hexToBytes(HARD_CODED_MASTER_KEY) ?: return InitKeyProbeResult(false, false, false)
        val key0Default = ByteArray(16)

        fun auth(keyNo: Int, key: ByteArray): Boolean {
            var iso: IsoDep? = null
            return try {
                iso = IsoDep.get(tag) ?: return false
                if (iso.isConnected) {
                    try { iso.close() } catch (_: Exception) {}
                }
                iso.connect()
                iso.timeout = 3000
                val ev2 = Ntag424Ev2(iso)
                ev2.authenticateEV2First(keyNo, key)
                true
            } catch (_: Exception) {
                false
            } finally {
                try { iso?.close() } catch (_: Exception) {}
            }
        }

        val key0AppMatch = auth(0x00, key0App)
        val key0DefaultMatch = auth(0x00, key0Default)
        val sdmKey = try {
            val domain = "BEAMIO_SDM_V1".toByteArray(Charsets.UTF_8)
            val msg = domain + byteArrayOf(0x00) + uid
            Crypto.aesCmac(master, msg).copyOfRange(0, 16)
        } catch (_: Exception) {
            ByteArray(0)
        }
        val masterMatch = sdmKey.size == 16 && auth(0x02, sdmKey)
        return InitKeyProbeResult(key0AppMatch, key0DefaultMatch, masterMatch)
    }

    private fun inspectCardInitState(tag: Tag): InitInspectResult {
        val ndef = Ndef.get(tag) ?: return InitInspectResult(false, null)
        return try {
            ndef.connect()
            val msg = ndef.cachedNdefMessage ?: ndef.ndefMessage
            ndef.close()
            if (msg == null || msg.records.isEmpty()) {
                return InitInspectResult(false, null)
            }
            val url = msg.records.firstNotNullOfOrNull { it.toUri()?.toString() }
            if (url.isNullOrBlank()) return InitInspectResult(false, null)
            if (!isLikelyInitUrl(url)) return InitInspectResult(false, null)
            InitInspectResult(true, url)
        } catch (_: Exception) {
            InitInspectResult(false, null)
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    private data class ReadCheckResult(
        val status: String,
        val url: String?,
        val counter: Int? = null,
        val paramText: String? = null
    )

    private fun checkInitStatusByNdef(tag: Tag): ReadCheckResult {
        val ndef = Ndef.get(tag) ?: return ReadCheckResult("❌ 未按 init 初始化：标签不支持 NDEF", null)
        return try {
            ndef.connect()
            val msg = ndef.cachedNdefMessage ?: ndef.ndefMessage
            ndef.close()

            if (msg == null || msg.records.isEmpty()) {
                return ReadCheckResult("❌ 未按 init 初始化：NDEF 为空", null)
            }

            val url = msg.records.firstNotNullOfOrNull { record ->
                record.toUri()?.toString()
            } ?: return ReadCheckResult("❌ 未按 init 初始化：未找到 URL Record", null)

            if (!isInitUrl(url)) {
                return ReadCheckResult("❌ 未按 init 初始化：URL 不符合规则", null)
            }

            val uri = Uri.parse(url)
            val uid = uri.getQueryParameter("uid").orEmpty()
            val e = uri.getQueryParameter("e").orEmpty()
            val c = uri.getQueryParameter("c").orEmpty()
            val m = uri.getQueryParameter("m").orEmpty()
            val counter = c.toIntOrNull(16)
            val paramText = "uid=$uid\ne=$e\nc=$c (counter=${counter ?: "n/a"})\nm=$m"
            val isAllZero = e.all { it == '0' } && c.all { it == '0' } && m.all { it == '0' }

            if (isAllZero) {
                ReadCheckResult("⚠️ 模板已写入（e/c/m 全0），SUN 动态未启用", url, counter, paramText)
            } else {
                ReadCheckResult("✅ 已完成 init 且检测到 SUN 动态数据", url, counter, paramText)
            }
        } catch (e: Exception) {
            ReadCheckResult("❌ 初始化状态检测失败：${e.message ?: "unknown error"}", null)
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    private fun applyLastCounter() {
        val next = lastCounterInput.trim().toIntOrNull()
        if (next == null || next < 0) {
            readCheckText = "❌ lastCounter 必须是非负整数"
            return
        }
        lastCounter = next
        readCheckText = "✅ 已设置 lastCounter=$next"
    }

    private fun copyUrlToClipboard() {
        if (readUrlText.isBlank()) return
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("init-url", readUrlText))
        readCheckText = "✅ URL 已复制到剪贴板"
    }

    private fun isInitUrl(url: String): Boolean {
        if (!url.startsWith(SUN_BASE_URL)) return false
        val uri = Uri.parse(url)
        val uid = uri.getQueryParameter("uid") ?: return false
        val e = uri.getQueryParameter("e") ?: return false
        val c = uri.getQueryParameter("c") ?: return false
        val m = uri.getQueryParameter("m") ?: return false
        if (uid.isBlank()) return false
        return e.length == 64 &&
            c.length == 6 &&
            m.length == 16 &&
            e.matches(Regex("^[0-9a-fA-F]+$")) &&
            c.matches(Regex("^[0-9a-fA-F]+$")) &&
            m.matches(Regex("^[0-9a-fA-F]+$"))
    }

    private fun isLikelyInitUrl(url: String): Boolean {
        val uri = Uri.parse(url)
        val e = uri.getQueryParameter("e") ?: return false
        val c = uri.getQueryParameter("c") ?: return false
        val m = uri.getQueryParameter("m") ?: return false
        return e.length == 64 &&
            c.length == 6 &&
            m.length == 16 &&
            e.matches(Regex("^[0-9a-fA-F]+$")) &&
            c.matches(Regex("^[0-9a-fA-F]+$")) &&
            m.matches(Regex("^[0-9a-fA-F]+$"))
    }

    private fun statusHexFromError(e: Exception): String {
        val msg = e.message.orEmpty()
        val m = Regex("0x[0-9A-Fa-f]{4}").find(msg)?.value
        return m ?: "unknown"
    }

    private fun handleTemplateOnlyTag(tag: Tag) {
        appendInitStep("⏳ [T 1/2] 检测到卡，开始仅写 URL 模板...")
        try {
            val uidBytes = tag.id ?: run {
                initFail("[T 1/2] 读取 UID", "无法读取卡 UID")
                return
            }
            val publicUid = uidBytes.joinToString("") { b -> "%02X".format(b) }
            val initUrl = buildSunTemplateUrl(base = SUN_BASE_URL, publicUid = publicUid, lastCounter = 0)
            val msg = NdefMessage(arrayOf(NdefRecord.createUri(initUrl)))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    initFail("[T 2/2] NDEF 写入", "标签只读")
                    ndef.close()
                    return
                }
                ndef.writeNdefMessage(msg)
                ndef.close()
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable == null) {
                    initFail("[T 2/2] NDEF 写入", "标签不支持 NDEF")
                    return
                }
                formatable.connect()
                formatable.format(msg)
                formatable.close()
            }

            appendInitStep("✅ [T 2/2] 仅写模板完成（未执行 EV2/SDM）")
            readUrlText = initUrl
            readParamText = "template-only: e/c/m 占位，未启用卡侧动态 SDM"
        } catch (e: Exception) {
            initFail("[T X] 模板写入", e.message ?: "unknown error")
        } finally {
            initTemplateArmed = false
            initArmed = false
        }
    }

    private fun handleInitTag(tag: Tag, allowKey2Fallback: Boolean, useDefaultKey0: Boolean) {
        appendInitStep("⏳ [1/11] 已检测到 NFC 标签")
        try {
            if (HARD_CODED_KEY0.length != 32 || HARD_CODED_MASTER_KEY.length != 32) {
                initFail("[2/11] 参数校验", "Key0 或 MasterKey 长度不是 16-byte HEX")
                return
            }
            val appKey0 = hexToBytes(HARD_CODED_KEY0)
            val key0 = if (useDefaultKey0) ByteArray(16) else appKey0
            val masterKey = hexToBytes(HARD_CODED_MASTER_KEY)
            if (key0 == null || appKey0 == null || masterKey == null || key0.size != 16 || appKey0.size != 16 || masterKey.size != 16) {
                initFail("[2/11] 参数校验", "Key0/MasterKey 不是有效的 16-byte HEX")
                return
            }
            appendInitStep("✅ [2/11] 参数校验通过（${if (useDefaultKey0) "Key0=0x00" else "Key0=AppKey0"} / MasterKey）")

            val uidBytes = tag.id ?: run {
                initFail("[3/11] 读取 UID", "无法读取卡 UID")
                return
            }
            val publicUid = uidBytes.joinToString("") { b -> "%02X".format(b) }
            appendInitStep("✅ [3/11] UID=$publicUid")

            val isoDep = IsoDep.get(tag) ?: run {
                initFail("[4/11] EV2 准备", "该卡不支持 IsoDep，无法执行 EV2/SDM")
                return
            }
            isoDep.connect()
            isoDep.timeout = 5000
            appendInitStep("✅ [4/11] IsoDep 已连接")

            val ev2 = Ntag424Ev2(isoDep)
            val sdmKey = ev2.deriveSdmKey(masterKey, uidBytes)
            appendInitStep("✅ [5/11] 通过 masterKey+UID 派生 SDM Key")

            var session: Ev2Session? = null
            var authenticatedWithKey2Fallback = false
            try {
                appendInitStep("⏳ [6/11] 尝试 EV2First(${if (useDefaultKey0) "Key0=0x00" else "Key0=AppKey0"}) ...")
                session = ev2.authenticateEV2First(0x00, key0)
                appendInitStep("✅ [6/11] EV2First(Key0) 认证成功")
            } catch (e: Exception) {
                val msg = e.message.orEmpty()
                appendInitStep("⚠️ [6/11] EV2First(Key0) 失败，status=${statusHexFromError(e)}")
                if (msg.contains("0x9140") && allowKey2Fallback) {
                    appendInitStep("⏳ [6/11] 尝试 EV2First(Key2) 回退认证...")
                    try {
                        session = ev2.authenticateEV2First(0x02, sdmKey)
                        appendInitStep("✅ [6/11] EV2First(Key2) 回退认证成功（该卡可能已是已初始化卡）")
                        authenticatedWithKey2Fallback = true
                    } catch (e2: Exception) {
                        initFail(
                            "[6/11] EV2First(Key2)",
                            "回退认证失败，status=${statusHexFromError(e2)}"
                        )
                        return
                    }
                } else if (msg.contains("0x9140") && useDefaultKey0) {
                    appendInitStep("⏳ [6/11] 尝试 EV2First(AppKey0) 认证...")
                    try {
                        session = ev2.authenticateEV2First(0x00, appKey0)
                        appendInitStep("✅ [6/11] EV2First(AppKey0) 认证成功")
                    } catch (e2: Exception) {
                        initFail(
                            "[6/11] EV2First(AppKey0)",
                            "认证失败，status=${statusHexFromError(e2)}；0x00/AppKey0 均不匹配"
                        )
                        return
                    }
                } else if (msg.contains("0x9140")) {
                    initFail("[6/11] EV2First(Key0)", "认证失败 0x9140。当前卡的 Key0 不匹配（0x00/AppKey0）。")
                    return
                } else {
                    throw e
                }
            }
            val sessionSafe = session ?: run {
                initFail("[6/11] EV2First", "未建立有效会话")
                return
            }

            if (!authenticatedWithKey2Fallback) {
                ev2.changeKey(sessionSafe, changingKeyNo = 0x02, newKey = sdmKey)
                appendInitStep("✅ [7/11] ChangeKey(0x02) 写入派生 SDM Key")
            } else {
                appendInitStep("ℹ️ [7/11] Key2 回退模式：跳过 ChangeKey(0x02)")
            }

            val ndefFileNo = ev2.autoDetectNdefFileNo()
            appendInitStep("✅ [8/11] autoDetect NDEF fileNo=0x${String.format("%02X", ndefFileNo)}")

            if (!authenticatedWithKey2Fallback) {
                val patchedSettings = ev2.patchSdmFileSettingsFromLiveNdef(
                    fileNo = ndefFileNo,
                    expectedEncHexLen = 64,
                    expectedCtrHexLen = 6,
                    expectedMacHexLen = 16,
                    readBytes = 300
                )
                ev2.changeFileSettings(sessionSafe, ndefFileNo, patchedSettings)
                appendInitStep("✅ [9/11] 已完成 SDM offsets + ChangeFileSettings")
            } else {
                appendInitStep("ℹ️ [9/11] Key2 回退模式：跳过 ChangeFileSettings（避免权限失败）")
            }
            try {
                isoDep.close()
            } catch (_: Exception) {
                // ignore close error
            }

            val initUrl = buildSunTemplateUrl(base = SUN_BASE_URL, publicUid = publicUid, lastCounter = 0)
            if (!initUrl.startsWith("https://")) {
                initFail("[10/11] URL 模板", "sunBaseUrl 非 HTTPS")
                return
            }
            val msg = NdefMessage(arrayOf(NdefRecord.createUri(initUrl)))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    initFail("[10/11] NDEF 写入", "标签只读")
                    ndef.close()
                    return
                }
                ndef.writeNdefMessage(msg)
                ndef.close()
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable == null) {
                    initFail("[10/11] NDEF 写入", "标签不支持 NDEF")
                    return
                }
                formatable.connect()
                formatable.format(msg)
                formatable.close()
            }

            uidText = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: "读取失败"
            appendInitStep("✅ [10/11] Init 完成：EV2/SDM 已配置，URL 模板已写入")
            appendInitStep("⏳ [11/11] 开始 init 后检验（读取并校验 URL）")
            val verify = checkInitStatusByNdef(tag)
            if (verify.url.isNullOrBlank() || verify.status.startsWith("❌")) {
                initFail("[11/11] Init 检验", verify.status)
                return
            }
            readUrlText = verify.url
            readParamText = verify.paramText ?: ""
            appendInitStep("✅ [11/11] Init 检验通过：${verify.status}")
            appendInitStep("key0=${HARD_CODED_KEY0.take(8)}... masterKey=${HARD_CODED_MASTER_KEY.take(8)}...")
            appendInitStep("publicUID=$publicUid")
        } catch (e: Exception) {
            initFail("[X] 未捕获异常", e.message ?: "unknown error")
        } finally {
            initArmed = false
        }
    }

    private fun buildSunTemplateUrl(base: String, publicUid: String, lastCounter: Int): String {
        val e = "0".repeat(64)
        val c = String.format("%06X", lastCounter.coerceAtLeast(0))
        val m = "0".repeat(16)
        return if (base.contains("?")) {
            if (base.endsWith("?") || base.endsWith("&")) {
                "${base}uid=$publicUid&e=$e&c=$c&m=$m"
            } else {
                "${base}&uid=$publicUid&e=$e&c=$c&m=$m"
            }
        } else {
            "$base?uid=$publicUid&e=$e&c=$c&m=$m"
        }
    }

    private fun hexToBytes(hex: String): ByteArray? {
        val s = hex.trim()
        if (s.length % 2 != 0) return null
        return try {
            ByteArray(s.length / 2) { i ->
                s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun BeamioWebView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    // 关闭放大缩小
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    // 关闭 overscroll 弹性效果
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                webViewClient = WebViewClient()
                loadUrl("https://beamio.app/app/")
            }
        }
    )
}

@Composable
fun NdefScreen(
    uidText: String,
    readCheckText: String,
    readUrlText: String,
    readParamText: String,
    lastCounter: Int,
    lastCounterInput: String,
    initText: String,
    topupAmount: String,
    paymentAmount: String,
    onReadClick: () -> Unit,
    onInitClick: () -> Unit,
    onInitTemplateClick: () -> Unit,
    onTopupChange: (String) -> Unit,
    onPaymentChange: (String) -> Unit,
    onTopupClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onCopyUrlClick: () -> Unit,
    onLastCounterInputChange: (String) -> Unit,
    onApplyLastCounterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onReadClick) {
                Text("read")
            }
            Text(text = "NDEF UID: $uidText")
        }
        Text(
            text = readCheckText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "lastCounter=$lastCounter")
            OutlinedTextField(
                value = lastCounterInput,
                onValueChange = onLastCounterInputChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("设置 lastCounter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(onClick = onApplyLastCounterClick) {
                Text("apply")
            }
        }
        if (readUrlText.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = readUrlText,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopyUrlClick) {
                    Text("📋")
                }
            }
        }
        if (readParamText.isNotBlank()) {
            Text(
                text = readParamText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onInitClick) {
                Text("init")
            }
            Button(onClick = onInitTemplateClick) {
                Text("init-template")
            }
            Text(text = initText)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onTopupClick) {
                Text("topup")
            }
            OutlinedTextField(
                value = topupAmount,
                onValueChange = onTopupChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onPaymentClick) {
                Text("payment")
            }
            OutlinedTextField(
                value = paymentAmount,
                onValueChange = onPaymentChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun InitFlowScreen(
    initText: String,
    showReconfirm: Boolean,
    allowContinue: Boolean,
    reconfirmText: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Init 模式")
        Text(
            text = initText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        if (showReconfirm) {
            Text(
                text = reconfirmText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (allowContinue) {
                    Button(onClick = onContinue) {
                        Text("继续")
                    }
                }
                Button(onClick = onCancel) {
                    Text("cancel")
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onCancel) {
                    Text("cancel")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidNTAGTheme {
        NdefScreen(
            uidText = "04A1B2C3D4",
            readCheckText = "✅ 已按 init 规则初始化",
            readUrlText = "https://api.beamio.app/api/sun?e=...&c=...&m=...",
            readParamText = "e=...\nc=000001 (counter=1)\nm=...",
            lastCounter = 1,
            lastCounterInput = "1",
            initText = "未初始化",
            topupAmount = "",
            paymentAmount = "",
            onReadClick = {},
            onInitClick = {},
            onInitTemplateClick = {},
            onTopupChange = {},
            onPaymentChange = {},
            onTopupClick = {},
            onPaymentClick = {},
            onCopyUrlClick = {},
            onLastCounterInputChange = {},
            onApplyLastCounterClick = {}
        )
    }
}