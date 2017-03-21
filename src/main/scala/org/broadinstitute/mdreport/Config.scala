package org.broadinstitute.mdreport

/**
  * Created by Amr on 10/21/2016.
  */
case class Config(
                  var setId: Option[String] = None,
                  var version: Option[Long] = None,
                  preset: Option[String] = None,
                  entryFile: Option[String] = None,
                  outDir: String = "",
                  metricsList: String = "",
                  sampleList: Option[List[String]] = None,
                  delimiter: String = ",",
                  rdfFile: Option[String] = None,
                  sampleFile: Option[String] = None,
                  test: Boolean = false
                 )