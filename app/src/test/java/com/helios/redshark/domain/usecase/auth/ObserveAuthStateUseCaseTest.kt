package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObserveAuthStateUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: ObserveAuthStateUseCase

    private val testUser = User(
        id = "uid_123",
        email = "test@example.com",
        displayName = "Test User",
        avatarUrl = null,
        bio = null,
        skills = emptyList(),
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = ObserveAuthStateUseCase(authRepository)
    }

    @Test
    fun `invoke emits user when signed in`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(testUser)

        val results = useCase().toList()

        assertEquals(1, results.size)
        assertEquals(testUser, results.first())
    }

    @Test
    fun `invoke emits null when signed out`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(null)

        val results = useCase().toList()

        assertEquals(1, results.size)
        assertNull(results.first())
    }

    @Test
    fun `invoke emits auth state transitions`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(null, testUser, null)

        val results = useCase().toList()

        assertEquals(3, results.size)
        assertNull(results[0])
        assertEquals(testUser, results[1])
        assertNull(results[2])
    }

    @Test
    fun `invoke delegates to repository`() = runTest {
        every { authRepository.observeAuthState() } returns flowOf(testUser)

        useCase()

        verify { authRepository.observeAuthState() }
    }
}
