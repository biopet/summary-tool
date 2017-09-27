package nl.biopet.summary.tool

import java.io.{File, PrintWriter}
import java.sql.Date

import nl.biopet.summary.SummaryDb
import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test
import play.api.libs.json.{JsString, Json}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class SummaryMainTest extends TestNGSuite with Matchers {

  @Test
  def testUnknownMethod(): Unit = {
    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()

    intercept[UnsupportedOperationException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "--method", "I_do_not_exist", "-p", "test", "-r", "name"))
    }.getMessage shouldBe "Method 'I_do_not_exist' does not exist"
  }

  @Test
  def testBothInput(): Unit = {
    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()

    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("--jdbcUrl", s"jdbc:h2:${dbFile.getAbsolutePath}", "-h2", dbFile.getAbsolutePath, "--method", "addProject", "-p", "test"))
    }.getMessage shouldBe "h2 file and jdbcUrl are given"
  }

  @Test
  def testFileInit(): Unit = {
    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath))
    }
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("--method", "initDb"))
    }.getMessage shouldBe "h2 file or jdbcUrl not given"
    SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "--method", "initDb"))

    val db = SummaryDb.openReadOnlyH2Summary(dbFile)
    require(db.tablesExist(), "Tables are missing from database")
  }

  @Test
  def testUrlInit(): Unit = {
    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("--jdbcUrl", s"jdbc:h2:${dbFile.getAbsolutePath}"))
    }
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("--method", "initDb"))
    }.getMessage shouldBe "h2 file or jdbcUrl not given"
    SummaryMain.main(Array("--jdbcUrl", s"jdbc:h2:${dbFile.getAbsolutePath}", "--method", "initDb"))

    val db = SummaryDb.openReadOnlyH2Summary(dbFile)
    require(db.tablesExist(), "Tables are missing from database")
  }

  @Test
  def testAddProject(): Unit = {
    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "--method", "addProject"))
    }.getMessage shouldBe "Project Name should be given"
    SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "--method", "addProject"))

    val db = SummaryDb.openReadOnlyH2Summary(dbFile)

    val projects = Await.result(db.getProjects(), Duration.Inf)

    projects.size shouldBe 1
    projects.head.name shouldBe "test"

    intercept[IllegalStateException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "--method", "addProject"))
    }.getMessage shouldBe "Project name 'test' does already exist"
  }

  @Test
  def testAddRun(): Unit = {
    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()
    val db = SummaryDb.openH2Summary(dbFile)
    db.createTables()

    Await.result(db.getRuns(), Duration.Inf).size shouldBe 0

    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "--method", "addRun"))
    }.getMessage shouldBe "Project Name should be given"
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "--method", "addRun"))
    }.getMessage shouldBe "Run Name should be given"
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addRun"))
    }.getMessage shouldBe "requirement failed: outputDir is missing"
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addRun", "--outputDir", "test"))
    }.getMessage shouldBe "requirement failed: version is missing"
    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addRun", "--outputDir", "test", "--runVersion", "test"))
    }.getMessage shouldBe "requirement failed: commitHash is missing"
    intercept[IllegalStateException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addRun", "--outputDir", "test", "--runVersion", "test", "--commitHash", "test"))
    }.getMessage shouldBe "Project 'test' does not exist"

    val pipelineId = Await.result(db.createProject("test"), Duration.Inf)

    SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "name", "--method", "addRun", "--outputDir", "dir", "--runVersion", "version", "--commitHash", "hash"))

    intercept[IllegalStateException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "name", "--method", "addRun", "--outputDir", "dir", "--runVersion", "version", "--commitHash", "hash"))
    }.getMessage shouldBe "Run 'name' already exist"

    val runs = Await.result(db.getRuns(), Duration.Inf)
    runs.size shouldBe 1
    runs.head.name shouldBe "name"
    runs.head.outputDir shouldBe "dir"
    runs.head.commitHash shouldBe "hash"
    runs.head.version shouldBe "version"
    runs.head.projectId shouldBe pipelineId
  }

  val yamlSamples: String =
    """
      |samples:
      |  sample1:
      |    tags:
      |      key: value
      |    R1: test1
      |    libraries:
      |      lib1:
      |        tags:
      |          key: value
      |        R1: test2
      |        readgroups:
      |          rg1:
      |            tags:
      |              key: value
      |            R1: test3
      |      lib2:
      |        R1: test5
      |  sample2:
      |    R1: test4
      |""".stripMargin

  @Test
  def testSamples(): Unit = {
    val configFile = File.createTempFile("config.", ".yaml")
    configFile.deleteOnExit()
    val writer = new PrintWriter(configFile)
    writer.println(yamlSamples)
    writer.close()

    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()
    val db = SummaryDb.openH2Summary(dbFile)
    db.createTables()

    intercept[IllegalStateException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addSamples", "--samplesConfigFile", configFile.getAbsolutePath))
    }.getMessage shouldBe "Project 'test' does not exist"

    val projectId = Await.result(db.createProject("test"), Duration.Inf)

    intercept[IllegalStateException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addSamples", "--samplesConfigFile", configFile.getAbsolutePath))
    }.getMessage shouldBe "Run 'test' does not exist"

    val runId = Await.result(db.createRun("test", projectId, "test", "test", "test", new Date(System.currentTimeMillis())), Duration.Inf)

    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addSamples"))
    }.getMessage shouldBe "requirement failed: sample config file required"

    SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addSamples", "--samplesConfigFile", configFile.getAbsolutePath))

    val samples = Await.result(db.getSamples(), Duration.Inf)
    samples.size shouldBe 2
    samples.map(_.name).sorted shouldBe Seq("sample1", "sample2").sorted
    val sample1Id = Await.result(db.getSampleId(runId, "sample1"), Duration.Inf).get
    (Await.result(db.getSampleTags(sample1Id), Duration.Inf).get \ "key").get shouldBe JsString("value")

    val libraries = Await.result(db.getLibraries(), Duration.Inf)
    libraries.size shouldBe 2
    libraries.map(c => (c.name, c.sampleId)).sorted shouldBe Seq(("lib1", sample1Id), ("lib2", sample1Id)).sorted
    val lib1Id = Await.result(db.getLibraryId(sample1Id, "lib1"), Duration.Inf).get
    (Await.result(db.getLibraryTags(lib1Id), Duration.Inf).get \ "key").get shouldBe JsString("value")

    val readgroups = Await.result(db.getReadgroups(), Duration.Inf)
    readgroups.size shouldBe 1
    readgroups.map(c => (c.name, c.libraryId)).sorted shouldBe Seq(("rg1", lib1Id)).sorted
    Json.parse(readgroups.head.tags.get) shouldBe Json.obj("key" -> "value")
  }

  @Test
  def testRunAndSamples(): Unit = {
    val configFile = File.createTempFile("config.", ".yaml")
    configFile.deleteOnExit()
    val writer = new PrintWriter(configFile)
    writer.println(yamlSamples)
    writer.close()

    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()
    val db = SummaryDb.openH2Summary(dbFile)
    db.createTables()

    val projectId = Await.result(db.createProject("test"), Duration.Inf)

    SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "-r", "test", "--method", "addRunAndSamples", "--samplesConfigFile", configFile.getAbsolutePath, "--outputDir", "dir", "--runVersion", "version", "--commitHash", "hash"))
    val samples = Await.result(db.getSamples(), Duration.Inf)
    samples.size shouldBe 2
    val libraries = Await.result(db.getLibraries(), Duration.Inf)
    libraries.size shouldBe 2
    val readgroups = Await.result(db.getReadgroups(), Duration.Inf)
    readgroups.size shouldBe 1
  }
}
