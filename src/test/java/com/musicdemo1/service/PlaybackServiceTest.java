package com.musicdemo1.service;

import com.musicdemo1.common.BusinessException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaybackServiceTest {
    @Test
    void rejectsMissingSongsWithoutWritingEvents() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(99L))).thenReturn(0);
        PlaybackService service = new PlaybackService(jdbc);
        BusinessException error = assertThrows(BusinessException.class, () -> service.record(99, UUID.randomUUID()));
        assertEquals(404, error.getCode());
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }
}
