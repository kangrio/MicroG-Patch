package app.revanced.patches.all.misc.microg

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.extension.extensionPatch
import app.revanced.patches.shared.misc.mapping.appPackageName
import app.revanced.patches.shared.misc.mapping.originalSignatrue
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.gms.microGSupportResourcePatch

@Suppress("unused")
val microGSupportPatch = bytecodePatch(
    name = "MicroG Support",
    use = false,
) {
    dependsOn(
        resourceMappingPatch,
        microGSupportResourcePatch(),
        extensionPatch,
    )
    execute {
        classes.forEach {
            if(it.superclass.equals("Landroid/app/Application;") && !it.equals("Lapp/revanced/extension/shared/SignaturePatchApplication;")){
                proxy(it).mutableClass.setSuperClass("Lapp/revanced/extension/shared/SignaturePatchApplication;")
            }
        }

        signaturePatchApplication.method.apply {
            addInstructions(0, """
                const-string v0, "$originalSignatrue"
                invoke-static {v0}, Lapp/revanced/extension/shared/SignaturePatchApplication;->setSpoofedAppSignature(Ljava/lang/String;)V
                const-string v0, "$appPackageName"
                invoke-static {v0}, Lapp/revanced/extension/shared/SignaturePatchApplication;->addUniversalPackage(Ljava/lang/String;)V
                """.trimIndent())
        }
    }
}