package dev.dotworld.sip

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.linphone.core.*


class MainActivity : AppCompatActivity() {
    private lateinit var core: Core

    companion object {
        private const val TAG = "MainActivity"
    }

    private val callListener = object : CallListenerStub() {
        override fun onDtmfReceived(call: Call, dtmf: Int) {
            super.onDtmfReceived(call, dtmf)
            Log.d(TAG, "DTMF Received $dtmf")
        }
    }

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            Log.d(TAG, "State Changed $message")
        }

        override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
        }

        override fun onAudioDevicesListUpdated(core: Core) {

        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {


            Log.d(TAG, "onCallStateChanged $message $state")
            when (state) {
                Call.State.IncomingReceived -> {
                    core.currentCall?.accept()
                }
                Call.State.Connected -> {
                    call.sendDtmfs("#1234")
                    call.addListener(callListener)
                }
                else -> {

                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val username = ""
        val password = ""
        val domain = ""

        val factory = Factory.instance()
        factory.setDebugMode(true, "Hello Linphone")
        core = factory.createCore(null, null, this)

        val authInfo =
            Factory.instance().createAuthInfo(username, null, password, null, null, domain, null)

        val params = core.createAccountParams()
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        params.identityAddress = identity

        val address = Factory.instance().createAddress("sip:$domain")
        address?.transport = TransportType.Tcp
        params.serverAddress = address
        params.isRegisterEnabled = true
        val account = core.createAccount(params)

        core.addAuthInfo(authInfo)
        core.addAccount(account)

        //https://www.voip-info.org/sip-dtmf-signalling/
        core.useRfc2833ForDtmf = true

        core.defaultAccount = account
        core.addListener(coreListener)
        core.start()
    }
}

