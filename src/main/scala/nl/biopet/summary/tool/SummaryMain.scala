package nl.biopet.summary.tool

import java.sql.Date

import nl.biopet.summary.{SummaryDb, SummaryDbWrite}
import nl.biopet.utils.tool.ToolCommand

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object SummaryMain extends ToolCommand {
  def main(args: Array[String]): Unit = {
    val parser = new ArgsParser
    val cmdArgs = parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)

    val db = (cmdArgs.h2File, cmdArgs.jdbc) match {
      case (Some(_), Some(_)) => throw new IllegalArgumentException("h2 file and jdbcUrl are given")
      case (Some(h2), _) => SummaryDb.openH2Summary(h2)
      case (_, Some(url)) => SummaryDb.openSummary(url)
      case _ => throw new IllegalArgumentException("h2 file or jdbcUrl not given")
    }

  cmdArgs.method match {
    case "initDb" => db.createTables()
    case _ if cmdArgs.projectName.isEmpty =>
      throw new IllegalArgumentException("Project Name should be given")
    case "addProject" => addProject(db, cmdArgs.projectName.get)
    case _ if cmdArgs.runName.isEmpty =>
      throw new IllegalArgumentException("Run Name should be given")
    case "addRun" => addRun(db, cmdArgs.projectName.get, cmdArgs.runName.get, cmdArgs.outputDir, cmdArgs.version, cmdArgs.commitHash)
    case m => throw new UnsupportedOperationException(s"Method '$m' does not exist")
  }

  }

  def addProject(db: SummaryDbWrite, projectName: String): Unit = {
    Await.result(db.getProjects(name = Some(projectName)).flatMap { _.headOption match {
      case Some(project) => throw new IllegalStateException(s"Project name '$projectName' does already exist")
      case _ => db.createProject(projectName)
    }}, Duration.Inf)
  }

  def addRun(db: SummaryDbWrite,
             projectName: String,
             runName: String,
             outputDir: Option[String],
             version: Option[String],
             commitHash: Option[String]): Unit = {
    require(outputDir.nonEmpty, "outputDir is missing")
    require(version.nonEmpty, "version is missing")
    require(commitHash.nonEmpty, "commitHash is missing")
    Await.result(db.getProjects(name = Some(projectName)).flatMap { _.headOption match {
      case Some(project) =>
        db.getRuns(protectId = Some(project.id), runName = Some(runName)).flatMap { _.headOption match {
          case Some(run) => throw new IllegalStateException(s"Run '$runName' already exist")
          case _ =>
            db.createRun(runName, project.id, outputDir.get, version.get, commitHash.get, new Date(System.currentTimeMillis()))
        }}
      case _ => throw new IllegalStateException(s"Project '$projectName' does not exist")
    }}, Duration.Inf)
  }
}
