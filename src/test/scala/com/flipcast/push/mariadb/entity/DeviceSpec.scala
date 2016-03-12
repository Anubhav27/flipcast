package com.flipcast.push.mariadb.entity

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._
import org.joda.time.{DateTime}


class DeviceSpec extends Specification {

  "Device" should {

    val d = Device.syntax("d")

    "find by primary keys" in new AutoRollback {
      val maybeFound = Device.find("MyString")
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = Device.findBy("default", "id = 'test'")
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = Device.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = Device.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = Device.findAllBy(sqls.eq(d.id, "MyString"))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = Device.countBy("default", "deleted = false")
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = Device.create(id = "MyString", configName = "MyString", deviceId = "MyString", osName = "MyString", osVersion = "MyString", brand = "MyString", model = "MyString", appName = "MyString", appVersion = "MyString", created = DateTime.now, updated = DateTime.now, deleted = false)
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = Device.findAll().head
      // TODO modify something
      val modified = entity
      val updated = Device.save(modified.id, modified.configName, modified.deviceId, modified.cloudMessagingId.get, modified.osName,
        modified.osVersion, modified.brand, modified.model, modified.appName, modified.appVersion)
      updated should beTrue
    }
    "destroy a record" in new AutoRollback {
      val entity = Device.findAll().head
      Device.destroy(entity)
      val shouldBeNone = Device.find("MyString")
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = Device.findAll()
      entities.foreach(e => Device.destroy(e))
      val batchInserted = Device.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
