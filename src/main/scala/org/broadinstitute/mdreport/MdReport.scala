package org.broadinstitute.mdreport

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.lambdaworks.jacks.JacksMapper
import com.typesafe.scalalogging.Logger
import java.io.PrintWriter
import scala.io.Source

/**
  * Created by amr on 10/19/2016.
  */
object MdReport extends App{
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val logger =Logger("MdReport")
  def parser = {
    new scopt.OptionParser[Config]("MdReport") {
      head("MdReport", "1.0")
      opt[String]('i', "setId").valueName("<id>").optional().action((x,c) => c.copy(setId = x))
        .text("The ID of the metrics/analysis/set entry. Must supply this or an entry file.")
      opt[Long]('v', "version").valueName("version").optional().action((x,c) => c.copy(version = x))
        .text("Optional version string for the entry.")
      opt[String]('e', "entryFile").optional().action((x, c) => c.copy(entryFile = x))
        .text("If EntryCreator was used you may supply the entry file to pass along sampleId and version.")
      opt[String]('o',"outDir").valueName("<outDir>").required().action((x, c) => c.copy(outDir = x))
        .text("The directory to write the report to.")
      help("help").text("Prints this help text.")
      note("\nA tool for generating reports from MD.")
    }
  }

  parser.parse(args, Config()
  ) match {
    case Some(config) =>
      if (config.entryFile.length > 0) {
        val json = Source.fromFile(config.entryFile).getLines().next()
        val mapper = JacksMapper.readValue[Map[String, String]](json)
        config.setId = mapper("id")
        config.version = mapper("version").toLong
      }
      execute(config)
    case None => failureExit("Please provide valid input.")
  }
  def failureExit(msg: String) {
    logger.error(s"MdReport failed: $msg")
    System.exit(1)
  }
  def execute(config: Config): Unit = {
    println(config)
    val rm = new RetrieveMetrics(config.setId, config.version)
    val metrics = rm.retrieve()
    val id = config.setId
    val version = config.version
    val outDir = config.outDir
    val pw = new PrintWriter(s"$outDir/$id.$version.MdReport.csv")
    for (m <- metrics) pw.write(m + "\n")
    pw.close()
    System.exit(0)
  }
}
