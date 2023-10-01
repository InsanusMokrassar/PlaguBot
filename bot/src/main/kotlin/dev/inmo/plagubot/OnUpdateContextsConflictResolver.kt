package dev.inmo.plagubot

import dev.inmo.micro_utils.fsm.common.State

fun interface OnUpdateContextsConflictResolver {
    /**
     * This method will be called when [sourceStateWithOldContext] [State.context] and [newStateWithNewContext] are not equal and currently there is
     * launched [currentStateOnNewContext] state on the chain with [State.context] from [currentStateOnNewContext]
     *
     * @param sourceStateWithOldContext Old state where from [newStateWithNewContext] came
     * @param newStateWithNewContext New state with changing [State.context] (it is different with [sourceStateWithOldContext] [State.context])
     * @param currentStateOnNewContext State which is currently running on [newStateWithNewContext] [State.context]
     * @return Should return:
     *
     * * Null in case when current realization unable to resolve conflict
     * * False when [currentStateOnNewContext] **should not** be stopped in favor to [newStateWithNewContext]
     * * True when [currentStateOnNewContext] **should** be stopped in favor to [newStateWithNewContext]
     */
    suspend operator fun invoke(sourceStateWithOldContext: State, newStateWithNewContext: State, currentStateOnNewContext: State): Boolean?
}