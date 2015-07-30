package im.tox.gui.events

import java.awt.event.{ ActionEvent, ActionListener }
import im.tox.client.hlapi.adapter.ToxAdapter
import im.tox.client.hlapi.entity.{CoreState, Event}
import im.tox.gui.util.InvokeLaterToxEventListener
import im.tox.tox4j.ToxCoreTestBase.readablePublicKey
import im.tox.gui.{GuiToxEventListener, MainView}
import im.tox.gui.MainView.state
import ToxAdapter._
import Event._
import CoreState._

final class ConnectButtonOnAction(toxGui: MainView) extends ActionListener {

  private def setConnectSettingsEnabled(enabled: Boolean): Unit = {
    Seq(
      toxGui.enableIPv6CheckBox,
      toxGui.enableUdpCheckBox,
      toxGui.noneRadioButton,
      toxGui.httpRadioButton,
      toxGui.socksRadioButton,
      toxGui.proxyHost,
      toxGui.proxyPort
    ).foreach(_.setEnabled(enabled))

    Seq(
      toxGui.bootstrapHost,
      toxGui.bootstrapPort,
      toxGui.bootstrapKey,
      toxGui.bootstrapButton,
      toxGui.friendId,
      toxGui.friendRequest,
      toxGui.addFriendButton,
      toxGui.actionRadioButton,
      toxGui.messageRadioButton,
      toxGui.messageText,
      toxGui.sendButton
    ).foreach(_.setEnabled(!enabled))
  }

  private def toxOptions: ConnectionOptions = {
    val proxy: ProxyOption = {
      if (toxGui.httpRadioButton.isSelected) {
        Http(toxGui.proxyHost.getText, toxGui.proxyPort.getText.toInt)
      } else if (toxGui.socksRadioButton.isSelected) {
        Socks5(toxGui.proxyHost.getText, toxGui.proxyPort.getText.toInt)
      } else {
        NoProxy()
      }
    }


    val toxSave =
      toxGui.load() match {
        case None       => NoSaveData()
        case Some(data) => ToxSave(data)
      }
    ConnectionOptions(
      toxGui.enableIPv6CheckBox.isSelected,
      toxGui.enableUdpCheckBox.isSelected,
      proxy,
      toxSave
    )
  }

  private def connect(): Unit = {
    acceptEvent(state, SetConnectionStatusEvent(Connect(toxOptions)))
    acceptEvent(state, RegisterEventListener(toxGui.toxEvents))
    toxGui.selfPublicKey.setText(readablePublicKey(state.publicKey))
    toxGui.connectButton.setText("Disconnect")
    setConnectSettingsEnabled(false)
    toxGui.addMessage("Created Tox instance; started event loop")
  }

  private def disconnect(): Unit = {
    acceptEvent(state, SetConnectionStatusEvent(Disconnect()))
    setConnectSettingsEnabled(true)
    toxGui.connectButton.setText("Connect")
    toxGui.addMessage("Disconnected")
  }

  override def actionPerformed(event: ActionEvent): Unit = {
    toxGui.connectButton.getText match {
      case "Connect" =>
        connect()
      case "Disconnect" =>
        disconnect()
    }
  }

}
