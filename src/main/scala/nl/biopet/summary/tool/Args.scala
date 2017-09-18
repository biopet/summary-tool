package nl.biopet.summary.tool

import java.io.File

case class Args(h2File: Option[File] = None,
                jdbc: Option[String] = None,
                projectName: Option[String] = None,
                runName: Option[String] = None,
                pipelineName: Option[String] = None,
                moduleName: Option[String] = None,
                sampleName: Option[String] = None,
                libraryName: Option[String] = None,
                readgroupName: Option[String] = None,
                method: String = null,
                outputDir: Option[String] = None,
                version: Option[String] = None,
                commitHash: Option[String] = None)
