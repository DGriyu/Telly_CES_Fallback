package com.example.telly_ces_fallback.model.knowledge_graph

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class KnowledgeGraphResponse(
    @SerialName("result")
    val result: KnowledgeGraphResult
)

@Serializable
data class KnowledgeGraphResult(
    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("image")
    val image: KnowledgeGraphImage? = null,

    @SerialName("detailedDescription")
    val detailedDescription: KnowledgeGraphDetailedDescription? = null,

    @SerialName("url")
    val url: String? = null
)

@Serializable
data class KnowledgeGraphImage(
    @SerialName("contentUrl")
    val contentUrl: String? = null
)

@Serializable
data class KnowledgeGraphDetailedDescription(
    @SerialName("articleBody")
    val articleBody: String? = null
)