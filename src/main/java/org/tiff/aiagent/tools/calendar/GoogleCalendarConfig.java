package org.tiff.aiagent.tools.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Google Calendar API 設定檔。
 * 負責處理與 Google API 的認證和客戶端實例化。
 */
@Configuration
public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "Photographer AI Agent";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // 儲存使用者授權 token 的目錄
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * 定義應用程式所需的 Google Calendar 權限範圍。
     * CalendarScopes.CALENDAR 允許讀取和寫入日曆事件。
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    // 您的 OAuth 2.0 用戶端憑證檔案路徑。請將 credentials.json 放在 src/main/resources/ 目錄下。
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * 取得授權憑證。
     * 此方法會處理 OAuth 2.0 流程。首次執行時，它會啟動一個本地伺服器，
     * 並在控制台顯示一個 URL，您需要將其複製到瀏覽器中進行授權。
     * 授權成功後，access token 和 refresh token 會被儲存在 TOKENS_DIRECTORY_PATH 中。
     * @param httpTransport HTTP 傳輸實例
     * @return 授權後的 Credential 物件
     * @throws IOException 如果讀取憑證檔案失敗
     */
    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
        // 讀取 credentials.json 檔案
        InputStream in = GoogleCalendarConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // 建立授權流程
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline") // "offline" 表示我們需要 refresh token，以便在用戶離線時也能刷新 access token
                .build();

        // 啟動本地伺服器以接收授權碼
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        // 觸發使用者授權流程
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * 建立並設定一個 Calendar 服務的 Bean。
     * 這個 Bean 將被 Spring 容器管理，並可以注入到其他服務中，例如 CalendarService。
     * @return 一個經過授權且可用的 Calendar 服務實例
     * @throws GeneralSecurityException 如果建立 HTTP 傳輸時發生安全錯誤
     * @throws IOException 如果授權或 API 通訊時發生 I/O 錯誤
     */
    @Bean
    public Calendar googleCalendarClient() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(httpTransport);
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
