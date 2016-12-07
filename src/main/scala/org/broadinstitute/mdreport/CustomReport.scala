package org.broadinstitute.mdreport
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType
import scala.collection.immutable.ListMap

/**
  * Created by amr on 12/5/2016.
  */
/**
  * Contains the contents of the RDF file represented as a map.
  * @param contents A ListMap MetricsType as specified by an RDF and the fields requested from that MetricsType.
  */
case class CustomReport (
                        contents: ListMap[MetricsType, List[String]]
                        )


