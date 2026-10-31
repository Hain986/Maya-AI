package com.Lia.assistant.agent.social

enum class ExecPhase {
    IDLE, OPENING_APP, OBSERVING, RESOLVING_TARGET, VALIDATING_TARGET,
    EXECUTING_ACTION, WAITING_FOR_UI, VERIFYING, NEXT_STEP, RETRY,
    REOBSERVE, WAITING_FOR_CONFIRMATION, WAITING_FOR_USER, COMPLETED, FAILED, CANCELLED
}

class TaskStateMachine {
    var currentPhase: ExecPhase = ExecPhase.IDLE
        private set

    private val history = mutableListOf<ExecPhase>()

    fun transitionTo(newPhase: ExecPhase) {
        if (currentPhase == ExecPhase.COMPLETED || currentPhase == ExecPhase.FAILED || currentPhase == ExecPhase.CANCELLED) {
            throw IllegalStateException("Cannot transition out of terminal state $currentPhase")
        }
        history.add(currentPhase)
        currentPhase = newPhase
    }
}
