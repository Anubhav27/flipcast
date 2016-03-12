package com.flipcast.push.mariadb.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class SidelineMessageSpec extends Specification {

  "SidelineMessage" should {

    val sm = SidelineMessage.syntax("sm")

    "find by primary keys" in new AutoRollback {
      val maybeFound = SidelineMessage.find("MyString")
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = SidelineMessage.findBy(sqls.eq(sm.id, "MyString"))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = SidelineMessage.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = SidelineMessage.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = SidelineMessage.findAllBy(sqls.eq(sm.id, "MyString"))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = SidelineMessage.countBy(sqls.eq(sm.id, "MyString"))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = SidelineMessage.create(id = "MyString", configName = "MyString", configType = "MyString", created = DateTime.now)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = SidelineMessage.findAll().head
      // TODO modify something
      val modified = entity
      val updated = SidelineMessage.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = SidelineMessage.findAll().head
      SidelineMessage.destroy(entity)
      val shouldBeNone = SidelineMessage.find("MyString")
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = SidelineMessage.findAll()
      entities.foreach(e => SidelineMessage.destroy(e))
      val batchInserted = SidelineMessage.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
