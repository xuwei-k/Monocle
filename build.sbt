import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtunidoc.Plugin.UnidocKeys._
import org.scalajs.sbtplugin.cross.CrossType

lazy val CrossTypeMixed = new CrossType {
  override def projectDir(crossBase: File, projectType: String) =
    crossBase / projectType

  override def sharedSrcDir(projectBase: File, conf: String) =
    Some(projectBase.getParentFile / "src" / conf / "scala")
}

lazy val buildSettings = Seq(
  organization       := "com.github.julien-truffaut",
  scalaVersion       := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  scalacOptions     ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps",
    "-unchecked",
    "-Xfatal-warnings",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-value-discard",
    "-Xfuture"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2,10)) => Seq("-Yno-generic-signatures") // no generic signatures for scala 2.10.x, see SI-7932, #571 and #828
    case _ => Seq( // https://github.com/scala/make-release-notes/blob/9cfbdc8c92f94/experimental-backend.md#emitting-java-8-style-lambdas
      "-Ybackend:GenBCode",
      "-Ydelambdafy:method",
      "-target:jvm-1.8"
    )
  }),
  resolvers ++= Seq(
    "bintray/non" at "http://dl.bintray.com/non/maven",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  scmInfo := Some(ScmInfo(url("https://github.com/julien-truffaut/Monocle"), "scm:git:git@github.com:julien-truffaut/Monocle.git"))
)

lazy val scalaz     = Def.setting("org.scalaz"      %%% "scalaz-core" % "7.2.1")
lazy val shapeless  = Def.setting("com.chuusai"     %%% "shapeless"   % "2.3.0")
lazy val refinedDep = Def.setting("eu.timepit"      %%% "refined"     % "0.3.7")

lazy val discpline  = Def.setting("org.typelevel"   %%% "discipline"  % "0.4")
lazy val scalatest  = Def.setting("org.scalatest"   %%% "scalatest"   % "3.0.0-M7"  % "test")

lazy val macroCompat = Def.setting("org.typelevel" %%% "macro-compat" % "1.1.1")

lazy val macroVersion = "2.1.0"
lazy val paradisePlugin = compilerPlugin("org.scalamacros" %  "paradise"       % macroVersion cross CrossVersion.full)

def mimaSettings(module: String): Seq[Setting[_]] = mimaDefaultSettings ++ Seq(
  previousArtifact := Some("com.github.julien-truffaut" %  (s"monocle-${module}_2.11") % "1.1.0")
)

lazy val monocleSettings = buildSettings ++ publishSettings

lazy val jsProjects = Seq(
  monocleJS, coreJS, genericJS, lawJS, macrosJS, stateJS, refinedJS, testJS, exampleJS
)
lazy val jsProjectReferences = jsProjects.map(p => p: ProjectReference)
lazy val jvmProjects = Seq[ProjectReference](
  monocleJVM, coreJVM, genericJVM, lawJVM, macrosJVM, stateJVM, refinedJVM, testJVM, exampleJVM, docs, bench
)

lazy val root = project.in(file("."))
  .settings(monocleSettings: _*)
  .settings(noPublishSettings: _*)
  .aggregate(jsProjectReferences ++ jvmProjects : _*)

lazy val rootJVM = project
  .settings(monocleSettings: _*)
  .settings(noPublishSettings: _*)
  .aggregate(jvmProjects: _*)

lazy val rootJS = project
  .settings(monocleSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    commands += Command.command("testSequential"){
      jsProjects.map(_.id).map(_ + "/test").sorted ::: _
    }
  )
  .aggregate(jsProjectReferences: _*)


lazy val monocle = crossProject.crossType(CrossTypeMixed)
  .settings(moduleName := "monocle")
  .settings(monocleSettings: _*)
  .dependsOn(core, generic, law, macros, state, refined)

lazy val monocleJVM = monocle.jvm
lazy val monocleJS = monocle.js

lazy val core = crossProject.crossType(CrossTypeMixed)
  .settings(moduleName := "monocle-core")
  .settings(monocleSettings: _*)
  .settings(mimaSettings("core"): _*)
  .settings(libraryDependencies ++= Seq(scalaz.value))
  .settings(libraryDependencies ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
    case Some((2, 11)) => "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0"
  }.toList)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val generic = crossProject.crossType(CrossTypeMixed).dependsOn(core)
  .settings(moduleName := "monocle-generic")
  .settings(monocleSettings: _*)
  .settings(mimaSettings("generic"): _*)
  .settings(libraryDependencies ++= Seq(scalaz.value, shapeless.value))

lazy val genericJVM = generic.jvm
lazy val genericJS = generic.js

lazy val refined = crossProject.crossType(CrossTypeMixed).dependsOn(core)
  .settings(moduleName := "monocle-refined")
  .settings(monocleSettings: _*)
  .settings(libraryDependencies ++= Seq(scalaz.value, refinedDep.value))

lazy val refinedJVM = refined.jvm
lazy val refinedJS = refined.js

lazy val law = crossProject.crossType(CrossTypeMixed).dependsOn(core)
  .settings(moduleName := "monocle-law")
  .settings(monocleSettings: _*)
  .settings(libraryDependencies ++= Seq(discpline.value))

lazy val lawJVM = law.jvm
lazy val lawJS = law.js

lazy val macros = crossProject.crossType(CrossTypeMixed).dependsOn(core)
  .in(file("macro"))
  .settings(moduleName := "monocle-macro")
  .settings(monocleSettings: _*)
  .settings(
  scalacOptions  += "-language:experimental.macros",
  libraryDependencies ++= Seq(
    "org.scala-lang"  %  "scala-reflect"  % scalaVersion.value,
    "org.scala-lang"  %  "scala-compiler" % scalaVersion.value % "provided",
    macroCompat.value
  ),
  addCompilerPlugin(paradisePlugin),
  libraryDependencies ++= CrossVersion partialVersion scalaVersion.value collect {
    case (2, scalaMajor) if scalaMajor < 11 => Seq("org.scalamacros" %% "quasiquotes" % macroVersion)
  } getOrElse Nil,
  unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / s"scala-${scalaBinaryVersion.value}"
  )

lazy val macrosJVM = macros.jvm
lazy val macrosJS = macros.js

lazy val state = crossProject.crossType(CrossTypeMixed).dependsOn(core)
  .settings(moduleName := "monocle-state")
  .settings(monocleSettings: _*)
  .settings(libraryDependencies ++= Seq(scalaz.value))

lazy val stateJVM = state.jvm
lazy val stateJS = state.js

lazy val test = crossProject.crossType(CrossTypeMixed).dependsOn(core, generic, macros, law, state, refined)
  .settings(moduleName := "monocle-test")
  .settings(monocleSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(scalaz.value, shapeless.value, scalatest.value, discpline.value, compilerPlugin(paradisePlugin))
  )
  .jsSettings(
    jsEnv := NodeJSEnv().value,
    scalaJSUseRhino in Global := false
  )

lazy val testJVM = test.jvm
lazy val testJS = test.js

lazy val bench = project.dependsOn(coreJVM, genericJVM, macrosJVM)
  .settings(moduleName := "monocle-bench")
  .settings(monocleSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(libraryDependencies ++= Seq(
    shapeless.value,
    compilerPlugin(paradisePlugin)
  )).enablePlugins(JmhPlugin)

lazy val example = crossProject.crossType(CrossTypeMixed).dependsOn(core, generic, refined, macros, state, test % "test->test")
  .settings(moduleName := "monocle-example")
  .settings(monocleSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(scalaz.value, shapeless.value, scalatest.value, compilerPlugin(paradisePlugin))
  )

lazy val exampleJVM = example.jvm
lazy val exampleJS = example.js

lazy val docs = project.dependsOn(coreJVM, exampleJVM)
  .settings(moduleName := "monocle-docs")
  .settings(monocleSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(unidocSettings)
  .settings(site.settings)
  .settings(ghpages.settings)
  .settings(docSettings)
  .settings(tutSettings)
  .settings(
    libraryDependencies ++= Seq(scalaz.value, shapeless.value, compilerPlugin(paradisePlugin))
  )


lazy val docSettings = Seq(
  autoAPIMappings := true,
  unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(coreJVM),
  site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "api"),
  site.addMappingsToSiteDir(tut, "_tut"),
  ghpagesNoJekyll := false,
  scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
    "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
    "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath
  ),
  git.remoteRepo := "git@github.com:julien-truffaut/Monocle.git",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
)


lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/julien-truffaut/Monocle")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  autoAPIMappings := true,
  apiURL := Some(url("https://julien-truffaut.github.io/Monocle/api/")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <developers>
      <developer>
        <id>julien-truffaut</id>
        <name>Julien Truffaut</name>
      </developer>
      <developer>
        <id>NightRa</id>
        <name>Ilan Godik</name>
      </developer>
      <developer>
        <id>aoiroaoino</id>
        <name>Naoki Aoyama</name>
      </developer>
    </developers>
    ),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

addCommandAlias("validate", ";compile;test;unidoc;tut")

// For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
