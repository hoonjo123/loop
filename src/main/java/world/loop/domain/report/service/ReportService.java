package world.loop.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.loop.domain.chat.entity.ChatMessage;
import world.loop.domain.chat.entity.ChatRoom;
import world.loop.domain.chat.repository.ChatMessageRepository;
import world.loop.domain.chat.repository.ChatRoomRepository;
import world.loop.domain.chat.service.ChatRoomService;
import world.loop.domain.report.dto.req.ReportCreateRequest;
import world.loop.domain.report.dto.res.ReportCreateResponse;
import world.loop.domain.report.entity.Report;
import world.loop.domain.report.repository.ReportRepository;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.repository.UserRepository;
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;

    @Transactional
    public ReportCreateResponse create(Long reporterId, ReportCreateRequest request) {
        validateTarget(request);
        User reporter = findUser(reporterId);
        User reportedUser = request.reportedUserId() == null ? null : findUser(request.reportedUserId());
        ChatRoom reportedRoom = request.reportedRoomId() == null ? null : findRoom(request.reportedRoomId());
        ChatMessage reportedMessage = request.reportedMessageId() == null
                ? null
                : chatMessageRepository.findById(request.reportedMessageId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
        Long relatedRoomId = reportedRoom != null
                ? reportedRoom.getId()
                : reportedMessage == null ? null : reportedMessage.getRoom().getId();
        if (relatedRoomId != null) {
            chatRoomService.requireMembership(relatedRoomId, reporterId);
        }
        Report report = reportRepository.save(Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportedRoom(reportedRoom)
                .reportedMessage(reportedMessage)
                .reason(request.reason().trim())
                .description(normalize(request.description()))
                .build());
        return ReportCreateResponse.from(report);
    }

    private void validateTarget(ReportCreateRequest request) {
        int targetCount = (request.reportedUserId() == null ? 0 : 1)
                + (request.reportedRoomId() == null ? 0 : 1)
                + (request.reportedMessageId() == null ? 0 : 1);
        if (targetCount != 1) {
            throw new BusinessException(ErrorCode.INVALID_REPORT_TARGET);
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoom findRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
