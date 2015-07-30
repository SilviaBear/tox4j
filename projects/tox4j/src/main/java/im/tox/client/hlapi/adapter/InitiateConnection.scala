package im.tox.client.hlapi.adapter

import im.tox.client.hlapi.adapter.ToxAdapter._
import im.tox.client.hlapi.entity.{ CoreState, Event }
import Event.{ AddToFriendList, GetSelfPublicKeyEvent }
import CoreState._
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
        tox = new ToxCoreImpl[Unit](toxOption)
        for (friendNumber <- tox.getFriendList) {
          ToxAdapter.parseSelfEvent(state, AddToFriendList(friendNumber))
        }
        ToxAdapter.parseSelfEvent(state, GetSelfPublicKeyEvent())
        eventLoop = new Thread(new Runnable() {
          @tailrec
          override def run(): Unit = {
            Thread.sleep(tox.iterationInterval)
            tox.iterate(())
            run()
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
}