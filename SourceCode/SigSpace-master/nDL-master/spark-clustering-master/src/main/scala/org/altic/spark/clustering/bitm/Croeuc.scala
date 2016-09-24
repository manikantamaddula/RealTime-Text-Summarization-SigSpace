package org.altic.spark.clustering.bitm

import org.altic.spark.clustering.utils.NamedVector
import org.apache.spark.rdd.RDD

/**
 * Company : Altic - LIPN
 * User: Tugdual Sarazin
 * Date: 06/01/14
 * Time: 12:28
 */
class Croeuc(val nbCluster: Int, datas: RDD[NamedVector]) extends BiTM(nbCluster, 1, datas, CroeucTopoFactor) {
}
