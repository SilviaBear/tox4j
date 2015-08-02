package im.tox.hlapi.adapter

import im.tox.hlapi.adapter.NetworkActionPerformer.tox
import im.tox.hlapi.event.Event
import im.tox.hlapi.event.Event.SelfEventType
import im.tox.hlapi.event.SelfEvent.{ GetSelfPublicKeyEvent, AddToFriendList }
import im.tox.hlapi.state.ConnectionState._
import im.tox.hlapi.state.CoreState
import CoreState._
import im.tox.hlapi.state.FriendState.Friend
import im.tox.tox4j.core.options.{ ProxyOptions, SaveDataOptions, ToxOptions }
import im.tox.tox4j.impl.jni.ToxCoreImpl

import scala.annotation.tailrec

object InitiateConnection {

  var eventLoop: Thread = new Thread()

  def acceptConnectionAction(state: ToxState, status: ConnectionStatus): ToxState = {
    status match {
      case Connect(connectionOptions) => {
        val toxOption: ToxOptions = {
          val p = connectionOptions.proxyOption
          val proxy = {
            p match {
              case p: Http    => ProxyOptions.Http(p.proxyHost, p.proxyPort)
              case p: Socks5  => ProxyOptions.Socks5(p.proxyHost, p.proxyPort)
              case p: NoProxy => ProxyOptions.None
            }
          }
          val s = connectionOptions.saveDataOption
          val saveData = s match {
            case s: NoSaveData => SaveDataOptions.None
            case s: ToxSave    => SaveDataOptions.ToxSave(s.data)
          }
          ToxOptions(connectionOptions.enableIPv6, connectionOptions.enableUdp, proxy, saveData = saveData)
        }
        tox = new ToxCoreImpl[ToxState](toxOption)
        for (friendNumber <- tox.getFriendList) {
          ToxAdapter.acceptEvent(SelfEventType(AddToFriendList(friendNumber, Friend())))
        }
        ToxAdapter.acceptEvent(SelfEventType(GetSelfPublicKeyEvent()))
        eventLoop = new Thread(new Runnable() {
          @tailrec
          override def run(): Unit = {
            mainLoop(state)
          }
        })

        eventLoop.start()
        state
      }
      case Disconnect() => {
        eventLoop.interrupt()
        tox.close()
        tox = null
        eventLoop.join()
        state
      }
    }
  }

  def mainLoop(state: ToxState): Unit = {
    Thread.sleep(tox.iterationInterval)
    val nextState = tox.iterate(state)
    mainLoop(nextState)
  }
}