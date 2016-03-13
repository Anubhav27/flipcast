package com.flipcast.push.model.requests

import com.flipcast.model.requests.FlipcastRequest

import scala.reflect.ClassTag

/**
 * Model class to represent a push history event
 */
case class RecordPushHistoryRequest[T <: FlipcastRequest: ClassTag](configName: String, key: String, message: T)
