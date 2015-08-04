package im.tox.hlapi.adapter

import im.tox.hlapi.action.Action.{ SelfActionType, NetworkActionType }
import im.tox.hlapi.action.{ SelfAction, NetworkAction, Action }
import im.tox.hlapi.event.Event
import im.tox.hlapi.response.Response
import im.tox.hlapi.state.CoreState.ToxState
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.jni.ToxCoreImpl

import scalaz.State

import Event._
import EventParser._
import im.tox.hlapi.adapter.NetworkActionPerformer.performNetworkAction
import im.tox.hlapi.adapter.SelfActionPerformer.performSelfAction

final class ToxAdapter {

  var state: ToxState = ToxState()
  var tox: ToxCoreImpl[ToxState] = new ToxCoreImpl[ToxState](ToxOptions())
  var isInit: Boolean = false
  var eventLoop: Thread = new Thread()

  def acceptEvent(e: Event): Response = {
    val decision = parseEvent(e)
    decision.flatMap(parseAction).eval(state)
  }

  def parseEvent(e: Event): State[ToxState, Action] = {
    e match {
      case e: NetworkEventType => parseNetworkEvent(e)
      case e: UiEventType      => parseUiEvent(e)
      case e: SelfEventType    => parseSelfEvent(e)
    }
  }

  def parseAction(action: Action): State[ToxState, Response] = {
    action match {
      case networkAction: NetworkActionType => performNetworkAction(networkAction, this)
      case selfAction: SelfActionType       => performSelfAction(selfAction)
    }
  }

}

