package org.broadinstitute.mdreport

/**
  * Created by Amr on 10/21/2016.
  */
case class Config(
                  var setId: String = "",
                  var version: Option[Long] = None,
                  entryFile: String = "",
                  outDir: String = "",
                  metricsList: String = "",
                  sampleList: Iterator[String] = Iterator(),
                  test: Boolean = false
                 )