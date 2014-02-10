package org.dbpedia.spotlight.util

/**
 * Created with IntelliJ IDEA.
 * User: Renan
 * Date: 06/02/14
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */

import collection.mutable.HashMap
import scala.io.Source
import java.io.{PrintStream, File}
import scala.collection.mutable

class CrossLanguage {
  //val sameAsEnPtMap = new HashMap[String, String]()
  var resourceSet = Set[String]()
  var resourceMap = new mutable.HashMap[String, String]
  var labelsMap = new mutable.HashMap[String, String]

  def getLanguagePrefix(aLanguage: String): String = {
    if (aLanguage == "en") {
      "http://dbpedia"
    } else {
      "http://" + aLanguage + ".dbpedia"
    }
  }

  def fillResourceMap(aSameAsFile: String, sourceLanguage: String, targetLanguage: String, inverse: Boolean) {
    println("Filling the resource set...")
    //val resourceStream = new PrintStream("E:/CrossLanguage/output/" + targetLanguage + "/" + sourceLanguage + "_links.list", "UTF-8")
    val resourceStream = new PrintStream("/mnt/CrossLanguage/output/" + targetLanguage + "/" + sourceLanguage + "_links.list", "UTF-8")
    val languagePrefix = getLanguagePrefix(targetLanguage)

    if (!resourceMap.isEmpty) resourceMap = new mutable.HashMap[String, String]

    for (line <- Source.fromFile(aSameAsFile).getLines().drop(1)) {
      try {
        val lineArray = line.split(" ")
        val targetLanguageURI = lineArray(2).split("<")(1).dropRight(1)
        val sourceLanguageURI = lineArray(0).split("<")(1).dropRight(1)

        if (targetLanguageURI.startsWith(languagePrefix)) {
          //println("A source = " + sourceLanguageURI.reverse.split("/")(0).reverse)
          //println("O target = " + targetLanguageURI.reverse.split("/")(0).reverse)

          if (!inverse) {
            resourceMap += (sourceLanguageURI.reverse.split("/")(0).reverse -> targetLanguageURI.reverse.split("/")(0).reverse)
            resourceStream.println(sourceLanguageURI.reverse.split("/")(0).reverse + "\t" + targetLanguageURI.reverse.split("/")(0).reverse)
          } else {
            resourceMap += (targetLanguageURI.reverse.split("/")(0).reverse -> sourceLanguageURI.reverse.split("/")(0).reverse)
            resourceStream.println(targetLanguageURI.reverse.split("/")(0).reverse + "\t" + sourceLanguageURI.reverse.split("/")(0).reverse)
          }
        }
      } catch {
        case err : Exception => println("Line in the wrong format skipped.")
      }
    }
    println("Done.")
  }

  def filterOccs(anOccsFile: String, targetResourcesFile: String) {
    println("Filtering the occs file...")
    val filteredResourcesStream = new PrintStream(targetResourcesFile, "UTF-8")

    var i = 1
    for (line <- Source.fromFile(anOccsFile).getLines()) {
      val lineArray = line.split("\t")
      val currentValue = resourceMap.getOrElse(lineArray(1), "")
      if (currentValue != "") {
        filteredResourcesStream.println(lineArray(1) + "\t" + currentValue + "\t" + lineArray(2))
      }
      if (i % 1000000 == 0) println (i + " lines processed...")
      i += 1
    }

    println("Done.")
  }


  def updateSurfaceForms(aResourcesFile: String, outputDir: String) {
    println("Updating the surface forms...")
    val updaterStream = new PrintStream(outputDir + "final_map.nt", "UTF-8")

    var i = 1
    for (line <- Source.fromFile(aResourcesFile).getLines()) {
      try {
        //println("A linha = " + line)
        val lineArray = line.split("\t")
        //println("A key = " + lineArray(2).replaceAll(" ","_"))
        val currentResource = resourceMap.getOrElse(lineArray(2).replaceAll(" ","_"), "")
        //println("A resource = " + currentResource)
        if (currentResource != "") {
          updaterStream.println(lineArray(0) + "\t" + lineArray(1) + "\t" + currentResource)
        }
        if (i % 1000000 == 0) println (i + " lines processed...")
        i += 1
      } catch {
        case err : Exception => println("Line in the wrong format skipped.")
      }
    }

    println("Done.")
  }

  def extractLabels(sfMapFile: String, labelsFile: String, outputDir: String) {
    println("Extracting labels...")
    val labelStream = new PrintStream(outputDir + "labels_map.list", "UTF-8")

    if (!labelsMap.isEmpty) labelsMap = new mutable.HashMap[String, String]

    for (line <- Source.fromFile(labelsFile).getLines().drop(1)) {
      try {
        val lineArray = line.split(" ",3)
        //println("A label = " + lineArray(2))
        val label = lineArray(2).dropRight(5).replaceAll(""""""", "")
        //println("A label final = " + label)
        val resourceURI = lineArray(0).split("<")(1).dropRight(1).reverse.split("/")(0).reverse

        labelsMap += (resourceURI -> label)
      } catch {
        case err : Exception => println("Line in the wrong format skipped.")
      }
    }
    println("Done.")

    println("Saving final labels map...")
    var i = 1
    for (line <- Source.fromFile(sfMapFile).getLines()) {
      val lineArray = line.split("\t")
      val currentValue = labelsMap.getOrElse(lineArray(2), "")

      if (currentValue != "") {
        labelStream.println(lineArray(1) + "\t" + currentValue)
      }
      if (i % 1000000 == 0) println (i + " lines processed...")
      i += 1
    }
    println("Done.")
  }

  def complementContext(anOccsFile: String, aLabelsMap: String) {
    println("Complementing context of the occs.tsv file...")
    var tmpLabelsMap = new mutable.HashMap[String, String]
    for (line <- Source.fromFile(aLabelsMap).getLines()) {
      val lineArray = line.split("\t")
      tmpLabelsMap += (lineArray(0) -> lineArray(1))
    }

    val occsStream = new PrintStream("/mnt/CrossLanguage/output/pt/final_occs.tsv", "UTF-8")
    for (line <- Source.fromFile(anOccsFile).getLines()) {
      try {
        val lineArray = line.split("\t")
        val currentValue = tmpLabelsMap.getOrElse(lineArray(1).reverse.split("/")(0).reverse, "")

        if (currentValue != "") {
          lineArray(3) += (" " + currentValue)
        }

        occsStream.println(lineArray(0) + "\t" + lineArray(1).reverse.split("/")(0).reverse + "\t" + lineArray(2) + "\t" + lineArray(3) + "\t" + lineArray(4))
      } catch {
        case err : Exception => println("Line in the wrong format skipped.")
      }
    }
    println("Done.")
  }
}

object CrossLanguage {

  def main(args : Array[String]) {
    val indexingConfigFileName = args(0)
    val filteredResources = args(1)

    val config = new IndexingConfiguration(indexingConfigFileName)
    val interlanguageEnFileName = config.get("org.dbpedia.spotlight.data.interlanguageEn")
    val interlanguagePtFileName = config.get("org.dbpedia.spotlight.data.interlanguagePt")
    val enOccs = config.get("org.dbpedia.spotlight.data.enOccs")
    val ptOccs = config.get("org.dbpedia.spotlight.data.ptOccs")
    val labelsFile = config.get("org.dbpedia.spotlight.data.labels")
    val outputDir = config.get("org.dbpedia.spotlight.data.outputBaseDir")

    val crossLanguage = new CrossLanguage
    // Fills a map with all the resources from a desired language
    crossLanguage.fillResourceMap(interlanguagePtFileName, "pt", "en", inverse = true)
    // Uses the resource map to filter an occs.tsv file for all the pages with those resources. This
    // also gets all the resources from these pages
    crossLanguage.filterOccs(enOccs, filteredResources)
    // Fills a map with all the resources from a desired language
    crossLanguage.fillResourceMap(interlanguageEnFileName, "en", "pt", inverse = false)
    // Updates the map with the source language surface forms with the target language ones
    crossLanguage.updateSurfaceForms(filteredResources, outputDir)
    // Gets all the labels related to these surface forms
    crossLanguage.extractLabels(outputDir + "final_map.nt", labelsFile, outputDir)
    // Adds the labels in the respective contexts
    crossLanguage.complementContext(ptOccs, outputDir + "labels_map.list")
  }
}