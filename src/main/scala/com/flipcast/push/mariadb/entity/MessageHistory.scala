package com.flipcast.push.mariadb.entity

import java.util.Date

import scalikejdbc._
import org.joda.time.{DateTime}

case class MessageHistory(
  id: String,
  configName: String,
  deviceId: String,
  cloudMessagingId: Option[String] = None,
  osName: String,
  osVersion: String,
  brand: String,
  model: String,
  appName: String,
  appVersion: String,
  created: DateTime,
  messageData: String) {

  def save()(implicit session: DBSession = MessageHistory.autoSession): MessageHistory = MessageHistory.save(this)(session)

  def destroy()(implicit session: DBSession = MessageHistory.autoSession): Unit = MessageHistory.destroy(this)(session)

}


object MessageHistory extends SQLSyntaxSupport[MessageHistory] {

  override val schemaName = Some("flipcast")

  override val tableName = "message_history"

  override val columns = Seq("id", "config_name", "device_id", "cloud_messaging_id", "os_name", "os_version", "brand", "model", "app_name", "app_version", "created", "message_data")

  def apply(mh: SyntaxProvider[MessageHistory])(rs: WrappedResultSet): MessageHistory = apply(mh.resultName)(rs)
  def apply(mh: ResultName[MessageHistory])(rs: WrappedResultSet): MessageHistory = new MessageHistory(
    id = rs.get(mh.id),
    configName = rs.get(mh.configName),
    deviceId = rs.get(mh.deviceId),
    cloudMessagingId = rs.get(mh.cloudMessagingId),
    osName = rs.get(mh.osName),
    osVersion = rs.get(mh.osVersion),
    brand = rs.get(mh.brand),
    model = rs.get(mh.model),
    appName = rs.get(mh.appName),
    appVersion = rs.get(mh.appVersion),
    created = rs.get(mh.created),
    messageData = rs.get(mh.messageData)
  )

  val mh = MessageHistory.syntax("mh")

  override val autoSession = AutoSession

  def find(id: String)(implicit session: DBSession = autoSession): Option[MessageHistory] = {
    withSQL {
      select.from(MessageHistory as mh).where.eq(mh.id, id)
    }.map(MessageHistory(mh.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[MessageHistory] = {
    withSQL(select.from(MessageHistory as mh)).map(MessageHistory(mh.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(MessageHistory as mh)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[MessageHistory] = {
    withSQL {
      select.from(MessageHistory as mh).where.append(where)
    }.map(MessageHistory(mh.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[MessageHistory] = {
    withSQL {
      select.from(MessageHistory as mh).where.append(where)
    }.map(MessageHistory(mh.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(MessageHistory as mh).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def pushHistory(config: String, from: Date)(implicit session: DBSession = autoSession) = {
    withSQL {
      select(sqls.count).from(MessageHistory as mh)
        .where.eq(mh.configName, config).and.gt(mh.created, new DateTime(from))
    }.map(_.long(1)).single.apply.get
  }

  def create(
    id: String,
    configName: String,
    deviceId: String,
    cloudMessagingId: Option[String] = None,
    osName: String,
    osVersion: String,
    brand: String,
    model: String,
    appName: String,
    appVersion: String,
    created: DateTime,
    messageData: String)(implicit session: DBSession = autoSession): MessageHistory = {
    withSQL {
      insert.into(MessageHistory).columns(
        column.id,
        column.configName,
        column.deviceId,
        column.cloudMessagingId,
        column.osName,
        column.osVersion,
        column.brand,
        column.model,
        column.appName,
        column.appVersion,
        column.created,
        column.messageData
      ).values(
        id,
        configName,
        deviceId,
        cloudMessagingId,
        osName,
        osVersion,
        brand,
        model,
        appName,
        appVersion,
        created,
        messageData
      )
    }.update.apply()

    MessageHistory(
      id = id,
      configName = configName,
      deviceId = deviceId,
      cloudMessagingId = cloudMessagingId,
      osName = osName,
      osVersion = osVersion,
      brand = brand,
      model = model,
      appName = appName,
      appVersion = appVersion,
      created = created,
      messageData = messageData)
  }

  def batchInsert(entities: Seq[MessageHistory])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'id -> entity.id,
        'configName -> entity.configName,
        'deviceId -> entity.deviceId,
        'cloudMessagingId -> entity.cloudMessagingId,
        'osName -> entity.osName,
        'osVersion -> entity.osVersion,
        'brand -> entity.brand,
        'model -> entity.model,
        'appName -> entity.appName,
        'appVersion -> entity.appVersion,
        'created -> entity.created,
        'messageData -> entity.messageData))
        SQL("""insert into message_history(
        id,
        config_name,
        device_id,
        cloud_messaging_id,
        os_name,
        os_version,
        brand,
        model,
        app_name,
        app_version,
        created,
        message_data
      ) values (
        {id},
        {configName},
        {deviceId},
        {cloudMessagingId},
        {osName},
        {osVersion},
        {brand},
        {model},
        {appName},
        {appVersion},
        {created},
        {messageData}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: MessageHistory)(implicit session: DBSession = autoSession): MessageHistory = {
    withSQL {
      update(MessageHistory).set(
        column.id -> entity.id,
        column.configName -> entity.configName,
        column.deviceId -> entity.deviceId,
        column.cloudMessagingId -> entity.cloudMessagingId,
        column.osName -> entity.osName,
        column.osVersion -> entity.osVersion,
        column.brand -> entity.brand,
        column.model -> entity.model,
        column.appName -> entity.appName,
        column.appVersion -> entity.appVersion,
        column.created -> entity.created,
        column.messageData -> entity.messageData
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: MessageHistory)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(MessageHistory).where.eq(column.id, entity.id) }.update.apply()
  }

}
