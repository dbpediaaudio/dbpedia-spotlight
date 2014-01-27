package org.dbpedia.spotlight.util

import scala.io.Source


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

    var map = Source.fromFile(args(0)).getLines().map(line => (line.split("\t")(2), line.split("\t")(0))).toMap

    var occs = Source.fromFile(args(1)).getLines().map(line => (line.split("\t")(1),
      new OCCS(line.split("\t")(0), line.split("\t")(1), line.split("\t")(2), line.split("\t")(3), (line.split("\t")(4)).toInt))
    ).toMap


    map.foreach(m => {
      val o = occs.get(m._1)
      o.get.uri = m._2
    })

    val buffer = new StringBuilder

    occs.foreach(o => {

      buffer.append(o._2.surfaceForm + "\t" +
        o._2.uri + "\t" +
        o._2.label + "\t" +
        o._2.context + "\t" +
        o._2.count)

    })

    //TODO: Gravar o conteúdo no arquivo
    println(buffer.toString())



  }

}
