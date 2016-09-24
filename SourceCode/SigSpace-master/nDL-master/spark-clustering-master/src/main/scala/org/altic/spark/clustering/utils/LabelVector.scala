package org.altic.spark.clustering.utils

import org.apache.spark.util.Vector


class LabelVector(elements: Array[Double], val cls: String) extends Vector(elements) with Serializable {
  override def toString(): String = {
    "#" + cls + " " + super.toString()
  }

  def toJSON(clusterId: Int): String = {
    var str = new StringBuilder
    str append "{"
    for (i <- 0 until elements.length) {
      str append "attr" + i + ":" + elements(i) + ", "
    }
    str append "cls:\"" + cls + "\", "
    str append "clusterId:" + clusterId
    str append "}\n"
    str.toString()
  }
}