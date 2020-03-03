package com.jonikoone.statemachine

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonikoone.state_mechine_lib.DefaultStateMachine
import com.jonikoone.state_mechine_lib.IState
import com.jonikoone.state_mechine_lib.IStateMachine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel(),
    IStateMachine by DefaultStateMachine(MyState.Load) {

    //show
    val mainText = MutableLiveData<String>("main text")
    //load
    val enabledReloadBtn = MutableLiveData<Boolean>(true)

    //load
    val visibleProgress =
        MutableLiveData<Int>(View.VISIBLE)

    val updateBtnText = MutableLiveData<String>("not set")

    fun onClickBtn1() {
        //reload start
        nextStep(MyState.Reload::class)
    }

    init {

        MyState.Load.initAction {
            visibleProgress.value = View.VISIBLE
            enabledReloadBtn.value = false
            updateBtnText.value = "Download Data..."
            launch(Dispatchers.IO) {
                try {
                    (10 downTo 0).forEach {
                        withContext(Dispatchers.Main) {
                            mainText.value = "loaded data - $it"
                        }
                        delay(1000)
                    }

                    nextStep(MyState.Show::class)
                } catch (e: Exception) {
                    nextStep(MyState.ErrorLoad::class)
                }
            }
        }

        MyState.Show.initAction {
            updateBtnText.value = "Reload"
            mainText.value = "compete"
            visibleProgress.value = View.GONE
            enabledReloadBtn.value = true
        }

        MyState.ErrorLoad.initAction {
            updateBtnText.value = "Try again"
            mainText.value = "error load data"
            visibleProgress.value = View.GONE
            enabledReloadBtn.value = true
        }

        MyState.Reload.initAction {
            updateBtnText.value = "Reloading..."
            nextStep(MyState.Load::class)
        }

    }

    override var currentState: IState =
        MyState.Load
}

/*
* states
* 1 - load, 2 - show("suspend point"), 3 - reload, 4 - errorLoad("suspend point")
*
* 1 -> {2, 4)
* 2 -> 3
* 3 -> 1
* 4 -> 3
*
* */

sealed class MyState {
    object Load : IState(Show::class, ErrorLoad::class)
    object Show : IState(Reload::class)
    object Reload : IState(Load::class)
    object ErrorLoad : IState(Reload::class)
}