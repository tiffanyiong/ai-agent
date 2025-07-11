package org.tiff.aiagent.tools.calendar;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CalendarTool 的端到端整合測試。
 * 使用 @SpringBootTest 來載入完整的應用程式上下文。
 * 這個測試會實際連接到 Google Calendar API。
 */
@SpringBootTest
class CalendarToolTest {

    // @MockBean 已被移除，Spring 會注入由 GoogleCalendarConfig 創建的真實 Calendar Bean。

    // @Autowired 會注入一個真實的 CalendarTool 實例，
    // 該實例內部持有一個真實的、可進行網路連線的 Calendar 客戶端。
    @Autowired
    private CalendarTool calendarTool;

    // @BeforeEach 和所有 Mockito 的設定都已被移除。

    /**
     * 這個測試會實際呼叫 Google Calendar API。
     * 請確保您的 src/main/resources/ 目錄下有有效的 credentials.json，
     * 並且在需要時已完成 OAuth 2.0 授權流程（即已生成 tokens 目錄）。
     * @throws IOException 如果網路連線或 API 呼叫失敗
     */
    @Test
    void checkAvailability_withRealConnection() throws IOException {
        // 安排 (Arrange)
        // 設定一個未來的時間進行查詢，以減少與現有事件衝突的可能性
        LocalDateTime testStart = LocalDateTime.now().plusYears(5).withHour(10).withMinute(0);
        LocalDateTime testEnd = testStart.plusHours(1);

        // 執行 (Act)
        // 這裡會觸發真實的網路請求到 Google Calendar
        String result = calendarTool.checkAvailability(testStart, testEnd);

        // 斷言 (Assert)
        // 因為我們無法預測您真實日曆的狀態，所以最穩定的斷言是檢查結果是否為 null。
        // 一個非 null 的結果證明了整個應用程式的 Bean 配置、服務呼叫和 API 連線都是成功的。
        System.out.println("從 Google API 收到的真實回覆: " + result);
        assertNotNull(result, "回傳結果不應為 null，這表示與 Google API 的通訊出現問題。");
    }
}
