package org.tiff.aiagent.tools.calendar;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 為 Spring AI Agent 定義一組可用的 Google Calendar 工具。
 * AI 會根據這些工具的描述來決定如何與 Google Calendar 互動。
 */
@Component
public class CalendarTool {

    // 注入您現有的 CalendarService 來處理與 Google API 的實際通訊
    private final CalendarService calendarService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日 HH:mm");


    public CalendarTool(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * 工具 1: 查詢攝影師的空閒時間
     * AI 會在用戶詢問 "有空嗎？", "可以預約嗎？" 或類似問題時使用此工具。
     * @param start 查詢的開始時間，ISO 格式，例如："2024-12-01T09:00:00"
     * @param end 查詢的結束時間，ISO 格式，例如："2024-12-01T18:00:00"
     * @return 一個字串，描述攝影師在該時間段內已被佔用的時段。如果沒有，則告知用戶該時段有空。
     */
    @Tool(description = "查詢攝影師在指定時間範圍內的空閒情況。返回攝影師已被佔用的時段列表。")
    public String checkAvailability(
            @ToolParam(description = "查詢的開始時間，ISO 格式，例如：'2024-12-01T09:00:00'") LocalDateTime start,
            @ToolParam(description = "查詢的結束時間，ISO 格式，例如：'2024-12-01T18:00:00'") LocalDateTime end) {
        try {
            AvailabilityResponse response = calendarService.getAvailability(start, end);
            if (response.busySlots() == null || response.busySlots().isEmpty()) {
                return "太好了！攝影師在您查詢的時段內沒有任何安排。";
            } else {
                String busyTimes = response.busySlots().stream()
                        .map(slot -> String.format(" - 從 %s 到 %s (事件: %s)",
                                slot.start().format(formatter),
                                slot.end().format(formatter),
                                slot.summary()))
                        .collect(Collectors.joining("\n"));
                return "攝影師在以下時段已有安排，請避開這些時間：\n" + busyTimes;
            }
        } catch (Exception e) {
            return "查詢空閒時間時發生錯誤: " + e.getMessage();
        }
    }

    /**
     * 工具 2: 創建一個新的預約
     * AI 會在與用戶確認完所有預約細節（時間、姓名、聯絡方式）後使用此工具。
     * @param summary 預約的標題，例如："個人寫真 - 王先生"
     * @param start 預約的開始時間，ISO 格式
     * @param end 預約的結束時間，ISO 格式
     * @param attendeeName 客戶的全名
     * @param attendeeContact 客戶的聯絡方式（電話或電子郵件）
     * @param description 預約的額外備註，可選
     * @return 一個字串，確認預約已成功創建，並包含預約的唯一 ID (eventId)。
     */
    @Tool(description = "在攝影師的日曆上創建一個新的預約事件。")
    public String createEvent(
            @ToolParam(description = "預約的標題，例如：'個人寫真 - 王先生'") String summary,
            @ToolParam(description = "預約的開始時間，ISO 格式") LocalDateTime start,
            @ToolParam(description = "預約的結束時間，ISO 格式") LocalDateTime end,
            @ToolParam(description = "客戶的全名") String attendeeName,
            @ToolParam(description = "客戶的聯絡方式（電話或電子郵件）") String attendeeContact,
            @ToolParam(description = "預約的額外備註，可選") String description) {
        try {
            CreateEventRequest request = new CreateEventRequest(summary, description, start, end, attendeeName, attendeeContact);
            EventResponse response = calendarService.createEvent(request);
            return String.format("預約創建成功！已為 %s 在 %s 安排了 '%s'。預約 ID 是：%s",
                    attendeeName,
                    response.start().format(formatter),
                    response.summary(),
                    response.eventId());
        } catch (Exception e) {
            return "創建預約時發生錯誤: " + e.getMessage();
        }
    }

    /**
     * 工具 3: 修改現有的預約
     * AI 會在用戶提出修改請求時使用此工具，例如："我想把預約改到下週三"。
     * @param eventId 要修改的預約的唯一 ID。AI 需要先找到這個 ID。
     * @param newStart 預約的新開始時間，ISO 格式
     * @param newEnd 預約的新結束時間，ISO 格式
     * @return 一個字串，確認預約已成功更新。
     */
    @Tool(description = "修改一個已存在的預約時間。")
    public String updateEvent(
            @ToolParam(description = "要修改的預約的唯一標識符 (event ID)") String eventId,
            @ToolParam(description = "預約的新開始時間，ISO 格式") LocalDateTime newStart,
            @ToolParam(description = "預約的新結束時間，ISO 格式") LocalDateTime newEnd) {
        try {
            UpdateEventRequest request = new UpdateEventRequest(newStart, newEnd);
            EventResponse response = calendarService.updateEvent(eventId, request);
            return String.format("ID 為 %s 的預約已成功更新！新時間為 %s。",
                    eventId,
                    response.start().format(formatter));
        } catch (Exception e) {
            return "修改預約時發生錯誤: " + e.getMessage();
        }
    }

    /**
     * 工具 4: 刪除一個預約
     * AI 會在用戶提出取消請求時使用此工具，例如："我想取消我的預約"。
     * @param eventId 要刪除的預約的唯一 ID。AI 需要先找到這個 ID。
     * @return 一個字串，確認預約已成功刪除。
     */
    @Tool(description = "從日曆上刪除一個現有的預約。")
    public String deleteEvent(
            @ToolParam(description = "要刪除的預約的唯一標識符 (event ID)") String eventId) {
        try {
            calendarService.deleteEvent(eventId);
            return String.format("ID 為 %s 的預約已成功取消。", eventId);
        } catch (Exception e) {
            return "刪除預約時發生錯誤: " + e.getMessage();
        }
    }
}
