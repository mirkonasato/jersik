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
package io.encoded.jersik.java.codegen

import java.io.File
import java.io.FileWriter

import scala.io.Source

import io.encoded.jersik.schema.SchemaParser

object JavaCodeGenerator {

  def generateModule(schemaFile: File, targetDir: File, generateClient: Boolean = true, generateServer: Boolean = false): Unit = {
    val source = Source.fromFile(schemaFile).mkString("")
    val parseResult = SchemaParser.parseModule(source)
    if (!parseResult.successful)
      throw new RuntimeException("Could not parse "+ schemaFile +": "+ parseResult)
    val module = parseResult.get
    val moduleDir = new File(targetDir, module.name.replace('.', File.separatorChar))
    moduleDir.mkdirs()
    for (enum <- module.enums) {
      val enumFile = new File(moduleDir, enum.name +".java")
      val enumCode = new JavaEnum(module.name, enum).toCode
      writeFile(enumFile, enumCode)
    }
    for (struct <- module.structs) {
      val structFile = new File(moduleDir, struct.name +".java")
      val structCode = new JavaStruct(module.name, struct).toCode
      writeFile(structFile, structCode)
      val structCodecFile = new File(moduleDir, struct.name +"Codec.java")
      val structCodecCode = new JavaStructCodec(module, struct).toCode
      writeFile(structCodecFile, structCodecCode)
    }
    for (service <- module.services) {
      val serviceFile = new File(moduleDir, service.name +".java")
      val serviceCode = new JavaService(module.name, service).toCode
      writeFile(serviceFile, serviceCode)
      if (generateServer) {
        val operationMapFile = new File(moduleDir, service.name +"OperationMap.java")
        val operationMapCode = new JavaServiceOperationMap(module.name, service).toCode
        writeFile(operationMapFile, operationMapCode)
      }
      if (generateClient) {
        val clientFile = new File(moduleDir, service.name +"Client.java")
        val clientCode = new JavaServiceClient(module.name, service).toCode
        writeFile(clientFile, clientCode)
      }
    }
  }

  private def writeFile(file: File, body: String) = {
    val writer = new FileWriter(file)
    writer.write(body)
    writer.close()
  }

}
