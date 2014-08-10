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
package io.encoded.jersik.scala.codegen

import java.io.File
import java.io.FileWriter

import scala.io.Source

import io.encoded.jersik.schema.SchemaParser

object ScalaCodeGenerator {

  def generateModule(schemaFile: File, targetDir: File, generateClient: Boolean = true, generateServer: Boolean = false): Unit = {
    val source = Source.fromFile(schemaFile).mkString("")
    val parseResult = SchemaParser.parseModule(source)
    if (!parseResult.successful)
      throw new RuntimeException("Could not parse "+ schemaFile +": "+ parseResult)
    val module = parseResult.get
    val moduleDir = new File(targetDir, module.name.replace('.', File.separatorChar))
    moduleDir.mkdirs()
    for (enum <- module.enums) {
      val enumFile = new File(moduleDir, enum.name +".scala")
      val enumCode = new ScalaEnum(module.name, enum).toCode
      writeFile(enumFile, enumCode)
    }
    for (struct <- module.structs) {
      val structFile = new File(moduleDir, struct.name +".scala")
      val structCode = new ScalaStruct(module.name, struct).toCode
      writeFile(structFile, structCode)
      val structCodecFile = new File(moduleDir, struct.name +"Codec.scala")
      val structCodecCode = new ScalaStructCodec(module, struct).toCode
      writeFile(structCodecFile, structCodecCode)
    }
    for (service <- module.services) {
      val serviceFile = new File(moduleDir, service.name +".scala")
      val serviceCode = new ScalaService(module.name, service).toCode
      writeFile(serviceFile, serviceCode)
      if (generateServer) {
        val operationsFile = new File(moduleDir, service.name +"Operations.scala")
        val operationsCode = new ScalaServiceOperations(module.name, service).toCode
        writeFile(operationsFile, operationsCode)
      }
      if (generateClient) {
        val clientFile = new File(moduleDir, service.name +"Client.scala")
        val clientCode = new ScalaServiceClient(module.name, service).toCode
        writeFile(clientFile, clientCode)
      }
    }
  }

  private def writeFile(file: File, body: String) = {
    val writer = new FileWriter(file)
    writer.write(body)
    writer.close()
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("USAGE: ScalaCodeGenerator <schema-file> <target-dir>")
      sys.exit(255)
    }
    val Array(schemaFile, targetDir) = args
    generateModule(new File(schemaFile), new File(targetDir))
  }

}
