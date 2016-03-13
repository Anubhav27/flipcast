package com.flipcast.mariadb

import javax.sql.DataSource

import com.flipcast.model.config.MariadbConfig
import com.jolbox.bonecp.{BoneCPConfig, BoneCPDataSource}
import scalikejdbc._

/**
  * @author phaneesh
  */
object MariadbConnectionHelper {

  var database: DBSession = null

  def init() (implicit config: MariadbConfig) {
    Class.forName(config.driver)
    val bonecpConfig = new BoneCPConfig()
    bonecpConfig.setDefaultCatalog(config.database)
    bonecpConfig.setConnectionTimeoutInMs(config.connectTimeout)
    bonecpConfig.setJdbcUrl(String.format("jdbc:mariadb://%s/%s", config.hosts.mkString(","), config.database))
    bonecpConfig.setMinConnectionsPerPartition(config.maxConnectionPoolSize)
    bonecpConfig.setMinConnectionsPerPartition(config.minConnectionPoolSize)
    bonecpConfig.setPartitionCount(1)
    bonecpConfig.setPassword(config.password)
    bonecpConfig.setPoolName("flipcast")
    bonecpConfig.setUsername(config.user)
    val dataSource: DataSource = {
      val ds = new BoneCPDataSource(bonecpConfig)
      ds
    }
    ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))
    database = AutoSession
  }
}
