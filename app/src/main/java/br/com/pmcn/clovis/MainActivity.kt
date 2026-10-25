package br.com.pmcn.clovis

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private data class ChatLine(val role: String, val text: String)

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
                    candidate.initialize()
                    val prior = history.mapNotNull { line ->
                        when (line.role) {
                            "user" -> Message.user(line.text)
                            "assistant" -> if (line.text.isNotBlank()) Message.model(line.text) else null
                            else -> null
                        }
                    }
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
        status.text = "Clóvis está gerando a resposta no telefone…"
        scope.launch {
            try {
                withTimeout(180_000L) {
                    withContext(Dispatchers.IO) {
                        chat.sendMessageAsync(text).collect { piece ->
                            val chunk = piece.toString()
                            if (chunk.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    val line = history.getOrNull(answerIndex)
                                    if (line != null && line.role == "assistant") {
                                        history[answerIndex] = line.copy(text = line.text + chunk)
                                        updateLineView(answerIndex)
                                        scrollToBottom()
                                    }
                                }
                            }
                        }
                    }
                }
                val line = history.getOrNull(answerIndex)
                if (line != null && line.role == "assistant" && line.text.isBlank()) {
                    history[answerIndex] = line.copy(text = "[A geração terminou sem retornar texto. Isso indica um problema no runtime ou no modelo.]")
                    updateLineView(answerIndex)
                    status.text = "A geração terminou, mas o modelo não retornou texto."
                } else {
                    status.text = "Clóvis está pronto. Resposta concluída."
                }
                persistHistory()
            } catch (e: TimeoutCancellationException) {
                val line = history.getOrNull(answerIndex)
                if (line != null && line.role == "assistant") {
                    history[answerIndex] = line.copy(text = (line.text + "\n\n[A geração ultrapassou 3 minutos e foi interrompida para evitar espera indefinida.]").trim())
                }
                updateLineView(answerIndex)
                status.text = "A resposta demorou mais de 3 minutos. O teste indica um problema de geração, não apenas uma demora normal."
                persistHistory()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val line = history.getOrNull(answerIndex)
                if (line != null) history[answerIndex] = line.copy(text = (line.text + "\n\n[Erro ao gerar resposta: ${e.message ?: "falha desconhecida"}]").trim())
                updateLineView(answerIndex)
                status.text = "Ocorreu um erro ao gerar a resposta."
                persistHistory()
            } finally {
                busy = false
                sendButton.isEnabled = conversation != null
                input.isEnabled = true
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
        text = (if (line.role == "user") "Você" else "Clóvis") + "\n" + line.text
        textSize = 15f
        setTextColor(Color.rgb(35, 47, 42))
        setPadding(dp(13), dp(10), dp(13), dp(10))
        background = rounded(if (line.role == "user") Color.rgb(220, 239, 231) else Color.WHITE, 15)
        val params = LinearLayout.LayoutParams(
            if (line.role == "user") (resources.displayMetrics.widthPixels * 0.82).toInt() else -1, -2
        )
        params.gravity = if (line.role == "user") Gravity.END else Gravity.START
        params.bottomMargin = dp(8)
        layoutParams = params
    }

    private fun updateLineView(index: Int) {
        if (index !in lineViews.indices || index !in history.indices) return
        lineViews[index].text = (if (history[index].role == "user") "Você" else "Clóvis") + "\n" + history[index].text
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
