package com.flipcast.push.mariadb.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class MessageHistorySpec extends Specification {

  "MessageHistory" should {

    val mh = MessageHistory.syntax("mh")

    "find by primary keys" in new AutoRollback {
      val maybeFound = MessageHistory.find("MyString")
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = MessageHistory.findBy(sqls.eq(mh.id, "MyString"))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = MessageHistory.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = MessageHistory.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = MessageHistory.findAllBy(sqls.eq(mh.id, "MyString"))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = MessageHistory.countBy(sqls.eq(mh.id, "MyString"))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = MessageHistory.create(id = "MyString", configName = "MyString", deviceId = "MyString", osName = "MyString", osVersion = "MyString", brand = "MyString", model = "MyString", appName = "MyString", appVersion = "MyString", created = DateTime.now, messageData = "MyString")
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = MessageHistory.findAll().head
      // TODO modify something
      val modified = entity
      val updated = MessageHistory.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = MessageHistory.findAll().head
      MessageHistory.destroy(entity)
      val shouldBeNone = MessageHistory.find("MyString")
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = MessageHistory.findAll()
      entities.foreach(e => MessageHistory.destroy(e))
      val batchInserted = MessageHistory.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
