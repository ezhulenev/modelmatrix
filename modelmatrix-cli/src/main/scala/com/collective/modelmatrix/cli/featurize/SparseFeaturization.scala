package com.collective.modelmatrix.cli.featurize

import com.collective.modelmatrix.catalog.ModelMatrixCatalog
import com.collective.modelmatrix.cli.{Source, _}
import com.collective.modelmatrix.transform.Transformer
import com.collective.modelmatrix.{Featurization, IdentifiedPoint, ModelMatrix}
import com.typesafe.config.Config
import org.apache.spark.mllib.linalg.{DenseVector, SparseVector}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scalaz._

case class SparseFeaturization(
  modelInstanceId: Int,
  source: Source,
  sink: Sink,
  idColumn: String,
  cacheSource: Boolean,
  dbName: String,
  dbConfig: Config
)(implicit val ec: ExecutionContext @@ ModelMatrixCatalog) extends Script with CliModelCatalog with CliSparkContext {

  private val log = LoggerFactory.getLogger(classOf[ValidateInputData])

  private def sparseSchema(idType: DataType) = StructType(Seq(
    StructField(idColumn, idType),
    StructField("column_id", IntegerType),
    StructField("value", DoubleType)
  ))

  import com.collective.modelmatrix.cli.ASCIITableFormat._
  import com.collective.modelmatrix.cli.ASCIITableFormats._

  def run(): Unit = {

    log.info(s"Run sparse featurization using Model Matrix instance: $modelInstanceId. " +
      s"Input source: $source. " +
      s"Featurized sink: $sink. " +
      s"Id column: $idColumn" +
      s"Database: $dbName @ ${dbConfig.origin()}")

    implicit val sqlContext = ModelMatrix.hiveContext(sc)

    val features = blockOn(db.run(modelInstanceFeatures.features(modelInstanceId)))
    require(features.nonEmpty, s"No features are defined for model instance: $modelInstanceId. " +
      s"Ensure that this model instance exists")

    val featurization = new Featurization(features)

    val df = if (cacheSource) source.asDataFrame.cache() else source.asDataFrame
    Transformer.selectFeaturesWithId(df, idColumn, features.map(_.feature)) match {
      // Feature extraction failed
      case -\/(extractionErrors) =>
        Console.out.println(s"Feature extraction failed:")
        extractionErrors.printASCIITable()

      // Extracted feature type validation failed
      case \/-(extracted) if featurization.validate(Transformer.removeIdColumn(extracted)).exists(_.isLeft) =>
        val errors = featurization.validate(Transformer.removeIdColumn(extracted)).collect { case -\/(err) => err }
        Console.out.println(s"Input schema errors:")
        errors.printASCIITable()

      // Looks good, let's do featurization
      case \/-(extracted) =>
        val (idType, featurized) = featurization.featurize(extracted, idColumn)

        // Switch from 0-based Vector index to 1-based ColumnId
        val rows = featurized.flatMap {
          case IdentifiedPoint(id, sparse: SparseVector) =>
            (sparse.values zip sparse.indices).map { case (value, idx) => Row(id, idx + 1, value) }
          case IdentifiedPoint(id, dense: DenseVector) =>
            dense.values.zipWithIndex.map { case (value, idx) => Row(id, idx + 1, value) }
        }

        // Apply schema and save
        sink.saveDataFrame(sqlContext.createDataFrame(rows, sparseSchema(idType)))

        Console.out.println(s"Featurized data set was successfully saved to: $sink")
    }
  }
}
