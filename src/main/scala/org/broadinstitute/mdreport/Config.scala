package org.broadinstitute.mdreport
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.metrics.PicardAlignmentSummaryAnalysis

/**
  * Created by Amr on 10/21/2016.
  */
case class Config(
                  var setId: String = "",
                  var version: Long = -999,
                  entryFile: String = "",
                  outDir: String = "",
                  metricsList: String = ""
                 )