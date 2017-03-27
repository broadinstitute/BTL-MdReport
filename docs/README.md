#GENERAL USAGE

MdReport 2.0
Usage: MdReport [options]

  -i, --setId <id>         The ID of the metrics/analysis/set entry. Must supply this or an entry file.
  -v, --version version    Optional version string for the entry.
  -e, --entryFile <value>  If EntryCreator was used you may supply the entry file to pass along sampleId and version.
  -o, --outDir <outDir>    The directory to write the report to.
  -s, --sampleList <sampleList>
                           A comma-separated list of sampleIds to include in the report.
  -r, --reporter <reporter>
                           Use one reporter preset from the following:List(SmartSeqReporter, LegacyReporter, CustomReporter)
  -d, --delimiter <delimiter>
                           Specify delimiter. Default is comma.
  -f, --rdfFile <rdfFile>  Optional report definition file for custom reports.
  --help                   Prints this help text.

A tool for generating reports from MD.

* If --sampleList is not provided, one will be queried from MD using setId
* If --reporter is not specified, LegacyReporter will be used, which gets all metrics for the given samples.
* If CustomReporter is used, an rdfFile must be supplied.

##PRESETS

usage examples:
java -jar MdReport.jar -e C:\Dev\Scala\MDReport\src\test\resources\entrycreator.json -o C:\Dev\Scala\MdReport
-r SmartSeqReporter -s "SSF1859B12_A375_AkiYoda,SSF1859A11_A375_AkiYoda"

For Custom Reports, supply an RDF file formatted as follows:

usage example:
java -jar MdReport.jar -e C:\Dev\Scala\MDReport\src\test\resources\entrycreator.json
-o C:\Dev\Scala\MdReport -r CustomReporter -f C:\Dev\Scala\MdReport\src\test\resources\rdf.tsv


RDF FILE FORMAT

<top level module>\t<submodule.field, submodule.submodule.field ...>
<top level module>\t<submodule.field, submodule.submodule.field ...>
...

For example:

PicardAlignmentSummaryAnalysis	PicardAlignmentSummaryMetrics.totalReads,PicardAlignmentSummaryMetrics.pfReads,PicardAlignmentSummaryMetrics.pctAdapter
RnaSeqQcStats	alignmentMetrics.AlignmentMetrics.mapped,alignmentMetrics.AlignmentMetrics.mappedPairs
PicardReadGcMetrics	meanGcContent

In the example above, RnaSeqQcStats is a top level module. It has several sub-modules, each which have their own fields.
Above, we've specified that we want the 'mapped' and 'mappedPairs' fields from the alignmentMetrics submodule.
PicardMreadGcMetrics does not have any submodules, so we've just specified in column 2 that we want the field
meanGcContent.

# Deploy Instructions

1) Navigate to local MdReport directory via command line tool.
2) execute 'sbt' then 'assembly'
3) If unit tests pass, navigate to MdReport/target/scala-2.11/
4) Copy MdReport-assembly-<version>.jar to Unix filesystem.
5) Move jar file to /cil/shed/apps/internal/MdReport/bin
6) chmod 755 jar file
7) In  /cil/shed/apps/internal/MdReport:

`>rm MdReport.jar` 

`>ln -s /cil/shed/apps/internal/MdReport/bin/MdReport-assembly-2017.4.0.jar MdReport.jar`

8) Done! 