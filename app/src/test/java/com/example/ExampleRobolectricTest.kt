package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.ChatMessageEntity
import com.example.data.db.GlitchDatabase
import com.example.data.model.AiEmotion
import com.example.data.model.ChatMessage
import com.example.data.repository.RoomChatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: GlitchDatabase
  private lateinit var repository: RoomChatRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, GlitchDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = RoomChatRepository(db.glitchDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GLITCH.AI", appName)
  }

  @Test
  fun `ChatMessageEntity to and from ChatMessage mapping preserves all fields`() {
    val original = ChatMessage(
      id = "msg-123",
      text = "Wie ben jij?",
      isUser = true,
      timestamp = 123456789L,
      emotion = AiEmotion.THINKING
    )

    val entity = ChatMessageEntity.fromChatMessage(original)
    assertEquals("msg-123", entity.id)
    assertEquals("Wie ben jij?", entity.text)
    assertEquals(true, entity.isUser)
    assertEquals(123456789L, entity.timestamp)
    assertEquals("THINKING", entity.emotion)

    val restored = entity.toChatMessage()
    assertEquals(original.id, restored.id)
    assertEquals(original.text, restored.text)
    assertEquals(original.isUser, restored.isUser)
    assertEquals(original.timestamp, restored.timestamp)
    assertEquals(original.emotion, restored.emotion)
  }

  @Test
  fun `RoomChatRepository persists messages and returns them in historical order`() = runBlocking {
    val msg1 = ChatMessage(id = "1", text = "Hallo GLITCH", isUser = true, timestamp = 1000L)
    val msg2 = ChatMessage(id = "2", text = "Hoi mens! Wat kan ik voor je doen?", isUser = false, timestamp = 2000L)
    val msg3 = ChatMessage(id = "3", text = "Wat is de hoofdstad van Nederland?", isUser = true, timestamp = 3000L)

    repository.saveMessage(msg1)
    repository.saveMessage(msg2)
    repository.saveMessage(msg3)

    val history = repository.getConversationHistory().first()
    assertEquals(3, history.size)
    assertEquals("Hallo GLITCH", history[0].text)
    assertEquals("Hoi mens! Wat kan ik voor je doen?", history[1].text)
    assertEquals("Wat is de hoofdstad van Nederland?", history[2].text)

    val recent = repository.getRecentHistoricalMessages(limit = 2)
    assertEquals(2, recent.size)
    assertEquals("Hoi mens! Wat kan ik voor je doen?", recent[0].text)
    assertEquals("Wat is de hoofdstad van Nederland?", recent[1].text)

    assertEquals(3, repository.getMessageCount())

    repository.clearConversation()
    assertEquals(0, repository.getMessageCount())
  }
}


