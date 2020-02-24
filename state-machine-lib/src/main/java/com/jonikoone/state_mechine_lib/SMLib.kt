package com.jonikoone.state_mechine_lib

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * abstract class for extends in concrete State
 * @param distancesState distances states
 * */
abstract class IState(vararg val distancesState: KClass<out IState>) {

    /**
     * validate next state on current state
     * @param klassState next state
     * @return true if state contains in possible states
     * */
    fun hasNextStep(klassState: KClass<out IState>) = distancesState.contains(klassState)

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

    var currentState: IState

    /**
    * change current state on klassState if validate is true
    * @param klassState
    * */
    fun nextStep(klassState: KClass<out IState>) {
        Log.e("State machine", "goto navigate: ${klassState.simpleName}")
        if (currentState.hasNextStep(klassState)) {
            currentState = klassState.objectInstance!!
            Log.e("klassInfo", "${klassState.simpleName}:$currentState")
            launch(Dispatchers.Main) {
                currentState.stateAction?.invokeAction()
            }
        }
    }

    /**
    * starting first state in state machine
    * */
    fun initStateMachineAsync() = nextStep(currentState::class)
}

