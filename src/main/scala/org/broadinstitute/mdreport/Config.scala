package org.broadinstitute.mdreport

/**
  * Created by Amr on 10/21/2016.
  */
case class Config(
                  var setId: String = "",
                  var version: Long = -999,
                  entryFile: String = "",
                  outDir: String = ""
                 )