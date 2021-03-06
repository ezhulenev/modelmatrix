
object Dependencies {

  import Dependency._

  val spark = Seq(
      sparkYarn
    , sparkHive
    , sparkMLLib
  )

  val test = Seq(
      Test.scalaTest
    , Test.scalaMock
    , Test.scalaCheck
    , Test.h2
  )

  // Project dependencies

  val modelmatrixCore =
    spark ++
    test ++
    Seq(
      slf4jApi
    , scalazCore
    , config
    , scodecBits
    , slick
    , pgDriver
    , hikariCP
    )

  val modelmatrixCli =
    spark ++
    test ++
    Seq(
      log4j
    , slf4jLog4j
    , scopt
    , guava
    , sparkCSV
    , scalazStream
    )

  val modelmatrixClient =
    spark ++
    test ++
    Seq(
      guava
    )

}
