package com.jonikoone.statemachine

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BaseObservable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.jonikoone.state_mechine_lib.IState
import com.jonikoone.statemachine.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

val okHttpClient = OkHttpClient.Builder().build()

val retrofitService = Retrofit.Builder()
    .baseUrl("https://raw.githubusercontent.com/SkbkonturMobile/mobile-test-droid/master/json/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create(Gson()))
    .build().create(Service::class.java)

class MainActivity : AppCompatActivity() {

    val viewModel = MainActivityViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.viewModel = viewModel
        viewModel.mainText.observe({ this.lifecycle }) {
            binding.textView.text = it
        }
        viewModel.initStateMachineAsync()

    }
}


class MainActivityViewModel : BaseObservable(),
    com.jonikoone.state_mechine_lib.IStateMachine {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    val listData = mutableListOf<Contact>()

    //show
    val mainText = MutableLiveData<String>("main text")

    //load
    val enabledReloadBtn = MutableLiveData<Boolean>(true)

    //load
    val visibleProgress = MutableLiveData<Int>(View.VISIBLE)

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
            notifyChange()
            listData.clear()
            launch(Dispatchers.IO) {
                try {
                    listData.addAll(retrofitService.getData().await())
                    nextStep(MyState.Show::class)
                } catch (e: Exception) {
                    nextStep(MyState.ErrorLoad::class)
                }
            }
        }
        MyState.Show.initAction {
            updateBtnText.value = "Reload"
            mainText.value = listData.toString()
            visibleProgress.value = View.GONE
            enabledReloadBtn.value = true
            notifyChange()
        }
        MyState.ErrorLoad.initAction {
            updateBtnText.value = "Try again"
            mainText.value = "error load data"
            visibleProgress.value = View.GONE
            enabledReloadBtn.value = true
            notifyChange()
        }

        MyState.Reload.initAction {
            updateBtnText.value = "Reloading..."
            notifyChange()
            nextStep(MyState.Load::class)
        }

    }

    override var currentState: IState = MyState.Load
}


/*
* states
* 1 - load, 2 - show(view), 3 - reload, 4 - errorLoad(view)
*
* 1 -> {2, 4)
* 2 -> 3
* 3 -> 1
* 4 -> 3
*
* */

sealed class MyState(vararg val possibleState: KClass<out IState>) :
    IState(*possibleState) {
    object Load : MyState(Show::class, ErrorLoad::class)
    object Show : MyState(Reload::class)
    object Reload : MyState(Load::class)
    object ErrorLoad : MyState(Reload::class)
}

interface Service {
    @GET("generated-01.json")
    fun getData(): Call<List<Contact>>
}