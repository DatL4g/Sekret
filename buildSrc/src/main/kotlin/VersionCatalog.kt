import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog as VCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension as VCatalogEx
import org.gradle.kotlin.dsl.getByType

class VersionCatalog(project: Project) {
    private val libs: VCatalog = project.extensions.getByType<VCatalogEx>().named("libs")

    private fun version(key: String): String = libs.findVersion(key).get().requiredVersion
    private fun versionInt(key: String): Int = version(key).getDigitsOrNull()?.toIntOrNull() ?: version(key).toInt()

    private fun String.getDigitsOrNull(): String? {
        val replaced = this.replace("\\D+".toRegex(), String())
        return replaced.ifBlank {
            null
        }
    }

    val libVersion: String
        get() = version("lib")

    val libVersionCode: Int
        get() = versionInt("lib")

    companion object {
        fun artifactName(module: String = String()): String {
            return if (module.isBlank()) {
                Configuration.artifact
            } else {
                "${Configuration.artifact}.$module"
            }
        }
    }
}

val Project.libVersion: String
    get() = VersionCatalog(this).libVersion

val Project.libVersionCode: Int
    get() = VersionCatalog(this).libVersionCode