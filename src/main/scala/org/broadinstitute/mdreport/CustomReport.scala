package org.broadinstitute.mdreport
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType

import scala.collection.immutable.ListMap
import scala.collection.mutable

/**
  * Created by amr on 12/5/2016.
  */
/**
  * Contains the contents of the RDF file represented as a map.
  * @param contents A tuple of a MetricsType as specified by an RDF and the fields requested from that MetricsType.
  */
case class CustomReport (
                        contents: ListMap[MetricsType, List[String]]
                        )


