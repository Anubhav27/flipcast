resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

libraryDependencies += "org.mariadb.jdbc" % "mariadb-java-client" % "1.3.6"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" %  "0.10.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.3.5")