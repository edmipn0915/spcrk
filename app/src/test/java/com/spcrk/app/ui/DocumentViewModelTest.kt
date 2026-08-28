package com.spcrk.app.ui

import android.app.Application
import com.spcrk.app.ai.api.DocumentService
import com.spcrk.app.data.KnowledgeDocument
import com.spcrk.app.data.Repository
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
class DocumentViewModelTest {

    private val app = Mockito.mock(Application::class.java)
    private val repository = Mockito.mock(Repository::class.java)
    private val documentService = Mockito.mock(DocumentService::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Mockito.`when`(repository.getAllKnowledgeDocuments()).thenReturn(flowOf())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `removeDocument deletes only the document whose filePath matches full uri string`() = runTest {
        val fullUri = "content://com.android.providers.downloads.documents/document/123"
        val doc1 = KnowledgeDocument(title = "a", content = "", filePath = fullUri, fileType = "pdf")
        val doc2 = KnowledgeDocument(title = "b", content = "", filePath = "content://other/doc/9", fileType = "txt")
        Mockito.`when`(repository.getAllKnowledgeDocuments()).thenReturn(flowOf(listOf(doc1, doc2)))

        val viewModel = DocumentViewModel(app, repository, documentService)
        viewModel.removeDocument(fullUri)

        Mockito.verify(repository).deleteKnowledgeDocument(doc1)
        Mockito.verify(repository, Mockito.never()).deleteKnowledgeDocument(doc2)
    }
}
