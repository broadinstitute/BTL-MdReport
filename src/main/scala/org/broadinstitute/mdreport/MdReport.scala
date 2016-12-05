package org.broadinstitute.mdreport

import com.lambdaworks.jacks.JacksMapper
import com.typesafe.scalalogging.Logger
import java.io.PrintWriter
import scala.io.Source

/**
  * Created by amr on 10/19/2016.
  */
object MdReport extends App{
  private val logger = Logger("MdReport")
  private val reporters = List("SmartSeqReporter")
  def parser = {
    new scopt.OptionParser[Config]("MdReport") {
      head("MdReport", "1.0")
      opt[String]('i', "setId").valueName("<id>").optional().action((x,c) => c.copy(setId = x))
        .text("The ID of the metrics/analysis/set entry. Must supply this or an entry file.")
      opt[Long]('v', "version").valueName("version").optional().action((x,c) => c.copy(version = Some(x)))
        .text("Optional version string for the entry.")
      opt[String]('e', "entryFile").optional().action((x, c) => c.copy(entryFile = x))
        .text("If EntryCreator was used you may supply the entry file to pass along sampleId and version.")
      opt[String]('o',"outDir").valueName("<outDir>").required().action((x, c) => c.copy(outDir = x))
        .text("The directory to write the report to.")
      opt[String]('s', "sampleList").valueName("<sampleList>").optional().action((x, c) => c.copy(sampleList = x.split(',').toList))
        .text("A comma-separated list of sampleIds to include in the report.")
      opt[String]('r', "reporter").valueName("<reporter>").optional().action((x,c) => c.copy(preset = Some(x)))
        .text("Use one reporter preset from the following:".concat(reporters.toString()))
      opt[String]('d', "delimiter").valueName("<delimiter>").optional().action((x,c) => c.copy(delimiter = x))
        .text("Specify delimiter. Default is comma.")
      opt[String]('t', "test").hidden().action((_, c) => c.copy(test = true))
        .text("Enable test mode which retrieves reports from MDBeta.")
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
        config.version = Some(mapper("version").toLong)
//        if (config.sampleList.isEmpty)
//          {
//            config.sampleList = doFind(config.setId, config.version)
//          }
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
    config.preset match {
        case Some(p) => p match {
          case "SmartSeqReporter" => val ssr = new Reporters.SmartSeqReporter(config)
            ssr.run()
          case "LegacyReporter" => val lr = new Reporters.LegacyReporter(config)
            lr.run()
          case _ => failureExit("Unrecognized reporter preset specified.")
        }
        case None =>
          val lr = new Reporters.LegacyReporter(config)
          lr.run()
    }
    System.exit(0)
  }
}
