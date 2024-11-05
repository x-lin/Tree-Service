package com.example.prewave.tree

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest


/**
 * @author LinX
 */
@RestController
@RequestMapping("tree")
class TreeController(@Autowired private val dbService: TreeDbService) {
    private val logger: org.slf4j.Logger = LoggerFactory.getLogger(TreeController::class.java)

    @GetMapping(
        "/{nodeId}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getTreeByNodeId(@Valid @Min(0, message = "nodeId must be at least 0") @PathVariable nodeId: Int): Tree {
        this.logger.info("Received request to fetch subtree with root id {}.", nodeId)
        return this.dbService.getSubTree(nodeId);
    }

    @PostMapping(
        "/edges",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createEdge(@Valid @RequestBody edge: Edge) {
        this.logger.info(
            "Received request to create edge with source id {} and target id {}.",
            edge.fromId,
            edge.toId
        )
        this.dbService.addEdge(edge.fromId, edge.toId)
    }

    @DeleteMapping(
        "/edges",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun deleteEdge(@Valid @RequestBody edge: Edge) {
        this.logger.info(
            "Received request to delete edge with source id {} and target id {}.",
            edge.fromId,
            edge.toId
        )
        this.dbService.deleteEdge(edge.fromId, edge.toId)
    }

    @ExceptionHandler(DataIntegrityViolationException::class, ConstraintViolationException::class)
    fun handleDataIntegrityException(
        e: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        this.logger.info("Failed request with {}", e.message)
        return ResponseEntity.badRequest().body(ErrorResponse("Invalid request parameters and/or body."))
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(
        e: DuplicateKeyException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        this.logger.info("Failed request with {}", e.message)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse("Entry already exists."))
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handleEmptyResultException(
        e: EmptyResultDataAccessException,
        request: WebRequest
    ): ResponseEntity<Any> {
        this.logger.info("Failed request with {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse("Entry does not exist."))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidArgumentException(
        e: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<Any> {
        this.logger.info("Failed request with {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("Invalid parameter(s)."))
    }
}

data class ErrorResponse(val reason: String)

data class Edge(
    @field:NotNull(message = "sourceId must not be null")
    @field:Min(0, message = "sourceId must be at least 0")
    @JsonProperty("sourceId")
    val sourceIdOrNull: Int?,
    @field:NotNull(message = "targetId must not be null")
    @field:Min(0, message = "targetId must be at least 0")
    @JsonProperty("targetId")
    val targetIdOrNull: Int?,
) {
    val fromId: Int = sourceIdOrNull ?: 0
    val toId: Int = targetIdOrNull ?: 0
}
