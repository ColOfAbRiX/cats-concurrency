import sbt._
import Keys._
import sbtbuildinfo.BuildInfoPlugin.autoImport.BuildInfoKey

object AllProjectsKeys extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {
    val projectPackage = settingKey[String]("The root package where a project resides")
    val projectBuildInfo = settingKey[Seq[BuildInfoKey]]("List of BuildInfoKey shared by all projects")
  }
  import autoImport._

  override lazy val projectSettings = Seq(
    projectPackage := {
      organization.value + "." + name.value.replaceAll("-service$", "").replaceAll("-", "")
    },
    projectBuildInfo := Seq[BuildInfoKey](
      "organization"   -> organization.value,
      "description"    -> description.value,
      "projectPackage" -> projectPackage.value,
    )
  )
}
