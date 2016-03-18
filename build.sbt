import sbtassembly.Plugin._
import com.typesafe.sbt.SbtStartScript
import AssemblyKeys._
import spray.revolver.RevolverPlugin.Revolver

organization  := "com.flipcast"

name          := "flipcast"

version       := "2.2"

scalaVersion  := "2.11.6"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

scalacOptions := Seq(
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Xlog-reflective-calls",
  "-Ywarn-adapted-args",
  "-language:existentials"
)

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "org.scala-lang"                  %    "scala-compiler"                % "2.11.6",
  "org.scala-lang"                  %    "scala-library"                 % "2.11.6",
  "org.scala-lang"                  %    "scala-reflect"                 % "2.11.6",
  "io.spray"                        %%   "spray-can"                     % "1.3.3",
  "io.spray"                        %%   "spray-routing"                 % "1.3.3",
  "io.spray"                        %%   "spray-testkit"                 % "1.3.3"                  % "test",
  "io.spray"                        %%   "spray-client"                  % "1.3.3",
  "io.spray"                        %%   "spray-json"                    % "1.3.2",
  "com.typesafe.akka"               %%   "akka-actor"                    % "2.3.9",
  "com.typesafe.akka"               %%   "akka-slf4j"                    % "2.3.9",
  "com.typesafe.akka"               %%   "akka-testkit"                  % "2.3.9"                  % "test",
  "ch.qos.logback"                  %    "logback-classic"               % "1.1.2",
  "com.fasterxml.uuid"              %    "java-uuid-generator"           % "3.1.3",
  "com.codahale.metrics"            %    "metrics-logback"               % "3.0.2",
  "com.codahale.metrics"            %    "metrics-graphite"              % "3.0.2",
  "com.codahale.metrics"            %    "metrics-jvm"                   % "3.0.2",
  "commons-validator"               %    "commons-validator"             % "1.4.0",
  "commons-codec"                   %    "commons-codec"                 % "1.5",
  "com.google.guava"                %    "guava"                         % "18.0",
  "com.notnoop.apns"                %    "apns"                          % "1.0.0.Beta6",
  "org.mongodb"                     %%   "casbah"                        % "3.1.0",
  "org.mariadb.jdbc"                %    "mariadb-java-client"           % "1.3.6",
  "org.scalikejdbc"                 %%   "scalikejdbc"                   % "2.3.5",
  "org.scalikejdbc"                 %%   "scalikejdbc-jsr310"            % "2.3.5",
  "org.scalatra.scalate"            %%   "scalate-core"                  % "1.7.1",
  "org.scalikejdbc"                 %%   "scalikejdbc-test"              % "2.3.5"                  % "test",
  "com.jolbox"                      %    "bonecp"                        % "0.8.0.RELEASE",
  "commons-io"                      %    "commons-io"                    % "2.4",
  "com.github.sstone"               %%   "amqp-client"                   % "1.5",
  "com.google.code.findbugs"        %    "jsr305"                        % "3.0.1",
  "org.specs2"                      %%   "specs2"                        % "2.3.11"                 % "test"
)

assemblySettings

scalikejdbcSettings

parallelExecution in Test := false

assembleArtifact in packageScala := true

test in assembly := {}

jarName in assembly := "flipcast-service.jar"

logLevel in assembly := Level.Warn

Seq(Revolver.settings: _*)

Seq(SbtStartScript.startScriptForJarSettings: _*)
