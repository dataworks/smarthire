name := "etl"
version := "1.0"

scalaVersion := "2.10.6"

// Add Spark Library to classpath
unmanagedJars in Compile += file(sys.props.get("spark.lib").get)

// Use src/scala
scalaSource in Compile := baseDirectory.value / "src"

// Use test/scala
scalaSource in Test := baseDirectory.value / "test"

//run with the features and deprecation options
scalacOptions ++= Seq("-feature", "-deprecation")

// Needed for JavaCPP
classpathTypes += "maven-plugin"

resolvers += "clojars" at "https://clojars.org/repo"
resolvers += "conjars" at "http://conjars.org/repo"

libraryDependencies ++= Seq(
    "com.github.scopt" %% "scopt" % "3.4.0",
    "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.16",
    "org.apache.opennlp" % "opennlp-tools" % "1.6.0",
    "org.apache.lucene" % "lucene-analyzers-common" % "6.0.1",
    "org.apache.lucene" % "lucene-core" % "6.0.1",
    "org.deeplearning4j" % "deeplearning4j-core" % "0.4-rc3.10",
    "org.nd4j" % "nd4j-native" % "0.4-rc3.10" classifier "" classifier "linux-x86_64",
    "org.scalatest" % "scalatest_2.10" % "2.2.6" % "test",
    "org.apache.tika" % "tika-core" % "1.13",
    "org.apache.tika" % "tika-parsers" % "1.13",
    "org.elasticsearch" % "elasticsearch-hadoop" % "2.3.2",
    "commons-io" % "commons-io" % "2.5",
    "org.json4s" %% "json4s-native" % "3.3.0"
    "com.twelvemonkeys.imageio" % "imageio-core" % "3.2.1"
)
