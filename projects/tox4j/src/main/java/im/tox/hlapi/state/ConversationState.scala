package im.tox.hlapi.state

import im.tox.hlapi.state.FileState.FileList
import im.tox.hlapi.state.MessageState.MessageList

import scalaz.Lens

object ConversationState {
  final case class FriendConversation(
    isTyping: Boolean = false,
    sentMessageList: MessageList = MessageList(),
    receivedMessageList: MessageList = MessageList(),
    fileList: FileList = FileList()
  )

  val ConversationSentMessageListL = Lens.lensu[FriendConversation, MessageList](
    (a, value) => a.copy(sentMessageList = value),
    _.sentMessageList
  )

  val ConversationReceivedMessageListL = Lens.lensu[FriendConversation, MessageList](
    (a, value) => a.copy(receivedMessageList = value),
    _.receivedMessageList
  )

  val conversationIsTypingL = Lens.lensu[FriendConversation, Boolean](
    (a, value) => a.copy(isTyping = value),
    _.isTyping
  )

  val conversationFileSentListL = Lens.lensu[FriendConversation, FileList](
    (a, value) => a.copy(fileList = value),
    _.fileList
  )
}
