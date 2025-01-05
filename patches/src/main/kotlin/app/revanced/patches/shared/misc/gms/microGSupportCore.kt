package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.originalSignatrue
import org.w3c.dom.Element
import org.w3c.dom.Node

@Suppress("unused")
fun microGSupportResourcePatch() = resourcePatch {
    execute {
        fun addSpoofingMetadata() {
            fun Node.adoptChild(
                tagName: String,
                block: Element.() -> Unit,
            ) {
                val child = ownerDocument.createElement(tagName)
                child.block()
                appendChild(child)
            }

            document("AndroidManifest.xml").use { document ->
                val applicationNode =
                    document
                        .getElementsByTagName("application")
                        .item(0)

                // GmsCore presence detection in extension.
                applicationNode.adoptChild("meta-data") {
                    setAttribute("android:name", "org.microg.gms.spoofed_certificates")
                    setAttribute(
                        "android:value",
                        originalSignatrue
                    )
                }
            }
        }

        addSpoofingMetadata()

    }
}