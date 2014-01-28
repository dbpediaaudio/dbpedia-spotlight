package org.dbpedia.spotlight.util

import scala.io.Source
import java.io.File

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
    var i = 1
    var occs = Source.fromFile(args(1)).getLines().map(line => {
      (line.split("\t")(1).trim(),
        new OCCS(line.split("\t")(0), line.split("\t")(1), line.split("\t")(2), line.split("\t")(3), (line.split("\t")(line.split("\t").size - 1)).toInt))
    }
    ).toMap


    map.foreach(m => {
      val o = occs.get(m._1.trim())
      if (!o.isEmpty)
        o.get.uri = m._2
    })

    val buffer = new StringBuilder
    occs.foreach(o => {

      buffer.append(o._2.surfaceForm + "\t" +
        o._2.uri + "\t" +
        o._2.label + "\t" +
        o._2.context + "\t" +
        o._2.count + "\n")

    })

    writeToFile(args(2), buffer.toString())
    println("done!")


  }


  def writeToFile(p: String, s: String) {
    val pw = new java.io.PrintWriter(new File(p))
    try pw.write(s) finally pw.close()
  }

}
