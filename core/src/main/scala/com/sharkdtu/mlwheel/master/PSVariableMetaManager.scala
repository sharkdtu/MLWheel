package com.sharkdtu.mlwheel.master

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable

import akka.actor.ActorRef

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.conf.PSConf

/**
 * A PSVariable metadata manager
 */
private[mlwheel] class PSVariableMetaManager(conf: PSConf)
  extends Logging {

  private val nextVariableId = new AtomicInteger(0)

  private val psVariables = new mutable.HashMap[ClientId, PSVariableMeta]()

  def addPSVector(clientId: ClientId, ): Unit = {

  }

}

private case class PSVariableMeta(variableId: Int, partitions: Array[PartitionMeta]) {
  def numPartitions: Int = partitions.length
}

private case class PartitionMeta(partitionId: Int, workers: Array[ActorRef]) {
  def copies: Int = workers.length
}
