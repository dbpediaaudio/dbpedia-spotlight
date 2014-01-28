package org.dbpedia.spotlight.util

import scala.io.Source
import java.io.{FileWriter, File}

class OCCS {

  var surfaceForm: String = ""
  var uri: String = ""
  var label: String = ""
  var context: String = ""
  var count: Int = 0


  def this(surfaceForm: String, uri: String, label: String, context: String, count: Int) = {
    this();

    this.surfaceForm = surfaceForm
    this.uri = uri
    this.label = label
    this.context = context
    this.count = count
  }


}

object MergeOccsURI {

  def main(args: Array[String]) {

    //args(0) SameAsFile
    //args(1) OCCS
    //args(2) output occs

    var map = Source.fromFile(args(0)).getLines().map(line => {
      (line.split("> <")(2).replace("> .", ""), line.split("> <")(0).replace("<", ""))
    }).toMap

    val buffer = new StringBuilder

    var processed = 0

    Source.fromFile(args(1)).getLines().foreach(line => {

      buffer.append(line.split("\t")(0) + "\t" +
        getURI(map, line.split("\t")(1)) + "\t" +
        line.split("\t")(2) + "\t" +
        line.split("\t")(3) + "\t" +
        (line.split("\t")(line.split("\t").size - 1)).toInt + "\n")

      processed += 1

      if (processed % 1000 == 0) {
        writeToFile(args(2), buffer.toString())
        buffer.clear()
        println(" %d rows processed".format(processed))
      }


    }
    )


    println("done!")


  }


  def getURI(map: Map[String, String], uri: String): String = {
    map.getOrElse(uri, uri)
  }

  def writeToFile(p: String, s: String) {
    val fw = new FileWriter(p, true);
    fw.write(s);
    fw.close()

  }

}
