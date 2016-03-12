package com.flipcast.push.mariadb.entity

import scalikejdbc._
import org.joda.time.{DateTime}

case class SidelineMessage(
  id: String,
  configName: String,
  configType: String,
  created: DateTime,
  messageData: Option[String] = None) {

  def save()(implicit session: DBSession = SidelineMessage.autoSession): SidelineMessage = SidelineMessage.save(this)(session)

  def destroy()(implicit session: DBSession = SidelineMessage.autoSession): Unit = SidelineMessage.destroy(this)(session)

}


object SidelineMessage extends SQLSyntaxSupport[SidelineMessage] {

  override val schemaName = Some("flipcast")

  override val tableName = "sideline_message"

  override val columns = Seq("id", "config_name", "config_type", "created", "message_data")

  def apply(sm: SyntaxProvider[SidelineMessage])(rs: WrappedResultSet): SidelineMessage = apply(sm.resultName)(rs)
  def apply(sm: ResultName[SidelineMessage])(rs: WrappedResultSet): SidelineMessage = new SidelineMessage(
    id = rs.get(sm.id),
    configName = rs.get(sm.configName),
    configType = rs.get(sm.configType),
    created = rs.get(sm.created),
    messageData = rs.get(sm.messageData)
  )

  val sm = SidelineMessage.syntax("sm")

  override val autoSession = AutoSession

  def find(id: String)(implicit session: DBSession = autoSession): Option[SidelineMessage] = {
    withSQL {
      select.from(SidelineMessage as sm).where.eq(sm.id, id)
    }.map(SidelineMessage(sm.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[SidelineMessage] = {
    withSQL(select.from(SidelineMessage as sm)).map(SidelineMessage(sm.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(SidelineMessage as sm)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[SidelineMessage] = {
    withSQL {
      select.from(SidelineMessage as sm).where.append(where)
    }.map(SidelineMessage(sm.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[SidelineMessage] = {
    withSQL {
      select.from(SidelineMessage as sm).where.append(where)
    }.map(SidelineMessage(sm.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(SidelineMessage as sm).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    id: String,
    configName: String,
    configType: String,
    created: DateTime,
    messageData: Option[String] = None)(implicit session: DBSession = autoSession): SidelineMessage = {
    withSQL {
      insert.into(SidelineMessage).columns(
        column.id,
        column.configName,
        column.configType,
        column.created,
        column.messageData
      ).values(
        id,
        configName,
        configType,
        created,
        messageData
      )
    }.update.apply()

    SidelineMessage(
      id = id,
      configName = configName,
      configType = configType,
      created = created,
      messageData = messageData)
  }

  def batchInsert(entities: Seq[SidelineMessage])(implicit session: DBSession = autoSession): Seq[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'id -> entity.id,
        'configName -> entity.configName,
        'configType -> entity.configType,
        'created -> entity.created,
        'messageData -> entity.messageData))
        SQL("""insert into sideline_message(
        id,
        config_name,
        config_type,
        created,
        message_data
      ) values (
        {id},
        {configName},
        {configType},
        {created},
        {messageData}
      )""").batchByName(params: _*).apply()
    }

  def save(entity: SidelineMessage)(implicit session: DBSession = autoSession): SidelineMessage = {
    withSQL {
      update(SidelineMessage).set(
        column.id -> entity.id,
        column.configName -> entity.configName,
        column.configType -> entity.configType,
        column.created -> entity.created,
        column.messageData -> entity.messageData
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: SidelineMessage)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(SidelineMessage).where.eq(column.id, entity.id) }.update.apply()
  }

}
