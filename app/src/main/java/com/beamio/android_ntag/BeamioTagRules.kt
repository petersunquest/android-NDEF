package com.beamio.android_ntag

import java.text.Normalizer
import java.util.Locale

/**
 * Mirrors [src/bizSite/src/utils/beamioTagRules.ts]: same charset as Cluster `/addUser` / SilentPassUI.
 */
object BeamioTagRules {
    val ALLOWED_REGEX = Regex("^[a-zA-Z0-9_.]{3,20}$")

    const val RULE_HINT = "Use 3–20 letters, numbers, dots, or underscores"

    /** NFKC strip @, trim, zero-width — same semantics as TS `normalizeBeamioTagInput`. */
    fun normalizeInput(raw: String): String =
        Normalizer.normalize(
            raw.replace("@", ""),
            Normalizer.Form.NFKC,
        ).replace(ZW_RE, "").trim()

    private val ZW_RE = Regex("[\\u200B-\\u200D\\uFEFF]")
}
