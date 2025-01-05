package app.revanced.patches.all.misc.microg

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val signaturePatchApplication = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.PRIVATE)
    custom { method, classDef ->
        method.name == "killPM"
    }
}