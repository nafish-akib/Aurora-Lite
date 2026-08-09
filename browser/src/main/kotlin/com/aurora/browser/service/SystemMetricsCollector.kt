package com.aurora.browser.service

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.os.Process
import android.view.Choreographer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.RandomAccessFile

data class RealProcess(
    val pid: Int,
    val name: String,
    val type: String,
    val memoryMB: Int,
    val cpuPercent: Float = 0f
)

class SystemMetricsCollector(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private var pollJob: Job? = null
    private var fpsJob: Job? = null

    private val _processes = MutableStateFlow<List<RealProcess>>(emptyList())
    val processes: StateFlow<List<RealProcess>> = _processes.asStateFlow()

    private val _memoryMB = MutableStateFlow(0)
    val memoryMB: StateFlow<Int> = _memoryMB.asStateFlow()

    private val _cpuPercent = MutableStateFlow(0)
    val cpuPercent: StateFlow<Int> = _cpuPercent.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _networkKbps = MutableStateFlow(0L)
    val networkKbps: StateFlow<Long> = _networkKbps.asStateFlow()

    private val _gpuPercent = MutableStateFlow(0)
    val gpuPercent: StateFlow<Int> = _gpuPercent.asStateFlow()

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastCpuTime = System.currentTimeMillis()
    private var lastProcessCpuTime = 0L

    fun start(fpsEnabled: Boolean = false) {
        if (pollJob?.isActive == true) return
        pollJob = scope.launch {
            while (isActive) {
                try {
                    collectMetrics()
                } catch (_: Exception) { }
                delay(2000)
            }
        }
        if (fpsEnabled) {
            fpsJob = scope.launch(Dispatchers.Main) {
            var frameCount = 0
            val choreographer = Choreographer.getInstance()
            val callback = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    frameCount++
                    choreographer.postFrameCallback(this)
                }
            }
            choreographer.postFrameCallback(callback)
            while (isActive) {
                delay(1000)
                _fps.value = frameCount
                frameCount = 0
            }
            }
        }
    }

    fun stop() {
        pollJob?.cancel()
        fpsJob?.cancel()
    }

    fun destroy() {
        scope.cancel()
    }

    private fun collectMetrics() {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val usedMemMB = ((memInfo.totalMem - memInfo.availMem) / (1024 * 1024)).toInt()
        _memoryMB.value = usedMemMB

        val runningProcesses = try {
            activityManager.runningAppProcesses?.map { rpi ->
                val mem = try {
                    val memInfoArr = activityManager.getProcessMemoryInfo(intArrayOf(rpi.pid))
                    if (memInfoArr.isNotEmpty()) memInfoArr[0].totalPss / 1024 else 0
                } catch (_: Exception) { 0 }
                RealProcess(
                    pid = rpi.pid,
                    name = rpi.processName.substringAfterLast("."),
                    type = when {
                        rpi.processName.contains("aurora") -> "Browser"
                        rpi.processName.contains("gecko") || rpi.processName.contains("mozilla") -> "Renderer"
                        rpi.processName.contains("gpu") || rpi.processName.contains("graphics") -> "GPU"
                        else -> "Service"
                    },
                    memoryMB = mem,
                    cpuPercent = 0f
                )
            } ?: emptyList()
        } catch (_: Exception) { emptyList() }
        _processes.value = runningProcesses

        val selfPid = Process.myPid()
        val selfMemInfo = activityManager.getProcessMemoryInfo(intArrayOf(selfPid))
        val selfPss = if (selfMemInfo.isNotEmpty()) selfMemInfo[0].totalPss / 1024 else 0

        val statFile = try { RandomAccessFile("/proc/stat", "r").use { it.readLine() } } catch (_: Exception) { null }
        val cpuLine = statFile?.replace("cpu  ", "")?.trim()?.split("\\s+".toRegex())?.mapNotNull { it.toLongOrNull() }
        if (cpuLine != null && cpuLine.size >= 4) {
            // Track per-process Aurora CPU from /proc/<pid>/stat
            val processCpuTime = try {
                RandomAccessFile("/proc/$selfPid/stat", "r").use { raf ->
                    val line = raf.readLine()
                    val parts = line.split("\\s+".toRegex())
                    // utime (idx 13) + stime (idx 14) in jiffies
                    if (parts.size >= 15) {
                        (parts[13].toLongOrNull() ?: 0) + (parts[14].toLongOrNull() ?: 0)
                    } else 0L
                }
            } catch (_: Exception) { 0L }
            val now = System.currentTimeMillis()
            val cpuDelta = processCpuTime - lastProcessCpuTime
            val timeDelta = now - lastCpuTime
            if (timeDelta > 0 && cpuDelta > 0) {
                // jiffies to ms (CLK_TCK = 100), divided by CPU core count for per-core percentage
                val cpuMs = (cpuDelta * 10) / Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
                val cpuPercentage = ((cpuMs.toFloat() / timeDelta) * 100f).toInt().coerceIn(0, 100)
                _cpuPercent.value = cpuPercentage
            }
            lastCpuTime = now
            lastProcessCpuTime = processCpuTime
        }

        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        val rxDelta = rx - lastRxBytes
        val txDelta = tx - lastTxBytes
        val kbps = ((rxDelta + txDelta) * 8 / 1000) / 2
        _networkKbps.value = kbps.coerceAtLeast(0)
        lastRxBytes = rx
        lastTxBytes = tx

        val gpuMem = try {
            val selfMem = activityManager.getProcessMemoryInfo(intArrayOf(selfPid))
            if (selfMem.isNotEmpty()) {
                selfMem[0].getMemoryStat("summary.graphics")?.toIntOrNull()?.div(1024) ?: 0
            } else 0
        } catch (_: Exception) { 0 }
        _gpuPercent.value = if (gpuMem > 0) ((gpuMem.toFloat() / selfPss.toFloat().coerceAtLeast(1f)) * 100f).toInt().coerceIn(0, 100) else 0
    }
}
