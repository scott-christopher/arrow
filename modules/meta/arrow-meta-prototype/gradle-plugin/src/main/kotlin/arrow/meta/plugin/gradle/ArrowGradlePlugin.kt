package arrow.meta.plugin.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact

/**
 * The project-level Gradle plugin behavior that is used specifying the plugin's configuration through the
 * [ArrowExtension] class.
 */
/*class ArrowGradlePlugin : Plugin<Project> {
  companion object {
    fun isEnabled(project: Project) = project.plugins.findPlugin(ArrowGradlePlugin::class.java) != null

    fun getArrowExtension(project: Project): ArrowExtension {
      return project.extensions.getByType(ArrowExtension::class.java)
    }
  }

  override fun apply(project: Project) {
    project.extensions.create("arrow", ArrowExtension::class.java)
    *//*project.afterEvaluate { p ->
      if (isEnabled(p))
        p.tasks.withType(KotlinCompile::class.java).all {
          it.kotlinOptions.freeCompilerArgs.run {
            p.configurations
            //println("compiler plugin is added to $p")
            //this + "-Xplugin=${project.rootProject.project("arrow-meta").projectDir.path.replace("/arrow-meta", "/arrow-meta-prototype/compiler-plugin/build/libs/compiler-plugin.jar")}"
          }
        }
    }*//*
  }
}*/

fun Project.addGradleDependency(configuration: String, artifact: SubpluginArtifact): Unit {
  val artifactVersion = artifact.version ?: "0.0.1"
  val gradleCoordinate = "${artifact.groupId}:${artifact.artifactId}:$artifactVersion"
  project.dependencies.add(configuration, gradleCoordinate)
}
