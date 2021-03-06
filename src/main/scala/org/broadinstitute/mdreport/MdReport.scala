package org.broadinstitute.mdreport
import com.lambdaworks.jacks.JacksMapper
import com.typesafe.scalalogging.Logger
import scopt.OptionParser
import scala.io.Source

/**
  * Created by amr on 10/19/2016.
  */
object MdReport extends App{
  private val logger = Logger("MdReport")
  private val reporters = List("SmartSeqReporter, LegacyReporter, CustomReporter")
  def parser: OptionParser[Config] = {
    new scopt.OptionParser[Config]("MdReport") {
      head("MdReport: a tool for pulling delimitted reports from MD.")
      opt[String]('i', "setId").valueName("<id>").optional().action((x,c) => c.copy(setId = Some(x)))
        .text("The ID of the metrics/analysis/set entry. Must supply this or an entry file.")
      opt[Long]('v', "version").valueName("<version>").optional().action((x,c) => c.copy(version = Some(x)))
        .text("Optional version string for the entry.")
      opt[String]('e', "entryFile").valueName("<entryFile>").optional().action((x, c) => c.copy(entryFile = Some(x)))
        .text("If EntryCreator was used you may supply the entry file to pass along sampleId and version.")
      opt[String]('o',"outDir").valueName("<outDir>").required().action((x, c) => c.copy(outDir = x))
        .text("The directory to write the report to.")
      opt[String]('s', "sampleList").valueName("<sampleList>").optional().action((x, c) =>
        c.copy(sampleList = Some(x.split(",").toList)))
        .text("A comma-separated list of sampleIds to include in the report. Default uses all samples for setId.")
      opt[String]('r', "reporter").valueName("<reporter>").optional().action((x,c) => c.copy(preset = Some(x)))
        .text("Use one reporter preset from the following:".concat(reporters.toString()))
      opt[String]('d', "delimiter").valueName("<delimiter>").optional().action((x,c) => c.copy(delimiter = x))
        .text("Specify delimiter. Default is comma.")
      opt[String]('f', "rdfFile").valueName("<rdfFile>").optional().action((x, c) => c.copy(rdfFile = Some(x)))
        .text("Optional report definition file for custom reports.")
      opt[String]('S', "sampleFile").valueName("<sampleFile>").optional().action((x, c) => c.copy(sampleFile = Some(x)))
        .text("Optional input sample TSV used to initiate pipeline. Used for populating barcode in report.")
      opt[String]('H', "HOST").valueName("<host url>").optional().action((x, c) => c.copy(host = x))
        .text("Optional. Specify database host. Default is http:\\\\btllims.broadinstitute.org.")
      opt[Int]('P', "PORT").valueName("<port>").optional().action((x, c) => c.copy(port = x))
        .text("Optional database host port. Default is 9100. Use 9101 for MdBeta.")
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
          if (config.version.isDefined || config.setId.isDefined)
            failureExit("Do not specify version or id with flags when using --entryFile.")
          val json = Source.fromFile(e).getLines().next()
          val mapper = JacksMapper.readValue[Map[String, String]](json)
          config.setId = Some(mapper("id"))
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
