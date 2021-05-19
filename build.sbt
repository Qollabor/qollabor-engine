lazy val basicSettings = {
  val currentScalaVersion = "2.12.7"
  val scala211Version = "2.11.11"


  Seq(
    organization := "org.qollabor",
    description := "Case Engine",
    scalaVersion := Deps.V.scala,
    resolvers ++= Deps.depsRepos,
    resolvers += Resolver.jcenterRepo,
    resolvers += Resolver.sonatypeRepo("releases"),
    scalacOptions := Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-Xlog-reflective-calls"
    ),
    parallelExecution in Compile := true,
    parallelExecution in Test := false,
    fork in Test := true,
    sources in doc in Compile := List()
  )
}

lazy val moduleSettings = basicSettings ++ Seq(
  libraryDependencies ++=
    Deps.compile(
      Deps.javaXmlJDK9Compat
    ) ++
      Deps.test(
        Deps.junit,
        Deps.sbtJUnitInterface,
        Deps.scalaMock,
        Deps.scalaTest,
        Deps.commonsIO
      )
)

// PROJECT ROOT
lazy val qollabor = Project("qollabor-engine", file(""))
  .aggregate(
    engine,
    service
  )
  .settings(basicSettings: _*)

// CMMN CASE ENGINE
lazy val engine = project("case-engine")
  .settings(libraryDependencies ++=
    Deps.compile(
      Deps.akkaActor,
      Deps.akkaClusterTools,
      Deps.akkaClusterSharding,
      Deps.akkaContrib,
      Deps.akkaHttp,
      Deps.akkaHttpCors,
      Deps.akkaHttpSprayJson,
      Deps.akkaHttpXml,
      Deps.akkaInMemoryTestDB,
      Deps.cassandraPersistence,
      Deps.akkaPersistenceJDBC,
      Deps.akkaPersistence,
      Deps.akkaQuery,
      Deps.akkaSlf4j,
      Deps.akkaStream,
      Deps.apacheCommonsText,
      Deps.cassandraPersistence,
      Deps.config,
      Deps.enumeratum,
      Deps.flyway,
      Deps.flywaySlickBindings,
      Deps.jacksonDatabind,
      Deps.jasperReports,
      Deps.jasperReportFonts,
      Deps.javaMail,
      Deps.javaxws,
      Deps.jsonJava,
      Deps.jsonPath,
      Deps.h2,
      Deps.hikariCP,
      Deps.hsqldb,
      Deps.logback,
      Deps.lowagie,
      Deps.postgres,
      Deps.scalaLogging,
      Deps.slick,
      Deps.slickMigration,
      Deps.spel,
      Deps.sqlserver,
      Deps.swaggerAkkaHttp,
      Deps.swaggerAnnotations,
      Deps.swaggerCore,
      Deps.swaggerjaxrs2,
      Deps.swaggerModels,
    ) ++
      Deps.test(
        Deps.akkaMultiNodeTestKit,
        Deps.akkaInMemoryTestDB,
        Deps.junit,
        Deps.sbtJUnitInterface,
        Deps.wireMock,
      )
  )
  .settings(
    sbtbuildinfo.BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](description, organization, version, git.baseVersion, git.gitHeadCommit, git.gitCurrentBranch, git.gitUncommittedChanges),
    sbtbuildinfo.BuildInfoKeys.buildInfoOptions += BuildInfoOption.BuildTime,
    sbtbuildinfo.BuildInfoKeys.buildInfoOptions += BuildInfoOption.ToMap,
    sbtbuildinfo.BuildInfoKeys.buildInfoOptions += BuildInfoOption.ToJson,
    sbtbuildinfo.BuildInfoKeys.buildInfoPackage := "org.qollabor.cmmn.akka",
    sbtbuildinfo.BuildInfoKeys.buildInfoObject := "BuildInfo"
  )
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitPlugin)
  .enablePlugins(GitVersioning, GitBranchPrompt)
  .settings(
    git.gitTagToVersionNumber := { tag: String =>
      val splittedTag = tag.split("-");
      splittedTag.length match {
        case 0 => Some(tag)
        case 1 => Some(tag)
        case _ => splittedTag(1) match {
          case "1" => Some(splittedTag(0) + "-with-1-additional-commit")
          case more => Some(splittedTag(0) + s"-with-$more-additional-commits")
        }
      }
    },
    git.useGitDescribe := true
  )
  .configs(MultiJvm)

lazy val service = project("case-service")
  .dependsOn(engine)
  .settings(libraryDependencies ++=
    Deps.compile(
      Deps.akkaHttpCore,
      Deps.akkaHtppJackson,
      Deps.bcrypt,
      Deps.hikariCP,
      Deps.jacksonScala,
      Deps.javaxws,
      Deps.joseJwt,
      Deps.sw4jj,
    ) ++
      Deps.test(
        Deps.akkaTestKit,
        Deps.akkaHttpTestkit,
        Deps.akkaMultiNodeTestKit,
        Deps.junit,
        Deps.wireMock,
      )
  )
  .settings(
    packageName in Docker := "qollabor/engine",
    version in Docker := "latest",
    maintainer in Docker := """Qollabor <info@qollabor.io>""",
    defaultLinuxInstallLocation in Docker := "/opt/qollabor",
    bashScriptExtraDefines += s"""addJava "-Dlogback.configurationFile=$${app_home}/../conf/logback.xml"""",
    bashScriptExtraDefines += s"""addJava "-Dconfig.file=$${app_home}/../conf/local.conf"""",
    dockerExposedPorts := Seq(2027, 9999),
    dockerBaseImage := "qollabor/base:openjdk-11-buster",//TODO change base
    name in Universal := "qollabor",
    packageName in Universal := "qollabor"
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .enablePlugins(ClasspathJarPlugin)
  .enablePlugins(LauncherJarPlugin)
  .enablePlugins(DockerPlugin)
  .configs(MultiJvm)

def project(name: String) = Project(name, file(name)).settings(moduleSettings: _*)

