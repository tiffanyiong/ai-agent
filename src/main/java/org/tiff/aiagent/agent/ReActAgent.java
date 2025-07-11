package org.tiff.aiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting)
 * think -> act
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {
    /**
     *  Manage the current state and decide the next step
     * @return true - execute, false - not execute
     */
    public abstract boolean think();

    /**
     * Execute the decided step;
     * @return  result from the execution
     */
    public abstract String act();


    /**
     * think -> act
     * @return result
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "completed thinking - no action needed";
            }
            return act();
        } catch (Exception e) {
            return "Error executing this step: " + e.getMessage();
        }
    }
}
