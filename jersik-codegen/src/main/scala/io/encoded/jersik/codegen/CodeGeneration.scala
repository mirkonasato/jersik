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
package io.encoded.jersik.codegen

object CodeGeneration {

  implicit class AutoIndented(val stringContext: StringContext) extends AnyVal {

    def ai(args: Any*): String = {
      stringContext.checkLengths(args)
      val parts = stringContext.parts.iterator
      val params = args.iterator
      val builder = new StringBuilder(StringContext.treatEscapes(parts.next()))
      while (params.hasNext) {
        val spaces = detectCurrentIndent(builder)
        builder.append(indent(spaces, params.next.toString))
        builder.append(StringContext.treatEscapes(parts.next()))
      }
      builder.toString.trim
    }

    private def indent(spaces: String, block: String): String =
      if (block.isEmpty()) block
      else {
        val lines = block.lines.toList
        (lines.head :: lines.tail.map(indentLine(spaces))).mkString("\n")
      }

    private def indentLine(spaces: String)(line: String) =
      if (line.isEmpty) line else spaces + line

    private def detectCurrentIndent(source: StringBuilder) = {
      val index = source.lastIndexOf('\n') + 1
      val lastLine = source.substring(index)
      lastLine.takeWhile(_.isSpaceChar)
    }

  }

  def mapJoin[T](xs: Traversable[T], sep: String)(f: T => String): String =
    xs.map(f).mkString(sep)

}
