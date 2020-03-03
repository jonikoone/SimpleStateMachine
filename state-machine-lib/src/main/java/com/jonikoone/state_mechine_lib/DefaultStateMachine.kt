package com.jonikoone.state_mechine_lib

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class DefaultStateMachine(
    override var currentState: IState,
    override val coroutineContext: CoroutineContext = Dispatchers.Main,
    override var startStateMachine: StartStateMachine = StartStateMachine.Start
) : IStateMachine