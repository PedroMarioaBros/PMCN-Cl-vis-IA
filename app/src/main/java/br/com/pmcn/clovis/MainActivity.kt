package br.com.pmcn.clovis

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.ThinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.TimeoutCancellationException
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private data class ChatLine(val role: String, val text: String)

private const val TAG = "Clovis"
private const val MAX_OUTPUT_TOKENS = 512
private const val GENERATION_TIMEOUT_MS = 180_000L

// Diagnósticos antigos podem estar anexados a respostas parciais no histórico.
private fun isAppNote(text: String): Boolean =
    text.contains("[A geração ultrapassou 3 minutos") ||
    text.contains("[A geração terminou sem retornar texto") ||
    text.contains("[Erro ao gerar resposta:")

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val modelName = "Qwen3-0.6B.litertlm"
    private val modelUrl = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm"
    private val modelFile by lazy { File(filesDir, modelName) }
    private val history = mutableListOf<ChatLine>()

    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var downloadButton: Button
    private lateinit var chatScroll: ScrollView
    private lateinit var chatContent: LinearLayout
    private lateinit var input: EditText
    private lateinit var sendButton: Button
    private lateinit var welcomePanel: LinearLayout
    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var busy = false
    private val lineViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadHistory()
        buildUi()
        if (modelFile.exists() && modelFile.length() > 100_000_000L) {
            status.text = "Modelo baixado. Preparando o Clóvis…"
            initializeModel()
        } else {
            modelFile.delete()
            showDownloadState()
        }
        renderHistory()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setBackgroundColor(Color.rgb(244, 246, 245))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(6), dp(2), dp(12))
        }
        header.addView(TextView(this).apply {
            text = "Clóvis"
            textSize = 29f
            setTextColor(Color.rgb(13, 76, 64))
            typeface = Typeface.DEFAULT_BOLD
        })
        header.addView(TextView(this).apply {
            text = "PMCN Clóvis IA · versão de teste 0.1.0"
            textSize = 13f
            setTextColor(Color.rgb(90, 104, 99))
        })
        root.addView(header)

        welcomePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 16)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        welcomePanel.addView(TextView(this).apply {
            text = "Seu assistente local"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(28, 43, 38))
        })
        welcomePanel.addView(TextView(this).apply {
            text = "Esta versão usa um único modelo no próprio telefone. O modelo é baixado uma vez; depois, as respostas são geradas localmente, sem enviar suas mensagens para um servidor."
            textSize = 14f
            setTextColor(Color.rgb(69, 81, 76))
            setPadding(0, dp(6), 0, dp(8))
        })
        status = TextView(this).apply {
            text = "Aguardando download do modelo."
            textSize = 13f
            setTextColor(Color.rgb(13, 76, 64))
        }
        welcomePanel.addView(status)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            this.progress = 0
            visibility = View.GONE
        }
        welcomePanel.addView(progress, LinearLayout.LayoutParams(-1, dp(6)).apply { topMargin = dp(8) })
        downloadButton = Button(this).apply {
            text = "Baixar modelo (requer internet)"
            setOnClickListener { downloadModel() }
        }
        welcomePanel.addView(downloadButton, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(welcomePanel, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        chatScroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(0, dp(2), 0, dp(4))
        }
        chatContent = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        chatScroll.addView(chatContent)
        root.addView(chatScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(2))
        }
        input = EditText(this).apply {
            hint = "Converse com o Clóvis…"
            textSize = 16f
            setSingleLine(false)
            maxLines = 4
            imeOptions = EditorInfo.IME_ACTION_SEND
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(Color.WHITE, 22)
        }
        composer.addView(input, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(8) })
        sendButton = Button(this).apply {
            text = "Enviar"
            isEnabled = false
            setOnClickListener { sendMessage() }
        }
        composer.addView(sendButton)
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
        }
        root.addView(composer)
        setContentView(root)
    }

    private fun showDownloadState() {
        status.text = "O modelo ainda não está neste telefone. Baixe-o para começar."
        downloadButton.visibility = View.VISIBLE
        downloadButton.isEnabled = true
        downloadButton.text = "Baixar modelo (aprox. 600 MB)"
        progress.visibility = View.GONE
        sendButton.isEnabled = false
        downloadButton.setOnClickListener { downloadModel() }
    }

    private fun downloadModel() {
        if (busy) return
        busy = true
        downloadButton.isEnabled = false
        downloadButton.text = "Baixando…"
        progress.visibility = View.VISIBLE
        progress.progress = 0
        status.text = "Conectando ao repositório do modelo…"
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val temp = File(filesDir, "$modelName.part")
                    temp.delete()
                    val connection = (URL(modelUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 20_000
                        readTimeout = 60_000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "PMCN-Clovis-IA/0.1.0 Android")
                    }
                    try {
                        connection.connect()
                        val code = connection.responseCode
                        if (code !in 200..299) throw IllegalStateException("Servidor respondeu HTTP $code")
                        val total = connection.contentLengthLong
                        connection.inputStream.use { source ->
                            temp.outputStream().buffered().use { target ->
                                val buffer = ByteArray(1024 * 128)
                                var received = 0L
                                while (true) {
                                    val count = source.read(buffer)
                                    if (count < 0) break
                                    if (count == 0) continue
                                    target.write(buffer, 0, count)
                                    received += count
                                    val pct = if (total > 0) ((received * 100) / total).toInt().coerceIn(0, 100) else -1
                                    withContext(Dispatchers.Main) {
                                        if (pct >= 0) progress.progress = pct
                                        status.text = "Baixando: ${formatBytes(received)}" +
                                            if (total > 0) " / ${formatBytes(total)} ($pct%)" else ""
                                    }
                                }
                            }
                        }
                        if (total > 0 && temp.length() != total) {
                            throw IllegalStateException("Download incompleto: ${temp.length()} de $total bytes.")
                        }
                        if (temp.length() < 100_000_000L) throw IllegalStateException("O arquivo baixado é pequeno demais para ser o modelo. Verifique a conexão e tente novamente.")
                        if (!temp.renameTo(modelFile)) {
                            temp.copyTo(modelFile, overwrite = true)
                            temp.delete()
                        }
                    } finally { connection.disconnect() }
                }
                status.text = "Download concluído. Preparando o Clóvis…"
                initializeModel()
            } catch (e: Exception) {
                modelFile.delete()
                status.text = "Não consegui baixar o modelo: ${e.message ?: "erro de conexão"}"
                downloadButton.text = "Tentar baixar novamente"
                downloadButton.isEnabled = true
                progress.visibility = View.GONE
                busy = false
            }
        }
    }

    private fun initializeModel() {
        if (busy && conversation != null) return
        busy = true
        downloadButton.isEnabled = false
        status.text = "Carregando o modelo na memória. A primeira inicialização pode demorar…"
        scope.launch {
            try {
                val ready = withContext(Dispatchers.IO) {
                    val candidate = Engine(EngineConfig(
                        modelPath = modelFile.absolutePath,
                        backend = Backend.CPU(),
                        cacheDir = cacheDir.absolutePath
                    ))
                    val startedAt = System.nanoTime()
                    candidate.initialize()
                    Log.i(TAG, "Engine.initialize() concluído em ${(System.nanoTime() - startedAt) / 1_000_000} ms; modelo=${modelFile.length()} bytes")

                    // Recria apenas pares usuário/resposta concluídos; avisos e mensagens órfãs não entram no contexto.
                    val prior = mutableListOf<Message>()
                    var i = 0
                    while (i < history.size - 1) {
                        val userLine = history[i]
                        val assistantLine = history[i + 1]
                        val interruptedByNotice = history.getOrNull(i + 2)?.let {
                            it.role == "notice" && (it.text.startsWith("A geração") || it.text.startsWith("Erro ao gerar"))
                        } ?: false
                        if (
                            userLine.role == "user" &&
                            assistantLine.role == "assistant" &&
                            assistantLine.text.isNotBlank() &&
                            !isAppNote(assistantLine.text) &&
                            !interruptedByNotice
                        ) {
                            prior.add(Message.user(userLine.text))
                            prior.add(Message.model(assistantLine.text))
                            i += 2
                        } else {
                            i += 1
                        }
                    }
                    Log.i(TAG, "Mensagens anteriores enviadas ao contexto=${prior.size}; histórico=${history.size}")
                    val chat = candidate.createConversation(ConversationConfig(
                        systemInstruction = Contents.of(
                            "Você é Clóvis, assistente pessoal de Pedro. Responda em português brasileiro por padrão. " +
                            "Seja útil, claro e honesto; não invente fatos nem diga que fez algo que não fez. " +
                            "Você está executando localmente no telefone e não tem acesso à internet durante a conversa."
                        ),
                        initialMessages = prior
                    ))
                    Pair(candidate, chat)
                }
                engine = ready.first
                conversation = ready.second
                status.text = "Clóvis está pronto. As respostas são geradas no telefone."
                downloadButton.text = "Modelo instalado ✓"
                downloadButton.isEnabled = false
                sendButton.isEnabled = true
                busy = false
            } catch (e: Exception) {
                status.text = "O modelo foi baixado, mas não carregou: ${e.message ?: "erro de inicialização"}. Vamos ajustar com o resultado do teste."
                downloadButton.text = "Tentar carregar novamente"
                downloadButton.isEnabled = true
                downloadButton.setOnClickListener { initializeModel() }
                progress.visibility = View.GONE
                busy = false
            }
        }
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        val chat = conversation
        if (text.isEmpty() || chat == null || busy) return

        input.setText("")
        addLine("user", text)
        val answerIndex = addLine("assistant", "")
        persistHistory()
        busy = true
        sendButton.isEnabled = false
        input.isEnabled = false
        status.text = "Clóvis está iniciando a geração local…"

        scope.launch {
            try {
                withTimeout(GENERATION_TIMEOUT_MS) {
                    generateWithCallback(chat, text, answerIndex)
                }

                val line = history.getOrNull(answerIndex)
                if (line != null && line.role == "assistant" && line.text.isBlank()) {
                    status.text = "A geração terminou sem texto. Consulte o diagnóstico."
                    addLine("notice", "A geração terminou sem retornar texto. O runtime/modelo precisa de investigação.")
                } else {
                    status.text = "Clóvis está pronto. Resposta concluída."
                }
                persistHistory()
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Timeout de ${GENERATION_TIMEOUT_MS / 1000} segundos na geração")
                addLine("notice", "A geração ultrapassou 3 minutos e foi cancelada para evitar espera indefinida.")
                status.text = "A geração excedeu 3 minutos. O processo de inferência foi cancelado; consulte o diagnóstico."
                persistHistory()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Falha na geração", e)
                addLine("notice", "Erro ao gerar resposta: ${e.message ?: "falha desconhecida"}")
                status.text = "Ocorreu um erro ao gerar a resposta."
                persistHistory()
            } finally {
                busy = false
                sendButton.isEnabled = conversation != null
                input.isEnabled = true
            }
        }
    }

    /**
     * Usa a API de callback do LiteRT-LM, evitando o adaptador Flow interno da biblioteca.
     * O callback entrega mensagens estruturadas; toString() contém o texto visível e o canal
     * "thought" é registrado apenas como diagnóstico, nunca mostrado como resposta.
     */
    private suspend fun generateWithCallback(chat: Conversation, prompt: String, answerIndex: Int) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val terminal = AtomicBoolean(false)
            val startedAt = System.nanoTime()
            var chunks = 0

            continuation.invokeOnCancellation {
                if (terminal.compareAndSet(false, true)) {
                    Log.w(TAG, "Cancelando geração nativa")
                    try {
                        chat.cancelProcess()
                    } catch (cancelError: Exception) {
                        Log.w(TAG, "Não foi possível cancelar a inferência nativa", cancelError)
                    }
                }
            }

            try {
                Log.i(TAG, "sendMessageAsync(callback) início; thinking=false; maxOutputToken=$MAX_OUTPUT_TOKENS")
                chat.sendMessageAsync(
                    prompt,
                    object : MessageCallback {
                        override fun onMessage(message: Message) {
                            if (terminal.get()) return
                            chunks++
                            val chunk = message.toString()
                            val thought = message.channels["thought"].orEmpty()
                            Log.i(
                                TAG,
                                "bloco=$chunks tempo=${(System.nanoTime() - startedAt) / 1_000_000}ms " +
                                    "texto=${chunk.length} pensamento=${thought.length}"
                            )

                            runOnUiThread {
                                // Callbacks já enfileirados antes de onDone/cancelamento ainda precisam
                                // ser exibidos; a checagem terminal é feita antes de enfileirar.
                                if (chunk.isNotEmpty()) {
                                    val line = history.getOrNull(answerIndex)
                                    if (line != null && line.role == "assistant") {
                                        history[answerIndex] = line.copy(text = line.text + chunk)
                                        updateLineView(answerIndex)
                                        scrollToBottom()
                                        persistHistory()
                                    }
                                } else if (thought.isNotEmpty()) {
                                    status.text = "Clóvis está processando a resposta…"
                                }
                            }
                        }

                        override fun onDone() {
                            if (!terminal.compareAndSet(false, true)) return
                            Log.i(TAG, "Geração concluída: blocos=$chunks; duração=${(System.nanoTime() - startedAt) / 1_000_000}ms")
                            // Enfileira a conclusão depois dos chunks já enviados à UI.
                            runOnUiThread {
                                if (continuation.isActive) continuation.resume(Unit)
                            }
                        }

                        override fun onError(throwable: Throwable) {
                            if (!terminal.compareAndSet(false, true)) return
                            Log.e(TAG, "Callback de geração retornou erro", throwable)
                            runOnUiThread {
                                if (continuation.isActive) continuation.resumeWithException(throwable)
                            }
                        }
                    },
                    maxOutputToken = MAX_OUTPUT_TOKENS,
                    thinkingConfig = ThinkingConfig(enableThinking = false)
                )
            } catch (e: Throwable) {
                if (terminal.compareAndSet(false, true) && continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    private fun addLine(role: String, text: String): Int {
        history.add(ChatLine(role, text))
        val view = makeBubble(history.last())
        lineViews.add(view)
        chatContent.addView(view)
        scrollToBottom()
        return history.lastIndex
    }

    private fun renderHistory() {
        chatContent.removeAllViews()
        lineViews.clear()
        if (history.isEmpty()) {
            chatContent.addView(TextView(this).apply {
                text = "Quando o modelo estiver pronto, mande uma mensagem para iniciar. Esta é uma versão inicial de teste; ainda não tem pesquisa de conversas nem gerenciamento de múltiplos chats."
                textSize = 14f
                setTextColor(Color.rgb(90, 104, 99))
                setPadding(dp(10), dp(18), dp(10), dp(18))
            })
        } else {
            history.forEach {
                val view = makeBubble(it)
                lineViews.add(view)
                chatContent.addView(view)
            }
        }
        scrollToBottom()
    }

    private fun makeBubble(line: ChatLine): TextView = TextView(this).apply {
        text = (when (line.role) {
            "user" -> "Você"
            "notice" -> "Aviso do aplicativo"
            else -> "Clóvis"
        }) + "\n" + line.text
        textSize = 15f
        setTextColor(Color.rgb(35, 47, 42))
        setPadding(dp(13), dp(10), dp(13), dp(10))
        background = rounded(
            when (line.role) {
                "user" -> Color.rgb(220, 239, 231)
                "notice" -> Color.rgb(255, 243, 205)
                else -> Color.WHITE
            },
            15
        )
        val params = LinearLayout.LayoutParams(
            if (line.role == "user") (resources.displayMetrics.widthPixels * 0.82).toInt() else -1, -2
        )
        params.gravity = if (line.role == "user") Gravity.END else Gravity.START
        params.bottomMargin = dp(8)
        layoutParams = params
    }

    private fun updateLineView(index: Int) {
        if (index !in lineViews.indices || index !in history.indices) return
        lineViews[index].text = (when (history[index].role) {
            "user" -> "Você"
            "notice" -> "Aviso do aplicativo"
            else -> "Clóvis"
        }) + "\n" + history[index].text
    }

    private fun persistHistory() {
        val arr = JSONArray()
        history.forEach { arr.put(JSONObject().put("role", it.role).put("text", it.text)) }
        getSharedPreferences("clovis_chat", MODE_PRIVATE).edit().putString("history", arr.toString()).apply()
    }

    private fun loadHistory() {
        try {
            val arr = JSONArray(getSharedPreferences("clovis_chat", MODE_PRIVATE).getString("history", "[]"))
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                history.add(ChatLine(obj.optString("role"), obj.optString("text")))
            }
        } catch (_: Exception) { history.clear() }
    }

    private fun scrollToBottom() { chatScroll.post { chatScroll.fullScroll(View.FOCUS_DOWN) } }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024) String.format(Locale.US, "%.2f GB", mb / 1024.0) else String.format(Locale.US, "%.0f MB", mb)
    }

    override fun onDestroy() {
        scope.cancel()
        try { conversation?.close() } catch (_: Exception) {}
        try { engine?.close() } catch (_: Exception) {}
        super.onDestroy()
    }
}
