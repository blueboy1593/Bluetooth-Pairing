package com.kkobook.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kkobook.bluetooth.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var bluetoothAdapter: BluetoothAdapter
    private var handlerAnimation = Handler()
    private var statusAnimation = false
    private var pairedDevicesShowed = false
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestBluetoothPermission()
        setPulseAnimation()
        setBluetooth()
        registerBroadcastReceiver()
    }

    private fun registerBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                checkPermission()
                when (intent?.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        binding.imgAnimation.setImageResource(R.drawable.circle_green)
                    }
                }
            }
        }

        val stateFilter = IntentFilter()
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        registerReceiver(broadcastReceiver, stateFilter)
    }

    private fun setBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        binding.discoverableBtn.setOnClickListener {
            checkPermission()
            if (statusAnimation) {
                Toast.makeText(this, "Stop device first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!bluetoothAdapter.isDiscovering) {
                val intent = Intent(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
                startActivityForResult(intent, REQUEST_CODE_DISCOVERABLE_BT)
            }
        }

        binding.pairedDevicesBtn.setOnClickListener {
            if (!pairedDevicesShowed) {
                if (bluetoothAdapter.isEnabled) {
                    binding.pairedDevicesBtn.text = "Hide Paired Devices"
                    binding.pairedTv.visibility = View.VISIBLE
                    binding.pairedTv.text = "Paired Devices"
                    val devices = bluetoothAdapter.bondedDevices
                    for (device in devices) {
                        val deviceName = device.name
                        binding.pairedTv.append("\nDevice: $deviceName, $device")
                    }
                    pairedDevicesShowed = true
                } else {
                    Toast.makeText(this, "Turn on bluetooth first", Toast.LENGTH_LONG).show()
                }
            } else {
                binding.pairedDevicesBtn.text = "Get Paired Devices"
                binding.pairedTv.visibility = View.GONE
                pairedDevicesShowed = false
            }
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    private fun setPulseAnimation() {
        binding.button.setOnClickListener {
            if (statusAnimation) {
                handlerAnimation.removeCallbacks(runnable)
                binding.button.text = "Pair Bluetooth"
                unpairBluetooth()
            }
            else {
                runnable.run()
                binding.button.text = "Stop Pairing"
                pairBluetooth()
            }
            statusAnimation = !statusAnimation
        }
    }

    private fun pairBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            checkPermission()
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
        }
    }

    private fun unpairBluetooth() {
        checkPermission()
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.disable()
        }
    }

    private var runnable = object : Runnable {
        override fun run() {
            binding.imgAnimation.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000)
                .withEndAction {
                    binding.imgAnimation.scaleX = 1f
                    binding.imgAnimation.scaleY = 1f
                    binding.imgAnimation.alpha = 1f
                }

            binding.imgAnimation.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(700)
                .withEndAction {
                    binding.imgAnimation.scaleX = 1f
                    binding.imgAnimation.scaleY = 1f
                    binding.imgAnimation.alpha = 1f
                }

            handlerAnimation.postDelayed(this, 1500)
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH
                ),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Bluetooth 거절당했을 시 앱 종료.
        if (grantResults[2] == -1 || grantResults[3] == -1) {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Can not access to bluetooth", Toast.LENGTH_LONG).show()
                }
            REQUEST_CODE_DISCOVERABLE_BT -> {
                runnable.run()
                binding.button.text = "Stop Pairing"
                statusAnimation = true
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiver 등록해제
        unregisterReceiver(broadcastReceiver)
    }

    companion object {
        const val REQUEST_CODE_ENABLE_BT = 1
        const val REQUEST_CODE_DISCOVERABLE_BT = 2
    }
}