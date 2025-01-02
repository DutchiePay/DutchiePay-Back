package dutchiepay.backend.domain.chat.service;

import dutchiepay.backend.domain.chat.dto.*;
import dutchiepay.backend.domain.chat.exception.ChatErrorCode;
import dutchiepay.backend.domain.chat.exception.ChatException;
import dutchiepay.backend.domain.chat.repository.ChatRoomRepository;
import dutchiepay.backend.domain.chat.repository.MessageRepository;
import dutchiepay.backend.domain.community.service.MartService;
import dutchiepay.backend.domain.community.service.PurchaseService;
import dutchiepay.backend.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserChatroomService userChatroomService;
    private final MartService martService;
    private final PurchaseService purchaseService;

    /**
     * 게시글에 연결된 채팅방에 참여한다.
     * @param user 유저
     * @param postId 게시글 ID
     * @param type 게시글 타입
     * @throws ChatException 채팅방이 가득 찼을 경우
     * @throws ChatException 유효하지 않은 타입일 경우
     * @throws ChatException 사용자가 채팅방에서 차단된 경우
     */
    @Transactional
    public JoinChatRoomResponseDto joinChatRoomFromPost(User user, Long postId, String type) {
        // postId 및 type 검증
        Object post = validatePostIdAndType(postId, type);

        // 채팅방이 존재하는지 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findByPostIdAndType(postId, type.equals("share") ? "group" : "direct");

        // 존재하지 않는다면 새로운 채팅방 생성
        if (chatRoom == null) {
            ChatRoom newChatRoom = createChatRoom(post, postId, type);

            // 유저가 게시글 작성자라면 1명만 참여, 작성자가 아니라면 작성자도 포함해서 참여시킨다.
            if (post instanceof Share s) {
                if (s.getUser().getUserId().equals(user.getUserId())) {
                    userChatroomService.joinChatRoom(user, newChatRoom, "manager");
                } else {
                    userChatroomService.joinChatRoom(user, newChatRoom, "member");
                    userChatroomService.joinChatRoom(s.getUser(), newChatRoom, "manager");
                }
            } else if (post instanceof Purchase p) {
                if (p.getUser().getUserId().equals(user.getUserId())) {
                    userChatroomService.joinChatRoom(user, newChatRoom, "manager");
                } else {
                    userChatroomService.joinChatRoom(user, newChatRoom, "member");
                    userChatroomService.joinChatRoom(p.getUser(), newChatRoom, "manager");
                }
            }

            return JoinChatRoomResponseDto.of(newChatRoom.getChatroomId());
        }
        // 유저가 이미 채팅방에 속해있는 지 검증
        if (userChatroomService.alreadyJoined(user, chatRoom)) {
            throw new ChatException(ChatErrorCode.ALREADY_JOINED);
        }

        // 채팅방의 인원이 가득 찼을 경우 예외처리
        if (chatRoom.getNowPartInc() >= chatRoom.getMaxPartInc()) {
            throw new ChatException(ChatErrorCode.FULL_CHAT);
        }

        // 블랙리스트 여부 확인
        if (userChatroomService.isBanned(user, chatRoom)) {
            throw new ChatException(ChatErrorCode.USER_BANNED);
        }

        // 채팅방에 유저 참여
        userChatroomService.joinChatRoom(user, chatRoom, "member");

        return JoinChatRoomResponseDto.of(chatRoom.getChatroomId());
    }

    /**
     * 채팅방을 나간다.
     * @param user 유저
     * @param chatRoomId 채팅방 Id
     * @throws ChatException 채팅방이 존재하지 않을 경우
     * @throws ChatException 채팅방 방장이 나가려고 할 경우
     */
    @Transactional
    public void leaveChatRoom(User user, Long chatRoomId) {
        UserChatRoom ucr = userChatroomService.findByUserAndChatRoomId(user, chatRoomId);

        if (ucr.getRole().equals("manager")) {
            throw new ChatException(ChatErrorCode.MANAGER_CANNOT_LEAVE);
        }

        userChatroomService.leaveChatRoom(ucr);
        ucr.getChatroom().leave();
    }

    /**
     * 채팅방을 생성한다.
     * @param post 게시글 object
     * @param postId 게시글 postId
     * @param type 게시글 타입
     * @return ChatRoom 생성된 채팅방
     */
    private ChatRoom createChatRoom(Object post, Long postId, String type) {
        int maxPartInc = 2;

        // 마트/배달일 경우 최대 인원 데이터를 가져온다.
        if (post instanceof Share s) {
            maxPartInc = s.getMaximum();
        }

        // 채팅방을 생성한다.
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomName(post instanceof Share ? ((Share) post).getTitle() : ((Purchase) post).getTitle())
                .chatRoomImg("기본 이미지")
                .postId(postId)
                .type(type.equals("share") ? "group" : "direct")
                .maxPartInc(maxPartInc)
                .nowPartInc(0)
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    /**
     * 게시글 ID 및 타입을 검증하고 검증된 게시글 Obejct를 반환한다.
     * @param postId 게시글Id
     * @param type 게시글타입
     * @return 타입에 맞는 게시글 Object
     */
    private Object validatePostIdAndType(Long postId, String type) {
        if (type.equals("share")) return martService.findById(postId);
        else if (type.equals("purchase")) return purchaseService.findById(postId);
        else throw new ChatException(ChatErrorCode.INVALID_TYPE);
    }

    /**
     * 채팅방 구독을 시도한 유저에게 채팅방 정보를 전송한다.
     * 메시지의 경우 채팅방에 속한 모든 유저에게 전달되며, 클라이언트 측에서 해당하는 유저에서만 데이터를 처리한다.
     * @param userId 유저 Id
     * @param chatRoomId 채팅방 Id
     * @throws ChatException 채팅방이 존재하지 않을 경우
     */
    public void sendChatRoomInfo(String userId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.INVALID_CHAT));

        // 우선적으로 isSendActivated를 true로 설정하여 채팅방 정보를 전송한다.
        // 추후 상황에 맞게 isSendActivated를 변경한다.
        ChatRoomInfoResponse chatRoomInfo = ChatRoomInfoResponse.from(Long.valueOf(userId), chatRoom, true);

        simpMessagingTemplate.convertAndSend("/sub/chatRoomId=" + chatRoomId, chatRoomInfo);
    }

    /**
     * 채팅방에 메시지를 전송한다.
     * @param chatRoomId 채팅방 Id
     * @param message 메시지 객체
     * @throws ChatException 채팅방이 존재하지 않을 경우
     */
    @Transactional
    public void sendToChatRoomUser(String chatRoomId, ChatMessage message) {
        ChatRoom chatRoom = chatRoomRepository.findById(Long.parseLong(chatRoomId))
                .orElseThrow(() -> new ChatException(ChatErrorCode.INVALID_CHAT));

        Message newMessage = Message.builder()
                .chatroom(chatRoom)
                .type(message.getType())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .date(message.getDate())
                .time(message.getTime())
                .unreadCount(chatRoom.getNowPartInc() - getSubscribedUserCount(chatRoomId))
                .build();

        messageRepository.save(newMessage);

//        updateLastMessageToAllSubscribers(chatRoomId, newMessage.getMessageId());

        simpMessagingTemplate.convertAndSend("/sub?chatRoomId=" + chatRoomId, MessageResponse.of(newMessage));
    }

    /**
     * 채팅방 목록을 가져온다.
     * @param user 유저
     * @return 채팅방 목록 Dto
     */
    public List<GetChatRoomListResponseDto> getChatRoomList(User user) {
        return userChatroomService.getChatRoomList(user);
    }

    private int getSubscribedUserCount(String chatRoomId) {
        String destination = "/sub/chat/room/" + chatRoomId;
        int count = 0;

        for (SimpUser user : simpUserRegistry.getUsers()) {
            for (SimpSession session : user.getSessions()) {
                for (SimpSubscription subscription : session.getSubscriptions()) {
                    if (destination.equals(subscription.getDestination())) {
                        count++;
                        break;
                    }
                }
            }
        }

        return count;
    }

    public void kickUser(User user, KickUserRequestDto dto) {
        UserChatRoom ucr = userChatroomService.findByUserAndChatRoomId(user, dto.getChatRoomId());

        if (!ucr.getRole().equals("manager")) {
            throw new ChatException(ChatErrorCode.NOT_MANAGER);
        }

        UserChatRoom target = userChatroomService.findByUserUserIdAndChatRoomId(dto.getUserId(), dto.getChatRoomId());
        userChatroomService.kickedChatRoom(target);
    }

    public List<GetChatRoomUsersResponseDto> getChatRoomUsers(Long chatRoomId) {
        return userChatroomService.getChatRoomUsers(chatRoomId);
    }
//
//    public List<MessageResponse> getChatRoomMessageList(User user, Long chatRoomId) {
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
//
//        List<Message> messageList = messageRepository.findAllByChatroom(chatRoom);
//        List<MessageResponse> dto = new ArrayList<>();
//
//        for (Message message : messageList) {
//            dto.add(MessageResponse.of(message));
//        }
//
//        return dto;
//    }

//    public void checkCursorId(Long chatRoomId, Long userId) {
//        Long cursor = messageRepository.findCursorId(chatRoomId, userId);
//
//        if (cursor != null) {
//            simpMessagingTemplate.convertAndSend("/sub/chat/room/read/" + chatRoomId, CursorResponse.of(cursor));
//            userChatroomService.updateLastMessageToUser(userId, chatRoomId);
//        } else {
//            simpMessagingTemplate.convertAndSend("/sub/chat/room/read/" + chatRoomId, CursorResponse.of(0L));
//            userChatroomService.updateLastMessageToUser(userId, chatRoomId);
//        }
//    }
//
//    private void updateLastMessageToAllSubscribers(String chatRoomId, Long messageId) {
//        List<Long> userIds = new ArrayList<>();
//
//        for (SimpUser user : simpUserRegistry.getUsers()) {
//            for (SimpSession session : user.getSessions()) {
//                for (SimpSubscription subscription : session.getSubscriptions()) {
//                    if (subscription.getDestination().equals("/sub/chat/room/" + chatRoomId)) {
//                        userIds.add(Long.parseLong(user.getName()));
//                    }
//                }
//            }
//        }
//
//        userChatroomService.updateLastMessageToAllSubscribers(userIds, Long.parseLong(chatRoomId), messageId);
//    }
//
//    public void sendListUnreadMessage(String userId) {
//        List<UserChatRoom> userChatRoomList = userChatroomService.findAllByUserId(Long.valueOf(userId));
//
//        List<ChatRoomUnreadMessageDto> dto = new ArrayList<>();
//
//        for (UserChatRoom userChatRoom : userChatRoomList) {
//            ChatRoom chatRoom = userChatRoom.getChatroom();
//
//            dto.add(ChatRoomUnreadMessageDto.builder()
//                    .chatRoomId(chatRoom.getChatroomId())
//                    .unreadCount(0L)
//                    .message("test")
//                    .build());
//        }
//
//        simpMessagingTemplate.convertAndSendToUser(
//                userId,
//                "/chat/list/message",
//                dto);
//    }
}