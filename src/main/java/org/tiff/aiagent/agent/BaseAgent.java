package org.tiff.aiagent.agent;


import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.tiff.aiagent.agent.model.AgentState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract base agent class used to manage agent states and execution steps
 */
@Data

public abstract class BaseAgent {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaseAgent.class);

    // core attributes
    private String name;

    private String systemPrompt;
    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int maxSteps = 10;
    private int currentStep = 0;

    private ChatClient chatClient;

    // Memory
    private List<Message> messageList = new ArrayList<>();

    /**
     *  execute agent
     * @param userPrompt user's prompt
     * @return execution result
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("cannot run agent from state: " + this.state);
        }
        if (StringUtils.isBlank(userPrompt)) {
            throw new RuntimeException("cannot run agent with empty user prompt");
        }

        // update the state
        this.state = AgentState.RUNNING;
        // memorize the context
        messageList.add(new UserMessage(userPrompt));
        // save the result to the list
        List<String> results = new ArrayList<>();
        try{
            for (int i = 0; i <maxSteps && this.state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info(String.format("Executing step %d / %d", stepNumber, maxSteps));

                // single step
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }

            // check if it exceeds the max step
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e){
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "Error executing agent: " + e.getMessage();
        } finally{
            this.cleanup();
        }

    }


    /**
     * SSE - execute agent
     * @param userPrompt user's prompt
     * @return execution result
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 mins timeout
        // use thread async, avoid blocking main thread
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("ERROR: Not able to run the agent from "  + this.state + " state");
                    emitter.complete();
                    return;
                }
                if (StringUtils.isBlank(userPrompt)) {
                    emitter.send("ERROR: Not able to run with empty user prompt");
                    emitter.complete();
                    return;
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }

            // update the state
            this.state = AgentState.RUNNING;
            // memorize the context
            messageList.add(new UserMessage(userPrompt));
            // save the result to the list
            List<String> results = new ArrayList<>();
            try{
                for (int i = 0; i <maxSteps && this.state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info(String.format("Executing step %d / %d", stepNumber, maxSteps));

                    // single step
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);

                    // send each current step result to SSE
                    emitter.send(result);

                }

                // check if it exceeds the max step
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    emitter.send("Terminated: Reached max steps (" + maxSteps + ")");
                }
                // emitter complete
                emitter.complete();
            } catch (Exception e){
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    emitter.send("ERROR: " + e.getMessage());
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }

            } finally{
                this.cleanup();
            }
        });

        // set up timeout
        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection complete");
        });

        return emitter;
    }

    /**
     * Execute a step
     * @return return result from a execution
     */
    public abstract String step();

    protected void cleanup() {
        // child class can overwrite this method to clean up resources
    }
}
