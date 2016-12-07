package org.broadinstitute.mdreport
import com.lambdaworks.jacks.JacksMapper
import com.typesafe.scalalogging.Logger
import scala.io.Source

/**
  * Created by amr on 10/19/2016.
  */
object MdReport extends App{
  private val logger = Logger("MdReport")
  private val reporters = List("SmartSeqReporter, LegacyReporter, CustomReporter")
  def parser = {
    new scopt.OptionParser[Config]("MdReport") {
      head("MdReport", "2.0")
      opt[String]('i', "setId").valueName("<id>").optional().action((x,c) => c.copy(setId = x))
        .text("The ID of the metrics/analysis/set entry. Must supply this or an entry file.")
      opt[Long]('v', "version").valueName("version").optional().action((x,c) => c.copy(version = Some(x)))
        .text("Optional version string for the entry.")
      opt[String]('e', "entryFile").optional().action((x, c) => c.copy(entryFile = Some(x)))
        .text("If EntryCreator was used you may supply the entry file to pass along sampleId and version.")
      opt[String]('o',"outDir").valueName("<outDir>").required().action((x, c) => c.copy(outDir = x))
        .text("The directory to write the report to.")
      opt[String]('s', "sampleList").valueName("<sampleList>").optional().action((x, c) =>
        c.copy(sampleList = Some(x.split(",").toList)))
        .text("A comma-separated list of sampleIds to include in the report.")
      opt[String]('r', "reporter").valueName("<reporter>").optional().action((x,c) => c.copy(preset = Some(x)))
        .text("Use one reporter preset from the following:".concat(reporters.toString()))
      opt[String]('d', "delimiter").valueName("<delimiter>").optional().action((x,c) => c.copy(delimiter = x))
        .text("Specify delimiter. Default is comma.")
      opt[String]('f', "rdfFile").valueName("<rdfFile>").optional().action((x, c) => c.copy(rdfFile = Some(x)))
        .text("Optional report definition file for custom reports.")
      opt[String]('t', "test").hidden().action((_, c) => c.copy(test = true))
        .text("Enable test mode which retrieves reports from MDBeta.")
      help("help").text("Prints this help text.")
      note("\nA tool for generating reports from MD.")
    }
  }
  parser.parse(args, Config()
  ) match {
    case Some(config) =>
      // If entry file exists, set config.setId and config.version to what's in the file.
      config.entryFile match {
        case Some(e) =>
          val json = Source.fromFile(e).getLines().next()
          val mapper = JacksMapper.readValue[Map[String, String]](json)
          config.setId = mapper("id")
          config.version = Some(mapper("version").toLong)
        case None => logger.info("No entry file specified.")
      }
      logger.info(s"Config: ${config.toString}")
      execute(config)
    case None => failureExit("Please provide valid input.")
  }
  def failureExit(msg: String) {
    logger.error(s"MdReport failed: $msg")
    System.exit(1)
  }
  def execute(config: Config): Unit = {
    // If preset is selected by user...
    config.preset match {
        // Execute the appropriate preset...
        case Some(p) => p match {
          case "SmartSeqReporter" => val ssr = new Reporters.SmartSeqReporter(config)
            logger.info("Running SmartSeqReporter Preset.")
            ssr.run()
          case "LegacyReporter" => val lr = new Reporters.LegacyReporter(config)
            lr.run()
          case "CustomReporter" =>
            config.rdfFile match {
              case Some(r) =>
                val cr = new Reporters.CustomReporter(config)
                cr.run()
              case None => failureExit("CustomReporter selected but no RDF file specified.")
            }
          case _ => failureExit("Unrecognized reporter preset specified.")
        }
          // Otherwise, run the legacy reporter.
        case None =>
          val lr = new Reporters.LegacyReporter(config)
          lr.run()
    }
    System.exit(0)
  }
}
