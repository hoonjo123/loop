package world.loop.domain.chat.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatMessageTest {

    @Test
    void softDeletePreservesOriginalEvidence() {
        ChatMessage message = ChatMessage.builder()
                .messageType(MessageType.TEXT)
                .content("보존해야 할 원본 메시지")
                .imageUrl("https://example.com/evidence.png")
                .build();

        message.delete();

        assertThat(message.getDeletedAt()).isNotNull();
        assertThat(message.getContent()).isEqualTo("보존해야 할 원본 메시지");
        assertThat(message.getImageUrl()).isEqualTo("https://example.com/evidence.png");
    }
}
