package com.example.prewave.tree

import com.example.prewave.tree.db.jooq.tables.references.EDGE
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.core.io.ClassPathResource
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import kotlin.test.BeforeTest
import kotlin.test.assertEquals


/**
 * @author LinX
 */
@JooqTest
@Import(TreeDbService::class)
class TreeDbServiceTest(
    @Autowired val dslContext: DSLContext,
    @Autowired val sut: TreeDbService
) {
    @BeforeTest
    fun setup() {
        createTable()
    }

    private fun createTable() {
        val create =
            InputStreamReader(ClassPathResource("db/CreateTable.sql").inputStream, Charsets.UTF_8).use { r ->
                FileCopyUtils.copyToString(r)
            }
        dslContext.connection { c ->
            c.createStatement().use { s ->
                s.execute(create)
            }
        }
    }

    @Test
    fun when_addUnknownEdge_then_entryAddedToDb() {
        //GIVEN
        val sourceId = 1
        val targetId = 2

        //WHEN
        this.sut.addEdge(sourceId, targetId)

        //THEN
        verifyEntryExists(sourceId, targetId)
    }

    @Test
    fun when_addMultipleEdges_then_entriesAddedToDb() {
        //WHEN
        this.sut.addEdge(1, 2)
        this.sut.addEdge(2, 3)
        this.sut.addEdge(1, 4)

        //THEN
        verifyEntryExists(1, 2)
        verifyEntryExists(2, 3)
        verifyEntryExists(1, 4)
    }

    @Test
    fun when_addKnownEdge_then_throwsDuplicateKeyException() {
        //GIVEN
        val sourceId = 1
        val targetId = 2
        this.sut.addEdge(sourceId, targetId)

        //WHEN + THEN
        assertThrows<DuplicateKeyException> { this.sut.addEdge(sourceId, targetId) }
        verifyEntryExists(sourceId, targetId)
    }

    @Test
    fun when_deleteKnownEdge_then_entryDeletedFromDb() {
        //GIVEN
        val sourceId = 1
        val targetId = 2
        this.sut.addEdge(sourceId, targetId)

        //WHEN + THEN
        this.sut.deleteEdge(sourceId, targetId)
        verifyEntryDoesNotExist(sourceId, targetId)
    }

    @Test
    fun when_deleteUnknownEdge_then_throwsEmptyResultDataAccessException() {
        //GIVEN
        this.sut.addEdge(1, 2)

        //WHEN + THEN
        assertThrows<EmptyResultDataAccessException> { this.sut.deleteEdge(2, 3) }
        verifyEntryExists(1, 2)
    }

    @Test
    fun when_getTreeForLeafNode_then_returnsEmptyTree() {
        //GIVEN
        this.sut.addEdge(1, 2)
        this.sut.addEdge(2, 3)
        this.sut.addEdge(2, 4)

        //WHEN
        val actual: Tree = this.sut.getSubTree(3)

        //THEN
        assertEquals(Tree(3, emptyMap()), actual)
    }

    @Test
    fun when_getTreeForRootNode_then_returnsWholeTree() {
        //GIVEN
        this.sut.addEdge(1, 2)
        this.sut.addEdge(2, 3)
        this.sut.addEdge(2, 4)

        //WHEN
        val actual: Tree = this.sut.getSubTree(1)

        //THEN
        assertEquals(Tree(1, mapOf(1 to setOf(2), 2 to setOf(3, 4))), actual)
    }

    @Test
    fun when_getTreeForBranchNode_then_returnsNonEmptySubTree() {
        //GIVEN
        this.sut.addEdge(1, 2)
        this.sut.addEdge(2, 3)
        this.sut.addEdge(2, 4)

        //WHEN
        val actual: Tree = this.sut.getSubTree(2)

        //THEN
        assertEquals(Tree(2, mapOf(2 to setOf(3, 4))), actual)
    }

    private fun verifyEntryExists(sourceId: Int, targetId: Int) {
        val result = selectEntry(sourceId, targetId)
        assertEquals(1, result.size)
    }

    private fun verifyEntryDoesNotExist(sourceId: Int, targetId: Int) {
        val result = selectEntry(sourceId, targetId)
        assertEquals(0, result.size)
    }

    private fun selectEntry(sourceId: Int, targetId: Int): List<DbEdge> {
        return this.dslContext
            .selectFrom(EDGE)
            .where(EDGE.FROM_ID.eq(sourceId))
            .and(EDGE.TO_ID.eq(targetId))
            .fetchInto(DbEdge::class.java)
    }
}
