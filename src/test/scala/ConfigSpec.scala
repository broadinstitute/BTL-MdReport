import org.broadinstitute.mdreport.Config
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by amr on 11/2/2016.
  */
class ConfigSpec extends FlatSpec with Matchers{
  val config = Config(
    setId = Some("ConfigSpec"),
    version = Some(1L),
    entryFile = Some("C:\\Dev\\Scala\\MdReport\\entrycreator.json"),
    outDir = "C:\\Dev\\Scala\\MdReport",
    metricsList = ""
  )
  "A config" should "have reassignable setId and version" in {
    config.setId = Some("Foo")
    config.version = Some(1975L)
    assert(config.setId contains "Foo")
    assert(config.version contains 1975)
    assert(config.entryFile.get == "C:\\Dev\\Scala\\MdReport\\entrycreator.json")
    assert(config.outDir == "C:\\Dev\\Scala\\MdReport")
  }
}
