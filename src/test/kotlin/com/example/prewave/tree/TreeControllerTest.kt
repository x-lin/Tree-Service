package com.example.prewave.tree

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.result.StatusResultMatchersDsl
import kotlin.test.assertEquals

/**
 * @author LinX
 */
@WebMvcTest(TreeController::class)
class TreeControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @MockBean
    private lateinit var mockDbService: TreeDbService

    @Nested
    inner class GetTreeTest {
        @Test
        fun when_getTreeWithValidNodeId_then_returnsOkWithTree() {
            //GIVEN
            val nodeId = 1
            val tree = Tree(nodeId, mapOf(nodeId to setOf(2), 2 to setOf(3, 4), 3 to setOf(), 4 to setOf()))
            `when`(mockDbService.getSubTree(nodeId)).thenReturn(tree)

            //WHEN + THEN
            val result = mockMvcGetTreeExpectResponse(1) { isOk() }
                .andReturn()
            val actual: Tree = objectMapper.readValue(result.response.contentAsString, Tree::class.java)
            assertEquals(tree, actual)
        }

        @Test
        fun when_getTreeWithNodeIdSmallerThanZero_then_badRequestResponse() {
            //GIVEN
            `when`(mockDbService.getSubTree(anyInt())).thenAnswer { i -> Tree(i.getArgument(0), emptyMap()) }

            //WHEN + THEN
            mockMvcGetTreeExpectResponse(-1) { isBadRequest() }
        }

        @Test
        fun when_getTreeWithNodeIdNotInt_then_badRequestResponse() {
            //GIVEN
            `when`(mockDbService.getSubTree(anyInt())).thenAnswer { i -> Tree(i.getArgument(0), emptyMap()) }

            //WHEN + THEN
            mockMvcGetTreeExpectResponse("abc") { isBadRequest() }
        }

        private fun mockMvcGetTreeExpectResponse(
            nodeId: Any,
            statusMatcher: StatusResultMatchersDsl.() -> Unit
        ): ResultActionsDsl {
            return mockMvc.get("/tree/${nodeId}")
                .andExpect { status(statusMatcher) }
        }
    }

    @Nested
    inner class PostEdgeTest {
        @Test
        fun when_postEdgeWithValidBody_then_okResponseAndAddEdge() {
            //GIVEN
            val sourceId = 2
            val targetId = 3

            //WHEN + THEN
            mockMvcPostEdgeExpectResponse(Edge(sourceId, targetId)) { isOk() }
            verify(mockDbService).addEdge(sourceId, targetId)
        }

        @Test
        fun when_dbServiceThrowsDuplicateKeyExceptionOnPostEdge_then_conflictResponse() {
            //GIVEN
            val sourceId = 2
            val targetId = 3
            `when`(mockDbService.addEdge(sourceId, targetId)).thenThrow(DuplicateKeyException::class.java)

            //WHEN + THEN
            mockMvcPostEdgeExpectResponse(Edge(sourceId, targetId)) { isConflict() }
        }

        @Test
        fun when_postEdgeWithNullSourceId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcPostEdgeExpectResponse(Edge(null, 3)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_postEdgeWithNegativeSourceId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcPostEdgeExpectResponse(Edge(1, -3)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_postEdgeWithNullTargetId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcPostEdgeExpectResponse(Edge(1, null)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_postEdgeWithNegativeTargetId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcPostEdgeExpectResponse(Edge(1, -2)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_postEdgeWithNullBody_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcPostEdgeExpectResponse(null) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        private fun mockMvcPostEdgeExpectResponse(
            edge: Any?,
            statusMatcher: StatusResultMatchersDsl.() -> Unit
        ): ResultActionsDsl {
            return mockMvc.post("/tree/edges") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(edge)
            }
                .andExpect { status(statusMatcher) }
        }
    }

    @Nested
    inner class DeleteEdgeTest {
        @Test
        fun when_deleteEdgeWithValidBody_then_okResponseAndDeleteEdge() {
            //GIVEN
            val sourceId = 2
            val targetId = 3

            //WHEN + THEN
            mockMvcDeleteEdgeExpectResponse(Edge(sourceId, targetId)) { isOk() }
            verify(mockDbService).deleteEdge(sourceId, targetId)
        }

        @Test
        fun when_dbServiceThrowsEmptyResultDataAccessExceptionOnDeleteEdge_then_notFoundResponse() {
            //GIVEN
            val sourceId = 2
            val targetId = 3
            `when`(mockDbService.deleteEdge(sourceId, targetId)).thenThrow(EmptyResultDataAccessException::class.java)

            //WHEN + THEN
            mockMvcDeleteEdgeExpectResponse(Edge(sourceId, targetId)) { isNotFound() }
        }

        @Test
        fun when_deleteEdgeWithNullSourceId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcDeleteEdgeExpectResponse(Edge(null, 3)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_deleteEdgeWithNegativeSourceId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcDeleteEdgeExpectResponse(Edge(1, -3)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_deleteEdgeWithNullTargetId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcDeleteEdgeExpectResponse(Edge(1, null)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_deleteEdgeWithNegativeTargetId_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcDeleteEdgeExpectResponse(Edge(1, -2)) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        @Test
        fun when_deleteEdgeWithNullBody_then_badRequestResponse() {
            //WHEN + THEN
            mockMvcDeleteEdgeExpectResponse(null) { isBadRequest() }
            verifyNoMoreInteractionsOnDbService()
        }

        private fun mockMvcDeleteEdgeExpectResponse(
            edge: Any?,
            statusMatcher: StatusResultMatchersDsl.() -> Unit
        ): ResultActionsDsl {
            return mockMvc.delete("/tree/edges") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(edge)
            }
                .andExpect { status(statusMatcher) }
        }
    }

    private fun verifyNoMoreInteractionsOnDbService() {
        verifyNoMoreInteractions(this.mockDbService)
    }
}

