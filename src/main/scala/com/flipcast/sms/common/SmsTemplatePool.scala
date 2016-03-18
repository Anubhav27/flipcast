package com.flipcast.sms.common

import com.flipcast.Flipcast
import com.google.common.cache.{CacheBuilder, CacheLoader}
import org.fusesource.scalate._

/**
  * @author phaneesh
  */
object SmsTemplatePool {

  val engine = new TemplateEngine

  private lazy val templateCache = CacheBuilder.newBuilder()
    .maximumSize(5000)
    .concurrencyLevel(30)
    .recordStats()
    .build(
      new CacheLoader[String, SmsTemplate]() {
        def load(key: String) = {
          val config = Flipcast.smsConfigurationProvider.config(key)
          val source = TemplateSource.fromFile(config.templatePath.get).templateType("mustache")
          SmsTemplate(source, engine.load(source))
        }
      })

  def template(config: String) = {
    templateCache.get(config)
  }
}

case class SmsTemplate(source: TemplateSource, template: Template)

