package org.tiff.aiagent.tools.calendar;


import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 處理所有與 Google Calendar 相關的業務邏輯。
 * 這個 Service 是 AI 工具 (CalendarTools) 與 Google API 之間的中介。
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final Calendar googleCalendarClient; // 透過依賴注入從 GoogleCalendarConfig 獲得
    private final String calendarId = "primary"; // 使用主要日曆

    /**
     * 查詢指定時間範圍內的忙碌時段
     * @param startRange 查詢開始時間
     * @param endRange 查詢結束時間
     * @return AvailabilityResponse 包含忙碌時段列表和一條摘要訊息
     * @throws IOException 如果與 Google API 通訊失敗
     */
    public AvailabilityResponse getAvailability(LocalDateTime startRange, LocalDateTime endRange) throws IOException {
        DateTime timeMin = new DateTime(startRange.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        DateTime timeMax = new DateTime(endRange.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        Events events = googleCalendarClient.events().list(calendarId)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setOrderBy("startTime")
                .setSingleEvents(true) // 將重複性事件展開為單一事件
                .execute();

        List<AvailabilityResponse.TimeSlot> busySlots = events.getItems().stream()
                .map(this::convertEventToTimeSlot)
                .collect(Collectors.toList());

        String message = busySlots.isEmpty() ? "攝影師在查詢範圍內是空閒的。" : "攝影師在以下時段有安排。";
        return new AvailabilityResponse(message, busySlots);
    }

    /**
     * 創建一個新的日曆事件
     * @param request 包含創建事件所需所有資訊的請求物件
     * @return EventResponse 包含已創建事件的詳細資訊
     * @throws IOException 如果與 Google API 通訊失敗
     */
    public EventResponse createEvent(CreateEventRequest request) throws IOException {
        Event event = new Event()
                .setSummary(request.summary())
                .setDescription("客戶: " + request.attendeeName() + "\n聯絡方式: " + request.attendeeContact() + "\n---\n" + request.description());

        DateTime startDateTime = new DateTime(request.start().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(ZoneId.systemDefault().getId());
        event.setStart(start);

        DateTime endDateTime = new DateTime(request.end().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(ZoneId.systemDefault().getId());
        event.setEnd(end);

        Event createdEvent = googleCalendarClient.events().insert(calendarId, event).execute();
        return convertEventToEventResponse(createdEvent);
    }

    /**
     * 更新現有的日曆事件
     * @param eventId 要更新的事件 ID
     * @param request 包含新時間的請求物件
     * @return EventResponse 包含已更新事件的詳細資訊
     * @throws IOException 如果與 Google API 通訊失敗
     */
    public EventResponse updateEvent(String eventId, UpdateEventRequest request) throws IOException {
        // 首先，獲取現有事件以保留其原始資訊
        Event event = googleCalendarClient.events().get(calendarId, eventId).execute();

        // 更新時間
        DateTime startDateTime = new DateTime(request.newStart().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(ZoneId.systemDefault().getId());
        event.setStart(start);

        DateTime endDateTime = new DateTime(request.newEnd().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(ZoneId.systemDefault().getId());
        event.setEnd(end);

        Event updatedEvent = googleCalendarClient.events().update(calendarId, event.getId(), event).execute();
        return convertEventToEventResponse(updatedEvent);
    }

    /**
     * 刪除一個日曆事件
     * @param eventId 要刪除的事件 ID
     * @throws IOException 如果與 Google API 通訊失敗
     */
    public void deleteEvent(String eventId) throws IOException {
        googleCalendarClient.events().delete(calendarId, eventId).execute();
    }

    // --- 私有輔助方法 ---

    private EventResponse convertEventToEventResponse(Event event) {
        LocalDateTime start = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(event.getStart().getDateTime().getValue()), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()), ZoneId.systemDefault());
        return new EventResponse(event.getId(), event.getSummary(), event.getDescription(), start, end);
    }

    private AvailabilityResponse.TimeSlot convertEventToTimeSlot(Event event) {
        LocalDateTime start = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(event.getStart().getDateTime().getValue()), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()), ZoneId.systemDefault());
        return new AvailabilityResponse.TimeSlot(event.getId(), event.getSummary(), start, end);
    }
}


// ===================================================================================
// DTO (Data Transfer Objects) - 資料傳輸物件
// 通常這些會放在各自的檔案中，例如在 com.example.aiagentphotographer.dto 套件下
// 為了方便查閱，將它們放在這裡。
// ===================================================================================

/**
 * 回應攝影師有空/忙碌時段的 DTO
 */
record AvailabilityResponse(
        String message,
        List<TimeSlot> busySlots
) {
    public record TimeSlot(
            String eventId,
            String summary,
            LocalDateTime start,
            LocalDateTime end
    ) {}
}

/**
 * 創建新預約事件的請求 DTO
 */
record CreateEventRequest(
        String summary,
        String description,
        LocalDateTime start,
        LocalDateTime end,
        String attendeeName,
        String attendeeContact
) {}

/**
 * 代表一個日曆事件的回應 DTO
 */
record EventResponse(
        String eventId,
        String summary,
        String description,
        LocalDateTime start,
        LocalDateTime end
) {}

/**
 * 更新預約事件的請求 DTO
 */
record UpdateEventRequest(
        LocalDateTime newStart,
        LocalDateTime newEnd
) {}

