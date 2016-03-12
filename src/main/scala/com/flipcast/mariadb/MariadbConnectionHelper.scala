package com.flipcast.mariadb

import javax.sql.DataSource

import com.flipcast.model.config.MariadbConfig
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import scalikejdbc._

/**
  * @author phaneesh
  */
object MariadbConnectionHelper {

  var database: DBSession = null

  def init() (implicit config: MariadbConfig) {
    Class.forName(config.driver)
    val hikariConfig = new HikariConfig()
    hikariConfig.setCatalog(config.database)
    hikariConfig.setDriverClassName(config.driver)
    hikariConfig.setConnectionTimeout(config.connectTimeout)
    hikariConfig.setConnectionTestQuery(config.validationQuery)
    hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s/%s", config.hosts.mkString(","), config.database))
    hikariConfig.setMaximumPoolSize(config.maxConnectionPoolSize)
    hikariConfig.setMinimumIdle(config.minConnectionPoolSize)
    hikariConfig.setPassword(config.password)
    hikariConfig.setPoolName("flipcast")
    hikariConfig.setUsername(config.user)
    val dataSource: DataSource = {
      val ds = new HikariDataSource(hikariConfig)
      ds
    }
    ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))
    database = AutoSession
  }
}
