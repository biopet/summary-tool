package nl.biopet.summary.tool

import nl.biopet.utils.tool.ToolCommand

object Main extends ToolCommand {
  def main(args: Array[String]): Unit = {
    val parser = new ArgsParser
    val cmdArgs = parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)


  }
}
