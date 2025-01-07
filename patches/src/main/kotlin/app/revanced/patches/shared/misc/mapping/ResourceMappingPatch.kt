package app.revanced.patches.shared.misc.mapping

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import com.android.apksig.apk.ApkUtils
import com.android.apksig.internal.apk.v2.V2SchemeVerifier
import com.android.apksig.util.DataSources
import com.android.apksig.util.RunnablesExecutor
import org.w3c.dom.Element
import java.io.File
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

lateinit var resourceMappings: List<ResourceElement>
    private set
var appPackageName:String? = null
    private set

var originalSignatrue:String? = null
    private set

fun getSignatureBase64(apkFile: File): String{
    val dataSource = DataSources.asDataSource(RandomAccessFile(apkFile.path, "r"))
    val zipSections = ApkUtils.findZipSections(dataSource)
    val v2 = V2SchemeVerifier.verify(RunnablesExecutor.SINGLE_THREADED, dataSource, zipSections, mapOf(2 to "APK Signature Scheme v2"), hashSetOf(2), 24, Int.MAX_VALUE)
    return Base64.getEncoder().encodeToString(v2.signers[0].certs[0].encoded)
}

val resourceMappingPatch = resourcePatch {
    val resourceMappings = Collections.synchronizedList(mutableListOf<ResourceElement>())

    execute {
        val threadCount = Runtime.getRuntime().availableProcessors()
        val threadPoolExecutor = Executors.newFixedThreadPool(threadCount)

        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            appPackageName = manifest.getAttribute("package")
        }

        originalSignatrue = getSignatureBase64(get("../../in.apk"))

        // Save the file in memory to concurrently read from it.
        val resourceXmlFile = get("res/values/public.xml").readBytes()

        for (threadIndex in 0 until threadCount) {
            threadPoolExecutor.execute thread@{
                document(resourceXmlFile.inputStream()).use { document ->

                    val resources = document.documentElement.childNodes
                    val resourcesLength = resources.length
                    val jobSize = resourcesLength / threadCount

                    val batchStart = jobSize * threadIndex
                    val batchEnd = jobSize * (threadIndex + 1)
                    element@ for (i in batchStart until batchEnd) {
                        // Prevent out of bounds.
                        if (i >= resourcesLength) return@thread

                        val node = resources.item(i)
                        if (node !is Element) continue

                        val nameAttribute = node.getAttribute("name")
                        val typeAttribute = node.getAttribute("type")

                        if (node.nodeName != "public" || nameAttribute.startsWith("APKTOOL")) continue

                        val id = node.getAttribute("id").substring(2).toLong(16)

                        resourceMappings.add(ResourceElement(typeAttribute, nameAttribute, id))
                    }
                }
            }
        }

        threadPoolExecutor.also { it.shutdown() }.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)

        app.revanced.patches.shared.misc.mapping.resourceMappings = resourceMappings
    }
}

operator fun List<ResourceElement>.get(type: String, name: String) = resourceMappings.firstOrNull {
    it.type == type && it.name == name
}?.id ?: throw PatchException("Could not find resource type: $type name: $name")

data class ResourceElement internal constructor(val type: String, val name: String, val id: Long)
