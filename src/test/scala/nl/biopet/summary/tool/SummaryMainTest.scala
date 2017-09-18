package nl.biopet.summary.tool

import java.io.File

import nl.biopet.summary.SummaryDb
import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global

class SummaryMainTest extends TestNGSuite with Matchers {
  @Test
  def testAddProject(): Unit = {
    val dbFile = File.createTempFile("summary.", ".db")
    dbFile.deleteOnExit()
    val db = SummaryDb.openH2Summary(dbFile)
    db.createTables()

    Await.result(db.getProjects(), Duration.Inf).size shouldBe 0

    intercept[IllegalArgumentException] {
      SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "--method", "addProject"))
    }.getMessage shouldBe "Project Name should be given"
    SummaryMain.main(Array("-h2", dbFile.getAbsolutePath, "-p", "test", "--method", "addProject"))

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
}
