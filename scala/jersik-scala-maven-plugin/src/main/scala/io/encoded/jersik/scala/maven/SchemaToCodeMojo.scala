//
// Copyright (c) 2014 Mirko Nasato
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package io.encoded.jersik.scala.maven

import java.io.File
import java.io.FilenameFilter
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import io.encoded.jersik.scala.codegen.ScalaCodeGenerator
import org.apache.maven.plugins.annotations.LifecyclePhase

@Mojo(name = "schemaToCode", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
class SchemaToCodeMojo extends AbstractMojo {

  @Parameter(readonly = true, defaultValue = "${project}")
  var project: MavenProject = _

  @Parameter(defaultValue = "${project.basedir}/src/main/jersik")
  var inputPath: File = _

  @Parameter(defaultValue = "${project.build.directory}/generated-sources/jersik/scala")
  var targetPath: File = _

  @Parameter(defaultValue = "true")
  var generateClient: Boolean = _

  @Parameter(defaultValue = "false")
  var generateServer: Boolean = _

  val log = getLog()

  override def execute(): Unit = {
    log.info("Generating Jersik Scala sources in "+ targetPath)
    val inputFiles = inputPath.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".jidl")
    })
    for (inputFile <- inputFiles) {
      log.info("Parsing "+ inputFile)
      ScalaCodeGenerator.generateModule(inputFile, targetPath, generateClient, generateServer)
    }
    project.addCompileSourceRoot(targetPath.getCanonicalPath)
  }

}
