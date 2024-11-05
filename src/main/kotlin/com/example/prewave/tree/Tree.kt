package com.example.prewave.tree

/**
 * @author LinX
 */
data class Tree(
    val rootNode: Int,
    val sourceNodes: Map<Int, Set<Int>>
)
