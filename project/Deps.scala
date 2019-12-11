import sbt._

object Deps {
  val depsRepos = Seq(
    "maven repo"          at "http://repo.maven.apache.org/maven2",
    "typesafe releases"   at "http://repo.typesafe.com/typesafe/releases",
    "typesafe snapshots"  at "http://repo.typesafe.com/typesafe/snapshots",
    "akka repo"           at "http://repo.akka.io/",
//    "spray repo"          at "http://repo.spray.io",
//    "untyped"             at "http://ivy.untyped.com",
    "jasper reports repo" at "http://jasperreports.sourceforge.net/maven2",
    "jasper artifacts"    at "http://jaspersoft.artifactoryonline.com/jaspersoft/third-party-ce-artifacts/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  object V {
    val scala       = "2.12.10"
    val spray       = "1.3.5"
    val akkaHttp    = "10.1.10"
    val slf4j       = "1.7.12"
    val akka        = "2.5.25"
    val jackson     = "2.10.0"
    val lucene      = "6.6.1"
    val enumeratum  = "1.5.12"
    val swagger     = "2.0.9"
  }

  val akkaActor             = "com.typesafe.akka"       %% "akka-actor"                           % V.akka
  val akkaStream            = "com.typesafe.akka"       %% "akka-stream"                          % V.akka
  val akkaContrib           = "com.typesafe.akka"       %% "akka-contrib"                         % V.akka
  val akkaClusterTools      = "com.typesafe.akka"       %% "akka-cluster-tools"                   % V.akka
  val akkaPersistence       = "com.typesafe.akka"       %% "akka-persistence"                     % V.akka    // exclude("org.iq80.leveldb","leveldb")
  val akkaQuery             = "com.typesafe.akka"       %% "akka-persistence-query"               % V.akka
  val akkaClusterSharding   = "com.typesafe.akka"       %% "akka-cluster-sharding"                % V.akka
  val akkaKryo              = "com.github.romix.akka"   %% "akka-kryo-serialization"              % "0.5.2"
  val levelDbFixed          = "org.iq80.leveldb"        %  "leveldb"                              % "0.9"
  val levelDbJNI            = "org.fusesource.leveldbjni" % "leveldbjni-all"                      % "1.8"
  val cassandraPersistence	= "com.typesafe.akka"		    %% "akka-persistence-cassandra" 		      % "0.99"
  val akkaInMemoryTestDB    = "com.github.dnvriend"     %% "akka-persistence-inmemory"            % "2.5.15.2" excludeAll ExclusionRule(organization = "com.typesafe.akka")
  val akkaTestKit           = "com.typesafe.akka"       %% "akka-testkit"                         % V.akka
  val akkaMultiNodeTestKit  = "com.typesafe.akka"       %% "akka-multi-node-testkit"              % V.akka
  val akkaSlf4j             = "com.typesafe.akka"       %% "akka-slf4j"                           % V.akka
  val slf4j                 = akkaSlf4j
//  val slf4j                 = "org.slf4j"               % "slf4j-simple"                          % "1.7.28"
  val bcrypt                = "com.github.t3hnar"       %% "scala-bcrypt"                         % "3.1"
  val logback               = "ch.qos.logback"          %  "logback-classic"                      % "1.0.13"
  val config                = "com.typesafe"            %  "config"                               % "1.2.1"
  val scalaLogging          = "com.typesafe.scala-logging"      %% "scala-logging"                % "3.9.0"
  val enumeratum            = "com.beachape"            %% "enumeratum"                           % V.enumeratum
  val joseJwt               = "com.nimbusds"            % "nimbus-jose-jwt"                       % "8.2.1"


  // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
  val joda = "org.apache.httpcomponents" % "httpclient" % "4.5.3"

  // https://mvnrepository.com/artifact/org.apache.lucene/lucene-core
  val lucene = "org.apache.lucene" % "lucene-core" % V.lucene

  val akkaHttp              = "com.typesafe.akka"       %% "akka-http"                            % V.akkaHttp
  val akkHttpXml            = "com.typesafe.akka"       %% "akka-http-xml"                        % V.akkaHttp
  val akkaHttpSprayJson     = "com.typesafe.akka"       %% "akka-http-spray-json"                 % V.akkaHttp
  val akkaHttpCore          = "com.typesafe.akka"       %% "akka-http-core"                       % V.akkaHttp
  val akkaHttpTestkit       = "com.typesafe.akka"       %% "akka-http-testkit"                    % V.akkaHttp
  val akkaHtppJackson       = "com.typesafe.akka"       %% "akka-http-jackson"                    % V.akkaHttp
  val akkaHttpCors          = "ch.megard"               %% "akka-http-cors"                       % "0.4.1"
  // As suggested in https://stackoverflow.com/questions/43574426/how-to-resolve-java-lang-noclassdeffounderror-javax-xml-bind-jaxbexception-in-j
  // to resolve blow-up due to swagger :  java.lang.NoClassDefFoundError: javax/xml/bind/annotation/XmlRootElement.
  val javaxws               = "javax.ws.rs"             % "javax.ws.rs-api"                       % "2.0.1"
  val swaggerAkkaHttp       = "com.github.swagger-akka-http" %% "swagger-akka-http"               % "2.0.4"
  val swaggerAkkaHttpFix    = "io.swagger"              % "swagger-jaxrs"                         % "1.5.24"
  val swaggerAkkaHttpFix2   = "javax.xml.bind"          % "jaxb-api"                              % "2.4.0-b180830.0359"
  val swaggerAkkaHttpScala  = "com.github.swagger-akka-http" %% "swagger-scala-module"            % "2.0.5"
  val swaggerCore           = "io.swagger.core.v3"      % "swagger-core"                          % V.swagger
  val swaggerAnnotations    = "io.swagger.core.v3"      % "swagger-annotations"                   % V.swagger
  val swaggerModels         = "io.swagger.core.v3"      % "swagger-models"                        % V.swagger
  val swaggerjaxrs2         = "io.swagger.core.v3"      % "swagger-jaxrs2"                        % V.swagger

  // The test scope will be added in the build so we don't need to declare it in here
  val scalaMock             = "org.scalamock"           %% "scalamock"                            % "4.1.0"
  val scalaTest             = "org.scalatest"           %% "scalatest"                            % "3.0.1"
  val junit                 = "junit"                   %  "junit"                                % "4.12"
  val sbtJUnitInterface     = "com.novocode"            % "junit-interface"                       % "0.11"
  val wireMock              = "com.github.tomakehurst"  % "wiremock"                              % "2.23.2"
  val commonsIO             = "commons-io"              %  "commons-io"                           % "2.4"
  val apacheCommonsLang     = "org.apache.commons"      % "commons-lang3"                         % "3.4"
  val jsonJava              = "com.fasterxml.jackson.core"   % "jackson-core"					            % V.jackson
  val jacksonDatabind       = "com.fasterxml.jackson.core"   % "jackson-databind"			            % V.jackson
  val jacksonScala          = "com.fasterxml.jackson.module" %% "jackson-module-scala"            % V.jackson

  val spel                  = "org.springframework"     %  "spring-expression"                    % "4.2.5.RELEASE"
  val jsonPath              = "com.jayway.jsonpath"  	  % "json-path"                             % "2.4.0"

  val javaMail              = "com.sun.mail"            % "javax.mail"                            % "1.5.5"
  val javaMailApi           = "javax.mail"              % "javax.mail-api"                        % "1.5.5"
  val jasperReports         = "net.sf.jasperreports"    % "jasperreports"                         % "6.7.0" excludeAll ExclusionRule(organization = "org.apache.lucene")
  val jasperReportFonts     = "net.sf.jasperreports"    % "jasperreports-fonts"                   % "6.0.0"

  val sw4jj                 = "com.github.j5ik2o"       % "sw4jj_2.11"                            % "1.0.2"
  val slick                 = "com.typesafe.slick"      %% "slick"                                % "3.3.2"
  val hikariCP              = "com.typesafe.slick"      %% "slick-hikaricp"                       % "3.3.2"
  val postgres              = "org.postgresql"          % "postgresql"                            % "9.4-1206-jdbc42"
  val h2                    = "com.h2database"          % "h2"                                    % "1.4.185"
  val hsqldb                = "org.hsqldb"              % "hsqldb"                                % "2.5.0"
  val flyway                = "org.flywaydb"            % "flyway-core"                           % "6.0.7"
  val slickMigration        = "io.github.nafg"          %% "slick-migration-api"                  % "0.7.0"
  val flywaySlickBindings   = "io.github.nafg"          %% "slick-migration-api-flyway"           % "0.6.0"

  val javaXmlJDK9Compat     = "javax.xml.bind" % "jaxb-api" % "2.3.0"

  object gatling {
    private val v   = "2.0.0.20130628"
    val app         = "io.gatling"              %  "gatling-app"               % v
    val recorder    = "io.gatling"              %  "gatling-recorder"          % v
    val charts      = "io.gatling.highcharts"   %  "gatling-charts-highcharts" % v
  }

}
