你是一位专业的前端开发，请帮我根据下列信息来生成对应的前端项目代码。

## 需求

1）主页：用于切换不同的应用

2）页面 1：AI 拍照助手。页面风格为聊天室，上方是聊天记录（用户信息在右边，AI 信息在左边），下方是输入框，进入页面后自动生成一个聊天室 id，用于区分不同的会话。通过 SSE 的方式调用 doChatWithPhotoApp 接口，实时显示对话内容。

3）页面 2：AI 超级智能体应用。页面风格同页面 1，但是调用 doChatWithManus 接口，也是实时显示对话内容。

4）支持响應式，多屏慕尺寸適配。

5）頁面外觀使用漸變色的極簡酷炫風。


## 技术选型

1. react 项目
2. Axios 请求库

## 后端接口信息

接口地址前缀：http://localhost:8080/api

## SpringBoot 后端接口代码

@RestController
@RequestMapping("/ai")
public class AiController {

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message){
       MockManus mockManus = new MockManus(allTools, openAiChatModel);
       return mockManus.runStream(message);
    }

    @GetMapping(value = "/photo_consult/chat", produces =  MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithPhotoApp(String message, String chatId){
    return photoConsultApp.doChatWithToolsByStream(message, chatId);
    }




}
