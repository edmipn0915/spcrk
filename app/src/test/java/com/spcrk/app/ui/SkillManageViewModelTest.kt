package com.spcrk.app.ui

import android.app.Application
import com.spcrk.app.ai.api.SkillService
import com.spcrk.app.data.Repository
import com.spcrk.app.data.Skill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class SkillManageViewModelTest {

    private val app = Mockito.mock(Application::class.java)
    private val repository = Mockito.mock(Repository::class.java)
    private val skillService = Mockito.mock(SkillService::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Mockito.`when`(repository.getAllSkills()).thenReturn(flowOf())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addSkill installs via service without inserting repository directly`() = runTest {
        val viewModel = SkillManageViewModel(app, repository, skillService)
        val skill = Skill(name = "test", description = "d", trigger = "t")

        viewModel.addSkill(skill)

        // installSkill 內部自行負責 repository 插入；VM 直接呼叫 repository 會造成雙重插入
        Mockito.verify(skillService).installSkill(skill)
        Mockito.verify(repository, Mockito.never()).addSkill(skill)
    }
}
