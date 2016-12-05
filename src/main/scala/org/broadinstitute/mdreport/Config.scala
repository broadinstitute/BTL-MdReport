package org.broadinstitute.mdreport

/**
  * Created by Amr on 10/21/2016.
  */
case class Config(
                  var setId: String = "",
                  var version: Option[Long] = None,
                  preset: Option[String] = None,
                  entryFile: Option[String] = None,
                  outDir: String = "",
                  metricsList: String = "",
                  sampleList: List[String] = List(),
                  delimiter: String = ",",
                  rdfFile: Option[String] = None,
                  test: Boolean = false
                 )