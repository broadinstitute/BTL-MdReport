package org.broadinstitute.mdreport

import scala.collection.mutable
import scala.io.Source

/**
  * Created by Amr Abouelleil on 3/21/2017.
  */
object Barcodes {

//  def get_barcode()

  def get_barcodes(file: String): mutable.LinkedHashMap[String, Any] = {
    val lines = Source.fromFile(file).getLines().filterNot(_.isEmpty()).toList
    val map: mutable.LinkedHashMap[String, Any] = mutable.LinkedHashMap(
      "indexBarcode1" -> None,
      "indexBarcode2" -> None
    )
    map
  }
}
