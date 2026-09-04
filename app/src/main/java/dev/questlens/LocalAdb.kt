package dev.questlens

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.AtomicFile
import io.github.muntashirakon.adb.AdbConnection
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.net.NetworkInterface
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** An authenticated client of this headset only. No remote host or arbitrary command API. */
internal class LocalAdb(private val context: Context) {
    private val timeout = Executors.newSingleThreadScheduledExecutor()

    fun withConnection(initialAuthorization: Boolean, action: (AdbConnection) -> Unit) {
        // TCP is used only during initial enrollment so Android can show its normal RSA consent.
        val port = if (initialAuthorization) 5555 else discoverTlsPort()
        val (key, certificate) = credentials()
        val connection = AdbConnection.Builder("127.0.0.1", port)
            .setApi(Build.VERSION.SDK_INT).setDeviceName("QuestLens-local")
            .setPrivateKey(key).setCertificate(certificate).build()
        val deadline = timeout.schedule({ runCatching { connection.close() } },
            if (initialAuthorization) 55 else 20, TimeUnit.SECONDS)
        try {
            check(connection.connect(if (initialAuthorization) 45 else 10, TimeUnit.SECONDS,
                !initialAuthorization)) { "Local authorization is pending. Accept the headset prompt and try again." }
            action(connection)
        } finally {
            deadline.cancel(false)
            connection.close()
        }
    }

    fun close() { timeout.shutdownNow() }

    fun sensorState(connection: AdbConnection): String = command(connection,
        "cmd sensorservice get-uid-state com.oculus.vrshell").trim()

    fun suspendNativeTap(connection: AdbConnection) {
        val reply = command(connection, "cmd sensorservice set-uid-state com.oculus.vrshell idle; cmd sensorservice get-uid-state com.oculus.vrshell")
        check(reply.trim() == "idle") { "The system did not confirm shortcut preparation." }
    }

    fun restoreNativeTap(connection: AdbConnection) {
        val reply = command(connection, "cmd sensorservice reset-uid-state com.oculus.vrshell; cmd sensorservice get-uid-state com.oculus.vrshell")
        check(reply.trim() == "active") { "Could not restore the system shortcut." }
    }

    private fun command(connection: AdbConnection, command: String): String =
        connection.open("shell:$command").use { stream ->
            stream.openInputStream().use { input ->
                val bytes = ByteArray(4096)
                var size = 0
                while (size < bytes.size) {
                    val count = input.read(bytes, size, bytes.size - size)
                    if (count < 0) break
                    size += count
                    // Every supported command returns its verified state on one line.
                    // Stop on that line: this library can throw IOException at normal remote EOF.
                    if ((0 until size).any { bytes[it] == '\n'.code.toByte() }) break
                }
                check(size < bytes.size) { "Unexpected local response." }
                String(bytes, 0, size, Charsets.UTF_8)
            }
        }

    private fun credentials(): Pair<PrivateKey, Certificate> {
        val file = AtomicFile(File(context.noBackupFilesDir, "local-adb.p12"))
        val store = KeyStore.getInstance("PKCS12")
        val password = charArrayOf()
        if (file.baseFile.exists()) {
            file.openRead().use { store.load(it, password) }
        } else {
            store.load(null, password)
            val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
            val name = X500Name("CN=QuestLens local")
            val now = System.currentTimeMillis()
            val provider = BouncyCastleProvider()
            val signer = JcaContentSignerBuilder("SHA256withRSA").setProvider(provider).build(pair.private)
            val cert = JcaX509CertificateConverter().setProvider(provider).getCertificate(
                JcaX509v3CertificateBuilder(name, BigInteger(128, SecureRandom()).setBit(127),
                    Date(now - 86_400_000), Date(now + 10L * 365 * 86_400_000), name, pair.public).build(signer))
            store.setKeyEntry("local", pair.private, password, arrayOf(cert))
            val output = file.startWrite()
            try { store.store(output, password); file.finishWrite(output) }
            catch (e: Exception) { file.failWrite(output); throw e }
        }
        return (store.getKey("local", password) as PrivateKey) to store.getCertificate("local")
    }

    @Suppress("DEPRECATION")
    private fun discoverTlsPort(): Int {
        val nsd = context.getSystemService(NsdManager::class.java)
        val done = CountDownLatch(1)
        val port = AtomicInteger(-1)
        val localAddresses = NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }.map { it.hostAddress }.toSet()
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(type: String) = Unit
            override fun onDiscoveryStopped(type: String) = Unit
            override fun onStopDiscoveryFailed(type: String, code: Int) = Unit
            override fun onStartDiscoveryFailed(type: String, code: Int) { done.countDown() }
            override fun onServiceLost(info: NsdServiceInfo) = Unit
            override fun onServiceFound(info: NsdServiceInfo) {
                nsd.resolveService(info, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(info: NsdServiceInfo, code: Int) = Unit
                    override fun onServiceResolved(info: NsdServiceInfo) {
                        if (info.host?.hostAddress in localAddresses && info.port in 1..65535) {
                            port.compareAndSet(-1, info.port)
                            done.countDown()
                        }
                    }
                })
            }
        }
        nsd.discoverServices("_adb-tls-connect._tcp.", NsdManager.PROTOCOL_DNS_SD, listener)
        try { done.await(15, TimeUnit.SECONDS) }
        finally { runCatching { nsd.stopServiceDiscovery(listener) } }
        check(port.get() > 0) { "Local debugging is unavailable. Check Wi-Fi, accept the headset prompt and try again." }
        return port.get()
    }
}
