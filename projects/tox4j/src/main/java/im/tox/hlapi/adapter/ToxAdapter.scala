package im.tox.hlapi.adapter

import im.tox.hlapi.action.NetworkAction.ToxInitAction
import im.tox.hlapi.adapter.parser.{ ClientNotifier, UiEventParser, RequestParser, NetworkEventParser }
import im.tox.hlapi.adapter.performer.DiskIOActionPerformer
import im.tox.hlapi.event.UiEvent.ToxInitEvent
import im.tox.hlapi.event.{ UiEvent, NetworkEvent }
import im.tox.hlapi.listener.ToxClientListener
import im.tox.hlapi.performer.NetworkActionPerformer
import im.tox.hlapi.request.{ Reply, Request }
import im.tox.hlapi.state.CoreState.ToxState
import im.tox.tox4j.impl.jni.ToxCoreImpl

import RequestParser._

import scala.collection.immutable.Queue

@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Null"))
final class ToxAdapter(toxClientListener: ToxClientListener) {

  private var state: ToxState = null
  private var tox: ToxCoreImpl[Queue[NetworkEvent]] = null

  def acceptUiEvent(e: UiEvent): Unit = {
    if (state == null) {
      println("Please init a new tox session first.")
    }
    val decision = UiEventParser.parse(state, e)
    state = {
      decision._2 match {
        case Some(action) => NetworkActionPerformer.perform(action, tox, state)
        case None         => state
      }
    }
  }

  def initToxSession(e: ToxInitEvent): Unit = {
    val action = ToxInitAction(e.options, e.toxClientListener)
    val result = NetworkActionPerformer.initConnection(action)
    tox = result._1
    state = result._2
  }

  def acceptNetworkEvent(e: NetworkEvent): Unit = {
    val decision = NetworkEventParser.parse(state, e)
    state = {
      decision._2 match {
        case Some(action) => DiskIOActionPerformer.perform(state)
        case None         => state
      }
    }
    ClientNotifier.notify(toxClientListener, e)
  }

  def acceptRequest(request: Request): Reply = {
    parseRequest(request, state)
  }

}

