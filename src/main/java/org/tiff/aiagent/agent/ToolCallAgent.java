package org.tiff.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.tiff.aiagent.agent.model.AgentState;

import java.util.List;
import java.util.stream.Collectors;

/**
 * implement the "think" and "act" methods.
 */
@EqualsAndHashCode(callSuper = true)
@Data

public class ToolCallAgent extends ReActAgent {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ToolCallAgent.class);

    // available tools
    private final ToolCallback[] availableTools;

    // store tool callback response
    private ChatResponse toolCallChatResponse;

    // tool call manager
    private final ToolCallingManager toolCallingManager;

    // try to see if we can disable Spring AI's internalToolExecution
    // TODO 可能要改成LLM自己的方法
    private ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    /**
     * Handle the current state and decide the next step
     *
     * @return if it needs action or not
     */
    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // record response for Act
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // output result info
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "think result: " + result);
            log.info(getName() + "chose " + toolCallList.size() + " tool(s) from the tool call list.");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("Tool's Name: %s, Params: %s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            if (toolCallList.isEmpty()) {
                // when it's not using tool, add assistant message
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // no need to record assistant message as it has record while it's using tool
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "think failed" +  e.getMessage());
            getMessageList().add(
                    new AssistantMessage("error when handling tool: " + e.getMessage())
            );
            return false;
        }
    }


    /**
     * invoke tools and handle the result
     *
     * @return execute
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "No tool is used";
        }

        // invoke tools
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // memorize the message history
        setMessageList(toolExecutionResult.conversationHistory());
        // current tool's status/result
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "TOOL " + response.name() + " completed its task! Result: " + response.responseData())
                .collect(Collectors.joining("\n"));


        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                        .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(getName() + "Act results: " + results);
        return results;
    }
}
