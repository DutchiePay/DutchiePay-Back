package dutchiepay.backend.domain.chat.service;

import dutchiepay.backend.domain.chat.dto.ChatMessage;
import dutchiepay.backend.domain.chat.repository.ChatRoomRepository;
import dutchiepay.backend.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public void createChatRoom() {
        ChatRoom newChat = ChatRoom.builder()
                .postId(4L)
                .maxPartInc(100)
                .maxPartIn(0)
                .build();

        chatRoomRepository.save(newChat);
    }

    public void sendToChatRoomUser(String chatRoomId, ChatMessage message) {
        ChatRoom chatRoom = chatRoomRepository.findById(Long.parseLong(chatRoomId))
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));


    }
}
