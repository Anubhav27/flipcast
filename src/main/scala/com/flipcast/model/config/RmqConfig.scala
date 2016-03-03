package com.flipcast.model.config

import com.flipcast.Flipcast
import com.typesafe.config.Config

/**
  * Model to represent RabbitMQ configuration
  *
  * @param config Sub config that resolves to rmq configuration
  * @author Phaneesh Nagaraja
  */
case class RmqConfig (config: Config) {

  def hosts : List[String] = {
    try {
      config.getString("hosts").split(",").toList
    } catch {
      case ex: Exception =>
        Flipcast.log.error("Error reading RMQ host config", ex)
        List.empty[String]
    }
  }

  def vhost :  String = {
    try {
      config.getString("vhost")
    } catch {
      case ex: Exception => "/"
    }
  }

  def user : String = {
    try {
      config.getString("user")
    } catch {
      case ex: Exception => "guest"
    }
  }

  def pass : String = {
    try {
      config.getString("pass")
    } catch {
      case ex: Exception => "guest"
    }
  }

  def reconnectDelay = {
    try {
      config.getInt("reconnectDelay")
    } catch {
      case ex: Exception => 1000
    }
  }
}