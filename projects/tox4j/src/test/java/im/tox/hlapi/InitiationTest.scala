package im.tox.hlapi

import im.tox.hlapi.adapter.ToxAdapter
import im.tox.hlapi.request.Reply.{ GetFriendListReply, GetSelfPublicKeyReply }
import im.tox.hlapi.request.Request.{ GetFriendListRequest, GetSelfPublicKeyRequest }
import im.tox.hlapi.state.ConnectionState.ConnectionStatus

final class InitiationTest extends BrownConyTestBase {
  override def newChatClient(name: String, friendName: String, adapter: ToxAdapter) = new ChatClient(name, friendName, adapter) {

    override def receiveFriendConnectionStatus(friendNumber: Int, connectionStatus: ConnectionStatus): Unit = {
      val publicKeyReply = selfAdapter.acceptRequest(GetSelfPublicKeyRequest())
      publicKeyReply match {
        case GetSelfPublicKeyReply(publicKey) => {
          val friendListReply = selfAdapter.acceptRequest(GetFriendListRequest())
          friendListReply match {
            case GetFriendListReply(friendList) => {
              assert(friendList.friends.size == 1)
              if (isBrown()) {
                assert(friendList.friends.seq(0).publicKey == conyPublicKey)
              } else {
                assert(friendList.friends.seq(0).publicKey == brownPublicKey)
              }
            }
          }
        }
      }
    }
  }
}
