package com.example.prewave.tree

import com.example.prewave.tree.db.jooq.tables.Edge.Companion.EDGE
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service

/**
 * @author LinX
 */
@Service
class TreeDbService(@Autowired private val dslContext: DSLContext) {
    @Throws(DuplicateKeyException::class)
    fun addEdge(sourceNodeId: Int, targetNodeId: Int) {
        val result: Int = dslContext
            .insertInto(EDGE, EDGE.FROM_ID, EDGE.TO_ID)
            .values(sourceNodeId, targetNodeId)
            .onConflictDoNothing()
            .execute()
        if (result == 0) {
            throw DuplicateKeyException(
                "Edge with source $sourceNodeId and target $targetNodeId already exists."
            )
        }
    }

    @Throws(EmptyResultDataAccessException::class)
    fun deleteEdge(sourceNodeId: Int, targetNodeId: Int) {
        val result: Int = dslContext
            .deleteFrom(EDGE)
            .where(EDGE.FROM_ID.eq(sourceNodeId)).and(EDGE.TO_ID.eq(targetNodeId))
            .execute()
        if (result == 0) {
            throw EmptyResultDataAccessException(
                "Edge with source $sourceNodeId and target $targetNodeId does not exist.",
                1
            )
        }
    }

    fun getSubTree(rootNodeId: Int): Tree {
        val edges: List<DbEdge> = selectSubtree(rootNodeId)
        return convertToTree(rootNodeId, edges)
    }

    private fun selectSubtree(rootNodeId: Int): List<DbEdge> {
        val cteName = "subtree"
        val edges: List<DbEdge> = dslContext //
            .withRecursive(cteName)
            .`as`(
                dslContext
                    .select(EDGE.FROM_ID, EDGE.TO_ID)
                    .from(EDGE)
                    .where(EDGE.FROM_ID.eq(rootNodeId))
                    .union(
                        dslContext
                            .select(EDGE.FROM_ID, EDGE.TO_ID)
                            .from(EDGE)
                            .innerJoin(cteName)
                            .on(EDGE.FROM_ID.eq(field("$cteName.to_id", Int::class.java)))
                    )
            )
            .select()
            .from(cteName)
            .fetchInto(DbEdge::class.java)
        return edges
    }

    private fun convertToTree(rootNodeId: Int, edges: List<DbEdge>): Tree {
        val sourceNodes: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
        edges.forEach { edge -> sourceNodes.getOrPut(edge.fromId) { mutableSetOf() }.add(edge.toId) }

        //For a performance improvement, we could already serialize Tree as JSON string here and return the serialized string.
        //This saves the step of converting the mutable map/set to an immutable version.
        return Tree(
            rootNodeId,
            sourceNodes.mapValues { (_, v) -> v.toSet() }.toMap()
        )
    }
}

private data class DbEdge(val fromId: Int, val toId: Int)
