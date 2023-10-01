package dev.inmo.plagubot

import dev.inmo.micro_utils.fsm.common.State

fun interface OnStartContextsConflictResolver {
    /**
     * @param old Old state which is currently placed on the [State.context]
     * @param new New state pretend to replace [old] one
     * @return Should return:
     *
     * * Null in case when current realization unable to resolve conflict
     * * False when current realization knows that [new] [State] must **not** replace [old] one
     * * True when current realization knows that [new] [State] must replace [old] one
     */
    suspend operator fun invoke(old: State, new: State): Boolean?
}