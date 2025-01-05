package app.revanced.patches.shared.misc.extension

import app.revanced.patcher.patch.bytecodePatch

fun sharedExtensionPatch() = bytecodePatch {
    extendWith("extensions/shared.rve")
}