package com.jonikoone.state_mechine_lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.reflect.KClass

/**
 * abstract class for extends in concrete State
 * @param distancesState distances states
 * */
abstract class IState(
    vararg val distancesState: KClass<out IState>
) {
    /**
     * validate next state on current state
     * @param kclassState next state
     * @return true if state contains in possible states
     * */
    fun hasNextStep(kclassState: KClass<out IState>) = distancesState.contains(kclassState)

    /*
    * this is it, that do work within set currentState
    * state action invoke within nextStep from StateMachine
    * */
    var stateAction: StateAction? = null

    inline fun initAction(crossinline block: () -> Unit) {
        stateAction = object : StateAction {
            override suspend fun invokeAction() = block()
        }
    }

    fun initAction(action: StateAction) {
        stateAction = action
    }

    interface StateAction {
        suspend fun invokeAction()
    }
}


/**
 * context states
 * */
interface IStateMachine : CoroutineScope {
    /**
     * first state
     * */
    var currentState: IState
    /**
     * flag anti-restart state machine
     * */
    var startStateMachine: StartStateMachine

    /**
     * change current state on klassState if validate is true
     * @param kclassState
     * */
    fun nextStep(kclassState: KClass<out IState>) {
        Timber.d("try change current state to %s", kclassState.qualifiedName)
        if (currentState.hasNextStep(kclassState)) {
            currentState = kclassState.objectInstance!!
            Timber.d("current state changed to %s", currentState::class.qualifiedName)
            launch {
                currentState.stateAction?.invokeAction()
            }
        } else {
            val error = MissNextStateThrowable(kclassState)
            Timber.e(error)
        }
    }

    /**
     * starting first state in state machine
     * */
    fun initStateMachine() {
        Timber.d(
            "start state machine, try invoke action in current state: %s",
            currentState::class.qualifiedName
        )
        launch {
            currentState.stateAction?.invokeAction()
            Timber.d("started state machine ")
        }
    }

    fun initStateMachineSingle() {
        if (startStateMachine == StartStateMachine.Start) {
            initStateMachine()
            startStateMachine = StartStateMachine.Started
        }
    }
}

class MissNextStateThrowable(kclassState: KClass<out IState>) : Throwable(
    message = "Your don`t change to %s state, because current state don`t contains this next state. Add distancesState in initialize your states."
        .format(kclassState)
)

class StartStateMachine(val stateStart: Int) {
    companion object {
        val Start = StartStateMachine(0)
        val Started = StartStateMachine(1)
    }
}




