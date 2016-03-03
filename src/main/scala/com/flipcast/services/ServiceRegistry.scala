package com.flipcast.services

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit._

import akka.actor._
import akka.event.slf4j.Logger
import akka.util.Timeout

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
 * Service registry for keeping all the service worker actors
 * This will make sure that we are not creating actors repeatedly
 * A single instance of this registry is used in Flipcast app object
 * @param system Actor system that should be used to create actors
 *
 * @author Phaneesh Nagaraja
 */
class ServiceRegistry (implicit val system: ActorSystem) {

  val log = Logger("ServiceRegistry")

  implicit val timeout: Timeout = Duration(60, SECONDS)

  /**
   * Map of all registered actors
   */
  private val serviceCache = new ConcurrentHashMap[String, ActorRef]()

  /**
   * Register a actor into the registry
   * @param name Name of the actor that needs ot be registered
   * @param instances Total no of actor instances
   * @tparam T Actor or any of the subclasses of actor
   */
  def register[T <: Actor : ClassTag](name: String, instances: Int = 1, dispatcher: String) {
    serviceCache.containsKey(name) match {
      case true => throw new IllegalArgumentException("Duplicate service registration")
      case false =>
        instances match {
          case 1 =>
            val aRef = system.actorOf(Props[T].withDispatcher(dispatcher), name)
            serviceCache.put(name, aRef)
          case _ =>
            List.range(0, instances).foreach( e => {
              serviceCache.containsKey(name +"_" +e) match {
                case true => None
                case false =>
                  val aRef = system.actorOf(Props[T].withDispatcher(dispatcher), name +"_" +e)
                  serviceCache.put(name +"_" +e, aRef)
              }
            })
        }
    }
  }

  /**
    * Register a actor into the registry
    * @param name Name of the actor that needs ot be registered
    * @param actorHolder Actor or any of the subclasses of actor
    */
  def register(actorHolder: Props, name: String, dispatcher: String, instances: Int) {
    if(instances == 1) {
      serviceCache.containsKey(name) match {
        case true => throw new IllegalArgumentException("Duplicate service registration")
        case false =>
          val aRef = system.actorOf(actorHolder.withDispatcher(dispatcher), name)
          serviceCache.put(name, aRef)
      }
    } else {
      List.range(0, instances).foreach( e => {
        serviceCache.containsKey(name +"_" +e) match {
          case true => None
          case false =>
            val aRef = system.actorOf(actorHolder.withDispatcher(dispatcher), name +"_" +e)
            serviceCache.put(name +"_" +e, aRef)
        }
      })
    }
  }

  /**
    * De-Register a actor into the registry
    * @param name Name of the actor that needs ot be registered
    */
  def deregister(name: String, instances: Int) {
    if(instances == 1) {
      serviceCache.containsKey(name) match {
        case false => throw new IllegalArgumentException("No service registration found")
        case true =>
          val aRef = serviceCache.get(name)
          aRef ! PoisonPill
      }
    } else {
      List.range(0, instances).foreach( e => {
        serviceCache.containsKey(name +"_" +e) match {
          case false => None
          case true =>
            val aRef = serviceCache.get(name +"_" +e)
            aRef ! PoisonPill
        }
      })
    }
  }


  /**
   * Lookup method for fetching back service worker actor
   * @param name of the service actor
   * @return ActorRef for service worker
   * @throws IllegalArgumentException when a invalid name is supplied
   */
  def actor(name: String) = {
    serviceCache.containsKey(name) match {
      case true => serviceCache.get(name)
      case false => throw new IllegalArgumentException("Invalid service! Service not registered: " +name)
    }
  }
}