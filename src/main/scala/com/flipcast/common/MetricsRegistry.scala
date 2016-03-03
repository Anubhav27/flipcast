package com.flipcast.common

import com.codahale.metrics.jvm.{GarbageCollectorMetricSet, ThreadStatesGaugeSet, MemoryUsageGaugeSet}
import com.codahale.metrics.MetricRegistry._

/**
 * Registry for all metrics elements
 *
 * @author Phaneesh Nagaraja
 */
object MetricsRegistry {

  val metrics = new com.codahale.metrics.MetricRegistry()

  /**
    * Global Counters
    */
  val IncomingGlobal = metrics.meter(name("count", "global", "Incoming"))
  val AckGlobal = metrics.meter(name("count", "global", "Ack"))
  val RejectGlobal = metrics.meter(name("count", "global", "Reject"))
  val SidelineGlobal = metrics.meter(name("count", "global", "Sideline"))

  def registerDefaults() {
    metrics.registerAll(new MemoryUsageGaugeSet())
    metrics.registerAll(new ThreadStatesGaugeSet())
    metrics.registerAll(new GarbageCollectorMetricSet())
  }

  def timer(config: String) = {
    metrics.timer(name("latency", config, "Process"))
  }

  def meter(config: String, counterName: String) = {
    metrics.meter(name("count", config, counterName))
  }

}
