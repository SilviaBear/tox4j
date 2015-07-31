package im.tox.gui.events

import java.awt.event.{ActionEvent, ActionListener}
import im.tox.hlapi.adapter.ToxAdapter
import im.tox.hlapi.entity.Event
import im.tox.gui.MainView
import im.tox.gui.MainView._
import Event._
import ToxAdapter._

final class SetNicknameButtonOnAction(toxGui: MainView) extends ActionListener {

  override def actionPerformed(event: ActionEvent): Unit = {
    acceptEvent(SetNicknameEvent(toxGui.setNicknameField.getText.getBytes))
  }

}