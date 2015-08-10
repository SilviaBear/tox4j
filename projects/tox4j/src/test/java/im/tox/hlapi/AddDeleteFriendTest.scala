package im.tox.hlapi

import im.tox.hlapi.adapter.ToxAdapter
import im.tox.hlapi.event.Event.UiEventType
import im.tox.hlapi.event.UiEvent.{ SendFriendRequestEvent, DeleteFriendEvent }
import im.tox.hlapi.request.Reply.GetFriendListReply
import im.tox.hlapi.request.Request.GetFriendListRequest
import im.tox.hlapi.state.ConnectionState.{ Disconnect, Connect, ConnectionStatus }
import im.tox.hlapi.state.FriendState.FriendRequestMessage

final class AddDeleteFriendTest extends BrownConyTestBase {
  override def newChatClient(name: String, expectedFriendName: String, adapter: ToxAdapter) = new ChatClient(name, expectedFriendName, adapter) {
    private var friendDeleted: Boolean = false
    override def receiveFriendConnectionStatus(friendNumber: Int, connectionStatus: ConnectionStatus): Unit = {
      connectionStatus match {
        case Connect(connectionOptions) => {
          if (!friendDeleted) {
            selfAdapter.acceptEvent(UiEventType(DeleteFriendEvent(friendNumber)))
            debug("delete friend " + expectedFriendName)
            friendDeleted = true
            val reply = selfAdapter.acceptRequest(GetFriendListRequest())
            reply match {
              case GetFriendListReply(friendList) => {
                assert(friendList.friends.size == 0)
              }
            }
          } else {
            debug("see friend appear online again")
          }
        }
        case Disconnect() => {
          if (isBrown()) {
            selfAdapter.acceptEvent(UiEventType(SendFriendRequestEvent(brownPublicKey, FriendRequestMessage("I am Brown".getBytes))))
            debug("add Cony back")
          }
        }
      }
    }
  }
}
