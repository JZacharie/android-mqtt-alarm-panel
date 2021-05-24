/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.content.Context
import android.text.TextUtils
import com.google.gson.JsonObject

import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTService

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import timber.log.Timber

class MqttUtils {

    companion object {

        // TODO use the rest of these in the app
        const val TYPE_ALARM = "alarm"
        const val TYPE_SENSOR = "sensor"
        const val TYPE_COMMAND = "command"
        const val TYPE_EVENT = "event"

        const val PORT = 1883
        const val TOPIC_COMMAND = "command"
        const val COMMAND_STATE = "state"

        const val EVENT_INVALID_CODE = "invalid_code_provided"
        const val EVENT_NO_CODE = "no_code_provided"
        const val EVENT_ARM_FAILED = "failed_to_arm"
        const val EVENT_TRIGGER = "trigger"
        const val EVENT_SYSTEM_DISABLED = "system_disabled"
        const val EVENT_COMMAND_NOT_ALLOWED = "command_not_allowed"
        const val EVENT_UNKNOWN = "unknown"

        const val VALUE = "value"
        const val COMMAND = "command"
        const val CODE = "code"

        const val COMMAND_SENSOR_FACE = "sensor/face"
        const val COMMAND_SENSOR_QR_CODE = "sensor/qrcode"
        const val COMMAND_SENSOR_MOTION = "sensor/motion"

        const val STATE_CURRENT_URL = "currentUrl"
        const val STATE_SCREEN_ON = "screenOn"
        const val STATE_BRIGHTNESS = "brightness"
        const val STATE_PRESENCE = "presence"
        const val COMMAND_SENSOR_PREFIX = "sensor/"

        const val COMMAND_WAKE = "wake"
        const val COMMAND_DASHBOARD = "dashboard"
        const val COMMAND_AUDIO = "audio"
        const val COMMAND_SPEAK = "speak"
        const val COMMAND_NOTIFICATION = "notification"
        const val COMMAND_ALERT = "alert"

        const val COMMAND_CAPTURE = "capture"
        const val COMMAND_WEATHER = "weather"
        const val COMMAND_SUN = "sun"

        const val SENSOR_GENERIC_TYPE = "GENERIC"
        const val SENSOR_DOOR_TYPE = "DOOR"
        const val SENSOR_WINDOW_TYPE = "WINDOW"
        const val SENSOR_SOUND_TYPE = "SOUND"
        const val SENSOR_MOTION_TYPE = "MOTION"
        const val SENSOR_CAMERA_TYPE = "CAMERA"

        // commands
        const val COMMAND_ARM_HOME = "ARM_HOME"
        const val COMMAND_ARM_NIGHT = "ARM_NIGHT"
        const val COMMAND_ARM_CUSTOM_BYPASS = "ARM_CUSTOM_BYPASS"
        const val COMMAND_ARM_AWAY = "ARM_AWAY"
        const val COMMAND_DISARM = "DISARM"
        const val COMMAND_PANIC = "PANIC"
        const val COMMAND_ON = "ON"

        // mqtt states
        const val STATE_DISARMED = "disarmed"
        const val STATE_ARMED_AWAY = "armed_away"
        const val STATE_ARMED_HOME = "armed_home"
        const val STATE_ARMED_NIGHT = "armed_night"
        const val STATE_PENDING = "pending"
        const val STATE_ARMING = "arming"
        const val STATE_ARMED_CUSTOM_BYPASS = "armed_custom_bypass"
        const val STATE_TRIGGERED = "triggered"
        const val STATE_DISABLED = "disabled"
        const val STATE_ARM_AWAY = "arm_away"
        const val STATE_ARM_HOME = "arm_home"
        const val STATE_DISARM = "disarm"
        const val STATE_ARM_NIGHT = "arm_night"
        const val STATE_ARM_CUSTOM_BYPASS = "arm_custom_bypass"

        const val DEFAULT_COMMAND_TOPIC = "home/alarm/set"
        const val DEFAULT_SENSOR_TOPIC = "home/alarm/sensor"
        const val DEFAULT_CONFIG_TOPIC = "home/alarm/config"
        const val DEFAULT_EVENT_TOPIC = "home/alarm/event"
        const val DEFAULT_STATE_TOPIC = "home/alarm"
        const val DEFAULT_PANEL_COMMAND_TOPIC = "alarmpanel"

        val sensorTypes = java.util.ArrayList<String>()
        
        init {
            sensorTypes.add(SENSOR_GENERIC_TYPE)
            sensorTypes.add(SENSOR_DOOR_TYPE)
            sensorTypes.add(SENSOR_WINDOW_TYPE)
            sensorTypes.add(SENSOR_SOUND_TYPE)
            sensorTypes.add(SENSOR_MOTION_TYPE)
            sensorTypes.add(SENSOR_CAMERA_TYPE)
        }
        
        @Deprecated ("We don't need a callback for the client.")
        fun getMqttAndroidClient(context: Context, serverUri: String, clientId: String,
                                 mqttCallbackExtended: MqttCallbackExtended): MqttAndroidClient {
            val mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
            mqttAndroidClient.setCallback(mqttCallbackExtended)
            return mqttAndroidClient
        }

        /**
         * We need to make an array of listeners to pass to the subscribe topics.
         * @param length
         * @return
         */
        fun getMqttMessageListeners(length: Int, listener: MQTTService.MqttManagerListener?): Array<IMqttMessageListener?> {
            val mqttMessageListeners = arrayOfNulls<IMqttMessageListener>(length)
            for (i in 0 until length) {
                val mqttMessageListener = IMqttMessageListener { topic, message ->
                    Timber.i("Subscribe Topic: " + topic + "  Payload: " + String(message.payload))
                    Timber.i("Subscribe Topic Listener: " + listener!!)
                    listener.subscriptionMessage(message.id.toString(), topic, String(message.payload))
                }
                mqttMessageListeners[i] = mqttMessageListener
            }
            return mqttMessageListeners
        }

        /**
         * Validate if we have valid json for parsing.
         */
        fun isJSONValid(value: String?): Boolean {
            value?.let {
                try {
                    JSONObject(value)
                } catch (ex: JSONException) {
                    return false
                }
                return true
            }?: return false
        }

        fun parseJSONObjectOrEmpty(value: String?): JSONObject? {
            value?.let {
                return try {
                    JSONObject(value)
                } catch (ex: JSONException) {
                    JSONObject()
                }
            }?: return null
        }

        fun parseEventFromJson(payload: String): String {
            var event = payload
            if(isJSONValid(payload)) {
                val json = parseJSONObjectOrEmpty(payload)
                json?.let {
                    if(json.has("event")) {
                        event = json.getString("event") ?: ""
                    }
                }
                return event
            } else {
                return event
            }
        }

        fun parseStateFromJson(payload: String): String {
            var state = payload
            if(isJSONValid(payload)) {
                val json = parseJSONObjectOrEmpty(payload)
                json?.let {
                    if(json.has("state")) {
                        state = json.getString("state") ?: ""
                    }
                }
                return state
            } else {
                return state
            }
        }

        fun parseDelayFromJson(payload: String): Int {
            var delay = -1
            if(isJSONValid(payload)) {
                val json = parseJSONObjectOrEmpty(payload)
                json?.let {
                    if(json.has("delay")) {
                        delay = json.getInt("delay")
                    }
                }
                return delay
            } else {
                return delay
            }
        }

        /**
         * Generate an array of QOS values for subscribing to multiple topics.
         * @param length
         * @return
         */
        fun getQos(length: Int): IntArray {
            val qos = IntArray(length)
            for (i in 0 until length) {
                qos[i] = 0
            }
            return qos
        }
    }
}
