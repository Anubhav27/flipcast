package com.flipcast.model.config

import com.typesafe.config.Config

/**
  * @author phaneesh
  */
case class MariadbConfig(config: Config) {
  def hosts: List[String] = {
    try {
      config.getString("host").split(",").toList
    } catch {
      case ex: Exception => List.empty[String]
    }
  }

  def database: String = {
    config.getString("database")
  }

  def user: String = {
    try {
      config.getString("user")
    } catch {
      case ex: Exception => "root"
    }
  }

  def password: String = {
    try {
      config.getString("password")
    } catch {
      case ex: Exception => ""
    }
  }

  def connectTimeout: Int = {
    try {
      config.getInt("connectTimeout")
    } catch {
      case ex: Exception => 10000
    }
  }

  def driver: String = {
    try {
      config.getString("driver")
    } catch {
      case ex: Exception => "org.mariadb.jdbc.Driver"
    }
  }

  def minConnectionPoolSize: Int = {
    try {
      config.getInt("minConnectionPoolSize")
    } catch {
      case ex: Exception => 10
    }
  }

  def maxConnectionPoolSize: Int = {
    try {
      config.getInt("maxConnectionPoolSize")
    } catch {
      case ex: Exception => 10
    }
  }

  def validationQuery: String = {
    try {
      config.getString("validationQuery")
    } catch {
      case ex: Exception => "SELECT 1"
    }
  }

  def connectionPoolFactoryName: String = {
    try {
      config.getString("connectionPoolFactoryName")
    } catch {
      case ex: Exception => "SELECT 1"
    }
  }
}
