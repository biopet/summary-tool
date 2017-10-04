package nl.biopet.tools.summary

import java.io.File

case class Args(h2File: Option[File] = None,
                jdbc: Option[String] = None,
                projectName: Option[String] = None,
                runName: Option[String] = None,
                method: String = null,
                outputDir: Option[String] = None,
                version: Option[String] = None,
                commitHash: Option[String] = None,
                samplesConfigFile: Option[File] = None)
